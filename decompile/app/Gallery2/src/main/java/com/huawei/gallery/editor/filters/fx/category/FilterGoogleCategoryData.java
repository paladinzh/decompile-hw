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

public class FilterGoogleCategoryData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainData = new FilterPlainData[]{new FilterPlainData((int) R.drawable.filtershow_fx_0000_vintage, (int) R.string.ffx_vintage, "LUT3D_VINTAGE", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0006_x_process, (int) R.string.ffx_x_process, "LUT3D_XPROCESS", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0001_instant, (int) R.string.ffx_instant, "LUT3D_INSTANT", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0005_punch, (int) R.string.ffx_punch, "LUT3D_PUNCH", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0008_washout_color, (int) R.string.ffx_washout_color, "LUT3D_WASHOUT_COLOR", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0004_bw_contrast, (int) R.string.ffx_bw_contrast, "LUT3D_BW", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0002_bleach, (int) R.string.ffx_bleach, "LUT3D_BLEACH", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0003_blue_crush, (int) R.string.ffx_blue_crush, "LUT3D_BLUECRUSH", FILTER_REPRESENTATION_STYLE.GOOGLE), new FilterPlainData((int) R.drawable.filtershow_fx_0007_washout, (int) R.string.ffx_washout, "LUT3D_WASHOUT", FILTER_REPRESENTATION_STYLE.GOOGLE)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        if (!EditorLoadLib.FILTERJNI_LOADED) {
            CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
            addOriginFilter(adapter, manager);
            for (FilterPlainData data : sFilterPlainData) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
            if (adapter.getCount() >= 2) {
                adapterSparseArray.put(FILTER_STYLE.CLASSIC.ordinal(), adapter);
            }
        }
    }
}
