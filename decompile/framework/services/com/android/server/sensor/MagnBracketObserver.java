package com.android.server.sensor;

import android.app.ActivityManagerNative;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.ContentObserver;
import android.database.Cursor;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.Slog;
import com.android.server.am.HwBroadcastRadarUtil;

public class MagnBracketObserver {
    static final String ACTION_MAGN_BRACKET_ATTACH = "com.huawei.magnbracket.action.ATTACH";
    static final boolean DBG = true;
    static final int DELAY_CHECK_PROVIDER_PUBLISH_TIME = 1000;
    static final int DISABLED = 0;
    static final int ENABLED = 1;
    static final String HWVDRIVE_PACKAGE_NAME = "com.huawei.vdrive";
    static final Uri HWVDRIVE_URI = Uri.parse("content://com.huawei.vdrive/setting");
    static final int MAX_RETRY_COUNT = 15;
    static final int MSG_CHECK_PROVIDER_PUBLISH = 2;
    static final int MSG_MAGN_BRACKET_SENSOR_SWITCH_CHANGED = 1;
    static final int SENSOR_TYPE_MAGN_BRACKET = 10009;
    static final int STATE_ATTACH = 1;
    static final int STATE_DETACH = 0;
    static final String TAG = "MagnBracketObserver";
    private int mCheckCount = 0;
    private final ContentObserver mContentObserver = new ContentObserver(null) {
        public void onChange(boolean selfChange, Uri u) {
            Slog.d(MagnBracketObserver.TAG, "onChange: selfChange = " + selfChange);
            MagnBracketObserver.this.mHandler.removeMessages(2);
            if (MagnBracketObserver.this.mHandler.hasMessages(1)) {
                MagnBracketObserver.this.mHandler.removeMessages(1);
            }
            MagnBracketObserver.this.mHandler.sendEmptyMessage(1);
        }
    };
    private Context mContext = null;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MagnBracketObserver.this.doMagnBracketSensorSwitchChanged();
                    return;
                case 2:
                    MagnBracketObserver.this.checkProviderPublish();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mMagnBracketSensorEnabled = false;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                Slog.e(MagnBracketObserver.TAG, "onReceive: intent is null");
                return;
            }
            String action = intent.getAction();
            if (action == null) {
                Slog.e(MagnBracketObserver.TAG, "onReceive: action is null");
                return;
            }
            Slog.d(MagnBracketObserver.TAG, "onReceive: action = " + action);
            if ("android.intent.action.PACKAGE_REMOVED".equals(action)) {
                Uri data = intent.getData();
                if (data != null) {
                    String pkgName = data.getSchemeSpecificPart();
                    Slog.d(MagnBracketObserver.TAG, "onReceive: pkgName = " + pkgName + ", mMagnBracketSensorEnabled = " + MagnBracketObserver.this.mMagnBracketSensorEnabled);
                    if (MagnBracketObserver.HWVDRIVE_PACKAGE_NAME.equals(pkgName) && MagnBracketObserver.this.mMagnBracketSensorEnabled) {
                        MagnBracketObserver.this.mMagnBracketSensorEnabled = false;
                        MagnBracketObserver.this.unregisterMagnBracketSensor();
                    }
                }
            }
        }
    };
    private Sensor mSensor = null;
    private final SensorEventListener mSensorEventListener = new SensorEventListener() {
        public void onSensorChanged(SensorEvent event) {
            int status = (int) event.values[0];
            Slog.d(MagnBracketObserver.TAG, "onSensorChanged: status = " + status);
            if (ActivityManagerNative.isSystemReady()) {
                if (status == 1) {
                    Slog.d(MagnBracketObserver.TAG, "MAGN BRACKET sensor attached, notify app");
                    Intent intent = new Intent(MagnBracketObserver.ACTION_MAGN_BRACKET_ATTACH);
                    intent.addFlags(536870912);
                    MagnBracketObserver.this.mContext.sendBroadcast(intent, "huawei.permission.MAGN_BRACKET_SENSORS");
                }
                return;
            }
            Slog.w(MagnBracketObserver.TAG, "system not ready");
        }

        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    };
    private SensorManager mSensorManager = null;

    public static MagnBracketObserver getInstance(Context context) {
        return new MagnBracketObserver(context);
    }

    private MagnBracketObserver(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        this.mSensorManager = (SensorManager) this.mContext.getSystemService("sensor");
        this.mSensor = this.mSensorManager.getDefaultSensor(SENSOR_TYPE_MAGN_BRACKET, DBG);
        boolean isHwVdriveExist = isHwVdriveExist();
        Slog.d(TAG, "init: mSensor = " + this.mSensor + ", isHwVdriveExist = " + isHwVdriveExist);
        if (isHwVdriveExist) {
            boolean isProviderPublish = isProviderPublish();
            Slog.d(TAG, "init: isProviderPublish = " + isProviderPublish);
            if (isProviderPublish) {
                this.mMagnBracketSensorEnabled = isMagnBracketSensorEnabled();
                if (this.mMagnBracketSensorEnabled) {
                    registerMagnBracketSensor();
                }
            } else {
                this.mHandler.sendEmptyMessageDelayed(2, 1000);
            }
        }
        this.mContext.getContentResolver().registerContentObserver(HWVDRIVE_URI, DBG, this.mContentObserver);
        registerPackageReceiver();
    }

    private void registerMagnBracketSensor() {
        Slog.d(TAG, "registerMagnBracketSensor: mSensor = " + this.mSensor);
        if (this.mSensor != null) {
            this.mSensorManager.registerListener(this.mSensorEventListener, this.mSensor, 3);
        }
    }

    private void unregisterMagnBracketSensor() {
        Slog.d(TAG, "unregisterMagnBracketSensor: mSensor = " + this.mSensor);
        if (this.mSensor != null) {
            this.mSensorManager.unregisterListener(this.mSensorEventListener, this.mSensor);
        }
    }

    private boolean isMagnBracketSensorEnabled() {
        Cursor cursor = this.mContext.getContentResolver().query(HWVDRIVE_URI, new String[]{"value"}, "name= ?", new String[]{"magn_bracket_sensors_switch"}, null);
        Slog.d(TAG, "isMagnBracketSensorEnabled: cursor = " + cursor);
        if (cursor != null) {
            int sensorSwitch = 0;
            try {
                if (cursor.moveToNext()) {
                    String value = cursor.getString(0);
                    Slog.d(TAG, "isMagnBracketSensorEnabled: value = " + value);
                    sensorSwitch = Integer.parseInt(value);
                }
                Slog.d(TAG, "isMagnBracketSensorEnabled: sensorSwitch = " + sensorSwitch);
                boolean z = 1 == sensorSwitch ? DBG : false;
                if (cursor != null) {
                    cursor.close();
                }
                return z;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return false;
        }
    }

    private void registerPackageReceiver() {
        IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme(HwBroadcastRadarUtil.KEY_PACKAGE);
        this.mContext.registerReceiver(this.mReceiver, filter);
    }

    private void doMagnBracketSensorSwitchChanged() {
        boolean isMagnBracketSensorEnabled = isMagnBracketSensorEnabled();
        Slog.d(TAG, "doMagnBracketSensorSwitchChanged: isMagnBracketSensorEnabled = " + isMagnBracketSensorEnabled + ", mMagnBracketSensorEnabled = " + this.mMagnBracketSensorEnabled);
        if (this.mMagnBracketSensorEnabled != isMagnBracketSensorEnabled) {
            this.mMagnBracketSensorEnabled = isMagnBracketSensorEnabled;
            if (this.mMagnBracketSensorEnabled) {
                registerMagnBracketSensor();
            } else {
                unregisterMagnBracketSensor();
            }
        }
    }

    private boolean isHwVdriveExist() {
        PackageInfo packageInfo = null;
        try {
            packageInfo = this.mContext.getPackageManager().getPackageInfo(HWVDRIVE_PACKAGE_NAME, 0);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            return DBG;
        }
        return false;
    }

    private boolean isProviderPublish() {
        Cursor cursor = this.mContext.getContentResolver().query(HWVDRIVE_URI, new String[]{"value"}, "name= ?", new String[]{"magn_bracket_sensors_switch"}, null);
        Slog.d(TAG, "isProviderPublish: cursor = " + cursor);
        if (cursor == null) {
            return false;
        }
        cursor.close();
        return DBG;
    }

    private void checkProviderPublish() {
        this.mCheckCount++;
        Slog.d(TAG, "checkProviderPublish: mCheckCount = " + this.mCheckCount);
        if (this.mCheckCount > 15) {
            Slog.e(TAG, "checkProviderPublish: mCheckCount > MAX_RETRY_COUNT");
            return;
        }
        boolean isProviderPublish = isProviderPublish();
        Slog.d(TAG, "checkProviderPublish: isProviderPublish = " + isProviderPublish);
        if (isProviderPublish) {
            this.mMagnBracketSensorEnabled = isMagnBracketSensorEnabled();
            if (this.mMagnBracketSensorEnabled) {
                registerMagnBracketSensor();
            }
        } else {
            this.mHandler.sendEmptyMessageDelayed(2, 1000);
        }
    }
}
