//
// Created by yoshi on 24/02/21.
//

#include "inversion.h"
#include "common.h"
#include "mp_arithmetic.h"
#include "mod_arithmetic.h"
#include "signed_arithmetic.h"

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
void inverse_p(uint32_t *a, uint32_t *res)
{
    uint32_t xone[SIZE+2] = {1,0,0,0,0,0,0,0,0,0};	//xone[SIZE+1]: sign
    uint32_t xtwo[SIZE+2] = {0,0,0,0,0,0,0,0,0,0};
    uint32_t u[SIZE];
    uint32_t v[SIZE];
    for (uint8_t i =0; i < SIZE; i++)
    {
        u[i] = a[i];
        v[i] = P[i];
    }
    uint8_t  uone = 0;
    uint8_t  vone = 0;
    if (u[0] == 1)
    {
        uone = 1;
        for(uint8_t i=1; i < SIZE; i++)
        {
            if (u[i] != 0)
            {
                uone = 0;
                break;
            }
        }
    }
    while((uone + vone) == 0)
    {
        uint32_t fbu = u[0];
        while ((fbu&1) == 0)
        {

            ms_divide_2(u,u,SIZE);
            uint32_t fbx = xone[0];
            if ((fbx&1) == 1)
            {

                uint32_t xone_add_p[SIZE + 3];
                signed_add(xone, P, xone_add_p, SIZE +1);
                for (uint8_t j =0; j < SIZE+1; j++)
                {
                    xone[j] = xone_add_p[j];
                }
                xone[SIZE + 1] =  xone_add_p[SIZE + 2]; // copy sign
            }
            ms_divide_2(xone,xone,SIZE+1);
            fbu = u[0];
        }
        uint32_t fbv  = v[0];
        while ((fbv&1) == 0)
        {

            ms_divide_2(v,v,SIZE);
            uint32_t fbx = xtwo[0];
            if (fbx&1 == 1)
            {

                uint32_t xtwo_add_p[SIZE + 3];
                signed_add(xtwo, P, xtwo_add_p, SIZE +1);
                for (uint8_t j =0; j < SIZE+1; j++)
                {
                    xtwo[j] = xtwo_add_p[j];
                }
                xtwo[SIZE + 1] =  xtwo_add_p[SIZE + 2];
            }
            ms_divide_2(xtwo,xtwo,SIZE+1);
            fbv = v[0];
        }
        for (uint8_t i = 1; i  < SIZE +1 ; i++)
        {
            if(u[SIZE-i] > v[SIZE-i]  )
            {
                uint32_t uNew[SIZE + 1];
                uint32_t xOneNew[SIZE + 3];
                mp_sub(u,v,uNew,SIZE);
                signed_sub(xone,xtwo,xOneNew,SIZE +1);
                for (uint8_t j = 0; j < SIZE; j++)
                {
                    xone[j] = xOneNew[j];
                    u[j]    = uNew[j];
                }
                xone[SIZE]   = xOneNew[SIZE];
                xone[SIZE+1] = xOneNew[SIZE+2];
                break;
            }
            if(u[SIZE-i] < v[SIZE-i]  )
            {
                uint32_t vNew[SIZE + 1];
                uint32_t xTwoNew[SIZE + 3];
                mp_sub(v,u,vNew,SIZE);
                signed_sub(xtwo,xone,xTwoNew,SIZE +1);
                for (uint8_t j =0; j < SIZE; j++)
                {
                    xtwo[j] = xTwoNew[j];
                    v[j]    = vNew[j];
                }
                xtwo[SIZE]   = xTwoNew[SIZE];
                xtwo[SIZE+1] = xTwoNew[SIZE+2];
                break;
            }
            if (i == SIZE)
            {
                uint32_t uNew[SIZE + 1];
                uint32_t xOneNew[SIZE + 2];
                mp_sub(u,v,uNew,SIZE);
                signed_sub(xone,xtwo,xOneNew,SIZE +1);
                for (uint8_t j =0; j < SIZE; j++)
                {
                    xone[j] = xOneNew[j];
                    u[j]    = uNew[j];
                }
                xone[SIZE]   = xOneNew[SIZE];
                xone[SIZE+1] = xOneNew[SIZE+2];
            }
        }
        if (u[0] == 1)
        {
            uone = 1;
            for(uint8_t i=1; i < SIZE; i++)
            {
                if (u[i] != 0)
                {
                    uone = 0;
                    break;
                }
            }
        }
        if (v[0] == 1)
        {
            vone = 1;
            for(uint8_t i=1; i < SIZE; i++)
            {
                if (v[i] != 0)
                {
                    vone = 0;
                    break;
                }
            }
        }
    }
    uint32_t inverse[SIZE + 2];
    if( uone == 1)
    {
        for(uint8_t i=0; i < SIZE+2; i++)
        {
            inverse[i] = xone[i];
        }
    }
    else
    {
        for(uint8_t i=0; i < SIZE +2; i++)
        {
            inverse[i] = xtwo[i];
        }
    }
    signed_mod(inverse, res, SIZE + 1);
}

