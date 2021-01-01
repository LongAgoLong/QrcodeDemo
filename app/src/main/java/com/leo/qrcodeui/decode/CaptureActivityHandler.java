package com.leo.qrcodeui.decode;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.IntDef;

import com.leo.libqrcode.R;
import com.leo.libqrcode.camera.CameraManager;

import java.lang.ref.WeakReference;

import com.leo.qrcodeui.ui.CaptureActivity;

/**
 * 描述: 扫描消息转发
 */
public final class CaptureActivityHandler extends Handler {

    private WeakReference<CaptureActivity> weakReference;
    private DecodeThread decodeThread;
    @State
    private int state;

    @IntDef({State.PREVIEW,
            State.SUCCESS,
            State.DONE})
    private @interface State {
        int PREVIEW = 0;
        int SUCCESS = 1;
        int DONE = 2;
    }

    public CaptureActivityHandler(CaptureActivity activity) {
        weakReference = new WeakReference<>(activity);
        decodeThread = new DecodeThread(activity);
        decodeThread.start();
        state = State.SUCCESS;
        CameraManager.get().startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        if (message.what == R.id.auto_focus) {
            if (state == State.PREVIEW) {
                CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
            }
        } else if (message.what == R.id.restart_preview) {
            restartPreviewAndDecode();

        } else if (message.what == R.id.decode_succeeded) {
            state = State.SUCCESS;
            CaptureActivity activity = weakReference.get();
            if (null != activity) {
                // 解析成功，回调
                activity.handleDecode((String) message.obj);
            }
        } else if (message.what == R.id.decode_failed) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
        }
    }

    public void quitSynchronously() {
        state = State.DONE;
        CameraManager.get().stopPreview();
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
        removeMessages(R.id.decode);
        removeMessages(R.id.auto_focus);
    }

    private void restartPreviewAndDecode() {
        if (state == State.SUCCESS) {
            state = State.PREVIEW;
            CameraManager.get().requestPreviewFrame(decodeThread.getHandler(),
                    R.id.decode);
            CameraManager.get().requestAutoFocus(this, R.id.auto_focus);
        }
    }

}
