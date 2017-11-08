package com.huawei.keyguard.view.charge.e50;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.RectF;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.view.WindowManager;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.KeyguardCfg;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

@SuppressLint({"FloatMath"})
public class UCRenderer implements Renderer {
    private float ANGLE_CHANGE_PER_FRAME_1 = -0.021551324f;
    private float ANGLE_CHANGE_PER_FRAME_2 = -0.01825265f;
    private float ANGLE_CHANGE_PER_FRAME_3 = -0.026200881f;
    private int MAX_BUBBLES_NUMBER = 29;
    private float colorAChange1D;
    private float colorAChange1O;
    private float colorAChange2D;
    private float colorAChange2O;
    private float colorAChange3D;
    private float colorAChange3O;
    private float colorAChange4D;
    private float colorAChange4O;
    private float colorAChangeBgD;
    private float colorAChangeBgO;
    private Bitmap[] mBitmap = new Bitmap[this.mImageNum];
    private long mBubbleCreateSpeed = 350;
    private float[] mBubbleMoveSpeed = new float[this.MAX_BUBBLES_NUMBER];
    private float[] mBubbleRotateCurrentAngle = new float[this.MAX_BUBBLES_NUMBER];
    private float[] mBubbleRotateRadius = new float[this.MAX_BUBBLES_NUMBER];
    private int[] mBubbleRotateTimes = new int[this.MAX_BUBBLES_NUMBER];
    private float[] mBubblesRadius = new float[this.MAX_BUBBLES_NUMBER];
    private BubbleStatus[] mBubblesStatus = new BubbleStatus[this.MAX_BUBBLES_NUMBER];
    private float[] mBubblesX = new float[this.MAX_BUBBLES_NUMBER];
    private float[] mBubblesY = new float[this.MAX_BUBBLES_NUMBER];
    private Context mContext;
    private int mCurrentReceiverChangeTimes = 1;
    private float mCurrentReceiverRadius;
    private int mDesTimes = 0;
    private int[] mFuseType = new int[this.MAX_BUBBLES_NUMBER];
    private int[] mImageFileIDs = new int[]{R$drawable.ic_number0, R$drawable.ic_number1, R$drawable.ic_number2, R$drawable.ic_number3, R$drawable.ic_number4, R$drawable.ic_number5, R$drawable.ic_number6, R$drawable.ic_number7, R$drawable.ic_number8, R$drawable.ic_number9, R$drawable.ic_charge_standard, R$drawable.ic_charge_quick, R$drawable.ic_charge_super, R$drawable.ic_charge_super_v, R$drawable.ic_percent};
    private int mImageNum = 15;
    private boolean mIsDesapreaEffectBegin = false;
    private long mLastBubbleTime = 0;
    private int mMaxBubblesRadius = 80;
    private float[] mMaxBubblesY = new float[this.MAX_BUBBLES_NUMBER];
    private float[] mMidCosValue = new float[241];
    private float[] mMidSinValue = new float[241];
    private int mMinBubblesRadius = 19;
    private int mOpenTimes = 0;
    private int mPower;
    private int mPowerIconNumber = 0;
    private int[] mPowerIconNumberIds = new int[3];
    private float[] mReceiberAngleChangePerFrame = new float[3];
    private float[] mReceiverCenterX = new float[3];
    private float[] mReceiverCenterY = new float[3];
    private float[] mReceiverCurrentAngle = new float[3];
    private float[] mReceiverCurrentOffset = new float[3];
    private float mReceiverEmptyRadius;
    private int[] mReceiverIdx = new int[this.MAX_BUBBLES_NUMBER];
    private float[] mReceiverRadius = new float[3];
    private int mRotation = -1;
    private int mScene;
    UCColors mSceneLockColor1 = new UCColors(80, 80);
    UCColors mSceneLockColor2 = new UCColors(80, 80);
    UCColors mSceneLockColor3 = new UCColors(80, 80);
    UCColors mSceneLockColor4 = new UCColors(80, 80);
    UCColors mSceneLockColorBg = new UCColors(80, 80);
    private int mStatus = 0;
    private float[] mTempPointer1 = new float[2];
    private float[] mTempPointer2 = new float[2];
    private int[] mTextureIDs = new int[this.mImageNum];
    private long mTime = 0;
    private FloatBuffer mVertexBuffer;
    private FloatBuffer mVertexTextureBuffer;
    private float[] mVerticFuseDown = new float[80];
    private float[] mVerticFuseTop = new float[80];
    private float[] mVertics = new float[480];

    public enum BubbleStatus {
        ON,
        OFF
    }

    private static class Circle {
        float[] center;
        float radius;

        private Circle() {
        }
    }

    public UCRenderer(Context context) {
        this.mContext = context;
    }

    public float degToRad(float deg) {
        return (3.1415927f * deg) / 240.0f;
    }

    private void inital_MaxBubbleNum() {
        this.mBubblesRadius = new float[this.MAX_BUBBLES_NUMBER];
        this.mBubblesX = new float[this.MAX_BUBBLES_NUMBER];
        this.mBubblesY = new float[this.MAX_BUBBLES_NUMBER];
        this.mFuseType = new int[this.MAX_BUBBLES_NUMBER];
        this.mReceiverIdx = new int[this.MAX_BUBBLES_NUMBER];
        this.mBubblesStatus = new BubbleStatus[this.MAX_BUBBLES_NUMBER];
        this.mBubbleMoveSpeed = new float[this.MAX_BUBBLES_NUMBER];
        this.mBubbleRotateRadius = new float[this.MAX_BUBBLES_NUMBER];
        this.mBubbleRotateCurrentAngle = new float[this.MAX_BUBBLES_NUMBER];
        this.mMaxBubblesY = new float[this.MAX_BUBBLES_NUMBER];
        this.mBubbleRotateTimes = new int[this.MAX_BUBBLES_NUMBER];
    }

    public void start(int power, int scene) {
        if (power >= 100) {
            this.mPower = 100;
        } else if (power <= 0) {
            this.mPower = 0;
        } else {
            this.mPower = power;
        }
        if (this.mPower == 100) {
            this.mPowerIconNumber = 3;
            this.mPowerIconNumberIds[0] = 1;
            this.mPowerIconNumberIds[1] = 0;
            this.mPowerIconNumberIds[2] = 0;
        } else if (this.mPower < 10) {
            this.mPowerIconNumber = 1;
            this.mPowerIconNumberIds[0] = this.mPower;
        } else {
            this.mPowerIconNumber = 2;
            this.mPowerIconNumberIds[0] = this.mPower / 10;
            this.mPowerIconNumberIds[1] = this.mPower % 10;
        }
        this.mScene = scene;
        this.mMaxBubblesRadius = 80;
        this.mMinBubblesRadius = 19;
        this.mBubbleCreateSpeed = 350;
        this.ANGLE_CHANGE_PER_FRAME_1 = -0.021551324f;
        this.ANGLE_CHANGE_PER_FRAME_2 = -0.01825265f;
        this.ANGLE_CHANGE_PER_FRAME_3 = -0.026200881f;
        if (scene == 1) {
            this.MAX_BUBBLES_NUMBER = 27;
            this.mBubbleCreateSpeed = 160;
            this.mMaxBubblesRadius = 65;
            this.mMinBubblesRadius = 29;
            this.ANGLE_CHANGE_PER_FRAME_1 = -0.05296725f;
            this.ANGLE_CHANGE_PER_FRAME_2 = -0.04966858f;
            this.ANGLE_CHANGE_PER_FRAME_3 = -0.057616808f;
        } else if (scene == 2) {
            this.MAX_BUBBLES_NUMBER = 29;
            this.mBubbleCreateSpeed = 250;
            this.mMaxBubblesRadius = 85;
            this.mMinBubblesRadius = 40;
        }
        inital_MaxBubbleNum();
        if (power > 20) {
            this.mSceneLockColor3.setColor(-16733369);
            this.mSceneLockColor2.setColor(-16722343);
            this.mSceneLockColor1.setColor(-1727998375);
        } else if (power > 10) {
            this.mSceneLockColor3.setColor(-3780581);
            this.mSceneLockColor2.setColor(-301790);
            this.mSceneLockColor1.setColor(-1711577822);
        } else {
            this.mSceneLockColor3.setColor(-3397632);
            this.mSceneLockColor2.setColor(-52448);
            this.mSceneLockColor1.setColor(-1714673664);
        }
        this.mSceneLockColor4.setColor(1.0f, 1.0f, 1.0f, 0.8f);
        this.mSceneLockColorBg.setColor(0.0f, 0.0f, 0.0f, 0.2f);
        this.colorAChange1D = this.mSceneLockColor1.getA() / 80.0f;
        this.colorAChange2D = this.mSceneLockColor2.getA() / 80.0f;
        this.colorAChange3D = this.mSceneLockColor3.getA() / 80.0f;
        this.colorAChange4D = this.mSceneLockColor4.getA() / 80.0f;
        this.colorAChangeBgD = this.mSceneLockColorBg.getA() / 80.0f;
        this.colorAChange1O = this.mSceneLockColor1.getA() / 80.0f;
        this.colorAChange2O = this.mSceneLockColor2.getA() / 80.0f;
        this.colorAChange3O = this.mSceneLockColor3.getA() / 80.0f;
        this.colorAChange4O = this.mSceneLockColor4.getA() / 80.0f;
        this.colorAChangeBgO = this.mSceneLockColorBg.getA() / 80.0f;
        this.mSceneLockColor1.setA(0.0f);
        this.mSceneLockColor2.setA(0.0f);
        this.mSceneLockColor3.setA(0.0f);
        this.mSceneLockColor4.setA(0.0f);
        this.mSceneLockColorBg.setA(0.0f);
        this.mDesTimes = 0;
        this.mOpenTimes = 0;
        this.mIsDesapreaEffectBegin = false;
        resetParas();
        this.mStatus = 1;
    }

    public void loadTexture(GL10 gl, Context context) {
        gl.glGenTextures(this.mImageNum, this.mTextureIDs, 0);
        int idx = 0;
        while (idx < this.mImageNum) {
            gl.glBindTexture(3553, this.mTextureIDs[idx]);
            InputStream istream = context.getResources().openRawResource(this.mImageFileIDs[idx]);
            try {
                this.mBitmap[idx] = BitmapFactory.decodeStream(istream);
                gl.glTexParameterf(3553, 10241, 9729.0f);
                gl.glTexParameterf(3553, 10240, 9729.0f);
                GLUtils.texImage2D(3553, 0, this.mBitmap[idx], 0);
                this.mBitmap[idx].recycle();
                idx++;
            } finally {
                try {
                    istream.close();
                } catch (IOException e) {
                }
            }
        }
    }

    public void stop() {
        this.mIsDesapreaEffectBegin = true;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        gl.glShadeModel(7425);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        gl.glHint(3152, 4353);
        gl.glEnableClientState(32884);
        gl.glEnable(3553);
        loadTexture(gl, this.mContext);
        iniParas();
    }

    @SuppressLint({"FloatMath"})
    public void iniParas() {
        this.mVertexBuffer = ByteBuffer.allocateDirect(1920).order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.mVertexTextureBuffer = ByteBuffer.allocateDirect(32).order(ByteOrder.nativeOrder()).asFloatBuffer();
        for (int i = 0; i < 240; i++) {
            this.mMidSinValue[i] = ((float) Math.sin((double) degToRad((float) (i * 2)))) * 0.5960265f;
            this.mMidCosValue[i] = (float) Math.cos((double) degToRad((float) (i * 2)));
        }
        resetParas();
    }

    public void resetParas() {
        int i;
        this.mCurrentReceiverRadius = ((((float) this.mPower) * 0.185f) / 100.0f) + 0.463f;
        this.mReceiverCurrentAngle[0] = 2.8274333f;
        this.mReceiverCurrentAngle[1] = -0.9424779f;
        this.mReceiverCurrentAngle[2] = -2.1991148f;
        for (i = 0; i < 3; i++) {
            this.mReceiverCurrentOffset[i] = 0.0f;
            this.mReceiverCenterX[i] = 0.0f;
            this.mReceiverCenterY[i] = 0.0f;
        }
        this.mReceiverRadius[0] = this.mCurrentReceiverRadius;
        this.mReceiverRadius[1] = this.mCurrentReceiverRadius + 0.005f;
        this.mReceiverRadius[2] = this.mCurrentReceiverRadius - 0.02f;
        this.mReceiberAngleChangePerFrame[0] = this.ANGLE_CHANGE_PER_FRAME_1;
        this.mReceiberAngleChangePerFrame[1] = this.ANGLE_CHANGE_PER_FRAME_2;
        this.mReceiberAngleChangePerFrame[2] = this.ANGLE_CHANGE_PER_FRAME_3;
        this.mReceiverEmptyRadius = this.mCurrentReceiverRadius - 0.01f;
        for (i = 0; i < this.MAX_BUBBLES_NUMBER; i++) {
            this.mBubblesStatus[i] = BubbleStatus.OFF;
            this.mBubblesX[i] = 0.0f;
            this.mBubblesY[i] = -1.0f;
        }
        this.mCurrentReceiverChangeTimes = 0;
        this.mLastBubbleTime = 0;
        createBeginBubbles();
        if (this.mVertexTextureBuffer == null) {
            stop();
            return;
        }
        this.mVertexTextureBuffer.put(0, 0.0f);
        this.mVertexTextureBuffer.put(1, 1.0f);
        this.mVertexTextureBuffer.put(2, 1.0f);
        this.mVertexTextureBuffer.put(3, 1.0f);
        this.mVertexTextureBuffer.put(4, 0.0f);
        this.mVertexTextureBuffer.put(5, 0.0f);
        this.mVertexTextureBuffer.put(6, 1.0f);
        this.mVertexTextureBuffer.put(7, 0.0f);
    }

    public void refreshEmitter(int idx) {
        boolean isDefaultPortOrientation = KeyguardCfg.isDefaultPortOrientation();
        for (int i = 0; i < 240; i++) {
            this.mTempPointer1[0] = ((this.mMidCosValue[i] * 0.21f) + 0.0f) + 0.0f;
            this.mTempPointer1[1] = (this.mMidSinValue[i] * 0.21f) - 3.86f;
            if (!isDefaultPortOrientation) {
                transpose(this.mTempPointer1);
            }
            this.mVertexBuffer.put(i * 2, this.mTempPointer1[0]);
            this.mVertexBuffer.put((i * 2) + 1, this.mTempPointer1[1]);
        }
    }

    public void refreshReceiverBase() {
        this.mReceiverCurrentOffset[0] = (((float) this.mCurrentReceiverChangeTimes) * 0.04999f) / 200.0f;
        this.mReceiverCurrentOffset[1] = (((float) this.mCurrentReceiverChangeTimes) * 0.0288f) / 200.0f;
        this.mReceiverCurrentOffset[2] = (((float) this.mCurrentReceiverChangeTimes) * 0.0358f) / 200.0f;
        if (this.mCurrentReceiverChangeTimes <= 200) {
            this.mTime = System.currentTimeMillis();
            this.mCurrentReceiverChangeTimes++;
        }
    }

    public void refreshReceiver(int idx) {
        if (idx >= 0 && idx < 3) {
            float angle;
            this.mReceiverCenterX[idx] = (float) (((double) this.mReceiverCurrentOffset[idx]) * Math.cos((double) this.mReceiverCurrentAngle[idx]));
            this.mReceiverCenterY[idx] = (float) (((double) this.mReceiverCurrentOffset[idx]) * Math.sin((double) this.mReceiverCurrentAngle[idx]));
            boolean isDefaultPortOrientation = KeyguardCfg.isDefaultPortOrientation();
            for (int i = 0; i < 240; i++) {
                this.mTempPointer1[0] = this.mReceiverCenterX[idx] + (this.mReceiverRadius[idx] * this.mMidCosValue[i]);
                this.mTempPointer1[1] = this.mReceiverCenterY[idx] + (this.mReceiverRadius[idx] * this.mMidSinValue[i]);
                if (!isDefaultPortOrientation) {
                    transpose(this.mTempPointer1);
                }
                this.mVertexBuffer.put(i * 2, this.mTempPointer1[0]);
                this.mVertexBuffer.put((i * 2) + 1, this.mTempPointer1[1]);
            }
            if (idx == 0) {
                angle = this.ANGLE_CHANGE_PER_FRAME_1;
            } else if (idx == 1) {
                angle = this.ANGLE_CHANGE_PER_FRAME_2;
            } else {
                angle = this.ANGLE_CHANGE_PER_FRAME_3;
            }
            float[] fArr = this.mReceiverCurrentAngle;
            fArr[idx] = fArr[idx] + angle;
            if (this.mReceiverCurrentAngle[idx] >= 6.2831855f) {
                fArr = this.mReceiverCurrentAngle;
                fArr[idx] = fArr[idx] - 6.2831855f;
            }
        }
    }

    public void refreshReceiverCircel() {
        boolean isDefaultPortOrientation = KeyguardCfg.isDefaultPortOrientation();
        for (int i = 0; i < 240; i++) {
            this.mTempPointer1[0] = this.mReceiverEmptyRadius * this.mMidCosValue[i];
            this.mTempPointer1[1] = this.mReceiverEmptyRadius * this.mMidSinValue[i];
            if (!isDefaultPortOrientation) {
                transpose(this.mTempPointer1);
            }
            this.mVertexBuffer.put(i * 2, this.mTempPointer1[0]);
            this.mVertexBuffer.put((i * 2) + 1, this.mTempPointer1[1]);
        }
    }

    public void createBubbles() {
        if (System.currentTimeMillis() - this.mLastBubbleTime >= this.mBubbleCreateSpeed) {
            for (int i = 0; i < this.MAX_BUBBLES_NUMBER; i++) {
                if (this.mBubblesStatus[i] == BubbleStatus.OFF) {
                    createOneBubble(i);
                    return;
                }
            }
        }
    }

    public float createBubllesRadius() {
        return (float) ((((double) this.mMinBubblesRadius) + (((double) (this.mMaxBubblesRadius - this.mMinBubblesRadius)) * Math.random())) * 0.0010000000474974513d);
    }

    public void createBeginBubbles() {
        int bubbleNum;
        if (this.mScene != 0) {
            bubbleNum = 3;
        } else {
            bubbleNum = 2;
        }
        for (int i = 0; i < bubbleNum; i++) {
            this.mBubblesRadius[i] = createBubllesRadius();
            this.mBubblesStatus[i] = BubbleStatus.ON;
            this.mBubblesX[i] = (float) (((Math.random() * 20.0d) - 10.0d) * 0.009999999776482582d);
            this.mBubblesY[i] = (((float) i) * 0.1f) - 4.8f;
            this.mMaxBubblesY[i] = (this.mReceiverEmptyRadius * this.mMidSinValue[180]) - (this.mBubblesRadius[i] / 4.0f);
            this.mBubbleRotateRadius[i] = (float) Math.sqrt((double) ((this.mBubblesX[i] * this.mBubblesX[i]) + ((this.mMaxBubblesY[i] / 0.5960265f) * (this.mMaxBubblesY[i] / 0.5960265f))));
            this.mBubbleRotateCurrentAngle[i] = (float) Math.atan((double) Math.abs(this.mBubblesX[i] / this.mBubblesY[i]));
            this.mBubbleRotateTimes[i] = 1;
            this.mFuseType[i] = 0;
            setBubblesMoveSpeed(i);
            setBubblesReceiverIdx(i);
        }
    }

    public void createOneBubble(int idx) {
        this.mBubblesRadius[idx] = createBubllesRadius();
        this.mBubblesStatus[idx] = BubbleStatus.ON;
        this.mBubblesX[idx] = (float) (((Math.random() * 20.0d) - 10.0d) * 0.009999999776482582d);
        this.mBubblesY[idx] = -1.07f;
        this.mMaxBubblesY[idx] = (this.mReceiverEmptyRadius * this.mMidSinValue[180]) - (this.mBubblesRadius[idx] / 4.0f);
        this.mBubbleRotateRadius[idx] = (float) Math.sqrt((double) ((this.mBubblesX[idx] * this.mBubblesX[idx]) + ((this.mMaxBubblesY[idx] / 0.5960265f) * (this.mMaxBubblesY[idx] / 0.5960265f))));
        this.mBubbleRotateCurrentAngle[idx] = (float) Math.atan((double) Math.abs(this.mBubblesX[idx] / this.mBubblesY[idx]));
        this.mBubbleRotateTimes[idx] = 1;
        this.mFuseType[idx] = 0;
        setBubblesMoveSpeed(idx);
        setBubblesReceiverIdx(idx);
        this.mLastBubbleTime = System.currentTimeMillis();
    }

    public void setBubblesMoveSpeed(int idx) {
        if (this.mScene == 1) {
            this.mBubbleMoveSpeed[idx] = (float) (((Math.random() * 3.0d) * 0.0010000000474974513d) + 0.00800000037997961d);
        } else if (this.mScene == 2) {
            this.mBubbleMoveSpeed[idx] = (float) (((Math.random() * 3.0d) * 0.0010000000474974513d) + 0.006599999964237213d);
        } else {
            this.mBubbleMoveSpeed[idx] = (float) (((Math.random() * 3.0d) * 0.0010000000474974513d) + 0.006300000008195639d);
        }
    }

    public void setBubblesReceiverIdx(int idx) {
        float v = (float) Math.random();
        if (v < 0.2f) {
            this.mReceiverIdx[idx] = 0;
        } else if (v < 0.4f) {
            this.mReceiverIdx[idx] = 1;
        } else {
            this.mReceiverIdx[idx] = 2;
        }
    }

    public int resfreshBubbles(int idx) {
        if (this.mBubblesStatus[idx] == BubbleStatus.OFF) {
            return 0;
        }
        boolean isDefaultPortOrientation = KeyguardCfg.isDefaultPortOrientation();
        for (int i = 0; i < 240; i++) {
            this.mVertics[i * 2] = (this.mBubblesRadius[idx] * this.mMidCosValue[i]) + this.mBubblesX[idx];
            this.mVertics[(i * 2) + 1] = (this.mBubblesRadius[idx] * this.mMidSinValue[i]) + this.mBubblesY[idx];
            this.mTempPointer1[0] = (this.mBubblesRadius[idx] * this.mMidCosValue[i]) + this.mBubblesX[idx];
            this.mTempPointer1[1] = (this.mBubblesRadius[idx] * this.mMidSinValue[i]) + this.mBubblesY[idx];
            if (!isDefaultPortOrientation) {
                transpose(this.mTempPointer1);
            }
            this.mVertexBuffer.put(i * 2, this.mTempPointer1[0]);
            this.mVertexBuffer.put((i * 2) + 1, this.mTempPointer1[1]);
        }
        return 1;
    }

    public void resfreshBubblesPostion(int idx) {
        float[] fArr;
        if (this.mBubblesY[idx] >= this.mMaxBubblesY[idx]) {
            if (this.mFuseType[idx] == 0) {
                float a1 = Math.abs(this.mReceiverCurrentAngle[1]) % 3.1415927f;
                if (((double) a1) < 0.6283185482025146d || ((double) a1) > 2.5132741928100586d) {
                    this.mFuseType[idx] = 1;
                } else {
                    this.mFuseType[idx] = 2;
                }
            }
            if (this.mFuseType[idx] == 2) {
                fArr = this.mBubblesY;
                fArr[idx] = fArr[idx] + this.mBubbleMoveSpeed[idx];
                if (this.mBubblesY[idx] >= this.mMaxBubblesY[idx] + 0.1f) {
                    this.mBubblesStatus[idx] = BubbleStatus.OFF;
                }
                return;
            }
            float angle;
            if (this.mBubbleRotateTimes[idx] <= 20) {
                angle = this.mBubbleRotateCurrentAngle[idx] + (((float) this.mBubbleRotateTimes[idx]) * 0.015707964f);
            } else {
                angle = (this.mBubbleRotateCurrentAngle[idx] + (((float) (this.mBubbleRotateTimes[idx] - 20)) * 0.0837758f)) + 0.31415927f;
            }
            float radius = this.mBubbleRotateRadius[idx] - (((float) this.mBubbleRotateTimes[idx]) * ((this.mBubblesRadius[idx] * 5.0f) / 140.0f));
            if (this.mBubblesX[idx] <= 0.0f) {
                this.mBubblesX[idx] = Math.min((float) (((double) (-radius)) * Math.sin((double) angle)), this.mBubblesX[idx]);
            } else {
                this.mBubblesX[idx] = Math.max((float) (((double) radius) * Math.sin((double) angle)), this.mBubblesX[idx]);
            }
            this.mBubblesY[idx] = Math.max((float) ((((double) (-radius)) * Math.cos((double) angle)) * 0.5960264801979065d), this.mBubblesY[idx]);
            int[] iArr = this.mBubbleRotateTimes;
            iArr[idx] = iArr[idx] + 1;
            if (this.mBubbleRotateTimes[idx] >= 35) {
                this.mBubblesStatus[idx] = BubbleStatus.OFF;
            }
        } else {
            fArr = this.mBubblesY;
            fArr[idx] = fArr[idx] + this.mBubbleMoveSpeed[idx];
        }
    }

    public int refreshFuseBubbleAndEmitter(int idx) {
        if (this.mBubblesStatus[idx] == BubbleStatus.OFF) {
            return 0;
        }
        float thef;
        Circle circle1 = new Circle();
        circle1.center = new float[]{0.0f, -1.07f};
        circle1.radius = 0.21f;
        Circle circle2 = new Circle();
        circle2.center = new float[]{this.mBubblesX[idx], this.mBubblesY[idx]};
        circle2.radius = this.mBubblesRadius[idx];
        float dist = (float) Math.sqrt((double) (((0.0f - this.mBubblesX[idx]) * (0.0f - this.mBubblesX[idx])) + ((-1.07f - this.mBubblesY[idx]) * (-1.07f - this.mBubblesY[idx]))));
        float maxd = (circle1.radius + circle2.radius) + 0.01f;
        float mind = circle2.radius;
        if (dist <= mind) {
            thef = 0.7f;
        } else {
            thef = ((dist * 0.7f) / (mind - maxd)) + ((maxd * 0.7f) / (maxd - mind));
        }
        metaball(circle1, circle2, thef, 20.0f, maxd + 0.01f);
        return 1;
    }

    public int refreshFuseBubbles(int bubbleID1, int bubbleID2) {
        Circle circle1 = new Circle();
        circle1.center = new float[]{this.mBubblesX[bubbleID1], this.mBubblesY[bubbleID1]};
        circle1.radius = this.mBubblesRadius[bubbleID1];
        Circle circle2 = new Circle();
        circle2.center = new float[]{this.mBubblesX[bubbleID2], this.mBubblesY[bubbleID2]};
        circle2.radius = this.mBubblesRadius[bubbleID2];
        metaball(circle1, circle2, 0.7f, 1.3f, (circle1.radius + circle2.radius) + 0.021f);
        return 1;
    }

    public void prepareMetaBallDraw() {
        boolean isDefaultPortOrientation = KeyguardCfg.isDefaultPortOrientation();
        for (int j = 0; j < 40; j++) {
            this.mTempPointer1[0] = this.mVerticFuseTop[j * 2];
            this.mTempPointer1[1] = this.mVerticFuseTop[(j * 2) + 1];
            this.mTempPointer2[0] = this.mVerticFuseDown[j * 2];
            this.mTempPointer2[1] = this.mVerticFuseDown[(j * 2) + 1];
            if (!isDefaultPortOrientation) {
                transpose(this.mTempPointer1);
                transpose(this.mTempPointer2);
            }
            this.mVertexBuffer.put(j * 4, this.mTempPointer1[0]);
            this.mVertexBuffer.put((j * 4) + 1, this.mTempPointer1[1]);
            this.mVertexBuffer.put((j * 4) + 2, this.mTempPointer2[0]);
            this.mVertexBuffer.put((j * 4) + 3, this.mTempPointer2[1]);
        }
    }

    public int refreshFuseBubbleAndReceiver(int bubbleIdx, int receiverIdx) {
        if (this.mBubblesStatus[bubbleIdx] == BubbleStatus.OFF) {
            return 0;
        }
        Circle circle1 = new Circle();
        circle1.center = new float[]{this.mReceiverCenterX[receiverIdx], this.mReceiverCenterY[receiverIdx]};
        circle1.radius = this.mReceiverRadius[receiverIdx];
        Circle circle2 = new Circle();
        circle2.center = new float[]{this.mBubblesX[bubbleIdx], this.mBubblesY[bubbleIdx]};
        circle2.radius = this.mBubblesRadius[bubbleIdx];
        metaball(circle1, circle2, 0.5f, 10.0f, (circle1.radius + circle2.radius) + 0.09f);
        return 1;
    }

    private float[] getVector(float radians, float length) {
        float x = (float) (Math.cos((double) radians) * ((double) length));
        float y = 0.5960265f * ((float) (Math.sin((double) radians) * ((double) length)));
        return new float[]{x, y};
    }

    private float getDistance(float[] b1, float[] b2) {
        float x = b1[0] - b2[0];
        float y = (b1[1] - b2[1]) / 0.5960265f;
        return (float) Math.sqrt((double) ((x * x) + (y * y)));
    }

    private void metaball(Circle circle1, Circle circle2, float v, float handle_len_rate, float maxDistance) {
        RectF ball1 = new RectF();
        ball1.left = circle1.center[0] - circle1.radius;
        ball1.top = circle1.center[1] - circle1.radius;
        ball1.right = ball1.left + (circle1.radius * 2.0f);
        ball1.bottom = ball1.top + (circle1.radius * 2.0f);
        RectF ball2 = new RectF();
        ball2.left = circle2.center[0] - circle2.radius;
        ball2.top = circle2.center[1] - circle2.radius;
        ball2.right = ball2.left + (circle2.radius * 2.0f);
        ball2.bottom = ball2.top + (circle2.radius * 2.0f);
        float d = getDistance(new float[]{ball1.centerX(), ball1.centerY()}, new float[]{ball2.centerX(), ball2.centerY()});
        float radius1 = ball1.width() / 2.0f;
        float radius2 = ball2.width() / 2.0f;
        int i;
        if (radius1 == 0.0f || radius2 == 0.0f) {
            for (i = 0; i < 40; i++) {
                this.mVerticFuseTop[i * 2] = 0.0f;
                this.mVerticFuseTop[(i * 2) + 1] = 0.0f;
                this.mVerticFuseDown[i * 2] = 0.0f;
                this.mVerticFuseDown[(i * 2) + 1] = 0.0f;
            }
        } else if (d > maxDistance || d <= Math.abs(radius1 - radius2)) {
            for (i = 0; i < 40; i++) {
                this.mVerticFuseTop[i * 2] = 0.0f;
                this.mVerticFuseTop[(i * 2) + 1] = 0.0f;
                this.mVerticFuseDown[i * 2] = 0.0f;
                this.mVerticFuseDown[(i * 2) + 1] = 0.0f;
            }
        } else {
            float u1;
            float u2;
            float t;
            float a1;
            float a2;
            float a3;
            float a4;
            float fuseTmpPointY;
            if (d < radius1 + radius2) {
                u1 = (float) Math.acos((double) ((((radius1 * radius1) + (d * d)) - (radius2 * radius2)) / ((2.0f * radius1) * d)));
                u2 = (float) Math.acos((double) ((((radius2 * radius2) + (d * d)) - (radius1 * radius1)) / ((2.0f * radius2) * d)));
            } else {
                u1 = 0.0f;
                u2 = 0.0f;
            }
            float[] centermin = new float[]{center2[0] - center1[0], center2[1] - center1[1]};
            float angle1 = (float) Math.atan2((double) (centermin[1] / 0.5960265f), (double) centermin[0]);
            float angle2 = (float) Math.acos((double) ((radius1 - radius2) / d));
            float angle1a = (angle1 + u1) + ((angle2 - u1) * v);
            float angle1b = (angle1 - u1) - ((angle2 - u1) * v);
            float angle2a = (float) (((((double) angle1) + 3.141592653589793d) - ((double) u2)) - (((3.141592653589793d - ((double) u2)) - ((double) angle2)) * ((double) v)));
            float angle2b = (float) (((((double) angle1) - 3.141592653589793d) + ((double) u2)) + (((3.141592653589793d - ((double) u2)) - ((double) angle2)) * ((double) v)));
            float[] p1a1 = getVector(angle1a, radius1);
            float[] p1b1 = getVector(angle1b, radius1);
            float[] p2a1 = getVector(angle2a, radius2);
            float[] p2b1 = getVector(angle2b, radius2);
            float[] p1a = new float[]{p1a1[0] + center1[0], p1a1[1] + center1[1]};
            float[] p1b = new float[]{p1b1[0] + center1[0], p1b1[1] + center1[1]};
            float[] p2a = new float[]{p2a1[0] + center2[0], p2a1[1] + center2[1]};
            float[] p2b = new float[]{p2b1[0] + center2[0], p2b1[1] + center2[1]};
            float[] p1_p2 = new float[]{p1a[0] - p2a[0], p1a[1] - p2a[1]};
            float d2 = Math.min(v * handle_len_rate, ((float) Math.sqrt((double) ((p1_p2[0] * p1_p2[0]) + ((p1_p2[1] / 0.5960265f) * (p1_p2[1] / 0.5960265f))))) / (radius1 + radius2)) * Math.min(1.0f, (2.0f * d) / (radius1 + radius2));
            radius1 *= d2;
            radius2 *= d2;
            float[] sp1 = getVector(angle1a - 1.5707964f, radius1);
            float[] sp2 = getVector(1.5707964f + angle2a, radius2);
            float[] sp3 = getVector(angle2b - 1.5707964f, radius2);
            float[] sp4 = getVector(1.5707964f + angle1b, radius1);
            float[] p3a = new float[]{p1a[0] + sp1[0], p1a[1] + sp1[1]};
            float[] p4a = new float[]{p2a[0] + sp2[0], p2a[1] + sp2[1]};
            float[] p3b = new float[]{p2b[0] + sp3[0], p2b[1] + sp3[1]};
            float[] p4b = new float[]{p1b[0] + sp4[0], p1b[1] + sp4[1]};
            float fuseXPoint1 = p1a[0];
            float fuseYPoint1 = p1a[1];
            float fuseXPoint2 = p3a[0];
            float fuseYPoint2 = p3a[1];
            float fuseXPoint3 = p4a[0];
            float fuseYPoint3 = p4a[1];
            float fuseXPoint4 = p2a[0];
            float fuseYPoint4 = p2a[1];
            for (i = 0; i < 40; i++) {
                t = 0.025641026f * ((float) i);
                a1 = (float) Math.pow((double) (1.0f - t), 3.0d);
                a2 = (((float) Math.pow((double) (1.0f - t), 2.0d)) * 3.0f) * t;
                a3 = ((3.0f * t) * t) * (1.0f - t);
                a4 = (t * t) * t;
                fuseTmpPointY = (((a1 * fuseYPoint1) + (a2 * fuseYPoint2)) + (a3 * fuseYPoint3)) + (a4 * fuseYPoint4);
                this.mVerticFuseTop[i * 2] = (((a1 * fuseXPoint1) + (a2 * fuseXPoint2)) + (a3 * fuseXPoint3)) + (a4 * fuseXPoint4);
                this.mVerticFuseTop[(i * 2) + 1] = fuseTmpPointY;
            }
            fuseXPoint4 = p2b[0];
            fuseYPoint4 = p2b[1];
            fuseXPoint3 = p3b[0];
            fuseYPoint3 = p3b[1];
            fuseXPoint2 = p4b[0];
            fuseYPoint2 = p4b[1];
            fuseXPoint1 = p1b[0];
            fuseYPoint1 = p1b[1];
            for (i = 0; i < 40; i++) {
                t = 0.025641026f * ((float) i);
                a1 = (float) Math.pow((double) (1.0f - t), 3.0d);
                a2 = (((float) Math.pow((double) (1.0f - t), 2.0d)) * 3.0f) * t;
                a3 = ((3.0f * t) * t) * (1.0f - t);
                a4 = (t * t) * t;
                fuseTmpPointY = (((a1 * fuseYPoint1) + (a2 * fuseYPoint2)) + (a3 * fuseYPoint3)) + (a4 * fuseYPoint4);
                this.mVerticFuseDown[i * 2] = (((a1 * fuseXPoint1) + (a2 * fuseXPoint2)) + (a3 * fuseXPoint3)) + (a4 * fuseXPoint4);
                this.mVerticFuseDown[(i * 2) + 1] = fuseTmpPointY;
            }
        }
    }

    public void drawforSleepScene(GL10 gl) {
        if (this.mOpenTimes <= 80 && !this.mIsDesapreaEffectBegin) {
            this.mOpenTimes++;
            this.mSceneLockColor1.setColorOFlip();
            this.mSceneLockColor2.setColorOFlip();
            this.mSceneLockColor3.setColorOFlip();
            this.mSceneLockColor4.setColorOFlip();
            if (this.mOpenTimes > 80) {
                this.mSceneLockColor1.beginColorDflip();
                this.mSceneLockColor2.beginColorDflip();
                this.mSceneLockColor3.beginColorDflip();
                this.mSceneLockColor4.beginColorDflip();
            }
        }
        gl.glDisable(3553);
        gl.glClear(16640);
        gl.glLoadIdentity();
        gl.glDisable(3042);
        gl.glShadeModel(7425);
        gl.glEnableClientState(32884);
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        if (this.mStatus == 0) {
            gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
            gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
            gl.glDrawArrays(6, 0, 3);
            gl.glDisableClientState(32884);
            gl.glDisableClientState(32886);
            gl.glFinish();
            return;
        }
        refreshReceiverBase();
        refreshReceiver(0);
        gl.glColor4f(this.mSceneLockColor1.cr, this.mSceneLockColor1.cg, this.mSceneLockColor1.cb, this.mSceneLockColor1.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        int i = 0;
        while (i < this.MAX_BUBBLES_NUMBER) {
            if (this.mBubblesStatus[i] != BubbleStatus.OFF && this.mReceiverIdx[i] == 0 && 1 == refreshFuseBubbleAndReceiver(i, 0)) {
                prepareMetaBallDraw();
                gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                gl.glDrawArrays(5, 0, 80);
            }
            i++;
        }
        refreshReceiver(1);
        gl.glColor4f(this.mSceneLockColor2.cr, this.mSceneLockColor2.cg, this.mSceneLockColor2.cb, this.mSceneLockColor2.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        i = 0;
        while (i < this.MAX_BUBBLES_NUMBER) {
            if (this.mBubblesStatus[i] != BubbleStatus.OFF && this.mReceiverIdx[i] == 1 && 1 == refreshFuseBubbleAndReceiver(i, 1)) {
                prepareMetaBallDraw();
                gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                gl.glDrawArrays(5, 0, 80);
            }
            i++;
        }
        refreshReceiver(2);
        gl.glColor4f(this.mSceneLockColor3.cr, this.mSceneLockColor3.cg, this.mSceneLockColor3.cb, this.mSceneLockColor3.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        i = 0;
        while (i < this.MAX_BUBBLES_NUMBER) {
            if (this.mBubblesStatus[i] != BubbleStatus.OFF && this.mReceiverIdx[i] == 2 && 1 == refreshFuseBubbleAndReceiver(i, 2)) {
                prepareMetaBallDraw();
                gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                gl.glDrawArrays(5, 0, 80);
            }
            i++;
        }
        i = 0;
        while (i < this.MAX_BUBBLES_NUMBER) {
            if (this.mBubblesStatus[i] != BubbleStatus.OFF) {
                int j = i + 1;
                while (j < this.MAX_BUBBLES_NUMBER) {
                    if (this.mBubblesStatus[j] != BubbleStatus.OFF && this.mReceiverIdx[i] == this.mReceiverIdx[j]) {
                        refreshFuseBubbles(i, j);
                        prepareMetaBallDraw();
                        if (this.mReceiverIdx[i] == 0) {
                            gl.glColor4f(this.mSceneLockColor1.cr, this.mSceneLockColor1.cg, this.mSceneLockColor1.cb, this.mSceneLockColor1.a);
                        } else if (this.mReceiverIdx[i] == 1) {
                            gl.glColor4f(this.mSceneLockColor2.cr, this.mSceneLockColor2.cg, this.mSceneLockColor2.cb, this.mSceneLockColor2.a);
                        } else {
                            gl.glColor4f(this.mSceneLockColor3.cr, this.mSceneLockColor3.cg, this.mSceneLockColor3.cb, this.mSceneLockColor3.a);
                        }
                        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                        gl.glDrawArrays(5, 0, 80);
                    }
                    j++;
                }
            }
            i++;
        }
        createBubbles();
        for (i = 0; i < this.MAX_BUBBLES_NUMBER; i++) {
            if (1 == resfreshBubbles(i)) {
                if (this.mReceiverIdx[i] == 0) {
                    gl.glColor4f(this.mSceneLockColor1.cr, this.mSceneLockColor1.cg, this.mSceneLockColor1.cb, this.mSceneLockColor1.a);
                } else if (this.mReceiverIdx[i] == 1) {
                    gl.glColor4f(this.mSceneLockColor2.cr, this.mSceneLockColor2.cg, this.mSceneLockColor2.cb, this.mSceneLockColor2.a);
                } else {
                    gl.glColor4f(this.mSceneLockColor3.cr, this.mSceneLockColor3.cg, this.mSceneLockColor3.cb, this.mSceneLockColor3.a);
                }
                gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                gl.glDrawArrays(6, 0, 240);
                if (1 == refreshFuseBubbleAndEmitter(i)) {
                    prepareMetaBallDraw();
                    gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
                    gl.glDrawArrays(5, 0, 80);
                }
                resfreshBubblesPostion(i);
            }
        }
        refreshEmitter(0);
        gl.glColor4f(this.mSceneLockColor1.cr, this.mSceneLockColor1.cg, this.mSceneLockColor1.cb, this.mSceneLockColor1.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        refreshEmitter(1);
        gl.glColor4f(this.mSceneLockColor2.cr, this.mSceneLockColor2.cg, this.mSceneLockColor2.cb, this.mSceneLockColor2.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        refreshEmitter(2);
        gl.glColor4f(this.mSceneLockColor3.cr, this.mSceneLockColor3.cg, this.mSceneLockColor3.cb, this.mSceneLockColor3.a);
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        gl.glColor4f(0.0f, 0.0f, 0.0f, 0.0f);
        refreshReceiverCircel();
        gl.glVertexPointer(2, 5126, 0, this.mVertexBuffer);
        gl.glDrawArrays(6, 0, 240);
        if (System.currentTimeMillis() - this.mTime >= 9000 || this.mIsDesapreaEffectBegin) {
            this.mDesTimes++;
        }
        if (this.mDesTimes >= 1) {
            this.mSceneLockColor1.setColorDFlip();
            this.mSceneLockColor2.setColorDFlip();
            this.mSceneLockColor3.setColorDFlip();
            this.mSceneLockColor4.setColorDFlip();
            if (this.mDesTimes == 80) {
                this.mStatus = 0;
            }
        }
    }

    public void onDrawFrame(GL10 gl) {
        drawforSleepScene(gl);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mRotation = ((WindowManager) this.mContext.getSystemService("window")).getDefaultDisplay().getRotation();
    }

    private void transpose(float[] pointer) {
        if (this.mRotation == 0) {
            pointer[1] = -pointer[1];
        } else if (this.mRotation == 1) {
            temp = pointer[0];
            pointer[0] = pointer[1];
            pointer[1] = temp;
        } else if (this.mRotation == 3) {
            temp = pointer[0];
            pointer[0] = pointer[1];
            pointer[1] = temp;
            pointer[0] = -pointer[0];
        }
    }
}
