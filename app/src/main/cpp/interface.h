//
// Created by yoshi on 24/02/21.
//

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>
#include "ecc_point.h"

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H

    int     getSizeOfArrayListRandomPoly(JNIEnv *env, jobject thiz);
    void    fillPolyWithData(JNIEnv *env, jobject thiz, uint32_t **poly, int size);
    void    fillArrayListWithData(JNIEnv *env, uint32_t ** results, jobject arrayList, int resultSize );
    int     getArrayListSize(JNIEnv const *env, const jobject *arrayList);
    jobject getObjectFromArrayList(const JNIEnv *env, const jobject arr, int index);
    void    transformArrayListBigNumbersToCArray(const JNIEnv *env, const jobject arr,  uint32_t ** result, int size);
    void    fillPointWithData(JNIEnv *env, Point * c_point, jobject java_point);
    jobject getNewBigNumberObject(JNIEnv *env, uint32_t * results);
    jobject getNewPoint(JNIEnv *env, jobject x, jobject  y);
    void    bigNumberToArrayC(const JNIEnv *env, jobject bigNumber, uint32_t * c);
    void    javaPointToPointC(const JNIEnv *env, jobject java_point, Point * c_point);
    void    getXFromJavaPoint(const JNIEnv *env, jobject java_point, uint32_t *x);
    void    getYFromJavaPoint(const JNIEnv *env, jobject java_point, uint32_t *y);
    void    isZeroFromJavaPoint(const JNIEnv *env, jobject java_point, uint8_t *isZero);
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
