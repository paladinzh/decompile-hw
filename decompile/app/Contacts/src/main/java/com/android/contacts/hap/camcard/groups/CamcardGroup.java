package com.android.contacts.hap.camcard.groups;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class CamcardGroup {
    private static final String TAG = CamcardGroup.class.getSimpleName();

    public static void updateIsCamcardToGroupIfNeed(android.content.Context r18) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00c7 in list []
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
        if (r18 != 0) goto L_0x0003;
    L_0x0002:
        return;
    L_0x0003:
        r10 = 0;
        r2 = r18.getContentResolver();
        r12 = 0;
        r14 = -1;
        r9 = 0;
        r8 = new android.accounts.Account;
        r3 = "Phone";
        r4 = "com.android.huawei.phone";
        r8.<init>(r3, r4);
        r16 = new java.util.ArrayList;
        r16.<init>();
        r3 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = 1;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r5 = "_id";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r6 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r5 = "is_camcard in(1,2) AND account_name = ? AND account_type = ?";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r6 = 2;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r7 = r8.name;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r17 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r6[r17] = r7;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r7 = r8.type;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r17 = 1;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r6[r17] = r7;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r7 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r10 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x003e:
        if (r10 == 0) goto L_0x00c8;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x0040:
        r3 = r10.moveToNext();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        if (r3 == 0) goto L_0x00c8;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x0046:
        if (r12 != 0) goto L_0x005b;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x0048:
        r0 = r18;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r14 = getCCGroupId(r0, r8);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r12 = 1;
        r4 = 0;
        r3 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1));
        if (r3 > 0) goto L_0x005b;
    L_0x0055:
        if (r10 == 0) goto L_0x005a;
    L_0x0057:
        r10.close();
    L_0x005a:
        return;
    L_0x005b:
        r3 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r13 = android.content.ContentProviderOperation.newInsert(r3);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r3 = "raw_contact_id";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = r10.getInt(r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r13.withValue(r3, r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r3 = "mimetype";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = "vnd.android.cursor.item/group_membership";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r13.withValue(r3, r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r3 = "data1";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = java.lang.Long.valueOf(r14);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r13.withValue(r3, r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r3 = r13.build();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r0 = r16;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r0.add(r3);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r9 = r9 + 1;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r3 = r9 % 100;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        if (r3 != 0) goto L_0x003e;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x0092:
        r3 = r16.isEmpty();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        if (r3 != 0) goto L_0x003e;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x0098:
        r3 = "com.android.contacts";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r0 = r16;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r2.applyBatch(r3, r0);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r16.clear();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        goto L_0x003e;
    L_0x00a4:
        r11 = move-exception;
        r3 = TAG;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4.<init>();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r5 = "updateIsCamcardToGroupIfNeed exception ";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r5 = r11.getMessage();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r4 = r4.toString();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        com.android.contacts.util.HwLog.e(r3, r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        if (r10 == 0) goto L_0x00c7;
    L_0x00c4:
        r10.close();
    L_0x00c7:
        return;
    L_0x00c8:
        r3 = r16.isEmpty();	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        if (r3 != 0) goto L_0x00d6;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x00ce:
        r3 = "com.android.contacts";	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r0 = r16;	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
        r2.applyBatch(r3, r0);	 Catch:{ Exception -> 0x00a4, all -> 0x00dc }
    L_0x00d6:
        if (r10 == 0) goto L_0x00c7;
    L_0x00d8:
        r10.close();
        goto L_0x00c7;
    L_0x00dc:
        r3 = move-exception;
        if (r10 == 0) goto L_0x00e2;
    L_0x00df:
        r10.close();
    L_0x00e2:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.camcard.groups.CamcardGroup.updateIsCamcardToGroupIfNeed(android.content.Context):void");
    }

    public static String replaceTitle(String str, Context context) {
        return str.replace("PREDEFINED_HUAWEI_GROUP_CCARD", context.getString(R.string.contact_list_business_cards));
    }

    public static String replaceTitle(StringBuilder sb, Context context) {
        return replaceTitle(sb.toString(), context);
    }

    public static ContentProviderOperation addDiff(Context context, RawContactDeltaList state) {
        if (context == null || state == null) {
            return null;
        }
        AccountWithDataSet account;
        RawContactDelta rawContactDelta = (RawContactDelta) state.get(0);
        String name = rawContactDelta.getAccountName();
        String type = rawContactDelta.getAccountType();
        String dataSet = rawContactDelta.getDataSet();
        if (name == null || type == null) {
            account = new AccountWithDataSet("Phone", "com.android.huawei.phone", null);
        } else {
            account = new AccountWithDataSet(name, type, dataSet);
        }
        long groupId = getCCGroupId(context, account);
        Builder lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
        lBuilder.withValueBackReference("raw_contact_id", 0);
        lBuilder.withValue("mimetype", "vnd.android.cursor.item/group_membership");
        lBuilder.withValue("data1", Long.valueOf(groupId));
        return lBuilder.build();
    }

    private static final long getCCGroupId(Context context, Account account) {
        long groupId = queryPrefinedCCGroup(context, account);
        if (groupId <= 0) {
            return insertPrefinedCCGroup(context, account);
        }
        return groupId;
    }

    private static long queryPrefinedCCGroup(Context context, Account account) {
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        long groupId = -1;
        String selection = "title=? AND account_name=? AND account_type=? AND deleted=0";
        String[] selectionArgs = new String[]{"PREDEFINED_HUAWEI_GROUP_CCARD", account.name, account.type};
        try {
            cursor = cr.query(Groups.CONTENT_URI, new String[]{"_id"}, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                groupId = cursor.getLong(0);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(TAG, "queryPrefinedCCGroup exception " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return groupId;
    }

    public static String queryPrefinedCCGroups(Context context) {
        StringBuilder groupIds = new StringBuilder();
        ContentResolver cr = context.getContentResolver();
        Cursor cursor = null;
        String selection = "title=? AND deleted=0";
        String[] selectionArgs = new String[]{"PREDEFINED_HUAWEI_GROUP_CCARD"};
        try {
            cursor = cr.query(Groups.CONTENT_URI, new String[]{"_id"}, selection, selectionArgs, null);
            while (cursor != null && cursor.moveToNext()) {
                groupIds.append(",").append(cursor.getLong(0));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(TAG, "queryPrefinedCCGroups exception " + e.getMessage());
            if (groupIds.length() <= 1) {
                return null;
            }
            return groupIds.substring(1);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (groupIds.length() <= 1) {
            return groupIds.substring(1);
        }
        return null;
    }

    private static long insertPrefinedCCGroup(Context context, Account account) {
        ContentValues cv = new ContentValues();
        cv.put("account_name", account.name);
        cv.put("account_type", account.type);
        cv.put("title", "PREDEFINED_HUAWEI_GROUP_CCARD");
        cv.put("sync1", "PREDEFINED_HUAWEI_GROUP_CCARD");
        cv.put("res_package", context.getPackageName());
        long groupId = -1;
        try {
            Uri groupUri = context.getContentResolver().insert(Groups.CONTENT_URI, cv);
            if (groupUri != null) {
                groupId = ContentUris.parseId(groupUri);
            }
        } catch (Exception e) {
            HwLog.e(TAG, "insertPrefinedCCGroup exception " + e.getMessage());
        }
        return groupId;
    }

    public static void updateGroupSync2Title(Context context) {
        if (context != null) {
            Uri groupUir = Groups.CONTENT_URI;
            ContentValues values = new ContentValues();
            values.put("title", "PREDEFINED_HUAWEI_GROUP_CCARD");
            values.putNull("title_res");
            String[] args = new String[]{"PREDEFINED_HUAWEI_GROUP_CCARD", "PREDEFINED_HUAWEI_GROUP_CCARD"};
            int count = context.getContentResolver().update(groupUir, values, "sync1 =? AND title !=?", args);
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "updateGroupSync2Title count : " + count);
            }
        }
    }
}
