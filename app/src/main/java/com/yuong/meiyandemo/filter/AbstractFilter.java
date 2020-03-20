package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yuong.meiyandemo.util.OpenGLUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public abstract class AbstractFilter {

    //顶点着色器Id
    protected int mVertexShaderId;
    //片元着色器ID
    protected int mFragmentShaderId;

    protected FloatBuffer mTextureBuffer;
    protected FloatBuffer mVertexBuffer;    //顶点着色器

    protected int mProgram;
    protected int vPosition;
    protected int vMatrix;
    protected int vCoord;
    protected int vTexture;
    protected int mWidth;
    protected int mHeight;


    public AbstractFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        mVertexShaderId = vertexShaderId;
        mFragmentShaderId = fragmentShaderId;

        //摄像头是2D的  二维
        /*
        顶点着色器
            第一个4：摄像头有四个顶点，即屏幕又四个顶点
            第二个2：摄像头是二维的
            第三个4：数据是float类型的，占4个字节
         */
        mVertexBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //清空在GPU中的内存
        mVertexBuffer.clear();
        //OpenGL 里面坐标的顶点
        float[] VERTEX = {
                -1.0f, -1.0f,
                1.0f, -1.0f,
                -1.0f, 1.0f,
                1.0f, 1.0f
        };
        mVertexBuffer.put(VERTEX);

        /*
        片元着色器
            第一个4：摄像头有四个顶点，即屏幕又四个顶点
            第二个2：摄像头是二维的
            第三个4：数据是float类型的，占4个字节
         */
        mTextureBuffer = ByteBuffer.allocateDirect(4 * 2 * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer();
        //清空在GPU中的内存
        mTextureBuffer.clear();
        //Camera 里面坐标的顶点， 同手机的坐标
        float[] TEXTURE = {
                0.0f, 1.0f,
                1.0f, 1.0f,
                0.0f, 0.0f,
                1.0f, 0.0f
        };
        mTextureBuffer.put(TEXTURE);

        initilize(context);
        initCoordinate();
    }

    protected abstract void initCoordinate();

    public void onReady(int width, int height){
        mWidth = width;
        mHeight = height;
    }

    private void initilize(Context context) {
        String vertexShader = OpenGLUtil.readRawTextFile(context, mVertexShaderId);
        String fragmentShader = OpenGLUtil.readRawTextFile(context, mFragmentShaderId);
        mProgram = OpenGLUtil.loadProgram(vertexShader, fragmentShader);

        //获取vPosition地址
        vPosition = GLES20.glGetAttribLocation(mProgram, "vPosition");
        vCoord = GLES20.glGetAttribLocation(mProgram, "vCoord");
        vMatrix = GLES20.glGetUniformLocation(mProgram, "vMatrix");
        vTexture = GLES20.glGetUniformLocation(mProgram, "vTexture");
    }

    //  渲染
    public int onDrawFrame(int textureID) {
        //设置显示窗口
        GLES20.glViewport(0, 0, mWidth, mHeight);
        //使用着色器
        GLES20.glUseProgram(mProgram);
        mVertexBuffer.position(0);
        //允许对变量的读写权限
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition , 2, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);
//        GLES20.glDisableVertexAttribArray(vPosition);

        mTextureBuffer.position(0);
        GLES20.glEnableVertexAttribArray(vCoord);
        GLES20.glVertexAttribPointer(vCoord , 2, GLES20.GL_FLOAT, false,
                0, mTextureBuffer);
        //关闭读写权限
//        GLES20.glDisableVertexAttribArray(vCoord);

        //激活采样器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定采样器
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        //把vTexture 置为空
        GLES20.glUniform1i(vTexture, 0);

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);
        return textureID;
    }
}
