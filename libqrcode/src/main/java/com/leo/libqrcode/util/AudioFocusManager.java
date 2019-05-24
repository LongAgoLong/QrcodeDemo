/**
 * Copyright (C) 2016 Baidu, Inc. All Rights Reserved.
 */
package com.leo.libqrcode.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.os.Handler;

public class AudioFocusManager {
    public static final String TAG = "AudioFocusManager";
    public static int AUDIO_STREAM_TYPE = AudioManager.STREAM_MUSIC;

    private static final Object mLock = new Object();
    private static AudioFocusManager mInstance;
    private AudioManager mAudioManager;
    private Handler mHandler;

    private AudioFocusManager() {

    }

    /**
     * 单例获得AudioFocusManager对象
     *
     * @return
     */
    public static AudioFocusManager getInstance() {
        if (mInstance == null) {
            synchronized (mLock) {
                if (mInstance == null) {
                    mInstance = new AudioFocusManager();
                }
            }
        }
        return mInstance;
    }

    /**
     * 初始化AudioFocusManager
     *
     * @param context：对用对象context
     */
    public void initAudioFocusManager(Context context) {
        if (mAudioManager == null) {
            mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }
        if (mHandler == null) {
            mHandler = new Handler();
        }
    }

    public int requestAudioFocus() {
        if (mAudioManager == null) {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.requestAudioFocus(mAudioFocusListener,
                AUDIO_STREAM_TYPE, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)) {
            mTtsAudioFocus = AudioManager.AUDIOFOCUS_GAIN;
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
    }

    public int abandonAudioFocus() {
        if (AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager.abandonAudioFocus(mAudioFocusListener)) {
            mTtsAudioFocus = AudioManager.AUDIOFOCUS_LOSS;
            return AudioManager.AUDIOFOCUS_REQUEST_GRANTED;
        } else {
            return AudioManager.AUDIOFOCUS_REQUEST_FAILED;
        }
    }

    public void setAudioStreamType(int type) {
        this.AUDIO_STREAM_TYPE = type;
    }

    private int mTtsAudioFocus;

    public int getAudioFocus() {
        return mTtsAudioFocus;
    }

    private OnAudioFocusChangeListener mRegisterAudioFocusListener;

    public void registerAudioFocusListener(OnAudioFocusChangeListener mTtsAudioFocusListener) {
        mRegisterAudioFocusListener = mTtsAudioFocusListener;
    }

    private OnAudioFocusChangeListener mAudioFocusListener = new OnAudioFocusChangeListener() {
        @Override
        public void onAudioFocusChange(int focusChange) {
            if (mRegisterAudioFocusListener != null) {
                mRegisterAudioFocusListener.onAudioFocusChange(focusChange);
            }
            mTtsAudioFocus = focusChange;
            switch (focusChange) {
                case AudioManager.AUDIOFOCUS_GAIN:
                case AudioManager.AUDIOFOCUS_GAIN_TRANSIENT:
                    break;
                case AudioManager.AUDIOFOCUS_LOSS:
                case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                    break;
                default:
                    break;
            }
        }
    };

}
