package com.android.contacts.hap.delete;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telecom.TelecomManager;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonConstants.DatabaseConstants;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.GroupAndContactMetaData;
import com.android.contacts.hap.GroupAndContactMetaData.GroupsData;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.IntentServiceWithWakeLock;
import com.android.contacts.hap.utils.EraseContactMarkUtils;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.AsyncTaskExecutors;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class ExtendedContactSaveService extends IntentServiceWithWakeLock {
    private ArrayList<ContentProviderOperation> mDeleteOperations = new ArrayList();
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();
    private NotificationManager mNotificationManager;
    private final String[] projection = new String[]{"_id", "company"};
    private ArrayList<Long> rawContactIds = new ArrayList();

    private static class ContactsBatchDetelion implements Runnable {
        private ArrayList<ContentProviderOperation> mOperation;
        private ContentResolver mResolver;

        public ContactsBatchDetelion(ContentResolver resolver, ArrayList<ContentProviderOperation> operation) {
            this.mOperation = operation;
            this.mResolver = resolver;
        }

        public void run() {
            try {
                ExceptionCapture.checkSimContactDeleteResult(this.mResolver.applyBatch("com.android.contacts", this.mOperation), "the returned value is not correct when deleting SIM contact from database");
                this.mOperation.clear();
            } catch (RemoteException e) {
                HwLog.e("ExtendedContactSaveService", e.getMessage(), e);
                ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from database error: " + e, e);
            } catch (OperationApplicationException e2) {
                HwLog.e("ExtendedContactSaveService", e2.getMessage(), e2);
                ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from database error: " + e2, e2);
            } catch (RuntimeException e3) {
                ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from database error: " + e3, e3);
            }
        }
    }

    public static class EfidIndexObject {
        public String accountType;
        public String efid;
        public String index;
    }

    private static class RemoveMissedCallNotification extends AsyncTask<Void, Void, Void> {
        private IntentServiceWithWakeLock mIntentService;

        public RemoveMissedCallNotification(IntentServiceWithWakeLock intentService) {
            this.mIntentService = intentService;
        }

        protected Void doInBackground(Void... params) {
            ((TelecomManager) this.mIntentService.getSystemService("telecom")).cancelMissedCallsNotification();
            return null;
        }

        protected void onPostExecute(Void unused) {
        }
    }

    private int deleteSimContacts(long[] r40, java.util.ArrayList<java.lang.Long> r41, android.os.Messenger r42) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x03e2 in list []
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
        r39 = this;
        r31 = new java.util.ArrayList;
        r31.<init>();
        r4 = 0;
        r0 = r40;
        r5 = r0.length;
    L_0x0009:
        if (r4 >= r5) goto L_0x0019;
    L_0x000b:
        r16 = r40[r4];
        r6 = java.lang.Long.valueOf(r16);
        r0 = r41;
        r0.add(r6);
        r4 = r4 + 1;
        goto L_0x0009;
    L_0x0019:
        r27 = 0;
        r28 = 0;
        r29 = 0;
        r4 = com.android.contacts.hap.sim.SimFactoryManager.isDualSim();
        if (r4 != 0) goto L_0x0167;
    L_0x0025:
        r30 = new java.lang.StringBuilder;
        r30.<init>();
        r4 = "contact_id IN (";
        r0 = r30;
        r0.append(r4);
        r4 = 0;
        r0 = r40;
        r5 = r0.length;
    L_0x0036:
        if (r4 >= r5) goto L_0x004c;
    L_0x0038:
        r16 = r40[r4];
        r0 = r30;
        r1 = r16;
        r0.append(r1);
        r6 = ",";
        r0 = r30;
        r0.append(r6);
        r4 = r4 + 1;
        goto L_0x0036;
    L_0x004c:
        r4 = r30.length();
        r4 = r4 + -1;
        r0 = r30;
        r0.setLength(r4);
        r4 = ") AND account_type=?";
        r0 = r30;
        r0.append(r4);
        r4 = 1;
        r8 = new java.lang.String[r4];
        r4 = "com.android.huawei.sim";
        r5 = 0;
        r8[r5] = r4;
        r4 = r39.getContentResolver();
        r5 = android.provider.ContactsContract.RawContacts.CONTENT_URI;
        r6 = 1;
        r6 = new java.lang.String[r6];
        r7 = "contact_id";
        r9 = 0;
        r6[r9] = r7;
        r7 = r30.toString();
        r9 = 0;
        r34 = r4.query(r5, r6, r7, r8, r9);
        if (r34 == 0) goto L_0x00a1;
    L_0x0082:
        r4 = r34.moveToFirst();	 Catch:{ all -> 0x00a9 }
        if (r4 == 0) goto L_0x009e;	 Catch:{ all -> 0x00a9 }
    L_0x0088:
        r4 = 0;	 Catch:{ all -> 0x00a9 }
        r0 = r34;	 Catch:{ all -> 0x00a9 }
        r32 = r0.getLong(r4);	 Catch:{ all -> 0x00a9 }
        r4 = java.lang.Long.valueOf(r32);	 Catch:{ all -> 0x00a9 }
        r0 = r31;	 Catch:{ all -> 0x00a9 }
        r0.add(r4);	 Catch:{ all -> 0x00a9 }
        r4 = r34.moveToNext();	 Catch:{ all -> 0x00a9 }
        if (r4 != 0) goto L_0x0088;
    L_0x009e:
        r34.close();
    L_0x00a1:
        r4 = r31.isEmpty();
        if (r4 == 0) goto L_0x00ae;
    L_0x00a7:
        r4 = 0;
        return r4;
    L_0x00a9:
        r4 = move-exception;
        r34.close();
        throw r4;
    L_0x00ae:
        r0 = r41;
        r1 = r31;
        r0.removeAll(r1);
        r4 = "SimInfoFile";
        r5 = -1;
        r27 = com.android.contacts.hap.sim.SimFactoryManager.getSharedPreferences(r4, r5);
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r27;
        r15 = r0.getBoolean(r4, r5);
    L_0x00c7:
        if (r15 == 0) goto L_0x00f1;
    L_0x00c9:
        r4 = com.android.contacts.util.HwLog.HWDBG;	 Catch:{ InterruptedException -> 0x00e6 }
        if (r4 == 0) goto L_0x00d6;	 Catch:{ InterruptedException -> 0x00e6 }
    L_0x00cd:
        r4 = "ExtendedContactSaveService";	 Catch:{ InterruptedException -> 0x00e6 }
        r5 = "Waiting for SIM to complete delete operation";	 Catch:{ InterruptedException -> 0x00e6 }
        com.android.contacts.util.HwLog.d(r4, r5);	 Catch:{ InterruptedException -> 0x00e6 }
    L_0x00d6:
        r4 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;	 Catch:{ InterruptedException -> 0x00e6 }
        java.lang.Thread.sleep(r4);	 Catch:{ InterruptedException -> 0x00e6 }
    L_0x00db:
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r27;
        r15 = r0.getBoolean(r4, r5);
        goto L_0x00c7;
    L_0x00e6:
        r14 = move-exception;
        r4 = "ExtendedContactSaveService";
        r5 = "deleteSimContacts InterruptedException";
        com.android.contacts.util.HwLog.d(r4, r5);
        goto L_0x00db;
    L_0x00f1:
        r4 = r27.edit();
        r5 = "sim_delete_progress";
        r6 = 1;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x0100:
        r38 = r41.size();
        r4 = 10000; // 0x2710 float:1.4013E-41 double:4.9407E-320;
        r0 = r38;
        if (r0 < r4) goto L_0x02d6;
    L_0x010a:
        r36 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;
    L_0x010c:
        r0 = r31;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r24 = getEfidAndIndexForSimContact(r0, r1);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r11 = 0;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r10 = 25;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4.clear();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r26 = r24.iterator();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0122:
        r4 = r26.hasNext();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 == 0) goto L_0x037e;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0128:
        r25 = r26.next();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r25 = (com.android.contacts.hap.delete.ExtendedContactSaveService.EfidIndexObject) r25;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r25;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r2 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r12 = r0.deleteSingleSimContactForMultSelection(r1, r2);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r12 > 0) goto L_0x032c;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x013a:
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 <= 0) goto L_0x014f;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0144:
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r36;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.executeSimBatch(r4, r1);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x014f:
        r4 = com.android.contacts.hap.sim.SimFactoryManager.isDualSim();
        if (r4 != 0) goto L_0x0308;
    L_0x0155:
        if (r27 == 0) goto L_0x0166;
    L_0x0157:
        r4 = r27.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x0166:
        return r11;
    L_0x0167:
        r22 = com.android.contacts.list.ContactListFilterController.getInstance(r39);
        r23 = r22.getFilter();
        r30 = new java.lang.StringBuilder;
        r30.<init>();
        r4 = "contact_id IN (";
        r0 = r30;
        r0.append(r4);
        r4 = 0;
        r0 = r40;
        r5 = r0.length;
    L_0x0180:
        if (r4 >= r5) goto L_0x0196;
    L_0x0182:
        r16 = r40[r4];
        r0 = r30;
        r1 = r16;
        r0.append(r1);
        r6 = ",";
        r0 = r30;
        r0.append(r6);
        r4 = r4 + 1;
        goto L_0x0180;
    L_0x0196:
        r4 = r30.length();
        r4 = r4 + -1;
        r0 = r30;
        r0.setLength(r4);
        r4 = ") AND (account_type=? OR account_type=? )";
        r0 = r30;
        r0.append(r4);
        r4 = 2;
        r8 = new java.lang.String[r4];
        r4 = "com.android.huawei.sim";
        r5 = 0;
        r8[r5] = r4;
        r4 = "com.android.huawei.secondsim";
        r5 = 1;
        r8[r5] = r4;
        r4 = r39.getContentResolver();
        r5 = android.provider.ContactsContract.RawContacts.CONTENT_URI;
        r6 = 1;
        r6 = new java.lang.String[r6];
        r7 = "contact_id";
        r9 = 0;
        r6[r9] = r7;
        r7 = r30.toString();
        r9 = 0;
        r34 = r4.query(r5, r6, r7, r8, r9);
        if (r34 == 0) goto L_0x01f1;
    L_0x01d2:
        r4 = r34.moveToFirst();	 Catch:{ all -> 0x01f9 }
        if (r4 == 0) goto L_0x01ee;	 Catch:{ all -> 0x01f9 }
    L_0x01d8:
        r4 = 0;	 Catch:{ all -> 0x01f9 }
        r0 = r34;	 Catch:{ all -> 0x01f9 }
        r32 = r0.getLong(r4);	 Catch:{ all -> 0x01f9 }
        r4 = java.lang.Long.valueOf(r32);	 Catch:{ all -> 0x01f9 }
        r0 = r31;	 Catch:{ all -> 0x01f9 }
        r0.add(r4);	 Catch:{ all -> 0x01f9 }
        r4 = r34.moveToNext();	 Catch:{ all -> 0x01f9 }
        if (r4 != 0) goto L_0x01d8;
    L_0x01ee:
        r34.close();
    L_0x01f1:
        r4 = r31.isEmpty();
        if (r4 == 0) goto L_0x01fe;
    L_0x01f7:
        r4 = 0;
        return r4;
    L_0x01f9:
        r4 = move-exception;
        r34.close();
        throw r4;
    L_0x01fe:
        r0 = r41;
        r1 = r31;
        r0.removeAll(r1);
        r4 = "SimInfoFile";
        r5 = 0;
        r28 = com.android.contacts.hap.sim.SimFactoryManager.getSharedPreferences(r4, r5);
        r4 = "SimInfoFile";
        r5 = 1;
        r29 = com.android.contacts.hap.sim.SimFactoryManager.getSharedPreferences(r4, r5);
        r0 = r23;
        r4 = r0.accountType;
        if (r4 != 0) goto L_0x0290;
    L_0x021b:
        r4 = r28.edit();
        r5 = "sim_delete_progress";
        r6 = 1;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        r4 = r29.edit();
        r5 = "sim_delete_progress";
        r6 = 1;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x0239:
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r28;
        r18 = r0.getBoolean(r4, r5);
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r29;
        r20 = r0.getBoolean(r4, r5);
        r4 = "sim_delete_progress";
        r5 = 0;
        r0 = r28;
        r19 = r0.getBoolean(r4, r5);
        r4 = "sim_delete_progress";
        r5 = 0;
        r0 = r29;
        r21 = r0.getBoolean(r4, r5);
    L_0x0261:
        if (r19 == 0) goto L_0x0265;
    L_0x0263:
        if (r18 != 0) goto L_0x0269;
    L_0x0265:
        if (r21 == 0) goto L_0x0100;
    L_0x0267:
        if (r20 == 0) goto L_0x0100;
    L_0x0269:
        r4 = com.android.contacts.util.HwLog.HWDBG;	 Catch:{ InterruptedException -> 0x02cb }
        if (r4 == 0) goto L_0x0276;	 Catch:{ InterruptedException -> 0x02cb }
    L_0x026d:
        r4 = "ExtendedContactSaveService";	 Catch:{ InterruptedException -> 0x02cb }
        r5 = "Waiting for SIM to complete delete operation";	 Catch:{ InterruptedException -> 0x02cb }
        com.android.contacts.util.HwLog.d(r4, r5);	 Catch:{ InterruptedException -> 0x02cb }
    L_0x0276:
        r4 = 2000; // 0x7d0 float:2.803E-42 double:9.88E-321;	 Catch:{ InterruptedException -> 0x02cb }
        java.lang.Thread.sleep(r4);	 Catch:{ InterruptedException -> 0x02cb }
    L_0x027b:
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r28;
        r18 = r0.getBoolean(r4, r5);
        r4 = "sim_copy_contacts_progress";
        r5 = 0;
        r0 = r29;
        r20 = r0.getBoolean(r4, r5);
        goto L_0x0261;
    L_0x0290:
        r0 = r23;
        r4 = r0.accountType;
        r5 = "com.android.huawei.sim";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x02ad;
    L_0x029d:
        r4 = r28.edit();
        r5 = "sim_delete_progress";
        r6 = 1;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        goto L_0x0239;
    L_0x02ad:
        r0 = r23;
        r4 = r0.accountType;
        r5 = "com.android.huawei.secondsim";
        r4 = r4.equals(r5);
        if (r4 == 0) goto L_0x0239;
    L_0x02ba:
        r4 = r29.edit();
        r5 = "sim_delete_progress";
        r6 = 1;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        goto L_0x0239;
    L_0x02cb:
        r14 = move-exception;
        r4 = "ExtendedContactSaveService";
        r5 = "deleteSimContacts InterruptedException";
        com.android.contacts.util.HwLog.d(r4, r5);
        goto L_0x027b;
    L_0x02d6:
        r4 = 8000; // 0x1f40 float:1.121E-41 double:3.9525E-320;
        r0 = r38;
        if (r0 < r4) goto L_0x02e0;
    L_0x02dc:
        r36 = 1600; // 0x640 float:2.242E-42 double:7.905E-321;
        goto L_0x010c;
    L_0x02e0:
        r4 = 6000; // 0x1770 float:8.408E-42 double:2.9644E-320;
        r0 = r38;
        if (r0 < r4) goto L_0x02ea;
    L_0x02e6:
        r36 = 1200; // 0x4b0 float:1.682E-42 double:5.93E-321;
        goto L_0x010c;
    L_0x02ea:
        r4 = 3000; // 0xbb8 float:4.204E-42 double:1.482E-320;
        r0 = r38;
        if (r0 < r4) goto L_0x02f4;
    L_0x02f0:
        r36 = 800; // 0x320 float:1.121E-42 double:3.953E-321;
        goto L_0x010c;
    L_0x02f4:
        r4 = 1000; // 0x3e8 float:1.401E-42 double:4.94E-321;
        r0 = r38;
        if (r0 <= r4) goto L_0x02fe;
    L_0x02fa:
        r36 = 600; // 0x258 float:8.41E-43 double:2.964E-321;
        goto L_0x010c;
    L_0x02fe:
        if (r38 <= 0) goto L_0x0304;
    L_0x0300:
        r36 = 300; // 0x12c float:4.2E-43 double:1.48E-321;
        goto L_0x010c;
    L_0x0304:
        r36 = 0;
        goto L_0x010c;
    L_0x0308:
        if (r28 == 0) goto L_0x0319;
    L_0x030a:
        r4 = r28.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x0319:
        if (r29 == 0) goto L_0x0166;
    L_0x031b:
        r4 = r29.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        goto L_0x0166;
    L_0x032c:
        r11 = r11 + r12;
        r4 = r11 % 50;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 != 0) goto L_0x034b;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0331:
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r35 = r4.clone();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r35 = (java.util.ArrayList) r35;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4.clear();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r35;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r2 = r36;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.executeSimBatch(r1, r2);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x034b:
        r4 = r11 % 25;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 != 0) goto L_0x0122;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x034f:
        r0 = r40;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.length;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.updateDeleteNotification(r11, r4);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = 1;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r42;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.sendMessage(r4, r11, r1);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        goto L_0x0122;
    L_0x0361:
        r13 = move-exception;
        r13.printStackTrace();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = com.android.contacts.hap.sim.SimFactoryManager.isDualSim();
        if (r4 != 0) goto L_0x03e2;
    L_0x036b:
        if (r27 == 0) goto L_0x037c;
    L_0x036d:
        r4 = r27.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x037c:
        r4 = 0;
        return r4;
    L_0x037e:
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r4.size();	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 <= 0) goto L_0x0393;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0388:
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.mDeleteOperations;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r36;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.executeSimBatch(r4, r1);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0393:
        r4 = r11 % 25;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        if (r4 == 0) goto L_0x03a7;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x0397:
        r0 = r40;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = r0.length;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.updateDeleteNotification(r11, r4);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r4 = 1;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0 = r39;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r1 = r42;	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
        r0.sendMessage(r4, r11, r1);	 Catch:{ Exception -> 0x0361, all -> 0x0406 }
    L_0x03a7:
        r4 = com.android.contacts.hap.sim.SimFactoryManager.isDualSim();
        if (r4 != 0) goto L_0x03bf;
    L_0x03ad:
        if (r27 == 0) goto L_0x03be;
    L_0x03af:
        r4 = r27.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x03be:
        return r11;
    L_0x03bf:
        if (r28 == 0) goto L_0x03d0;
    L_0x03c1:
        r4 = r28.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x03d0:
        if (r29 == 0) goto L_0x03be;
    L_0x03d2:
        r4 = r29.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        goto L_0x03be;
    L_0x03e2:
        if (r28 == 0) goto L_0x03f3;
    L_0x03e4:
        r4 = r28.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
    L_0x03f3:
        if (r29 == 0) goto L_0x037c;
    L_0x03f5:
        r4 = r29.edit();
        r5 = "sim_delete_progress";
        r6 = 0;
        r4 = r4.putBoolean(r5, r6);
        r4.apply();
        goto L_0x037c;
    L_0x0406:
        r4 = move-exception;
        r5 = com.android.contacts.hap.sim.SimFactoryManager.isDualSim();
        if (r5 != 0) goto L_0x041f;
    L_0x040d:
        if (r27 == 0) goto L_0x041e;
    L_0x040f:
        r5 = r27.edit();
        r6 = "sim_delete_progress";
        r7 = 0;
        r5 = r5.putBoolean(r6, r7);
        r5.apply();
    L_0x041e:
        throw r4;
    L_0x041f:
        if (r28 == 0) goto L_0x0430;
    L_0x0421:
        r5 = r28.edit();
        r6 = "sim_delete_progress";
        r7 = 0;
        r5 = r5.putBoolean(r6, r7);
        r5.apply();
    L_0x0430:
        if (r29 == 0) goto L_0x041e;
    L_0x0432:
        r5 = r29.edit();
        r6 = "sim_delete_progress";
        r7 = 0;
        r5 = r5.putBoolean(r6, r7);
        r5.apply();
        goto L_0x041e;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.delete.ExtendedContactSaveService.deleteSimContacts(long[], java.util.ArrayList, android.os.Messenger):int");
    }

    public ExtendedContactSaveService() {
        super("ExtendedContactSaveService");
        setIntentRedelivery(true);
    }

    protected void doWakefulWork(Intent intent) {
        String action = intent.getAction();
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Action opted : " + action);
        }
        long[] lContactIds;
        if ("deleteMultiple".equals(action)) {
            lContactIds = intent.getLongArrayExtra("ContactIds");
            if (lContactIds == null || lContactIds.length == 0) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
                return;
            }
            Messenger msger = (Messenger) intent.getParcelableExtra("messenger");
            if (msger != null) {
                if (QueryUtil.isHAPProviderInstalled()) {
                    batchDeleteSelectedContacts(lContactIds, msger);
                } else {
                    deleteSelectedContacts(lContactIds, msger);
                }
            }
        } else if ("markUnmarkFavoriteMultiple".equals(action)) {
            lContactIds = intent.getLongArrayExtra("ContactIds");
            if (lContactIds == null || lContactIds.length == 0) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
                return;
            } else {
                markUnmarkFavoriteSelectedContacts(lContactIds, intent.getBooleanExtra("markFavorite", false));
            }
        } else if ("clearUsageForSelectedContactsData".equals(action)) {
            long[] dataIds = intent.getLongArrayExtra("extra_data_ids");
            if (dataIds == null || dataIds.length == 0) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments for clear usage");
                return;
            }
            EraseContactMarkUtils.deleteDataUsageByDataIds(this, dataIds);
        } else if ("add_members_to_group".equals(action)) {
            addOrRemoveGroupMembership(intent);
        } else if ("remove_frequent_contacts".equals(action)) {
            String lookupKey = intent.getStringExtra("contact_lookup_key");
            if (lookupKey != null) {
                removeContactsFromFrequentList(lookupKey);
            }
        } else if ("remove_call_log_entries".equals(action)) {
            removeCallLogEntries(intent.getLongArrayExtra("removedEntries"));
        } else if ("remove_frequent_phone".equals(action)) {
            removeContactsFromFrequentList(intent.getLongExtra("usage_data_id", -1));
        } else if ("editFavoritesList".equals(action)) {
            long[] lContactIdsToAdd = intent.getLongArrayExtra("ContactIds");
            long[] lContactIdsToRemove = intent.getLongArrayExtra("ContactIdsToRemove");
            if ((lContactIdsToAdd == null || lContactIdsToAdd.length == 0) && (lContactIdsToRemove == null || lContactIdsToRemove.length == 0)) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
                return;
            }
            if (lContactIdsToAdd != null) {
                markUnmarkFavoriteSelectedContacts(lContactIdsToAdd, true);
            }
            if (lContactIdsToRemove != null) {
                markUnmarkFavoriteSelectedContacts(lContactIdsToRemove, false);
            }
        } else if ("markUnmarkPrivateContacts".equals(action)) {
            lContactIds = intent.getLongArrayExtra("ContactIds");
            if (lContactIds == null || lContactIds.length == 0) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
                return;
            } else {
                markAndUnmarkPrivateContacts(lContactIds, intent.getBooleanExtra("markPrivate", true));
            }
        } else if ("add_company_contacts".equals(action)) {
            lContactIds = intent.getLongArrayExtra("ContactIds");
            String company = intent.getStringExtra("company_name");
            boolean isNoCompanyGroup = intent.getBooleanExtra("is_no_company_group", false);
            if (lContactIds == null || lContactIds.length == 0 || company == null) {
                HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
                return;
            } else if (isNoCompanyGroup) {
                addSelectedContactsToNoCompanyGroup(lContactIds);
            } else {
                addSelectedContactsToCompany(lContactIds, company, getApplicationContext());
            }
        }
        stopSelf();
    }

    private Uri constructBatchDeleteUri(ArrayList<Long> lContactIds) {
        StringBuffer paras = new StringBuffer();
        int size = lContactIds.size();
        int lastPos = size - 1;
        for (int j = 0; j < size; j++) {
            paras.append(lContactIds.get(j) + "");
            if (j != lastPos) {
                paras.append(",");
            }
        }
        return Uri.parse("content://com.android.contacts/contacts/batch_delete?contactsIds=" + Uri.encode(paras.toString()));
    }

    private void batchDeleteSelectedContacts(long[] aContactIds, Messenger msger) {
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        if (HwLog.HWFLOW) {
            HwLog.i("ExtendedContactSaveService", "Inside deleteSelectedContacts for " + aContactIds.length + " contacts");
        }
        ArrayList<Long> nonSimContacts = new ArrayList();
        sendMessage(2, aContactIds.length, msger);
        int simContactsDeleted = deleteSimContacts(aContactIds, nonSimContacts, msger);
        if (simContactsDeleted < aContactIds.length - nonSimContacts.size()) {
            updateDeleteNotification(aContactIds.length, aContactIds.length);
            sendMessage(6, aContactIds.length, msger);
            return;
        }
        ContentResolver lResolver = getContentResolver();
        ArrayList<ContentProviderOperation> lOperations = new ArrayList();
        ArrayList<Long> lContactIds = new ArrayList();
        int numberOfContactsDeleted = simContactsDeleted;
        int totalNonSimContactsSize = nonSimContacts.size();
        for (int i = 0; i < totalNonSimContactsSize; i++) {
            lContactIds.add((Long) nonSimContacts.get(i));
            if (lContactIds.size() % 50 == 0) {
                try {
                    long sleep_time;
                    lOperations.add(ContentProviderOperation.newDelete(constructBatchDeleteUri(lContactIds)).build());
                    ExceptionCapture.checkContactDeleteResult(lResolver.applyBatch("com.android.contacts", lOperations), "the returned value is not correct when deleting contact from database");
                    if (HwLog.HWFLOW) {
                        HwLog.v("ExtendedContactSaveService", "Applied batch");
                    }
                    numberOfContactsDeleted += lContactIds.size();
                    updateDeleteNotification(numberOfContactsDeleted, aContactIds.length);
                    sendMessage(1, numberOfContactsDeleted, msger);
                    if (totalNonSimContactsSize - i >= 10000) {
                        sleep_time = 2000;
                    } else if (totalNonSimContactsSize - i >= 8000) {
                        sleep_time = 1600;
                    } else if (totalNonSimContactsSize - i >= 6000) {
                        sleep_time = 1200;
                    } else if (totalNonSimContactsSize - i >= 3000) {
                        sleep_time = 800;
                    } else if (totalNonSimContactsSize - i >= 1000) {
                        sleep_time = 600;
                    } else {
                        sleep_time = 300;
                    }
                    try {
                        Thread.sleep(sleep_time);
                    } catch (InterruptedException e) {
                        HwLog.d("ExtendedContactSaveService", "batchDeleteSelectedContacts InterruptedException");
                    }
                    lOperations.clear();
                    lContactIds.clear();
                } catch (RemoteException e2) {
                    HwLog.e("ExtendedContactSaveService", e2.getMessage(), e2);
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e2);
                    return;
                } catch (OperationApplicationException e3) {
                    HwLog.e("ExtendedContactSaveService", e3.getMessage(), e3);
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e3);
                    return;
                } catch (RuntimeException e4) {
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error:", e4);
                    return;
                }
            }
        }
        if (!lContactIds.isEmpty()) {
            try {
                lOperations.add(ContentProviderOperation.newDelete(constructBatchDeleteUri(lContactIds)).build());
                ExceptionCapture.checkContactDeleteResult(lResolver.applyBatch("com.android.contacts", lOperations), "the returned value is not correct when deleting contact from database");
                if (HwLog.HWDBG) {
                    HwLog.v("ExtendedContactSaveService", "Applied batch");
                }
                numberOfContactsDeleted += lContactIds.size();
                updateDeleteNotification(numberOfContactsDeleted, aContactIds.length);
                sendMessage(1, numberOfContactsDeleted, msger);
            } catch (RemoteException e22) {
                HwLog.e("ExtendedContactSaveService", e22.getMessage(), e22);
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e22, e22);
                return;
            } catch (OperationApplicationException e32) {
                HwLog.e("ExtendedContactSaveService", e32.getMessage(), e32);
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e32, e32);
                return;
            } catch (RuntimeException e42) {
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e42, e42);
                return;
            }
        }
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Total number of contacts deleted : " + numberOfContactsDeleted);
        }
        onDeleteContactsFinished(getResources().getQuantityString(R.plurals.delete_contact_notification, numberOfContactsDeleted, new Object[]{Integer.valueOf(numberOfContactsDeleted)}), 0);
        sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
        stopForeground(true);
    }

    private void addSelectedContactsToNoCompanyGroup(long[] aContactIds) {
        ContentResolver lResolver = getContentResolver();
        StringBuilder delWhereArgs = new StringBuilder("(");
        for (int i = 1; i <= aContactIds.length; i++) {
            delWhereArgs.append(aContactIds[i - 1]).append(",");
            if (i % 50 == 0) {
                int length = delWhereArgs.length();
                delWhereArgs.replace(length - 1, length, ")");
                int result = lResolver.delete(Data.CONTENT_URI, "contact_id IN " + delWhereArgs.toString() + " AND " + "mimetype" + " = '" + "vnd.android.cursor.item/organization" + "'", null);
                if (HwLog.HWDBG) {
                    HwLog.v("ExtendedContactSaveService", "Deleted : " + result);
                    HwLog.v("ExtendedContactSaveService", "Applied batch");
                }
                delWhereArgs.delete(1, length);
            }
        }
        if (delWhereArgs.length() > 1) {
            length = delWhereArgs.length();
            delWhereArgs.replace(length - 1, length, ")");
            lResolver.delete(Data.CONTENT_URI, "contact_id IN " + delWhereArgs.toString() + " AND " + "mimetype" + " = '" + "vnd.android.cursor.item/organization" + "'", null);
            if (HwLog.HWDBG) {
                HwLog.v("ExtendedContactSaveService", "Applied batch");
            }
        }
    }

    private Cursor getRawContactsByContactIds(ContentResolver lResolver, long[] aContactIds, Context context) {
        int i;
        StringBuilder queryStringBuilder = new StringBuilder();
        for (i = 0; i <= aContactIds.length - 1; i++) {
            if (queryStringBuilder.length() > 0) {
                queryStringBuilder.append(",");
            }
            queryStringBuilder.append(aContactIds[i]);
        }
        StringBuilder selection = new StringBuilder();
        selection.append("contact_id IN (").append(queryStringBuilder).append(") AND (");
        List<String> selectionArgs = new ArrayList();
        List<AccountWithDataSet> accountList = AccountTypeManager.getInstance(context).getAccountsExcludeBothSim(true);
        if (accountList != null && accountList.size() > 0) {
            i = 0;
            for (AccountWithDataSet account : accountList) {
                if (i > 0) {
                    selection.append(" OR ");
                }
                selection.append("account_name=? AND account_type=?");
                selectionArgs.add(account.name);
                selectionArgs.add(account.type);
                if (account.dataSet != null) {
                    selection.append(" AND data_set=?");
                    selectionArgs.add(account.dataSet);
                } else {
                    selection.append(" AND data_set IS NULL");
                }
                i++;
            }
            selection.append(")");
        }
        return lResolver.query(RawContacts.CONTENT_URI, this.projection, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null);
    }

    private void addSelectedContactsToCompany(long[] aContactIds, String company, Context context) {
        ContentResolver lResolver = getContentResolver();
        Cursor cursor = getRawContactsByContactIds(lResolver, aContactIds, context);
        ArrayList<ContentProviderOperation> lOperations = new ArrayList();
        while (cursor.moveToNext()) {
            String tempCompany = cursor.getString(1);
            long rawContactId = cursor.getLong(0);
            Builder builder;
            if (tempCompany != null) {
                builder = ContentProviderOperation.newUpdate(Data.CONTENT_URI);
                builder.withValue("data1", company);
                builder.withSelection("mimetype= 'vnd.android.cursor.item/organization' AND raw_contact_id=?", new String[]{String.valueOf(rawContactId)});
                lOperations.add(builder.build());
            } else {
                try {
                    builder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    builder.withValue("data1", company);
                    builder.withValue("mimetype", "vnd.android.cursor.item/organization");
                    builder.withValue("raw_contact_id", Long.valueOf(rawContactId));
                    lOperations.add(builder.build());
                } catch (Exception e) {
                    HwLog.e("ExtendedContactSaveService", e.getMessage());
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            }
            if (lOperations.size() % 50 == 0) {
                try {
                    lResolver.applyBatch("com.android.contacts", lOperations);
                    lOperations.clear();
                } catch (RemoteException e2) {
                    HwLog.e("ExtendedContactSaveService", e2.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                } catch (OperationApplicationException e3) {
                    HwLog.e("ExtendedContactSaveService", e3.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                } catch (RuntimeException e4) {
                    HwLog.e("ExtendedContactSaveService", e4.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                }
            }
        }
        if (!lOperations.isEmpty()) {
            try {
                lResolver.applyBatch("com.android.contacts", lOperations);
            } catch (RemoteException e22) {
                HwLog.e("ExtendedContactSaveService", e22.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                return;
            } catch (OperationApplicationException e32) {
                HwLog.e("ExtendedContactSaveService", e32.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                return;
            } catch (RuntimeException e42) {
                HwLog.e("ExtendedContactSaveService", e42.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
                return;
            }
        }
        if (cursor != null) {
            cursor.close();
        }
    }

    private void deleteSelectedContacts(long[] aContactIds, Messenger msger) {
        this.mNotificationManager = (NotificationManager) getSystemService("notification");
        if (HwLog.HWFLOW) {
            HwLog.i("ExtendedContactSaveService", "Inside deleteSelectedContacts for " + aContactIds.length + " contacts");
        }
        ArrayList<Long> nonSimContacts = new ArrayList();
        sendMessage(2, aContactIds.length, msger);
        int simContactsDeleted = deleteSimContacts(aContactIds, nonSimContacts, msger);
        if (simContactsDeleted < aContactIds.length - nonSimContacts.size()) {
            updateDeleteNotification(aContactIds.length, aContactIds.length);
            sendMessage(6, aContactIds.length, msger);
            return;
        }
        ContentResolver lResolver = getContentResolver();
        ArrayList<ContentProviderOperation> lOperations = new ArrayList();
        int numberOfContactsDeleted = simContactsDeleted;
        for (int i = 0; i < nonSimContacts.size(); i++) {
            lOperations.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Contacts.CONTENT_URI, ((Long) nonSimContacts.get(i)).longValue())).build());
            if (lOperations.size() % 50 == 0) {
                try {
                    ExceptionCapture.checkContactDeleteResult(lResolver.applyBatch("com.android.contacts", lOperations), "the returned value is not correct when deleting contact from database");
                    if (HwLog.HWDBG) {
                        HwLog.v("ExtendedContactSaveService", "Applied batch");
                    }
                    numberOfContactsDeleted += lOperations.size();
                    updateDeleteNotification(numberOfContactsDeleted, aContactIds.length);
                    sendMessage(1, numberOfContactsDeleted, msger);
                    lOperations.clear();
                } catch (RemoteException e) {
                    HwLog.e("ExtendedContactSaveService", e.getMessage(), e);
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e);
                    return;
                } catch (OperationApplicationException e2) {
                    HwLog.e("ExtendedContactSaveService", e2.getMessage(), e2);
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e2);
                    return;
                } catch (RuntimeException e3) {
                    ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e3);
                    return;
                }
            }
        }
        if (!lOperations.isEmpty()) {
            try {
                ExceptionCapture.checkContactDeleteResult(lResolver.applyBatch("com.android.contacts", lOperations), "the returned value is not correct when deleting contact from database");
                if (HwLog.HWDBG) {
                    HwLog.v("ExtendedContactSaveService", "Applied batch");
                }
                numberOfContactsDeleted += lOperations.size();
                updateDeleteNotification(numberOfContactsDeleted, aContactIds.length);
                sendMessage(1, numberOfContactsDeleted, msger);
            } catch (RemoteException e4) {
                HwLog.e("ExtendedContactSaveService", e4.getMessage(), e4);
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e4, e4);
                return;
            } catch (OperationApplicationException e22) {
                HwLog.e("ExtendedContactSaveService", e22.getMessage(), e22);
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e22, e22);
                return;
            } catch (RuntimeException e32) {
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: " + e32, e32);
                return;
            }
        }
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Total number of contacts deleted : " + numberOfContactsDeleted);
        }
        onDeleteContactsFinished(getResources().getQuantityString(R.plurals.delete_contact_notification, numberOfContactsDeleted, new Object[]{Integer.valueOf(numberOfContactsDeleted)}), 0);
        sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
        stopForeground(true);
    }

    private void markUnmarkFavoriteSelectedContacts(long[] aContactIds, boolean aFavorite) {
        if (HwLog.HWFLOW) {
            HwLog.i("ExtendedContactSaveService", "Inside markUnmarkFavoriteSelectedContacts for " + aContactIds.length + " contacts, mark favorite : " + aFavorite);
        }
        ContentResolver lResolver = getContentResolver();
        ContentValues lValues = new ContentValues();
        lValues.put("starred", Boolean.valueOf(aFavorite));
        int i = 1;
        StringBuilder lWhereArgs = new StringBuilder("(");
        while (i <= aContactIds.length) {
            lWhereArgs.append(aContactIds[i - 1]).append(",");
            if (i % 50 == 0) {
                int length = lWhereArgs.length();
                lWhereArgs.replace(length - 1, length, ")");
                int lDel = lResolver.update(Contacts.CONTENT_URI, lValues, "_id in " + lWhereArgs.toString(), null);
                if (HwLog.HWDBG) {
                    HwLog.v("ExtendedContactSaveService", "Updated : " + lDel);
                    HwLog.v("ExtendedContactSaveService", "Applied batch");
                }
                lWhereArgs.delete(1, length);
            }
            i++;
        }
        if (lWhereArgs.length() > 1) {
            length = lWhereArgs.length();
            lWhereArgs.replace(length - 1, length, ")");
            lResolver.update(Contacts.CONTENT_URI, lValues, "_id in " + lWhereArgs.toString(), null);
            if (HwLog.HWDBG) {
                HwLog.v("ExtendedContactSaveService", "Applied batch");
            }
        }
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Total number of contacts updated : " + (i - 1));
        }
        sendBroadcast(new Intent("com.android.contacts.favorites.updated"));
    }

    private void markAndUnmarkPrivateContacts(long[] aContactIds, boolean aPrivate) {
        if (aContactIds == null || aContactIds.length == 0) {
            HwLog.e("ExtendedContactSaveService", "Invalid arguments selected request");
            return;
        }
        int j;
        if (HwLog.HWFLOW) {
            HwLog.i("ExtendedContactSaveService", "Inside markAndUnmarkPrivateContacts for " + aContactIds.length + " contacts, mark favorite : " + aPrivate);
        }
        ContentResolver lResolver = getContentResolver();
        ContentValues lValues = new ContentValues();
        lValues.put("is_private", Integer.valueOf(aPrivate ? 1 : 0));
        int loopTimes = aContactIds.length / 50;
        for (int i = 0; i < loopTimes; i++) {
            StringBuilder lWhereArgs = new StringBuilder("(");
            for (j = i * 50; j < (i + 1) * 50; j++) {
                lWhereArgs.append(aContactIds[j]).append(",");
            }
            lWhereArgs.replace(lWhereArgs.length() - 1, lWhereArgs.length(), ")");
            lResolver.update(Contacts.CONTENT_URI, lValues, "_id IN " + lWhereArgs.toString(), null);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                HwLog.w("ExtendedContactSaveService", "markAndUnmarkPrivateContacts InterruptedException");
            }
        }
        if (aContactIds.length % 50 != 0) {
            lWhereArgs = new StringBuilder("(");
            for (j = loopTimes * 50; j < aContactIds.length; j++) {
                lWhereArgs.append(aContactIds[j]).append(",");
            }
            lWhereArgs.replace(lWhereArgs.length() - 1, lWhereArgs.length(), ")");
            lResolver.update(Contacts.CONTENT_URI, lValues, "_id IN " + lWhereArgs.toString(), null);
        }
    }

    public void onDeleteContactsFinished(String aDescription, int jobId) {
        this.mNotificationManager.cancelAll();
    }

    static Notification constructProgressNotification(Context context, String description, String tickerText, int jobId, int totalCount, int currentCount) {
        boolean z;
        CommonUtilMethods.constructAndSendSummaryNotification(context, description);
        Notification.Builder builder = new Notification.Builder(context);
        Notification.Builder ongoing = builder.setOngoing(true);
        if (totalCount == -1) {
            z = true;
        } else {
            z = false;
        }
        ongoing.setProgress(totalCount, currentCount, z).setContentTitle(description).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true).setSmallIcon(CommonUtilMethods.getBitampIcon(context, R.drawable.ic_notification_contacts));
        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        percentInstance.setMaximumFractionDigits(0);
        if (totalCount > 0) {
            builder.setContentText(percentInstance.format(((double) currentCount) / ((double) totalCount)));
        }
        return builder.getNotification();
    }

    private void addOrRemoveGroupMembership(android.content.Intent r25) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r24 = this;
        r6 = "addMembers";
        r0 = r25;
        r19 = r0.getParcelableExtra(r6);
        r19 = (com.android.contacts.hap.GroupAndContactMetaData) r19;
        r6 = "isFromGroups";
        r7 = 0;
        r0 = r25;
        r17 = r0.getBooleanExtra(r6, r7);
        r0 = r19;
        r6 = r0.contactIds;
        if (r6 == 0) goto L_0x0027;
    L_0x001b:
        r0 = r19;
        r6 = r0.groupsToAdd;
        if (r6 != 0) goto L_0x0028;
    L_0x0021:
        r0 = r19;
        r6 = r0.groupsToRemove;
        if (r6 != 0) goto L_0x0028;
    L_0x0027:
        return;
    L_0x0028:
        r4 = r24.getContentResolver();
        r23 = new java.lang.StringBuilder;
        r23.<init>();
        r22 = new java.util.ArrayList;
        r22.<init>();
        r20 = 0;
        r21 = 0;
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToAdd;	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x00eb;	 Catch:{ all -> 0x017a }
    L_0x0040:
        r15 = 0;	 Catch:{ all -> 0x017a }
    L_0x0041:
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToAdd;	 Catch:{ all -> 0x017a }
        r6 = r6.length;	 Catch:{ all -> 0x017a }
        if (r15 >= r6) goto L_0x00eb;	 Catch:{ all -> 0x017a }
    L_0x0048:
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToAdd;	 Catch:{ all -> 0x017a }
        r13 = r6[r15];	 Catch:{ all -> 0x017a }
        if (r13 == 0) goto L_0x00a8;	 Catch:{ all -> 0x017a }
    L_0x0050:
        r12 = new com.android.contacts.model.account.AccountWithDataSet;	 Catch:{ all -> 0x017a }
        r6 = r13.accountName;	 Catch:{ all -> 0x017a }
        r7 = r13.accountType;	 Catch:{ all -> 0x017a }
        r8 = r13.accountDataSet;	 Catch:{ all -> 0x017a }
        r12.<init>(r6, r7, r8);	 Catch:{ all -> 0x017a }
        r0 = r20;	 Catch:{ all -> 0x017a }
        r6 = r12.equals(r0);	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x00ab;	 Catch:{ all -> 0x017a }
    L_0x0063:
        if (r21 == 0) goto L_0x00a8;	 Catch:{ all -> 0x017a }
    L_0x0065:
        r6 = r21.moveToFirst();	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x00a8;	 Catch:{ all -> 0x017a }
    L_0x006b:
        r6 = r21.getCount();	 Catch:{ all -> 0x017a }
        r0 = new long[r6];	 Catch:{ all -> 0x017a }
        r18 = r0;	 Catch:{ all -> 0x017a }
        r16 = 0;	 Catch:{ all -> 0x017a }
    L_0x0075:
        r6 = 0;	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r6 = r0.getLong(r6);	 Catch:{ all -> 0x017a }
        r18[r16] = r6;	 Catch:{ all -> 0x017a }
        r16 = r16 + 1;	 Catch:{ all -> 0x017a }
        r6 = r21.moveToNext();	 Catch:{ all -> 0x017a }
        if (r6 != 0) goto L_0x0075;	 Catch:{ all -> 0x017a }
    L_0x0086:
        r5 = new com.android.contacts.hap.util.GroupMemberEditHelper$GroupInfo;	 Catch:{ all -> 0x017a }
        r6 = r13.groupId;	 Catch:{ all -> 0x017a }
        r8 = 1;	 Catch:{ all -> 0x017a }
        r10 = new long[r8][];	 Catch:{ all -> 0x017a }
        r8 = 0;	 Catch:{ all -> 0x017a }
        r10[r8] = r18;	 Catch:{ all -> 0x017a }
        r8 = 0;	 Catch:{ all -> 0x017a }
        r9 = r17;	 Catch:{ all -> 0x017a }
        r5.<init>(r6, r8, r9, r10);	 Catch:{ all -> 0x017a }
        r14 = new com.android.contacts.hap.util.GroupMemberEditHelper;	 Catch:{ all -> 0x017a }
        r6 = r24.getContentResolver();	 Catch:{ all -> 0x017a }
        r14.<init>(r6);	 Catch:{ all -> 0x017a }
        r6 = 1;	 Catch:{ all -> 0x017a }
        r6 = new com.android.contacts.hap.util.GroupMemberEditHelper.GroupInfo[r6];	 Catch:{ all -> 0x017a }
        r7 = 0;	 Catch:{ all -> 0x017a }
        r6[r7] = r5;	 Catch:{ all -> 0x017a }
        r14.executeInPararell(r6);	 Catch:{ all -> 0x017a }
    L_0x00a8:
        r15 = r15 + 1;	 Catch:{ all -> 0x017a }
        goto L_0x0041;	 Catch:{ all -> 0x017a }
    L_0x00ab:
        r6 = 0;	 Catch:{ all -> 0x017a }
        r0 = r23;	 Catch:{ all -> 0x017a }
        r0.setLength(r6);	 Catch:{ all -> 0x017a }
        r22.clear();	 Catch:{ all -> 0x017a }
        r20 = r12;	 Catch:{ all -> 0x017a }
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.contactIds;	 Catch:{ all -> 0x017a }
        r0 = r24;	 Catch:{ all -> 0x017a }
        r1 = r23;	 Catch:{ all -> 0x017a }
        r2 = r22;	 Catch:{ all -> 0x017a }
        r0.buildSelectionForGroups(r1, r2, r6, r13);	 Catch:{ all -> 0x017a }
        if (r21 == 0) goto L_0x00ca;	 Catch:{ all -> 0x017a }
    L_0x00c5:
        r21.close();	 Catch:{ all -> 0x017a }
        r21 = 0;	 Catch:{ all -> 0x017a }
    L_0x00ca:
        r5 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ all -> 0x017a }
        r6 = 1;	 Catch:{ all -> 0x017a }
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x017a }
        r7 = "_id";	 Catch:{ all -> 0x017a }
        r8 = 0;	 Catch:{ all -> 0x017a }
        r6[r8] = r7;	 Catch:{ all -> 0x017a }
        r7 = r23.toString();	 Catch:{ all -> 0x017a }
        r8 = 0;	 Catch:{ all -> 0x017a }
        r8 = new java.lang.String[r8];	 Catch:{ all -> 0x017a }
        r0 = r22;	 Catch:{ all -> 0x017a }
        r8 = r0.toArray(r8);	 Catch:{ all -> 0x017a }
        r8 = (java.lang.String[]) r8;	 Catch:{ all -> 0x017a }
        r9 = 0;	 Catch:{ all -> 0x017a }
        r21 = r4.query(r5, r6, r7, r8, r9);	 Catch:{ all -> 0x017a }
        goto L_0x0063;	 Catch:{ all -> 0x017a }
    L_0x00eb:
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToRemove;	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x0174;	 Catch:{ all -> 0x017a }
    L_0x00f1:
        r15 = 0;	 Catch:{ all -> 0x017a }
    L_0x00f2:
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToRemove;	 Catch:{ all -> 0x017a }
        r6 = r6.length;	 Catch:{ all -> 0x017a }
        if (r15 >= r6) goto L_0x0174;	 Catch:{ all -> 0x017a }
    L_0x00f9:
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.groupsToRemove;	 Catch:{ all -> 0x017a }
        r13 = r6[r15];	 Catch:{ all -> 0x017a }
        if (r13 == 0) goto L_0x0131;	 Catch:{ all -> 0x017a }
    L_0x0101:
        r12 = new com.android.contacts.model.account.AccountWithDataSet;	 Catch:{ all -> 0x017a }
        r6 = r13.accountName;	 Catch:{ all -> 0x017a }
        r7 = r13.accountType;	 Catch:{ all -> 0x017a }
        r8 = r13.accountDataSet;	 Catch:{ all -> 0x017a }
        r12.<init>(r6, r7, r8);	 Catch:{ all -> 0x017a }
        r0 = r20;	 Catch:{ all -> 0x017a }
        r6 = r12.equals(r0);	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x0134;	 Catch:{ all -> 0x017a }
    L_0x0114:
        if (r21 == 0) goto L_0x0131;	 Catch:{ all -> 0x017a }
    L_0x0116:
        r6 = r21.moveToFirst();	 Catch:{ all -> 0x017a }
        if (r6 == 0) goto L_0x0131;	 Catch:{ all -> 0x017a }
    L_0x011c:
        r6 = 0;	 Catch:{ all -> 0x017a }
        r0 = r21;	 Catch:{ all -> 0x017a }
        r8 = r0.getLong(r6);	 Catch:{ all -> 0x017a }
        r10 = r13.groupId;	 Catch:{ all -> 0x017a }
        r6 = r24;	 Catch:{ all -> 0x017a }
        r7 = r4;	 Catch:{ all -> 0x017a }
        r6.removeMembersFromGroup(r7, r8, r10);	 Catch:{ all -> 0x017a }
        r6 = r21.moveToNext();	 Catch:{ all -> 0x017a }
        if (r6 != 0) goto L_0x011c;	 Catch:{ all -> 0x017a }
    L_0x0131:
        r15 = r15 + 1;	 Catch:{ all -> 0x017a }
        goto L_0x00f2;	 Catch:{ all -> 0x017a }
    L_0x0134:
        r6 = 0;	 Catch:{ all -> 0x017a }
        r0 = r23;	 Catch:{ all -> 0x017a }
        r0.setLength(r6);	 Catch:{ all -> 0x017a }
        r22.clear();	 Catch:{ all -> 0x017a }
        r20 = r12;	 Catch:{ all -> 0x017a }
        r0 = r19;	 Catch:{ all -> 0x017a }
        r6 = r0.contactIds;	 Catch:{ all -> 0x017a }
        r0 = r24;	 Catch:{ all -> 0x017a }
        r1 = r23;	 Catch:{ all -> 0x017a }
        r2 = r22;	 Catch:{ all -> 0x017a }
        r0.buildSelectionForGroups(r1, r2, r6, r13);	 Catch:{ all -> 0x017a }
        if (r21 == 0) goto L_0x0153;	 Catch:{ all -> 0x017a }
    L_0x014e:
        r21.close();	 Catch:{ all -> 0x017a }
        r21 = 0;	 Catch:{ all -> 0x017a }
    L_0x0153:
        r7 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ all -> 0x017a }
        r6 = 1;	 Catch:{ all -> 0x017a }
        r8 = new java.lang.String[r6];	 Catch:{ all -> 0x017a }
        r6 = "_id";	 Catch:{ all -> 0x017a }
        r9 = 0;	 Catch:{ all -> 0x017a }
        r8[r9] = r6;	 Catch:{ all -> 0x017a }
        r9 = r23.toString();	 Catch:{ all -> 0x017a }
        r6 = 0;	 Catch:{ all -> 0x017a }
        r6 = new java.lang.String[r6];	 Catch:{ all -> 0x017a }
        r0 = r22;	 Catch:{ all -> 0x017a }
        r10 = r0.toArray(r6);	 Catch:{ all -> 0x017a }
        r10 = (java.lang.String[]) r10;	 Catch:{ all -> 0x017a }
        r11 = 0;	 Catch:{ all -> 0x017a }
        r6 = r4;	 Catch:{ all -> 0x017a }
        r21 = r6.query(r7, r8, r9, r10, r11);	 Catch:{ all -> 0x017a }
        goto L_0x0114;
    L_0x0174:
        if (r21 == 0) goto L_0x0179;
    L_0x0176:
        r21.close();
    L_0x0179:
        return;
    L_0x017a:
        r6 = move-exception;
        if (r21 == 0) goto L_0x0180;
    L_0x017d:
        r21.close();
    L_0x0180:
        throw r6;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.delete.ExtendedContactSaveService.addOrRemoveGroupMembership(android.content.Intent):void");
    }

    private void buildSelectionForGroups(StringBuilder aSelectionBuilder, List<String> aSelectionArgs, long[] aContactIds, GroupsData aGroup) {
        aSelectionBuilder.append("contact_id IN (");
        for (long contactId : aContactIds) {
            aSelectionBuilder.append(contactId).append(",");
        }
        aSelectionBuilder.setLength(aSelectionBuilder.length() - 1);
        aSelectionBuilder.append(")");
        aSelectionBuilder.append(" AND deleted=0 AND ").append("account_name=?").append(" AND account_type=? AND ");
        aSelectionArgs.add(aGroup.accountName);
        aSelectionArgs.add(aGroup.accountType);
        if (aGroup.accountDataSet == null) {
            aSelectionBuilder.append("data_set IS NULL");
            return;
        }
        aSelectionBuilder.append("data_set=?");
        aSelectionArgs.add(aGroup.accountDataSet);
    }

    private void removeMembersFromGroup(ContentResolver resolver, long rawContactId, long groupId) {
        resolver.delete(Data.CONTENT_URI, "raw_contact_id=? AND mimetype=? AND data1=?", new String[]{String.valueOf(rawContactId), "vnd.android.cursor.item/group_membership", String.valueOf(groupId)});
    }

    private void removeContactsFromFrequentList(String aLookupKey) {
        getContentResolver().delete(Uri.withAppendedPath(DatabaseConstants.DATA_USAGE_DELETE_URI, aLookupKey), null, null);
    }

    private void removeContactsFromFrequentList(long aId) {
        Cursor cursor = getContentResolver().query(Data.CONTENT_URI, new String[]{"contact_id"}, "_id=" + aId, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    removeContactsFromFrequentList(cursor.getString(0));
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
    }

    public static Intent createDeleteSelectedContactsIntent(Context context, long[] aContactIds, Messenger msger) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create Delete Intent");
        }
        Intent serviceIntent = new Intent(context, ExtendedContactSaveService.class);
        serviceIntent.setAction("deleteMultiple");
        serviceIntent.putExtra("ContactIds", aContactIds);
        serviceIntent.putExtra("messenger", msger);
        return serviceIntent;
    }

    public static Intent createAddCompanyContactsIntent(Context context, long[] aContactIds, String company, boolean isNoCompanyGroup) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create add contacts to company Intent");
        }
        Intent serviceIntent = new Intent(context, ExtendedContactSaveService.class);
        serviceIntent.setAction("add_company_contacts");
        serviceIntent.putExtra("ContactIds", aContactIds);
        serviceIntent.putExtra("company_name", company);
        serviceIntent.putExtra("is_no_company_group", isNoCompanyGroup);
        return serviceIntent;
    }

    public static Intent createMarkUnmarkFavoriteSelectedContactsIntent(Context context, long[] aContactIds, boolean aFavorite) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create favorute Intent : " + aFavorite);
        }
        Intent serviceIntent = new Intent(context, ExtendedContactSaveService.class);
        serviceIntent.setAction("markUnmarkFavoriteMultiple");
        serviceIntent.putExtra("ContactIds", aContactIds);
        serviceIntent.putExtra("markFavorite", aFavorite);
        return serviceIntent;
    }

    public static Intent createClearUsageSelectedContactsIntent(Context context, long[] dataIds) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create clear usage Intent : ");
        }
        Intent serviceIntent = new Intent(context, ExtendedContactSaveService.class);
        serviceIntent.setAction("clearUsageForSelectedContactsData");
        serviceIntent.putExtra("extra_data_ids", dataIds);
        return serviceIntent;
    }

    public static Intent createMarkUnmarkPrivateContactsIntent(Context context, long[] aContactIds, boolean aPrivate) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create Intent for setting Private Contacts ");
        }
        Intent serviceIntent = new Intent(context, ExtendedContactSaveService.class);
        serviceIntent.setAction("markUnmarkPrivateContacts");
        serviceIntent.putExtra("ContactIds", aContactIds);
        serviceIntent.putExtra("markPrivate", aPrivate);
        return serviceIntent;
    }

    public static Intent createAddAndRemoveMembersToGroupIntent(Context aContext, GroupAndContactMetaData aGroupAndContactMetaData, boolean isCalledFromContacts) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create add to members intent");
        }
        Intent serviceIntent = new Intent(aContext, ExtendedContactSaveService.class);
        serviceIntent.setAction("add_members_to_group");
        serviceIntent.putExtra("addMembers", aGroupAndContactMetaData);
        serviceIntent.putExtra("isFromGroups", isCalledFromContacts);
        return serviceIntent;
    }

    private void executeSimBatch(ArrayList<ContentProviderOperation> aOperation, long sleep_time) {
        try {
            this.mExecutor.execute(new ContactsBatchDetelion(getContentResolver(), aOperation));
        } catch (RejectedExecutionException e) {
            HwLog.e("ExtendedContactSaveService", e.getMessage(), e);
        }
        if (sleep_time > 0) {
            try {
                Thread.sleep(sleep_time);
            } catch (InterruptedException e2) {
                HwLog.e("ExtendedContactSaveService", "executeSimBatch InterruptedException");
            }
        }
    }

    private void updateDeleteNotification(int numberOfContactsDeleted, int totalCount) {
        String description = String.format(getString(R.string.deleting_contact_description), new Object[]{getString(R.string.contactsList).toLowerCase()});
        String totalCountString = String.valueOf(totalCount);
        startForeground(1, constructProgressNotification(getApplicationContext(), description, String.format(getString(R.string.progress_delete_notifier_message, new Object[]{String.valueOf(numberOfContactsDeleted), totalCountString}), new Object[0]), 0, totalCount, numberOfContactsDeleted));
    }

    public static ArrayList<EfidIndexObject> getEfidAndIndexForSimContact(ArrayList<Long> aContactIds, Context context) {
        String[] projection = new String[]{"sync1", "sync2", "account_type"};
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList(0);
        ArrayList<EfidIndexObject> list = new ArrayList();
        buildSelectionAndArgsForSimContactsId(aContactIds, selection, selectionArgs);
        if (HwLog.HWFLOW) {
            HwLog.i("ExtendedContactSaveService", "getEfidAndIndexForSimContact,seletion=" + selection.toString() + ",selectionArgs=" + selectionArgs);
        }
        Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, projection, selection.toString(), (String[]) selectionArgs.toArray(new String[0]), null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        EfidIndexObject object = new EfidIndexObject();
                        object.efid = cursor.getString(0);
                        object.index = cursor.getString(1);
                        object.accountType = cursor.getString(2);
                        if (HwLog.HWFLOW) {
                            HwLog.i("ExtendedContactSaveService", "getEfidAndIndexForSimContact ACCOUNT TYPE FOR DELETION" + object.accountType);
                        }
                        list.add(object);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return list;
    }

    private static void buildSelectionAndArgsForSimContactsId(ArrayList<Long> contactIds, StringBuilder selection, ArrayList<String> selectionArgs) {
        if (selection != null && selectionArgs != null) {
            StringBuilder idsBuilder = new StringBuilder();
            getCommaSeperatedIds(contactIds, idsBuilder);
            if (SimFactoryManager.isDualSim()) {
                selection.append("contact_id IN (").append(idsBuilder.toString()).append(")").append(" AND (").append("account_type=? OR account_type=? )");
                selectionArgs.add("com.android.huawei.sim");
                selectionArgs.add("com.android.huawei.secondsim");
            } else {
                selection.append("contact_id IN (").append(idsBuilder.toString()).append(")").append("AND account_type=?");
                selectionArgs.add("com.android.huawei.sim");
            }
        }
    }

    public static ArrayList<EfidIndexObject> getEfidAndIndexForSimRawContact(ArrayList<Long> aContactIds, Context context) {
        StringBuilder builder = new StringBuilder();
        getCommaSeperatedIds(aContactIds, builder);
        String selection = "_id IN (" + builder.toString() + ")";
        String[] projection = new String[]{"sync1", "sync2", "account_type"};
        ArrayList<EfidIndexObject> list = new ArrayList();
        Cursor cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, projection, selection, null, null);
        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    do {
                        EfidIndexObject object = new EfidIndexObject();
                        object.efid = cursor.getString(0);
                        object.index = cursor.getString(1);
                        object.accountType = cursor.getString(2);
                        list.add(object);
                    } while (cursor.moveToNext());
                }
                cursor.close();
            } catch (Throwable th) {
                cursor.close();
            }
        }
        return list;
    }

    public static int deleteSingleSimContactFromSim(EfidIndexObject obj, Context aContext) {
        SimFactory lSimFactory = SimFactoryManager.getSimFactory(obj.accountType);
        if (lSimFactory == null) {
            return 0;
        }
        deleteSingleContactFromDatabase(obj, aContext, obj.accountType);
        int count = lSimFactory.getSimPersistanceManager().delete(obj.efid, obj.index);
        if (HwLog.HWDBG) {
            HwLog.d("ExtendedContactSaveService", "Number of contacts deleted from SIM is " + count);
        }
        return count;
    }

    public static int deleteSingleContactFromDatabase(EfidIndexObject obj, Context aContext, String accountType) {
        StringBuilder builder = new StringBuilder();
        builder.append("sync1").append("=").append(obj.efid).append(" AND ").append("sync2").append(" = ").append(obj.index).append(" AND ").append("account_type=?");
        String[] selectionArgs = new String[]{accountType};
        int ret = 0;
        try {
            ret = aContext.getContentResolver().delete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", String.valueOf(true)).build(), builder.toString(), selectionArgs);
            if (ret <= 0) {
                ExceptionCapture.captureSimContactDeleteException("the returned value is " + ret + " when deleting SIM contact from database", null);
            }
        } catch (RuntimeException e) {
            ExceptionCapture.captureSimContactDeleteException("deleting SIM contact from database error: " + e, e);
        }
        return ret;
    }

    private int deleteSingleSimContactForMultSelection(EfidIndexObject obj, Context aContext) {
        SimFactory lSimFactory = SimFactoryManager.getSimFactory(obj.accountType);
        if (lSimFactory == null) {
            return 0;
        }
        int count = lSimFactory.getSimPersistanceManager().delete(obj.efid, obj.index);
        if (count > 0) {
            StringBuilder builder = new StringBuilder();
            builder.append("sync1").append("=").append(obj.efid).append(" AND ").append("sync2").append(" = ").append(obj.index).append(" AND ").append("account_type=?");
            String[] selectionArgs = new String[]{obj.accountType};
            Builder lBuilder = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI.buildUpon().appendQueryParameter("caller_is_syncadapter", String.valueOf(true)).build());
            lBuilder.withSelection(builder.toString(), selectionArgs);
            this.mDeleteOperations.add(lBuilder.build());
        }
        return count;
    }

    public static Intent createRemoveCallLogEntriesIntent(Context aContext, long[] ids) {
        if (HwLog.HWDBG) {
            HwLog.v("ExtendedContactSaveService", "Create remove call log entries intent");
        }
        Intent serviceIntent = new Intent(aContext, ExtendedContactSaveService.class);
        serviceIntent.setAction("remove_call_log_entries");
        serviceIntent.putExtra("removedEntries", ids);
        return serviceIntent;
    }

    private void removeCallLogEntries(long[] ids) {
        if (ids != null && ids.length >= 1) {
            StringBuilder callIds = new StringBuilder();
            boolean appendComma = false;
            for (long id : ids) {
                if (appendComma) {
                    callIds.append(",");
                }
                callIds.append(id);
                appendComma = true;
            }
            if (CommonUtilMethods.getShowCallLogMergeStatus(getApplicationContext())) {
                callIds = CommonUtilMethods.getDeleteCallLogIds(getApplicationContext(), callIds);
            }
            getContentResolver().delete(QueryUtil.getCallsContentUri(), "_id IN (" + callIds + ")", null);
            new RemoveMissedCallNotification(this).executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
        }
    }

    private static void getCommaSeperatedIds(ArrayList<Long> aContactIds, StringBuilder aBuilder) {
        if (aBuilder != null) {
            for (Long contactId : aContactIds) {
                aBuilder.append(contactId).append(",");
            }
            if (aBuilder.length() > 0) {
                aBuilder.setLength(aBuilder.length() - 1);
            }
        }
    }

    private void sendMessage(int what, int count, Messenger msger) {
        Message msg = Message.obtain();
        msg.what = what;
        msg.arg1 = count;
        try {
            msger.send(msg);
        } catch (RemoteException e) {
            HwLog.w("ExtendedContactSaveService", "Fail to send message.");
        }
    }
}
