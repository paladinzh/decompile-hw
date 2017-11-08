package com.huawei.netassistant.ui.view;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.MeasureSpec;
import com.huawei.netassistant.common.ParcelableDailyTrafficItem;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class TrafficLineChartView extends View {
    private static final String TAG = "TrafficLineChartView";
    private ChartSizeChangeListener mChartSizeChangeListener;
    private ClickPointListener mClickPoint;
    private List<ChartData> mDatas;
    private boolean mIsLand;
    private int mLastClickPosition = -1;
    private boolean mNeedUpdateY = true;
    private LineCharParam mParam;
    private int mResHeight;
    private long maxData;
    private int xDistance;

    public interface ClickPointListener {
        void onFstPointClick(ChartData chartData);

        void onLastPointClick(ChartData chartData);

        void onPointClick(ChartData chartData);
    }

    public interface ChartSizeChangeListener {
        void onChartSizeChanged(int i);
    }

    public TrafficLineChartView(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.LineChartViewStyle);
        this.mParam = new LineCharParam(a);
        this.mDatas = new ArrayList();
        a.recycle();
        this.mIsLand = getResources().getConfiguration().orientation == 2 ? Utility.isSupportOrientation() : false;
        updateXDistance(this.mIsLand, true);
    }

    private void updateXDistance(boolean isLand, boolean firstDraw) {
        int width = Utility.getDisplayMetricsWidth(getContext());
        if (isLand) {
            width = (int) (((double) width) * 0.5d);
        }
        this.xDistance = ((width - (this.mParam.lineOffset * 2)) - this.mParam.lineChartInterval) / 6;
        if (!firstDraw) {
            updateData();
            if (this.mChartSizeChangeListener != null) {
                this.mChartSizeChangeListener.onChartSizeChanged(getXWidth());
            }
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int resWidth = measureWidth(widthMeasureSpec);
        int resHeight = MeasureSpec.getSize(heightMeasureSpec);
        this.mResHeight = MeasureSpec.getSize(heightMeasureSpec);
        HwLog.d(TAG, "widthMeasureSpec = " + resWidth + " heightMeasureSpec = " + resHeight);
        setMeasuredDimension(resWidth, resHeight);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateY() {
        if (!(this.mResHeight == 0 || !this.mNeedUpdateY || this.mDatas == null || this.mDatas.size() == 0)) {
            for (ChartData data : this.mDatas) {
                float rawY = data.getY();
                if (rawY < 0.0f) {
                    float x = data.getX();
                    float y = rawY + ((float) this.mResHeight);
                    data.updateClickArea(new RectF(x - ((float) (this.mParam.pointCircleRadius * 4)), y - ((float) (this.mParam.pointCircleRadius * 4)), ((float) (this.mParam.pointCircleRadius * 4)) + x, ((float) (this.mParam.pointCircleRadius * 4)) + y));
                    data.updateY(y);
                }
            }
            this.mNeedUpdateY = false;
        }
    }

    private int measureWidth(int measureSpec) {
        int result = 0;
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        if (specMode == 1073741824) {
            return specSize;
        }
        if (this.mDatas != null) {
            if (this.mDatas.size() < 7) {
                result = (this.xDistance * 6) + (this.mParam.lineOffset * 2);
            } else {
                result = ((this.mDatas.size() - 1) * this.xDistance) + (this.mParam.lineOffset * 2);
            }
        }
        if (specMode == Integer.MIN_VALUE) {
            return Math.min(result, specSize);
        }
        return result;
    }

    protected void onDraw(Canvas canvas) {
        if (this.mDatas.size() != 0) {
            updateY();
            drawBackground(canvas);
            drawArea(canvas);
            drawPoint(canvas);
            HwLog.d(TAG, "onDraw");
            super.onDraw(canvas);
        }
    }

    private void drawBackground(Canvas canvas) {
        int numPaddingBottom = getResources().getDimensionPixelSize(R.dimen.net_assistant_line_trafficLinechartview_num_paddingBottom);
        Paint textPaint = LineChartPaintFactory.createChartDateTextPaint(this.mParam.numXYlineSize, getResources().getColor(R.color.emui_list_secondray_text));
        boolean isUsedTadayStr = getResources().getBoolean(R.bool.net_is_use_today);
        int N = this.mDatas.size();
        int i = 0;
        while (i < N) {
            ChartData chartData = (ChartData) this.mDatas.get(i);
            if (chartData != null) {
                String date = chartData.getDate();
                if (date != null) {
                    if (!isUsedTadayStr) {
                        textPaint.setTextAlign(Align.CENTER);
                    } else if (i == 0 && i == N - 1) {
                        textPaint.setTextAlign(Align.LEFT);
                    } else if (i <= 5 || i != N - 1) {
                        textPaint.setTextAlign(Align.CENTER);
                    } else {
                        textPaint.setTextAlign(Align.RIGHT);
                    }
                    canvas.drawText(date, chartData.getX(), (float) (getBottom() - numPaddingBottom), textPaint);
                }
            }
            i++;
        }
        if (N < 7) {
            int xTextDistance = this.xDistance;
            textPaint.setTextAlign(Align.CENTER);
            Calendar cal = Calendar.getInstance();
            for (i = N; i < 7; i++) {
                cal.add(5, 1);
                canvas.drawText(NumberFormat.getInstance().format((long) cal.get(5)), ((ChartData) this.mDatas.get(N - 1)).getX() + ((float) xTextDistance), (float) (getBottom() - numPaddingBottom), textPaint);
                xTextDistance += this.xDistance;
            }
        }
        textPaint.setColor(getResources().getColor(R.color.emui_list_secondray_text));
        textPaint.setTextAlign(Align.RIGHT);
        Paint XlinePaint = LineChartPaintFactory.createChartBackLinePaint();
        Paint numlinePaint = LineChartPaintFactory.createChartLinePaint();
        int lineStart = this.mParam.getLineChartStartY();
        canvas.drawLine((float) getLeft(), (float) (getHeight() - lineStart), (float) getRight(), (float) (getHeight() - lineStart), XlinePaint);
        for (int lineStartNum = 1; lineStartNum < 6; lineStartNum++) {
            canvas.drawLine((float) (getLeft() + this.mParam.lineOffset), (float) ((getHeight() - lineStart) - this.mParam.lineChartHorLineInterval), (float) getRight(), (float) ((getHeight() - lineStart) - this.mParam.lineChartHorLineInterval), numlinePaint);
            lineStart += this.mParam.lineChartHorLineInterval;
        }
    }

    private void drawPoint(Canvas canvas) {
        int i;
        Path circlePath = new Path();
        Paint paintCircle = LineChartPaintFactory.createCirclePaint();
        Paint paintPoint = LineChartPaintFactory.createPointPaint();
        int N = this.mDatas.size();
        for (i = 0; i < N; i++) {
            circlePath.addCircle(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY(), (float) ((ChartData) this.mDatas.get(i)).getPointRadius(), Direction.CCW);
            canvas.drawCircle(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY(), (float) this.mParam.pointCircleRadius, paintCircle);
            canvas.drawCircle(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY(), ((float) this.mParam.pointCircleRadius) / 2.0f, paintPoint);
        }
        canvas.clipPath(circlePath, Op.DIFFERENCE);
        Path dashedLinePath = new Path();
        for (i = 0; i < N; i++) {
            dashedLinePath.reset();
            dashedLinePath.moveTo(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY());
            dashedLinePath.lineTo(((ChartData) this.mDatas.get(i)).getX(), (float) (getHeight() - this.mParam.getLineChartStartY()));
            Paint dashedLinePaint = LineChartPaintFactory.createDashedLinePaint();
            dashedLinePaint.setShader(new LinearGradient(0.0f, ((ChartData) this.mDatas.get(i)).getY(), 0.0f, (float) (getHeight() - this.mParam.getLineChartStartY()), getResources().getColor(R.color.hwsystemmanager_white_alpha40_color), 0, TileMode.CLAMP));
            canvas.drawPath(dashedLinePath, dashedLinePaint);
        }
    }

    private void drawArea(Canvas canvas) {
        int initY = getHeight() - this.mParam.getLineChartStartY();
        Paint linePaint = LineChartPaintFactory.createLinkLinePaint(this.mParam.linkLineWidth);
        Paint areaPaint = LineChartPaintFactory.createAreaPaint((float) initY);
        Path pathLine = new Path();
        Path pathArea = new Path();
        pathLine.moveTo(((ChartData) this.mDatas.get(0)).getX(), ((ChartData) this.mDatas.get(0)).getY());
        pathArea.moveTo((float) this.mParam.lineOffset, (float) initY);
        int N = this.mDatas.size();
        for (int i = 0; i < N; i++) {
            pathLine.lineTo(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY());
            pathArea.lineTo(((ChartData) this.mDatas.get(i)).getX(), ((ChartData) this.mDatas.get(i)).getY());
        }
        canvas.drawPath(pathLine, linePaint);
        pathArea.lineTo(((ChartData) this.mDatas.get(N - 1)).getX(), (float) (getHeight() - this.mParam.getLineChartStartY()));
        canvas.drawPath(pathArea, areaPaint);
    }

    public void setData(List<ParcelableDailyTrafficItem> items) {
        if (items != null && items.size() != 0) {
            this.mDatas.clear();
            int N = items.size();
            int height = getLayoutParams().height - this.mParam.getLineChartStartY();
            int chartHeight = this.mParam.lineChartHorLineInterval * 5;
            this.maxData = getMax(items);
            double scale = 1.0d;
            if (this.maxData != 0) {
                scale = ((double) chartHeight) / ((double) this.maxData);
            }
            for (int i = 0; i < N; i++) {
                float x = (float) ((getLeft() + (this.xDistance * i)) + this.mParam.lineOffset);
                float y = (float) (((double) height) - (((double) ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic) * scale));
                this.mDatas.add(new TrafficLineChartData(i, x, y, CommonMethodUtil.formatBytes(GlobalContext.getContext(), ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic), this.mParam.pointCircleRadius, new RectF(x - ((float) (this.mParam.pointCircleRadius * 4)), y - ((float) (this.mParam.pointCircleRadius * 4)), ((float) (this.mParam.pointCircleRadius * 4)) + x, ((float) (this.mParam.pointCircleRadius * 4)) + y), ((ParcelableDailyTrafficItem) items.get(i)).mDate));
            }
            this.mNeedUpdateY = true;
            invalidate();
        }
    }

    public void updateData() {
        int N = this.mDatas.size();
        for (int i = 0; i < N; i++) {
            float x = (float) ((getLeft() + (this.xDistance * i)) + this.mParam.lineOffset);
            RectF rectf = new RectF(((ChartData) this.mDatas.get(i)).getClickArea());
            rectf.left = x - ((float) (this.mParam.pointCircleRadius * 4));
            rectf.right = ((float) (this.mParam.pointCircleRadius * 4)) + x;
            ((ChartData) this.mDatas.get(i)).updateX(x);
            ((ChartData) this.mDatas.get(i)).updateClickArea(rectf);
        }
    }

    public int getXWidth() {
        if (this.mDatas == null) {
            return 0;
        }
        if (this.mDatas.size() < 7) {
            return (this.xDistance * 6) + (this.mParam.lineOffset * 2);
        }
        return ((this.mDatas.size() - 1) * this.xDistance) + (this.mParam.lineOffset * 2);
    }

    public ChartData getLastClickData() {
        if (this.mDatas == null || this.mDatas.size() <= 0) {
            return null;
        }
        if (this.mLastClickPosition < 0 || this.mLastClickPosition >= this.mDatas.size()) {
            return (ChartData) this.mDatas.get(this.mDatas.size() - 1);
        }
        return (ChartData) this.mDatas.get(this.mLastClickPosition);
    }

    public ChartData getLastData() {
        if (this.mDatas == null || this.mDatas.size() <= 0) {
            return null;
        }
        return (ChartData) this.mDatas.get(this.mDatas.size() - 1);
    }

    public int getDataSize() {
        if (this.mDatas != null) {
            return this.mDatas.size();
        }
        return 0;
    }

    private long getMax(List<ParcelableDailyTrafficItem> items) {
        long result = 0;
        int N = items.size();
        for (int i = 0; i < N; i++) {
            if (result < ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic) {
                result = ((ParcelableDailyTrafficItem) items.get(i)).mDailyTraffic;
            }
        }
        return result;
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            HsmStat.statE(Events.E_NETASSISTANT_CLICK_DIFF_DAY_TRAFFIC);
            int N = this.mDatas.size();
            int i = 0;
            while (i < N) {
                ChartData data = (ChartData) this.mDatas.get(i);
                if (((ChartData) this.mDatas.get(i)).getClickArea().contains(event.getX(), event.getY())) {
                    this.mLastClickPosition = i;
                    if (this.mClickPoint != null) {
                        if (i == 0) {
                            this.mClickPoint.onFstPointClick(data);
                        } else if (i != N - 1 || i <= 5) {
                            this.mClickPoint.onPointClick(data);
                        } else {
                            this.mClickPoint.onLastPointClick(data);
                        }
                    }
                    invalidate();
                } else {
                    i++;
                }
            }
        }
        return super.onTouchEvent(event);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        boolean z;
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == 2) {
            z = true;
        } else {
            z = false;
        }
        this.mIsLand = z;
        updateXDistance(this.mIsLand, false);
    }

    public void addClickPointListener(ClickPointListener clickPoint) {
        this.mClickPoint = clickPoint;
    }

    public void removeClickPointListener() {
        this.mClickPoint = null;
    }

    public void addChartSizeChangeListener(ChartSizeChangeListener listener) {
        this.mChartSizeChangeListener = listener;
    }

    public void removeChartSizeChangeListener() {
        this.mChartSizeChangeListener = null;
    }
}
