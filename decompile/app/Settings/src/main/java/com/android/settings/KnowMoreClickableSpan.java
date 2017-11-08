package com.android.settings;

import android.content.Context;
import android.content.Intent;
import android.text.TextPaint;
import android.text.style.ClickableSpan;
import android.view.View;

public class KnowMoreClickableSpan extends ClickableSpan {
    private int mColorRid;
    private Context mContext;
    private int mFaqDeviceType;

    public KnowMoreClickableSpan(int color, Context context, int faqDeviceType) {
        this.mColorRid = color;
        this.mContext = context;
        this.mFaqDeviceType = faqDeviceType;
    }

    public void updateDrawState(TextPaint ds) {
        super.updateDrawState(ds);
        ds.setColor(this.mContext.getResources().getColor(this.mColorRid));
        ds.setUnderlineText(false);
    }

    public void onClick(View widget) {
        Intent intent = new Intent();
        intent.setAction("android.intent.action.FAQ_HELP");
        intent.putExtra("faq_device_type", this.mFaqDeviceType);
        this.mContext.startActivity(intent);
    }
}
