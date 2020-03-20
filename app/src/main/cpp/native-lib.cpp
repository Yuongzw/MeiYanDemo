#include <jni.h>
#include <string>
#include <exception>
#include <android/native_window_jni.h>
#include "FaceTrack.h"

using namespace cv;
using namespace std;

ANativeWindow *window = 0;

extern "C"
JNIEXPORT long JNICALL
Java_com_yuong_meiyandemo_OpenCVJni_init(JNIEnv *env, jobject thiz, jstring path_, jstring seeta_) {
    const char *path = env->GetStringUTFChars(path_, 0);
    const char *seeta = env->GetStringUTFChars(seeta_, 0);
    LOGW("path = %s", path);
    LOGW("初始化开始: %s", seeta);
    FaceTrack *faceTrack = new FaceTrack(path, seeta);

    LOGW("初始化结束");
    env->ReleaseStringUTFChars(path_, path);
    env->ReleaseStringUTFChars(seeta_, seeta);
    return reinterpret_cast<long>(faceTrack);
}extern "C"
JNIEXPORT void JNICALL
Java_com_yuong_meiyandemo_OpenCVJni_native_1startTrack(JNIEnv *env, jobject thiz,
                                                       jlong track_handler) {
    if (track_handler == 0) {
        return;
    }
    FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(track_handler);
    faceTrack->startTrack();
}extern "C"
JNIEXPORT jobject JNICALL
Java_com_yuong_meiyandemo_OpenCVJni_native_1detector(JNIEnv *env, jobject thiz, jlong track_handler,
                                                     jbyteArray data, jint w, jint h,
                                                     jint camera_id) {
    if (track_handler == 0) {
        return NULL;
    }
    FaceTrack *faceTrack = reinterpret_cast<FaceTrack *>(track_handler);
    faceTrack->startTrack();
    //NV21 一帧数据  ----》 转为 bitmap
    jbyte *data_ = env->GetByteArrayElements(data, NULL);
    //人脸检测
    //src  == bitmap
    Mat src(h + h / 2, w, CV_8UC1, data_);

    //nv21  ---> rgba
    cvtColor(src, src, COLOR_YUV2RGBA_NV21);
    if (camera_id == 1) {
        //前置摄像头  逆时针旋转90度
        rotate(src, src, ROTATE_90_COUNTERCLOCKWISE);
        //水平镜像  1：水平翻转；0：垂直翻转
        flip(src, src, 1);
    } else {
        rotate(src, src, ROTATE_90_CLOCKWISE);
    }
    Mat gray;
    //灰度化
    cvtColor(src, gray, COLOR_RGBA2GRAY);
    //直方图均衡化 增强对比效果
    equalizeHist(gray, gray);

    //Face对象
    jobject face = NULL;

    //得到5个关键点集合
    vector<Rect2f> rects = faceTrack->detector(gray);
    //初始化Java中的Face
    int imgWidth = src.cols;
    int imgHeight = src.rows;
    if (rects.size() > 0) {
        jclass faceClazz = env->FindClass("com/yuong/meiyandemo/face/Face");
        /**
         *  public Face( int width, int height, int imgWidth, int imgHeight, float[] eyesRect)
         * 注意  后面的参数为 int、int、int、int、float[]这几个类型
         * 所以方法签名就是类型第一个字母的大写即 IIII 注意数组的话只加前面的中括号
         * 所以最终的方法前面为 (IIII[F)V;   V:代表的是void 返回类型
         */
        jmethodID construct = env->GetMethodID(faceClazz, "<init>", "(IIII[F)V");
        int size = rects.size() * 2;
        //创建Java的 float数组
        jfloatArray  jfloatArray = env->NewFloatArray(size);
        for (int i = 0, j = 0; i < size; j++) {
            float f[2] = {rects[j].x, rects[j].y};
            env->SetFloatArrayRegion(jfloatArray, i, 2, f);
            i += 2;
        }
        //脸部的宽高
        Rect2f faceRect = rects[0];
        int width = faceRect.width;
        int height = faceRect.height;
        face = env->NewObject(faceClazz, construct,width, height, imgWidth, imgHeight, jfloatArray);
    }

    env-> ReleaseByteArrayElements(data, data_, 0);
    return face;
}