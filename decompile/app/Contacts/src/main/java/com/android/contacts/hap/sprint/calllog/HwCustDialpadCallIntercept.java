package com.android.contacts.hap.sprint.calllog;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.provider.SettingsEx.Systemex;
import android.telephony.PhoneNumberUtils;
import android.util.Log;
import com.android.contacts.hap.CommonConstants;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import com.huawei.telephony.HuaweiTelephonyManagerCustEx;
import java.util.Locale;

public class HwCustDialpadCallIntercept {
    private static final String PHONE_STATE_BRANDED = "1";
    private static final String PHONE_STATE_UNBRANDED = "2";
    private static final String TAG = "HwCustDialpadCallIntercept";
    private static final String UNBRANDED_CARRIER = "Chameleon";
    private static HwCustDialpadCallIntercept _instance;
    public final String CALLINTERCEPT_CONTACT = "contacts_call_intercept";
    private final String[] PROJECTION = new String[]{"value"};
    public final String SYSTEM_PRELOAD_CALLINTERCEPT = "callintercept_version";
    private HwCustCallInterceptData mCallInterceptData;
    private Context mContext;
    private final String mPhoneState;

    static class LoadDataInBackgroud extends AsyncTask<Void, Void, Void> {
        private HwCustDialpadCallIntercept mDialpadCallIntercept;
        private boolean mVersionCheck;

        LoadDataInBackgroud(HwCustDialpadCallIntercept dialpadCallIntercept) {
            this(dialpadCallIntercept, false);
        }

        LoadDataInBackgroud(HwCustDialpadCallIntercept dialpadCallIntercept, boolean versionCheck) {
            this.mDialpadCallIntercept = dialpadCallIntercept;
            this.mVersionCheck = versionCheck;
        }

        protected Void doInBackground(Void... params) {
            if (this.mVersionCheck) {
                this.mDialpadCallIntercept.reloadIfVersionChanged();
            } else {
                this.mDialpadCallIntercept.reloadCallInterceptNodeDB();
            }
            return null;
        }

        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
        }
    }

    void reloadCallInterceptNodeDB() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0067 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = this;
        r4 = 0;
        r7 = 0;
        r1 = r9.mContext;
        r0 = r1.getContentResolver();
        r1 = com.huawei.sprint.chameleon.provider.ChameleonContract.CONTENT_URI_CONTACTS;
        r2 = r9.PROJECTION;
        r3 = "category='contacts_call_intercept'";
        r5 = r4;
        r7 = r0.query(r1, r2, r3, r4, r5);
        if (r7 != 0) goto L_0x0024;
    L_0x0016:
        r1 = com.android.contacts.hap.CommonConstants.LOG_DEBUG;
        if (r1 == 0) goto L_0x0023;
    L_0x001a:
        r1 = "HwCustDialpadCallIntercept";
        r2 = "reloadCallInterceptNodeDB() , cursor is null , returning ";
        android.util.Log.d(r1, r2);
    L_0x0023:
        return;
    L_0x0024:
        r1 = r7.moveToFirst();	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        if (r1 == 0) goto L_0x003a;	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
    L_0x002a:
        r1 = 0;	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r6 = r7.getString(r1);	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r1 = r9.mCallInterceptData;	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r1.addRawCallIntercept(r6);	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r1 = r7.moveToNext();	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        if (r1 != 0) goto L_0x002a;
    L_0x003a:
        r1 = com.android.contacts.hap.CommonConstants.LOG_DEBUG;
        if (r1 == 0) goto L_0x0047;
    L_0x003e:
        r1 = "HwCustDialpadCallIntercept";
        r2 = "reloadCallInterceptNodeDB() , mCallInterceptData commiting the cache ";
        android.util.Log.d(r1, r2);
    L_0x0047:
        r1 = r9.mCallInterceptData;
        r1.commitCache();
        r7.close();
    L_0x004f:
        return;
    L_0x0050:
        r8 = move-exception;
        r1 = "HwCustDialpadCallIntercept";	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r2 = "reloadCallInterceptNodeDB() cannot query the entries ..";	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        android.util.Log.i(r1, r2);	 Catch:{ SQLiteException -> 0x0050, all -> 0x0070 }
        r1 = com.android.contacts.hap.CommonConstants.LOG_DEBUG;
        if (r1 == 0) goto L_0x0067;
    L_0x005e:
        r1 = "HwCustDialpadCallIntercept";
        r2 = "reloadCallInterceptNodeDB() , mCallInterceptData commiting the cache ";
        android.util.Log.d(r1, r2);
    L_0x0067:
        r1 = r9.mCallInterceptData;
        r1.commitCache();
        r7.close();
        goto L_0x004f;
    L_0x0070:
        r1 = move-exception;
        r2 = com.android.contacts.hap.CommonConstants.LOG_DEBUG;
        if (r2 == 0) goto L_0x007e;
    L_0x0075:
        r2 = "HwCustDialpadCallIntercept";
        r3 = "reloadCallInterceptNodeDB() , mCallInterceptData commiting the cache ";
        android.util.Log.d(r2, r3);
    L_0x007e:
        r2 = r9.mCallInterceptData;
        r2.commitCache();
        r7.close();
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.sprint.calllog.HwCustDialpadCallIntercept.reloadCallInterceptNodeDB():void");
    }

    public static HwCustDialpadCallIntercept getInstance(Context context) {
        synchronized (HwCustDialpadCallIntercept.class) {
            if (_instance == null) {
                _instance = new HwCustDialpadCallIntercept(context.getApplicationContext());
                new LoadDataInBackgroud(_instance).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
            }
        }
        return _instance;
    }

    private HwCustDialpadCallIntercept(Context context) {
        this.mContext = context;
        this.mCallInterceptData = new HwCustCallInterceptData();
        if (UNBRANDED_CARRIER.equals(SystemProperties.get("ro.home.operator.carrierid", UNBRANDED_CARRIER))) {
            this.mPhoneState = "2";
        } else {
            this.mPhoneState = "1";
        }
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, "HwCustDialpadCallIntercept(), mPhoneState is : " + this.mPhoneState);
        }
    }

    public void reloadIfRequired() {
        new LoadDataInBackgroud(this, true).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    void reloadIfVersionChanged() {
        long dbVersion = getDBVersion();
        long prefVersion = getPrefVersion();
        if (dbVersion != prefVersion) {
            if (CommonConstants.LOG_DEBUG) {
                Log.d(TAG, "reloadIfVersionChanged() , version is changed from " + prefVersion + "" + "to" + dbVersion + "so reloading from the db");
            }
            saveToPref(dbVersion);
            reloadCallInterceptNodeDB();
        }
    }

    public Intent getCallInterceptIntent(String dialNum) {
        int i = 1;
        try {
            i = HuaweiTelephonyManagerCustEx.getCdmaRoamStateForSprint();
            Log.d(TAG, " current roamingStateValue value is " + i);
        } catch (NoExtAPIException e) {
            Log.e(TAG, " Exception is thrown for api notfound ..");
        }
        String roamingState = Integer.toString(i);
        if (CommonConstants.LOG_DEBUG) {
            Log.d(TAG, " current roamingStateValue value is " + roamingState);
        }
        if ("0".equals(roamingState)) {
            roamingState = "null";
        }
        roamingState = roamingState.toLowerCase(Locale.US);
        return this.mCallInterceptData.getCallInterceptIntent(PhoneNumberUtils.stripSeparators(dialNum), this.mPhoneState, roamingState);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private long getDBVersion() {
        String[] projection = new String[]{"category", "version"};
        long dbVersion = -1;
        if (this.mContext.getContentResolver() != null) {
            Cursor cursor = this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_VERSION, projection, "category='contacts_call_intercept'", null, null);
            if (cursor == null) {
                return -1;
            }
            try {
                if (cursor.moveToFirst()) {
                    do {
                        if ("contacts_call_intercept".equals(cursor.getString(0))) {
                            dbVersion = cursor.getLong(1);
                        }
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (SQLiteException e) {
                Log.i(TAG, "getDBVersion() cannot query the entries ..");
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return dbVersion;
    }

    private long getPrefVersion() {
        return Systemex.getLong(this.mContext.getContentResolver(), "callintercept_version", -1);
    }

    private void saveToPref(long dbVersion) {
        Systemex.putLong(this.mContext.getContentResolver(), "callintercept_version", dbVersion);
    }
}
