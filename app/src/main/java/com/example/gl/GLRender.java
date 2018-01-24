package com.example.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * 当我们需要保存时，调用glPushMatrix函数，它相当于把矩阵（相当于盘子）放到堆栈上。当需要恢复最近一次的保存时，调用glPopMatrix函数，它相当于把矩阵从堆栈上取下。
 *
 * 绘制球体的现成函数：glutSolidSphere
 *
 * 在加载纹理图片时，OpenGL为每张图片分配好ID，将图片缓存起来。在贴图时，通过ID来查找图片
 *
 * pxy保存的就是当前stl文件中三角形顶点在纹理图片上对应的坐标。每个顶点占2个浮点数（对应x、y）
 */

public class GLRender implements GLSurfaceView.Renderer {

    public Context context;
    //纹理的ID是保存在一个int[]数组中，数组的第一个元素即为ID。
    private int[] textureids;
    private IntBuffer vertexBuffer;
    private IntBuffer texBuffer;
    private FloatBuffer mColorBuffer;
    private float xrot, yrot, zrot;
    private int one = 0x10000;

    // 正方体顶点
    private int[] vertices = {
            one, one, -one,
            -one, one, -one,
            one, one, one,

            -one, one, one,
            one, -one, one,
            -one, -one, one,

            one, -one, -one,
            -one, -one, -one,
            one, one, one,

            -one, one, one,
            one, -one, one,
            -one, -one, one,

            one, -one, -one,
            -one, -one, -one,
            one, one, -one,

            -one, one, -one,
            -one, one, one,
            -one, one, -one,

            -one, -one, one,
            -one, -one, -one,
            one, one, -one,

            one, one, one,
            one, -one, -one,
            one, -one, one
    };

    //纹理点
    private int[] texCoords = {
            0, one,
            one, one,
            0, 0,
            one, 0
    };

    //三角形各顶点颜色(三个顶点)
    private float[] mColor = new float[]{
            1, 1, 0, 1,
            0, 1, 1, 1,
            1, 0, 1, 1
    };

    public GLRender(Context context) {
        this.context = context;
        // 初始化
        textureids = new int[1];

        //OpenGL并不是对堆里面的数据进行操作，而是在直接内存中（Direct Memory），
        // 即操作的数据需要保存到NIO里面的Buffer对象中。而我们上面声明的float[]对象保存在堆中，
        // 因此，需要我们将float[]对象转为java.nio.Buffer对象。
        //先初始化buffer，数组的长度*4，因为一个float占4个字节（此为int）
        ByteBuffer vbb = ByteBuffer.allocateDirect(vertices.length * 4);
        //以本机字节顺序来修改此缓冲区的字节顺序
        //OpenGL在底层的实现是C语言，与Java默认的数据存储字节顺序可能不同，即大端小端问题。
        // 因此，为了保险起见，在将数据传递给OpenGL之前，我们需要指明使用本机的存储顺序。
        vbb.order(ByteOrder.nativeOrder());
        vertexBuffer = vbb.asIntBuffer();
        //将给定float[]数据从当前位置开始，依次写入此缓冲区
        vertexBuffer.put(vertices);
        //设置此缓冲区的位置。如果标记已定义并且大于新的位置，则要丢弃该标记。
        vertexBuffer.position(0);

        //纹理点
        ByteBuffer tbb = ByteBuffer.allocateDirect(texCoords.length * 4 * 6);
        tbb.order(ByteOrder.nativeOrder());
        texBuffer = tbb.asIntBuffer();
        //为每一个面贴上纹理
        for (int i = 0; i < 6; i++) {
            texBuffer.put(texCoords);
        }
        texBuffer.position(0);

        //颜色相关
        ByteBuffer bb2 = ByteBuffer.allocateDirect(mColor.length * 4);
        bb2.order(ByteOrder.nativeOrder());
        mColorBuffer = bb2.asFloatBuffer();
        mColorBuffer.put(mColor);
        mColorBuffer.position(0);
    }

    @Override
    public void onDrawFrame(GL10 gl) {


        // 1.清除屏幕和深度缓存
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT);

        //为了防止前面执行过变换“保留”在“当前矩阵”，我们需要把“当前矩阵”复位，即变为单位矩阵（对角线上的元素全为1）
        // 2.重置当前的模型观察矩阵
        gl.glLoadIdentity();

        //当我们需要启用顶点数组（保存每个顶点的坐标数据）、顶点颜色数组（保存每个顶点的颜色）等等
        //以下两步为绘制颜色与顶点前必做操作
        // 3.开启顶点和纹理功能(开启顶点和纹理缓冲)
        // 允许设置顶点
        //GL10.GL_VERTEX_ARRAY顶点数组
        gl.glEnableClientState(GL10.GL_VERTEX_ARRAY);
        // 允许设置颜色
        //GL10.GL_COLOR_ARRAY颜色数组
        gl.glEnableClientState(GL10.GL_COLOR_ARRAY);

        //启用纹理坐标数组
        gl.glEnableClientState(GL10.GL_TEXTURE_COORD_ARRAY);

        //其实就是设置一个指针，这个指针指向顶点数组，后面绘制三角形（或矩形）根据这里指定的顶点数组来读取数据。
        /**
         size: 每个顶点有几个数值描述。必须是2，3 ，4 之一。
         type: 数组中每个顶点的坐标类型。取值：GL_BYTE,GL_SHORT, GL_FIXED, GL_FLOAT。
         stride：数组中每个顶点间的间隔，步长（字节位移）。取值若为0，表示数组是连续的
         pointer：即存储顶点的Buffer
         */
        // 4.设置点点和纹理
        // 设置三角形顶点数据源
        gl.glVertexPointer(3, GL10.GL_FIXED, 0, vertexBuffer);

        //将纹理坐标数据设定好
        gl.glTexCoordPointer(2, GL10.GL_FIXED, 0, texBuffer);

        //跟上面类似，只是设定指向颜色数组的指针。
        gl.glColorPointer(4, GL10.GL_FLOAT, 0, mColorBuffer);
        /**
         size: 每种颜色组件的数量。 值必须为 3 或 4。
         type: 颜色数组中的每个颜色分量的数据类型。 使用下列常量指定可接受的数据类型：GL_BYTE GL_UNSIGNED_BYTE，
                GL_SHORT GL_UNSIGNED_SHORT，GL_INT GL_UNSIGNED_INT，GL_FLOAT，或 GL_DOUBLE。
         stride：连续颜色之间的字节偏移量。 当偏移量为0时，表示数据是连续的。
         pointer：即颜色的Buffer
         */


        // 5.向z轴里移入6.0f
        gl.glTranslatef(0.0f, 0.0f, -5.0f);

        //绕（1,0,0）向量旋转30度
        //gl.glRotatef(30, 1, 0, 0);
        //沿x轴方向移动1个单位
        //gl.glTranslatef(1, 0, 0);
        //x，y，z方向放缩0.1倍
        //gl.glScalef(0.1f, 0.1f, 0.1f);
        //上面的效果都是矩阵相乘实现，因此我们需要注意变换次序问题

        //如果我们不想改变物体，而是改变观察点
        /**
         * gl: GL10型变量
         * eyeX,eyeY,eyeZ: 观测点坐标（相机坐标）
         * centerX,centerY,centerZ：观察位置的坐标
         * upX,upY,upZ ：相机向上方向在世界坐标系中的方向（即保证看到的物体跟期望的不会颠倒）
         */
        //GLU.gluLookAt(gl, eyeX, eyeY, eyeZ, centerX, centerY, centerZ, upX, upY, upZ);


        // 6.设置3个方向的旋转
        gl.glRotatef(xrot, 1.0f, 0.0f, 0.0f);
        gl.glRotatef(yrot, 0.0f, 1.0f, 0.0f);
        gl.glRotatef(zrot, 0.0f, 0.0f, 1.0f);

        // 7.绘制正方体
        for (int i = 0; i < 6; i++) {
            switch (i) {
                case 0:
                    // 8.生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.iBitmap, 0);
                    break;
                case 1:
                    // 生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.jBitmap, 0);
                    break;
                case 2:
                    // 生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.kBitmap, 0);
                    break;
                case 3:
                    // 生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.lBitmap, 0);
                    break;
                case 4:
                    // 生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.mBitmap, 0);
                    break;
                case 5:
                    // 生成纹理
                    GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.nBitmap, 0);
                    break;
            }
            //绘制数组里面所有点构成的各个三角片。
            /**
             mode：有三种取值
                GL_TRIANGLES：每三个顶之间绘制三角形，之间不连接
                GL_TRIANGLE_FAN：以V0 V1 V2,V0 V2 V3,V0 V3 V4，……的形式绘制三角形
                GL_TRIANGLE_STRIP：顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。
                    以V0 V1 V2 ,V1 V2 V3,V2 V3 V4,……的形式绘制三角形
             first：从数组缓存中的哪一位开始绘制，一般都定义为0
             count：顶点的数量
             */
            gl.glDrawArrays(GL10.GL_TRIANGLE_STRIP, i * 4, 4);
        }

        // 9.关闭顶点和纹理功能
        //// 取消顶点设置
        gl.glDisableClientState(GL10.GL_TEXTURE_COORD_ARRAY);
        gl.glDisableClientState(GL10.GL_VERTEX_ARRAY);

        // 10.调节旋转角度
        xrot += 0.5f;
        yrot += 0.4f;
        zrot += 0.6f;

    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float ratio = (float) (width) / height;

        //每次窗口发生变化时，我们可以设置绘制区域，即在onSurfaceChanged函数中调用glViewport函数。
        //设置OpenGL场景的大小
        gl.glViewport(0, 0, width, height);


        //投影变换,OpenGL支持主要两种投影变换:透视投影 正投影
        //设置投影矩阵
        gl.glMatrixMode(GL10.GL_PROJECTION);
        //重置投影矩阵
        //glLoadIdentity()函数也需要立即调用
        gl.glLoadIdentity();

        //通过如下函数可将当前可视空间设置为透视投影空间：
        // 设置视口的大小
        //left,right,bottom,top,near(frustum到物体的距离),far(frustum到投影屏幕的距离)
        gl.glFrustumf(-ratio, ratio, -1, 1, 1, 10);

        //也可以通过另一个函数实现相同的效果：
        //GLU.gluPerspective(gl,fovy,aspect,near,far);


        //openGL的变换其实都是通过矩阵相乘来实现的
        //在进行变换之前，我们需要声明当前是使用哪种变换
        // 选择模型观察矩阵
        gl.glMatrixMode(GL10.GL_MODELVIEW);
        // 重置模型观察矩阵
        gl.glLoadIdentity();


        //以上都是针对改变物体的位置和方向来介绍的。如果要改变观察点的位置，除了配合使用glRotate*和glTranslate*函数以外，还可以使用这个函数：gluLookAt。
        // 它的参数比较多，前三个参数表示了观察点的位置，中间三个参数表示了观察目标的位置，最后三个参数代表从(0,0,0)到 (x,y,z)的直线，它表示了观察者认为的方向。
    }

    //灯光
    float[] ambient = {0.9f, 0.9f, 0.9f, 1.0f,};
    float[] diffuse = {0.5f, 0.5f, 0.5f, 1.0f,};
    float[] specular = {1.0f, 1.0f, 1.0f, 1.0f,};
    float[] lightPosition = {0.5f, 0.5f, 0.5f, 0.0f,};

    //材料
    float[] materialAmb = {0.4f, 0.4f, 1.0f, 1.0f};
    float[] materialDiff = {0.0f, 0.0f, 1.0f, 1.0f};//漫反射设置蓝色
    float[] materialSpec = {1.0f, 0.5f, 0.0f, 1.0f};

    public void openLight(GL10 gl) {

        gl.glEnable(GL10.GL_LIGHTING);
        gl.glEnable(GL10.GL_LIGHT0);
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_AMBIENT, Util.floatToBuffer(ambient));
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_DIFFUSE, Util.floatToBuffer(diffuse));
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_SPECULAR, Util.floatToBuffer(specular));
        gl.glLightfv(GL10.GL_LIGHT0, GL10.GL_POSITION, Util.floatToBuffer(lightPosition));
    }

    public void enableMaterial(GL10 gl) {

        //材料对环境光的反射情况
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_AMBIENT, Util.floatToBuffer(materialAmb));
        //散射光的反射情况
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_DIFFUSE, Util.floatToBuffer(materialDiff));
        //镜面光的反射情况
        gl.glMaterialfv(GL10.GL_FRONT_AND_BACK, GL10.GL_SPECULAR, Util.floatToBuffer(materialSpec));

    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        //如果OpenGL在某些地方不能有效执行是，给他指定其他操作。
        // 告诉系统对透视进行修正
        /**
         GL_FOG_HINT：指定雾化计算的精度。如果OpenGL实现不能有效的支持每个像素的雾化计算，则GL_DONT_CARE和GL_FASTEST雾化效果中每个定点的计算。
         GL_LINE_SMOOTH_HINT：指定反走样线段的采样质量。如果应用较大的滤波函数，GL_NICEST在光栅化期间可以生成更多的像素段。
         GL_PERSPECTIVE_CORRECTION_HINT：指定颜色和纹理坐标的差值质量。如果OpenGL不能有效的支持透视修正参数差值，那么GL_DONT_CARE 和 GL_FASTEST可以执行颜色、纹理坐标的简单线性差值计算。
         GL_POINT_SMOOTH_HINT：指定反走样点的采样质量，如果应用较大的滤波函数，GL_NICEST在光栅化期间可以生成更多的像素段。
         GL_POLYGON_SMOOTH_HINT：指定反走样多边形的采样质量，如果应用较大的滤波函数，GL_NICEST在光栅化期间可以生成更多的像素段。
         */
        /**
         GL_FASTEST：选择速度最快选项。
         GL_NICEST：选择最高质量选项。
         GL_DONT_CARE：对选项不做考虑。
         */
        gl.glHint(GL10.GL_PERSPECTIVE_CORRECTION_HINT, GL10.GL_FASTEST);


        //设置清屏颜色，每次清屏时，使用该颜色填充整个屏幕，RGBA，取值范围为[0,1]而不是[0,255]
        // 绿色背景
        gl.glClearColor(0, 1, 0, 0);
        //用于启用各种功能。功能由参数决定.根据函数glCullFace要求启用隐藏图形材料的面
        gl.glEnable(GL10.GL_CULL_FACE);

        //设置着色器模式，有如下两个选择：GL10.GL_FLAT  GL10.GL_SMOOTH（默认）
        /**
         GL_SMOOTH：根据顶点的不同颜色，最终以渐变的形式填充图形。
         GL_FLAT：假设有n个三角片，则取最后n个顶点的颜色填充着n个三角片。
         */
        // 启用阴影平滑
        gl.glShadeModel(GL10.GL_SMOOTH);

        //gl.glDepthFunc(GL10.GL_LEQUAL); 该函数只有启用“深度测试”时才有效 及glDisable(GL_DEPTH_TEST)关闭深度测试。
        ////关闭深度测试      gl.glDisable(GL10.GL_DEPTH_TEST)
        // 启用深度测试
        gl.glEnable(GL10.GL_DEPTH_TEST);


        //me
        //开启灯照效果
        gl.glEnable(GL10.GL_LIGHTING);
        // 启用光源0
        gl.glEnable(GL10.GL_LIGHT0);
        // 启用颜色追踪
        gl.glEnable(GL10.GL_COLOR_MATERIAL);
        //一旦开启了光照功能，就可以通过glLightfv函数来指定各种反射光的颜色了
        /**
         public void glLightfv(int light,int pname, FloatBuffer params)
         public void glLightfv(int light,int pname,float[] params,int offset)
         public void glLightf(int light,int pname,float param)
         light: 指光源的序号，OpenGL ES可以设置从0到7共八个光源。
         pname: 光源参数名称，可以有如下：
         GL_SPOT_EXPONENT
         GL_SPOT_CUTOFF
         GL_CONSTANT_ATTENUATION
         GL_LINEAR_ATTENUATION
         GL_QUADRATIC_ATTENUATION
         GL_AMBIENT(用于设置环境光颜色)
         GL_DIFFUSE(用于设置漫反射光颜色)
         GL_SPECULAR(用于设置镜面反射光颜色)
         GL_SPOT_DIRECTION
         GL_POSITION（用于设置光源位置）
         params: 参数的值（数组或是Buffer类型），数组里面含有4个值分别表示R,G,B,A。
         */
        openLight(gl);

        /**
         public void glMaterialf(int face,int pname,float param)
         public void glMaterialfv(int face,int pname,float[] params,int offset)
         public void glMaterialfv(int face,int pname,FloatBuffer params)
         face : 在OpenGL ES中只能使用GL_FRONT_AND_BACK，表示修改物体的前面和后面的材质光线属性。
         pname: 参数类型，这些参数用在光照方程。可以取如下值：
         GL_AMBIENT
         GL_DIFFUSE
         GL_SPECULAR
         GL_EMISSION
         GL_SHININESS。
         param：指定反射的颜色。
         */
        enableMaterial(gl);

        //给深度缓存设定默认值。缓存中的每个像素的深度值默认都是这个， 假设在 gl.glDepthFunc(GL10.GL_LEQUAL);前提下：
        /**
         如果指定“当前像素值”为1时，我们知道，一个模型深度值取值和范围为[0,1]。这个时候你往里面画一个物体，
            由于物体的每个像素的深度值都小于等于1， 所以整个物体都被显示了出来。
         如果指定“当前像素值”为0， 物体的每个像素的深度值都大于等于0， 所以整个物体都不可见。
         如果指定“当前像素值”为0.5， 那么物体就只有深度小于等于0.5的那部分才是可见的
         */
        //启用纹理映射
        gl.glClearDepthf(1.0f);

        //OpenGL中物体模型的每个像素都有一个深度缓存的值（在0到1之间，可以看成是距离）,可以通过glClearDepthf函数设置默认的“当前像素”z值。
        //在绘制时，通过将待绘制的模型像素点的深度值与“当前像素”z值进行比较，将符合条件的像素绘制出来，不符合条件的不绘制
        //深度测试的类型
        /**
         GL10.GL_NEVER：永不绘制
         GL10.GL_LESS：只绘制模型中像素点的z值<当前像素z值的部分
         GL10.GL_EQUAL：只绘制模型中像素点的z值=当前像素z值的部分
         GL10.GL_LEQUAL：只绘制模型中像素点的z值<=当前像素z值的部分
         GL10.GL_GREATER ：只绘制模型中像素点的z值>当前像素z值的部分
         GL10.GL_NOTEQUAL：只绘制模型中像素点的z值!=当前像素z值的部分
         GL10.GL_GEQUAL：只绘制模型中像素点的z值>=当前像素z值的部分
         GL10.GL_ALWAYS：总是绘制
         */
        gl.glDepthFunc(GL10.GL_LEQUAL);//只绘制模型中像素点的z值<=当前像素z值的部分

        //跟绘制三角形类似，如果需要开启贴纹理功能需要如下代码：
        // 1.允许2D贴图,纹理
        gl.glEnable(GL10.GL_TEXTURE_2D);

        //生成ID，并将此ID保存到Model对象中
        // 2.创建纹理
        gl.glGenTextures(1, textureids, 0);

        //根据ID绑定对应的纹理
        //在将ID和Bitmap绑定之前，需要调用glBindTexture函数。将生成的ID绑定到纹理通道
        // 3.绑定要使用的纹理
        gl.glBindTexture(GL10.GL_TEXTURE_2D, textureids[0]);

        //通过GLUtils.texImage2D函数将Bitmap对象与当前纹理通道绑定，而当前纹理通道已经绑定好了ID，从而达到了ID与纹理的间接绑定
        // 4.生成纹理
        GLUtils.texImage2D(GL10.GL_TEXTURE_2D, 0, GLImage.iBitmap, 0);

        // 5.线性滤波
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MIN_FILTER, GL10.GL_LINEAR);
        gl.glTexParameterx(GL10.GL_TEXTURE_2D, GL10.GL_TEXTURE_MAG_FILTER, GL10.GL_LINEAR);

    }

}