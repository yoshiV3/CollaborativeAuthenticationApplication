//
// Created by yoshi on 24/02/21.
//

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
    #include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>
    int getSizeOfArrayListRandomPoly(JNIEnv *env, jobject thiz);
    void fillPolyWithData(JNIEnv *env, jobject thiz, uint32_t **poly, int size);
    void fillResultWithData(JNIEnv *env, uint32_t ** evals, jobject result, int resultSize );
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_INTERFACE_H
