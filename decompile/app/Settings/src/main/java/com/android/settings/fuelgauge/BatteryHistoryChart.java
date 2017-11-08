package com.android.settings.fuelgauge;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Typeface;
import android.os.BatteryStats;
import android.os.BatteryStats.HistoryItem;
import android.text.TextPaint;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import com.android.internal.R;
import com.android.settings.R$styleable;
import com.android.settings.Utils;
import com.android.settingslib.BatteryInfo;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class BatteryHistoryChart extends View {
    final Path mBatCriticalPath = new Path();
    final Path mBatGoodPath = new Path();
    int mBatHigh;
    final Path mBatLevelPath = new Path();
    int mBatLow;
    final Path mBatWarnPath = new Path();
    final Paint mBatteryBackgroundPaint = new Paint(1);
    int mBatteryCriticalLevel = this.mContext.getResources().getInteger(17694805);
    final Paint mBatteryCriticalPaint = new Paint(1);
    final Paint mBatteryGoodPaint = new Paint(1);
    int mBatteryWarnLevel = this.mContext.getResources().getInteger(17694807);
    final Paint mBatteryWarnPaint = new Paint(1);
    Bitmap mBitmap;
    String mCameraOnLabel;
    int mCameraOnOffset;
    final Paint mCameraOnPaint = new Paint();
    final Path mCameraOnPath = new Path();
    Canvas mCanvas;
    String mChargeDurationString;
    int mChargeDurationStringWidth;
    int mChargeLabelStringWidth;
    String mChargingLabel;
    int mChargingOffset;
    final Paint mChargingPaint = new Paint();
    final Path mChargingPath = new Path();
    int mChartMinHeight;
    String mCpuRunningLabel;
    int mCpuRunningOffset;
    final Paint mCpuRunningPaint = new Paint();
    final Path mCpuRunningPath = new Path();
    private double mDSurplusTime;
    final ArrayList<DateLabel> mDateLabels = new ArrayList();
    final Paint mDateLinePaint = new Paint();
    final Path mDateLinePath = new Path();
    final Paint mDebugRectPaint = new Paint();
    String mDrainString;
    int mDrainStringWidth;
    String mDurationString;
    int mDurationStringWidth;
    long mEndDataWallTime;
    long mEndWallTime;
    float mEnlargerFactor = 2.2f;
    String mFlashlightOnLabel;
    int mFlashlightOnOffset;
    final Paint mFlashlightOnPaint = new Paint();
    final Path mFlashlightOnPath = new Path();
    String mGpsOnLabel;
    int mGpsOnOffset;
    final Paint mGpsOnPaint = new Paint();
    final Path mGpsOnPath = new Path();
    boolean mHaveCamera;
    boolean mHaveFlashlight;
    boolean mHaveGps;
    boolean mHavePhoneSignal;
    boolean mHaveWifi;
    int mHeaderHeight;
    int mHeaderTextAscent;
    int mHeaderTextDescent;
    final TextPaint mHeaderTextPaint = new TextPaint(1);
    long mHistStart;
    BatteryInfo mInfo;
    boolean mLargeMode;
    int mLastHeight = -1;
    int mLastWidth = -1;
    int mLevelBottom;
    int mLevelLeft;
    int mLevelOffset;
    int mLevelRight;
    int mLevelTop;
    int mLineWidth;
    String mMaxPercentLabelString;
    int mMaxPercentLabelStringWidth;
    String mMinPercentLabelString;
    int mMinPercentLabelStringWidth;
    int mNumHist;
    final ChartData mPhoneSignalChart = new ChartData();
    String mPhoneSignalLabel;
    int mPhoneSignalOffset;
    String mScreenOnLabel;
    int mScreenOnOffset;
    final Paint mScreenOnPaint = new Paint();
    final Path mScreenOnPath = new Path();
    long mStartWallTime;
    BatteryStats mStats;
    int mTextAscent;
    int mTextDescent;
    final TextPaint mTextPaint = new TextPaint(1);
    int mThinLineWidth = ((int) TypedValue.applyDimension(1, 2.0f, getResources().getDisplayMetrics()));
    private double mThresholdTime;
    final ArrayList<TimeLabel> mTimeLabels = new ArrayList();
    final Paint mTimeRemainPaint = new Paint(1);
    final Path mTimeRemainPath = new Path();
    String mWifiRunningLabel;
    int mWifiRunningOffset;
    final Paint mWifiRunningPaint = new Paint();
    final Path mWifiRunningPath = new Path();
    final Paint mXYPaint = new Paint(1);

    static class ChartData {
        int[] mColors;
        int mLastBin;
        int mNumTicks;
        Paint[] mPaints;
        int[] mTicks;

        ChartData() {
        }

        void setColors(int[] colors) {
            this.mColors = colors;
            this.mPaints = new Paint[colors.length];
            for (int i = 0; i < colors.length; i++) {
                this.mPaints[i] = new Paint();
                this.mPaints[i].setColor(colors[i]);
                this.mPaints[i].setStyle(Style.FILL);
            }
        }

        void init(int width) {
            if (width > 0) {
                this.mTicks = new int[(width * 2)];
            } else {
                this.mTicks = null;
            }
            this.mNumTicks = 0;
            this.mLastBin = 0;
        }

        void addTick(int x, int bin) {
            if (bin != this.mLastBin && this.mNumTicks < this.mTicks.length) {
                this.mTicks[this.mNumTicks] = (65535 & x) | (bin << 16);
                this.mNumTicks++;
                this.mLastBin = bin;
            }
        }

        void finish(int width) {
            if (this.mLastBin != 0) {
                addTick(width, 0);
            }
        }

        void draw(Canvas canvas, int top, int height) {
            int lastBin = 0;
            int lastX = 0;
            int bottom = top + height;
            for (int i = 0; i < this.mNumTicks; i++) {
                int tick = this.mTicks[i];
                int x = tick & 65535;
                int bin = (-65536 & tick) >> 16;
                if (lastBin != 0) {
                    canvas.drawRect((float) lastX, (float) top, (float) x, (float) bottom, this.mPaints[lastBin]);
                }
                lastBin = bin;
                lastX = x;
            }
        }
    }

    static class DateLabel {
        final String label;
        final int width;
        final int x;

        DateLabel(TextPaint paint, int x, Calendar cal, boolean dayFirst) {
            this.x = x;
            this.label = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), dayFirst ? "dM" : "Md"), cal).toString();
            this.width = (int) paint.measureText(this.label);
        }
    }

    static class TextAttrs {
        int styleIndex = -1;
        ColorStateList textColor = null;
        int textSize = 15;
        int typefaceIndex = -1;

        public void setTypeface(android.text.TextPaint r1, android.graphics.Typeface r2, int r3) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.settings.fuelgauge.BatteryHistoryChart.TextAttrs.setTypeface(android.text.TextPaint, android.graphics.Typeface, int):void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 6 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.settings.fuelgauge.BatteryHistoryChart.TextAttrs.setTypeface(android.text.TextPaint, android.graphics.Typeface, int):void");
        }

        TextAttrs() {
        }

        void retrieve(Context context, TypedArray from, int index) {
            TypedArray appearance = null;
            int ap = from.getResourceId(index, -1);
            if (ap != -1) {
                appearance = context.obtainStyledAttributes(ap, R.styleable.TextAppearance);
            }
            if (appearance != null) {
                int n = appearance.getIndexCount();
                for (int i = 0; i < n; i++) {
                    int attr = appearance.getIndex(i);
                    switch (attr) {
                        case 0:
                            this.textSize = appearance.getDimensionPixelSize(attr, this.textSize);
                            break;
                        case 1:
                            this.typefaceIndex = appearance.getInt(attr, -1);
                            break;
                        case 2:
                            this.styleIndex = appearance.getInt(attr, -1);
                            break;
                        case 3:
                            this.textColor = appearance.getColorStateList(attr);
                            break;
                        default:
                            break;
                    }
                }
                appearance.recycle();
            }
        }

        void apply(Context context, TextPaint paint) {
            paint.density = context.getResources().getDisplayMetrics().density;
            paint.setCompatibilityScaling(context.getResources().getCompatibilityInfo().applicationScale);
            paint.setColor(this.textColor.getDefaultColor());
            paint.setTextSize((float) this.textSize);
            Typeface tf = null;
            switch (this.typefaceIndex) {
                case 1:
                    tf = Typeface.SANS_SERIF;
                    break;
                case 2:
                    tf = Typeface.SERIF;
                    break;
                case 3:
                    tf = Typeface.MONOSPACE;
                    break;
            }
            setTypeface(paint, tf, this.styleIndex);
        }
    }

    static class TimeLabel {
        final String label;
        final int width;
        final int x;

        TimeLabel(TextPaint paint, int x, Calendar cal, boolean use24hr) {
            this.x = x;
            this.label = DateFormat.format(DateFormat.getBestDateTimePattern(Locale.getDefault(), use24hr ? "km" : "ha"), cal).toString();
            this.width = (int) paint.measureText(this.label);
        }
    }

    public BatteryHistoryChart(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mBatteryBackgroundPaint.setARGB(226, 226, 226, 226);
        this.mXYPaint.setARGB(160, 160, 160, 160);
        this.mBatteryBackgroundPaint.setStyle(Style.FILL);
        this.mBatteryGoodPaint.setARGB(128, 0, 128, 0);
        this.mBatteryGoodPaint.setStyle(Style.STROKE);
        this.mBatteryWarnPaint.setARGB(128, 128, 128, 0);
        this.mBatteryWarnPaint.setStyle(Style.STROKE);
        this.mBatteryCriticalPaint.setARGB(192, 128, 0, 0);
        this.mBatteryCriticalPaint.setStyle(Style.STROKE);
        this.mTimeRemainPaint.setColor(-3221573);
        this.mTimeRemainPaint.setStyle(Style.FILL);
        this.mChargingPaint.setARGB(255, 107, 167, 83);
        this.mChargingPaint.setStyle(Style.STROKE);
        this.mScreenOnPaint.setStyle(Style.STROKE);
        this.mGpsOnPaint.setStyle(Style.STROKE);
        this.mCameraOnPaint.setStyle(Style.STROKE);
        this.mFlashlightOnPaint.setStyle(Style.STROKE);
        this.mWifiRunningPaint.setStyle(Style.STROKE);
        this.mCpuRunningPaint.setStyle(Style.STROKE);
        this.mPhoneSignalChart.setColors(Utils.BADNESS_COLORS);
        this.mDebugRectPaint.setARGB(255, 255, 0, 0);
        this.mDebugRectPaint.setStyle(Style.STROKE);
        this.mScreenOnPaint.setColor(-16738680);
        this.mGpsOnPaint.setColor(-16738680);
        this.mCameraOnPaint.setColor(-16738680);
        this.mFlashlightOnPaint.setColor(-16738680);
        this.mWifiRunningPaint.setColor(-16738680);
        this.mCpuRunningPaint.setColor(-16738680);
        this.mChargingPaint.setColor(-16738680);
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.BatteryHistoryChart, 0, 0);
        TextAttrs mainTextAttrs = new TextAttrs();
        TextAttrs headTextAttrs = new TextAttrs();
        mainTextAttrs.retrieve(context, a, 0);
        headTextAttrs.retrieve(context, a, 9);
        int shadowcolor = 0;
        float dx = 0.0f;
        float dy = 0.0f;
        float r = 0.0f;
        int n = a.getIndexCount();
        for (int i = 0; i < n; i++) {
            int attr = a.getIndex(i);
            switch (attr) {
                case 1:
                    mainTextAttrs.textSize = a.getDimensionPixelSize(attr, mainTextAttrs.textSize);
                    headTextAttrs.textSize = a.getDimensionPixelSize(attr, headTextAttrs.textSize);
                    break;
                case 2:
                    mainTextAttrs.typefaceIndex = a.getInt(attr, mainTextAttrs.typefaceIndex);
                    headTextAttrs.typefaceIndex = a.getInt(attr, headTextAttrs.typefaceIndex);
                    break;
                case 3:
                    mainTextAttrs.styleIndex = a.getInt(attr, mainTextAttrs.styleIndex);
                    headTextAttrs.styleIndex = a.getInt(attr, headTextAttrs.styleIndex);
                    break;
                case 4:
                    mainTextAttrs.textColor = a.getColorStateList(attr);
                    headTextAttrs.textColor = a.getColorStateList(attr);
                    break;
                case 5:
                    shadowcolor = a.getInt(attr, 0);
                    break;
                case 6:
                    dx = a.getFloat(attr, 0.0f);
                    break;
                case 7:
                    dy = a.getFloat(attr, 0.0f);
                    break;
                case 8:
                    r = a.getFloat(attr, 0.0f);
                    break;
                case 10:
                    this.mBatteryBackgroundPaint.setColor(a.getInt(attr, 0));
                    this.mScreenOnPaint.setColor(a.getInt(attr, 0));
                    this.mGpsOnPaint.setColor(a.getInt(attr, 0));
                    this.mCameraOnPaint.setColor(a.getInt(attr, 0));
                    this.mFlashlightOnPaint.setColor(a.getInt(attr, 0));
                    this.mWifiRunningPaint.setColor(a.getInt(attr, 0));
                    this.mCpuRunningPaint.setColor(a.getInt(attr, 0));
                    this.mChargingPaint.setColor(a.getInt(attr, 0));
                    break;
                case 11:
                    this.mTimeRemainPaint.setColor(a.getInt(attr, 0));
                    break;
                case 12:
                    this.mChartMinHeight = a.getDimensionPixelSize(attr, 0);
                    break;
                default:
                    break;
            }
        }
        a.recycle();
        mainTextAttrs.apply(context, this.mTextPaint);
        headTextAttrs.apply(context, this.mHeaderTextPaint);
        this.mDateLinePaint.set(this.mTextPaint);
        this.mDateLinePaint.setStyle(Style.STROKE);
        int hairlineWidth = this.mThinLineWidth / 2;
        if (hairlineWidth < 1) {
            hairlineWidth = 1;
        }
        this.mDateLinePaint.setStrokeWidth((float) hairlineWidth);
        this.mDateLinePaint.setPathEffect(new DashPathEffect(new float[]{(float) (this.mThinLineWidth * 2), (float) (this.mThinLineWidth * 2)}, 0.0f));
        if (shadowcolor != 0) {
            this.mHeaderTextPaint.setShadowLayer(r, dx, dy, shadowcolor);
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        this.mMaxPercentLabelStringWidth = (int) this.mTextPaint.measureText(this.mMaxPercentLabelString);
        this.mMinPercentLabelStringWidth = (int) this.mTextPaint.measureText(this.mMinPercentLabelString);
        this.mDrainStringWidth = (int) this.mHeaderTextPaint.measureText(this.mDrainString);
        this.mChargeLabelStringWidth = (int) this.mHeaderTextPaint.measureText(this.mInfo.mChargeLabelString);
        this.mChargeDurationStringWidth = (int) this.mHeaderTextPaint.measureText(this.mChargeDurationString);
        this.mTextAscent = (int) this.mTextPaint.ascent();
        this.mTextDescent = (int) (this.mTextPaint.descent() * this.mEnlargerFactor);
        this.mHeaderTextAscent = (int) this.mHeaderTextPaint.ascent();
        this.mHeaderTextDescent = (int) this.mHeaderTextPaint.descent();
        this.mHeaderHeight = ((this.mHeaderTextDescent - this.mHeaderTextAscent) * 2) - this.mTextAscent;
        setMeasuredDimension(getDefaultSize(getSuggestedMinimumWidth(), widthMeasureSpec), getDefaultSize(this.mChartMinHeight + this.mHeaderHeight, heightMeasureSpec));
    }

    void finishPaths(int w, int h, int levelh, int startX, int y, Path curLevelPath, int lastX, boolean lastCharging, boolean lastScreenOn, boolean lastGpsOn, boolean lastFlashlightOn, boolean lastCameraOn, boolean lastWifiRunning, boolean lastCpuRunning, Path lastPath) {
        if (curLevelPath != null) {
            if (lastX >= 0 && lastX < w) {
                if (lastPath != null) {
                    lastPath.lineTo((float) w, (float) y);
                }
                curLevelPath.lineTo((float) w, (float) y);
            }
            curLevelPath.lineTo((float) w, (float) (this.mLevelTop + levelh));
            curLevelPath.lineTo((float) startX, (float) (this.mLevelTop + levelh));
            curLevelPath.close();
        }
        if (lastCharging) {
            this.mChargingPath.lineTo((float) w, (float) (h - this.mChargingOffset));
        }
        if (lastScreenOn) {
            this.mScreenOnPath.lineTo((float) w, (float) (h - this.mScreenOnOffset));
        }
        if (lastGpsOn) {
            this.mGpsOnPath.lineTo((float) w, (float) (h - this.mGpsOnOffset));
        }
        if (lastFlashlightOn) {
            this.mFlashlightOnPath.lineTo((float) w, (float) (h - this.mFlashlightOnOffset));
        }
        if (lastCameraOn) {
            this.mCameraOnPath.lineTo((float) w, (float) (h - this.mCameraOnOffset));
        }
        if (lastWifiRunning) {
            this.mWifiRunningPath.lineTo((float) w, (float) (h - this.mWifiRunningOffset));
        }
        if (lastCpuRunning) {
            this.mCpuRunningPath.lineTo((float) w, (float) (h - this.mCpuRunningOffset));
        }
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.finish(w);
        }
    }

    private boolean is24Hour() {
        return DateFormat.is24HourFormat(getContext());
    }

    private boolean isDayFirst() {
        String value = LocaleData.get(getResources().getConfiguration().locale).getDateFormat(3);
        return value.indexOf(77) > value.indexOf(100);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        if ((this.mLastWidth != w || this.mLastHeight != h) && this.mLastWidth != 0 && this.mLastHeight != 0) {
            byte value;
            Path path;
            this.mLastWidth = w;
            this.mLastHeight = h;
            this.mBitmap = null;
            this.mCanvas = null;
            int textHeight = this.mTextDescent - this.mTextAscent;
            if (h > (textHeight * 10) + this.mChartMinHeight) {
                this.mLargeMode = true;
                if (h > textHeight * 15) {
                    this.mLineWidth = textHeight / 6;
                } else {
                    this.mLineWidth = textHeight / 7;
                }
            } else {
                this.mLargeMode = false;
                this.mLineWidth = this.mThinLineWidth;
            }
            if (this.mLineWidth <= 0) {
                this.mLineWidth = 1;
            }
            this.mLevelTop = this.mHeaderHeight;
            this.mLevelLeft = this.mMaxPercentLabelStringWidth + (this.mThinLineWidth * 3);
            this.mLevelRight = w;
            int levelWidth = this.mLevelRight - this.mLevelLeft;
            this.mTextPaint.setStrokeWidth((float) this.mThinLineWidth);
            this.mBatteryGoodPaint.setStrokeWidth((float) this.mThinLineWidth);
            this.mBatteryWarnPaint.setStrokeWidth((float) this.mThinLineWidth);
            this.mBatteryCriticalPaint.setStrokeWidth((float) this.mThinLineWidth);
            this.mChargingPaint.setStrokeWidth((float) this.mLineWidth);
            this.mScreenOnPaint.setStrokeWidth((float) this.mLineWidth);
            this.mGpsOnPaint.setStrokeWidth((float) this.mLineWidth);
            this.mCameraOnPaint.setStrokeWidth((float) this.mLineWidth);
            this.mFlashlightOnPaint.setStrokeWidth((float) this.mLineWidth);
            this.mWifiRunningPaint.setStrokeWidth((float) this.mLineWidth);
            this.mCpuRunningPaint.setStrokeWidth((float) this.mLineWidth);
            this.mDebugRectPaint.setStrokeWidth(1.0f);
            int fullBarOffset = textHeight + this.mLineWidth;
            if (this.mLargeMode) {
                fullBarOffset += this.mLineWidth;
                this.mChargingOffset = this.mLineWidth;
                this.mScreenOnOffset = this.mChargingOffset + fullBarOffset;
                this.mCpuRunningOffset = this.mScreenOnOffset + fullBarOffset;
                this.mWifiRunningOffset = this.mCpuRunningOffset + fullBarOffset;
                this.mGpsOnOffset = (this.mHaveWifi ? fullBarOffset : 0) + this.mWifiRunningOffset;
                this.mFlashlightOnOffset = (this.mHaveGps ? fullBarOffset : 0) + this.mGpsOnOffset;
                this.mCameraOnOffset = (this.mHaveFlashlight ? fullBarOffset : 0) + this.mFlashlightOnOffset;
                this.mPhoneSignalOffset = (this.mHaveCamera ? fullBarOffset : 0) + this.mCameraOnOffset;
                this.mLevelOffset = ((((this.mHavePhoneSignal ? fullBarOffset : 0) + this.mPhoneSignalOffset) + (this.mLineWidth * 2)) + (this.mLineWidth / 2)) + textHeight;
                if (this.mHavePhoneSignal) {
                    this.mPhoneSignalChart.init(w);
                }
            } else {
                this.mPhoneSignalOffset = 0;
                this.mChargingOffset = 0;
                this.mCpuRunningOffset = 0;
                this.mWifiRunningOffset = 0;
                this.mFlashlightOnOffset = 0;
                this.mCameraOnOffset = 0;
                this.mGpsOnOffset = 0;
                this.mScreenOnOffset = 0;
                this.mLevelOffset = (this.mThinLineWidth * 4) + fullBarOffset;
                if (this.mHavePhoneSignal) {
                    this.mPhoneSignalChart.init(0);
                }
            }
            this.mBatLevelPath.reset();
            this.mBatGoodPath.reset();
            this.mBatWarnPath.reset();
            this.mTimeRemainPath.reset();
            this.mBatCriticalPath.reset();
            this.mScreenOnPath.reset();
            this.mGpsOnPath.reset();
            this.mFlashlightOnPath.reset();
            this.mCameraOnPath.reset();
            this.mWifiRunningPath.reset();
            this.mCpuRunningPath.reset();
            this.mChargingPath.reset();
            this.mTimeLabels.clear();
            this.mDateLabels.clear();
            long walltimeStart = this.mStartWallTime;
            long walltimeChange = this.mEndWallTime > walltimeStart ? this.mEndWallTime - walltimeStart : 1;
            long curWalltime = this.mStartWallTime;
            long lastRealtime = 0;
            int batLow = this.mBatLow;
            int batChange = this.mBatHigh - this.mBatLow;
            int levelh = (h - this.mLevelOffset) - this.mLevelTop;
            this.mLevelBottom = this.mLevelTop + levelh;
            int x = this.mLevelLeft;
            int i = 0;
            int startX = this.mLevelLeft;
            int lastX = -1;
            int lastY = -1;
            int i2 = 0;
            Path path2 = null;
            Path path3 = null;
            boolean z = false;
            boolean z2 = false;
            boolean z3 = false;
            boolean z4 = false;
            boolean z5 = false;
            boolean z6 = false;
            boolean z7 = false;
            int N = this.mNumHist;
            if (this.mEndDataWallTime > this.mStartWallTime && this.mStats.startIteratingHistoryLocked()) {
                HistoryItem rec = new HistoryItem();
                int x2 = x;
                while (this.mStats.getNextHistoryLocked(rec) && i2 < N) {
                    if (rec.isDeltaData()) {
                        curWalltime += rec.time - lastRealtime;
                        lastRealtime = rec.time;
                        x = this.mLevelLeft + ((int) (((curWalltime - walltimeStart) * ((long) levelWidth)) / walltimeChange));
                        if (x < 0) {
                            x = 0;
                        }
                        i = (this.mLevelTop + levelh) - (((rec.batteryLevel - batLow) * (levelh - 1)) / batChange);
                        if (!(lastX == x || lastY == i)) {
                            value = rec.batteryLevel;
                            if (value <= this.mBatteryCriticalLevel) {
                                path = this.mBatCriticalPath;
                            } else if (value <= this.mBatteryWarnLevel) {
                                path = this.mBatWarnPath;
                            } else {
                                path = null;
                            }
                            if (path != path3) {
                                if (path3 != null) {
                                    path3.lineTo((float) x, (float) i);
                                }
                                if (path != null) {
                                    path.moveTo((float) x, (float) i);
                                }
                                path3 = path;
                            } else if (path != null) {
                                path.lineTo((float) x, (float) i);
                            }
                            if (path2 == null) {
                                path2 = this.mBatLevelPath;
                                path2.moveTo((float) x, (float) i);
                                startX = x;
                            } else {
                                path2.lineTo((float) x, (float) i);
                            }
                            lastX = x;
                            lastY = i;
                        }
                        if (this.mLargeMode) {
                            boolean charging = (rec.states & 524288) != 0;
                            if (charging != z) {
                                if (charging) {
                                    this.mChargingPath.moveTo((float) x, (float) (h - this.mChargingOffset));
                                } else {
                                    this.mChargingPath.lineTo((float) x, (float) (h - this.mChargingOffset));
                                }
                                z = charging;
                            }
                            boolean screenOn = (rec.states & 1048576) != 0;
                            if (screenOn != z2) {
                                if (screenOn) {
                                    this.mScreenOnPath.moveTo((float) x, (float) (h - this.mScreenOnOffset));
                                } else {
                                    this.mScreenOnPath.lineTo((float) x, (float) (h - this.mScreenOnOffset));
                                }
                                z2 = screenOn;
                            }
                            boolean gpsOn = (rec.states & 536870912) != 0;
                            if (gpsOn != z3) {
                                if (gpsOn) {
                                    this.mGpsOnPath.moveTo((float) x, (float) (h - this.mGpsOnOffset));
                                } else {
                                    this.mGpsOnPath.lineTo((float) x, (float) (h - this.mGpsOnOffset));
                                }
                                z3 = gpsOn;
                            }
                            boolean flashlightOn = (rec.states2 & 134217728) != 0;
                            if (flashlightOn != z4) {
                                if (flashlightOn) {
                                    this.mFlashlightOnPath.moveTo((float) x, (float) (h - this.mFlashlightOnOffset));
                                } else {
                                    this.mFlashlightOnPath.lineTo((float) x, (float) (h - this.mFlashlightOnOffset));
                                }
                                z4 = flashlightOn;
                            }
                            boolean cameraOn = (rec.states2 & 2097152) != 0;
                            if (cameraOn != z5) {
                                if (cameraOn) {
                                    this.mCameraOnPath.moveTo((float) x, (float) (h - this.mCameraOnOffset));
                                } else {
                                    this.mCameraOnPath.lineTo((float) x, (float) (h - this.mCameraOnOffset));
                                }
                                z5 = cameraOn;
                            }
                            boolean wifiRunning = (rec.states2 & 536870912) != 0;
                            if (wifiRunning != z6) {
                                if (wifiRunning) {
                                    this.mWifiRunningPath.moveTo((float) x, (float) (h - this.mWifiRunningOffset));
                                } else {
                                    this.mWifiRunningPath.lineTo((float) x, (float) (h - this.mWifiRunningOffset));
                                }
                                z6 = wifiRunning;
                            }
                            boolean cpuRunning = (rec.states & Integer.MIN_VALUE) != 0;
                            if (cpuRunning != z7) {
                                if (cpuRunning) {
                                    this.mCpuRunningPath.moveTo((float) x, (float) (h - this.mCpuRunningOffset));
                                } else {
                                    this.mCpuRunningPath.lineTo((float) x, (float) (h - this.mCpuRunningOffset));
                                }
                                z7 = cpuRunning;
                            }
                            if (this.mLargeMode && this.mHavePhoneSignal) {
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
                        long lastWalltime = curWalltime;
                        if (rec.cmd == (byte) 5 || rec.cmd == (byte) 7) {
                            if (rec.currentTime >= this.mStartWallTime) {
                                curWalltime = rec.currentTime;
                            } else {
                                curWalltime = this.mStartWallTime + (rec.time - this.mHistStart);
                            }
                            lastRealtime = rec.time;
                        }
                        if (rec.cmd == (byte) 6 || (rec.cmd == (byte) 5 && Math.abs(lastWalltime - curWalltime) <= 3600000)) {
                            x = x2;
                        } else if (path2 != null) {
                            finishPaths(x2 + 1, h, levelh, startX, lastY, path2, lastX, z, z2, z3, z4, z5, z6, z7, path3);
                            lastY = -1;
                            lastX = -1;
                            path2 = null;
                            path3 = null;
                            z7 = false;
                            z5 = false;
                            z4 = false;
                            z3 = false;
                            z2 = false;
                            z = false;
                            x = x2;
                        } else {
                            x = x2;
                        }
                    }
                    i2++;
                    x2 = x;
                }
                this.mStats.finishIteratingHistoryLocked();
                x = x2;
            }
            if (lastY < 0 || lastX < 0) {
                lastX = this.mLevelLeft;
                x = lastX;
                lastY = (this.mLevelTop + levelh) - (((this.mInfo.mBatteryLevel - batLow) * (levelh - 1)) / batChange);
                i = lastY;
                value = (byte) this.mInfo.mBatteryLevel;
                if (value <= this.mBatteryCriticalLevel) {
                    path = this.mBatCriticalPath;
                } else if (value <= this.mBatteryWarnLevel) {
                    path = this.mBatWarnPath;
                } else {
                    path = null;
                }
                if (path != null) {
                    path.moveTo((float) lastX, (float) lastY);
                    path3 = path;
                }
                this.mBatLevelPath.moveTo((float) lastX, (float) lastY);
                path2 = this.mBatLevelPath;
                x = w;
            } else {
                x = this.mLevelLeft + ((int) (((this.mEndDataWallTime - walltimeStart) * ((long) levelWidth)) / walltimeChange));
                if (x < 0) {
                    x = 0;
                }
            }
            finishPaths(x, h, levelh, startX, lastY, path2, lastX, z, z2, z3, z4, z5, z6, z7, path3);
            if (x < w) {
                this.mTimeRemainPath.moveTo((float) x, (float) lastY);
                int fullY = (this.mLevelTop + levelh) - (((100 - batLow) * (levelh - 1)) / batChange);
                int emptyY = (this.mLevelTop + levelh) - (((0 - batLow) * (levelh - 1)) / batChange);
                if (this.mInfo.mDischarging) {
                    this.mTimeRemainPath.lineTo((float) this.mLevelRight, (float) emptyY);
                } else {
                    double rate = -1.0d;
                    if (this.mThresholdTime > 0.0d && this.mDSurplusTime > 0.0d) {
                        rate = this.mThresholdTime / this.mDSurplusTime;
                    }
                    if (rate > 0.0d && rate < 1.0d) {
                        int middleX = (int) (((double) x) + (((double) (this.mLevelRight - x)) * rate));
                        int middleY = (int) (((double) fullY) + (((double) (emptyY - fullY)) * 0.15000000000000002d));
                        if (middleY > i) {
                            middleY = i;
                        }
                        this.mTimeRemainPath.lineTo((float) middleX, (float) middleY);
                    }
                    this.mTimeRemainPath.lineTo((float) this.mLevelRight, (float) fullY);
                    this.mTimeRemainPath.lineTo((float) this.mLevelRight, (float) emptyY);
                }
                this.mTimeRemainPath.lineTo((float) x, (float) emptyY);
                this.mTimeRemainPath.close();
            }
            if (this.mStartWallTime > 0 && this.mEndWallTime > this.mStartWallTime) {
                Calendar calMid;
                long calMidMillis;
                boolean is24hr = is24Hour();
                Calendar calStart = Calendar.getInstance();
                calStart.setTimeInMillis(this.mStartWallTime);
                calStart.set(14, 0);
                calStart.set(13, 0);
                calStart.set(12, 0);
                long startRoundTime = calStart.getTimeInMillis();
                if (startRoundTime < this.mStartWallTime) {
                    calStart.set(11, calStart.get(11) + 1);
                    startRoundTime = calStart.getTimeInMillis();
                }
                Calendar calEnd = Calendar.getInstance();
                calEnd.setTimeInMillis(this.mEndWallTime);
                calEnd.set(14, 0);
                calEnd.set(13, 0);
                calEnd.set(12, 0);
                long endRoundTime = calEnd.getTimeInMillis();
                if (startRoundTime < endRoundTime) {
                    addTimeLabel(calStart, this.mLevelLeft, this.mLevelRight, is24hr);
                    calMid = Calendar.getInstance();
                    calMid.setTimeInMillis(this.mStartWallTime + ((this.mEndWallTime - this.mStartWallTime) / 2));
                    calMid.set(14, 0);
                    calMid.set(13, 0);
                    calMid.set(12, 0);
                    calMidMillis = calMid.getTimeInMillis();
                    if (calMidMillis > startRoundTime && calMidMillis < endRoundTime) {
                        addTimeLabel(calMid, this.mLevelLeft, this.mLevelRight, is24hr);
                    }
                    addTimeLabel(calEnd, this.mLevelLeft, this.mLevelRight, is24hr);
                }
                if (!(calStart.get(6) == calEnd.get(6) && calStart.get(1) == calEnd.get(1))) {
                    boolean isDayFirst = isDayFirst();
                    calStart.set(11, 0);
                    startRoundTime = calStart.getTimeInMillis();
                    if (startRoundTime < this.mStartWallTime) {
                        calStart.set(6, calStart.get(6) + 1);
                        startRoundTime = calStart.getTimeInMillis();
                    }
                    calEnd.set(11, 0);
                    endRoundTime = calEnd.getTimeInMillis();
                    if (startRoundTime < endRoundTime) {
                        addDateLabel(calStart, this.mLevelLeft, this.mLevelRight, isDayFirst);
                        calMid = Calendar.getInstance();
                        calMid.setTimeInMillis((((endRoundTime - startRoundTime) / 2) + startRoundTime) + 7200000);
                        calMid.set(11, 0);
                        calMid.set(12, 0);
                        calMidMillis = calMid.getTimeInMillis();
                        if (calMidMillis > startRoundTime && calMidMillis < endRoundTime) {
                            addDateLabel(calMid, this.mLevelLeft, this.mLevelRight, isDayFirst);
                        }
                    }
                    addDateLabel(calEnd, this.mLevelLeft, this.mLevelRight, isDayFirst);
                }
            }
            if (this.mTimeLabels.size() < 2) {
                this.mDurationString = Formatter.formatShortElapsedTime(getContext(), this.mEndWallTime - this.mStartWallTime);
                this.mDurationStringWidth = (int) this.mTextPaint.measureText(this.mDurationString);
            } else {
                this.mDurationString = null;
                this.mDurationStringWidth = 0;
            }
        }
    }

    void addTimeLabel(Calendar cal, int levelLeft, int levelRight, boolean is24hr) {
        long walltimeStart = this.mStartWallTime;
        this.mTimeLabels.add(new TimeLabel(this.mTextPaint, ((int) (((cal.getTimeInMillis() - walltimeStart) * ((long) (levelRight - levelLeft))) / (this.mEndWallTime - walltimeStart))) + levelLeft, cal, is24hr));
    }

    void addDateLabel(Calendar cal, int levelLeft, int levelRight, boolean isDayFirst) {
        long walltimeStart = this.mStartWallTime;
        this.mDateLabels.add(new DateLabel(this.mTextPaint, ((int) (((cal.getTimeInMillis() - walltimeStart) * ((long) (levelRight - levelLeft))) / (this.mEndWallTime - walltimeStart))) + levelLeft, cal, isDayFirst));
    }

    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawChart(canvas, getWidth(), getHeight());
    }

    void drawChart(Canvas canvas, int width, int height) {
        int y;
        int i;
        int x;
        int i2;
        boolean layoutRtl = isLayoutRtl();
        int textStartX = layoutRtl ? width : 0;
        int textEndX = layoutRtl ? 0 : width;
        Align textAlignLeft = layoutRtl ? Align.RIGHT : Align.LEFT;
        Align textAlignRight = layoutRtl ? Align.LEFT : Align.RIGHT;
        canvas.drawPath(this.mBatLevelPath, this.mBatteryBackgroundPaint);
        if (!this.mTimeRemainPath.isEmpty()) {
            canvas.drawPath(this.mTimeRemainPath, this.mTimeRemainPaint);
        }
        if (this.mTimeLabels.size() > 1) {
            y = (this.mLevelBottom - this.mTextAscent) + (this.mThinLineWidth * 4);
            int ytick = (this.mLevelBottom + this.mThinLineWidth) + (this.mThinLineWidth / 2);
            this.mTextPaint.setTextAlign(Align.LEFT);
            int lastX = 0;
            i = 0;
            while (i < this.mTimeLabels.size()) {
                TimeLabel label = (TimeLabel) this.mTimeLabels.get(i);
                if (i == 0) {
                    x = label.x - (label.width / 2);
                    if (x < 0) {
                        x = 0;
                    }
                    canvas.drawText(label.label, (float) x, (float) y, this.mTextPaint);
                    canvas.drawLine((float) label.x, (float) ytick, (float) label.x, (float) (this.mThinLineWidth + ytick), this.mTextPaint);
                    lastX = x + label.width;
                } else if (i < this.mTimeLabels.size() - 1) {
                    x = label.x - (label.width / 2);
                    if (x >= this.mTextAscent + lastX && x <= (width - ((TimeLabel) this.mTimeLabels.get(i + 1)).width) - this.mTextAscent) {
                        canvas.drawText(label.label, (float) x, (float) y, this.mTextPaint);
                        canvas.drawLine((float) label.x, (float) ytick, (float) label.x, (float) (this.mThinLineWidth + ytick), this.mTextPaint);
                        lastX = x + label.width;
                    }
                } else {
                    x = label.x - (label.width / 2);
                    if (label.width + x >= width) {
                        x = (width - 1) - label.width;
                    }
                    canvas.drawText(label.label, (float) x, (float) y, this.mTextPaint);
                    canvas.drawLine((float) label.x, (float) ytick, (float) label.x, (float) (this.mThinLineWidth + ytick), this.mTextPaint);
                }
                i++;
            }
        } else if (this.mDurationString != null) {
            y = (this.mLevelBottom - this.mTextAscent) + (this.mThinLineWidth * 4);
            this.mTextPaint.setTextAlign(Align.LEFT);
            canvas.drawText(this.mDurationString, (float) ((this.mLevelLeft + ((this.mLevelRight - this.mLevelLeft) / 2)) - (this.mDurationStringWidth / 2)), (float) y, this.mTextPaint);
        }
        int headerTop = (-this.mHeaderTextAscent) + ((this.mHeaderTextDescent - this.mHeaderTextAscent) / 3);
        this.mHeaderTextPaint.setTextAlign(textAlignLeft);
        canvas.drawText(this.mInfo.mChargeLabelString, (float) textStartX, (float) headerTop, this.mHeaderTextPaint);
        int stringHalfWidth = this.mChargeDurationStringWidth / 2;
        if (layoutRtl) {
            stringHalfWidth = -stringHalfWidth;
        }
        int i3 = ((width - this.mChargeDurationStringWidth) - this.mDrainStringWidth) / 2;
        if (layoutRtl) {
            i2 = this.mDrainStringWidth;
        } else {
            i2 = this.mChargeLabelStringWidth;
        }
        canvas.drawText(this.mChargeDurationString, (float) ((i3 + i2) - stringHalfWidth), (float) headerTop, this.mHeaderTextPaint);
        this.mHeaderTextPaint.setTextAlign(textAlignRight);
        canvas.drawText(this.mDrainString, (float) textEndX, (float) headerTop, this.mHeaderTextPaint);
        if (!this.mBatGoodPath.isEmpty()) {
            canvas.drawPath(this.mBatGoodPath, this.mBatteryGoodPaint);
        }
        if (!this.mBatWarnPath.isEmpty()) {
            canvas.drawPath(this.mBatWarnPath, this.mBatteryWarnPaint);
        }
        if (!this.mBatCriticalPath.isEmpty()) {
            canvas.drawPath(this.mBatCriticalPath, this.mBatteryCriticalPaint);
        }
        if (this.mHavePhoneSignal) {
            this.mPhoneSignalChart.draw(canvas, (height - this.mPhoneSignalOffset) - (this.mLineWidth / 2), this.mLineWidth);
        }
        if (!this.mScreenOnPath.isEmpty()) {
            canvas.drawPath(this.mScreenOnPath, this.mScreenOnPaint);
        }
        if (!this.mChargingPath.isEmpty()) {
            canvas.drawPath(this.mChargingPath, this.mChargingPaint);
        }
        if (this.mHaveGps && !this.mGpsOnPath.isEmpty()) {
            canvas.drawPath(this.mGpsOnPath, this.mGpsOnPaint);
        }
        if (this.mHaveFlashlight && !this.mFlashlightOnPath.isEmpty()) {
            canvas.drawPath(this.mFlashlightOnPath, this.mFlashlightOnPaint);
        }
        if (this.mHaveCamera && !this.mCameraOnPath.isEmpty()) {
            canvas.drawPath(this.mCameraOnPath, this.mCameraOnPaint);
        }
        if (this.mHaveWifi && !this.mWifiRunningPath.isEmpty()) {
            canvas.drawPath(this.mWifiRunningPath, this.mWifiRunningPaint);
        }
        if (!this.mCpuRunningPath.isEmpty()) {
            canvas.drawPath(this.mCpuRunningPath, this.mCpuRunningPaint);
        }
        if (this.mLargeMode) {
            Align align = this.mTextPaint.getTextAlign();
            this.mTextPaint.setTextAlign(textAlignLeft);
            if (this.mHavePhoneSignal) {
                canvas.drawText(this.mPhoneSignalLabel, (float) textStartX, (float) ((height - this.mPhoneSignalOffset) - this.mTextDescent), this.mTextPaint);
            }
            if (this.mHaveGps) {
                canvas.drawText(this.mGpsOnLabel, (float) textStartX, (float) ((height - this.mGpsOnOffset) - this.mTextDescent), this.mTextPaint);
            }
            if (this.mHaveFlashlight) {
                canvas.drawText(this.mFlashlightOnLabel, (float) textStartX, (float) ((height - this.mFlashlightOnOffset) - this.mTextDescent), this.mTextPaint);
            }
            if (this.mHaveCamera) {
                canvas.drawText(this.mCameraOnLabel, (float) textStartX, (float) ((height - this.mCameraOnOffset) - this.mTextDescent), this.mTextPaint);
            }
            if (this.mHaveWifi) {
                canvas.drawText(this.mWifiRunningLabel, (float) textStartX, (float) ((height - this.mWifiRunningOffset) - this.mTextDescent), this.mTextPaint);
            }
            canvas.drawText(this.mCpuRunningLabel, (float) textStartX, (float) ((height - this.mCpuRunningOffset) - this.mTextDescent), this.mTextPaint);
            canvas.drawText(this.mChargingLabel, (float) textStartX, (float) ((height - this.mChargingOffset) - this.mTextDescent), this.mTextPaint);
            canvas.drawText(this.mScreenOnLabel, (float) textStartX, (float) ((height - this.mScreenOnOffset) - this.mTextDescent), this.mTextPaint);
            this.mTextPaint.setTextAlign(align);
        }
        canvas.drawLine((float) (this.mLevelLeft - this.mThinLineWidth), (float) this.mLevelTop, (float) (this.mLevelLeft - this.mThinLineWidth), (float) (this.mLevelBottom + (this.mThinLineWidth / 2)), this.mXYPaint);
        if (this.mLargeMode) {
            for (i = 0; i < 10; i++) {
                y = (this.mLevelTop + (this.mThinLineWidth / 2)) + (((this.mLevelBottom - this.mLevelTop) * i) / 10);
                canvas.drawLine((float) ((this.mLevelLeft - (this.mThinLineWidth * 2)) - (this.mThinLineWidth / 2)), (float) y, (float) ((this.mLevelLeft - this.mThinLineWidth) - (this.mThinLineWidth / 2)), (float) y, this.mXYPaint);
            }
        }
        canvas.drawText(this.mMaxPercentLabelString, 0.0f, (float) this.mLevelTop, this.mTextPaint);
        canvas.drawText(this.mMinPercentLabelString, (float) (this.mMaxPercentLabelStringWidth - this.mMinPercentLabelStringWidth), (float) (this.mLevelBottom - this.mThinLineWidth), this.mTextPaint);
        canvas.drawLine((float) (this.mLevelLeft / 2), (float) (this.mLevelBottom + this.mThinLineWidth), (float) width, (float) (this.mLevelBottom + this.mThinLineWidth), this.mXYPaint);
        if (this.mDateLabels.size() > 0) {
            int ytop = this.mLevelTop + this.mTextAscent;
            int ybottom = this.mLevelBottom;
            int lastLeft = this.mLevelRight;
            this.mTextPaint.setTextAlign(Align.LEFT);
            for (i = this.mDateLabels.size() - 1; i >= 0; i--) {
                DateLabel label2 = (DateLabel) this.mDateLabels.get(i);
                int left = label2.x - this.mThinLineWidth;
                x = label2.x + (this.mThinLineWidth * 2);
                if (label2.width + x >= lastLeft) {
                    x = (label2.x - (this.mThinLineWidth * 2)) - label2.width;
                    left = x - this.mThinLineWidth;
                    if (left >= lastLeft) {
                    }
                }
                if (left >= this.mLevelLeft) {
                    this.mDateLinePath.reset();
                    this.mDateLinePath.moveTo((float) label2.x, (float) ytop);
                    this.mDateLinePath.lineTo((float) label2.x, (float) ybottom);
                    canvas.drawPath(this.mDateLinePath, this.mDateLinePaint);
                    canvas.drawText(label2.label, (float) x, (float) (ytop - this.mTextAscent), this.mTextPaint);
                }
            }
        }
    }
}
