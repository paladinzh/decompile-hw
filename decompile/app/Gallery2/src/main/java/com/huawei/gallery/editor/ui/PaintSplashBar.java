package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.tools.EditorUtils;

public class PaintSplashBar extends BasePaintBar {
    public PaintSplashBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getEraseButtonId() {
        return -1;
    }

    protected void initMenuButtonImageSource() {
    }

    protected int getSubMenuChildLayout(int viewId) {
        switch (viewId) {
            case 0:
                return isPort() ? R.layout.paint_brush_stroke_container : R.layout.paint_brush_stroke_container_land;
            default:
                return -1;
        }
    }

    protected int[] getSubMenuChildButtonsId(int viewId) {
        switch (viewId) {
            case 0:
                return SUB_STROKE_LEVEL_BUTTON_ID;
            default:
                return new int[0];
        }
    }

    protected void selectSubMenuChildButton(int viewId) {
        int buttonIndex = -1;
        switch (viewId) {
            case 0:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.splashStrokeIndex, 0, SUB_STROKE_LEVEL_BUTTON_ID.length - 1);
                break;
        }
        if (buttonIndex == -1) {
            GalleryLog.v("PaintSplashBar", "buttonIndex = -1");
            return;
        }
        int[] buttonId = getSubMenuChildButtonsId(viewId);
        if (buttonId == null) {
            GalleryLog.v("PaintSplashBar", "buttonId == null");
            return;
        }
        if (buttonIndex < buttonId.length) {
            onClick(findViewById(buttonId[buttonIndex]));
        }
    }

    protected int getSubMenuChildRootId(int viewId) {
        switch (viewId) {
            case 0:
                return R.id.brush_stroke_root;
            default:
                return -1;
        }
    }

    protected void processClickView(View v) {
        int viewId = v.getId();
        int index = EditorUtils.indexOf(SUB_STROKE_LEVEL_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.splashStrokeIndex = index;
            v.setSelected(true);
            for (int id : SUB_STROKE_LEVEL_BUTTON_ID) {
                if (id != viewId) {
                    findViewById(id).setSelected(false);
                }
            }
            ReportToBigData.report(125, String.format("{MosaicStroke:%s}", new Object[]{Integer.valueOf(index)}));
        }
    }
}
