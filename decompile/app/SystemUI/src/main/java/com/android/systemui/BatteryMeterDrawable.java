package com.android.systemui;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.ContentObserver;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.Op;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Handler;
import android.provider.Settings.System;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;

public class BatteryMeterDrawable extends Drawable implements BatteryStateChangeCallback {
    public static final String TAG = BatteryMeterDrawable.class.getSimpleName();
    private BatteryController mBatteryController;
    private final Paint mBatteryPaint;
    private final RectF mBoltFrame = new RectF();
    private final Paint mBoltPaint;
    private final Path mBoltPath = new Path();
    private final float[] mBoltPoints;
    private final RectF mButtonFrame = new RectF();
    private float mButtonHeightFraction;
    private int mChargeColor;
    private final Path mClipPath = new Path();
    private final int[] mColors;
    private final Context mContext;
    private final int mCriticalLevel;
    private int mDarkModeBackgroundColor;
    private int mDarkModeFillColor;
    private final RectF mFrame = new RectF();
    private final Paint mFramePaint;
    private final Handler mHandler;
    private int mHeight;
    private int mIconTint = -1;
    private final int mIntrinsicHeight;
    private final int mIntrinsicWidth;
    private int mLevel = -1;
    private int mLightModeBackgroundColor;
    private int mLightModeFillColor;
    private boolean mListening;
    private float mOldDarkIntensity = 0.0f;
    private boolean mPluggedIn;
    private final RectF mPlusFrame = new RectF();
    private final Paint mPlusPaint;
    private final Path mPlusPath = new Path();
    private final float[] mPlusPoints;
    private boolean mPowerSaveEnabled;
    private final SettingObserver mSettingObserver = new SettingObserver();
    private final Path mShapePath = new Path();
    private boolean mShowPercent;
    private float mSubpixelSmoothingLeft;
    private float mSubpixelSmoothingRight;
    private float mTextHeight;
    private final Paint mTextPaint;
    private final Path mTextPath = new Path();
    private String mWarningString;
    private float mWarningTextHeight;
    private final Paint mWarningTextPaint;
    private int mWidth;

    private final class SettingObserver extends ContentObserver {
        public SettingObserver() {
            super(new Handler());
        }

        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            BatteryMeterDrawable.this.updateShowPercent();
            BatteryMeterDrawable.this.postInvalidate();
        }
    }

    public BatteryMeterDrawable(Context context, Handler handler, int frameColor) {
        this.mContext = context;
        this.mHandler = handler;
        Resources res = context.getResources();
        TypedArray levels = res.obtainTypedArray(R.array.batterymeter_color_levels);
        TypedArray colors = res.obtainTypedArray(R.array.batterymeter_color_values);
        int N = levels.length();
        this.mColors = new int[(N * 2)];
        for (int i = 0; i < N; i++) {
            this.mColors[i * 2] = levels.getInt(i, 0);
            this.mColors[(i * 2) + 1] = colors.getColor(i, 0);
        }
        levels.recycle();
        colors.recycle();
        updateShowPercent();
        this.mWarningString = context.getString(R.string.battery_meter_very_low_overlay_symbol);
        this.mCriticalLevel = this.mContext.getResources().getInteger(17694805);
        this.mButtonHeightFraction = context.getResources().getFraction(R.fraction.battery_button_height_fraction, 1, 1);
        this.mSubpixelSmoothingLeft = context.getResources().getFraction(R.fraction.battery_subpixel_smoothing_left, 1, 1);
        this.mSubpixelSmoothingRight = context.getResources().getFraction(R.fraction.battery_subpixel_smoothing_right, 1, 1);
        this.mFramePaint = new Paint(1);
        this.mFramePaint.setColor(frameColor);
        this.mFramePaint.setDither(true);
        this.mFramePaint.setStrokeWidth(0.0f);
        this.mFramePaint.setStyle(Style.FILL_AND_STROKE);
        this.mBatteryPaint = new Paint(1);
        this.mBatteryPaint.setDither(true);
        this.mBatteryPaint.setStrokeWidth(0.0f);
        this.mBatteryPaint.setStyle(Style.FILL_AND_STROKE);
        this.mTextPaint = new Paint(1);
        this.mTextPaint.setTypeface(Typeface.create("sans-serif-condensed", 1));
        this.mTextPaint.setTextAlign(Align.CENTER);
        this.mWarningTextPaint = new Paint(1);
        this.mWarningTextPaint.setColor(this.mColors[1]);
        this.mWarningTextPaint.setTypeface(Typeface.create("sans-serif", 1));
        this.mWarningTextPaint.setTextAlign(Align.CENTER);
        this.mChargeColor = context.getColor(R.color.batterymeter_charge_color);
        this.mBoltPaint = new Paint(1);
        this.mBoltPaint.setColor(context.getColor(R.color.batterymeter_bolt_color));
        this.mBoltPoints = loadBoltPoints(res);
        this.mPlusPaint = new Paint(this.mBoltPaint);
        this.mPlusPoints = loadPlusPoints(res);
        this.mDarkModeBackgroundColor = context.getColor(R.color.dark_mode_icon_color_dual_tone_background);
        this.mDarkModeFillColor = context.getColor(R.color.dark_mode_icon_color_dual_tone_fill);
        this.mLightModeBackgroundColor = context.getColor(R.color.light_mode_icon_color_dual_tone_background);
        this.mLightModeFillColor = context.getColor(R.color.light_mode_icon_color_dual_tone_fill);
        this.mIntrinsicWidth = context.getResources().getDimensionPixelSize(R.dimen.battery_width);
        this.mIntrinsicHeight = context.getResources().getDimensionPixelSize(R.dimen.battery_height);
    }

    public int getIntrinsicHeight() {
        return this.mIntrinsicHeight;
    }

    public int getIntrinsicWidth() {
        return this.mIntrinsicWidth;
    }

    public void startListening() {
        this.mListening = true;
        this.mContext.getContentResolver().registerContentObserver(System.getUriFor("status_bar_show_battery_percent"), false, this.mSettingObserver);
        updateShowPercent();
        if (this.mBatteryController != null) {
            this.mBatteryController.addStateChangedCallback(this);
        }
    }

    public void stopListening() {
        this.mListening = false;
        this.mContext.getContentResolver().unregisterContentObserver(this.mSettingObserver);
        if (this.mBatteryController != null) {
            this.mBatteryController.removeStateChangedCallback(this);
        }
    }

    public void disableShowPercent() {
        this.mShowPercent = false;
        postInvalidate();
    }

    private void postInvalidate() {
        this.mHandler.post(new Runnable() {
            public void run() {
                BatteryMeterDrawable.this.invalidateSelf();
            }
        });
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        this.mLevel = level;
        this.mPluggedIn = pluggedIn;
        postInvalidate();
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
        this.mPowerSaveEnabled = isPowerSave;
        invalidateSelf();
    }

    private static float[] loadBoltPoints(Resources res) {
        int i;
        int[] pts = res.getIntArray(R.array.batterymeter_bolt_points);
        int maxX = 0;
        int maxY = 0;
        for (i = 0; i < pts.length; i += 2) {
            maxX = Math.max(maxX, pts[i]);
            maxY = Math.max(maxY, pts[i + 1]);
        }
        float[] ptsF = new float[pts.length];
        for (i = 0; i < pts.length; i += 2) {
            ptsF[i] = ((float) pts[i]) / ((float) maxX);
            ptsF[i + 1] = ((float) pts[i + 1]) / ((float) maxY);
        }
        return ptsF;
    }

    private static float[] loadPlusPoints(Resources res) {
        int i;
        int[] pts = res.getIntArray(R.array.batterymeter_plus_points);
        int maxX = 0;
        int maxY = 0;
        for (i = 0; i < pts.length; i += 2) {
            maxX = Math.max(maxX, pts[i]);
            maxY = Math.max(maxY, pts[i + 1]);
        }
        float[] ptsF = new float[pts.length];
        for (i = 0; i < pts.length; i += 2) {
            ptsF[i] = ((float) pts[i]) / ((float) maxX);
            ptsF[i + 1] = ((float) pts[i + 1]) / ((float) maxY);
        }
        return ptsF;
    }

    public void setBounds(int left, int top, int right, int bottom) {
        super.setBounds(left, top, right, bottom);
        this.mHeight = bottom - top;
        this.mWidth = right - left;
        this.mWarningTextPaint.setTextSize(((float) this.mHeight) * 0.75f);
        this.mWarningTextHeight = -this.mWarningTextPaint.getFontMetrics().ascent;
    }

    private void updateShowPercent() {
        boolean z = false;
        if (System.getInt(this.mContext.getContentResolver(), "status_bar_show_battery_percent", 0) != 0) {
            z = true;
        }
        this.mShowPercent = z;
    }

    private int getColorForLevel(int percent) {
        if (this.mPowerSaveEnabled) {
            return this.mColors[this.mColors.length - 1];
        }
        int color = 0;
        int i = 0;
        while (i < this.mColors.length) {
            int thresh = this.mColors[i];
            color = this.mColors[i + 1];
            if (percent > thresh) {
                i += 2;
            } else if (i == this.mColors.length - 2) {
                return this.mIconTint;
            } else {
                return color;
            }
        }
        return color;
    }

    public void draw(Canvas c) {
        int level = this.mLevel;
        if (level != -1) {
            float levelTop;
            float drawFrac = ((float) level) / 100.0f;
            int height = this.mHeight;
            int width = (int) (((float) this.mHeight) * 0.6551724f);
            int px = (this.mWidth - width) / 2;
            int buttonHeight = (int) (((float) height) * this.mButtonHeightFraction);
            this.mFrame.set(0.0f, 0.0f, (float) width, (float) height);
            this.mFrame.offset((float) px, 0.0f);
            this.mButtonFrame.set(this.mFrame.left + ((float) Math.round(((float) width) * 0.25f)), this.mFrame.top, this.mFrame.right - ((float) Math.round(((float) width) * 0.25f)), this.mFrame.top + ((float) buttonHeight));
            RectF rectF = this.mButtonFrame;
            rectF.top += this.mSubpixelSmoothingLeft;
            rectF = this.mButtonFrame;
            rectF.left += this.mSubpixelSmoothingLeft;
            rectF = this.mButtonFrame;
            rectF.right -= this.mSubpixelSmoothingRight;
            rectF = this.mFrame;
            rectF.top += (float) buttonHeight;
            rectF = this.mFrame;
            rectF.left += this.mSubpixelSmoothingLeft;
            rectF = this.mFrame;
            rectF.top += this.mSubpixelSmoothingLeft;
            rectF = this.mFrame;
            rectF.right -= this.mSubpixelSmoothingRight;
            rectF = this.mFrame;
            rectF.bottom -= this.mSubpixelSmoothingRight;
            this.mBatteryPaint.setColor(this.mPluggedIn ? this.mChargeColor : getColorForLevel(level));
            if (level >= 96) {
                drawFrac = 1.0f;
            } else if (level <= this.mCriticalLevel) {
                drawFrac = 0.0f;
            }
            if (drawFrac == 1.0f) {
                levelTop = this.mButtonFrame.top;
            } else {
                levelTop = this.mFrame.top + (this.mFrame.height() * (1.0f - drawFrac));
            }
            this.mShapePath.reset();
            this.mShapePath.moveTo(this.mButtonFrame.left, this.mButtonFrame.top);
            this.mShapePath.lineTo(this.mButtonFrame.right, this.mButtonFrame.top);
            this.mShapePath.lineTo(this.mButtonFrame.right, this.mFrame.top);
            this.mShapePath.lineTo(this.mFrame.right, this.mFrame.top);
            this.mShapePath.lineTo(this.mFrame.right, this.mFrame.bottom);
            this.mShapePath.lineTo(this.mFrame.left, this.mFrame.bottom);
            this.mShapePath.lineTo(this.mFrame.left, this.mFrame.top);
            this.mShapePath.lineTo(this.mButtonFrame.left, this.mFrame.top);
            this.mShapePath.lineTo(this.mButtonFrame.left, this.mButtonFrame.top);
            int i;
            if (this.mPluggedIn) {
                float bl = this.mFrame.left + (this.mFrame.width() / 4.0f);
                float bt = this.mFrame.top + (this.mFrame.height() / 6.0f);
                float br = this.mFrame.right - (this.mFrame.width() / 4.0f);
                float bb = this.mFrame.bottom - (this.mFrame.height() / 10.0f);
                if (!(this.mBoltFrame.left == bl && this.mBoltFrame.top == bt && this.mBoltFrame.right == br && this.mBoltFrame.bottom == bb)) {
                    this.mBoltFrame.set(bl, bt, br, bb);
                    this.mBoltPath.reset();
                    this.mBoltPath.moveTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
                    for (i = 2; i < this.mBoltPoints.length; i += 2) {
                        this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[i] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[i + 1] * this.mBoltFrame.height()));
                    }
                    this.mBoltPath.lineTo(this.mBoltFrame.left + (this.mBoltPoints[0] * this.mBoltFrame.width()), this.mBoltFrame.top + (this.mBoltPoints[1] * this.mBoltFrame.height()));
                }
                if (Math.min(Math.max((this.mBoltFrame.bottom - levelTop) / (this.mBoltFrame.bottom - this.mBoltFrame.top), 0.0f), 1.0f) <= 0.3f) {
                    c.drawPath(this.mBoltPath, this.mBoltPaint);
                } else {
                    this.mShapePath.op(this.mBoltPath, Op.DIFFERENCE);
                }
            } else if (this.mPowerSaveEnabled) {
                float pw = (this.mFrame.width() * 2.0f) / 3.0f;
                float pl = this.mFrame.left + ((this.mFrame.width() - pw) / 2.0f);
                float pt = this.mFrame.top + ((this.mFrame.height() - pw) / 2.0f);
                float pr = this.mFrame.right - ((this.mFrame.width() - pw) / 2.0f);
                float pb = this.mFrame.bottom - ((this.mFrame.height() - pw) / 2.0f);
                if (!(this.mPlusFrame.left == pl && this.mPlusFrame.top == pt && this.mPlusFrame.right == pr && this.mPlusFrame.bottom == pb)) {
                    this.mPlusFrame.set(pl, pt, pr, pb);
                    this.mPlusPath.reset();
                    this.mPlusPath.moveTo(this.mPlusFrame.left + (this.mPlusPoints[0] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[1] * this.mPlusFrame.height()));
                    for (i = 2; i < this.mPlusPoints.length; i += 2) {
                        this.mPlusPath.lineTo(this.mPlusFrame.left + (this.mPlusPoints[i] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[i + 1] * this.mPlusFrame.height()));
                    }
                    this.mPlusPath.lineTo(this.mPlusFrame.left + (this.mPlusPoints[0] * this.mPlusFrame.width()), this.mPlusFrame.top + (this.mPlusPoints[1] * this.mPlusFrame.height()));
                }
                if (Math.min(Math.max((this.mPlusFrame.bottom - levelTop) / (this.mPlusFrame.bottom - this.mPlusFrame.top), 0.0f), 1.0f) <= 0.3f) {
                    c.drawPath(this.mPlusPath, this.mPlusPaint);
                } else {
                    this.mShapePath.op(this.mPlusPath, Op.DIFFERENCE);
                }
            }
            boolean pctOpaque = false;
            float pctX = 0.0f;
            float pctY = 0.0f;
            String pctText = null;
            if (!this.mPluggedIn && !this.mPowerSaveEnabled && level > this.mCriticalLevel && this.mShowPercent) {
                this.mTextPaint.setColor(getColorForLevel(level));
                this.mTextPaint.setTextSize((this.mLevel == 100 ? 0.38f : 0.5f) * ((float) height));
                this.mTextHeight = -this.mTextPaint.getFontMetrics().ascent;
                pctText = String.valueOf(level);
                pctX = ((float) this.mWidth) * 0.5f;
                pctY = (((float) this.mHeight) + this.mTextHeight) * 0.47f;
                pctOpaque = levelTop > pctY;
                if (!pctOpaque) {
                    this.mTextPath.reset();
                    this.mTextPaint.getTextPath(pctText, 0, pctText.length(), pctX, pctY, this.mTextPath);
                    this.mShapePath.op(this.mTextPath, Op.DIFFERENCE);
                }
            }
            c.drawPath(this.mShapePath, this.mFramePaint);
            this.mFrame.top = levelTop;
            this.mClipPath.reset();
            this.mClipPath.addRect(this.mFrame, Direction.CCW);
            this.mShapePath.op(this.mClipPath, Op.INTERSECT);
            c.drawPath(this.mShapePath, this.mBatteryPaint);
            if (!(this.mPluggedIn || this.mPowerSaveEnabled)) {
                if (level <= this.mCriticalLevel) {
                    c.drawText(this.mWarningString, ((float) this.mWidth) * 0.5f, (((float) this.mHeight) + this.mWarningTextHeight) * 0.48f, this.mWarningTextPaint);
                } else if (pctOpaque) {
                    c.drawText(pctText, pctX, pctY, this.mTextPaint);
                }
            }
        }
    }

    public void setAlpha(int alpha) {
    }

    public void setColorFilter(ColorFilter colorFilter) {
    }

    public int getOpacity() {
        return 0;
    }
}
