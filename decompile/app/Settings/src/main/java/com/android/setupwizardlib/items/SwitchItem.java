package com.android.setupwizardlib.items;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CompoundButton;
import com.android.setupwizardlib.R$id;
import com.android.setupwizardlib.R$layout;
import com.android.setupwizardlib.R$styleable;

public class SwitchItem extends Item implements android.widget.CompoundButton.OnCheckedChangeListener {
    private boolean mChecked = false;
    private OnCheckedChangeListener mListener;

    public interface OnCheckedChangeListener {
        void onCheckedChange(SwitchItem switchItem, boolean z);
    }

    public SwitchItem(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.SuwSwitchItem);
        this.mChecked = a.getBoolean(R$styleable.SuwSwitchItem_android_checked, false);
        a.recycle();
    }

    protected int getDefaultLayoutResource() {
        return R$layout.suw_items_switch;
    }

    public void onBindView(View view) {
        super.onBindView(view);
        SwitchCompat switchView = (SwitchCompat) view.findViewById(R$id.suw_items_switch);
        switchView.setChecked(this.mChecked);
        switchView.setOnCheckedChangeListener(this);
        switchView.setEnabled(isEnabled());
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (this.mListener != null) {
            this.mListener.onCheckedChange(this, isChecked);
        }
    }
}
