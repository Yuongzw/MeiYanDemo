package com.yuong.meiyandemo.view;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class OpenGLView extends GLSurfaceView {
    GLRender glRender;

    public OpenGLView(Context context) {
        super(context);
    }

    public OpenGLView(Context context, AttributeSet attrs) {
        super(context, attrs);
        //设置OpenGL 版本
        setEGLContextClientVersion(2);
        glRender = new GLRender(this);
        setRenderer(glRender);
        //设置渲染模式， 按需渲染 效率高
        setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
    }

    public void enableBeauty(boolean isChecked) {
        glRender.enableBeauty(isChecked);
    }

    public void enableSticker(boolean isChecked) {
        glRender.enableSticker(isChecked);
    }

    public void enableBigEyes(boolean isChecked) {
        glRender.enableBigEyes(isChecked);
    }
}
