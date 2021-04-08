//
// Created by yoshi on 06/04/21.
//


#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"
#include "ecc_point.h"

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_THRESHOLD_SIGNATURE_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_THRESHOLD_SIGNATURE_H
    void Calculate_commitments_to_random_numbers(uint32_t **e, uint32_t **d, Point *E, Point *D, uint32_t number);
    void calculate_rho(uint32_t *identifiers, Point *E, Point *D, uint32_t * message, uint32_t number_of_participants, uint32_t **rho);
    void calculate_random_nonce(Point *E, Point *D, uint32_t number_of_participants, uint32_t **rho, Point *R);
    void calculate_hash_and_nonce_and_rhos(uint32_t number_of_participants, Point *E, Point *D, uint32_t *all_identifiers, uint32_t * message, uint32_t * hash, Point * R, uint32_t ** rhos);
    void produce_signature_shares_for_subset(uint32_t subset_size, uint32_t number_of_participants, Point *R, uint32_t *all_identifiers, uint32_t *subset_identifiers_indeces,
                                             uint32_t ** subset_e, uint32_t ** subset_d, uint32_t **rhos, uint32_t ** shares, uint32_t * hash, uint32_t * signature);
    void calculate_hash_threshold(Point * p, uint32_t const * const message, const uint32_t message_length, uint32_t * const hash);
    void sign_share(uint32_t *hash, uint32_t * const share, uint32_t index, uint32_t *identifiers, uint32_t *e, uint32_t *d, uint32_t *rho, uint32_t number_of_participants, uint32_t * signature);
    uint8_t verify_threshold(uint32_t const * const message, uint32_t const message_length, Point * public_key, uint32_t ** const signature);
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_THRESHOLD_SIGNATURE_H
