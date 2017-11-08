package com.huawei.systemmanager.spacecleanner.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.widget.ImageView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.profile.HwPowerProfile;
import com.huawei.systemmanager.util.HwLog;
import java.text.NumberFormat;

public class LowerMemChartView extends ImageView {
    private static final double CHART_LEFT_MARGIN_PERCENT = 0.072d;
    private static final double CHART_LINE_BOTTOM_MARGIN_PERCENT = 0.0895d;
    private static final double CHART_RIGHT_MARGIN_PERCENT = 0.1091d;
    private static final double CHART_TOP_MARGIN_PERCENT = 0.0522d;
    private static final int DEFAULT_MAX_PERCENTAGE_FOR_SHOW = 20;
    private static final int INVALIDATE_PERCENT = -1;
    private static final double LEFT_X_OFFSET = 0.02d;
    private static final int NORMAL_STATE_PERCENT = 10;
    private static final float STROTH_WIDTH = 3.0f;
    public static final String TAG = "LowerMemChartView";
    private static final double Y_PERCENT_TWO_WORDS_OFFSET = 0.1d;
    private static int sChartLineNormalColor;
    private static int sChartLineRedColor;
    private static int sChartPercentColor;
    private int mPercentage;
    private Paint paint;

    public LowerMemChartView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
        this.paint = createtPaint(context);
    }

    public static Paint createtPaint(Context context) {
        Paint paint = new Paint();
        paint.setStrokeWidth(STROTH_WIDTH);
        paint.setAntiAlias(true);
        paint.setStyle(Style.STROKE);
        paint.setColor(context.getResources().getColor(R.color.space_cleaner_low_mem_line_normal_color));
        paint.setTextSize(context.getResources().getDimension(R.dimen.chart_txt_size));
        sChartLineNormalColor = context.getResources().getColor(R.color.space_cleaner_low_mem_line_normal_color);
        sChartLineRedColor = context.getResources().getColor(R.color.space_cleaner_low_mem_line_red_color);
        sChartPercentColor = context.getResources().getColor(R.color.space_cleaner_low_mem_txt_color);
        return paint;
    }

    public LowerMemChartView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.mPercentage = -1;
    }

    public void setPercentage(int percentage) {
        if (checkValidate(percentage)) {
            this.mPercentage = percentage;
        } else {
            HwLog.w(TAG, "setPercentage,percentage is not invalidate.percentage:" + percentage);
            this.mPercentage = -1;
        }
        invalidate();
    }

    private boolean checkValidate(int percentage) {
        return percentage >= 0 && percentage <= 20;
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawLineByPercent(canvas);
        drawXPercentNumbers(canvas);
        drawYPercentNumbers(canvas);
    }

    private void drawLineByPercent(Canvas canvas) {
        if (-1 == this.mPercentage) {
            HwLog.w(TAG, "Percentage is not invalidate.Do not show line");
            return;
        }
        int left = ((int) (((double) getWidth()) * CHART_LEFT_MARGIN_PERCENT)) + ((int) (getOnePercentWidth() * ((double) this.mPercentage)));
        if (this.mPercentage < 10) {
            this.paint.setColor(sChartLineRedColor);
        } else {
            this.paint.setColor(sChartLineNormalColor);
        }
        this.paint.setStrokeWidth(STROTH_WIDTH);
        canvas.drawLine((float) left, 0.0f, (float) left, (float) ((int) (((double) getHeight()) - (((double) getHeight()) * CHART_LINE_BOTTOM_MARGIN_PERCENT))), this.paint);
        this.paint.setStrokeWidth(0.0f);
        canvas.drawText(getPercentNumberStr(this.mPercentage), (float) (left - ((int) (((double) getWidth()) * 0.02d))), (float) getHeight(), this.paint);
    }

    private void drawXPercentNumbers(Canvas canvas) {
        this.paint.setColor(sChartPercentColor);
        this.paint.setStrokeWidth(0.0f);
        int leftMargin = (int) ((((double) getWidth()) * CHART_LEFT_MARGIN_PERCENT) - (((double) getWidth()) * 0.02d));
        drawPercentNumber(canvas, 10, leftMargin);
        drawPercentNumber(canvas, 20, leftMargin);
        drawPercentNumber(canvas, 30, leftMargin);
    }

    private void drawYPercentNumbers(Canvas canvas) {
        this.paint.setColor(sChartPercentColor);
        this.paint.setStrokeWidth(0.0f);
        int bottomMargin = (int) (((double) getHeight()) * CHART_LINE_BOTTOM_MARGIN_PERCENT);
        drawYPercentNumber(canvas, 40, bottomMargin);
        drawYPercentNumber(canvas, 80, bottomMargin);
        drawYPercentNumber(canvas, 90, bottomMargin);
        drawYPercentNumber(canvas, 100, bottomMargin);
    }

    private void drawYPercentNumber(Canvas canvas, int percent, int bottomMargin) {
        int left = 0;
        if (percent < 100) {
            left = (int) ((((double) getWidth()) * CHART_LEFT_MARGIN_PERCENT) * Y_PERCENT_TWO_WORDS_OFFSET);
        }
        canvas.drawText(getYPercentNumberStr(percent), (float) left, (float) (getHeight() - (((int) (getYOnePercentHeight() * ((double) percent))) + bottomMargin)), this.paint);
    }

    private void drawPercentNumber(Canvas canvas, int percent, int leftMargin) {
        if (this.mPercentage > percent + 1 || this.mPercentage < percent - 1) {
            canvas.drawText(getPercentNumberStr(percent), (float) (((int) (getOnePercentWidth() * ((double) percent))) + leftMargin), (float) getHeight(), this.paint);
        }
    }

    private String getPercentNumberStr(int percent) {
        NumberFormat pnf = NumberFormat.getPercentInstance();
        double freeValue = ((double) percent) * 0.01d;
        pnf.setMinimumFractionDigits(0);
        return pnf.format(freeValue);
    }

    private String getYPercentNumberStr(int percent) {
        return NumberFormat.getInstance().format((long) percent);
    }

    private double getOnePercentWidth() {
        int widgetWidth = getWidth();
        return (((double) widgetWidth) - ((((double) widgetWidth) * CHART_LEFT_MARGIN_PERCENT) + (((double) widgetWidth) * CHART_RIGHT_MARGIN_PERCENT))) / HwPowerProfile.SYSTEM_BASE_NORMAL_POWER;
    }

    private double getYOnePercentHeight() {
        int widgetHeight = getHeight();
        return (((double) widgetHeight) - ((((double) widgetHeight) * CHART_TOP_MARGIN_PERCENT) + (((double) widgetHeight) * CHART_LINE_BOTTOM_MARGIN_PERCENT))) / 100.0d;
    }
}
