//SurfaceTexture比较特殊
//float数据是什么精度
precision mediump float;

//采样点坐标
varying vec2 aCoord;

uniform vec2 left_eye;//左眼
uniform vec2 right_eye;//右眼

//采样器 不是从Android的surfacetexture中的纹理采集数据，所以不需要Android的扩展纹理采样器了
//使用正常的 sample2D
uniform sampler2D vTexture;

//公式：得到需要采集的改变后的点距离眼睛中心点的距离
float fs(float r, float rmax) {
    //放大系数
    float a = 0.4;
    return (1.0 - (r / rmax - 1.0) * (r / rmax - 1.0) * a);
}
vec2 newCoord(vec2 coord, vec2 eye, float rmax) {
    vec2 nCoord = coord;
    float r = distance(coord, eye);
    if (r < rmax) {
        //改变顶点位置
        //算出采集点与眼睛中心点的距离
        float fsr = fs(r, rmax);

//        (nCoord - eye) / (coord - eye) = fsr / r;
        nCoord = fsr * (coord - eye) + eye;
    }
    return nCoord;
}

void main() {
    //算出两个眼睛的距离
    float rmax=distance(left_eye, right_eye) / 2.0;

    vec2 nCoord = newCoord(aCoord, left_eye, rmax);
    nCoord = newCoord(nCoord, right_eye, rmax);
    //变量接收像素值
    //texture2D:采样器，采集 aCoord的像素
    //赋值给 gl_FragColor就可以了
    gl_FragColor = texture2D(vTexture, nCoord);
}