package com.android.systemui.statusbar.policy;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.FrameLayout.LayoutParams;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.statusbar.ScrimView;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.statusbar.phone.StatusBarWindowView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.utils.HwLog;

public class BrightnessMirrorController {
    public long TRANSITION_DURATION_IN = 150;
    public long TRANSITION_DURATION_OUT = 150;
    private Bitmap mBlur;
    private View mBrightnessMirror;
    private final int[] mInt2Cache = new int[2];
    private boolean mIsMirrowShow = false;
    private final View mNotificationPanel;
    private final ScrimView mScrimBehind;
    private final NotificationStackScrollLayout mStackScroller;
    private final StatusBarWindowView mStatusBarWindow;

    public BrightnessMirrorController(StatusBarWindowView statusBarWindow) {
        this.mStatusBarWindow = statusBarWindow;
        this.mScrimBehind = (ScrimView) statusBarWindow.findViewById(R.id.scrim_behind);
        this.mBrightnessMirror = statusBarWindow.findViewById(R.id.brightness_mirror);
        this.mNotificationPanel = statusBarWindow.findViewById(R.id.notification_panel);
        this.mStackScroller = (NotificationStackScrollLayout) statusBarWindow.findViewById(R.id.notification_stack_scroller);
    }

    public void showMirror() {
        this.mIsMirrowShow = true;
        this.mBrightnessMirror.setVisibility(0);
        this.mStackScroller.setFadingOut(true);
        this.mScrimBehind.animateViewAlpha(0.0f, this.TRANSITION_DURATION_OUT, Interpolators.ALPHA_OUT);
        outAnimation(this.mNotificationPanel.animate()).withLayer();
        HwPhoneStatusBar.getInstance().showMirror();
    }

    public void hideMirror() {
        this.mScrimBehind.animateViewAlpha(1.0f, this.TRANSITION_DURATION_IN, Interpolators.ALPHA_IN);
        inAnimation(this.mNotificationPanel.animate()).withLayer().withStartAction(new Runnable() {
            public void run() {
                BrightnessMirrorController.this.mIsMirrowShow = false;
            }
        }).withEndAction(new Runnable() {
            public void run() {
                BrightnessMirrorController.this.mBrightnessMirror.setVisibility(4);
                BrightnessMirrorController.this.mStackScroller.setFadingOut(false);
            }
        });
        HwPhoneStatusBar.getInstance().hideMirror();
    }

    private ViewPropertyAnimator outAnimation(ViewPropertyAnimator a) {
        a.setListener(null);
        return a.alpha(0.0f).setDuration(this.TRANSITION_DURATION_OUT).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(null);
    }

    private ViewPropertyAnimator inAnimation(ViewPropertyAnimator a) {
        a.setListener(new AnimatorListenerAdapter() {
            public void onAnimationCancel(Animator arg0) {
                if (BrightnessMirrorController.this.mIsMirrowShow) {
                    BrightnessMirrorController.this.mIsMirrowShow = false;
                    return;
                }
                BrightnessMirrorController.this.mBrightnessMirror.setVisibility(4);
                BrightnessMirrorController.this.mStackScroller.setFadingOut(false);
            }
        });
        return a.alpha(1.0f).setDuration(this.TRANSITION_DURATION_IN).setInterpolator(Interpolators.ALPHA_IN);
    }

    public void setLocation(View original) {
        original.getLocationInWindow(this.mInt2Cache);
        int originalX = this.mInt2Cache[0] + (original.getWidth() / 2);
        int originalY = this.mInt2Cache[1] + (original.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX(0.0f);
        this.mBrightnessMirror.setTranslationY(0.0f);
        this.mBrightnessMirror.getLocationInWindow(this.mInt2Cache);
        int mirrorY = this.mInt2Cache[1] + (this.mBrightnessMirror.getHeight() / 2);
        this.mBrightnessMirror.setTranslationX((float) (originalX - (this.mInt2Cache[0] + (this.mBrightnessMirror.getWidth() / 2))));
        this.mBrightnessMirror.setTranslationY((float) (originalY - mirrorY));
        if (this.mBlur != null) {
            int left = this.mInt2Cache[0] + ((int) this.mBrightnessMirror.getTranslationX());
            int top = this.mInt2Cache[1] + ((int) this.mBrightnessMirror.getTranslationY());
            HwLog.i("BrightnessMirrorController", "left=" + left + ", top=" + top + ", width=" + this.mBrightnessMirror.getWidth() + ", height=" + this.mBrightnessMirror.getHeight());
            this.mBrightnessMirror.setBackground(new BitmapDrawable(this.mBrightnessMirror.getResources(), Bitmap.createBitmap(this.mBlur, left, top, this.mBrightnessMirror.getWidth(), this.mBrightnessMirror.getHeight())));
        }
    }

    public View getMirror() {
        return this.mBrightnessMirror;
    }

    public void updateResources() {
        LayoutParams lp = (LayoutParams) this.mBrightnessMirror.getLayoutParams();
        lp.width = this.mBrightnessMirror.getResources().getDimensionPixelSize(R.dimen.notification_panel_width);
        lp.gravity = this.mBrightnessMirror.getResources().getInteger(R.integer.notification_panel_layout_gravity);
        this.mBrightnessMirror.setLayoutParams(lp);
    }

    public void onDensityOrFontScaleChanged() {
        int index = this.mStatusBarWindow.indexOfChild(this.mBrightnessMirror);
        this.mStatusBarWindow.removeView(this.mBrightnessMirror);
        this.mBrightnessMirror = LayoutInflater.from(this.mBrightnessMirror.getContext()).inflate(R.layout.brightness_mirror, this.mStatusBarWindow, false);
        this.mStatusBarWindow.addView(this.mBrightnessMirror, index);
        HwPhoneStatusBar.getInstance().resetBrightnessMirror();
    }

    public void setBlur(Bitmap blur) {
        if (this.mBlur != null) {
            HwLog.i("BrightnessMirrorController", "setBlur w=" + this.mBlur.getWidth() + ", h=" + this.mBlur.getHeight());
            BitmapDrawable drawable = new BitmapDrawable(this.mBrightnessMirror.getResources(), blur);
            drawable.setBounds(0, 0, HwPhoneStatusBar.getInstance().getScreenWidth(), HwPhoneStatusBar.getInstance().getScreenHeight());
            this.mBlur = drawable.getBitmap();
        }
    }
}
