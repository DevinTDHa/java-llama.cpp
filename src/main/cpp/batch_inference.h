//
// Created by ducha on 20/04/24.
//

#ifndef JLLAMA_INFERENCE_H
#define JLLAMA_INFERENCE_H

#endif // JLLAMA_INFERENCE_H

#include "jllama.h"
#include "llama.h"
#include "sampling.h"
#include <cmath>
#include <cstdio>
#include <string>
#include <vector>

std::vector<std::vector<llama_token>>
tokenizePrompts(const llama_context *ctx, std::vector<std::string> &prompts);

void fill_batch(llama_batch &batch,
                const std::vector<std::vector<llama_token>> &batch_tokens);

bool decode_batches(llama_context *ctx, llama_batch &batch, int32_t n_batch);

bool generation_finished(const std::vector<int32_t> &i_batch);

void sample_and_add(const int max_len, const llama_model *model,
                    llama_context *ctx,
                    std::vector<std::string> &generated_results,
                    std::vector<int> &n_cur, llama_batch &batch,
                    std::vector<int32_t> &i_batch,
                    llama_sampling_context *ctx_sampling,
                    llama_context *ctx_cfg);

int get_n_kv_req(int max_len,
                 const std::vector<std::vector<llama_token>> &batch_tokens);

std::vector<std::string> batch_complete(llama_model *model, llama_context *ctx,
                                        llama_sampling_context *ctx_sampling,
                                        std::vector<std::string> prompts,
                                        int max_batch_tokens, int max_len);
