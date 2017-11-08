package huawei.com.android.server.policy;

import android.app.ActivityManager;
import android.content.Context;
import android.cover.CoverManager;
import android.database.ContentObserver;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewParent;
import android.view.ViewRootImpl;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerPolicy;
import android.widget.FrameLayout;
import com.android.server.security.trustcircle.tlv.command.register.RET_REG_CANCEL;

public class HwScreenOnProximityLock {
    private static final boolean DEBUG = false;
    private static final long DELAY = 1000;
    private static final boolean HWFLOW;
    private static final String KEY_TOUCH_DISABLE_MODE = "touch_disable_mode";
    private static final int MSG_FIRST_PROXIMITY_IN_TIME = 1;
    private static final int MSG_SHOW_HINT_VIEW = 2;
    private static final String SCREENON_TAG = "ScreenOn";
    private static final String TAG = "HwScreenOnProximityLock";
    private static final float TYPICAL_PROXIMITY_THRESHOLD = 5.0f;
    private static final String sProximityWndName = "Emui:ProximityWnd";
    private Context mContext;
    private CoverManager mCoverManager;
    private Handler mHandler;
    private boolean mHeld;
    private ProximitySensorListener mListener;
    private final Object mLock = new Object();
    private float mProximityThreshold;
    private FrameLayout mProximityView;
    private SensorManager mSensorManager;
    public ContentObserver mTouchDisableObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            boolean touchDisable = true;
            if (System.getIntForUser(HwScreenOnProximityLock.this.mContext.getContentResolver(), HwScreenOnProximityLock.KEY_TOUCH_DISABLE_MODE, 1, ActivityManager.getCurrentUser()) == 0) {
                touchDisable = false;
            }
            if (!touchDisable && HwScreenOnProximityLock.this.isShowing()) {
                HwScreenOnProximityLock.this.releaseLock();
            }
        }
    };
    private WindowManager mWindowManager;

    private class ProximitySensorListener implements SensorEventListener {
        private boolean mIsProximity;

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }

        public void onSensorChanged(SensorEvent event) {
            boolean z = false;
            float d = event.values[0];
            if (d >= 0.0f && d < HwScreenOnProximityLock.this.mProximityThreshold) {
                z = true;
            }
            this.mIsProximity = z;
            handleSensorChanges();
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private void handleSensorChanges() {
            if (HwScreenOnProximityLock.this.mCoverManager != null) {
                boolean isCoverOpen = HwScreenOnProximityLock.this.mCoverManager.isCoverOpen();
                if (this.mIsProximity && isCoverOpen) {
                    if (HwScreenOnProximityLock.this.mHandler.hasMessages(1)) {
                        HwScreenOnProximityLock.this.mHandler.removeMessages(1);
                    }
                    synchronized (HwScreenOnProximityLock.this.mLock) {
                        if (HwScreenOnProximityLock.this.mProximityView == null) {
                            HwScreenOnProximityLock.this.preparePoriximityView();
                        }
                    }
                } else {
                    synchronized (HwScreenOnProximityLock.this.mLock) {
                        if (isCoverOpen) {
                            if (HwScreenOnProximityLock.this.mProximityView == null) {
                            }
                        }
                    }
                }
            }
        }
    }

    static {
        boolean isLoggable = !Log.HWINFO ? Log.HWModuleLog ? Log.isLoggable(SCREENON_TAG, 4) : false : true;
        HWFLOW = isLoggable;
    }

    public HwScreenOnProximityLock(Context context) {
        if (context == null) {
            Log.w(TAG, "HwScreenOnProximityLock context is null");
            return;
        }
        this.mContext = context;
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mCoverManager = new CoverManager();
        this.mListener = new ProximitySensorListener();
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        HwScreenOnProximityLock.this.releaseLock();
                        return;
                    case 2:
                        HwScreenOnProximityLock.this.showHintView();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void acquireLock(WindowManagerPolicy policy) {
        synchronized (this.mLock) {
            if (this.mHeld) {
                Log.w(TAG, "acquire Lock: return because sensor listener has been held  = " + this.mHeld);
            } else if (policy == null) {
                Log.w(TAG, "acquire Lock: return because get Window Manager policy is null");
            } else {
                this.mContext.getContentResolver().registerContentObserver(System.getUriFor(KEY_TOUCH_DISABLE_MODE), true, this.mTouchDisableObserver);
                Sensor sensor = this.mSensorManager.getDefaultSensor(8);
                if (sensor == null) {
                    Log.w(TAG, "acquire Lock: return because of proximity sensor is not existed");
                    return;
                }
                this.mProximityThreshold = Math.min(sensor.getMaximumRange(), TYPICAL_PROXIMITY_THRESHOLD);
                this.mHeld = this.mSensorManager.registerListener(this.mListener, sensor, 3);
                if (this.mHeld) {
                    this.mHandler.sendEmptyMessageDelayed(1, 1000);
                } else {
                    Log.w(TAG, "registerListener fail");
                }
            }
        }
    }

    public void releaseLock() {
        synchronized (this.mLock) {
            if (this.mHeld) {
                removeProximityView();
                this.mSensorManager.unregisterListener(this.mListener);
                this.mHeld = false;
                this.mHandler.removeCallbacksAndMessages(null);
                this.mContext.getContentResolver().unregisterContentObserver(this.mTouchDisableObserver);
                return;
            }
            Log.w(TAG, "releaseLock: return because sensor listener is held = " + this.mHeld);
        }
    }

    public boolean isShowing() {
        boolean z;
        synchronized (this.mLock) {
            z = this.mProximityView != null;
        }
        return z;
    }

    public void forceShowHint() {
        this.mHandler.sendEmptyMessage(2);
    }

    private void preparePoriximityView() {
        removeProximityView();
        synchronized (this.mLock) {
            View view = View.inflate(this.mContext, 34013254, null);
            if (view instanceof FrameLayout) {
                this.mProximityView = (FrameLayout) view;
                this.mProximityView.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        HwScreenOnProximityLock.this.showHintView();
                        return false;
                    }
                });
                LayoutParams params = new LayoutParams(-1, -1, 2100, 134223104, -2);
                params.inputFeatures |= 4;
                params.privateFlags |= RET_REG_CANCEL.ID;
                params.hwFlags |= 4;
                params.setTitle(sProximityWndName);
                if (HWFLOW) {
                    Log.i(TAG, "preparePoriximityView addView ");
                }
                this.mWindowManager.addView(this.mProximityView, params);
                return;
            }
        }
    }

    private void showHintView() {
        synchronized (this.mLock) {
            if (this.mProximityView == null) {
                return;
            }
            View hintView = this.mProximityView.findViewById(34603178);
            if (hintView == null) {
                return;
            }
            hintView.setVisibility(0);
        }
    }

    private void removeProximityView() {
        synchronized (this.mLock) {
            if (this.mProximityView != null) {
                ViewParent vp = this.mProximityView.getParent();
                if (this.mWindowManager == null || !(vp instanceof ViewRootImpl)) {
                    Log.w(TAG, "removeView fail: mWindowManager = " + this.mWindowManager + ", viewparent = " + vp);
                } else {
                    if (HWFLOW) {
                        Log.i(TAG, "removeProximityView success vp " + vp);
                    }
                    this.mWindowManager.removeView(this.mProximityView);
                }
                this.mProximityView = null;
            }
        }
    }
}
