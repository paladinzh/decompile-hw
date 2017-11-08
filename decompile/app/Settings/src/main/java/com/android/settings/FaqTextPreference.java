package com.android.settings;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.TextView;

public class FaqTextPreference extends Preference {
    private Context mContext;
    private int mFaqDeviceType;
    private String mKey;
    private int mResId;

    public FaqTextPreference(Context context, int resId, int faqDeviceType) {
        super(context, null, 0);
        this.mResId = resId;
        this.mContext = context;
        this.mFaqDeviceType = faqDeviceType;
        setLayoutResource(this.mResId);
        setSelectable(false);
        setOnPreferenceClickListener(null);
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        View view = holder.itemView;
        if (2130968914 == this.mResId) {
            buildKnowMoreView((TextView) view.findViewById(2131886869), 2131628174);
        } else if (2130968915 == this.mResId) {
            buildKnowMoreView((TextView) view.findViewById(2131886870), 2131628043);
        } else if (2130968997 == this.mResId) {
            buildKnowMoreView((TextView) view.findViewById(2131886869), 2131628174);
        }
    }

    public void setKey(String key) {
        this.mKey = key;
    }

    public String getKey() {
        return this.mKey;
    }

    private void buildKnowMoreView(TextView knowMoreView, int resId) {
        if (knowMoreView != null) {
            String linkStr = this.mContext.getString(2131628044);
            String tmpInfo = this.mContext.getString(resId);
            String knowMoreInfo = String.format(tmpInfo, new Object[]{linkStr}) + " ";
            int start = knowMoreInfo.lastIndexOf(linkStr);
            int end = start + linkStr.length();
            ClickableSpan clicksss = new KnowMoreClickableSpan(2131427515, this.mContext, this.mFaqDeviceType);
            if (start < 0 || start >= end || end >= knowMoreInfo.length()) {
                knowMoreView.setText(knowMoreInfo);
                return;
            }
            SpannableString sp = new SpannableString(knowMoreInfo);
            sp.setSpan(clicksss, start, end, 33);
            knowMoreView.setText(sp);
            knowMoreView.setMovementMethod(LinkMovementMethod.getInstance());
            knowMoreView.setFocusable(false);
            knowMoreView.setClickable(false);
            knowMoreView.setLongClickable(false);
        }
    }
}
