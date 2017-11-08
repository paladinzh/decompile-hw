package com.android.gallery3d.gadget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.RemotableViewMethod;
import android.widget.ImageView;
import android.widget.RemoteViews.RemoteView;

@RemoteView
public class WidgetImageView extends ImageView {
    public WidgetImageView(Context context) {
        super(context);
    }

    public WidgetImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public WidgetImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @RemotableViewMethod
    public void setImageResource(int resId) {
        super.setImageResource(resId);
    }

    @RemotableViewMethod
    public void setImageBitmap(Bitmap bm) {
        if (bm != null && !bm.isRecycled()) {
            super.setImageBitmap(bm);
        }
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }
}
