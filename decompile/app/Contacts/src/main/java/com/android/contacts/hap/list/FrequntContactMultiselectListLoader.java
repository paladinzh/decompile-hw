package com.android.contacts.hap.list;

import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.list.RcsFrequentContactMultiselectListLoader;
import com.android.contacts.util.HwLog;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class FrequntContactMultiselectListLoader extends CursorLoader {
    private static final Uri DATA_FREQ_URI = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "data/frequent");
    static final String[] _PROJECTION = new String[]{"_id", "number", "date", "duration", "type", "countryiso", "voicemail_uri", "geocoded_location", "name", "numbertype", "numberlabel", "lookup_uri"};
    private HwCustFrequntContactMultiselectListLoader mCust = null;
    private DataListFilter mDataFilter;
    private RcsFrequentContactMultiselectListLoader mRcsCust = null;

    public FrequntContactMultiselectListLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context.getApplicationContext(), uri, projection, selection, selectionArgs, sortOrder);
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustFrequntContactMultiselectListLoader) HwCustUtils.createObj(HwCustFrequntContactMultiselectListLoader.class, new Object[]{getContext()});
        }
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mRcsCust = new RcsFrequentContactMultiselectListLoader();
            if (this.mRcsCust != null) {
                this.mRcsCust.initService(getContext());
                this.mRcsCust.setParentContext(context);
            }
        }
    }

    public Cursor loadInBackground() {
        ContentResolver contentResover = getContext().getContentResolver();
        String[] projection = getLoaderProjection();
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        setSelectionAndArgs(selection, selectionArgs);
        Cursor c = null;
        if (hasMessageRecentlyPhoneType()) {
            Cursor cursor = addMessageRecentlyPhone(projection, selection, selectionArgs);
            if (cursor.getCount() > 0) {
                return cursor;
            }
            cursor.close();
            return c;
        }
        try {
            return contentResover.query(DATA_FREQ_URI, projection, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), "times_used DESC,sort_key");
        } catch (IllegalArgumentException e) {
            HwLog.e("FrequntContactMultiselectListLoader", "uri exception: " + e.toString());
            return c;
        }
    }

    private String getLookupKey(String lookUpUri) {
        if (lookUpUri == null) {
            HwLog.w("FrequntContactMultiselectListLoader", "lookUpUri is null");
            return null;
        }
        List<String> pathSegmentList = Uri.parse(lookUpUri).getPathSegments();
        String str = null;
        if (pathSegmentList.size() == 4) {
            try {
                str = (String) pathSegmentList.get(2);
            } catch (NumberFormatException e) {
                str = null;
            }
        }
        return str;
    }

    void queryDataTableNumber(ContentResolver contentResover, Uri uri, String[] projection, StringBuilder selection, List<String> selectionArgs, String sortBy, HashMap<String, ArrayList<Object[]>> hm) {
        IllegalArgumentException e;
        Object[] row = new Object[projection.length];
        Cursor cursor = null;
        try {
            String[] strArr;
            ArrayList<Object> contactsArrayList;
            String stringBuilder = selection.toString();
            if (selectionArgs == null) {
                strArr = null;
            } else {
                strArr = (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
            }
            cursor = contentResover.query(uri, projection, stringBuilder, strArr, sortBy);
            ArrayList<Object> contactsArrayList2 = null;
            while (cursor != null) {
                try {
                    if (!cursor.moveToNext()) {
                        break;
                    }
                    Object lookupKey = null;
                    contactsArrayList = new ArrayList();
                    for (int i = 0; i < row.length; i++) {
                        if (cursor.getType(i) == 4) {
                            contactsArrayList.add(cursor.getBlob(i));
                        } else {
                            String value = cursor.getString(i);
                            contactsArrayList.add(value);
                            if (i == 7) {
                                String lookupKey2 = value;
                            }
                        }
                    }
                    if (!(contactsArrayList.isEmpty() || lookupKey == null)) {
                        ArrayList<Object[]> contactsRowArrayList = (ArrayList) hm.get(lookupKey);
                        if (contactsRowArrayList == null) {
                            contactsRowArrayList = new ArrayList();
                        }
                        contactsRowArrayList.add(contactsArrayList.toArray(new Object[contactsArrayList.size()]));
                        hm.put(lookupKey, contactsRowArrayList);
                    }
                    contactsArrayList2 = contactsArrayList;
                } catch (IllegalArgumentException e2) {
                    e = e2;
                    contactsArrayList = contactsArrayList2;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    contactsArrayList = contactsArrayList2;
                }
            }
            if (cursor != null) {
                cursor.close();
            }
            contactsArrayList = contactsArrayList2;
            return;
        } catch (IllegalArgumentException e3) {
            e = e3;
        }
        try {
            HwLog.e("FrequntContactMultiselectListLoader", "uri exception: " + e.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th3) {
            th2 = th3;
            if (cursor != null) {
                cursor.close();
            }
            throw th2;
        }
    }

    boolean hasMessageRecentlyPhoneType() {
        if (this.mDataFilter.filterType == -3 || this.mDataFilter.filterType == -5 || this.mDataFilter.filterType == -50) {
            return true;
        }
        return false;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean hasContactsNumber(String lookupUri) {
        if (lookupUri == null || lookupUri.length() == 0 || !lookupUri.startsWith("content://com.android.contacts/contacts/lookup/")) {
            return false;
        }
        boolean result;
        if (Uri.parse(lookupUri).getPathSegments().size() == 4) {
            result = true;
        } else {
            result = false;
            if (HwLog.HWDBG) {
                HwLog.d("FrequntContactMultiselectListLoader", "segment count is not 4");
            }
        }
        return result;
    }

    android.database.Cursor addMessageRecentlyPhone(java.lang.String[] r42, java.lang.StringBuilder r43, java.util.List<java.lang.String> r44) {
        /* JADX: method processing error */
/*
Error: java.util.ConcurrentModificationException
	at java.util.ArrayList$Itr.checkForComodification(ArrayList.java:901)
	at java.util.ArrayList$Itr.next(ArrayList.java:851)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:114)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
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
        r41 = this;
        r33 = new com.android.contacts.util.ListCursor;
        r0 = r33;
        r1 = r42;
        r0.<init>(r1);
        r5 = r41.getContext();
        r3 = r5.getContentResolver();
        r6 = "_id IN ( SELECT _id FROM calls WHERE deleted = 0 GROUP BY number )";
        r5 = com.android.contacts.compatibility.QueryUtil.getCallsContentUri();
        r5 = r5.buildUpon();
        r7 = "limit";
        r8 = 50;
        r8 = java.lang.Integer.toString(r8);
        r5 = r5.appendQueryParameter(r7, r8);
        r7 = "is_from_frequent";
        r8 = 1;
        r8 = java.lang.Boolean.toString(r8);
        r5 = r5.appendQueryParameter(r7, r8);
        r7 = "call_log_merge";
        r8 = 1;
        r8 = java.lang.Boolean.toString(r8);
        r5 = r5.appendQueryParameter(r7, r8);
        r4 = r5.build();
        r5 = _PROJECTION;
        r8 = "date DESC";
        r7 = 0;
        r25 = r3.query(r4, r5, r6, r7, r8);
        r27 = 0;
        if (r25 == 0) goto L_0x0298;
    L_0x0053:
        r24 = 0;
        r38 = new java.util.HashSet;
        r38.<init>();
        r14 = new java.util.HashMap;
        r14.<init>();
        r32 = 0;
        r31 = 0;
        r34 = 0;
        r23 = 0;
    L_0x0067:
        r5 = r25.moveToNext();	 Catch:{ all -> 0x00ec }
        if (r5 == 0) goto L_0x01b0;	 Catch:{ all -> 0x00ec }
    L_0x006d:
        r5 = 11;	 Catch:{ all -> 0x00ec }
        r0 = r25;	 Catch:{ all -> 0x00ec }
        r32 = r0.getString(r5);	 Catch:{ all -> 0x00ec }
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r1 = r32;	 Catch:{ all -> 0x00ec }
        r5 = r0.hasContactsNumber(r1);	 Catch:{ all -> 0x00ec }
        if (r5 == 0) goto L_0x0192;	 Catch:{ all -> 0x00ec }
    L_0x007f:
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r1 = r32;	 Catch:{ all -> 0x00ec }
        r31 = r0.getLookupKey(r1);	 Catch:{ all -> 0x00ec }
        if (r27 != 0) goto L_0x009e;	 Catch:{ all -> 0x00ec }
    L_0x0089:
        r9 = r41.getLoaderUri();	 Catch:{ all -> 0x00ec }
        r13 = "last_time_used DESC, times_used DESC";	 Catch:{ all -> 0x00ec }
        r7 = r41;	 Catch:{ all -> 0x00ec }
        r8 = r3;	 Catch:{ all -> 0x00ec }
        r10 = r42;	 Catch:{ all -> 0x00ec }
        r11 = r43;	 Catch:{ all -> 0x00ec }
        r12 = r44;	 Catch:{ all -> 0x00ec }
        r7.queryDataTableNumber(r8, r9, r10, r11, r12, r13, r14);	 Catch:{ all -> 0x00ec }
        r27 = 1;	 Catch:{ all -> 0x00ec }
    L_0x009e:
        r0 = r31;	 Catch:{ all -> 0x00ec }
        r5 = r14.containsKey(r0);	 Catch:{ all -> 0x00ec }
        if (r5 == 0) goto L_0x00f4;	 Catch:{ all -> 0x00ec }
    L_0x00a6:
        r0 = r31;	 Catch:{ all -> 0x00ec }
        r5 = r14.get(r0);	 Catch:{ all -> 0x00ec }
        r0 = r5;	 Catch:{ all -> 0x00ec }
        r0 = (java.util.ArrayList) r0;	 Catch:{ all -> 0x00ec }
        r24 = r0;	 Catch:{ all -> 0x00ec }
        r5 = 1;	 Catch:{ all -> 0x00ec }
        r0 = r25;	 Catch:{ all -> 0x00ec }
        r34 = r0.getString(r5);	 Catch:{ all -> 0x00ec }
        r5 = r24.size();	 Catch:{ all -> 0x00ec }
        r28 = r5 + -1;	 Catch:{ all -> 0x00ec }
    L_0x00be:
        if (r28 < 0) goto L_0x0067;	 Catch:{ all -> 0x00ec }
    L_0x00c0:
        r0 = r24;	 Catch:{ all -> 0x00ec }
        r1 = r28;	 Catch:{ all -> 0x00ec }
        r5 = r0.get(r1);	 Catch:{ all -> 0x00ec }
        r0 = r5;	 Catch:{ all -> 0x00ec }
        r0 = (java.lang.Object[]) r0;	 Catch:{ all -> 0x00ec }
        r23 = r0;	 Catch:{ all -> 0x00ec }
        r5 = 5;	 Catch:{ all -> 0x00ec }
        r0 = r25;	 Catch:{ all -> 0x00ec }
        r7 = r0.getString(r5);	 Catch:{ all -> 0x00ec }
        r5 = 8;	 Catch:{ all -> 0x00ec }
        r5 = r23[r5];	 Catch:{ all -> 0x00ec }
        r5 = (java.lang.String) r5;	 Catch:{ all -> 0x00ec }
        r8 = 0;	 Catch:{ all -> 0x00ec }
        r0 = r34;	 Catch:{ all -> 0x00ec }
        r5 = com.android.contacts.hap.CommonUtilMethods.compareNumsHw(r0, r7, r5, r8);	 Catch:{ all -> 0x00ec }
        if (r5 == 0) goto L_0x00f1;	 Catch:{ all -> 0x00ec }
    L_0x00e3:
        r0 = r33;	 Catch:{ all -> 0x00ec }
        r1 = r23;	 Catch:{ all -> 0x00ec }
        r0.addRow(r1);	 Catch:{ all -> 0x00ec }
        goto L_0x0067;
    L_0x00ec:
        r5 = move-exception;
        r25.close();
        throw r5;
    L_0x00f1:
        r28 = r28 + -1;
        goto L_0x00be;
    L_0x00f4:
        r9 = r41.getLoaderProjection();	 Catch:{ all -> 0x00ec }
        r39 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ec }
        r39.<init>();	 Catch:{ all -> 0x00ec }
        r5 = "contact_id";	 Catch:{ all -> 0x00ec }
        r0 = r39;	 Catch:{ all -> 0x00ec }
        r5 = r0.append(r5);	 Catch:{ all -> 0x00ec }
        r7 = " in (select _id from contacts where ";	 Catch:{ all -> 0x00ec }
        r5 = r5.append(r7);	 Catch:{ all -> 0x00ec }
        r7 = "lookup";	 Catch:{ all -> 0x00ec }
        r5 = r5.append(r7);	 Catch:{ all -> 0x00ec }
        r7 = " = ?) AND ";	 Catch:{ all -> 0x00ec }
        r5 = r5.append(r7);	 Catch:{ all -> 0x00ec }
        r7 = "mimetype";	 Catch:{ all -> 0x00ec }
        r5 = r5.append(r7);	 Catch:{ all -> 0x00ec }
        r7 = " = ? ";	 Catch:{ all -> 0x00ec }
        r5.append(r7);	 Catch:{ all -> 0x00ec }
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r5 = r0.mDataFilter;	 Catch:{ all -> 0x00ec }
        r5 = r5.filterType;	 Catch:{ all -> 0x00ec }
        r7 = -50;	 Catch:{ all -> 0x00ec }
        if (r5 != r7) goto L_0x0141;	 Catch:{ all -> 0x00ec }
    L_0x0132:
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r5 = r0.mRcsCust;	 Catch:{ all -> 0x00ec }
        if (r5 == 0) goto L_0x0141;	 Catch:{ all -> 0x00ec }
    L_0x0138:
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r5 = r0.mRcsCust;	 Catch:{ all -> 0x00ec }
        r0 = r39;	 Catch:{ all -> 0x00ec }
        r5.setSelectionToGetOnlyRCSContactsForMessaging(r0);	 Catch:{ all -> 0x00ec }
    L_0x0141:
        r8 = android.provider.ContactsContract.Data.CONTENT_URI;	 Catch:{ all -> 0x00ec }
        r10 = r39.toString();	 Catch:{ all -> 0x00ec }
        r5 = 2;	 Catch:{ all -> 0x00ec }
        r11 = new java.lang.String[r5];	 Catch:{ all -> 0x00ec }
        r5 = 0;	 Catch:{ all -> 0x00ec }
        r11[r5] = r31;	 Catch:{ all -> 0x00ec }
        r5 = "vnd.android.cursor.item/phone_v2";	 Catch:{ all -> 0x00ec }
        r7 = 1;	 Catch:{ all -> 0x00ec }
        r11[r7] = r5;	 Catch:{ all -> 0x00ec }
        r12 = 0;	 Catch:{ all -> 0x00ec }
        r7 = r3;	 Catch:{ all -> 0x00ec }
        r35 = r7.query(r8, r9, r10, r11, r12);	 Catch:{ all -> 0x00ec }
        r26 = 0;
        if (r35 == 0) goto L_0x0184;
    L_0x015d:
        r5 = r35.moveToNext();	 Catch:{ all -> 0x018b }
        if (r5 == 0) goto L_0x0184;	 Catch:{ all -> 0x018b }
    L_0x0163:
        r5 = r9.length;	 Catch:{ all -> 0x018b }
        r0 = r41;	 Catch:{ all -> 0x018b }
        r1 = r35;	 Catch:{ all -> 0x018b }
        r26 = r0.createNewRow(r1, r5);	 Catch:{ all -> 0x018b }
        r5 = 5;	 Catch:{ all -> 0x018b }
        r0 = r25;	 Catch:{ all -> 0x018b }
        r5 = r0.getString(r5);	 Catch:{ all -> 0x018b }
        r7 = 3;	 Catch:{ all -> 0x018b }
        r26[r7] = r5;	 Catch:{ all -> 0x018b }
        r0 = r33;	 Catch:{ all -> 0x018b }
        r1 = r26;	 Catch:{ all -> 0x018b }
        r0.addRow(r1);	 Catch:{ all -> 0x018b }
        r0 = r38;	 Catch:{ all -> 0x018b }
        r1 = r26;	 Catch:{ all -> 0x018b }
        r0.add(r1);	 Catch:{ all -> 0x018b }
    L_0x0184:
        if (r35 == 0) goto L_0x0067;
    L_0x0186:
        r35.close();	 Catch:{ all -> 0x00ec }
        goto L_0x0067;	 Catch:{ all -> 0x00ec }
    L_0x018b:
        r5 = move-exception;	 Catch:{ all -> 0x00ec }
        if (r35 == 0) goto L_0x0191;	 Catch:{ all -> 0x00ec }
    L_0x018e:
        r35.close();	 Catch:{ all -> 0x00ec }
    L_0x0191:
        throw r5;	 Catch:{ all -> 0x00ec }
    L_0x0192:
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r5 = r0.mDataFilter;	 Catch:{ all -> 0x00ec }
        r5 = r5.filterType;	 Catch:{ all -> 0x00ec }
        r7 = -50;	 Catch:{ all -> 0x00ec }
        if (r5 == r7) goto L_0x0067;	 Catch:{ all -> 0x00ec }
    L_0x019c:
        r0 = r42;	 Catch:{ all -> 0x00ec }
        r5 = r0.length;	 Catch:{ all -> 0x00ec }
        r0 = r41;	 Catch:{ all -> 0x00ec }
        r1 = r25;	 Catch:{ all -> 0x00ec }
        r26 = r0.newRow(r1, r5);	 Catch:{ all -> 0x00ec }
        r0 = r33;	 Catch:{ all -> 0x00ec }
        r1 = r26;	 Catch:{ all -> 0x00ec }
        r0.addRow(r1);	 Catch:{ all -> 0x00ec }
        goto L_0x0067;
    L_0x01b0:
        r25.close();
        r14.clear();
        r5 = r38.size();
        if (r5 <= 0) goto L_0x0298;
    L_0x01bc:
        r5 = new java.lang.StringBuilder;
        r7 = "_id";
        r5.<init>(r7);
        r7 = " in(select data_id from phone_lookup where min_match in(";
        r19 = r5.append(r7);
        r29 = 1;
        r37 = r38.iterator();
    L_0x01d1:
        r5 = r37.hasNext();
        if (r5 == 0) goto L_0x021a;
    L_0x01d7:
        r36 = r37.next();
        r36 = (java.lang.Object[]) r36;
        if (r29 != 0) goto L_0x01e7;
    L_0x01df:
        r5 = ",";
        r0 = r19;
        r0.append(r5);
    L_0x01e7:
        r29 = 0;
        r5 = 8;
        r40 = r36[r5];
        r40 = (java.lang.String) r40;
        if (r40 == 0) goto L_0x0204;
    L_0x01f1:
        r5 = r40.length();
        r7 = 7;
        if (r5 <= r7) goto L_0x0204;
    L_0x01f8:
        r5 = r40.length();
        r5 = r5 + -7;
        r0 = r40;
        r40 = r0.substring(r5);
    L_0x0204:
        r5 = "'";
        r0 = r19;
        r5 = r0.append(r5);
        r0 = r40;
        r5 = r5.append(r0);
        r7 = "'";
        r5.append(r7);
        goto L_0x01d1;
    L_0x021a:
        r5 = "))";
        r0 = r19;
        r0.append(r5);
        r17 = android.provider.ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        r20 = 0;
        r21 = 0;
        r15 = r41;
        r16 = r3;
        r18 = r42;
        r22 = r14;
        r15.queryDataTableNumber(r16, r17, r18, r19, r20, r21, r22);
        r37 = r38.iterator();
    L_0x0237:
        r5 = r37.hasNext();
        if (r5 == 0) goto L_0x0298;
    L_0x023d:
        r36 = r37.next();
        r36 = (java.lang.Object[]) r36;
        r30 = 0;
        r5 = 7;
        r5 = r36[r5];
        r5 = r14.containsKey(r5);
        if (r5 == 0) goto L_0x028e;
    L_0x024e:
        r5 = 7;
        r5 = r36[r5];
        r24 = r14.get(r5);
        r24 = (java.util.ArrayList) r24;
        r5 = 8;
        r34 = r36[r5];
        r34 = (java.lang.String) r34;
        r5 = r24.size();
        r28 = r5 + -1;
    L_0x0263:
        if (r28 < 0) goto L_0x028e;
    L_0x0265:
        r0 = r24;
        r1 = r28;
        r23 = r0.get(r1);
        r23 = (java.lang.Object[]) r23;
        r5 = 3;
        r5 = r36[r5];
        r5 = (java.lang.String) r5;
        r7 = 8;
        r7 = r23[r7];
        r7 = (java.lang.String) r7;
        r8 = 0;
        r0 = r34;
        r5 = com.android.contacts.hap.CommonUtilMethods.compareNumsHw(r0, r5, r7, r8);
        if (r5 == 0) goto L_0x0295;
    L_0x0283:
        r0 = r41;
        r1 = r23;
        r2 = r36;
        r0.copy(r1, r2);
        r30 = 1;
    L_0x028e:
        if (r30 != 0) goto L_0x0237;
    L_0x0290:
        r5 = 0;
        r7 = 3;
        r36[r7] = r5;
        goto L_0x0237;
    L_0x0295:
        r28 = r28 + -1;
        goto L_0x0263;
    L_0x0298:
        return r33;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.list.FrequntContactMultiselectListLoader.addMessageRecentlyPhone(java.lang.String[], java.lang.StringBuilder, java.util.List):android.database.Cursor");
    }

    private Object[] newRow(Cursor cursor, int columnLen) {
        Object[] callsrow = new Object[columnLen];
        callsrow[0] = null;
        callsrow[1] = cursor.getString(0);
        callsrow[2] = cursor.getString(8);
        callsrow[3] = null;
        callsrow[4] = null;
        callsrow[5] = null;
        callsrow[6] = null;
        callsrow[7] = null;
        callsrow[8] = cursor.getString(1);
        callsrow[9] = cursor.getString(9);
        callsrow[10] = cursor.getString(10);
        callsrow[11] = "vnd.android.cursor.item/phone_v2";
        callsrow[12] = null;
        callsrow[13] = cursor.getString(7);
        return callsrow;
    }

    private Object[] createNewRow(Cursor cursor, int columnLen) {
        Object[] callsrow = new Object[columnLen];
        callsrow[0] = Integer.valueOf(cursor.getInt(0));
        callsrow[1] = cursor.getString(1);
        callsrow[2] = cursor.getString(2);
        callsrow[3] = cursor.getString(3);
        callsrow[4] = cursor.getString(4);
        callsrow[5] = Integer.valueOf(cursor.getInt(5));
        callsrow[6] = cursor.getString(6);
        callsrow[7] = cursor.getString(7);
        callsrow[8] = cursor.getString(8);
        callsrow[9] = cursor.getString(9);
        callsrow[10] = cursor.getString(10);
        callsrow[11] = cursor.getString(11);
        callsrow[12] = Integer.valueOf(cursor.getInt(12));
        callsrow[13] = cursor.getString(13);
        return callsrow;
    }

    private void copy(Object[] source, Object[] target) {
        if (source == null || target == null) {
            HwLog.w("FrequntContactMultiselectListLoader", "illegal arguments!");
            return;
        }
        int length = source.length;
        if (length != target.length) {
            HwLog.w("FrequntContactMultiselectListLoader", "source array length is not equal to target!");
            return;
        }
        for (int i = 0; i < length; i++) {
            target[i] = source[i];
        }
    }

    public void setDataFilter(DataListFilter filter) {
        this.mDataFilter = filter;
    }

    private Uri getLoaderUri() {
        return DATA_FREQ_URI.buildUpon().appendQueryParameter("limit", "200").build();
    }

    private String[] getLoaderProjection() {
        return FrequentContactSelectAdapter.PROJECTION_DATA;
    }

    private void setSelectionAndArgs(StringBuilder selectionBuilder, List<String> selectionArgs) {
        if (this.mDataFilter != null && selectionBuilder != null && selectionArgs != null) {
            if (selectionBuilder.length() > 0) {
                selectionBuilder.append(" AND ");
            }
            switch (this.mDataFilter.filterType) {
                case -11:
                    setSelectionAndSelectionArgsForRemoveGroupMembers(selectionBuilder, selectionArgs);
                    break;
                case -9:
                case -8:
                case -7:
                case -6:
                    setSelectionAndSelectionArgsForMessagePlusOrRcse(selectionBuilder, selectionArgs);
                    break;
                case -5:
                case -4:
                case -3:
                    setSelectionAndSelectionArgsForMessaging(selectionBuilder, selectionArgs);
                    break;
                case -2:
                case -1:
                    setSelectionAndSelectionArgsForGroupMessage(selectionBuilder, selectionArgs);
                    break;
                default:
                    if (this.mRcsCust != null) {
                        this.mRcsCust.setSelectionAndSelectionArgsForCustomizations(this.mDataFilter.filterType, selectionBuilder, selectionArgs);
                        break;
                    }
                    break;
            }
        }
    }

    private void setSelectionAndSelectionArgsForRemoveGroupMembers(StringBuilder selectionBuilder, List<String> selectionArgs) {
        selectionBuilder.append("mimetype").append(" = ? AND ").append("data1").append(" = ? AND ").append("raw_contact_id").append(" in (select ").append("_id").append(" from raw_contacts where ").append("deleted").append(" =0) ");
        selectionArgs.add("vnd.android.cursor.item/group_membership");
        selectionArgs.add(String.valueOf(this.mDataFilter.groupId));
    }

    private void setSelectionAndSelectionArgsForGroupMessage(StringBuilder selectionBuilder, List<String> selectionArgs) {
        if (this.mDataFilter.filterType == -2) {
            selectionBuilder.append("mimetype IN (?)");
            selectionArgs.add("vnd.android.cursor.item/email_v2");
        } else {
            selectionBuilder.append("mimetype IN (?)");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
        selectionBuilder.append(" AND contact_id IN (");
        selectionBuilder.append("SELECT contact_id FROM view_data WHERE mimetype=? AND data1 = ?");
        selectionBuilder.append(")");
        selectionArgs.add("vnd.android.cursor.item/group_membership");
        selectionArgs.add(String.valueOf(this.mDataFilter.groupId));
    }

    private void setSelectionAndSelectionArgsForMessaging(StringBuilder selectionBuilder, List<String> selectionArgs) {
        if (this.mDataFilter.filterType == -4) {
            selectionBuilder.append("mimetype IN (?)");
            selectionArgs.add("vnd.android.cursor.item/email_v2");
        } else if (this.mDataFilter.filterType == -3) {
            selectionBuilder.append("mimetype IN (?)");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
        } else if (this.mCust == null || !this.mCust.getEnableEmailContactInMms()) {
            selectionBuilder.append("mimetype IN (?,?)");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
        } else {
            this.mCust.setSelectionQueryArgs(selectionBuilder, selectionArgs);
        }
    }

    private void setSelectionAndSelectionArgsForMessagePlusOrRcse(StringBuilder selectionBuilder, List<String> selectionArgs) {
        if (this.mDataFilter.filterType == -6) {
            selectionBuilder.append("mimetype=?");
            selectionArgs.add("vnd.android.cursor.item/himessage");
        } else if (this.mDataFilter.filterType == -7) {
            selectionBuilder.append("mimetype=?");
            selectionArgs.add("vnd.android.cursor.item/rcs");
        } else if (this.mDataFilter.filterType == -8) {
            selectionBuilder.append("mimetype IN (?,?,?,?)");
            selectionArgs.add("vnd.android.cursor.item/rcs");
            selectionArgs.add("vnd.android.cursor.item/himessage");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
            selectionArgs.add("vnd.android.cursor.item/email_v2");
        } else if (this.mDataFilter.filterType == -9) {
            selectionBuilder.append("mimetype IN (?,?,?)");
            selectionArgs.add("vnd.android.cursor.item/rcs");
            selectionArgs.add("vnd.android.cursor.item/himessage");
            selectionArgs.add("vnd.android.cursor.item/phone_v2");
        }
    }
}
