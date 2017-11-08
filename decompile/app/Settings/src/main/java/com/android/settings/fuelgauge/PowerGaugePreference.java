package com.android.settings.fuelgauge;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v7.preference.PreferenceViewHolder;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.settings.TintablePreference;
import com.android.settingslib.Utils;

public class PowerGaugePreference extends TintablePreference {
    private CharSequence mContentDescription;
    private final int mIconSize;
    private BatteryEntry mInfo;
    private int mPercentOfTotal;
    private CharSequence mProgress;
    private int mProgressValue;

    public PowerGaugePreference(Context context, Drawable icon, CharSequence contentDescription, BatteryEntry info) {
        super(context, null);
        if (icon == null) {
            icon = new ColorDrawable(0);
        }
        setIcon(icon);
        setWidgetLayoutResource(2130969004);
        this.mInfo = info;
        this.mContentDescription = contentDescription;
        this.mIconSize = context.getResources().getDimensionPixelSize(2131558591);
    }

    public void setContentDescription(String name) {
        this.mContentDescription = name;
        notifyChanged();
    }

    public void setPercent(double percentOfMax, double percentOfTotal) {
        this.mProgressValue = (int) Math.ceil(percentOfTotal);
        this.mPercentOfTotal = (int) Math.ceil(percentOfTotal);
        this.mProgress = Utils.formatPercentage((int) (0.5d + percentOfTotal));
        notifyChanged();
    }

    BatteryEntry getInfo() {
        return this.mInfo;
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        ((ImageView) view.findViewById(16908294)).setLayoutParams(new LayoutParams(this.mIconSize, this.mIconSize));
        ((TextView) view.findViewById(2131886970)).setText(this.mProgress);
        if (this.mContentDescription != null) {
            ((TextView) view.findViewById(16908310)).setContentDescription(this.mContentDescription);
        }
    }
}
