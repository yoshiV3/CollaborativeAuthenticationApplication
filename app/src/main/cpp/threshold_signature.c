//
// Created by yoshi on 06/04/21.
//

#include "threshold_signature.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"
#include "ecc_arithmetic.h"
#include "array.h"
#include "hash.h"
#include "mod_arithmetic.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>
#include "secret_sharing.h"


void Calculate_commitments_to_random_numbers(uint32_t ** e, uint32_t ** d, Point *E, Point *D, uint32_t number){
    Point A[256];
    Point B[256];
    generate_exponentiation_lookup_table( A,B);
    for (uint32_t i =0; i <number; i++){
        ecc_exponentiation(A, B, e[i], &E[i]);
        ecc_exponentiation(A, B, d[i], &D[i]);
    }
}


void calculate_rho(uint32_t *identifiers, Point *E, Point *D, uint32_t * message, uint32_t number_of_participants, uint32_t ** rho){
    uint32_t   message_length     = SIZE + 1 + number_of_participants*(4*SIZE);
    uint32_t   message_length_bit = message_length*32;
    uint32_t * messageAndB        = malloc(message_length*sizeof(uint32_t));
    for (uint32_t index =0; index <SIZE; index++){
        messageAndB[index] = message[index];
    }
    for (uint32_t x= 0; x < number_of_participants; x++){
        messageAndB[SIZE] = identifiers[x];
        uint32_t currentIndex = SIZE +1;
        for (uint32_t i = 0; i < number_of_participants; i++){
            for (uint32_t index =0; index <SIZE; index++){
                messageAndB[currentIndex+index] = E[i].x[index];
            }
            currentIndex += SIZE;
            for (uint32_t index =0; index <SIZE; index++){
                messageAndB[currentIndex+index] = E[i].y[index];
            }
            currentIndex += SIZE;
            for (uint32_t index =0; index <SIZE; index++){
                messageAndB[currentIndex+index] = D[i].x[index];
            }
            currentIndex += SIZE;
            for (uint32_t index =0; index <SIZE; index++){
                messageAndB[currentIndex+index] = D[i].y[index];
            }
            currentIndex += SIZE;
        }
        rho[x] = (uint32_t  *) malloc(SIZE*sizeof(uint32_t));
        sha(messageAndB, message_length_bit, rho[x]);
    }
    free(messageAndB);
}


void calculate_random_nonce(Point *E, Point *D, uint32_t number_of_participants, uint32_t **rho, Point *R){
    R->isZero = TRUE;
    for (uint32_t x=0; x < number_of_participants; x++){
        add_points(R, &D[x], R);
        Point Erho;
        ecc_exponentiation_mont(&E[x], rho[x], &Erho);
        add_points(R, &Erho, R);
    }
}

void calculate_hash_and_nonce_and_rhos(uint32_t number_of_participants, Point *E, Point *D, uint32_t *all_identifiers, uint32_t * message, uint32_t * hash, Point * R, uint32_t ** rhos){
    calculate_rho(all_identifiers, E, D, message, number_of_participants, rhos);
    calculate_random_nonce(E, D, number_of_participants, rhos, R);
    calculate_hash_threshold(R, message, SIZE, hash);
}

void produce_signature_shares_for_subset(uint32_t subset_size, uint32_t number_of_participants, Point *R, uint32_t *all_identifiers, uint32_t *subset_identifiers_indeces,
                                         uint32_t ** subset_e, uint32_t ** subset_d, uint32_t **rhos, uint32_t ** shares, uint32_t * hash, uint32_t * signature){
    MAKE_ZERO(signature, SIZE);
    for(uint32_t x =0; x <	subset_size; x++){
        uint32_t signature_share[SIZE];
        sign_share(hash, shares[x],  subset_identifiers_indeces[x], all_identifiers, subset_e[x], subset_d[x], rhos[subset_identifiers_indeces[x]], number_of_participants, signature_share);
        add_mod_n(signature_share, signature, signature);
    }
}



void calculate_hash_threshold(Point * p, uint32_t const * const message, const uint32_t message_length, uint32_t * const hash){
    uint32_t * const hash_message = (uint32_t *) malloc((message_length+16)*sizeof(uint32_t));
    const uint32_t bit_length_message = (message_length+16)*32;
    COPY(hash_message, message, message_length);
    COPY(hash_message+message_length, p->x, SIZE);
    COPY(hash_message+message_length+SIZE, p->y, SIZE);
    sha(hash_message, bit_length_message , hash);
    free(hash_message);
}

void sign_share(uint32_t *hash, uint32_t * const share, uint32_t index, uint32_t *identifiers, uint32_t *e, uint32_t *d, uint32_t *rho, uint32_t number_of_participants, uint32_t * signature)
{
    uint32_t hash_n[SIZE];
    uint32_t secr_n[SIZE];
    uint32_t rho_n[SIZE];
    uint32_t part_p[SIZE];
    uint32_t nonce[SIZE];
    uint32_t product[SIZE];

    uint32_t   nMinus   = (number_of_participants - 1) ;
    uint32_t * x_values = malloc(nMinus*sizeof(uint32_t));

    for (uint32_t i = 0; i<number_of_participants; i++){
        if (index>i){
            x_values[i] = identifiers[i];
        }
        else if (index< i){
            x_values[i-1] = identifiers[i];
        }
    }
    calculate_share_part_n(x_values, identifiers[index], nMinus, share, secr_n);

    mod_n(hash,   hash_n);
    mod_n(rho,    rho_n);

    multiply_mod_n(hash_n, secr_n, product);
    multiply_mod_n(rho_n, e, nonce);
    add_mod_n(nonce, d, nonce);
    sub_mod_n(nonce, product, signature);


    //signature[0] = x_values[0];
    //signature[1] = identifiers[index];
    //signature[2] = nMinus;

    free(x_values);
}



void combine_shares(uint32_t ** signatures, uint32_t number_of_shares, uint32_t *res){
    uint32_t s[SIZE] = {0};
    for(uint32_t i =0; i <number_of_shares; i++){
        add_mod_n(s, signatures[i], s);
    }
    COPY(res, s, SIZE);
}



uint8_t verify_threshold(uint32_t const * const message, uint32_t const message_length, Point * public_key, uint32_t ** const signature){
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
    calculate_hash_threshold(&g_r, message, message_length, hash);

    for(uint8_t index = 0; index < SIZE; index++){
        if (signature[1][index] != hash[index]){
            return 0;
        }
    }
    return 1;
}