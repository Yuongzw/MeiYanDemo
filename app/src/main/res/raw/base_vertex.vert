//把顶点的坐标赋值给这个变量，确定要画的形状
attribute vec4 vPosition;

//接收纹理坐标，接收采样器采样图片的坐标
//不用和矩阵相乘，接收了一个点只有2个float就可以了，所以写成了vec2
attribute vec2 vCoord;
//传给片元着色器 像素点
varying vec2 aCoord;

void main() {

    //内置变量 gl_Position， 我们吧顶点数据赋值给这个变量，OpenGL就知道要画的东西
    gl_Position = vPosition;
    //通过测试 和设备有关（有些设备直接就采集不到图像，有些则会镜像）
    aCoord = vCoord;
}