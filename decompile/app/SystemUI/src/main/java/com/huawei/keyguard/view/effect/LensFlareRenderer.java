package com.huawei.keyguard.view.effect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.GLSurfaceView.Renderer;
import android.opengl.GLUtils;
import android.opengl.Matrix;
import android.os.Handler;
import android.telephony.TelephonyManager;
import android.util.FloatMath;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import com.android.hwtransition.interpolator.CubicBezierInterpolator;
import com.android.keyguard.R$raw;
import com.huawei.keyguard.util.HwLog;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class LensFlareRenderer implements Renderer {
    private static final /* synthetic */ int[] -android-graphics-Bitmap$ConfigSwitchesValues = null;
    private static final float[] LENSFLARE_TOUCH_ALPHAS = new float[]{0.5f, 0.8f, 0.8f, 0.6f, 0.6f, 0.6f, 0.6f, 0.5f, 0.6f, 0.6f, 0.6f, 0.9f, 0.9f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f, 0.8f};
    private static final float[] LENSFLARE_TOUCH_COLORS = new float[]{0.894f, 0.459f, 0.114f, 0.051f, 0.651f, 0.02f, 0.894f, 0.388f, 0.114f, 0.224f, 0.71f, 0.012f, 0.224f, 0.71f, 0.012f, 0.224f, 0.71f, 0.012f, 0.224f, 0.71f, 0.012f, 0.224f, 0.71f, 0.012f, 0.914f, 0.51f, 0.216f, 0.914f, 0.51f, 0.216f, 0.914f, 0.51f, 0.216f, 0.914f, 0.51f, 0.216f, 0.914f, 0.51f, 0.216f, 0.71f, 0.518f, 0.106f, 0.224f, 0.71f, 0.071f, 0.549f, 0.71f, 0.094f, 0.835f, 0.404f, 0.102f, 0.224f, 0.71f, 0.071f, 0.224f, 0.71f, 0.071f};
    private static final float[] LENSFLARE_TOUCH_POSITOINS = new float[]{0.262f, 0.224f, 0.155f, 0.17f, 0.234f, 0.684f, 0.74f, 0.876f, 0.205f, 0.237f, 0.244f, 0.335f, 0.385f, 0.887f, 0.887f, 0.956f, 1.0f, 0.955f, 0.98f};
    private static final float[] LENSFLARE_TOUCH_SCALES = new float[]{0.503f, 0.271f, 0.347f, 0.101f, 0.071f, 0.08f, 0.101f, 0.08f, 0.02f, 0.015f, 0.025f, 0.015f, 0.025f, 0.352f, 0.063f, 0.142f, 0.196f, 0.034f, 0.034f};
    private static final float[] RAINBOW_ALPHAS = new float[]{0.8f, 0.25f, 2.0f, 3.0f, 1.0f};
    private static final float[] RAINBOW_POSITOINS = new float[]{0.761f, 0.761f, 0.646f, 0.88f, 0.69f};
    private static final float[] RAINBOW_SCALES = new float[]{0.725f, 0.725f, 0.53f, 0.544f, 0.118f};
    private static final float[] RAINBOW_START_POSITOINS = new float[]{0.761f, 0.761f, 0.7f, 0.83f, 0.6f};
    private static final float[] RAINBOW_START_SCALES = new float[]{1.0f, 1.0f, 0.8f, 1.0f, 0.1778f};
    private static final float[] SQUARE_TEXTURE = new float[]{0.0f, 0.0f, 0.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.0f};
    private static final float[] SQUARE_VERTEX = new float[]{-1.0f, 1.0f, 0.0f, -1.0f, -1.0f, 0.0f, 1.0f, -1.0f, 0.0f, 1.0f, 1.0f, 0.0f};
    private static final int[] TEXTURE_SRCS = new int[]{R$raw.mask, 0, R$raw.light, R$raw.pentagon, R$raw.rainbow0, R$raw.rainbow1, R$raw.rainbow2, R$raw.rainbow3, R$raw.blue};
    static boolean bFirstBoot = true;
    static Bitmap[] mBitmaps = new Bitmap[9];
    private CubicBezierInterpolator mAlphaInterpolator;
    private float mBackgroundScaleStart;
    private int mBaseAnimProgram;
    boolean[] mBitmapLoad;
    int mBitmapNm;
    private int[] mBufIds;
    private int mColorAnimProgram;
    private Context mContext;
    private float mDiagonal;
    private long mDisappearStart;
    boolean mEnableLensFlare;
    boolean mEnableMask;
    private float mGlowCenterX;
    private float mGlowCenterY;
    private Handler mHandler;
    private int mHeight;
    private long mHexagonAppearStart;
    private LinearInterpolator mInterpolator;
    boolean mIsHide;
    protected boolean mIsTouchDown;
    public boolean mIsUnlock;
    private boolean mKeepShow;
    LensFlareListener mListener;
    private float[] mMMatrix;
    private float[] mMVPMatrix;
    protected long mMaskDisappearStart;
    private float[] mPMatrix;
    private float mPressScale;
    private float mProcess;
    private float mProcessTouch;
    private float mRainbowDegree;
    private long mRainbowStart;
    private float mRange;
    private float mRatio;
    private float mRayAlphaStart;
    private float mScreenCenterX;
    private float mScreenCenterY;
    private float mSunAlphaStart;
    private Interpolator mSunInterpolator;
    private float mSunScaleStart;
    private CubicBezierInterpolator mTansInterpolatorTemp;
    private FloatBuffer mTexBuffer;
    private int[] mTextures;
    private Interpolator mUnlockInterpolator;
    private float[] mVMatrix;
    private FloatBuffer mVerBuffer;
    private GLSurfaceView mView;
    boolean mVisible;
    private int mWidth;
    private float mX;
    private float mY;
    private int maBaseTexCoord;
    private int maBaseVerCoord;
    private int maColorTexCoord;
    private int maColorVerCoord;
    private int muBaseAlpha;
    private int muBaseMVPMatrix;
    private int muBaseTex;
    private int muColorAlpha;
    private int muColorColor;
    private int muColorMVPMatrix;
    private int muColorTex;

    public interface LensFlareListener {
        Bitmap onGetUnderlayerBmp();
    }

    class WorkThread extends Thread {
        int mIdx;

        public WorkThread(int id) {
            this.mIdx = id;
        }

        public void run() {
            if (this.mIdx < 0 || this.mIdx > 9) {
                HwLog.w("LensFlareRenderer", "WorkThread.run index out of bound, mIdx = " + this.mIdx + " / " + 9);
                return;
            }
            if (this.mIdx == 1) {
                int callState = ((TelephonyManager) LensFlareRenderer.this.mContext.getSystemService("phone")).getCallState();
                if (!LensFlareRenderer.isbFirstBoot() && callState == 0) {
                    if (LensFlareRenderer.this.mListener == null) {
                        HwLog.e("LensFlareRenderer", "fitatc no listener specified, cannot get underlayer bitmap");
                        return;
                    }
                    LensFlareRenderer.mBitmaps[this.mIdx] = LensFlareRenderer.this.mListener.onGetUnderlayerBmp();
                }
            } else {
                LensFlareRenderer.mBitmaps[this.mIdx] = LensFlareRenderer.this.getBitmap(LensFlareRenderer.TEXTURE_SRCS[this.mIdx]);
            }
            LensFlareRenderer lensFlareRenderer = LensFlareRenderer.this;
            lensFlareRenderer.mBitmapNm++;
            LensFlareRenderer.this.mView.requestRender();
        }
    }

    private static /* synthetic */ int[] -getandroid-graphics-Bitmap$ConfigSwitchesValues() {
        if (-android-graphics-Bitmap$ConfigSwitchesValues != null) {
            return -android-graphics-Bitmap$ConfigSwitchesValues;
        }
        int[] iArr = new int[Config.values().length];
        try {
            iArr[Config.ALPHA_8.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Config.ARGB_4444.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Config.ARGB_8888.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Config.RGB_565.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -android-graphics-Bitmap$ConfigSwitchesValues = iArr;
        return iArr;
    }

    public static boolean isbFirstBoot() {
        return bFirstBoot;
    }

    public static void setbFirstBoot(boolean bFirstBoot) {
        bFirstBoot = bFirstBoot;
    }

    public void computeMatrix(float a, float sx, float sy, float tx, float ty) {
        Matrix.setIdentityM(this.mMMatrix, 0);
        if (!(tx == 0.0f && ty == 0.0f)) {
            Matrix.translateM(this.mMMatrix, 0, tx, ty, 0.0f);
        }
        if (!(sx == 1.0f && sy == 1.0f)) {
            Matrix.scaleM(this.mMMatrix, 0, sx, sy, 1.0f);
        }
        if (a != 0.0f) {
            Matrix.rotateM(this.mMMatrix, 0, a, 0.0f, 0.0f, 1.0f);
        }
        Matrix.multiplyMM(this.mMVPMatrix, 16, this.mVMatrix, 0, this.mMMatrix, 0);
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mPMatrix, 0, this.mMVPMatrix, 16);
    }

    public void clear() {
        mBitmaps[1] = null;
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        this.mHandler = null;
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Matrix.setLookAtM(this.mVMatrix, 0, 0.0f, 0.0f, 3.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        String vertexShader = readShaderSource(this.mContext, "baseAnim.vertex");
        String fragmentShaderBase = readShaderSource(this.mContext, "baseAnim.fragment");
        String fragmentShaderColor = readShaderSource(this.mContext, "colorAnim.fragment");
        createBaseAnimationProgram(vertexShader, fragmentShaderBase);
        createColorAnimationProgram(vertexShader, fragmentShaderColor);
        GLES20.glGenTextures(9, this.mTextures, 0);
        int i = 0;
        while (i < 9) {
            if (mBitmaps[i] == null || i == 1) {
                WorkThread workThread = new WorkThread(i);
                workThread.setDaemon(true);
                workThread.start();
            } else {
                this.mBitmapNm++;
            }
            i++;
        }
        for (i = 0; i < 9; i++) {
            this.mBitmapLoad[i] = false;
        }
        if (this.mBitmapNm > 0) {
            this.mView.requestRender();
        }
        GLES20.glGenBuffers(2, this.mBufIds, 0);
        ByteBuffer buf = ByteBuffer.allocateDirect(SQUARE_VERTEX.length * 4);
        buf.order(ByteOrder.nativeOrder());
        this.mVerBuffer = buf.asFloatBuffer();
        buf = ByteBuffer.allocateDirect(SQUARE_TEXTURE.length * 4);
        buf.order(ByteOrder.nativeOrder());
        this.mTexBuffer = buf.asFloatBuffer();
    }

    @SuppressLint({"NewApi"})
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        this.mGlowCenterX = ((float) this.mWidth) / 2.0f;
        this.mGlowCenterY = ((float) this.mHeight) / 2.0f;
        this.mScreenCenterX = ((float) this.mWidth) / 2.0f;
        this.mScreenCenterY = ((float) this.mHeight) / 2.0f;
        this.mDiagonal = FloatMath.sqrt(FloatMath.pow((float) this.mHeight, 2.0f) + FloatMath.pow((float) this.mWidth, 2.0f));
        this.mRange = this.mDiagonal / 2.0f;
        this.mRatio = ((float) width) / ((float) height);
        GLES20.glViewport(0, 0, width, height);
        Matrix.frustumM(this.mPMatrix, 0, -this.mRatio, this.mRatio, -1.0f, 1.0f, 1.0f, 5.0f);
        GLES20.glBindBuffer(34962, this.mBufIds[0]);
        this.mVerBuffer.put(SQUARE_VERTEX);
        this.mVerBuffer.position(0);
        GLES20.glBufferData(34962, this.mVerBuffer.capacity() * 4, this.mVerBuffer, 35044);
        GLES20.glBindBuffer(34962, this.mBufIds[1]);
        this.mTexBuffer.put(SQUARE_TEXTURE);
        this.mTexBuffer.position(0);
        GLES20.glBufferData(34962, this.mTexBuffer.capacity() * 4, this.mTexBuffer, 35044);
    }

    private void loadTexture() {
        synchronized (LensFlareRenderer.class) {
            if (this.mBitmapNm > 0) {
                int i = 0;
                while (i < 9) {
                    if (!(mBitmaps[i] == null || this.mBitmapLoad[i])) {
                        if (mBitmaps[i].isRecycled()) {
                            HwLog.w("fitatc", "bitmap is recyled : " + i);
                            mBitmaps[i] = null;
                        } else {
                            GLES20.glBindTexture(3553, this.mTextures[i]);
                            GLES20.glTexParameterf(3553, 10241, 9729.0f);
                            GLES20.glTexParameterf(3553, 10240, 9729.0f);
                            if (1 == i) {
                                int bytesPerPixel = computeBytesPerPixel(mBitmaps[1].getConfig());
                                int stride = bytesPerPixel == 0 ? 0 : mBitmaps[1].getRowBytes() / bytesPerPixel;
                                if (stride != mBitmaps[1].getWidth()) {
                                    GLES20.glPixelStorei(3314, stride);
                                    GLUtils.texImage2D(3553, 0, mBitmaps[i], 0);
                                    GLES20.glPixelStorei(3314, 0);
                                } else {
                                    GLUtils.texImage2D(3553, 0, mBitmaps[i], 0);
                                }
                            } else {
                                GLUtils.texImage2D(3553, 0, mBitmaps[i], 0);
                            }
                            this.mBitmapLoad[i] = true;
                            this.mBitmapNm--;
                        }
                    }
                    i++;
                }
            }
        }
    }

    public void onDrawFrame(GL10 gl) {
        loadTexture();
        GLES20.glBindFramebuffer(36160, 0);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES20.glClear(16640);
        if (this.mVisible && !this.mIsHide) {
            GLES20.glEnable(3042);
            if (this.mEnableMask) {
                GLES20.glBlendFunc(770, 1);
                drawBackground(this.mX, this.mY);
            }
            drawSunMask(this.mX, this.mY);
            if (this.mEnableLensFlare) {
                GLES20.glBlendFunc(770, 1);
                moveWithTouchAnimation(this.mX, this.mY);
                drawRainbowsAnimation(this.mX, this.mY);
            }
            GLES20.glDisable(3042);
            if (this.mKeepShow) {
                drawBackground();
            }
        }
    }

    private void checkGlError(String op) {
        while (true) {
            int error = GLES20.glGetError();
            if (error != 0) {
                HwLog.e("LensFlareRenderer", op + ": glError " + error);
                this.mContext.sendBroadcast(new Intent("Remove_GLSurfaceview"));
            } else {
                return;
            }
        }
    }

    private Bitmap getBitmap(int id) {
        InputStream is = this.mContext.getResources().openRawResource(id);
        try {
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            return bitmap;
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void drawBackground(float x, float y) {
        float scale;
        float alpha = 1.0f;
        if (this.mIsTouchDown) {
            scale = 1.8f - (0.8f * this.mInterpolator.getInterpolation(this.mProcessTouch));
            this.mBackgroundScaleStart = scale;
        } else {
            long t = System.currentTimeMillis() - this.mMaskDisappearStart;
            if (t > 400) {
                this.mMaskDisappearStart = 0;
                return;
            }
            float inter = this.mInterpolator.getInterpolation(((float) t) / 400.0f);
            if (this.mIsUnlock) {
                scale = this.mBackgroundScaleStart + ((1.0f - this.mBackgroundScaleStart) * this.mUnlockInterpolator.getInterpolation(((float) t) / 400.0f));
            } else {
                scale = this.mBackgroundScaleStart;
                alpha = 1.0f - (1.0f * inter);
            }
        }
        scale *= 3.0f;
        computeMatrix(0.0f, scale * this.mRatio, scale, 0.0f, 0.0f);
        drawBaseAnim(1, this.mMVPMatrix, 0, alpha);
    }

    private void drawBaseAnim(int texIdx, float[] mvpMatrix, int mOffset, float alpha) {
        if (texIdx < 0 || texIdx > 9) {
            HwLog.w("LensFlareRenderer", "drawBaseAnim, texture index out of bounds, " + texIdx + "/" + 9);
        } else if (this.mBaseAnimProgram == 0 || !this.mBitmapLoad[texIdx]) {
            HwLog.w("LensFlareRenderer", "drawBaseAnim, render fail: program = " + this.mBaseAnimProgram + ", texture ready = " + this.mBitmapLoad[texIdx]);
        } else {
            GLES20.glUseProgram(this.mBaseAnimProgram);
            checkGlError("mBackgroundProgram");
            GLES20.glBindBuffer(34962, this.mBufIds[0]);
            GLES20.glEnableVertexAttribArray(this.maBaseVerCoord);
            GLES20.glVertexAttribPointer(this.maBaseVerCoord, 3, 5126, false, 12, 0);
            GLES20.glBindTexture(3553, this.mTextures[texIdx]);
            GLES20.glUniform1i(this.muBaseTex, 0);
            GLES20.glBindBuffer(34962, this.mBufIds[1]);
            GLES20.glEnableVertexAttribArray(this.maBaseTexCoord);
            GLES20.glVertexAttribPointer(this.maBaseTexCoord, 2, 5126, false, 8, 0);
            GLES20.glUniformMatrix4fv(this.muBaseMVPMatrix, 1, false, mvpMatrix, mOffset);
            GLES20.glUniform1f(this.muBaseAlpha, alpha);
            GLES20.glDrawArrays(6, 0, 4);
            checkGlError("glDrawArrays");
        }
    }

    private void drawColorAnim(int texIdx, float[] mvpMatrix, int offset, float alpha, float r, float g, float b) {
        if (texIdx < 0 || texIdx > 9) {
            HwLog.w("LensFlareRenderer", "drawBaseAnim, texture index out of bounds, " + texIdx + "/" + 9);
        } else if (this.mColorAnimProgram == 0 || !this.mBitmapLoad[texIdx]) {
            HwLog.w("LensFlareRenderer", "drawBaseAnim, render fail: program = " + this.mBaseAnimProgram + ", texture ready = " + this.mBitmapLoad[texIdx]);
        } else {
            GLES20.glUseProgram(this.mColorAnimProgram);
            checkGlError("mColorAnimProgram");
            GLES20.glBindBuffer(34962, this.mBufIds[0]);
            GLES20.glEnableVertexAttribArray(this.maColorVerCoord);
            GLES20.glVertexAttribPointer(this.maColorVerCoord, 3, 5126, false, 12, 0);
            GLES20.glBindTexture(3553, this.mTextures[texIdx]);
            GLES20.glUniform1i(this.muColorTex, 0);
            GLES20.glBindBuffer(34962, this.mBufIds[1]);
            GLES20.glEnableVertexAttribArray(this.maColorTexCoord);
            GLES20.glVertexAttribPointer(this.maColorTexCoord, 2, 5126, false, 8, 0);
            GLES20.glUniformMatrix4fv(this.muColorMVPMatrix, 1, false, mvpMatrix, offset);
            GLES20.glUniform1f(this.muColorAlpha, alpha);
            GLES20.glUniform3f(this.muColorColor, r, g, b);
            GLES20.glDrawArrays(6, 0, 4);
            checkGlError("glDrawArrays");
        }
    }

    protected void drawBackground() {
        computeMatrix(0.0f, 3.0f * this.mRatio, 3.0f, 0.0f, 0.0f);
        drawBaseAnim(1, this.mMVPMatrix, 0, 1.0f);
    }

    protected void drawSunMask(float x, float y) {
        float scale;
        float alpha = 1.0f;
        float rayAlpha;
        if (this.mIsTouchDown) {
            float scaleInter = this.mInterpolator.getInterpolation(this.mProcessTouch);
            scale = 1.0f + (4.0f * scaleInter);
            alpha = 1.0f - (0.3f * scaleInter);
            rayAlpha = 1.0f - (0.3f * scaleInter);
            if (rayAlpha < 0.0f) {
                rayAlpha = 0.0f;
            }
            this.mSunAlphaStart = alpha;
            this.mSunScaleStart = scale;
            this.mRayAlphaStart = rayAlpha;
        } else {
            long t = System.currentTimeMillis() - this.mMaskDisappearStart;
            if (t > 400) {
                this.mMaskDisappearStart = 0;
                if (this.mIsUnlock) {
                    this.mKeepShow = true;
                }
                return;
            }
            float inter = this.mInterpolator.getInterpolation(((float) t) / 400.0f);
            if (this.mIsUnlock) {
                scale = this.mSunScaleStart + ((28.0f - this.mSunScaleStart) * this.mSunInterpolator.getInterpolation(((float) t) / 400.0f));
            } else {
                scale = this.mSunScaleStart + ((0.5f - this.mSunScaleStart) * inter);
                alpha = this.mSunAlphaStart - (this.mSunAlphaStart * inter);
                rayAlpha = this.mRayAlphaStart - (this.mRayAlphaStart * inter);
            }
        }
        if (this.mEnableMask) {
            GLES20.glBlendFunc(0, 770);
            updateMaskMVPMatrix(this.mMVPMatrix, x, y, scale);
            drawBaseAnim(0, this.mMVPMatrix, 0, 1.0f);
        }
        GLES20.glBlendFunc(770, 1);
        updateSunMVPMatrix(this.mMVPMatrix, x, y, scale);
        drawBaseAnim(2, this.mMVPMatrix, 0, 0.8f * alpha);
    }

    private void drawLensFlare(float dx, float dy, float interScale, float interAlpha) {
        if (dx != 0.0f || dy != 0.0f) {
            float add = 1.0f - this.mProcess;
            for (int i = 0; i < 19; i++) {
                float cx = calculatePosX(dx, LENSFLARE_TOUCH_POSITOINS[i]);
                float degree = (this.mRainbowDegree * 180.0f) / 3.1415927f;
                float alphaProcess = LENSFLARE_TOUCH_ALPHAS[i] * (1.0f + add);
                float scaleProcess = LENSFLARE_TOUCH_SCALES[i] * (getPorcess(add) + 1.0f);
                computeMatrix(degree, scaleProcess * interScale, scaleProcess * interScale, (((2.0f * cx) - ((float) this.mWidth)) * 3.0f) / ((float) this.mHeight), ((((float) this.mHeight) - (2.0f * calculatePosY(dy, LENSFLARE_TOUCH_POSITOINS[i]))) * 3.0f) / ((float) this.mHeight));
                drawColorAnim(3, this.mMVPMatrix, 0, (alphaProcess * interAlpha) * 0.6f, LENSFLARE_TOUCH_COLORS[(i * 3) + 0], LENSFLARE_TOUCH_COLORS[(i * 3) + 1], LENSFLARE_TOUCH_COLORS[(i * 3) + 2]);
            }
        }
    }

    private float getPorcess(float t) {
        AccelerateInterpolator inter = new AccelerateInterpolator(1.0f);
        if (t <= 0.5f) {
            return inter.getInterpolation(t) * 4.0f;
        }
        return ((1.0f - (((1.0f / 0.25f) * (t - 0.5f)) * (t - 0.5f))) * inter.getInterpolation(0.5f)) * 4.0f;
    }

    private float getBluePorcess(float t) {
        if (t < 0.5f) {
            return 0.0f;
        }
        return (2.0f * t) - 1.0f;
    }

    private float getBlueTransPorcess(float t) {
        return new AccelerateInterpolator(1.5f).getInterpolation(t) * 2.0f;
    }

    private float getDAPorcess(float t) {
        if (t <= 0.5f) {
            return (-2.0f * t) + 1.0f;
        }
        return (2.0f * t) - 1.0f;
    }

    private float getScalePorcess(float t) {
        if (t <= 0.5f) {
            return (-2.0f * t) + 1.0f;
        }
        if (t < 0.75f) {
            return (4.0f * t) - 2.0f;
        }
        return (-4.0f * t) + 4.0f;
    }

    private float getRainbowPorcess(float t) {
        if (t < 0.5f) {
            return 1.0f;
        }
        if (t < 0.75f) {
            return (-4.0f * t) + 3.0f;
        }
        return 0.0f;
    }

    private float getRainbow3AlphaPorcess(float t) {
        if (t < 0.5f) {
            return 1.0f;
        }
        if (t < 0.75f) {
            return (-4.0f * t) + 3.0f;
        }
        return (4.0f * t) - 3.0f;
    }

    private float getRainbowTransReversePorcess(float t) {
        if (t < 0.5f) {
            return 0.0f;
        }
        return (1.0f - (((t - 0.75f) * 16.0f) * (t - 0.75f))) * 1.5f;
    }

    private void moveWithTouchAnimation(float x, float y) {
        float interpolation;
        long t;
        if (this.mIsTouchDown) {
            t = System.currentTimeMillis() - this.mHexagonAppearStart;
            if (t > 270) {
                this.mHexagonAppearStart = 0;
                interpolation = 1.0f;
            } else {
                interpolation = this.mInterpolator.getInterpolation(((float) t) / 270.0f);
            }
        } else {
            t = System.currentTimeMillis() - this.mDisappearStart;
            if (t > 270 || this.mIsUnlock) {
                this.mDisappearStart = 0;
                return;
            }
            interpolation = 1.0f - this.mInterpolator.getInterpolation(((float) t) / 270.0f);
        }
        drawLensFlare(x, y, (-0.25f * interpolation) + 1.0f, interpolation * 0.4f);
    }

    private void drawRainbowsAnimation(float x, float y) {
        float interpolation;
        float alphaInterplation;
        long t;
        if (this.mIsTouchDown) {
            t = System.currentTimeMillis() - this.mRainbowStart;
            if (t > 270) {
                this.mRainbowStart = 0;
                interpolation = 1.0f;
                alphaInterplation = 1.0f;
            } else {
                interpolation = this.mInterpolator.getInterpolation(((float) t) / 270.0f);
                alphaInterplation = this.mAlphaInterpolator.getInterpolation(((float) t) / 270.0f);
            }
        } else {
            t = System.currentTimeMillis() - this.mDisappearStart;
            if (t > 270 || this.mIsUnlock) {
                this.mDisappearStart = 0;
                return;
            }
            interpolation = 1.0f - this.mInterpolator.getInterpolation(((float) t) / 270.0f);
            alphaInterplation = 1.0f - this.mAlphaInterpolator.getInterpolation(((float) t) / 270.0f);
        }
        float scaleInter = interpolation;
        float alphaInter = alphaInterplation;
        float transInter = interpolation;
        drawRainbows(x, y, interpolation, interpolation, alphaInterplation);
    }

    private void drawRainbows(float x, float y, float transInter, float scaleInter, float alphaInter) {
        float transProcess = ((1.25f - RAINBOW_POSITOINS[0]) * this.mTansInterpolatorTemp.getInterpolation(1.0f - this.mProcess)) + RAINBOW_POSITOINS[0];
        float alphaProcess = RAINBOW_ALPHAS[0] * getRainbowPorcess(1.0f - this.mProcess);
        updateDrawRainbowsMVPMatrix(this.mMVPMatrix, calculatePosX(x, ((transProcess - RAINBOW_START_POSITOINS[0]) * transInter) + RAINBOW_START_POSITOINS[0]), calculatePosY(y, ((transProcess - RAINBOW_START_POSITOINS[0]) * transInter) + RAINBOW_START_POSITOINS[0]), 1.3f * ((((RAINBOW_SCALES[0] * (getDAPorcess(1.0f - this.mProcess) + 1.0f)) - RAINBOW_START_SCALES[0]) * scaleInter) + RAINBOW_START_SCALES[0]));
        drawBaseAnim(4, this.mMVPMatrix, 0, (alphaProcess * alphaInter) * 0.5f);
        transProcess = ((1.25f - RAINBOW_POSITOINS[1]) * this.mTansInterpolatorTemp.getInterpolation(1.0f - this.mProcess)) + RAINBOW_POSITOINS[1];
        alphaProcess = RAINBOW_ALPHAS[1] * getRainbowPorcess(1.0f - this.mProcess);
        updateDrawRainbowsMVPMatrix(this.mMVPMatrix, calculatePosX(x, ((transProcess - RAINBOW_START_POSITOINS[1]) * transInter) + RAINBOW_START_POSITOINS[1]), calculatePosY(y, ((transProcess - RAINBOW_START_POSITOINS[1]) * transInter) + RAINBOW_START_POSITOINS[1]), 1.3f * ((((RAINBOW_SCALES[1] * (getDAPorcess(1.0f - this.mProcess) + 1.0f)) - RAINBOW_START_SCALES[1]) * scaleInter) + RAINBOW_START_SCALES[1]));
        drawBaseAnim(5, this.mMVPMatrix, 0, (alphaProcess * alphaInter) * 0.5f);
        transProcess = RAINBOW_POSITOINS[2] * (getRainbowTransReversePorcess(1.0f - this.mProcess) + 1.0f);
        updateDrawRainbowsMVPMatrix(this.mMVPMatrix, calculatePosX(x, ((transProcess - RAINBOW_START_POSITOINS[2]) * transInter) + RAINBOW_START_POSITOINS[2]), calculatePosY(y, ((transProcess - RAINBOW_START_POSITOINS[2]) * transInter) + RAINBOW_START_POSITOINS[2]), 1.3f * (((((RAINBOW_SCALES[2] * (getScalePorcess(1.0f - this.mProcess) + 1.0f)) / 1.5f) - RAINBOW_START_SCALES[2]) * scaleInter) + RAINBOW_START_SCALES[2]));
        drawBaseAnim(6, this.mMVPMatrix, 0, ((RAINBOW_ALPHAS[2] * alphaInter) * getRainbow3AlphaPorcess(1.0f - this.mProcess)) * 0.4f);
        updateDrawRainbowsMVPMatrix(this.mMVPMatrix, calculatePosX(x, ((RAINBOW_POSITOINS[3] - RAINBOW_START_POSITOINS[3]) * transInter) + RAINBOW_START_POSITOINS[3]), calculatePosY(y, ((RAINBOW_POSITOINS[3] - RAINBOW_START_POSITOINS[3]) * transInter) + RAINBOW_START_POSITOINS[3]), 1.3f * ((((RAINBOW_SCALES[3] * (getDAPorcess(1.0f - this.mProcess) + 1.0f)) - RAINBOW_START_SCALES[3]) * scaleInter) + RAINBOW_START_SCALES[3]));
        drawBaseAnim(7, this.mMVPMatrix, 0, (RAINBOW_ALPHAS[3] * alphaInter) * 0.5f);
        float angle = ((getBluePorcess(1.0f - this.mProcess) * 0.7330383f) + 0.5235988f) * (1.4f - (0.4f * scaleInter));
        transProcess = ((1.0f - RAINBOW_POSITOINS[4]) * getBlueTransPorcess(1.0f - this.mProcess)) + RAINBOW_POSITOINS[4];
        updateDrawRainbowsMVPMatrix(this.mMVPMatrix, calculatePosX(rotateX((double) (-angle), x, y), transProcess), calculatePosY(rotateY((double) (-angle), x, y), transProcess), 1.3f * ((((RAINBOW_SCALES[4] * (getBluePorcess(1.0f - this.mProcess) + 1.0f)) - RAINBOW_START_SCALES[4]) * scaleInter) + RAINBOW_START_SCALES[4]));
        drawBaseAnim(8, this.mMVPMatrix, 0, (RAINBOW_ALPHAS[4] * alphaInter) * 0.5f);
    }

    private void updateDrawRainbowsMVPMatrix(float[] mvpMatrix, float x, float y, float scale) {
        computeMatrix(((this.mRainbowDegree * 180.0f) / 3.1415927f) + 90.0f, scale, scale, (((x * 2.0f) - ((float) this.mWidth)) * 3.0f) / ((float) this.mHeight), ((((float) this.mHeight) - (y * 2.0f)) * 3.0f) / ((float) this.mHeight));
    }

    private void updateSunMVPMatrix(float[] mvpMatrix, float x, float y, float scale) {
        scale *= this.mPressScale * 3.0f;
        computeMatrix(0.0f, scale, scale, (((x * 2.0f) - ((float) this.mWidth)) * 3.0f) / ((float) this.mHeight), ((((float) this.mHeight) - (y * 2.0f)) * 3.0f) / ((float) this.mHeight));
    }

    private void updateMaskMVPMatrix(float[] mvpMatrix, float x, float y, float scale) {
        scale *= (this.mPressScale * 3.0f) * 4.0f;
        computeMatrix(0.0f, scale, scale, (((x * 2.0f) - ((float) this.mWidth)) * 3.0f) / ((float) this.mHeight), ((((float) this.mHeight) - (y * 2.0f)) * 3.0f) / ((float) this.mHeight));
    }

    private void createBaseAnimationProgram(String vertexShader, String fragmentShader) {
        this.mBaseAnimProgram = createProgram(vertexShader, fragmentShader);
        if (this.mBaseAnimProgram == 0) {
            HwLog.e("LensFlareRenderer", "create base animation program failed!");
            return;
        }
        this.maBaseVerCoord = GLES20.glGetAttribLocation(this.mBaseAnimProgram, "aVertexCoord");
        checkGlError("glGetAttribLocation maBaseVerCoord");
        if (-1 == this.maBaseVerCoord) {
            throw new RuntimeException("Could not get attrib vertex coordinate");
        }
        this.maBaseTexCoord = GLES20.glGetAttribLocation(this.mBaseAnimProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation maBaseTexCoord");
        if (-1 == this.maBaseTexCoord) {
            throw new RuntimeException("Could not get attrib texture coordinate");
        }
        this.muBaseTex = GLES20.glGetUniformLocation(this.mBaseAnimProgram, "uTexture");
        checkGlError("glGetUniformLocation muBaseTex");
        if (-1 == this.muBaseTex) {
            throw new RuntimeException("Could not get uniform texture");
        }
        this.muBaseMVPMatrix = GLES20.glGetUniformLocation(this.mBaseAnimProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation muBaseMVPMatrix");
        if (-1 == this.muBaseMVPMatrix) {
            throw new RuntimeException("Could not get uniform MVPMatrix");
        }
        this.muBaseAlpha = GLES20.glGetUniformLocation(this.mBaseAnimProgram, "uAlpha");
        checkGlError("glGetUniformLocation muBaseAlpha");
        if (-1 == this.muBaseAlpha) {
            throw new RuntimeException("Could not get uniform alpha");
        }
    }

    private void createColorAnimationProgram(String vertexShader, String fragmentShader) {
        this.mColorAnimProgram = createProgram(vertexShader, fragmentShader);
        if (this.mColorAnimProgram == 0) {
            HwLog.e("LensFlareRenderer", "create color animation program failed!");
            return;
        }
        this.maColorVerCoord = GLES20.glGetAttribLocation(this.mColorAnimProgram, "aVertexCoord");
        checkGlError("glGetAttribLocation maColorVerCoord");
        if (-1 == this.maColorVerCoord) {
            throw new RuntimeException("Could not get attrib vertex coordinate");
        }
        this.maColorTexCoord = GLES20.glGetAttribLocation(this.mColorAnimProgram, "aTextureCoord");
        checkGlError("glGetAttribLocation maColorTexCoord");
        if (-1 == this.maColorTexCoord) {
            throw new RuntimeException("Could not get attrib texture coordinate");
        }
        this.muColorTex = GLES20.glGetUniformLocation(this.mColorAnimProgram, "uTexture");
        checkGlError("glGetUniformLocation muColorTex");
        if (-1 == this.muColorTex) {
            throw new RuntimeException("Could not get uniform texture");
        }
        this.muColorMVPMatrix = GLES20.glGetUniformLocation(this.mColorAnimProgram, "uMVPMatrix");
        checkGlError("glGetUniformLocation muColorMVPMatrix");
        if (-1 == this.muColorMVPMatrix) {
            throw new RuntimeException("Could not get uniform MVPMatrix");
        }
        this.muColorAlpha = GLES20.glGetUniformLocation(this.mColorAnimProgram, "uAlpha");
        checkGlError("glGetUniformLocation muColorAlpha");
        if (-1 == this.muColorAlpha) {
            throw new RuntimeException("Could not get uniform alpha");
        }
        this.muColorColor = GLES20.glGetUniformLocation(this.mColorAnimProgram, "uColor");
        checkGlError("glGetUniformLocation muColorColor");
        if (-1 == this.muColorColor) {
            throw new RuntimeException("Could not get uniform alpha");
        }
    }

    private int loadShader(int shaderType, String source) {
        int shader = GLES20.glCreateShader(shaderType);
        if (shader == 0) {
            return shader;
        }
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        HwLog.e("LensFlareRenderer", "Could not compile shader + " + shaderType + ":");
        HwLog.e("LensFlareRenderer", GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    private int createProgram(String vertexSource, String fragmentSource) {
        int vertexShader = loadShader(35633, vertexSource);
        if (vertexShader == 0) {
            return 0;
        }
        int fragmentShader = loadShader(35632, fragmentSource);
        if (fragmentShader == 0) {
            return 0;
        }
        int program = GLES20.glCreateProgram();
        if (program != 0) {
            GLES20.glAttachShader(program, vertexShader);
            checkGlError("attachVertexShader");
            GLES20.glAttachShader(program, fragmentShader);
            checkGlError("attachFragmentShader");
            GLES20.glLinkProgram(program);
            int[] linkStatus = new int[1];
            GLES20.glGetProgramiv(program, 35714, linkStatus, 0);
            if (1 != linkStatus[0]) {
                HwLog.e("LensFlareRenderer", "Could not link program: ");
                HwLog.e("LensFlareRenderer", GLES20.glGetProgramInfoLog(program));
                GLES20.glDeleteProgram(program);
                program = 0;
            } else {
                GLES20.glDetachShader(program, vertexShader);
                GLES20.glDeleteShader(vertexShader);
                GLES20.glDetachShader(program, fragmentShader);
                GLES20.glDeleteShader(fragmentShader);
            }
        }
        return program;
    }

    private static String readShaderSource(Context context, String assetFilename) {
        Throwable th;
        AssetManager assetManager = context.getAssets();
        if (assetManager == null) {
            throw new RuntimeException("Error: Cannot get AssetManager from app's context");
        }
        InputStream inputStream = null;
        InputStreamReader inputStreamReader = null;
        Reader reader = null;
        String result = null;
        Writer writer = new StringWriter();
        char[] buffer = new char[32768];
        try {
            inputStream = assetManager.open(assetFilename);
            if (inputStream != null) {
                InputStreamReader inputStreamReader2 = new InputStreamReader(inputStream, "UTF-8");
                if (inputStreamReader2 != null) {
                    try {
                        Reader reader2 = new BufferedReader(inputStreamReader2);
                        while (true) {
                            try {
                                int readLength = reader2.read(buffer);
                                if (readLength == -1) {
                                    break;
                                }
                                writer.write(buffer, 0, readLength);
                            } catch (IOException e) {
                                reader = reader2;
                                inputStreamReader = inputStreamReader2;
                            } catch (Throwable th2) {
                                th = th2;
                                reader = reader2;
                                inputStreamReader = inputStreamReader2;
                            }
                        }
                        result = writer.toString();
                        reader = reader2;
                    } catch (IOException e2) {
                        inputStreamReader = inputStreamReader2;
                        try {
                            throw new RuntimeException("Error: Cannot get InputStream from app's asset manager:" + assetFilename);
                        } catch (Throwable th3) {
                            th = th3;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e3) {
                                    e3.printStackTrace();
                                    throw th;
                                }
                            }
                            if (inputStreamReader != null) {
                                inputStreamReader.close();
                            }
                            if (reader != null) {
                                reader.close();
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        inputStreamReader = inputStreamReader2;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (inputStreamReader != null) {
                            inputStreamReader.close();
                        }
                        if (reader != null) {
                            reader.close();
                        }
                        throw th;
                    }
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e32) {
                        e32.printStackTrace();
                    }
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (reader != null) {
                    reader.close();
                }
                return result;
            }
            throw new RuntimeException("Error: Null InputStream from app's asset manager:" + assetFilename);
        } catch (IOException e4) {
            throw new RuntimeException("Error: Cannot get InputStream from app's asset manager:" + assetFilename);
        }
    }

    private float calculatePosX(float x, float relativePos) {
        return (((this.mGlowCenterX + ((this.mGlowCenterX - x) / (1.1f + ((1.0f - this.mSunInterpolator.getInterpolation(this.mProcess)) * -0.48200005f)))) - x) * relativePos) + x;
    }

    private float calculatePosY(float y, float relativePos) {
        return (((this.mGlowCenterY + ((this.mGlowCenterY - y) / (1.1f + ((1.0f - this.mSunInterpolator.getInterpolation(this.mProcess)) * -0.48200005f)))) - y) * relativePos) + y;
    }

    private float rotateX(double a, float x, float y) {
        return (float) (((((double) (x - this.mGlowCenterX)) * Math.cos(a)) - (((double) (y - this.mGlowCenterY)) * Math.sin(a))) + ((double) this.mGlowCenterX));
    }

    private float rotateY(double a, float x, float y) {
        return (float) (((((double) (x - this.mGlowCenterX)) * Math.sin(a)) + (((double) (y - this.mGlowCenterY)) * Math.cos(a))) + ((double) this.mGlowCenterY));
    }

    public void onVisibilityChanged(int visibility) {
        boolean z = false;
        if (visibility == 0) {
            z = true;
        }
        this.mVisible = z;
    }

    private int computeBytesPerPixel(Config config) {
        switch (-getandroid-graphics-Bitmap$ConfigSwitchesValues()[config.ordinal()]) {
            case 1:
                return 1;
            case 2:
            case 4:
                return 2;
            case 3:
                return 4;
            default:
                return 0;
        }
    }
}
