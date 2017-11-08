package com.android.systemui.screenshot;

import android.app.KeyguardManager;
import android.app.StatusBarManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PointF;
import android.media.AudioManager;
import android.media.SoundPool;
import android.media.SoundPool.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.HwSecureWaterMark;
import android.view.MotionEvent;
import android.view.SurfaceControl;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.lazymode.SlideTouchEvent;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class HwGlobalScreenshot extends GlobalScreenshot {
    private AudioManager mAudioManager;
    private OnClickListener mEditClickListener = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i("HwGlobalScreenshot", "click edit button");
            HwGlobalScreenshot.this.handleActionClickAsync(1, 40);
        }
    };
    private TextView mHwEditButton;
    private ImageView mHwEditImageView;
    private LinearLayout mHwEditLinearLayout;
    private LinearLayout mHwScreenshotContainer;
    private TextView mHwScrollButton;
    private ImageView mHwScrollImageView;
    private LinearLayout mHwScrollLinearLayout;
    private TextView mHwShareButton;
    private ImageView mHwShareImageView;
    private LinearLayout mHwShareLinearLayout;
    private boolean mIsLoadComplete = false;
    private SoundPool mPlayer;
    private AtomicInteger mPostAction = new AtomicInteger(-1);
    private OnClickListener mScrollScreenClickListener = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i("HwGlobalScreenshot", "click scroll screen button");
            HwGlobalScreenshot.this.handleActionClickAsync(2, 42);
        }
    };
    private OnClickListener mShareClickListener = new OnClickListener() {
        public void onClick(View v) {
            HwLog.i("HwGlobalScreenshot", "click share button");
            HwGlobalScreenshot.this.handleActionClickAsync(0, 41);
        }
    };
    private int mSoundId;
    private Handler mSubHandler;

    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                case 1:
                case 2:
                    HwLog.i("HwGlobalScreenshot", "handleMessage code: " + msg.what);
                    ((StatusBarManager) HwGlobalScreenshot.this.mContext.getSystemService("statusbar")).collapsePanels();
                    if (HwGlobalScreenshot.this.mSaveInBgTask != null && (HwGlobalScreenshot.this.mSaveInBgTask instanceof HwSaveImageTaskItf)) {
                        ((HwSaveImageTaskItf) HwGlobalScreenshot.this.mSaveInBgTask).onButtonClicked(msg.what);
                        return;
                    }
                    return;
                default:
                    HwLog.e("HwGlobalScreenshot", "handleMessage invalid code: " + msg.what);
                    return;
            }
        }
    }

    public HwGlobalScreenshot(Context context) {
        super(context);
        setHwEmuiTheme();
        initHwScreenshotLayout();
        initHwScreenshotSound();
        HandlerThread subThread = new HandlerThread("HwGlobalScreenshot");
        subThread.start();
        this.mSubHandler = new MyHandler(subThread.getLooper());
    }

    public void resetMembersIfNeeded() {
        boolean provisioned;
        int i = 0;
        this.mPostAction.set(-1);
        if (Global.getInt(this.mContext.getContentResolver(), "device_provisioned", 0) != 0) {
            provisioned = true;
        } else {
            provisioned = false;
        }
        if (provisioned) {
            this.mHwShareLinearLayout.setEnabled(true);
            this.mHwShareButton.setAlpha(0.6f);
            this.mHwShareImageView.setAlpha(1.0f);
            this.mHwShareButton.setTextColor(this.mContext.getColor(R.color.global_screenshot_btn_text_color));
            this.mHwEditLinearLayout.setEnabled(true);
            this.mHwEditButton.setAlpha(0.6f);
            this.mHwEditImageView.setAlpha(1.0f);
            this.mHwEditButton.setTextColor(this.mContext.getColor(R.color.global_screenshot_btn_text_color));
            this.mHwScrollLinearLayout.setEnabled(true);
            this.mHwScrollButton.setAlpha(0.6f);
            this.mHwScrollImageView.setAlpha(1.0f);
            this.mHwScrollButton.setTextColor(this.mContext.getColor(R.color.global_screenshot_btn_text_color));
        } else {
            this.mHwShareLinearLayout.setEnabled(false);
            this.mHwShareButton.setAlpha(0.2f);
            this.mHwShareImageView.setAlpha(0.2f);
            this.mHwShareButton.setTextColor(-7829368);
            this.mHwEditLinearLayout.setEnabled(false);
            this.mHwEditButton.setAlpha(0.2f);
            this.mHwEditImageView.setAlpha(0.2f);
            this.mHwEditButton.setTextColor(-7829368);
            this.mHwScrollLinearLayout.setEnabled(false);
            this.mHwScrollButton.setAlpha(0.2f);
            this.mHwScrollImageView.setAlpha(0.2f);
            this.mHwScrollButton.setTextColor(-7829368);
        }
        LinearLayout linearLayout = this.mHwScrollLinearLayout;
        if (!hasIntentService()) {
            i = 8;
        }
        linearLayout.setVisibility(i);
        this.mHwShareButton.setText(R.string.screenshot_share);
        this.mHwEditButton.setText(R.string.screenshot_edit);
        this.mHwScrollButton.setText(R.string.scroll_screenshot);
    }

    public Bitmap getScreenshotBitmap(int width, int height) {
        Bitmap bitmap;
        if (SlideTouchEvent.isLazyMode(this.mContext)) {
            bitmap = SurfaceControl.screenshot(SlideTouchEvent.getScreenshotRect(this.mContext), ((int) (((float) width) * 0.75f)) & -16, ((int) (((float) height) * 0.75f)) & -16, 0, -1, false, 0);
        } else {
            bitmap = SurfaceControl.screenshot(width, height);
        }
        return addWaterMarkToScreenShot(bitmap);
    }

    private Bitmap addWaterMarkToScreenShot(Bitmap srcBitmap) {
        if (srcBitmap == null) {
            HwLog.e("HwGlobalScreenshot", "getScreenshotBitmap null return!");
            return srcBitmap;
        }
        if (HwSecureWaterMark.isWatermarkEnable()) {
            Bitmap tmp = HwSecureWaterMark.addWatermark(srcBitmap);
            if (tmp != null) {
                HwLog.e("HwGlobalScreenshot", "addWaterMarkToScreenShot success!");
                return tmp;
            }
            HwLog.e("HwGlobalScreenshot", "addWaterMarkToScreenShot null return!");
        } else {
            HwLog.d("HwGlobalScreenshot", "addWaterMarkToScreenShot watermark not enabled");
        }
        return srcBitmap;
    }

    public void preAnimationStart() {
        this.mScreenshotView.setImageBitmap(Bitmap.createScaledBitmap(this.mScreenBitmap, (this.mScreenBitmap.getWidth() * 78) / 100, (this.mScreenBitmap.getHeight() * 78) / 100, false));
    }

    public void playSoundAndSetViewLayer() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                HwGlobalScreenshot.this.readyToPlaySound();
                return false;
            }
        });
        this.mHwScreenshotContainer.setLayerType(2, null);
        this.mHwScreenshotContainer.buildLayer();
    }

    private void readyToPlaySound() {
        int count = 0;
        while (!this.mIsLoadComplete) {
            SystemClock.sleep(100);
            count++;
            if (count >= 10) {
                return;
            }
        }
        playSound();
    }

    public void onDropInAnimationStart() {
        this.mHwScreenshotContainer.setAlpha(0.0f);
        this.mHwScreenshotContainer.setTranslationX(0.0f);
        this.mHwScreenshotContainer.setTranslationY(0.0f);
        this.mHwScreenshotContainer.setScaleX(((this.mBgPaddingScale + 1.0f) * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setScaleY(((this.mBgPaddingScale + 1.0f) * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setVisibility(0);
    }

    public void onDropInAnimationUpdate(float t, float scaleT) {
        this.mHwScreenshotContainer.setAlpha(t);
        this.mHwScreenshotContainer.setScaleX((scaleT * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setScaleY((scaleT * 100.0f) / 78.0f);
    }

    public void onDropOutAnimationEnd() {
        this.mHwScreenshotContainer.setVisibility(8);
        this.mHwScreenshotContainer.setLayerType(0, null);
    }

    public void onDropOutAnimationUpdateWithOneBarInvisible(float t, float scaleT) {
        this.mHwScreenshotContainer.setAlpha(1.0f - t);
        this.mHwScreenshotContainer.setScaleX((scaleT * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setScaleY((scaleT * 100.0f) / 78.0f);
    }

    public void onDropOutAnimationUpdateWithAllBarVisible(float t, float scaleT, Interpolator scaleInterpolator, PointF finalPos) {
        this.mHwScreenshotContainer.setAlpha(1.0f - scaleInterpolator.getInterpolation(t));
        this.mHwScreenshotContainer.setScaleX((scaleT * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setScaleY((scaleT * 100.0f) / 78.0f);
        this.mHwScreenshotContainer.setTranslationX(finalPos.x * t);
        this.mHwScreenshotContainer.setTranslationY(finalPos.y * t);
    }

    void takeScreenshot(Runnable finisher, boolean statusBarVisible, boolean navBarVisible) {
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            HwLog.i("HwGlobalScreenshot", "can not take screen shot in super power mode!");
            finisher.run();
            return;
        }
        super.takeScreenshot(finisher, statusBarVisible, navBarVisible);
    }

    void saveScreenshotInWorkerThread(Runnable finisher) {
        if (this.mScreenBitmap == null) {
            HwLog.e("HwGlobalScreenshot", "mScreenBitmap == null");
            return;
        }
        SaveImageInBackgroundData data = new SaveImageInBackgroundData();
        data.context = this.mContext;
        data.image = this.mScreenBitmap;
        data.iconSize = this.mNotificationIconSize;
        data.finisher = finisher;
        data.previewWidth = this.mPreviewWidth;
        data.previewheight = this.mPreviewHeight;
        if (this.mSaveInBgTask != null) {
            this.mSaveInBgTask.cancel(false);
        }
        this.mSaveInBgTask = new HwSaveImageInBackgroundTask(this.mContext, data, this.mNotificationManager, R.id.notification_screenshot).execute(new Void[0]);
    }

    private void setHwEmuiTheme() {
        int themeID = this.mContext.getApplicationContext().getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeID != 0) {
            this.mContext.getApplicationContext().setTheme(themeID);
        }
    }

    private void initHwScreenshotLayout() {
        this.mHwShareButton = (TextView) this.mScreenshotLayout.findViewById(R.id.screenshot_share_button);
        this.mHwEditButton = (TextView) this.mScreenshotLayout.findViewById(R.id.screenshot_edit_button);
        this.mHwScrollButton = (TextView) this.mScreenshotLayout.findViewById(R.id.screenshot_scroll_button);
        this.mHwShareImageView = (ImageView) this.mScreenshotLayout.findViewById(R.id.share_image);
        this.mHwEditImageView = (ImageView) this.mScreenshotLayout.findViewById(R.id.edit_image);
        this.mHwScrollImageView = (ImageView) this.mScreenshotLayout.findViewById(R.id.scroll_image);
        this.mHwShareLinearLayout = (LinearLayout) this.mScreenshotLayout.findViewById(R.id.sharelinearlayout);
        this.mHwEditLinearLayout = (LinearLayout) this.mScreenshotLayout.findViewById(R.id.editlinearlayout);
        this.mHwScrollLinearLayout = (LinearLayout) this.mScreenshotLayout.findViewById(R.id.scrollscreenlinearlayout);
        this.mHwScreenshotContainer = (LinearLayout) this.mScreenshotLayout.findViewById(R.id.global_screenshot_linear_layout);
        this.mHwShareLinearLayout.setOnClickListener(this.mShareClickListener);
        this.mHwEditLinearLayout.setOnClickListener(this.mEditClickListener);
        this.mHwScrollLinearLayout.setOnClickListener(this.mScrollScreenClickListener);
        this.mScreenshotView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mBackgroundView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                HwGlobalScreenshot.this.cancelAnimation();
                return true;
            }
        });
    }

    private void initHwScreenshotSound() {
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mPlayer = new SoundPool(1, 7, 0);
        this.mSoundId = this.mPlayer.load("/system/media/audio/ui/camera_click.ogg", 1);
        setOnLoadCompleteListener();
    }

    public void setOnLoadCompleteListener() {
        this.mPlayer.setOnLoadCompleteListener(new OnLoadCompleteListener() {
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                HwGlobalScreenshot.this.mIsLoadComplete = true;
            }
        });
    }

    private void playSound() {
        HwLog.i("HwGlobalScreenshot", "playSound run...");
        int playCameraSound = System.getInt(this.mContext.getContentResolver(), "play_camera_sound", 1);
        boolean isPlay = "true".equals(System.getString(this.mContext.getContentResolver(), "always_play_screenshot_sound"));
        if (isPlay) {
            HwLog.i("HwGlobalScreenshot", "SoundId=" + this.mSoundId + ", streamId=" + this.mPlayer.play(this.mSoundId, 1.0f, 1.0f, 0, 0, 1.0f));
            return;
        }
        if (playCameraSound == 1 && this.mAudioManager.getRingerMode() == 2) {
            float leftVolume = ((float) this.mAudioManager.getStreamVolume(2)) / ((float) this.mAudioManager.getStreamMaxVolume(2));
            float rightVolume = leftVolume;
            HwLog.i("HwGlobalScreenshot", "SoundId=" + this.mSoundId + ", streamId=" + this.mPlayer.play(this.mSoundId, leftVolume, leftVolume, 0, 0, 1.0f) + ", left=" + leftVolume + ", right=" + leftVolume);
        } else {
            HwLog.w("HwGlobalScreenshot", "playSound failed: " + isPlay + ", " + playCameraSound + ", " + this.mAudioManager.getRingerMode());
        }
    }

    private boolean hasIntentService() {
        boolean hasService = false;
        List<ResolveInfo> list = this.mContext.getPackageManager().queryIntentServices(new Intent("com.huawei.HwMultiScreenShot.start"), 0);
        if (!(list == null || list.size() == 0)) {
            HwLog.d("HwGlobalScreenshot", "hasIntentService");
            hasService = true;
        }
        if (!hasService || isKeyguardShowing()) {
            return false;
        }
        return true;
    }

    private boolean isKeyguardShowing() {
        if (((KeyguardManager) this.mContext.getSystemService("keyguard")).inKeyguardRestrictedInputMode()) {
            return true;
        }
        return false;
    }

    private void cancelAnimation() {
        if (this.mScreenshotAnimation != null) {
            this.mScreenshotAnimation.cancel();
        }
    }

    private void handleActionClickAsync(int actionType, int bdEventId) {
        synchronized (this) {
            if (-1 != this.mPostAction.get()) {
                HwLog.w("HwGlobalScreenshot", "click too quick, only the first action will be handled. Ignore: " + actionType);
                return;
            }
            this.mPostAction.set(actionType);
            if (this.mSaveInBgTask != null && (this.mSaveInBgTask instanceof HwSaveImageTaskItf) && 2 == actionType) {
                ((HwSaveImageTaskItf) this.mSaveInBgTask).onScrollButtonClicked();
            }
            this.mScreenshotAnimation.cancel();
            this.mSubHandler.sendEmptyMessage(actionType);
            BDReporter.c(this.mContext, bdEventId);
        }
    }

    public static void sendBroadcastForNotification(Context context, Uri imageUri, long imageTime, int msgType, int messageStrId) {
        Intent intent = new Intent("com.android.systemui.action.SCREEN_SHOT_NOTIFY");
        if (imageUri != null) {
            intent.putExtra("uri", imageUri.toString());
        }
        intent.putExtra("result", msgType);
        intent.putExtra("time", imageTime);
        intent.putExtra("ticker", true);
        if (messageStrId > 0) {
            intent.putExtra("message_id", messageStrId);
        }
        context.sendBroadcast(intent, "huawei.android.permission.HW_SIGNATURE_OR_SYSTEM");
    }
}
