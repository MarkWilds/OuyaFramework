#include "wildrune_ouyaframework_util_BufferUtils.h"

JNIEXPORT void JNICALL Java_wildrune_ouyaframework_util_BufferUtils_CopyJNI
(JNIEnv *env, jclass clazz, jfloatArray obj_src, jint srcOffset, jobject obj_dst, jint dstOffset, jint numBytes )
{
	float* src = (float*)(*env)->GetPrimitiveArrayCritical(env, obj_src, 0);
	unsigned char* dst = (unsigned char*)(obj_dst?(*env)->GetDirectBufferAddress(env, obj_dst):0);

	memcpy(dst + dstOffset, src + srcOffset, numBytes);

	(*env)->ReleasePrimitiveArrayCritical(env, obj_src, src, 0);
}
