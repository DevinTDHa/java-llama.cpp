/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class de_kherud_llama_LlamaModel */

#ifndef _Included_de_kherud_llama_LlamaModel
#define _Included_de_kherud_llama_LlamaModel
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    embed
 * Signature: (Ljava/lang/String;)[F
 */
JNIEXPORT jfloatArray JNICALL Java_de_kherud_llama_LlamaModel_embed
  (JNIEnv *, jobject, jstring);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    encode
 * Signature: (Ljava/lang/String;)[I
 */
JNIEXPORT jintArray JNICALL Java_de_kherud_llama_LlamaModel_encode
  (JNIEnv *, jobject, jstring);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    setLogger
 * Signature: (Ljava/util/function/BiConsumer;)V
 */
JNIEXPORT void JNICALL Java_de_kherud_llama_LlamaModel_setLogger
  (JNIEnv *, jclass, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    loadModel
 * Signature: (Ljava/lang/String;Lde/kherud/llama/ModelParameters;)V
 */
JNIEXPORT void JNICALL Java_de_kherud_llama_LlamaModel_loadModel
  (JNIEnv *, jobject, jstring, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    newAnswerIterator
 * Signature: (Ljava/lang/String;Lde/kherud/llama/InferenceParameters;)V
 */
JNIEXPORT void JNICALL Java_de_kherud_llama_LlamaModel_newAnswerIterator
  (JNIEnv *, jobject, jstring, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    newInfillIterator
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lde/kherud/llama/InferenceParameters;)V
 */
JNIEXPORT void JNICALL Java_de_kherud_llama_LlamaModel_newInfillIterator
  (JNIEnv *, jobject, jstring, jstring, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    getNext
 * Signature: (Lde/kherud/llama/LlamaModel/LlamaIterator;)Lde/kherud/llama/LlamaModel/Output;
 */
JNIEXPORT jobject JNICALL Java_de_kherud_llama_LlamaModel_getNext
  (JNIEnv *, jobject, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    getAnswer
 * Signature: (Ljava/lang/String;Lde/kherud/llama/InferenceParameters;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_de_kherud_llama_LlamaModel_getAnswer
  (JNIEnv *, jobject, jstring, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    getInfill
 * Signature: (Ljava/lang/String;Ljava/lang/String;Lde/kherud/llama/InferenceParameters;)[B
 */
JNIEXPORT jbyteArray JNICALL Java_de_kherud_llama_LlamaModel_getInfill
  (JNIEnv *, jobject, jstring, jstring, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    decodeBytes
 * Signature: ([I)[B
 */
JNIEXPORT jbyteArray JNICALL Java_de_kherud_llama_LlamaModel_decodeBytes
  (JNIEnv *, jobject, jintArray);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    delete
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_de_kherud_llama_LlamaModel_delete
  (JNIEnv *, jobject);

/*
 * Class:     de_kherud_llama_LlamaModel
 * Method:    batchComplete
 * Signature: ([Ljava/lang/String;Lde/kherud/llama/InferenceParameters;)[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_de_kherud_llama_LlamaModel_batchComplete
  (JNIEnv *, jobject, jobjectArray, jobject);

#ifdef __cplusplus
}
#endif
#endif
