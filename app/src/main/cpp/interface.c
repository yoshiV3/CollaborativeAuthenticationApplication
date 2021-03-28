//
// Created by yoshi on 24/02/21.
//


#include "interface.h"
#include "common.h"
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/stdint.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/malloc.h>
#include </home/yoshi/Android/Sdk/ndk/21.1.6352462/toolchains/llvm/prebuilt/linux-x86_64/sysroot/usr/include/jni.h>




jobject getNewBigNumberObject(JNIEnv *env, uint32_t * results);


uint32_t getIntegerFromByteArray(JNIEnv *env, jbyteArray arr)
{
    jbyte *b = (jbyte *) (*env)->GetByteArrayElements(env, arr, 0);

    uint32_t one   = (uint32_t) b[0];
    uint32_t two   = (uint32_t) b[1];
    uint32_t three = (uint32_t) b[2];
    uint32_t four  = (uint32_t) b[3];
    uint32_t value = one + (two << 8) + (three << 16) + (four << 24);

    (*env)->ReleaseByteArrayElements(env, arr, b, 0 );

    return value;
}


void placeIntInByteArray(int wordNumber, uint32_t number, uint8_t *target)
{
    for (uint8_t position = 0; position <4; position++)
    {
        size_t index     = wordNumber*4 +position;
        target[index]    = (uint8_t ) number;
        number           = number >> 8;
    }
}


int getArrayListSize(JNIEnv const *env, const jobject *arrayList) {
    jclass arrListClass = (*env)->GetObjectClass(env, arrayList );

    jmethodID sizeMethod = (*env)->GetMethodID(env, arrListClass, "size", "()I");

    return (*env)->CallIntMethod(env,arrayList, sizeMethod);
}

int getSizeOfArrayListRandomPoly(JNIEnv *env, jobject thiz)
{
    jclass thisClass = (*env)->GetObjectClass(env, thiz);

    jfieldID fidPoly= (*env)->GetFieldID(env, thisClass, "polynomial", "Ljava/util/ArrayList;");

    if (NULL == fidPoly) return 0;

    jobject objPoly = (*env)->GetObjectField(env, thiz, fidPoly);

    return getArrayListSize(env, objPoly);

}







jobject getObjectFromArrayList(const JNIEnv *env, const jobject arr, int index)
{
    jclass polyClass = (*env)->GetObjectClass(env,arr );

    jmethodID getMethod = (*env)->GetMethodID(env,polyClass,"get", "(I)Ljava/lang/Object;");

    jobject object  =  (*env)->CallObjectMethod(env,arr, getMethod, index);
    return object;
}


void transformArrayListBigNumbersToCArray(const JNIEnv *env, const jobject arr,  uint32_t ** result, const int size)
{

    for (int index = 0; index < size; index++)
    {

        uint32_t *c      = malloc(SIZE*sizeof(uint32_t));
        result[index]    = c;

        jobject bigNumber  =  getObjectFromArrayList(env, arr , index);

        jclass bigNumberClass = (*env)->GetObjectClass(env, bigNumber);



        jmethodID getPartMethod = (*env)->GetMethodID(env,bigNumberClass,"getPart", "(I)[B");



        for (int innerIndex = 0; innerIndex < SIZE; innerIndex++)
        {
            jbyteArray  bigNumberAsArray;
            bigNumberAsArray = (*env)->CallObjectMethod(env, bigNumber,  getPartMethod, innerIndex);
            c[innerIndex]    = getIntegerFromByteArray(env, bigNumberAsArray) ;//getIntegerFromByteArray(env, bigNumberAsArray);
        }
    }
}

void fillPolyWithData(JNIEnv *env, jobject thiz, uint32_t **poly, int size)
{

    //get poly (array list)
    jclass thisClass = (*env)->GetObjectClass(env, thiz);

    jfieldID fidPoly= (*env)->GetFieldID(env, thisClass, "polynomial", "Ljava/util/ArrayList;");

    if (NULL == fidPoly) return;

    jobject objPoly = (*env)->GetObjectField(env, thiz, fidPoly);

    transformArrayListBigNumbersToCArray(env, objPoly, poly, size);
}

void fillResultWithData(JNIEnv *env, uint32_t ** results, jobject result, int resultSize )
{
    jclass listClass = (*env)->GetObjectClass(env,result );

    jmethodID addMethod = (*env)->GetMethodID(env,listClass,"add", "(Ljava/lang/Object;)Z");

    for (int res = 0; res <resultSize; res++)
    {
        jobject newObj = getNewBigNumberObject(env, results[res]);
        (*env)->CallBooleanMethod(env, result,  addMethod, newObj);
    }
}


void fillPointWithData(JNIEnv *env, Point * c_point, jobject java_point){
    jclass pointClass = (*env)->GetObjectClass(env, java_point);

    jobject x_java = getNewBigNumberObject(env, c_point->x);
    jmethodID setXMethodId = (*env)->GetMethodID(env,pointClass,"setX", "(Lcom/project/collaborativeauthenticationapplication/service/crypto/BigNumber;)V");
    (*env)->CallVoidMethod(env, java_point, setXMethodId, x_java);

    jobject   y_java = getNewBigNumberObject(env, c_point->y);
    jmethodID setYMethodId = (*env)->GetMethodID(env,pointClass,"setY", "(Lcom/project/collaborativeauthenticationapplication/service/crypto/BigNumber;)V");
    (*env)->CallVoidMethod(env, java_point, setYMethodId, y_java);

    jboolean  zero = c_point->isZero;
    jmethodID setZMethodId = (*env)->GetMethodID(env,pointClass,"setZero", "(Z)V");
    (*env)->CallVoidMethod(env, java_point, setZMethodId, zero);
}


jobject getNewBigNumberObject(JNIEnv *env, uint32_t * results){
    jclass bigNumberClass = (*env)->FindClass(env, "com/project/collaborativeauthenticationapplication/service/crypto/BigNumber");
    jmethodID constructorMethodId = (*env)->GetMethodID(env, bigNumberClass, "<init>", "([B)V");

    uint8_t number[SIZE*4];
    uint32_t  one   = (jint) results[0];
    placeIntInByteArray(0, one, number);
    jint two   = (jint) results[1];
    placeIntInByteArray(1, two, number);
    jint three = (jint) results[2];
    placeIntInByteArray(2, three, number);
    jint four  = (jint) results[3];
    placeIntInByteArray(3, four, number);
    jint five  = (jint) results[4];
    placeIntInByteArray(4, five, number);
    jint six   = (jint) results[5];
    placeIntInByteArray(5, six, number);
    jint seven = (jint) results[6];
    placeIntInByteArray(6, seven, number);
    jint eight = (jint) results[7];
    placeIntInByteArray(7, eight, number);

    jbyteArray numberInBytes;
    numberInBytes = (*env)->NewByteArray(env, SIZE*4);

    (*env)->SetByteArrayRegion(env, numberInBytes, 0, SIZE*4, number);

    jobject newObj = (*env)->NewObject(env, bigNumberClass, constructorMethodId, numberInBytes);

    return newObj;
}





























