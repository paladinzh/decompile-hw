package com.android.settings.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.util.AttributeSet;
import android.view.View;
import com.android.internal.R;
import com.android.internal.util.Preconditions;
import com.android.settings.R$styleable;

public class ChartGridView extends View {
    private Drawable mBorder;
    private ChartAxis mHoriz;
    private int mLabelColor;
    private Layout mLabelEnd;
    private Layout mLabelMid;
    private int mLabelSize;
    private Layout mLabelStart;
    private Drawable mPrimary;
    private Drawable mSecondary;
    private ChartAxis mVert;

    public ChartGridView(Context context) {
        this(context, null, 0);
    }

    public ChartGridView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ChartGridView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setWillNotDraw(false);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.ChartGridView, defStyle, 0);
        this.mPrimary = a.getDrawable(2);
        this.mSecondary = a.getDrawable(3);
        this.mBorder = a.getDrawable(4);
        TypedArray ta = context.obtainStyledAttributes(a.getResourceId(0, -1), R.styleable.TextAppearance);
        this.mLabelSize = ta.getDimensionPixelSize(0, 0);
        ta.recycle();
        this.mLabelColor = a.getColorStateList(1).getDefaultColor();
        a.recycle();
    }

    void init(ChartAxis horiz, ChartAxis vert) {
        this.mHoriz = (ChartAxis) Preconditions.checkNotNull(horiz, "missing horiz");
        this.mVert = (ChartAxis) Preconditions.checkNotNull(vert, "missing vert");
    }

    protected void onDraw(Canvas canvas) {
        int width = getWidth();
        int height = getHeight() - getPaddingBottom();
        Drawable secondary = this.mSecondary;
        if (secondary != null) {
            int secondaryHeight = secondary.getIntrinsicHeight();
            for (float y : this.mVert.getTickPoints()) {
                secondary.setBounds(0, (int) y, width, (int) Math.min(((float) secondaryHeight) + y, (float) height));
                secondary.draw(canvas);
            }
        }
        Drawable primary = this.mPrimary;
        if (primary != null) {
            int primaryWidth = primary.getIntrinsicWidth();
            int primaryHeight = primary.getIntrinsicHeight();
            for (float x : this.mHoriz.getTickPoints()) {
                primary.setBounds((int) x, 0, (int) Math.min(((float) primaryWidth) + x, (float) width), height);
                primary.draw(canvas);
            }
        }
        this.mBorder.setBounds(0, 0, width, height);
        this.mBorder.draw(canvas);
        int padding = this.mLabelStart != null ? this.mLabelStart.getHeight() / 8 : 0;
        Layout start = this.mLabelStart;
        if (start != null) {
            int saveCount = canvas.save();
            canvas.translate(0.0f, (float) (height + padding));
            start.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
        Layout mid = this.mLabelMid;
        if (mid != null) {
            saveCount = canvas.save();
            canvas.translate((float) ((width - mid.getWidth()) / 2), (float) (height + padding));
            mid.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
        Layout end = this.mLabelEnd;
        if (end != null) {
            saveCount = canvas.save();
            canvas.translate((float) (width - end.getWidth()), (float) (height + padding));
            end.draw(canvas);
            canvas.restoreToCount(saveCount);
        }
    }
}
