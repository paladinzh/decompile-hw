package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.hardware.input.InputManager;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import com.android.systemui.R$styleable;
import com.android.systemui.tint.TintImageView;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.UserSwitchUtils;

public class KeyButtonView extends TintImageView {
    private AudioManager mAudioManager;
    private final Runnable mCheckLongPress;
    private int mCode;
    private int mContentDescriptionRes;
    private long mDownTime;
    private boolean mFlag;
    private boolean mGestureAborted;
    private boolean mLongClicked;
    private boolean mSupportsLongpress;
    private int mTouchSlop;

    public KeyButtonView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyButtonView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs);
        this.mSupportsLongpress = true;
        this.mFlag = false;
        this.mCheckLongPress = new Runnable() {
            public void run() {
                if (!KeyButtonView.this.isPressed()) {
                    return;
                }
                if (KeyButtonView.this.isLongClickable()) {
                    KeyButtonView.this.performLongClick();
                    KeyButtonView.this.mLongClicked = true;
                } else if (KeyButtonView.this.mSupportsLongpress) {
                    KeyButtonView.this.sendEvent(0, 128);
                    KeyButtonView.this.sendAccessibilityEvent(2);
                    KeyButtonView.this.mLongClicked = true;
                }
            }
        };
        TypedArray a = context.obtainStyledAttributes(attrs, R$styleable.KeyButtonView, defStyle, 0);
        this.mCode = a.getInteger(1, 0);
        this.mSupportsLongpress = a.getBoolean(2, true);
        TypedValue value = new TypedValue();
        if (a.getValue(0, value)) {
            this.mContentDescriptionRes = value.resourceId;
        }
        a.recycle();
        setClickable(true);
        this.mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        setBackground(new KeyButtonRipple(context, this));
    }

    public void setCode(int code) {
        this.mCode = code;
    }

    public void loadAsync(String uri) {
        new AsyncTask<String, Void, Drawable>() {
            protected Drawable doInBackground(String... params) {
                return Icon.createWithContentUri(params[0]).loadDrawable(KeyButtonView.this.mContext);
            }

            protected void onPostExecute(Drawable drawable) {
                KeyButtonView.this.setImageDrawable(drawable);
            }
        }.execute(new String[]{uri});
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mContentDescriptionRes != 0) {
            setContentDescription(this.mContext.getString(this.mContentDescriptionRes));
        }
    }

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(info);
        if (this.mCode != 0) {
            info.addAction(new AccessibilityAction(16, null));
            if (this.mSupportsLongpress || isLongClickable()) {
                info.addAction(new AccessibilityAction(32, null));
            }
        }
    }

    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        if (visibility != 0) {
            jumpDrawablesToCurrentState();
        }
    }

    public boolean performAccessibilityActionInternal(int action, Bundle arguments) {
        if (action == 16 && this.mCode != 0) {
            sendEvent(0, 0, SystemClock.uptimeMillis());
            sendEvent(1, 0);
            sendAccessibilityEvent(1);
            playSoundEffect(0);
            return true;
        } else if (action != 32 || this.mCode == 0) {
            return super.performAccessibilityActionInternal(action, arguments);
        } else {
            sendEvent(0, 128);
            sendEvent(1, 0);
            sendAccessibilityEvent(2);
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent ev) {
        boolean z = false;
        if (ev.getActionMasked() != 2) {
            HwLog.i("TintImageView", "onTouchEvent: code=" + this.mCode + ", action=" + ev.getActionMasked() + ", event=" + ev);
        }
        int action = ev.getActionMasked();
        if (action == 0) {
            this.mGestureAborted = false;
        }
        if (this.mGestureAborted) {
            return false;
        }
        if (action == 5) {
            this.mFlag = true;
        }
        switch (action) {
            case 0:
            case 5:
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
            case 6:
                boolean doIt = isPressed() && !this.mLongClicked;
                setPressed(false);
                if (this.mCode != 0) {
                    if (doIt) {
                        sendEvent(1, 0);
                        sendAccessibilityEvent(1);
                        playSoundEffect(0);
                    } else {
                        sendEvent(1, 32);
                    }
                } else if (doIt) {
                    performClick();
                }
                removeCallbacks(this.mCheckLongPress);
                this.mFlag = false;
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
                if (!(this.mCode == 0 || this.mFlag)) {
                    sendEvent(1, 32);
                }
                removeCallbacks(this.mCheckLongPress);
                this.mFlag = false;
                break;
        }
        return true;
    }

    public void playSoundEffect(final int soundConstant) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                KeyButtonView.this.mAudioManager.playSoundEffect(soundConstant, UserSwitchUtils.getCurrentUser());
                return false;
            }
        });
    }

    public void sendEvent(int action, int flags) {
        sendEvent(action, flags, SystemClock.uptimeMillis());
    }

    void sendEvent(int action, int flags, long when) {
        HwLog.i("TintImageView", "sendEvent:action=" + action + ", flag=" + flags);
        final int i = flags;
        final long j = when;
        final int i2 = action;
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                InputManager.getInstance().injectInputEvent(new KeyEvent(KeyButtonView.this.mDownTime, j, i2, KeyButtonView.this.mCode, (i & 128) != 0 ? 1 : 0, 0, -1, 0, (i | 8) | 64, 257), 0);
                return false;
            }
        });
    }

    public void abortCurrentGesture() {
        setPressed(false);
        this.mGestureAborted = true;
    }
}
