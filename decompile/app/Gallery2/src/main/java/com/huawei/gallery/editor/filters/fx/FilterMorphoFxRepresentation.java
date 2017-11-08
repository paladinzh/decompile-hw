package com.huawei.gallery.editor.filters.fx;

import com.android.gallery3d.R;
import com.huawei.gallery.editor.filters.FilterRepresentation;

public class FilterMorphoFxRepresentation extends FilterChangableFxRepresentation {
    private String mEffectName;

    public FilterMorphoFxRepresentation(String name, String effectName, int nameResource) {
        super(name, nameResource);
        setFilterClass(ImageFilterMorphoFx.class);
        this.mEffectName = effectName;
        initSeekBarItems();
    }

    public String toString() {
        return "ImageFilterFxExtends: " + hashCode() + " : " + getName() + " effect name rsc: " + this.mEffectName;
    }

    public FilterRepresentation copy() {
        FilterMorphoFxRepresentation representation = new FilterMorphoFxRepresentation(getName(), null, 0);
        copyAllParameters(representation);
        return representation;
    }

    protected void copyAllParameters(FilterRepresentation representation) {
        super.copyAllParameters(representation);
        representation.useParametersFrom(this);
    }

    public synchronized void useParametersFrom(FilterRepresentation a) {
        super.useParametersFrom(a);
        if (a instanceof FilterMorphoFxRepresentation) {
            setEffectName(((FilterMorphoFxRepresentation) a).getEffectName());
        }
    }

    public boolean equals(FilterRepresentation representation) {
        if (super.equals(representation) && (representation instanceof FilterMorphoFxRepresentation)) {
            if (this.mEffectName.equalsIgnoreCase(((FilterMorphoFxRepresentation) representation).mEffectName)) {
                return true;
            }
        }
        return false;
    }

    public String getEffectName() {
        return this.mEffectName;
    }

    public void setEffectName(String effectName) {
        this.mEffectName = effectName;
    }

    private void initSeekBarItems() {
        if (this.mSeekBarItems != null) {
            this.mSeekBarItems.clear();
            if (this.mEffectName != null) {
                if (this.mEffectName.equalsIgnoreCase("huawei_monochrome")) {
                    this.mSeekBarItems.put(1, R.string.contrast);
                    this.mSeekBarItems.put(2, R.string.filter_par_grain);
                    this.mSeekBarItems.put(3, R.string.sharpness);
                    this.mSeekBarItems.put(4, R.string.tone);
                } else if (this.mEffectName.equalsIgnoreCase("huawei_moriyama")) {
                    this.mSeekBarItems.put(1, R.string.contrast);
                    this.mSeekBarItems.put(5, R.string.editor_grad_brightness);
                    this.mSeekBarItems.put(2, R.string.filter_par_grain);
                } else if (this.mEffectName.equalsIgnoreCase("huawei_graylevels")) {
                    this.mSeekBarItems.put(6, R.string.ev);
                    this.mSeekBarItems.put(7, R.string.highlight_recovery);
                    this.mSeekBarItems.put(8, R.string.shadow_recovery);
                } else {
                    this.mSeekBarItems.put(0, R.string.strength);
                }
            }
        }
    }
}
