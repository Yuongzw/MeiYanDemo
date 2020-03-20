//
// Created by Administrator on 2020/2/17.
//
#include "FaceTrack.h"

FaceTrack::FaceTrack(const char *path, const char *seeta) {
    //智能指针
    Ptr<CascadeClassifier> classFilter = makePtr<CascadeClassifier>(path);
    //创建检测器
    Ptr<CascadeDetectorAdapter> mainDetector = makePtr<CascadeDetectorAdapter>(classFilter);
    //创建跟踪器
    Ptr<CascadeClassifier> classFilter1 = makePtr<CascadeClassifier>(path);
    Ptr<CascadeDetectorAdapter> trackingDetector = makePtr<CascadeDetectorAdapter>(classFilter1);
    DetectionBasedTracker::Parameters detectorParams;
    //开启检测器和跟踪器
//    tracker = new DetectionBasedTracker(mainDetector, trackingDetector, detectorParams);
    tracker = makePtr<DetectionBasedTracker>(mainDetector, trackingDetector, detectorParams);
    faceAligment = makePtr<seeta::FaceAlignment>(seeta);
}

void FaceTrack::startTrack() {
    //让检测器和跟踪器运行起来
    tracker->run();
}

vector<Rect2f> FaceTrack::detector(const Mat src) {
    vector<Rect> faces;//人脸集合
    vector<Rect2f> rects;
    tracker->process(src);
    //获取人脸集合
    tracker->getObjects(faces);
    //定义5个关键点，因为眼睛、鼻子、嘴巴总共是占5个关键点
    //顺序依次是左眼：0；有眼：1；鼻子：2；左嘴巴：3；右嘴巴：4
    seeta::FacialLandmark points[5];//入参出参对象


    //检测到人脸
    if (faces.size() > 0) {
        Rect face = faces[0];
        //人脸的区域
        rects.push_back(Rect2f(face.x, face.y, face.width, face.height));
        seeta::ImageData imgData(src.cols, src.rows);
        imgData.data = src.data;

        //待检测的区域
        seeta::FaceInfo faceInfo;
        seeta::Rect bbox;
        bbox.x = face.x;
        bbox.y = face.y;
        bbox.width = face.width;
        bbox.height = face.height;
        faceInfo.bbox = bbox;
        faceAligment->PointDetectLandmarks(imgData, faceInfo, points);
        for (int i = 0; i < 5; ++i) {
            rects.push_back(Rect2f(static_cast<float>(points[i].x), static_cast<float>(points[i].y), 0, 0));
            if (i == 0) {
                LOGW("左眼坐标 x %f  y %f", points[i].x, points[i].y);
            } else if (i == 1) {
                LOGW("右眼坐标 x %f  y %f", points[i].x, points[i].y);
            }
        }
    }
    LOGW("人脸数：%ld", faces.size());
    return rects;
}

