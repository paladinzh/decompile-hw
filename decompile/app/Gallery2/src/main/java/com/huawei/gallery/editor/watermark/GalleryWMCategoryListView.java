package com.huawei.gallery.editor.watermark;

import android.content.Context;
import android.widget.RelativeLayout.LayoutParams;
import com.android.gallery3d.R;
import com.huawei.watermark.ui.watermarklib.WMCategoryListView;

public class GalleryWMCategoryListView extends WMCategoryListView {
    private Listener mListener;
    private boolean mNeedAnimation = true;

    public interface Listener {
        void onMenuPrepared();
    }

    public synchronized void setListener(Listener l) {
        this.mListener = l;
    }

    public GalleryWMCategoryListView(Context context) {
        super(context);
    }

    public void setLayoutParams() {
        LayoutParams rl = new LayoutParams(getContext().getResources().getDimensionPixelSize(R.dimen.watermark_locallibpage_categorynamelist_width), getContext().getResources().getDimensionPixelSize(R.dimen.watermark_locallibpage_categorynamelist_height));
        rl.addRule(14);
        rl.addRule(12);
        setLayoutParams(rl);
    }

    public void setShowType() {
        this.mShowType = 2;
    }

    protected synchronized void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        if (this.mNeedAnimation && getChildCount() > 0) {
            this.mNeedAnimation = false;
            if (this.mListener != null) {
                this.mListener.onMenuPrepared();
            }
        }
    }

    public synchronized void needAnimation() {
        this.mNeedAnimation = true;
        requestLayout();
    }
}
