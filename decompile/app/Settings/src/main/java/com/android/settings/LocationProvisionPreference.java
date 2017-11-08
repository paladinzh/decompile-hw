package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.android.settingslib.RestrictedSwitchPreference;

public class LocationProvisionPreference extends RestrictedSwitchPreference {
    private Context mContext;

    public class NoUnderLineClickSpan extends ClickableSpan {
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(LocationProvisionPreference.this.mContext.getResources().getColor(2131427515));
            ds.setUnderlineText(false);
        }

        public void onClick(View widget) {
            if (widget instanceof TextView) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.LocationProvisionActivity");
                LocationProvisionPreference.this.mContext.startActivity(intent);
            }
        }
    }

    public LocationProvisionPreference(Context context) {
        this(context, null);
    }

    public LocationProvisionPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
        setLayoutResource(2130968857);
    }

    private void updateView(PreferenceViewHolder view) {
        TextView summary = (TextView) view.findViewById(2131886387);
        if (SettingsExtUtils.isGlobalVersion()) {
            summary.setText(2131625499);
            return;
        }
        String linkStr = this.mContext.getResources().getString(2131628831);
        String tmpInfo = this.mContext.getResources().getString(2131628830);
        String agreeInfo = String.format(tmpInfo, new Object[]{linkStr}) + " ";
        int start = agreeInfo.lastIndexOf(linkStr);
        setClickableSpanForTextView(summary, new NoUnderLineClickSpan(), agreeInfo, start, start + linkStr.length());
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        super.onBindViewHolder(view);
        updateView(view);
    }

    private void setClickableSpanForTextView(TextView tv, ClickableSpan clickableSpan, String text, int start, int end) {
        if (start < 0 || start >= end || end >= text.length()) {
            tv.setText(text);
            return;
        }
        SpannableString sp = new SpannableString(text);
        sp.setSpan(clickableSpan, start, end, 33);
        tv.setText(sp);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setFocusable(false);
        tv.setClickable(false);
        tv.setLongClickable(false);
    }
}
