package com.johnsnowlabs.nlp.llama;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LlamaModelBatchIT {
    private static final int nPredict = 100;
    private static final int maxBatchSize = 2048;
    private static final int seed = 1234;
    private static final int n_ctx = 2048;
    private static LlamaModel model;
    InferenceParameters inferenceParams = new InferenceParameters("").setTemperature(0.4f).setNPredict(nPredict).setSeed(seed).setTopK(40).setTopP(0.9f);

    String[] prompts = new String[]{"The moons of Jupiter are ", // "The moons of Jupiter are 77 in total,
            // with 79 confirmed natural satellites and 2
            // man-made ones. The four"
            "Earth is ", // "Earth is 4.5 billion years old. It has been home to
            // countless species, some of which have gone extinct, while
            // others have evolved into"
            "The moon is ", // "The moon is 1/400th the size of the sun. The sun
            // is 1.39 million kilometers in diameter, while"
            "The sun is "
    };

    static ModelParameters modelParams = new ModelParameters()
            .setNGpuLayers(43)
            .setEmbedding(false)
            .setNBatch(maxBatchSize)
            .setSeed(seed)
            .setNCtx(n_ctx)
            .setModelFilePath("models/codellama-7b.Q2_K.gguf")
            .setContinuousBatching(true);

    @BeforeClass
    public static void setup() {
        model = new LlamaModel(modelParams);
    }

    @AfterClass
    public static void tearDown() {
        if (model != null) {
            model.close();
        }
    }

    @Test
    public void testBatchCompleteAnswer() {
        System.out.println("Model Parameters: " + modelParams.toString());
        System.out.println("Inference Parameters: " + inferenceParams.toString());
        String[] output = model.requestBatchCompletion(prompts, inferenceParams);
        Assert.assertNotEquals(0, output.length);
        Assert.assertEquals(prompts.length, output.length);

        //print contents
        for (String s : output) {
            System.out.println(s);
        }
    }

    @Test
    public void benchmarkBatchServer() {
        long start;
        long end;

        start = System.currentTimeMillis();
        for (String prompt : prompts) {
            inferenceParams.setPrompt(prompt);
            model.complete(inferenceParams);
        }
        end = System.currentTimeMillis();
        System.out.println("DHA | Sequential Server Execution: " + (end - start) + "ms");

        model.close();

        // Batch Complete
        start = System.currentTimeMillis();
        model = new LlamaModel(modelParams.setNParallel(4));
        model.requestBatchCompletion(prompts, inferenceParams);
        end = System.currentTimeMillis();
        System.out.println("DHA | Batch Execution: " + (end - start) + "ms");
    }
}
