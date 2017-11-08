package com.huawei.harassmentinterception.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.huawei.harassmentinterception.strategy.HotlineInterceptionConfigs;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class HotlineNumberHelper {
    private static final String HOTLINE_NUMBER_QUERY_URI = "content://com.android.contacts.app/yellow_page_data";
    private static final String TAG = "HotlineNumberHelper";
    private static Uri mHotlineUri = Uri.parse(HOTLINE_NUMBER_QUERY_URI);
    private static Uri mParsePhoneNumberUri = Uri.parse("content://com.android.contacts.app/parse_fixed_phone_number");

    public static java.util.List<java.lang.String> getHotlineNumbers(android.content.Context r11) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0081 in list []
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
        r1 = 0;
        r10 = 0;
        r0 = 1;
        r2 = new java.lang.String[r0];
        r0 = "number";
        r2[r1] = r0;
        r7 = 0;
        r0 = r11.getContentResolver();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = mHotlineUri;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r3 = 0;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r4 = 0;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r5 = 0;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        if (r7 != 0) goto L_0x0029;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
    L_0x001a:
        r0 = "HotlineNumberHelper";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = "getHotlineNumbers: Fail to get hotline numbers";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        if (r7 == 0) goto L_0x0028;
    L_0x0025:
        r7.close();
    L_0x0028:
        return r10;
    L_0x0029:
        r0 = "HotlineNumberHelper";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1.<init>();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r3 = "getHotlineNumbers: Hotline numbers count = ";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.append(r3);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r3 = r7.getCount();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.append(r3);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        com.huawei.systemmanager.util.HwLog.i(r0, r1);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r8 = new java.util.ArrayList;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r8.<init>();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
    L_0x004c:
        r0 = r7.moveToNext();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        if (r0 == 0) goto L_0x0082;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
    L_0x0052:
        r0 = 0;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r9 = r7.getString(r0);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        if (r9 == 0) goto L_0x004c;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
    L_0x0059:
        r8.add(r9);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        goto L_0x004c;
    L_0x005d:
        r6 = move-exception;
        r0 = "HotlineNumberHelper";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1.<init>();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r3 = "getHotlineNumbers: Exception = ";	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.append(r3);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r3 = r6.getMessage();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.append(r3);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        com.huawei.systemmanager.util.HwLog.e(r0, r1);	 Catch:{ Exception -> 0x005d, all -> 0x0088 }
        if (r7 == 0) goto L_0x0081;
    L_0x007e:
        r7.close();
    L_0x0081:
        return r10;
    L_0x0082:
        if (r7 == 0) goto L_0x0087;
    L_0x0084:
        r7.close();
    L_0x0087:
        return r8;
    L_0x0088:
        r0 = move-exception;
        if (r7 == 0) goto L_0x008e;
    L_0x008b:
        r7.close();
    L_0x008e:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.harassmentinterception.util.HotlineNumberHelper.getHotlineNumbers(android.content.Context):java.util.List<java.lang.String>");
    }

    public static boolean isHotlineNumber(Context context, String number) {
        if (number == null || number.trim().length() < 1) {
            return false;
        }
        boolean isHotline = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(mHotlineUri, Uri.encode(number)), new String[]{"name"}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                isHotline = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exp) {
            exp.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isHotline;
    }

    public static String getHotlineNumberName(Context context, String hotlineNumber) {
        String str = null;
        if (!HotlineInterceptionConfigs.isHotlineNumberWithoutAreaCodeFuzzyMatchEnable() || hotlineNumber == null || hotlineNumber.trim().length() < 1) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(Uri.withAppendedPath(mHotlineUri, Uri.encode(hotlineNumber)), new String[]{"name"}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String name = cursor.getString(cursor.getColumnIndex("name"));
                    if (!TextUtils.isEmpty(name)) {
                        str = name;
                        break;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception exp) {
            HwLog.e(TAG, "getHotlineNumberName: Exception = " + exp.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static String getNoAreaNumber(Context context, String number) {
        String str = null;
        if (TextUtils.isEmpty(number) || !HotlineInterceptionConfigs.isHotlineNumberWithoutAreaCodeFuzzyMatchEnable()) {
            return null;
        }
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(mParsePhoneNumberUri, new String[]{number}, null, null, null);
            if (cursor != null && cursor.getCount() > 0) {
                while (cursor.moveToNext()) {
                    String noareanumber = cursor.getString(cursor.getColumnIndex("noAreaNum"));
                    if (!TextUtils.isEmpty(noareanumber)) {
                        str = noareanumber;
                        break;
                    }
                }
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            HwLog.e(TAG, "getNoAreaNumber: Exception = " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return str;
    }

    public static String getHotlineNumber(Context context, String number) {
        String hotlineNumber = "";
        if (!HotlineInterceptionConfigs.isHotlineNumberWithoutAreaCodeFuzzyMatchEnable()) {
            return hotlineNumber;
        }
        if (number.length() >= 5 && number.length() <= 9) {
            String noreaNumber = getNoAreaNumber(context, number);
            if (!TextUtils.isEmpty(noreaNumber) && noreaNumber.startsWith(HotlineInterceptionConfigs.NUMBER_START) && noreaNumber.length() == 5 && isHotlineNumber(context, noreaNumber)) {
                hotlineNumber = noreaNumber;
            }
        }
        return hotlineNumber;
    }

    public static String getSqlSelectionStatement(String phoneColName) {
        return getSqlSelectionStatement(phoneColName, (String[]) null);
    }

    public static String getSqlSelectionStatement(String phoneColName, String... otherColumns) {
        StringBuilder selection = new StringBuilder();
        if (TextUtils.isEmpty(phoneColName)) {
            return selection.toString();
        }
        selection.append(phoneColName);
        selection.append(" = ?");
        if (otherColumns == null || otherColumns.length <= 0) {
            return selection.toString();
        }
        for (String column : otherColumns) {
            if (!TextUtils.isEmpty(column)) {
                selection.append(" AND ");
                selection.append(column);
                selection.append(" = ?");
            }
        }
        return selection.toString();
    }

    public static String[] getSqlSelectionArgs(String hotlineNumber) {
        return new String[]{hotlineNumber};
    }

    public static String[] getSqlSelectionFuzzyArgs(String hotlineNumber) {
        return new String[]{"%" + hotlineNumber};
    }

    public static String[] getSqlSelectionArgs(String hotlineNumber, String... otherValues) {
        List<String> selectionArgs = new ArrayList();
        String phoneNumberValue = hotlineNumber;
        selectionArgs.add(hotlineNumber);
        for (String value : otherValues) {
            if (!TextUtils.isEmpty(value)) {
                selectionArgs.add(value);
            }
        }
        return (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
    }
}
