//
// Created by yoshi on 23/03/21.
//
#include "common.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_POINT_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_POINT_H

    #define TRUE  1
    #define FALSE 0

    typedef struct Point
    {

        uint32_t   x[SIZE];
        uint32_t   y[SIZE];
        uint8_t    isZero;

    } Point;

    #define INIT_POINT(point) Point point = {{0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0}, FALSE}
    #define POINT_ZERO {{0,0,0,0,0,0,0,0}, {0,0,0,0,0,0,0,0}, TRUE}

#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_POINT_H
