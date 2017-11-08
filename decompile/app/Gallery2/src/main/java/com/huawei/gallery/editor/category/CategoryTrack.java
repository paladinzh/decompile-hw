package com.huawei.gallery.editor.category;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.gallery3d.R;
import com.android.gallery3d.R$styleable;

public class CategoryTrack extends BaseViewTrack {
    private int mElemHeight;
    private int mElemWidth;

    public CategoryTrack(Context context) {
        super(context);
        this.mElemWidth = context.getResources().getDimensionPixelSize(R.dimen.category_panel_item_width);
        this.mElemHeight = context.getResources().getDimensionPixelSize(R.dimen.category_panel_item_width);
    }

    public CategoryTrack(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.CategoryTrack);
        this.mElemWidth = a.getDimensionPixelSize(3, 0);
        this.mElemHeight = a.getDimensionPixelSize(4, 0);
        a.recycle();
    }

    public void fillContent() {
        if (this.mAdapter != null && (this.mAdapter instanceof CategoryAdapter)) {
            ((CategoryAdapter) this.mAdapter).setItemWidth(this.mElemWidth);
            ((CategoryAdapter) this.mAdapter).setItemHeight(this.mElemHeight);
        }
        super.fillContent();
    }

    public void invalidate(View view) {
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                invalidate(((ViewGroup) view).getChildAt(i));
            }
            return;
        }
        view.invalidate();
    }

    public void invalidate() {
        invalidate(this);
    }
}
