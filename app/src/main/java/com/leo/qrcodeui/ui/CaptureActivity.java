package com.leo.qrcodeui.ui;

import android.app.Activity;
import android.graphics.Point;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.leo.libqrcode.camera.CameraManager;
import com.leo.libqrcode.util.NotifyUtil;
import com.leo.qrcodedemo.R;

import java.io.IOException;

import com.leo.qrcodeui.decode.CaptureActivityHandler;
import com.leo.qrcodeui.decode.InactivityTimer;

/**
 * 描述: 扫描界面
 */
public abstract class CaptureActivity extends AppCompatActivity implements Callback {

    int FLAGS_FULLSCREEN = View.SYSTEM_UI_FLAG_LOW_PROFILE |
            View.SYSTEM_UI_FLAG_FULLSCREEN |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

    protected CaptureActivityHandler handler;
    protected boolean hasSurface;
    protected InactivityTimer inactivityTimer;
    protected boolean vibrate;
    protected int x = 0;
    protected int y = 0;
    protected int cropWidth = 0;
    protected int cropHeight = 0;
    protected RelativeLayout mContainer = null;
    protected RelativeLayout mCropLayout = null;
    protected boolean isNeedCapture = false;
    private ImageView lightImg;
    private ImageView mQrLineView;
    private TranslateAnimation mAnimation;

    public boolean isNeedCapture() {
        return isNeedCapture;
    }

    public void setNeedCapture(boolean isNeedCapture) {
        this.isNeedCapture = isNeedCapture;
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getCropWidth() {
        return cropWidth;
    }

    public void setCropWidth(int cropWidth) {
        this.cropWidth = cropWidth;
    }

    public int getCropHeight() {
        return cropHeight;
    }

    public void setCropHeight(int cropHeight) {
        this.cropHeight = cropHeight;
    }

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUI();
        initUI();

        // 初始化 CameraManager
        CameraManager.init(getApplication());
        hasSurface = false;
        inactivityTimer = new InactivityTimer(this);
    }

    private void startScanAnimation() {
        mAnimation = new TranslateAnimation(TranslateAnimation.ABSOLUTE,
                0f, TranslateAnimation.ABSOLUTE, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0f,
                TranslateAnimation.RELATIVE_TO_PARENT, 0.9f);
        mAnimation.setDuration(1500);
        mAnimation.setRepeatCount(-1);
        mAnimation.setRepeatMode(Animation.REVERSE);
        mAnimation.setInterpolator(new LinearInterpolator());
        mQrLineView.setAnimation(mAnimation);
    }

    private void stopScanAnimation() {
        if (null != mAnimation) {
            mAnimation.cancel();
            mAnimation = null;
        }
    }

    protected void setUI() {
        setContentView(R.layout.activity_qr_scan);
    }

    protected void initUI() {
        mContainer = findViewById(R.id.capture_containter);
        mCropLayout = findViewById(R.id.capture_crop_layout);

        lightImg = findViewById(R.id.light_img);
        lightImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                light();
            }
        });
        mQrLineView = findViewById(R.id.capture_scan_line);
    }

    boolean flag = true;

    protected void light() {
        if (flag) {
            flag = false;
            // 开闪光灯
            CameraManager.get().openLight();
            lightImg.setSelected(true);
        } else {
            flag = true;
            // 关闪光灯
            CameraManager.get().offLight();
            lightImg.setSelected(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mContainer.postDelayed(new Runnable() {
            @Override
            public void run() {
                mContainer.setSystemUiVisibility(FLAGS_FULLSCREEN);
            }
        },500);
        SurfaceView surfaceView = findViewById(R.id.capture_preview);
        SurfaceHolder surfaceHolder = surfaceView.getHolder();
        if (hasSurface) {
            initCamera(this, surfaceHolder);
        } else {
            surfaceHolder.addCallback(this);
            surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        }
        setVolumeControlStream(AudioManager.STREAM_MUSIC);
        vibrate = true;
        startScanAnimation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (handler != null) {
            handler.quitSynchronously();
            handler = null;
        }
        CameraManager.get().closeDriver();
        stopScanAnimation();
    }

    @Override
    protected void onDestroy() {
        inactivityTimer.shutdown();
        NotifyUtil.getInstance().destroy();
        super.onDestroy();
    }

    /**
     * 处理结果
     *
     * @param result 二维码内容
     */
    public void handleDecode(String result) {
        inactivityTimer.onActivity();
        scanResult(result);
    }

    public abstract void scanResult(String result);

    /**
     * 连续扫描，不发送此消息扫描一次结束后就不能再次扫描
     */
    protected void restartScan() {
        if (null != handler) {
            handler.sendEmptyMessage(R.id.restart_preview);
        }
    }

    private void initCamera(Activity activity, SurfaceHolder surfaceHolder) {
        try {
            CameraManager.get().openDriver(activity, surfaceHolder);

            Point point = CameraManager.get().getCameraResolution();
            int width = point.y;
            int height = point.x;

            int x = mCropLayout.getLeft() * width / mContainer.getWidth();
            int y = mCropLayout.getTop() * height / mContainer.getHeight();

            int cropWidth = mCropLayout.getWidth() * width / mContainer.getWidth();
            int cropHeight = mCropLayout.getHeight() * height / mContainer.getHeight();

            setX(x);
            setY(y);
            setCropWidth(cropWidth);
            setCropHeight(cropHeight);
            // 设置是否需要截图
            setNeedCapture(false);
        } catch (IOException ioe) {
            return;
        } catch (RuntimeException e) {
            return;
        }
        if (handler == null) {
            handler = new CaptureActivityHandler(CaptureActivity.this);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (!hasSurface) {
            hasSurface = true;
            initCamera(this, holder);
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        hasSurface = false;
    }

    public Handler getHandler() {
        return handler;
    }
}