package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.ui.GLRoot;
import com.huawei.gallery.editor.glrender.BaseRender.EditorViewDelegate;
import com.huawei.gallery.util.ColorfulUtils;

public class ScreenShotsPreviewUIController extends EditorUIController {
    private static final SparseArray<AspectInfo> sAspects = new SparseArray();
    private final Listener mListener;
    private int mSelectedViewId = -1;

    public interface Listener extends com.huawei.gallery.editor.ui.EditorUIController.Listener {
        void changeActionBar();

        OnClickListener getClickListener();

        GLRoot getGLRoot();

        void onClickIcon(View view, boolean z);
    }

    public ScreenShotsPreviewUIController(Context context, ViewGroup parentLayout, Listener listener, EditorViewDelegate EditorViewDelegate) {
        super(context, parentLayout, listener, EditorViewDelegate);
        this.mListener = listener;
    }

    static {
        sAspects.put(R.id.textview_screenshots_cut, new AspectInfo(R.drawable.ic_gallery_edit_crop, R.drawable.ic_gallery_edit_crop, -1, -1));
        sAspects.put(R.id.textview_screenshots_mosaic, new AspectInfo(R.drawable.ic_gallery_edit_mosaic, R.drawable.ic_gallery_edit_mosaic, 0, 0));
        sAspects.put(R.id.textview_screenshots_brush, new AspectInfo(R.drawable.ic_gallery_edit_pen, R.drawable.ic_gallery_edit_pen, 0, 0));
        sAspects.put(R.id.textview_screenshots_eraser, new AspectInfo(R.drawable.ic_gallery_edit_eraser, R.drawable.ic_gallery_edit_eraser, 1, 1));
    }

    protected int getControlLayoutId() {
        return R.layout.screenshots_editor_controls;
    }

    public void show() {
        boolean firstShow;
        if (this.mContainer == null) {
            firstShow = true;
        } else {
            firstShow = false;
        }
        if (firstShow) {
            super.show();
            this.mListener.changeActionBar();
        }
        setSelectedAspect(null, false);
        this.mContainer.post(new Runnable() {
            public void run() {
                GLRoot root = ScreenShotsPreviewUIController.this.mListener.getGLRoot();
                if (root != null) {
                    root.lockRenderThread();
                    try {
                        ViewGroup container = ScreenShotsPreviewUIController.this.mContainer;
                        if (container != null) {
                            ScreenShotsPreviewUIController.this.mListener.onClickIcon(container.findViewById(R.id.textview_screenshots_cut), true);
                        }
                        root.unlockRenderThread();
                    } catch (Throwable th) {
                        root.unlockRenderThread();
                    }
                }
            }
        });
    }

    public void hide() {
    }

    protected boolean hasFootAction() {
        return false;
    }

    protected void inflateFootLayout() {
        super.inflateFootLayout();
        for (int index = 0; index < sAspects.size(); index++) {
            this.mContainer.findViewById(sAspects.keyAt(index)).setOnClickListener(this.mListener.getClickListener());
        }
        this.mSelectedViewId = -1;
    }

    protected int getFootLayout() {
        return this.mEditorViewDelegate.isPort() ? R.layout.screenshots_foot_bar : R.layout.screenshots_foot_bar_land;
    }

    public void onLeaveEditor() {
        this.mParentLayout.removeView(this.mContainer);
        this.mContainer = null;
    }

    public void updateContainerLayoutParams() {
        if (this.mContainer != null) {
            LayoutParams params = (LayoutParams) this.mContainer.getLayoutParams();
            int bottomPadding = 0;
            if (this.mContainer.getResources().getConfiguration().orientation == 2) {
                params.leftMargin = 0;
                params.rightMargin = this.mEditorViewDelegate.getNavigationBarHeight();
            } else {
                params.leftMargin = 0;
                params.rightMargin = 0;
                bottomPadding = this.mEditorViewDelegate.getNavigationBarHeight();
            }
            this.mContainer.setLayoutParams(params);
            this.mContainer.setPadding(this.mContainer.getPaddingLeft(), this.mContainer.getPaddingTop(), this.mContainer.getPaddingRight(), bottomPadding);
        }
    }

    public boolean setSelectedAspect(View view, boolean force) {
        int id = view == null ? -1 : view.getId();
        if (this.mSelectedViewId == id && !force) {
            return false;
        }
        this.mSelectedViewId = id;
        for (int index = 0; index < sAspects.size(); index++) {
            int key = sAspects.keyAt(index);
            AspectInfo info = (AspectInfo) sAspects.get(key);
            TextView textView;
            if (key != id || view == null) {
                textView = (TextView) this.mContainer.findViewById(key);
                textView.setCompoundDrawablesWithIntrinsicBounds(0, info.drawableId, 0, 0);
                textView.setSelected(false);
            } else {
                textView = (TextView) view;
                textView.setCompoundDrawablesWithIntrinsicBounds(null, ColorfulUtils.mappingColorfulDrawableForce(this.mContext, info.pressDrawableId), null, null);
                textView.setSelected(true);
            }
        }
        return true;
    }

    protected void saveUIController() {
        this.mTransitionStore.put("menu_select_index", Integer.valueOf(this.mSelectedViewId));
    }

    protected void restoreUIController() {
        this.mListener.onClickIcon(this.mContainer.findViewById(((Integer) this.mTransitionStore.get("menu_select_index")).intValue()), false);
    }
}
