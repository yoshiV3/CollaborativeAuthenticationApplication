//
// Created by yoshi on 24/02/21.
//

#include "secret_sharing.h"
#include "inversion.h"
#include "common.h"
#include "mp_arithmetic.h"
#include "mod_arithmetic.h"
#include "signed_arithmetic.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>



void evaluate_poly(uint32_t ** coefficients, uint32_t value, uint32_t degree, uint32_t *res)
{
    uint32_t old[SIZE];
    uint32_t *coef  = coefficients[0];
    for(uint8_t i = 0; i < SIZE; i++)
    {
        old[i] =  coef[i];
    }
    uint32_t exp;
    for (exp = 1; exp < degree +1 ; exp ++)
    {
        uint32_t product[SIZE];
        multiply_mod_p_32(old, value, product);
        coef  = (uint32_t *)  coefficients[exp];
        add_mod_p(product, coef, old);
    }



    for(uint8_t i = 0; i < SIZE; i++)
    {
        res[i] = old[i];
    }
}



void evaluate_poly_n(uint32_t ** coefficients, uint32_t value, uint32_t degree, uint32_t *res)
{
    uint32_t old[SIZE];
    uint32_t *coef  = coefficients[0];
    for(uint8_t i = 0; i < SIZE; i++)
    {
        old[i] =  coef[i];
    }
    uint32_t exp;
    for (exp = 1; exp < degree +1 ; exp ++)
    {
        uint32_t product[SIZE];
        multiply_mod_n_32(old, value, product);
        coef  = (uint32_t *)  coefficients[exp];
        add_mod_n(product, coef, old);
    }
    for(uint8_t i = 0; i < SIZE; i++)
    {
        res[i] = old[i];
    }
}



void calculate_share_part(uint32_t *x_values, uint32_t x_value, uint32_t nminus, uint32_t *share, uint32_t *res)
{
    uint32_t x_own[SIZE+1] = {0};
    uint32_t x_oth[SIZE+1] = {0}; //sign x[SIZE]
    uint32_t l_int[SIZE]   = {0};
    uint32_t x_oth_mr[SIZE+2];
    uint32_t x_oth_m[SIZE+1];
    uint32_t x_oth_mm[SIZE];
    uint32_t x_low[SIZE];
    uint32_t p_int[SIZE];

    for (uint8_t i=0; i < SIZE ; i++)
    {
        l_int[i] = share[i]; //copy share
    }


    x_own[0]   = x_value; //big number from own identifier


    for (uint32_t x=0; x < nminus ; x++)
    {
        x_oth[0] = x_values[x]; //big number from input other identifiers

        signed_sub(x_oth, x_own, x_oth_mr, SIZE); //xj-xi
        uint8_t i;
        for (i= 0; i < SIZE; i++)
        {
            x_oth_m[i] = x_oth_mr[i]; //copy results for resizing of the big number representation
        }
        x_oth_m[i] = x_oth_mr[i+1];

        signed_mod(x_oth_m, x_oth_mm,SIZE); //modulo result

        inverse_p(x_oth_mm,x_low); //1:(xj-xi)

        multiply_mod_p(x_low, l_int, p_int);  //s*(1/(xj-xi))
        multiply_mod_p(p_int, x_oth, l_int); //(s*(1/(xj-xi)))*xj = s((xj)/(xj-xi))
    }
    for (uint8_t i=0; i < SIZE ; i++)
    {
        res[i] = l_int[i];
    }
}



void calculate_share_part_n(uint32_t *x_values, uint32_t x_value, uint32_t nminus, uint32_t *share, uint32_t *res)
{
    uint32_t x_own[SIZE+1] = {0};
    uint32_t x_oth[SIZE+1] = {0}; //sign x[SIZE]
    uint32_t l_int[SIZE]   = {0};
    uint32_t x_oth_mr[SIZE+2];
    uint32_t x_oth_m[SIZE+1];
    uint32_t x_oth_mm[SIZE];
    uint32_t x_low[SIZE];
    uint32_t p_int[SIZE];

    for (uint8_t i=0; i < SIZE ; i++)
    {
        l_int[i] = share[i]; //copy share
    }


    x_own[0]   = x_value; //big number from own identifier


    for (uint32_t x=0; x < nminus ; x++)
    {
        x_oth[0] = x_values[x]; //big number from input other identifiers

        signed_sub(x_oth, x_own, x_oth_mr, SIZE); //xj-xi
        uint8_t i;
        for (i= 0; i < SIZE; i++)
        {
            x_oth_m[i] = x_oth_mr[i]; //copy results for resizing of the big number representation
        }
        x_oth_m[i] = x_oth_mr[i+1];

        signed_mod_n(x_oth_m, x_oth_mm,SIZE); //modulo result

        inverse_n(x_oth_mm,x_low); //1:(xj-xi)

        multiply_mod_n(x_low, l_int, p_int);  //s*(1/(xj-xi))
        multiply_mod_n(p_int, x_oth, l_int); //(s*(1/(xj-xi)))*xj = s((xj)/(xj-xi))
    }
    for (uint8_t i=0; i < SIZE ; i++)
    {
        res[i] = l_int[i];
    }
}




void calculate_share_part_for_x(uint32_t *x_values, uint32_t x_this, uint32_t x_target,  uint32_t nminus, uint32_t *share, uint32_t *res)
{
    uint32_t x_own[SIZE+1]    = {0};
    uint32_t x_tar[SIZE]      = {0};
    uint32_t x_oth[SIZE+1]    = {0}; //sign x[SIZE]
    uint32_t l_int[SIZE]      = {0};
    uint32_t x_oth_mr[SIZE+2];
    uint32_t x_oth_m[SIZE+1];
    uint32_t x_oth_mm[SIZE];
    uint32_t x_low[SIZE];
    uint32_t x_high[SIZE];
    uint32_t p_int[SIZE];

    for (uint8_t i=0; i < SIZE ; i++)
    {
        l_int[i] = share[i];
    }


    x_own[0]     = x_this;
    x_tar[0]     = x_target;


    for (uint32_t x=0; x < nminus ; x++)
    {
        x_oth[0] = x_values[x];

        signed_sub(x_oth, x_own, x_oth_mr, SIZE);
        uint8_t i;
        for (i= 0; i < SIZE; i++)
        {
            x_oth_m[i] = x_oth_mr[i];
        }
        x_oth_m[i] = x_oth_mr[i+1];

        signed_mod(x_oth_m, x_oth_mm,SIZE);

        inverse_p(x_oth_mm,x_low);

        sub_mod_p(x_oth, x_tar, x_high);

        multiply_mod_p(x_low, l_int, p_int);
        multiply_mod_p(p_int, x_high, l_int);
    }
    for (uint8_t i=0; i < SIZE ; i++)
    {
        res[i] = l_int[i];
    }
}



void calculate_share_part_for_x_n(uint32_t *x_values, uint32_t x_this, uint32_t x_target,  uint32_t nminus, uint32_t *share, uint32_t *res)
{
    uint32_t x_own[SIZE+1]    = {0};
    uint32_t x_tar[SIZE]      = {0};
    uint32_t x_oth[SIZE+1]    = {0}; //sign x[SIZE]
    uint32_t l_int[SIZE]      = {0};
    uint32_t x_oth_mr[SIZE+2];
    uint32_t x_oth_m[SIZE+1];
    uint32_t x_oth_mm[SIZE];
    uint32_t x_low[SIZE];
    uint32_t x_high[SIZE];
    uint32_t p_int[SIZE];

    for (uint8_t i=0; i < SIZE ; i++)
    {
        l_int[i] = share[i];
    }


    x_own[0]     = x_this;
    x_tar[0]     = x_target;


    for (uint32_t x=0; x < nminus ; x++)
    {
        x_oth[0] = x_values[x];

        signed_sub(x_oth, x_own, x_oth_mr, SIZE);
        uint8_t i;
        for (i= 0; i < SIZE; i++)
        {
            x_oth_m[i] = x_oth_mr[i];
        }
        x_oth_m[i] = x_oth_mr[i+1];

        signed_mod_n(x_oth_m, x_oth_mm,SIZE);

        inverse_n(x_oth_mm,x_low);

        sub_mod_n(x_oth, x_tar, x_high);

        multiply_mod_n(x_low, l_int, p_int);
        multiply_mod_n(p_int, x_high, l_int);
    }
    for (uint8_t i=0; i < SIZE ; i++)
    {
        res[i] = l_int[i];
    }
}






















