package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.yuong.meiyandemo.R;
import com.yuong.meiyandemo.util.OpenGLUtil;


/*
    主要是获取摄像头的数据 并且创建FBO 在FBO中添加特效
 */
public class CameraFilter extends AbstractFilter {
    //FBO
    private int[] mFrameBuffer;
    private int[] mFrameBufferTextures;//FBO 纹理Id
    private float[] matrix;

    public CameraFilter(Context context) {
        super(context, R.raw.camera_vertex, R.raw.camera_frag);
    }

    //坐标转换
    @Override
    protected void initCoordinate() {
        //清空
        mTextureBuffer.clear();
        //摄像机原始坐标 没处理前 摄像头是颠倒的（逆时针旋转90度） 是镜像的
        float[] TEXTURE = {
                0.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 0.0f,
                1.0f, 1.0f
        };
        mTextureBuffer.put(TEXTURE);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
        mFrameBuffer = new int[1];
        //生成FBO
        GLES20.glGenFramebuffers(1, mFrameBuffer, 0);

        //实例化一个纹理  目的：纹理和FBO进行绑定 然后对FBO进行操作
        mFrameBufferTextures = new int[1];
        OpenGLUtil.glGenTextures(mFrameBufferTextures);
        //将纹理跟FBO进行绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0]);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        //设置纹理显示信息  宽度高度
        GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, GLES20.GL_RGBA, mWidth, mHeight, 0,
                GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, null);
        //将纹理与FBO进行联系
        GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0,
                GLES20.GL_TEXTURE_2D, mFrameBufferTextures[0], 0);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
    }

    @Override
    public int onDrawFrame(int textureID) {
        //设置显示窗口
        GLES20.glViewport(0, 0, mWidth, mHeight);
        //不调用下面这句  就会默认显示到屏幕上了  这里我们海之声把它画到FBO中
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
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

        //变换矩阵
        GLES20.glUniformMatrix4fv(vMatrix, 1, false, matrix, 0);

        //激活采样器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定采样器
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, textureID);

        //把vTexture 置为空
        GLES20.glUniform1i(vTexture, 0);

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }

    public void setMatrix(float[] mtx) {
        matrix = mtx;
    }
}
