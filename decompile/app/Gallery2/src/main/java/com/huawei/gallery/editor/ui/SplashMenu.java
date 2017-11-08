package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import com.android.gallery3d.R;
import com.huawei.gallery.editor.category.IconData;

public class SplashMenu extends BasePaintMenu {
    private MenuClickListener mListener;
    private boolean mLock = false;

    public interface MenuClickListener {
        void onStyleChange(int i);
    }

    public void lock() {
        this.mLock = true;
    }

    public void unLock() {
        this.mLock = false;
    }

    public SplashMenu(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void updateIconDataContainer() {
        this.mIconDataContainer.put(0, new IconData(0, (int) R.drawable.ic_gallery_edit_eraser, (int) R.string.simple_editor_eraser));
    }

    public void initialize(MenuClickListener listener) {
        this.mListener = listener;
    }

    public void onClick(View view) {
        if (!this.mLock && this.mListener != null) {
            super.onClick(view);
            this.mListener.onStyleChange(view.isSelected() ? 1 : 2);
        }
    }

    public boolean inOpenState() {
        return findViewById(0).isSelected();
    }

    public void close() {
        View view = findViewById(0);
        if (view.isSelected()) {
            onClick(view);
        }
    }
}
