//
// Created by yoshi on 24/02/21.
//


#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>


#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_SECRET_SHARING_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_SECRET_SHARING_H
    void calculate_share_part(uint32_t *x_values, uint32_t x_value, uint32_t nminus, uint32_t *share, uint32_t *res);
    void evaluate_poly(size_t *coefficients, uint32_t value, uint32_t degree, uint32_t *res);
    void calculate_share_part_for_x(uint32_t *x_values, uint32_t x_this, uint32_t x_target,  uint32_t nminus, uint32_t *share, uint32_t *res);
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_SECRET_SHARING_H
