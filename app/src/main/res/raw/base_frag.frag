//SurfaceTexture比较特殊
//float数据是什么精度
precision mediump float;

//采样点坐标
varying vec2 aCoord;

//采样器 不是从Android的surfacetexture中的纹理采集数据，所以不需要Android的扩展纹理采样器了
//使用正常的 sample2D
uniform sampler2D vTexture;

void main() {
    //变量接收像素值
    //texture2D:采样器，采集 aCoord的像素
    //赋值给 gl_FragColor就可以了
    gl_FragColor = texture2D(vTexture, aCoord);
}