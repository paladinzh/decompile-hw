package com.huawei.gallery.editor.filters;

import com.android.gallery3d.R;

public class FilterRotateRepresentation extends FilterRepresentation {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues = null;
    private boolean mClockwise;
    Rotation mRotation;

    public enum Rotation {
        ZERO(0),
        NINETY(90),
        ONE_EIGHTY(180),
        TWO_SEVENTY(270);
        
        private final int mValue;

        private Rotation(int value) {
            this.mValue = value;
        }

        public int value() {
            return this.mValue;
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues() {
        if (-com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues != null) {
            return -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues;
        }
        int[] iArr = new int[Rotation.values().length];
        try {
            iArr[Rotation.NINETY.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Rotation.ONE_EIGHTY.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Rotation.TWO_SEVENTY.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Rotation.ZERO.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues = iArr;
        return iArr;
    }

    public FilterRotateRepresentation(Rotation rotation) {
        super("ROTATION");
        setSerializationName("ROTATION");
        setFilterClass(FilterRotateRepresentation.class);
        setFilterType(4);
        setTextId(R.string.rotate);
        setRotation(rotation);
    }

    public FilterRotateRepresentation(FilterRotateRepresentation r) {
        this(r.getRotation());
        setName(r.getName());
    }

    public FilterRotateRepresentation() {
        this(getNil());
    }

    public Rotation getRotation() {
        return this.mRotation;
    }

    public void setClockwise(boolean clockwise) {
        this.mClockwise = clockwise;
    }

    public void rotateCW() {
        switch (-getcom-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues()[this.mRotation.ordinal()]) {
            case 1:
                this.mRotation = this.mClockwise ? Rotation.ONE_EIGHTY : Rotation.ZERO;
                return;
            case 2:
                this.mRotation = this.mClockwise ? Rotation.TWO_SEVENTY : Rotation.NINETY;
                return;
            case 3:
                this.mRotation = this.mClockwise ? Rotation.ZERO : Rotation.ONE_EIGHTY;
                return;
            case 4:
                this.mRotation = this.mClockwise ? Rotation.NINETY : Rotation.TWO_SEVENTY;
                return;
            default:
                return;
        }
    }

    public Rotation getNextRotation() {
        switch (-getcom-huawei-gallery-editor-filters-FilterRotateRepresentation$RotationSwitchesValues()[this.mRotation.ordinal()]) {
            case 1:
                return this.mClockwise ? Rotation.ONE_EIGHTY : Rotation.ZERO;
            case 2:
                return this.mClockwise ? Rotation.TWO_SEVENTY : Rotation.NINETY;
            case 3:
                return this.mClockwise ? Rotation.ZERO : Rotation.ONE_EIGHTY;
            case 4:
                return this.mClockwise ? Rotation.NINETY : Rotation.TWO_SEVENTY;
            default:
                return Rotation.ZERO;
        }
    }

    public void setRotation(Rotation rotation) {
        if (rotation == null) {
            throw new IllegalArgumentException("Argument to setRotation is null");
        }
        this.mRotation = rotation;
    }

    public FilterRepresentation copy() {
        return new FilterRotateRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterRotateRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterRotateRepresentation) {
            setRotation(((FilterRotateRepresentation) a).getRotation());
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return this.mRotation == getNil();
    }

    public static Rotation getNil() {
        return Rotation.ZERO;
    }

    public boolean equals(FilterRepresentation rep) {
        boolean z = false;
        if (!(rep instanceof FilterRotateRepresentation)) {
            return false;
        }
        if (((FilterRotateRepresentation) rep).mRotation.value() == this.mRotation.value()) {
            z = true;
        }
        return z;
    }
}
