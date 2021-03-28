# Sets the minimum version of CMake required to build your native library.
# This ensures that a certain set of CMake features is available to
# your build.

cmake_minimum_required(VERSION 3.4.1)

# Specifies a library name, specifies whether the library is STATIC or
# SHARED, and provides relative paths to the source code. You can
# define multiple libraries by adding multiple add_library() commands,
# and CMake builds them for you. When you build your app, Gradle
# automatically packages shared libraries with your APK.

add_library( # Specifies the name of the library.
             crypto-lib

             # Sets the library as a shared library.
             SHARED

             # Provides a relative path to your source file(s).
        src/main/cpp/mp_arithmetic.c        src/main/cpp/mp_arithmetic.h
        src/main/cpp/ecc_arithmetic.c       src/main/cpp/ecc_arithmetic.h
                                            src/main/cpp/ecc_point.h
        src/main/cpp/hash.c                 src/main/cpp/hash.h
        src/main/cpp/schnorr.c              src/main/cpp/schnorr.h
        src/main/cpp/mod_arithmetic.c       src/main/cpp/mod_arithmetic.h
        src/main/cpp/signed_arithmetic.c    src/main/cpp/signed_arithmetic.h
        src/main/cpp/secret_sharing.c       src/main/cpp/secret_sharing.h
        src/main/cpp/inversion.c            src/main/cpp/inversion.h
        src/main/cpp/interface.c            src/main/cpp/interface.h
        src/main/cpp/jni_c_wrapper_crypto.c
        )