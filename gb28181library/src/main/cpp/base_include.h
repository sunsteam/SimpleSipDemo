#ifndef BASE_INCLUDE_H
#define BASE_INCLUDE_H

extern "C"
{
#include "include/libavcodec/avcodec.h"
#include "include/libavformat/avformat.h"
#include "include/libavcodec/avcodec.h"
#include "include/libavutil/opt.h"
}

#include "threadsafe_queue.cpp"
#include "log.h"
#include <jni.h>
#include <string>

#define END_STATE 1
#define START_STATE 0

#define RELEASE_TRUE 1
#define RELEASE_FALSE 0

#define ROTATE_0_CROP_LT 0

/**
 * 旋转90度剪裁左上
 */
#define ROTATE_90_CROP_LT 1
/**
 * 暂时没处理
 */
#define ROTATE_180 2
/**
 * 旋转270(-90)裁剪左上，左右镜像
 */
#define ROTATE_270_CROP_LT_MIRROR_LR 3

using namespace std;


static uint64_t getCurrentTime() {
    struct timeval tv;
    gettimeofday(&tv,NULL);
    return (int64_t)tv.tv_sec * 1000 + tv.tv_usec / 1000;
}


static uint64_t bytes2long(uint8_t b[]) {
    uint64_t temp = 0;
    uint64_t res = 0;
    for (int i=0;i<8;i++) {
        res <<= 8;
        temp = b[i];
        temp = temp & 0xff;
        res |= temp;
    }
    return res;
}

static uint16_t bytes2short(uint8_t b[]) {
    uint16_t temp = 0;
    uint16_t res = 0;
    for (int i=0;i<2;i++) {
        res <<= 8;
        temp = b[i];
        temp = (uint16_t) (temp & 0xff);
        res |= temp;
    }
    return res;
}

static uint8_t * short2Bytes(uint16_t i) {
    uint16_t temp = i;
    uint8_t * bytes = (uint8_t *) malloc(2);
    bytes[1] = (uint8_t) (temp & 0xff);
    temp >>= 8;
    bytes[0] = (uint8_t) (temp & 0xff);
    return bytes;
}
#endif //BASE_INCLUDE_H
