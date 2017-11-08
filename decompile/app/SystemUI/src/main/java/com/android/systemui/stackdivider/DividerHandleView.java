package com.android.systemui.stackdivider;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Property;
import android.view.View;
import com.android.systemui.R;

public class DividerHandleView extends View {
    private static final Property<DividerHandleView, Integer> HEIGHT_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "height") {
        public Integer get(DividerHandleView object) {
            return Integer.valueOf(object.mCurrentHeight);
        }

        public void set(DividerHandleView object, Integer value) {
            object.mCurrentHeight = value.intValue();
            object.invalidate();
        }
    };
    private static final Property<DividerHandleView, Integer> WIDTH_PROPERTY = new Property<DividerHandleView, Integer>(Integer.class, "width") {
        public Integer get(DividerHandleView object) {
            return Integer.valueOf(object.mCurrentWidth);
        }

        public void set(DividerHandleView object, Integer value) {
            object.mCurrentWidth = value.intValue();
            object.invalidate();
        }
    };
    private final int mCircleDiameter;
    private int mCurrentHeight;
    private int mCurrentWidth;
    private final int mHeight;
    private final Paint mPaint = new Paint();
    private final int mWidth;

    public DividerHandleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPaint.setColor(getResources().getColor(R.color.docked_divider_handle, null));
        this.mPaint.setAntiAlias(true);
        this.mWidth = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_width);
        this.mHeight = getResources().getDimensionPixelSize(R.dimen.docked_divider_handle_height);
        this.mCurrentWidth = this.mWidth;
        this.mCurrentHeight = this.mHeight;
        this.mCircleDiameter = (this.mWidth + this.mHeight) / 3;
    }

    protected void onDraw(Canvas canvas) {
        int left = (getWidth() / 2) - (this.mCurrentWidth / 2);
        int top = (getHeight() / 2) - (this.mCurrentHeight / 2);
        int radius = Math.min(this.mCurrentWidth, this.mCurrentHeight) / 2;
        canvas.drawRoundRect((float) left, (float) top, (float) (this.mCurrentWidth + left), (float) (this.mCurrentHeight + top), (float) radius, (float) radius, this.mPaint);
    }

    public boolean hasOverlappingRendering() {
        return false;
    }
}
