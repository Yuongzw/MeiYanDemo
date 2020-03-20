package com.yuong.meiyandemo.view;

import android.annotation.SuppressLint;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.yuong.meiyandemo.OpenCVJni;
import com.yuong.meiyandemo.filter.BeautyFilter;
import com.yuong.meiyandemo.filter.BigEyesFilter;
import com.yuong.meiyandemo.filter.CameraFilter;
import com.yuong.meiyandemo.filter.ScreenFilter;
import com.yuong.meiyandemo.filter.StickFilter;
import com.yuong.meiyandemo.util.CameraHelper2;
import com.yuong.meiyandemo.util.Utils;

import java.io.File;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLRender implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener, Camera.PreviewCallback {
    private OpenGLView mView;
    private CameraHelper2 mCameraHelper;
    private SurfaceTexture mSurfaceTexture;
    private int[] mTextures;//纹理id
    private float[] mtx = new float[16];

    private CameraFilter mCameraFilter;
    private BigEyesFilter mBigEyesFilter;
    private StickFilter mStickFilter;
    private BeautyFilter mBeautyFilter;
    private ScreenFilter mScreenFilter;

    private OpenCVJni openCVJni;
    private int mWidth;
    private int mHeight;

    private File lbpcascade_frontalface = new File(Environment.getExternalStorageDirectory(), "lbpcascade_frontalface.xml");
    private File seeta_fa = new File(Environment.getExternalStorageDirectory(), "seeta_fa_v1.1.bin");

    public GLRender(OpenGLView view) {
        this.mView = view;
        init();
    }

    @SuppressLint({"StaticFieldLeak","WrongThread"})
    private void init() {
        new AsyncTask<Void, Void, Void>() {

            @Override
            protected Void doInBackground(Void... params) {
                Utils.copyAssets(mView.getContext(), "lbpcascade_frontalface.xml");
                //加载训练库
                String path = new File(Environment.getExternalStorageDirectory(), "lbpcascade_frontalface.xml").getAbsolutePath();
                Log.w("yuongzw", path);
                Utils.copyAssets(mView.getContext(), "seeta_fa_v1.1.bin");
                return null;
            }

            @Override
            protected void onPreExecute() {
            }

            @Override
            protected void onPostExecute(Void aVoid) {
            }
        }.execute();
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //打开摄像头
        mCameraHelper = new CameraHelper2(Camera.CameraInfo.CAMERA_FACING_FRONT);
        mCameraHelper.setPreviewCallback(this);
        mTextures = new int[1];
        //创建纹理id
        GLES20.glGenTextures(mTextures.length, mTextures, 0);
        //SurfaceTexture与纹理id相关联，方便摄像头的数据能够传递给OpenGL
        mSurfaceTexture = new SurfaceTexture(mTextures[0]);
        //设置监听
        mSurfaceTexture.setOnFrameAvailableListener(this);
        //获取摄像头的矩阵，不会变形
        mSurfaceTexture.getTransformMatrix(mtx);

        mCameraFilter = new CameraFilter(mView.getContext());
//        mBigEyesFilter = new BigEyesFilter(mView.getContext());
//        mStickFilter = new StickFilter(mView.getContext());
        mScreenFilter = new ScreenFilter(mView.getContext());
        mCameraFilter.setMatrix(mtx);

    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int width, int height) {
        mCameraHelper.startPreview(mSurfaceTexture);
        mWidth = width;
        mHeight = height;
        mCameraFilter.onReady(width, height);
//        mBigEyesFilter.onReady(width, height);
//        mStickFilter.onReady(width, height);
        mScreenFilter.onReady(width, height);
        if (openCVJni == null) {
            openCVJni = new OpenCVJni(lbpcascade_frontalface.getAbsolutePath(), seeta_fa.getAbsolutePath(), mCameraHelper);
        }
        openCVJni.startTrack();
    }

    //摄像头获取一帧数据会回调该方法
    @Override
    public void onDrawFrame(GL10 gl10) {
        //清空成黑色
        GLES20.glClearColor(0, 0, 0, 0);
        //把之前的都清空
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        mSurfaceTexture.updateTexImage();
        mSurfaceTexture.getTransformMatrix(mtx);
        mCameraFilter.setMatrix(mtx);
        int id = mCameraFilter.onDrawFrame(mTextures[0]);
        if (mBigEyesFilter != null) {
            mBigEyesFilter.setFace(openCVJni.getFace());
            id = mBigEyesFilter.onDrawFrame(id);//大眼效果
        }
        if (mStickFilter != null) {
            mStickFilter.setFace(openCVJni.getFace());
            id = mStickFilter.onDrawFrame(id);//贴纸效果
        }
        if (mBeautyFilter != null) {
            id = mBeautyFilter.onDrawFrame(id);//美颜效果
        }
        //mScreenFilter将最终的特效运用到 SurfaceView中
        mScreenFilter.onDrawFrame(id);
    }

    /**'
     * 获取到一帧数据
     *
     * @param surfaceTexture
     */
    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        //请求重新渲染
        ((OpenGLView) mView).requestRender();

    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        //每一帧数据会回调到data数组里
        if (openCVJni != null) {
            openCVJni.detector(data);
        }
    }

    public void enableBeauty(final boolean isChecked) {
        //因为操作滤镜只能在 GLThread里面操作，所以要这样写
        mView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (isChecked) {
                    mBeautyFilter = new BeautyFilter(mView.getContext());
                    mBeautyFilter.onReady(mWidth, mHeight);
                } else {
                    mBeautyFilter = null;
                }
            }
        });
    }

    public void enableSticker(final boolean isChecked) {
        //因为操作滤镜只能在 GLThread里面操作，所以要这样写
        mView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (isChecked) {
                    mStickFilter = new StickFilter(mView.getContext());
                    mStickFilter.onReady(mWidth, mHeight);
                } else {
                    mStickFilter = null;
                }
            }
        });
    }

    public void enableBigEyes(final boolean isChecked) {
        //因为操作滤镜只能在 GLThread里面操作，所以要这样写
        mView.queueEvent(new Runnable() {
            @Override
            public void run() {
                if (isChecked) {
                    mBigEyesFilter = new BigEyesFilter(mView.getContext());
                    mBigEyesFilter.onReady(mWidth, mHeight);
                } else {
                    mBigEyesFilter = null;
                }
            }
        });
    }
}
