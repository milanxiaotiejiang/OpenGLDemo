package com.roger.airhockey2;

import android.content.Context;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import com.roger.airhockey2.util.LoggerConfig;
import com.roger.airhockey2.util.ShaderHelper;
import com.roger.airhockey2.util.TextResourceReader;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by Administrator on 2016/6/30.
 */
public class AirHockeyRenderer implements GLSurfaceView.Renderer {

    private static final int BYTES_PER_FLOAT = 4;
    private final FloatBuffer vertexData;
    private static final int POSITION_COMOPNENT_COUNT = 2;
    private Context context;
    private int program;
    float[] tableVertices = {
            //Order of coordinates:X,Y,R,G,B


            //Triangle Fan
            0.0f, 0.0f, 1.0f, 1.0f, 1.0f, -0.5f, -0.5f,
            0.7f, 0.7f, 0.7f, 0.5f, -0.5f, 0.7f, 0.7f, 0.7f,
            0.5f, 0.5f, 0.7f, 0.7f, 0.7f, -0.5f, 0.5f,
            0.7f, 0.7f, 0.7f, -0.5f, -0.5f, 0.7f, 0.7f,
            0.7f, -0.5f, 0f, 1f, 0f, 0f, 0.5f,
            0f, 1f, 0f, 0f, 0f, -0.25f, 0f,
            0f, 1f, 0f, 0.25f, 1f, 0f, 0f
    };

    private static final String A_POSITION = "a_Position";
    private int aPostionLocation;

    private static final String A_COLOR = "a_Color";
    private static final int COLOR_COMPONENT_COUNT = 3;
    private static final int STRIDE = (POSITION_COMOPNENT_COUNT + COLOR_COMPONENT_COUNT) * BYTES_PER_FLOAT;
    private int aColorLocation;

    public AirHockeyRenderer(Context context) {
        this.context = context;
        vertexData = ByteBuffer.allocateDirect(tableVertices.length * BYTES_PER_FLOAT).order(
                ByteOrder.nativeOrder()).asFloatBuffer();
        vertexData.put(tableVertices);
    }

    @Override
    public void onSurfaceCreated(GL10 gl10, EGLConfig eglConfig) {
        //清屏
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        //读取顶点着色器
        String vertexShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_vertex_shader);
        //读取片段着色器
        String fragmentShaderSource = TextResourceReader.readTextFileFromResource(context, R.raw.simple_fragment_shader);
        //生成并编译顶点着色器
        int vertexShader = ShaderHelper.compileVertexShader(vertexShaderSource);
        //生成并编译片段着色器
        int fragmentShader = ShaderHelper.compileFragmentShader(fragmentShaderSource);
        //将着色器附加到程序对象上并执行链接操作
        program = ShaderHelper.linkProgram(vertexShader, fragmentShader);

        if (LoggerConfig.ON) {
            ShaderHelper.validateProgram(program);
        }

        GLES20.glUseProgram(program);
        //获取 A_COLOR 在 shader 中的位置
        aColorLocation = GLES20.glGetAttribLocation(program, A_COLOR);
        //获取 A_POSITION 在 shader 中的位置
        aPostionLocation = GLES20.glGetAttribLocation(program, A_POSITION);
        //将读取指针复位
        vertexData.position(0);
        // 指定了渲染时索引值为 aPostionLocation 的顶点属性数组的数据格式和位置
        GLES20.glVertexAttribPointer(aPostionLocation, POSITION_COMOPNENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aPostionLocation);

        vertexData.position(POSITION_COMOPNENT_COUNT);
        GLES20.glVertexAttribPointer(aColorLocation, COLOR_COMPONENT_COUNT, GLES20.GL_FLOAT, false, STRIDE, vertexData);
        GLES20.glEnableVertexAttribArray(aColorLocation);
    }

    @Override
    public void onSurfaceChanged(GL10 gl10, int i, int i1) {
        GLES20.glViewport(0, 0, i, i1);
    }

    @Override
    public void onDrawFrame(GL10 gl10) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_FAN, 0, 6);

        GLES20.glDrawArrays(GLES20.GL_LINES, 6, 2);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 8, 1);

        GLES20.glDrawArrays(GLES20.GL_POINTS, 9, 1);

        /*
        glDrawArrays(int mode, int first,int count)
        参数1：有三种取值
        1.GL_TRIANGLES：每三个顶之间绘制三角形，之间不连接
        2.GL_TRIANGLE_FAN：以V0V1V2,V0V2V3,V0V3V4，……的形式绘制三角形
        3.GL_TRIANGLE_STRIP：顺序在每三个顶点之间均绘制三角形。这个方法可以保证从相同的方向上所有三角形均被绘制。以V0V1V2,V1V2V3,V2V3V4……的形式绘制三角形
        参数2：从数组缓存中的哪一位开始绘制，一般都定义为0
        参数3：顶点的数量
        */

    }
}
