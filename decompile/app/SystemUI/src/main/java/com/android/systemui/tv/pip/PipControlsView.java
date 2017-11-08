package com.android.systemui.tv.pip;

import android.content.Context;
import android.media.session.MediaController;
import android.media.session.MediaController.Callback;
import android.media.session.PlaybackState;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.tv.pip.PipManager.MediaListener;

public class PipControlsView extends LinearLayout {
    private PipControlButtonView mCloseButtonView;
    private final OnFocusChangeListener mFocusChangeListener;
    private PipControlButtonView mFocusedChild;
    private PipControlButtonView mFullButtonView;
    Listener mListener;
    private MediaController mMediaController;
    private Callback mMediaControllerCallback;
    final PipManager mPipManager;
    private final MediaListener mPipMediaListener;
    private PipControlButtonView mPlayPauseButtonView;

    public interface Listener {
        void onClosed();
    }

    public PipControlsView(Context context) {
        this(context, null, 0, 0);
    }

    public PipControlsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0, 0);
    }

    public PipControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PipControlsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPipManager = PipManager.getInstance();
        this.mMediaControllerCallback = new Callback() {
            public void onPlaybackStateChanged(PlaybackState state) {
                PipControlsView.this.updatePlayPauseView();
            }
        };
        this.mPipMediaListener = new MediaListener() {
            public void onMediaControllerChanged() {
                PipControlsView.this.updateMediaController();
            }
        };
        this.mFocusChangeListener = new OnFocusChangeListener() {
            public void onFocusChange(View view, boolean hasFocus) {
                if (hasFocus) {
                    PipControlsView.this.mFocusedChild = (PipControlButtonView) view;
                } else if (PipControlsView.this.mFocusedChild == view) {
                    PipControlsView.this.mFocusedChild = null;
                }
            }
        };
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(R.layout.tv_pip_controls, this);
        setOrientation(0);
        setGravity(49);
    }

    public void onFinishInflate() {
        super.onFinishInflate();
        this.mFullButtonView = (PipControlButtonView) findViewById(R.id.full_button);
        this.mFullButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mFullButtonView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PipControlsView.this.mPipManager.movePipToFullscreen();
            }
        });
        this.mCloseButtonView = (PipControlButtonView) findViewById(R.id.close_button);
        this.mCloseButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mCloseButtonView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                PipControlsView.this.mPipManager.closePip();
                if (PipControlsView.this.mListener != null) {
                    PipControlsView.this.mListener.onClosed();
                }
            }
        });
        this.mPlayPauseButtonView = (PipControlButtonView) findViewById(R.id.play_pause_button);
        this.mPlayPauseButtonView.setOnFocusChangeListener(this.mFocusChangeListener);
        this.mPlayPauseButtonView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (PipControlsView.this.mMediaController != null && PipControlsView.this.mMediaController.getPlaybackState() != null) {
                    long actions = PipControlsView.this.mMediaController.getPlaybackState().getActions();
                    int state = PipControlsView.this.mMediaController.getPlaybackState().getState();
                    if (PipControlsView.this.mPipManager.getPlaybackState() == 1) {
                        PipControlsView.this.mMediaController.getTransportControls().play();
                    } else if (PipControlsView.this.mPipManager.getPlaybackState() == 0) {
                        PipControlsView.this.mMediaController.getTransportControls().pause();
                    }
                }
            }
        });
    }

    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        updateMediaController();
        this.mPipManager.addMediaListener(this.mPipMediaListener);
    }

    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mPipManager.removeMediaListener(this.mPipMediaListener);
        if (this.mMediaController != null) {
            this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
        }
    }

    private void updateMediaController() {
        MediaController newController = this.mPipManager.getMediaController();
        if (this.mMediaController != newController) {
            if (this.mMediaController != null) {
                this.mMediaController.unregisterCallback(this.mMediaControllerCallback);
            }
            this.mMediaController = newController;
            if (this.mMediaController != null) {
                this.mMediaController.registerCallback(this.mMediaControllerCallback);
            }
            updatePlayPauseView();
        }
    }

    private void updatePlayPauseView() {
        int state = this.mPipManager.getPlaybackState();
        if (state == 2) {
            this.mPlayPauseButtonView.setVisibility(8);
            return;
        }
        this.mPlayPauseButtonView.setVisibility(0);
        if (state == 0) {
            this.mPlayPauseButtonView.setImageResource(R.drawable.ic_pause_white_24dp);
            this.mPlayPauseButtonView.setText(R.string.pip_pause);
            return;
        }
        this.mPlayPauseButtonView.setImageResource(R.drawable.ic_play_arrow_white_24dp);
        this.mPlayPauseButtonView.setText(R.string.pip_play);
    }

    public void reset() {
        this.mFullButtonView.reset();
        this.mCloseButtonView.reset();
        this.mPlayPauseButtonView.reset();
        this.mFullButtonView.requestFocus();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    PipControlButtonView getFocusedButton() {
        return this.mFocusedChild;
    }
}
