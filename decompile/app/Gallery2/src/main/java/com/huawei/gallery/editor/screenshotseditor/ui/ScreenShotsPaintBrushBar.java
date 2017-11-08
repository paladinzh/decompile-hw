package com.huawei.gallery.editor.screenshotseditor.ui;

import android.content.Context;
import android.util.AttributeSet;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.ui.PaintBrushBar;

public class ScreenShotsPaintBrushBar extends PaintBrushBar {
    public ScreenShotsPaintBrushBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getEraseButtonId() {
        return -1;
    }

    protected int getSubMenuChildLayout(int viewId) {
        switch (viewId) {
            case 0:
                return isPort() ? R.layout.screen_shots_paint_brush_shape_container : R.layout.screen_shots_paint_brush_shape_container_land;
            case 1:
                return isPort() ? R.layout.screen_shots_paint_brush_color_container : R.layout.screen_shots_paint_brush_color_container_land;
            case 2:
                return isPort() ? R.layout.screen_shots_paint_brush_stroke_container : R.layout.screen_shots_paint_brush_stroke_container_land;
            default:
                return -1;
        }
    }
}
