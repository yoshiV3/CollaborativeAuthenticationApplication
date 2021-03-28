//
// Created by yoshi on 23/03/21.
//

#include "ecc_point.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"


#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_ARITHMETIC_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_ARITHMETIC_H

    void point_doubling(Point const * const p, Point * const res);
    void add_points(Point const * const p, Point  const * const q, Point * const res);
    void sum_points(Point const * const P, uint32_t number, Point * const res);
    void generate_exponentiation_lookup_table(Point * const A, Point * const B);
    void ecc_exponentiation(Point const * const A,  Point const * const B, uint32_t const  * const exponent, Point * const res);
    void ecc_exponentiation_mont(const Point * const p, uint32_t const  * const exponent, Point * const res);
    #define START_MASK_A 32768
    #define START_MASK_B 2147483648
    #define COMPARE(x1,x2, size, equal)\
                            {\
                            equal = 1;\
                            for (uint8_t i=0; i<size; i++)\
                            {\
                                if (x1[i] != x2[i])\
                                {\
                                    equal = 0; \
                                }\
                            }\
                           }
    #define COPY_COOR(cx, cy, point)\
                               {\
                                        COPY((cx), (point->x), SIZE);\
                                        COPY((cy), (point)->y, SIZE);\
                               }
    #define COPY_POINT(dest,src)\
                            {\
                                (dest).isZero = (src).isZero;\
                                COPY((dest).x, (src).x, SIZE);\
                                COPY((dest).y, (src).y, SIZE);\
                            }
    #define GET_BITS(n,mask_a, mask_b, bits_a, bits_b)\
                            {\
                                bits_a =0;\
                                bits_b =0;\
                                uint8_t currentPart = 1;\
                                for(uint8_t integer =0; integer <SIZE; integer++){\
                                    if (n[integer] & mask_a) {\
                                        bits_a += currentPart;\
                                    }\
                                    if(n[integer] & mask_b) {\
                                        bits_b += currentPart;\
                                    }\
                                    currentPart <<= 1;\
                                }\
                            }


#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_ECC_ARITHMETIC_H
