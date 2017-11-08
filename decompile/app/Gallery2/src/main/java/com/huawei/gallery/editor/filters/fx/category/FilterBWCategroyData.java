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

public class FilterBWCategroyData extends FilterPlugin {
    private static FilterPlainData[] sFilterPlainDataFilm = new FilterPlainData[]{new FilterPlainData("darkroom_fujifilm_n100a", (int) R.string.mph_fujifilm_neopan_100_acros, "LUT3D_DARKROOM_FUJIFILM", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("darkroom_ilford_d400", (int) R.string.mph_ilford_delta_400, "LUT3D_DARKROOM_ILFORD", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("darkroom_kodak_100tmx", (int) R.string.mph_kodak_t_max_100_pushed1stops, "LUT3D_DARKROOM_KODAK_100TMX", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("darkroom_kodak_400tx", (int) R.string.mph_kodak_tri_x_400tx_pushed2stops, "LUT3D_DARKROOM_KODAK_100TX", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE), new FilterPlainData("darkroom_kodak_bw400cn", (int) R.string.mph_kodak_bw_400cn, "LUT3D_DARKROOM_KODAK_BW400CN", FILTER_REPRESENTATION_STYLE.MORPHO_NOT_CHANGE)};
    private static FilterPlainData[] sFilterPlainDataHWBW = new FilterPlainData[]{new FilterPlainData(FILTERMODE.HEIBAI.ordinal(), (int) R.string.pref_camera_coloreffect_entry_mono, "LUT3D_HEIBAI", FILTER_REPRESENTATION_STYLE.HWCOMMOM)};
    private static FilterPlainData[] sFilterPlainDataMorphoBW = new FilterPlainData[]{new FilterPlainData("huawei_monochrome", (int) R.string.pref_camera_coloreffect_entry_mono, "LUT3D_MONO", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE), new FilterPlainData("huawei_moriyama", (int) R.string.pref_camera_coloreffect_entry_mph_impact, "LUT3D_IMPACT", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE), new FilterPlainData("huawei_graylevels", (int) R.string.pref_camera_coloreffect_entry_mph_graylevels, "LUT3D_ND", FILTER_REPRESENTATION_STYLE.MORPHO_CHANGE)};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        int i = 0;
        CommonFilterAdapter adapter = new CommonFilterAdapter(manager.getContext());
        addOriginFilter(adapter, manager);
        if (EditorLoadLib.FILTERJNI_MORPHO_LOADED) {
            for (FilterPlainData data : sFilterPlainDataMorphoBW) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
        } else if (EditorLoadLib.FILTERJNI_LOADED) {
            for (FilterPlainData data2 : sFilterPlainDataHWBW) {
                adapter.add(new Action(manager, data2.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
        }
        if (EditorLoadLib.FEMININE_EFFECT_LOADED) {
            FilterPlainData[] filterPlainDataArr = sFilterPlainDataFilm;
            int length = filterPlainDataArr.length;
            while (i < length) {
                adapter.add(new Action(manager, filterPlainDataArr[i].createRepresentation(manager.getContext()), 2, FxEditorStep.class));
                i++;
            }
        }
        if (adapter.getCount() >= 2) {
            adapterSparseArray.put(FILTER_STYLE.HEIBAI.ordinal(), adapter);
        }
    }
}
