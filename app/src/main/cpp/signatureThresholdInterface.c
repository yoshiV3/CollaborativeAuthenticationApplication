//
// Created by yoshi on 06/04/21.
//

#include "signatureThresholdInterface.h"
#include "interface.h"
#include "common.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/malloc.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>

void getRandomnessLists(JNIEnv *env, jobject  processor, uint32_t ** e, uint32_t ** d, uint32_t  number){

    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID geteListMethod = (*env)->GetMethodID(env, jProcessorClass, "geteList", "()Ljava/util/ArrayList;");
    jmethodID getdListMethod = (*env)->GetMethodID(env, jProcessorClass, "getdList", "()Ljava/util/ArrayList;");
    jobject  eList = (*env)->CallObjectMethod(env,processor, geteListMethod);
    jobject  dList = (*env)->CallObjectMethod(env,processor, getdListMethod);

    transformArrayListBigNumbersToCArray(env, eList,  e, number);
    transformArrayListBigNumbersToCArray(env, dList,  d, number);
}


int  getLocalWeight(JNIEnv *env, jobject  processor){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID getLocalWeightMethod = (*env)->GetMethodID(env, jProcessorClass, "getLocalWeight", "()I");
    return (*env)->CallIntMethod(env,processor, getLocalWeightMethod);
}


int  getTotalWeight(JNIEnv *env, jobject  processor){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID getLocalWeightMethod = (*env)->GetMethodID(env, jProcessorClass, "getTotalWeight", "()I");
    return (*env)->CallIntMethod(env,processor, getLocalWeightMethod);
}


void getCommitmentListEAndD(JNIEnv *env, jobject  processor, jobject * lists){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID geteListMethod = (*env)->GetMethodID(env, jProcessorClass, "getCommitmentE", "()Ljava/util/ArrayList;");
    jmethodID getdListMethod = (*env)->GetMethodID(env, jProcessorClass, "getCommitmentD", "()Ljava/util/ArrayList;");
    lists[0] = (*env)->CallObjectMethod(env,processor, geteListMethod);
    lists[1] = (*env)->CallObjectMethod(env,processor, getdListMethod);
}



void getListEAndD(JNIEnv *env, jobject  processor, jobject * lists){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID geteListMethod = (*env)->GetMethodID(env, jProcessorClass, "getE", "()Ljava/util/ArrayList;");
    jmethodID getdListMethod = (*env)->GetMethodID(env, jProcessorClass, "getD", "()Ljava/util/ArrayList;");
    lists[0] = (*env)->CallObjectMethod(env,processor, geteListMethod);
    lists[1] = (*env)->CallObjectMethod(env,processor, getdListMethod);
}


void getAllRandomness(JNIEnv *env, jobject  processor, Point * E, Point * D, int number){
    jobject lists[2];
    getListEAndD(env, processor, lists);
    for (int i = 0; i <number; i++){
        jobject e = getObjectFromArrayList(env, lists[0], i);
        jobject d = getObjectFromArrayList(env, lists[1], i);

        javaPointToPointC(env, e, &E[i]);
        javaPointToPointC(env, d, &D[i]);
    }
}


jobject getSharesFromJava(JNIEnv *env, jobject  processor){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID getSharetMethod = (*env)->GetMethodID(env, jProcessorClass, "getShares", "()Ljava/util/ArrayList;");
    return (*env)->CallObjectMethod(env,processor, getSharetMethod);
}


void getShares(JNIEnv *env, jobject  processor, uint32_t ** shares, int number){
    jobject shares_java = getSharesFromJava(env, processor);
    transformArrayListBigNumbersToCArray(env, shares_java, shares, number);
}

jobject getSignatureSharesFromJava(JNIEnv *env, jobject  processor){
    jclass jProcessorClass = (*env)->GetObjectClass(env, processor);
    jmethodID getSharetMethod = (*env)->GetMethodID(env, jProcessorClass, "getSignatureShare", "()Ljava/util/ArrayList;");
    return (*env)->CallObjectMethod(env,processor, getSharetMethod);
}


void writeSignatureShareToJava(JNIEnv *env, jobject  processor, uint32_t ** shares){
    jobject  signatureShares = getSignatureSharesFromJava(env, processor);
    fillArrayListWithData(env, shares, signatureShares, 2);
}




void placeCommitmentsInProcessor(JNIEnv *env, jobject  processor, Point * E, Point * D, int number){
    jobject lists[2];
    getCommitmentListEAndD(env, processor, lists);

    jclass listClass = (*env)->GetObjectClass(env, lists[0]);

    jmethodID addMethod = (*env)->GetMethodID(env,listClass,"add", "(Ljava/lang/Object;)Z");

    for(int i =0; i <number; i++){
        jobject xE = getNewBigNumberObject(env, E[i].x);
        jobject yE = getNewBigNumberObject(env, E[i].y);

        jobject xD = getNewBigNumberObject(env, D[i].x);
        jobject yD = getNewBigNumberObject(env, D[i].y);

        jobject  pE = getNewPoint(env, xE, yE);
        (*env)->CallBooleanMethod(env, lists[0],  addMethod, pE);

        jobject  pD = getNewPoint(env, xD, yD);
        (*env)->CallBooleanMethod(env, lists[1],  addMethod, pD);
    }
}