package com.android.dialer.greeting.presenter;

import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import com.android.contacts.util.HwLog;
import java.io.IOException;

public class PlaybackPresenter implements OnCompletionListener {
    private static final String TAG = PlaybackPresenter.class.getSimpleName();
    private static PlaybackPresenter mInstance;
    private Context mContext;
    private int mInitialOrientation;
    private MediaPlayer mMediaPlayer = null;

    public static synchronized PlaybackPresenter getInstance(Activity activity) {
        PlaybackPresenter playbackPresenter;
        synchronized (PlaybackPresenter.class) {
            if (mInstance == null) {
                mInstance = new PlaybackPresenter();
            }
            mInstance.init(activity);
            playbackPresenter = mInstance;
        }
        return playbackPresenter;
    }

    private void init(Activity activity) {
        this.mContext = activity;
        if (this.mContext != null) {
            this.mInitialOrientation = this.mContext.getResources().getConfiguration().orientation;
        }
    }

    private PlaybackPresenter() {
    }

    public void resumePlayback(Uri uri) {
        if (this.mContext != null) {
            pausePresenter();
            this.mMediaPlayer = new MediaPlayer();
            try {
                this.mMediaPlayer.setOnCompletionListener(this);
                this.mMediaPlayer.setDataSource(this.mContext, uri);
                this.mMediaPlayer.prepare();
                this.mMediaPlayer.start();
            } catch (IOException e) {
                HwLog.e(TAG, "prepare() failed");
            }
        }
    }

    public void onPause() {
        if (this.mContext == null || this.mInitialOrientation == this.mContext.getResources().getConfiguration().orientation) {
            pausePresenter();
        } else {
            HwLog.d(TAG, "onPause: Orientation changed.");
        }
    }

    public void pausePresenter() {
        if (this.mMediaPlayer != null) {
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void onDestroy() {
        this.mContext = null;
    }

    public void onCompletion(MediaPlayer mp) {
        pausePresenter();
    }
}
