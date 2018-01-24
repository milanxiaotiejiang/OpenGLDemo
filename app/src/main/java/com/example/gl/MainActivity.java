package com.example.gl;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

/**
 *
 * http://blog.csdn.net/column/details/android-opengl.html
 *
 * 鸣谢
 * https://github.com/Rogero0o
 * https://github.com/doggycoder
 *
 */

public class MainActivity extends AppCompatActivity {

    private GLSurfaceView glView;
    private GLRender render;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_main);
        GLImage.load(this.getResources());
        glView = new GLSurfaceView(this);
        render = new GLRender(this);

        glView.setRenderer(render);
        setContentView(glView);
    }
}
