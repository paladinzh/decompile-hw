package com.android.contacts.hap.rcs;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.contacts.hap.rcs.RcsMediaplayer.OnMediaplayerStopListener;
import com.google.android.gms.R;

public class VoiceImageView extends ImageView implements OnCompletionListener, OnErrorListener, OnMediaplayerStopListener {
    private boolean isAnim = false;
    private AnimationDrawable mAnimationDrawable;
    private String mMark;
    private String mVoicePath;

    public VoiceImageView(Context context) {
        super(context);
    }

    public VoiceImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        RcsMediaplayer.getInstance().addOnMediaplayerStopListener(this);
        this.mAnimationDrawable = (AnimationDrawable) getResources().getDrawable(R.drawable.voice_play_annimation);
    }

    public VoiceImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        RcsMediaplayer.getInstance().removeOnMediaplayerStopListener(this);
        stopAnim();
    }

    private void startAnim() {
        setBackground(this.mAnimationDrawable);
        if (!(this.mAnimationDrawable == null || this.mAnimationDrawable.isRunning())) {
            this.mAnimationDrawable.start();
        }
        this.isAnim = true;
    }

    private void stopAnim() {
        if (this.isAnim) {
            setBackgroundResource(R.drawable.voice_icon_normal);
            if (this.mAnimationDrawable != null) {
                this.mAnimationDrawable.stop();
            }
            this.isAnim = false;
        }
    }

    public void resetState(String mark, String voicePath) {
        this.mMark = mark + voicePath;
        this.mVoicePath = voicePath;
        RcsMediaplayer mp = RcsMediaplayer.getInstance();
        if (mp.equalsAndPlaying(this.mMark)) {
            startAnim();
            mp.resetListener(this.mMark, this);
            return;
        }
        stopAnim();
    }

    public void play() {
        RcsMediaplayer.getInstance().play(this.mMark, this.mVoicePath, this, this);
        startAnim();
    }

    public void onCompletion(MediaPlayer mp) {
        if (isShown()) {
            stopAnim();
        }
    }

    public boolean onError(MediaPlayer mp, int whatError, int extra) {
        if (isShown()) {
            stopAnim();
        }
        return true;
    }

    public void onMediaplayerStop(MediaPlayer mp) {
        if (isShown()) {
            stopAnim();
        }
    }
}
