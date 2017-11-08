package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;

public class AbsListPreference extends ListPreference implements ICardPrefer {
    protected CardItem mCard;
    private OnPreferenceChangeListener mListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            return AbsListPreference.this.onValueChanged(newValue);
        }
    };
    private PreferenceHelper preferenceHelper = new PreferenceHelper(this);

    public AbsListPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        initValue();
    }

    public AbsListPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValue();
    }

    public AbsListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }

    protected void initValue() {
        this.preferenceHelper.initLayout();
        super.setOnPreferenceChangeListener(this.mListener);
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        this.preferenceHelper.onBindView(view);
    }

    public void setCard(CardItem card) {
        this.mCard = card;
    }

    public void refreshPreferShow() {
    }

    public void setSummary2(String summary2) {
        if (this.preferenceHelper.setSummary2(summary2)) {
            notifyChanged();
        }
    }

    public void setSummary2(int resId) {
        setSummary2(getContext().getString(resId));
    }

    public void postSetSummary(String str) {
        this.preferenceHelper.postSetSummary(str);
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

    public void setOnPreferenceChangeListener(OnPreferenceChangeListener onPreferenceChangeListener) {
    }

    protected boolean onValueChanged(Object newValue) {
        return false;
    }
}
