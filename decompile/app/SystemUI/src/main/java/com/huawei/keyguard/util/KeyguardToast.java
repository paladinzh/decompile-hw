package com.huawei.keyguard.util;

import android.app.INotificationManager;
import android.app.ITransientNotification.Stub;
import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.hwcontrol.HwWidgetFactory;
import android.hwcontrol.HwWidgetFactory.HwToast;
import android.os.Handler;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;

public class KeyguardToast {
    private static HwToast mHwToast = null;
    private static INotificationManager sService;
    final Context mContext;
    int mDuration;
    View mNextView;
    final TN mTN = new TN();

    private static class TN extends Stub {
        int mGravity;
        final Handler mHandler = new Handler();
        final Runnable mHide = new Runnable() {
            public void run() {
                TN.this.handleHide();
                TN.this.mNextView = null;
            }
        };
        float mHorizontalMargin;
        View mNextView;
        private final LayoutParams mParams = new LayoutParams();
        final Runnable mShow = new Runnable() {
            public void run() {
                TN.this.handleShow();
            }
        };
        float mVerticalMargin;
        View mView;
        WindowManager mWM;
        int mX;
        int mY;

        TN() {
            LayoutParams params = this.mParams;
            params.height = -2;
            params.width = -2;
            params.format = -3;
            params.windowAnimations = 16973828;
            params.type = 2009;
            params.setTitle("Toast");
            params.flags = 152;
        }

        public void show() {
            this.mHandler.post(this.mShow);
        }

        public void hide() {
            if (!this.mHandler.post(this.mHide)) {
                String threadInfo = "NULL";
                if (this.mHandler.getLooper() != null) {
                    Thread t = this.mHandler.getLooper().getThread();
                    if (t != null) {
                        threadInfo = "ThreadID[" + t.getId() + "]:" + t.toString();
                    }
                }
                HwLog.w("Toast", "Toast post hide failed in " + threadInfo + ", try hide immediate in " + ("ThreadID[" + Thread.currentThread().getId() + "]:" + Thread.currentThread().toString()));
                hideImmediate();
            }
        }

        public void hideImmediate() {
            HwLog.w("Toast", "HIDE IMMEDIATE: " + this + " mView=" + this.mView);
            if (this.mView != null && this.mWM != null) {
                if (this.mView.getParent() != null) {
                    HwLog.w("Toast", "REMOVE IMMEDIATE: " + this.mView + " in " + this);
                    try {
                        this.mWM.removeViewImmediate(this.mView);
                    } catch (Exception ex) {
                        HwLog.w("Toast", ex.getMessage(), ex);
                    }
                }
                this.mView = null;
            }
        }

        public void handleShow() {
            if (this.mView != this.mNextView) {
                handleHide();
                this.mView = this.mNextView;
                Context context = this.mView.getContext().getApplicationContext();
                if (context == null) {
                    context = this.mView.getContext();
                }
                this.mWM = (WindowManager) context.getSystemService("window");
                int gravity = Gravity.getAbsoluteGravity(this.mGravity, this.mView.getContext().getResources().getConfiguration().getLayoutDirection());
                this.mParams.gravity = gravity;
                if ((gravity & 7) == 7) {
                    this.mParams.horizontalWeight = 1.0f;
                }
                if ((gravity & 112) == 112) {
                    this.mParams.verticalWeight = 1.0f;
                }
                this.mParams.x = this.mX;
                this.mParams.y = this.mY;
                this.mParams.verticalMargin = this.mVerticalMargin;
                this.mParams.horizontalMargin = this.mHorizontalMargin;
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mWM.addView(this.mView, this.mParams);
                trySendAccessibilityEvent();
            }
        }

        private void trySendAccessibilityEvent() {
            AccessibilityManager accessibilityManager = AccessibilityManager.getInstance(this.mView.getContext());
            if (accessibilityManager.isEnabled()) {
                AccessibilityEvent event = AccessibilityEvent.obtain(64);
                event.setClassName(getClass().getName());
                event.setPackageName(this.mView.getContext().getPackageName());
                this.mView.dispatchPopulateAccessibilityEvent(event);
                accessibilityManager.sendAccessibilityEvent(event);
            }
        }

        public void handleHide() {
            if (this.mView != null) {
                if (this.mView.getParent() != null) {
                    this.mWM.removeView(this.mView);
                }
                this.mView = null;
            }
        }
    }

    public KeyguardToast(Context context) {
        this.mContext = context;
        this.mTN.mY = context.getResources().getDimensionPixelSize(17104918);
        this.mTN.mGravity = context.getResources().getInteger(17694865);
    }

    public void show() {
        if (this.mNextView == null) {
            throw new RuntimeException("setView must have been called");
        }
        INotificationManager service = getService();
        String pkg = this.mContext.getPackageName();
        TN tn = this.mTN;
        tn.mNextView = this.mNextView;
        try {
            service.enqueueToast(pkg, tn, this.mDuration);
        } catch (RemoteException e) {
        }
    }

    public void cancel() {
        this.mTN.hide();
        try {
            getService().cancelToast(this.mContext.getPackageName(), this.mTN);
        } catch (RemoteException e) {
        }
    }

    public void setGravity(int gravity, int xOffset, int yOffset) {
        this.mTN.mGravity = gravity;
        this.mTN.mX = xOffset;
        this.mTN.mY = yOffset;
    }

    public static KeyguardToast makeText(Context context, CharSequence text, int duration) {
        if (context == null) {
            HwLog.w("Toast", "makeText() context is null");
            return null;
        }
        Context hwContext = KeyguardUtils.getHwThemeContext(context);
        if (hwContext == null) {
            HwLog.w("Toast", "makeText() hwContext is null");
            return null;
        }
        View v;
        KeyguardToast result = new KeyguardToast(hwContext);
        LayoutInflater inflate = (LayoutInflater) hwContext.getSystemService("layout_inflater");
        mHwToast = HwWidgetFactory.getHwToast(hwContext, null, null);
        if (mHwToast != null) {
            v = mHwToast.layoutInflate(hwContext);
        } else {
            v = inflate.inflate(17367296, null);
        }
        TextView tv = (TextView) v.findViewById(16908299);
        if (tv != null) {
            tv.setText(text);
        }
        result.mNextView = v;
        result.mDuration = duration;
        return result;
    }

    public static KeyguardToast makeText(Context context, int resId, int duration) throws NotFoundException {
        return makeText(context, context.getResources().getText(resId), duration);
    }

    private static INotificationManager getService() {
        if (sService != null) {
            return sService;
        }
        sService = INotificationManager.Stub.asInterface(ServiceManager.getService("notification"));
        return sService;
    }

    public static void showKeyguardToast(Context context, String msg) {
        KeyguardToast kt = makeText(context, (CharSequence) msg, 0);
        if (kt != null) {
            kt.show();
        }
    }
}
