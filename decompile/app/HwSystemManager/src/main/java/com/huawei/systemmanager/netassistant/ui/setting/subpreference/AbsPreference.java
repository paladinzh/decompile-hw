package com.huawei.systemmanager.netassistant.ui.setting.subpreference;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;

public class AbsPreference extends Preference implements ICardPrefer {
    protected CardItem mCard;
    private PreferenceHelper preferenceHelper = new PreferenceHelper(this);

    public AbsPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        initValue();
    }

    public AbsPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initValue();
    }

    protected void initValue() {
        this.preferenceHelper.initLayout();
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

    public void setSummary2(int resId) {
        setSummary2(getContext().getString(resId));
    }

    public void setSummary2(String summary2) {
        if (this.preferenceHelper.setSummary2(summary2)) {
            notifyChanged();
        }
    }

    public void postSetSummary(String str) {
        this.preferenceHelper.postSetSummary(str);
    }

    public void postRunnableAsync(Runnable r) {
        this.preferenceHelper.postRunnableAsync(r);
    }

    public void setValueChangedListener(IValueChangedListener l) {
        this.preferenceHelper.setValueChangeListener(l);
    }

    public void postRunnableUI(Runnable r) {
        this.preferenceHelper.postRunnableUI(r);
    }

    protected void callValueChanged(Object newValue) {
        this.preferenceHelper.callValueChanged(newValue);
    }
}
