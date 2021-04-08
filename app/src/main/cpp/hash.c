//
// Created by yoshi on 23/03/21.
//

#include "hash.h"

#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdlib.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/lib64/clang/9.0.8/include/stddef.h>


const uint32_t K[64] = {
        0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
        0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
        0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
        0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
        0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
        0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
        0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
        0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2
};




void init_message_schedule(uint32_t const * const message, uint32_t * const w, uint32_t index)
{
    uint32_t i;
    for (i=0; i < 16; i++)
    {
       w[i] =  message[index + i];
    }
    uint8_t j;
    for(j = i;j < 64; j++)
    {
       w[j] = SIG1(w[j-2]) + w[j-7] + SIG0(w[j-15]) + w[j-16];
    }
}


void sha(uint32_t const * const message, uint32_t const bitlength, uint32_t * const res)
{
    uint32_t k;
    const uint32_t l = (bitlength + 1)&MODFHT; // bit length mod 512
    if (l > PADDON)
    {
        k = (BL-l) + PADDON;
    }
    else
    {
        k =  PADDON - l;
    }
    const uint32_t nb_rem_bits    = bitlength&MODTT; //mod 32
    const uint32_t nb_full_words  = bitlength>>5; //div by 32
    const uint32_t nb_blocks      = (bitlength + 1 + k + 64) >> 9; //div by 512

    uint32_t * const mes   = (uint32_t*) malloc(nb_blocks*BYTESPBL);

    uint32_t i;
    for (i=0; i < nb_full_words; i++)
    {
        mes[i] = message[i];
    }
    uint32_t add_zeros;
    if (nb_rem_bits > 0){
         add_zeros = 32 - nb_rem_bits -1;
        uint32_t extra_one = 1 << (add_zeros);
        mes[i] = message[i] ^ extra_one;
    }
    else {
           add_zeros = 31;
           mes[i] = 1<<31;
    }
    k = k - add_zeros;
    while (k > 32)
    {
        i      = i + 1;
        mes[i] = 0;
        k      = k -32;
    }
    i = i +1;
    mes[i]   = 0;
    mes[i+1] = 0;
    mes[i+2] = bitlength;
    uint32_t w[64] = {0};
    uint32_t h0    = Hzero;
    uint32_t h1    = Hone;
    uint32_t h2    = Htwo;
    uint32_t h3    = Hthree;
    uint32_t h4    = Hfour;
    uint32_t h5    = Hfive;
    uint32_t h6    = Hsix;
    uint32_t h7    = Hseven;


    uint32_t  index = 0;

    for (uint32_t block = 0; block < nb_blocks; block++)//1; block++)//n
    {
        init_message_schedule(mes, w, index);
        uint32_t a = h0;
        uint32_t b = h1;
        uint32_t c = h2;
        uint32_t d = h3;
        uint32_t e = h4;
        uint32_t f = h5;
        uint32_t g = h6;
        uint32_t h = h7;
        for (uint32_t round = 0; round < 64; round ++)
        {
            uint32_t temp1 =  h    + SUM1(e)  +  CH(e,f,g)  + K[round] +  w[round];
            uint32_t temp2 = SUM0(a) +  MAJ(a,b,c);
            h = g;
            g = f;
            f = e;
            e = d     + temp1;
            d = c;
            c = b;
            b =	a;
            a = temp1 + temp2;
        }
        index = index + 16;
        h0 = a + h0;
        h1 = b + h1;
        h2 = c + h2;
        h3 = d + h3;
        h4 = e + h4;
        h5 = f + h5;
        h6 = g + h6;
        h7 = h + h7;
    }
    res[0] =  h0;//w[0];//h0;
    res[1] =  h1;//w[1];//h1;
    res[2] =  h2;//w[2];//h2;
    res[3] =  h3;//w[3];//h3;
    res[4] =  h4;//w[4];//h4;
    res[5] =  h5;//w[5];//h5;
    res[6] =  h6;//w[6];//h6;
    res[7] =  h7;//w[7];//h7;
    free(mes);
}

