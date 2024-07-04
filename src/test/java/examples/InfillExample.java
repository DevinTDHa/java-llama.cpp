package examples;

import com.johnsnowlabs.nlp.llama.InferenceParameters;
import com.johnsnowlabs.nlp.llama.LlamaModel;
import com.johnsnowlabs.nlp.llama.LlamaOutput;
import com.johnsnowlabs.nlp.llama.ModelParameters;

public class InfillExample {

	public static void main(String... args) {
		ModelParameters modelParams = new ModelParameters()
				.setModelFilePath("models/codellama-7b.Q2_K.gguf")
				.setNGpuLayers(43);

		String prefix = "def remove_non_ascii(s: str) -> str:\n    \"\"\" ";
		String suffix = "\n    return result\n";
		try (LlamaModel model = new LlamaModel(modelParams)) {
			System.out.print(prefix);
			InferenceParameters inferParams = new InferenceParameters("")
					.setInputPrefix(prefix)
					.setInputSuffix(suffix);
			for (LlamaOutput output : model.generate(inferParams)) {
				System.out.print(output);
			}
			System.out.print(suffix);
		}
	}
}
