package com.yuong.meiyandemo.filter;

import android.content.Context;

import com.yuong.meiyandemo.R;

/*
    滤镜
    他是作为显示滤镜  显示CameraFilter已经渲染好的特效
 */
public class ScreenFilter extends AbstractFilter {

    public ScreenFilter(Context context) {
        super(context, R.raw.base_vertex, R.raw.base_frag);
    }

    @Override
    protected void initCoordinate() {

    }
}
