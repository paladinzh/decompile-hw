package com.huawei.gallery.editor.filters;

import android.graphics.Rect;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.IllusionBar.STYLE;
import com.huawei.gallery.editor.ui.ShapeControl.Line;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class FilterIllusionRepresentation extends FilterRepresentation implements Line, com.huawei.gallery.editor.ui.ShapeControl.Circle {
    private Rect mBound = new Rect();
    private Band mCurrentBand = new Band();
    private Circle mCurrentCircle = new Circle();
    private boolean mNeedApply = true;
    private STYLE mStyle = STYLE.UNKONW;
    private int mValue = 50;

    public static class Band {
        private float xPos1 = GroundOverlayOptions.NO_DIMENSION;
        private float xPos2 = GroundOverlayOptions.NO_DIMENSION;
        private float yPos1 = GroundOverlayOptions.NO_DIMENSION;
        private float yPos2 = GroundOverlayOptions.NO_DIMENSION;

        public void set(Band band) {
            this.xPos1 = band.xPos1;
            this.yPos1 = band.yPos1;
            this.xPos2 = band.xPos2;
            this.yPos2 = band.yPos2;
        }

        @SuppressWarnings({"HE_EQUALS_USE_HASHCODE", "EQ_SELF_USE_OBJECT"})
        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Band)) {
                return false;
            }
            Band band = (Band) o;
            if (EditorUtils.isAlmostEquals(band.xPos1, this.xPos1) && EditorUtils.isAlmostEquals(band.yPos1, this.yPos1) && EditorUtils.isAlmostEquals(band.xPos2, this.xPos2)) {
                z = EditorUtils.isAlmostEquals(band.yPos2, this.yPos2);
            }
            return z;
        }
    }

    public static class Circle {
        private float centerX = GroundOverlayOptions.NO_DIMENSION;
        private float centerY = GroundOverlayOptions.NO_DIMENSION;
        private float radius = GroundOverlayOptions.NO_DIMENSION;

        public void set(Circle circle) {
            this.centerX = circle.centerX;
            this.centerY = circle.centerY;
            this.radius = circle.radius;
        }

        @SuppressWarnings({"HE_EQUALS_USE_HASHCODE", "EQ_SELF_USE_OBJECT"})
        public boolean equals(Object o) {
            boolean z = false;
            if (!(o instanceof Circle)) {
                return false;
            }
            Circle circle = (Circle) o;
            if (EditorUtils.isAlmostEquals(circle.centerX, this.centerX) && EditorUtils.isAlmostEquals(circle.centerY, this.centerY)) {
                z = EditorUtils.isAlmostEquals(circle.radius, this.radius);
            }
            return z;
        }
    }

    public FilterIllusionRepresentation() {
        super("ILLUSION");
        setSerializationName("ILLUSION");
        setFilterType(11);
        setFilterClass(ImageFilterIllusion.class);
        setTextId(R.string.simple_editor_illusion);
    }

    public void setNeedApply(boolean apply) {
        this.mNeedApply = apply;
    }

    public boolean getNeedApply() {
        return this.mNeedApply;
    }

    public void setBound(Rect rect) {
        this.mBound.set(rect);
    }

    public void setValue(int value) {
        this.mValue = value;
    }

    public int getValue() {
        return this.mValue;
    }

    public Rect getBound() {
        return this.mBound;
    }

    public void setStyle(STYLE style) {
        this.mStyle = style;
    }

    public STYLE getStyle() {
        return this.mStyle;
    }

    public void setPoint1(float x, float y) {
        this.mCurrentBand.xPos1 = x;
        this.mCurrentBand.yPos1 = y;
    }

    public void setPoint2(float x, float y) {
        this.mCurrentBand.xPos2 = x;
        this.mCurrentBand.yPos2 = y;
    }

    public float getPoint1X() {
        return this.mCurrentBand.xPos1;
    }

    public float getPoint1Y() {
        return this.mCurrentBand.yPos1;
    }

    public float getPoint2X() {
        return this.mCurrentBand.xPos2;
    }

    public float getPoint2Y() {
        return this.mCurrentBand.yPos2;
    }

    public void setPoint(float x, float y) {
        this.mCurrentCircle.centerX = x;
        this.mCurrentCircle.centerY = y;
    }

    public void setRadius(float radius) {
        this.mCurrentCircle.radius = (float) Math.round(radius);
    }

    public float getPointX() {
        return this.mCurrentCircle.centerX;
    }

    public float getPointY() {
        return this.mCurrentCircle.centerY;
    }

    public float getRadius() {
        return this.mCurrentCircle.radius;
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterIllusionRepresentation)) {
            FilterIllusionRepresentation fdRep = (FilterIllusionRepresentation) representation;
            if (fdRep.mCurrentBand.equals(this.mCurrentBand) && fdRep.mCurrentCircle.equals(this.mCurrentCircle) && fdRep.mBound.equals(this.mBound) && fdRep.mNeedApply == this.mNeedApply && fdRep.mValue == this.mValue) {
                return true;
            }
        }
        return false;
    }

    public FilterIllusionRepresentation copy() {
        FilterIllusionRepresentation representation = new FilterIllusionRepresentation();
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterIllusionRepresentation) {
            FilterIllusionRepresentation representation = (FilterIllusionRepresentation) a;
            try {
                this.mCurrentBand.set(representation.mCurrentBand);
                this.mCurrentCircle.set(representation.mCurrentCircle);
                this.mBound = new Rect(representation.mBound);
                this.mStyle = representation.mStyle;
                this.mNeedApply = representation.mNeedApply;
                this.mValue = representation.mValue;
                return;
            } catch (RuntimeException e) {
                GalleryLog.i("FilterIllusionRepresentation", "catch a RuntimeException." + e.getMessage());
                return;
            }
        }
        GalleryLog.v("FilterIllusionRepresentation", "cannot use parameters from " + a);
    }

    public void reset() {
        this.mCurrentCircle.centerX = 0.0f;
        this.mCurrentCircle.centerY = 0.0f;
        this.mCurrentCircle.radius = 0.0f;
        this.mCurrentBand.xPos1 = 0.0f;
        this.mCurrentBand.yPos1 = 0.0f;
        this.mCurrentBand.xPos2 = 0.0f;
        this.mCurrentBand.yPos2 = 0.0f;
        this.mBound.setEmpty();
        this.mStyle = STYLE.UNKONW;
        this.mNeedApply = true;
        this.mValue = 50;
        super.reset();
    }
}
