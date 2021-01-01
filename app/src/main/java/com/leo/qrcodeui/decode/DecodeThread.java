package com.leo.qrcodeui.decode;

import android.os.Handler;
import android.os.Looper;


import java.lang.ref.WeakReference;
import java.util.concurrent.CountDownLatch;

import com.leo.qrcodeui.ui.CaptureActivity;

/**
 * 描述: 解码线程
 */
public final class DecodeThread extends Thread {

    private WeakReference<CaptureActivity> weakReference;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    public DecodeThread(CaptureActivity activity) {
        weakReference = new WeakReference<>(activity);
        handlerInitLatch = new CountDownLatch(1);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        CaptureActivity activity = weakReference.get();
        if (null != activity) {
            handler = new DecodeHandler(activity);
            handlerInitLatch.countDown();
        }
        Looper.loop();
    }

}
