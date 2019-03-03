package com.leo.libqrcode.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Vibrator;

import com.leo.libqrcode.R;

import static android.content.Context.AUDIO_SERVICE;
import static android.content.Context.VIBRATOR_SERVICE;

public final class NotifyUtil {

    private MediaPlayer mediaPlayer;
    private static NotifyUtil notifyUtil;
    private int rePlayCount;

    private NotifyUtil(Context context) {
        mediaPlayer = MediaPlayer.create(context.getApplicationContext(), R.raw.beep);
        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        mediaPlayer.setVolume(0.7f, 0.7f);
        AudioFocusManager.getInstance().initAudioFocusManager(context);
    }

    public static NotifyUtil getInstance(Context context) {
        if (null == notifyUtil) {
            synchronized (NotifyUtil.class) {
                if (null == notifyUtil) {
                    notifyUtil = new NotifyUtil(context);
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
        AudioFocusManager.getInstance().abandonAudioFocus();
        notifyUtil = null;
    }

    /*
     * 警示响铃
     * */
    public void playBeepSoundAndVibrate(Context context, boolean vibrate) {
        playBeepSoundAndVibrate(context, vibrate, 1);
    }

    public synchronized void playBeepSoundAndVibrate(Context context, boolean vibrate, int count) {
        AudioManager audioService = (AudioManager) context.getSystemService(AUDIO_SERVICE);
        // 检查当前是否是静音模式
        if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
            return;
        }
        // 请求音频焦点
        int result = AudioFocusManager.getInstance().requestAudioFocus();
        if (result == AudioManager.AUDIOFOCUS_REQUEST_FAILED) {
            return;
        }
        this.rePlayCount = count;
        try {
            if (rePlayCount == 1) {//单次
                mediaPlayer.setOnCompletionListener(null);
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
            Vibrator vibrator = (Vibrator) context.getSystemService(VIBRATOR_SERVICE);
            vibrator.vibrate(200L);
        }
    }
}
