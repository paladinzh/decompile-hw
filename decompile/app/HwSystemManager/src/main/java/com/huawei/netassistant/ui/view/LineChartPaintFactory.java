package com.huawei.netassistant.ui.view;

import android.graphics.DashPathEffect;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class LineChartPaintFactory {
    public static Paint createChartDateTextPaint(int textSize, int textColor) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setTextSize((float) textSize);
        paint.setStyle(Style.STROKE);
        paint.setColor(textColor);
        return paint;
    }

    public static Paint createChartBackLinePaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.trafficchartlineview_line));
        paint.setStrokeWidth(2.0f);
        return paint;
    }

    public static Paint createChartLinePaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.trafficchartlineview_line));
        paint.setStrokeWidth(Utility.ALPHA_MAX);
        return paint;
    }

    public static Paint createLinkLinePaint(float width) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line));
        paint.setStyle(Style.STROKE);
        paint.setStrokeWidth(width);
        return paint;
    }

    public static Paint createCirclePaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line));
        return paint;
    }

    public static Paint createPointPaint() {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.hwsystemmanager_white_color));
        return paint;
    }

    public static Paint createAnimaPaintPaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(2.0f);
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line));
        return paint;
    }

    public static Paint createDashedLinePaint() {
        Paint paint = new Paint();
        paint.setStrokeWidth(Utility.ALPHA_MAX);
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setPathEffect(new DashPathEffect(new float[]{2.0f, 2.0f}, 0.0f));
        return paint;
    }

    public static Paint createAreaPaint(float starty) {
        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL);
        paint.setShader(new LinearGradient(0.0f, 0.0f, 0.0f, starty, GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line_alpha30), GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line_alpha5), TileMode.CLAMP));
        return paint;
    }
}
