package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.android.gallery3d.app.TransitionStore;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.editor.tools.EditorConstant;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.BasePaintBar.UIListener;
import edu.umd.cs.findbugs.annotations.SuppressWarnings;

public class LabelBar extends BasePaintBar {
    @SuppressWarnings({"MS_MUTABLE_ARRAY"})
    public static final LabelPainData[] LABEL_PAIN_DATAS = new LabelPainData[]{new LabelPainData(0, PaintBrushBar.SUB_MENU_COLOR_VALUE[8]), new LabelPainData(R.drawable.textbox_0, PaintBrushBar.SUB_MENU_COLOR_VALUE[8]), new LabelPainData(R.drawable.textbox_1, PaintBrushBar.SUB_MENU_COLOR_VALUE[4]), new LabelPainData(R.drawable.textbox_2, PaintBrushBar.SUB_MENU_COLOR_VALUE[4]), new LabelPainData(R.drawable.textbox_3, PaintBrushBar.SUB_MENU_COLOR_VALUE[7]), new LabelPainData(R.drawable.textbox_4, PaintBrushBar.SUB_MENU_COLOR_VALUE[0]), new LabelPainData(R.drawable.textbox_5, PaintBrushBar.SUB_MENU_COLOR_VALUE[2]), new LabelPainData(R.drawable.textbox_6, PaintBrushBar.SUB_MENU_COLOR_VALUE[7]), new LabelPainData(R.drawable.textbox_7, PaintBrushBar.SUB_MENU_COLOR_VALUE[0]), new LabelPainData(R.drawable.textbox_8, PaintBrushBar.SUB_MENU_COLOR_VALUE[3]), new LabelPainData(R.drawable.textbox_9, PaintBrushBar.SUB_MENU_COLOR_VALUE[8]), new LabelPainData(R.drawable.textbox_10, PaintBrushBar.SUB_MENU_COLOR_VALUE[6]), new LabelPainData(R.drawable.textbox_11, PaintBrushBar.SUB_MENU_COLOR_VALUE[5])};
    private static final int[] SUB_MENU_BUBBLE_BUTTON_ID = new int[]{R.id.noBubble, R.id.bubble_0, R.id.bubble_1, R.id.bubble_2, R.id.bubble_3, R.id.bubble_4, R.id.bubble_5, R.id.bubble_6, R.id.bubble_7, R.id.bubble_8, R.id.bubble_9, R.id.bubble_10, R.id.bubble_11};
    private static final int[] SUB_MENU_STYLE_BUTTON_ID = new int[]{R.id.menu_color_1, R.id.menu_color_2, R.id.menu_color_3, R.id.menu_color_4, R.id.menu_color_5, R.id.menu_color_6, R.id.menu_color_7, R.id.menu_color_8, R.id.menu_color_9, R.id.menu_bold, R.id.menu_italic, R.id.menu_shadow};
    private LabelBarDelegate mLabelBarDelegate;
    private LabelPainData mLabelPaintData = new LabelPainData();

    public interface LabelBarDelegate {
        boolean noBubbleChoosed();
    }

    public LabelBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected int getEraseButtonId() {
        GalleryLog.printDFXLog("U called stub method: label_data");
        return -1;
    }

    protected void initMenuButtonImageSource() {
        GalleryLog.printDFXLog("U called stub method: label_data");
    }

    protected int getSubMenuChildLayout(int viewId) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        switch (viewId) {
            case 0:
                return isPort() ? R.layout.label_bubble_container : R.layout.label_bubble_container_land;
            case 1:
                return isPort() ? R.layout.label_style_container : R.layout.label_style_container_land;
            default:
                return -1;
        }
    }

    protected int[] getSubMenuChildButtonsId(int viewId) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        switch (viewId) {
            case 0:
                return SUB_MENU_BUBBLE_BUTTON_ID;
            case 1:
                return SUB_MENU_STYLE_BUTTON_ID;
            default:
                return new int[0];
        }
    }

    protected void selectSubMenuChildButton(int viewId) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        switch (viewId) {
            case 0:
                updateSelectedBubbleView(this.mLabelPaintData);
                return;
            case 1:
                updateSelectedStyleView(this.mLabelPaintData);
                return;
            default:
                return;
        }
    }

    protected int getSubMenuChildRootId(int viewId) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        switch (viewId) {
            case 0:
                return R.id.itemScrollView;
            case 1:
                return R.id.menu_style_root;
            default:
                return -1;
        }
    }

    protected void processClickView(View v) {
        int viewId = v.getId();
        int index = EditorUtils.indexOf(SUB_MENU_BUBBLE_BUTTON_ID, viewId);
        if (index != -1) {
            if (this.mLabelBarDelegate == null || !this.mLabelBarDelegate.noBubbleChoosed()) {
                this.mLabelPaintData.iconRes = LABEL_PAIN_DATAS[index].iconRes;
                this.mLabelPaintData.color = LABEL_PAIN_DATAS[index].color;
            } else {
                this.mLabelPaintData = new LabelPainData(LABEL_PAIN_DATAS[index]);
            }
            updateSelectedBubbleView(this.mLabelPaintData);
            ReportToBigData.report(130, String.format("{LabelType:%s}", new Object[]{Integer.valueOf(index)}));
            return;
        }
        index = EditorUtils.indexOf(SUB_MENU_STYLE_BUTTON_ID, viewId);
        if (index != -1) {
            if (index < SUB_MENU_STYLE_BUTTON_ID.length - 3) {
                this.mLabelPaintData.color = PaintBrushBar.SUB_MENU_COLOR_VALUE[index];
                ReportToBigData.report(131, String.format("{LabelFontColor:%s}", new Object[]{Integer.valueOf(index)}));
            } else if (index == 9) {
                r5 = this.mLabelPaintData;
                if (v.isSelected()) {
                    r2 = false;
                } else {
                    r2 = true;
                }
                r5.bold = r2;
                r5 = "{LabelFontType:%s,Status:%s}";
                r6 = new Object[2];
                r6[0] = "BOLD";
                r6[1] = !v.isSelected() ? "On" : "Off";
                ReportToBigData.report(132, String.format(r5, r6));
            } else if (index == 10) {
                r5 = this.mLabelPaintData;
                if (v.isSelected()) {
                    r2 = false;
                } else {
                    r2 = true;
                }
                r5.italic = r2;
                r5 = "{LabelFontType:%s,Status:%s}";
                r6 = new Object[2];
                r6[0] = "ITALIC";
                r6[1] = !v.isSelected() ? "On" : "Off";
                ReportToBigData.report(132, String.format(r5, r6));
            } else if (index == 11) {
                r5 = this.mLabelPaintData;
                if (v.isSelected()) {
                    r2 = false;
                } else {
                    r2 = true;
                }
                r5.shadow = r2;
                r5 = "{LabelFontType:%s,Status:%s}";
                r6 = new Object[2];
                r6[0] = "SHADOW";
                r6[1] = !v.isSelected() ? "On" : "Off";
                ReportToBigData.report(132, String.format(r5, r6));
            }
            updateSelectedStyleView(this.mLabelPaintData);
        }
    }

    public int getSubMenuHeight() {
        GalleryLog.printDFXLog("U called stub method: label_data");
        return EditorConstant.SUB_MENU_HEIGHT_BIG;
    }

    public void initialize(UIListener uiListener) {
        super.initialize(uiListener);
        if (uiListener instanceof LabelBarDelegate) {
            this.mLabelBarDelegate = (LabelBarDelegate) uiListener;
        }
    }

    public void updateSelectedView(LabelPainData painData) {
        this.mLabelPaintData = new LabelPainData(painData);
        updateSelectedBubbleView(this.mLabelPaintData);
        updateSelectedStyleView(this.mLabelPaintData);
    }

    public LabelPainData getCurrentPaintData() {
        GalleryLog.printDFXLog("U called stub method: label_data");
        return this.mLabelPaintData;
    }

    private void updateSelectedBubbleView(LabelPainData painData) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        for (int index = 0; index < LABEL_PAIN_DATAS.length; index++) {
            View v = findViewById(SUB_MENU_BUBBLE_BUTTON_ID[index]);
            if (v != null) {
                v.setSelected(painData.canSelected(LABEL_PAIN_DATAS[index]));
            }
        }
    }

    private void updateSelectedStyleView(LabelPainData painData) {
        GalleryLog.printDFXLog("U called stub method: label_data");
        for (int index = 0; index < SUB_MENU_STYLE_BUTTON_ID.length; index++) {
            View v = findViewById(SUB_MENU_STYLE_BUTTON_ID[index]);
            if (v != null) {
                if (index == 9) {
                    v.setSelected(painData.bold);
                } else if (index == 10) {
                    v.setSelected(painData.italic);
                } else if (index == 11) {
                    v.setSelected(painData.shadow);
                } else {
                    v.setSelected(PaintBrushBar.SUB_MENU_COLOR_VALUE[index] == painData.color);
                }
            }
        }
    }

    protected void saveUIController(TransitionStore store) {
        store.put("label_data", this.mLabelPaintData);
        super.saveUIController(store);
    }

    protected void restoreUIController(TransitionStore store) {
        this.mLabelPaintData = (LabelPainData) store.get("label_data");
        super.restoreUIController(store);
    }
}
