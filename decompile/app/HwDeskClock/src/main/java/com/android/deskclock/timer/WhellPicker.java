package com.android.deskclock.timer;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import com.android.deskclock.R;
import com.android.deskclock.timer.AdvancedNumberPicker.OnColorChangeListener;
import com.android.deskclock.timer.AdvancedNumberPicker.OnValueChangeListener;
import com.android.util.ClockReporter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class WhellPicker extends LinearLayout implements OnValueChangeListener {
    static OnColorChangeListener onColorChangeListener = new OnColorChangeListener() {
        public void onColorChange(AdvancedNumberPicker picker) {
        }
    };
    private Context mContext;
    private boolean mHasTouch;
    private int mHour;
    private AdvancedNumberPicker mHourPicker;
    private int mMinute;
    private AdvancedNumberPicker mMinutePicker;
    private List<AdvancedNumberPicker> mNumberPickerList;
    private int mSecond;
    private AdvancedNumberPicker mSecondPicker;
    private OnWhellPickerValueChangedListener onWhellPickerValueChangedListener;

    public interface OnWhellPickerValueChangedListener {
        void onHourChanged(int i);

        void onMinuteChanged(int i);

        void onSecondChanged(int i);
    }

    public WhellPicker(Context context, int outHour, int outMinute, int outSecond) {
        this(context, null);
        this.mHour = outHour;
        this.mMinute = outMinute;
        this.mSecond = outSecond;
    }

    public WhellPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WhellPicker(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mNumberPickerList = new ArrayList();
        this.mContext = context;
        initView();
    }

    private void initView() {
        ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.timepick, this, true);
        this.mHourPicker = (AdvancedNumberPicker) findViewById(R.id.hourpicker);
        this.mHourPicker.setOnValueChangedListener(this);
        this.mMinutePicker = (AdvancedNumberPicker) findViewById(R.id.minutepicker);
        this.mMinutePicker.setOnValueChangedListener(this);
        this.mSecondPicker = (AdvancedNumberPicker) findViewById(R.id.secondpicker);
        this.mSecondPicker.setOnValueChangedListener(this);
        this.mHourPicker.setmOnColorChangeListener(onColorChangeListener);
        this.mHourPicker.setMaxValue(99);
        this.mHourPicker.setMinValue(0);
        this.mMinutePicker.setmOnColorChangeListener(onColorChangeListener);
        this.mMinutePicker.setMaxValue(59);
        this.mMinutePicker.setMinValue(0);
        this.mSecondPicker.setmOnColorChangeListener(onColorChangeListener);
        this.mSecondPicker.setMaxValue(59);
        this.mSecondPicker.setMinValue(0);
        this.mHourPicker.setValue(this.mHour);
        this.mMinutePicker.setValue(this.mMinute);
        this.mSecondPicker.setValue(this.mSecond);
        this.mNumberPickerList.add(this.mHourPicker);
        this.mNumberPickerList.add(this.mMinutePicker);
        this.mNumberPickerList.add(this.mSecondPicker);
    }

    public int getTimeSecond() {
        return (((this.mHour * 60) * 60) + (this.mMinute * 60)) + this.mSecond;
    }

    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        this.mHourPicker.setValue(this.mHour);
        this.mMinutePicker.setValue(this.mMinute);
        this.mSecondPicker.setValue(this.mSecond);
    }

    public void onValueChange(AdvancedNumberPicker picker, int oldVal, int newVal) {
        if (picker != null) {
            if (!this.mHasTouch) {
                ClockReporter.reportEventMessage(this.mContext, 76, "");
                this.mHasTouch = true;
            }
            if (picker == this.mHourPicker) {
                this.mHour = newVal;
                if (this.onWhellPickerValueChangedListener != null) {
                    this.onWhellPickerValueChangedListener.onHourChanged(newVal);
                }
            } else if (picker == this.mMinutePicker) {
                this.mMinute = newVal;
                if (this.onWhellPickerValueChangedListener != null) {
                    this.onWhellPickerValueChangedListener.onMinuteChanged(newVal);
                }
            } else {
                this.mSecond = newVal;
                if (this.onWhellPickerValueChangedListener != null) {
                    this.onWhellPickerValueChangedListener.onSecondChanged(newVal);
                }
            }
        }
    }

    public int getHour() {
        return this.mHourPicker.getValue();
    }

    public int getMinute() {
        return this.mMinutePicker.getValue();
    }

    public int getSecond() {
        return this.mSecondPicker.getValue();
    }

    public boolean resolveLayoutDirection() {
        String languageString = Locale.getDefault().toString();
        if (languageString.contains("ar") || languageString.contains("fa") || Locale.getDefault().getLanguage().contains("ur")) {
            return false;
        }
        return super.resolveLayoutDirection();
    }

    public void setOnWhellPickerValueChangedListener(OnWhellPickerValueChangedListener listener) {
        this.onWhellPickerValueChangedListener = listener;
    }

    public void resetTimeNumber(int hour, int minute, int second) {
        this.mHourPicker.changeCurrent(hour);
        this.mMinutePicker.changeCurrent(minute);
        this.mSecondPicker.changeCurrent(second);
    }
}
