package com.huawei.gallery.editor.filters.fx;

import android.util.SparseArray;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.CategoryAdapter;
import com.huawei.gallery.editor.filters.fx.category.FilterBWCategroyData;
import com.huawei.gallery.editor.filters.fx.category.FilterClassicCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterFeminieCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterFuguCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterGoogleCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterPaintCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterPlugin;
import com.huawei.gallery.editor.filters.fx.category.FilterShadowCategoryData;
import com.huawei.gallery.editor.filters.fx.category.FilterWeatherCategoryData;
import com.huawei.gallery.editor.pipeline.SimpleEditorManager;

public class FilterLoader {
    private static FilterPlugin[] sPlugins = new FilterPlugin[]{new FilterBWCategroyData(), new FilterFuguCategoryData(), new FilterClassicCategoryData(), new FilterShadowCategoryData(), new FilterGoogleCategoryData(), new FilterPaintCategoryData(), new FilterWeatherCategoryData(), new FilterFeminieCategoryData()};

    public void fillAdapter(SparseArray<CategoryAdapter> adapterSparseArray, SimpleEditorManager manager) {
        for (FilterPlugin plugin : sPlugins) {
            plugin.fillAdapter(adapterSparseArray, manager);
        }
        int verticalItemHeight = (int) manager.getContext().getResources().getDimension(R.dimen.action_item_height);
        for (int i = 0; i < adapterSparseArray.size(); i++) {
            CategoryAdapter adapter = (CategoryAdapter) adapterSparseArray.get(adapterSparseArray.keyAt(i));
            adapter.setItemHeight(verticalItemHeight);
            adapter.setOrientation(1);
        }
    }
}
