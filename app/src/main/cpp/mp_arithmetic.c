//
// Created by yoshi on 24/02/21.
//

#include "mp_arithmetic.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr//include/stdint.h>
#include "common.h"


void mp_add(uint32_t const *const a, uint32_t const * const b, uint32_t *const res,  uint32_t const size)
{
    uint32_t carry = 0;
    uint8_t  i;
    for (i=0; i < size; i++)
    {
        uint64_t temp = (uint64_t) a[i] + b[i] + carry;
        res[i]        = (uint32_t) temp;
        carry         = (uint32_t) (temp >> WORD_SIZE);
    }
    res[size] = carry;
}


void mp_sub(uint32_t const * const a, uint32_t const * const b, uint32_t * const res,uint32_t const size)
{
    uint32_t carry = 0;
    uint8_t  i;
    for (i=0; i < size; i++)
    {
        res[i] = (uint32_t) (a[i] - b[i] - carry);
        if ((uint64_t) a[i] >= ((uint64_t) b[i] + (uint64_t) carry))
        {
            carry = 0;
        }
        else
        {
            carry = 1;
        }

    }
    res[size] = carry;
}


void ms_multiply(uint32_t const * const a, uint32_t const * const b, uint32_t * const res, uint32_t const  size)
{
    uint32_t rzero = 0;
    uint32_t rone  = 0;
    uint32_t rtwo  = 0;
    uint8_t   k;
    for (k=0; k < ((uint8_t) size)*2 -1;k ++)
    {
        for (uint8_t i = 0; i < (uint8_t) size; i++)
        {
            if (i > k)
            {
                break;
            }
            uint8_t j = k - i;
            if (j < (uint8_t) size)
            {
                uint64_t prod    = ((uint64_t) a[i])*((uint64_t)b[j]);
                uint64_t first   = ((uint64_t) rzero) + (prod & MAX);
                rzero            = (uint32_t) first;
                uint64_t second  = ((uint64_t) rone) + (prod >> WORD_SIZE) + (first>> WORD_SIZE);
                rone             = (uint32_t) second;
                rtwo             = rtwo  + ((uint32_t) (second >> WORD_SIZE));
            }
        }
        res[k] = rzero;
        rzero  = rone;
        rone   = rtwo;
        rtwo   = 0;
    }
    res[k] = rzero;
}


void ms_divide_2(uint32_t const * const a, uint32_t * const res, uint32_t const  size)
{
    uint32_t result[2*SIZE +1];
    uint32_t olb = 0;
    for (uint8_t i = 1; i < size +1; i++)
    {
        uint32_t current     = a[size - i];
        uint32_t lb          = ((current&1) << (WORD_SIZE -1)) ;
        result[size - i]     = (current  >> 1) + olb;
        olb                  = lb;
    }
    for (uint8_t i =0; i < size; i++)
    {
        res[i] = result[i];
    }
}