





#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>

    void mp_add(uint32_t *a, uint32_t *b, uint32_t *res, uint32_t size);
    void mp_sub(uint32_t *a, uint32_t *b, uint32_t *res,uint32_t size);
    void ms_multiply(uint32_t *a, uint32_t *b, uint32_t *res, uint32_t size);
    void ms_divide_2(uint32_t *a, uint32_t *res, uint32_t size);

#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H
