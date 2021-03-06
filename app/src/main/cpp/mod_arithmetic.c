//
// Created by yoshi on 24/02/21.
//

#include "mod_arithmetic.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"
#include "mp_arithmetic.h"
#include "signed_arithmetic.h"
#include "array.h"

void barret_reduction_n(uint32_t  * const a, uint32_t * const res);
void barret_reduction_preprocessing(uint32_t  * const signed_a, uint32_t * const res);

const uint32_t P[2*SIZE + 1] =  {0xFFFFFC2F, 0xFFFFFFFE, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF,0,0,0,0,0,0,0,0,0};
const uint32_t N[2*SIZE + 1] =  {3493216577, 3218235020, 2940772411, 3132021990, 4294967294, 4294967295, 4294967295, 4294967295,0,0,0,0,0,0,0,0,0};
const uint32_t B[2*SIZE + 1] =  {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0,0};

void mod_p(uint32_t const * const a, uint32_t * const res, const uint32_t size)
{
    uint32_t substraction[2*SIZE  + 1]  = {0,0,0,0,0,0,0,0,0};
    uint32_t substractionr[2*SIZE +2]  = {0,0,0,0,0,0,0,0,0,0};
    mp_sub(a, P,substractionr,size);
    for (uint8_t i =0; i < SIZE; i++)
    {
        res[i] = a[i];
    }
    while  (substractionr[size] == 0)
    {
        for (uint8_t i =0; i < SIZE; i++)
        {
            res[i] = substractionr[i];
        }
        for (uint8_t i =0; i < size + 1; i++)
        {
            substraction[i] = substractionr[i];
        }
        mp_sub(substraction, P,substractionr,size);
    }
}



void mod_n(uint32_t const * const a, uint32_t * const res)
{
    uint32_t temp[SIZE+1];
    mp_sub(a, N, temp,SIZE);
    if (temp[SIZE] == 1)
    {
        COPY(res, a, SIZE);
    }
    else
    {
        COPY(res, temp, SIZE);
    }
}


void mod_n_size(uint32_t const * const a, uint32_t * const res, const uint32_t size)
{
    uint32_t substraction[2*SIZE  + 1]  = {0,0,0,0,0,0,0,0,0};
    uint32_t substractionr[2*SIZE +2]  = {0,0,0,0,0,0,0,0,0,0};
    mp_sub(a, N,substractionr,size);
    for (uint8_t i =0; i < SIZE; i++)
    {
        res[i] = a[i];
    }
    while  (substractionr[size] == 0)
    {
        for (uint8_t i =0; i < SIZE; i++)
        {
            res[i] = substractionr[i];
        }
        for (uint8_t i =0; i < size + 1; i++)
        {
            substraction[i] = substractionr[i];
        }
        mp_sub(substraction, N,substractionr,size);
    }
}


void add_mod_p(uint32_t const * const a, uint32_t const * const b, uint32_t * const res)
{
    uint32_t tempADD[SIZE+1];
    mp_add(a,b,tempADD,SIZE);
    uint32_t tempSUB[SIZE+2];
    mp_sub(tempADD, P, tempSUB,SIZE + 1);
    if (tempSUB[SIZE + 1] == 1)
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempADD[i];
        }
    }
    else
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempSUB[i];
        }
    }
}

void sub_mod_p(uint32_t const * const a, uint32_t const * const b, uint32_t * const res)
{
    uint32_t tempSUB[SIZE+1];
    uint32_t tempADD[SIZE+1];
    mp_sub(a,b,tempSUB,SIZE);
    mp_add(tempSUB, P, tempADD, SIZE);
    if (tempSUB[SIZE] == 1)
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempADD[i];
        }
    }
    else
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempSUB[i];
        }
    }
}



void sub_mod_n(uint32_t const * const a, uint32_t const * const b, uint32_t * const res)
{
    uint32_t tempSUB[SIZE+1];
    uint32_t tempADD[SIZE+1];
    mp_sub(a,b,tempSUB,SIZE);
    mp_add(tempSUB, N, tempADD, SIZE);
    if (tempSUB[SIZE] == 1)
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempADD[i];
        }
    }
    else
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempSUB[i];
        }
    }
}


void multiply_mod_p(uint32_t const * const a, uint32_t const  * const b, uint32_t * const res)
{
    uint32_t prod[2*SIZE];
    uint32_t prodr[SIZE+1] = {0,0,0,0,0,0,0,0,0};
    uint64_t prodc;
    uint32_t lower;
    uint32_t higher;
    uint64_t sum;
    ms_multiply(a,b,prod,SIZE);
    prodc          = ((uint64_t)prod[2*SIZE -1])*((uint64_t) CONSTANT); //c15*977 (should be shifted by 7)
    lower          = (uint32_t) prodc;
    higher         = (uint32_t) (prodc >> WORD_SIZE);
    prodr[SIZE]    =    higher;
    prodr[SIZE-1]  =    lower;
    sum            =    ((uint64_t) lower) + ((uint64_t) prod[0]);
    prodr[0]       =    (uint32_t) sum ;
    sum            =    ((uint64_t) higher) +  ((uint64_t) prod[15]) + (sum >> 32);
    prodr[1]       =    (uint32_t) sum;
    prodr[2]       =    (uint32_t) (sum >> WORD_SIZE);
    uint32_t index = 7;
    for (uint8_t shift = 7; shift > 0 ; shift --)
    {
        prodc           = ((uint64_t)prod[index + shift])*((uint64_t) CONSTANT);
        lower           = (uint32_t) prodc;
        higher          = (uint32_t) (prodc >> WORD_SIZE);
        sum             = ((uint64_t) lower) + ((uint64_t) prodr[shift -1] );
        prodr[shift -1] = (uint32_t) sum;
        sum             = ((uint64_t) higher) + ((uint64_t) prodr[shift]) + ( sum  >> WORD_SIZE) + ((uint64_t) prod[index + shift]) + ((uint64_t) prod[shift]);
        prodr[shift]    = (uint32_t) sum;
        for (uint8_t i = shift +1; i < SIZE +1; i++)
        {
            uint64_t rest = ( sum  >> WORD_SIZE);
            if (rest == 0)
            {
                break;
            }
            sum = ((uint64_t) prodr[i]) + rest;
            prodr[i] = (uint32_t) sum;
        }
    }
    mod_p(prodr, res, SIZE +1);
}

void multiply_mod_n(uint32_t const * const a, uint32_t const  * const b, uint32_t * const res){
    uint32_t prod[2*SIZE];
    ms_multiply(a,b,prod,SIZE);
    barret_reduction_n(prod, res);
}


void multiply_mod_p_32(uint32_t const * const a, const uint32_t b, uint32_t * const res)
{
    uint32_t prod[SIZE+1] = {0};
    uint64_t rest = 0;
    for (uint8_t i =0; i < SIZE; i++)
    {
        uint64_t pr      = ((uint64_t) a[i])*((uint64_t) b);
        uint32_t lower   = (uint32_t) pr;
        uint32_t higher  = (uint32_t) (pr >> WORD_SIZE);
        uint64_t sum     = ((uint64_t)lower) + ((uint64_t)prod[i]);
        prod[i]          = (uint32_t) sum;
        sum              = (sum >> WORD_SIZE) + ((uint64_t) higher) + rest;
        prod[i+1]        = (uint32_t) sum;
        rest             = sum >> WORD_SIZE;
    }
    // Reduction
    //c*977
    uint32_t reducedT[8] = {0};
    uint32_t reducedR[9] = {0};
    uint64_t prc         =  ((uint64_t) prod[SIZE])*CONSTANT;
    reducedT[0]          =  (uint32_t) prc;
    uint64_t sum         =  (prc >> WORD_SIZE) +  ((uint64_t) prod[SIZE]);
    reducedT[1]          =  (uint32_t) sum;
    reducedT[2]          = (uint32_t) (sum >> WORD_SIZE);
    add_mod_p(reducedT,prod, reducedR);
    for (uint8_t i =0; i < SIZE; i++)
    {
        res[i] = reducedR[i];
    }
}


void multiply_mod_n_32(uint32_t const * const a, const uint32_t b, uint32_t * const res)
{
    uint32_t prod[2*SIZE] = {0};
    uint64_t rest = 0;
    for (uint8_t i =0; i < SIZE; i++)
    {
        uint64_t pr      = ((uint64_t) a[i])*((uint64_t) b);
        uint32_t lower   = (uint32_t) pr;
        uint32_t higher  = (uint32_t) (pr >> WORD_SIZE);
        uint64_t sum     = ((uint64_t)lower) + ((uint64_t)prod[i]);
        prod[i]          = (uint32_t) sum;
        sum              = (sum >> WORD_SIZE) + ((uint64_t) higher) + rest;
        prod[i+1]        = (uint32_t) sum;
        rest             = sum >> WORD_SIZE;
    }
    // Reduction
    barret_reduction_n(prod, res);
}



void sum_mod_p(uint32_t const ** const a, uint32_t * const res, const uint32_t n)
{
    uint32_t si[SIZE] = {0};
    uint32_t sr[SIZE] = {0};
    for (uint32_t i =0; i < n; i++)
    {
        uint32_t *current = (uint32_t *) a[i];
        add_mod_p(si, current, sr);
        for (uint8_t j=0; j < SIZE; j++)
        {
            si[j] = sr[j];
        }
    }
    for (uint8_t j=0; j < SIZE ; j++)
    {
        res[j] = sr[j];
    }
}


void additive_inverse_mod_p(uint32_t const * const a, uint32_t * const res)
{
    mp_sub(P, a, res, SIZE);
}





void barret_reduction_n(uint32_t  * const a, uint32_t * const res){
    uint32_t a_signed[2*SIZE+1]        = {0};
    uint32_t a_reduced_zero[2*SIZE+2]  = {0};
    uint32_t a_reduced_one[2*SIZE+2]   = {0};
    uint32_t a_reduced_two[2*SIZE+2]   = {0};
    COPY(a_signed, a, 2*SIZE);
    barret_reduction_preprocessing(a_signed, a_reduced_zero);
    barret_reduction_preprocessing(a_reduced_zero, a_reduced_zero);
    signed_sub(a_reduced_zero,N, a_reduced_one, 2*SIZE);
    a_reduced_one[2*SIZE] = a_reduced_one[2*SIZE+1];
    signed_sub(a_reduced_one,N, a_reduced_two, 2*SIZE);
    a_reduced_two[2*SIZE] = a_reduced_two[2*SIZE+1];
    if (a_reduced_one[2*SIZE] == 1){
        COPY(res,a_reduced_zero, SIZE);
    }
    else if (a_reduced_two[2*SIZE] == 1){
        COPY(res,a_reduced_one, SIZE);
    }
    else{
        COPY(res,a_reduced_two, SIZE);
    }
}

void barret_reduction_preprocessing(uint32_t  * const signed_a, uint32_t * const res){
    uint32_t q_estimate[K-1]         = {0};
    uint32_t residual[2*SIZE + 2]    = {0};
    uint32_t product[2*SIZE  + 1]    = {0};
    uint32_t result[2*SIZE+2]        = {0};

    BIG_SHIFT(signed_a, q_estimate, K-1);

    ms_multiply(N, q_estimate, product, SIZE);
    signed_sub(signed_a,product, residual, 2*SIZE);
    residual[2*SIZE] = residual[2*SIZE+1];
    signed_add(residual,B,result, 2*SIZE);
    if (residual[2*SIZE] == 1){
        COPY(res, result, 2*SIZE);
    }
    else{
        COPY(res,residual, 2*SIZE)
    }
}

void add_mod_n(uint32_t const * const a, uint32_t const * const b, uint32_t * const res){
    uint32_t tempADD[SIZE+1];
    uint32_t tempSUB[SIZE+2];
    mp_add(a,b,tempADD,SIZE);
    mp_sub(tempADD, N, tempSUB, SIZE+1);
    if (tempSUB[SIZE+1] == 1)
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempADD[i];
        }
    }
    else
    {
        for (uint8_t i=0; i < SIZE ; i++)
        {
            res[i] = tempSUB[i];
        }
    }
}

void sum_mod_n(uint32_t const ** const a, uint32_t * const res, const uint32_t n)
{
    uint32_t si[SIZE] = {0};
    uint32_t sr[SIZE] = {0};
    for (uint32_t i =0; i < n; i++)
    {
        uint32_t *current = (uint32_t *) a[i];
        add_mod_n(si, current, sr);
        for (uint8_t j=0; j < SIZE; j++)
        {
            si[j] = sr[j];
        }
    }
    for (uint8_t j=0; j < SIZE ; j++)
    {
        res[j] = sr[j];
    }
}
