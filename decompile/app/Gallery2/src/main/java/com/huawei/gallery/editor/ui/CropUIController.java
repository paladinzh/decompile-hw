package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.amap.api.maps.model.GroundOverlayOptions;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.EditorTextView;
import com.huawei.gallery.editor.category.IconData;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.editor.tools.EditorUtils;
import com.huawei.gallery.editor.ui.EditorUIController.Listener;
import com.huawei.gallery.util.ColorfulUtils;

public class CropUIController extends EditorUIController {
    protected static final SparseArray<AspectInfo> sAspects = new SparseArray();
    private OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            CropUIController.this.setSelectedAspect(view, false);
        }
    };
    private CropListener mCropListener;
    private int[] mCropTextViewId;
    private int mSelectedViewId = -1;

    public interface CropListener extends Listener {
        void onClick(float f, boolean z, String str);
    }

    private enum CropAction {
        FREE,
        ONE_ONE,
        SIXTEEN_NINE,
        NINE_SIXTEEN,
        FOUR_THREE,
        THREE_FOUR,
        THREE_TWO,
        TWO_THREE
    }

    public CropUIController(Context context, ViewGroup parentLayout, CropListener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mCropListener = listener;
        this.mCropTextViewId = new int[sAspects.size()];
        for (int i = 0; i < sAspects.size(); i++) {
            this.mCropTextViewId[i] = sAspects.keyAt(i);
        }
    }

    static {
        sAspects.put(CropAction.FREE.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_free, R.drawable.ic_gallery_edit_crop_free, R.string.aspectNone_effect, -1, 1));
        sAspects.put(CropAction.ONE_ONE.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_1_1, R.drawable.ic_gallery_edit_crop_1_1, 0, 1, 1));
        sAspects.put(CropAction.SIXTEEN_NINE.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_16_9, R.drawable.ic_gallery_edit_crop_16_9, 0, 16, 9));
        sAspects.put(CropAction.NINE_SIXTEEN.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_9_16, R.drawable.ic_gallery_edit_crop_9_16, 0, 9, 16));
        sAspects.put(CropAction.FOUR_THREE.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_4_3, R.drawable.ic_gallery_edit_crop_4_3, 0, 4, 3));
        sAspects.put(CropAction.THREE_FOUR.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_3_4, R.drawable.ic_gallery_edit_crop_3_4, 0, 3, 4));
        sAspects.put(CropAction.THREE_TWO.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_3_2, R.drawable.ic_gallery_edit_crop_3_2, 0, 3, 2));
        sAspects.put(CropAction.TWO_THREE.ordinal(), new AspectInfo(R.drawable.ic_gallery_edit_crop_2_3, R.drawable.ic_gallery_edit_crop_2_3, 0, 2, 3));
    }

    public void show() {
        super.show();
        setSelectedAspect(this.mContainer.findViewById(CropAction.FREE.ordinal()), true);
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        initCropContainer();
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.editor_crop_foot_bar : R.layout.editor_crop_foot_bar_land;
    }

    private void initCropContainer() {
        LinearLayout cropLinearlayout = (LinearLayout) this.mContainer.findViewById(R.id.state_root);
        for (int i = 0; i < sAspects.size(); i++) {
            EditorTextView editorTextView = new EditorTextView(this.mContext);
            editorTextView.setId(EditorUtils.getViewId(i));
            if (((AspectInfo) sAspects.get(sAspects.keyAt(i))).textId == 0) {
                editorTextView.setAttributes(new IconData(sAspects.keyAt(i), ((AspectInfo) sAspects.get(sAspects.keyAt(i))).drawableId, String.format("%d:%d", new Object[]{Integer.valueOf(((AspectInfo) sAspects.get(sAspects.keyAt(i))).aspectX), Integer.valueOf(((AspectInfo) sAspects.get(sAspects.keyAt(i))).aspectY)})), i, sAspects.size());
            } else {
                editorTextView.setAttributes(new IconData(sAspects.keyAt(i), ((AspectInfo) sAspects.get(sAspects.keyAt(i))).drawableId, ((AspectInfo) sAspects.get(sAspects.keyAt(i))).textId), i, sAspects.size());
            }
            editorTextView.setOnClickListener(this.mClickListener);
            editorTextView.setTextColor(AspectInfo.getColor());
            cropLinearlayout.addView(editorTextView);
        }
    }

    private void setSelectedAspect(View view, boolean force) {
        int id = view.getId();
        if (this.mSelectedViewId != id || force) {
            this.mSelectedViewId = id;
            for (int index = 0; index < sAspects.size(); index++) {
                int key = sAspects.keyAt(index);
                AspectInfo info = (AspectInfo) sAspects.get(key);
                TextView textView;
                if (key == id) {
                    float ratio;
                    textView = (TextView) view;
                    textView.setCompoundDrawablesWithIntrinsicBounds(null, ColorfulUtils.mappingColorfulDrawableForce(this.mContext, info.pressDrawableId), null, null);
                    textView.setSelected(true);
                    if (force) {
                        ratio = GroundOverlayOptions.NO_DIMENSION;
                    } else {
                        ratio = ((float) info.aspectX) / ((float) info.aspectY);
                    }
                    String reportMsg = null;
                    if (textView.getText() != null) {
                        reportMsg = textView.getText().toString();
                    }
                    if (this.mContext.getString(R.string.aspectNone_effect).equalsIgnoreCase(reportMsg)) {
                        reportMsg = "aspectFree";
                    }
                    this.mCropListener.onClick(ratio, force, reportMsg);
                } else {
                    textView = (TextView) this.mContainer.findViewById(key);
                    textView.setCompoundDrawablesWithIntrinsicBounds(0, info.drawableId, 0, 0);
                    textView.setSelected(false);
                }
            }
        }
    }

    protected void onHideFootAnimeEnd() {
        super.onHideFootAnimeEnd();
        this.mEditorViewDelegate.invalidate();
    }

    protected void saveUIController() {
        this.mTransitionStore.put("menu_select_index", Integer.valueOf(this.mSelectedViewId));
    }

    protected void restoreUIController() {
        int id = ((Integer) this.mTransitionStore.get("menu_select_index")).intValue();
        TextView textView = (TextView) this.mContainer.findViewById(id);
        AspectInfo info = (AspectInfo) sAspects.get(id);
        textView.setSelected(true);
        textView.setCompoundDrawablesWithIntrinsicBounds(null, ColorfulUtils.mappingColorfulDrawableForce(this.mContext, info.pressDrawableId), null, null);
    }
}
