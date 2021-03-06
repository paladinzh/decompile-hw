package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

public class EyeComfortSeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private Callback mCallback;
    private int mProgress = 50;
    private SeekBar mSeekBar;

    public interface Callback {
        void onProgressChanged(SeekBar seekBar, int i, boolean z);

        void onStartTrackingTouch(SeekBar seekBar);

        void onStopTrackingTouch(SeekBar seekBar);
    }

    public EyeComfortSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130968777);
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        SeekBar seekBar = (SeekBar) view.findViewById(2131886575);
        if (seekBar != this.mSeekBar) {
            this.mSeekBar = seekBar;
            this.mSeekBar.setMax(100);
            this.mSeekBar.setProgress(this.mProgress);
            this.mSeekBar.setOnSeekBarChangeListener(this);
        }
    }

    public void setProgress(int progress) {
        this.mProgress = progress;
        if (this.mSeekBar != null) {
            this.mSeekBar.setOnSeekBarChangeListener(null);
            this.mSeekBar.setProgress(progress);
            this.mSeekBar.setOnSeekBarChangeListener(this);
        }
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
        if (this.mCallback != null) {
            this.mCallback.onStartTrackingTouch(seekBar);
        }
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        if (this.mSeekBar != null) {
            this.mProgress = this.mSeekBar.getProgress();
        }
        if (this.mCallback != null) {
            this.mCallback.onStopTrackingTouch(seekBar);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        this.mProgress = progress;
        if (this.mCallback != null) {
            this.mCallback.onProgressChanged(seekBar, progress, fromTouch);
        }
    }
}
