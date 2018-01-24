package com.roger.opengl3;

import android.opengl.GLSurfaceView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    GLSurfaceView.Renderer render = new GLRender(this);
    GLSurfaceView glView;
    Button start;           // 演示开始


    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GLImage.load(this.getResources());
        glView = new GLSurfaceView(this);


        glView.setRenderer(render);
//        setContentView(R.layout.activity_main);
//        start = (Button) findViewById(R.id.button1);   // "演示开始"按钮初始化
//        start.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
        // TODO Auto-generated method stub
        setContentView(glView);
//            }
//        });


        //setContentView(glView);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d("MainActivity", "keyCode:" + keyCode);
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        Log.d("MainActivity", "keyCode:" + event.getAction());
        return super.onGenericMotionEvent(event);
    }
}
