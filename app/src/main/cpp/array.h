//
// Created by yoshi on 23/03/21.
//

#ifndef COLLABORATIVEAUTHENTICATIONAPPLICATION_ARRAY_H
#define COLLABORATIVEAUTHENTICATIONAPPLICATION_ARRAY_H
    #define COPY(dest, src, size)\
                               {\
                                for (uint8_t i=0; i<(size); i++)\
                                {\
                                    (dest)[i] = (src)[i];\
                                }\
                              }

    #define FREE_2D_ARRAY(arr, size) \
                            {\
                                 for (int index = 0; index <size; index++){\
                                    free(arr[index]);\
                                 }\
                                 free(arr);\
                            }
    #define ALLOC_2D_ARRAY(arr, size) \
                            {\
                                 (arr) = malloc((size)*sizeof(uint32_t*));\
                                 for (int index = 0; index <(size); index++){\
                                    (arr)[index] = malloc(SIZE*sizeof(uint32_t));\
                                 }\
                            }
    #define MAKE_ZERO(dest, size)\
                               {\
                                for (uint8_t i=0; i<(size); i++)\
                                {\
                                    (dest)[i] = 0;\
                                }\
                               }
#endif //COLLABORATIVEAUTHENTICATIONAPPLICATION_ARRAY_H
