package com.android.settings.notification;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.settings.SeekBarPreference;

public class ImportanceSeekBarPreference extends SeekBarPreference implements OnSeekBarChangeListener {
    private ColorStateList mActiveSliderTint;
    private boolean mAutoOn;
    private Callback mCallback;
    private Handler mHandler;
    private ColorStateList mInactiveSliderTint;
    private int mMinProgress;
    private final Runnable mNotifyChanged;
    private SeekBar mSeekBar;
    private String mSummary;
    private TextView mSummaryTextView;

    public interface Callback {
        void onImportanceChanged(int i, boolean z);
    }

    public ImportanceSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mNotifyChanged = new Runnable() {
            public void run() {
                ImportanceSeekBarPreference.this.postNotifyChanged();
            }
        };
        setLayoutResource(2130968948);
        this.mActiveSliderTint = ColorStateList.valueOf(context.getColor(2131427493));
        this.mInactiveSliderTint = ColorStateList.valueOf(context.getColor(2131427494));
        this.mHandler = new Handler();
    }

    public ImportanceSeekBarPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ImportanceSeekBarPreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ImportanceSeekBarPreference(Context context) {
        this(context, null);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setMinimumProgress(int minProgress) {
        this.mMinProgress = minProgress;
        notifyChanged();
    }

    public void setProgress(int progress) {
        this.mSummary = getProgressSummary(progress);
        super.setProgress(progress);
    }

    public void setAutoOn(boolean autoOn) {
        this.mAutoOn = autoOn;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        this.mSummaryTextView = (TextView) view.findViewById(16908304);
        this.mSeekBar = (SeekBar) view.findViewById(16909257);
        final ImageView autoButton = (ImageView) view.findViewById(2131886916);
        applyAutoUi(autoButton);
        autoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ImportanceSeekBarPreference.this.applyAuto(autoButton);
            }
        });
    }

    private void applyAuto(ImageView autoButton) {
        boolean z;
        if (this.mAutoOn) {
            z = false;
        } else {
            z = true;
        }
        this.mAutoOn = z;
        if (!this.mAutoOn) {
            setProgress(3);
            if (this.mCallback != null) {
                this.mCallback.onImportanceChanged(3, true);
            }
        } else if (this.mCallback != null) {
            this.mCallback.onImportanceChanged(-1000, true);
        }
        applyAutoUi(autoButton);
    }

    private void applyAutoUi(ImageView autoButton) {
        this.mSeekBar.setEnabled(!this.mAutoOn);
        ColorStateList sliderTint = this.mAutoOn ? this.mInactiveSliderTint : this.mActiveSliderTint;
        ColorStateList starTint = this.mAutoOn ? this.mActiveSliderTint : this.mInactiveSliderTint;
        Drawable icon = autoButton.getDrawable().mutate();
        icon.setTintList(starTint);
        autoButton.setImageDrawable(icon);
        this.mSeekBar.setProgressTintList(sliderTint);
        this.mSeekBar.setThumbTintList(sliderTint);
        if (this.mAutoOn) {
            setProgress(3);
            this.mSummary = getProgressSummary(-1000);
        }
        this.mSummaryTextView.setText(this.mSummary);
    }

    public CharSequence getSummary() {
        return this.mSummary;
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
        super.onProgressChanged(seekBar, progress, fromTouch);
        if (progress < this.mMinProgress) {
            seekBar.setProgress(this.mMinProgress);
            progress = this.mMinProgress;
        }
        if (this.mSummaryTextView != null) {
            this.mSummary = getProgressSummary(progress);
            this.mSummaryTextView.setText(this.mSummary);
        }
        if (this.mCallback != null) {
            this.mCallback.onImportanceChanged(progress, fromTouch);
        }
    }

    private String getProgressSummary(int progress) {
        switch (progress) {
            case 0:
                return getContext().getString(2131626741);
            case 1:
                return getContext().getString(2131626742);
            case 2:
                return getContext().getString(2131626743);
            case 3:
                return getContext().getString(2131626744);
            case 4:
                return getContext().getString(2131626745);
            case 5:
                return getContext().getString(2131626746);
            default:
                return getContext().getString(2131626747);
        }
    }

    protected void notifyChanged() {
        this.mHandler.post(this.mNotifyChanged);
    }

    private void postNotifyChanged() {
        super.notifyChanged();
    }
}
