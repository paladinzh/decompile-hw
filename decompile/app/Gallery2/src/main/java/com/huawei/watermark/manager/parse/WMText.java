package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.graphics.Color;
import android.text.TextUtils.TruncateAt;
import android.view.View;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.wmutil.WMBaseUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import com.huawei.watermark.wmutil.WMUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMText extends WMTextStyleElement {
    private String ellipsize;
    public int mEms;
    private WaterMark mWaterMark;
    private String orientation;
    private boolean singleline;
    protected String text;

    public WMText(XmlPullParser parser) {
        super(parser);
        this.text = getStringByAttributeName(parser, WMEditor.TYPETEXT);
        this.mEms = getIntByAttributeName(parser, "ems", 0);
        this.ellipsize = getStringByAttributeName(parser, "ellipsize");
        this.singleline = getBooleanByAttributeName(parser, "singleline");
        this.orientation = getStringByAttributeName(parser, "orientation");
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        this.mWaterMark = wm;
        this.mOri = ori;
        TextView tv = new TextView(context);
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        if (WMBaseUtil.supportJELLYBEANMR1()) {
            tv.setLayoutDirection(0);
        }
        tv.setSingleLine(this.singleline);
        if (!WMStringUtil.isEmptyString(this.ellipsize)) {
            try {
                tv.setEllipsize(TruncateAt.valueOf(this.ellipsize));
            } catch (IllegalArgumentException e) {
            }
        }
        tv.setId(wm.generateId(this.id));
        tv.setLayoutParams(generateLp(context, wm, parentLayoutMode));
        tv.setTextAppearance(context, WMResourceUtil.getStyleId(context, "wm_jar_TextStyle"));
        tv.setTextColor(Color.parseColor(this.fontcolor));
        tv.setShadowLayer(this.shadowr, this.shadowx, this.shadowy, Color.parseColor(this.shadowcolor));
        tv.setGravity(getGravity());
        WMLog.d("WMText", "fn=" + this.fontname);
        tv.setTextSize(1, ((float) this.size) * wm.getScale());
        tv.setIncludeFontPadding(false);
        if (!WMStringUtil.isEmptyString(this.bold)) {
            tv.getPaint().setFakeBoldText(true);
        }
        decoratorText(tv);
        setPadding(context, wm, tv);
        if (this.max_width > 0) {
            tv.setMaxWidth(WMBaseUtil.dpToPixel((float) this.max_width, context));
        }
        if (this.max_height > 0) {
            tv.setMaxHeight(WMBaseUtil.dpToPixel((float) this.max_height, context));
        }
        return tv;
    }

    public void decoratorText(TextView tv) {
        this.text = WMUIUtil.getDecoratorText(tv.getContext(), this.text);
        WMUtil.setLKTypeFace(tv.getContext(), tv);
        tv.setText(processStringToShow(this.text, tv.getContext()));
    }

    public String getText() {
        return this.text;
    }

    public void resetWaterMarkLayoutParams() {
        if (this.mWaterMark != null) {
            this.mWaterMark.resetWaterMarkLayoutParams();
        }
    }

    public boolean getCanShowWhenLocked() {
        if (this.mWaterMark != null) {
            return this.mWaterMark.getCanShowWhenLocked();
        }
        return false;
    }

    public String matchEMS(String text, int ems) {
        if (ems <= 0 || WMStringUtil.isEmptyString(text)) {
            return text;
        }
        int m = text.length();
        StringBuffer sb = new StringBuffer();
        int i = 0;
        while (i < m) {
            int startIndex = i;
            int endIndex = i + ems;
            if (endIndex > m) {
                endIndex = m;
            }
            sb.append(text.toString().subSequence(startIndex, endIndex) + "\n");
            i += ems;
        }
        return sb.toString();
    }

    public String matchOrientation(String text, String orientation, Context context) {
        if (!"port".equals(orientation) || WMStringUtil.isEmptyString(text) || context == null) {
            return text;
        }
        return text.replaceAll(context.getResources().getString(R.string.water_mark_left_big_brackets), context.getResources().getString(R.string.water_mark_left_brackets_port_value)).replaceAll(context.getResources().getString(R.string.water_mark_left_small_brackets), context.getResources().getString(R.string.water_mark_left_brackets_port_value)).replaceAll(context.getResources().getString(R.string.water_mark_right_big_brackets), context.getResources().getString(R.string.water_mark_right_brackets_port_value)).replaceAll(context.getResources().getString(R.string.water_mark_right_small_brackets), context.getResources().getString(R.string.water_mark_right_brackets_port_value));
    }

    public String processStringToShow(String text, Context context) {
        return matchEMS(matchOrientation(text, this.orientation, context), this.mEms);
    }
}
