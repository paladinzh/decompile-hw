package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.tools.EditorUtils;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class PaintBrushBar extends BasePaintBar {
    private static final int[] MENU_COLOR_DRAWABLE_LABEL = new int[]{R.drawable.btn_menu_color_label_1, R.drawable.btn_menu_color_label_2, R.drawable.btn_menu_color_label_3, R.drawable.btn_menu_color_label_4, R.drawable.btn_menu_color_label_5, R.drawable.btn_menu_color_label_6, R.drawable.btn_menu_color_label_7, R.drawable.btn_menu_color_label_8, R.drawable.btn_menu_color_label_9};
    private static final int[] MENU_SHAPE_DRAWABLE = new int[]{R.drawable.ic_gallery_edit_pen_free, R.drawable.ic_gallery_edit_pen_arrow, R.drawable.ic_gallery_edit_pen_line, R.drawable.ic_gallery_edit_pen_rectangle, R.drawable.ic_gallery_edit_pen_circle};
    private static final int[] SUB_MENU_COLOR_BUTTON_ID = new int[]{R.id.menu_color_1, R.id.menu_color_2, R.id.menu_color_3, R.id.menu_color_4, R.id.menu_color_5, R.id.menu_color_6, R.id.menu_color_7, R.id.menu_color_8, R.id.menu_color_9};
    @SuppressWarnings({"MS_MUTABLE_ARRAY"})
    public static final int[] SUB_MENU_COLOR_VALUE = new int[]{Color.parseColor("#F2914A"), Color.parseColor("#FFF45C"), Color.parseColor("#22AD38"), Color.parseColor("#00B8EE"), Color.parseColor("#5F52A1"), Color.parseColor("#000000"), Color.parseColor("#DC3374"), Color.parseColor("#EF7175"), Color.parseColor("#E5E5E5")};
    private static final int[] SUB_MENU_SHAPE_BUTTON_ID = new int[]{R.id.free_line_button, R.id.arrow_button, R.id.line_button, R.id.square_button, R.id.circle_button};

    public PaintBrushBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getEraseButtonId() {
        return 3;
    }

    protected void initMenuButtonImageSource() {
        ((EditorTextView) this.mBasePaintMenu.findViewById(0)).updateDrawable(MENU_SHAPE_DRAWABLE[Utils.clamp(EditorUtils.sEditorBrushData.brushShapeIndex, 0, MENU_SHAPE_DRAWABLE.length - 1)]);
        ((EditorTextView) this.mBasePaintMenu.findViewById(1)).updateDrawable(MENU_COLOR_DRAWABLE_LABEL[Utils.clamp(EditorUtils.sEditorBrushData.brushColorIndex, 0, MENU_COLOR_DRAWABLE_LABEL.length - 1)]);
        ((EditorTextView) this.mBasePaintMenu.findViewById(2)).updateDrawable(MENU_STROKE_DRAWABLE[Utils.clamp(EditorUtils.sEditorBrushData.brushStrokeIndex, 0, MENU_STROKE_DRAWABLE.length - 1)]);
    }

    protected int getSubMenuChildLayout(int viewId) {
        switch (viewId) {
            case 0:
                return isPort() ? R.layout.paint_brush_shape_container : R.layout.paint_brush_shape_container_land;
            case 1:
                return isPort() ? R.layout.paint_brush_color_container : R.layout.paint_brush_color_container_land;
            case 2:
                return isPort() ? R.layout.paint_brush_stroke_container : R.layout.paint_brush_stroke_container_land;
            default:
                return -1;
        }
    }

    protected int[] getSubMenuChildButtonsId(int viewId) {
        switch (viewId) {
            case 0:
                return SUB_MENU_SHAPE_BUTTON_ID;
            case 1:
                return SUB_MENU_COLOR_BUTTON_ID;
            case 2:
                return SUB_STROKE_LEVEL_BUTTON_ID;
            default:
                return new int[0];
        }
    }

    protected void selectSubMenuChildButton(int viewId) {
        int buttonIndex = -1;
        switch (viewId) {
            case 0:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.brushShapeIndex, 0, SUB_MENU_SHAPE_BUTTON_ID.length - 1);
                break;
            case 1:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.brushColorIndex, 0, SUB_MENU_COLOR_BUTTON_ID.length - 1);
                break;
            case 2:
                buttonIndex = Utils.clamp(EditorUtils.sEditorBrushData.brushStrokeIndex, 0, SUB_STROKE_LEVEL_BUTTON_ID.length - 1);
                break;
        }
        if (buttonIndex == -1) {
            GalleryLog.d("PaintBrushBar", "PaintBrushBar buttonIndex not exised");
            return;
        }
        int[] buttonId = getSubMenuChildButtonsId(viewId);
        if (buttonId == null) {
            GalleryLog.d("PaintBrushBar", "PaintBrushBar buttonId not exised");
            return;
        }
        if (buttonIndex < buttonId.length) {
            onClick(findViewById(buttonId[buttonIndex]));
        }
    }

    protected int getSubMenuChildRootId(int viewId) {
        switch (viewId) {
            case 0:
                return R.id.brush_shape_root;
            case 1:
                return R.id.menu_color_root;
            case 2:
                return R.id.brush_stroke_root;
            default:
                return -1;
        }
    }

    protected void processClickView(View v) {
        int viewId = v.getId();
        int index = EditorUtils.indexOf(SUB_MENU_SHAPE_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.brushShapeIndex = index;
            v.setSelected(true);
            for (int id : SUB_MENU_SHAPE_BUTTON_ID) {
                if (id != viewId) {
                    findViewById(id).setSelected(false);
                }
            }
            ((EditorTextView) this.mBasePaintMenu.findViewById(0)).updateDrawable(MENU_SHAPE_DRAWABLE[index]);
            ReportToBigData.report(127, String.format("{BrushType:%s}", new Object[]{Integer.valueOf(index)}));
            return;
        }
        index = EditorUtils.indexOf(SUB_MENU_COLOR_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.brushColorIndex = index;
            v.setSelected(true);
            for (int id2 : SUB_MENU_COLOR_BUTTON_ID) {
                if (id2 != viewId) {
                    findViewById(id2).setSelected(false);
                }
            }
            ((EditorTextView) this.mBasePaintMenu.findViewById(1)).updateDrawable(MENU_COLOR_DRAWABLE_LABEL[index]);
            ReportToBigData.report(128, String.format("{BrushColor:%s}", new Object[]{Integer.valueOf(index)}));
            return;
        }
        index = EditorUtils.indexOf(SUB_STROKE_LEVEL_BUTTON_ID, viewId);
        if (index != -1) {
            EditorUtils.sEditorBrushData.brushStrokeIndex = index;
            v.setSelected(true);
            for (int id22 : SUB_STROKE_LEVEL_BUTTON_ID) {
                if (id22 != viewId) {
                    findViewById(id22).setSelected(false);
                }
            }
            ((EditorTextView) this.mBasePaintMenu.findViewById(2)).updateDrawable(MENU_STROKE_DRAWABLE[index]);
            ReportToBigData.report(129, String.format("{BrushStroke:%s}", new Object[]{Integer.valueOf(index)}));
        }
    }
}
