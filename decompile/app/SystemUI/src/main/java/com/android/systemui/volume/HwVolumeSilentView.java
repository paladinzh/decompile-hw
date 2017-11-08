package com.android.systemui.volume;

import android.content.Context;
import android.media.AudioManager;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.BDReporter;

public class HwVolumeSilentView extends RelativeLayout implements OnCheckedChangeListener {
    private AudioManager mAudioManager;
    private TextView mSientTitle;
    private Switch mSilentSwitch;

    public interface HwSilentViewCallback {
        void initView();

        void playSound(int i);

        void stopSound();

        void updateExpandButton();

        void updateStreamVolume(int i, int i2);

        void updateVisibility(boolean z);
    }

    public HwVolumeSilentView(Context context) {
        super(context);
    }

    public HwVolumeSilentView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HwVolumeSilentView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mAudioManager = (AudioManager) getContext().getSystemService("audio");
        this.mSilentSwitch = (Switch) findViewById(R.id.silent_switch);
        this.mSientTitle = (TextView) findViewById(R.id.silent_title);
        this.mSilentSwitch.setOnCheckedChangeListener(this);
    }

    public void onCheckedChanged(CompoundButton compoundButton, final boolean isChecked) {
        boolean checked = isChecked;
        BDReporter.e(getContext(), 359, "status : " + isChecked);
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                int i;
                AudioManager -get0 = HwVolumeSilentView.this.mAudioManager;
                if (isChecked) {
                    i = 0;
                } else {
                    i = 1;
                }
                -get0.setRingerModeInternal(i);
                return false;
            }
        });
    }

    public void updateViewGroupState(int streamType, int ringMode, boolean expanded) {
        boolean z = false;
        int currentRingMode = ringMode != -1 ? ringMode : this.mAudioManager != null ? this.mAudioManager.getRingerModeInternal() : 2;
        boolean isSilentOrVibrate = 1 != currentRingMode ? currentRingMode == 0 : true;
        if (!expanded && isSilentOrVibrate && 2 == streamType) {
            if (this.mSilentSwitch != null) {
                Switch switchR = this.mSilentSwitch;
                if (1 != currentRingMode) {
                    z = true;
                }
                switchR.setChecked(z);
            }
            updateVisibility(true);
        } else {
            updateVisibility(false);
        }
        if (this.mSientTitle != null) {
            this.mSientTitle.setText(R.string.status_bar_settings_mute_label);
        }
    }

    public void updateVisibility(boolean show) {
        Util.setVisOrGone(this, show);
    }
}
