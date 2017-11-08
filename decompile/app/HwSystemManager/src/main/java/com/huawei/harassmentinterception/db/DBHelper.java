package com.huawei.harassmentinterception.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.common.Tables;
import com.huawei.harassmentinterception.common.Tables.TbBlacklist;
import com.huawei.harassmentinterception.common.Tables.TbKeywords;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.harassmentinterception.common.Tables.TbNumberLocation;
import com.huawei.harassmentinterception.update.UpdateHelper;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.systemmanager.backup.HsmSQLiteOpenHelper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationHelper;
import com.huawei.systemmanager.util.numberlocation.NumberLocationInfo;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import com.huawei.systemmanager.util.phonematch.PhoneMatchInfo;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class DBHelper extends HsmSQLiteOpenHelper {
    private static final String DATABASE_NAME = "HarassmentInterception.db";
    private static final int DATABASE_VERSION = 10;
    private static final String TAG = "HarassmentInterceptionDBHelper";
    private final TableCall mCallTable = new TableCall();
    private Context mContext = null;
    private final TableMessage mMessageTable = new TableMessage();

    private boolean combineRecoverData(android.database.sqlite.SQLiteDatabase r26, int r27) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:27:? in {4, 7, 10, 12, 17, 22, 23, 25, 26, 29, 30} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r25 = this;
        r20 = "HarassmentInterceptionDBHelper";
        r21 = new java.lang.StringBuilder;
        r21.<init>();
        r22 = "combineRecoverData: Start, oldVersion = ";
        r21 = r21.append(r22);
        r0 = r21;
        r1 = r27;
        r21 = r0.append(r1);
        r21 = r21.toString();
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r10 = r25.getNewPhoneNumbersFromRecoverData(r26, r27);
        r26.beginTransaction();	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r3 = "interception_blacklist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r4 = "interception_blacklist_tmpbak";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r5 = "phone,name, option,type";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = java.util.Locale.US;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "INSERT INTO %1$s(%2$s) SELECT %3$s FROM %4$s where %5$s NOT IN (SELECT %6$s FROM %7$s)";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = 7;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r22;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = new java.lang.Object[r0];	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 1;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 2;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r4;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 4;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 6;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r13 = java.lang.String.format(r20, r21, r22);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.execSQL(r13);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: Combine blacklist end";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.i(r20, r21);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r27;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        if (r0 < r1) goto L_0x00c3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x0077:
        r18 = "tbWhitelist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r19 = "tbWhitelist_tmpbak";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r5 = "phone,name, option,type";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = java.util.Locale.US;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "INSERT INTO %1$s(%2$s) SELECT %3$s FROM %4$s WHERE %5$s NOT IN (SELECT %6$s FROM %7$s)";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = 7;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r22;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = new java.lang.Object[r0];	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r18;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 1;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 2;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r19;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 4;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 6;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r18;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r16 = java.lang.String.format(r20, r21, r22);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r16;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.execSQL(r1);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: Combine whitelist end";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.i(r20, r21);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x00c3:
        r0 = r25;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r0.mCallTable;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r2 = r27;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.combineRecoverData(r1, r2);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r25;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r0.mMessageTable;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r2 = r27;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.combineRecoverData(r1, r2);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r11 = "interception_rules";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r12 = "interception_rules_tmpbak";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r2 = r21;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.delete(r11, r1, r2);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = java.util.Locale.US;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "INSERT INTO %1$s SELECT * FROM %2$s";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = 2;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r22;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = new java.lang.Object[r0];	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r11;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 1;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r12;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r15 = java.lang.String.format(r20, r21, r22);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.execSQL(r15);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: Combine rules end";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.i(r20, r21);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r27;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        if (r0 < r1) goto L_0x018a;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x0123:
        r20 = java.util.Locale.US;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "DELETE FROM %1$s WHERE %2$s IN (SELECT %3$s.%4$s FROM %5$s, %6$s WHERE %7$s.%8$s = %9$s.%10$s)";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = 10;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r22;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = new java.lang.Object[r0];	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "interception_blacklist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "_id";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 1;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "interception_blacklist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 2;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "_id";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "interception_blacklist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 4;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "tbWhitelist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "interception_blacklist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 6;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 7;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "tbWhitelist";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 8;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "phone";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 9;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r17 = java.lang.String.format(r20, r21, r22);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r17;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.execSQL(r1);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: check blacklist and whitelist end";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.i(r20, r21);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x018a:
        r20 = 7;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r27;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        if (r0 < r1) goto L_0x01dc;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x0192:
        r8 = "tbKeywordsTable";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r9 = "tbKeywordsTable_tmpbak";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r5 = "keyword";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = java.util.Locale.US;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "INSERT INTO %1$s(%2$s) SELECT %3$s FROM %4$s WHERE %5$s NOT IN (SELECT %6$s FROM %7$s)";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = 7;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r22;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = new java.lang.Object[r0];	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22 = r0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 0;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r8;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 1;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 2;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 3;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r9;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "keyword";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 4;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = "keyword";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r24 = 5;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r24] = r23;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r23 = 6;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r22[r23] = r8;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r14 = java.lang.String.format(r20, r21, r22);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r26;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0.execSQL(r14);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: Combine keywords end";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.i(r20, r21);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
    L_0x01dc:
        r26.setTransactionSuccessful();	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r26.endTransaction();
        r20 = "interception_calls";
        r0 = r25;
        r1 = r26;
        r2 = r20;
        r0.checkRecordsCount(r1, r2);
        r20 = "HarassmentInterceptionDBHelper";
        r21 = "combineRecoverData: check calls count end";
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r20 = "interception_messages";
        r0 = r25;
        r1 = r26;
        r2 = r20;
        r0.checkRecordsCount(r1, r2);
        r20 = "HarassmentInterceptionDBHelper";
        r21 = "combineRecoverData: check messages count end";
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r25.updateBlacklistStatInfo(r26);
        r20 = "HarassmentInterceptionDBHelper";
        r21 = "combineRecoverData: update blacklist statics end";
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r0 = r25;
        r1 = r26;
        r0.updateNumberLocationCache(r1, r10);
        r20 = "HarassmentInterceptionDBHelper";
        r21 = "combineRecoverData: updateNumberLocationCache end";
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r0 = r25;
        r0 = r0.mContext;
        r20 = r0;
        com.huawei.harassmentinterception.util.CommonHelper.notifyInterceptionSettingChange(r20);
        r20 = "HarassmentInterceptionDBHelper";
        r21 = "combineRecoverData: Finish";
        com.huawei.systemmanager.util.HwLog.i(r20, r21);
        r20 = 1;
        return r20;
    L_0x023d:
        r7 = move-exception;
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: Exception";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r21;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = 0;
        r26.endTransaction();
        return r20;
    L_0x0251:
        r6 = move-exception;
        r20 = "HarassmentInterceptionDBHelper";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r21 = "combineRecoverData: SQLException";	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r0 = r20;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r1 = r21;	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r6);	 Catch:{ SQLException -> 0x0251, Exception -> 0x023d, all -> 0x0265 }
        r20 = 0;
        r26.endTransaction();
        return r20;
    L_0x0265:
        r20 = move-exception;
        r26.endTransaction();
        throw r20;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBHelper.combineRecoverData(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    private boolean recoverFromVersion7(android.database.sqlite.SQLiteDatabase r6, int r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:13:? in {4, 9, 10, 12, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r5 = this;
        r2 = "HarassmentInterceptionDBHelper";
        r3 = new java.lang.StringBuilder;
        r3.<init>();
        r4 = "recoverFromVersion7: Start, oldVersion = ";
        r3 = r3.append(r4);
        r3 = r3.append(r7);
        r3 = r3.toString();
        com.huawei.systemmanager.util.HwLog.i(r2, r3);
        r0 = "interception_messages_tmpbak";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.beginTransaction();	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD size long DEFAULT 0;";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.execSQL(r2);	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD exp_date long DEFAULT 0;";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.execSQL(r2);	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD pdu BLOB ;";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.execSQL(r2);	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD type INTEGER DEFAULT 0;";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.execSQL(r2);	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r6.setTransactionSuccessful();	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = 1;
        r6.endTransaction();
        return r2;
    L_0x0040:
        r1 = move-exception;
        r2 = "HarassmentInterceptionDBHelper";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r3 = "upgradeTmpMsgTable7To8: Exception";	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        com.huawei.systemmanager.util.HwLog.e(r2, r3, r1);	 Catch:{ Exception -> 0x0040, all -> 0x004f }
        r2 = 0;
        r6.endTransaction();
        return r2;
    L_0x004f:
        r2 = move-exception;
        r6.endTransaction();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBHelper.recoverFromVersion7(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    private boolean recoverFromVersion9(android.database.sqlite.SQLiteDatabase r6, int r7) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:12:? in {3, 8, 9, 11, 13, 14} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r5 = this;
        r4 = 1;
        r1 = "HarassmentInterceptionDBHelper";
        r2 = new java.lang.StringBuilder;
        r2.<init>();
        r3 = "recoverFromVersion9: Start, oldVersion = ";
        r2 = r2.append(r3);
        r2 = r2.append(r7);
        r2 = r2.toString();
        com.huawei.systemmanager.util.HwLog.i(r1, r2);
        r6.beginTransaction();	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r1 = r5.mCallTable;	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r2 = 1;	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r1.upgrade8to9(r6, r2);	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r1 = r5.mMessageTable;	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r2 = 1;	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r1.upgrade8to9(r6, r2);	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r6.setTransactionSuccessful();	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r6.endTransaction();
        return r4;
    L_0x0031:
        r0 = move-exception;
        r1 = "HarassmentInterceptionDBHelper";	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r2 = "recoverFromVersion9: Exception";	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        com.huawei.systemmanager.util.HwLog.e(r1, r2, r0);	 Catch:{ Exception -> 0x0031, all -> 0x0040 }
        r1 = 0;
        r6.endTransaction();
        return r1;
    L_0x0040:
        r1 = move-exception;
        r6.endTransaction();
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBHelper.recoverFromVersion9(android.database.sqlite.SQLiteDatabase, int):boolean");
    }

    private boolean upgradeTmpMsgTable7To8(android.database.sqlite.SQLiteDatabase r5) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:13:? in {4, 9, 10, 12, 14, 15} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r4 = this;
        r2 = "HarassmentInterceptionDBHelper";
        r3 = "upgradeTmpMsgTable7To8: begin";
        com.huawei.systemmanager.util.HwLog.i(r2, r3);
        r0 = "interception_messages_tmpbak";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.beginTransaction();	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD size long DEFAULT 0;";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.execSQL(r2);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD exp_date long DEFAULT 0;";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.execSQL(r2);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD pdu BLOB ;";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.execSQL(r2);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = "ALTER TABLE interception_messages_tmpbak ADD type INTEGER DEFAULT 0;";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.execSQL(r2);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r5.setTransactionSuccessful();	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = "HarassmentInterceptionDBHelper";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r3 = "upgradeTmpMsgTable7To8: end: success";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        com.huawei.systemmanager.util.HwLog.i(r2, r3);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = 1;
        r5.endTransaction();
        return r2;
    L_0x0038:
        r1 = move-exception;
        r2 = "HarassmentInterceptionDBHelper";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r3 = "upgradeTmpMsgTable7To8: Exception";	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        com.huawei.systemmanager.util.HwLog.e(r2, r3, r1);	 Catch:{ Exception -> 0x0038, all -> 0x0047 }
        r2 = 0;
        r5.endTransaction();
        return r2;
    L_0x0047:
        r2 = move-exception;
        r5.endTransaction();
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBHelper.upgradeTmpMsgTable7To8(android.database.sqlite.SQLiteDatabase):boolean");
    }

    public static int getDBVersion() {
        return 10;
    }

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, 10);
        this.mContext = context;
    }

    public void onCreate(SQLiteDatabase db) {
        HwLog.i(TAG, "onCreate");
        createTables(db);
        createViews(db);
        createIndex(db);
        initValues(db);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onUpgrade, oldVersion = " + oldVersion + ", newVersion = " + newVersion);
        upgradeFrom1To2(db, oldVersion, newVersion);
        upgradeFrom2To3(db, oldVersion, newVersion);
        upgradeFrom3To4(db, oldVersion, newVersion);
        upgradeToRcs(db, oldVersion, newVersion);
        upgradeFrom5To6(db, oldVersion, newVersion);
        upgradeFrom6To7(db, oldVersion, newVersion);
        upgradeFrom7To8(db, oldVersion, newVersion);
        upgradeFrom8To9(db, oldVersion, newVersion);
        upgradeFrom9To10(db, oldVersion, newVersion);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        HwLog.i(TAG, "onDowngrade, oldVersion = " + oldVersion + ", newVersion = " + newVersion);
        dropViews(db);
        dropTables(db);
        createTables(db);
        createViews(db);
        createIndex(db);
    }

    private void createTables(SQLiteDatabase db) {
        HwLog.i(TAG, "createTables");
        db.execSQL("CREATE TABLE IF NOT EXISTS interception_blacklist(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,name TEXT,interception_call_count int DEFAULT 0,interception_msg_count int DEFAULT 0,option int DEFAULT 3,type int DEFAULT 0);");
        this.mMessageTable.create(db);
        this.mCallTable.create(db);
        db.execSQL("CREATE TABLE IF NOT EXISTS interception_rules(_id INTEGER PRIMARY KEY AUTOINCREMENT,name Text,state int DEFAULT 0);");
        db.execSQL("CREATE TABLE IF NOT EXISTS tbWhitelist(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,name TEXT, option int DEFAULT 3,type int DEFAULT 0);");
        db.execSQL("CREATE TABLE IF NOT EXISTS tbNumberLocation(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,location TEXT,operator TEXT);");
        db.execSQL("CREATE TABLE IF NOT EXISTS tbKeywordsTable(_id INTEGER PRIMARY KEY AUTOINCREMENT,keyword TEXT);");
    }

    private void createViews(SQLiteDatabase db) {
        HwLog.i(TAG, "createViews");
        db.execSQL(SqlStatements.SQL_CREATE_BLACKLIST_VIEW);
        db.execSQL(SqlStatements.SQL_CREATE_MESSAGES_VIEW);
        db.execSQL(SqlStatements.SQL_CREATE_CALLS_VIEW);
        db.execSQL(SqlStatements.SQL_CREATE_WHITELIST_VIEW);
    }

    private void createIndex(SQLiteDatabase db) {
        HwLog.i(TAG, "createIndex");
        db.execSQL(SqlStatements.SQL_CREATE_NUMBERLOCATION_INDEX);
    }

    private void initValues(SQLiteDatabase db) {
        RulesOps.initRules(this.mContext);
    }

    private void dropTables(SQLiteDatabase db) {
        HwLog.i(TAG, "dropTables");
        db.execSQL("DROP TABLE IF EXISTS interception_blacklist");
        this.mMessageTable.drop(db);
        this.mCallTable.drop(db);
        db.execSQL("DROP TABLE IF EXISTS interception_rules");
        db.execSQL("DROP TABLE IF EXISTS tbWhitelist");
        db.execSQL("DROP TABLE IF EXISTS tbNumberLocation");
        db.execSQL("DROP TABLE IF EXISTS tbKeywordsTable");
    }

    private void dropViews(SQLiteDatabase db) {
        HwLog.i(TAG, "dropViews");
        db.execSQL("DROP VIEW IF EXISTS vBlacklist");
        db.execSQL("DROP VIEW IF EXISTS vMessages");
        db.execSQL("DROP VIEW IF EXISTS vCalls");
        db.execSQL("DROP VIEW IF EXISTS vWhitelist");
    }

    private void upgradeFrom1To2(SQLiteDatabase db, int oldVersion, int newVersion) {
    }

    private void upgradeFrom2To3(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 2 && newVersion > 2) {
            try {
                db.beginTransaction();
                db.execSQL("CREATE TABLE IF NOT EXISTS tbWhitelist(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,name TEXT, option int DEFAULT 3,type int DEFAULT 0);");
                db.execSQL("CREATE TABLE IF NOT EXISTS tbNumberLocation(_id INTEGER PRIMARY KEY AUTOINCREMENT,phone TEXT,location TEXT,operator TEXT);");
                createViews(db);
                createIndex(db);
                updateNumberLocationCache(db, getAllPhoneNumbers(db));
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeFrom2To3: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void upgradeFrom3To4(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 3 && newVersion > 3) {
            try {
                db.beginTransaction();
                dropViews(db);
                createViews(db);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeFrom3To4: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void upgradeToRcs(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 4 && newVersion > 4) {
            try {
                db.beginTransaction();
                db.execSQL("ALTER TABLE interception_messages ADD message_type INTEGER DEFAULT 0;");
                db.execSQL("ALTER TABLE interception_messages ADD message_id INTEGER DEFAULT -1;");
                db.execSQL("ALTER TABLE interception_messages ADD group_message_name Text;");
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeToRcs: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void upgradeFrom5To6(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 5 && newVersion > 5) {
            try {
                String blTable = Tables.BLACKLIST_TABLE;
                String wlTable = Tables.WHITELIST_TABLE;
                db.beginTransaction();
                db.execSQL("DROP VIEW IF EXISTS vBlacklist");
                db.execSQL("DROP VIEW IF EXISTS vWhitelist");
                db.execSQL("ALTER TABLE interception_blacklist ADD option INTEGER DEFAULT 3;");
                db.execSQL("ALTER TABLE interception_blacklist ADD type INTEGER DEFAULT 0;");
                db.execSQL("ALTER TABLE tbWhitelist ADD option INTEGER DEFAULT 3;");
                db.execSQL("ALTER TABLE tbWhitelist ADD type INTEGER DEFAULT 0;");
                db.execSQL(SqlStatements.SQL_CREATE_BLACKLIST_VIEW);
                db.execSQL(SqlStatements.SQL_CREATE_WHITELIST_VIEW);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeFrom5To6: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void upgradeFrom6To7(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 6 && newVersion > 6) {
            db.execSQL("CREATE TABLE IF NOT EXISTS tbKeywordsTable(_id INTEGER PRIMARY KEY AUTOINCREMENT,keyword TEXT);");
        }
    }

    private void upgradeFrom7To8(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 7 && newVersion > 7) {
            try {
                String MsgTable = "interception_messages";
                db.beginTransaction();
                db.execSQL("DROP VIEW IF EXISTS vMessages");
                db.execSQL("ALTER TABLE interception_messages ADD size long DEFAULT 0;");
                db.execSQL("ALTER TABLE interception_messages ADD exp_date long DEFAULT 0;");
                db.execSQL("ALTER TABLE interception_messages ADD pdu BLOB ;");
                db.execSQL("ALTER TABLE interception_messages ADD type INTEGER DEFAULT 0;");
                db.execSQL(SqlStatements.SQL_CREATE_MESSAGES_VIEW);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeFrom7To8: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private void upgradeFrom8To9(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 8 && newVersion > 8) {
            HwLog.i(TAG, "upgradeFrom8To9, oldversion:" + oldVersion + ", new version:" + newVersion);
            try {
                db.beginTransaction();
                this.mCallTable.upgrade8to9(db, false);
                this.mMessageTable.upgrade8to9(db, false);
                db.execSQL("DROP VIEW IF EXISTS vMessages");
                db.execSQL(SqlStatements.SQL_CREATE_MESSAGES_VIEW);
                db.execSQL("DROP VIEW IF EXISTS vCalls");
                db.execSQL(SqlStatements.SQL_CREATE_CALLS_VIEW);
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "upgradeFrom8To9: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    public void upgradeFrom9To10(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion <= 9 && newVersion > 9) {
            RulesOps.initRules(this.mContext);
            if (oldVersion == 9) {
                int i;
                DBVersion9To10.upgradeFrom9To10(this.mContext, db);
                boolean autoUpdate = PreferenceHelper.getState(this.mContext, "harassment_auto_update_state");
                Context context = this.mContext;
                if (autoUpdate) {
                    i = 2;
                } else {
                    i = 1;
                }
                UpdateHelper.setAutoUpdateStrategy(context, i);
            } else {
                recoverRulesFrom8(PreferenceHelper.getInterceptionStrategy(this.mContext));
                int updateStrategy = !PreferenceHelper.getState(this.mContext, "harassment_auto_update_state") ? 1 : PreferenceHelper.getState(this.mContext, PreferenceHelper.KEY_ONLY_WIFI_UPDATE_STATE) ? 2 : 3;
                UpdateHelper.setAutoUpdateStrategy(this.mContext, updateStrategy);
            }
        }
    }

    private Set<String> getAllPhoneNumbers(SQLiteDatabase db) {
        Set<String> phoneNumberSet = new HashSet();
        String[] columns = new String[]{"phone"};
        String group = "phone";
        phoneNumberSet.addAll(getPhoneSetFromCursor(db.query(Tables.BLACKLIST_TABLE, columns, null, null, group, null, null)));
        phoneNumberSet.addAll(getPhoneSetFromCursor(db.query(Tables.CALLS_TABLE, columns, null, null, group, null, null)));
        phoneNumberSet.addAll(getPhoneSetFromCursor(db.query("interception_messages", columns, null, null, group, null, null)));
        phoneNumberSet.addAll(getPhoneSetFromCursor(db.query(Tables.WHITELIST_TABLE, columns, null, null, group, null, null)));
        return phoneNumberSet;
    }

    private Set<String> getNumberLocationCaches(SQLiteDatabase db) {
        Set<String> locationCaches = new HashSet();
        locationCaches.addAll(getPhoneSetFromCursor(db.query(Tables.NUMBERLOCATION_TABLE, new String[]{"phone"}, null, null, null, null, null)));
        return locationCaches;
    }

    void updateNumberLocationCache(SQLiteDatabase db, Set<String> phoneNumberSet) {
        int phoneNumberCount = phoneNumberSet.size();
        HwLog.i(TAG, "updateNumberLocationCache: Phone number count = " + phoneNumberCount);
        if (phoneNumberCount > 0) {
            Set<String> locationCache = getNumberLocationCaches(db);
            HwLog.i(TAG, "updateNumberLocationCache: location cache count = " + locationCache.size());
            Set<String> tmpNumberSet = new HashSet(phoneNumberSet);
            tmpNumberSet.removeAll(locationCache);
            if (tmpNumberSet.isEmpty()) {
                HwLog.i(TAG, "updateNumberLocationCache: No number needs to be updated ");
                return;
            }
            ContentValues value;
            HwLog.i(TAG, "updateNumberLocationCache: new number count = " + tmpNumberSet.size());
            ContentValues[] values = new ContentValues[tmpNumberSet.size()];
            int nIndex = 0;
            for (String phone : tmpNumberSet) {
                NumberLocationInfo location = NumberLocationHelper.queryNumberLocation(this.mContext, phone);
                value = new ContentValues();
                value.put("phone", phone);
                value.put("location", location.getLocation());
                value.put(TbNumberLocation.OPERATOR, location.getOperator());
                int nIndex2 = nIndex + 1;
                values[nIndex] = value;
                nIndex = nIndex2;
            }
            try {
                db.beginTransaction();
                for (ContentValues value2 : values) {
                    db.replace(Tables.NUMBERLOCATION_TABLE, null, value2);
                }
                db.setTransactionSuccessful();
            } catch (Exception e) {
                HwLog.e(TAG, "updateNumberLocationCache: Exception", e);
            } finally {
                db.endTransaction();
            }
        }
    }

    private Set<String> getPhoneSetFromCursor(Cursor cursor) {
        Set<String> phoneSet = new HashSet();
        if (!Utility.isNullOrEmptyCursor(cursor, true)) {
            while (cursor.moveToNext()) {
                phoneSet.add(cursor.getString(0));
            }
            cursor.close();
        }
        return phoneSet;
    }

    protected boolean onRecoverStart(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "onRecoverStart: oldVersion = " + oldVersion);
        boolean bSuccess = false;
        switch (oldVersion) {
            case 1:
            case 2:
                bSuccess = createTempTablesVersion1or2(db, oldVersion);
                break;
            case 3:
            case 4:
                bSuccess = createTempTablesVersion3or4(db, oldVersion);
                break;
            case 5:
                bSuccess = createTempTablesVersion5(db, oldVersion);
                break;
            case 6:
                bSuccess = createTempTablesVersion6(db, oldVersion);
                break;
            case 7:
                bSuccess = createTempTablesVersion7(db, oldVersion);
                break;
            case 8:
                bSuccess = createTempTablesVersion8(db, oldVersion);
                break;
            case 9:
                bSuccess = createTempTablesVersion9(db, oldVersion);
                break;
            case 10:
                bSuccess = createTempTablesVersion10(db, oldVersion);
                break;
            default:
                HwLog.w(TAG, "onRecoverStart: Invalid recover version = " + oldVersion);
                break;
        }
        HwLog.i(TAG, "onRecoverStart: Result = " + bSuccess);
        return bSuccess;
    }

    protected boolean onRecoverComplete(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "onRecoverComplete: Start, oldVersion = " + oldVersion);
        boolean bSuccess = true;
        switch (oldVersion) {
            case 1:
            case 2:
                if (!recoverFromVersion1or2(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 3:
            case 4:
                if (!recoverFromVersion3or4(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 5:
                if (!recoverFromVersion5(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 6:
                if (!recoverFromVersion6(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 7:
                if (!recoverFromVersion7(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 8:
                if (!recoverFromVersion8(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
            case 9:
                if (!recoverFromVersion9(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
                break;
            case 10:
                if (!recoverFromVersion10(db, oldVersion)) {
                    bSuccess = false;
                    break;
                }
                break;
            default:
                HwLog.w(TAG, "onRecoverComplete: Invalid recover version = " + oldVersion);
                break;
        }
        if (bSuccess) {
            try {
                bSuccess = combineRecoverData(db, oldVersion);
            } catch (SQLException e) {
                bSuccess = false;
                HwLog.e(TAG, "onRecoverComplete: SQLException happens in combineRecoverData", e);
            } catch (Exception e2) {
                bSuccess = false;
                HwLog.e(TAG, "onRecoverComplete: Exception happens in combineRecoverData", e2);
            }
        }
        clearRecoverTmpTablesAndMap(db);
        HwLog.i(TAG, "onRecoverComplete: End, bSuccess = " + bSuccess);
        return bSuccess;
    }

    private boolean createTempTablesVersion1or2(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion1or2: Start, oldVersion = " + oldVersion);
        boolean bSuccess = false;
        try {
            db.beginTransaction();
            String blTableBak = "interception_blacklist_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + blTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + blTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "phone" + " TEXT," + "name" + " TEXT," + TbBlacklist.INTERCEPTED_CALL_COUNT + " int DEFAULT 0," + TbBlacklist.INTERCEPTED_MSG_COUNT + " int DEFAULT 0);");
            putRecoverTmpTableMap(Tables.BLACKLIST_TABLE, blTableBak);
            String msgTableBak = "interception_messages_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + msgTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + msgTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "phone" + " Text," + "name" + " Text," + TbMessages.BODY + " Text," + "date" + " long," + "sub_id" + " int DEFAULT 0);");
            putRecoverTmpTableMap("interception_messages", msgTableBak);
            String callsTableBak = "interception_calls_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + callsTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + callsTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "phone" + " Text," + "name" + " Text," + "date" + " long);");
            putRecoverTmpTableMap(Tables.CALLS_TABLE, callsTableBak);
            String rulesTableBak = "interception_rules_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + rulesTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + rulesTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "name" + " Text," + "state" + " int DEFAULT 0);");
            putRecoverTmpTableMap(Tables.RULES_TABLE, rulesTableBak);
            db.setTransactionSuccessful();
            bSuccess = true;
            HwLog.i(TAG, "createTempTablesVersion1or2: Succeeds");
        } catch (SQLException e) {
            HwLog.e(TAG, "createTempTablesVersion1or2: SQLException", e);
        } finally {
            db.endTransaction();
        }
        return bSuccess;
    }

    private boolean createTempTablesVersion3or4(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion3or4: Start, oldVersion = " + oldVersion);
        boolean createTempTablesVersion1or2 = createTempTablesVersion1or2(db, oldVersion);
        if (!createTempTablesVersion1or2) {
            return false;
        }
        try {
            String wlTableBak = "tbWhitelist_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + wlTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + wlTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + "phone" + " TEXT," + "name" + " TEXT);");
            putRecoverTmpTableMap(Tables.WHITELIST_TABLE, wlTableBak);
            createTempTablesVersion1or2 = true;
            HwLog.i(TAG, "createTempTablesVersion3or4: Succeeds");
        } catch (SQLException e) {
            HwLog.e(TAG, "createTempTablesVersion3or4: Exception", e);
        }
        return createTempTablesVersion1or2;
    }

    private boolean createTempTablesVersion5(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion5: Start, oldVersion = " + oldVersion);
        boolean bSuccess = createTempTablesVersion3or4(db, oldVersion);
        if (!bSuccess) {
            return false;
        }
        upgradeTmpMsgTableForRCS(db);
        return bSuccess;
    }

    private boolean createTempTablesVersion6(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion6: Start, oldVersion = " + oldVersion);
        boolean bSuccess = createTempTablesVersion5(db, oldVersion);
        if (!bSuccess) {
            return false;
        }
        upgradeTmpBlTable5To6(db);
        return bSuccess;
    }

    private boolean createTempTablesVersion7(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion7: Start, oldVersion = " + oldVersion);
        boolean createTempTablesVersion6 = createTempTablesVersion6(db, oldVersion);
        if (!createTempTablesVersion6) {
            return false;
        }
        try {
            String kwTableBak = "tbKeywordsTable_tmpbak";
            db.execSQL("DROP TABLE IF EXISTS " + kwTableBak);
            db.execSQL("CREATE TABLE IF NOT EXISTS " + kwTableBak + "(_id INTEGER PRIMARY KEY AUTOINCREMENT," + TbKeywords.KEYWORD + " TEXT);");
            putRecoverTmpTableMap(Tables.KEYWORDS_TABLE, kwTableBak);
            createTempTablesVersion6 = true;
            HwLog.i(TAG, "createTempTablesVersion7: Succeeds");
        } catch (SQLException e) {
            HwLog.e(TAG, "createTempTablesVersion7: Exception", e);
        }
        return createTempTablesVersion6;
    }

    private boolean createTempTablesVersion8(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion8: Start, oldVersion = " + oldVersion);
        boolean bSuccess = createTempTablesVersion7(db, oldVersion);
        if (bSuccess) {
            upgradeTmpMsgTable7To8(db);
            return bSuccess;
        }
        HwLog.w(TAG, "createTempTablesVersion8 step0 failed");
        return false;
    }

    private boolean createTempTablesVersion9(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion9: Start, oldVersion = " + oldVersion);
        if (createTempTablesVersion8(db, oldVersion)) {
            try {
                this.mCallTable.upgrade8to9(db, true);
                this.mMessageTable.upgrade8to9(db, true);
                return true;
            } catch (Exception e) {
                HwLog.e(TAG, "createTempTablesVersion9: Exception", e);
                return false;
            }
        }
        HwLog.w(TAG, "createTempTablesVersion9 step0 failed");
        return false;
    }

    private boolean createTempTablesVersion10(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "createTempTablesVersion10: Start, oldVersion = " + oldVersion);
        if (createTempTablesVersion9(db, oldVersion)) {
            return true;
        }
        HwLog.w(TAG, "createTempTablesVersion10 step0 failed");
        return false;
    }

    private boolean upgradeTmpMsgTableForRCS(SQLiteDatabase db) {
        boolean z;
        try {
            String msgTableBak = "interception_messages_tmpbak";
            db.beginTransaction();
            db.execSQL("ALTER TABLE " + msgTableBak + " ADD " + "message_type" + " INTEGER DEFAULT " + 0 + SqlMarker.SQL_END);
            db.execSQL("ALTER TABLE " + msgTableBak + " ADD " + "message_id" + " INTEGER DEFAULT -1;");
            db.execSQL("ALTER TABLE " + msgTableBak + " ADD " + "group_message_name" + " Text;");
            db.setTransactionSuccessful();
            z = true;
            return z;
        } catch (SQLException e) {
            z = TAG;
            HwLog.e(z, "upgradeTmpMsgTableForRCS: SQLException", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private boolean upgradeTmpBlTable5To6(SQLiteDatabase db) {
        boolean z;
        try {
            String blTableBak = "interception_blacklist_tmpbak";
            String wlTableBak = "tbWhitelist_tmpbak";
            db.beginTransaction();
            db.execSQL("ALTER TABLE " + blTableBak + " ADD " + "option" + " INTEGER DEFAULT " + 3 + SqlMarker.SQL_END);
            db.execSQL("ALTER TABLE " + blTableBak + " ADD " + "type" + " INTEGER DEFAULT " + 0 + SqlMarker.SQL_END);
            db.execSQL("ALTER TABLE " + wlTableBak + " ADD " + "option" + " INTEGER DEFAULT " + 3 + SqlMarker.SQL_END);
            db.execSQL("ALTER TABLE " + wlTableBak + " ADD " + "type" + " INTEGER DEFAULT " + 0 + SqlMarker.SQL_END);
            db.setTransactionSuccessful();
            z = true;
            return z;
        } catch (Exception e) {
            z = TAG;
            HwLog.e(z, "upgradeTmpBlTable5To6: Exception", e);
            return false;
        } finally {
            db.endTransaction();
        }
    }

    private boolean recoverFromVersion1or2(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion1or2: Start, oldVersion = " + oldVersion);
        return true;
    }

    private boolean recoverFromVersion3or4(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion3or4: Start, oldVersion = " + oldVersion);
        upgradeTmpMsgTableForRCS(db);
        return true;
    }

    private boolean recoverFromVersion5(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion5: Start, oldVersion = " + oldVersion);
        upgradeTmpBlTable5To6(db);
        return true;
    }

    private boolean recoverFromVersion6(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion6: Start, oldVersion = " + oldVersion);
        return true;
    }

    private boolean recoverFromVersion8(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion8: Start, oldVersion = " + oldVersion);
        return true;
    }

    private boolean recoverFromVersion10(SQLiteDatabase db, int oldVersion) {
        HwLog.i(TAG, "recoverFromVersion10: Start, oldVersion = " + oldVersion);
        return true;
    }

    private Set<String> getNewPhoneNumbersFromRecoverData(SQLiteDatabase db, int oldVersion) {
        Set<String> phoneNumberSet = new HashSet();
        String blTable = Tables.BLACKLIST_TABLE;
        SQLiteDatabase sQLiteDatabase = db;
        phoneNumberSet.addAll(getPhoneSetFromCursor(sQLiteDatabase.rawQuery(String.format(Locale.US, "SELECT %1$s FROM %2$s where %3$s NOT IN (SELECT %4$s FROM %5$s) GROUP BY %6$s", new Object[]{"phone", "interception_blacklist_tmpbak", "phone", "phone", blTable, "phone"}), null)));
        String callTable = Tables.CALLS_TABLE;
        sQLiteDatabase = db;
        phoneNumberSet.addAll(getPhoneSetFromCursor(sQLiteDatabase.rawQuery(String.format(Locale.US, "SELECT %1$s FROM %2$s where %3$s NOT IN (SELECT %4$s FROM %5$s) GROUP BY %6$s", new Object[]{"phone", "interception_calls_tmpbak", "phone", "phone", callTable, "phone"}), null)));
        sQLiteDatabase = db;
        phoneNumberSet.addAll(getPhoneSetFromCursor(sQLiteDatabase.rawQuery(String.format(Locale.US, "SELECT %1$s FROM %2$s where %3$s NOT IN (SELECT %4$s FROM %5$s) GROUP BY %6$s", new Object[]{"phone", "interception_messages_tmpbak", "phone", "phone", "interception_messages", "phone"}), null)));
        if (oldVersion > 3) {
            String wlTable = Tables.WHITELIST_TABLE;
            phoneNumberSet.addAll(getPhoneSetFromCursor(db.rawQuery(String.format(Locale.US, "SELECT %1$s FROM %2$s where %3$s NOT IN (SELECT %4$s FROM %5$s) GROUP BY %6$s", new Object[]{"phone", "tbWhitelist_tmpbak", "phone", "phone", wlTable, "phone"}), null)));
        }
        return phoneNumberSet;
    }

    private void updateBlacklistStatInfo(SQLiteDatabase db) {
        Cursor blCursor = db.query(Tables.BLACKLIST_TABLE, null, null, null, null, null, null);
        if (Utility.isNullOrEmptyCursor(blCursor, true)) {
            HwLog.i(TAG, "updateBlacklistStatInfo: Empty blacklist or an error happens");
            return;
        }
        int nTotalBlCount = blCursor.getCount();
        int nTotalCallCount = getTableRecordsCount(db, Tables.CALLS_TABLE);
        int nTotalMsgCount = getTableRecordsCount(db, "interception_messages");
        HwLog.i(TAG, "updateBlacklistStatInfo: blacklist count = " + nTotalBlCount + ", calls count = " + nTotalCallCount + ", messages count = " + nTotalMsgCount);
        if (nTotalCallCount == 0 && nTotalMsgCount == 0) {
            ContentValues updateValue = new ContentValues();
            updateValue.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(0));
            updateValue.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(0));
            db.update(Tables.BLACKLIST_TABLE, updateValue, null, null);
            blCursor.close();
            return;
        }
        ContentValues[] values = readBlacklistFromCursor(blCursor);
        blCursor.close();
        updateBlacklistStatInMem(db, values, Tables.CALLS_TABLE, nTotalCallCount);
        updateBlacklistStatInMem(db, values, "interception_messages", nTotalMsgCount);
        bulkUpateBlacklistStatToDB(db, values);
    }

    private ContentValues[] readBlacklistFromCursor(Cursor blCursor) {
        ContentValues[] values = new ContentValues[blCursor.getCount()];
        int colIndexId = blCursor.getColumnIndex("_id");
        int colIndexPhone = blCursor.getColumnIndex("phone");
        int colIndexType = blCursor.getColumnIndex("type");
        int nIndex = 0;
        while (blCursor.moveToNext()) {
            ContentValues value = new ContentValues();
            value.put("_id", Integer.valueOf(blCursor.getInt(colIndexId)));
            value.put("phone", blCursor.getString(colIndexPhone));
            value.put("type", Integer.valueOf(blCursor.getInt(colIndexType)));
            value.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(0));
            value.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(0));
            int nIndex2 = nIndex + 1;
            values[nIndex] = value;
            nIndex = nIndex2;
        }
        return values;
    }

    private void updateBlacklistStatInMem(SQLiteDatabase db, ContentValues[] values, String statisticSrcTable, int nTotalRecordsCount) {
        if (nTotalRecordsCount > 0) {
            String statisticKey;
            if (Tables.CALLS_TABLE.equals(statisticSrcTable)) {
                statisticKey = TbBlacklist.INTERCEPTED_CALL_COUNT;
            } else {
                statisticKey = TbBlacklist.INTERCEPTED_MSG_COUNT;
            }
            for (ContentValues value : values) {
                int nCount = getPhoneMatchedRecordsCount(db, statisticSrcTable, value.getAsString("phone"), value.getAsInteger("type").intValue());
                if (nCount > 0) {
                    value.put(statisticKey, Integer.valueOf(nCount));
                }
            }
        }
    }

    private void bulkUpateBlacklistStatToDB(SQLiteDatabase db, ContentValues[] values) {
        try {
            HwLog.i(TAG, "bulkUpateBlacklistStatToDB: beginTransaction");
            db.beginTransaction();
            for (ContentValues value : values) {
                int nId = value.getAsInteger("_id").intValue();
                value.remove("_id");
                value.remove("phone");
                db.update(Tables.BLACKLIST_TABLE, value, "_id = ?", new String[]{String.valueOf(nId)});
            }
            db.setTransactionSuccessful();
            HwLog.i(TAG, "bulkUpateBlacklistStatToDB: setTransactionSuccessful");
        } catch (Exception e) {
            HwLog.e(TAG, "bulkUpateBlacklistStatToDB: Exception on updating ", e);
        } finally {
            db.endTransaction();
            HwLog.i(TAG, "bulkUpateBlacklistStatToDB: endTransaction");
        }
    }

    private int getTableRecordsCount(SQLiteDatabase db, String tableName) {
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + tableName, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return 0;
        }
        cursor.moveToFirst();
        int nCount = cursor.getInt(0);
        cursor.close();
        return nCount;
    }

    private int getPhoneMatchedRecordsCount(SQLiteDatabase db, String tableName, String phone, int type) {
        String[] selectionArgs;
        String selection = "";
        if (type == 0) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
        } else {
            String phoneWithCountryCode = ConstValues.PHONE_COUNTRY_CODE_CHINA + CommonHelper.trimPhoneCountryCode(phone);
            selection = "phone like ? OR phone like ?";
            selectionArgs = new String[]{phoneWithCountryCode + "%", phoneWithoutCountryCode + "%"};
        }
        Cursor cursor = db.query(tableName, new String[]{"_id"}, selection, selectionArgs, null, null, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return 0;
        }
        int nCount = cursor.getCount();
        cursor.close();
        return nCount;
    }

    void checkRecordsCount(SQLiteDatabase db, String table) {
        int nCount = getTableRecordsCount(db, table);
        HwLog.d(TAG, "checkRecordsCount: table = " + table + ", nCount = " + nCount);
        if (nCount > 10000) {
            int middleId = getMiddleId(db, table);
            HwLog.i(TAG, "checkRecordsCount: Records exceed max count , middleId = " + middleId);
            if (middleId >= 0) {
                HwLog.i(TAG, "checkRecordsCount: delete count = " + db.delete(table, "_id < ?", new String[]{String.valueOf(middleId)}));
            }
        }
    }

    private int getMiddleId(SQLiteDatabase db, String table) {
        SQLiteDatabase sQLiteDatabase = db;
        String str = table;
        Cursor cursor = sQLiteDatabase.query(str, new String[]{"_id"}, null, null, null, null, "_id DESC", String.valueOf(5000));
        if (cursor == null) {
            HwLog.w(TAG, "getMiddleId: Fail to get middle id for " + table);
            return -1;
        }
        cursor.moveToLast();
        int id = cursor.getInt(0);
        cursor.close();
        return id;
    }

    void recoverRulesFrom8(int nStrategyCfg) {
        RulesInheritHelper.inheritRulesFrom8(this.mContext, nStrategyCfg);
    }
}
