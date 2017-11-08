package com.huawei.gallery.editor.filters;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRotateRepresentation.Rotation;

public class FilterMirrorRepresentation extends FilterRepresentation {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues = null;
    private boolean mHorizontal;
    Mirror mMirror;

    public enum Mirror {
        NONE('N'),
        VERTICAL('V'),
        HORIZONTAL('H'),
        BOTH('B');
        
        char mValue;

        private Mirror(char value) {
            this.mValue = value;
        }

        public char value() {
            return this.mValue;
        }
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues() {
        if (-com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues != null) {
            return -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues;
        }
        int[] iArr = new int[Mirror.values().length];
        try {
            iArr[Mirror.BOTH.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mirror.HORIZONTAL.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mirror.NONE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mirror.VERTICAL.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues = iArr;
        return iArr;
    }

    public FilterMirrorRepresentation(Mirror mirror) {
        super("MIRROR");
        setSerializationName("MIRROR");
        setFilterClass(FilterMirrorRepresentation.class);
        setFilterType(4);
        setTextId(R.string.mirror);
        setMirror(mirror);
    }

    public FilterMirrorRepresentation(FilterMirrorRepresentation m) {
        this(m.getMirror());
        setName(m.getName());
    }

    public FilterMirrorRepresentation() {
        this(getNil());
    }

    public boolean equals(FilterRepresentation rep) {
        boolean z = false;
        if (!(rep instanceof FilterMirrorRepresentation)) {
            return false;
        }
        if (this.mMirror == ((FilterMirrorRepresentation) rep).mMirror) {
            z = true;
        }
        return z;
    }

    public Mirror getMirror() {
        return this.mMirror;
    }

    public void setMirror(Mirror mirror) {
        if (mirror == null) {
            throw new IllegalArgumentException("Argument to setMirror is null");
        }
        this.mMirror = mirror;
    }

    public void setHorizontal(boolean horizontal) {
        this.mHorizontal = horizontal;
    }

    public void cycle(Rotation r) {
        int rotation = r.value();
        boolean hasHorizontalRotation = rotation == 90 || rotation == 270;
        switch (-getcom-huawei-gallery-editor-filters-FilterMirrorRepresentation$MirrorSwitchesValues()[this.mMirror.ordinal()]) {
            case 1:
                if (hasHorizontalRotation) {
                    this.mMirror = this.mHorizontal ? Mirror.HORIZONTAL : Mirror.VERTICAL;
                    return;
                } else {
                    this.mMirror = this.mHorizontal ? Mirror.VERTICAL : Mirror.HORIZONTAL;
                    return;
                }
            case 2:
                if (hasHorizontalRotation) {
                    this.mMirror = this.mHorizontal ? Mirror.BOTH : Mirror.NONE;
                    return;
                } else {
                    this.mMirror = this.mHorizontal ? Mirror.NONE : Mirror.BOTH;
                    return;
                }
            case 3:
                if (hasHorizontalRotation) {
                    this.mMirror = this.mHorizontal ? Mirror.VERTICAL : Mirror.HORIZONTAL;
                    return;
                } else {
                    this.mMirror = this.mHorizontal ? Mirror.HORIZONTAL : Mirror.VERTICAL;
                    return;
                }
            case 4:
                if (hasHorizontalRotation) {
                    this.mMirror = this.mHorizontal ? Mirror.NONE : Mirror.BOTH;
                    return;
                } else {
                    this.mMirror = this.mHorizontal ? Mirror.BOTH : Mirror.NONE;
                    return;
                }
            default:
                return;
        }
    }

    public FilterRepresentation copy() {
        return new FilterMirrorRepresentation(this);
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        if (representation instanceof FilterMirrorRepresentation) {
            super.copyAllParameters(representation);
            representation.useParametersFrom(this);
            return;
        }
        throw new IllegalArgumentException("calling copyAllParameters with incompatible types!");
    }

    public void useParametersFrom(FilterRepresentation a) {
        if (a instanceof FilterMirrorRepresentation) {
            setMirror(((FilterMirrorRepresentation) a).getMirror());
            return;
        }
        throw new IllegalArgumentException("calling useParametersFrom with incompatible types!");
    }

    public boolean isNil() {
        return this.mMirror == getNil();
    }

    public static Mirror getNil() {
        return Mirror.NONE;
    }
}
