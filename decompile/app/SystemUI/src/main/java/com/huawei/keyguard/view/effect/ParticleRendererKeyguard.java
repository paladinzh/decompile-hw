package com.huawei.keyguard.view.effect;

import android.opengl.GLSurfaceView.Renderer;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.KeyguardUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ParticleRendererKeyguard implements Renderer {
    private static final /* synthetic */ int[] -com-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues = null;
    private static final float SQRT2PI = ((float) Math.sqrt(6.283185307179586d));
    private float LCD_RATIO;
    private float RADIRBASE;
    private float center_change_level_x;
    private float center_change_level_y;
    private int center_current_index_x;
    private float change_current_level_x;
    private float change_current_level_y;
    private int change_direction_x;
    private int change_direction_y;
    private float change_ratio_x_a;
    private float change_ratio_x_c;
    private float[] mAdjustSide;
    private float mAlphaStart;
    private float mColorA;
    private float mColorB;
    private float mColorG;
    private float mColorR;
    private float mDownPositionY;
    private DrawStates mDrawState;
    private float mLCDHeigh;
    private float mLCDWidth;
    private float mLeftMax;
    private float mLeftSigmaTmp;
    private float mLeftXMax;
    private float[] mMidCosValue;
    private float mMidMaxLeft;
    private float mMidMaxRight;
    private float[] mMidSinValue;
    private float mNormalJumpScreen;
    private float mNormalJumpSigmaLeft;
    private float mNormalJumpSigmaRight;
    private float mNormalLeftA;
    private float mNormalLeftB;
    private float mNormalLeftSigmaMax;
    private float mNormalLeftSigmaMin;
    private float mNormalMovValue;
    private float mNormalRightA;
    private float mNormalRightB;
    private float mNormalRightSigmaMax;
    private float mNormalRightSigmaMin;
    private int mOpenFlag;
    private int mOpenLeftorRightFlag;
    private int mOpenStep2Flag;
    private float mOpenStep2ratio;
    private float mOpenStepX;
    private float mPositionX;
    private float mPositionY;
    private float mRadirStart;
    private float mRightMax;
    private float mRightSigmaTmp;
    private float mRightXMax;
    private float mSideSubRatio;
    private FloatBuffer mVertexBuffer;
    private float[] mVerticFuseBottom;
    private float[] mVerticFuseTop;
    private float[] mVerticsMid;
    private float[] mVerticsSideL;
    private float[] mVerticsSideR;

    public enum DrawStates {
        NODRAW,
        STARTDRAW,
        MOVEDRAW,
        DISDRAW,
        OPENDRAW
    }

    public enum DrawWhere {
        LEFT_SIDE,
        RIGHT_SIDE,
        FUSE_Q1,
        FUSE_Q2,
        FUSE_Q3,
        FUSE_Q4
    }

    private static /* synthetic */ int[] -getcom-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues() {
        if (-com-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues != null) {
            return -com-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues;
        }
        int[] iArr = new int[DrawWhere.values().length];
        try {
            iArr[DrawWhere.FUSE_Q1.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[DrawWhere.FUSE_Q2.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[DrawWhere.FUSE_Q3.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[DrawWhere.FUSE_Q4.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[DrawWhere.LEFT_SIDE.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[DrawWhere.RIGHT_SIDE.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues = iArr;
        return iArr;
    }

    public ParticleRendererKeyguard() {
        this.mMidSinValue = new float[161];
        this.mMidCosValue = new float[161];
        this.mAdjustSide = new float[1002];
        this.mVerticsSideR = new float[2004];
        this.mVerticsSideL = new float[2004];
        this.mLeftXMax = 501.0f;
        this.mRightXMax = 501.0f;
        this.mLeftSigmaTmp = 1.0f;
        this.mRightSigmaTmp = 1.0f;
        this.mNormalLeftSigmaMin = 2.5f;
        this.mNormalLeftSigmaMax = 6.0f;
        this.mNormalRightSigmaMin = 2.5f;
        this.mNormalRightSigmaMax = 6.0f;
        this.mNormalJumpScreen = 0.0024f;
        this.mNormalJumpSigmaLeft = 0.0f;
        this.mNormalJumpSigmaRight = 0.0f;
        this.mNormalLeftA = 0.0f;
        this.mNormalLeftB = 0.0f;
        this.mNormalRightA = 0.0f;
        this.mNormalRightB = 0.0f;
        this.mNormalMovValue = 0.0f;
        this.mLCDWidth = 0.0f;
        this.mLCDHeigh = 0.0f;
        this.LCD_RATIO = 0.0f;
        this.mPositionX = 0.0f;
        this.mPositionY = 0.0f;
        this.mDownPositionY = 0.0f;
        this.center_current_index_x = 0;
        this.mVerticsMid = new float[322];
        this.mVerticFuseTop = new float[84];
        this.mVerticFuseBottom = new float[84];
        this.mRadirStart = 0.05f;
        this.RADIRBASE = 0.25f;
        this.mAlphaStart = 0.5f;
        this.mSideSubRatio = 1.0f;
        this.mOpenStepX = 0.0f;
        this.mOpenFlag = 0;
        this.mOpenStep2Flag = 0;
        this.mOpenStep2ratio = 1.0f;
        this.mOpenLeftorRightFlag = 0;
        this.mColorR = 0.5f;
        this.mColorG = 0.5f;
        this.mColorB = 0.5f;
        this.mColorA = 0.5f;
    }

    public ParticleRendererKeyguard(int width, int height) {
        this.mMidSinValue = new float[161];
        this.mMidCosValue = new float[161];
        this.mAdjustSide = new float[1002];
        this.mVerticsSideR = new float[2004];
        this.mVerticsSideL = new float[2004];
        this.mLeftXMax = 501.0f;
        this.mRightXMax = 501.0f;
        this.mLeftSigmaTmp = 1.0f;
        this.mRightSigmaTmp = 1.0f;
        this.mNormalLeftSigmaMin = 2.5f;
        this.mNormalLeftSigmaMax = 6.0f;
        this.mNormalRightSigmaMin = 2.5f;
        this.mNormalRightSigmaMax = 6.0f;
        this.mNormalJumpScreen = 0.0024f;
        this.mNormalJumpSigmaLeft = 0.0f;
        this.mNormalJumpSigmaRight = 0.0f;
        this.mNormalLeftA = 0.0f;
        this.mNormalLeftB = 0.0f;
        this.mNormalRightA = 0.0f;
        this.mNormalRightB = 0.0f;
        this.mNormalMovValue = 0.0f;
        this.mLCDWidth = 0.0f;
        this.mLCDHeigh = 0.0f;
        this.LCD_RATIO = 0.0f;
        this.mPositionX = 0.0f;
        this.mPositionY = 0.0f;
        this.mDownPositionY = 0.0f;
        this.center_current_index_x = 0;
        this.mVerticsMid = new float[322];
        this.mVerticFuseTop = new float[84];
        this.mVerticFuseBottom = new float[84];
        this.mRadirStart = 0.05f;
        this.RADIRBASE = 0.25f;
        this.mAlphaStart = 0.5f;
        this.mSideSubRatio = 1.0f;
        this.mOpenStepX = 0.0f;
        this.mOpenFlag = 0;
        this.mOpenStep2Flag = 0;
        this.mOpenStep2ratio = 1.0f;
        this.mOpenLeftorRightFlag = 0;
        this.mColorR = 0.5f;
        this.mColorG = 0.5f;
        this.mColorB = 0.5f;
        this.mColorA = 0.5f;
        this.mLCDWidth = (float) (Math.min(width, height) >> 1);
        this.mLCDHeigh = (float) (Math.max(width, height) >> 1);
        this.LCD_RATIO = this.mLCDWidth / this.mLCDHeigh;
    }

    private static float getFloatFrom255(int b8) {
        return ((float) b8) * 0.003921569f;
    }

    public void setColor(int argb8888) {
        int alpha = (argb8888 >> 24) & 255;
        if (alpha > 191) {
            alpha = 191;
        }
        this.mColorA = getFloatFrom255(alpha);
        this.mColorR = getFloatFrom255((argb8888 >> 16) & 255);
        this.mColorG = getFloatFrom255((argb8888 >> 8) & 255);
        this.mColorB = getFloatFrom255(argb8888 & 255);
        HwLog.w("ParticleRenderer", "setColor to " + this.mColorA + " " + Integer.toHexString(argb8888));
    }

    public float degToRad(float deg) {
        return (3.1415927f * deg) / 160.0f;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glShadeModel(7425);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(2929);
        gl.glDepthFunc(515);
        gl.glHint(3152, 4353);
        iniParas();
    }

    public void iniParas() {
        int i;
        this.mVertexBuffer = ByteBuffer.allocateDirect(16032).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mPositionY = 0.0f;
        this.mPositionX = 0.0f;
        float adjNormalA = 1.0f / (SQRT2PI * 5.0f);
        float adjNormalMin = 0.0f;
        for (i = 1; i <= 1001; i++) {
            float PointPosY = ((float) ((i - 500) - 1)) * 0.015f;
            float adjNormalB = ((-1.0f * PointPosY) * PointPosY) / 25.0f;
            if (i == 1) {
                adjNormalMin = adjNormalA * ((float) Math.exp((double) adjNormalB));
            }
            this.mAdjustSide[i] = Math.max(((((float) Math.exp((double) adjNormalB)) * adjNormalA) - adjNormalMin) - 0.02f, 0.0f);
        }
        for (i = 0; i < 160; i++) {
            this.mMidCosValue[i] = this.RADIRBASE * ((float) Math.cos((double) degToRad((float) (i * 2))));
            this.mMidSinValue[i] = ((this.RADIRBASE * 1.1f) * this.LCD_RATIO) * ((float) Math.sin((double) degToRad((float) (i * 2))));
        }
        this.center_change_level_x = (KeyguardUtils.nextFloat() * 3.0f) / 25.0f;
        this.change_current_level_x = 0.0f;
        this.change_ratio_x_c = 0.0f;
        this.change_ratio_x_a = 0.0f;
        this.change_direction_x = 0;
        this.change_direction_y = 0;
        this.change_current_level_y = 0.0f;
        this.center_change_level_y = (KeyguardUtils.nextFloat() * 1.0f) / 25.0f;
        this.center_current_index_x = (int) (KeyguardUtils.nextFloat() * 160.0f);
    }

    public void setTranslaterF(float x, float y) {
        this.mPositionX = (x - this.mLCDWidth) / this.mLCDWidth;
        this.mPositionY = (this.mLCDHeigh - y) / this.mLCDHeigh;
        this.mLeftSigmaTmp = ((this.mNormalLeftSigmaMax - this.mNormalLeftSigmaMin) * (((this.mPositionX + 0.5f) * (this.mPositionX + 0.5f)) / 2.25f)) + this.mNormalLeftSigmaMin;
        this.mRightSigmaTmp = ((this.mNormalRightSigmaMax - this.mNormalRightSigmaMin) * (((this.mPositionX - 0.5f) * (this.mPositionX - 0.5f)) / 2.25f)) + this.mNormalRightSigmaMin;
        this.mNormalLeftA = 1.0f / (SQRT2PI * this.mLeftSigmaTmp);
        this.mNormalRightA = 1.0f / (SQRT2PI * this.mRightSigmaTmp);
        this.mNormalJumpSigmaLeft = (this.mLeftSigmaTmp * 3.0f) / 150.0f;
        this.mNormalJumpSigmaRight = (this.mRightSigmaTmp * 3.0f) / 150.0f;
    }

    public void setTranslaterXY(float x, float y) {
        this.mPositionX = x;
        this.mPositionY = y;
        this.mLeftSigmaTmp = ((this.mNormalLeftSigmaMax - this.mNormalLeftSigmaMin) * (((this.mPositionX + 0.5f) * (this.mPositionX + 0.5f)) / 2.25f)) + this.mNormalLeftSigmaMin;
        this.mRightSigmaTmp = ((this.mNormalRightSigmaMax - this.mNormalRightSigmaMin) * (((this.mPositionX - 0.5f) * (this.mPositionX - 0.5f)) / 2.25f)) + this.mNormalRightSigmaMin;
        this.mNormalLeftA = 1.0f / (SQRT2PI * this.mLeftSigmaTmp);
        this.mNormalRightA = 1.0f / (SQRT2PI * this.mRightSigmaTmp);
        this.mNormalJumpSigmaLeft = (this.mLeftSigmaTmp * 3.0f) / 150.0f;
        this.mNormalJumpSigmaRight = (this.mRightSigmaTmp * 3.0f) / 150.0f;
    }

    public void resetVertexSide(DrawWhere who) {
        int i;
        float PointPos;
        float PointPosY;
        switch (-getcom-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues()[who.ordinal()]) {
            case 5:
                this.mVertexBuffer.put(0, -1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                for (i = 1; i <= 1001; i++) {
                    PointPos = ((float) ((i - 500) - 1)) - Math.max(Math.min((this.mPositionY - this.mDownPositionY) * 400.0f, 60.0f), -60.0f);
                    float lenthLeft;
                    if (Math.abs(PointPos) <= 150.0f) {
                        PointPosY = PointPos * this.mNormalJumpSigmaLeft;
                        this.mNormalLeftB = ((-1.0f * PointPosY) * PointPosY) / (this.mLeftSigmaTmp * this.mLeftSigmaTmp);
                        lenthLeft = (Math.max((((this.mNormalLeftA * ((float) Math.exp((double) this.mNormalLeftB))) - 1.0f) + this.mAdjustSide[i]) - this.mNormalMovValue, -1.0f) + 1.0f) - 1.0f;
                        this.mVertexBuffer.put(i * 2, lenthLeft);
                        this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                        this.mVerticsSideL[i * 2] = lenthLeft;
                        this.mVerticsSideL[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                    } else {
                        lenthLeft = -1.0f + this.mAdjustSide[i];
                        this.mVertexBuffer.put(i * 2, lenthLeft);
                        this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                        this.mVerticsSideL[i * 2] = lenthLeft;
                        this.mVerticsSideL[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                    }
                }
                this.mLeftMax = this.mVerticsSideL[2];
                for (i = 1; i <= 1001; i++) {
                    if (this.mVerticsSideL[i * 2] > this.mLeftMax) {
                        this.mLeftMax = this.mVerticsSideL[i * 2];
                        this.mLeftXMax = (float) i;
                    }
                }
                return;
            case 6:
                this.mVertexBuffer.put(0, 1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                for (i = 1; i <= 1001; i++) {
                    PointPos = ((float) ((i - 500) - 1)) - Math.max(Math.min((this.mPositionY - this.mDownPositionY) * 400.0f, 60.0f), -60.0f);
                    float lenthRight;
                    if (Math.abs(PointPos) <= 150.0f) {
                        PointPosY = PointPos * this.mNormalJumpSigmaRight;
                        this.mNormalRightB = ((-1.0f * PointPosY) * PointPosY) / (this.mRightSigmaTmp * this.mRightSigmaTmp);
                        lenthRight = 1.0f - (1.0f - Math.min(this.mNormalMovValue + ((1.0f - (this.mNormalRightA * ((float) Math.exp((double) this.mNormalRightB)))) - this.mAdjustSide[i]), 1.0f));
                        this.mVertexBuffer.put(i * 2, lenthRight);
                        this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                        this.mVerticsSideR[i * 2] = lenthRight;
                        this.mVerticsSideR[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                    } else {
                        lenthRight = 1.0f - this.mAdjustSide[i];
                        this.mVertexBuffer.put(i * 2, lenthRight);
                        this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                        this.mVerticsSideR[i * 2] = lenthRight;
                        this.mVerticsSideR[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                    }
                }
                this.mRightMax = this.mVerticsSideR[2];
                for (i = 1; i <= 1001; i++) {
                    if (this.mVerticsSideR[i * 2] < this.mRightMax) {
                        this.mRightMax = this.mVerticsSideR[i * 2];
                        this.mRightXMax = (float) i;
                    }
                }
                return;
            default:
                return;
        }
    }

    public void resetVertexSideStart(DrawWhere who) {
        int i;
        float PointPos;
        if (who == DrawWhere.LEFT_SIDE) {
            this.mVertexBuffer.put(0, -1.0f);
            this.mVertexBuffer.put(1, this.mPositionY);
            float SubLeft = (this.mNormalLeftA + this.mAdjustSide[501]) - 0.006f;
            for (i = 1; i <= 1001; i++) {
                PointPos = (float) ((i - 500) - 1);
                float lenthLeft;
                if (Math.abs(PointPos) <= 150.0f) {
                    float PointPosY = PointPos * this.mNormalJumpSigmaLeft;
                    this.mNormalLeftB = ((-1.0f * PointPosY) * PointPosY) / (this.mLeftSigmaTmp * this.mLeftSigmaTmp);
                    lenthLeft = Math.max(((Math.max((((this.mNormalLeftA * ((float) Math.exp((double) this.mNormalLeftB))) - 1.0f) + this.mAdjustSide[i]) - this.mNormalMovValue, -1.0f) + 1.0f) - 1.0f) - (this.mSideSubRatio * SubLeft), -1.0f);
                    if (i == 501) {
                        this.mLeftMax = lenthLeft;
                        this.mLeftXMax = 501.0f;
                    }
                    this.mVertexBuffer.put(i * 2, lenthLeft);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideL[i * 2] = lenthLeft;
                    this.mVerticsSideL[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                } else {
                    lenthLeft = Math.max((this.mAdjustSide[i] - 4.0f) - (this.mSideSubRatio * SubLeft), -1.0f);
                    this.mVertexBuffer.put(i * 2, lenthLeft);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideL[i * 2] = lenthLeft;
                    this.mVerticsSideL[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                }
            }
        }
        if (who == DrawWhere.RIGHT_SIDE) {
            this.mVertexBuffer.put(0, 1.0f);
            this.mVertexBuffer.put(1, this.mPositionY);
            float SubRight = (this.mNormalRightA + this.mAdjustSide[501]) + 0.006f;
            for (i = 1; i <= 1001; i++) {
                PointPos = (float) ((i - 500) - 1);
                float lenthRight;
                if (Math.abs(PointPos) <= 150.0f) {
                    PointPosY = PointPos * this.mNormalJumpSigmaRight;
                    this.mNormalRightB = ((-1.0f * PointPosY) * PointPosY) / (this.mRightSigmaTmp * this.mRightSigmaTmp);
                    lenthRight = Math.min((this.mSideSubRatio * SubRight) + (1.0f - (1.0f - Math.min(this.mNormalMovValue + ((1.0f - (this.mNormalRightA * ((float) Math.exp((double) this.mNormalRightB)))) - this.mAdjustSide[i]), 1.0f))), 1.0f);
                    if (i == 501) {
                        this.mRightMax = lenthRight;
                        this.mRightXMax = 501.0f;
                    }
                    this.mVertexBuffer.put(i * 2, lenthRight);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideR[i * 2] = lenthRight;
                    this.mVerticsSideR[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                } else {
                    lenthRight = Math.min((1.0f - this.mAdjustSide[i]) + (this.mSideSubRatio * SubRight), 1.0f);
                    this.mVertexBuffer.put(i * 2, lenthRight);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideR[i * 2] = lenthRight;
                    this.mVerticsSideR[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                }
            }
        }
    }

    private void resetVertexMid() {
        int i;
        for (i = 0; i < 160; i++) {
            this.mVerticsMid[i * 2] = this.mMidCosValue[i];
            this.mVerticsMid[(i * 2) + 1] = this.mMidSinValue[i];
        }
        if (this.mPositionX < 0.5f && this.mPositionX > -0.5f) {
            int cIdx = this.center_current_index_x;
            if (cIdx >= 160) {
                cIdx %= 160;
            }
            float newX = 1.0f + this.change_ratio_x_c;
            float[] fArr = this.mVerticsMid;
            int i2 = cIdx * 2;
            fArr[i2] = fArr[i2] * newX;
            for (i = 1; i < 80; i++) {
                int idx1 = cIdx + i;
                int idx2 = cIdx - i;
                if (idx1 >= 160) {
                    idx1 %= 160;
                }
                if (idx2 < 0) {
                    idx2 += 160;
                }
                newX = ((this.change_ratio_x_a * ((float) i)) + 1.0f) + this.change_ratio_x_c;
                fArr = this.mVerticsMid;
                i2 = idx1 * 2;
                fArr[i2] = fArr[i2] * newX;
                fArr = this.mVerticsMid;
                i2 = idx2 * 2;
                fArr[i2] = fArr[i2] * newX;
            }
            if (this.change_direction_x == 0) {
                this.change_current_level_x += 0.005f;
                if (this.change_current_level_x >= this.center_change_level_x) {
                    this.change_current_level_x = this.center_change_level_x;
                    this.change_direction_x = 1;
                }
            } else if (this.change_direction_x == 1) {
                this.change_current_level_x -= 0.005f;
                if (this.change_current_level_x < 0.0f) {
                    this.center_change_level_x = (KeyguardUtils.nextFloat() * 3.0f) / 15.0f;
                    this.change_current_level_x = 0.0f;
                    this.change_direction_x = 0;
                }
            }
            this.change_ratio_x_c = this.change_current_level_x;
            this.change_ratio_x_a = (-this.change_ratio_x_c) / 80.0f;
            this.center_current_index_x++;
            if (this.center_current_index_x >= 160) {
                this.center_current_index_x = 0;
            }
            if (this.change_direction_y == 0) {
                this.change_current_level_y += 0.002f;
                if (this.change_current_level_y >= this.center_change_level_y) {
                    this.change_current_level_y = this.center_change_level_y;
                    this.change_direction_y = 1;
                }
            } else if (this.change_direction_y == 1) {
                this.change_current_level_y -= 0.002f;
                if (this.change_current_level_y < 0.0f) {
                    this.center_change_level_y = KeyguardUtils.nextFloat() / 15.0f;
                    this.change_current_level_y = 0.0f;
                    this.change_direction_y = 0;
                }
            }
        }
        this.mMidMaxLeft = this.mVerticsMid[160] + this.mPositionX;
        this.mMidMaxRight = this.mVerticsMid[0] + this.mPositionX;
        float maxG;
        float ratio;
        if (this.mPositionX >= 0.0f) {
            if (this.mRightMax - this.mMidMaxRight <= 0.4f) {
                maxG = ((0.4f - Math.max(this.mRightMax - this.mMidMaxRight, 0.0f)) * 0.08f) / 0.4f;
                for (i = 0; i < 160; i++) {
                    ratio = Math.max(this.mVerticsMid[i * 2], 0.0f) / this.RADIRBASE;
                    this.mVerticsMid[i * 2] = this.mVerticsMid[i * 2] + (((maxG * ratio) * ratio) * ratio);
                    this.mVerticsMid[(i * 2) + 1] = this.mVerticsMid[(i * 2) + 1];
                    this.mVertexBuffer.put(i * 2, this.mVerticsMid[i * 2] + this.mPositionX);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticsMid[(i * 2) + 1] + this.mPositionY);
                }
            } else {
                for (i = 0; i < 160; i++) {
                    this.mVerticsMid[i * 2] = this.mVerticsMid[i * 2];
                    this.mVerticsMid[(i * 2) + 1] = this.mVerticsMid[(i * 2) + 1];
                    this.mVertexBuffer.put(i * 2, this.mVerticsMid[i * 2] + this.mPositionX);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticsMid[(i * 2) + 1] + this.mPositionY);
                }
            }
        } else if (this.mMidMaxLeft - this.mLeftMax <= 0.4f) {
            maxG = ((0.4f - Math.max(this.mMidMaxLeft - this.mLeftMax, 0.0f)) * 0.08f) / 0.4f;
            for (i = 0; i < 160; i++) {
                ratio = Math.abs(Math.min(this.mVerticsMid[i * 2], 0.0f)) / this.RADIRBASE;
                this.mVerticsMid[i * 2] = this.mVerticsMid[i * 2] - (((maxG * ratio) * ratio) * ratio);
                this.mVerticsMid[(i * 2) + 1] = this.mVerticsMid[(i * 2) + 1];
                this.mVertexBuffer.put(i * 2, this.mVerticsMid[i * 2] + this.mPositionX);
                this.mVertexBuffer.put((i * 2) + 1, this.mVerticsMid[(i * 2) + 1] + this.mPositionY);
            }
        } else {
            for (i = 0; i < 160; i++) {
                this.mVerticsMid[i * 2] = this.mVerticsMid[i * 2];
                this.mVerticsMid[(i * 2) + 1] = this.mVerticsMid[(i * 2) + 1];
                this.mVertexBuffer.put(i * 2, this.mVerticsMid[i * 2] + this.mPositionX);
                this.mVertexBuffer.put((i * 2) + 1, this.mVerticsMid[(i * 2) + 1] + this.mPositionY);
            }
        }
        this.mMidMaxLeft = this.mVerticsMid[160];
        this.mMidMaxRight = this.mVerticsMid[0];
        for (i = 0; i < 160; i++) {
            if (this.mVerticsMid[i * 2] < this.mMidMaxLeft) {
                this.mMidMaxLeft = this.mVerticsMid[i * 2];
            }
            if (this.mVerticsMid[i * 2] > this.mMidMaxRight) {
                this.mMidMaxRight = this.mVerticsMid[i * 2];
            }
        }
        this.mMidMaxLeft += this.mPositionX;
        this.mMidMaxRight += this.mPositionX;
    }

    public void resetVertexMidOpen() {
        for (int i = 0; i < 160; i++) {
            float tmpXPos = this.mVerticsMid[i * 2] + this.mPositionX;
            float tmpYPos = ((this.change_current_level_y + 1.0f) * this.mVerticsMid[(i * 2) + 1]) + this.mPositionY;
            if (this.mOpenLeftorRightFlag == 0) {
                tmpXPos = ((tmpXPos + 1.0f) * this.mOpenStep2ratio) - 1.0f;
            } else {
                tmpXPos = 1.0f - ((1.0f - tmpXPos) * this.mOpenStep2ratio);
            }
            this.mVertexBuffer.put(i * 2, tmpXPos);
            this.mVertexBuffer.put((i * 2) + 1, tmpYPos);
        }
    }

    public void resetVertexOpen(DrawWhere where) {
        int i;
        switch (-getcom-huawei-keyguard-view-effect-ParticleRendererKeyguard$DrawWhereSwitchesValues()[where.ordinal()]) {
            case 1:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, 1.0f - ((1.0f - this.mVerticFuseTop[i * 2]) * this.mOpenStep2ratio));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseTop[(i * 2) + 1]);
                }
                return;
            case 2:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, ((this.mVerticFuseTop[i * 2] + 1.0f) * this.mOpenStep2ratio) - 1.0f);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseTop[(i * 2) + 1]);
                }
                return;
            case 3:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, ((this.mVerticFuseBottom[i * 2] + 1.0f) * this.mOpenStep2ratio) - 1.0f);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseBottom[(i * 2) + 1]);
                }
                return;
            case 4:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, 1.0f - ((1.0f - this.mVerticFuseBottom[i * 2]) * this.mOpenStep2ratio));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseBottom[(i * 2) + 1]);
                }
                return;
            case 5:
                this.mVertexBuffer.put(0, -1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                for (i = 1; i <= 1001; i++) {
                    this.mVertexBuffer.put(i * 2, ((this.mVerticsSideL[i * 2] + 1.0f) * this.mOpenStep2ratio) - 1.0f);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticsSideL[(i * 2) + 1]);
                }
                return;
            case 6:
                this.mVertexBuffer.put(0, 1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                for (i = 1; i <= 1001; i++) {
                    this.mVertexBuffer.put(i * 2, 1.0f - ((1.0f - this.mVerticsSideR[i * 2]) * this.mOpenStep2ratio));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticsSideR[(i * 2) + 1]);
                }
                return;
            default:
                return;
        }
    }

    private void resetVertexFuseQ1() {
        int i;
        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 0.0f;
        float y1 = 0.0f;
        float x2 = 0.0f;
        float y2 = 0.0f;
        float x3 = 0.0f;
        float y3 = 0.0f;
        float leftX = Math.max(this.mRightMax - 0.03f, this.mPositionX - 0.18f);
        for (i = 1; i < 160; i++) {
            if (this.mVerticsMid[i * 2] + this.mPositionX < leftX) {
                y0 = this.mVerticsMid[(i * 2) + 1] + this.mPositionY;
                x0 = this.mVerticsMid[i * 2] + this.mPositionX;
                y1 = this.mVerticsMid[(i * 2) - 1] + this.mPositionY;
                x1 = this.mVerticsMid[(i * 2) - 2] + this.mPositionX;
                break;
            }
        }
        float baseDis = 1.0f - this.mRightMax;
        float ratio = (baseDis - Math.max(0.0f, 1.0f - this.mMidMaxRight)) / baseDis;
        float rightY = ((((ratio * ratio) * ratio) * 0.26999998f) + 0.08f) + this.mPositionY;
        for (i = (int) this.mRightXMax; i <= 1001; i++) {
            if (this.mVerticsSideR[(i * 2) + 1] > rightY) {
                x2 = this.mVerticsSideR[i * 2];
                y2 = this.mVerticsSideR[(i * 2) + 1];
                x3 = this.mVerticsSideR[(i * 2) - 2];
                y3 = this.mVerticsSideR[(i * 2) - 1];
                break;
            }
        }
        float k1 = (y0 - y1) / (x0 - x1);
        float k2 = (y2 - y3) / (x2 - x3);
        float x = ((((k1 * x0) - (k2 * x2)) + y2) - y0) / (k1 - k2);
        float y = y0 + ((x - x0) * k1);
        this.mVertexBuffer.put(0, Math.min(this.mMidMaxRight + 0.0f, 1.0f));
        this.mVertexBuffer.put(1, this.mPositionY);
        this.mVerticFuseTop[0] = Math.min(this.mMidMaxRight + 0.0f, 1.0f);
        this.mVerticFuseTop[1] = this.mPositionY;
        float fuseXPoint1 = x0;
        float fuseYPoint1 = y0;
        float fuseXPoint2 = x;
        float fuseYPoint2 = y;
        float fuseXPoint3 = x2;
        float fuseYPoint3 = y2;
        for (i = 1; i < 40; i++) {
            float t = 0.025f * ((float) (i - 1));
            float a1 = (float) Math.pow((double) (1.0f - t), 2.0d);
            float a2 = (2.0f * t) * (1.0f - t);
            float a3 = t * t;
            float fuseTmpPointX = ((a1 * fuseXPoint1) + (a2 * x)) + (a3 * fuseXPoint3);
            float fuseTmpPointY = ((a1 * fuseYPoint1) + (a2 * y)) + (a3 * fuseYPoint3);
            this.mVertexBuffer.put(i * 2, fuseTmpPointX - 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, fuseTmpPointY - 0.0f);
            this.mVerticFuseTop[i * 2] = fuseTmpPointX - 0.0f;
            this.mVerticFuseTop[(i * 2) + 1] = fuseTmpPointY - 0.0f;
        }
        this.mVertexBuffer.put(80, fuseXPoint3);
        this.mVertexBuffer.put(81, fuseYPoint3);
        this.mVertexBuffer.put(82, 1.0f);
        this.mVertexBuffer.put(83, this.mPositionY);
        this.mVerticFuseTop[80] = fuseXPoint3;
        this.mVerticFuseTop[81] = fuseYPoint3;
        this.mVerticFuseTop[82] = 1.0f;
        this.mVerticFuseTop[83] = this.mPositionY;
    }

    public void resetVertexFuseQ4() {
        int i;
        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 0.0f;
        float y1 = 0.0f;
        float x2 = 0.0f;
        float y2 = 0.0f;
        float x3 = 0.0f;
        float y3 = 0.0f;
        float leftX = Math.max(this.mRightMax - 0.03f, this.mPositionX - 0.18f);
        for (i = 0; i < 160; i++) {
            if (this.mVerticsMid[(159 - i) * 2] + this.mPositionX < leftX) {
                int leftIndex = 159 - i;
                y0 = this.mVerticsMid[(leftIndex * 2) + 1] + this.mPositionY;
                x0 = this.mVerticsMid[leftIndex * 2] + this.mPositionX;
                y1 = this.mVerticsMid[(leftIndex * 2) + 3] + this.mPositionY;
                x1 = this.mVerticsMid[(leftIndex * 2) + 2] + this.mPositionX;
                break;
            }
        }
        float baseDis = 1.0f - this.mRightMax;
        float ratio = (baseDis - Math.max(0.0f, 1.0f - this.mMidMaxRight)) / baseDis;
        float rightY = (this.mPositionY - (((ratio * ratio) * ratio) * 0.26999998f)) - 0.08f;
        for (i = (int) this.mRightXMax; i >= 1; i--) {
            if (this.mVerticsSideR[(i * 2) + 1] < rightY) {
                x2 = this.mVerticsSideR[i * 2];
                y2 = this.mVerticsSideR[(i * 2) + 1];
                x3 = this.mVerticsSideR[(i * 2) + 2];
                y3 = this.mVerticsSideR[(i * 2) + 3];
                break;
            }
        }
        float k1 = (y0 - y1) / (x0 - x1);
        float k2 = (y2 - y3) / (x2 - x3);
        float x = ((((k1 * x0) - (k2 * x2)) + y2) - y0) / (k1 - k2);
        float y = y0 + ((x - x0) * k1);
        this.mVertexBuffer.put(0, Math.min(this.mMidMaxRight + 0.0f, 1.0f));
        this.mVertexBuffer.put(1, this.mPositionY);
        this.mVerticFuseBottom[0] = Math.min(this.mMidMaxRight + 0.0f, 1.0f);
        this.mVerticFuseBottom[1] = this.mPositionY;
        float fuseXPoint1 = x0;
        float fuseYPoint1 = y0;
        float fuseXPoint2 = x;
        float fuseYPoint2 = y;
        float fuseXPoint3 = x2;
        float fuseYPoint3 = y2;
        for (i = 1; i < 40; i++) {
            float t = 0.025f * ((float) (i - 1));
            float a1 = (float) Math.pow((double) (1.0f - t), 2.0d);
            float a2 = (2.0f * t) * (1.0f - t);
            float a3 = t * t;
            float fuseTmpPointX = ((a1 * fuseXPoint1) + (a2 * x)) + (a3 * fuseXPoint3);
            float fuseTmpPointY = ((a1 * fuseYPoint1) + (a2 * y)) + (a3 * fuseYPoint3);
            this.mVertexBuffer.put(i * 2, fuseTmpPointX - 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, fuseTmpPointY - 0.0f);
            this.mVerticFuseBottom[i * 2] = fuseTmpPointX - 0.0f;
            this.mVerticFuseBottom[(i * 2) + 1] = fuseTmpPointY - 0.0f;
        }
        this.mVertexBuffer.put(80, fuseXPoint3);
        this.mVertexBuffer.put(81, fuseYPoint3);
        this.mVertexBuffer.put(82, 1.0f);
        this.mVertexBuffer.put(83, this.mPositionY);
        this.mVerticFuseBottom[80] = fuseXPoint3;
        this.mVerticFuseBottom[81] = fuseYPoint3;
        this.mVerticFuseBottom[82] = 1.0f;
        this.mVerticFuseBottom[83] = this.mPositionY;
    }

    public void resetVertexFuseQ2() {
        int i;
        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 0.0f;
        float y1 = 0.0f;
        float x2 = 0.0f;
        float y2 = 0.0f;
        float x3 = 0.0f;
        float y3 = 0.0f;
        float rightX = Math.min(this.mLeftMax + 0.03f, this.mPositionX + 0.18f);
        for (i = 0; i < 80; i++) {
            if (this.mVerticsMid[(80 - i) * 2] + this.mPositionX > rightX) {
                int rightIndex = 80 - i;
                y0 = this.mVerticsMid[(rightIndex * 2) + 1] + this.mPositionY;
                x0 = this.mVerticsMid[rightIndex * 2] + this.mPositionX;
                y1 = this.mVerticsMid[(rightIndex * 2) + 3] + this.mPositionY;
                x1 = this.mVerticsMid[(rightIndex * 2) + 2] + this.mPositionX;
                break;
            }
        }
        float baseDis = 1.0f + this.mLeftMax;
        float ratio = (baseDis - Math.max(0.0f, 1.0f + this.mMidMaxLeft)) / baseDis;
        float leftY = ((((ratio * ratio) * ratio) * 0.26999998f) + 0.08f) + this.mPositionY;
        for (i = (int) this.mLeftXMax; i <= 1001; i++) {
            if (this.mVerticsSideL[(i * 2) + 1] > leftY) {
                x2 = this.mVerticsSideL[i * 2];
                y2 = this.mVerticsSideL[(i * 2) + 1];
                x3 = this.mVerticsSideL[(i * 2) - 2];
                y3 = this.mVerticsSideL[(i * 2) - 1];
                break;
            }
        }
        float k1 = (y0 - y1) / (x0 - x1);
        float k2 = (y2 - y3) / (x2 - x3);
        float x = ((((k1 * x0) - (k2 * x2)) + y2) - y0) / (k1 - k2);
        float y = y0 + ((x - x0) * k1);
        this.mVertexBuffer.put(0, Math.max(this.mMidMaxLeft + 0.0f, -1.0f));
        this.mVertexBuffer.put(1, this.mPositionY);
        this.mVerticFuseTop[0] = Math.max(this.mMidMaxLeft + 0.0f, -1.0f);
        this.mVerticFuseTop[1] = this.mPositionY;
        float fuseXPoint1 = x0;
        float fuseYPoint1 = y0;
        float fuseXPoint2 = x;
        float fuseYPoint2 = y;
        float fuseXPoint3 = x2;
        float fuseYPoint3 = y2;
        for (i = 1; i < 40; i++) {
            float t = 0.025f * ((float) (i - 1));
            float a1 = (float) Math.pow((double) (1.0f - t), 2.0d);
            float a2 = (2.0f * t) * (1.0f - t);
            float a3 = t * t;
            float fuseTmpPointX = ((a1 * fuseXPoint1) + (a2 * x)) + (a3 * fuseXPoint3);
            float fuseTmpPointY = ((a1 * fuseYPoint1) + (a2 * y)) + (a3 * fuseYPoint3);
            this.mVertexBuffer.put(i * 2, fuseTmpPointX - 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, fuseTmpPointY - 0.0f);
            this.mVerticFuseTop[i * 2] = fuseTmpPointX - 0.0f;
            this.mVerticFuseTop[(i * 2) + 1] = fuseTmpPointY - 0.0f;
        }
        this.mVertexBuffer.put(80, fuseXPoint3);
        this.mVertexBuffer.put(81, fuseYPoint3);
        this.mVertexBuffer.put(82, -1.0f);
        this.mVertexBuffer.put(83, this.mPositionY);
        this.mVerticFuseTop[80] = fuseXPoint3;
        this.mVerticFuseTop[81] = fuseYPoint3;
        this.mVerticFuseTop[82] = -1.0f;
        this.mVerticFuseTop[83] = this.mPositionY;
    }

    public void resetVertexFuseQ3() {
        int i;
        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 0.0f;
        float y1 = 0.0f;
        float x2 = 0.0f;
        float y2 = 0.0f;
        float x3 = 0.0f;
        float y3 = 0.0f;
        float rightX = Math.min(this.mLeftMax + 0.03f, this.mPositionX + 0.18f);
        for (i = 0; i < 80; i++) {
            if (this.mVerticsMid[(i + 80) * 2] + this.mPositionX > rightX) {
                int rightIndex = i + 80;
                y0 = this.mVerticsMid[(rightIndex * 2) + 1] + this.mPositionY;
                x0 = this.mVerticsMid[rightIndex * 2] + this.mPositionX;
                y1 = this.mVerticsMid[(rightIndex * 2) - 1] + this.mPositionY;
                x1 = this.mVerticsMid[(rightIndex * 2) - 2] + this.mPositionX;
                break;
            }
        }
        float baseDis = 1.0f + this.mLeftMax;
        float ratio = (baseDis - Math.max(0.0f, 1.0f + this.mMidMaxLeft)) / baseDis;
        float leftY = (this.mPositionY - (((ratio * ratio) * ratio) * 0.26999998f)) - 0.08f;
        for (i = (int) this.mLeftXMax; i >= 1; i--) {
            if (this.mVerticsSideL[(i * 2) + 1] < leftY) {
                x2 = this.mVerticsSideL[i * 2];
                y2 = this.mVerticsSideL[(i * 2) + 1];
                x3 = this.mVerticsSideL[(i * 2) + 2];
                y3 = this.mVerticsSideL[(i * 2) + 3];
                break;
            }
        }
        float k1 = (y0 - y1) / (x0 - x1);
        float k2 = (y2 - y3) / (x2 - x3);
        float x = ((((k1 * x0) - (k2 * x2)) + y2) - y0) / (k1 - k2);
        float y = y0 + ((x - x0) * k1);
        this.mVertexBuffer.put(0, Math.max(this.mMidMaxLeft + 0.0f, -1.0f));
        this.mVertexBuffer.put(1, this.mPositionY);
        this.mVerticFuseBottom[0] = Math.max(this.mMidMaxLeft + 0.0f, -1.0f);
        this.mVerticFuseBottom[1] = this.mPositionY;
        float fuseXPoint1 = x0;
        float fuseYPoint1 = y0;
        float fuseXPoint2 = x;
        float fuseYPoint2 = y;
        float fuseXPoint3 = x2;
        float fuseYPoint3 = y2;
        for (i = 1; i < 40; i++) {
            float t = 0.025f * ((float) (i - 1));
            float a1 = (float) Math.pow((double) (1.0f - t), 2.0d);
            float a2 = (2.0f * t) * (1.0f - t);
            float a3 = t * t;
            float fuseTmpPointX = ((a1 * fuseXPoint1) + (a2 * x)) + (a3 * fuseXPoint3);
            float fuseTmpPointY = ((a1 * fuseYPoint1) + (a2 * y)) + (a3 * fuseYPoint3);
            this.mVertexBuffer.put(i * 2, fuseTmpPointX - 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, fuseTmpPointY - 0.0f);
            this.mVerticFuseBottom[i * 2] = fuseTmpPointX - 0.0f;
            this.mVerticFuseBottom[(i * 2) + 1] = fuseTmpPointY - 0.0f;
        }
        this.mVertexBuffer.put(80, fuseXPoint3);
        this.mVertexBuffer.put(81, fuseYPoint3);
        this.mVertexBuffer.put(82, -1.0f);
        this.mVertexBuffer.put(83, this.mPositionY);
        this.mVerticFuseBottom[80] = fuseXPoint3;
        this.mVerticFuseBottom[81] = fuseYPoint3;
        this.mVerticFuseBottom[82] = -1.0f;
        this.mVerticFuseBottom[83] = this.mPositionY;
    }

    public void resetVertexMidStart() {
        for (int i = 0; i < 160; i++) {
            this.mVertexBuffer.put(i * 2, ((this.mRadirStart / this.RADIRBASE) * this.mMidCosValue[i]) + this.mPositionX);
            this.mVertexBuffer.put((i * 2) + 1, ((this.mRadirStart / this.RADIRBASE) * this.mMidSinValue[i]) + this.mPositionY);
        }
        this.mMidMaxLeft = this.mVerticsMid[160] + this.mPositionX;
        this.mMidMaxRight = this.mVerticsMid[0] + this.mPositionX;
    }

    public void drawStart(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mAlphaStart * this.mColorA);
        gl.glDisable(3042);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mColorA);
        gl.glEnableClientState(32884);
        this.mOpenFlag = 0;
        resetVertexMidStart();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 160);
        gl.glDisable(3042);
        resetVertexSideStart(DrawWhere.LEFT_SIDE);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        resetVertexSideStart(DrawWhere.RIGHT_SIDE);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void drawMov(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glDisable(3042);
        gl.glShadeModel(7425);
        gl.glEnable(32925);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mColorA);
        gl.glEnableClientState(32884);
        if (this.mPositionX < 0.0f && this.mMidMaxLeft <= this.mLeftMax) {
            this.mOpenFlag = 1;
        } else if (this.mPositionX <= 0.0f || this.mMidMaxRight < this.mRightMax) {
            this.mOpenFlag = 0;
        } else {
            this.mOpenFlag = 1;
        }
        resetVertexSide(DrawWhere.LEFT_SIDE);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        resetVertexSide(DrawWhere.RIGHT_SIDE);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        resetVertexMid();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 160);
        if (this.mPositionX > 0.0f && this.mMidMaxRight >= this.mRightMax) {
            resetVertexFuseQ1();
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexFuseQ4();
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        } else if (this.mPositionX < 0.0f && this.mMidMaxLeft <= this.mLeftMax) {
            resetVertexFuseQ2();
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexFuseQ3();
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        }
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void drawOpen(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mColorA);
        gl.glEnableClientState(32884);
        resetVertexMidOpen();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 160);
        if (this.mPositionX > 0.0f) {
            resetVertexOpen(DrawWhere.RIGHT_SIDE);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 1002);
            resetVertexOpen(DrawWhere.FUSE_Q1);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexOpen(DrawWhere.FUSE_Q4);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        } else if (this.mPositionX < 0.0f) {
            resetVertexOpen(DrawWhere.LEFT_SIDE);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 1002);
            resetVertexOpen(DrawWhere.FUSE_Q2);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexOpen(DrawWhere.FUSE_Q3);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        }
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void drawDis(GL10 gl) {
        int i;
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glDisable(3042);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mColorA);
        gl.glEnableClientState(32884);
        for (i = 0; i < 160; i++) {
            this.mVertexBuffer.put(i * 2, ((this.mRadirStart / this.RADIRBASE) * this.mMidCosValue[i]) + this.mPositionX);
            this.mVertexBuffer.put((i * 2) + 1, ((this.mRadirStart / this.RADIRBASE) * this.mMidSinValue[i]) + this.mPositionY);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 160);
        this.mVertexBuffer.put(0, -1.0f);
        this.mVertexBuffer.put(1, this.mPositionY);
        for (i = 1; i <= 1001; i++) {
            this.mVertexBuffer.put(i * 2, (this.mVerticsSideL[i * 2] + 1.0f) - 1.0f);
            this.mVertexBuffer.put((i * 2) + 1, this.mVerticsSideL[(i * 2) + 1]);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        this.mVertexBuffer.put(0, 1.0f);
        this.mVertexBuffer.put(1, this.mPositionY);
        for (i = 1; i <= 1001; i++) {
            this.mVertexBuffer.put(i * 2, 1.0f - (1.0f - this.mVerticsSideR[i * 2]));
            this.mVertexBuffer.put((i * 2) + 1, this.mVerticsSideR[(i * 2) + 1]);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 1002);
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void drawEnd(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glColor4f(this.mColorR, this.mColorG, this.mColorB, this.mColorA);
        gl.glEnableClientState(32884);
        for (int i = 0; i < 160; i++) {
            this.mVertexBuffer.put(i * 2, 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, 0.0f);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 3);
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void updateDrawState(DrawStates tmpDrawState) {
        if (tmpDrawState == DrawStates.DISDRAW && this.mOpenFlag == 1) {
            if (this.mPositionX < 0.0f) {
                this.mOpenStepX = (this.mPositionX + 1.0f) / 5.0f;
            } else {
                this.mOpenStepX = (1.0f - this.mPositionX) / 5.0f;
            }
            this.mOpenStep2Flag = 0;
            this.mOpenStep2ratio = 1.0f;
            setDrawState(DrawStates.OPENDRAW);
        } else if (tmpDrawState == DrawStates.DISDRAW && this.mOpenFlag == 0) {
            setDrawState(tmpDrawState);
        } else if (tmpDrawState == DrawStates.STARTDRAW) {
            this.mRadirStart = 0.2f;
            this.mSideSubRatio = 0.2f;
            this.mAlphaStart = 0.0f;
            this.mDownPositionY = this.mPositionY;
            setDrawState(tmpDrawState);
        } else {
            setDrawState(tmpDrawState);
        }
    }

    private synchronized void setDrawState(DrawStates old, DrawStates tmpDrawState) {
        if (this.mDrawState != old) {
            HwLog.w("ParticleRenderer", "skip setDrawState to " + tmpDrawState + " as changed to " + this.mDrawState + " from " + old);
        } else {
            this.mDrawState = tmpDrawState;
        }
    }

    private synchronized void setDrawState(DrawStates tmpDrawState) {
        HwLog.w("ParticleRenderer", "setDrawState: from " + this.mDrawState + " to " + tmpDrawState);
        this.mDrawState = tmpDrawState;
    }

    private synchronized DrawStates getDrawState() {
        return this.mDrawState;
    }

    public void onDrawFrame(GL10 gl) {
        DrawStates drawState = getDrawState();
        if (drawState == DrawStates.NODRAW) {
            gl.glClear(16640);
        } else if (drawState == DrawStates.DISDRAW) {
            if (this.mAlphaStart > 0.1f) {
                this.mAlphaStart -= 0.1f;
                drawDis(gl);
                return;
            }
            this.mAlphaStart = 0.0f;
            drawEnd(gl);
        } else if (drawState == DrawStates.STARTDRAW) {
            if (this.mRadirStart < this.RADIRBASE) {
                this.mRadirStart += 0.01f;
                this.mAlphaStart += 0.2f;
                if (this.mRadirStart > 0.1f) {
                    this.mSideSubRatio -= 0.04f;
                    this.mSideSubRatio = Math.max(this.mSideSubRatio, 0.0f);
                }
                drawStart(gl);
                return;
            }
            this.mRadirStart = this.RADIRBASE;
            setDrawState(drawState, DrawStates.MOVEDRAW);
            drawStart(gl);
        } else if (drawState != DrawStates.OPENDRAW) {
            this.mRadirStart = this.RADIRBASE;
            drawMov(gl);
        } else if (this.mOpenStep2Flag == 1) {
            if (this.mOpenStep2ratio > 0.1f) {
                this.mOpenStep2ratio -= 0.1f;
                drawOpen(gl);
                return;
            }
            drawEnd(gl);
        } else if (this.mPositionX < 0.0f && this.mPositionX > -1.0f) {
            nextX = this.mPositionX - this.mOpenStepX;
            if (this.mPositionX + 1.0f <= this.mOpenStepX) {
                this.mOpenStep2Flag = 1;
                this.mOpenLeftorRightFlag = 0;
                nextX = -1.0f;
            }
            setTranslaterXY(nextX, this.mPositionY);
            drawMov(gl);
        } else if (this.mPositionX > 0.0f && this.mPositionX < 1.0f) {
            nextX = this.mPositionX + this.mOpenStepX;
            if (1.0f - this.mPositionX <= this.mOpenStepX) {
                this.mOpenStep2Flag = 1;
                this.mOpenLeftorRightFlag = 1;
                nextX = 1.0f;
            }
            setTranslaterXY(nextX, this.mPositionY);
            drawMov(gl);
        } else if (this.mPositionX <= -1.0f) {
            this.mOpenStep2Flag = 1;
            this.mOpenLeftorRightFlag = 0;
        } else if (this.mPositionX >= 1.0f) {
            this.mOpenStep2Flag = 1;
            this.mOpenLeftorRightFlag = 1;
        }
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mLCDWidth = (float) (width >> 1);
        this.mLCDHeigh = (float) (height >> 1);
    }
}
