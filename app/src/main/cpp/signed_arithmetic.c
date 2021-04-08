//
// Created by yoshi on 24/02/21.
//


#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"
#include "mp_arithmetic.h"
#include "signed_arithmetic.h"
#include "mod_arithmetic.h"

void signed_add(uint32_t const * const a, uint32_t const * const b, uint32_t * const res, const uint32_t size)
{
    uint8_t op = 0;
    if(a[size]==1)
    {
        op = op + 1;
    }
    if (b[size]==1)
    {
        op = op +2;
    }
    uint32_t temp[2*SIZE + 1] = {0};
    switch(op)
    {
        case 0:
            res[size+1] = 0;
            mp_add(a,b, res, size);
            break;
        case 1:
            for (uint8_t i = 1; i  < size +1 ; i++)
            {
                if(a[size - i] > b[size - i]  )
                {
                    mp_sub(a,b,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =1;
                    break;
                }
                if(a[size - i] < b[size - i]  )
                {
                    mp_sub(b,a,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =0;
                    break;
                }
                if (i == size)
                {
                    for (uint8_t j = 0; j  < size + 2; j++)
                    {
                        res[j] = 0;
                    }
                }
            }
            break;
        case 2:
            for (uint8_t i = 1; i  < size +1 ; i++)
            {
                if(a[size - i] > b[size - i]  )
                {
                    mp_sub(a,b,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =0;
                    break;
                }
                if(a[size - i] < b[size - i]  )
                {
                    mp_sub(b,a,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =1;
                    break;
                }
                if (i == size)
                {
                    for (uint8_t j = 0; j  < size + 2; j++)
                    {
                        res[j] = 0;
                    }
                }
            }
            break;
        case 3:
            res[size+1] = 1;
            mp_add(a,b, res, size);
            break;
    }
}



void signed_sub(uint32_t const * const a, uint32_t const * const b, uint32_t * const res, const uint32_t size)
{
    uint8_t op = 0;
    if(a[size]==1)
    {
        op = op + 1;
    }
    if (b[size]==1)
    {
        op = op +2;
    }
    uint32_t temp[2*SIZE + 1] = {0};
    switch(op)
    {
        case 0:
            for (uint8_t i = 1; i  < size +1 ; i++)
            {
                if(a[size - i] > b[size - i]  )
                {
                    mp_sub(a,b,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =0;
                    break;
                }
                if(a[size - i] < b[size - i]  )
                {
                    mp_sub(b,a,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   = 0;
                    res[j+1] = 1;
                    break;
                }
                if (i == size)
                {
                    for (uint8_t j = 0; j  < size + 2; j++)
                    {
                        res[j] = 0;
                    }
                }
            }
            break;
        case 1:
            res[size+1] = 1;
            mp_add(a,b, res, size);
            break;
        case 2:
            res[size+1] = 0;
            mp_add(a,b, res, size);
            break;
        case 3:
            for (uint8_t i = 1; i  < size +1 ; i++)
            {
                if(a[ size - i] > b[size - i]  )
                {
                    mp_sub(a,b,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =1;
                    break;
                }
                if(a[size - i] < b[size - i]  )
                {
                    mp_sub(b,a,temp, size);
                    uint8_t j;
                    for(j=0; j < size; j++)
                    {
                        res[j] = temp[j];
                    }
                    res[j]   =0;
                    res[j+1] =0;
                    break;
                }
                if (i == size)
                {
                    for (uint8_t j = 0; j  < size + 2; j++)
                    {
                        res[j] = 0;
                    }
                }
            }
            break;
    }
}


void signed_mod(uint32_t const * const a, uint32_t * const res, const uint32_t size)
{
    if( a[size] == 0)
    {
        mod_p(a,res,size);
    }
    else
    {
        uint32_t addition[2*SIZE  + 1]  = {0,0,0,0,0,0,0,0,0};
        uint32_t additionr[2*SIZE + 2]  = {0,0,0,0,0,0,0,0,0,0};
        signed_add(a, P,additionr,size);
        for (uint8_t i =0; i < SIZE; i++)
        {
            res[i] = additionr[i];
        }
        while  (additionr[size+1] == 1)
        {
            for (uint8_t i =0; i < size + 1; i++)
            {
                addition[i] = additionr[i];
            }
            addition[size] = additionr[size+1];
            signed_add(addition, P,additionr,size);
            for (uint8_t i =0; i < SIZE; i++)
            {
                res[i] = additionr[i];
            }
        }
    }
}


void signed_mod_n(uint32_t const * const a, uint32_t * const res, const uint32_t size)
{
    if( a[size] == 0)
    {
        mod_n_size(a,res,size);
    }
    else
    {
        uint32_t addition[2*SIZE  + 1]  = {0,0,0,0,0,0,0,0,0};
        uint32_t additionr[2*SIZE + 2]  = {0,0,0,0,0,0,0,0,0,0};
        signed_add(a, N,additionr,size);
        for (uint8_t i =0; i < SIZE; i++)
        {
            res[i] = additionr[i];
        }
        while  (additionr[size+1] == 1)
        {
            for (uint8_t i =0; i < size + 1; i++)
            {
                addition[i] = additionr[i];
            }
            addition[size] = additionr[size+1];
            signed_add(addition, N,additionr,size);
            for (uint8_t i =0; i < SIZE; i++)
            {
                res[i] = additionr[i];
            }
        }
    }
}