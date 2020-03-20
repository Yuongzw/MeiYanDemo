package com.yuong.meiyandemo.filter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.yuong.meiyandemo.R;
import com.yuong.meiyandemo.face.Face;
import com.yuong.meiyandemo.util.OpenGLUtil;

/**
 * 贴纸效果
 */
public class StickFilter extends AbstractFrameFilter {
    private Bitmap bitmap;
    //关联到GPU的纹理id
    private int[] mBitmapTextureId;
    private Face face;
    public StickFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
        //创建一个bitmap  内存地址在CPU
        bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.cat);
    }

    public void setFace(Face face) {
        this.face = face;
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
        mBitmapTextureId = new int[1];
        //在GPU开辟一块内存来加载bitmap
        OpenGLUtil.glGenTextures(mBitmapTextureId);

        //绑定纹理
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapTextureId[0]);
        //将bitmap与纹理id绑定
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    @Override
    public int onDrawFrame(int textureID) {
        if (face == null) {
            return textureID;
        }
        GLES20.glViewport(0, 0, mWidth, mHeight);
        //绑定fbo
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        //使用程序
        GLES20.glUseProgram(mProgram);
        //顶点着色器归位
        mVertexBuffer.position(0);
        //获取GPU的变量
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        //设置可读写
        GLES20.glEnableVertexAttribArray(vPosition);

        mTextureBuffer.position(0);
        //获取GPU的变量
        GLES20.glVertexAttribPointer(vCoord, 2, GLES20.GL_FLOAT, false, 0, mTextureBuffer);
        //设置可读写
        GLES20.glEnableVertexAttribArray(vCoord);

        //激活图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE);
        //绑定id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureID);

        //获取片元着色器属性 vTexture
        GLES20.glUniform1i(vTexture, 0);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);

        onDrawStick();



        return mFrameBufferTextures[0];
    }

    private void onDrawStick() {
        //开启混合模式
        GLES20.glEnable(GLES20.GL_BLEND);
        //绑定渲染函数
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        //获取脸的宽高
        float x = face.eyesRect[0] / face.imgWidth * mWidth;
        float y = face.eyesRect[1] / face.imgHeight * mHeight;

        GLES20.glViewport((int)x, (int)y - bitmap.getHeight() / 2, (int) ((float)face.width / face.imgWidth * mWidth), bitmap.getHeight());

        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, mFrameBuffer[0]);
        GLES20.glUseProgram(mProgram);
        mVertexBuffer.position(0);
        //获取GPU的变量
        GLES20.glVertexAttribPointer(vPosition, 2, GLES20.GL_FLOAT, false, 0, mVertexBuffer);
        //设置可读写
        GLES20.glEnableVertexAttribArray(vPosition);

        //激活图层
        GLES20.glActiveTexture(GLES20.GL_TEXTURE);
        //绑定id
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mBitmapTextureId[0]);

        //获取片元着色器属性 vTexture
        GLES20.glUniform1i(vTexture, 0);
        //绘制
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4);

        //解绑
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
        GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        //关闭混合模式
        GLES20.glDisable(GLES20.GL_BLEND);
    }
}
