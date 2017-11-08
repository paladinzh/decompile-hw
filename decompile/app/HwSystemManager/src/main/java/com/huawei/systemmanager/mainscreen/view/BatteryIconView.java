package com.huawei.systemmanager.mainscreen.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.R;

public class BatteryIconView extends ImageView {
    private Drawable mBatteryInner;
    private int mBatteryPercent;

    public BatteryIconView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBatteryOuter();
    }

    private void initBatteryOuter() {
        setImageResource(R.drawable.ic_phonemanager_powersaving_outer);
    }

    public void setBatteryPercent(int percent) {
        this.mBatteryPercent = percent;
        invalidate();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBatteryInner(canvas);
    }

    Drawable getBatteryInner() {
        if (this.mBatteryInner != null) {
            return this.mBatteryInner;
        }
        this.mBatteryInner = getContext().getDrawable(R.drawable.ic_phonemanager_powersaving_inner);
        this.mBatteryInner.setBounds(0, 0, this.mBatteryInner.getIntrinsicWidth(), this.mBatteryInner.getIntrinsicHeight());
        return this.mBatteryInner;
    }

    public void drawBatteryInner(Canvas canvas) {
        float clipPercent = Math.min(Math.max(0.0f, (((((float) (100 - this.mBatteryPercent)) / 100.0f) * 25.0f) + 13.0f) / ((float) (38 + 10))), 100.0f);
        Drawable batteryDrawable = getBatteryInner();
        int width = batteryDrawable.getIntrinsicWidth();
        int height = batteryDrawable.getIntrinsicHeight();
        float clipHeight = clipPercent * ((float) height);
        canvas.save();
        canvas.clipRect(0.0f, clipHeight, (float) width, (float) height);
        batteryDrawable.draw(canvas);
        canvas.restore();
    }
}
