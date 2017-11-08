package com.huawei.systemmanager.power.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.power.data.profile.HwPowerProfile;
import com.huawei.systemmanager.util.HSMConst;

public class PowerWaveView extends View {
    private static final int FULL_BATTERY = 100;
    private static final int FULL_BATTERY_ALMOST = 95;
    private static final float TRANSLATION_ANGLE = 0.04f;
    private float mAngle = 0.0f;
    private Shader mBgShader;
    private int mCurrentLevel = 0;
    private boolean mGettingTop = false;
    private float mHeight = 0.0f;
    private Shader mMidShader;
    private float[] mPointMid;
    private float[] mPointbg;
    private float[] mPoints;
    private Shader mShader;
    private float mWaterHeight;
    private Paint mWavePaint;
    private float mWidth = 0.0f;

    public PowerWaveView(Context context) {
        super(context);
        initComponent(context);
    }

    public PowerWaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initComponent(context);
    }

    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        this.mWidth = (float) w;
        this.mHeight = (float) h;
        this.mWaterHeight = this.mHeight;
        this.mPoints = new float[((int) (this.mWidth * 2.0f))];
        this.mPointbg = new float[((int) (this.mWidth * 2.0f))];
        this.mPointMid = new float[((int) (this.mWidth * 2.0f))];
    }

    private void initComponent(Context context) {
        this.mContext = context;
        this.mWavePaint = new Paint();
        this.mWavePaint.setAntiAlias(true);
        this.mWavePaint.setStyle(Style.STROKE);
        this.mWavePaint.setStrokeWidth(4.0f);
    }

    protected void onDraw(Canvas canvas) {
        caculateWaterLevel();
        drawWaterWave(canvas);
        invalidate();
    }

    private void caculateWaterLevel() {
        this.mAngle += TRANSLATION_ANGLE;
        int tempCurrLevel = this.mCurrentLevel;
        if (tempCurrLevel >= 95) {
            tempCurrLevel = 95;
        }
        this.mWaterHeight = this.mHeight * (Utility.ALPHA_MAX - (((float) tempCurrLevel) / 100.0f));
    }

    private void drawWaterWave(Canvas canvas) {
        float amplifier = 20.0f;
        float amplifierMid = 25.0f;
        float amplifierBg = 20.0f;
        if (this.mCurrentLevel >= 95) {
            amplifier = HSMConst.DEVICE_SIZE_100;
            amplifierMid = 13.0f;
            amplifierBg = 15.0f;
        }
        if (this.mCurrentLevel == 100) {
            amplifier = 0.0f;
            amplifierMid = 0.0f;
            amplifierBg = 0.0f;
        }
        for (int i = 0; ((float) i) < this.mWidth; i += 4) {
            this.mPoints[i] = (float) i;
            this.mPointbg[i] = (float) i;
            this.mPointMid[i] = (float) i;
            if (this.mCurrentLevel == 100) {
                this.mPoints[i + 1] = 0.0f;
                this.mPointMid[i + 1] = 0.0f;
                this.mPointbg[i + 1] = 0.0f;
            } else {
                this.mPoints[i + 1] = this.mWaterHeight - (((float) Math.sin(((6.283185307179586d * ((double) i)) / ((double) this.mWidth)) - ((double) this.mAngle))) * amplifier);
                this.mPointMid[i + 1] = this.mWaterHeight - (((float) Math.sin((((6.283185307179586d * ((double) i)) / ((double) this.mWidth)) - ((double) this.mAngle)) - HwPowerProfile.SYSTEM_BASE_NORMAL_POWER)) * amplifierMid);
                this.mPointbg[i + 1] = this.mWaterHeight - (((float) Math.sin((((8.16814059972784d * ((double) i)) / ((double) this.mWidth)) - ((double) this.mAngle)) - 60.0d)) * amplifierBg);
            }
            this.mPoints[i + 2] = (float) i;
            this.mPointMid[i + 2] = (float) i;
            this.mPointbg[i + 2] = (float) i;
            this.mPoints[i + 3] = this.mHeight;
            this.mPointMid[i + 3] = this.mHeight;
            this.mPointbg[i + 3] = this.mHeight;
        }
        canvas.save();
        this.mWavePaint.setShader(this.mBgShader);
        canvas.drawLines(this.mPointbg, this.mWavePaint);
        this.mWavePaint.setShader(this.mMidShader);
        canvas.drawLines(this.mPointMid, this.mWavePaint);
        this.mWavePaint.setShader(this.mShader);
        canvas.drawLines(this.mPoints, this.mWavePaint);
        canvas.restore();
    }

    private void linearShaderMaker(int yBottom, int yTop) {
        this.mShader = new LinearGradient(0.0f, (float) yTop, 0.0f, (float) yBottom, getResources().getColor(R.color.power_powerwave_shader1), getResources().getColor(R.color.power_powerwave_shader2), TileMode.CLAMP);
        this.mMidShader = new LinearGradient(0.0f, (float) yTop, 0.0f, (float) yBottom, getResources().getColor(R.color.power_powerwave_midshader1), getResources().getColor(R.color.power_powerwave_midshader2), TileMode.CLAMP);
        this.mBgShader = new LinearGradient(0.0f, (float) yTop, 0.0f, (float) yBottom, getResources().getColor(R.color.power_powerwave_bgshader1), getResources().getColor(R.color.power_powerwave_bgshader2), TileMode.CLAMP);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.mCurrentLevel = 0;
    }

    public boolean isTopLevel() {
        return this.mGettingTop;
    }

    public int dip2px(float dipValue) {
        return (int) ((dipValue * this.mContext.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public void updateWaterLevel(int level) {
        this.mCurrentLevel = level;
        linearShaderMaker((int) this.mHeight, (int) (this.mHeight * (Utility.ALPHA_MAX - (((float) this.mCurrentLevel) / 100.0f))));
    }

    public boolean onTouchEvent(MotionEvent event) {
        boolean isOutRange;
        if (Float.compare((float) Math.pow(Math.pow(Math.abs(((double) event.getX()) - (((double) getWidth()) / 2.0d)), 2.0d) + Math.pow(Math.abs(((double) event.getY()) - (((double) getHeight()) / 2.0d)), 2.0d), 0.5d), (float) dip2px(92.0f)) > 0) {
            isOutRange = true;
        } else {
            isOutRange = false;
        }
        if (!isOutRange) {
            return super.onTouchEvent(event);
        }
        setPressed(false);
        return true;
    }
}
