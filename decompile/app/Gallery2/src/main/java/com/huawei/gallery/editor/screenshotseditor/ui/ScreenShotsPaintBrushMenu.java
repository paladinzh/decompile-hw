package com.huawei.gallery.editor.screenshotseditor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.ui.BasePaintMenu;

public class ScreenShotsPaintBrushMenu extends BasePaintMenu {
    public ScreenShotsPaintBrushMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void attachView() {
        ((LayoutInflater) getContext().getSystemService("layout_inflater")).inflate(getResources().getConfiguration().orientation == 1 ? R.layout.screen_shots_brush_menu : R.layout.screen_shots_brush_menu_land, this, true);
        View view = findViewById(R.id.paint_style);
        view.setId(0);
        view.setOnClickListener(this);
        view = findViewById(R.id.paint_color);
        view.setId(1);
        view.setOnClickListener(this);
        view = findViewById(R.id.stroke_icon);
        view.setId(2);
        view.setOnClickListener(this);
    }

    protected void updateIconDataContainer() {
    }
}
