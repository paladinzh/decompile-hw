package com.huawei.harassmentinterception.db;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.provider.CallLog.Calls;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.huawei.harassmentinterception.blackwhitelist.GoogleBlackListContract;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistContactQueryColumn;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.CallInfo;
import com.huawei.harassmentinterception.common.CommonObject.CallLogInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactQueryColumn;
import com.huawei.harassmentinterception.common.CommonObject.InterceptionRuleInfo;
import com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.SmsInfo;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.common.Tables;
import com.huawei.harassmentinterception.common.Tables.TbBlacklist;
import com.huawei.harassmentinterception.common.Tables.TbCalls;
import com.huawei.harassmentinterception.common.Tables.TbKeywords;
import com.huawei.harassmentinterception.common.Tables.TbMessages;
import com.huawei.harassmentinterception.common.Tables.TbNumberLocation;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.harassmentinterception.util.MmsIntentHelper;
import com.huawei.harassmentinterception.util.PreferenceHelper;
import com.huawei.rcs.db.RcsDBAdapter;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.numberlocation.NumberLocationHelper;
import com.huawei.systemmanager.util.numberlocation.NumberLocationInfo;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import com.huawei.systemmanager.util.phonematch.PhoneMatchInfo;
import java.io.Closeable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

public class DBAdapter {
    private static final String[] ADDRESS_PROJECTION = new String[]{"_id", "address"};
    public static final Uri BACKUP_END_URI = DBProvider.BACKUP_END_RUI;
    private static final String[] CALLLOG_PROJECTION = new String[]{"name", "number", "date", "geocoded_location"};
    private static final Uri CANONICAL_ADD_URI = Uri.parse("content://mms-sms/canonical-addresses");
    private static Map<Uri, ContactQueryColumn> CONTACT_COLUMN_MAP = new HashMap();
    private static final String[] MESSAGE_PROJECTION = new String[]{"address", TbMessages.BODY, "date"};
    private static final String[] MESSAGE_THREADID_PROJECTION = new String[]{"snippet", "snippet_cs", "recipient_ids", "date", "has_attachment"};
    private static final String[] PHONE_LOOKUP_PROJECTION = new String[]{"_id", "display_name", "number"};
    private static final String TAG = "HarassmentInterceptionDBAdapter";
    private static final Uri THREAD_URI = Uri.parse("content://mms-sms/conversations?simple=true");
    private static final Uri blacklist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.BLACKLIST_TABLE);
    private static final Uri blacklist_view_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.BLACKLIST_VIEW);
    private static final Uri calls_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.CALLS_TABLE);
    private static final Uri calls_view_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.CALLS_VIEW);
    private static final Uri cloud_permission_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.rainbow.rainbowprovider"), "phoneNumberTable");
    private static final Uri keywords_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.KEYWORDS_TABLE);
    private static RcsDBAdapter mRcs = new RcsDBAdapter();
    private static final Uri messages_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), "interception_messages");
    private static final Uri messages_view_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.MESSAGES_VIEW);
    private static final Uri rules_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.RULES_TABLE);
    public static final Uri whitelist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.WHITELIST_TABLE);
    private static final Uri whitelist_view_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.WHITELIST_VIEW);

    public static java.util.List<com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo> getKeywordsList(android.content.Context r14) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0064 in list []
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
        r11 = new java.util.ArrayList;
        r11.<init>();
        r6 = 0;
        r0 = 2;
        r2 = new java.lang.String[r0];	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r0 = "_id";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r1 = 0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r2[r1] = r0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r0 = "keyword";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r1 = 1;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r2[r1] = r0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r0 = r14.getContentResolver();	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r1 = keywords_uri;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r3 = 0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r4 = 0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r5 = 0;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        if (r6 == 0) goto L_0x0065;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
    L_0x0024:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        if (r0 <= 0) goto L_0x0065;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
    L_0x002a:
        r0 = "_id";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r12 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r0 = "keyword";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r13 = r6.getColumnIndex(r0);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
    L_0x0038:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        if (r0 == 0) goto L_0x0065;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
    L_0x003e:
        r8 = r6.getInt(r12);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r9 = r6.getString(r13);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r0 = android.text.TextUtils.isEmpty(r9);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        if (r0 != 0) goto L_0x0038;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
    L_0x004c:
        r10 = new com.huawei.harassmentinterception.common.CommonObject$KeywordsInfo;	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r10.<init>(r8, r9);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r11.add(r10);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        goto L_0x0038;
    L_0x0055:
        r7 = move-exception;
        r0 = "HarassmentInterceptionDBAdapter";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        r1 = "getKeywordsList: Exception";	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x0055, all -> 0x006b }
        if (r6 == 0) goto L_0x0064;
    L_0x0061:
        r6.close();
    L_0x0064:
        return r11;
    L_0x0065:
        if (r6 == 0) goto L_0x006a;
    L_0x0067:
        r6.close();
    L_0x006a:
        return r11;
    L_0x006b:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0071;
    L_0x006e:
        r6.close();
    L_0x0071:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBAdapter.getKeywordsList(android.content.Context):java.util.List<com.huawei.harassmentinterception.common.CommonObject$KeywordsInfo>");
    }

    public static boolean isCallContact(android.content.Context r12, java.lang.String r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0068 in list []
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
        r11 = 1;
        r10 = 0;
        r8 = com.huawei.systemmanager.util.phonematch.PhoneMatch.getPhoneNumberMatchInfo(r13);
        r1 = r8.isValid();
        if (r1 != 0) goto L_0x0016;
    L_0x000c:
        r1 = "HarassmentInterceptionDBAdapter";
        r2 = "isCallContact: Invalid number";
        com.huawei.systemmanager.util.HwLog.w(r1, r2);
        return r10;
    L_0x0016:
        r0 = r12.getContentResolver();
        r6 = 0;
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "type = 2 AND ";
        r1 = r1.append(r2);
        r2 = "number";
        r2 = r8.getSqlSelectionStatement(r2);
        r1 = r1.append(r2);
        r3 = r1.toString();
        r4 = r8.getSqlSelectionArgs();
        r1 = android.provider.CallLog.Calls.CONTENT_URI;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r2 = 1;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r5 = "_id";	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r9 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r2[r9] = r5;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r5 = 0;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r1 = 1;	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r1 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyCursor(r6, r1);	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        if (r1 == 0) goto L_0x0053;
    L_0x0051:
        r6 = 0;
        return r10;
    L_0x0053:
        if (r6 == 0) goto L_0x0058;
    L_0x0055:
        r6.close();
    L_0x0058:
        return r11;
    L_0x0059:
        r7 = move-exception;
        r1 = "HarassmentInterceptionDBAdapter";	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        r2 = "isCallContact: Exception";	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        com.huawei.systemmanager.util.HwLog.e(r1, r2, r7);	 Catch:{ Exception -> 0x0059, all -> 0x0069 }
        if (r6 == 0) goto L_0x0068;
    L_0x0065:
        r6.close();
    L_0x0068:
        return r10;
    L_0x0069:
        r1 = move-exception;
        if (r6 == 0) goto L_0x006f;
    L_0x006c:
        r6.close();
    L_0x006f:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBAdapter.isCallContact(android.content.Context, java.lang.String):boolean");
    }

    public static boolean isContact(android.content.Context r11, java.lang.String r12) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x005b in list []
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
        r10 = 0;
        r8 = android.telephony.PhoneNumberUtils.normalizeNumber(r12);
        r0 = android.text.TextUtils.isEmpty(r8);
        if (r0 == 0) goto L_0x0015;
    L_0x000b:
        r0 = "HarassmentInterceptionDBAdapter";
        r2 = "isContact: Invalid phone number";
        com.huawei.systemmanager.util.HwLog.w(r0, r2);
        return r10;
    L_0x0015:
        r0 = "#";
        r8 = android.net.Uri.encode(r8, r0);
        r0 = android.provider.ContactsContract.PhoneLookup.CONTENT_FILTER_URI;
        r9 = r0.buildUpon();
        r9.appendPath(r8);
        r1 = r9.build();
        r6 = 0;
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r2 = PHONE_LOOKUP_PROJECTION;	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r3 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r4 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r5 = 0;	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        if (r6 == 0) goto L_0x0046;	 Catch:{ Exception -> 0x004c, all -> 0x005c }
    L_0x0039:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        if (r0 <= 0) goto L_0x0046;
    L_0x003f:
        r0 = 1;
        if (r6 == 0) goto L_0x0045;
    L_0x0042:
        r6.close();
    L_0x0045:
        return r0;
    L_0x0046:
        if (r6 == 0) goto L_0x004b;
    L_0x0048:
        r6.close();
    L_0x004b:
        return r10;
    L_0x004c:
        r7 = move-exception;
        r0 = "HarassmentInterceptionDBAdapter";	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        r2 = "isContact: Exception";	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        com.huawei.systemmanager.util.HwLog.e(r0, r2, r7);	 Catch:{ Exception -> 0x004c, all -> 0x005c }
        if (r6 == 0) goto L_0x005b;
    L_0x0058:
        r6.close();
    L_0x005b:
        return r10;
    L_0x005c:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0062;
    L_0x005f:
        r6.close();
    L_0x0062:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBAdapter.isContact(android.content.Context, java.lang.String):boolean");
    }

    private static boolean isKeywordsExist(android.content.Context r10, java.lang.String r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x003f in list []
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
        r9 = 1;
        r8 = 0;
        r2 = new java.lang.String[r9];
        r0 = "_id";
        r2[r8] = r0;
        r3 = "keyword=?";
        r4 = new java.lang.String[r9];
        r4[r8] = r11;
        r6 = 0;
        r0 = r10.getContentResolver();	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r1 = keywords_uri;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r5 = 0;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r6 == 0) goto L_0x002a;	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
    L_0x001e:
        r0 = r6.getCount();	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r0 <= 0) goto L_0x002a;
    L_0x0024:
        if (r6 == 0) goto L_0x0029;
    L_0x0026:
        r6.close();
    L_0x0029:
        return r9;
    L_0x002a:
        if (r6 == 0) goto L_0x002f;
    L_0x002c:
        r6.close();
    L_0x002f:
        return r8;
    L_0x0030:
        r7 = move-exception;
        r0 = "HarassmentInterceptionDBAdapter";	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        r1 = "isKeywordsExist: Exception";	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1, r7);	 Catch:{ Exception -> 0x0030, all -> 0x0040 }
        if (r6 == 0) goto L_0x003f;
    L_0x003c:
        r6.close();
    L_0x003f:
        return r8;
    L_0x0040:
        r0 = move-exception;
        if (r6 == 0) goto L_0x0046;
    L_0x0043:
        r6.close();
    L_0x0046:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBAdapter.isKeywordsExist(android.content.Context, java.lang.String):boolean");
    }

    public static boolean isSmsContact(android.content.Context r12, java.lang.String r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0080 in list []
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
        r11 = 1;
        r10 = 0;
        r8 = com.huawei.systemmanager.util.phonematch.PhoneMatch.getPhoneNumberMatchInfo(r13);
        r1 = r8.getPhoneNumber();
        r1 = android.text.TextUtils.isEmpty(r1);
        if (r1 == 0) goto L_0x001a;
    L_0x0010:
        r1 = "HarassmentInterceptionDBAdapter";
        r2 = "isSmsContact: Invalid number";
        com.huawei.systemmanager.util.HwLog.w(r1, r2);
        return r10;
    L_0x001a:
        r0 = r12.getContentResolver();
        r6 = 0;
        r3 = 0;
        r4 = 0;
        r1 = r8.isExactMatch();
        if (r1 == 0) goto L_0x004b;
    L_0x0027:
        r3 = "type = 2 AND address = ?";
        r4 = new java.lang.String[r11];
        r1 = r8.getPhoneNumber();
        r4[r10] = r1;
    L_0x0032:
        r1 = android.provider.Telephony.Sms.CONTENT_URI;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r2 = 1;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r2 = new java.lang.String[r2];	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r5 = "_id";	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r9 = 0;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r2[r9] = r5;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r5 = 0;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r1 = 1;	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r1 = com.huawei.systemmanager.comm.misc.Utility.isNullOrEmptyCursor(r6, r1);	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        if (r1 == 0) goto L_0x006b;
    L_0x0049:
        r6 = 0;
        return r10;
    L_0x004b:
        r3 = "type = 2 AND address like ?";
        r4 = new java.lang.String[r11];
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "%";
        r1 = r1.append(r2);
        r2 = r8.getPhoneNumber();
        r1 = r1.append(r2);
        r1 = r1.toString();
        r4[r10] = r1;
        goto L_0x0032;
    L_0x006b:
        if (r6 == 0) goto L_0x0070;
    L_0x006d:
        r6.close();
    L_0x0070:
        return r11;
    L_0x0071:
        r7 = move-exception;
        r1 = "HarassmentInterceptionDBAdapter";	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        r2 = "isSmsContact: Exception";	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        com.huawei.systemmanager.util.HwLog.e(r1, r2, r7);	 Catch:{ Exception -> 0x0071, all -> 0x0081 }
        if (r6 == 0) goto L_0x0080;
    L_0x007d:
        r6.close();
    L_0x0080:
        return r10;
    L_0x0081:
        r1 = move-exception;
        if (r6 == 0) goto L_0x0087;
    L_0x0084:
        r6.close();
    L_0x0087:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.db.DBAdapter.isSmsContact(android.content.Context, java.lang.String):boolean");
    }

    static {
        CONTACT_COLUMN_MAP.put(messages_uri, new ContactQueryColumn("_id", "name", "phone"));
        CONTACT_COLUMN_MAP.put(calls_uri, new ContactQueryColumn("_id", "name", "phone"));
        CONTACT_COLUMN_MAP.put(blacklist_uri, new ContactQueryColumn("_id", "name", "phone"));
        CONTACT_COLUMN_MAP.put(whitelist_uri, new BlacklistContactQueryColumn("_id", "name", "phone"));
    }

    public static RcsDBAdapter getRcs() {
        return mRcs;
    }

    public static List<MessageInfo> getInterceptedMsgs(Context context) {
        Cursor cursor = context.getContentResolver().query(messages_view_uri, new String[]{"_id", "phone", "name", TbMessages.BODY, "date", "sub_id", TbMessages.EXPDATE, "size", TbMessages.PDU, "type", "block_reason", "location", TbNumberLocation.OPERATOR}, null, null, "date desc");
        List<MessageInfo> messagelist = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return messagelist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexBody = cursor.getColumnIndex(TbMessages.BODY);
        int nColIndexDate = cursor.getColumnIndex("date");
        int nColIndexSubId = cursor.getColumnIndex("sub_id");
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        int nColIndexExpdate = cursor.getColumnIndex(TbMessages.EXPDATE);
        int nColIndexSize = cursor.getColumnIndex("size");
        int nColIndexPdu = cursor.getColumnIndex(TbMessages.PDU);
        int nColIndexType = cursor.getColumnIndex("type");
        int nColIndexBlockReason = cursor.getColumnIndex("block_reason");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(nColIndexID);
            String phone = cursor.getString(nColIndexPhone);
            String name = cursor.getString(nColIndexName);
            String body = cursor.getString(nColIndexBody);
            long date = cursor.getLong(nColIndexDate);
            int subId = cursor.getInt(nColIndexSubId);
            String location = cursor.getString(nColIndexLocation);
            String operator = cursor.getString(nColIndexOperator);
            long expdate = cursor.getLong(nColIndexExpdate);
            long size = cursor.getLong(nColIndexSize);
            byte[] pdu = cursor.getBlob(nColIndexPdu);
            int msgType = cursor.getInt(nColIndexType);
            int blockReason = cursor.getInt(nColIndexBlockReason);
            MessageInfo messageInfoItem = new MessageInfo(id, phone, name, body, size, date, expdate, subId, pdu, new NumberLocationInfo(location, operator), msgType);
            messageInfoItem.setBlockReason(blockReason);
            messagelist.add(messageInfoItem);
        }
        cursor.close();
        return messagelist;
    }

    public static List<MessageInfo> getInterceptedMsgs(Context context, String phoneNumber) {
        return getInterceptedMsgs(context, phoneNumber, 0);
    }

    public static List<MessageInfo> getInterceptedMsgsByFuzzyPhone(Context context, String phoneNumber) {
        return getInterceptedMsgsByFuzzyPhone(context, phoneNumber, 0);
    }

    private static String getSelection() {
        StringBuilder selection = new StringBuilder();
        selection.append("phone");
        selection.append(" like ? OR ");
        selection.append("phone");
        selection.append(" like ?");
        for (int temp = 0; temp < CommonHelper.getRCSCountryCode().length; temp++) {
            selection.append(" OR ");
            selection.append("phone");
            selection.append(" like ?");
        }
        return selection.toString();
    }

    private static String[] getSelectionArg(String phoneNumber) {
        String phoneWithoutCountryCode = CommonHelper.trimPhoneCountryCode(phoneNumber);
        String phoneWithCountryCode = ConstValues.PHONE_COUNTRY_CODE_CHINA + phoneWithoutCountryCode;
        StringBuilder phoneWithRcsCode = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        selectionArgs.add(phoneWithCountryCode + "%");
        selectionArgs.add(phoneWithoutCountryCode + "%");
        for (String prefix : CommonHelper.getRCSCountryCode()) {
            phoneWithRcsCode.append(prefix);
            phoneWithRcsCode.append(phoneWithoutCountryCode);
            phoneWithRcsCode.append("%");
            selectionArgs.add(phoneWithRcsCode.toString());
        }
        return (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
    }

    public static List<MessageInfo> getInterceptedMsgs(Context context, String phoneNumber, int type) {
        List<MessageInfo> messagelist = new ArrayList();
        if (TextUtils.isEmpty(formatPhoneNumber(phoneNumber))) {
            HwLog.w(TAG, "getInterceptedMsgs: Invalid phone number");
            return messagelist;
        }
        String selection;
        String[] selectionArgs;
        if (type == 0) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phoneNumber);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
        } else {
            selection = getSelection();
            selectionArgs = getSelectionArg(phoneNumber);
        }
        Cursor cursor = context.getContentResolver().query(messages_view_uri, new String[]{"_id", "phone", "name", TbMessages.BODY, "date", "sub_id", TbMessages.EXPDATE, "size", TbMessages.PDU, "type", "location", TbNumberLocation.OPERATOR}, selection, selectionArgs, "date desc");
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return messagelist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexBody = cursor.getColumnIndex(TbMessages.BODY);
        int nColIndexDate = cursor.getColumnIndex("date");
        int nColIndexSubId = cursor.getColumnIndex("sub_id");
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        int nColIndexExpdate = cursor.getColumnIndex(TbMessages.EXPDATE);
        int nColIndexSize = cursor.getColumnIndex("size");
        int nColIndexPdu = cursor.getColumnIndex(TbMessages.PDU);
        int nColIndexType = cursor.getColumnIndex("type");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(nColIndexID);
            String phone = cursor.getString(nColIndexPhone);
            String name = cursor.getString(nColIndexName);
            String body = cursor.getString(nColIndexBody);
            long date = cursor.getLong(nColIndexDate);
            int subId = cursor.getInt(nColIndexSubId);
            String location = cursor.getString(nColIndexLocation);
            String operator = cursor.getString(nColIndexOperator);
            long expdate = cursor.getLong(nColIndexExpdate);
            messagelist.add(new MessageInfo(id, phone, name, body, cursor.getLong(nColIndexSize), date, expdate, subId, cursor.getBlob(nColIndexPdu), new NumberLocationInfo(location, operator), cursor.getInt(nColIndexType)));
        }
        cursor.close();
        return messagelist;
    }

    public static List<MessageInfo> getInterceptedMsgsByFuzzyPhone(Context context, String phoneNumber, int type) {
        List<MessageInfo> messagelist = new ArrayList();
        String formatedNumber = formatPhoneNumber(phoneNumber);
        if (TextUtils.isEmpty(formatedNumber)) {
            HwLog.w(TAG, "getInterceptedMsgs: Invalid phone number");
            return messagelist;
        }
        String selection;
        String[] selectionArgs;
        boolean isHotlineNumber = false;
        if (type == 0) {
            String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, formatedNumber);
            if (TextUtils.isEmpty(hotlineNumber)) {
                PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phoneNumber);
                selection = phoneMatchInfo.getSqlSelectionStatement("phone");
                selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
            } else {
                isHotlineNumber = true;
                selection = "phone like ?";
                selectionArgs = HotlineNumberHelper.getSqlSelectionFuzzyArgs(hotlineNumber);
            }
        } else {
            String phoneWithCountryCode = ConstValues.PHONE_COUNTRY_CODE_CHINA + CommonHelper.trimPhoneCountryCode(formatedNumber);
            selection = "phone like ? OR phone like ?";
            selectionArgs = new String[]{phoneWithCountryCode + "%", phoneWithoutCountryCode + "%"};
        }
        Cursor cursor = context.getContentResolver().query(messages_view_uri, new String[]{"_id", "phone", "name", TbMessages.BODY, "date", "sub_id", TbMessages.EXPDATE, "size", TbMessages.PDU, "type", "location", TbNumberLocation.OPERATOR}, selection, selectionArgs, "date desc");
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return messagelist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexBody = cursor.getColumnIndex(TbMessages.BODY);
        int nColIndexDate = cursor.getColumnIndex("date");
        int nColIndexSubId = cursor.getColumnIndex("sub_id");
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        int nColIndexExpdate = cursor.getColumnIndex(TbMessages.EXPDATE);
        int nColIndexSize = cursor.getColumnIndex("size");
        int nColIndexPdu = cursor.getColumnIndex(TbMessages.PDU);
        int nColIndexType = cursor.getColumnIndex("type");
        while (cursor.moveToNext()) {
            int id = cursor.getInt(nColIndexID);
            String phone = cursor.getString(nColIndexPhone);
            String name = cursor.getString(nColIndexName);
            String body = cursor.getString(nColIndexBody);
            long date = cursor.getLong(nColIndexDate);
            int subId = cursor.getInt(nColIndexSubId);
            String location = cursor.getString(nColIndexLocation);
            String operator = cursor.getString(nColIndexOperator);
            long expdate = cursor.getLong(nColIndexExpdate);
            long size = cursor.getLong(nColIndexSize);
            byte[] pdu = cursor.getBlob(nColIndexPdu);
            int msgType = cursor.getInt(nColIndexType);
            if (!isHotlineNumber || !TextUtils.isEmpty(HotlineNumberHelper.getHotlineNumber(context, phone))) {
                messagelist.add(new MessageInfo(id, phone, name, body, size, date, expdate, subId, pdu, new NumberLocationInfo(location, operator), msgType));
            }
        }
        cursor.close();
        return messagelist;
    }

    public static List<CallInfo> getInterceptedCalls(Context context) {
        Cursor cursor = context.getContentResolver().query(calls_view_uri, new String[]{"_id", "phone", "name", "date", "sub_id", "block_reason", TbCalls.BLOCK_TYPE, TbCalls.MARK_COUNT, "location", TbNumberLocation.OPERATOR}, null, null, "date desc");
        List<CallInfo> callList = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return callList;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexDate = cursor.getColumnIndex("date");
        int nSubId = cursor.getColumnIndex("sub_id");
        int nBlockReason = cursor.getColumnIndex("block_reason");
        int nBlockType = cursor.getColumnIndex(TbCalls.BLOCK_TYPE);
        int nMarkCount = cursor.getColumnIndex(TbCalls.MARK_COUNT);
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        while (cursor.moveToNext()) {
            CallInfo callInfo = new CallInfo(cursor.getInt(nColIndexID), cursor.getString(nColIndexPhone), cursor.getString(nColIndexName), cursor.getLong(nColIndexDate), new NumberLocationInfo(cursor.getString(nColIndexLocation), cursor.getString(nColIndexOperator)));
            int subId = cursor.getInt(nSubId);
            BlockReason blockReason = new BlockReason(cursor.getInt(nBlockReason), cursor.getInt(nBlockType), cursor.getInt(nMarkCount));
            callInfo.setSubId(subId);
            callInfo.setBlockReason(blockReason);
            callList.add(callInfo);
        }
        cursor.close();
        return callList;
    }

    public static List<BlacklistInfo> getBlacklist(Context context) {
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(blacklist_view_uri, new String[]{"_id", "phone", "name", TbBlacklist.INTERCEPTED_CALL_COUNT, TbBlacklist.INTERCEPTED_MSG_COUNT, "location", TbNumberLocation.OPERATOR, "option", "type"}, null, null, null);
        } catch (Throwable e) {
            HwLog.e(TAG, "getBlacklist RuntimeException", e);
        }
        List<BlacklistInfo> blacklist = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return blacklist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexCallCount = cursor.getColumnIndex(TbBlacklist.INTERCEPTED_CALL_COUNT);
        int nColIndexMsgCount = cursor.getColumnIndex(TbBlacklist.INTERCEPTED_MSG_COUNT);
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        int nColIndexOption = cursor.getColumnIndex("option");
        int nColIndexType = cursor.getColumnIndex("type");
        while (cursor.moveToNext()) {
            blacklist.add(new BlacklistInfo(cursor.getInt(nColIndexID), cursor.getString(nColIndexPhone), cursor.getString(nColIndexName), cursor.getInt(nColIndexCallCount), cursor.getInt(nColIndexMsgCount), cursor.getInt(nColIndexOption), cursor.getInt(nColIndexType), new NumberLocationInfo(cursor.getString(nColIndexLocation), cursor.getString(nColIndexOperator))));
        }
        cursor.close();
        return blacklist;
    }

    public static int getBlackListCount(Context ctx) {
        Closeable closeable = null;
        try {
            closeable = ctx.getContentResolver().query(blacklist_view_uri, new String[]{"_id"}, null, null, null);
            if (closeable == null) {
                HwLog.e(TAG, "getBlackListCount cursor is null!");
                return 0;
            }
            int count = closeable.getCount();
            Closeables.close(closeable);
            return count;
        } catch (Exception e) {
            HwLog.e(TAG, "getBlackListCount", e);
            return 0;
        } finally {
            Closeables.close(closeable);
        }
    }

    public static List<WhitelistInfo> getWhitelist(Context context) {
        Cursor cursor = context.getContentResolver().query(whitelist_view_uri, new String[]{"_id", "phone", "name", "location", TbNumberLocation.OPERATOR}, null, null, null);
        List<WhitelistInfo> whitelist = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return whitelist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexName = cursor.getColumnIndex("name");
        int nColIndexLocation = cursor.getColumnIndex("location");
        int nColIndexOperator = cursor.getColumnIndex(TbNumberLocation.OPERATOR);
        while (cursor.moveToNext()) {
            int id = cursor.getInt(nColIndexID);
            String phone = cursor.getString(nColIndexPhone);
            whitelist.add(new WhitelistInfo(id, phone, cursor.getString(nColIndexName), new NumberLocationInfo(cursor.getString(nColIndexLocation), cursor.getString(nColIndexOperator))));
        }
        cursor.close();
        return whitelist;
    }

    public static int getWhiteListCount(Context context) {
        Closeable closeable = null;
        try {
            closeable = context.getContentResolver().query(whitelist_view_uri, new String[]{"_id"}, null, null, null);
            if (closeable == null) {
                HwLog.e(TAG, "getBlackListCount cursor is null!");
                return 0;
            }
            int count = closeable.getCount();
            Closeables.close(closeable);
            return count;
        } catch (Exception e) {
            HwLog.e(TAG, "getWhiteListCount", e);
            return 0;
        } finally {
            Closeables.close(closeable);
        }
    }

    public static List<String> getBlacklistedPhones(Context context) {
        return getPhoneListFromUri(context, blacklist_uri);
    }

    public static List<String> getWhitelistPhones(Context context) {
        return getPhoneListFromUri(context, whitelist_uri);
    }

    private static List<String> getPhoneListFromUri(Context context, Uri uri) {
        String[] projection = new String[]{"phone"};
        String[] selectionArgs = new String[]{String.valueOf(0)};
        Cursor cursor = context.getContentResolver().query(uri, projection, "type=?", selectionArgs, null);
        List<String> phoneList = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return phoneList;
        }
        int nColIndexPhone = cursor.getColumnIndex("phone");
        while (cursor.moveToNext()) {
            phoneList.add(cursor.getString(nColIndexPhone));
        }
        cursor.close();
        return phoneList;
    }

    public static int checkBlackAndWhiteListOption(Context context, String phone, int checkOption) {
        String formatedPhone = formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            HwLog.w(TAG, "checkBlackAndWhiteListOption: Invalid phone number");
            return -1;
        } else if (isWhitelisted(context, formatedPhone)) {
            HwLog.w(TAG, "checkBlackAndWhiteListOption: Whitelist phone number");
            return -1;
        } else {
            int nMatchedBlOpt = getMatchedBlacklistOption(context, formatedPhone);
            if (3 == nMatchedBlOpt) {
                return 3;
            }
            int nMatchedBlHeaderOpt = getMatchedBlacklistHeaderOption(context, formatedPhone);
            if (3 == nMatchedBlHeaderOpt) {
                return 3;
            }
            if (-1 == nMatchedBlOpt) {
                return nMatchedBlHeaderOpt;
            }
            if (-1 == nMatchedBlHeaderOpt) {
                return nMatchedBlOpt;
            }
            return nMatchedBlOpt | nMatchedBlHeaderOpt;
        }
    }

    private static int getMatchedBlacklistOption(Context context, String phone) {
        String selection;
        String[] selectionArgs;
        String[] projection = new String[]{"option"};
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phone);
        if (TextUtils.isEmpty(hotlineNumber)) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone", "type");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs(String.valueOf(0));
        } else {
            selection = HotlineNumberHelper.getSqlSelectionStatement("phone", "type");
            selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber, String.valueOf(0));
        }
        Cursor cursor = context.getContentResolver().query(blacklist_view_uri, projection, selection, selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return -1;
        }
        if (cursor.moveToNext()) {
            int nBlOption = cursor.getInt(0);
            cursor.close();
            return nBlOption;
        }
        cursor.close();
        return -1;
    }

    private static int getMatchedBlacklistHeaderOption(Context context, String phone) {
        List<BlacklistInfo> blacklistHeader = getMatchedBlacklistHeaders(context, phone);
        if (Utility.isNullOrEmptyList(blacklistHeader)) {
            return -1;
        }
        int nMatchOption = 0;
        for (BlacklistInfo header : blacklistHeader) {
            if (header.isMatchHeader(phone)) {
                nMatchOption |= header.getOption();
                if (3 == (nMatchOption & 3)) {
                    break;
                }
            }
        }
        return nMatchOption;
    }

    public static int checkMatchBlacklist(Context context, String phone, int checkOption) {
        String formatedPhone = formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            HwLog.w(TAG, "checkMatchBlacklist: Invalid phone number");
            return -1;
        } else if (checkOption == 0) {
            return isBlacklisted(context, phone);
        } else {
            if (checkMatchedBlacklistOption(context, formatedPhone, checkOption) == 0) {
                return 0;
            }
            return checkMatchedBlacklistHeaderOption(context, formatedPhone, checkOption);
        }
    }

    private static int checkMatchedBlacklistOption(Context context, String phone, int checkOption) {
        int nBlOption = getMatchedBlacklistOption(context, phone);
        return (-1 == nBlOption || (nBlOption & checkOption) == 0) ? -1 : 0;
    }

    private static int checkMatchedBlacklistHeaderOption(Context context, String phone, int checkOption) {
        List<BlacklistInfo> blacklistHeader = getMatchedBlacklistHeaders(context, phone);
        if (Utility.isNullOrEmptyList(blacklistHeader)) {
            return -1;
        }
        for (BlacklistInfo header : blacklistHeader) {
            if (header.isMatchHeader(phone) && header.isMatchOption(checkOption)) {
                return 0;
            }
        }
        return -1;
    }

    public static int isBlacklisted(Context context, String phone) {
        if (TextUtils.isEmpty(phone)) {
            HwLog.w(TAG, "isBlacklisted: Invalid phone number");
            return -1;
        }
        String selection;
        String[] selectionArgs;
        String[] projection = new String[]{"_id"};
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phone);
        if (TextUtils.isEmpty(hotlineNumber)) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone", "type");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs(String.valueOf(0));
        } else {
            selection = HotlineNumberHelper.getSqlSelectionStatement("phone", "type");
            selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber, String.valueOf(0));
        }
        Cursor cursor = context.getContentResolver().query(blacklist_view_uri, projection, selection, selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return -1;
        }
        cursor.close();
        return 0;
    }

    public static boolean isWhitelisted(Context context, String phone) {
        if (!TextUtils.isEmpty(phone)) {
            return isPhoneExist(context, phone, whitelist_uri);
        }
        HwLog.w(TAG, "isWhitelisted: Invalid phone number");
        return false;
    }

    public static boolean isNumberFromCloud(Context context, String phone) {
        if (!TextUtils.isEmpty(phone)) {
            return isPhoneExist(context, phone, cloud_permission_uri, "packageName");
        }
        HwLog.w(TAG, "isNumberFromCloud: Invalid phone number");
        return false;
    }

    public static boolean isNumberMatch(Context context, String phone) {
        String number = PhoneMatch.getPhoneNumberMatchInfo(phone).getPhoneNumber();
        String[] projection = new String[]{"packageName"};
        String[] selectionArgs = new String[]{"%d+"};
        boolean isNumMatch = false;
        Cursor cursor = context.getContentResolver().query(cloud_permission_uri, projection, "packageName like ?", selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return false;
        }
        int nPhoneColumnIndex = cursor.getColumnIndex("packageName");
        while (cursor.moveToNext()) {
            if (Pattern.matches(cursor.getString(nPhoneColumnIndex), number)) {
                isNumMatch = true;
                break;
            }
        }
        cursor.close();
        return isNumMatch;
    }

    private static boolean isPhoneExist(Context context, String phone, Uri uri) {
        PhoneMatch.outputPhoneMathConfig();
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phone);
        if (TextUtils.isEmpty(hotlineNumber)) {
            return isPhoneExist(context, phone, uri, "phone");
        }
        return isHotlinePhoneExist(context, uri, hotlineNumber, "phone");
    }

    private static boolean isPhoneExist(Context context, String phone, Uri uri, String columnName) {
        PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
        Cursor cursor = context.getContentResolver().query(uri, new String[]{columnName}, phoneMatchInfo.getSqlSelectionStatement(columnName), phoneMatchInfo.getSqlSelectionArgs(), null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return false;
        }
        cursor.close();
        return true;
    }

    private static boolean isHotlinePhoneExist(Context context, Uri uri, String hotlineNumber, String columnName) {
        if (TextUtils.isEmpty(hotlineNumber)) {
            return false;
        }
        Cursor cursor = context.getContentResolver().query(uri, new String[]{columnName}, columnName + " = ?", new String[]{hotlineNumber}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return false;
        }
        cursor.close();
        return true;
    }

    public static String getNameFromBlacklist(Context context, String phone) {
        if (TextUtils.isEmpty(phone)) {
            HwLog.w(TAG, "getNameFromBlacklist: Invalid phone number");
            return "";
        }
        String[] projection = new String[]{"name"};
        String name = "";
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phone);
        if (TextUtils.isEmpty(hotlineNumber)) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            Cursor cursor = context.getContentResolver().query(blacklist_uri, projection, phoneMatchInfo.getSqlSelectionStatement("phone"), phoneMatchInfo.getSqlSelectionArgs(), null);
            if (!Utility.isNullOrEmptyCursor(cursor, true)) {
                cursor.moveToNext();
                name = cursor.getString(cursor.getColumnIndex("name"));
                cursor.close();
            }
            return name;
        }
        String hotlineName = HotlineNumberHelper.getHotlineNumberName(context, hotlineNumber);
        if (hotlineName != null) {
            hotlineName = name;
        }
        return hotlineName;
    }

    public static int getRuleState(Context context, String ruleName) {
        String[] projection = new String[]{"state"};
        Cursor cursor = context.getContentResolver().query(rules_uri, projection, "name=?", new String[]{ruleName}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return 1;
        }
        cursor.moveToNext();
        int state = cursor.getInt(cursor.getColumnIndex("state"));
        cursor.close();
        return state;
    }

    public static List<ContactInfo> getContactInfoFromBlackList(Context context) {
        return getContactInfoFromUri(context, blacklist_uri);
    }

    public static List<ContactInfo> getContactInfoFromInterceptedCalls(Context context) {
        return getContactInfoFromUri(context, calls_uri);
    }

    public static List<ContactInfo> getContactInfoFromInterceptedMsgs(Context context) {
        return getContactInfoFromUri(context, messages_uri);
    }

    public static List<ContactInfo> getContactInfoFromWhitelist(Context context) {
        return getContactInfoFromUri(context, whitelist_uri);
    }

    private static List<ContactInfo> getContactInfoFromUri(Context context, Uri uri) {
        ContactQueryColumn columnInfo = (ContactQueryColumn) CONTACT_COLUMN_MAP.get(uri);
        if (columnInfo == null) {
            HwLog.w(TAG, "getContactInfoFromUri: Fail to get contact column info for uri: " + uri);
            return null;
        }
        try {
            Cursor cursor = context.getContentResolver().query(uri, columnInfo.getSqlQueryProjection(), columnInfo.getQuerySelection(), columnInfo.getQuerySelectionArgs(), null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                return null;
            }
            List<ContactInfo> contactInfoList = new ArrayList();
            int nIdColumnIndex = columnInfo.getIdColIndexFromCursor(cursor);
            int nNameColumnIndex = columnInfo.getNameColIndexFromCursor(cursor);
            int nPhoneColumnIndex = columnInfo.getPhoneColIndexFromCursor(cursor);
            while (cursor.moveToNext()) {
                contactInfoList.add(new ContactInfo(cursor.getInt(nIdColumnIndex), cursor.getString(nPhoneColumnIndex), cursor.getString(nNameColumnIndex)));
            }
            cursor.close();
            return contactInfoList;
        } catch (Exception e) {
            HwLog.i(TAG, "getContactInfoFromUri ,Exception , Uri = " + uri.toString(), e);
            return null;
        }
    }

    public static int updateContactInfoInBlackList(Context context, List<ContactInfo> contactInfoList) {
        return updateContactInfoByUri(context, blacklist_uri, contactInfoList);
    }

    public static int updateContactInfoInInterceptedCalls(Context context, List<ContactInfo> contactInfoList) {
        return updateContactInfoByUri(context, calls_uri, contactInfoList);
    }

    public static int updateContactInfoInInterceptedMsgs(Context context, List<ContactInfo> contactInfoList) {
        return updateContactInfoByUri(context, messages_uri, contactInfoList);
    }

    public static int updateContactInfoInWhitelist(Context context, List<ContactInfo> contactInfoList) {
        return updateContactInfoByUri(context, whitelist_uri, contactInfoList);
    }

    private static int updateContactInfoByUri(Context context, Uri uri, List<ContactInfo> contactInfoList) {
        ContactQueryColumn columnInfo = (ContactQueryColumn) CONTACT_COLUMN_MAP.get(uri);
        if (columnInfo == null) {
            HwLog.w(TAG, "updateContactInfoByUri: Fail to get contact column info for uri: " + uri);
            return -1;
        }
        int nUpdateCount = 0;
        for (ContactInfo contactInfo : contactInfoList) {
            try {
                nUpdateCount += context.getContentResolver().update(uri, columnInfo.getSqlUpdateValues(contactInfo), columnInfo.getSqlUpdateSelection(), columnInfo.getSqlUpdateAgrs(contactInfo));
            } catch (Exception e) {
                HwLog.e(TAG, "updateContactInfoByUri ,Exception, Uri = " + uri.toString(), e);
                return -1;
            }
        }
        return nUpdateCount;
    }

    public static void addInterceptedMsg(Context context, SmsMsgInfo interceptedMsg) {
        context.getContentResolver().insert(messages_uri, interceptedMsg.getAsContentValues());
        updateBlacklistStatInfo(context, interceptedMsg.getPhone(), 0);
    }

    public static void addInterceptedMsg(Context context, MessageInfo interceptedMsg) {
        context.getContentResolver().insert(messages_uri, interceptedMsg.getAsContentValues());
        updateBlacklistStatInfo(context, interceptedMsg.getPhone(), 0);
    }

    public static void addInterceptedCalls(Context context, CallInfo interceptedCalls) {
        context.getContentResolver().insert(calls_uri, interceptedCalls.getAsContentValues());
        updateBlacklistStatInfo(context, interceptedCalls.getPhone(), 1);
    }

    public static int addBlacklist(Context context, String phone, String name, int option) {
        return addBlacklistEx(context, phone, name, option, 0)[0];
    }

    public static int addBlacklistHeader(Context context, String phone, String name, int option) {
        return addBlacklistEx(context, phone, name, option, 1)[0];
    }

    public static int[] addBlacklistEx(Context context, String phone, String name, int option, int type) {
        int[] result = new int[]{-1, 0, 0, 0};
        String formatedPhone = formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            HwLog.w(TAG, "addBlacklistEx : get invalid phone number by format method");
            return result;
        }
        String selection;
        String[] selectionArgs;
        if (type == 0) {
            String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, formatedPhone);
            if (TextUtils.isEmpty(hotlineNumber)) {
                PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(formatedPhone);
                PhoneMatchInfo phoneMatchInfo2 = phoneMatchInfo;
                selection = phoneMatchInfo2.getSqlSelectionStatement("phone", "type");
                selectionArgs = phoneMatchInfo.getSqlSelectionArgs(String.valueOf(type));
            } else {
                selection = HotlineNumberHelper.getSqlSelectionStatement("phone", "type");
                selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber, String.valueOf(type));
            }
        } else {
            selection = "phone=? AND type=?";
            selectionArgs = new String[]{formatedPhone, String.valueOf(type)};
        }
        option = CommonHelper.calibrateBlacklistOption(option);
        Cursor cursor = context.getContentResolver().query(blacklist_uri, new String[]{"_id", "option"}, selection, selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            HwLog.i(TAG, "addBlacklistEx: Add new");
            return addBlacklistInner(context, formatedPhone, name, option, type);
        } else if (cursor.moveToNext()) {
            int currentOption = cursor.getInt(cursor.getColumnIndex("option"));
            int id = cursor.getInt(cursor.getColumnIndex("_id"));
            cursor.close();
            if (currentOption == option) {
                HwLog.i(TAG, "addBlacklistEx: Already exists");
                return result;
            }
            int updated = updateBlackListOption(context, formatedPhone, option, type);
            HwLog.i(TAG, "addBlacklistEx: Update existed, updated = " + updated);
            if (updated <= 0) {
                return result;
            }
            int[] count = getInterceptedCallAndMsgCount(context, formatedPhone, type);
            result[0] = id;
            result[1] = count[0];
            result[2] = count[1];
            result[3] = 1;
            return result;
        } else {
            HwLog.i(TAG, "addBlacklistEx: Fail to read db ,skip.");
            cursor.close();
            return result;
        }
    }

    public static int addPhonesToBlacklist(Context context, List<String> plist) {
        return addPhonesToBlacklist(context, plist, 3);
    }

    public static int addPhonesToBlacklist(Context context, List<String> plist, int option) {
        if (Utility.isNullOrEmptyList(plist)) {
            HwLog.w(TAG, "addPhonesToBlacklist: Invalid list");
            return 0;
        }
        int insertCount = 0;
        for (int i = plist.size() - 1; i >= 0; i--) {
            String phone = (String) plist.get(i);
            if ((!isWhitelisted(context, phone) || deleteWhitelist(context, phone) > 0) && addBlacklist(context, phone, "", option) >= 0) {
                insertCount++;
            }
        }
        return insertCount;
    }

    public static int addContactsToBlacklist(Context context, List<ContactInfo> plist) {
        return addContactsToBlacklist(context, plist, 3);
    }

    public static int addContactsToBlacklist(Context context, List<ContactInfo> plist, int option) {
        if (Utility.isNullOrEmptyList(plist)) {
            HwLog.w(TAG, "addContactsToBlacklist: Invalid list");
            return 0;
        }
        int insertCount = 0;
        for (int i = plist.size() - 1; i >= 0; i--) {
            ContactInfo contact = (ContactInfo) plist.get(i);
            String phone = contact.getPhone();
            if ((!isWhitelisted(context, phone) || deleteWhitelist(context, phone) > 0) && addBlacklist(context, contact.getPhone(), contact.getName(), option) >= 0) {
                insertCount++;
            }
        }
        return insertCount;
    }

    private static int[] addBlacklistInner(Context context, String formatedPhone, String name, int option, int type) {
        int[] result = new int[]{-1, 0, 0, 0};
        ContentValues values = new ContentValues();
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, formatedPhone);
        String number = formatedPhone;
        if (!TextUtils.isEmpty(hotlineNumber)) {
            name = HotlineNumberHelper.getHotlineNumberName(context, hotlineNumber);
            number = hotlineNumber;
        }
        int[] count = getInterceptedCallAndMsgCount(context, number, type);
        values.put("phone", number);
        values.put("name", name);
        values.put("option", Integer.valueOf(option));
        values.put("type", Integer.valueOf(type));
        values.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(count[0]));
        values.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(count[1]));
        Uri retUri = context.getContentResolver().insert(blacklist_uri, values);
        if (GoogleBlackListContract.isGoogleBlockNumberType(type)) {
            GoogleBlackListContract.addBlockedNumber(number);
        }
        if (retUri == null || retUri.toString().equals(blacklist_uri.toString())) {
            HwLog.w(TAG, "addBlacklistInner: Fails");
            return result;
        }
        result[0] = (int) ContentUris.parseId(retUri);
        result[1] = count[0];
        result[2] = count[1];
        return result;
    }

    public static int updateBlackListOption(Context context, int id, int option) {
        option = CommonHelper.calibrateBlacklistOption(option);
        ContentValues values = new ContentValues();
        values.put("option", Integer.valueOf(option));
        String[] selectionArgs = new String[]{String.valueOf(id)};
        return context.getContentResolver().update(blacklist_uri, values, "_id=?", selectionArgs);
    }

    public static int updateBlackListOption(Context context, String phone, int option, int type) {
        String formatedPhone = formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            HwLog.w(TAG, "updateBlackListOption : get invalid phone number by format method");
            return 0;
        }
        option = CommonHelper.calibrateBlacklistOption(option);
        ContentValues values = new ContentValues();
        values.put("option", Integer.valueOf(option));
        values.put("phone", formatedPhone);
        String[] selectionArgs;
        if (type == 0) {
            String selection;
            String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, formatedPhone);
            if (TextUtils.isEmpty(hotlineNumber)) {
                PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(formatedPhone);
                selection = phoneMatchInfo.getSqlSelectionStatement("phone", "type");
                selectionArgs = phoneMatchInfo.getSqlSelectionArgs(String.valueOf(type));
            } else {
                selection = HotlineNumberHelper.getSqlSelectionStatement("phone", "type");
                selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber, String.valueOf(type));
            }
            return context.getContentResolver().update(blacklist_uri, values, selection, selectionArgs);
        }
        selectionArgs = new String[]{formatedPhone, String.valueOf(type)};
        return context.getContentResolver().update(blacklist_uri, values, "phone=? AND type=?", selectionArgs);
    }

    public static int addWhitelist(Context context, String phone, String name) {
        String formatedPhone = formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            HwLog.w(TAG, "addWhitelist : get invalid phone number by format method");
            return -1;
        }
        String selection;
        String[] selectionArgs;
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, formatedPhone);
        if (TextUtils.isEmpty(hotlineNumber)) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
        } else {
            name = HotlineNumberHelper.getHotlineNumberName(context, hotlineNumber);
            formatedPhone = hotlineNumber;
            selection = HotlineNumberHelper.getSqlSelectionStatement("phone");
            selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber);
        }
        Cursor cursor = context.getContentResolver().query(whitelist_uri, new String[]{"phone"}, selection, selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            ContentValues values = new ContentValues();
            values.put("phone", formatedPhone);
            values.put("name", name);
            Uri retUri = context.getContentResolver().insert(whitelist_uri, values);
            if (retUri != null && !retUri.toString().equals(whitelist_uri.toString())) {
                return (int) ContentUris.parseId(retUri);
            }
            HwLog.w(TAG, "addWhitelist: Fails");
            return -1;
        }
        cursor.close();
        return -2;
    }

    public static int[] getInterceptedCallAndMsgCount(Context context, String phone, int type) {
        return getInterceptedCallAndMsgCountInnner(context, phone, type);
    }

    private static int[] getInterceptedCallAndMsgCountInnner(Context context, String phone, int type) {
        int[] count = new int[]{0, 0};
        if (TextUtils.isEmpty(phone)) {
            HwLog.w(TAG, "getInterceptedCallAndMsgCount: Invalid phone number");
            return count;
        }
        String selectionCall;
        String selectionMessage;
        String[] selectionArgs;
        ContentResolver resolver = context.getContentResolver();
        String[] projection = new String[]{"_id", "phone"};
        boolean isHotlineNumber = false;
        if (type == 0) {
            String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phone);
            if (TextUtils.isEmpty(hotlineNumber)) {
                PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
                selectionCall = phoneMatchInfo.getSqlSelectionStatement("phone");
                selectionMessage = phoneMatchInfo.getSqlSelectionStatement("phone");
                selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
            } else {
                HwLog.i(TAG, "getInterceptedCallAndMsgCountInnner  ,this is  hotlineNumber ");
                isHotlineNumber = true;
                selectionCall = "phone like ?";
                selectionMessage = "phone like ?";
                selectionArgs = HotlineNumberHelper.getSqlSelectionFuzzyArgs(hotlineNumber);
            }
        } else {
            String phoneWithCountryCode = ConstValues.PHONE_COUNTRY_CODE_CHINA + CommonHelper.trimPhoneCountryCode(phone);
            selectionCall = "phone like ? OR phone like ?";
            selectionMessage = "phone like ? OR phone like ?";
            selectionArgs = new String[]{phoneWithCountryCode + "%", phoneWithoutCountryCode + "%"};
        }
        Cursor cursorCall = resolver.query(calls_uri, projection, selectionCall, selectionArgs, null);
        if (cursorCall != null) {
            if (isHotlineNumber) {
                count[0] = getCountIfHotlineNumber(context, cursorCall);
            } else {
                count[0] = cursorCall.getCount();
            }
            cursorCall.close();
        }
        Cursor cursorMsg = resolver.query(messages_uri, projection, selectionMessage, selectionArgs, null);
        if (cursorMsg != null) {
            if (isHotlineNumber) {
                count[1] = getCountIfHotlineNumber(context, cursorMsg);
            } else {
                count[1] = cursorMsg.getCount();
            }
            cursorMsg.close();
        }
        return count;
    }

    private static int getCountIfHotlineNumber(Context context, Cursor cursor) {
        int count = 0;
        try {
            int phoneIndex = cursor.getColumnIndex("phone");
            while (cursor.moveToNext()) {
                String phoneNumber = cursor.getString(phoneIndex);
                HwLog.i(TAG, "getCountIfHotlineNumber phone=");
                if (!(TextUtils.isEmpty(phoneNumber) || TextUtils.isEmpty(HotlineNumberHelper.getHotlineNumber(context, phoneNumber)))) {
                    count++;
                }
            }
        } catch (Exception e) {
            HwLog.e(TAG, e.toString());
        }
        return count;
    }

    public static boolean addMsgToSystemInbox(Context context, MessageInfo msg) {
        switch (msg.getMsgType()) {
            case 0:
                return addMsgToSMS(context, MessageInfo.translateFromMessageInfo(msg));
            case 1:
                return MmsIntentHelper.writeMmsToMmsInbox(context, msg);
            default:
                return false;
        }
    }

    public static boolean addMsgToSMS(Context context, SmsMsgInfo msg) {
        if (mRcs != null && mRcs.isNotOriginalType(context, msg)) {
            return true;
        }
        try {
            Uri retUri = context.getContentResolver().insert(Inbox.CONTENT_URI, msg.getAsSysSmsContentValues(getSmsDBColumnSubIdKey()));
            if (retUri == null || retUri.equals(Inbox.CONTENT_URI)) {
                HwLog.w(TAG, "addMsgToSMS: Fail to insert message to SMS DB");
                return false;
            } else if (Uri.withAppendedPath(Inbox.CONTENT_URI, "0").compareTo(retUri) == 0) {
                HwLog.e(TAG, "addMsgToSMS: Fail to insert message to SMS DB, Permission denied");
                return false;
            } else {
                HwLog.i(TAG, "addMsgToSMS: Succeeds");
                return true;
            }
        } catch (Exception e) {
            HwLog.e(TAG, "addMsgToSMS: Fail to insert message to SMS DB", e);
            return false;
        }
    }

    public static int addMsgsToSMS(Context context, List<SmsMsgInfo> messageList) {
        int i = -1;
        if (messageList == null) {
            HwLog.e(TAG, "addMsgsToSMS: Invalid message list");
            return -1;
        } else if (messageList.size() <= 0) {
            HwLog.i(TAG, "addMsgsToSMS: Empty message list");
            return 0;
        } else {
            int count = 0;
            if (mRcs != null) {
                count = messageList.size();
                messageList = mRcs.getOriginalMsgList(context, messageList);
                if (messageList.size() < 1) {
                    return count;
                }
                count -= messageList.size();
            }
            String sysSmsDBSubIdKey = getSmsDBColumnSubIdKey();
            ContentValues[] values = new ContentValues[messageList.size()];
            for (int i2 = 0; i2 < messageList.size(); i2++) {
                values[i2] = ((SmsMsgInfo) messageList.get(i2)).getAsSysSmsContentValues(sysSmsDBSubIdKey);
            }
            int nInsert = -1;
            try {
                nInsert = context.getContentResolver().bulkInsert(Inbox.CONTENT_URI, values);
            } catch (Exception e) {
                HwLog.e(TAG, "addMsgsToSMS: Fail to insert messages to SMS DB", e);
            }
            HwLog.d(TAG, "addMsgsToSMS: nInsert = " + nInsert);
            if (nInsert + count > 0) {
                i = nInsert + count;
            }
            return i;
        }
    }

    public static List<MessageInfo> addInterceptedMsgToSystemInBoxByType(Context context, String phone, int type) {
        List<MessageInfo> messageList = getInterceptedMsgs(context, phone, type);
        if (addInterceptedListMsgsToSystemInbox(context, messageList) < 0) {
            return null;
        }
        return messageList;
    }

    public static List<MessageInfo> addInterceptedMsgToSystemInBoxByTypeAndFuzzyPhone(Context context, String phone, int type) {
        List<MessageInfo> messageList = getInterceptedMsgsByFuzzyPhone(context, phone, type);
        if (addInterceptedListMsgsToSystemInbox(context, messageList) < 0) {
            return null;
        }
        return messageList;
    }

    private static int addInterceptedListMsgsToSystemInbox(Context context, List<MessageInfo> messageInfos) {
        if (messageInfos == null) {
            HwLog.e(TAG, "addInterceptedListMsgsToSystemInbox: Invalid message list");
            return -1;
        } else if (messageInfos.size() <= 0) {
            HwLog.i(TAG, "addMsgsToSMS: Empty message list");
            return 0;
        } else {
            int count = 0;
            for (MessageInfo messageInfo : messageInfos) {
                if (addMsgToSystemInbox(context, messageInfo)) {
                    count++;
                }
            }
            return count;
        }
    }

    @Deprecated
    public static int updateBlacklist(Context context, String phone, int flag) {
        return 0;
    }

    public static int resetInterceptedCallCount(Context context) {
        ContentValues values = new ContentValues();
        values.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(0));
        return context.getContentResolver().update(blacklist_uri, values, null, null);
    }

    public static int resetInterceptedMsgCount(Context context) {
        ContentValues values = new ContentValues();
        values.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(0));
        return context.getContentResolver().update(blacklist_uri, values, null, null);
    }

    public static int updateInterceptionRule(Context context, InterceptionRuleInfo rule) {
        ContentResolver reslover = context.getContentResolver();
        String selection = "name=?";
        String[] selectionArgs = rule.getSqlSelectionArgs();
        ContentValues values = rule.getAsContentValues();
        Cursor cursor = reslover.query(rules_uri, new String[]{"name", "state"}, selection, selectionArgs, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            reslover.insert(rules_uri, values);
            HwLog.i(TAG, "updateInterceptionRule : inserted " + values);
            return 1;
        }
        cursor.close();
        int updateNum = reslover.update(rules_uri, values, selection, selectionArgs);
        HwLog.i(TAG, "updateInterceptionRule : updated " + values);
        return updateNum;
    }

    public static int deleteInterceptedMsg(Context context, MessageInfo msg) {
        String[] selectionArgs = msg.getIdAsSqlSeclectionArgs();
        int nDelCount = context.getContentResolver().delete(messages_uri, "_id = ?", selectionArgs);
        if (nDelCount > 0) {
            updateBlacklistStatInfo(context, msg.getPhone(), 4);
        }
        return nDelCount;
    }

    public static int deleteInterceptedMsg(Context context, List<MessageInfo> messageList) {
        HashMap<String, Integer> msgCount = new HashMap();
        int nTotalDelCount = 0;
        for (MessageInfo msgInfo : messageList) {
            String[] selectionArgs = msgInfo.getIdAsSqlSeclectionArgs();
            int nResult = context.getContentResolver().delete(messages_uri, "_id = ?", selectionArgs);
            if (nResult > 0) {
                String phoneNumber = msgInfo.getPhone();
                if (msgCount.containsKey(phoneNumber)) {
                    msgCount.put(phoneNumber, Integer.valueOf(((Integer) msgCount.get(phoneNumber)).intValue() + nResult));
                } else {
                    msgCount.put(phoneNumber, Integer.valueOf(nResult));
                }
                nTotalDelCount += nResult;
            }
        }
        for (Entry<String, Integer> countInfo : msgCount.entrySet()) {
            updateBlacklistStatInfo(context, (String) countInfo.getKey(), 4, ((Integer) countInfo.getValue()).intValue());
        }
        return nTotalDelCount;
    }

    public static int deleteInterceptedCall(Context context, CallInfo callInfo) {
        String[] selectionArgs = callInfo.getIdAsSqlSeclectionArgs();
        int nDelCount = context.getContentResolver().delete(calls_uri, "_id = ?", selectionArgs);
        if (nDelCount > 0) {
            updateBlacklistStatInfo(context, callInfo.getPhone(), 3);
        }
        return nDelCount;
    }

    public static int deleteBlacklist(Context context, BlacklistInfo blacklist) {
        if (blacklist.getType() == 0) {
            return GoogleBlackListContract.deleteBlockedNumber(blacklist.getPhone());
        }
        String[] selectionArgs = blacklist.getIdAsSqlSeclectionArgs();
        return context.getContentResolver().delete(blacklist_uri, "_id = ?", selectionArgs);
    }

    public static void updateBlacklistStatInfo(Context context, String phoneNumber, int nFlag) {
        updateBlacklistStatInfo(context, phoneNumber, nFlag, 1);
    }

    public static void updateBlacklistInfo(Context context, List<BlacklistInfo> blacklist) {
        if (!Utility.isNullOrEmptyList(blacklist)) {
            for (BlacklistInfo blInfo : blacklist) {
                String[] selectionArgs = new String[]{String.valueOf(blInfo.getId())};
                ContentValues values = new ContentValues();
                values.put("phone", blInfo.getPhone());
                values.put("name", blInfo.getName());
                values.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(blInfo.getCallCount()));
                values.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(blInfo.getMsgCount()));
                values.put("option", Integer.valueOf(blInfo.getOption()));
                values.put("type", Integer.valueOf(blInfo.getType()));
                context.getContentResolver().update(blacklist_uri, values, "_id=?", selectionArgs);
            }
        }
    }

    public static void updateWhitelistInfo(Context context, List<WhitelistInfo> whitelistInfos) {
        if (!Utility.isNullOrEmptyList(whitelistInfos)) {
            for (WhitelistInfo whitelistInfo : whitelistInfos) {
                String[] selectionArgs = new String[]{String.valueOf(whitelistInfo.getId())};
                ContentValues values = new ContentValues();
                values.put("phone", whitelistInfo.getPhone());
                values.put("name", whitelistInfo.getName());
                context.getContentResolver().update(whitelist_uri, values, "_id=?", selectionArgs);
            }
        }
    }

    public static void updateBlacklistStatInfo(Context context, String phoneNumber, int nFlag, int nCount) {
        if (TextUtils.isEmpty(phoneNumber)) {
            HwLog.i(TAG, "phone number is empty");
            return;
        }
        List<BlacklistInfo> blacklist = getAllMatchedBlacklistInfo(context, phoneNumber);
        if (!Utility.isNullOrEmptyList(blacklist)) {
            for (BlacklistInfo blInfo : blacklist) {
                int nCallCount = blInfo.getCallCount();
                int nMsgCount = blInfo.getMsgCount();
                ContentValues values = new ContentValues();
                switch (nFlag) {
                    case 0:
                        values.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(nMsgCount + nCount));
                        break;
                    case 1:
                        values.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(nCallCount + nCount));
                        break;
                    case 3:
                        nCallCount -= nCount;
                        if (nCallCount < 0) {
                            nCallCount = 0;
                        }
                        values.put(TbBlacklist.INTERCEPTED_CALL_COUNT, Integer.valueOf(nCallCount));
                        break;
                    case 4:
                        nMsgCount -= nCount;
                        if (nMsgCount < 0) {
                            nMsgCount = 0;
                        }
                        values.put(TbBlacklist.INTERCEPTED_MSG_COUNT, Integer.valueOf(nMsgCount));
                        break;
                }
                if (values.size() > 0) {
                    String[] selectionArgs = new String[]{String.valueOf(blInfo.getId())};
                    context.getContentResolver().update(blacklist_uri, values, "_id=?", selectionArgs);
                }
            }
        }
    }

    private static List<BlacklistInfo> getAllMatchedBlacklistInfo(Context context, String phoneNumber) {
        List<BlacklistInfo> blacklist = getMatchedBlacklist(context, phoneNumber);
        blacklist.addAll(getMatchedBlacklistHeaders(context, phoneNumber));
        return blacklist;
    }

    private static List<BlacklistInfo> getMatchedBlacklist(Context context, String phoneNumber) {
        String selection;
        String[] selectionArgs;
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(context, phoneNumber);
        if (TextUtils.isEmpty(hotlineNumber)) {
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phoneNumber);
            selection = phoneMatchInfo.getSqlSelectionStatement("phone", "type");
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs(String.valueOf(0));
        } else {
            HwLog.i(TAG, "this is hotlineNumber");
            selection = HotlineNumberHelper.getSqlSelectionStatement("phone", "type");
            selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(hotlineNumber, String.valueOf(0));
        }
        return queryBlacklist(context, new String[]{"_id", "phone", "name", TbBlacklist.INTERCEPTED_CALL_COUNT, TbBlacklist.INTERCEPTED_MSG_COUNT, "option", "type"}, selection, selectionArgs);
    }

    private static List<BlacklistInfo> getMatchedBlacklistHeaders(Context context, String phoneNumber) {
        List<BlacklistInfo> headers = queryBlacklist(context, new String[]{"_id", "phone", TbBlacklist.INTERCEPTED_CALL_COUNT, TbBlacklist.INTERCEPTED_MSG_COUNT, "option", "type"}, "type = ?", new String[]{String.valueOf(1)});
        if (!Utility.isNullOrEmptyList(headers)) {
            Iterator<BlacklistInfo> iter = headers.iterator();
            while (iter.hasNext()) {
                if (!((BlacklistInfo) iter.next()).isMatchHeader(phoneNumber)) {
                    iter.remove();
                }
            }
        }
        return headers;
    }

    private static List<BlacklistInfo> queryBlacklist(Context context, String[] projection, String selection, String[] selectionArgs) {
        Cursor cursor = context.getContentResolver().query(blacklist_view_uri, projection, selection, selectionArgs, null);
        List<BlacklistInfo> blacklist = new ArrayList();
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return blacklist;
        }
        int nColIndexID = cursor.getColumnIndex("_id");
        int nColIndexPhone = cursor.getColumnIndex("phone");
        int nColIndexCallCount = cursor.getColumnIndex(TbBlacklist.INTERCEPTED_CALL_COUNT);
        int nColIndexMsgCount = cursor.getColumnIndex(TbBlacklist.INTERCEPTED_MSG_COUNT);
        int nColIndexOption = cursor.getColumnIndex("option");
        int nColIndexType = cursor.getColumnIndex("type");
        while (cursor.moveToNext()) {
            blacklist.add(new BlacklistInfo(cursor.getInt(nColIndexID), cursor.getString(nColIndexPhone), "", cursor.getInt(nColIndexCallCount), cursor.getInt(nColIndexMsgCount), cursor.getInt(nColIndexOption), cursor.getInt(nColIndexType), null));
        }
        cursor.close();
        return blacklist;
    }

    public static int deleteBlacklist(Context context, String phone) {
        return deleleFromUriByPhonenumber(context, blacklist_uri, "phone", phone);
    }

    public static int deleteBlacklistDft(Context context, String phone) {
        if (TextUtils.isEmpty(phone)) {
            return 0;
        }
        String[] selectionArgs = new String[]{phone, String.valueOf(0)};
        return context.getContentResolver().delete(blacklist_uri, "phone=? AND type=?", selectionArgs);
    }

    public static int deleteBlacklistHeader(Context context, String phone) {
        if (TextUtils.isEmpty(formatPhoneNumber(phone))) {
            return 0;
        }
        String[] selectionArgs = new String[]{formatPhoneNumber(phone), String.valueOf(1)};
        return context.getContentResolver().delete(blacklist_uri, "phone=? AND type=?", selectionArgs);
    }

    public static int deleteWhitelist(Context context, WhitelistInfo info) {
        return deleteWhitelist(context, info.getPhone());
    }

    public static int deleteWhitelist(Context context, int nId) {
        String[] selectionArgs = new String[]{String.valueOf(nId)};
        return context.getContentResolver().delete(whitelist_uri, "_id = ?", selectionArgs);
    }

    public static int deleteWhitelist(Context context, String phone) {
        return deleleFromUriByPhonenumber(context, whitelist_uri, "phone", phone);
    }

    private static int deleleFromUriByPhonenumber(Context context, Uri uri, String colNamePhone, String phone) {
        if (TextUtils.isEmpty(colNamePhone) || TextUtils.isEmpty(phone)) {
            HwLog.w(TAG, "deleleFromUriByPhonenumber: Invalid params, uri = " + uri);
            return 0;
        }
        String selection;
        String[] selectionArgs;
        PhoneMatch.outputPhoneMathConfig();
        String number = HotlineNumberHelper.getHotlineNumber(context, phone);
        if (TextUtils.isEmpty(number) || !isHotlinePhoneExist(context, uri, number, colNamePhone)) {
            number = phone;
            PhoneMatchInfo phoneMatchInfo = PhoneMatch.getPhoneNumberMatchInfo(phone);
            selection = phoneMatchInfo.getSqlSelectionStatement(colNamePhone);
            selectionArgs = phoneMatchInfo.getSqlSelectionArgs();
        } else {
            selection = HotlineNumberHelper.getSqlSelectionStatement(colNamePhone);
            selectionArgs = HotlineNumberHelper.getSqlSelectionArgs(number);
        }
        int result = context.getContentResolver().delete(uri, selection, selectionArgs);
        if (result > 0 && blacklist_uri.equals(uri)) {
            HwLog.i(TAG, "deleleFromUriByPhonenumber, delete google nb data, res:" + GoogleBlackListContract.deleteBlockedNumber(formatPhoneNumber(number)));
        }
        return result;
    }

    public static int deleteAllBlacklist(Context context) {
        ProviderUtils.deleteAll(context, blacklist_uri);
        return GoogleBlackListContract.deleteAllBlockedNumber();
    }

    public static int deleteAllInterceptedCall(Context context) {
        return ProviderUtils.deleteAll(context, calls_uri);
    }

    public static int deleteAllInterceptedMsg(Context context) {
        return ProviderUtils.deleteAll(context, messages_uri);
    }

    public static List<CallLogInfo> getCallLogListInBatches(Context context, int nBatchSize, long nLastCallLogDate) {
        if (nBatchSize <= 0) {
            HwLog.w(TAG, "getCallLogListInBatches: Invalid batch size");
            return null;
        }
        List<CallLogInfo> callLogList = new ArrayList();
        ContentResolver resolver = context.getContentResolver();
        String strSelection = "";
        if (nLastCallLogDate <= 0) {
            strSelection = String.format(Locale.US, "%1$s IN (SELECT %2$s FROM CALLS GROUP BY %3$s ORDER BY %4$s DESC LIMIT %5$d)", new Object[]{"_id", "_id", "number", "date", Integer.valueOf(nBatchSize)});
        } else {
            strSelection = String.format(Locale.US, "%1$s IN (SELECT %2$s FROM CALLS WHERE %3$s<%4$d GROUP BY %5$s ORDER BY %6$s DESC LIMIT %7$d)", new Object[]{"_id", "_id", "date", Long.valueOf(nLastCallLogDate), "number", "date", Integer.valueOf(nBatchSize)});
        }
        try {
            Cursor cursor = resolver.query(Calls.CONTENT_URI, CALLLOG_PROJECTION, strSelection, null, "date DESC LIMIT " + nBatchSize);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                return null;
            }
            int nColIndexNumber = cursor.getColumnIndex("number");
            int nColIndexName = cursor.getColumnIndex("name");
            int nColIndexDate = cursor.getColumnIndex("date");
            int nColIndexLocation = cursor.getColumnIndex("geocoded_location");
            boolean bLocationFeatureEnabled = CustomizeWrapper.isNumberLocationEnabled();
            while (cursor.moveToNext()) {
                String number = cursor.getString(nColIndexNumber);
                if (!CommonHelper.isInvalidPhoneNumber(number)) {
                    CallLogInfo callLogInfo;
                    String name = cursor.getString(nColIndexName);
                    long date = cursor.getLong(nColIndexDate);
                    String geoLocation = cursor.getString(nColIndexLocation);
                    if (bLocationFeatureEnabled) {
                        callLogInfo = new CallLogInfo(name, number, date, NumberLocationHelper.parseNumberLocation(geoLocation));
                    } else {
                        callLogInfo = new CallLogInfo(name, number, date);
                    }
                    callLogList.add(callLogInfo);
                }
            }
            cursor.close();
            return callLogList;
        } catch (Throwable e) {
            HwLog.e(TAG, "getCallLogListInBatches exception", e);
            return null;
        }
    }

    public static List<SmsInfo> getMsgListInBatches_new(Context context, int nBatchSize, long nLastMsgDate) {
        if (nBatchSize <= 0) {
            HwLog.w(TAG, "getMsgListInBatches: Invalid batch size");
            return null;
        }
        Cursor cursor;
        List<SmsInfo> msgList = new ArrayList();
        ContentResolver resolver = context.getContentResolver();
        String strSelection = null;
        if (nLastMsgDate > 0) {
            strSelection = String.format(Locale.US, " %1$s<%2$d ", new Object[]{"date", Long.valueOf(nLastMsgDate)});
        }
        String strOrderBy = "date DESC LIMIT " + nBatchSize;
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            cursor = mRcs.getMsgListCursorInBatches(context, nBatchSize, nLastMsgDate);
        } else {
            try {
                cursor = resolver.query(THREAD_URI, MESSAGE_THREADID_PROJECTION, strSelection, null, strOrderBy);
            } catch (Throwable e) {
                HwLog.e(TAG, "getMsgListInBatches exception", e);
                return null;
            }
        }
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return null;
        }
        Hashtable<Integer, String> addressTable = getAddressFromCanonicalAddressesTable(context);
        if (addressTable.isEmpty()) {
            cursor.close();
            return null;
        }
        HwLog.i(TAG, "group msg count = " + cursor.getCount());
        int nColRecIdIndex = cursor.getColumnIndex("recipient_ids");
        int nColSnippetIndex = cursor.getColumnIndex("snippet");
        int nColSnippetCharIndex = cursor.getColumnIndex("snippet_cs");
        int nColDataIndex = cursor.getColumnIndex("date");
        int nColHasAttchmentIndex = cursor.getColumnIndex("has_attachment");
        while (cursor.moveToNext()) {
            String recId = cursor.getString(nColRecIdIndex);
            String snippet = cursor.getString(nColSnippetIndex);
            String snippetChar = cursor.getString(nColSnippetCharIndex);
            long date = cursor.getLong(nColDataIndex);
            int hasAttachment = cursor.getInt(nColHasAttchmentIndex);
            String address = null;
            try {
                address = (String) addressTable.get(Integer.valueOf(Integer.parseInt(recId)));
            } catch (Throwable e2) {
                HwLog.e(TAG, e2.getMessage(), e2);
            }
            if (!(TextUtils.isEmpty(address) || TextUtils.isEmpty(snippetChar))) {
                String name = getContactNameFromPhoneBook(context, address);
                int type = 0;
                if (hasAttachment == 1 || TextUtils.isEmpty(snippet) || (!TextUtils.isEmpty(snippet) && "106".equals(snippetChar))) {
                    type = 1;
                }
                String body = getBodyFromSnippet(snippet, snippetChar);
                if (!TextUtils.isEmpty(body) || type != 0) {
                    msgList.add(new SmsInfo(name, address, body, date, type));
                }
            }
        }
        cursor.close();
        return msgList;
    }

    private static String getBodyFromSnippet(String snippet, String snippet_char) {
        if (TextUtils.isEmpty(snippet) || !"106".equals(snippet_char)) {
            return snippet;
        }
        try {
            return new String(snippet.getBytes("ISO8859_1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            HwLog.e(TAG, e.getMessage(), e);
            return "";
        }
    }

    private static Hashtable<Integer, String> getAddressFromCanonicalAddressesTable(Context context) {
        Hashtable<Integer, String> threadIdToAddressTable = new Hashtable();
        try {
            Cursor cursor = context.getContentResolver().query(CANONICAL_ADD_URI, ADDRESS_PROJECTION, null, null, null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                return threadIdToAddressTable;
            }
            HwLog.i(TAG, "canonical-addresses count = " + cursor.getCount());
            int addressIndex = cursor.getColumnIndex("address");
            int idIndex = cursor.getColumnIndex("_id");
            while (cursor.moveToNext()) {
                threadIdToAddressTable.put(Integer.valueOf(cursor.getInt(idIndex)), cursor.getString(addressIndex));
            }
            cursor.close();
            return threadIdToAddressTable;
        } catch (Exception e) {
            HwLog.e(TAG, "getAddressFromCanonicalAddressesTable exception", e);
            return threadIdToAddressTable;
        }
    }

    public static List<SmsInfo> getMsgListInBatches(Context context, int nBatchSize, long nLastMsgDate) {
        if (nBatchSize <= 0) {
            HwLog.w(TAG, "getMsgListInBatches: Invalid batch size");
            return null;
        }
        Cursor cursor;
        List<SmsInfo> msgList = new ArrayList();
        ContentResolver resolver = context.getContentResolver();
        String strSelection = "";
        if (nLastMsgDate <= 0) {
            strSelection = String.format(Locale.US, "%1$s IN (SELECT %2$s FROM SMS GROUP BY %3$s ORDER BY %4$s DESC LIMIT %5$d )", new Object[]{"_id", "_id", "thread_id", "date", Integer.valueOf(nBatchSize)});
        } else {
            strSelection = String.format(Locale.US, "%1$s IN (SELECT %2$s FROM SMS WHERE %3$s<%4$d GROUP BY %5$s ORDER BY %6$s DESC LIMIT %7$d)", new Object[]{"_id", "_id", "date", Long.valueOf(nLastMsgDate), "thread_id", "date", Integer.valueOf(nBatchSize)});
        }
        String strOrderBy = "date DESC LIMIT " + nBatchSize;
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            cursor = mRcs.getMsgListCursorInBatches(context, nBatchSize, nLastMsgDate);
        } else {
            try {
                cursor = resolver.query(Sms.CONTENT_URI, MESSAGE_PROJECTION, strSelection, null, strOrderBy);
            } catch (Throwable e) {
                HwLog.e(TAG, "getMsgListInBatches exception", e);
                return null;
            }
        }
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return null;
        }
        int nColIndexAddress = cursor.getColumnIndex("address");
        int nColIndexBody = cursor.getColumnIndex(TbMessages.BODY);
        int nColIndexDate = cursor.getColumnIndex("date");
        while (cursor.moveToNext()) {
            String address = cursor.getString(nColIndexAddress);
            List<SmsInfo> list = msgList;
            list.add(new SmsInfo(getContactNameFromPhoneBook(context, address), address, cursor.getString(nColIndexBody), cursor.getLong(nColIndexDate)));
        }
        cursor.close();
        return msgList;
    }

    private static String getContactNameFromPhoneBook(Context context, String address) {
        try {
            String normalizedNumber = Uri.encode(PhoneNumberUtils.normalizeNumber(address), "#");
            Builder uriBuilder = PhoneLookup.CONTENT_FILTER_URI.buildUpon();
            uriBuilder.appendPath(normalizedNumber);
            Cursor cursor = context.getContentResolver().query(uriBuilder.build(), PHONE_LOOKUP_PROJECTION, null, null, null);
            if (Utility.isNullOrEmptyCursor(cursor, true)) {
                return null;
            }
            cursor.moveToPosition(0);
            String name = cursor.getString(cursor.getColumnIndex("display_name"));
            cursor.close();
            return name;
        } catch (Exception e) {
            HwLog.e(TAG, "getContactNameFromPhoneBook exception", e);
            return null;
        }
    }

    public static int getUnreadCallCount(Context context) {
        ContentResolver resolver = context.getContentResolver();
        long lastTime = PreferenceHelper.getLastWatchCallTime(context);
        Cursor cursor = resolver.query(calls_uri, new String[]{"date"}, "date>?", new String[]{"" + lastTime}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return 0;
        }
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static int getUnreadMsgCount(Context context) {
        ContentResolver resolver = context.getContentResolver();
        long lastTime = PreferenceHelper.getLastWatchMessageTime(context);
        Cursor cursor = resolver.query(messages_uri, new String[]{"date"}, "date>?", new String[]{"" + lastTime}, null);
        if (Utility.isNullOrEmptyCursor(cursor, true)) {
            return 0;
        }
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    public static String formatPhoneNumber(String phone) {
        if (!CommonHelper.isInvalidPhoneNumber(phone)) {
            return PhoneNumberUtils.stripSeparators(phone);
        }
        HwLog.w(TAG, "formatPhoneNumber : Invalid phone number");
        return "";
    }

    public static String getSmsDBColumnSubIdKey() {
        try {
            final Field field = Class.forName("android.provider.Telephony$TextBasedSmsColumns").getDeclaredField("SUBSCRIPTION_ID");
            AccessController.doPrivileged(new PrivilegedAction<Object>() {
                public Void run() {
                    field.setAccessible(true);
                    return null;
                }
            });
            return (String) field.get(null);
        } catch (Exception e) {
            HwLog.w(TAG, "getSmsDBColumnSubIdKey , Exception" + e);
            HwLog.w(TAG, "getSmsDBColumnSubIdKey: Fail to get key for subId");
            return null;
        } catch (Error error) {
            HwLog.w(TAG, "getSmsDBColumnSubIdKey , Error" + error);
            HwLog.w(TAG, "getSmsDBColumnSubIdKey: Fail to get key for subId");
            return null;
        }
    }

    public static int addKeywords(Context context, String[] keywords) {
        int i = 1;
        if (keywords == null || keywords.length <= 0) {
            HwLog.e(TAG, "addKeywords: Invalid params");
            return 0;
        } else if (keywords.length != 1) {
            int nIndex;
            Set<String> keywordsList = new HashSet();
            for (nIndex = 0; nIndex < keywords.length; nIndex++) {
                if (shouldAddKeyword(context, keywords[nIndex])) {
                    keywordsList.add(keywords[nIndex]);
                }
            }
            if (keywordsList.isEmpty()) {
                return 0;
            }
            ContentValues[] values = new ContentValues[keywordsList.size()];
            nIndex = 0;
            for (String keyword : keywordsList) {
                value = new ContentValues();
                value.put(TbKeywords.KEYWORD, keyword);
                int nIndex2 = nIndex + 1;
                values[nIndex] = value;
                nIndex = nIndex2;
            }
            int nInsertCount = 0;
            try {
                nInsertCount = context.getContentResolver().bulkInsert(keywords_uri, values);
            } catch (Exception e) {
                HwLog.e(TAG, "addKeywords: Exception");
            }
            return nInsertCount;
        } else if (!shouldAddKeyword(context, keywords[0])) {
            return 0;
        } else {
            value = new ContentValues();
            value.put(TbKeywords.KEYWORD, keywords[0]);
            Uri uri = null;
            try {
                uri = context.getContentResolver().insert(keywords_uri, value);
            } catch (Exception e2) {
                HwLog.e(TAG, "addKeywords: Exception");
            }
            if (keywords_uri == uri) {
                i = 0;
            }
            return i;
        }
    }

    public static int deleteKeywords(Context context, List<KeywordsInfo> keywords) {
        if (Utility.isNullOrEmptyList(keywords)) {
            HwLog.e(TAG, "deleteKeywords");
            return 0;
        }
        int nCount = 0;
        for (KeywordsInfo keyword : keywords) {
            nCount += deleteKeywords(context, keyword);
        }
        HwLog.i(TAG, "deleteKeywords: total count = " + nCount);
        return nCount;
    }

    public static int deleteKeywords(Context context, KeywordsInfo keyword) {
        return context.getContentResolver().delete(keywords_uri, "_id = ?", new String[]{String.valueOf(keyword.getId())});
    }

    private static boolean shouldAddKeyword(Context context, String keyword) {
        if (TextUtils.isEmpty(keyword)) {
            HwLog.w(TAG, "shouldAddKeyword: Invalid keyword");
            return false;
        } else if (keyword.trim().isEmpty()) {
            HwLog.w(TAG, "shouldAddKeyword: Empty keyword");
            return false;
        } else if (!isKeywordsExist(context, keyword)) {
            return true;
        } else {
            HwLog.w(TAG, "shouldAddKeyword: Keyword already exists");
            return false;
        }
    }
}
