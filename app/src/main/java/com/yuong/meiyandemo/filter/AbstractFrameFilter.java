package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yuong.meiyandemo.util.OpenGLUtil;


/*
    主要是获取摄像头的数据 并且创建FBO 在FBO中添加特效
 */
public abstract class AbstractFrameFilter extends AbstractFilter {
    //FBO
    public int[] mFrameBuffer;
    public int[] mFrameBufferTextures;//FBO 纹理Id

    public AbstractFrameFilter(Context context, int vertexShaderId, int fragmentShaderId) {
        super(context, vertexShaderId, fragmentShaderId);
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

    private void destroyFrameBuffers() {
        //删除fbo的纹理
        if (mFrameBufferTextures != null) {
            GLES20.glDeleteTextures(1, mFrameBufferTextures, 0);
            mFrameBufferTextures = null;
        }
        //删除FBO
        if (mFrameBuffer != null) {
            GLES20.glDeleteFramebuffers(1, mFrameBuffer, 0);
            mFrameBuffer = null;
        }
    }

}
