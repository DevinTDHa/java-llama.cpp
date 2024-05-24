#include "batch_inference.h"
#include "common.h"
#include "llama.h"

#include <cmath>
#include <cstdio>
#include <string>
#include <vector>

/** Tokenize the provided batch of prompts.
 *
 * @param ctx llama context of the model
 * @param prompts vector of strings representing the prompts
 * @return vector of vectors representing a sequence of tokens ids
 */
std::vector<std::vector<llama_token>>
tokenizePrompts(const llama_context *ctx, std::vector<std::string> &prompts) {
  std::vector<std::vector<llama_token>> batch_tokens(prompts.size());

  // Tokenize all prompts
  std::transform(
      prompts.begin(), prompts.end(), batch_tokens.begin(),
      [&ctx](const std::string &prompt) { // provide ctx to the lambda
        return llama_tokenize(ctx, prompt, true);
      });
  return batch_tokens;
}

/** Fill the batch with the tokens from the provided batch of tokens.
 *
 * We request the logits for the last tokens of each sequence, which will be
 * filled when we call decode.
 *
 * @param batch llama batch to fill
 * @param batch_tokens vector of vectors representing a sequence of tokens
 * @param batch_size number of sequences in the batch
 * @return the filled batch
 */
void fill_batch(llama_batch &batch,
                const std::vector<std::vector<llama_token>> &batch_tokens) {
  const int n_sequences = static_cast<int>(batch_tokens.size());

  for (int b = 0; b < n_sequences; b++) {
    const auto seq_length = (llama_pos)batch_tokens[b].size();
    for (llama_pos tok = 0; tok < seq_length;
         tok++) { // TODO Check data type ok?
      llama_batch_add(batch, batch_tokens[b][tok], tok, {b},
                      tok ==
                          seq_length - 1); // If last token, we need the logits
    }
  }
}

/** Decodes batches of ctx_params.n_batch tokens.
 *
 * @param ctx llama context of the model
 * @param batch llama batch to decode, containing the tokens of all batches.
 * @param n_batch logical maximum batch size that can be submitted to
 * llama_decode
 * @return true if successful, false otherwise
 */
bool decode_batches(llama_context *ctx, llama_batch &batch, int32_t n_batch) {
  for (int32_t i = 0; i < batch.n_tokens; i += n_batch) {
    const int32_t n_tokens = std::min(n_batch, (batch.n_tokens - i));

    llama_batch batch_view = {
        n_tokens,
        batch.token + i,
        nullptr,
        batch.pos + i,
        batch.n_seq_id + i,
        batch.seq_id + i,
        batch.logits + i,
        0,
        0,
        0, // unused
    };

    const int ret = llama_decode(ctx, batch_view);
    if (ret != 0) {
      LOG_TEE("failed to decode the batch, n_batch = %d, ret = %d\n", n_batch,
              ret);
      return false;
    }

    llama_synchronize(ctx);
  }

  return true;
}

bool generation_finished(const std::vector<int32_t> &i_batch) {
  return std::all_of(i_batch.begin(), i_batch.end(),
                     [](int i) { return i < 0; });
}

/**
 * This function is used to sample and add tokens to the batch for each
 * sequence. It uses the sampling context to sample a new token for each
 * sequence and adds it to the batch. If the end of the sequence is reached or
 * the maximum length is exceeded, the sequence is marked as finished.
 *
 * @param n_sequences: The number of sequences to process.
 * @param max_len: The maximum length of a sequence.
 * @param model: Pointer to the llama model.
 * @param ctx: Pointer to the main llama context.
 * @param generated_results: Vector of strings where the generated results for
 * each sequence are stored.
 * @param n_cur: Vector of integers representing the current length of each
 * sequence.
 * @param batch: The batch where the new tokens are added.
 * @param i_batch: Vector of integers representing the index in the batch for
 * each sequence.
 * @param n_decode: Reference to an integer representing the total number of
 * decoded tokens.
 * @param ctx_sampling: Pointer to the sampling context used for sampling new
 * tokens.
 * @param ctx_cfg: Pointer to the context used for classifier-free guidance.
 */
void sample_and_add(const int max_len, const llama_model *model,
                    llama_context *ctx,
                    std::vector<std::string> &generated_results,
                    std::vector<int> &n_cur, llama_batch &batch,
                    std::vector<int32_t> &i_batch,
                    llama_sampling_context *ctx_sampling,
                    llama_context *ctx_cfg = nullptr) {
  const int n_sequences = (int)i_batch.size();
  for (int32_t i = 0; i < n_sequences; ++i) {
    if (i_batch[i] < 0) {
      // the stream has already finished
      continue;
    }

    const llama_token new_token_id =
        llama_sampling_sample(ctx_sampling, ctx, ctx_cfg, i_batch[i]);

    // const llama_token new_token_id = llama_sample_token_greedy(ctx,
    // &candidates_p);

    // is it an end of stream? -> mark the stream as finished
    if (new_token_id == llama_token_eos(model) || n_cur[i] == max_len) {
      i_batch[i] = -1;
      LOG_TEE("\n");
      if (n_sequences > 1) {
        LOG_TEE("%s: stream %d finished at n_cur[%d] = %d\n", __func__, i, i,
                n_cur[i]);
      }

      continue;
    }

    generated_results[i] += llama_token_to_piece(ctx, new_token_id);

    // Log generated token
    LOG_TEE("Seq %d generated %s\n", i, generated_results[i].c_str());

    i_batch[i] = batch.n_tokens;
    n_cur[i] += 1;

    // push this new token for next evaluation
    llama_batch_add(batch, new_token_id, n_cur[i], {i}, true);
  }
}

int get_n_kv_req(const int max_len,
                 const std::vector<std::vector<llama_token>> &batch_tokens) {
  int n_kv_req = 0;

  // calculate the required cache for each sequence in the batch
  for (const auto &tokens_list : batch_tokens) {
    n_kv_req += (int)(max_len - tokens_list.size());
  }
  return n_kv_req;
}

/**
 * Performs batch inference using the given Llama model, context, and
 * parameters.
 *
 * TODO: Truncation and other prompt preprocessing?
 *
 * @param model The Llama model to use for inference.
 * @param ctx The Llama context to use for inference.
 * @param prompts The vector of prompts for batch inference.
 * @param max_batch_tokens maximum number of tokens the batch can hold.
 * @param max_len The maximum length of the generated sequences.
 * @return A pointer to a vector of strings containing the generated results.
 * @throws std::runtime_error if the decoding process fails.
 */
std::vector<std::string> *batch_complete(llama_model *model, llama_context *ctx,
                                         std::vector<std::string> prompts,
                                         int max_batch_tokens, int max_len) {
  gpt_params params;

  // batch of prompts
  // std::vector<std::string> prompts = {"The moons of Jupiter are ", "Earth is
  // ",
  //                                     "The moon is ", //"The solar system
  //                                     consists of ",
  // };
  const int n_sequences = (int)prompts.size();

  // init LLM TODO: Might be done somewhere else?
  // llama_backend_init();
  // llama_numa_init(params.numa);
  // initialize the model
  // llama_model_params model_params = llama_model_default_params();
  // model_params.n_gpu_layers = 99; // offload all layers to the GPU

  // llama_model *model = llama_load_model_from_file(params.model.c_str(),
  // model_params);

  // TODO: Might move this somewhere else during model init
  // if (model == nullptr) {
  //     fprintf(stderr, "%s: error: unable to load model\n", __func__);
  //     return 1;
  // }

  // initialize the context
  // llama_context_params ctx_params = llama_context_default_params();

  // Context setup
  // ctx_params.seed = 1234;
  // ctx_params.n_ctx = 2048; // text context, 0 = from model, size of the KV
  // cache ctx_params.n_batch = 512; // logical maximum batch size that can be
  // submitted to llama_decode ctx_params.n_threads = params.n_threads;
  // ctx_params.n_threads_batch = params.n_threads_batch == -1 ?
  // params.n_threads : params.n_threads_batch;

  // llama_context *ctx = llama_new_context_with_model(model, ctx_params);

  // if (ctx == nullptr) {
  //     fprintf(stderr, "%s: error: failed to create the llama_context\n",
  //     __func__); return 1;
  // }

  // Create a vector of vectors to hold the result
  std::vector<std::vector<llama_token>> batch_tokens =
      tokenizePrompts(ctx, prompts);

  const int n_ctx = (int)llama_n_ctx(ctx);

  int n_kv_req = get_n_kv_req(max_len, batch_tokens);

  // LOG_TEE("\n%s: n_len = %d, n_ctx = %d, n_kv_req = %d\n", __func__, max_len,
  // n_ctx, n_kv_req);

  // make sure the KV cache is big enough to hold all the prompt and generated
  // tokens
  // TODO: How to handle this?
  // if (n_kv_req > n_ctx) {
  //     LOG_TEE("%s: error: n_kv_req > n_ctx, the required KV cache size is not
  //     big enough\n", __func__); LOG_TEE("%s:        either reduce n_len or
  //     increase n_ctx\n", __func__); return 1;
  // }

  // we use this object to submit token data for decoding
  // int max_batch_tokens = (int) n_batch; // maximum number of tokens the batch
  // can hold. TODO: Calculation? Max number of generated sequences per prompt
  // (i.e. distinct states for recurrent models) Future Work: Support
  // multi-sequence generation at some point.
  int max_sequences = 1;
  llama_batch batch = llama_batch_init(max_batch_tokens, 0, max_sequences);
  fill_batch(batch, batch_tokens);

  bool decode_result = decode_batches(ctx, batch, (int32_t)max_batch_tokens);
  if (!decode_result) {
    throw std::runtime_error("decode_batches() failed");
  }

  // main loop
  // we will store the parallel decoded sequences in this vector
  std::vector<std::string> generated_results(n_sequences);

  // remember the batch index of the last token for each parallel sequence
  // we need this to determine which logits to sample from
  // This needs to be adjusted for different batch lengths.
  // It should contain the index of the last token of each sequence.
  std::vector<int32_t> i_batch(n_sequences, -1);
  {
    int total_tokens = 0;
    std::transform(batch_tokens.begin(), batch_tokens.end(), i_batch.begin(),
                   [&total_tokens](const std::vector<llama_token> &tokens) {
                     total_tokens = total_tokens + (int32_t)tokens.size();
                     return total_tokens - 1;
                   });
  }

  // Keep track of indices of each sequence in the batch
  std::vector<int> n_cur = i_batch;
  // int n_decode = 0; // Only For benchmarking

  // initialize the sampling context
  llama_sampling_params sampling_params = params.sparams;
  const int top_k = 40;
  const float top_p = 0.9f;
  const float temp = 0.4f;

  sampling_params.top_k = top_k;
  sampling_params.top_p = top_p;
  sampling_params.temp = temp;

  llama_sampling_context *ctx_sampling = llama_sampling_init(sampling_params);
  llama_context *ctx_cfg = nullptr; // Optional classifier-free guidance context

  const auto t_main_start = ggml_time_us();

  // While there are still sequences to decode
  while (!generation_finished(i_batch)) {
    // prepare the next batch
    llama_batch_clear(batch);

    // sample the next token for each parallel sequence / stream
    sample_and_add(max_len, model, ctx, generated_results, n_cur, batch,
                   i_batch, ctx_sampling, ctx_cfg);

    // all streams are finished, no new tokens were added
    if (batch.n_tokens == 0) {
      break;
    }

    // evaluate the current batch with the transformer model
    if (!decode_batches(ctx, batch, (int32_t)max_batch_tokens)) {
      fprintf(stderr, "%s : failed to eval, return code %d\n", __func__, 1);
      return nullptr;
    }
  }

  // free resources. TODO: When for the others?
  llama_batch_free(batch);
  // llama_free(ctx);
  // llama_free_model(model);
  // llama_backend_free();

  return &generated_results;
}
