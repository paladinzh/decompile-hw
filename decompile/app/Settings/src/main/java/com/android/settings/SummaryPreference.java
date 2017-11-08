package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.widget.TextView;
import com.android.settings.applications.LinearColorBar;

public class SummaryPreference extends Preference {
    private String mAmount;
    private String mEndLabel;
    private int mLeft;
    private float mLeftRatio;
    private int mMiddle;
    private float mMiddleRatio;
    private int mRight;
    private float mRightRatio;
    private String mStartLabel;
    private String mUnits;

    public SummaryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130969105);
        this.mLeft = context.getColor(2131427475);
        this.mRight = context.getColor(2131427476);
    }

    public void setAmount(String amount) {
        this.mAmount = amount;
        if (this.mAmount != null && this.mUnits != null) {
            setTitle(TextUtils.expandTemplate(getContext().getText(2131625307), new CharSequence[]{this.mAmount, this.mUnits}));
        }
    }

    public void setUnits(String units) {
        this.mUnits = units;
        if (this.mAmount != null && this.mUnits != null) {
            setTitle(TextUtils.expandTemplate(getContext().getText(2131625307), new CharSequence[]{this.mAmount, this.mUnits}));
        }
    }

    public void setLabels(String start, String end) {
        this.mStartLabel = start;
        this.mEndLabel = end;
        notifyChanged();
    }

    public void setRatios(float left, float middle, float right) {
        this.mLeftRatio = left;
        this.mMiddleRatio = middle;
        this.mRightRatio = right;
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        LinearColorBar colorBar = (LinearColorBar) holder.itemView.findViewById(2131887014);
        colorBar.setRatios(this.mLeftRatio, this.mMiddleRatio, this.mRightRatio);
        colorBar.setColors(this.mLeft, this.mMiddle, this.mRight);
        if (TextUtils.isEmpty(this.mStartLabel) && TextUtils.isEmpty(this.mEndLabel)) {
            holder.findViewById(2131887154).setVisibility(8);
            return;
        }
        holder.findViewById(2131887154).setVisibility(0);
        ((TextView) holder.findViewById(16908308)).setText(this.mStartLabel);
        ((TextView) holder.findViewById(16908309)).setText(this.mEndLabel);
    }
}
