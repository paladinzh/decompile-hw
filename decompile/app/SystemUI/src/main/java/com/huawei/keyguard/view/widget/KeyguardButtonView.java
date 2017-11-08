package com.huawei.keyguard.view.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.Button;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.R$styleable;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.BigPicture;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.theme.KeyguardTheme;
import com.huawei.keyguard.util.HwLog;
import com.huawei.openalliance.ad.inter.constant.EventType;
import fyusion.vislib.BuildConfig;

public class KeyguardButtonView extends Button {
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    private int mCode;
    private long mDownTime;
    private boolean mLongClicked;
    private boolean mSupportsLongpress;
    private boolean mSupportsLongpressBack;
    private int mTouchSlop;

    public KeyguardButtonView(Context context) {
        this(context, null);
    }

    public KeyguardButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 16842824);
    }

    public KeyguardButtonView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyguardButtonView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mSupportsLongpress = true;
        this.mSupportsLongpressBack = true;
        this.mCheckLongPress = new Runnable() {
            public void run() {
                if (!KeyguardButtonView.this.isPressed()) {
                    return;
                }
                if (KeyguardButtonView.this.isLongClickable()) {
                    KeyguardButtonView.this.performLongClick();
                    KeyguardButtonView.this.mLongClicked = true;
                } else if (KeyguardButtonView.this.mSupportsLongpress) {
                    KeyguardButtonView.this.sendEvent(0, 128);
                    KeyguardButtonView.this.sendAccessibilityEvent(2);
                    KeyguardButtonView.this.mLongClicked = true;
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.KeyguardButtonView, defStyleAttr, 0);
        this.mCode = a.getInteger(R$styleable.KeyguardButtonView_kgKeyCode, 4);
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        a.recycle();
    }

    public void setSupportsLongpressBack(boolean support) {
        this.mSupportsLongpressBack = support;
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = false;
        int action = ev.getAction();
        HwLog.w("KeyguardButtonView", "onTouchEvent " + action + ";  " + this.mCode);
        if (!isClickable()) {
            return true;
        }
        switch (action) {
            case 0:
                this.mDownTime = SystemClock.uptimeMillis();
                this.mLongClicked = false;
                setPressed(true);
                if (this.mCode != 0) {
                    sendEvent(0, 0, this.mDownTime);
                } else {
                    performHapticFeedback(1);
                }
                removeCallbacks(this.mCheckLongPress);
                postDelayed(this.mCheckLongPress, (long) ViewConfiguration.getLongPressTimeout());
                break;
            case 1:
                boolean doIt = isPressed() && !this.mLongClicked;
                setPressed(false);
                if (this.mCode != 0) {
                    if (doIt) {
                        sendEvent(1, 0);
                        sendAccessibilityEvent(1);
                        playSoundEffect(0);
                        HwLockScreenReporter.report(getContext(), 159, BuildConfig.FLAVOR);
                        if (4 == this.mCode && 2 == KeyguardTheme.getInst().getLockStyle()) {
                            BigPicture bigPic = MagazineWallpaper.getInst(getContext()).getWallPaper(0);
                            if (bigPic != null) {
                                HwLockScreenReporter.reportAdEvent(getContext(), bigPic.getBigPictureInfo(), EventType.IMPRESSION);
                            }
                            HwLockScreenReporter.report(getContext(), 159, BuildConfig.FLAVOR);
                        }
                    } else if (this.mSupportsLongpressBack) {
                        sendEvent(1, 32);
                        HwLockScreenReporter.report(getContext(), 159, BuildConfig.FLAVOR);
                    }
                } else if (doIt) {
                    performClick();
                }
                removeCallbacks(this.mCheckLongPress);
                break;
            case 2:
                int x = (int) ev.getX();
                int y = (int) ev.getY();
                if (x >= (-this.mTouchSlop) && x < getWidth() + this.mTouchSlop && y >= (-this.mTouchSlop) && y < getHeight() + this.mTouchSlop) {
                    z = true;
                }
                setPressed(z);
                break;
            case 3:
                setPressed(false);
                if (this.mCode != 0) {
                    sendEvent(1, 32);
                }
                removeCallbacks(this.mCheckLongPress);
                break;
        }
        return true;
    }

    public void playSoundEffect(int soundConstant) {
        if (this.mAudioManager != null) {
            this.mAudioManager.playSoundEffect(soundConstant, KeyguardUpdateMonitor.getCurrentUser());
        }
    }

    private void sendEvent(int action, int flags) {
        sendEvent(action, flags, SystemClock.uptimeMillis());
    }

    private void sendEvent(int action, int flags, long when) {
        HwLog.w("KeyguardButtonView", "sendEvent " + action + ";  " + flags + when);
        InputManager.getInstance().injectInputEvent(new KeyEvent(this.mDownTime, when, action, this.mCode, (flags & 128) != 0 ? 1 : 0, 0, -1, 0, (flags | 8) | 64, 257), 0);
    }
}
