package com.huawei.gallery.editor.filters;

import android.graphics.Bitmap;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.imageshow.GeometryMathUtils.GeometryHolder;

public class FilterWaterMarkRepresentation extends FilterRepresentation {
    private static final String[] TAGS = new String[]{"WM_LEFT", "WM_TOP", "WM_WIDTH", "WM_HEIGHT", "WM_NAME", "WM_C0", "WM_C1", "WM_C2", "WM_C3", "WM_MIRROR", "WM_ROTATE", "WM_BITMAP"};
    private GeometryHolder mGeometryHolder;
    private WaterMarkHolder mWaterMarkHolder;

    public static class WaterMarkHolder {
        public float height = GroundOverlayOptions.NO_DIMENSION;
        public float left = GroundOverlayOptions.NO_DIMENSION;
        public String name;
        public float top = GroundOverlayOptions.NO_DIMENSION;
        public Bitmap waterMarkBitmap;
        public float width = GroundOverlayOptions.NO_DIMENSION;

        public void set(WaterMarkHolder holder) {
            this.left = holder.left;
            this.top = holder.top;
            this.width = holder.width;
            this.height = holder.height;
            this.waterMarkBitmap = holder.waterMarkBitmap;
            this.name = holder.name;
        }

        public boolean isNil() {
            if (this.left < 0.0f || this.top < 0.0f || this.width < 0.0f || this.height < 0.0f || this.waterMarkBitmap == null) {
                return true;
            }
            return false;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean equals(Object o) {
            if (!(o instanceof WaterMarkHolder)) {
                return false;
            }
            WaterMarkHolder holder = (WaterMarkHolder) o;
            if (this.name != null && holder.name != null && this.waterMarkBitmap != null && holder.waterMarkBitmap != null && this.left == holder.left && this.top == holder.top && this.width == holder.width && this.height == holder.height && this.name.equals(holder.name) && this.waterMarkBitmap == holder.waterMarkBitmap) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (this.name + this.left + this.top + this.width + this.height).hashCode();
        }
    }

    public FilterWaterMarkRepresentation() {
        super("WATERMARK");
        this.mWaterMarkHolder = new WaterMarkHolder();
        this.mGeometryHolder = new GeometryHolder();
        setSerializationName("WATERMARK");
        setFilterClass(ImageFilterWaterMark.class);
        setFilterType(7);
        setTextId(R.string.water_mark_attention_dialog_title_511);
    }

    public FilterWaterMarkRepresentation(FilterWaterMarkRepresentation m) {
        this();
        setName(m.getName());
        this.mWaterMarkHolder.set(m.mWaterMarkHolder);
        this.mGeometryHolder = new GeometryHolder();
        this.mGeometryHolder.set(m.mGeometryHolder);
    }

    public synchronized void setWaterMarkHolder(WaterMarkHolder holder) {
        if (holder != null) {
            this.mWaterMarkHolder.set(holder);
        }
    }

    public synchronized WaterMarkHolder getWaterMarkHolder() {
        WaterMarkHolder holder;
        holder = new WaterMarkHolder();
        holder.set(this.mWaterMarkHolder);
        return holder;
    }

    public boolean equals(FilterRepresentation representation) {
        if (!(representation instanceof FilterWaterMarkRepresentation)) {
            return false;
        }
        FilterWaterMarkRepresentation waterMarkRepresentation = (FilterWaterMarkRepresentation) representation;
        if (this.mWaterMarkHolder.equals(waterMarkRepresentation.mWaterMarkHolder) && this.mGeometryHolder.equals(waterMarkRepresentation.mGeometryHolder)) {
            return true;
        }
        return false;
    }

    public FilterRepresentation copy() {
        return new FilterWaterMarkRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterWaterMarkRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterWaterMarkRepresentation) {
            FilterWaterMarkRepresentation representation = (FilterWaterMarkRepresentation) a;
            this.mWaterMarkHolder.set(representation.mWaterMarkHolder);
            this.mGeometryHolder.set(representation.mGeometryHolder);
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return this.mWaterMarkHolder.isNil();
    }

    public void reset() {
        super.reset();
        if (this.mWaterMarkHolder.waterMarkBitmap != null) {
            this.mWaterMarkHolder.waterMarkBitmap.recycle();
        }
    }
}
