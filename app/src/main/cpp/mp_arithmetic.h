





#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>

    void mp_add(uint32_t const *const a, uint32_t const * const b, uint32_t *const res,  uint32_t const size);
    void mp_sub(uint32_t const * const a, uint32_t const * const b, uint32_t * const res,uint32_t const size);
    void ms_multiply(uint32_t const * const a, uint32_t const * const b, uint32_t * const res, uint32_t const  size);
    void ms_divide_2(uint32_t const * const a, uint32_t * const res, uint32_t const  size);

#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_MP_ARITHMETIC_H
