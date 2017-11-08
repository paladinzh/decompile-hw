package com.android.mms.attachment.ui.mediapicker;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.AutoFocusMoveCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Looper;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.attachment.ui.mediapicker.camerafocus.FocusOverlayManager;
import com.android.mms.attachment.ui.mediapicker.camerafocus.FocusOverlayManager.Listener;
import com.android.mms.attachment.ui.mediapicker.camerafocus.PieRenderer;
import com.android.mms.attachment.ui.mediapicker.camerafocus.RenderOverlay;
import com.android.mms.attachment.utils.ThreadUtil;
import com.android.mms.attachment.utils.UiUtils;
import com.huawei.cspcommon.MLog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CameraManager implements Listener {
    private static ExecutorService mCameraManagerThread = Executors.newSingleThreadExecutor();
    private static CameraManager sCameraManager = new CameraManager();
    private static CameraWrapper sCameraWrapper = new CameraWrapper() {
        public int getNumberOfCameras() {
            return Camera.getNumberOfCameras();
        }

        public void getCameraInfo(int index, CameraInfo cameraInfo) {
            Camera.getCameraInfo(index, cameraInfo);
        }

        public Camera open(int cameraId) {
            return Camera.open(cameraId);
        }

        public void release(Camera camera) {
            camera.release();
        }
    };
    private Camera mCamera;
    private int mCameraIndex = -1;
    private final CameraInfo mCameraInfo = new CameraInfo();
    private CameraPreview mCameraPreview;
    private final FocusOverlayManager mFocusOverlayManager;
    private final boolean mHasFrontAndBackCamera;
    private boolean mIsHardwareAccelerationSupported;
    private int mLimitSize = -1;
    private CameraManagerListener mListener;
    private MmsVideoRecorder mMediaRecorder;
    private AsyncTask<Integer, Void, Camera> mOpenCameraTask;
    private boolean mOpenRequested;
    private OrientationHandler mOrientationHandler;
    private int mPendingOpenCameraIndex = -1;
    private int mRotation;
    private Integer mSavedOrientation = null;
    private boolean mTakingPicture;
    private MediaCallback mVideoCallback;
    private boolean mVideoModeRequested;

    interface CameraWrapper {
        void getCameraInfo(int i, CameraInfo cameraInfo);

        int getNumberOfCameras();

        Camera open(int i);

        void release(Camera camera);
    }

    interface CameraManagerListener {
        void onCameraChanged();

        void onCameraError(int i, Exception exception);
    }

    interface MediaCallback {
        void onMediaFailed(Exception exception);

        void onMediaInfo(int i);

        void onMediaReady(Uri uri, String str, int i, int i2);
    }

    private class OrientationHandler extends OrientationEventListener {
        OrientationHandler(Context context) {
            super(context);
        }

        public void onOrientationChanged(int orientation) {
            CameraManager.this.updateCameraOrientation();
        }
    }

    private static class SizeComparator implements Comparator<Size>, Serializable {
        private static final long serialVersionUID = 5533389372821303258L;
        private final int mMaxHeight;
        private final int mMaxWidth;
        private final double mTargetAspectRatio;
        private final int mTargetPixels;

        public SizeComparator(int maxWidth, int maxHeight, double targetAspectRatio, int targetPixels) {
            this.mMaxWidth = maxWidth;
            this.mMaxHeight = maxHeight;
            this.mTargetAspectRatio = targetAspectRatio;
            this.mTargetPixels = targetPixels;
        }

        public int compare(Size left, Size right) {
            Object obj = (left.width > this.mMaxWidth || left.height > this.mMaxHeight) ? null : 1;
            Object obj2 = (right.width > this.mMaxWidth || right.height > this.mMaxHeight) ? null : 1;
            if (obj != obj2) {
                return left.width <= this.mMaxWidth ? -1 : 1;
            }
            double rightAspectRatio = ((double) right.width) / ((double) right.height);
            double leftAspectRatioDiff = Math.abs((((double) left.width) / ((double) left.height)) - this.mTargetAspectRatio);
            double rightAspectRatioDiff = Math.abs(rightAspectRatio - this.mTargetAspectRatio);
            if (leftAspectRatioDiff - rightAspectRatioDiff == 0.0d) {
                return Math.abs((left.width * left.height) - this.mTargetPixels) - Math.abs((right.width * right.height) - this.mTargetPixels);
            }
            return Double.compare(leftAspectRatioDiff - rightAspectRatioDiff, 0.0d) < 0 ? -1 : 1;
        }
    }

    private CameraManager() {
        boolean hasFrontCamera = false;
        boolean hasBackCamera = false;
        CameraInfo cameraInfo = new CameraInfo();
        int cameraCount = sCameraWrapper.getNumberOfCameras();
        int i = 0;
        while (i < cameraCount) {
            try {
                sCameraWrapper.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == 1) {
                    hasFrontCamera = true;
                } else if (cameraInfo.facing == 0) {
                    hasBackCamera = true;
                }
                if (hasFrontCamera && hasBackCamera) {
                    break;
                }
                i++;
            } catch (RuntimeException e) {
                MLog.e("CameraManager", "Unable to load camera info", (Throwable) e);
            }
        }
        if (!hasFrontCamera) {
            hasBackCamera = false;
        }
        this.mHasFrontAndBackCamera = hasBackCamera;
        this.mFocusOverlayManager = new FocusOverlayManager(this, Looper.getMainLooper());
        this.mIsHardwareAccelerationSupported = true;
    }

    static synchronized CameraManager get() {
        CameraManager cameraManager;
        synchronized (CameraManager.class) {
            if (sCameraManager == null) {
                sCameraManager = new CameraManager();
            }
            cameraManager = sCameraManager;
        }
        return cameraManager;
    }

    static synchronized void destory() {
        synchronized (CameraManager.class) {
            sCameraManager = null;
        }
    }

    void setSurface(CameraPreview preview) {
        if (preview != this.mCameraPreview) {
            if (preview != null) {
                preview.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View view, MotionEvent motionEvent) {
                        if ((motionEvent.getActionMasked() & 1) == 1) {
                            CameraManager.this.mFocusOverlayManager.setPreviewSize(view.getWidth(), view.getHeight());
                            CameraManager.this.mFocusOverlayManager.onSingleTapUp(((int) motionEvent.getX()) + view.getLeft(), ((int) motionEvent.getY()) + view.getTop());
                        }
                        return true;
                    }
                });
            }
            this.mCameraPreview = preview;
            tryShowPreview();
        }
    }

    void setRenderOverlay(RenderOverlay renderOverlay) {
        PieRenderer pieRenderer = null;
        FocusOverlayManager focusOverlayManager = this.mFocusOverlayManager;
        if (renderOverlay != null) {
            pieRenderer = renderOverlay.getPieRenderer();
        }
        focusOverlayManager.setFocusRenderer(pieRenderer);
    }

    void swapCamera() {
        int i = 1;
        if (this.mCameraInfo.facing == 1) {
            i = 0;
        }
        selectCamera(i);
    }

    boolean isCameraFront() {
        if (this.mCameraInfo.facing == 1) {
            return true;
        }
        return false;
    }

    boolean selectCamera(int desiredFacing) {
        try {
            if (this.mCameraIndex >= 0 && this.mCameraInfo.facing == desiredFacing) {
                return true;
            }
            int cameraCount = sCameraWrapper.getNumberOfCameras();
            this.mCameraIndex = -1;
            setCamera(null);
            CameraInfo cameraInfo = new CameraInfo();
            for (int i = 0; i < cameraCount; i++) {
                sCameraWrapper.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == desiredFacing) {
                    this.mCameraIndex = i;
                    sCameraWrapper.getCameraInfo(i, this.mCameraInfo);
                    break;
                }
            }
            if (this.mCameraIndex < 0) {
                this.mCameraIndex = 0;
                sCameraWrapper.getCameraInfo(0, this.mCameraInfo);
            }
            if (this.mOpenRequested) {
                openCamera();
            }
            return true;
        } catch (RuntimeException e) {
            MLog.e("CameraManager", "RuntimeException in CameraManager.selectCamera", (Throwable) e);
            if (this.mListener != null) {
                this.mListener.onCameraError(1, e);
            }
            return false;
        }
    }

    int getCameraIndex() {
        return this.mCameraIndex;
    }

    void setLimitSize(int size) {
        this.mLimitSize = size;
    }

    void selectCameraByIndex(int cameraIndex) {
        if (this.mCameraIndex != cameraIndex) {
            try {
                this.mCameraIndex = cameraIndex;
                sCameraWrapper.getCameraInfo(this.mCameraIndex, this.mCameraInfo);
                if (this.mOpenRequested) {
                    openCamera();
                }
            } catch (RuntimeException e) {
                MLog.e("CameraManager", "RuntimeException in CameraManager.selectCameraByIndex", (Throwable) e);
                if (this.mListener != null) {
                    this.mListener.onCameraError(1, e);
                }
            }
        }
    }

    CameraInfo getCameraInfo() {
        if (this.mCameraIndex == -1) {
            return null;
        }
        return this.mCameraInfo;
    }

    boolean hasAnyCamera() {
        return sCameraWrapper.getNumberOfCameras() > 0;
    }

    boolean hasFrontAndBackCamera() {
        return this.mHasFrontAndBackCamera;
    }

    void openCamera() {
        if (this.mCameraIndex == -1) {
            selectCamera(0);
        }
        this.mOpenRequested = true;
        if (this.mPendingOpenCameraIndex != this.mCameraIndex && this.mCamera == null) {
            boolean delayTask = false;
            if (this.mOpenCameraTask != null) {
                this.mPendingOpenCameraIndex = -1;
                delayTask = true;
            }
            this.mPendingOpenCameraIndex = this.mCameraIndex;
            this.mOpenCameraTask = new AsyncTask<Integer, Void, Camera>() {
                private Exception mException;

                protected Camera doInBackground(Integer... params) {
                    try {
                        int cameraIndex = params[0].intValue();
                        if (MLog.isLoggable("Mms_app", 2)) {
                            MLog.v("CameraManager", "Opening camera " + CameraManager.this.mCameraIndex);
                        }
                        return CameraManager.sCameraWrapper.open(cameraIndex);
                    } catch (Exception e) {
                        MLog.e("CameraManager", "Exception while opening camera", (Throwable) e);
                        this.mException = e;
                        return null;
                    }
                }

                protected void onPostExecute(Camera camera) {
                    if (CameraManager.this.mOpenCameraTask == this && CameraManager.this.mOpenRequested) {
                        cleanup();
                        if (MLog.isLoggable("Mms_app", 2)) {
                            MLog.v("CameraManager", "Opened camera " + CameraManager.this.mCameraIndex + " " + (camera != null));
                        }
                        CameraManager.this.setCamera(camera);
                        if (camera == null) {
                            if (CameraManager.this.mListener != null) {
                                CameraManager.this.mListener.onCameraError(1, this.mException);
                            }
                            MLog.e("CameraManager", "Error opening camera");
                        }
                        return;
                    }
                    CameraManager.this.releaseCamera(camera);
                    cleanup();
                }

                protected void onCancelled() {
                    super.onCancelled();
                    cleanup();
                }

                private void cleanup() {
                    CameraManager.this.mPendingOpenCameraIndex = -1;
                    if (CameraManager.this.mOpenCameraTask == null || CameraManager.this.mOpenCameraTask.getStatus() != Status.PENDING) {
                        CameraManager.this.mOpenCameraTask = null;
                        return;
                    }
                    CameraManager.this.mOpenCameraTask.execute(new Integer[]{Integer.valueOf(CameraManager.this.mCameraIndex)});
                }
            };
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.v("CameraManager", "Start opening camera " + this.mCameraIndex);
            }
            if (!delayTask) {
                this.mOpenCameraTask.execute(new Integer[]{Integer.valueOf(this.mCameraIndex)});
            }
        }
    }

    boolean isVideoMode() {
        return this.mVideoModeRequested;
    }

    void updateVideoMode(boolean videoState) {
        this.mVideoModeRequested = videoState;
    }

    boolean isRecording() {
        return this.mVideoModeRequested && this.mVideoCallback != null;
    }

    void setVideoMode(boolean videoMode) {
        if (this.mVideoModeRequested != videoMode) {
            this.mVideoModeRequested = videoMode;
            tryInitOrCleanupVideoMode();
        }
    }

    void closeCamera() {
        this.mOpenRequested = false;
        setCamera(null);
    }

    void setListener(CameraManagerListener listener) {
        this.mListener = listener;
        if (!this.mIsHardwareAccelerationSupported && this.mListener != null) {
            this.mListener.onCameraError(6, null);
        }
    }

    void takePicture(final float heightPercent, final MediaCallback callback) {
        if (this.mCamera == null) {
            callback.onMediaFailed(null);
            return;
        }
        PictureCallback jpegCallback = new PictureCallback() {
            public void onPictureTaken(byte[] bytes, Camera camera) {
                CameraManager.this.mTakingPicture = false;
                if (CameraManager.this.mCamera != camera) {
                    callback.onMediaInfo(1);
                } else if (bytes == null) {
                    callback.onMediaInfo(2);
                } else {
                    int width;
                    int height;
                    Size size = camera.getParameters().getPictureSize();
                    if (CameraManager.this.mRotation == 90 || CameraManager.this.mRotation == 270) {
                        width = size.height;
                        height = size.width;
                    } else {
                        width = size.width;
                        height = size.height;
                    }
                    new ImagePersistTask(width, height, heightPercent, bytes, CameraManager.this.mCameraPreview.getContext(), callback).executeOnThreadPool((Object[]) new Void[0]);
                }
            }
        };
        this.mTakingPicture = true;
        try {
            this.mCamera.takePicture(null, null, null, jpegCallback);
        } catch (RuntimeException e) {
            MLog.e("CameraManager", "RuntimeException in CameraManager.takePicture", (Throwable) e);
            this.mTakingPicture = false;
            if (this.mListener != null) {
                this.mListener.onCameraError(7, e);
            }
        }
    }

    void startVideo(MediaCallback callback) {
        this.mVideoCallback = callback;
        tryStartVideoCapture();
    }

    private void releaseCamera(final Camera camera) {
        if (camera != null) {
            this.mFocusOverlayManager.onCameraReleased();
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... params) {
                    if (MLog.isLoggable("Mms_app", 2)) {
                        MLog.v("CameraManager", "Releasing camera " + CameraManager.this.mCameraIndex);
                    }
                    CameraManager.sCameraWrapper.release(camera);
                    return null;
                }
            }.execute(new Void[0]);
        }
    }

    public void releaseVideoErrorFile() {
        if (this.mMediaRecorder != null) {
            this.mMediaRecorder.cleanupErrorFile();
        }
    }

    private void releaseMediaRecorder(boolean cleanupFile) {
        if (this.mMediaRecorder != null) {
            this.mVideoModeRequested = false;
            if (cleanupFile) {
                this.mMediaRecorder.cleanupErrorFile();
                if (this.mVideoCallback != null) {
                    MediaCallback callback = this.mVideoCallback;
                    this.mVideoCallback = null;
                    callback.onMediaReady(null, null, 0, 0);
                }
            }
            releaseMediaRecorder(this.mMediaRecorder);
            this.mMediaRecorder = null;
            if (this.mCamera != null) {
                try {
                    this.mCamera.reconnect();
                } catch (IOException e) {
                    MLog.e("CameraManager", "IOException in CameraManager.releaseMediaRecorder", (Throwable) e);
                    if (this.mListener != null) {
                        this.mListener.onCameraError(1, e);
                    }
                } catch (RuntimeException e2) {
                    MLog.e("CameraManager", "RuntimeException in CameraManager.releaseMediaRecorder", (Throwable) e2);
                    if (this.mListener != null) {
                        this.mListener.onCameraError(1, e2);
                    }
                }
            }
            restoreRequestedOrientation();
        }
    }

    private void releaseMediaRecorder(MmsVideoRecorder mmsVideoRecorder) {
        if (mmsVideoRecorder != null) {
            mmsVideoRecorder.release();
        }
    }

    private void updateCameraOrientation() {
        if (this.mCamera != null && this.mCameraPreview != null && !this.mTakingPicture) {
            int orientation;
            int rotation;
            int degrees = 0;
            switch (((WindowManager) this.mCameraPreview.getContext().getSystemService("window")).getDefaultDisplay().getRotation()) {
                case 0:
                    degrees = 0;
                    break;
                case 1:
                    degrees = 90;
                    break;
                case 2:
                    degrees = 180;
                    break;
                case 3:
                    degrees = 270;
                    break;
            }
            if (this.mCameraInfo.facing == 1) {
                orientation = (this.mCameraInfo.orientation + degrees) % 360;
                rotation = orientation;
                orientation = (360 - orientation) % 360;
            } else {
                orientation = ((this.mCameraInfo.orientation - degrees) + 360) % 360;
                rotation = orientation;
            }
            this.mRotation = rotation;
            if (this.mMediaRecorder == null) {
                try {
                    this.mCamera.setDisplayOrientation(orientation);
                    final Parameters params = this.mCamera.getParameters();
                    params.setRotation(rotation);
                    mCameraManagerThread.execute(new Runnable() {
                        public void run() {
                            try {
                                if (CameraManager.this.mCamera != null) {
                                    CameraManager.this.mCamera.setParameters(params);
                                }
                            } catch (final Exception e) {
                                MLog.e("CameraManager", "Exception in CameraManager updateCameraOrientation mCameraManagerThread run.", (Throwable) e);
                                ThreadUtil.getMainThreadHandler().post(new Runnable() {
                                    public void run() {
                                        if (CameraManager.this.mListener != null) {
                                            CameraManager.this.mListener.onCameraError(1, e);
                                        }
                                    }
                                });
                            }
                        }
                    });
                } catch (RuntimeException e) {
                    MLog.e("CameraManager", "RuntimeException in CameraManager.updateCameraOrientation", (Throwable) e);
                    if (this.mListener != null) {
                        this.mListener.onCameraError(1, e);
                    }
                }
            }
        }
    }

    private void setCamera(Camera camera) {
        if (this.mCamera != camera) {
            releaseMediaRecorder(true);
            releaseCamera(this.mCamera);
            this.mCamera = camera;
            tryShowPreview();
            if (this.mListener != null) {
                this.mListener.onCameraChanged();
            }
        }
    }

    public void tryShowPreview() {
        boolean z = true;
        if (this.mCameraPreview == null || this.mCamera == null) {
            if (this.mOrientationHandler != null) {
                this.mOrientationHandler.disable();
                this.mOrientationHandler = null;
            }
            releaseMediaRecorder(true);
            this.mFocusOverlayManager.onPreviewStopped();
            return;
        }
        try {
            this.mCamera.stopPreview();
            updateCameraOrientation();
            Parameters params = this.mCamera.getParameters();
            Size pictureSize = chooseBestPictureSize();
            Size previewSize = chooseBestPreviewSize(pictureSize);
            params.setPreviewSize(previewSize.width, previewSize.height);
            params.setPictureSize(pictureSize.width, pictureSize.height);
            logCameraSize("Setting preview size: ", previewSize);
            logCameraSize("Setting picture size: ", pictureSize);
            this.mCameraPreview.setSize(previewSize, this.mCameraInfo.orientation);
            for (String focusMode : params.getSupportedFocusModes()) {
                if (TextUtils.equals(focusMode, "continuous-picture")) {
                    params.setFocusMode(focusMode);
                    break;
                }
            }
            this.mCamera.setParameters(params);
            this.mCameraPreview.startPreview(this.mCamera);
            this.mCamera.startPreview();
            this.mCamera.setAutoFocusMoveCallback(new AutoFocusMoveCallback() {
                public void onAutoFocusMoving(boolean start, Camera camera) {
                    CameraManager.this.mFocusOverlayManager.onAutoFocusMoving(start);
                }
            });
            this.mFocusOverlayManager.setParameters(this.mCamera.getParameters());
            FocusOverlayManager focusOverlayManager = this.mFocusOverlayManager;
            if (this.mCameraInfo.facing != 0) {
                z = false;
            }
            focusOverlayManager.setMirror(z);
            this.mFocusOverlayManager.onPreviewStarted();
            tryInitOrCleanupVideoMode();
            if (this.mOrientationHandler == null) {
                this.mOrientationHandler = new OrientationHandler(this.mCameraPreview.getContext());
                this.mOrientationHandler.enable();
            }
        } catch (IOException e) {
            MLog.e("CameraManager", "IOException in CameraManager.tryShowPreview", (Throwable) e);
            if (this.mListener != null) {
                this.mListener.onCameraError(2, e);
            }
        } catch (RuntimeException e2) {
            MLog.e("CameraManager", "RuntimeException in CameraManager.tryShowPreview", (Throwable) e2);
            if (this.mListener != null) {
                this.mListener.onCameraError(2, e2);
            }
        }
    }

    private void tryInitOrCleanupVideoMode() {
        if (!this.mVideoModeRequested || this.mCamera == null || this.mCameraPreview == null) {
            releaseMediaRecorder(true);
        } else if (this.mMediaRecorder == null) {
            try {
                int maxMessageSize = MmsConfig.getMaxMessageSize();
                if (this.mLimitSize > 0) {
                    maxMessageSize = this.mLimitSize;
                }
                this.mMediaRecorder = new MmsVideoRecorder(this.mCamera, this.mCameraIndex, this.mRotation, maxMessageSize);
                this.mMediaRecorder.prepare();
                tryStartVideoCapture();
            } catch (FileNotFoundException e) {
                MLog.e("CameraManager", "FileNotFoundException in CameraManager.tryInitOrCleanupVideoMode", (Throwable) e);
                if (this.mListener != null) {
                    this.mListener.onCameraError(4, e);
                }
                setVideoMode(false);
            } catch (IOException e2) {
                MLog.e("CameraManager", "IOException in CameraManager.tryInitOrCleanupVideoMode", (Throwable) e2);
                if (this.mListener != null) {
                    this.mListener.onCameraError(3, e2);
                }
                setVideoMode(false);
            } catch (RuntimeException e3) {
                MLog.e("CameraManager", "RuntimeException in CameraManager.tryInitOrCleanupVideoMode", (Throwable) e3);
                if (this.mListener != null) {
                    this.mListener.onCameraError(3, e3);
                }
                setVideoMode(false);
            }
        }
    }

    private void tryStartVideoCapture() {
        if (this.mMediaRecorder != null && this.mVideoCallback != null) {
            this.mMediaRecorder.setOnErrorListener(new OnErrorListener() {
                public void onError(MediaRecorder mediaRecorder, int what, int extra) {
                    if (CameraManager.this.mListener != null) {
                        CameraManager.this.mListener.onCameraError(5, null);
                    }
                    CameraManager.this.restoreRequestedOrientation();
                }
            });
            this.mMediaRecorder.setOnInfoListener(new OnInfoListener() {
                public void onInfo(MediaRecorder mediaRecorder, int what, int extra) {
                    if (what == 800 || what == 801) {
                        CameraManager.this.stopVideo();
                    }
                }
            });
            try {
                this.mCamera.unlock();
                this.mMediaRecorder.start();
                UiUtils.getActivity(this.mCameraPreview.getContext()).getWindow().addFlags(128);
                lockOrientation();
            } catch (IllegalStateException e) {
                MLog.e("CameraManager", "IllegalStateException in CameraManager.tryStartVideoCapture", (Throwable) e);
                if (this.mListener != null) {
                    this.mListener.onCameraError(5, e);
                }
                if (this.mMediaRecorder != null) {
                    this.mMediaRecorder.cleanupTempFile();
                }
                setVideoMode(false);
                restoreRequestedOrientation();
            } catch (RuntimeException e2) {
                MLog.e("CameraManager", "RuntimeException in CameraManager.tryStartVideoCapture", (Throwable) e2);
                if (this.mListener != null) {
                    this.mListener.onCameraError(5, e2);
                }
                if (this.mMediaRecorder != null) {
                    this.mMediaRecorder.cleanupTempFile();
                }
                setVideoMode(false);
                restoreRequestedOrientation();
            }
        }
    }

    void stopVideo() {
        int width = -1;
        int height = -1;
        Uri uri = null;
        String contentType = null;
        try {
            UiUtils.getActivity(this.mCameraPreview.getContext()).getWindow().clearFlags(128);
            this.mMediaRecorder.stop();
            this.mCamera.lock();
            width = this.mMediaRecorder.getVideoWidth();
            height = this.mMediaRecorder.getVideoHeight();
            uri = this.mMediaRecorder.getVideoUri();
            contentType = this.mMediaRecorder.getContentType();
        } catch (RuntimeException e) {
            MLog.e("CameraManager", "RuntimeException in CameraManager.stopVideo", (Throwable) e);
        } finally {
            MediaCallback videoCallback = this.mVideoCallback;
            this.mVideoCallback = null;
            releaseMediaRecorder(false);
            if (uri == null) {
                tryInitOrCleanupVideoMode();
            }
            videoCallback.onMediaReady(uri, contentType, width, height);
        }
    }

    boolean isCameraAvailable() {
        return (this.mCamera == null || this.mTakingPicture) ? false : this.mIsHardwareAccelerationSupported;
    }

    private Size chooseBestPictureSize() {
        return getSizeWithRatio(new ArrayList(this.mCamera.getParameters().getSupportedPictureSizes()), 1.3333333333333333d);
    }

    private Size chooseBestPreviewSize(Size pictureSize) {
        List<Size> sizes = new ArrayList(this.mCamera.getParameters().getSupportedPreviewSizes());
        Collections.sort(sizes, new SizeComparator(Integer.MAX_VALUE, Integer.MAX_VALUE, ((double) pictureSize.width) / ((double) pictureSize.height), pictureSize.width * pictureSize.height));
        return (Size) sizes.get(0);
    }

    private Size getSizeWithRatio(List<Size> sizes, double targetRatio) {
        if (sizes == null) {
            return null;
        }
        Size optimalSize = null;
        for (Size size : sizes) {
            if (Math.abs((((double) size.width) / ((double) size.height)) - targetRatio) <= 0.05d) {
                if (optimalSize == null) {
                    optimalSize = size;
                } else if (compareSize(optimalSize, size)) {
                    optimalSize = size;
                }
            }
        }
        return optimalSize;
    }

    private boolean compareSize(Size leftSize, Size rightSize) {
        int widthScreen = -1;
        if (!(this.mCameraPreview == null || this.mCameraPreview.getContext() == null)) {
            Context context = this.mCameraPreview.getContext();
            widthScreen = context.getResources().getConfiguration().orientation == 2 ? context.getResources().getDisplayMetrics().heightPixels : context.getResources().getDisplayMetrics().widthPixels;
        }
        if (leftSize == null || rightSize == null) {
            return false;
        }
        if (leftSize.width > rightSize.width || leftSize.height > rightSize.height) {
            if (leftSize.width < rightSize.width || leftSize.height < rightSize.height) {
                double chargeRatio = (((double) leftSize.width) / ((double) leftSize.height)) - (((double) rightSize.width) / ((double) rightSize.height));
                if (chargeRatio == 0.0d) {
                    return Math.abs(leftSize.width * leftSize.height) - Math.abs(rightSize.width * rightSize.height) <= 0;
                } else if (chargeRatio < 0.0d) {
                    return false;
                } else {
                    return true;
                }
            } else if (rightSize.width <= widthScreen || widthScreen <= 0) {
                return false;
            } else {
                return true;
            }
        } else if (leftSize.width <= widthScreen || widthScreen <= 0) {
            return true;
        } else {
            return false;
        }
    }

    public void autoFocus() {
        if (this.mCamera != null) {
            try {
                this.mCamera.autoFocus(new AutoFocusCallback() {
                    public void onAutoFocus(boolean success, Camera camera) {
                        CameraManager.this.mFocusOverlayManager.onAutoFocus(success, false);
                    }
                });
            } catch (RuntimeException e) {
                MLog.e("CameraManager", "RuntimeException in CameraManager.autoFocus", (Throwable) e);
                this.mFocusOverlayManager.onAutoFocus(false, false);
            }
        }
    }

    public void cancelAutoFocus() {
        if (this.mCamera != null) {
            try {
                this.mCamera.cancelAutoFocus();
            } catch (RuntimeException e) {
                MLog.e("CameraManager", "RuntimeException in CameraManager.cancelAutoFocus", (Throwable) e);
            }
        }
    }

    public boolean capture() {
        return false;
    }

    public void setFocusParameters() {
        if (this.mCamera != null) {
            try {
                Parameters parameters = this.mCamera.getParameters();
                parameters.setFocusMode(this.mFocusOverlayManager.getFocusMode());
                if (parameters.getMaxNumFocusAreas() > 0) {
                    parameters.setFocusAreas(this.mFocusOverlayManager.getFocusAreas());
                }
                parameters.setMeteringAreas(this.mFocusOverlayManager.getMeteringAreas());
                this.mCamera.setParameters(parameters);
            } catch (RuntimeException e) {
                MLog.e("CameraManager", "RuntimeException in CameraManager setFocusParameters");
            }
        }
    }

    private void logCameraSize(String prefix, Size size) {
        MLog.i("CameraManager", prefix + size.width + "x" + size.height + " (" + (((float) size.width) / ((float) size.height)) + ")");
    }

    private void lockOrientation() {
        Activity a = UiUtils.getActivity(this.mCameraPreview.getContext());
        int rotation = ((WindowManager) a.getSystemService("window")).getDefaultDisplay().getRotation();
        this.mSavedOrientation = Integer.valueOf(a.getRequestedOrientation());
        switch (rotation) {
            case 0:
                a.setRequestedOrientation(1);
                return;
            case 1:
                a.setRequestedOrientation(0);
                return;
            case 2:
                a.setRequestedOrientation(9);
                return;
            case 3:
                a.setRequestedOrientation(8);
                return;
            default:
                return;
        }
    }

    private void restoreRequestedOrientation() {
        if (this.mSavedOrientation != null && this.mCameraPreview != null) {
            Activity a = UiUtils.getActivity(this.mCameraPreview.getContext());
            if (a != null) {
                a.setRequestedOrientation(this.mSavedOrientation.intValue());
            }
            this.mSavedOrientation = null;
        }
    }

    static boolean hasCameraPermission() {
        return OsUtil.hasPermission("android.permission.CAMERA");
    }

    public MmsVideoRecorder getMmsVideoRecorder() {
        return this.mMediaRecorder;
    }

    public void releaseFoucsPieRender() {
        if (this.mFocusOverlayManager != null) {
            this.mFocusOverlayManager.setFocusRenderer(null);
        }
    }
}
