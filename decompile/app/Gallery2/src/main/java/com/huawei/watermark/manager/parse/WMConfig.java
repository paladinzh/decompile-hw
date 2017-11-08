package com.huawei.watermark.manager.parse;

import android.content.Context;
import android.view.View;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.wmutil.WMBaseUtil;
import org.xmlpull.v1.XmlPullParser;

public class WMConfig extends WMElement {
    public static final int FACTOR = 5;
    public static final String SUPPORTALL = "all";
    public static final String SUPPORTEN = "en";
    public static final String SUPPORTZH = "zh";
    private String category;
    private String landLayoutGravity;
    private int[] landMarginBottom;
    private int[] landMarginLeft;
    private int[] landMarginRgiht;
    private int[] landMarginTop;
    private String name;
    private String portLayoutGravity;
    private int[] portMarginBottom;
    private int[] portMarginLeft;
    private int[] portMarginRgiht;
    private int[] portMarginTop;
    private String preview;
    private int tipHeight;
    private int tipWidth;

    public WMConfig(XmlPullParser parser) {
        super(parser);
        this.name = getStringByAttributeName(parser, "name");
        this.category = getStringByAttributeName(parser, "category");
        this.preview = getStringByAttributeName(parser, "preview");
        this.portLayoutGravity = getStringByAttributeName(parser, "port_layout_gravity");
        this.portMarginLeft = getMarginIntFromString(getStringByAttributeName(parser, "port_margin_left"));
        this.portMarginRgiht = getMarginIntFromString(getStringByAttributeName(parser, "port_margin_right"));
        this.portMarginBottom = getMarginIntFromString(getStringByAttributeName(parser, "port_margin_bottom"));
        this.portMarginTop = getMarginIntFromString(getStringByAttributeName(parser, "port_margin_top"));
        this.landLayoutGravity = getStringByAttributeName(parser, "land_layout_gravity");
        this.landMarginLeft = getMarginIntFromString(getStringByAttributeName(parser, "land_margin_left"));
        this.landMarginRgiht = getMarginIntFromString(getStringByAttributeName(parser, "land_margin_right"));
        this.landMarginBottom = getMarginIntFromString(getStringByAttributeName(parser, "land_margin_bottom"));
        this.landMarginTop = getMarginIntFromString(getStringByAttributeName(parser, "land_margin_top"));
        this.tipWidth = getIntByAttributeName(parser, "tip_width");
        this.tipHeight = getIntByAttributeName(parser, "tip_height");
    }

    public View toView(Context context, WaterMark wm, String parentLayoutMode, int ori) {
        return null;
    }

    public String getPortLayoutGravity() {
        return this.portLayoutGravity;
    }

    public String getName() {
        return this.name;
    }

    public String getPreview() {
        return this.preview;
    }

    public String getLandLayoutGravity() {
        return this.landLayoutGravity;
    }

    public String getCategory() {
        return this.category;
    }

    public int getLandMarginTop(int index) {
        return this.landMarginTop[index];
    }

    public int getLandMarginLeft(int index) {
        return this.landMarginLeft[index];
    }

    public int getLandMarginRgiht(int index) {
        return this.landMarginRgiht[index];
    }

    public int getLandMarginBottom(int index) {
        return this.landMarginBottom[index];
    }

    public int getPortMarginTop(int index) {
        return this.portMarginTop[index];
    }

    public int getPortMarginBottom(int index) {
        return this.portMarginBottom[index];
    }

    public int getPortMarginRgiht(int index) {
        return this.portMarginRgiht[index];
    }

    public int getPortMarginLeft(int index) {
        return this.portMarginLeft[index];
    }

    public int getViewWidth() {
        return this.w;
    }

    public int getViewHeight() {
        return this.h;
    }

    public int getTipWidth(Context context) {
        return WMBaseUtil.dpToPixel((float) (((this.tipWidth + getPortMarginLeft(this.mCameraSizeType)) + getLandMarginRgiht(this.mCameraSizeType)) + 5), context);
    }

    public int getTipHeight(Context context) {
        return WMBaseUtil.dpToPixel((float) (((this.tipHeight + getPortMarginTop(this.mCameraSizeType)) + getPortMarginBottom(this.mCameraSizeType)) + 5), context);
    }

    private int[] getMarginIntFromString(String margin) {
        int[] res = new int[3];
        if (margin == null || margin.length() == 0) {
            return res;
        }
        String[] tempmarginstr = margin.split(",");
        int index = 0;
        int i = 0;
        while (true) {
            if (i >= (tempmarginstr.length < 3 ? tempmarginstr.length : 3)) {
                break;
            }
            index = i;
            try {
                res[i] = Integer.parseInt(tempmarginstr[i]);
            } catch (Exception e) {
                WMLog.d("WMConfig", "Integer.parseInt e margin=" + margin);
            }
            i++;
        }
        if (index < 2) {
            for (i = index + 1; i < res.length; i++) {
                res[i] = res[index];
            }
        }
        return res;
    }
}
