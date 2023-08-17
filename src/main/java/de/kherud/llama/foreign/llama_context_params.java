package de.kherud.llama.foreign;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.FloatByReference;
import de.kherud.llama.foreign.LlamaLibrary.llama_progress_callback;
import java.util.Arrays;
import java.util.List;
/**
 * This file was autogenerated by <a href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few opensource projects.</a>.<br>
 * For help, please visit <a href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a href="http://rococoa.dev.java.net/">Rococoa</a>, or <a href="http://jna.dev.java.net/">JNA</a>.
 */
public class llama_context_params extends Structure {
	/** RNG seed, -1 for random */
	public int seed;
	public int getSeed() {
		return seed;
	}
	public void setSeed(int seed) {
		this.seed = seed;
	}
	/** text context */
	public int n_ctx;
	public int getN_ctx() {
		return n_ctx;
	}
	public void setN_ctx(int n_ctx) {
		this.n_ctx = n_ctx;
	}
	/** prompt processing batch size */
	public int n_batch;
	public int getN_batch() {
		return n_batch;
	}
	public void setN_batch(int n_batch) {
		this.n_batch = n_batch;
	}
	/** grouped-query attention (TEMP - will be moved to model hparams) */
	public int n_gqa;
	public int getN_gqa() {
		return n_gqa;
	}
	public void setN_gqa(int n_gqa) {
		this.n_gqa = n_gqa;
	}
	/** rms norm epsilon (TEMP - will be moved to model hparams) */
	public float rms_norm_eps;
	public float getRms_norm_eps() {
		return rms_norm_eps;
	}
	public void setRms_norm_eps(float rms_norm_eps) {
		this.rms_norm_eps = rms_norm_eps;
	}
	/** number of layers to store in VRAM */
	public int n_gpu_layers;
	public int getN_gpu_layers() {
		return n_gpu_layers;
	}
	public void setN_gpu_layers(int n_gpu_layers) {
		this.n_gpu_layers = n_gpu_layers;
	}
	/** the GPU that is used for scratch and small tensors */
	public int main_gpu;
	public int getMain_gpu() {
		return main_gpu;
	}
	public void setMain_gpu(int main_gpu) {
		this.main_gpu = main_gpu;
	}
	/** how to split layers across multiple GPUs (size: LLAMA_MAX_DEVICES) */
	public FloatByReference tensor_split;
	public FloatByReference getTensor_split() {
		return tensor_split;
	}
	public void setTensor_split(FloatByReference tensor_split) {
		this.tensor_split = tensor_split;
	}
	/** RoPE base frequency */
	public float rope_freq_base;
	public float getRope_freq_base() {
		return rope_freq_base;
	}
	public void setRope_freq_base(float rope_freq_base) {
		this.rope_freq_base = rope_freq_base;
	}
	/** RoPE frequency scaling factor */
	public float rope_freq_scale;
	public float getRope_freq_scale() {
		return rope_freq_scale;
	}
	public void setRope_freq_scale(float rope_freq_scale) {
		this.rope_freq_scale = rope_freq_scale;
	}
	public llama_progress_callback progress_callback;
	public llama_progress_callback getProgress_callback() {
		return progress_callback;
	}
	public void setProgress_callback(llama_progress_callback progress_callback) {
		this.progress_callback = progress_callback;
	}
	public Pointer progress_callback_user_data;
	public Pointer getProgress_callback_user_data() {
		return progress_callback_user_data;
	}
	public void setProgress_callback_user_data(Pointer progress_callback_user_data) {
		this.progress_callback_user_data = progress_callback_user_data;
	}
	/** if true, reduce VRAM usage at the cost of performance */
	public byte low_vram;
	public byte getLow_vram() {
		return low_vram;
	}
	public void setLow_vram(byte low_vram) {
		this.low_vram = low_vram;
	}
	/** if true, use experimental mul_mat_q kernels */
	public byte mul_mat_q;
	public byte getMul_mat_q() {
		return mul_mat_q;
	}
	public void setMul_mat_q(byte mul_mat_q) {
		this.mul_mat_q = mul_mat_q;
	}
	/** use fp16 for KV cache */
	public byte f16_kv;
	public byte getF16_kv() {
		return f16_kv;
	}
	public void setF16_kv(byte f16_kv) {
		this.f16_kv = f16_kv;
	}
	/** the llama_eval() call computes all logits, not just the last one */
	public byte logits_all;
	public byte getLogits_all() {
		return logits_all;
	}
	public void setLogits_all(byte logits_all) {
		this.logits_all = logits_all;
	}
	/** only load the vocabulary, no weights */
	public byte vocab_only;
	public byte getVocab_only() {
		return vocab_only;
	}
	public void setVocab_only(byte vocab_only) {
		this.vocab_only = vocab_only;
	}
	/** use mmap if possible */
	public byte use_mmap;
	public byte getUse_mmap() {
		return use_mmap;
	}
	public void setUse_mmap(byte use_mmap) {
		this.use_mmap = use_mmap;
	}
	/** force system to keep model in RAM */
	public byte use_mlock;
	public byte getUse_mlock() {
		return use_mlock;
	}
	public void setUse_mlock(byte use_mlock) {
		this.use_mlock = use_mlock;
	}
	/** embedding mode only */
	public byte embedding;
	public byte getEmbedding() {
		return embedding;
	}
	public void setEmbedding(byte embedding) {
		this.embedding = embedding;
	}
	public llama_context_params() {
		super();
	}
	@Override
	protected List<String> getFieldOrder() {
		return Arrays.asList("seed", "n_ctx", "n_batch", "n_gqa", "rms_norm_eps", "n_gpu_layers", "main_gpu", "tensor_split", "rope_freq_base", "rope_freq_scale", "progress_callback", "progress_callback_user_data", "low_vram", "mul_mat_q", "f16_kv", "logits_all", "vocab_only", "use_mmap", "use_mlock", "embedding");
	}
	public llama_context_params(Pointer peer) {
		super(peer);
	}
	public static class ByReference extends llama_context_params implements Structure.ByReference {

	}
	public static class ByValue extends llama_context_params implements Structure.ByValue {

	}
}
