package examples;

import com.johnsnowlabs.nlp.llama.LlamaOutput;
import com.johnsnowlabs.nlp.llama.ModelParameters;

import com.johnsnowlabs.nlp.llama.InferenceParameters;
import com.johnsnowlabs.nlp.llama.LlamaModel;

public class GrammarExample {

	public static void main(String... args) {
		String grammar = "root  ::= (expr \"=\" term \"\\n\")+\n" +
				"expr  ::= term ([-+*/] term)*\n" +
				"term  ::= [0-9]";
		ModelParameters modelParams = new ModelParameters()
				.setModelFilePath("models/mistral-7b-instruct-v0.2.Q2_K.gguf");
		InferenceParameters inferParams = new InferenceParameters("")
				.setGrammar(grammar);
		try (LlamaModel model = new LlamaModel(modelParams)) {
			for (LlamaOutput output : model.generate(inferParams)) {
				System.out.print(output);
			}
		}
	}

}
