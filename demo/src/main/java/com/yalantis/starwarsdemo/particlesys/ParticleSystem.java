package com.yalantis.starwarsdemo.particlesys;

import android.opengl.GLES20;

import com.yalantis.starwars.interfaces.Renderable;
import com.yalantis.starwarsdemo.util.gl.Const;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import timber.log.Timber;

import static android.opengl.GLES20.GL_ARRAY_BUFFER;
import static android.opengl.GLES20.GL_STATIC_DRAW;
import static android.opengl.GLES20.glBindBuffer;
import static android.opengl.GLES20.glBufferData;
import static android.opengl.GLES20.glGenBuffers;

/**
 * Created by Artem Kholodnyi on 11/12/15.
 */
public class ParticleSystem implements Renderable {
    //渲染一个Renderable就算一个批次,Renderable是Ogre中最小的渲染单元,所有需要渲染的对象都继承与此.
    private final ParticleSystemRenderer mRenderer;
    public static final int PARTICLE_COUNT = 1_000;
    private int mBufferId;

    public static final int POS_DATA_SIZE = 3;
    public static final int TEXTURE_COORDS_DATA_SIZE = 2;
    public static final int MISC_DATA_SIZE = 3;


    public ParticleSystem(ParticleSystemRenderer renderer, FloatBuffer vertexBuffer) {
        long startTime = System.currentTimeMillis();

        mRenderer = renderer;

        Timber.d("generated in %d ms", System.currentTimeMillis() - startTime);

        //创建VBO
        //创建VBO需要3个步骤：
        //使用glGenBuffers()生成新缓存对象。
        //使用glBindBuffer()绑定缓存对象。
        //使用glBufferData()将顶点数据拷贝到缓存对象中。

        // Copy buffer into OpenGL's memory. After, we don't need to keep the client-side buffers around.
        final int buffers[] = new int[1];

        //glGenBuffers()创建缓存对象并且返回缓存对象的标示符。它需要2个参数：第一个为需要创建的缓存数量，第二个为用于存储单一ID或多个ID的GLuint变量或数组的地址。
        glGenBuffers(1, buffers, 0);

        //当缓存对象创建之后，在使用缓存对象之前，我们需要将缓存对象连接到相应的缓存上。glBindBuffer()有2个参数：target与buffer。
        //target 告诉 VBO 该缓存对象将保存顶点数组数据还是索引数组数据：GL_ARRAY_BUFFER 或 GL_ELEMENT_ARRAY。
        // 任何顶点属性，如顶点坐标、纹理坐标、法线与颜色分量数组都使用GL_ARRAY_BUFFER。用于glDraw[Range]Elements()的索引数据需要使用GL_ELEMENT_ARRAY绑定。
        // 注意，target标志帮助VBO确定缓存对象最有效的位置，如有些系统将索引保存AGP或系统内存中，将顶点保存在显卡内存中。
        glBindBuffer(GL_ARRAY_BUFFER, buffers[0]);

        //当缓存初始化之后，你可以使用glBufferData()将数据拷贝到缓存对象。
        //第一个参数target可以为GL_ARRAY_BUFFER或GL_ELEMENT_ARRAY。size为待传递数据字节数量。第三个参数为源数据数组指针，如data为NULL，则VBO仅仅预留给定数据大小的内存空间。
        // 最后一个参数usage标志位VBO的另一个性能提示，它提供缓存对象将如何使用：static、dynamic或stream、与read、copy或draw。
        glBufferData(GL_ARRAY_BUFFER, vertexBuffer.capacity() * Const.BYTES_PER_FLOAT, vertexBuffer, GL_STATIC_DRAW);

        glBindBuffer(GL_ARRAY_BUFFER, 0);

        mBufferId = buffers[0];

        vertexBuffer.limit(0);
        Timber.d("done in %d ms", System.currentTimeMillis() - startTime);
    }


    // use to make native order buffers
    private ShortBuffer makeShortBuffer(short[] arr) {
        ByteBuffer bb = ByteBuffer.allocateDirect(arr.length*4);
        bb.order(ByteOrder.nativeOrder());
        ShortBuffer ib = bb.asShortBuffer();
        ib.put(arr);
        ib.position(0);
        return ib;
    }

    @Override
    public void render() {
        final int stride = Const.BYTES_PER_FLOAT
                * (POS_DATA_SIZE + TEXTURE_COORDS_DATA_SIZE + MISC_DATA_SIZE);

        // a_Position
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId);
        GLES20.glEnableVertexAttribArray(mRenderer.positionHandle);
        GLES20.glVertexAttribPointer(mRenderer.positionHandle,
                POS_DATA_SIZE,
                GLES20.GL_FLOAT,
                false,
                stride,
                0
        );

        // a_TexCoordinate
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId);
        GLES20.glEnableVertexAttribArray(mRenderer.textureCoordinateHandle);
        GLES20.glVertexAttribPointer(mRenderer.textureCoordinateHandle,
                TEXTURE_COORDS_DATA_SIZE,
                GLES20.GL_FLOAT,
                false,
                stride,
                Const.BYTES_PER_FLOAT * (POS_DATA_SIZE)
        );

        // a_Misc
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, mBufferId);
        GLES20.glEnableVertexAttribArray(mRenderer.miscHandle);
        GLES20.glVertexAttribPointer(mRenderer.miscHandle,
                MISC_DATA_SIZE,
                GLES20.GL_FLOAT,
                false,
                stride,
                Const.BYTES_PER_FLOAT * (POS_DATA_SIZE + TEXTURE_COORDS_DATA_SIZE)
        );

        // Clear the currently bound buffer (so future OpenGL calls do not use this buffer).
        glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

        // Draw tiles
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, PARTICLE_COUNT * 6);
    }

    @Override
    public void release() {
        final int[] buffersToDelete = new int[] { mBufferId };
        GLES20.glDeleteBuffers(buffersToDelete.length, buffersToDelete, 0);
    }
}
