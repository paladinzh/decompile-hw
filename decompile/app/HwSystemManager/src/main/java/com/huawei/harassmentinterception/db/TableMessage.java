package com.huawei.harassmentinterception.db;

import android.database.sqlite.SQLiteDatabase;
import com.huawei.systemmanager.util.HwLog;
import java.util.Locale;

public class TableMessage extends TableBase {
    public static final String NAME = "interception_messages";
    private static final String TAG = "TableMessage";

    public boolean upgrade8to9(android.database.sqlite.SQLiteDatabase r6, boolean r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:18:? in {3, 6, 9, 14, 15, 17, 19} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
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
        r5 = this;
        r2 = "TableMessage";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "updateMessageTable8to9, isTemp:";
        r3 = r3.append(r4);
        r3 = r3.append(r7);
        r3 = r3.toString();
        com.huawei.systemmanager.util.HwLog.i(r2, r3);
        if (r7 == 0) goto L_0x0060;
    L_0x001c:
        r1 = r5.getTempTablename();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
    L_0x0020:
        r6.beginTransaction();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2.<init>();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = "ALTER TABLE ";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.append(r1);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = " ADD ";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = "block_reason";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = "  INTEGER DEFAULT 0;";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.append(r3);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r6.execSQL(r2);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r6.setTransactionSuccessful();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = "TableMessage";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = "updateMessageTable8to9: end: success";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        com.huawei.systemmanager.util.HwLog.i(r2, r3);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = 1;
        r6.endTransaction();
        return r2;
    L_0x0060:
        r1 = r5.getTableName();	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        goto L_0x0020;
    L_0x0065:
        r0 = move-exception;
        r2 = "TableMessage";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r3 = "updateMessageTable8to9: Exception";	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        com.huawei.systemmanager.util.HwLog.e(r2, r3, r0);	 Catch:{ Exception -> 0x0065, all -> 0x0074 }
        r2 = 0;
        r6.endTransaction();
        return r2;
    L_0x0074:
        r2 = move-exception;
        r6.endTransaction();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.TableMessage.upgrade8to9(android.database.sqlite.SQLiteDatabase, boolean):boolean");
    }

    public String getTableName() {
        return "interception_messages";
    }

    public void create(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS interception_messages(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone Text,name Text,body Text,date long,sub_id int DEFAULT 0, size long, exp_date long, pdu BLOB, type INTEGER DEFAULT 0, message_type INTEGER DEFAULT 0, message_id INTEGER DEFAULT -1,group_message_name Text,block_reason INTEGER DEFAULT 0)");
    }

    public String createTempTable(SQLiteDatabase db) {
        return null;
    }

    public void combineRecoverData(SQLiteDatabase db, int oldVersion) {
        String msgTable = "interception_messages";
        String msgTableBak = "interception_messages_tmpbak";
        String columns = "phone,name, body, date, sub_id,message_type,message_id,group_message_name, size, exp_date, pdu, type,block_reason";
        db.execSQL(String.format(Locale.US, "INSERT INTO %1$s(%2$s) SELECT %3$s FROM %4$s WHERE %5$s NOT IN (SELECT %6$s.%7$s FROM %8$s, %9$s WHERE %10$s.%11$s = %12$s.%13$s AND %14$s.%15$s = %16$s.%17$s)", new Object[]{msgTable, columns, columns, msgTableBak, "_id", msgTableBak, "_id", msgTableBak, msgTable, msgTableBak, "phone", msgTable, "phone", msgTableBak, "date", msgTable, "date"}));
        HwLog.i(TAG, "combineRecoverData: Combine messages end");
    }
}
