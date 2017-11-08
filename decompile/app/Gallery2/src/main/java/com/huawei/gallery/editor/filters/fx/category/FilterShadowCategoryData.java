package com.huawei.gallery.editor.filters.fx.category;

import android.util.SparseArray;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.category.CommonFilterAdapter;
import com.huawei.gallery.editor.filters.fx.category.FilterPlainData.FILTER_REPRESENTATION_STYLE;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTERMODE;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTER_STYLE;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.step.FxEditorStep;

public class FilterShadowCategoryData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainData = new FilterPlainData[]{new FilterPlainData(FILTERMODE.LIANGHONG.ordinal(), (int) R.string.pref_camera_coloreffect_entry_lianghong, "LUT3D_LIANGHONG", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.EARLYBIRD.ordinal(), (int) R.string.pref_camera_coloreffect_entry_earlybird, "LUT3D_EARLYBIRD", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.NUANYANGYANG.ordinal(), (int) R.string.pref_camera_coloreffect_entry_nyy, "LUT3D_NUANYANGYANG", FILTER_REPRESENTATION_STYLE.HWCOMMOM)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        if (EditorLoadLib.FILTERJNI_LOADED) {
            CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
            addOriginFilter(adapter, manager);
            for (FilterPlainData data : sFilterPlainData) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
            if (adapter.getCount() >= 2) {
                adapterSparseArray.put(FILTER_STYLE.SHADOW.ordinal(), adapter);
            }
        }
    }
}
