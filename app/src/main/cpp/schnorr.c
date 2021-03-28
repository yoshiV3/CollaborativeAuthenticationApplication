//
// Created by yoshi on 23/03/21.
//

#include "schnorr.h"
#include "ecc_point.h"
#include "ecc_arithmetic.h"
#include "mod_arithmetic.h"
#include "array.h"
#include "hash.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>


void calculate_hash(Point * p, uint32_t const * const message, const uint32_t message_length, uint32_t * const hash);

void sign(uint32_t const * const message, uint32_t const length_message, uint32_t  const * const nonce, uint32_t const * const secret,  uint32_t ** const signature){
    Point A[256];
    Point B[256];
    Point R;
    generate_exponentiation_lookup_table( A,B);
    ecc_exponentiation(A, B, nonce, &R);

    uint32_t hash[SIZE];

    calculate_hash(&R, message, length_message, hash);

    uint32_t hash_n[SIZE];
    uint32_t secr_n[SIZE];
    uint32_t product[SIZE];

    mod_n(hash,   hash_n);
    mod_n(secret, secr_n);

    multiply_mod_n(hash_n, secr_n, product);

    sub_mod_n(nonce, product, signature[0]);
    COPY(signature[1], hash, SIZE);
}




uint8_t verify(uint32_t const * const message, uint32_t const message_length, Point * public_key, uint32_t ** const signature){
    Point A[256];
    Point B[256];
    Point g_s;
    Point y_e;
    Point g_r;
    generate_exponentiation_lookup_table( A,B);
    ecc_exponentiation(A, B, signature[0], &g_s);
    ecc_exponentiation_mont(public_key, signature[1], &y_e);
    add_points(&g_s, &y_e, &g_r);

    uint32_t hash[SIZE];
    calculate_hash(&g_r, message, message_length, hash);

    for(uint8_t index = 0; index < SIZE; index++){
        if (signature[1][index] != hash[index]){
            return 0;
        }
    }
    return 1;
}



void calculate_hash(Point * p, uint32_t const * const message, const uint32_t message_length, uint32_t * const hash){
    uint32_t * const hash_message = (uint32_t *) malloc((message_length+16)*sizeof(uint32_t));
    const uint32_t bit_length_message = (message_length+16)*32;
    COPY(hash_message, message, message_length);
    COPY(hash_message+message_length, p->x, SIZE);
    COPY(hash_message+message_length+SIZE, p->y, SIZE);
    sha(hash_message, bit_length_message , hash);
    free(hash_message);
}
