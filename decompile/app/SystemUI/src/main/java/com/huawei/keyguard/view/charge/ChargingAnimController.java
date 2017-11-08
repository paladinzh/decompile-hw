package com.huawei.keyguard.view.charge;

import android.content.Context;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import com.android.keyguard.R$id;
import com.android.keyguard.R$layout;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.cover.CoverViewManager;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.events.AppHandler;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.KgViewUtils;
import com.huawei.keyguard.view.charge.e50.ChargeEffectsLayerViewE50;
import com.huawei.keyguard.view.charge.e50.ChargeInfoView;
import com.huawei.keyguard.view.charge.e50.UnlockChargingView;

public class ChargingAnimController implements Runnable, Callback {
    private static ChargingAnimController mInst = null;
    private final Runnable mAddRunner = new Runnable() {
        public void run() {
            ChargingAnimController.this.addChargingView();
        }
    };
    private ChargeInfoView mChargePercentView;
    private Context mContext;
    private View mCurrentChargingView = null;
    private final Runnable mRemoveRunner = new Runnable() {
        public void run() {
            ChargingAnimController.this.removeChargingView();
        }
    };
    private UnlockChargingView mRenderView;
    private View mRootView = null;
    private Handler mUIHandler = null;
    Runnable removeViewAnimationEnd = new Runnable() {
        public void run() {
            if (ChargingAnimController.this.mRenderView != null) {
                HwLog.d("ChargingAnimController", "stopRender");
                ChargingAnimController.this.mRenderView.setVisibility(8);
                ChargingAnimController.this.mRenderView.stopRender();
            }
            ChargingAnimController.this.mChargePercentView.setVisibility(8);
            ChargingAnimController.this.mCurrentChargingView.setVisibility(8);
            WindowManager wm = (WindowManager) ChargingAnimController.this.mContext.getSystemService("window");
            if (wm == null) {
                HwLog.e("ChargingAnimController", "removeView chargingView fail when get WINDOW_SERVICE.");
                ChargingAnimController.this.mCurrentChargingView = null;
                return;
            }
            wm.removeView(ChargingAnimController.this.mCurrentChargingView);
            ChargingAnimController.this.mCurrentChargingView = null;
        }
    };

    public ChargingAnimController(Context context) {
        this.mContext = context.getApplicationContext();
        this.mUIHandler = GlobalContext.getUIHandler();
    }

    public static ChargingAnimController getInst(Context context) {
        ChargingAnimController chargingAnimController;
        synchronized (ChargingAnimController.class) {
            if (mInst == null) {
                mInst = new ChargingAnimController(context);
            }
            chargingAnimController = mInst;
        }
        return chargingAnimController;
    }

    public void onKeyguardExit() {
        if (this.mCurrentChargingView != null && this.mRootView != null) {
            if (this.mRenderView != null) {
                this.mRenderView.setVisibility(8);
            }
            this.mCurrentChargingView.setVisibility(8);
            removeChargingView();
        }
    }

    public void onPluginStateChanged(View rootView) {
        this.mRootView = rootView;
        this.mUIHandler.removeCallbacks(this);
        if (HwKeyguardUpdateMonitor.getInstance(this.mContext).isDeviceInteractive()) {
            this.mUIHandler.postDelayed(this, 100);
            HwLog.i("ChargingAnimController", "Battery plugin status changed in screen off");
            return;
        }
        this.mUIHandler.postDelayed(this, 100);
        HwLog.i("ChargingAnimController", "Battery plugin status changed");
    }

    public boolean isChargingViewVisible() {
        boolean z = false;
        if (this.mCurrentChargingView == null) {
            return false;
        }
        if (this.mCurrentChargingView.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    public void removeChargingView() {
        if (this.mCurrentChargingView == null) {
            HwLog.w("ChargingAnimController", "Remove charging skip as not exist");
        } else if (this.mCurrentChargingView.getParent() == null) {
            HwLog.e("ChargingAnimController", "Remove charging skip as not attached to window");
        } else {
            this.mCurrentChargingView.animate().alpha(0.0f).setStartDelay(0).setDuration(300).withLayer().withEndAction(this.removeViewAnimationEnd).start();
            HwLog.d("ChargingAnimController", "Remove charging view finish");
            AppHandler.removeListener(this);
        }
    }

    private void addChargingView() {
        if (this.mContext == null || this.mRootView == null) {
            HwLog.w("ChargingAnimController", "Add charging view is skiped as no context.");
        } else if (this.mCurrentChargingView != null) {
            HwLog.w("ChargingAnimController", "Add charging View skiped as already create . " + this.mCurrentChargingView.getParent());
        } else {
            HwKeyguardUpdateMonitor updater = HwKeyguardUpdateMonitor.getInstance(this.mContext);
            if (updater == null || (!updater.isOccluded() && updater.isShowing() && CoverViewManager.getInstance(this.mContext).isCoverOpen())) {
                LayoutInflater inflater = KgViewUtils.createLayoutInflater(this.mContext);
                if (inflater != null) {
                    AppHandler.addListener(this);
                    ChargeEffectsLayerViewE50 chargeView = (ChargeEffectsLayerViewE50) inflater.inflate(R$layout.charge_effects_layer_e50, null);
                    this.mChargePercentView = (ChargeInfoView) chargeView.findViewById(R$id.charge_info_view);
                    this.mRenderView = chargeView.getAnimateView();
                    addToWindow(chargeView);
                    chargeView.setVisibility(0);
                    chargeView.setAlpha(0.0f);
                    chargeView.animate().alpha(1.0f).setStartDelay(0).setDuration(300).withLayer().start();
                    this.mCurrentChargingView = chargeView;
                    HwLog.w("ChargingAnimController", "ChargingView window added");
                    return;
                }
                return;
            }
            HwLog.w("ChargingAnimController", "Add charging view is skiped as occluded or not showing.");
        }
    }

    private void addToWindow(View view) {
        WindowManager wm = (WindowManager) this.mContext.getSystemService("window");
        if (wm == null) {
            HwLog.e("ChargingAnimController", "AddChargingScreen fail when get WINDOW_SERVICE.");
        } else if (view == null) {
            HwLog.e("ChargingAnimController", "addToWindow null target.");
        } else {
            LayoutParams viewParams = new LayoutParams(2100);
            viewParams.height = -1;
            viewParams.width = -1;
            viewParams.setTitle("ChargingAnimView");
            viewParams.flags |= 67109632;
            viewParams.privateFlags |= -2147483632;
            viewParams.isEmuiStyle = 1;
            viewParams.inputFeatures |= 4;
            if (KeyguardCfg.isDefaultPortOrientation()) {
                viewParams.screenOrientation = 1;
            } else {
                viewParams.screenOrientation = 14;
            }
            wm.addView(view, viewParams);
        }
    }

    public void run() {
        if (!BatteryStateInfo.getInst().isCharge()) {
            this.mUIHandler.removeCallbacks(this.mAddRunner);
            this.mUIHandler.removeCallbacks(this.mRemoveRunner);
            this.mUIHandler.post(this.mRemoveRunner);
            if (this.mCurrentChargingView == null) {
                HwLog.w("ChargingAnimController", "skip remove view.");
            }
        } else if (this.mCurrentChargingView != null) {
            HwLog.w("ChargingAnimController", "skip add charging window as already show.");
        } else {
            this.mUIHandler.postDelayed(this.mAddRunner, 1300);
            this.mUIHandler.removeCallbacks(this.mRemoveRunner);
            HwLog.w("ChargingAnimController", "postDelayed mRemoveRunner");
            this.mUIHandler.postDelayed(this.mRemoveRunner, 10300);
        }
    }

    public boolean handleMessage(Message msg) {
        if (!isChargingViewVisible()) {
            return false;
        }
        switch (msg.what) {
            case 10:
                HwKeyguardUpdateMonitor updater = HwKeyguardUpdateMonitor.getInstance(this.mContext);
                HwLog.d("ChargingAnimController", "MSG_KEYGUARD_STATE_CHANGED:" + updater.isOccluded());
                if (!(!updater.isOccluded() && updater.isShowing() && CoverViewManager.getInstance(this.mContext).isCoverOpen())) {
                    removeChargingView();
                    break;
                }
            case 11:
            case 13:
                HwLog.d("ChargingAnimController", "MSG_KEYGUARD_SCREENOFF Or EXIT");
                removeChargingView();
                break;
            case 30:
                HwLog.d("ChargingAnimController", "MSG_KEYGUARD_ADD_COVER");
                this.mUIHandler.post(this.mRemoveRunner);
                break;
            case 100:
                HwLog.d("ChargingAnimController", "MSG_BATTERY_STATE_CHANGE");
                if (!(this.mRenderView == null || this.mChargePercentView == null || !"huawei.intent.action.BATTERY_QUICK_CHARGE".equals(msg.obj))) {
                    this.mRenderView.startRender(0, BatteryStateInfo.getInst().getChargingMode(), BatteryStateInfo.getInst().getChargeLevel());
                    this.mChargePercentView.initialViewViaChargeStatus();
                    this.mUIHandler.removeCallbacks(this.mRemoveRunner);
                    this.mUIHandler.postDelayed(this.mRemoveRunner, 9000);
                    break;
                }
        }
        return false;
    }
}
