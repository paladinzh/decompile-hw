package com.huawei.gallery.editor.filters.fx.category;

import android.content.Context;
import com.huawei.gallery.editor.filters.FilterRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterFeminineFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterGoogleFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterHuaweiCommonFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterHuaweiMistFxRepresentation;
import com.huawei.gallery.editor.filters.fx.FilterMorphoFxRepresentation;

public class FilterPlainData {
    private static final /* synthetic */ int[] -com-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues = null;
    private final String filterEffectName;
    private final int filterId;
    private final int filterNameId;
    private final String serializationName;
    private final FILTER_REPRESENTATION_STYLE style;

    public enum FILTER_REPRESENTATION_STYLE {
        ORIGIN,
        GOOGLE,
        HWCOMMOM,
        HWMIST,
        MORPHO_CHANGE,
        MORPHO_NOT_CHANGE
    }

    private static /* synthetic */ int[] -getcom-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues() {
        if (-com-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues != null) {
            return -com-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues;
        }
        int[] iArr = new int[FILTER_REPRESENTATION_STYLE.values().length];
        try {
            iArr[FILTER_REPRESENTATION_STYLE.GOOGLE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[FILTER_REPRESENTATION_STYLE.HWCOMMOM.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[FILTER_REPRESENTATION_STYLE.HWMIST.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[FILTER_REPRESENTATION_STYLE.ORIGIN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues = iArr;
        return iArr;
    }

    public FilterPlainData(int id, int filterNameId, String serializationName, FILTER_REPRESENTATION_STYLE style) {
        this.filterId = id;
        this.filterNameId = filterNameId;
        this.serializationName = serializationName;
        this.filterEffectName = "";
        this.style = style;
    }

    public FilterPlainData(String filterEffectName, int filterNameId, String serializationName, FILTER_REPRESENTATION_STYLE style) {
        this.filterEffectName = filterEffectName;
        this.filterNameId = filterNameId;
        this.serializationName = serializationName;
        this.filterId = 0;
        this.style = style;
    }

    FilterRepresentation createRepresentation(Context context) {
        FilterFxRepresentation fx;
        switch (-getcom-huawei-gallery-editor-filters-fx-category-FilterPlainData$FILTER_REPRESENTATION_STYLESwitchesValues()[this.style.ordinal()]) {
            case 1:
                return new FilterGoogleFxRepresentation(context.getString(this.filterNameId), this.filterNameId, this.filterId);
            case 2:
                fx = new FilterHuaweiCommonFxRepresentation(context.getString(this.filterNameId), this.filterNameId, this.filterId);
                fx.setSerializationName(this.serializationName);
                return fx;
            case 3:
                fx = new FilterHuaweiMistFxRepresentation(context.getString(this.filterNameId), this.filterNameId);
                fx.setSerializationName(this.serializationName);
                return fx;
            case 4:
                FilterMorphoFxRepresentation fx2 = new FilterMorphoFxRepresentation(context.getString(this.filterNameId), this.filterEffectName, this.filterNameId);
                fx2.setSerializationName(this.serializationName);
                return fx2;
            case 5:
                FilterFeminineFxRepresentation fx3 = new FilterFeminineFxRepresentation(context.getString(this.filterNameId), this.filterEffectName, this.filterNameId);
                fx3.setSerializationName(this.serializationName);
                return fx3;
            case 6:
                return new FilterFxRepresentation(context.getString(this.filterNameId), this.filterNameId);
            default:
                return null;
        }
    }
}
