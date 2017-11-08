package com.android.contacts.hap.sim;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.sqlite.SQLiteDiskIOException;
import android.os.RemoteException;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import java.util.ArrayList;

public class SimDatabaseHelper {
    public static int deleteSimContactsFromSimCard(android.content.Context r22, java.util.ArrayList<java.lang.Long> r23, java.lang.String r24) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0041 in list []
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
        r14 = 0;
        if (r23 == 0) goto L_0x0009;
    L_0x0003:
        r2 = r23.isEmpty();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x000b;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x0009:
        r2 = 0;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        return r2;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x000b:
        r19 = new java.lang.StringBuilder;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r19.<init>();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = "_id IN (";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r18 = r23.iterator();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x001c:
        r2 = r18.hasNext();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x0043;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x0022:
        r17 = r18.next();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r17 = (java.lang.Long) r17;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r1 = r17;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r1);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = ",";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        goto L_0x001c;
    L_0x0038:
        r11 = move-exception;
        r11.printStackTrace();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r14 == 0) goto L_0x0041;
    L_0x003e:
        r14.close();
    L_0x0041:
        r2 = 0;
        return r2;
    L_0x0043:
        r2 = r19.length();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = r2 + -1;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.setLength(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = ")";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = r22.getContentResolver();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r3 = android.provider.ContactsContract.RawContactsEntity.CONTENT_URI;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r5 = r19.toString();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r4 = 0;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r6 = 0;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r7 = 0;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r14 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r12 = android.provider.ContactsContract.RawContacts.newEntityIterator(r14);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r16 = com.android.contacts.hap.sim.SimFactoryManager.getSimFactory(r24);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r16 == 0) goto L_0x0132;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x0071:
        r15 = r16.getSimPersistanceManager();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r8 = new java.util.ArrayList;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r8.<init>();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x007a:
        r2 = r12.hasNext();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x0091;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x0080:
        r2 = r12.next();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = (android.content.Entity) r2;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r15.getSimContacts(r2, r8);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        goto L_0x007a;
    L_0x008a:
        r2 = move-exception;
        if (r14 == 0) goto L_0x0090;
    L_0x008d:
        r14.close();
    L_0x0090:
        throw r2;
    L_0x0091:
        r10 = 0;
        r21 = r8.iterator();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x0096:
        r2 = r21.hasNext();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x00be;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x009c:
        r20 = r21.next();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r20 = (com.android.contacts.hap.sim.SimContact) r20;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r20;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r9 = r15.delete(r0);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r9 <= 0) goto L_0x00ac;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00aa:
        r10 = r10 + r9;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        goto L_0x0096;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00ac:
        r0 = r20;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = r0.id;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = java.lang.Long.parseLong(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r13 = java.lang.Long.valueOf(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r23;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.remove(r13);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        goto L_0x0096;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00be:
        r2 = r23.isEmpty();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x00cb;
    L_0x00c4:
        r2 = 0;
        if (r14 == 0) goto L_0x00ca;
    L_0x00c7:
        r14.close();
    L_0x00ca:
        return r2;
    L_0x00cb:
        r2 = 0;
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.setLength(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = "_id IN (";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r18 = r23.iterator();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00dd:
        r2 = r18.hasNext();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r2 == 0) goto L_0x00f9;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00e3:
        r17 = r18.next();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r17 = (java.lang.Long) r17;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r1 = r17;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r1);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = ",";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        goto L_0x00dd;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
    L_0x00f9:
        r2 = r19.length();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = r2 + -1;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.setLength(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = ")";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0 = r19;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r0.append(r2);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2 = r22.getContentResolver();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r3 = android.provider.ContactsContract.RawContacts.CONTENT_URI;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r3 = r3.buildUpon();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r4 = "caller_is_syncadapter";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r5 = "true";	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r3 = r3.appendQueryParameter(r4, r5);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r3 = r3.build();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r4 = r19.toString();	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r5 = 0;	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        r2.delete(r3, r4, r5);	 Catch:{ NumberFormatException -> 0x0038, all -> 0x008a }
        if (r14 == 0) goto L_0x0131;
    L_0x012e:
        r14.close();
    L_0x0131:
        return r10;
    L_0x0132:
        r2 = 0;
        if (r14 == 0) goto L_0x0138;
    L_0x0135:
        r14.close();
    L_0x0138:
        return r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.contacts.hap.sim.SimDatabaseHelper.deleteSimContactsFromSimCard(android.content.Context, java.util.ArrayList, java.lang.String):int");
    }

    public static void markSimContactsAsDeleted(Context aContext, String aSimAccountType) {
        ArrayList<ContentProviderOperation> operationList = new ArrayList();
        String[] selectionArgs = new String[]{aSimAccountType};
        ContentValues values = new ContentValues();
        Builder lBuilder = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI);
        lBuilder.withSelection("account_type=? AND deleted=0", selectionArgs);
        operationList.add(lBuilder.build());
        values.clear();
        values.put("deleted", Integer.valueOf(1));
        lBuilder = ContentProviderOperation.newUpdate(Groups.CONTENT_URI);
        lBuilder.withSelection("account_type=?", selectionArgs);
        lBuilder.withValues(values);
        operationList.add(lBuilder.build());
        try {
            aContext.getContentResolver().applyBatch("com.android.contacts", operationList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } catch (SQLiteDiskIOException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        }
    }

    public static void markSimContactsAsNonDeleted(Context aContext, String aSimAccountType) {
        ArrayList<ContentProviderOperation> operationList = new ArrayList();
        String selection = "account_type=?";
        String[] selectionArgs = new String[]{aSimAccountType};
        ContentValues values = new ContentValues();
        values.put("deleted", Integer.valueOf(0));
        Builder lBuilder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        lBuilder.withSelection(selection, selectionArgs);
        lBuilder.withValues(values);
        operationList.add(lBuilder.build());
        lBuilder = ContentProviderOperation.newUpdate(RawContacts.CONTENT_URI);
        lBuilder.withSelection(selection, selectionArgs);
        values.clear();
        values.put("aggregation_mode", Integer.valueOf(3));
        lBuilder.withValues(values);
        operationList.add(lBuilder.build());
        values.clear();
        values.put("deleted", Integer.valueOf(0));
        lBuilder = ContentProviderOperation.newUpdate(Groups.CONTENT_URI);
        lBuilder.withSelection("account_type=?", selectionArgs);
        lBuilder.withValues(values);
        operationList.add(lBuilder.build());
        try {
            aContext.getContentResolver().applyBatch("com.android.contacts", operationList);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } catch (SQLiteDiskIOException e3) {
            e3.printStackTrace();
        } catch (IllegalArgumentException e4) {
            e4.printStackTrace();
        }
    }

    public static void markSingleContactAsNonDelete(Context aContext, String aRawContactId) {
        String[] selectionArgs = new String[]{aRawContactId};
        ContentValues values = new ContentValues();
        values.put("deleted", Integer.valueOf(0));
        aContext.getContentResolver().update(RawContacts.CONTENT_URI, values, "_id=?", selectionArgs);
    }
}
