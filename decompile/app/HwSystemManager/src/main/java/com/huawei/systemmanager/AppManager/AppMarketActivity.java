package com.huawei.systemmanager.AppManager;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.TextArrowPreference;
import android.text.TextUtils;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.concurrent.HsmAsyncTask;
import com.huawei.systemmanager.comm.widget.IPreferenceClickListener.SimplePreferenceBase;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.util.HwLog;

public class AppMarketActivity extends HsmPreferenceActivity {
    private static final String COLUMNAME = "appUpdateNum";
    private static final int INDEX_HUAWEI_ZONE = 2;
    private static final int INDEX_MOVE_APP = 5;
    private static final int INDEX_NEED_APP = 1;
    private static final int INDEX_SAFE_MARKET = 3;
    private static final int INDEX_UNINSTALL_APP = 6;
    private static final int INDEX_UPDATE_APP = 4;
    private static final String KEY_HUAWEI_ZONE = "app_manager_hw_zone";
    private static final String KEY_MOVE_APP = "app_manager_app_move";
    private static final String KEY_NEED_APP = "app_manager_app_need";
    private static final String KEY_SAFE_MARKET = "app_manager_safe_market";
    private static final String KEY_UNINSTALL_APP = "app_manager_app_uninstall";
    private static final String KEY_UPDATE_APP = "app_manager_app_update";
    private static final String MARKET_PACKAGE_INTENT = "com.huawei.appmarket.ext.public";
    private static final String MARKET_PACKAGE_NAME = "com.huawei.appmarket";
    private static final String OPEN_STRING_KEY = "openStr";
    private static final String OPEN_STRING_VALUE_POS = "\"}";
    private static final String OPEN_STRING_VALUE_PRE = "{\"openId\":\"";
    private static final String TAG = "AppMarketActivity";
    private static final String THIRD_ID_KEY = "thirdId";
    private static final String THIRD_ID_VALUE = "4026616";
    private AppMarketControl mControl;

    private class AppMarketControl {
        private AppUpdateTask mAppUpdateTask;
        private Context mContext;
        private BroadcastReceiver mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent != null && !TextUtils.isEmpty(intent.getAction())) {
                    AppMarketControl.this.startUpdateTask();
                }
            }
        };

        public AppMarketControl(Context context) {
            this.mContext = context;
            this.mAppUpdateTask = null;
        }

        public boolean isNetworkAvailable() {
            NetworkInfo networkInfo = ((ConnectivityManager) AppMarketActivity.this.getSystemService("connectivity")).getActiveNetworkInfo();
            return networkInfo != null ? networkInfo.isAvailable() : false;
        }

        public void init() {
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
            this.mContext.registerReceiver(this.mReceiver, filter);
        }

        public void destory() {
            if (this.mReceiver != null) {
                this.mContext.unregisterReceiver(this.mReceiver);
                this.mReceiver = null;
            }
            if (this.mAppUpdateTask != null) {
                this.mAppUpdateTask.cancel(true);
                this.mAppUpdateTask = null;
            }
            this.mContext = null;
        }

        public void startUpdateTask() {
            if (this.mAppUpdateTask == null || this.mAppUpdateTask.isTaskCompleted()) {
                this.mAppUpdateTask = new AppUpdateTask();
                this.mAppUpdateTask.executeParallel(new Void[0]);
            }
        }
    }

    private class AppUpdateTask extends HsmAsyncTask<Void, Void, CharSequence> {
        private boolean mIsWorking;

        private AppUpdateTask() {
            this.mIsWorking = false;
        }

        protected CharSequence doInBackground(Void... params) {
            this.mIsWorking = true;
            return AppMarketActivity.this.getUpdateNum();
        }

        protected void onSuccess(CharSequence result) {
            this.mIsWorking = false;
            TextArrowPreference preference = (TextArrowPreference) AppMarketActivity.this.findPreference(AppMarketActivity.KEY_UPDATE_APP);
            if (preference == null) {
                HwLog.e(AppMarketActivity.TAG, "update app number exception, can not found preference");
            } else if (AppMarketActivity.this.mControl.isNetworkAvailable()) {
                preference.setDetail(result);
            } else {
                preference.setDetail(null);
            }
        }

        public boolean isTaskCompleted() {
            return this.mIsWorking;
        }
    }

    private class HuaweiZonePreference extends SimplePreferenceBase {
        public HuaweiZonePreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 2;
            return AppMarketActivity.this.buildIntent(2);
        }
    }

    private class MoveAppPreference extends SimplePreferenceBase {
        public MoveAppPreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 5;
            return AppMarketActivity.this.buildIntent(5);
        }
    }

    private class NeedAppPreference extends SimplePreferenceBase {
        public NeedAppPreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 1;
            return AppMarketActivity.this.buildIntent(1);
        }
    }

    private class SafeMarketPreference extends SimplePreferenceBase {
        public SafeMarketPreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 3;
            Intent intent = AppMarketActivity.this.buildIntent(3);
            intent.addFlags(ShareCfg.PERMISSION_MODIFY_CALENDAR);
            return intent;
        }
    }

    private class UninstallAppPreference extends SimplePreferenceBase {
        public UninstallAppPreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 6;
            return AppMarketActivity.this.buildIntent(6);
        }
    }

    private class UpdateAppPreference extends SimplePreferenceBase {
        public UpdateAppPreference(Activity ac) {
            super(ac);
        }

        public Intent getIntent(Context ctx) {
            this.mIndex = 4;
            return AppMarketActivity.this.buildIntent(4);
        }
    }

    private java.lang.CharSequence getUpdateNum() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x008b in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r9 = 0;
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = "content://com.huawei.appmarket.appinfos/item/17";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = android.net.Uri.parse(r1);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r2 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r3 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r4 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r5 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        if (r6 != 0) goto L_0x001d;
    L_0x0017:
        if (r6 == 0) goto L_0x001c;
    L_0x0019:
        r6.close();
    L_0x001c:
        return r9;
    L_0x001d:
        r8 = 0;
        r0 = r6.moveToFirst();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        if (r0 == 0) goto L_0x0049;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
    L_0x0024:
        r0 = "appUpdateNum";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r8 = r6.getInt(r0);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0 = "AppMarketActivity";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1.<init>();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r2 = "number = ";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.append(r8);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        com.huawei.systemmanager.util.HwLog.d(r0, r1);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
    L_0x0049:
        if (r8 == 0) goto L_0x0065;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
    L_0x004b:
        r0 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0.<init>();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0 = r0.append(r8);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = "";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0 = r0.append(r1);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r0 = r0.toString();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
    L_0x005f:
        if (r6 == 0) goto L_0x0064;
    L_0x0061:
        r6.close();
    L_0x0064:
        return r0;
    L_0x0065:
        r0 = r9;
        goto L_0x005f;
    L_0x0067:
        r7 = move-exception;
        r0 = "AppMarketActivity";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1.<init>();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r2 = "read update app number exception : ";	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r2 = r7.toString();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        com.huawei.systemmanager.util.HwLog.e(r0, r1);	 Catch:{ Exception -> 0x0067, all -> 0x008c }
        if (r6 == 0) goto L_0x008b;
    L_0x0088:
        r6.close();
    L_0x008b:
        return r9;
    L_0x008c:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0092;
    L_0x008f:
        r6.close();
    L_0x0092:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.systemmanager.AppManager.AppMarketActivity.getUpdateNum():java.lang.CharSequence");
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.app_manager_preference);
        setTitle(R.string.app_manager_title_str);
        initPreference();
        this.mControl = new AppMarketControl(this);
        this.mControl.init();
    }

    protected void onDestroy() {
        super.onDestroy();
        this.mControl.destory();
    }

    private void initPreference() {
        ((TextArrowPreference) findPreference(KEY_UPDATE_APP)).setOnPreferenceClickListener(new UpdateAppPreference(this));
        findPreference(KEY_UNINSTALL_APP).setOnPreferenceClickListener(new UninstallAppPreference(this));
        findPreference(KEY_SAFE_MARKET).setOnPreferenceClickListener(new SafeMarketPreference(this));
        findPreference(KEY_NEED_APP).setOnPreferenceClickListener(new NeedAppPreference(this));
        findPreference(KEY_HUAWEI_ZONE).setOnPreferenceClickListener(new HuaweiZonePreference(this));
    }

    protected void onResume() {
        super.onResume();
        this.mControl.startUpdateTask();
    }

    private Intent buildIntent(int index) {
        Intent intent = new Intent(MARKET_PACKAGE_INTENT);
        intent.putExtra(THIRD_ID_KEY, THIRD_ID_VALUE);
        intent.putExtra(OPEN_STRING_KEY, new StringBuffer().append(OPEN_STRING_VALUE_PRE).append(index).append(OPEN_STRING_VALUE_POS).toString());
        intent.setPackage("com.huawei.appmarket");
        return intent;
    }
}
