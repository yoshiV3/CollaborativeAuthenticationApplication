//
// Created by yoshi on 23/03/21.
//

#include "ecc_arithmetic.h"
#include "ecc_point.h"
#include "array.h"
#include "mod_arithmetic.h"
#include "inversion.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include "common.h"



Point const G = {{0x16F81798, 0x59F2815B, 0x2DCE28D9, 0x029BFCDB, 0xCE870B07, 0x55A06295, 0xF9DCBBAC, 0x79BE667E},
                 {0xFB10D4B8, 0x9C47D08F, 0xA6855419, 0xFD17B448, 0x0E1108A8, 0x5DA4FBFC, 0x26A3C465, 0x483ADA77},
                 FALSE};




void point_doubling(Point const * const p, Point * const res){
    if (p->isZero == TRUE)
    {
        uint32_t res_x[SIZE] = {0};
        uint32_t res_y[SIZE] = {0};
        COPY(res->x, res_x, SIZE);
        COPY(res->y, res_y, SIZE);
        res->isZero = TRUE;
        return;
    }
    uint32_t ytwo[SIZE];
    multiply_mod_p_32(p->y,2,ytwo);

    uint32_t l_low[SIZE];
    inverse_p(ytwo, l_low);

    uint32_t xs[SIZE];
    multiply_mod_p(p->x,p->x, xs);

    uint32_t xthree[SIZE];
    multiply_mod_p_32(xs,3,xthree);

    uint32_t l[SIZE];
    multiply_mod_p(l_low, xthree, l);

    uint32_t ls[SIZE];
    multiply_mod_p(l,l, ls);

    uint32_t x_new_int[SIZE];
    sub_mod_p(ls, p->x, x_new_int);

    uint32_t x_new[SIZE];
    sub_mod_p(x_new_int, p->x, x_new);

    uint32_t y_new_int_one[SIZE];
    sub_mod_p(p->x, x_new, y_new_int_one);

    uint32_t y_new_int_two[SIZE];
    multiply_mod_p(l,y_new_int_one, y_new_int_two);

    uint32_t y_new[SIZE];
    sub_mod_p(y_new_int_two, p->y, y_new);

    res->isZero = FALSE;
    COPY(res->x, x_new, SIZE);
    COPY(res->y, y_new, SIZE);
}


void  add_points(Point const * const p, Point  const * const q, Point * const res){
    uint32_t x1[SIZE];
    uint32_t y1[SIZE];
    uint32_t x2[SIZE];
    uint32_t y2[SIZE];

    COPY_COOR(x1, y1, p);
    COPY_COOR(x2, y2, q);


    if (p->isZero == TRUE)
    {
        COPY_COOR(res->x, res->y, q);
        res->isZero = q->isZero;
        return;
    }

    if (q->isZero == TRUE)
    {
        COPY_COOR(res->x, res->y, p);
        res->isZero = p->isZero;
        return;
    }
    uint8_t equal_x;
    COMPARE(x1,x2, SIZE, equal_x);
    uint8_t equal_y;
    COMPARE(y1,y2, SIZE, equal_y);
    uint32_t y_inv[SIZE];
    additive_inverse_mod_p(y2, y_inv);
    uint8_t equal_yinv;
    COMPARE(y1,y_inv, SIZE, equal_yinv);
    if (equal_x && equal_y)
    {
        point_doubling(p,res);
        return;
    }
    if (equal_x && equal_yinv) {
        uint32_t res_x[SIZE] = {0};
        uint32_t res_y[SIZE] = {0};
        COPY(res->x, res_x, SIZE);
        COPY(res->y, res_y, SIZE);
        res->isZero = FALSE;
        return;
    }
    uint32_t x_diff[SIZE];
    sub_mod_p(q->x, p->x, x_diff);

    uint32_t y_diff[SIZE];
    sub_mod_p(q->y, p->y, y_diff);

    uint32_t l_low[SIZE];
    inverse_p(x_diff, l_low);

    uint32_t l[SIZE];
    multiply_mod_p(l_low, y_diff, l);

    uint32_t ls[SIZE];
    multiply_mod_p(l,l, ls);

    uint32_t x_new_int[SIZE];
    sub_mod_p(ls, p->x, x_new_int);

    uint32_t x_new[SIZE];
    sub_mod_p(x_new_int, q->x, x_new);

    uint32_t y_new_int_one[SIZE];
    sub_mod_p(p->x, x_new, y_new_int_one);

    uint32_t y_new_int_two[SIZE];
    multiply_mod_p(l,y_new_int_one, y_new_int_two);

    uint32_t y_new[SIZE];
    sub_mod_p(y_new_int_two, p->y, y_new);

    res->isZero = FALSE;
    COPY(res->x, x_new, SIZE);
    COPY(res->y, y_new, SIZE);
}


void sum_points(Point const * const P, uint32_t number, Point * const res){
    COPY_POINT(*res, P[0]);
    for(uint32_t point = 1; point < number; point++){
        add_points(res, &P[point], res);
    }
}


void multiple_double(uint8_t times, Point *p, Point *r){
    point_doubling(p,r);
    for(uint8_t exp = 1; exp <times; exp++) {
        point_doubling(r,r);
    }
}


void generate_exponentiation_lookup_table(Point * const A, Point * const B){
    size_t firstIndex = 2;
    size_t previous   = 1;
    size_t bitlength  = 8;
    Point point = POINT_ZERO;
    A[0] = point;
    B[0] = point;
    A[1] = G;
    multiple_double(16,(A+1), B+1);
    for(uint8_t bit =1; bit < bitlength; bit++){
        multiple_double(16, B+previous,   A+firstIndex);
        multiple_double(16, A+firstIndex, B+firstIndex);
        for(size_t offset =1; offset <firstIndex; offset++){
            add_points( A+firstIndex,  A+offset,  A+(offset+firstIndex));
            add_points( B+firstIndex,  B+offset,  B+(offset+firstIndex));
        }
        previous    = firstIndex;
        firstIndex *= 2;
    }
}

void ecc_exponentiation(Point const * const A,  Point const * const B, uint32_t const  * const exponent, Point * const res){
    uint32_t mask_a = START_MASK_A;
    uint32_t mask_b = START_MASK_B;
    uint8_t bits_a;
    uint8_t bits_b;
    GET_BITS(exponent,mask_a, mask_b, bits_a, bits_b);

    add_points(A+bits_a, B+bits_b,  res);

    mask_a >>=1;
    mask_b >>=1;
    for(uint8_t step=0; step <15;step++){
        GET_BITS(exponent,mask_a, mask_b, bits_a, bits_b);

        point_doubling(res, res);

        add_points(res, A+bits_a,  res);
        add_points(res, B+bits_b,  res);

        mask_a >>=1;
        mask_b >>=1;
    }
}



void ecc_exponentiation_mont(const Point * const p, uint32_t const  * const exponent, Point * const res){
    Point r_one;
    COPY_POINT(r_one,*p);
    Point r_zero = POINT_ZERO;
    for (int block = SIZE-1; block >=0 ; block--){
        for (uint32_t mask = 2147483648; mask > 0; mask = mask>>1){
            if(exponent[block] & mask){
                add_points(&r_one, &r_zero, &r_zero);
                point_doubling(&r_one, &r_one);
            }
            else {
                add_points(&r_one, &r_zero, &r_one);
                point_doubling(&r_zero, &r_zero);
            }
        }
    }
    COPY_POINT(*res,r_zero);
}