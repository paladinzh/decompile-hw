package com.android.mms.attachment.ui.mediapicker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.hardware.Camera.CameraInfo;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.StatFs;
import android.os.SystemClock;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.amap.api.services.core.AMapException;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsApp;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.attachment.ui.AsyncImageView;
import com.android.mms.attachment.ui.AttachmentPreviewFactory;
import com.android.mms.attachment.ui.mediapicker.CameraPreview.CameraPreviewHost;
import com.android.mms.attachment.ui.mediapicker.camerafocus.RenderOverlay;
import com.android.mms.attachment.utils.ThreadUtil;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.ConversationList;
import com.android.mms.ui.FragmentTag;
import com.android.mms.ui.MessageUtils;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.AudioManagerUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsCommon;
import com.huawei.mms.util.StatisticalHelper;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import com.huawei.rcs.utils.RcseMmsExt;

public class CameraMediaChooser extends MediaChooser implements CameraManagerListener {
    private View mAttachmentBtnView;
    private AsyncImageView mAttachmentOperatePicture;
    private View mAttachmentOperateView;
    private View mCameraPreviewBackground;
    private CameraPreviewHost mCameraPreviewHost;
    private View mCammeraSlideUpTip;
    private View mCaptureButton;
    private ImageView mCaptureIcon;
    private boolean mCurrentAddState = false;
    private Uri mCurrentAttachemntUri = null;
    private int mCurrentAttachmentType = -1;
    private View mEnabledView;
    private int mErrorToast;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2009:
                    Context context = MmsApp.getApplication().getApplicationContext();
                    if (context != null) {
                        if (CameraMediaChooser.this.isAvailableSpace(context)) {
                            if (CameraManager.get().isRecording()) {
                                CameraMediaChooser.this.continueMemoryCheck();
                                break;
                            }
                        }
                        CameraMediaChooser.this.stopMemoryCheck(context);
                        return;
                    }
                    return;
                    break;
                case 2010:
                    CameraMediaChooser.this.mCameraPreviewBackground.setVisibility(8);
                    break;
            }
        }
    };
    private int mHeight = -1;
    private boolean mIsBtnRotation = false;
    private Runnable mLoadVideoThumbnail = new Runnable() {
        public void run() {
            if (CameraMediaChooser.this.mCurrentAttachemntUri != null) {
                String videoPath = CameraMediaChooser.this.mCurrentAttachemntUri.getPath();
                if (!CameraMediaChooser.this.mMediaPicker.isDetached()) {
                    MessageUtils.addFileToIndex(CameraMediaChooser.this.mMediaPicker.getActivity(), videoPath);
                    try {
                        CameraMediaChooser.this.mVideoPicture = ThumbnailUtils.createVideoThumbnail(videoPath, 1);
                        if (CameraMediaChooser.this.mVideoPicture != null) {
                            CameraMediaChooser.this.mVideoPicture = RcsUtility.fixRotateBitmap(videoPath, CameraMediaChooser.this.mVideoPicture);
                            ThreadUtil.getMainThreadHandler().post(new Runnable() {
                                public void run() {
                                    if (CameraMediaChooser.this.mAttachmentOperatePicture != null) {
                                        CameraMediaChooser.this.mAttachmentOperatePicture.setImageBitmap(CameraMediaChooser.this.mVideoPicture);
                                    }
                                }
                            });
                        }
                    } catch (Exception e) {
                        MLog.e("CameraMeidaChooser", "loaded video thumbnail failed, error:" + e);
                    }
                }
            }
        }
    };
    private View mMissingPermissionView;
    private long mRequestTimeMillis = 0;
    private boolean mScrollFullScreenState = false;
    private ImageView mSwapCameraButton;
    private View mSwapModeButton;
    private ImageView mSwapModeIcon;
    private boolean mVideoCancelled;
    private Chronometer mVideoCounter;
    private View mVideoCounterLayout;
    private Bitmap mVideoPicture = null;
    private int mWidth = -1;
    private Button perimissionButton;

    private static class CameraAnimationListener implements AnimationListener {
        private View shutterVisual = null;

        public CameraAnimationListener(View shuttervisual) {
            this.shutterVisual = shuttervisual;
        }

        public void onAnimationStart(Animation animation) {
            if (this.shutterVisual != null) {
                this.shutterVisual.setVisibility(0);
            }
        }

        public void onAnimationEnd(Animation animation) {
            if (this.shutterVisual != null) {
                this.shutterVisual.setVisibility(8);
            }
        }

        public void onAnimationRepeat(Animation animation) {
        }
    }

    private class CameraListener implements OnTouchListener, OnClickListener {
        private CameraListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (CameraManager.get().isVideoMode()) {
                return true;
            }
            return false;
        }

        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.camera_swapCamera_button:
                    CameraManager.get().swapCamera();
                    CameraMediaChooser.this.updateViewState();
                    StatisticalHelper.incrementReportCount(CameraMediaChooser.this.getContext(), 2251);
                    return;
                default:
                    return;
            }
        }
    }

    private static class CameraMediaChooserOnTouchListener implements OnTouchListener {
        private CameraMediaChooserOnTouchListener() {
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (CameraManager.get().isVideoMode()) {
                return true;
            }
            return false;
        }
    }

    public CameraMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    public int getSupportedMediaTypes() {
        if (CameraManager.get().hasAnyCamera()) {
            return 3;
        }
        return 0;
    }

    public View destroyView() {
        CameraManager.get().closeCamera();
        CameraManager.get().setListener(null);
        CameraManager.destory();
        return super.destroyView();
    }

    private boolean isAvailableSpace(Context mContext) {
        if (!Environment.getExternalStorageState().equals("mounted")) {
            return false;
        }
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long availableSpare = (((long) statFs.getAvailableBlocks()) * ((long) statFs.getBlockSize())) / 1024;
        MLog.d("CameraMeidaChooser", "availableSpare = " + availableSpare);
        if (availableSpare > 10240) {
            return true;
        }
        return false;
    }

    protected View createView(ViewGroup container) {
        CameraManager.get().setListener(this);
        CameraManager.get().setVideoMode(false);
        CameraMediaChooserView view = (CameraMediaChooserView) getLayoutInflater().inflate(R.layout.mediapicker_camera_chooser, container, false);
        this.mCameraPreviewHost = (CameraPreviewHost) view.findViewById(R.id.camera_preview);
        this.mCameraPreviewBackground = view.findViewById(R.id.camera_preview_background);
        CameraListener cameraListener = new CameraListener();
        this.mCameraPreviewHost.getView().setOnTouchListener(cameraListener);
        this.mCameraPreviewHost.getView().setVisibility(8);
        final View shutterVisual = view.findViewById(R.id.camera_shutter_visual);
        this.mSwapCameraButton = (ImageView) view.findViewById(R.id.camera_swapCamera_button);
        this.mSwapCameraButton.setOnClickListener(cameraListener);
        this.mAttachmentBtnView = view.findViewById(R.id.camera_button_container);
        this.mCaptureButton = view.findViewById(R.id.camera_capture_button);
        this.mCaptureIcon = (ImageView) view.findViewById(R.id.camera_capture_button_icon);
        this.mCaptureButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                float heightPercent = Math.min(((float) CameraMediaChooser.this.mMediaPicker.getViewPager().getHeight()) / ((float) CameraMediaChooser.this.mCameraPreviewHost.getView().getHeight()), ContentUtil.FONT_SIZE_NORMAL);
                if (CameraManager.get().isRecording()) {
                    CameraManager.get().stopVideo();
                    AudioManagerUtils.abandonAudioFocus(CameraMediaChooser.this.getContext());
                } else {
                    MediaCallback callback = new MediaCallback() {
                        public void onMediaReady(Uri uriToVideo, String contentType, int width, int height) {
                            CameraMediaChooser.this.mVideoCounter.stop();
                            if (CameraMediaChooser.this.mVideoCancelled || uriToVideo == null) {
                                CameraMediaChooser.this.mVideoCancelled = false;
                                CameraManager.get().tryShowPreview();
                                CameraManager.get().updateVideoMode(true);
                            } else {
                                int i;
                                Rect startRect = new Rect();
                                if (CameraMediaChooser.this.mView != null) {
                                    CameraMediaChooser.this.mView.getGlobalVisibleRect(startRect);
                                }
                                CameraMediaChooser cameraMediaChooser = CameraMediaChooser.this;
                                if ("image/jpeg".equals(contentType)) {
                                    i = AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS;
                                } else {
                                    i = 1211;
                                }
                                cameraMediaChooser.mCurrentAttachmentType = i;
                                CameraMediaChooser.this.mCurrentAttachemntUri = uriToVideo;
                                CameraMediaChooser.this.mCurrentAddState = true;
                                CameraMediaChooser.this.mWidth = width;
                                CameraMediaChooser.this.mHeight = height;
                            }
                            CameraMediaChooser.this.updateViewState();
                        }

                        public void onMediaFailed(Exception exception) {
                            CameraMediaChooser.this.updateViewState();
                        }

                        public void onMediaInfo(int what) {
                            CameraMediaChooser.this.updateViewState();
                        }
                    };
                    if (CameraManager.get().isVideoMode()) {
                        if (!CameraMediaChooser.this.isAvailableSpace(CameraMediaChooser.this.getContext())) {
                            callback.onMediaInfo(0);
                            Toast.makeText(CameraMediaChooser.this.getContext(), R.string.storage_warning_title, 1).show();
                        } else if (AudioManagerUtils.isTelephonyCalling(CameraMediaChooser.this.getContext())) {
                            Toast.makeText(CameraMediaChooser.this.getContext(), R.string.record_video_in_calling_toast, 1).show();
                        } else {
                            if (CameraManager.get().getMmsVideoRecorder() == null) {
                                CameraManager.get().updateVideoMode(false);
                                CameraManager.get().setVideoMode(true);
                            }
                            CameraManager.get().startVideo(callback);
                            CameraMediaChooser.this.mVideoCounter.setBase(SystemClock.elapsedRealtime());
                            CameraMediaChooser.this.mVideoCounter.start();
                            CameraMediaChooser.this.updateViewState();
                            AudioManagerUtils.requestAudioManagerFocus(MmsApp.getApplication().getApplicationContext(), 2);
                            CameraMediaChooser.this.mHandler.sendEmptyMessageDelayed(2009, 500);
                            StatisticalHelper.incrementReportCount(CameraMediaChooser.this.getContext(), 2250);
                        }
                    } else if (CameraMediaChooser.this.isAvailableSpace(CameraMediaChooser.this.getContext())) {
                        CameraMediaChooser.this.showShutterEffect(shutterVisual);
                        CameraManager.get().takePicture(heightPercent, callback);
                        CameraMediaChooser.this.updateViewState();
                        StatisticalHelper.incrementReportCount(CameraMediaChooser.this.getContext(), 2249);
                    } else {
                        callback.onMediaInfo(0);
                        Toast.makeText(CameraMediaChooser.this.getContext(), R.string.storage_warning_title, 1).show();
                    }
                }
            }
        });
        this.mSwapModeButton = view.findViewById(R.id.camera_swap_mode_button);
        this.mSwapModeIcon = (ImageView) view.findViewById(R.id.camera_swap_mode_button_icon);
        this.mSwapModeButton.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                if (!(!CameraManager.get().isVideoMode()) || OsUtil.hasRecordAudioPermission()) {
                    CameraMediaChooser.this.onSwapMode();
                } else {
                    CameraMediaChooser.this.requestRecordAudioPermission();
                }
            }
        });
        this.mAttachmentOperateView = view.findViewById(R.id.camera_btn_operate_container);
        this.mAttachmentOperatePicture = (AsyncImageView) view.findViewById(R.id.camera_picture);
        View attachmentCancleView = this.mAttachmentOperateView.findViewById(R.id.camera_picture_btn_cancle);
        View attachmentSubmitView = this.mAttachmentOperateView.findViewById(R.id.camera_picture_btn_submit);
        attachmentCancleView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                CameraMediaChooser.this.mCurrentAttachmentType = -1;
                CameraMediaChooser.this.mCurrentAttachemntUri = null;
                CameraMediaChooser.this.mCurrentAddState = false;
                CameraMediaChooser.this.updateViewState();
                CameraManager.get().tryShowPreview();
            }
        });
        attachmentSubmitView.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                CameraMediaChooser.this.submitAttachement();
            }
        });
        this.mVideoCounterLayout = view.findViewById(R.id.camera_video_counter_layout);
        this.mVideoCounter = (Chronometer) view.findViewById(R.id.camera_video_counter);
        CameraManager.get().setRenderOverlay((RenderOverlay) view.findViewById(R.id.focus_visual));
        this.mEnabledView = view.findViewById(R.id.mediapicker_enabled);
        this.mMissingPermissionView = view.findViewById(R.id.missing_permission_view);
        this.perimissionButton = (Button) view.findViewById(R.id.request_perimission_btn);
        this.perimissionButton.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                CameraMediaChooser.this.requestCameraPermission();
            }
        });
        this.mCammeraSlideUpTip = view.findViewById(R.id.textview_slideup_tip);
        if (this.mMediaPicker.isInLandscape()) {
            this.mCammeraSlideUpTip.setVisibility(8);
        } else {
            HwBackgroundLoader.getUIHandler().postDelayed(new Runnable() {
                public void run() {
                    CameraMediaChooser.this.mCammeraSlideUpTip.setVisibility(8);
                }
            }, 2500);
        }
        setViewOntouchEvent(view);
        this.mView = view;
        updateViewState();
        updateForPermissionState(CameraManager.hasCameraPermission());
        return view;
    }

    private void setViewOntouchEvent(CameraMediaChooserView view) {
        view.setOnTouchListener(new CameraMediaChooserOnTouchListener());
    }

    public int getIconResource() {
        return this.mSelected ? R.drawable.ic_attachment_tab_camera_checked : R.drawable.ic_attachment_tab_camera;
    }

    protected int getIconTextResource() {
        return R.string.attach_take_photo;
    }

    protected void onFullScreenChanged(boolean fullScreen) {
        super.onFullScreenChanged(fullScreen);
        if (fullScreen) {
            setScrollFullScreenState(fullScreen);
        }
        if (!fullScreen && CameraManager.get().isVideoMode()) {
            CameraManager.get().setVideoMode(false);
        }
        updateViewState();
    }

    protected void onOpenedChanged(boolean open) {
        super.onOpenedChanged(open);
        updateViewState();
        if (!open) {
            setCameraPreviewHostVisibility(false);
        }
    }

    protected void setSelected(boolean selected) {
        super.setSelected(selected);
        if (selected) {
            setCameraPreviewHostVisibility(true);
            if (CameraManager.hasCameraPermission()) {
                showErrorToastIfNeeded();
                return;
            }
            return;
        }
        setCameraPreviewHostVisibility(false);
    }

    public void onResume() {
        updateForPermissionState(hasPermission("android.permission.CAMERA"));
    }

    private void requestCameraPermission() {
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(this.mMediaPicker.getActivity(), new String[]{"android.permission.CAMERA"}, 1);
    }

    private void requestRecordAudioPermission() {
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(this.mMediaPicker.getActivity(), new String[]{"android.permission.RECORD_AUDIO"}, 3);
    }

    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        boolean z = false;
        if (grantResults == null || grantResults.length == 0) {
            MLog.d("CameraMeidaChooser", "onRequestPermissionsResult grantResults is invaild.");
            return;
        }
        long currentTimeMillis = SystemClock.elapsedRealtime();
        int permissionGranted = grantResults[0];
        if (requestCode == 1) {
            if (permissionGranted == 0) {
                z = true;
            }
            updateForPermissionState(z);
            if (permissionGranted == 0) {
                this.mCameraPreviewHost.onCameraPermissionGranted();
                StatisticalHelper.incrementReportCount(getContext(), 2258);
            } else if (permissionGranted == -1 && currentTimeMillis - this.mRequestTimeMillis < 500) {
                gotoPackageSettings(this.mMediaPicker.getActivity(), 1);
            }
        } else if (requestCode == 3) {
            if (permissionGranted == 0) {
                onSwapMode();
                StatisticalHelper.incrementReportCount(getContext(), 2259);
            } else if (permissionGranted == -1 && currentTimeMillis - this.mRequestTimeMillis < 500) {
                gotoPackageSettings(this.mMediaPicker.getActivity(), 3);
            }
        }
    }

    public static void gotoPackageSettings(Activity act, int requestCode) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + act.getPackageName()));
        intent.setFlags(268435456);
        act.startActivityForResult(intent, requestCode);
    }

    private boolean hasPermission(String perm) {
        return this.mMediaPicker.getContext().checkSelfPermission(perm) == 0;
    }

    private void updateForPermissionState(boolean granted) {
        int i = 8;
        if (this.mEnabledView != null) {
            int i2;
            View view = this.mEnabledView;
            if (granted) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            view.setVisibility(i2);
            View view2 = this.mMissingPermissionView;
            if (!granted) {
                i = 0;
            }
            view2.setVisibility(i);
            if (granted && this.mMediaPicker != null && this.mMediaPicker.isOpen()) {
                setCameraPreviewHostVisibility(true);
            }
        }
    }

    public boolean canSwipeDown() {
        if (CameraManager.get().isVideoMode()) {
            return true;
        }
        return super.canSwipeDown();
    }

    public void onCameraError(int errorCode, Exception e) {
        switch (errorCode) {
            case 1:
            case 2:
            case 7:
                break;
            case 3:
                CameraManager.get().releaseVideoErrorFile();
                updateViewState();
                break;
            case 4:
                updateViewState();
                break;
            default:
                MLog.w("CameraMeidaChooser", "Unknown camera error:" + errorCode);
                break;
        }
        showErrorToastIfNeeded();
    }

    private void showErrorToastIfNeeded() {
        if (this.mErrorToast != 0 && this.mSelected) {
            this.mErrorToast = 0;
        }
    }

    public void onCameraChanged() {
        updateViewState();
    }

    private void onSwapMode() {
        boolean z;
        if (CameraManager.get().isVideoMode()) {
            CameraManager.get().setLimitSize(-1);
        } else if (getCurrentLimitSize() <= 10240) {
            Toast.makeText(this.mMediaPicker.getContext(), R.string.attachment_vedio_limitsize_toast, 1).show();
            updateViewState();
            StatisticalHelper.incrementReportCount(getContext(), 2252);
            return;
        } else {
            CameraManager.get().setLimitSize(getCurrentLimitSize());
        }
        CameraManager cameraManager = CameraManager.get();
        if (CameraManager.get().isVideoMode()) {
            z = false;
        } else {
            z = true;
        }
        cameraManager.setVideoMode(z);
        if (CameraManager.get().isVideoMode()) {
            this.mMediaPicker.setFullScreen(true);
        }
        updateViewState();
    }

    private void showShutterEffect(View shutterVisual) {
        float maxAlpha = getContext().getResources().getFraction(R.fraction.camera_shutter_max_alpha, 1, 1);
        int animationDuration = getContext().getResources().getInteger(R.integer.camera_shutter_duration) / 2;
        AnimationSet animation = new AnimationSet(false);
        Animation alphaInAnimation = new AlphaAnimation(0.0f, maxAlpha);
        alphaInAnimation.setDuration((long) animationDuration);
        animation.addAnimation(alphaInAnimation);
        Animation alphaOutAnimation = new AlphaAnimation(maxAlpha, 0.0f);
        alphaOutAnimation.setStartOffset((long) animationDuration);
        alphaOutAnimation.setDuration((long) animationDuration);
        animation.addAnimation(alphaOutAnimation);
        animation.setAnimationListener(new CameraAnimationListener(shutterVisual));
        shutterVisual.startAnimation(animation);
    }

    private void updateViewState() {
        if (this.mView != null) {
            Context context = getContext();
            if (context != null) {
                int i;
                boolean fullScreen = this.mMediaPicker.isFullScreen();
                boolean videoMode = CameraManager.get().isVideoMode();
                boolean isRecording = CameraManager.get().isRecording();
                boolean isCameraAvailable = isCameraAvailable();
                boolean isCameraFacingFront = CameraManager.get().isCameraFront();
                CameraInfo cameraInfo = CameraManager.get().getCameraInfo();
                if (cameraInfo == null || cameraInfo.facing != 1) {
                }
                View view = this.mView;
                if (fullScreen) {
                    i = 1;
                } else {
                    i = 0;
                }
                view.setSystemUiVisibility(i);
                ImageView imageView = this.mSwapCameraButton;
                i = (this.mCurrentAddState || isRecording || !CameraManager.get().hasFrontAndBackCamera()) ? 8 : 0;
                imageView.setVisibility(i);
                this.mSwapCameraButton.setEnabled(isCameraAvailable);
                this.mSwapCameraButton.setContentDescription(context.getString(isCameraFacingFront ? R.string.camera_switch_camera_face_back : R.string.camera_switch_camera_face_front));
                view = this.mVideoCounterLayout;
                i = (this.mCurrentAddState || !isRecording) ? 8 : 0;
                view.setVisibility(i);
                imageView = this.mSwapModeIcon;
                if (videoMode) {
                    i = R.drawable.ic_attachment_swape_camera;
                } else {
                    i = R.drawable.ic_attachment_swape_video;
                }
                imageView.setImageResource(i);
                this.mSwapModeButton.setContentDescription(context.getString(videoMode ? R.string.camera_switch_to_still_mode : R.string.camera_switch_to_video_mode));
                view = this.mSwapModeButton;
                i = (this.mCurrentAddState || isRecording) ? 8 : 0;
                view.setVisibility(i);
                this.mSwapModeButton.setBackgroundResource(R.drawable.bg_camera_switchbutton_background);
                this.mSwapModeButton.setEnabled(isCameraAvailable);
                if (isRecording) {
                    this.mCaptureIcon.setImageResource(R.drawable.bg_camera_shutterbutton_videoing);
                    this.mCaptureButton.setContentDescription(context.getString(R.string.camera_stop_recording));
                } else if (videoMode) {
                    this.mCaptureIcon.setImageResource(R.drawable.bg_camera_shutterbutton_video);
                    this.mCaptureButton.setContentDescription(context.getString(R.string.camera_start_recording));
                } else {
                    this.mCaptureIcon.setImageResource(R.drawable.bg_camera_shutterbutton_camera);
                    this.mCaptureButton.setContentDescription(context.getString(R.string.camera_take_picture));
                }
                if (!this.mCurrentAddState && isRecording) {
                    setVedioCounterRotaion();
                }
                this.mCaptureButton.setVisibility(!this.mCurrentAddState ? 0 : 8);
                this.mCaptureButton.setEnabled(isCameraAvailable);
                this.mAttachmentBtnView.setVisibility(!this.mCurrentAddState ? 0 : 8);
                this.mAttachmentOperateView.setVisibility(this.mCurrentAddState ? 0 : 8);
                if (this.mAttachmentOperateView.getVisibility() == 0 && this.mCurrentAttachemntUri != null) {
                    if (this.mCurrentAttachmentType == AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS) {
                        this.mWidth = getContext().getResources().getDisplayMetrics().widthPixels;
                        Options options = new Options();
                        options.inJustDecodeBounds = true;
                        BitmapFactory.decodeFile(this.mCurrentAttachemntUri.getPath(), options);
                        options.inJustDecodeBounds = false;
                        this.mHeight = (this.mWidth * options.outHeight) / options.outWidth;
                        this.mAttachmentOperatePicture.setImageResourceId(AttachmentPreviewFactory.getImageRequestDescriptor(this.mCurrentAttachemntUri, this.mWidth, this.mHeight, options.outWidth, options.outHeight, false));
                    } else if (this.mCurrentAttachmentType == 1211) {
                        new Thread(this.mLoadVideoThumbnail).start();
                    }
                }
            }
        }
    }

    protected int getActionBarTitleResId() {
        return 0;
    }

    protected void onRestoreChooserState() {
        setSelected(false);
        this.mCurrentAddState = false;
        this.mCurrentAttachemntUri = null;
        this.mCurrentAttachmentType = -1;
        this.mWidth = -1;
        this.mHeight = -1;
        updateViewState();
    }

    private boolean isCameraAvailable() {
        return CameraManager.get().isCameraAvailable();
    }

    private AttachmentSelectData createAttachmentData(int attachmentType, Uri uriItem) {
        AttachmentSelectData attachmentItem = new AttachmentSelectData(attachmentType);
        attachmentItem.setAttachmentUri(uriItem);
        return attachmentItem;
    }

    public void setScrollFullScreenState(boolean scrollFullScreenState) {
        this.mScrollFullScreenState = scrollFullScreenState;
    }

    public boolean getScrollFullScreenState() {
        return this.mScrollFullScreenState;
    }

    private void setVedioCounterRotaion() {
        if (this.mMediaPicker.getResources().getConfiguration().orientation == 2) {
            if (!this.mIsBtnRotation) {
                Animation animation = AnimationUtils.loadAnimation(this.mMediaPicker.getActivity(), R.anim.vedio_counter_rotation);
                this.mVideoCounterLayout.setAnimation(animation);
                animation.start();
                this.mIsBtnRotation = true;
            }
        } else if (this.mIsBtnRotation) {
            this.mIsBtnRotation = false;
        }
    }

    public void onPause() {
        if (CameraManager.get().isRecording()) {
            CameraManager.get().stopVideo();
            submitAttachement();
        }
        super.onPause();
    }

    private void submitAttachement() {
        this.mMediaPicker.dispatchItemsSelected(createAttachmentData(this.mCurrentAttachmentType, this.mCurrentAttachemntUri), true);
        StatisticalHelper.incrementReportCount(getContext(), this.mCurrentAttachmentType == AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS ? 2253 : 2254);
        this.mCurrentAttachmentType = -1;
        this.mCurrentAttachemntUri = null;
        this.mCurrentAddState = false;
        updateViewState();
        CameraManager.get().tryShowPreview();
        this.mMediaPicker.setFullScreen(false);
    }

    private int getCurrentLimitSize() {
        if (this.mMediaPicker != null) {
            if (HwMessageUtils.isSplitOn() && !(this.mMediaPicker.getActivity() instanceof ComposeMessageActivity)) {
                HwBaseFragment fragment = ((ConversationList) this.mMediaPicker.getActivity()).getRightFragment();
                if (fragment != null) {
                    if (((ConversationList) this.mMediaPicker.getActivity()).getRightFragment() instanceof RcsGroupChatComposeMessageFragment) {
                        return (int) ((RcsGroupChatComposeMessageFragment) fragment).getRichEditor().computeAddRecordSizeLimit();
                    }
                    return (int) (((ComposeMessageFragment) fragment).getRichEditor().computeAddRecordSizeLimit() - 5120);
                }
            } else if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
                if (fragment != null) {
                    return ((int) ((RcsGroupChatComposeMessageFragment) fragment).getRichEditor().computeAddRecordSizeLimit()) * Place.TYPE_SUBLOCALITY_LEVEL_2;
                }
            } else {
                fragment = FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
                if (fragment != null) {
                    if (RcseMmsExt.isRcsMode()) {
                        return RcsTransaction.getWarFileSizePermitedValue() * Place.TYPE_SUBLOCALITY_LEVEL_2;
                    }
                    return (int) (((ComposeMessageFragment) fragment).getRichEditor().computeAddRecordSizeLimit() - 5120);
                }
            }
        }
        return -1;
    }

    public void onStop() {
        setCameraPreviewHostVisibility(false);
    }

    private void stopMemoryCheck(Context context) {
        Toast.makeText(context, R.string.storage_warning_title, 1).show();
        CameraManager.get().stopVideo();
        AudioManagerUtils.abandonAudioFocus(context);
        if (this.mHandler != null) {
            this.mHandler.removeMessages(2009);
        }
    }

    private void continueMemoryCheck() {
        if (this.mHandler != null) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2009), 500);
        }
    }

    public void setCameraPreviewHostVisibility(boolean isVisibility) {
        if (this.mCameraPreviewHost != null && this.mCameraPreviewHost.getView() != null) {
            this.mHandler.removeMessages(2010);
            if (isVisibility && this.mMediaPicker != null && this.mMediaPicker.isOpen() && this.mMediaPicker.getIsCameraChooser()) {
                this.mCameraPreviewHost.getView().setVisibility(0);
                this.mHandler.sendEmptyMessageDelayed(2010, 800);
            } else {
                this.mCameraPreviewBackground.setVisibility(0);
                this.mCameraPreviewHost.getView().setVisibility(8);
            }
        }
    }
}
