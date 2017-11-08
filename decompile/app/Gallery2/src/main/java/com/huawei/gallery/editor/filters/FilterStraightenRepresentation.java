package com.huawei.gallery.editor.filters;

import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;

public class FilterStraightenRepresentation extends FilterRepresentation {
    float mStraighten;

    public FilterStraightenRepresentation(float straighten) {
        super("STRAIGHTEN");
        setSerializationName("STRAIGHTEN");
        setFilterClass(FilterStraightenRepresentation.class);
        setFilterType(4);
        setTextId(R.string.straighten);
        setStraighten(straighten);
    }

    public FilterStraightenRepresentation(FilterStraightenRepresentation s) {
        this(s.getStraighten());
        setName(s.getName());
    }

    public FilterStraightenRepresentation() {
        this(getNil());
    }

    public boolean equals(FilterRepresentation rep) {
        if ((rep instanceof FilterStraightenRepresentation) && Utils.equal(((FilterStraightenRepresentation) rep).mStraighten, this.mStraighten)) {
            return true;
        }
        return false;
    }

    public float getStraighten() {
        return this.mStraighten;
    }

    public void setStraighten(float straighten) {
        if (!rangeCheck((double) straighten)) {
            straighten = Math.min(Math.max(straighten, -45.0f), 45.0f);
        }
        this.mStraighten = straighten;
    }

    public FilterRepresentation copy() {
        return new FilterStraightenRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterStraightenRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterStraightenRepresentation) {
            setStraighten(((FilterStraightenRepresentation) a).getStraighten());
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return Utils.isNil(this.mStraighten);
    }

    public static float getNil() {
        return 0.0f;
    }

    private boolean rangeCheck(double s) {
        if (s < -45.0d || s > 45.0d) {
            return false;
        }
        return true;
    }
}
