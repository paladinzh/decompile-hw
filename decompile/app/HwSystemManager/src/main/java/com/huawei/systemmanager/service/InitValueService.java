package com.huawei.systemmanager.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.notificationmanager.db.DBProvider;
import com.huawei.systemmanager.comm.concurrent.HsmExecutor;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.optimize.OptimizeIntentService;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.service.MainService.HsmService;
import com.huawei.systemmanager.spacecleanner.engine.hwscanner.custom.HwCustDataInitService;
import com.huawei.systemmanager.spacecleanner.setting.SpaceScheduleService;
import com.huawei.systemmanager.spacecleanner.setting.SpaceSettingPreference;
import com.huawei.systemmanager.util.HwLog;

public class InitValueService implements HsmService {
    public static final String DEFAULT_VALUE_SETTING = "InitValueService";
    public static final String IS_THUMBNAIL_MONTH_SWITCH_OPEN = "is_thumbnail_month_switch_open";
    private static final String TAG = "InitValueService";
    private static final int THUMBNAIL_CLEAN_DAYS = 30;
    private Context mContext;
    private BroadcastReceiver mReceiver;
    private Handler mUiHanlder = new Handler();

    private static final class ConnectivityBroadcastReceiver extends BroadcastReceiver {
        private ConnectivityBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (!TextUtils.isEmpty(action)) {
                    String packageName = context.getPackageName();
                    if (TextUtils.equals("android.net.conn.CONNECTIVITY_CHANGE", action)) {
                        long currentTime = System.currentTimeMillis();
                        if (currentTime - PreferenceHelper.getLastAlarmTime(context) > 259200000) {
                            Intent harassmentinterceptionAutoUpdate = new Intent(ConstValues.ACTION_ALARM_AUTO_UPDATE);
                            harassmentinterceptionAutoUpdate.putExtra("android.net.conn.CONNECTIVITY_CHANGE", true);
                            harassmentinterceptionAutoUpdate.setPackage(packageName);
                            context.sendBroadcast(harassmentinterceptionAutoUpdate, "com.huawei.systemmanager.permission.ACCESS_INTERFACE");
                        }
                        if (currentTime - SpaceSettingPreference.getDefault().getUpdateSetting().getUpdateTimeStamp() > 259200000) {
                            Intent spacecleannerAutoUpdate = new Intent(SpaceScheduleService.ACTION_AUTO_UPDATE_SPACE_LIB);
                            spacecleannerAutoUpdate.setClass(context, SpaceScheduleService.class);
                            spacecleannerAutoUpdate.putExtra("android.net.conn.CONNECTIVITY_CHANGE", true);
                            spacecleannerAutoUpdate.setPackage(packageName);
                            context.startService(spacecleannerAutoUpdate);
                        }
                    }
                }
            }
        }
    }

    private class InitNotificationDatabase implements Runnable {
        private InitNotificationDatabase() {
        }

        public void run() {
            Cursor cursor = null;
            try {
                cursor = InitValueService.this.mContext.getContentResolver().query(DBProvider.URI_NOTIFICATION_CFG, null, null, null, null);
            } catch (RuntimeException e) {
                HwLog.w("InitValueService", "InitNotificationDatabase", e);
            } finally {
                Closeables.close(cursor);
            }
        }
    }

    public InitValueService(Context context) {
        this.mContext = context;
    }

    public void init() {
        this.mUiHanlder.post(new Runnable() {
            public void run() {
                InitValueService.this.startInitDataServices();
            }
        });
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        this.mReceiver = new ConnectivityBroadcastReceiver();
        this.mContext.registerReceiver(this.mReceiver, filter);
        HsmExecutor.THREAD_POOL_EXECUTOR.execute(new InitNotificationDatabase());
    }

    private void startInitDataServices() {
        HwLog.i("InitValueService", "startInitDataServices called");
        Intent intent = new Intent(Const.ACTION_INITIAL_PROTECT_DATA);
        intent.setClass(this.mContext, OptimizeIntentService.class);
        this.mContext.startService(intent);
        Intent hwCustTrashIntent = new Intent(this.mContext, HwCustDataInitService.class);
        hwCustTrashIntent.setAction(Const.ACTION_INITIAL_HWCUST_TRASH);
        this.mContext.startService(hwCustTrashIntent);
    }

    public void onDestroy() {
        if (this.mReceiver != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mReceiver = null;
        }
    }

    public void onConfigurationChange(Configuration newConfig) {
    }

    public void onStartCommand(Intent intent, int flags, int startId) {
    }
}
