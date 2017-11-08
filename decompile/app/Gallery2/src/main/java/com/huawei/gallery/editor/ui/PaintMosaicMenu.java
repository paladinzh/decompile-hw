package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.IconData;

public class PaintMosaicMenu extends BasePaintMenu {
    public PaintMosaicMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void updateIconDataContainer() {
        this.mIconDataContainer.put(0, new IconData(0, (int) R.drawable.btn_menu_mosaic_1, (int) R.string.simple_editor_mosaic));
        this.mIconDataContainer.put(1, new IconData(1, (int) R.drawable.ic_gallery_edit_stroke_size_3, (int) R.string.paint_icon_size));
        this.mIconDataContainer.put(2, new IconData(2, (int) R.drawable.ic_gallery_edit_eraser, (int) R.string.simple_editor_eraser));
    }
}
