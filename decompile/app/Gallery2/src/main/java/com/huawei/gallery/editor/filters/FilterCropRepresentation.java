package com.huawei.gallery.editor.filters;

import android.graphics.RectF;
import com.android.gallery3d.R;
import com.huawei.watermark.manager.parse.WMElement;

public class FilterCropRepresentation extends FilterRepresentation {
    protected static final String[] BOUNDS = new String[]{"C0", "C1", "C2", "C3"};
    public static final RectF sNilRect = new RectF(0.0f, 0.0f, WMElement.CAMERASIZEVALUE1B1, WMElement.CAMERASIZEVALUE1B1);
    RectF mCrop;

    public FilterCropRepresentation(RectF crop) {
        super("CROP");
        this.mCrop = getNil();
        setSerializationName("CROP");
        setFilterClass(FilterCropRepresentation.class);
        setFilterType(4);
        setTextId(R.string.crop);
        setCrop(crop);
    }

    public FilterCropRepresentation(FilterCropRepresentation m) {
        this(m.mCrop);
        setName(m.getName());
        setReportMsg(m.getReportMsg());
    }

    public FilterCropRepresentation() {
        this(sNilRect);
    }

    public boolean equals(FilterRepresentation rep) {
        if (!(rep instanceof FilterCropRepresentation)) {
            return false;
        }
        FilterCropRepresentation crop = (FilterCropRepresentation) rep;
        if (this.mCrop.bottom == crop.mCrop.bottom && this.mCrop.left == crop.mCrop.left && this.mCrop.right == crop.mCrop.right && this.mCrop.top == crop.mCrop.top) {
            return true;
        }
        return false;
    }

    public void getCrop(RectF r) {
        r.set(this.mCrop);
    }

    public void setCrop(RectF crop) {
        if (crop == null) {
            throw new IllegalArgumentException("Argument to setCrop is null");
        }
        this.mCrop.set(crop);
    }

    public static void findNormalizedCrop(RectF crop, int bitmapWidth, int bitmapHeight) {
        crop.left /= (float) bitmapWidth;
        crop.top /= (float) bitmapHeight;
        crop.right /= (float) bitmapWidth;
        crop.bottom /= (float) bitmapHeight;
    }

    public void setReportMsg(String reportMsg) {
        this.mReportMsg = reportMsg;
    }

    public String getReportMsg() {
        return this.mReportMsg;
    }

    public static void findScaledCrop(RectF crop, int bitmapWidth, int bitmapHeight) {
        crop.left *= (float) bitmapWidth;
        crop.top *= (float) bitmapHeight;
        crop.right *= (float) bitmapWidth;
        crop.bottom *= (float) bitmapHeight;
    }

    public FilterRepresentation copy() {
        return new FilterCropRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterCropRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterCropRepresentation) {
            setCrop(((FilterCropRepresentation) a).mCrop);
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return this.mCrop.equals(sNilRect);
    }

    public static RectF getNil() {
        return new RectF(sNilRect);
    }
}
