package com.yalantis.starwarsdemo.view;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.yalantis.starwarsdemo.R;
import com.yalantis.starwarsdemo.interfaces.DemoActivityInterface;
import com.yalantis.starwarsdemo.interfaces.GreetingFragmentInterface;
import com.yalantis.starwarsdemo.interfaces.TilesRendererInterface;
import com.yalantis.starwarsdemo.particlesys.ParticleSystemRenderer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Artem Kholodnyi on 11/11/15.
 */
public class DemoActivity extends AppCompatActivity implements GreetingFragmentInterface,
        DemoActivityInterface, TilesRendererInterface {
    @BindView(R.id.gl_surface_view)
    GLSurfaceView mGlSurfaceView;

    private SideFragment mDarkFragment;
    private SideFragment mBrightFragment;
    private GreetingsFragment mGreetingsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_demo);
        ButterKnife.bind(this);

        // Check if the system supports OpenGL ES 2.0.
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        //用于获取设备的信息，返回值是ConfigurationInfo。
        final ConfigurationInfo configurationInfo = activityManager.getDeviceConfigurationInfo();
        /**
         reqTouchScreen：设备的触摸屏幕信息。
         值: Configuration.TOUCHSCREEN_NOTOUCH 不能触摸
         Configuration.TOUCHSCREEN_STYLUS 手写笔
         Configuration.TOUCHSCREEN_FINGER手指触摸
         reqKeyboardType：应用程序的输入方法的偏好。
         值：Configuration.KEYBOARD_UNDEFINED（不明）
         Configuration.KEYBOARD_NOKEYS（没有物理键盘）
         Configuration.KEYBOARD_QWERTY（普通键盘）
         Configuration.KEYBOARD_12KEY（12键的小键盘）
         reqNavigation：设备导航
         值：Configuration.NAVIGATION_UNDEFINED（不明）
         Configuration.NAVIGATION_DPAD（DPAD导航）
         Configuration.NAVIGATION_TRACKBALL（轨迹球导航）
         Configuration.NAVIGATION_WHEEL（滚轮导航）
         reqInputFeatures：输入功能的特性
         值：INPUT_FEATURE_HARD_KEYBOARD（物理键盘输入）
         INPUT_FEATURE_FIVE_WAY_NAV （软键盘输入）
         reqGlEsVersion：应用程序使用的GLES版本。
         */
        final boolean supportsEs2 = configurationInfo.reqGlEsVersion >= 0x20000;

        if (supportsEs2) {
            // Request an OpenGL ES 2.0 compatible context.
            mGlSurfaceView.setEGLContextClientVersion(2);

            // Set the renderer to our demo renderer, defined below.
            ParticleSystemRenderer mRenderer = new ParticleSystemRenderer(mGlSurfaceView);
            mGlSurfaceView.setRenderer(mRenderer);
            mGlSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);
        } else {
            throw new UnsupportedOperationException();
        }

        if (savedInstanceState == null) {
            showGreetings();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        mGlSurfaceView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGlSurfaceView.onResume();
    }

    //使用Fragment需要熟悉几个类，包括FragmentActivity、FragmentManager、 FragmentTranscation，
    //一个FragmentActivity可以包含多个Fragment， FragmentManager就起到了作用。做Fragment的增加、删除、替换的时候，事务FragmentTranslation 来负责执行。
    private void showGreetings() {
        mGreetingsFragment = GreetingsFragment.newInstance();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.setCustomAnimations(R.anim.slide_downward, 0, R.anim.slide_downward, 0);
        transaction.add(R.id.container, mGreetingsFragment, "greetings");
        transaction.commit();
    }

     @Override
    public void onSetupProfileClick() {
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
                transaction.setCustomAnimations(R.anim.slide_upward, 0);
                transaction.add(R.id.container, BrightSideFragment.newInstance(), "bright");
                transaction.commit();
            }
        });

    }

    @Override
    public void goToSide(int cx, int cy, boolean appBarExpanded, String side) {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();


        mDarkFragment = DarkSideFragment.newInstance(cx, cy, appBarExpanded);
        Fragment fragment;
        switch (side) {
            case "bright":
                fragment = BrightSideFragment.newInstance(cx, cy, appBarExpanded);
                break;
            case "dark":
                fragment = DarkSideFragment.newInstance(cx, cy, appBarExpanded);
                break;
            default:
                throw new IllegalStateException();
        }
        ft.add(R.id.container, fragment, side).commit();
    }

    @Override
    public void removeAllFragmentExcept(@Nullable String tag) {
        List<Fragment> frags = getSupportFragmentManager().getFragments();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        Fragment frag;
        for (int i = 0; i < frags.size(); i++) {
            frag = frags.get(i);
            if (frag == null) {
                continue;
            }
            if (tag == null || !tag.equals(frag.getTag())) {
                ft.remove(frag);
            }
        }
        ft.commit();
    }

    @Override
    public void onTilesFinished() {
        showGreetings();
    }

}
