//
// Created by yoshi on 24/02/21.
//

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>

uint32_t P[17];

void add_mod_p(uint32_t *a, uint32_t *b, uint32_t *res);
void sub_mod_p(uint32_t *a, uint32_t *b, uint32_t *res);
void multiply_mod_p(uint32_t *a, uint32_t *b, uint32_t *res);
void multiply_mod_p_32(uint32_t *a, uint32_t b, uint32_t *res);
void mod_p(uint32_t *a, uint32_t *res, uint32_t size);
void sum_mod_p(size_t *a, uint32_t *res,uint32_t n);
void additive_inverse_mod_p(uint32_t *a, uint32_t *res);


#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_MOD_ARITHMETIC_H
