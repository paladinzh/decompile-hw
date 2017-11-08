package com.huawei.mms.util;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.SpannableStringBuilder;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.mms.ui.PreferenceUtils;
import com.google.android.gms.R;
import java.util.List;

public class SafetySmsParser {
    private static float LOCAL_SCALE = 0.8f;
    private static int SP_SIZE = 11;
    private static SafetySmsParser sInstance;
    private int imageSize = this.mContext.getResources().getDimensionPixelOffset(R.dimen.smiley_size);
    private Context mContext;

    private SafetySmsParser(Context context) {
        this.mContext = context;
    }

    public static SafetySmsParser getInstance() {
        if (sInstance == null) {
            init(MmsApp.getApplication().getApplicationContext());
        }
        return sInstance;
    }

    public static void init(Context context) {
        if (sInstance == null) {
            sInstance = new SafetySmsParser(context);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void appendSafetySmsSpan(SpannableStringBuilder source, List<TextSpan> spanList, Context context) {
        if (!(source == null || spanList == null || spanList.isEmpty() || !containsSafetySms(spanList))) {
            source.append("\n");
            source.append(createSafeSpan(context));
        }
    }

    private CharSequence createSafeSpan(Context context) {
        float scale = PreferenceUtils.getPreferenceFloat(this.mContext, "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        String safetyTag = "safety";
        String text = this.mContext.getResources().getString(R.string.safety_sms_notice_update);
        Drawable drawable = this.mContext.getResources().getDrawable(R.drawable.ic_mms_safe_12dp);
        SpannableStringBuilder builder = new SpannableStringBuilder(safetyTag + " " + text);
        int showSize = (int) (((((float) this.imageSize) * this.mContext.getResources().getConfiguration().fontScale) * scale) * LOCAL_SCALE);
        drawable.setBounds(0, 0, showSize, showSize);
        builder.setSpan(new VerticalImageSpan(drawable), 0, safetyTag.length(), 33);
        builder.setSpan(new AbsoluteSizeSpan((int) (((((float) SP_SIZE) * this.mContext.getResources().getDisplayMetrics().scaledDensity) + 0.5f) * scale)), safetyTag.length(), builder.length(), 33);
        builder.setSpan(new ForegroundColorSpan(-7829368), safetyTag.length(), builder.length(), 33);
        return builder;
    }

    private boolean containsSafetySms(List<TextSpan> spanList) {
        if (spanList == null) {
            return false;
        }
        for (TextSpan span : spanList) {
            if (span != null && span.getSpanType() == 5) {
                return true;
            }
        }
        return false;
    }

    public void appendRiskSpan(SpannableStringBuilder source, List<TextSpan> spanList) {
        if (source != null && spanList != null && !spanList.isEmpty()) {
            int warningType = containsWarningUrl(spanList);
            if (warningType == -6 || warningType == -7) {
                String warningStr;
                if (warningType == -6) {
                    warningStr = this.mContext.getResources().getString(R.string.risk_url_notice);
                } else {
                    warningStr = this.mContext.getResources().getString(R.string.unoffcial_url_notice);
                }
                if (!source.toString().contains(warningStr)) {
                    source.append("\n");
                    source.append(createRiskSpan(warningType));
                }
            }
        }
    }

    private CharSequence createRiskSpan(int type) {
        String text;
        float scale = PreferenceUtils.getPreferenceFloat(this.mContext, "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        String riskTag = "risk";
        if (type == -6) {
            text = this.mContext.getResources().getString(R.string.risk_url_notice);
        } else {
            text = this.mContext.getResources().getString(R.string.unoffcial_url_notice);
        }
        Drawable drawable = this.mContext.getResources().getDrawable(R.drawable.ic_public_error);
        SpannableStringBuilder builder = new SpannableStringBuilder(riskTag + text);
        int showSize = (int) (((((float) this.imageSize) * this.mContext.getResources().getConfiguration().fontScale) * scale) * LOCAL_SCALE);
        drawable.setBounds(0, 0, showSize, showSize);
        builder.setSpan(new VerticalImageSpan(drawable), 0, riskTag.length(), 33);
        builder.setSpan(new AbsoluteSizeSpan((int) (((((float) SP_SIZE) * this.mContext.getResources().getDisplayMetrics().scaledDensity) + 0.5f) * scale)), riskTag.length(), builder.length(), 33);
        builder.setSpan(new ForegroundColorSpan(-7829368), riskTag.length(), builder.length(), 33);
        return builder;
    }

    private int containsWarningUrl(List<TextSpan> spanList) {
        if (spanList == null) {
            return -1;
        }
        for (TextSpan span : spanList) {
            if (span != null && span.getSpanType() == -6) {
                return -6;
            }
            if (span != null && span.getSpanType() == -7) {
                return -7;
            }
        }
        return -1;
    }
}
