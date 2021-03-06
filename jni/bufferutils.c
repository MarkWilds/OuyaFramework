#include "wildrune_ouyaframework_graphics_utils_BufferUtils.h"

// Copy a float array over to a float buffer
JNIEXPORT void JNICALL Java_wildrune_ouyaframework_graphics_utils_BufferUtils_CopyJNI___3FILjava_nio_Buffer_2II
  (JNIEnv * env, jclass clazz, jfloatArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes)
{
	float* src = (float*)(*env)->GetPrimitiveArrayCritical(env, obj_src, 0);
	unsigned char* dst = (unsigned char*)(obj_dst?(*env)->GetDirectBufferAddress(env, obj_dst):0);

	memcpy(dst + dstOffset, src + srcOffset, numBytes);

	(*env)->ReleasePrimitiveArrayCritical(env, obj_src, src, 0);
}

// Copy a short array over to a short buffer
JNIEXPORT void JNICALL Java_wildrune_ouyaframework_graphics_utils_BufferUtils_CopyJNI___3SILjava_nio_Buffer_2II
  (JNIEnv * env, jclass clazz, jshortArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes)
{
	short* src = (short*)(*env)->GetPrimitiveArrayCritical(env, obj_src, 0);
	unsigned char* dst = (unsigned char*)(obj_dst?(*env)->GetDirectBufferAddress(env, obj_dst):0);

	memcpy(dst + dstOffset, src + srcOffset, numBytes);

	(*env)->ReleasePrimitiveArrayCritical(env, obj_src, src, 0);
}
