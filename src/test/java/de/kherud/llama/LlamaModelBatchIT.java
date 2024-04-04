package de.kherud.llama;

import java.util.HashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LlamaModelBatchIT {
    private static final int nPredict = 10;
    private static LlamaModel model;

    @BeforeClass
    public static void setup() {
        LlamaModel.setLogger((level, msg) -> System.out.println(level + ": " + msg));
        ModelParameters params = new ModelParameters().setNGpuLayers(43).setEmbedding(false);
        model = new LlamaModel(ModelResolver.getPathToITModel(), params);
    }

    @AfterClass
    public static void tearDown() {
        if (model != null) {
            model.close();
        }
    }

    @Test
    public void testBatchCompleteAnswer() {
        InferenceParameters params = new InferenceParameters().setTemperature(0.95f).setNPredict(nPredict).setSeed(42);

        String[] prompts = new String[]{"Hello world, ", "Goodbye world, "};

        String[] output = model.batchComplete(prompts, params);
        Assert.assertNotEquals(0, output.length);

        //print contents
        for (String s : output) {
            System.out.println(s);
        }
    }

    @Test
    public void testBatchTokenization() {
        String prompt = "Hello, world!";
        int[] encoded = model.encode(prompt);
        String decoded = model.decode(encoded);
        // the llama tokenizer adds a space before the prompt
        Assert.assertEquals(" " + prompt, decoded);
    }

}
