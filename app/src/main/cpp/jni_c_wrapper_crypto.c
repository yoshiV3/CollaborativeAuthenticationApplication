//
// Created by yoshi on 04/04/21.
//

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
#include "ecc_arithmetic.h"
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
    int size          = getSizeOfArrayListRandomPoly(env, thiz); //number of coefficients
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

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoKeyShareGenerator_generateShares(
        JNIEnv *env, jobject thiz, jobject shares, jobject key_parts) {


    int weight                 = getArrayListSize(env, key_parts); //local weight: number of local participants

    if (weight == 0){
        return;
    }

    jobject  parts             = getObjectFromArrayList(env, key_parts, 0);
    int totalWeight            = getArrayListSize(env, parts);

    uint32_t ** partsC         = (uint32_t **) malloc(totalWeight*sizeof(uint32_t **));
    uint32_t ** sharesC        = (uint32_t **) malloc(weight * sizeof(uint32_t*));



    for (int index = 0; index < weight; index++)
    {
        parts         = getObjectFromArrayList(env, key_parts, index);
        transformArrayListBigNumbersToCArray(env,  parts , partsC, totalWeight);
        uint32_t *sum    = malloc(SIZE*sizeof(uint32_t));
        sharesC[index]   = sum;
        sum_mod_p(partsC, sum, totalWeight);
    }

    fillResultWithData(env, sharesC, shares, weight);
    freeArrayOfArrays(partsC, totalWeight);
    freeArrayOfArrays(sharesC, weight);
}


/*
 * Calculates the parts of the key shares for all participants from the local participants
 * calculates a part of the public key
 */
JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoProcessor_generateKeyParts(
        JNIEnv *env, jobject thiz, jint total_weight, jobject polynomials, jobject parts,
        jobject public_key_part) {
    //get polynomials for each participant
    // for each poly: calculate  all the parts
    // calculate for each poly a part of the public key
    // and combine the parts of the public key in a bigger part of the public key



    Point A[256]; //look up tables
    Point B[256];
    generate_exponentiation_lookup_table(A,B);

    uint32_t  number_of_local = getArrayListSize(env, polynomials);
    uint32_t ** evals         = (uint32_t **) malloc(total_weight * sizeof(uint32_t*));


    jobject   arr             = getObjectFromArrayList(env, polynomials, 0);
    uint32_t  threshold       = getArrayListSize(env, arr); //threshold = degree + 1

    uint32_t ** poly  = (uint32_t **) malloc(threshold*sizeof(uint32_t*));
    transformArrayListBigNumbersToCArray(env,arr, poly, threshold); //place data into a c 2D array (polynomial representation)

    Point publicKey;


    for (uint32_t identifier = 1; identifier <= total_weight; identifier++)
    {
        uint32_t * point_on_poly    = malloc(SIZE*sizeof(uint32_t));
        evals[identifier-1]         = point_on_poly;
        evaluate_poly(poly, identifier, threshold-1, point_on_poly);
    }



    ecc_exponentiation(A,B, poly[threshold-1], &publicKey );


    Point publicKeyPart;
    uint32_t share[SIZE];
    for(uint32_t local = 1; local <number_of_local; local++){
        for (uint32_t identifier = 1; identifier <= total_weight; identifier++)
        {
            evaluate_poly(poly, identifier, threshold-1, share);
            add_mod_p(evals[identifier-1], share, evals[identifier-1]);
        }
        ecc_exponentiation(A,B, poly[threshold-1], &publicKeyPart);
        add_points(&publicKey, &publicKeyPart, &publicKey);
    }
    fillResultWithData(env,evals,parts, total_weight);
    fillPointWithData(env, &publicKey, public_key_part);
    freeArrayOfArrays(evals, total_weight);
}

JNIEXPORT jobject JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoPartKeyRecovery_createLocalSecretSharePartsFromSharesForTargetNative(
        JNIEnv *env, jobject thiz, jobject shares, jintArray identifiers, jint identifier_target) {
    jsize lenIdentifiers = (*env)->GetArrayLength(env, identifiers);
    jsize lenShares      = getArrayListSize(env, shares);
    if (lenShares != lenIdentifiers){
        jclass Exception = (*env)->FindClass(env, "java/lang/IllegalArgumentException");
        (*env)->ThrowNew(env, Exception,"not as many shares as identifiers");
    }
    uint32_t *identifiersC = (uint32_t *) (*env)->GetIntArrayElements(env, identifiers, 0);
    uint32_t  ** sharesC = (uint32_t **)  malloc(lenShares*sizeof(uint32_t*));
    transformArrayListBigNumbersToCArray(env, shares, sharesC, lenShares);
    uint32_t newShare[SIZE] = {0};
    uint32_t * x_values = malloc((lenShares - 1) * sizeof(uint32_t));
    uint32_t x_target = (uint32_t) identifier_target;
    uint32_t degree   = ((uint32_t)lenShares) - 1;
    for (uint32_t x=0; x<lenShares; x++){
        for (uint32_t i = 0; i<lenShares; i++){
            if (x>i){
                x_values[i] = identifiersC[i];
            }
            else if (x< i){
                x_values[i-1] = identifiersC[i];
            }
        }
        uint32_t e[SIZE];
        calculate_share_part_for_x(x_values, identifiersC[x], x_target, degree, sharesC[x], e );
        add_mod_p(newShare, e, newShare);
    }
    free(x_values);
    freeArrayOfArrays(sharesC, lenShares);
    (*env)->ReleaseIntArrayElements(env, identifiers, identifiersC, 0);
    return getNewBigNumberObject(env, newShare);

}