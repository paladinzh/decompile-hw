package com.android.mms.util;

import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.preference.PreferenceManager;
import android.provider.Settings.System;
import android.provider.Telephony.Mms;
import android.telephony.ServiceState;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.ui.HwCustMessageUtils;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.NotificationInd;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.ResEx;
import java.util.ArrayList;
import java.util.Collection;

public class DownloadManager {
    private static ConversationQueryHandler mAsyncHandler;
    private static HwCustMessageUtils mHwCustMessageUtils = ((HwCustMessageUtils) HwCustUtils.createObj(HwCustMessageUtils.class, new Object[0]));
    private static Collection<Long> mThreadIds = new ArrayList();
    private static volatile DownloadManager sInstance;
    public boolean isNeedUpdateTime = false;
    private boolean mAutoDownload;
    private final Context mContext;
    private final Handler mHandler;
    private final SharedPreferences mPreferences;
    private final OnSharedPreferenceChangeListener mPreferencesChangeListener = new OnSharedPreferenceChangeListener() {
        public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
            if ("pref_key_mms_auto_retrieval".equals(key) || "pref_key_mms_retrieval_during_roaming".equals(key)) {
                MLog.v("DownloadManager", "Preferences updated.");
                if (DownloadManager.sInstance != null) {
                    synchronized (DownloadManager.sInstance) {
                        DownloadManager.this.mAutoDownload = DownloadManager.getAutoDownloadState(prefs);
                        MLog.v("DownloadManager", "mAutoDownload ------> " + DownloadManager.this.mAutoDownload);
                    }
                }
            }
        }
    };
    private final BroadcastReceiver mRoamingStateListener = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SERVICE_STATE".equals(intent.getAction())) {
                Bundle extras = intent.getExtras();
                if (extras != null) {
                    MLog.v("DownloadManager", "Service state changed: " + extras);
                    if (DownloadManager.sInstance != null) {
                        boolean isRoaming = ServiceState.newFromBundle(extras).getRoaming();
                        MLog.v("DownloadManager", "roaming ------> " + isRoaming);
                        if (DownloadManager.mHwCustMessageUtils != null && DownloadManager.mHwCustMessageUtils.configRoamingNationalAsLocal() && isRoaming && DownloadManager.mHwCustMessageUtils.isRoamingNationalP4(0)) {
                            isRoaming = false;
                        }
                        synchronized (DownloadManager.sInstance) {
                            DownloadManager.this.mAutoDownload = DownloadManager.getAutoDownloadState(DownloadManager.this.mPreferences, isRoaming);
                            MLog.v("DownloadManager", "mAutoDownload ------> " + DownloadManager.this.mAutoDownload);
                        }
                    }
                }
            }
        }
    };

    public static void ChecktoDelete_ThreadorPdu(android.net.Uri r19, android.content.Context r20) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
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
        r2 = 1;
        r5 = new java.lang.String[r2];
        r2 = "thread_id";
        r3 = 0;
        r5[r3] = r2;
        r2 = 1;
        r9 = new java.lang.String[r2];
        r2 = "message_count";
        r3 = 0;
        r9[r3] = r2;
        r16 = 0;
        r17 = 0;
        r3 = r20.getContentResolver();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r6 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r7 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r8 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = r20;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r4 = r19;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r16 = com.huawei.cspcommon.ex.SqliteWrapper.query(r2, r3, r4, r5, r6, r7, r8);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r16 == 0) goto L_0x008f;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x0027:
        r2 = r16.moveToNext();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r2 == 0) goto L_0x008f;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x002d:
        r2 = mThreadIds;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2.clear();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = mThreadIds;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r0 = r16;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r6 = r0.getLong(r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = java.lang.Long.valueOf(r6);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2.add(r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = new java.lang.StringBuilder;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2.<init>();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = "_id = ";	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = r2.append(r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r0 = r16;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r6 = r0.getLong(r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = r2.append(r6);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r10 = r2.toString();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r7 = r20.getContentResolver();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r8 = com.android.mms.data.Conversation.sAllThreadsUri;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r11 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r12 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r6 = r20;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r17 = com.huawei.cspcommon.ex.SqliteWrapper.query(r6, r7, r8, r9, r10, r11, r12);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r17 == 0) goto L_0x008f;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x006d:
        r2 = r17.moveToNext();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r2 == 0) goto L_0x008f;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x0073:
        r18 = 1;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r0 = r17;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r14 = r0.getLong(r2);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = 1;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = (r14 > r2 ? 1 : (r14 == r2 ? 0 : -1));	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r2 <= 0) goto L_0x009a;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x0082:
        r2 = r20.getContentResolver();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r4 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r0 = r20;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r1 = r19;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        com.huawei.cspcommon.ex.SqliteWrapper.delete(r0, r2, r1, r3, r4);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
    L_0x008f:
        if (r16 == 0) goto L_0x0094;
    L_0x0091:
        r16.close();
    L_0x0094:
        if (r17 == 0) goto L_0x0099;
    L_0x0096:
        r17.close();
    L_0x0099:
        return;
    L_0x009a:
        r2 = mAsyncHandler;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = mThreadIds;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r4 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r0 = r18;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        com.android.mms.data.Conversation.startDeleteOnceForAll(r2, r0, r4, r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2 = com.android.mms.util.DraftCache.getInstance();	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = mThreadIds;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r4 = 0;	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r2.setDraftState(r3, r4);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        goto L_0x008f;
    L_0x00af:
        r13 = move-exception;
        r2 = "DownloadManager";	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        r3 = "NullPointerException occured while querying the database";	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        com.huawei.cspcommon.MLog.e(r2, r3);	 Catch:{ NullPointerException -> 0x00af, all -> 0x00c4 }
        if (r16 == 0) goto L_0x00be;
    L_0x00bb:
        r16.close();
    L_0x00be:
        if (r17 == 0) goto L_0x0099;
    L_0x00c0:
        r17.close();
        goto L_0x0099;
    L_0x00c4:
        r2 = move-exception;
        if (r16 == 0) goto L_0x00ca;
    L_0x00c7:
        r16.close();
    L_0x00ca:
        if (r17 == 0) goto L_0x00cf;
    L_0x00cc:
        r17.close();
    L_0x00cf:
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.mms.util.DownloadManager.ChecktoDelete_ThreadorPdu(android.net.Uri, android.content.Context):void");
    }

    private DownloadManager(Context context) {
        this.mContext = context;
        this.mHandler = new Handler();
        this.mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        this.mPreferences.registerOnSharedPreferenceChangeListener(this.mPreferencesChangeListener);
        context.registerReceiver(this.mRoamingStateListener, new IntentFilter("android.intent.action.SERVICE_STATE"));
        this.mAutoDownload = getAutoDownloadState(this.mPreferences);
        mAsyncHandler = new ConversationQueryHandler(context.getContentResolver());
        MLog.v("DownloadManager", "mAutoDownload ------> " + this.mAutoDownload);
    }

    public boolean isAuto() {
        MLog.v("DownloadManager", "DownloadManager: mAutoDownload = " + this.mAutoDownload);
        return this.mAutoDownload;
    }

    public static void init(Context context) {
        MLog.v("DownloadManager", "DownloadManager.init()");
        if (sInstance != null) {
            MLog.w("DownloadManager", "Already initialized.");
        }
        sInstance = new DownloadManager(context);
    }

    public static DownloadManager getInstance() {
        if (sInstance != null) {
            return sInstance;
        }
        throw new IllegalStateException("Uninitialized.");
    }

    static boolean getAutoDownloadState(SharedPreferences prefs) {
        return getAutoDownloadState(prefs, isRoaming());
    }

    static boolean getAutoDownloadState(SharedPreferences prefs, boolean roaming) {
        boolean autoDownload = prefs.getBoolean("pref_key_mms_auto_retrieval", true);
        MLog.v("DownloadManager", "auto download without roaming -> " + autoDownload);
        if (autoDownload) {
            boolean alwaysAuto = prefs.getBoolean("pref_key_mms_retrieval_during_roaming", false);
            MLog.v("DownloadManager", "auto download during roaming -> " + alwaysAuto);
            if (!roaming || alwaysAuto) {
                return true;
            }
        }
        return false;
    }

    static boolean isRoaming() {
        String roaming = SystemProperties.get("gsm.operator.isroaming", null);
        MLog.v("DownloadManager", "roaming ------> " + roaming);
        if (mHwCustMessageUtils != null && mHwCustMessageUtils.configRoamingNationalAsLocal() && "true".equals(roaming) && mHwCustMessageUtils.isRoamingNationalP4(0)) {
            roaming = "false";
        }
        return "true".equals(roaming);
    }

    public void markState(final Uri uri, int state) {
        ContentValues values;
        if (this.isNeedUpdateTime && state == 129) {
            values = new ContentValues(2);
            values.put("date", Long.valueOf(System.currentTimeMillis() / 1000));
        } else {
            values = new ContentValues(1);
        }
        NotificationInd nInd = null;
        try {
            GenericPdu pdu = PduPersister.getPduPersister(this.mContext).load(uri);
            if (pdu != null && (pdu instanceof NotificationInd)) {
                nInd = (NotificationInd) pdu;
            }
            if (nInd == null || nInd.getExpiry() >= System.currentTimeMillis() / 1000 || !(state == 129 || state == 136)) {
                if (state == 135) {
                    this.mHandler.post(new Runnable() {
                        public void run() {
                            try {
                                ResEx.makeToast(DownloadManager.this.getMessage(uri), 1);
                            } catch (MmsException e) {
                                MLog.e("DownloadManager", e.getMessage(), (Throwable) e);
                            }
                        }
                    });
                } else if (MessageUtils.isMultiSimEnabled()) {
                    Cursor c = this.mContext.getContentResolver().query(uri, null, null, null, null);
                    int subId = -1;
                    if (c != null) {
                        try {
                            if (c.moveToFirst()) {
                                subId = c.getInt(c.getColumnIndex("sub_id"));
                            }
                            c.close();
                        } catch (Throwable th) {
                            c.close();
                        }
                    }
                    if (!getAutoDownloadState(subId)) {
                        state |= 4;
                    }
                } else if (!this.mAutoDownload) {
                    state |= 4;
                }
                values.put("st", Integer.valueOf(state));
                SqliteWrapper.update(this.mContext, this.mContext.getContentResolver(), uri, values, null, null);
                return;
            }
            this.mHandler.post(new Runnable() {
                public void run() {
                    ResEx.makeToast((int) R.string.service_message_not_found_Toast, 1);
                }
            });
            ChecktoDelete_ThreadorPdu(uri, this.mContext);
            SqliteWrapper.delete(this.mContext, this.mContext.getContentResolver(), uri, null, null);
        } catch (MmsException e) {
            MLog.e("DownloadManager", e.getMessage(), (Throwable) e);
        }
    }

    public void showErrorCodeToast(final int errorStr) {
        int errStr = errorStr;
        this.mHandler.post(new Runnable() {
            public void run() {
                try {
                    ResEx.makeToast(errorStr, 1);
                } catch (Exception e) {
                    MLog.e("DownloadManager", "Caught an exception in showErrorCodeToast");
                }
            }
        });
    }

    private String getMessage(Uri uri) throws MmsException {
        String subject;
        String from;
        NotificationInd ind = (NotificationInd) PduPersister.getPduPersister(this.mContext).load(uri);
        EncodedStringValue v = ind.getSubject();
        if (v != null) {
            subject = v.getString();
        } else {
            subject = this.mContext.getString(R.string.no_subject);
        }
        v = ind.getFrom();
        if (v != null) {
            from = Contact.get(v.getString(), false).getName();
        } else {
            from = this.mContext.getString(R.string.unknown_sender);
        }
        return this.mContext.getString(R.string.dl_failure_notification, new Object[]{subject, from});
    }

    public int getState(Uri uri) {
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, new String[]{"st"}, null, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int i = cursor.getInt(0) & -5;
                    return i;
                }
                cursor.close();
            } finally {
                cursor.close();
            }
        }
        return 128;
    }

    public boolean isAuto(int subscription) {
        MLog.v("AutoDownloadMms", "subscription is:" + subscription);
        return getAutoDownloadState(subscription);
    }

    private boolean getAutoDownloadState(int subscription) {
        Boolean isAutoRetrivalChecked = Boolean.valueOf(this.mPreferences.getBoolean("pref_key_mms_auto_retrieval", true));
        MLog.v("AutoDownloadMms", "isAutoRetrivalChecked is:" + isAutoRetrivalChecked + ",Sub " + subscription + " isRoaming:" + MessageUtils.isNetworkRoaming(subscription));
        MLog.v("AutoDownloadMms", "RoamingAuto-retrieve Value stored in SettingProvider for Card1 is:" + System.getInt(this.mContext.getContentResolver(), "auto_download_mms_card1_roaming", 0) + ",and for Card2 is:" + System.getInt(this.mContext.getContentResolver(), "auto_download_mms_card2_roaming", 0));
        if (isAutoRetrivalChecked.booleanValue()) {
            if (MessageUtils.isNetworkRoaming(subscription)) {
                return (subscription == 0 && System.getInt(this.mContext.getContentResolver(), "auto_download_mms_card1_roaming", 0) == 1) || (subscription == 1 && System.getInt(this.mContext.getContentResolver(), "auto_download_mms_card2_roaming", 0) == 1);
            } else {
                return true;
            }
        }
    }

    public void updateStateToUnstarted(Context context) {
        ContentValues values = new ContentValues(1);
        values.put("st", Integer.valueOf(128));
        Context context2 = context;
        SqliteWrapper.update(context2, context.getContentResolver(), Mms.CONTENT_URI, values, "m_type = ? ", new String[]{"130"});
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getStateWithTimeCheck(Uri uri) {
        Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, new String[]{"st", "date"}, null, null, null);
        MessageUtils.printCursorInfo(cursor);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    int status = cursor.getInt(0) & -5;
                    long time = cursor.getLong(1);
                    if (status == 129 && Math.abs(System.currentTimeMillis() - (1000 * time)) >= 900000) {
                        status = 128;
                        this.isNeedUpdateTime = true;
                        markState(uri, 128);
                        this.isNeedUpdateTime = false;
                        MessageListAdapter.saveConnectionManagerToMap(uri.toString(), false, false, true, null);
                    }
                    cursor.close();
                    return status;
                }
                cursor.close();
            } catch (Exception ex) {
                MLog.e("DownloadManager", "getStateWithTimeCheck exception -> " + ex);
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return 128;
    }
}
