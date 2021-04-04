//
// Created by yoshi on 24/02/21.
//

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>
#include "ecc_point.h"

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H

    int     getSizeOfArrayListRandomPoly(JNIEnv *env, jobject thiz);
    void    fillPolyWithData(JNIEnv *env, jobject thiz, uint32_t **poly, int size);
    void    fillResultWithData(JNIEnv *env, uint32_t ** results, jobject result, int resultSize );
    int     getArrayListSize(JNIEnv const *env, const jobject *arrayList);
    jobject getObjectFromArrayList(const JNIEnv *env, const jobject arr, int index);
    void    transformArrayListBigNumbersToCArray(const JNIEnv *env, const jobject arr,  uint32_t ** result, int size);
    void    fillPointWithData(JNIEnv *env, Point * c_point, jobject java_point);
    jobject getNewBigNumberObject(JNIEnv *env, uint32_t * results);
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
