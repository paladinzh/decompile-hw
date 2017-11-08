package com.huawei.gallery.editor.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.NinePatchDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.android.gallery3d.R;
import com.huawei.gallery.util.ColorfulUtils;

public class LabelIconView extends ImageView {
    public LabelIconView(Context context) {
        super(context);
    }

    public LabelIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isSelected()) {
            NinePatchDrawable frame = (NinePatchDrawable) ColorfulUtils.mappingColorfulDrawableForce(getContext(), R.drawable.pic_frame_selected);
            frame.setBounds(0, 0, getWidth(), getHeight());
            frame.draw(canvas);
        }
    }
}
