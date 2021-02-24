//
// Created by yoshi on 24/02/21.
//

#include "secret_sharing.h"
#include "inversion.h"
#include "common.h"
#include "mp_arithmetic.h"
#include "mod_arithmetic.h"
#include "signed_arithmetic.h"
#include "interface.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/malloc.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>





void freeArrayOfArrays(uint32_t ** poly, size)
{
    for (int index = 0; index <size; index++)
    {
        free(poly[index]);
    }
    free(poly);
}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoKeyPartGenerator_getPartsForRange(
        JNIEnv *env, jobject thiz, jobject result, jint min_weight, jint max_weight) {
    int size          = getSizeOfArrayListRandomPoly(env, thiz);
    uint32_t ** poly  = (uint32_t **) malloc(size*sizeof(uint32_t*));
    fillPolyWithData(env, thiz, poly, size);

    int resultSize = max_weight - min_weight + 1;
    uint32_t ** evals  = (uint32_t **) malloc(resultSize * sizeof(uint32_t*));

    for (uint32_t identifier = min_weight; identifier <= max_weight; identifier++)
    {
        uint32_t *c    = malloc(SIZE*sizeof(uint32_t));
        evals[identifier-min_weight]    = c;
        evaluate_poly(poly, identifier, size-1, c);
    }

    fillResultWithData(env,evals,result, resultSize );
    freeArrayOfArrays(evals, resultSize);
    freeArrayOfArrays(poly, size);
}