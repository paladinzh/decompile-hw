package com.huawei.gallery.app;

import android.content.Context;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.gallery.util.HwCustGalleryUtils;

public class HwCustSlideShowPageImpl extends HwCustSlideShowPage {
    public static final String KEY_BCK_AUDIO = "back-audio";
    public static final String KEY_INTERVAL = "interval";
    private OnAudioFocusChangeListener mAudioListener = new OnAudioFocusChangeListener() {
        public void onAudioFocusChange(int focusChange) {
            switch (focusChange) {
                case -2:
                case -1:
                    HwCustSlideShowPageImpl.this.mRemoveListener = false;
                    HwCustSlideShowPageImpl.this.handlePauseCustomizations();
                    break;
                case 1:
                    HwCustSlideShowPageImpl.this.handleResumeCustomizations();
                    break;
            }
            HwCustSlideShowPageImpl.this.mRemoveListener = true;
        }
    };
    private AudioManager mAudioManager;
    private MediaPlayer mMediaPlayer;
    private boolean mRemoveListener = true;

    public void handleResumeCustomizations() {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && this.mMediaPlayer != null && requestAudioFocus()) {
            this.mRemoveListener = true;
            this.mMediaPlayer.start();
        }
    }

    public void handlePauseCustomizations() {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && this.mMediaPlayer != null) {
            this.mMediaPlayer.pause();
            if (this.mRemoveListener) {
                removeAudioFocus();
            }
            this.mRemoveListener = true;
        }
    }

    public void releaseCustmizations() {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && this.mMediaPlayer != null) {
            this.mMediaPlayer.stop();
            this.mMediaPlayer.release();
            this.mMediaPlayer = null;
        }
    }

    public void initializeCustData(Bundle data, GLHost host) {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && data != null && host != null) {
            int duration = data.getInt(KEY_INTERVAL);
            HwCustGalleryUtils.setSlideShowDuration(duration);
            HwCustGalleryUtils.setAnimationDuration(duration);
            String audioPath = data.getString(KEY_BCK_AUDIO);
            if (!TextUtils.isEmpty(audioPath)) {
                this.mMediaPlayer = MediaPlayer.create((Context) host.getGalleryContext(), Uri.parse(audioPath));
                if (this.mMediaPlayer != null) {
                    this.mMediaPlayer.setLooping(true);
                }
            }
            this.mAudioManager = (AudioManager) ((Context) host.getGalleryContext()).getSystemService("audio");
        }
    }

    private boolean requestAudioFocus() {
        if (this.mAudioManager.requestAudioFocus(this.mAudioListener, 3, 2) == 1) {
            return true;
        }
        return false;
    }

    private void removeAudioFocus() {
        if (HwCustGalleryUtils.isSlideshowSettingsSupported() && this.mAudioListener != null) {
            this.mAudioManager.abandonAudioFocus(this.mAudioListener);
        }
    }

    public boolean handleCustShowPendingBitmap(Handler handler, int msgloadNextBitmap) {
        if (!HwCustGalleryUtils.isSlideshowSettingsSupported() || handler == null) {
            return false;
        }
        handler.sendEmptyMessageDelayed(msgloadNextBitmap, (long) HwCustGalleryUtils.getSlideShowDuration());
        return true;
    }
}
