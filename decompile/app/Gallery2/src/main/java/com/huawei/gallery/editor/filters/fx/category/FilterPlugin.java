package com.huawei.gallery.editor.filters.fx.category;

import android.util.SparseArray;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.Action;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.filters.fx.category.FilterPlainData.FILTER_REPRESENTATION_STYLE;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;
import com.huawei.gallery.editor.step.FxEditorStep;

public abstract class FilterPlugin {
    private static final FilterPlainData[] sFilterPlainDataOrigin = new FilterPlainData[]{new FilterPlainData(FILTERMODE.ORIGINAL.ordinal(), (int) R.string.pref_camera_coloreffect_entry_original, "", FILTER_REPRESENTATION_STYLE.ORIGIN)};

    public enum FILTERMODE {
        ORIGINAL,
        MORAN,
        RIXI,
        FUGU,
        VALENCIA,
        LAODIANYING,
        LIANGHONG,
        YUNDUAN,
        EARLYBIRD,
        NUANYANGYANG,
        TIANMEIKEREN,
        MEISHIMENGHUAN,
        HEIBAI,
        XPRO2,
        HUDSON,
        MYFAIR,
        LOFI,
        MIST
    }

    public enum FILTER_STYLE {
        ORIGIN(0),
        HEIBAI(R.string.pref_camera_coloreffect_entry_mono),
        FUGU(R.string.pref_camera_coloreffect_entry_fugu),
        SHADOW(R.string.filter_class_shadow),
        CLASSIC(R.string.filter_class_classic),
        PAINT(R.string.filter_class_paint),
        WEATHER(R.string.filter_class_weather),
        FEMININE(R.string.filter_class_feminie);
        
        public final int filterClassNameId;

        private FILTER_STYLE(int nameId) {
            this.filterClassNameId = nameId;
        }
    }

    public abstract void fillAdapter(SparseArray<CategoryAdapter> sparseArray, SimpleEditorManager simpleEditorManager);

    public void addOriginFilter(CategoryAdapter adapter, SimpleEditorManager manager) {
        if (adapter != null && manager != null) {
            for (FilterPlainData data : sFilterPlainDataOrigin) {
                adapter.add(new Action(manager, data.createRepresentation(manager.getContext()), 2, FxEditorStep.class));
            }
        }
    }
}
