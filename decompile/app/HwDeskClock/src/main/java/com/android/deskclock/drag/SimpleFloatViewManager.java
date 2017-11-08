package com.android.deskclock.drag;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Point;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.deskclock.drag.DragSortListView.FloatViewManager;

public class SimpleFloatViewManager implements FloatViewManager {
    private Bitmap mFloatBitmap;
    private ImageView mImageView;
    private ListView mListView;

    public SimpleFloatViewManager(ListView lv) {
        this.mListView = lv;
    }

    public View onCreateFloatView(int position) {
        View v = this.mListView.getChildAt((this.mListView.getHeaderViewsCount() + position) - this.mListView.getFirstVisiblePosition());
        if (v == null) {
            return null;
        }
        v.setPressed(false);
        this.mFloatBitmap = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Config.ARGB_8888);
        v.draw(new Canvas(this.mFloatBitmap));
        v.setBackgroundColor(Color.parseColor("#00000000"));
        if (this.mImageView == null) {
            this.mImageView = new ImageView(this.mListView.getContext());
        }
        this.mImageView.setBackgroundColor(Color.parseColor("#12ffffff"));
        this.mImageView.setPadding(0, 0, 0, 0);
        this.mImageView.setImageBitmap(this.mFloatBitmap);
        this.mImageView.setLayoutParams(new LayoutParams(v.getWidth(), v.getHeight()));
        return this.mImageView;
    }

    public void onDragFloatView(View floatView, Point position, Point touch) {
    }

    public void onDestroyFloatView(View floatView) {
        ((ImageView) floatView).setImageDrawable(null);
        if (this.mFloatBitmap != null) {
            this.mFloatBitmap.recycle();
            this.mFloatBitmap = null;
        }
    }
}
