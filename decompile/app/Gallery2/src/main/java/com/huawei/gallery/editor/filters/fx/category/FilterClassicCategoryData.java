package com.huawei.gallery.editor.filters.fx.category;

import android.util.SparseArray;
import com.android.gallery3d.R;
import com.huawei.android.sdk.imagefilter.HwImageFilters;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.category.CommonFilterAdapter;
import com.huawei.gallery.editor.filters.ImageFilterFx;
import com.huawei.gallery.editor.filters.fx.category.FilterPlainData.FILTER_REPRESENTATION_STYLE;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTERMODE;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin.FILTER_STYLE;
import com.huawei.gallery.editor.pipeline.EditorLoadLib;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.step.FxEditorStep;

public class FilterClassicCategoryData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainData = new FilterPlainData[]{new FilterPlainData(FILTERMODE.RIXI.ordinal(), (int) R.string.pref_camera_coloreffect_entry_rixi, "LUT3D_RIXI", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.VALENCIA.ordinal(), (int) R.string.pref_camera_coloreffect_entry_valencia, "LUT3D_VALENCIA", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.TIANMEIKEREN.ordinal(), (int) R.string.pref_camera_coloreffect_entry_sweet, "LUT3D_TIANMEIKEREN", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.MEISHIMENGHUAN.ordinal(), (int) R.string.pref_camera_coloreffect_entry_food, "LUT3D_MEISHIMENGHUAN", FILTER_REPRESENTATION_STYLE.HWCOMMOM)};
    private static FilterPlainData[] sFilterPlainData2 = new FilterPlainData[]{new FilterPlainData(FILTERMODE.XPRO2.ordinal(), (int) R.string.pref_camera_coloreffect_entry_blue, "LUT3D_XPRO2", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.HUDSON.ordinal(), (int) R.string.pref_camera_coloreffect_entry_handsome, "LUT3D_HUDSON", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.MYFAIR.ordinal(), (int) R.string.pref_camera_coloreffect_entry_sentimental, "LUT3D_MYFAIR", FILTER_REPRESENTATION_STYLE.HWCOMMOM), new FilterPlainData(FILTERMODE.LOFI.ordinal(), (int) R.string.pref_camera_coloreffect_entry_individuality, "LUT3D_LOFI", FILTER_REPRESENTATION_STYLE.HWCOMMOM)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        int i = 0;
        if (EditorLoadLib.FILTERJNI_LOADED) {
            CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
            addOriginFilter(adapter, manager);
            for (FilterPlainData data : sFilterPlainData) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
            if (ImageFilterFx.getVersion() >= HwImageFilters.VERSION_FOR_HONOR_PLUS) {
                FilterPlainData[] filterPlainDataArr = sFilterPlainData2;
                int length = filterPlainDataArr.length;
                while (i < length) {
                    adapter.add(new Action(manager, filterPlainDataArr[i].createRepresentation(manager.getContext()), 2, FxEditorStep.class));
                    i++;
                }
            }
            if (adapter.getCount() >= 2) {
                adapterSparseArray.put(FILTER_STYLE.CLASSIC.ordinal(), adapter);
            }
        }
    }
}
