package com.huawei.systemmanager.power.batterychart;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.FontMetrics;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.os.BatteryStats;
import android.os.BatteryStats.HistoryItem;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;

public class BatteryHistoryText extends View {
    private static final long FIVE_MINUTES_IN_MILLI_SECONDS = 300000;
    private static final long MAX_DAYS_IN_MILLI_SECONDS = 15552000000L;
    private static final long ONE_HOUR_IN_MILLI_SECONDS = 3600000;
    private static final String TAG = "BatteryHistoryText";
    private static final long TWO_DAY_IN_MICRO_SECONDS = 172800000;
    private final int CHART_AREA_RIGHT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_area_right_padding));
    private final int ITEM_HEIGHT = ((int) getResources().getDimension(R.dimen.battery_history_text_item_height));
    private final int LEFT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_left_padding));
    private final int LINE_WIDTH = ((int) getResources().getDimension(R.dimen.battery_history_text_line_width));
    private final int RIGHT_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_chart_right_margin));
    private final int TEXT_LINE_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_text_line_margin));
    private final int TEXT_TOP_PADDING = ((int) getResources().getDimension(R.dimen.battery_history_text_top_margin));
    private Paint mBatteryLinePaint;
    private int mBatteryLineTopMargin;
    private long mBinFileStartWallTime;
    private String mCameraOnLabel;
    private Path mCameraOnPath = new Path();
    private int mCameraOnY;
    private String mChargingLabel;
    private Path mChargingPath = new Path();
    private int mChargingY;
    private long mChartStartWallTime;
    private String mCpuRunningLabel;
    private Path mCpuRunningPath = new Path();
    private int mCpuRunningY;
    private int mCustomTextTopMargin;
    private long mEndWallTime;
    private int mEndY;
    private String mFlashlightOnLabel;
    private Path mFlashlightOnPath = new Path();
    private int mFlashlightOnY;
    private String mGpsOnLabel;
    private Path mGpsOnPath = new Path();
    private int mGpsOnY;
    private boolean mHaveCamera;
    private boolean mHaveFlashlight;
    private boolean mHaveGps;
    private boolean mHavePhoneSignal;
    private boolean mHaveWifi;
    private long mHistEnd;
    private long mHistStart;
    private int mLevelLeft;
    private int mLevelRight;
    private int mNumHist;
    private ChartData mPhoneSignalChart;
    private String mPhoneSignalLabel;
    private int mPhoneSignalY;
    private String mScreenOnLabel;
    private Path mScreenOnPath = new Path();
    private int mScreenOnY;
    private BatteryStats mStats;
    private TextPaint mTextPaint;
    private String mWifiRunningLabel;
    private Path mWifiRunningPath = new Path();
    private int mWifiRunningY;

    public BatteryHistoryText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initBatteryPaint();
        initTextPaint();
        initLabel();
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawContext(canvas, getWidth());
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        resetPath();
        resetLevel(w);
        resetOffset(w);
        long wallTimeStart = this.mChartStartWallTime;
        long curWallTime = this.mBinFileStartWallTime;
        long lastRealTime = 0;
        int levelWidth = this.mLevelRight - this.mLevelLeft;
        int x = this.mLevelLeft;
        int i = 0;
        boolean curLevelPathFlag = false;
        boolean z = false;
        boolean z2 = false;
        boolean z3 = false;
        boolean z4 = false;
        boolean z5 = false;
        boolean z6 = false;
        boolean z7 = false;
        int N = this.mNumHist;
        if (this.mEndWallTime > this.mBinFileStartWallTime && this.mStats.startIteratingHistoryLocked()) {
            HistoryItem rec = new HistoryItem();
            while (this.mStats.getNextHistoryLocked(rec) && i < N) {
                if (rec.isDeltaData()) {
                    curWallTime += rec.time - lastRealTime;
                    lastRealTime = rec.time;
                    if (curWallTime >= this.mChartStartWallTime) {
                        if (curWallTime >= this.mEndWallTime) {
                            curWallTime = this.mEndWallTime;
                        }
                        long delta = curWallTime - wallTimeStart;
                        if (delta > TWO_DAY_IN_MICRO_SECONDS) {
                            delta = TWO_DAY_IN_MICRO_SECONDS;
                        }
                        x = this.mLevelLeft + ((int) ((((long) levelWidth) * delta) / TWO_DAY_IN_MICRO_SECONDS));
                        if (!curLevelPathFlag) {
                            curLevelPathFlag = true;
                        }
                        boolean charging = (rec.states & 524288) != 0;
                        if (charging != z) {
                            if (charging) {
                                this.mChargingPath.moveTo((float) x, (float) (this.mChargingY + this.mBatteryLineTopMargin));
                            } else {
                                this.mChargingPath.lineTo((float) x, (float) (this.mChargingY + this.mBatteryLineTopMargin));
                            }
                            z = charging;
                        }
                        boolean screenOn = (rec.states & 1048576) != 0;
                        if (screenOn != z2) {
                            if (screenOn) {
                                this.mScreenOnPath.moveTo((float) x, (float) (this.mScreenOnY + this.mBatteryLineTopMargin));
                            } else {
                                this.mScreenOnPath.lineTo((float) x, (float) (this.mScreenOnY + this.mBatteryLineTopMargin));
                            }
                            z2 = screenOn;
                        }
                        boolean gpsOn = (rec.states & 536870912) != 0;
                        if (gpsOn != z3) {
                            if (gpsOn) {
                                this.mGpsOnPath.moveTo((float) x, (float) (this.mGpsOnY + this.mBatteryLineTopMargin));
                            } else {
                                this.mGpsOnPath.lineTo((float) x, (float) (this.mGpsOnY + this.mBatteryLineTopMargin));
                            }
                            z3 = gpsOn;
                        }
                        boolean flashlightOn = (rec.states2 & 134217728) != 0;
                        if (flashlightOn != z4) {
                            if (flashlightOn) {
                                this.mFlashlightOnPath.moveTo((float) x, (float) (this.mFlashlightOnY + this.mBatteryLineTopMargin));
                            } else {
                                this.mFlashlightOnPath.lineTo((float) x, (float) (this.mFlashlightOnY + this.mBatteryLineTopMargin));
                            }
                            z4 = flashlightOn;
                        }
                        boolean cameraOn = (rec.states2 & 2097152) != 0;
                        if (cameraOn != z5) {
                            if (cameraOn) {
                                this.mCameraOnPath.moveTo((float) x, (float) (this.mCameraOnY + this.mBatteryLineTopMargin));
                            } else {
                                this.mCameraOnPath.lineTo((float) x, (float) (this.mCameraOnY + this.mBatteryLineTopMargin));
                            }
                            z5 = cameraOn;
                        }
                        boolean wifiRunning = (rec.states2 & 536870912) != 0;
                        if (wifiRunning != z6) {
                            if (wifiRunning) {
                                this.mWifiRunningPath.moveTo((float) x, (float) (this.mWifiRunningY + this.mBatteryLineTopMargin));
                            } else {
                                this.mWifiRunningPath.lineTo((float) x, (float) (this.mWifiRunningY + this.mBatteryLineTopMargin));
                            }
                            z6 = wifiRunning;
                        }
                        boolean cpuRunning = (rec.states & Integer.MIN_VALUE) != 0;
                        if (cpuRunning != z7) {
                            if (cpuRunning) {
                                this.mCpuRunningPath.moveTo((float) x, (float) (this.mCpuRunningY + this.mBatteryLineTopMargin));
                            } else {
                                this.mCpuRunningPath.lineTo((float) x, (float) (this.mCpuRunningY + this.mBatteryLineTopMargin));
                            }
                            z7 = cpuRunning;
                        }
                        if (this.mHavePhoneSignal) {
                            int bin;
                            if (((rec.states & 448) >> 6) == 3) {
                                bin = 0;
                            } else if ((rec.states & 2097152) != 0) {
                                bin = 1;
                            } else {
                                bin = ((rec.states & 56) >> 3) + 2;
                            }
                            this.mPhoneSignalChart.addTick(x, bin);
                        }
                    }
                } else {
                    long lastWallTime = curWallTime;
                    if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                        if (rec.currentTime >= this.mBinFileStartWallTime) {
                            curWallTime = rec.currentTime;
                        } else {
                            curWallTime = this.mBinFileStartWallTime + (rec.time - this.mHistStart);
                        }
                        lastRealTime = rec.time;
                    }
                    if (rec.cmd != (byte) 6 && ((rec.cmd != (byte) 5 || Math.abs(lastWallTime - curWallTime) > 3600000) && curLevelPathFlag)) {
                        finishPaths(x + 1, z, z2, z3, z4, z5, z6, z7);
                        curLevelPathFlag = false;
                        z6 = false;
                        z7 = false;
                        z5 = false;
                        z4 = false;
                        z3 = false;
                        z2 = false;
                        z = false;
                    }
                }
                i++;
            }
            this.mStats.finishIteratingHistoryLocked();
        }
        finishPaths(x + 1, z, z2, z3, z4, z5, z6, z7);
    }

    public void setData(BatteryStats stats) {
        if (stats == null) {
            Log.d(TAG, "setData , arg is wrong");
            return;
        }
        this.mStats = stats;
        int pos = 0;
        int lastInteresting = 0;
        long lastWallTime = 0;
        long lastRealtime = 0;
        byte lastLevel = (byte) -1;
        boolean first = true;
        int aggrStates = 0;
        int aggrStates2 = 0;
        if (stats.startIteratingHistoryLocked()) {
            HistoryItem rec = new HistoryItem();
            while (stats.getNextHistoryLocked(rec)) {
                pos++;
                if (first) {
                    this.mHistStart = rec.time;
                    first = false;
                }
                if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                    if (rec.currentTime > MAX_DAYS_IN_MILLI_SECONDS + lastWallTime || rec.time < this.mHistStart + 300000) {
                        this.mBinFileStartWallTime = 0;
                    }
                    lastRealtime = rec.time;
                    lastWallTime = rec.currentTime;
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
                    aggrStates |= rec.states;
                    aggrStates2 |= rec.states2;
                }
            }
        }
        this.mNumHist = lastInteresting;
        this.mEndWallTime = (this.mHistEnd + lastWallTime) - lastRealtime;
        if (this.mEndWallTime - this.mBinFileStartWallTime <= TWO_DAY_IN_MICRO_SECONDS) {
            HwLog.i(TAG, "setData, it is in two days");
            this.mChartStartWallTime = this.mBinFileStartWallTime;
        } else {
            HwLog.i(TAG, "setData, it is not in two days");
            this.mChartStartWallTime = this.mEndWallTime - TWO_DAY_IN_MICRO_SECONDS;
        }
        this.mHaveGps = (536870912 & aggrStates) != 0;
        this.mHaveFlashlight = (134217728 & aggrStates2) != 0;
        this.mHaveCamera = (2097152 & aggrStates2) != 0;
        boolean z = (536870912 & aggrStates2) == 0 ? (402718720 & aggrStates) != 0 : true;
        this.mHaveWifi = z;
        if (!BatterHistoryUtils.isWifiOnly(getContext())) {
            this.mHavePhoneSignal = true;
        }
    }

    private void resetPath() {
        this.mScreenOnPath.reset();
        this.mGpsOnPath.reset();
        this.mFlashlightOnPath.reset();
        this.mCameraOnPath.reset();
        this.mWifiRunningPath.reset();
        this.mCpuRunningPath.reset();
        this.mChargingPath.reset();
    }

    private void initBatteryPaint() {
        this.mBatteryLinePaint = new Paint();
        this.mBatteryLinePaint.setColor(getResources().getColor(R.color.emui5_theme));
        this.mBatteryLinePaint.setStyle(Style.STROKE);
        this.mBatteryLinePaint.setStrokeWidth((float) this.LINE_WIDTH);
        this.mPhoneSignalChart = new ChartData();
    }

    private void initTextPaint() {
        this.mTextPaint = new TextPaint(1);
        this.mTextPaint.setTextSize((float) getResources().getDimensionPixelSize(R.dimen.battery_history_text_size));
        this.mTextPaint.setColor(GlobalContext.getContext().getResources().getColor(R.color.emui_list_primary_text));
        FontMetrics fontMetrics = this.mTextPaint.getFontMetrics();
        int textHight = (int) (Math.abs(fontMetrics.descent) + Math.abs(fontMetrics.ascent));
        this.mCustomTextTopMargin = this.TEXT_TOP_PADDING + ((int) Math.abs(this.mTextPaint.ascent()));
        this.mBatteryLineTopMargin = (this.TEXT_TOP_PADDING + textHight) + this.TEXT_LINE_PADDING;
    }

    private void initLabel() {
        this.mChargingLabel = getContext().getString(R.string.battery_stats_charging_label);
        this.mScreenOnLabel = getContext().getString(R.string.battery_stats_screen_on_label);
        this.mGpsOnLabel = getContext().getString(R.string.battery_stats_gps_on_label);
        this.mCameraOnLabel = getContext().getString(R.string.battery_stats_camera_on_label);
        this.mFlashlightOnLabel = getContext().getString(R.string.battery_stats_flashlight_on_label);
        this.mWifiRunningLabel = getContext().getString(R.string.WIFI);
        this.mCpuRunningLabel = getContext().getString(R.string.battery_stats_wake_lock_label);
        this.mPhoneSignalLabel = getContext().getString(R.string.battery_stats_phone_signal_label);
    }

    private void resetLevel(int w) {
        this.mLevelLeft = this.LEFT_PADDING;
        this.mLevelRight = (w - this.RIGHT_PADDING) - this.CHART_AREA_RIGHT_PADDING;
    }

    private void resetOffset(int w) {
        int i;
        int i2 = 0;
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.init(w);
        }
        this.mPhoneSignalY = 0;
        int i3 = this.mPhoneSignalY;
        if (this.mHavePhoneSignal) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mCameraOnY = i + i3;
        i3 = this.mCameraOnY;
        if (this.mHaveCamera) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mFlashlightOnY = i + i3;
        i3 = this.mFlashlightOnY;
        if (this.mHaveFlashlight) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mGpsOnY = i + i3;
        i3 = this.mGpsOnY;
        if (this.mHaveGps) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mWifiRunningY = i + i3;
        i = this.mWifiRunningY;
        if (this.mHaveWifi) {
            i2 = this.ITEM_HEIGHT;
        }
        this.mCpuRunningY = i + i2;
        this.mScreenOnY = this.mCpuRunningY + this.ITEM_HEIGHT;
        this.mChargingY = this.mScreenOnY + this.ITEM_HEIGHT;
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.init(w);
        }
        this.mEndY = this.mChargingY + this.ITEM_HEIGHT;
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int i;
        int i2 = 0;
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        this.mPhoneSignalY = 0;
        int i3 = this.mPhoneSignalY;
        if (this.mHavePhoneSignal) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mCameraOnY = i + i3;
        i3 = this.mCameraOnY;
        if (this.mHaveCamera) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mFlashlightOnY = i + i3;
        i3 = this.mFlashlightOnY;
        if (this.mHaveFlashlight) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mGpsOnY = i + i3;
        i3 = this.mGpsOnY;
        if (this.mHaveGps) {
            i = this.ITEM_HEIGHT;
        } else {
            i = 0;
        }
        this.mWifiRunningY = i + i3;
        i = this.mWifiRunningY;
        if (this.mHaveWifi) {
            i2 = this.ITEM_HEIGHT;
        }
        this.mCpuRunningY = i + i2;
        this.mScreenOnY = this.mCpuRunningY + this.ITEM_HEIGHT;
        this.mChargingY = this.mScreenOnY + this.ITEM_HEIGHT;
        this.mEndY = this.mChargingY + this.ITEM_HEIGHT;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(this.mEndY, heightMeasureSpec));
    }

    private void drawContext(Canvas canvas, int width) {
        boolean layoutRtl = isLayoutRtl();
        int textStartX = layoutRtl ? width - this.RIGHT_PADDING : this.LEFT_PADDING;
        this.mTextPaint.setTextAlign(layoutRtl ? Align.RIGHT : Align.LEFT);
        if (this.mHavePhoneSignal) {
            canvas.drawText(this.mPhoneSignalLabel, (float) textStartX, (float) (this.mPhoneSignalY + this.mCustomTextTopMargin), this.mTextPaint);
            this.mPhoneSignalChart.draw(canvas, (this.mPhoneSignalY + this.mBatteryLineTopMargin) - (this.LINE_WIDTH / 2), this.LINE_WIDTH);
        }
        if (this.mHaveCamera) {
            canvas.drawText(this.mCameraOnLabel, (float) textStartX, (float) (this.mCameraOnY + this.mCustomTextTopMargin), this.mTextPaint);
            if (!this.mCameraOnPath.isEmpty()) {
                canvas.drawPath(this.mCameraOnPath, this.mBatteryLinePaint);
            }
        }
        if (this.mHaveFlashlight) {
            canvas.drawText(this.mFlashlightOnLabel, (float) textStartX, (float) (this.mFlashlightOnY + this.mCustomTextTopMargin), this.mTextPaint);
            if (!this.mFlashlightOnPath.isEmpty()) {
                canvas.drawPath(this.mFlashlightOnPath, this.mBatteryLinePaint);
            }
        }
        if (this.mHaveGps) {
            canvas.drawText(this.mGpsOnLabel, (float) textStartX, (float) (this.mGpsOnY + this.mCustomTextTopMargin), this.mTextPaint);
            if (!this.mGpsOnPath.isEmpty()) {
                canvas.drawPath(this.mGpsOnPath, this.mBatteryLinePaint);
            }
        }
        if (this.mHaveWifi) {
            canvas.drawText(this.mWifiRunningLabel, (float) textStartX, (float) (this.mWifiRunningY + this.mCustomTextTopMargin), this.mTextPaint);
            if (!this.mWifiRunningPath.isEmpty()) {
                canvas.drawPath(this.mWifiRunningPath, this.mBatteryLinePaint);
            }
        }
        canvas.drawText(this.mCpuRunningLabel, (float) textStartX, (float) (this.mCpuRunningY + this.mCustomTextTopMargin), this.mTextPaint);
        if (!this.mCpuRunningPath.isEmpty()) {
            canvas.drawPath(this.mCpuRunningPath, this.mBatteryLinePaint);
        }
        canvas.drawText(this.mScreenOnLabel, (float) textStartX, (float) (this.mScreenOnY + this.mCustomTextTopMargin), this.mTextPaint);
        if (!this.mScreenOnPath.isEmpty()) {
            canvas.drawPath(this.mScreenOnPath, this.mBatteryLinePaint);
        }
        canvas.drawText(this.mChargingLabel, (float) textStartX, (float) (this.mChargingY + this.mCustomTextTopMargin), this.mTextPaint);
        if (!this.mChargingPath.isEmpty()) {
            canvas.drawPath(this.mChargingPath, this.mBatteryLinePaint);
        }
        this.mTextPaint.setTextAlign(Align.LEFT);
    }

    private void finishPaths(int w, boolean lastCharging, boolean lastScreenOn, boolean lastGpsOn, boolean lastFlashlightOn, boolean lastCameraOn, boolean lastWifiRunning, boolean lastCpuRunning) {
        if (lastCharging) {
            this.mChargingPath.lineTo((float) w, (float) (this.mChargingY + this.mBatteryLineTopMargin));
        }
        if (lastScreenOn) {
            this.mScreenOnPath.lineTo((float) w, (float) (this.mScreenOnY + this.mBatteryLineTopMargin));
        }
        if (lastGpsOn) {
            this.mGpsOnPath.lineTo((float) w, (float) (this.mGpsOnY + this.mBatteryLineTopMargin));
        }
        if (lastFlashlightOn) {
            this.mFlashlightOnPath.lineTo((float) w, (float) (this.mFlashlightOnY + this.mBatteryLineTopMargin));
        }
        if (lastCameraOn) {
            this.mCameraOnPath.lineTo((float) w, (float) (this.mCameraOnY + this.mBatteryLineTopMargin));
        }
        if (lastWifiRunning) {
            this.mWifiRunningPath.lineTo((float) w, (float) (this.mWifiRunningY + this.mBatteryLineTopMargin));
        }
        if (lastCpuRunning) {
            this.mCpuRunningPath.lineTo((float) w, (float) (this.mCpuRunningY + this.mBatteryLineTopMargin));
        }
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.finish(w);
        }
    }
}
