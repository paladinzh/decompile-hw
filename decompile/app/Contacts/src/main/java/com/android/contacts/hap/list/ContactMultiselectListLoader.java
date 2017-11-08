package com.android.contacts.hap.list;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import com.android.contacts.list.ContactListLoader;
import com.google.common.collect.Maps;
import java.util.HashMap;

public class ContactMultiselectListLoader extends ContactListLoader {
    private static final String TAG = ContactMultiselectListLoader.class.getSimpleName();
    private Uri mAssociateUri;
    HashMap<Long, Long> mFrequentDatadMap = Maps.newHashMap();

    private void loadFrequentMap() {
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
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r0 = android.provider.ContactsContract.AUTHORITY_URI;
        r2 = "contacts/data_phone_frequent";
        r1 = android.net.Uri.withAppendedPath(r0, r2);
        r6 = 0;
        r0 = r14.mFrequentDatadMap;
        r0.clear();
        r0 = r14.getContext();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r0.getContentResolver();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r3 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r4 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r5 = 0;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
    L_0x001f:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        if (r0 == 0) goto L_0x009c;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
    L_0x0025:
        r0 = "data_id";	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r8 = r6.getLong(r0);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = "times_used";	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r10 = r6.getLong(r0);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r14.mFrequentDatadMap;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r0.containsKey(r2);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        if (r0 == 0) goto L_0x0087;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
    L_0x0047:
        r0 = r14.mFrequentDatadMap;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r0.get(r2);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = (java.lang.Long) r0;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r12 = r0.longValue();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0 = r14.mFrequentDatadMap;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r4 = r12 + r10;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r3 = java.lang.Long.valueOf(r4);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0.put(r2, r3);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        goto L_0x001f;
    L_0x0067:
        r7 = move-exception;
        r0 = TAG;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2.<init>();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r3 = "loadfrequentMap query exception : ";	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = r2.append(r7);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        com.android.contacts.util.HwLog.e(r0, r2);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        if (r6 == 0) goto L_0x0086;
    L_0x0083:
        r6.close();
    L_0x0086:
        return;
    L_0x0087:
        r0 = r14.mFrequentDatadMap;	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r2 = java.lang.Long.valueOf(r8);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r3 = java.lang.Long.valueOf(r10);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        r0.put(r2, r3);	 Catch:{ Exception -> 0x0067, all -> 0x0095 }
        goto L_0x001f;
    L_0x0095:
        r0 = move-exception;
        if (r6 == 0) goto L_0x009b;
    L_0x0098:
        r6.close();
    L_0x009b:
        throw r0;
    L_0x009c:
        if (r6 == 0) goto L_0x0086;
    L_0x009e:
        r6.close();
        goto L_0x0086;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.list.ContactMultiselectListLoader.loadFrequentMap():void");
    }

    public ContactMultiselectListLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public HashMap<Long, Long> getFrequentDataMap() {
        return this.mFrequentDatadMap;
    }

    public Cursor loadInBackground() {
        loadFrequentMap();
        if (getUri() == null) {
            return null;
        }
        final Cursor data = super.loadInBackground();
        if (data == null) {
            return null;
        }
        if (this.mAssociateUri != null) {
            if (createAssociateLoader() == null) {
                data.close();
                return null;
            }
            Cursor data2 = data;
            data = new MergeCursor(new Cursor[]{data, createAssociateLoader()}) {
                public Bundle getExtras() {
                    if (data != null) {
                        return data.getExtras();
                    }
                    return null;
                }
            };
        }
        return data;
    }

    private Cursor createAssociateLoader() {
        return getContext().getContentResolver().query(this.mAssociateUri, getProjection(), getSelection(), getSelectionArgs(), getSortOrder());
    }
}
