//
// Created by yoshi on 24/02/21.
//

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNED_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNED_ARITHMETIC_H
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
void signed_sub(uint32_t *a, uint32_t *b, uint32_t *res, uint32_t size);
void signed_add(uint32_t *a, uint32_t *b, uint32_t *res, uint32_t size);
void signed_mod(uint32_t *a, uint32_t *res, uint32_t size);
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_SIGNED_ARITHMETIC_H
