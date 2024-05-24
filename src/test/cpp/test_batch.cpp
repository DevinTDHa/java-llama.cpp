#include "batch_inference.h"
#include "jllama.h" // Include the file that contains the function to test

int main(int argc, char **argv) {
  if (argc == 1 || argv[1][0] == '-') {
    printf("usage: %s MODEL_PATH\n", argv[0]);
    return 1;
  }

  gpt_params params;
  if (argc >= 2) {
    params.model = argv[1];
  }

  // batch of prompts with reference completions
  std::vector<std::string> prompts = {
      "The moons of Jupiter are ", // "The moons of Jupiter are 77 in total,
                                   // with 79 confirmed natural satellites and 2
                                   // man-made ones. The four"
      "Earth is ", // "Earth is 4.5 billion years old. It has been home to
                   // countless species, some of which have gone extinct, while
                   // others have evolved into"
      "The moon is ", // "The moon is 1/400th the size of the sun. The sun
                      // is 1.39 million kilometers in diameter, while"
  };

  // init LLM
  llama_backend_init();
  llama_numa_init(params.numa);

  // initialize the model
  llama_model_params model_params = llama_model_default_params();

  model_params.n_gpu_layers = 99; // offload all layers to the GPU
  llama_model *model =
      llama_load_model_from_file(params.model.c_str(), model_params);

  if (model == nullptr) {
    fprintf(stderr, "%s: error: unable to load model\n", __func__);
    return 1;
  }

  // initialize the context
  llama_context_params ctx_params = llama_context_default_params();

  // Context setup
  ctx_params.seed = 1234;
  ctx_params.n_ctx = 2048; // text context, 0 = from model, size of the KV cache
  ctx_params.n_batch =
      512; // logical maximum batch size that can be submitted to llama_decode
  ctx_params.n_threads = params.n_threads;
  ctx_params.n_threads_batch =
      params.n_threads_batch == -1 ? params.n_threads : params.n_threads_batch;

  llama_context *ctx = llama_new_context_with_model(model, ctx_params);

  if (ctx == nullptr) {
    fprintf(stderr, "%s: error: failed to create the llama_context\n",
            __func__);
    return 1;
  }

  int max_batch_tokens = 2048;
  int max_len = 200;
  std::vector<std::string> generatedPrompts =
      batch_complete(model, ctx, prompts, max_batch_tokens, max_len);

  for (const auto &prompt : generatedPrompts) {
    printf("%s\n", prompt.c_str());
  }

}