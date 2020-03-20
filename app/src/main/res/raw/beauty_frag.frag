//SurfaceTexture比较特殊
//float数据是什么精度
precision mediump float;

//采样点坐标
varying mediump vec2 aCoord;

//图片宽高
uniform int width;
uniform int height;

//采样器 不是从Android的surfacetexture中的纹理采集数据，所以不需要Android的扩展纹理采样器了
//使用正常的 sample2D
uniform sampler2D vTexture;

//20个采样点（坐标）
vec2 blurCoordinatos[20];

void main() {
    //1、高斯模糊
    vec2 singleStepOfsset=vec2(1.0/float(width), 1.0/float(height));
    blurCoordinatos[0]=aCoord.xy+singleStepOfsset*vec2(0.0, -10.0);
    blurCoordinatos[1]=aCoord.xy+singleStepOfsset*vec2(0.0, 10.0);
    blurCoordinatos[2]=aCoord.xy+singleStepOfsset*vec2(-10.0, 0.0);
    blurCoordinatos[3]=aCoord.xy+singleStepOfsset*vec2(10.0, 0.0);
    blurCoordinatos[4]=aCoord.xy+singleStepOfsset*vec2(5.0, -8.0);
    blurCoordinatos[5]=aCoord.xy+singleStepOfsset*vec2(5.0, 8.0);
    blurCoordinatos[6]=aCoord.xy+singleStepOfsset*vec2(-5.0, 8.0);
    blurCoordinatos[7]=aCoord.xy+singleStepOfsset*vec2(-5.0, -8.0);
    blurCoordinatos[8]=aCoord.xy+singleStepOfsset*vec2(8.0, -5.0);
    blurCoordinatos[9]=aCoord.xy+singleStepOfsset*vec2(8.0, 5.0);
    blurCoordinatos[10]=aCoord.xy+singleStepOfsset*vec2(-8.0, 5.0);
    blurCoordinatos[11]=aCoord.xy+singleStepOfsset*vec2(-8.0, -5.0);
    blurCoordinatos[12]=aCoord.xy+singleStepOfsset*vec2(0.0, -6.0);
    blurCoordinatos[13]=aCoord.xy+singleStepOfsset*vec2(0.0, 6.0);
    blurCoordinatos[14]=aCoord.xy+singleStepOfsset*vec2(6.0, 0.0);
    blurCoordinatos[15]=aCoord.xy+singleStepOfsset*vec2(-6.0, 0.0);
    blurCoordinatos[16]=aCoord.xy+singleStepOfsset*vec2(-4.0, -4.0);
    blurCoordinatos[17]=aCoord.xy+singleStepOfsset*vec2(-4.0, 4.0);
    blurCoordinatos[18]=aCoord.xy+singleStepOfsset*vec2(4.0, -4.0);
    blurCoordinatos[19]=aCoord.xy+singleStepOfsset*vec2(4.0, 4.0);

    //求出当前中心点的color值
    vec4 currentColor=texture2D(vTexture, aCoord);
    //获取中心点的rgb
    vec3 rgb=currentColor.rgb;

    for(int i=0; i<20; i++) {
        //把21个点的rgb都相加
        rgb += texture2D(vTexture, blurCoordinatos[i].xy).rgb;
    }
    vec4 blur= vec4(rgb * 1.0 / 21.0, currentColor.a);

    //2、高反差
    vec4 highPassColor = currentColor - blur;//原图 - 模糊的图

    //反向  增强对比度
    // 强光处理 color = 2 * color1 * color2
    //  24.0 强光程度
    //clamp  获取三个参数中处于中间的那个
    highPassColor.r = clamp(2.0 * highPassColor.r * highPassColor.r * 24.0, 0.0, 1.0);
    highPassColor.g = clamp(2.0 * highPassColor.g * highPassColor.g * 24.0, 0.0, 1.0);
    highPassColor.b = clamp(2.0 * highPassColor.b * highPassColor.b * 24.0, 0.0, 1.0);
    //过滤疤痕
    vec4 highPassBlur = vec4(highPassColor.rgb,1.0);
    // 融合 -> 磨皮
    //蓝色通道
    float b = min(currentColor.b,blur.b);
    float value = clamp((b - 0.2) * 5.0,0.0,1.0);
    //RGB 的最大值
    float maxChannelColor = max(max(highPassBlur.r,highPassColor.g),highPassBlur.b);
    //磨皮程度
    float intensity = 1.0;
    float currentIntensity = (1.0 - maxChannelColor / (maxChannelColor + 0.2)) * value *intensity;
    //混合
    //OpenGL 内置函数 线性融合
    // 公式 x * (1-a) + y.a
    vec3 r = mix(currentColor.rgb,blur.rgb,currentIntensity);

    //叠加 后面的数值也高，叠加的越厉害 美颜就越厉害
//    vec3 r = mix(currentColor.rgb, blur.rgb, 0.2);
    gl_FragColor = vec4(r, 1.0);
    //变量接收像素值
    //texture2D:采样器，采集 aCoord的像素
    //赋值给 gl_FragColor就可以了
//    gl_FragColor = texture2D(vTexture, aCoord);
}