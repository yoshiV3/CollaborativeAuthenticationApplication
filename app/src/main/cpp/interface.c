//
// Created by yoshi on 24/02/21.
//


#include "interface.h"
#include "common.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/malloc.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>
int getSizeOfArrayListRandomPoly(JNIEnv *env, jobject thiz)
{
    jclass thisClass = (*env)->GetObjectClass(env, thiz);

    jfieldID fidPoly= (*env)->GetFieldID(env, thisClass, "polynomial", "Ljava/util/ArrayList;");

    if (NULL == fidPoly) return 0;

    jobject objPoly = (*env)->GetObjectField(env, thiz, fidPoly);

    jclass polyClass = (*env)->GetObjectClass(env,objPoly );

    jmethodID sizeMethod = (*env)->GetMethodID(env,polyClass,"size", "()I");

    return (*env)->CallIntMethod(env,objPoly, sizeMethod);
}


void fillPolyWithData(JNIEnv *env, jobject thiz, uint32_t **poly, int size)
{

    jclass thisClass = (*env)->GetObjectClass(env, thiz);

    jfieldID fidPoly= (*env)->GetFieldID(env, thisClass, "polynomial", "Ljava/util/ArrayList;");

    if (NULL == fidPoly) return;

    jobject objPoly = (*env)->GetObjectField(env, thiz, fidPoly);

    jclass polyClass = (*env)->GetObjectClass(env,objPoly );

    jmethodID getMethod = (*env)->GetMethodID(env,polyClass,"get", "(I)Ljava/lang/Object;");

    for (int index = 0; index < size; index++)
    {

        uint32_t *c    = malloc(SIZE*sizeof(uint32_t));
        poly[index]    = c;

        jobject coeff  =  (*env)->CallObjectMethod(env,objPoly, getMethod, index);

        jclass coeffClass = (*env)->GetObjectClass(env, coeff);

        jmethodID getPartMethod = (*env)->GetMethodID(env,coeffClass,"getPart", "(I)I");

        jint value;

        for (int innerIndex = 0; innerIndex < SIZE; innerIndex++)
        {
            value = (*env)->CallIntMethod(env, coeff,  getPartMethod, innerIndex);
            c[innerIndex]  = (uint32_t) value;
        }
    }
}


void fillResultWithData(JNIEnv *env, uint32_t ** evals, jobject result, int resultSize )
{
    jclass bigNumberClass = (*env)->FindClass(env, "com/project/collaborativeauthenticationapplication/service/crypto/BigNumber");

    jmethodID mid = (*env)->GetMethodID(env, bigNumberClass, "<init>", "(IIIIIIII)V");

    jclass listClass = (*env)->GetObjectClass(env,result );

    jmethodID addMethod = (*env)->GetMethodID(env,listClass,"add", "(Ljava/lang/Object;)Z");

    for (int res = 0; res <resultSize; res++)
    {
        jint one   = (jint) evals[res][0];
        jint two   = (jint) evals[res][1];
        jint three = (jint) evals[res][2];
        jint four  = (jint) evals[res][3];
        jint five  = (jint) evals[res][4];
        jint six   = (jint) evals[res][5];
        jint seven = (jint) evals[res][6];
        jint eight = (jint) evals[res][7];

        jobject newObj = (*env)->NewObject(env, bigNumberClass, mid,one, two, three, four, five, six, seven, eight );

        (*env)->CallBooleanMethod(env, result,  addMethod, newObj);
    }
}
