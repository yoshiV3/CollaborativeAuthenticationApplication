//
// Created by yoshi on 24/02/21.
//

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"


#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
    #define K 9
    const uint32_t P[2*SIZE + 1];
    const uint32_t N[2*SIZE + 1];
    void add_mod_p(uint32_t const * const a, uint32_t const * const b, uint32_t * const res);
    void multiply_mod_n(uint32_t const * const a, uint32_t const  * const b, uint32_t * const res);
    void sub_mod_p(uint32_t const * const a, uint32_t const * const b, uint32_t * const res);
    void multiply_mod_p(uint32_t const * const a, uint32_t const  * const b, uint32_t * const res);
    void multiply_mod_p_32(uint32_t const * const a, const uint32_t b, uint32_t * const res);
    void mod_p(uint32_t const * const a, uint32_t * const res, const uint32_t size);
    void sum_mod_p(uint32_t const ** const a, uint32_t * const res, const uint32_t n);
    void additive_inverse_mod_p(uint32_t const * const a, uint32_t * const res);
    void multiply_mod_n(uint32_t const * const a, uint32_t const  * const b, uint32_t * const res);
    void sub_mod_n(uint32_t const * const a, uint32_t const * const b, uint32_t * const res);
    void add_mod_n(uint32_t const * const a, uint32_t const * const b, uint32_t * const res);
    void mod_n(uint32_t const * const a, uint32_t * const res);
    void mod_n_size(uint32_t const * const a, uint32_t * const res,  const uint32_t size);
    void multiply_mod_n_32(uint32_t const * const a, const uint32_t b, uint32_t * const res);
    void sum_mod_n(uint32_t const ** const a, uint32_t * const res, const uint32_t n);

    #define BIG_SHIFT(in_arr, out_arr,  shift)\
                            {\
                                size_t i = 0;\
                                for(size_t index = (shift); index <2*SIZE; index++){\
                                    out_arr[i] = in_arr[index];\
                                    i++; \
                                }\
                            }


#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
