# Spark NLP Java Bindings for [llama.cpp](https://github.com/ggerganov/llama.cpp)

## Publishing

### Publish locally

```shell
sbt publishLocal
```

### Publish on Maven

Standalone:

CPU

```shell
sbt +publishSigned 
sbt sonatypeRelease
```

GPU

```shell
sbt -Dis_gpu=true +publishSigned
sbt sonatypeRelease
```

M1

```shell
sbt -Dis_m1=true +publishSigned
sbt sonatypeRelease
```

aarch64 - ARM

```shell
sbt -Dis_aarch64=true +publishSigned
sbt sonatypeRelease
```

All at once:

```shell
sbt -mem 4096 clean && sbt +publishSigned && sbt -Dis_gpu=true +publishSigned && sbt -Dis_m1=true +publishSigned && sbt -Dis_aarch64=true +publishSigned
sbt sonatypeRelease
```



## Custom Compilation

TODO: Just provide a Docker image. And move the section below.

> [!TIP]
> Use `-DLLAMA_CURL=ON` to download models via Java code using `ModelParameters#setModelUrl(String)`.

All required files will be put in a resources directory matching your platform, which will appear in the cmake output. For example something like:

```shell
--  Installing files to /java-llama.cpp/src/main/resources/com/johnsnowlabs/nlp/llama/Linux/x86_64
```

This includes:

- Linux: `libllama.so`, `libjllama.so`
- MacOS: `libllama.dylib`, `libjllama.dylib`, `ggml-metal.metal`
- Windows: `llama.dll`, `jllama.dll`

If you then compile your own JAR from this directory, you are ready to go. Otherwise, if you still want to use the library
as a Maven dependency, see below how to set the necessary paths in order for Java to find your compiled libraries.

### Custom llama.cpp Setup (GPU acceleration)

This repository provides default support for CPU based inference. You can compile `llama.cpp` any way you want, however (see [Setup Required](#setup-required)).
In order to use your self-compiled library, set either of the [JVM options](https://www.jetbrains.com/help/idea/tuning-the-ide.html#configure-jvm-options):

- `com.johnsnowlabs.nlp.llama.lib.path`, for example `-Dcom.johnsnowlabs.nlp.llama.lib.path=/directory/containing/lib`
- `java.library.path`, for example `-Djava.library.path=/directory/containing/lib`

This repository uses [`System#mapLibraryName`](https://docs.oracle.com/javase%2F7%2Fdocs%2Fapi%2F%2F/java/lang/System.html) to determine the name of the shared library for you platform.
If for any reason your library has a different name, you can set it with

- `com.johnsnowlabs.nlp.llama.lib.name`, for example `-Dcom.johnsnowlabs.nlp.llama.lib.name=myname.so`

For compiling `llama.cpp`, refer to the official [readme](https://github.com/ggerganov/llama.cpp#build) for details.
The library can be built with the `llama.cpp` project:

```shell
sbt -Dis_gpu=true compile
```

Example for CUDA Support:

```shell
mvn compile && mkdir -p build && cd build && cmake .. -DLLAMA_CUDA=ON && cmake --build . --config Debug && cd .. && mvn package
```

Look for the shared library in `build`.

> [!IMPORTANT]
> If you are running MacOS with Metal, you have to put the file `ggml-metal.metal` from `build/bin` in the same directory as the shared library.

### Debug Build

To build llama.cpp with debug symbols, use the following commands:

```shell
mvn compile && mkdir -p build && cd build && cmake .. -DLLAMA_CUDA=ON -DLLAMA_DEBUG=1 -DCMAKE_BUILD_TYPE=Debug  && cmake --build . --config Debug && cd .. && mvn package
```

## Documentation

### Example

This is a short example on how to use this library:

```java
public class Example {

    public static void main(String... args) throws IOException {
        ModelParameters modelParams = new ModelParameters()
                .setModelFilePath("/path/to/model.gguf")
                .setNGpuLayers(43);

        String system = "This is a conversation between User and Llama, a friendly chatbot.\n" +
                "Llama is helpful, kind, honest, good at writing, and never fails to answer any " +
                "requests immediately and with precision.\n";
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.UTF_8));
        try (LlamaModel model = new LlamaModel(modelParams)) {
            System.out.print(system);
            String prompt = system;
            while (true) {
                prompt += "\nUser: ";
                System.out.print("\nUser: ");
                String input = reader.readLine();
                prompt += input;
                System.out.print("Llama: ");
                prompt += "\nLlama: ";
                InferenceParameters inferParams = new InferenceParameters(prompt)
                        .setTemperature(0.7f)
                        .setPenalizeNl(true)
                        .setMirostat(InferenceParameters.MiroStat.V2)
                        .setAntiPrompt("\n");
                for (LlamaOutput output : model.generate(inferParams)) {
                    System.out.print(output);
                    prompt += output;
                }
            }
        }
    }
}
```

Also have a look at the other [examples](src/test/java/examples).

### Inference

There are multiple inference tasks. In general, `LlamaModel` is stateless, i.e., you have to append the output of the
model to your prompt in order to extend the context. If there is repeated content, however, the library will internally
cache this, to improve performance.

```java
ModelParameters modelParams = new ModelParameters().setModelFilePath("/path/to/model.gguf");
InferenceParameters inferParams = new InferenceParameters("Tell me a joke.");
try (LlamaModel model = new LlamaModel(modelParams)) {
    // Stream a response and access more information about each output.
    for (LlamaOutput output : model.generate(inferParams)) {
        System.out.print(output);
    }
    // Calculate a whole response before returning it.
    String response = model.complete(inferParams);
    // Returns the hidden representation of the context + prompt.
    float[] embedding = model.embed("Embed this");
}
```

> [!NOTE]
> Since llama.cpp allocates memory that can't be garbage collected by the JVM, `LlamaModel` is implemented as an
> AutoClosable. If you use the objects with `try-with` blocks like the examples, the memory will be automatically
> freed when the model is no longer needed. This isn't strictly required, but avoids memory leaks if you use different
> models throughout the lifecycle of your application.

### Infilling

You can simply set `InferenceParameters#setInputPrefix(String)` and `InferenceParameters#setInputSuffix(String)`.

### Model/Inference Configuration

There are two sets of parameters you can configure, `ModelParameters` and `InferenceParameters`. Both provide builder
classes to ease configuration. `ModelParameters` are once needed for loading a model, `InferenceParameters` are needed
for every inference task. All non-specified options have sensible defaults.

```java
ModelParameters modelParams = new ModelParameters()
        .setModelFilePath("/path/to/model.gguf")
        .setLoraAdapter("/path/to/lora/adapter")
        .setLoraBase("/path/to/lora/base");
String grammar = """
  root  ::= (expr "=" term "\\n")+
  expr  ::= term ([-+*/] term)*
  term  ::= [0-9]""";
InferenceParameters inferParams = new InferenceParameters("")
        .setGrammar(grammar)
        .setTemperature(0.8);
try (LlamaModel model = new LlamaModel(modelParams)) {
    model.generate(inferParams);
}
```

### Logging

Per default, logs are written to stdout.
This can be intercepted via the static method `LlamaModel.setLogger(LogFormat, BiConsumer<LogLevel, String>)`.
There is text- and JSON-based logging. The default is JSON.
Note, that text-based logging will include additional output of the GGML backend, while JSON-based logging
only provides request logs (while still writing GGML messages to stdout).
To only change the log format while still writing to stdout, `null` can be passed for the callback.
Logging can be disabled by passing an empty callback.

```java
// Re-direct log messages however you like (e.g. to a logging library)
LlamaModel.setLogger(LogFormat.TEXT, (level, message) -> System.out.println(level.name() + ": " + message));
// Log to stdout, but change the format
LlamaModel.setLogger(LogFormat.TEXT, null);
// Disable logging by passing a no-op
LlamaModel.setLogger(null, (level, message) -> {});
```
