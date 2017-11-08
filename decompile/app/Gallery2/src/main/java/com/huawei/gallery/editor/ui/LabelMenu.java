package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.IconData;

public class LabelMenu extends BasePaintMenu {
    public LabelMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void updateIconDataContainer() {
        this.mIconDataContainer.put(0, new IconData(0, (int) R.drawable.ic_gallery_edit_label_textbox, (int) R.string.editor_label));
        this.mIconDataContainer.put(1, new IconData(1, (int) R.drawable.ic_gallery_edit_label_text, (int) R.string.label_icon_text));
    }
}
