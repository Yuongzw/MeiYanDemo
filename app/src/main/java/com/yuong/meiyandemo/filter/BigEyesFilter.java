package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;

import com.yuong.meiyandemo.R;
import com.yuong.meiyandemo.face.Face;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class BigEyesFilter extends AbstractFrameFilter {

    private int left_eye;
    private int right_eye;
    private FloatBuffer left;
    private FloatBuffer right;
    private Face mFace;


    public BigEyesFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.big_eyes_frag);
        //获取程序中的变量
        left_eye = GLES20.glGetUniformLocation(mProgram, "left_eye");
        right_eye = GLES20.glGetUniformLocation(mProgram, "right_eye");

        left = ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
        right= ByteBuffer.allocateDirect(2 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer();
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

    public void setFace(Face face) {
        mFace = face;
    }

    @Override
    public int onDrawFrame(int textureID) {
        if (mFace == null) {
            return textureID;
        }
        GLES20.glViewport(0, 0, mWidth, mHeight);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        GLES20.glUseProgram(mProgram);
        mVertexBuffer.position(0);
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        GLES20.glEnableVertexAttribArray(vPosition);

        mTextureBuffer.position(0);
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        GLES20.glEnableVertexAttribArray(vCoord);

        float[] eyesRect = mFace.eyesRect;
        //左眼
        float leftEyeX = eyesRect[2] / mFace.imgWidth;//因为OpenGL的坐标系是0到1的所以要转为百分比
        float leftEyeY = eyesRect[3] / mFace.imgHeight;
        left.clear();
        left.put(leftEyeX);
        left.put(leftEyeY);
        left.position(0);
        GLES20.glUniform2fv(left_eye, 1, left);//传递数据给left_eye
        //右眼
        float rightEyeX = eyesRect[4] / mFace.imgWidth;
        float rightEyeY = eyesRect[5] / mFace.imgHeight;
        right.clear();
        right.put(rightEyeX);
        right.put(rightEyeY);
        right.position(0);
        GLES20.glUniform2fv(right_eye, 1, right);//传递数据给right_eye
        //激活采样器
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        //绑定采样器
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        //把vTexture 置为空
        GLES20.glUniform1i(vTexture, 0);

        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES11Ext.GL_TEXTURE_EXTERNAL_OES, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        return mFrameBufferTextures[0];
    }
}
