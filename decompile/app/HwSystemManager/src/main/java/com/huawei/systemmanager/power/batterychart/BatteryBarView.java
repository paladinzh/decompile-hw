package com.huawei.systemmanager.power.batterychart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;

public class BatteryBarView extends View {
    private static final int CENTER_VIEW_COLOR = GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line_alpha30);
    private static final int SIDE_LINE_COLOR = GlobalContext.getContext().getResources().getColor(R.color.emui5_theme_alpha20);
    private int heightView = 0;
    private Paint mLinePaint;
    private int widthView = 0;

    public BatteryBarView(Context context) {
        super(context);
    }

    public BatteryBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initLinePaint();
        setBackgroundColor(CENTER_VIEW_COLOR);
    }

    void initLinePaint() {
        this.mLinePaint = new Paint(1);
        this.mLinePaint.setColor(SIDE_LINE_COLOR);
        this.mLinePaint.setStyle(Style.STROKE);
        this.mLinePaint.setStrokeWidth(4.0f);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        this.widthView = w;
        this.heightView = h;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        canvas.drawLine(0.0f, 0.0f, 0.0f, (float) this.heightView, this.mLinePaint);
        canvas.drawLine((float) this.widthView, 0.0f, (float) this.widthView, (float) this.heightView, this.mLinePaint);
    }
}
