package com.huawei.systemmanager.power.batterychart;

import android.content.res.Resources;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader.TileMode;
import android.text.TextPaint;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class BatteryHistoryChartPaintFactory {
    public static Paint createXLinePaint() {
        Paint mXPaint = new Paint(1);
        mXPaint.setColor(GlobalContext.getContext().getResources().getColor(R.color.twenty_percent_primary_color, null));
        mXPaint.setStyle(Style.STROKE);
        mXPaint.setStrokeWidth(Utility.ALPHA_MAX);
        return mXPaint;
    }

    public static TextPaint createDatePaint(int textSize) {
        TextPaint paint = new TextPaint(1);
        Resources resources = GlobalContext.getContext().getResources();
        paint.setColor(resources.getColor(resources.getIdentifier("secondary_text_emui", "color", "androidhwext"), null));
        paint.setAntiAlias(true);
        paint.setTextSize((float) textSize);
        return paint;
    }

    public static TextPaint createPercentScalePaint(int textSize) {
        TextPaint paint = new TextPaint(1);
        Resources resources = GlobalContext.getContext().getResources();
        paint.setColor(resources.getColor(resources.getIdentifier("secondary_text_emui", "color", "androidhwext"), null));
        paint.setAntiAlias(true);
        paint.setTextSize((float) textSize);
        return paint;
    }

    public static Paint createChartLinePaint() {
        Paint mChartLinePaint = new Paint(1);
        mChartLinePaint.setAntiAlias(true);
        mChartLinePaint.setStyle(Style.STROKE);
        mChartLinePaint.setStrokeWidth(4.0f);
        mChartLinePaint.setColor(GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line));
        return mChartLinePaint;
    }

    public static Paint createChartPaint(int Ystart, int Yend) {
        Paint mChartPaint = new Paint(1);
        mChartPaint.setAntiAlias(true);
        mChartPaint.setStyle(Style.FILL);
        mChartPaint.setShader(new LinearGradient(0.0f, (float) Ystart, 0.0f, (float) Yend, GlobalContext.getContext().getResources().getColor(R.color.hsm_widget_canvas_degree_line_alpha30), GlobalContext.getContext().getResources().getColor(R.color.emui5_theme_alpha), TileMode.CLAMP));
        return mChartPaint;
    }
}
