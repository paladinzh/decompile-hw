package com.android.systemui.flashlight;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings.Global;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.SysUIToast;
import com.android.systemui.statusbar.policy.BatteryController;
import com.android.systemui.statusbar.policy.BatteryController.BatteryStateChangeCallback;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.huawei.keyguard.inf.IFlashlightController.FlashlightListener;

public class FlashlightActivity extends Activity implements FlashlightListener, BatteryStateChangeCallback {
    private static final String TAG = FlashlightActivity.class.getSimpleName();
    BatteryController mBatterController = HwSystemUIApplication.getInstance().getBatteryController();
    int mBatteryLevel = 100;
    ImageButton mClickBtn = null;
    OnClickListener mClickListener = new OnClickListener() {
        public void onClick(View view) {
            boolean z = false;
            if (FlashlightActivity.this.mLightOnView == null) {
                HwLog.e(FlashlightActivity.TAG, "mLightOnView is null, return!!");
                FlashlightActivity.this.mClickBtn.setEnabled(false);
                return;
            }
            HwLog.i(FlashlightActivity.TAG, "click mLightOnView.getVisibility()" + FlashlightActivity.this.mLightOnView.getVisibility());
            if (!SystemUiUtil.isMonkeyRunning()) {
                FlashlightActivity flashlightActivity = FlashlightActivity.this;
                Context applicationContext = FlashlightActivity.this.getApplicationContext();
                if (FlashlightActivity.this.mLightOnView.getVisibility() != 0) {
                    z = true;
                }
                flashlightActivity.setLightOnOrOff(applicationContext, z);
            }
        }
    };
    private Context mContext;
    FlashlightController mFlashlightController = HwSystemUIApplication.getInstance().getFlashlightController();
    private boolean mIsFirstIn = false;
    ImageView mLightOffView = null;
    ImageView mLightOnView = null;
    private ContentObserver mStateChangeObserver = new ContentObserver(this.mUiHandler) {
        public void onChange(boolean selfChange) {
            boolean z = true;
            if (FlashlightActivity.this.mFlashlightController != null) {
                FlashlightActivity flashlightActivity = FlashlightActivity.this;
                FlashlightController flashlightController = FlashlightActivity.this.mFlashlightController;
                if (1 != FlashlightController.getState(FlashlightActivity.this.mContext)) {
                    z = false;
                }
                flashlightActivity.updateLightOnOffView(z);
            }
        }
    };
    protected final Handler mUiHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    boolean enabled = ((Boolean) msg.obj).booleanValue();
                    HwLog.i(FlashlightActivity.TAG, "mUiHandler MSG_SET_LIGHT_ON_OFF_VIEW , msg.obj:" + enabled);
                    if (FlashlightActivity.this.mLightOnView != null && FlashlightActivity.this.mLightOffView != null) {
                        if (!enabled) {
                            FlashlightActivity.this.mLightOnView.setVisibility(8);
                            FlashlightActivity.this.mLightOffView.setVisibility(0);
                            FlashlightActivity.this.mClickBtn.setSelected(false);
                            break;
                        }
                        FlashlightActivity.this.mLightOnView.setVisibility(0);
                        FlashlightActivity.this.mLightOffView.setVisibility(8);
                        FlashlightActivity.this.mClickBtn.setSelected(true);
                        break;
                    }
                    HwLog.e(FlashlightActivity.TAG, "mLightOnView == null || mLightOffView == null, return!");
                    return;
                    break;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(1);
        super.onCreate(savedInstanceState);
        this.mContext = this;
        Window window = getWindow();
        window.setFlags(67108864, 67108864);
        window.setFlags(134217728, 134217728);
        setContentView(R.layout.flashlight_main);
        this.mLightOnView = (ImageView) findViewById(R.id.lightOn);
        this.mLightOffView = (ImageView) findViewById(R.id.lightOff);
        this.mClickBtn = (ImageButton) findViewById(R.id.clickBtn);
        this.mClickBtn.setOnClickListener(this.mClickListener);
        this.mIsFirstIn = true;
        HwLog.i(TAG, "onCreate, add flashlight controller listener");
        this.mFlashlightController.addListener(this);
        this.mBatterController.addStateChangedCallback(this);
        getContentResolver().registerContentObserver(Global.getUriFor("flashlight_current_state"), true, this.mStateChangeObserver);
    }

    protected void onResume() {
        super.onResume();
        HwLog.i(TAG, "onResume, mIsFirstIn:" + this.mIsFirstIn);
        if (this.mIsFirstIn) {
            setLightOnOrOff(getApplicationContext(), true);
            this.mIsFirstIn = false;
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        HwLog.i(TAG, "onNewIntent mIsFirstIn:" + this.mIsFirstIn);
        this.mIsFirstIn = true;
    }

    protected void onDestroy() {
        super.onDestroy();
        HwLog.i(TAG, "onDestroy , removeListener");
        if (this.mFlashlightController != null) {
            this.mFlashlightController.removeListener(this);
        } else {
            HwLog.e(TAG, "onDestroy mFlashlightController is null");
        }
        this.mBatterController.removeStateChangedCallback(this);
        getContentResolver().unregisterContentObserver(this.mStateChangeObserver);
    }

    public void onBackPressed() {
        super.onBackPressed();
        HwLog.i(TAG, "onBackPressed , setFlashlight false");
        setLightOnOrOff(getApplicationContext(), false);
    }

    public void onFlashlightAvailabilityChanged(boolean available) {
        HwLog.i(TAG, "onFlashlightAvailabilityChanged available:" + available);
        if (!available) {
            updateLightOnOffView(false);
        }
    }

    public void onFlashlightError() {
        HwLog.i(TAG, "onFlashlightError");
        updateLightOnOffView(false);
    }

    public void onFlashlightChanged(boolean enabled) {
        HwLog.i(TAG, "onFlashlightChanged enabled:" + enabled);
        if (this.mLightOffView == null || this.mLightOnView == null) {
            HwLog.e(TAG, "mLightOffView == null || mLightOnView == null ,and return!!");
        } else {
            updateLightOnOffView(enabled);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        boolean z = true;
        super.onWindowFocusChanged(hasFocus);
        if (this.mFlashlightController != null) {
            FlashlightController flashlightController = this.mFlashlightController;
            if (1 != FlashlightController.getState(this.mContext)) {
                z = false;
            }
            updateLightOnOffView(z);
        }
    }

    private void setLightOnOrOff(Context context, final boolean enable) {
        HwLog.i(TAG, "setLightOnOrOff enable:" + enable);
        if (!enable || canOpenFlashLight()) {
            SystemUIThread.runAsync(new SimpleAsyncTask() {
                public boolean runInThread() {
                    if (FlashlightActivity.this.mFlashlightController != null) {
                        FlashlightActivity.this.mFlashlightController.setFlashlight(enable);
                    } else {
                        HwLog.e(FlashlightActivity.TAG, "setLightOnOrOff mFlashlightController is null");
                    }
                    return false;
                }
            });
            updateLightOnOffView(enable);
            return;
        }
        updateLightOnOffView(false);
        SysUIToast.makeText(getApplicationContext(), getApplicationContext().getString(R.string.systemui_flashlight_not_open), 0).show();
        this.mClickBtn.setEnabled(false);
    }

    private void updateLightOnOffView(boolean enabled) {
        Message msg = new Message();
        msg.what = 0;
        msg.obj = Boolean.valueOf(enabled);
        HwLog.i(TAG, "updateLightOnOffView msg.what:0 msg.obj:" + enabled);
        this.mUiHandler.sendMessage(msg);
    }

    public void onBatteryLevelChanged(int level, boolean pluggedIn, boolean charging) {
        this.mBatteryLevel = level;
    }

    public void onPowerSaveChanged(boolean isPowerSave) {
    }

    public boolean canOpenFlashLight() {
        return this.mBatteryLevel >= 5;
    }
}
