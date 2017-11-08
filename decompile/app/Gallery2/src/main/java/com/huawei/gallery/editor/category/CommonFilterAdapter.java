package com.huawei.gallery.editor.category;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import com.android.gallery3d.util.GalleryLog;

public class CommonFilterAdapter extends CategoryAdapter {
    public CommonFilterAdapter(Context context) {
        super(context);
    }

    public CommonFilterAdapter(Context context, BaseViewAdapter baseViewAdapter) {
        super(context, baseViewAdapter);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            GalleryLog.v("CommonFilterAdapter", "CommonFilterAdapter set view is list");
            convertView = new CategoryView(getContext());
        }
        CategoryView view = (CategoryView) convertView;
        view.setOrientation(this.mOrientation);
        view.setAction((Action) getItem(position), this);
        view.setLayoutParams(new LayoutParams(this.mItemWidth, this.mItemHeight));
        view.setTag(Integer.valueOf(position));
        invalidate(view);
        return view;
    }
}
