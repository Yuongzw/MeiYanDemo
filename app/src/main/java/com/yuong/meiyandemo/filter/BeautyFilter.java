package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.opengl.GLES20;

import com.yuong.meiyandemo.R;

public class BeautyFilter extends AbstractFrameFilter {

    private int width;
    private int height;

    public BeautyFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.beauty);
        width = GLES20.glGetUniformLocation(mProgram, "width");
        height = GLES20.glGetUniformLocation(mProgram, "height");
    }

    @Override
    protected void initCoordinate() {
//清空
        mTextureBuffer.clear();
        float[] TEXTURE = {
                0.0f, 0.0f,
                1.0f, 0.0f,
                0.0f, 1.0f,
                1.0f, 1.0f
        };
        mTextureBuffer.put(TEXTURE);
    }

    @Override
    public void onReady(int width, int height) {
        super.onReady(width, height);
    }

    @Override
    public int onDrawFrame(int textureID) {
        //设置显示窗口
        GLES20.glViewport(0, 0, mWidth, mHeight);
        //不调用下面这句  就会默认显示到屏幕上了  这里我们海之声把它画到FBO中
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        //使用着色器
        GLES20.glUseProgram(mProgram);
        GLES20.glUniform1i(width, mWidth);
        GLES20.glUniform1i(height, mHeight);
        mVertexBuffer.position(0);
        //允许对变量的读写权限
        GLES20.glEnableVertexAttribArray(vPosition);
        GLES20.glVertexAttribPointer(vPosition , 2, GLES20.GL_FLOAT, false,
                0, mVertexBuffer);

        mTextureBuffer.position(0);
        GLES20.glEnableVertexAttribArray(vCoord);
        GLES20.glVertexAttribPointer(vCoord , 2, GLES20.GL_FLOAT, false,
                0, mTextureBuffer);

        //激活采样器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定采样器
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        //把vTexture 置为空
        GLES20.glUniform1i(vTexture, 0);

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }
}
