package com.huawei.systemmanager.netassistant.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class HsmSeekBarPreference extends Preference implements OnSeekBarChangeListener {
    private static final String ANDROIDNS = "http://schemas.android.com/apk/res/android";
    private static final String APPLICATIONNS = "http://com.huawei.systemmanager";
    private final String TAG = getClass().getName();
    private int mCurrentValue;
    private int mInterval = 1;
    private int mMaxValue = 100;
    private int mMinValue = 0;
    private SeekBar mSeekBar;
    private TextView mStatusText;

    public HsmSeekBarPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initPreference(context, attrs);
    }

    public HsmSeekBarPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initPreference(context, attrs);
    }

    private void initPreference(Context context, AttributeSet attrs) {
        setValuesFromXml(context, attrs);
        this.mSeekBar = new SeekBar(context, attrs);
        this.mSeekBar.setMax(this.mMaxValue - this.mMinValue);
        this.mSeekBar.setOnSeekBarChangeListener(this);
        this.mSeekBar.setId(R.id.systemmanager_hsmseekbarpreference);
        setLayoutResource(R.layout.preference_emui);
        setWidgetLayoutResource(R.layout.seekbar_preference);
    }

    private void setValuesFromXml(Context context, AttributeSet attrs) {
        try {
            String newInterval = attrs.getAttributeValue(APPLICATIONNS, "interval");
            if (newInterval != null) {
                this.mInterval = Integer.parseInt(newInterval);
            }
        } catch (Exception e) {
            HwLog.e(this.TAG, "Invalid interval value", e);
        }
    }

    protected View onCreateView(ViewGroup parent) {
        View view = super.onCreateView(parent);
        ((LinearLayout) view).setOrientation(1);
        return view;
    }

    public void onBindView(View view) {
        super.onBindView(view);
        try {
            ViewParent oldContainer = this.mSeekBar.getParent();
            ViewParent newContainer = (ViewGroup) view.findViewById(R.id.seekBarPrefBarContainer);
            if (oldContainer != newContainer) {
                if (oldContainer != null) {
                    ((ViewGroup) oldContainer).removeView(this.mSeekBar);
                }
                newContainer.removeAllViews();
                newContainer.addView(this.mSeekBar, -1, -2);
            }
        } catch (Exception ex) {
            HwLog.e(this.TAG, "Error binding view: " + ex.toString());
        }
        if (!view.isEnabled()) {
            this.mSeekBar.setEnabled(false);
        }
        updateView(view);
    }

    protected void updateView(View view) {
        try {
            this.mStatusText = (TextView) view.findViewById(R.id.seekBarPrefValue);
            this.mStatusText.setText(CommonMethodUtil.formatPercentString(this.mCurrentValue));
            this.mStatusText.setMinimumWidth(30);
            this.mSeekBar.setProgress(this.mCurrentValue - this.mMinValue);
        } catch (Exception e) {
            HwLog.e(this.TAG, "Error updating seek bar preference", e);
        }
    }

    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int newValue = progress + this.mMinValue;
        if (newValue > this.mMaxValue) {
            newValue = this.mMaxValue;
        } else if (newValue < this.mMinValue) {
            newValue = this.mMinValue;
        } else if (!(this.mInterval == 1 || newValue % this.mInterval == 0)) {
            newValue = Math.round(((float) newValue) / ((float) this.mInterval)) * this.mInterval;
        }
        this.mCurrentValue = newValue;
        this.mStatusText.setText(CommonMethodUtil.formatPercentString(this.mCurrentValue));
    }

    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    public void onStopTrackingTouch(SeekBar seekBar) {
        notifyChanged();
        persistInt(seekBar.getProgress());
        callChangeListener(Integer.valueOf(seekBar.getProgress()));
    }

    protected Object onGetDefaultValue(TypedArray ta, int index) {
        return Integer.valueOf(ta.getInt(index, 0));
    }

    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        if (restoreValue) {
            try {
                this.mCurrentValue = getPersistedInt(this.mCurrentValue);
                return;
            } catch (Exception e) {
                HwLog.e(this.TAG, "Invalid default value: " + this.mCurrentValue);
                return;
            }
        }
        int temp = 0;
        try {
            temp = ((Integer) defaultValue).intValue();
        } catch (Exception e2) {
            HwLog.e(this.TAG, "Invalid default value: " + defaultValue.toString());
        }
        persistInt(temp);
        this.mCurrentValue = temp;
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mSeekBar.setEnabled(enabled);
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        super.onDependencyChanged(dependency, disableDependent);
        if (this.mSeekBar != null) {
            boolean z;
            SeekBar seekBar = this.mSeekBar;
            if (disableDependent) {
                z = false;
            } else {
                z = true;
            }
            seekBar.setEnabled(z);
        }
    }

    public void setDefaultProgress(int progress) {
        persistInt(progress);
        this.mCurrentValue = progress;
    }
}
