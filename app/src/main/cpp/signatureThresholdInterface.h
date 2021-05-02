//
// Created by yoshi on 06/04/21.
//

#include "interface.h"
#include "common.h"
#include "ecc_point.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/malloc.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>


#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNATURETHRESHOLDINTERFACE_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNATURETHRESHOLDINTERFACE_H
    void getRandomnessLists(JNIEnv *env, jobject  processor, uint32_t ** e, uint32_t ** d, uint32_t  number);
    int  getLocalWeight(JNIEnv *env, jobject  processor);
    void placeCommitmentsInProcessor(JNIEnv *env, jobject  processor, Point * E, Point * D, int number);
    void getListEAndD(JNIEnv *env, jobject  processor, jobject * lists);
    int  getTotalWeight(JNIEnv *env, jobject  processor);
    void getAllRandomness(JNIEnv *env, jobject  processor, Point * E, Point * D, int number);
    void getShares(JNIEnv *env, jobject  processor, uint32_t ** shares, int number);
    void writeSignatureShareToJava(JNIEnv *env, jobject  processor, uint32_t ** shares);

    #define createIdentifiersIndexes(arr, localWeight, firstIndex) \
                                        {\
                                            (arr) = malloc(localWeight*sizeof(uint32_t));\
                                            for(uint32_t i =0; i < localWeight; i++){\
                                                (arr)[i] = firstIndex + i;\
                                            }\
                                        }
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNATURETHRESHOLDINTERFACE_H
