package de.kherud.llama;

import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class LlamaModelBatchIT {
    private static final int nPredict = 200;
    private static final int maxBatchSize = 2048;
    private static final int seed = 1234;
    private static final int n_ctx = 2048;
    private static LlamaModel model;
    InferenceParameters params = new InferenceParameters().setTemperature(0.4f).setNPredict(nPredict).setSeed(seed).setTopK(40).setTopP(0.9f);

    @BeforeClass
    public static void setup() {
        LlamaModel.setLogger((level, msg) -> System.out.println(level + ": " + msg));
        ModelParameters params = new ModelParameters().setNGpuLayers(43).setEmbedding(false).setNBbatch(maxBatchSize).setSeed(seed).setNCtx(n_ctx);
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

        String[] prompts = new String[]{"The moons of Jupiter are ", // "The moons of Jupiter are 77 in total,
                // with 79 confirmed natural satellites and 2
                // man-made ones. The four"
                "Earth is ", // "Earth is 4.5 billion years old. It has been home to
                // countless species, some of which have gone extinct, while
                // others have evolved into"
                "The moon is ", // "The moon is 1/400th the size of the sun. The sun
                // is 1.39 million kilometers in diameter, while"

        };

        String[] output = model.batchComplete(prompts, params);
        Assert.assertNotEquals(0, output.length);
        Assert.assertEquals(prompts.length, output.length);

        //print contents
        for (String s : output) {
            System.out.println(s);
        }
    }

    @Test
    public void benchmarkBatch() {
        // Benchmarking

        String[] prompts = new String[]{"The moons of Jupiter are ", // "The moons of Jupiter are 77 in total,
                // with 79 confirmed natural satellites and 2
                // man-made ones. The four"
                "Earth is ", // "Earth is 4.5 billion years old. It has been home to
                // countless species, some of which have gone extinct, while
                // others have evolved into"
                "The moon is ", // "The moon is 1/400th the size of the sun. The sun
                // is 1.39 million kilometers in diameter, while"

        };

        // Preimplemented Serial Execution
        long start = System.currentTimeMillis();
        for (String prompt : prompts) {
            model.complete(prompt, params);
        }
        long end = System.currentTimeMillis();
        System.out.println("Serial Execution: " + (end - start) + "ms");

        // Batch Execution
        start = System.currentTimeMillis();
        model.batchComplete(prompts, params);
        end = System.currentTimeMillis();
        System.out.println("Batch Execution: " + (end - start) + "ms");

    }
}
