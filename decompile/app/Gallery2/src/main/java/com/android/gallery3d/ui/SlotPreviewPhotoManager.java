package com.android.gallery3d.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.ActivityExWrapper;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.LauncherVibrator;
import com.huawei.gallery.actionbar.Action;
import com.huawei.watermark.manager.parse.WMElement;
import java.util.ArrayList;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class SlotPreviewPhotoManager {
    private static float BLUR_RADIUS = 6.0f;
    private static float BLUR_SCALE = 4.0f;
    private static final int PADDING = GalleryUtils.dpToPixel(4);
    private static float PRESS_PRIM = WMElement.CAMERASIZEVALUE1B1;
    private static float PRESS_THRE = WMElement.CAMERASIZEVALUE1B1;
    private Activity mActivity;
    private Bitmap mContent;
    private int mCurrentColor;
    private MotionEvent mCurrentEvent;
    private Rect mCurrentRect;
    private SlotPreviewModeDelegate mDelegate;
    private EventProcess mEventProcess = new EventProcess();
    public Handler mHandler;
    private boolean mIsActive = false;
    private boolean mIsFailed = false;
    private SlotPreviewModeListener mListener;
    private final ImageView mPreviewBackground;
    private final ImageView mPreviewBlur;
    private final ImageView mPreviewView;
    private Rect mPrimaryRect;
    private Bitmap mScreenShot = null;
    private PreviewAnim mStartFailedAnimation;
    private PreviewAnim mStartLongClickAnimation;
    private SlotPreviewState mState = SlotPreviewState.INIT;
    private TimeClock mTimeClock = new TimeClock();
    private final LauncherVibrator mVibrator;
    private ActivityExWrapper mWrapper;

    private interface AnimationListener {
        void onAnimationEnd();
    }

    private class EventProcess {
        private ArrayList<PreviewModeEvent> mEventList;

        private class PreviewModeEvent {
            Action mAction;
            ArrayList<Path> mPaths;

            public PreviewModeEvent(Action action, ArrayList<Path> paths) {
                this.mAction = action;
                this.mPaths = paths;
            }
        }

        private EventProcess() {
            this.mEventList = new ArrayList();
        }

        public void addEvent(Action action, ArrayList<Path> paths) {
            this.mEventList.add(new PreviewModeEvent(action, paths));
        }

        public void processPreviewModeEvent() {
            if (SlotPreviewPhotoManager.this.mDelegate != null) {
                for (PreviewModeEvent event : this.mEventList) {
                    if (event != null) {
                        SlotPreviewPhotoManager.this.mDelegate.process(event.mAction, event.mPaths);
                        this.mEventList.remove(event);
                    }
                }
            }
        }
    }

    private class PreviewAnim {
        private final ValueAnimator mAnimation = ValueAnimator.ofFloat(new float[]{0.0f, WMElement.CAMERASIZEVALUE1B1});
        private Rect mCurrentRect = new Rect();
        private int mSourceColor;
        private Rect mSourceRect = new Rect();
        private int mTargetColor;
        private Rect mTargetRect = new Rect();

        public PreviewAnim(final AnimationListener listener) {
            this.mAnimation.addListener(new AnimatorListenerAdapter() {
                public void onAnimationEnd(Animator animation) {
                    if (listener != null) {
                        listener.onAnimationEnd();
                    }
                }

                public void onAnimationStart(Animator animation) {
                    SlotPreviewPhotoManager.this.mIsActive = false;
                }
            });
            this.mAnimation.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    float progress = ((Float) valueAnimator.getAnimatedValue()).floatValue();
                    int color = PreviewAnim.this.getColorByProgress(progress);
                    PreviewAnim.this.updateRectByProgress(progress);
                    SlotPreviewPhotoManager.this.mPreviewView.setPadding(PreviewAnim.this.mCurrentRect.left, PreviewAnim.this.mCurrentRect.top, SlotPreviewPhotoManager.this.mPreviewView.getWidth() - PreviewAnim.this.mCurrentRect.right, SlotPreviewPhotoManager.this.mPreviewView.getHeight() - PreviewAnim.this.mCurrentRect.bottom);
                    SlotPreviewPhotoManager.this.mPreviewBackground.setBackgroundColor(Color.argb(color, 0, 0, 0));
                }
            });
        }

        private int getColorByProgress(float progress) {
            return (int) (((float) this.mSourceColor) + (((float) (this.mTargetColor - this.mSourceColor)) * progress));
        }

        private void updateRectByProgress(float progress) {
            int centerX = (int) (((float) this.mSourceRect.centerX()) + (((float) (this.mTargetRect.centerX() - this.mSourceRect.centerX())) * progress));
            int centerY = (int) (((float) this.mSourceRect.centerY()) + (((float) (this.mTargetRect.centerY() - this.mSourceRect.centerY())) * progress));
            int width = (int) (((float) this.mSourceRect.width()) + (((float) (this.mTargetRect.width() - this.mSourceRect.width())) * progress));
            int height = (int) (((float) this.mSourceRect.height()) + (((float) (this.mTargetRect.height() - this.mSourceRect.height())) * progress));
            this.mCurrentRect.set(centerX - (width / 2), centerY - (height / 2), (width / 2) + centerX, (height / 2) + centerY);
        }

        public void startAnimation(Rect source, Rect target, int srcColor, int tagColor, int duration) {
            this.mSourceRect.set(source);
            this.mTargetRect.set(target);
            this.mSourceColor = srcColor;
            this.mTargetColor = tagColor;
            this.mAnimation.setDuration((long) duration);
            this.mAnimation.start();
        }
    }

    public interface SlotPreviewModeDelegate {
        Bitmap getPreviewBitmap(MotionEvent motionEvent);

        Rect getPreviewImageRect(MotionEvent motionEvent);

        boolean isVideo(MotionEvent motionEvent);

        void process(Action action, ArrayList<Path> arrayList);
    }

    public interface SlotPreviewModeListener {
        void onClick(MotionEvent motionEvent);

        void onEnterPreviewMode();

        void onLeavePreviewMode();

        void onLongClick(MotionEvent motionEvent);

        void onStartPreview(MotionEvent motionEvent);
    }

    private enum SlotPreviewState {
        INIT(0),
        PREPARE(1),
        QUICKPRESS(2),
        PREVIEW(3);
        
        private int mState;

        private SlotPreviewState(int state) {
            this.mState = state;
        }
    }

    private static class TimeClock {
        private long mCurrentTime;
        private long mStartTime;

        private TimeClock() {
        }

        public void resetClock() {
            this.mStartTime = -1;
            this.mCurrentTime = -1;
        }

        public void initClock() {
            this.mStartTime = System.currentTimeMillis();
        }

        public boolean isQuickPress() {
            this.mCurrentTime = System.currentTimeMillis();
            return this.mCurrentTime - this.mStartTime < 150;
        }

        public boolean isOnLongPress() {
            this.mCurrentTime = System.currentTimeMillis();
            return this.mCurrentTime - this.mStartTime > 800;
        }

        public boolean pressEnoughLong() {
            this.mCurrentTime = System.currentTimeMillis();
            return this.mCurrentTime - this.mStartTime > 300;
        }
    }

    public SlotPreviewPhotoManager(Activity activity, GLRoot root) {
        this.mActivity = activity;
        this.mVibrator = new LauncherVibrator(activity);
        this.mWrapper = new ActivityExWrapper(activity);
        this.mPreviewBlur = new ImageView(activity);
        this.mPreviewBlur.setLayoutParams(new LayoutParams(-1, -1));
        this.mPreviewBackground = new ImageView(activity);
        this.mPreviewBackground.setLayoutParams(new LayoutParams(-1, -1));
        this.mPreviewView = new ImageView(activity);
        this.mPreviewView.setLayoutParams(new LayoutParams(-1, -1));
        this.mStartLongClickAnimation = new PreviewAnim(new AnimationListener() {
            public void onAnimationEnd() {
                SlotPreviewPhotoManager.this.onLongClick();
            }
        });
        this.mStartFailedAnimation = new PreviewAnim(new AnimationListener() {
            public void onAnimationEnd() {
                SlotPreviewPhotoManager.this.leavePreviewMode();
            }
        });
        this.mHandler = new SynchronizedHandler(this.mActivity.getMainLooper(), root) {
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        if (!SlotPreviewPhotoManager.this.mTimeClock.isOnLongPress()) {
                            SlotPreviewPhotoManager.this.mHandler.sendMessageDelayed(obtainMessage(1), 100);
                            return;
                        } else if (SlotPreviewPhotoManager.this.mListener != null) {
                            SlotPreviewPhotoManager.this.mStartLongClickAnimation.startAnimation(SlotPreviewPhotoManager.this.mCurrentRect, SlotPreviewPhotoManager.this.mPrimaryRect, SlotPreviewPhotoManager.this.mCurrentColor, 0, 300);
                            return;
                        } else {
                            return;
                        }
                    default:
                        return;
                }
            }
        };
    }

    private void onLongClick() {
        if (this.mListener != null) {
            leavePreviewMode();
            this.mListener.onLongClick(this.mCurrentEvent);
        }
    }

    private void onClick() {
        if (this.mListener != null) {
            leavePreviewMode();
            this.mListener.onClick(this.mCurrentEvent);
        }
    }

    private void startPreview() {
        this.mState = SlotPreviewState.PREVIEW;
        if (this.mIsFailed) {
            this.mPreviewView.setImageBitmap(this.mContent);
            this.mStartFailedAnimation.startAnimation(this.mCurrentRect, this.mPrimaryRect, this.mCurrentColor, 0, 450);
            this.mVibrator.vibrate(null, 2);
        } else if (this.mListener != null) {
            this.mListener.onStartPreview(this.mCurrentEvent);
        }
        this.mHandler.removeCallbacksAndMessages(null);
    }

    private void enterPreviewMode() {
        if (this.mState.equals(SlotPreviewState.PREPARE) || (this.mState.equals(SlotPreviewState.QUICKPRESS) && this.mTimeClock.pressEnoughLong())) {
            startPreview();
        }
    }

    private void updateByProgress(float progress, int padding, int backgroundColor) {
        if (this.mPrimaryRect != null && this.mContent != null && this.mCurrentRect != null && this.mPreviewView.getHeight() != 0 && this.mPreviewView.getWidth() != 0) {
            progress = Utils.clamp(progress, 0.0f, (float) WMElement.CAMERASIZEVALUE1B1);
            int d = this.mIsFailed ? 0 : (int) (((float) padding) * progress);
            this.mCurrentRect.set(this.mPrimaryRect.left - d, this.mPrimaryRect.top - d, this.mPrimaryRect.right + d, this.mPrimaryRect.bottom + d);
            this.mPreviewView.setPadding(this.mPrimaryRect.left - d, this.mPrimaryRect.top - d, this.mPreviewView.getWidth() - (this.mPrimaryRect.right + d), this.mPreviewView.getHeight() - (this.mPrimaryRect.bottom + d));
            this.mPreviewView.setImageBitmap(this.mContent);
            this.mPreviewBlur.setImageBitmap(this.mScreenShot);
            this.mCurrentColor = (int) (((float) backgroundColor) * progress);
            this.mPreviewBackground.setBackgroundColor(Color.argb(this.mCurrentColor, 0, 0, 0));
        }
    }

    private void initParams() {
        this.mIsActive = true;
        PRESS_THRE = GalleryUtils.getPressureLimit();
        PRESS_PRIM = PRESS_THRE * 0.3f;
        this.mTimeClock.initClock();
        this.mCurrentColor = 0;
        this.mCurrentRect = new Rect(this.mPrimaryRect);
        this.mPreviewView.setImageBitmap(null);
        this.mPreviewBlur.setImageBitmap(null);
        this.mPreviewBackground.setBackgroundColor(0);
    }

    private boolean isEventValid(MotionEvent event) {
        boolean z = false;
        if (this.mDelegate == null) {
            return false;
        }
        boolean z2;
        if (!(this.mContent == null || this.mContent.isRecycled())) {
            this.mContent.recycle();
        }
        if (this.mDelegate.isVideo(event)) {
            z2 = true;
        } else {
            z2 = false;
        }
        this.mIsFailed = z2;
        this.mContent = this.mDelegate.getPreviewBitmap(event);
        this.mPrimaryRect = this.mDelegate.getPreviewImageRect(event);
        this.mCurrentEvent = MotionEvent.obtain(event);
        if (!(this.mContent == null || this.mPrimaryRect == null)) {
            z = true;
        }
        return z;
    }

    public void onLongClickPrepare() {
        this.mHandler.sendEmptyMessage(1);
    }

    public void onTouchEvent(MotionEvent event) {
        if (event.getAction() == 0) {
            if (isEventValid(event)) {
                initParams();
            }
        } else if (this.mIsActive) {
            float press = event.getPressure();
            float progress = (press - PRESS_PRIM) / (PRESS_THRE - PRESS_PRIM);
            switch (event.getAction()) {
                case 1:
                case 3:
                    if (SlotPreviewState.QUICKPRESS.equals(this.mState) && !this.mTimeClock.pressEnoughLong()) {
                        onClick();
                        break;
                    } else {
                        leavePreviewMode();
                        break;
                    }
                case 2:
                    if (press > PRESS_PRIM) {
                        enterPrepareMode();
                        if (this.mState.equals(SlotPreviewState.PREPARE)) {
                            updateByProgress(progress, PADDING, this.mIsFailed ? 80 : SmsCheckResult.ESCT_178);
                        }
                    }
                    if (press > PRESS_THRE) {
                        enterPreviewMode();
                        break;
                    }
                    break;
            }
        }
    }

    public void setListener(SlotPreviewModeListener listener) {
        this.mListener = listener;
    }

    public void setDelegate(SlotPreviewModeDelegate delegate) {
        this.mDelegate = delegate;
    }

    public boolean inPrepareMode() {
        return !SlotPreviewState.INIT.equals(this.mState);
    }

    public boolean inPreviewMode() {
        return SlotPreviewState.PREVIEW.equals(this.mState);
    }

    private void enterPrepareMode() {
        if (!inPrepareMode()) {
            this.mState = SlotPreviewState.PREPARE;
            if (this.mTimeClock.isQuickPress()) {
                this.mState = SlotPreviewState.QUICKPRESS;
            }
            this.mWrapper.run("setScreenShortBmpFromView");
            Object obj = this.mWrapper.run("getScreenShotBmp");
            if (obj != null && (obj instanceof Bitmap)) {
                this.mScreenShot = GalleryUtils.blurBitmap(this.mActivity, (Bitmap) obj, BLUR_RADIUS, BLUR_SCALE);
            }
            View parent = this.mActivity.getWindow().getDecorView();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).addView(this.mPreviewBlur);
                ((ViewGroup) parent).addView(this.mPreviewBackground);
                ((ViewGroup) parent).addView(this.mPreviewView);
            }
            if (this.mListener != null) {
                this.mListener.onEnterPreviewMode();
            }
        }
    }

    public void leavePreviewMode() {
        if (inPrepareMode()) {
            View parent = this.mActivity.getWindow().getDecorView();
            if (parent instanceof ViewGroup) {
                ((ViewGroup) parent).removeView(this.mPreviewBlur);
                ((ViewGroup) parent).removeView(this.mPreviewBackground);
                ((ViewGroup) parent).removeView(this.mPreviewView);
            }
            this.mState = SlotPreviewState.INIT;
            this.mIsActive = false;
            this.mTimeClock.resetClock();
            this.mHandler.removeCallbacksAndMessages(null);
            if (this.mContent != null) {
                this.mContent.recycle();
                this.mContent = null;
            }
            if (this.mScreenShot != null) {
                this.mScreenShot.recycle();
                this.mScreenShot = null;
            }
            if (this.mListener != null) {
                this.mListener.onLeavePreviewMode();
            }
        }
    }

    public void addEvent(Action action, ArrayList<Path> paths) {
        this.mEventProcess.addEvent(action, paths);
    }

    public void processPreviewModeEvent() {
        this.mEventProcess.processPreviewModeEvent();
    }
}
