package com.android.contacts.hap.rcs;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.text.TextUtils;
import com.android.contacts.util.HwLog;
import java.io.IOException;
import java.util.ArrayList;

public class RcsMediaplayer implements OnCompletionListener, OnErrorListener {
    private static String TAG = "RcsMediapplayer";
    private static RcsMediaplayer mInstance;
    private OnErrorListener mErrorListener;
    private String mMark;
    private MediaPlayer mMediaPlayer = null;
    private ArrayList<OnMediaplayerStopListener> mOnMediaplayerStopListenerList = new ArrayList();
    private OnCompletionListener mViewListener;

    public interface OnMediaplayerStopListener {
        void onMediaplayerStop(MediaPlayer mediaPlayer);
    }

    public void addOnMediaplayerStopListener(OnMediaplayerStopListener onMediaplayerStopListener) {
        this.mOnMediaplayerStopListenerList.add(onMediaplayerStopListener);
    }

    public void removeOnMediaplayerStopListener(OnMediaplayerStopListener onMediaplayerStopListener) {
        this.mOnMediaplayerStopListenerList.remove(onMediaplayerStopListener);
    }

    public static synchronized RcsMediaplayer getInstance() {
        RcsMediaplayer rcsMediaplayer;
        synchronized (RcsMediaplayer.class) {
            if (mInstance == null) {
                mInstance = new RcsMediaplayer();
            }
            rcsMediaplayer = mInstance;
        }
        return rcsMediaplayer;
    }

    public void resetListener(String mark, OnCompletionListener listener) {
        this.mViewListener = listener;
    }

    public void play(String mark, String filePath, OnCompletionListener listener, OnErrorListener errorlistener) {
        try {
            stop();
            this.mMark = mark;
            this.mViewListener = listener;
            this.mErrorListener = errorlistener;
            this.mMediaPlayer = new MediaPlayer();
            this.mMediaPlayer.setAudioStreamType(3);
            this.mMediaPlayer.setOnCompletionListener(this);
            this.mMediaPlayer.setOnErrorListener(this);
            this.mMediaPlayer.setDataSource(filePath);
            this.mMediaPlayer.prepare();
            this.mMediaPlayer.start();
        } catch (IllegalArgumentException e) {
            HwLog.e(TAG, "IllegalArgumentException", e);
        } catch (SecurityException e2) {
            HwLog.e(TAG, "SecurityException", e2);
        } catch (IllegalStateException e3) {
            HwLog.e(TAG, "IllegalStateException", e3);
        } catch (IOException e4) {
            HwLog.e(TAG, "IOException", e4);
        }
    }

    public void stop() {
        for (OnMediaplayerStopListener mediaplayerStopListener : this.mOnMediaplayerStopListenerList) {
            if (mediaplayerStopListener != null) {
                mediaplayerStopListener.onMediaplayerStop(this.mMediaPlayer);
            }
        }
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
        this.mMark = null;
    }

    public boolean equalsAndPlaying(String mark) {
        if (this.mMediaPlayer == null || !this.mMediaPlayer.isPlaying()) {
            return false;
        }
        return TextUtils.equals(this.mMark, mark);
    }

    public void onCompletion(MediaPlayer mp) {
        if (this.mViewListener != null) {
            this.mViewListener.onCompletion(mp);
        }
        stop();
    }

    public boolean onError(MediaPlayer mp, int whatError, int extra) {
        HwLog.i(TAG, "onError called, errorCode = " + whatError + ", extra : " + extra);
        if (this.mErrorListener != null) {
            this.mErrorListener.onError(mp, whatError, extra);
        }
        stop();
        return true;
    }
}
