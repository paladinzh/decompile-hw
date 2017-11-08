package com.huawei.systemmanager.power.batterychart;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.os.BatteryStats;
import android.os.BatteryStats.HistoryItem;
import android.os.SystemProperties;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.power.data.battery.BatteryInfo;
import com.huawei.systemmanager.power.model.RemainingTimeSceneHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public final class BatteryHistoryOnlyChart extends View {
    private static final long FIVE_MINUTES_IN_MILLI_SECONDS = 300000;
    private static final long MAX_DAYS_IN_MILLI_SECONDS = 15552000000L;
    private static final int MAX_PERCENT = 100;
    private static final int MAX_PERCENT_LABEL = 100;
    private static final int MIDDLE_PERCENT_LABEL = 50;
    private static final long ONE_HOUR_IN_MILLI_SECONDS = 3600000;
    private static final int PERCENT_DEGREE_SCALE_NUM = 3;
    private static final String TAG = "BatteryHistoryOnlyChart";
    private static final long TWO_DAY_IN_MILLI_SECONDS = 172800000;
    private final int BOTTOM_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_bottom_padding));
    private final int CHART_AREA_RIGHT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_area_right_padding));
    private final int LEFT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_left_padding));
    private final int LINE_WIDTH = ((int) getResources().getDimension(R.dimen.battery_history_chart_linewidth));
    private final int RIGHT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_right_margin));
    private final int TEXT_TOP_MARGIN = ((int) getResources().getDimension(R.dimen.battery_history_chart_line_text_margin));
    private final int TOP_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_top_padding));
    private long mBinFileStartWallTime;
    private int mChartAreaLeft;
    private int mChartAreaRight;
    private Paint mChartLinePaint = BatteryHistoryChartPaintFactory.createChartLinePaint();
    private Path mChartLinePath = new Path();
    private Paint mChartPaint;
    private Path mChartPath = new Path();
    private long mChartStartWallTime;
    private int mCustomDateXYMargin;
    final ArrayList<DateLabel> mDateLabels = new ArrayList();
    private long mEndWallTime;
    private long mHistEnd;
    private long mHistStart;
    private int mLevelBottom;
    private int mLevelLeft;
    private int mLevelRight;
    private int mLevelTop;
    private TimeLabel mNowLabel;
    private int mNumHist;
    final ArrayList<PercentScaleLabel> mPercentScaleLabels = new ArrayList();
    private TextPaint mPercentScalePaint;
    private int mRawLevel;
    private BatteryStats mStats;
    private TextPaint mTimeDatePaint;
    final ArrayList<TimeLabel> mTimeLabels = new ArrayList();
    private Paint mXLinePaint = BatteryHistoryChartPaintFactory.createXLinePaint();

    static class DateLabel {
        final int height;
        final String label;
        final int width;
        final int x;

        @TargetApi(18)
        DateLabel(TextPaint paint, int x, Calendar cal, boolean dayFirst) {
            this.label = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), dayFirst ? "dM" : "Md"), cal).toString();
            Rect bounds = new Rect();
            paint.getTextBounds(this.label, 0, this.label.length(), bounds);
            this.height = bounds.height();
            this.width = bounds.width();
            this.x = x - (this.width / 2);
        }
    }

    static class PercentScaleLabel {
        static final int MAX_PERCENT = 100;
        final int height;
        final String label;
        final int width;
        final int x;
        final int y;

        PercentScaleLabel(TextPaint paint, int mChartEnd, int mChartTop, int mChartHeight, int percent) {
            this.label = BatterHistoryUtils.formatPercentage(percent);
            Rect bounds = new Rect();
            paint.getTextBounds(this.label, 0, this.label.length(), bounds);
            this.height = bounds.height();
            this.width = bounds.width();
            this.x = mChartEnd - this.width;
            this.y = ((mChartTop + mChartHeight) - ((percent * mChartHeight) / 100)) + (this.height / 2);
        }
    }

    static class TimeLabel {
        final int height;
        final String label;
        final int width;
        final int x;
        final int y;

        TimeLabel(TextPaint paint, int mLabelStart, int mLabelTop, String label) {
            Rect bounds = new Rect();
            paint.getTextBounds(label, 0, label.length(), bounds);
            this.height = bounds.height();
            this.width = bounds.width();
            this.x = mLabelStart;
            this.y = this.height + mLabelTop;
            this.label = label;
        }
    }

    public BatteryHistoryOnlyChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        int textSizePx = getResources().getDimensionPixelSize(R.dimen.battery_history_chart_dateText_size);
        this.mTimeDatePaint = BatteryHistoryChartPaintFactory.createDatePaint(textSizePx);
        this.mPercentScalePaint = BatteryHistoryChartPaintFactory.createPercentScalePaint(textSizePx);
        this.mCustomDateXYMargin = this.TEXT_TOP_MARGIN;
    }

    private void reSetPath() {
        if (SystemProperties.get("ro.build.characteristics", RemainingTimeSceneHelper.DB_RECORD_DATE_DEFAULT).equals("tablet")) {
            this.mChartLinePath = new Path();
            this.mChartPath = new Path();
            this.mDateLabels.clear();
            this.mTimeLabels.clear();
            this.mPercentScaleLabels.clear();
        }
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        reSetPath();
        this.mLevelTop = this.TOP_PADDING;
        this.mLevelBottom = h - this.BOTTOM_PADDING;
        this.mLevelLeft = this.LEFT_PADDING;
        this.mLevelRight = w - this.RIGHT_PADDING;
        this.mChartAreaRight = this.mLevelRight - this.CHART_AREA_RIGHT_PADDING;
        this.mChartAreaLeft = this.mLevelLeft;
        long wallTimeStart = this.mChartStartWallTime;
        long curWallTime = this.mBinFileStartWallTime;
        long lastRealTime = 0;
        int x = this.mLevelLeft;
        int startX = this.mLevelLeft;
        int lastX = -1;
        int lastY = -1;
        int index = 0;
        int sumHeightLevel = this.mLevelBottom - this.mLevelTop;
        int sumWidthLevel = this.mChartAreaRight - this.mChartAreaLeft;
        Path path = null;
        if (this.mEndWallTime > this.mBinFileStartWallTime && this.mStats.startIteratingHistoryLocked()) {
            HistoryItem rec = new HistoryItem();
            while (this.mStats.getNextHistoryLocked(rec) && index < this.mNumHist) {
                if (rec.isDeltaData()) {
                    curWallTime += rec.time - lastRealTime;
                    lastRealTime = rec.time;
                    if (curWallTime >= this.mChartStartWallTime) {
                        long delta = curWallTime - wallTimeStart;
                        if (delta > TWO_DAY_IN_MILLI_SECONDS) {
                            delta = TWO_DAY_IN_MILLI_SECONDS;
                        }
                        x = this.mChartAreaLeft + ((int) ((((long) sumWidthLevel) * delta) / TWO_DAY_IN_MILLI_SECONDS));
                        int y = (this.mLevelTop + sumHeightLevel) - ((rec.batteryLevel * sumHeightLevel) / 100);
                        if (!(lastX == x || lastY == y)) {
                            if (path == null) {
                                path = this.mChartPath;
                                path.moveTo((float) x, (float) y);
                                this.mChartLinePath.moveTo((float) x, (float) y);
                                startX = x;
                            } else {
                                path.lineTo((float) x, (float) y);
                                this.mChartLinePath.lineTo((float) x, (float) y);
                            }
                            lastX = x;
                            lastY = y;
                        }
                    }
                } else {
                    long lastWalltime = curWallTime;
                    if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                        if (rec.currentTime >= this.mBinFileStartWallTime) {
                            curWallTime = rec.currentTime;
                        } else {
                            curWallTime = this.mBinFileStartWallTime + (rec.time - this.mHistStart);
                        }
                        lastRealTime = rec.time;
                    }
                    if (rec.cmd != (byte) 6 && ((rec.cmd != (byte) 5 || Math.abs(lastWalltime - curWallTime) > 3600000) && path != null)) {
                        finishChartPath(x + 1, sumHeightLevel, startX, lastY, path, lastX);
                        path = null;
                        lastY = -1;
                        lastX = -1;
                    }
                }
                index++;
            }
            this.mStats.finishIteratingHistoryLocked();
        }
        finishChartPath(x + 1, sumHeightLevel, startX, lastY, path, lastX);
        addDateLabel();
        int chartHeight = this.mLevelBottom - this.mLevelTop;
        addPercentScaleLabel(this.mPercentScalePaint, this.mLevelRight, this.mLevelTop, chartHeight, this.mRawLevel);
        addPercentScaleLabel(this.mPercentScalePaint, this.mLevelRight, this.mLevelTop, chartHeight, 100);
        addPercentScaleLabel(this.mPercentScalePaint, this.mLevelRight, this.mLevelTop, chartHeight, 50);
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawXAndPercentScale(canvas);
        canvas.drawPath(this.mChartLinePath, this.mChartLinePaint);
        this.mChartPaint = BatteryHistoryChartPaintFactory.createChartPaint(this.mLevelTop, this.mLevelBottom);
        canvas.drawPath(this.mChartPath, this.mChartPaint);
        drawNowLabel(canvas);
        drawDateLabel(canvas);
        drawTimeLabel(canvas);
    }

    private void addDateLabel() {
        if (this.mChartStartWallTime > 0 && this.mEndWallTime > this.mChartStartWallTime) {
            Calendar calStart = Calendar.getInstance();
            calStart.setTimeInMillis(this.mChartStartWallTime);
            calStart.set(14, 0);
            calStart.set(13, 0);
            calStart.set(12, 0);
            if (calStart.getTimeInMillis() < this.mChartStartWallTime) {
                calStart.set(11, calStart.get(11) + 1);
            }
            Calendar calEnd = Calendar.getInstance();
            calEnd.setTimeInMillis(this.mEndWallTime);
            calEnd.set(14, 0);
            calEnd.set(13, 0);
            calEnd.set(12, 0);
            if (calStart.get(6) != calEnd.get(6) || calStart.get(1) != calEnd.get(1)) {
                boolean isDayFirst = BatterHistoryUtils.isDayFirst();
                calStart.set(11, 0);
                long startRoundTime = calStart.getTimeInMillis();
                if (startRoundTime < this.mChartStartWallTime) {
                    calStart.set(6, calStart.get(6) + 1);
                    startRoundTime = calStart.getTimeInMillis();
                }
                long endRoundTime = calEnd.getTimeInMillis();
                if (startRoundTime < endRoundTime) {
                    addDateLabel(calStart, this.mChartAreaLeft, this.mChartAreaRight, isDayFirst);
                }
                calStart.set(6, calStart.get(6) + 1);
                if (calStart.getTimeInMillis() < endRoundTime) {
                    addDateLabel(calStart, this.mChartAreaLeft, this.mChartAreaRight, isDayFirst);
                }
            }
        }
    }

    public void setData(BatteryStats stats) {
        if (stats == null) {
            Log.d(TAG, "setData , argu is wrong");
            return;
        }
        resetField();
        this.mStats = stats;
        int pos = 0;
        int lastInteresting = 0;
        long lastWallTime = 0;
        long lastRealtime = 0;
        byte lastLevel = (byte) -1;
        boolean first = true;
        if (stats.startIteratingHistoryLocked()) {
            HistoryItem rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec)) {
                pos++;
                if (first) {
                    first = false;
                    this.mHistStart = rec.time;
                }
                if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                    if (rec.currentTime > MAX_DAYS_IN_MILLI_SECONDS + lastWallTime || rec.time < this.mHistStart + 300000) {
                        this.mBinFileStartWallTime = 0;
                    }
                    lastWallTime = rec.currentTime;
                    lastRealtime = rec.time;
                    if (this.mBinFileStartWallTime == 0) {
                        this.mBinFileStartWallTime = lastWallTime - (lastRealtime - this.mHistStart);
                    }
                }
                if (rec.isDeltaData()) {
                    if (rec.batteryLevel != lastLevel || pos == 1) {
                        lastLevel = rec.batteryLevel;
                    }
                    lastInteresting = pos;
                    this.mHistEnd = rec.time;
                }
            }
        }
        this.mNumHist = lastInteresting;
        this.mEndWallTime = (this.mHistEnd + lastWallTime) - lastRealtime;
        if (this.mEndWallTime - this.mBinFileStartWallTime <= TWO_DAY_IN_MILLI_SECONDS) {
            HwLog.i(TAG, "setData, it is in two days");
            this.mChartStartWallTime = this.mBinFileStartWallTime;
        } else {
            HwLog.i(TAG, "setData, it is not in two days");
            this.mChartStartWallTime = this.mEndWallTime - TWO_DAY_IN_MILLI_SECONDS;
        }
        this.mRawLevel = BatteryInfo.getBatteryLevelValue();
        HwLog.i(TAG, "mChartStartWallTime is: " + this.mChartStartWallTime + "   " + "  mEndWallTime is: " + this.mEndWallTime);
    }

    public long getChartStartTime() {
        return this.mChartStartWallTime;
    }

    public long getChartEndTime() {
        return this.mEndWallTime;
    }

    public long getBinFileStartWallTime() {
        return this.mBinFileStartWallTime;
    }

    private void resetField() {
        this.mBinFileStartWallTime = 0;
        this.mEndWallTime = 0;
        this.mDateLabels.clear();
        this.mTimeLabels.clear();
    }

    private void drawNowLabel(Canvas canvas) {
        long startTime = getChartStartTime();
        long currTime = getChartEndTime();
        int sumWidthLevel = this.mChartAreaRight - this.mChartAreaLeft;
        if (currTime >= startTime) {
            int nowX = this.mChartAreaLeft + ((int) (((currTime - startTime) * ((long) sumWidthLevel)) / TWO_DAY_IN_MILLI_SECONDS));
            String labelStr = getResources().getString(R.string.power_battery_history_chart_now);
            int nowLabelWidth = (int) this.mTimeDatePaint.measureText(labelStr);
            int posX = nowX - (nowLabelWidth / 2);
            if ((nowLabelWidth / 2) + posX >= this.mChartAreaRight) {
                posX = this.mChartAreaRight - nowLabelWidth;
            }
            if (posX < this.mChartAreaLeft) {
                posX = this.mChartAreaLeft;
            }
            this.mNowLabel = new TimeLabel(this.mTimeDatePaint, posX, this.mLevelBottom + this.mCustomDateXYMargin, labelStr);
            canvas.drawText(this.mNowLabel.label, (float) this.mNowLabel.x, (float) this.mNowLabel.y, this.mTimeDatePaint);
            return;
        }
        HwLog.i(TAG, "drawNowLabel is error, currTime is below with chart start time");
    }

    private void drawDateLabel(Canvas canvas) {
        int tempPadding = dp2px(getContext(), 0.3f);
        if (this.mDateLabels.size() > 0) {
            int yBottom = this.mLevelBottom;
            for (int i = this.mDateLabels.size() - 1; i >= 0; i--) {
                DateLabel label = (DateLabel) this.mDateLabels.get(i);
                int x = label.x <= this.mChartAreaLeft ? this.mChartAreaLeft : label.x;
                if (this.mNowLabel != null && (x >= (this.mNowLabel.x + this.mNowLabel.width) + this.LINE_WIDTH || label.width + x <= this.mNowLabel.x + this.LINE_WIDTH)) {
                    canvas.drawText(label.label, (float) x, (float) (((this.mCustomDateXYMargin + yBottom) + label.height) + tempPadding), this.mTimeDatePaint);
                }
            }
        }
    }

    private String fomatTimeString(long startTime) {
        String timeString = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), "HH:mm"), startTime).toString();
        HwLog.i(TAG, "startTimeString =" + timeString);
        return timeString;
    }

    private void drawTimeLabel(Canvas canvas) {
        int tempPadding = dp2px(getContext(), 0.7f);
        addTimeLabel(this.mTimeDatePaint, this.mChartAreaLeft, this.mLevelBottom + this.mCustomDateXYMargin, fomatTimeString(getChartStartTime()));
        int i;
        TimeLabel label;
        if (this.mDateLabels.size() != 0 || this.mTimeLabels.size() <= 0) {
            if (this.mTimeLabels.size() <= 0 || this.mDateLabels.size() <= 0) {
                HwLog.i(TAG, "error mTimeLabels or mDateLabels is error.");
            } else {
                for (i = this.mTimeLabels.size() - 1; i >= 0; i--) {
                    label = (TimeLabel) this.mTimeLabels.get(i);
                    if (i == 0 && this.mNowLabel != null && this.mNowLabel.x - label.x >= label.width + this.LINE_WIDTH && ((DateLabel) this.mDateLabels.get(0)).x - label.x >= label.width + this.LINE_WIDTH) {
                        canvas.drawText(label.label, (float) label.x, (float) (label.y + tempPadding), this.mTimeDatePaint);
                    }
                }
            }
            return;
        }
        for (i = this.mTimeLabels.size() - 1; i >= 0; i--) {
            if (i == 0) {
                label = (TimeLabel) this.mTimeLabels.get(i);
                if (this.mNowLabel != null && this.mNowLabel.x - label.x >= label.width + this.LINE_WIDTH) {
                    canvas.drawText(label.label, (float) label.x, (float) (label.y + tempPadding), this.mTimeDatePaint);
                }
            }
        }
    }

    private int dp2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    private void finishChartPath(int w, int levelh, int startX, int y, Path curLevelPath, int lastX) {
        if (curLevelPath != null) {
            if (lastX >= 0 && lastX < w) {
                curLevelPath.lineTo((float) w, (float) y);
                this.mChartLinePath.lineTo((float) w, (float) y);
            }
            curLevelPath.lineTo((float) w, (float) (this.mLevelTop + levelh));
            curLevelPath.lineTo((float) startX, (float) (this.mLevelTop + levelh));
            curLevelPath.close();
        }
    }

    private void drawXAndPercentScale(Canvas canvas) {
        canvas.drawLine((float) this.mLevelLeft, (float) this.mLevelBottom, (float) this.mLevelRight, (float) this.mLevelBottom, this.mXLinePaint);
        if (this.mPercentScaleLabels.size() == 3) {
            canvas.drawText(((PercentScaleLabel) this.mPercentScaleLabels.get(0)).label, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(0)).x, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(0)).y, this.mPercentScalePaint);
            if (Math.abs(((PercentScaleLabel) this.mPercentScaleLabels.get(1)).y - ((PercentScaleLabel) this.mPercentScaleLabels.get(0)).y) >= ((PercentScaleLabel) this.mPercentScaleLabels.get(1)).height + this.LINE_WIDTH) {
                canvas.drawText(((PercentScaleLabel) this.mPercentScaleLabels.get(1)).label, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(1)).x, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(1)).y, this.mPercentScalePaint);
                canvas.drawLine((float) (this.mLevelLeft - this.LINE_WIDTH), (float) (((PercentScaleLabel) this.mPercentScaleLabels.get(1)).y - (((PercentScaleLabel) this.mPercentScaleLabels.get(1)).height / 2)), (float) ((this.mLevelRight - ((PercentScaleLabel) this.mPercentScaleLabels.get(1)).width) - this.LINE_WIDTH), (float) (((PercentScaleLabel) this.mPercentScaleLabels.get(1)).y - (((PercentScaleLabel) this.mPercentScaleLabels.get(1)).height / 2)), this.mXLinePaint);
            }
            if (Math.abs(((PercentScaleLabel) this.mPercentScaleLabels.get(2)).y - ((PercentScaleLabel) this.mPercentScaleLabels.get(0)).y) >= ((PercentScaleLabel) this.mPercentScaleLabels.get(2)).height + this.LINE_WIDTH) {
                canvas.drawText(((PercentScaleLabel) this.mPercentScaleLabels.get(2)).label, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(2)).x, (float) ((PercentScaleLabel) this.mPercentScaleLabels.get(2)).y, this.mPercentScalePaint);
                canvas.drawLine((float) (this.mLevelLeft - this.LINE_WIDTH), (float) (((PercentScaleLabel) this.mPercentScaleLabels.get(2)).y - (((PercentScaleLabel) this.mPercentScaleLabels.get(2)).height / 2)), (float) ((this.mLevelRight - ((PercentScaleLabel) this.mPercentScaleLabels.get(2)).width) - this.LINE_WIDTH), (float) (((PercentScaleLabel) this.mPercentScaleLabels.get(2)).y - (((PercentScaleLabel) this.mPercentScaleLabels.get(2)).height / 2)), this.mXLinePaint);
            }
        }
    }

    private void addTimeLabel(TextPaint paint, int mLabelStart, int mLabelTop, String label) {
        this.mTimeLabels.add(new TimeLabel(paint, mLabelStart, mLabelTop, label));
    }

    private void addDateLabel(Calendar cal, int levelLeft, int levelRight, boolean isDayFirst) {
        this.mDateLabels.add(new DateLabel(this.mTimeDatePaint, ((int) (((cal.getTimeInMillis() - this.mChartStartWallTime) * ((long) (levelRight - levelLeft))) / TWO_DAY_IN_MILLI_SECONDS)) + levelLeft, cal, isDayFirst));
    }

    private void addPercentScaleLabel(TextPaint paint, int mChartEnd, int mChartTop, int mChartHeight, int percent) {
        this.mPercentScaleLabels.add(new PercentScaleLabel(paint, mChartEnd, mChartTop, mChartHeight, percent));
    }
}
