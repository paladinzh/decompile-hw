package com.android.mms.attachment.ui.mediapicker.camerafocus;

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.hardware.Camera.Area;
import android.hardware.Camera.Parameters;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;
import java.util.List;

public class FocusOverlayManager {
    private boolean mAeAwbLock;
    private int mDisplayOrientation;
    private List<Object> mFocusArea;
    private boolean mFocusAreaSupported;
    private String mFocusMode;
    private Handler mHandler;
    private boolean mInitialized;
    Listener mListener;
    private boolean mLockAeAwbNeeded;
    private Matrix mMatrix;
    private List<Object> mMeteringArea;
    private boolean mMeteringAreaSupported;
    private boolean mMirror;
    private String mOverrideFocusMode;
    private Parameters mParameters = null;
    private PieRenderer mPieRenderer;
    private int mPreviewHeight;
    private int mPreviewWidth;
    private int mState = 0;

    public interface Listener {
        void autoFocus();

        void cancelAutoFocus();

        boolean capture();

        void setFocusParameters();
    }

    private class MainHandler extends Handler {
        public MainHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    FocusOverlayManager.this.cancelAutoFocus();
                    return;
                default:
                    return;
            }
        }
    }

    public FocusOverlayManager(Listener listener, Looper looper) {
        this.mHandler = new MainHandler(looper);
        this.mMatrix = new Matrix();
        this.mListener = listener;
    }

    public void setFocusRenderer(PieRenderer renderer) {
        this.mPieRenderer = renderer;
        this.mInitialized = this.mMatrix != null;
    }

    public void setParameters(Parameters parameters) {
        if (parameters != null) {
            boolean z;
            this.mParameters = parameters;
            this.mFocusAreaSupported = isFocusAreaSupported(parameters);
            this.mMeteringAreaSupported = isMeteringAreaSupported(parameters);
            if (isAutoExposureLockSupported(this.mParameters)) {
                z = true;
            } else {
                z = isAutoWhiteBalanceLockSupported(this.mParameters);
            }
            this.mLockAeAwbNeeded = z;
        }
    }

    public void setPreviewSize(int previewWidth, int previewHeight) {
        if (this.mPreviewWidth != previewWidth || this.mPreviewHeight != previewHeight) {
            this.mPreviewWidth = previewWidth;
            this.mPreviewHeight = previewHeight;
            setMatrix();
        }
    }

    public void setMirror(boolean mirror) {
        this.mMirror = mirror;
        setMatrix();
    }

    private void setMatrix() {
        boolean z = false;
        if (this.mPreviewWidth != 0 && this.mPreviewHeight != 0) {
            Matrix matrix = new Matrix();
            prepareMatrix(matrix, this.mMirror, this.mDisplayOrientation, this.mPreviewWidth, this.mPreviewHeight);
            matrix.invert(this.mMatrix);
            if (this.mPieRenderer != null) {
                z = true;
            }
            this.mInitialized = z;
        }
    }

    private void lockAeAwbIfNeeded() {
        if (this.mLockAeAwbNeeded && !this.mAeAwbLock) {
            this.mAeAwbLock = true;
            this.mListener.setFocusParameters();
        }
    }

    public void onAutoFocus(boolean focused, boolean shutterButtonPressed) {
        if (this.mState == 2) {
            if (focused) {
                this.mState = 3;
            } else {
                this.mState = 4;
            }
            updateFocusUI();
            capture();
        } else if (this.mState == 1) {
            if (focused) {
                this.mState = 3;
            } else {
                this.mState = 4;
            }
            updateFocusUI();
            if (this.mFocusArea != null) {
                this.mHandler.sendEmptyMessageDelayed(0, 3000);
            }
            if (shutterButtonPressed) {
                lockAeAwbIfNeeded();
            }
        }
    }

    public void onAutoFocusMoving(boolean moving) {
        if (this.mInitialized && this.mState == 0 && this.mPieRenderer != null) {
            if (moving) {
                this.mPieRenderer.showStart();
            } else {
                this.mPieRenderer.showSuccess(true);
            }
        }
    }

    private void initializeFocusAreas(int focusWidth, int focusHeight, int x, int y, int previewWidth, int previewHeight) {
        if (this.mFocusArea == null) {
            this.mFocusArea = new ArrayList();
            this.mFocusArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(focusWidth, focusHeight, ContentUtil.FONT_SIZE_NORMAL, x, y, previewWidth, previewHeight, ((Area) this.mFocusArea.get(0)).rect);
    }

    private void initializeMeteringAreas(int focusWidth, int focusHeight, int x, int y, int previewWidth, int previewHeight) {
        if (this.mMeteringArea == null) {
            this.mMeteringArea = new ArrayList();
            this.mMeteringArea.add(new Area(new Rect(), 1));
        }
        calculateTapArea(focusWidth, focusHeight, 1.5f, x, y, previewWidth, previewHeight, ((Area) this.mMeteringArea.get(0)).rect);
    }

    public void onSingleTapUp(int x, int y) {
        if (this.mInitialized && this.mState != 2) {
            if (this.mFocusArea != null) {
                if (!(this.mState == 1 || this.mState == 3)) {
                    if (this.mState == 4) {
                    }
                }
                cancelAutoFocus();
            }
            if (this.mPieRenderer != null) {
                int focusWidth = this.mPieRenderer.getSize();
                int focusHeight = this.mPieRenderer.getSize();
                if (focusWidth != 0 && this.mPieRenderer.getWidth() != 0 && this.mPieRenderer.getHeight() != 0) {
                    int previewWidth = this.mPreviewWidth;
                    int previewHeight = this.mPreviewHeight;
                    if (this.mFocusAreaSupported) {
                        initializeFocusAreas(focusWidth, focusHeight, x, y, previewWidth, previewHeight);
                    }
                    if (this.mMeteringAreaSupported) {
                        initializeMeteringAreas(focusWidth, focusHeight, x, y, previewWidth, previewHeight);
                    }
                    this.mPieRenderer.setFocus(x, y);
                    this.mListener.setFocusParameters();
                    if (this.mFocusAreaSupported) {
                        autoFocus();
                    } else {
                        updateFocusUI();
                        this.mHandler.removeMessages(0);
                        this.mHandler.sendEmptyMessageDelayed(0, 3000);
                    }
                }
            }
        }
    }

    public void onPreviewStarted() {
        this.mState = 0;
    }

    public void onPreviewStopped() {
        this.mState = 0;
        resetTouchFocus();
        updateFocusUI();
    }

    public void onCameraReleased() {
        onPreviewStopped();
    }

    private void autoFocus() {
        MLog.v("FocusOverlayManager", "Start autofocus.");
        this.mListener.autoFocus();
        this.mState = 1;
        updateFocusUI();
        this.mHandler.removeMessages(0);
    }

    private void cancelAutoFocus() {
        MLog.v("FocusOverlayManager", "Cancel autofocus.");
        resetTouchFocus();
        this.mListener.cancelAutoFocus();
        this.mState = 0;
        updateFocusUI();
        this.mHandler.removeMessages(0);
    }

    private void capture() {
        if (this.mListener.capture()) {
            this.mState = 0;
            this.mHandler.removeMessages(0);
        }
    }

    public String getFocusMode() {
        if (this.mOverrideFocusMode != null) {
            return this.mOverrideFocusMode;
        }
        if (this.mParameters == null) {
            MLog.e("FocusOverlayManager", "getFocusMode fialed, mParameters error null.");
            if (!this.mFocusAreaSupported || this.mFocusArea == null) {
                this.mFocusMode = "continuous-picture";
            } else {
                this.mFocusMode = "auto";
            }
            return this.mFocusMode;
        }
        List<String> supportedFocusModes = this.mParameters.getSupportedFocusModes();
        if (!this.mFocusAreaSupported || this.mFocusArea == null) {
            this.mFocusMode = "continuous-picture";
        } else {
            this.mFocusMode = "auto";
        }
        if (!isSupported(this.mFocusMode, supportedFocusModes)) {
            if (isSupported("auto", this.mParameters.getSupportedFocusModes())) {
                this.mFocusMode = "auto";
            } else {
                this.mFocusMode = this.mParameters.getFocusMode();
            }
        }
        return this.mFocusMode;
    }

    public List getFocusAreas() {
        return this.mFocusArea;
    }

    public List getMeteringAreas() {
        return this.mMeteringArea;
    }

    public void updateFocusUI() {
        if (this.mInitialized) {
            FocusIndicator focusIndicator = this.mPieRenderer;
            if (focusIndicator != null) {
                if (this.mState == 0) {
                    if (this.mFocusArea == null) {
                        focusIndicator.clear();
                    } else {
                        focusIndicator.showStart();
                    }
                } else if (this.mState == 1 || this.mState == 2) {
                    focusIndicator.showStart();
                } else if ("continuous-picture".equals(this.mFocusMode)) {
                    focusIndicator.showSuccess(false);
                } else if (this.mState == 3) {
                    focusIndicator.showSuccess(false);
                } else if (this.mState == 4) {
                    focusIndicator.showFail(false);
                }
            }
        }
    }

    public void resetTouchFocus() {
        if (this.mInitialized) {
            if (this.mPieRenderer != null) {
                this.mPieRenderer.clear();
            }
            this.mFocusArea = null;
            this.mMeteringArea = null;
        }
    }

    private void calculateTapArea(int focusWidth, int focusHeight, float areaMultiple, int x, int y, int previewWidth, int previewHeight, Rect rect) {
        int areaWidth = (int) (((float) focusWidth) * areaMultiple);
        int areaHeight = (int) (((float) focusHeight) * areaMultiple);
        int left = clamp(x - (areaWidth / 2), 0, previewWidth - areaWidth);
        int top = clamp(y - (areaHeight / 2), 0, previewHeight - areaHeight);
        RectF rectF = new RectF((float) left, (float) top, (float) (left + areaWidth), (float) (top + areaHeight));
        this.mMatrix.mapRect(rectF);
        rectFToRect(rectF, rect);
    }

    public static boolean isAutoExposureLockSupported(Parameters params) {
        return "true".equals(params.get("auto-exposure-lock-supported"));
    }

    public static boolean isAutoWhiteBalanceLockSupported(Parameters params) {
        return "true".equals(params.get("auto-whitebalance-lock-supported"));
    }

    public static boolean isSupported(String value, List<String> supported) {
        return supported != null && supported.indexOf(value) >= 0;
    }

    public static boolean isMeteringAreaSupported(Parameters params) {
        return params.getMaxNumMeteringAreas() > 0;
    }

    public static boolean isFocusAreaSupported(Parameters params) {
        if (params.getMaxNumFocusAreas() > 0) {
            return isSupported("auto", params.getSupportedFocusModes());
        }
        return false;
    }

    public static void prepareMatrix(Matrix matrix, boolean mirror, int displayOrientation, int viewWidth, int viewHeight) {
        matrix.setScale((float) (mirror ? -1 : 1), ContentUtil.FONT_SIZE_NORMAL);
        matrix.postRotate((float) displayOrientation);
        matrix.postScale(((float) viewWidth) / 2000.0f, ((float) viewHeight) / 2000.0f);
        matrix.postTranslate(((float) viewWidth) / 2.0f, ((float) viewHeight) / 2.0f);
    }

    public static int clamp(int x, int min, int max) {
        if (x > max) {
            return max;
        }
        if (x < min) {
            return min;
        }
        return x;
    }

    public static void rectFToRect(RectF rectF, Rect rect) {
        rect.left = Math.round(rectF.left);
        rect.top = Math.round(rectF.top);
        rect.right = Math.round(rectF.right);
        rect.bottom = Math.round(rectF.bottom);
    }
}
