package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.view.HsmSeekBarPreference;

public class AbsSeekBarPrefer extends HsmSeekBarPreference implements ICardPrefer {
    protected CardItem mCard;
    private OnPreferenceChangeListener mListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return AbsSeekBarPrefer.this.onValueChanged(newValue);
        }
    };
    private PreferenceHelper preferenceHelper = new PreferenceHelper(this);

    public AbsSeekBarPrefer(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }

    public AbsSeekBarPrefer(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initValue();
    }

    protected void initValue() {
        setPersistent(false);
        super.setOnPreferenceChangeListener(this.mListener);
    }

    public void setCard(CardItem card) {
        this.mCard = card;
    }

    public void refreshPreferShow() {
    }

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
    }

    public void postRunnableUI(Runnable r) {
        this.preferenceHelper.postRunnableUI(r);
    }

    public void postRunnableAsync(Runnable r) {
        this.preferenceHelper.postRunnableAsync(r);
    }

    public void setValueChangedListener(IValueChangedListener l) {
        this.preferenceHelper.setValueChangeListener(l);
    }

    protected void callValueChanged(Object newValue) {
        this.preferenceHelper.callValueChanged(newValue);
    }

    protected boolean onValueChanged(Object newValue) {
        return false;
    }
}
