package com.leo.libqrcode.util;

import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.leo.libqrcode.R;
import com.leo.system.AudioFocusHelp;
import com.leo.system.ContextHelp;
import com.leo.system.LogUtil;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

public final class NotifyUtil {
    private static final String TAG = NotifyUtil.class.getSimpleName();
    private MediaPlayer mediaPlayer;
    private static NotifyUtil notifyUtil;
    private int rePlayCount;

    private NotifyUtil() {
        mediaPlayer = MediaPlayer.create(ContextHelp.getContext(), R.raw.beep);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(0.7f, 0.7f);
    }

    public static NotifyUtil getInstance() {
        if (null == notifyUtil) {
            synchronized (NotifyUtil.class) {
                if (null == notifyUtil) {
                    notifyUtil = new NotifyUtil();
                }
            }
        }
        return notifyUtil;
    }

    public void destroy() {
        if (null != mediaPlayer) {
            mediaPlayer.stop();
            mediaPlayer = null;
        }
        AudioFocusHelp.getInstance().abandonAudioFocus();
        notifyUtil = null;
    }

    /**
     * 警示响铃
     *
     * @param vibrate
     */
    public void playBeepSoundAndVibrate(boolean vibrate) {
        playBeepSoundAndVibrate(vibrate, 1);
    }

    public synchronized void playBeepSoundAndVibrate(boolean vibrate, int count) {
        AudioManager audioService = (AudioManager) ContextHelp.getContext().getSystemService(AUDIO_SERVICE);
        // 检查当前是否是静音模式
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            LogUtil.i(TAG, "当前为静音模式");
            return;
        }
        // 请求音频焦点
        int result = AudioFocusHelp.getInstance().requestAudioFocus();
        if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            LogUtil.i(TAG, "请求音频焦点失败");
            return;
        }
        this.rePlayCount = count;
        try {
            if (rePlayCount == 1) {//单次
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        AudioFocusHelp.getInstance().abandonAudioFocus();
                    }
                });
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            } else {
                //多次
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        try {
                            if (rePlayCount > 0) {
                                rePlayCount--;
                                mediaPlayer.seekTo(0);
                                mediaPlayer.start();
                            } else {
                                AudioFocusHelp.getInstance().abandonAudioFocus();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                mediaPlayer.seekTo(0);
                mediaPlayer.start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (vibrate) {
            Vibrator vibrator = (Vibrator) ContextHelp.getContext().getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200L);
        }
    }
}
