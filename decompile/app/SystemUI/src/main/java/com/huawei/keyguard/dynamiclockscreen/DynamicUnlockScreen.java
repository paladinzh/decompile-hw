package com.huawei.keyguard.dynamiclockscreen;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.keyguard.R$id;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$HwLockScreenReal;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LockScreenCallback;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.events.CallLogMonitor.CallLogInfo;
import com.huawei.keyguard.events.HwUpdateMonitor;
import com.huawei.keyguard.events.HwUpdateMonitor.HwUpdateCallback;
import com.huawei.keyguard.events.MessageMonitor.MessageInfo;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import com.huawei.keyguard.theme.ThemeCfg;
import com.huawei.keyguard.util.HwLog;
import java.lang.reflect.InvocationTargetException;

public class DynamicUnlockScreen extends RelativeLayout implements HwUnlockInterface$HwLockScreenReal {
    private Object mEnginerClass;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 512:
                    HwKeyguardPolicy.getInst().dismiss();
                    break;
                case 1000:
                    DynamicUnlockScreen.this.synchronizeData("unreadmms", String.valueOf(msg.arg1));
                    break;
                case 1001:
                    DynamicUnlockScreen.this.synchronizeData("misscall", String.valueOf(msg.arg1));
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private KeyguardUpdateMonitorCallback mInfoCallback = new KeyguardUpdateMonitorCallback() {
        public void onScreenTurnedOff() {
            Log.d("DynamicUnlockScreen", "controlAnimation stop");
            DynamicUnlockUtils.excuteEngineMethod(DynamicUnlockScreen.this.mEnginerClass, "stop");
        }

        public void onScreenTurnedOn() {
            Log.d("DynamicUnlockScreen", "controlAnimation play");
            DynamicUnlockUtils.excuteEngineMethod(DynamicUnlockScreen.this.mEnginerClass, "play");
        }
    };
    private boolean mIsUnlock = false;
    private HwUnlockInterface$LockScreenCallback mLockScreenCallback;
    HwUpdateCallback mUpdateCallback = new HwUpdateCallback() {
        public void onNewMessageChange(MessageInfo info) {
            if (info == null) {
                Log.i("DynamicUnlockScreen", "onNewMessageChange info is null - no change happened");
                return;
            }
            Log.i("DynamicUnlockScreen", "onNewMessageChange missedCount=" + info.getUnReadCount());
            Message message = DynamicUnlockScreen.this.mHandler.obtainMessage(1000);
            message.arg1 = info.getUnReadCount();
            DynamicUnlockScreen.this.mHandler.sendMessage(message);
        }

        public void onCalllogChange(CallLogInfo info) {
            if (info == null) {
                Log.i("DynamicUnlockScreen", "onCalllogChange info is null - no change happened");
                return;
            }
            Log.i("DynamicUnlockScreen", "onCalllogChange missedCount=" + info.getMissedcount());
            Message message = DynamicUnlockScreen.this.mHandler.obtainMessage(1001);
            message.arg1 = info.getMissedcount();
            DynamicUnlockScreen.this.mHandler.sendMessage(message);
        }
    };

    protected void onFinishInflate() {
        super.onFinishInflate();
    }

    public DynamicUnlockScreen(Context context) {
        super(context, null);
    }

    public DynamicUnlockScreen(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public boolean init(String enginePackageName, String subPathName) {
        try {
            Class<?> obj = DynamicUnlockUtils.loadClass(this.mContext, enginePackageName);
            if (obj == null) {
                return false;
            }
            Class[] paramTypes = new Class[]{Context.class, String.class};
            String unlockPath = ThemeCfg.getUnlockDir();
            if (!TextUtils.isEmpty(subPathName)) {
                unlockPath = unlockPath.concat(subPathName);
            }
            this.mEnginerClass = obj.getConstructor(paramTypes).newInstance(new Object[]{this.mContext.getApplicationContext(), unlockPath});
            setUnlockRunnable();
            View view = creatView();
            if (view != null) {
                addView(view, -2, -2);
                KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mInfoCallback);
                HwUpdateMonitor.getInstance(getContext()).registerCallback(this.mUpdateCallback);
                return true;
            }
            return false;
        } catch (NoSuchMethodException e) {
            Log.w("DynamicUnlockScreen", "Error info :", e);
        } catch (InstantiationException e2) {
            Log.w("DynamicUnlockScreen", "Error info :", e2);
        } catch (IllegalAccessException e3) {
            Log.w("DynamicUnlockScreen", "Error info :", e3);
        } catch (IllegalArgumentException e4) {
            Log.w("DynamicUnlockScreen", "Error info :", e4);
        } catch (InvocationTargetException e5) {
            Log.w("DynamicUnlockScreen", "Error info :", e5);
        } catch (Exception e6) {
            Log.w("DynamicUnlockScreen", "Error info :", e6);
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        KeyguardUpdateMonitor.getInstance(this.mContext).removeCallback(this.mInfoCallback);
        HwUpdateMonitor.getInstance(getContext()).unRegisterCallback(this.mUpdateCallback);
    }

    public void unlockByThirdKeyguard() {
        Log.w("DynamicUnlockScreen", "unlockByThirdKeyguard to send unlock message.");
        this.mIsUnlock = true;
        this.mHandler.sendEmptyMessage(512);
    }

    private void setUnlockRunnable() {
        Runnable unlockRunnable = new Runnable() {
            public void run() {
                DynamicUnlockScreen.this.unlockByThirdKeyguard();
                if (DynamicUnlockScreen.this.mEnginerClass != null) {
                    DynamicUnlockScreen.this.mEnginerClass = null;
                }
            }
        };
        DynamicUnlockUtils.excuteEngineMethod(this.mEnginerClass, "setUnlockRunnable", new Class[]{Runnable.class}, new Object[]{unlockRunnable});
    }

    private View creatView() {
        View returnObject = DynamicUnlockUtils.excuteEngineMethod(this.mEnginerClass, "createUnlockView");
        if (returnObject == null || !(returnObject instanceof View)) {
            return null;
        }
        return returnObject;
    }

    public void setLockScreenCallback(HwUnlockInterface$LockScreenCallback callback) {
        this.mLockScreenCallback = callback;
    }

    public void setLockPatternUtils(LockPatternUtils lockPatternUtils) {
    }

    public void onBatteryInfoChanged() {
    }

    public void onTimeChanged() {
    }

    public void onPhoneStateChanged() {
    }

    public boolean needsInput() {
        return false;
    }

    public void onResume() {
        hiddenHuaweiKeyguardView();
        if (KeyguardCfg.isExtremePowerSavingMode()) {
            HwKeyguardUpdateMonitor.getInstance().dispatchSetBackground(null);
        }
        if (this.mLockScreenCallback != null) {
            boolean ownerInfoEnabled = this.mLockScreenCallback.isShowOwnerInfo();
            CharSequence ownerInfo = ownerInfoEnabled ? this.mLockScreenCallback.getOwnerInfo() : null;
            Log.w("DynamicUnlockScreen", "mLockScreenCallback.isShowOwnerInfo() " + ownerInfoEnabled);
            Log.w("DynamicUnlockScreen", "mLockScreenCallback.getOwnerInfo()() " + this.mLockScreenCallback.getOwnerInfo());
            if (!TextUtils.isEmpty(ownerInfo)) {
                synchronizeData("ownerinfo", String.valueOf(ownerInfo));
            }
        }
    }

    public void playDynamic() {
        HwLog.i("DynamicUnlockScreen", "Excute play ");
        DynamicUnlockUtils.excuteEngineMethod(this.mEnginerClass, "play");
    }

    private void synchronizeData(String type, String value) {
        DynamicUnlockUtils.excuteEngineMethod(this.mEnginerClass, "synchronizeData", new Class[]{String.class, String.class}, new Object[]{type, value});
    }

    public boolean onTouchEvent(MotionEvent event) {
        Log.w("DynamicUnlockScreen", "onTouchEvent is in Dynamic on touch envent.");
        return super.onTouchEvent(event);
    }

    private void hiddenHuaweiKeyguardView() {
        View keyguardStatusView = getRootView().findViewById(R$id.keyguard_clock_container);
        if (keyguardStatusView != null) {
            keyguardStatusView.setVisibility(8);
        }
        View cameraDraglayerView = getRootView().findViewById(R$id.camera_container);
        if (cameraDraglayerView != null) {
            cameraDraglayerView.setVisibility(8);
        }
    }
}
