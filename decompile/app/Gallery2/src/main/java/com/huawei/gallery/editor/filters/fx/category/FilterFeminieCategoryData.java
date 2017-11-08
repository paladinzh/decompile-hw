package com.huawei.gallery.editor.filters.fx.category;

import android.util.SparseArray;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.category.CommonFilterAdapter;
import com.huawei.gallery.editor.filters.fx.category.FilterPlainData.FILTER_REPRESENTATION_STYLE;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTER_STYLE;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.step.FxEditorStep;

public class FilterFeminieCategoryData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainDataFeminie = new FilterPlainData[]{new FilterPlainData("huawei1", (int) R.string.effect_huawei1, "LUT3D_HUAWEI1", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("huawei2", (int) R.string.effect_huawei2, "LUT3D_HUAWEI2", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("huawei3", (int) R.string.effect_huawei3, "LUT3D_HUAWEI3", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("huawei4", (int) R.string.effect_huawei4, "LUT3D_HUAWEI4", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("huawei5", (int) R.string.effect_huawei5, "LUT3D_HUAWEI5", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("huawei6", (int) R.string.effect_huawei6, "LUT3D_HUAWEI6", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("snow", (int) R.string.effect_snow, "LUT3D_SNOW", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("firefly", (int) R.string.effect_firefly, "LUT3D_FIREFLY", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("petal", (int) R.string.effect_petal, "LUT3D_PETAL", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("waterbubble", (int) R.string.effect_waterbubble, "LUT3D_WATERBUBBLE", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        if (EditorLoadLib.FEMININE_EFFECT_LOADED) {
            CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
            addOriginFilter(adapter, manager);
            for (FilterPlainData data : sFilterPlainDataFeminie) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
            if (adapter.getCount() >= 2) {
                adapterSparseArray.put(FILTER_STYLE.FEMININE.ordinal(), adapter);
            }
        }
    }
}
