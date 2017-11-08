package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.tools.EditorUtils;

public class PaintMosaicBar extends BasePaintBar {
    private static final int[] MENU_MOSAIC_DRAWABLE = new int[]{R.drawable.btn_menu_mosaic_label_1, R.drawable.btn_menu_mosaic_label_2, R.drawable.btn_menu_mosaic_label_3, R.drawable.btn_menu_mosaic_label_4, R.drawable.btn_menu_mosaic_label_5, R.drawable.btn_menu_mosaic_label_6, R.drawable.btn_menu_mosaic_label_7, R.drawable.btn_menu_mosaic_label_8, R.drawable.btn_menu_mosaic_label_9};
    private static final int[] SUB_MENU_MOSAIC_BUTTON_ID = new int[]{R.id.menu_mosaic_1, R.id.menu_mosaic_2, R.id.menu_mosaic_3, R.id.menu_mosaic_4, R.id.menu_mosaic_5, R.id.menu_mosaic_6, R.id.menu_mosaic_7, R.id.menu_mosaic_8, R.id.menu_mosaic_9};

    public PaintMosaicBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getEraseButtonId() {
        return 2;
    }

    protected void initMenuButtonImageSource() {
        ((EditorTextView) this.mBasePaintMenu.findViewById(0)).updateDrawable(MENU_MOSAIC_DRAWABLE[Utils.clamp(EditorUtils.sEditorBrushData.mosaicShapeIndex, 0, MENU_MOSAIC_DRAWABLE.length - 1)]);
        ((EditorTextView) this.mBasePaintMenu.findViewById(1)).updateDrawable(MENU_STROKE_DRAWABLE[Utils.clamp(EditorUtils.sEditorBrushData.mosaicStrokeIndex, 0, MENU_STROKE_DRAWABLE.length - 1)]);
    }

    protected int getSubMenuChildLayout(int viewId) {
        switch (viewId) {
            case 0:
                return isPort() ? R.layout.paint_mosaic_container : R.layout.paint_mosaic_container_land;
            case 1:
                return isPort() ? R.layout.paint_brush_stroke_container : R.layout.paint_brush_stroke_container_land;
            default:
                return -1;
        }
    }

    protected int[] getSubMenuChildButtonsId(int viewId) {
        switch (viewId) {
            case 0:
                return SUB_MENU_MOSAIC_BUTTON_ID;
            case 1:
                return SUB_STROKE_LEVEL_BUTTON_ID;
            default:
                return new int[0];
        }
    }

    protected void selectSubMenuChildButton(int viewId) {
        int buttonIndex = -1;
        switch (viewId) {
            case 0:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.mosaicShapeIndex, 0, SUB_MENU_MOSAIC_BUTTON_ID.length - 1);
                break;
            case 1:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.mosaicStrokeIndex, 0, SUB_STROKE_LEVEL_BUTTON_ID.length - 1);
                break;
        }
        if (buttonIndex == -1) {
            GalleryLog.d("PaintMosaicBar", "PaintMosaicBar buttonIndex not exised");
            return;
        }
        int[] buttonId = getSubMenuChildButtonsId(viewId);
        if (buttonId == null) {
            GalleryLog.d("PaintMosaicBar", "PaintMosaicBar buttonId not exised");
            return;
        }
        if (buttonIndex < buttonId.length) {
            onClick(findViewById(buttonId[buttonIndex]));
        }
    }

    protected int getSubMenuChildRootId(int viewId) {
        switch (viewId) {
            case 0:
                return R.id.menu_mosaic_root;
            case 1:
                return R.id.brush_stroke_root;
            default:
                return -1;
        }
    }

    protected void processClickView(View v) {
        int viewId = v.getId();
        int index = EditorUtils.indexOf(SUB_MENU_MOSAIC_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.mosaicShapeIndex = index;
            v.setSelected(true);
            for (int id : SUB_MENU_MOSAIC_BUTTON_ID) {
                if (id != viewId) {
                    findViewById(id).setSelected(false);
                }
            }
            ((EditorTextView) this.mBasePaintMenu.findViewById(0)).updateDrawable(MENU_MOSAIC_DRAWABLE[index]);
            ReportToBigData.report(124, String.format("{MosaicType:%s}", new Object[]{Integer.valueOf(index)}));
            return;
        }
        index = EditorUtils.indexOf(SUB_STROKE_LEVEL_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.mosaicStrokeIndex = index;
            v.setSelected(true);
            for (int id2 : SUB_STROKE_LEVEL_BUTTON_ID) {
                if (id2 != viewId) {
                    findViewById(id2).setSelected(false);
                }
            }
            ((EditorTextView) this.mBasePaintMenu.findViewById(1)).updateDrawable(MENU_STROKE_DRAWABLE[index]);
            ReportToBigData.report(125, String.format("{MosaicStroke:%s}", new Object[]{Integer.valueOf(index)}));
        }
    }
}
