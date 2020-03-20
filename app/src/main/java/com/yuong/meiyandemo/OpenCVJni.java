package com.yuong.meiyandemo;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.annotation.NonNull;

import com.yuong.meiyandemo.face.Face;
import com.yuong.meiyandemo.util.CameraHelper2;

public class OpenCVJni {
    static {
        System.loadLibrary("native-lib");
    }

    private long trackHandler;

    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private static final int CHECK_FACE = 1;
    private Face face;

    @SuppressLint("HandlerLeak")
    public OpenCVJni(String path, String seeta, final CameraHelper2 cameraHelper) {
        trackHandler = init(path, seeta);
        mHandlerThread = new HandlerThread("mHandlerThread");
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper()){
            @Override
            public void handleMessage(@NonNull Message msg) {
                face = native_detector(trackHandler, (byte[]) msg.obj, CameraHelper2.WIDTH, CameraHelper2.HEIGHT, cameraHelper.getCameraId());
            }
        };
    }

    public void startTrack() {
        native_startTrack(trackHandler);
    }


    //先检测人脸，再检测人眼  由于比较耗时，要开启线程进行检测
    public void detector(byte[] data) {
        //先把之前的消息移除
        mHandler.removeMessages(CHECK_FACE);
        Message message = mHandler.obtainMessage(CHECK_FACE);
        message.obj = data;
        mHandler.sendMessage(message);
    }

    public Face getFace() {
        return face;
    }

    //加载训练库
    public native long init(String path, String seeta);

    //开启检测
    private native void native_startTrack(long trackHandler);

    //检测人脸、人眼
    private native Face native_detector(long trackHandler, byte[] data, int width, int height, int cameraId);


}
