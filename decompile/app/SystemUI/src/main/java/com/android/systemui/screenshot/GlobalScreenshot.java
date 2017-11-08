package com.android.systemui.screenshot;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.animation.Interpolator;
import android.widget.ImageView;
import com.android.systemui.R;

abstract class GlobalScreenshot implements HwScreenshotItf {
    ImageView mBackgroundView;
    private float mBgPadding;
    float mBgPaddingScale;
    Context mContext;
    private Display mDisplay;
    private Matrix mDisplayMatrix = new Matrix();
    private DisplayMetrics mDisplayMetrics;
    int mNotificationIconSize;
    NotificationManager mNotificationManager;
    final int mPreviewHeight;
    final int mPreviewWidth;
    AsyncTask<Void, Void, Void> mSaveInBgTask;
    Bitmap mScreenBitmap;
    AnimatorSet mScreenshotAnimation;
    private ImageView mScreenshotFlash;
    View mScreenshotLayout;
    private ScreenshotSelectorView mScreenshotSelectorView;
    ImageView mScreenshotView;
    private LayoutParams mWindowLayoutParams;
    private WindowManager mWindowManager;

    public static class DeleteScreenshotReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra("android:screenshot_uri_id")) {
                NotificationManager nm = (NotificationManager) context.getSystemService("notification");
                Uri uri = Uri.parse(intent.getStringExtra("android:screenshot_uri_id"));
                nm.cancel(R.id.notification_screenshot);
                new DeleteImageInBackgroundTask(context).execute(new Uri[]{uri});
            }
        }
    }

    public static class TargetChosenReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            ((NotificationManager) context.getSystemService("notification")).cancel(R.id.notification_screenshot);
        }
    }

    public GlobalScreenshot(Context context) {
        Resources r = context.getResources();
        this.mContext = context;
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        this.mScreenshotLayout = layoutInflater.inflate(R.layout.global_screenshot, null);
        this.mBackgroundView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_background);
        this.mScreenshotView = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot);
        this.mScreenshotFlash = (ImageView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_flash);
        this.mScreenshotSelectorView = (ScreenshotSelectorView) this.mScreenshotLayout.findViewById(R.id.global_screenshot_selector);
        this.mScreenshotLayout.setFocusable(true);
        this.mScreenshotSelectorView.setFocusable(true);
        this.mScreenshotSelectorView.setFocusableInTouchMode(true);
        this.mScreenshotLayout.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        this.mWindowLayoutParams = new LayoutParams(-1, -1, 0, 0, 2036, 17302792, -3);
        this.mWindowLayoutParams.setTitle("ScreenshotAnimation");
        this.mWindowManager = (WindowManager) context.getSystemService("window");
        this.mNotificationManager = (NotificationManager) context.getSystemService("notification");
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDisplayMetrics = new DisplayMetrics();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        this.mNotificationIconSize = r.getDimensionPixelSize(17104902);
        this.mBgPadding = (float) r.getDimensionPixelSize(R.dimen.global_screenshot_bg_padding);
        this.mBgPaddingScale = this.mBgPadding / ((float) this.mDisplayMetrics.widthPixels);
        int panelWidth = 0;
        try {
            panelWidth = r.getDimensionPixelSize(R.dimen.notification_panel_width);
        } catch (NotFoundException e) {
        }
        if (panelWidth <= 0) {
            panelWidth = this.mDisplayMetrics.widthPixels;
        }
        this.mPreviewWidth = panelWidth;
        this.mPreviewHeight = r.getDimensionPixelSize(R.dimen.notification_max_height);
    }

    void saveScreenshotInWorkerThread(Runnable finisher) {
        throw new RuntimeException("Please use concrete sub-class override function.");
    }

    private float getDegreesForRotation(int value) {
        switch (value) {
            case 1:
                return 270.0f;
            case 2:
                return 180.0f;
            case 3:
                return 90.0f;
            default:
                return 0.0f;
        }
    }

    void takeScreenshot(Runnable finisher, boolean statusBarVisible, boolean navBarVisible, int x, int y, int width, int height) {
        resetMembersIfNeeded();
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        float[] dims = new float[]{(float) this.mDisplayMetrics.widthPixels, (float) this.mDisplayMetrics.heightPixels};
        float degrees = getDegreesForRotation(this.mDisplay.getRotation());
        boolean requiresRotation = degrees > 0.0f;
        if (requiresRotation) {
            this.mDisplayMatrix.reset();
            this.mDisplayMatrix.preRotate(-degrees);
            this.mDisplayMatrix.mapPoints(dims);
            dims[0] = Math.abs(dims[0]);
            dims[1] = Math.abs(dims[1]);
        }
        this.mScreenBitmap = getScreenshotBitmap((int) dims[0], (int) dims[1]);
        if (this.mScreenBitmap == null) {
            notifyScreenshotError(this.mContext, this.mNotificationManager, R.string.screenshot_failed_to_capture_text);
            finisher.run();
            return;
        }
        if (requiresRotation) {
            Bitmap ss = Bitmap.createBitmap(this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels, Config.ARGB_8888);
            Canvas c = new Canvas(ss);
            c.translate((float) (ss.getWidth() / 2), (float) (ss.getHeight() / 2));
            c.rotate(degrees);
            c.translate((-dims[0]) / 2.0f, (-dims[1]) / 2.0f);
            c.drawBitmap(this.mScreenBitmap, 0.0f, 0.0f, null);
            c.setBitmap(null);
            this.mScreenBitmap.recycle();
            this.mScreenBitmap = ss;
        }
        if (!(width == this.mDisplayMetrics.widthPixels && height == this.mDisplayMetrics.heightPixels) && x + width <= this.mScreenBitmap.getWidth() && y + height <= this.mScreenBitmap.getHeight()) {
            Bitmap cropped = Bitmap.createBitmap(this.mScreenBitmap, x, y, width, height);
            this.mScreenBitmap.recycle();
            this.mScreenBitmap = cropped;
        }
        this.mScreenBitmap.setHasAlpha(false);
        this.mScreenBitmap.prepareToDraw();
        startAnimation(finisher, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels, statusBarVisible, navBarVisible);
    }

    void takeScreenshot(Runnable finisher, boolean statusBarVisible, boolean navBarVisible) {
        this.mDisplay.getRealMetrics(this.mDisplayMetrics);
        takeScreenshot(finisher, statusBarVisible, navBarVisible, 0, 0, this.mDisplayMetrics.widthPixels, this.mDisplayMetrics.heightPixels);
    }

    void takeScreenshotPartial(final Runnable finisher, final boolean statusBarVisible, final boolean navBarVisible) {
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        this.mScreenshotSelectorView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                ScreenshotSelectorView view = (ScreenshotSelectorView) v;
                switch (event.getAction()) {
                    case 0:
                        view.startSelection((int) event.getX(), (int) event.getY());
                        return true;
                    case 1:
                        view.setVisibility(8);
                        GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                        final Rect rect = view.getSelectionRect();
                        if (!(rect == null || rect.width() == 0 || rect.height() == 0)) {
                            View view2 = GlobalScreenshot.this.mScreenshotLayout;
                            final Runnable runnable = finisher;
                            final boolean z = statusBarVisible;
                            final boolean z2 = navBarVisible;
                            view2.post(new Runnable() {
                                public void run() {
                                    GlobalScreenshot.this.takeScreenshot(runnable, z, z2, rect.left, rect.top, rect.width(), rect.height());
                                }
                            });
                        }
                        view.stopSelection();
                        return true;
                    case 2:
                        view.updateSelection((int) event.getX(), (int) event.getY());
                        return true;
                    default:
                        return false;
                }
            }
        });
        this.mScreenshotLayout.post(new Runnable() {
            public void run() {
                GlobalScreenshot.this.mScreenshotSelectorView.setVisibility(0);
                GlobalScreenshot.this.mScreenshotSelectorView.requestFocus();
            }
        });
    }

    void stopScreenshot() {
        if (this.mScreenshotSelectorView.getSelectionRect() != null) {
            this.mWindowManager.removeView(this.mScreenshotLayout);
            this.mScreenshotSelectorView.stopSelection();
        }
    }

    private void startAnimation(final Runnable finisher, int w, int h, boolean statusBarVisible, boolean navBarVisible) {
        preAnimationStart();
        this.mScreenshotLayout.requestFocus();
        if (this.mScreenshotAnimation != null) {
            if (this.mScreenshotAnimation.isStarted()) {
                this.mScreenshotAnimation.end();
            }
            this.mScreenshotAnimation.removeAllListeners();
        }
        this.mWindowManager.addView(this.mScreenshotLayout, this.mWindowLayoutParams);
        Log.i("GlobalScreenshot", "addView Screenshot preview layout");
        ValueAnimator screenshotDropInAnim = createScreenshotDropInAnimation();
        ValueAnimator screenshotFadeOutAnim = createScreenshotDropOutAnimation(w, h, statusBarVisible, navBarVisible);
        this.mScreenshotAnimation = new AnimatorSet();
        this.mScreenshotAnimation.playSequentially(new Animator[]{screenshotDropInAnim, screenshotFadeOutAnim});
        this.mScreenshotAnimation.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                GlobalScreenshot.this.saveScreenshotInWorkerThread(finisher);
            }

            public void onAnimationEnd(Animator animation) {
                GlobalScreenshot.this.mWindowManager.removeView(GlobalScreenshot.this.mScreenshotLayout);
                Log.i("GlobalScreenshot", "removeView Screenshot preview layout");
                GlobalScreenshot.this.mScreenBitmap = null;
                GlobalScreenshot.this.mScreenshotView.setImageBitmap(null);
                if (GlobalScreenshot.this.mSaveInBgTask != null && (GlobalScreenshot.this.mSaveInBgTask instanceof HwSaveImageTaskItf)) {
                    ((HwSaveImageTaskItf) GlobalScreenshot.this.mSaveInBgTask).onScreenshotAnimationEnd();
                }
            }
        });
        this.mScreenshotLayout.post(new Runnable() {
            public void run() {
                GlobalScreenshot.this.playSoundAndSetViewLayer();
                GlobalScreenshot.this.mScreenshotAnimation.start();
            }
        });
    }

    private ValueAnimator createScreenshotDropInAnimation() {
        final Interpolator flashAlphaInterpolator = new Interpolator() {
            public float getInterpolation(float x) {
                if (x <= 0.60465115f) {
                    return (float) Math.sin(((double) (x / 0.60465115f)) * 3.141592653589793d);
                }
                return 0.0f;
            }
        };
        final Interpolator scaleInterpolator = new Interpolator() {
            public float getInterpolation(float x) {
                if (x < 0.30232558f) {
                    return 0.0f;
                }
                return (x - 0.60465115f) / 0.39534885f;
            }
        };
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.setDuration(200);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationStart(Animator animation) {
                GlobalScreenshot.this.mBackgroundView.setAlpha(0.0f);
                GlobalScreenshot.this.mBackgroundView.setVisibility(0);
                GlobalScreenshot.this.onDropInAnimationStart();
                GlobalScreenshot.this.mScreenshotFlash.setAlpha(0.0f);
                GlobalScreenshot.this.mScreenshotFlash.setVisibility(0);
            }

            public void onAnimationEnd(Animator animation) {
                GlobalScreenshot.this.mScreenshotFlash.setVisibility(8);
            }
        });
        anim.addUpdateListener(new AnimatorUpdateListener() {
            public void onAnimationUpdate(ValueAnimator animation) {
                float t = ((Float) animation.getAnimatedValue()).floatValue();
                float scaleT = (GlobalScreenshot.this.mBgPaddingScale + 1.0f) - (scaleInterpolator.getInterpolation(t) * 0.27499998f);
                GlobalScreenshot.this.mBackgroundView.setAlpha(scaleInterpolator.getInterpolation(t) * 0.5f);
                GlobalScreenshot.this.onDropInAnimationUpdate(t, scaleT);
                GlobalScreenshot.this.mScreenshotFlash.setAlpha(flashAlphaInterpolator.getInterpolation(t));
            }
        });
        return anim;
    }

    private ValueAnimator createScreenshotDropOutAnimation(int w, int h, boolean statusBarVisible, boolean navBarVisible) {
        ValueAnimator anim = ValueAnimator.ofFloat(new float[]{0.0f, 1.0f});
        anim.setStartDelay(2500);
        anim.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                GlobalScreenshot.this.mBackgroundView.setVisibility(8);
                GlobalScreenshot.this.onDropOutAnimationEnd();
            }
        });
        if (statusBarVisible && navBarVisible) {
            final Interpolator scaleInterpolator = new Interpolator() {
                public float getInterpolation(float x) {
                    if (x < 0.8604651f) {
                        return (float) (1.0d - Math.pow((double) (1.0f - (x / 0.8604651f)), 2.0d));
                    }
                    return 1.0f;
                }
            };
            float halfScreenWidth = (((float) w) - (this.mBgPadding * 2.0f)) / 2.0f;
            float halfScreenHeight = (((float) h) - (this.mBgPadding * 2.0f)) / 2.0f;
            final PointF finalPos = new PointF((-halfScreenWidth) + (0.45f * halfScreenWidth), (-halfScreenHeight) + (0.45f * halfScreenHeight));
            anim.setDuration(430);
            anim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = ((Float) animation.getAnimatedValue()).floatValue();
                    float scaleT = (GlobalScreenshot.this.mBgPaddingScale + 0.725f) - (scaleInterpolator.getInterpolation(t) * 0.27500004f);
                    GlobalScreenshot.this.mBackgroundView.setAlpha((1.0f - t) * 0.5f);
                    GlobalScreenshot.this.onDropOutAnimationUpdateWithAllBarVisible(t, scaleT, scaleInterpolator, finalPos);
                }
            });
        } else {
            anim.setDuration(320);
            anim.addUpdateListener(new AnimatorUpdateListener() {
                public void onAnimationUpdate(ValueAnimator animation) {
                    float t = ((Float) animation.getAnimatedValue()).floatValue();
                    float scaleT = (GlobalScreenshot.this.mBgPaddingScale + 0.725f) - (0.125f * t);
                    GlobalScreenshot.this.mBackgroundView.setAlpha((1.0f - t) * 0.5f);
                    GlobalScreenshot.this.onDropOutAnimationUpdateWithOneBarInvisible(t, scaleT);
                }
            });
        }
        return anim;
    }

    static void notifyScreenshotError(Context context, NotificationManager nManager, int msgResId) {
        HwGlobalScreenshot.sendBroadcastForNotification(context, null, 0, 1, msgResId);
    }
}
