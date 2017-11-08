package com.android.deskclock.alarmclock;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import com.android.deskclock.R;
import com.android.util.HwLog;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class ParticleRenderer implements Renderer {
    private static final /* synthetic */ int[] -com-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues = null;
    private float LCD_RATIO = 0.0f;
    private Bitmap bitmap;
    private FloatBuffer lightTexBuffer;
    private FloatBuffer lightVertexBuffer;
    private float[] mAdjustSide = new float[66];
    private float mAlphaStart = 1.0f;
    private Context mContext;
    DrawStates mDrawState;
    private float mLCDHeigh = 0.0f;
    private float mLCDWidth = 0.0f;
    private float mLeftMax = -1.0f;
    private float mLeftSigmaTmp = 1.0f;
    private float[] mLightCosValue = new float[81];
    private float mLightLeftRadirEnd = 0.0f;
    private float mLightLeftRadirStep = 0.0f;
    private float mLightLeftRadirTmp = 0.0f;
    private float mLightRadirRatio = 12.0f;
    private float mLightRightRadirEnd = 0.0f;
    private float mLightRightRadirStep = 0.0f;
    private float mLightRightRadirTmp = 0.0f;
    private float[] mLightSinValue = new float[81];
    private float[] mMidCosValue = new float[81];
    private float mMidMaxLeft;
    private float mMidMaxRight;
    private float[] mMidSinValue = new float[81];
    private float mNormalJumpScreen = 0.0140625f;
    private float mNormalJumpSigmaLeft = 0.0f;
    private float mNormalJumpSigmaRight = 0.0f;
    private float mNormalLeftA = 0.0f;
    private float mNormalLeftB = 0.0f;
    private float mNormalLeftSigmaMax = 12.0f;
    private float mNormalLeftSigmaMin = 6.0f;
    private float mNormalMovValue = 0.005f;
    private float mNormalRightA = 0.0f;
    private float mNormalRightB = 0.0f;
    private float mNormalRightSigmaMax = 12.0f;
    private float mNormalRightSigmaMin = 6.0f;
    private int mOpenFlag = 0;
    private int mOpenLeftorRightFlag = 0;
    private int mOpenStep2Flag = 0;
    private float mOpenStep2ratio = 1.0f;
    private float mOpenStepX = 0.0f;
    private float mOpenSubRatio = 1.0f;
    private float mPositionLightLeftX = -0.95f;
    private float mPositionLightLeftY = 0.1f;
    private float mPositionLightRightX = 0.95f;
    private float mPositionLightRightY = 0.1f;
    private float mPositionX = 0.0f;
    private float mPositionY = 0.0f;
    private float mRadirStart = 0.02f;
    private float mRightMax = 1.0f;
    private float mRightSigmaTmp = 1.0f;
    private float mSideSubRatio = 1.0f;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mVertexBufferColor;
    private float[] mVerticFuseBottom = new float[84];
    private float[] mVerticFuseTop = new float[84];
    private float[] mVerticsLightLeft = new float[164];
    private float[] mVerticsLightRight = new float[164];
    private float[] mVerticsM = new float[160];
    private float[] mVerticsSideL = new float[132];
    private float[] mVerticsSideR = new float[132];
    private int[] textureids;

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

    private static /* synthetic */ int[] -getcom-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues() {
        if (-com-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues != null) {
            return -com-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues;
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
        -com-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues = iArr;
        return iArr;
    }

    public ParticleRenderer(Context context) {
        this.mContext = context;
    }

    public void setLCDRatio(float ratio) {
        this.LCD_RATIO = ratio;
    }

    public float degToRadForMid(float deg) {
        return (float) ((((double) deg) * 3.141592653589793d) / 80.0d);
    }

    public float degToRadForSideLight(float deg) {
        return (float) ((((double) deg) * 3.141592653589793d) / 80.0d);
    }

    public float getColorR(float xPos) {
        return 1.0f - (Math.abs(xPos) * 0.8f);
    }

    public float getColorG(float xPos) {
        return 1.0f - (Math.abs(xPos) * 0.2f);
    }

    public float getColorB(float xPos) {
        return 1.0f - (Math.abs(xPos) * 0.05f);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glShadeModel(7425);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glClearDepthf(1.0f);
        gl.glEnable(2929);
        gl.glDepthFunc(515);
        gl.glHint(3152, 4353);
        this.bitmap = BitmapFactory.decodeResource(this.mContext.getResources(), R.drawable.clock);
        if (this.bitmap == null) {
            HwLog.e("ParticleRenderer", "onSurfaceCreated onSurfaceCreated bitmap == null");
            return;
        }
        this.textureids = new int[1];
        gl.glEnable(3553);
        gl.glGenTextures(1, this.textureids, 0);
        gl.glBindTexture(3553, this.textureids[0]);
        GLUtils.texImage2D(3553, 0, this.bitmap, 0);
        gl.glTexParameterx(3553, 10241, 9729);
        gl.glTexParameterx(3553, 10240, 9729);
        this.bitmap.recycle();
        iniParas();
    }

    public void iniParas() {
        int i;
        this.mVertexBuffer = ByteBuffer.allocateDirect(656).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mVertexBufferColor = ByteBuffer.allocateDirect(1312).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.lightVertexBuffer = ByteBuffer.allocateDirect(48).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.lightTexBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mPositionY = 0.0f;
        this.mPositionX = 0.0f;
        for (i = 1; i <= 65; i++) {
            this.mAdjustSide[i] = 0.0f;
        }
        for (i = 0; i <= 80; i++) {
            this.mLightSinValue[i] = this.LCD_RATIO * ((float) Math.sin((double) degToRadForSideLight((float) (i * 2))));
            this.mLightCosValue[i] = (float) Math.cos((double) degToRadForSideLight((float) (i * 2)));
        }
        for (i = 0; i < 80; i++) {
            this.mMidSinValue[i] = (this.LCD_RATIO * 0.19f) * ((float) Math.sin((double) degToRadForMid((float) (i * 2))));
            this.mMidCosValue[i] = ((float) Math.cos((double) degToRadForMid((float) (i * 2)))) * 0.19f;
        }
        this.mDrawState = DrawStates.NODRAW;
        this.mLightLeftRadirEnd = (float) ((Math.random() / 200.0d) + 0.014999999664723873d);
        this.mLightLeftRadirStep = 0.0f;
        this.mLightRightRadirEnd = (float) ((Math.random() / 200.0d) + 0.014999999664723873d);
        this.mLightRightRadirStep = 0.0f;
        this.mPositionLightLeftY = (float) ((Math.random() / 2.0d) - 0.25d);
        this.mPositionLightRightY = (float) ((Math.random() / 2.0d) - 0.25d);
        this.mLeftSigmaTmp = ((this.mNormalLeftSigmaMax - this.mNormalLeftSigmaMin) * (((this.mPositionX + 0.6f) * (this.mPositionX + 0.6f)) / 2.5600002f)) + this.mNormalLeftSigmaMin;
        this.mRightSigmaTmp = ((this.mNormalRightSigmaMax - this.mNormalRightSigmaMin) * (((this.mPositionX - 0.6f) * (this.mPositionX - 0.6f)) / 2.5600002f)) + this.mNormalRightSigmaMin;
        this.mNormalLeftA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mLeftSigmaTmp);
        this.mNormalRightA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mRightSigmaTmp);
        this.mNormalJumpSigmaLeft = (this.mLeftSigmaTmp * 3.0f) / 32.0f;
        this.mNormalJumpSigmaRight = (this.mRightSigmaTmp * 3.0f) / 32.0f;
        resetVertexSide(DrawWhere.LEFT_SIDE);
        resetVertexSide(DrawWhere.RIGHT_SIDE);
    }

    public void setTranslaterF(float x, float y) {
        this.mPositionX = (x - this.mLCDWidth) / this.mLCDWidth;
        this.mPositionY = (this.mLCDHeigh - y) / this.mLCDHeigh;
        this.mLeftSigmaTmp = ((this.mNormalLeftSigmaMax - this.mNormalLeftSigmaMin) * (((this.mPositionX + 0.6f) * (this.mPositionX + 0.6f)) / 2.5600002f)) + this.mNormalLeftSigmaMin;
        this.mRightSigmaTmp = ((this.mNormalRightSigmaMax - this.mNormalRightSigmaMin) * (((this.mPositionX - 0.6f) * (this.mPositionX - 0.6f)) / 2.5600002f)) + this.mNormalRightSigmaMin;
        this.mNormalLeftA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mLeftSigmaTmp);
        this.mNormalRightA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mRightSigmaTmp);
        this.mNormalJumpSigmaLeft = (this.mLeftSigmaTmp * 3.0f) / 32.0f;
        this.mNormalJumpSigmaRight = (this.mRightSigmaTmp * 3.0f) / 32.0f;
    }

    public void setTranslaterXY(float x, float y) {
        this.mPositionX = x;
        this.mPositionY = y;
        this.mLeftSigmaTmp = ((this.mNormalLeftSigmaMax - this.mNormalLeftSigmaMin) * (((this.mPositionX + 0.6f) * (this.mPositionX + 0.6f)) / 2.5600002f)) + this.mNormalLeftSigmaMin;
        this.mRightSigmaTmp = ((this.mNormalRightSigmaMax - this.mNormalRightSigmaMin) * (((this.mPositionX - 0.6f) * (this.mPositionX - 0.6f)) / 2.5600002f)) + this.mNormalRightSigmaMin;
        this.mNormalLeftA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mLeftSigmaTmp);
        this.mNormalRightA = 1.0f / (((float) Math.sqrt(6.283185307179586d)) * this.mRightSigmaTmp);
        this.mNormalJumpSigmaLeft = (this.mLeftSigmaTmp * 3.0f) / 32.0f;
        this.mNormalJumpSigmaRight = (this.mRightSigmaTmp * 3.0f) / 32.0f;
    }

    public void resetVertexSide(DrawWhere who) {
        int i;
        float PointPos;
        float PointPosY;
        switch (-getcom-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues()[who.ordinal()]) {
            case 5:
                this.mVertexBuffer.put(0, -1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                this.mVerticsSideL[0] = -1.0f;
                this.mVerticsSideL[1] = this.mPositionY;
                for (i = 1; i <= 65; i++) {
                    PointPos = (float) ((i - 32) - 1);
                    PointPosY = PointPos * this.mNormalJumpSigmaLeft;
                    this.mNormalLeftB = ((-1.0f * PointPosY) * PointPosY) / (this.mLeftSigmaTmp * this.mLeftSigmaTmp);
                    float lenthLeft = (Math.max(((this.mNormalLeftA * ((float) Math.exp((double) this.mNormalLeftB))) - 1.0f) - this.mNormalMovValue, -1.0f) + 1.0f) - 1.0f;
                    if (i == 33) {
                        this.mLeftMax = lenthLeft;
                    }
                    this.mVertexBuffer.put(i * 2, lenthLeft);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideL[i * 2] = lenthLeft;
                    this.mVerticsSideL[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                }
                return;
            case 6:
                this.mVertexBuffer.put(0, 1.0f);
                this.mVertexBuffer.put(1, this.mPositionY);
                this.mVerticsSideR[0] = 1.0f;
                this.mVerticsSideR[1] = this.mPositionY;
                for (i = 1; i <= 65; i++) {
                    PointPos = (float) ((i - 32) - 1);
                    PointPosY = PointPos * this.mNormalJumpSigmaRight;
                    this.mNormalRightB = ((-1.0f * PointPosY) * PointPosY) / (this.mRightSigmaTmp * this.mRightSigmaTmp);
                    float lenthRight = 1.0f - (1.0f - Math.min(this.mNormalMovValue + (1.0f - (this.mNormalRightA * ((float) Math.exp((double) this.mNormalRightB)))), 1.0f));
                    if (i == 33) {
                        this.mRightMax = lenthRight;
                    }
                    this.mVertexBuffer.put(i * 2, lenthRight);
                    this.mVertexBuffer.put((i * 2) + 1, (this.mNormalJumpScreen * PointPos) + this.mPositionY);
                    this.mVerticsSideR[i * 2] = lenthRight;
                    this.mVerticsSideR[(i * 2) + 1] = (this.mNormalJumpScreen * PointPos) + this.mPositionY;
                }
                return;
            default:
                return;
        }
    }

    public void resetVertexMid() {
        float maxG;
        int i;
        float ratio;
        if (this.mPositionX > 0.0f) {
            if (this.mRightMax - this.mMidMaxRight <= 0.1f) {
                maxG = ((0.1f - Math.max(this.mRightMax - this.mMidMaxRight, 0.0f)) * 0.05f) / 0.1f;
                for (i = 0; i < 80; i++) {
                    ratio = Math.max(this.mMidCosValue[i], 0.0f) / 0.19f;
                    this.mVerticsM[i * 2] = this.mMidCosValue[i] + (((maxG * ratio) * ratio) * ratio);
                    this.mVerticsM[(i * 2) + 1] = this.mMidSinValue[i];
                    this.mVertexBuffer.put(i * 2, this.mVerticsM[i * 2] + this.mPositionX);
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticsM[(i * 2) + 1] + this.mPositionY);
                }
            } else {
                for (i = 0; i < 80; i++) {
                    this.mVerticsM[i * 2] = this.mMidCosValue[i];
                    this.mVerticsM[(i * 2) + 1] = this.mMidSinValue[i];
                    this.mVertexBuffer.put(i * 2, this.mMidCosValue[i] + this.mPositionX);
                    this.mVertexBuffer.put((i * 2) + 1, this.mMidSinValue[i] + this.mPositionY);
                }
            }
        } else if (this.mMidMaxLeft - this.mLeftMax <= 0.1f) {
            maxG = ((0.1f - Math.max(this.mMidMaxLeft - this.mLeftMax, 0.0f)) * 0.05f) / 0.1f;
            for (i = 0; i < 80; i++) {
                ratio = Math.abs(Math.min(this.mMidCosValue[i], 0.0f)) / 0.19f;
                this.mVerticsM[i * 2] = this.mMidCosValue[i] - (((maxG * ratio) * ratio) * ratio);
                this.mVerticsM[(i * 2) + 1] = this.mMidSinValue[i];
                this.mVertexBuffer.put(i * 2, this.mVerticsM[i * 2] + this.mPositionX);
                this.mVertexBuffer.put((i * 2) + 1, this.mVerticsM[(i * 2) + 1] + this.mPositionY);
            }
        } else {
            for (i = 0; i < 80; i++) {
                this.mVerticsM[i * 2] = this.mMidCosValue[i];
                this.mVerticsM[(i * 2) + 1] = this.mMidSinValue[i];
                this.mVertexBuffer.put(i * 2, this.mMidCosValue[i] + this.mPositionX);
                this.mVertexBuffer.put((i * 2) + 1, this.mMidSinValue[i] + this.mPositionY);
            }
        }
        this.mMidMaxLeft = this.mVerticsM[80] + this.mPositionX;
        this.mMidMaxRight = this.mVerticsM[0] + this.mPositionX;
    }

    public void resetVertexMidMixOpen() {
        int i;
        float LeftTopY = 0.0f;
        if (this.mOpenLeftorRightFlag == 0) {
            for (i = 0; i < 80; i++) {
                if (((this.mVerticsM[i * 2] * Math.max(this.mOpenStep2ratio, 0.5f)) + this.mPositionX) - (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f) <= -1.0f) {
                    LeftTopY = this.mVerticsM[(i * 2) + 1];
                    break;
                }
            }
        }
        for (i = 0; i < 80; i++) {
            float tmpXPos = (this.mVerticsM[i * 2] * Math.max(this.mOpenStep2ratio, 0.5f)) + this.mPositionX;
            float tmpYPos = this.mVerticsM[(i * 2) + 1] + this.mPositionY;
            if (this.mOpenLeftorRightFlag == 0) {
                tmpXPos = Math.max(tmpXPos - (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f), -1.0f);
                if (tmpXPos == -1.0f) {
                    tmpYPos = tmpYPos >= this.mPositionY ? LeftTopY + this.mPositionY : this.mPositionY - LeftTopY;
                }
            } else {
                tmpXPos = Math.min((Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f) + tmpXPos, 1.0f);
            }
            this.mVertexBuffer.put(i * 2, tmpXPos);
            this.mVertexBuffer.put((i * 2) + 1, tmpYPos);
        }
    }

    public void resetVertexMixOpen(DrawWhere where) {
        int i;
        switch (-getcom-android-deskclock-alarmclock-ParticleRenderer$DrawWhereSwitchesValues()[where.ordinal()]) {
            case 1:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, Math.min((1.0f - (Math.max(this.mOpenStep2ratio, 0.5f) * (1.0f - this.mVerticFuseTop[i * 2]))) + (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f), 1.0f));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseTop[(i * 2) + 1]);
                }
                return;
            case 2:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, Math.max(((Math.max(this.mOpenStep2ratio, 0.5f) * (1.0f + this.mVerticFuseTop[i * 2])) - 4.0f) - (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f), -1.0f));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseTop[(i * 2) + 1]);
                }
                return;
            case 3:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, Math.max(((Math.max(this.mOpenStep2ratio, 0.5f) * (1.0f + this.mVerticFuseTop[i * 2])) - 4.0f) - (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f), -1.0f));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseBottom[(i * 2) + 1]);
                }
                return;
            case MetaballPath.POINT_NUM /*4*/:
                for (i = 0; i <= 41; i++) {
                    this.mVertexBuffer.put(i * 2, Math.min((1.0f - (Math.max(this.mOpenStep2ratio, 0.5f) * (1.0f - this.mVerticFuseTop[i * 2]))) + (Math.max((1.0f - this.mOpenStep2ratio) - 0.5f, 0.0f) * 0.19f), 1.0f));
                    this.mVertexBuffer.put((i * 2) + 1, this.mVerticFuseBottom[(i * 2) + 1]);
                }
                return;
            default:
                return;
        }
    }

    public void resetVertexFuse(int point) {
        int i;
        boolean isLeft = (point & 2) != 0;
        boolean isTop = (point & 1) != 0;
        float x0 = 0.0f;
        float y0 = 0.0f;
        float x1 = 0.0f;
        float y1 = 0.0f;
        float leftX;
        if (isLeft && isTop) {
            leftX = Math.max(this.mRightMax - 0.06f, this.mPositionX - 0.12f);
            for (i = 0; i < 80; i++) {
                if (this.mVerticsM[i * 2] + this.mPositionX < leftX) {
                    y0 = this.mVerticsM[(i * 2) + 1] + this.mPositionY;
                    x0 = this.mVerticsM[i * 2] + this.mPositionX;
                    y1 = this.mVerticsM[(i * 2) - 1] + this.mPositionY;
                    x1 = this.mVerticsM[(i * 2) - 2] + this.mPositionX;
                    break;
                }
            }
        } else {
            float rightX;
            int rightIndex;
            if (((isLeft ? 0 : 1) & isTop) != 0) {
                rightX = Math.min(this.mLeftMax + 0.06f, this.mPositionX + 0.12f);
                for (i = 0; i < 40; i++) {
                    if (this.mVerticsM[(40 - i) * 2] + this.mPositionX > rightX) {
                        rightIndex = 40 - i;
                        y0 = this.mVerticsM[(rightIndex * 2) + 1] + this.mPositionY;
                        x0 = this.mVerticsM[rightIndex * 2] + this.mPositionX;
                        y1 = this.mVerticsM[(rightIndex * 2) + 3] + this.mPositionY;
                        x1 = this.mVerticsM[(rightIndex * 2) + 2] + this.mPositionX;
                        break;
                    }
                }
            } else if (isLeft || isTop) {
                leftX = Math.max(this.mRightMax - 0.06f, this.mPositionX - 0.12f);
                for (i = 0; i < 80; i++) {
                    if (this.mVerticsM[(79 - i) * 2] + this.mPositionX < leftX) {
                        int leftIndex = 79 - i;
                        y0 = this.mVerticsM[(leftIndex * 2) + 1] + this.mPositionY;
                        x0 = this.mVerticsM[leftIndex * 2] + this.mPositionX;
                        y1 = this.mVerticsM[(leftIndex * 2) + 3] + this.mPositionY;
                        x1 = this.mVerticsM[(leftIndex * 2) + 2] + this.mPositionX;
                        break;
                    }
                }
            } else {
                rightX = Math.min(this.mLeftMax + 0.06f, this.mPositionX + 0.12f);
                for (i = 0; i < 40; i++) {
                    if (this.mVerticsM[(i + 40) * 2] + this.mPositionX > rightX) {
                        rightIndex = i + 40;
                        y0 = this.mVerticsM[(rightIndex * 2) + 1] + this.mPositionY;
                        x0 = this.mVerticsM[rightIndex * 2] + this.mPositionX;
                        y1 = this.mVerticsM[(rightIndex * 2) - 1] + this.mPositionY;
                        x1 = this.mVerticsM[(rightIndex * 2) - 2] + this.mPositionX;
                        break;
                    }
                }
            }
        }
        float baseDis = 1.0f + (isLeft ? -this.mRightMax : this.mLeftMax);
        float ratio = (baseDis - Math.max(0.0f, 1.0f + (isLeft ? -this.mMidMaxRight : this.mMidMaxLeft))) / baseDis;
        float x2 = isLeft ? 1.0f : -1.0f;
        float y2 = this.mPositionY + (((float) (isTop ? 1 : -1)) * ((((ratio * ratio) * ratio) * 0.059999995f) + 0.1f));
        float k1 = (y0 - y1) / (x0 - x1);
        float k2 = (y2 - (y2 + (((float) (isTop ? -1 : 1)) * 0.001f))) / (x2 - (x2 + (((float) (isLeft ? -1 : 1)) * 5.0E-4f)));
        float x = ((((k1 * x0) - (k2 * x2)) + y2) - y0) / (k1 - k2);
        float y = y0 + ((x - x0) * k1);
        float positionX = isLeft ? Math.min(this.mMidMaxRight + 0.0f, 1.0f) : Math.max(this.mMidMaxLeft + 0.0f, -1.0f);
        this.mVertexBuffer.put(0, positionX);
        this.mVertexBuffer.put(1, this.mPositionY);
        if (isTop) {
            this.mVerticFuseTop[0] = positionX;
            this.mVerticFuseTop[1] = this.mPositionY;
        }
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
            float fuseTmpPointX = ((a1 * fuseXPoint1) + (a2 * x)) + (a3 * x2);
            float fuseTmpPointY = ((a1 * fuseYPoint1) + (a2 * y)) + (a3 * y2);
            this.mVertexBuffer.put(i * 2, fuseTmpPointX - 0.0f);
            this.mVertexBuffer.put((i * 2) + 1, fuseTmpPointY - 0.0f);
            if (isTop) {
                this.mVerticFuseTop[i * 2] = fuseTmpPointX - 0.0f;
                this.mVerticFuseTop[(i * 2) + 1] = fuseTmpPointY - 0.0f;
            } else {
                this.mVerticFuseBottom[i * 2] = fuseTmpPointX - 0.0f;
                this.mVerticFuseBottom[(i * 2) + 1] = fuseTmpPointY - 0.0f;
            }
        }
        this.mVertexBuffer.put(80, x2);
        this.mVertexBuffer.put(81, y2);
        this.mVertexBuffer.put(82, isLeft ? 1.0f : -1.0f);
        this.mVertexBuffer.put(83, this.mPositionY);
        if (isTop) {
            this.mVerticFuseTop[80] = x2;
            this.mVerticFuseTop[81] = y2;
            this.mVerticFuseTop[82] = isLeft ? 1.0f : -1.0f;
            this.mVerticFuseTop[83] = this.mPositionY;
            return;
        }
        this.mVerticFuseBottom[80] = x2;
        this.mVerticFuseBottom[81] = y2;
        this.mVerticFuseBottom[82] = isLeft ? 1.0f : -1.0f;
        this.mVerticFuseBottom[83] = this.mPositionY;
    }

    public void resetVertexFuseQ1() {
        resetVertexFuse(3);
    }

    public void resetVertexFuseQ4() {
        resetVertexFuse(2);
    }

    public void resetVertexFuseQ2() {
        resetVertexFuse(1);
    }

    public void resetVertexFuseQ3() {
        resetVertexFuse(0);
    }

    public void resetVertexMidStart() {
        for (int i = 0; i < 80; i++) {
            this.mVertexBuffer.put(i * 2, ((this.mRadirStart / 0.19f) * this.mMidCosValue[i]) + this.mPositionX);
            this.mVertexBuffer.put((i * 2) + 1, ((this.mRadirStart / 0.19f) * this.mMidSinValue[i]) + this.mPositionY);
        }
        this.mMidMaxLeft = (this.mRadirStart * this.mMidCosValue[40]) + this.mPositionX;
        this.mMidMaxRight = (this.mRadirStart * this.mMidCosValue[0]) + this.mPositionX;
    }

    public void resetTextureLeft() {
        float leftX0 = this.mPositionLightLeftX - this.mLightLeftRadirTmp;
        float leftX1 = this.mPositionLightLeftX + this.mLightLeftRadirTmp;
        float leftY0 = this.mPositionLightLeftY - (this.mLightLeftRadirTmp * this.mLightRadirRatio);
        float leftY1 = this.mPositionLightLeftY + (this.mLightLeftRadirTmp * this.mLightRadirRatio);
        this.lightVertexBuffer.put(0, leftX0);
        this.lightVertexBuffer.put(1, leftY0);
        this.lightVertexBuffer.put(2, 0.0f);
        this.lightVertexBuffer.put(3, leftX1);
        this.lightVertexBuffer.put(4, leftY0);
        this.lightVertexBuffer.put(5, 0.0f);
        this.lightVertexBuffer.put(6, leftX0);
        this.lightVertexBuffer.put(7, leftY1);
        this.lightVertexBuffer.put(8, 0.0f);
        this.lightVertexBuffer.put(9, leftX1);
        this.lightVertexBuffer.put(10, leftY1);
        this.lightVertexBuffer.put(11, 0.0f);
        this.lightVertexBuffer.position(0);
        this.lightTexBuffer.put(0, 0.0f);
        this.lightTexBuffer.put(1, 0.0f);
        this.lightTexBuffer.put(2, 1.0f);
        this.lightTexBuffer.put(3, 0.0f);
        this.lightTexBuffer.put(4, 0.0f);
        this.lightTexBuffer.put(5, 1.0f);
        this.lightTexBuffer.put(6, 1.0f);
        this.lightTexBuffer.put(7, 1.0f);
        this.lightTexBuffer.position(0);
    }

    public void resetTextureRight() {
        float RightX0 = this.mPositionLightRightX - this.mLightRightRadirTmp;
        float RightX1 = this.mPositionLightRightX + this.mLightRightRadirTmp;
        float RightY0 = this.mPositionLightRightY - (this.mLightRightRadirTmp * this.mLightRadirRatio);
        float RightY1 = this.mPositionLightRightY + (this.mLightRightRadirTmp * this.mLightRadirRatio);
        this.lightVertexBuffer.put(0, RightX0);
        this.lightVertexBuffer.put(1, RightY0);
        this.lightVertexBuffer.put(2, 0.0f);
        this.lightVertexBuffer.put(3, RightX1);
        this.lightVertexBuffer.put(4, RightY0);
        this.lightVertexBuffer.put(5, 0.0f);
        this.lightVertexBuffer.put(6, RightX0);
        this.lightVertexBuffer.put(7, RightY1);
        this.lightVertexBuffer.put(8, 0.0f);
        this.lightVertexBuffer.put(9, RightX1);
        this.lightVertexBuffer.put(10, RightY1);
        this.lightVertexBuffer.put(11, 0.0f);
        this.lightVertexBuffer.position(0);
    }

    private void drawBase(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glDisable(3042);
        gl.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
        gl.glEnable(3553);
        gl.glEnableClientState(32884);
        gl.glEnableClientState(32888);
        gl.glDisableClientState(32886);
        resetTextureLeft();
        gl.glVertexPointer(3, 5126, 0, this.lightVertexBuffer);
        gl.glTexCoordPointer(2, 5126, 0, this.lightTexBuffer);
        gl.glDrawArrays(5, 0, 4);
        resetTextureRight();
        gl.glVertexPointer(3, 5126, 0, this.lightVertexBuffer);
        gl.glTexCoordPointer(2, 5126, 0, this.lightTexBuffer);
        gl.glDrawArrays(5, 0, 4);
    }

    public void drawNoDraw(GL10 gl) {
        drawBase(gl);
        gl.glDisableClientState(32888);
        gl.glDisableClientState(32884);
        gl.glDisable(3553);
        gl.glFinish();
    }

    public void drawStart(GL10 gl) {
        drawBase(gl);
        gl.glDisableClientState(32888);
        gl.glDisable(3553);
        gl.glEnableClientState(32886);
        gl.glColor4f(0.5f, 0.5f, 0.5f, 0.5f);
        this.mOpenFlag = 0;
        resetVertexMidStart();
        for (int i = 0; i < 80; i++) {
            this.mVertexBufferColor.put(i * 4, getColorR(this.mVertexBuffer.get(i * 2)));
            this.mVertexBufferColor.put((i * 4) + 1, getColorG(this.mVertexBuffer.get(i * 2)));
            this.mVertexBufferColor.put((i * 4) + 2, getColorB(this.mVertexBuffer.get(i * 2)));
            this.mVertexBufferColor.put((i * 4) + 3, 0.5f);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glColorPointer(4, 5126, 0, this.mVertexBufferColor);
        gl.glDrawArrays(6, 0, 80);
        gl.glDisableClientState(32884);
        gl.glDisableClientState(32886);
        gl.glDisable(3042);
        gl.glFinish();
    }

    public void drawMov(GL10 gl) {
        drawBase(gl);
        gl.glDisableClientState(32888);
        gl.glDisable(3553);
        if (this.mPositionX < 0.0f && this.mMidMaxLeft <= this.mLeftMax) {
            this.mOpenFlag = 1;
        } else if (this.mPositionX <= 0.0f || this.mMidMaxRight < this.mRightMax) {
            this.mOpenFlag = 0;
        } else {
            this.mOpenFlag = 1;
        }
        gl.glColor4f(getColorR(this.mPositionX), getColorG(this.mPositionX), getColorB(this.mPositionX), 0.5f);
        resetVertexMid();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 80);
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
        gl.glEnableClientState(32884);
        gl.glColor4f(getColorR(this.mPositionX), getColorG(this.mPositionX), getColorB(this.mPositionX), 0.5f);
        resetVertexMidMixOpen();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 80);
        if (this.mPositionX > 0.0f) {
            resetVertexMixOpen(DrawWhere.FUSE_Q1);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexMixOpen(DrawWhere.FUSE_Q4);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        } else if (this.mPositionX < 0.0f) {
            resetVertexMixOpen(DrawWhere.FUSE_Q2);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
            resetVertexMixOpen(DrawWhere.FUSE_Q3);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 42);
        }
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void drawDis(GL10 gl) {
        drawBase(gl);
        gl.glDisableClientState(32888);
        gl.glDisable(3553);
        gl.glEnable(3042);
        gl.glBlendFunc(770, 771);
        gl.glColor4f(0.5f, 0.5f, 0.5f, this.mAlphaStart);
        resetVertexMid();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 80);
        gl.glDisableClientState(32884);
        gl.glDisable(3042);
        gl.glFinish();
    }

    public void drawEnd(GL10 gl) {
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glEnable(32925);
        gl.glColor4f(0.4f, 0.8f, 0.4f, 0.5f);
        gl.glEnableClientState(32884);
        for (int i = 0; i < 164; i++) {
            this.mVertexBuffer.put(i, 0.0f);
        }
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 3);
        gl.glDisableClientState(32884);
        gl.glFinish();
    }

    public void setDrawState(DrawStates tmpDrawState) {
        if (tmpDrawState == DrawStates.DISDRAW && this.mOpenFlag == 1) {
            this.mOpenStepX = 0.05f;
            this.mDrawState = DrawStates.OPENDRAW;
            this.mOpenStep2Flag = 0;
            this.mOpenStep2ratio = 1.0f;
            this.mOpenSubRatio = 0.0f;
        } else if (tmpDrawState == DrawStates.DISDRAW && this.mOpenFlag == 0) {
            this.mAlphaStart = 1.0f;
            this.mDrawState = tmpDrawState;
        } else if (tmpDrawState == DrawStates.STARTDRAW) {
            this.mRadirStart = 0.1f;
            this.mDrawState = tmpDrawState;
            this.mSideSubRatio = 1.0f;
        } else {
            this.mDrawState = tmpDrawState;
        }
    }

    public void onDrawFrame(GL10 gl) {
        this.mLightLeftRadirStep += 0.003f;
        this.mLightLeftRadirTmp = this.mLightLeftRadirEnd - Math.abs(this.mLightLeftRadirStep - this.mLightLeftRadirEnd);
        if (this.mLightLeftRadirTmp < 0.0f) {
            this.mLightLeftRadirEnd = (float) ((Math.random() / 25.0d) + 0.03999999910593033d);
            this.mLightLeftRadirStep = 0.0f;
            this.mPositionLightLeftY = (float) ((Math.random() / 2.5d) - 0.20000000298023224d);
        }
        this.mLightRightRadirStep += 0.003f;
        this.mLightRightRadirTmp = this.mLightRightRadirEnd - Math.abs(this.mLightRightRadirStep - this.mLightRightRadirEnd);
        if (this.mLightRightRadirTmp < 0.0f) {
            this.mLightRightRadirEnd = (float) ((Math.random() / 25.0d) + 0.03999999910593033d);
            this.mLightRightRadirStep = 0.0f;
            this.mPositionLightRightY = (float) ((Math.random() / 2.5d) - 0.20000000298023224d);
        }
        if (this.mDrawState == DrawStates.NODRAW) {
            drawNoDraw(gl);
        } else if (this.mDrawState == DrawStates.DISDRAW) {
            if (this.mAlphaStart > 0.1f) {
                this.mAlphaStart -= 0.1f;
                drawDis(gl);
                return;
            }
            this.mAlphaStart = 0.0f;
            drawNoDraw(gl);
            this.mDrawState = DrawStates.NODRAW;
        } else if (this.mDrawState == DrawStates.STARTDRAW) {
            if (this.mRadirStart < 0.19f) {
                this.mRadirStart += 0.01f;
                if (this.mRadirStart > 0.1f) {
                    this.mSideSubRatio -= 0.1f;
                    this.mSideSubRatio = Math.max(this.mSideSubRatio, 0.0f);
                }
                drawStart(gl);
                return;
            }
            this.mRadirStart = 0.19f;
            drawStart(gl);
            this.mDrawState = DrawStates.MOVEDRAW;
        } else if (this.mDrawState == DrawStates.OPENDRAW) {
            onOpenDrawAction(gl);
        } else {
            this.mRadirStart = 0.19f;
            drawMov(gl);
        }
    }

    public void onOpenDrawAction(GL10 gl) {
        if (this.mOpenStep2Flag == 1) {
            if (this.mOpenStep2ratio > 0.08f) {
                this.mOpenStep2ratio -= 0.08f;
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
        this.mLCDWidth = ((float) width) / 2.0f;
        this.mLCDHeigh = ((float) height) / 2.0f;
    }
}
