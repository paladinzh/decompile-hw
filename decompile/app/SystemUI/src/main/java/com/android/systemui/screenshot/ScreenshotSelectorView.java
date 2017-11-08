package com.android.systemui.screenshot;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

public class ScreenshotSelectorView extends View {
    private final Paint mPaintBackground;
    private final Paint mPaintSelection;
    private Rect mSelectionRect;
    private Point mStartPoint;

    public ScreenshotSelectorView(Context context) {
        this(context, null);
    }

    public ScreenshotSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mPaintBackground = new Paint(-16777216);
        this.mPaintBackground.setAlpha(160);
        this.mPaintSelection = new Paint(0);
        this.mPaintSelection.setXfermode(new PorterDuffXfermode(Mode.CLEAR));
    }

    public void startSelection(int x, int y) {
        this.mStartPoint = new Point(x, y);
        this.mSelectionRect = new Rect(x, y, x, y);
    }

    public void updateSelection(int x, int y) {
        if (this.mSelectionRect != null) {
            this.mSelectionRect.left = Math.min(this.mStartPoint.x, x);
            this.mSelectionRect.right = Math.max(this.mStartPoint.x, x);
            this.mSelectionRect.top = Math.min(this.mStartPoint.y, y);
            this.mSelectionRect.bottom = Math.max(this.mStartPoint.y, y);
            invalidate();
        }
    }

    public Rect getSelectionRect() {
        return this.mSelectionRect;
    }

    public void stopSelection() {
        this.mStartPoint = null;
        this.mSelectionRect = null;
    }

    public void draw(Canvas canvas) {
        canvas.drawRect((float) this.mLeft, (float) this.mTop, (float) this.mRight, (float) this.mBottom, this.mPaintBackground);
        if (this.mSelectionRect != null) {
            canvas.drawRect(this.mSelectionRect, this.mPaintSelection);
        }
    }
}
