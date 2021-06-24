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
#include "threshold_signature.h"
#include "signatureThresholdInterface.h"
#include "array.h"
#include "hash.h"
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
        evaluate_poly_n(poly, identifier, size-1, c);
    }

    fillArrayListWithData(env, evals, result, resultSize);

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
        sum_mod_n(partsC, sum, totalWeight);
    }

    fillArrayListWithData(env, sharesC, shares, weight);
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
        evaluate_poly_n(poly, identifier, threshold-1, point_on_poly);
    }



    ecc_exponentiation(A,B, poly[threshold-1], &publicKey );


    Point publicKeyPart;
    uint32_t share[SIZE];
    for(uint32_t local = 1; local <number_of_local; local++){
        for (uint32_t identifier = 1; identifier <= total_weight; identifier++)
        {
            evaluate_poly_n(poly, identifier, threshold-1, share);
            add_mod_n(evals[identifier-1], share, evals[identifier-1]);
        }
        ecc_exponentiation(A,B, poly[threshold-1], &publicKeyPart);
        add_points(&publicKey, &publicKeyPart, &publicKey);
    }
    fillArrayListWithData(env, evals, parts, total_weight);
    fillPointWithData(env, &publicKey, public_key_part);
    freeArrayOfArrays(evals, total_weight);
    freeArrayOfArrays(poly, threshold);

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
            if (x>i) {
                x_values[i] = identifiersC[i];
            } else if (x< i){
                x_values[i-1] = identifiersC[i];
            }
        }
        uint32_t e[SIZE];
        calculate_share_part_for_x_n(x_values, identifiersC[x], x_target, degree, sharesC[x], e );
        add_mod_n(newShare, e, newShare);
    }
    free(x_values);
    freeArrayOfArrays(sharesC, lenShares);
    (*env)->ReleaseIntArrayElements(env, identifiers, identifiersC, 0);
    return getNewBigNumberObject(env, newShare);

}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoThresholdSignatureProcessor_calculateCommitmentsToRandomnessNative(
        JNIEnv *env, jobject thiz) {

    int localWeight = getLocalWeight(env, thiz);
    uint32_t ** e   = malloc(localWeight*sizeof(uint32_t*));
    uint32_t ** d   = malloc(localWeight*sizeof(uint32_t*));

    getRandomnessLists(env, thiz, e, d, localWeight);

    Point * E = malloc(localWeight*sizeof(Point));
    Point * D = malloc(localWeight*sizeof(Point));
    Calculate_commitments_to_random_numbers(e, d, E, D, localWeight);

    placeCommitmentsInProcessor(env, thiz, E, D, localWeight);

    free(E);
    free(D);

    freeArrayOfArrays(e, localWeight);
    freeArrayOfArrays(d, localWeight);

}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoThresholdSignatureProcessor_calculateSignatureShare(
        JNIEnv *env, jobject thiz, jobject message, jintArray identifiers, jint firstLocalIndex) {
    // get all the inputs to c
    uint32_t messageC[SIZE];
    bigNumberToArrayC(env, message, messageC);
    uint32_t *identifiersC = (uint32_t *) (*env)->GetIntArrayElements(env, identifiers, 0);

    int totalWeight        =  getTotalWeight(env, thiz);
    int localWeight        =  getLocalWeight(env, thiz);
    uint32_t ** e   = malloc(localWeight*sizeof(uint32_t*));
    uint32_t ** d   = malloc(localWeight*sizeof(uint32_t*));

    Point * E = malloc(totalWeight*sizeof(Point));
    Point * D = malloc(totalWeight*sizeof(Point));

    getAllRandomness(env, thiz ,E ,D , totalWeight);
    getRandomnessLists(env, thiz, e, d, localWeight);

    uint32_t ** rhos;
    rhos = (uint32_t **) malloc(totalWeight*sizeof(uint32_t *));
    Point R;
    uint32_t hash[SIZE] = {0};
    calculate_hash_and_nonce_and_rhos(totalWeight, E, D, identifiersC, messageC, hash, &R, rhos);

    uint32_t * signature[2];
    uint32_t signature_s[SIZE]  = {0};
    uint32_t signature_e[SIZE]  = {0};
    signature[0] = signature_s;
    signature[1] = signature_e;

    uint32_t * identifiers_indexes;
    createIdentifiersIndexes(identifiers_indexes, localWeight, firstLocalIndex);

    uint32_t ** sharesC;
    sharesC = malloc(localWeight*sizeof(uint32_t));
    getShares(env, thiz, sharesC, localWeight);




    produce_signature_shares_for_subset(localWeight, totalWeight, &R, identifiersC, identifiers_indexes, e, d, rhos, sharesC, hash, signature[0]);
    for (uint32_t index =0; index <SIZE; index++) {
        signature[1][index] = hash[index];
    }



    writeSignatureShareToJava(env, thiz, signature);

    FREE_2D_ARRAY(sharesC, localWeight);
    FREE_2D_ARRAY(rhos, totalWeight);
    free(identifiers_indexes);
    free(E);
    free(D);
    free(e);
    free(d);
    (*env)->ReleaseIntArrayElements(env, identifiers, identifiersC, 0);
}

JNIEXPORT jboolean JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoVerificationProcessor_verifyNative(
        JNIEnv *env, jobject thiz, jobject signature, jobject message, jobject public_key) {
    uint32_t messageC[SIZE];
    bigNumberToArrayC(env, message, messageC);

    uint32_t * signatureC[2];
    uint32_t signature_s[SIZE]  = {0};
    uint32_t signature_e[SIZE]  = {0};
    signatureC[0] = signature_s;
    signatureC[1] = signature_e;
    transformArrayListBigNumbersToCArray(env, signature, signatureC, 2);

    Point pk;
    javaPointToPointC(env, public_key, &pk);

    uint8_t  correct = verify_threshold(messageC, SIZE, &pk, signatureC);

    if (correct){
        return JNI_TRUE;
    }
    return JNI_FALSE;
}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoKeyShareGenerator_generateFinalPublicKey(
        JNIEnv *env, jobject thiz, jobject parts, jobject public_key) {
    int numberOfParts = getArrayListSize(env, parts);
    Point * points = (Point *) malloc(numberOfParts*sizeof(Point));
    for (size_t i = 0; i < numberOfParts; i++){
        jobject javaPoint = getObjectFromArrayList(env, parts, i);
        javaPointToPointC(env, javaPoint, &points[i]);
    }
    Point res;
    sum_points(points, numberOfParts, &res);
    fillPointWithData(env, &res, public_key);
    free(points);
}

JNIEXPORT jobject JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoThresholdSignatureProcessor_calculateFinalSignatureNative(
        JNIEnv *env, jobject thiz, jobject parts) {

    int size = getArrayListSize(env, parts);

    uint32_t ** partsC = malloc(size*sizeof(uint32_t *));
    transformArrayListBigNumbersToCArray(env, parts, partsC, size);

    uint32_t result[SIZE];

    sum_mod_n(partsC, result, size);

    return getNewBigNumberObject(env, result);
}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoRefreshShareUnit_createSharesNative(
        JNIEnv *env, jobject thiz, jobject poly, jintArray identifiers, jobject shares) {

    uint32_t  threshold       = getArrayListSize(env, poly); //threshold = degree + 1
    uint32_t ** pol  = (uint32_t **) malloc(threshold*sizeof(uint32_t*)); //RELEASE
    transformArrayListBigNumbersToCArray(env,poly, pol, threshold); //place data into a c 2D array (polynomial representation)

    jsize lenIdentifiers = (*env)->GetArrayLength(env, identifiers);
    uint32_t *identifiersC = (uint32_t *) (*env)->GetIntArrayElements(env, identifiers, 0); //RELEASE

    uint32_t ** evals         = (uint32_t **) malloc(lenIdentifiers * sizeof(uint32_t*));

    for (int index =0; index < lenIdentifiers; index++)
    {
        uint32_t * point_on_poly    = malloc(SIZE*sizeof(uint32_t));
        evals[index]         = point_on_poly;
        evaluate_poly_n(pol, identifiersC[index], threshold-1, point_on_poly);
    }


    fillArrayListWithData(env, evals, shares, lenIdentifiers);
    freeArrayOfArrays(evals, lenIdentifiers);
    freeArrayOfArrays(pol, threshold);
    (*env)->ReleaseIntArrayElements(env, identifiers, identifiersC, 0);


}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoRefreshShareUnit_calculateSharesNative(
        JNIEnv *env, jobject thiz, jobject list, jobject shares) {
    int weight                 = getArrayListSize(env, list); //local weight: number of local participants

    if (weight == 0){
        return;
    }

    jobject  parts             = getObjectFromArrayList(env, list, 0);
    int totalWeight            = getArrayListSize(env, parts);

    uint32_t ** partsC         = (uint32_t **) malloc(totalWeight*sizeof(uint32_t **));
    uint32_t ** sharesC        = (uint32_t **) malloc(weight * sizeof(uint32_t*));



    for (int index = 0; index < weight; index++)
    {
        parts         = getObjectFromArrayList(env, list, index);
        transformArrayListBigNumbersToCArray(env,  parts , partsC, totalWeight);
        uint32_t *sum    = malloc(SIZE*sizeof(uint32_t));
        sharesC[index]   = sum;
        sum_mod_n(partsC, sum, totalWeight);
    }

    fillArrayListWithData(env, sharesC, shares, weight);
    freeArrayOfArrays(partsC, totalWeight);
    freeArrayOfArrays(sharesC, weight);
}

JNIEXPORT void JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoExtendUnit_getSlicesNative(
        JNIEnv *env, jobject thiz, jintArray identifiers, jobject shares, jint weight,
        jint new_identifier, jobject randomness, jobject slices) {
    int numberOfSlices                 = getArrayListSize(env, randomness) +1; //local weight: number of local participants

    uint32_t ** randomnessC    = (uint32_t **) malloc((numberOfSlices-1)*sizeof(uint32_t **));
    uint32_t ** sharesC        = (uint32_t **) malloc(weight * sizeof(uint32_t*));

    transformArrayListBigNumbersToCArray(env,  randomness , randomnessC, numberOfSlices-1);
    uint32_t * sum[SIZE];
    sum_mod_n(randomnessC, sum, numberOfSlices-1);

    transformArrayListBigNumbersToCArray(env,  shares , sharesC, weight);



    uint32_t *identifiersC = (uint32_t *) (*env)->GetIntArrayElements(env, identifiers, 0);

    jsize lenIdentifiers = (*env)->GetArrayLength(env, identifiers);

    uint32_t total[SIZE] = {0};
    uint32_t * x_values = malloc((lenIdentifiers - 1) * sizeof(uint32_t));
    uint32_t x_target = (uint32_t) new_identifier;
    uint32_t degree   = ((uint32_t)lenIdentifiers) - 1;
    for (uint32_t x=0; x<weight; x++){
        for (uint32_t i = 0; i<lenIdentifiers; i++){
            if (x>i) {
                x_values[i] = identifiersC[i];
            } else if (x< i){
                x_values[i-1] = identifiersC[i];
            }
        }
        uint32_t e[SIZE];
        calculate_share_part_for_x_n(x_values, identifiersC[x], x_target, degree, sharesC[x], e );
        add_mod_n(total, e, total);
    }

    uint32_t slice[SIZE];
    sub_mod_n(total, sum, slice);


    uint32_t ** result = (uint32_t **) malloc((numberOfSlices)*sizeof(uint32_t **));

    for (int i = 0; i < numberOfSlices-1; i++){
        result[i] = randomnessC[i];
    }
    result[numberOfSlices-1] = slice;

    fillArrayListWithData(env, result, slices, numberOfSlices);
    free(result);
    freeArrayOfArrays(randomnessC, numberOfSlices-1);
    freeArrayOfArrays(sharesC, weight);
    (*env)->ReleaseIntArrayElements(env, identifiers, identifiersC, 0);
}

JNIEXPORT jobject JNICALL
Java_com_project_collaborativeauthenticationapplication_service_crypto_CryptoExtendUnit_calculateMessageNative(
        JNIEnv *env, jobject thiz, jobject slices) {
    int numberOfSlices                 = getArrayListSize(env, slices);

    uint32_t ** slicesC    = (uint32_t **) malloc((numberOfSlices)*sizeof(uint32_t **));

    transformArrayListBigNumbersToCArray(env,  slices , slicesC, numberOfSlices);

    uint32_t message[SIZE];

    sum_mod_n(slicesC, message, numberOfSlices);



    freeArrayOfArrays(slicesC, numberOfSlices);

    return getNewBigNumberObject(env, message);

}