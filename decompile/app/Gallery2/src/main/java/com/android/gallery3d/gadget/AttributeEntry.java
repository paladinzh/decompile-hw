package com.android.gallery3d.gadget;

import android.text.TextUtils;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class AttributeEntry {
    private static final String PERCENT = "%";
    private int mFlag;
    private double mHeight;
    private String mId;
    private double mMarginBottom;
    private double mMarginLeft;
    private double mWidth;

    public String toString() {
        return "mMarginLeft = " + this.mMarginLeft + ", mMarginBottom = " + this.mMarginBottom + ", mId =" + this.mId;
    }

    public double getMarginLeft() {
        return this.mMarginLeft;
    }

    public double getMarginBottom() {
        return this.mMarginBottom;
    }

    public String getId() {
        return this.mId;
    }

    public void setId(String id) {
        this.mId = id;
    }

    public void setMarginLeft(String paddingLeft) {
        if (!TextUtils.isEmpty(paddingLeft)) {
            this.mMarginLeft = accuracyValue(paddingLeft);
        }
    }

    public void setMarginBottom(String paddingBottom) {
        if (!TextUtils.isEmpty(paddingBottom)) {
            this.mMarginBottom = accuracyValue(paddingBottom);
        }
    }

    public int getmFlag() {
        return this.mFlag;
    }

    public void setmFlag(String flag) {
        if (TextUtils.isEmpty(flag)) {
            this.mFlag = 1;
            return;
        }
        try {
            this.mFlag = Integer.parseInt(flag);
        } catch (Exception e) {
            this.mFlag = 1;
        }
    }

    public double getmWidth() {
        return this.mWidth;
    }

    public void setmWidth(String width) {
        if (!TextUtils.isEmpty(width)) {
            this.mWidth = accuracyValue(width);
        }
    }

    public double getmHeight() {
        return this.mHeight;
    }

    public void setmHeight(String height) {
        if (!TextUtils.isEmpty(height)) {
            this.mHeight = accuracyValue(height);
        }
    }

    public double accuracyValue(String data) {
        double value = Double.parseDouble(filterPercent(data));
        return Double.parseDouble(new DecimalFormat("0.####", DecimalFormatSymbols.getInstance(Locale.US)).format(value / 100.0d));
    }

    public String filterPercent(String value) {
        if (value.contains(PERCENT)) {
            return value.substring(0, value.indexOf(PERCENT));
        }
        return value;
    }
}
