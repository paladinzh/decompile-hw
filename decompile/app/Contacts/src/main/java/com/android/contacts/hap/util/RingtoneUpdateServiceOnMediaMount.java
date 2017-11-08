package com.android.contacts.hap.util;

import android.content.ContentProviderOperation;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.os.RemoteException;
import com.android.contacts.util.HwLog;
import java.util.ArrayList;

public class RingtoneUpdateServiceOnMediaMount extends IntentServiceWithWakeLock {
    private String TAG = RingtoneUpdateServiceOnMediaMount.class.getSimpleName();
    SharedPreferences mPreference;

    private static class DataHolder {
        String mMediaFilePath;
        String mNewRingtoneUri;
        String mOldRingtoneUri;

        public DataHolder(String oldRingtoneUri, String newRingtoneUri, String mediaFilePath) {
            this.mOldRingtoneUri = oldRingtoneUri;
            this.mNewRingtoneUri = newRingtoneUri;
            this.mMediaFilePath = mediaFilePath;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o instanceof DataHolder) {
                DataHolder anotherObject = (DataHolder) o;
                if (this.mOldRingtoneUri != null && this.mOldRingtoneUri.equals(anotherObject.mOldRingtoneUri)) {
                    return true;
                }
            }
            return false;
        }

        public int hashCode() {
            return this.mOldRingtoneUri.hashCode();
        }
    }

    private void updateRingtoneForContacts(android.content.Context r29) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x016d in list []
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
        r28 = this;
        r15 = 0;
        r2 = 3;
        r4 = new java.lang.String[r2];
        r2 = "_id";
        r3 = 0;
        r4[r3] = r2;
        r2 = "lookup";
        r3 = 1;
        r4[r3] = r2;
        r2 = "custom_ringtone";
        r3 = 2;
        r4[r3] = r2;
        r8 = 0;
        r9 = 1;
        r10 = 2;
        r11 = 10;
        r23 = com.google.common.collect.Lists.newArrayList();
        r14 = new java.util.HashSet;
        r14.<init>();
        r2 = r29.getContentResolver();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = android.provider.ContactsContract.Contacts.CONTENT_URI;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = "custom_ringtone IS NOT NULL";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r6 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r7 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r15 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r17 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r24 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r25 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r27 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r13 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r16 = 0;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = android.provider.MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r12 = r2.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r15 == 0) goto L_0x00af;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x0046:
        r2 = r15.moveToFirst();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x00af;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x004c:
        r2 = 2;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r17 = r15.getString(r2);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = android.net.Uri.parse(r17);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r29;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r27 = android.media.RingtoneManager.getRingtone(r0, r2);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r27 == 0) goto L_0x0067;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x005d:
        r2 = r27.getTitle(r28);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = android.text.TextUtils.isEmpty(r2);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x00ef;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x0067:
        r0 = r28;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r0.mPreference;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r21 = r2.getAll();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r21;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r17;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r24 = r0.get(r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r24 = (java.lang.String) r24;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = com.android.contacts.util.HwLog.HWDBG;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x00a7;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x007d:
        r0 = r28;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r0.TAG;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3.<init>();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = "getPathFromSP: key=";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r17;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = ", value=";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r24;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        com.android.contacts.util.HwLog.d(r2, r3);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x00a7:
        if (r24 != 0) goto L_0x00f9;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x00a9:
        r2 = r15.moveToNext();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 != 0) goto L_0x004c;
    L_0x00af:
        if (r15 == 0) goto L_0x00b4;
    L_0x00b1:
        r15.close();
    L_0x00b4:
        r2 = r23.size();
        if (r2 <= 0) goto L_0x00c1;
    L_0x00ba:
        r0 = r28;
        r1 = r23;
        r0.update(r1);
    L_0x00c1:
        r0 = r28;
        r2 = r0.mPreference;
        r26 = r2.edit();
        r20 = r14.iterator();
    L_0x00cd:
        r2 = r20.hasNext();
        if (r2 == 0) goto L_0x0234;
    L_0x00d3:
        r19 = r20.next();
        r19 = (com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount.DataHolder) r19;
        r0 = r19;
        r2 = r0.mOldRingtoneUri;
        r0 = r26;
        r2 = r0.remove(r2);
        r0 = r19;
        r3 = r0.mNewRingtoneUri;
        r0 = r19;
        r5 = r0.mMediaFilePath;
        r2.putString(r3, r5);
        goto L_0x00cd;
    L_0x00ef:
        r0 = r17;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r0.contains(r12);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x00a9;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x00f7:
        goto L_0x0067;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x00f9:
        r0 = r29;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r24;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r25 = com.android.contacts.hap.CommonUtilMethods.getRingtoneUriFromPath(r0, r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = com.android.contacts.util.HwLog.HWDBG;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x012f;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x0105:
        r0 = r28;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r0.TAG;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3.<init>();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = "getUriFromPath:";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r24;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = "->";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r25;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        com.android.contacts.util.HwLog.d(r2, r3);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x012f:
        if (r25 == 0) goto L_0x00a9;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x0131:
        r0 = r29;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r25;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = com.android.contacts.hap.CommonUtilMethods.getPathFromUri(r0, r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r2 == 0) goto L_0x00a9;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x013b:
        r0 = r29;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r25;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r27 = android.media.RingtoneManager.getRingtone(r0, r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r27 != 0) goto L_0x01a8;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x0145:
        r0 = r28;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r0.TAG;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3.<init>();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = "the ringtone of possibleRingtoneUri is null: ";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r25;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.append(r0);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r3.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        com.android.contacts.util.HwLog.e(r2, r3);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        goto L_0x00a9;
    L_0x0164:
        r18 = move-exception;
        r18.printStackTrace();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r15 == 0) goto L_0x016d;
    L_0x016a:
        r15.close();
    L_0x016d:
        r2 = r23.size();
        if (r2 <= 0) goto L_0x017a;
    L_0x0173:
        r0 = r28;
        r1 = r23;
        r0.update(r1);
    L_0x017a:
        r0 = r28;
        r2 = r0.mPreference;
        r26 = r2.edit();
        r20 = r14.iterator();
    L_0x0186:
        r2 = r20.hasNext();
        if (r2 == 0) goto L_0x023b;
    L_0x018c:
        r19 = r20.next();
        r19 = (com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount.DataHolder) r19;
        r0 = r19;
        r2 = r0.mOldRingtoneUri;
        r0 = r26;
        r2 = r0.remove(r2);
        r0 = r19;
        r3 = r0.mNewRingtoneUri;
        r0 = r19;
        r5 = r0.mMediaFilePath;
        r2.putString(r3, r5);
        goto L_0x0186;
    L_0x01a8:
        r2 = 0;
        r2 = r15.getLong(r2);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = 1;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = r15.getString(r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r13 = android.provider.ContactsContract.Contacts.getLookupUri(r2, r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = android.content.ContentProviderOperation.newUpdate(r13);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = "custom_ringtone";	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r5 = r25.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = r2.withValue(r3, r5);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r22 = r2.build();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r23;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r22;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0.add(r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r16 = r23.size();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = new com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount$DataHolder;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r3 = r25.toString();	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r17;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r24;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2.<init>(r0, r3, r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r14.add(r2);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r2 = 10;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0 = r16;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        if (r0 < r2) goto L_0x00a9;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
    L_0x01ea:
        r0 = r28;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r1 = r23;	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        r0.update(r1);	 Catch:{ SQLiteException -> 0x0164, all -> 0x01f3 }
        goto L_0x00a9;
    L_0x01f3:
        r2 = move-exception;
        if (r15 == 0) goto L_0x01f9;
    L_0x01f6:
        r15.close();
    L_0x01f9:
        r3 = r23.size();
        if (r3 <= 0) goto L_0x0206;
    L_0x01ff:
        r0 = r28;
        r1 = r23;
        r0.update(r1);
    L_0x0206:
        r0 = r28;
        r3 = r0.mPreference;
        r26 = r3.edit();
        r20 = r14.iterator();
    L_0x0212:
        r3 = r20.hasNext();
        if (r3 == 0) goto L_0x0242;
    L_0x0218:
        r19 = r20.next();
        r19 = (com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount.DataHolder) r19;
        r0 = r19;
        r3 = r0.mOldRingtoneUri;
        r0 = r26;
        r3 = r0.remove(r3);
        r0 = r19;
        r5 = r0.mNewRingtoneUri;
        r0 = r19;
        r6 = r0.mMediaFilePath;
        r3.putString(r5, r6);
        goto L_0x0212;
    L_0x0234:
        r26.commit();
        r14.clear();
    L_0x023a:
        return;
    L_0x023b:
        r26.commit();
        r14.clear();
        goto L_0x023a;
    L_0x0242:
        r26.commit();
        r14.clear();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.util.RingtoneUpdateServiceOnMediaMount.updateRingtoneForContacts(android.content.Context):void");
    }

    public RingtoneUpdateServiceOnMediaMount() {
        super(RingtoneUpdateServiceOnMediaMount.class.getSimpleName());
    }

    public void onCreate() {
        super.onCreate();
        this.mPreference = getSharedPreferences("com.android.contacts.custom_ringtone_emui_2.0", 0);
    }

    protected void doWakefulWork(Intent intent) {
        if (this.mPreference.getAll().isEmpty()) {
            if (HwLog.HWDBG) {
                HwLog.d(this.TAG, "no key-values in sharedpreference");
            }
            return;
        }
        updateRingtoneForContacts(this);
    }

    private void update(ArrayList<ContentProviderOperation> operations) {
        try {
            getContentResolver().applyBatch("com.android.contacts", operations);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } finally {
            operations.clear();
        }
    }
}
