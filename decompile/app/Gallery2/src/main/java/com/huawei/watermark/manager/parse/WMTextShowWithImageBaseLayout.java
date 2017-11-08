package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import com.huawei.watermark.ui.WMEditor;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import com.huawei.watermark.wmutil.WMResourceUtil;
import com.huawei.watermark.wmutil.WMStringUtil;
import com.huawei.watermark.wmutil.WMUIUtil;
import java.util.HashMap;
import org.xmlpull.v1.XmlPullParser;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class WMTextShowWithImageBaseLayout extends WMElement {
    protected HashMap<String, String> iconkeyvaluemap = new HashMap();
    protected View mBaseview;
    protected int mLatitude = 91;
    protected int mLongitude = SmsCheckResult.ESCT_181;
    protected WaterMark mWaterMark;
    protected int marginw;
    protected int singlenumw;
    protected String text;

    protected abstract void refreshNumValue();

    public WMTextShowWithImageBaseLayout(XmlPullParser parser) {
        super(parser);
        this.text = getStringByAttributeName(parser, WMEditor.TYPETEXT);
        this.singlenumw = getIntByAttributeName(parser, "singlenumw", -2);
        this.marginw = getIntByAttributeName(parser, "marginw", 0);
        String iconkeyvalue = getStringByAttributeName(parser, "iconkeyvalue");
        this.iconkeyvaluemap.clear();
        if (!WMStringUtil.isEmptyString(iconkeyvalue)) {
            Object[] level1 = iconkeyvalue.split(";");
            if (!WMCollectionUtil.isEmptyCollection(level1)) {
                for (String split : level1) {
                    Object[] level2 = split.split(",");
                    if (!WMCollectionUtil.isEmptyCollection(level2) && level2.length == 2) {
                        this.iconkeyvaluemap.put(level2[0], level2[1]);
                    }
                }
            }
        }
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        this.mOri = ori;
        this.mWaterMark = wm;
        View view = LayoutInflater.from(context).inflate(Integer.valueOf(WMResourceUtil.getLayoutId(context, "wm_jar_numimage_common_style")).intValue(), null);
        view.setId(wm.generateId(this.id));
        view.setLayoutParams(generateLp(context, this.mWaterMark, parentLayoutMode));
        this.mBaseview = view;
        refreshNumValue();
        showContent();
        return view;
    }

    protected void showContent() {
        WMUIUtil.showNumAndIcon((LinearLayout) this.mBaseview, this.text, this.singlenumw, this.h, this.iconkeyvaluemap, this.mWaterMark.getPath(), this.mWaterMark.getScale(), this.marginw);
    }

    public void resetWaterMarkLayoutParams() {
        if (this.mWaterMark != null) {
            this.mWaterMark.resetWaterMarkLayoutParams();
        }
    }
}
