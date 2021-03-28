//
// Created by yoshi on 23/03/21.
//

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_HASH_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_HASH_H

    #define Hzero        0x6a09e667
    #define Hone         0xbb67ae85
    #define Htwo         0x3c6ef372
    #define Hthree       0xa54ff53a
    #define Hfour        0x510e527f
    #define Hfive        0x9b05688c
    #define Hsix         0x1f83d9ab
    #define Hseven       0x5be0cd19

    #define PADDON       448
    #define BL           512
    #define MODFHT       0x1FF
    #define MODTT        0x1F
    #define BYTESPBL     64


    #define ROLL(a,b) (((a) << (b)) | ((a) >> (32-(b))))
    #define ROLR(a,b) (((a) >> (b)) | ((a) << (32-(b))))
    #define CH(x,y,z) (((x) & (y)) ^ (~(x) & (z)))
    #define MAJ(x,y,z) (((x) & (y)) ^ ((x) & (z)) ^ ((y) & (z)))
    #define SUM0(x) (ROLR(x,2)  ^ ROLR(x,13) ^ ROLR(x,22))
    #define SUM1(x) (ROLR(x,6)  ^ ROLR(x,11) ^ ROLR(x,25))
    #define SIG0(x) (ROLR(x,7)  ^ ROLR(x,18) ^ ((x) >> 3))
    #define SIG1(x) (ROLR(x,17) ^ ROLR(x,19) ^ ((x) >> 10))


    void sha(uint32_t const * const message, uint32_t const bitlength, uint32_t * const res);

#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_HASH_H
