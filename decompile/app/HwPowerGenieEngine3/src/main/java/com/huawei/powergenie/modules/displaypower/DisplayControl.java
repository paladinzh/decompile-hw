package com.huawei.powergenie.modules.displaypower;

import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.util.Log;
import com.huawei.powergenie.api.BaseModule;
import com.huawei.powergenie.core.PowerAction;

public final class DisplayControl extends BaseModule {
    private static final boolean CAMERA_CABC = SystemProperties.getBoolean("ro.config.pg_camera_cabc", false);
    private BacklightControl mBacklightControl;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    DisplayControl.this.setCABC(true);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsReStartAfterCrash = false;
    private boolean mSavePowerCABC = false;

    public void onCreate() {
        super.onCreate();
        addAction(208);
        addAction(350);
        addAction(254);
        addAction(358);
        addAction(230);
        addAction(324);
        if (CAMERA_CABC || getCoreContext().isQcommPlatform()) {
            addAction(221);
            addAction(244);
        }
        addAction(204);
        addAction(233);
        addAction(246);
        addAction(247);
        if (getCoreContext().isHisiPlatform()) {
            addAction(333);
            addAction(334);
        }
        this.mBacklightControl = new BacklightControl(getCoreContext(), getModId());
    }

    public void onStart() {
        super.onStart();
        this.mBacklightControl.handleStart(getPowerMode());
    }

    public boolean handleAction(PowerAction action) {
        if (!super.handleAction(action)) {
            return true;
        }
        this.mBacklightControl.handleBacklightAction(action);
        switch (action.getActionId()) {
            case 204:
            case 221:
            case 233:
            case 246:
                postCABC(20);
                break;
            case 208:
            case 244:
            case 247:
                setCABC(false);
                break;
            case 324:
                this.mIsReStartAfterCrash = true;
                break;
            default:
                setCABC(false);
                break;
        }
        return true;
    }

    private void postCABC(long delayMillis) {
        Message msg = this.mHandler.obtainMessage(100);
        this.mHandler.removeMessages(100);
        this.mHandler.sendMessageDelayed(msg, delayMillis);
    }

    private void setCABC(boolean savePower) {
        this.mHandler.removeMessages(100);
        if (this.mIsReStartAfterCrash || this.mSavePowerCABC != savePower) {
            if (getCoreContext().setCABC(savePower)) {
                this.mSavePowerCABC = savePower;
                Log.d("DisplayControl", "OK! set cabc save power:" + savePower);
            } else {
                Log.e("DisplayControl", "Fail! set cabc save power: " + savePower);
            }
            if (this.mIsReStartAfterCrash) {
                Log.i("DisplayControl", "PG restart after crash setCABC " + savePower);
                this.mIsReStartAfterCrash = false;
            }
        }
    }
}
