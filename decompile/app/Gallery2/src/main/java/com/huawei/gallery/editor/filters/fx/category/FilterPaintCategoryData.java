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

public class FilterPaintCategoryData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainDataPaint = new FilterPlainData[]{new FilterPlainData("pencil_mono", (int) R.string.mph_pencil, "LUT3D_PENCIL", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE), new FilterPlainData("pencil_color", (int) R.string.mph_pencil_color, "LUT3D_PENCIL_COLOR", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE), new FilterPlainData("crayon", (int) R.string.mph_crayon, "LUT3D_CRAYON", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE), new FilterPlainData("watercolor2", (int) R.string.mph_watercolor, "LUT3D_WATERCOLOR", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        if (EditorLoadLib.FILTERJNI_MORPHO_LOADED) {
            CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
            addOriginFilter(adapter, manager);
            for (FilterPlainData data : sFilterPlainDataPaint) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
            if (adapter.getCount() >= 2) {
                adapterSparseArray.put(FILTER_STYLE.PAINT.ordinal(), adapter);
            }
        }
    }
}
