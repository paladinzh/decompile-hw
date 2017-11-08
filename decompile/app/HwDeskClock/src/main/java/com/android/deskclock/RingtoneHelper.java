package com.android.deskclock;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.media.RingtoneManager;
import android.net.Uri;
import android.provider.MediaStore.Audio.Media;
import android.provider.Settings.System;
import android.text.TextUtils;
import com.android.util.CompatUtils;
import com.android.util.Log;
import java.io.File;

public class RingtoneHelper {
    private static String[] projection = new String[]{"_id", "_data", "title"};

    public static java.lang.String getActualUri(android.content.Context r9, android.net.Uri r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0085 in list []
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
        if (r10 != 0) goto L_0x0006;
    L_0x0002:
        r0 = "silent";
        return r0;
    L_0x0006:
        r0 = "content://settings/system/ringtone1";
        r1 = r10.toString();
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x0017;
    L_0x0013:
        r0 = "content://settings/system/ringtone1";
        return r0;
    L_0x0017:
        r0 = "android.permission.READ_EXTERNAL_STORAGE";
        r0 = com.android.util.CompatUtils.hasPermission(r9, r0);
        if (r0 != 0) goto L_0x002d;
    L_0x0020:
        r0 = "RingtoneHelper";
        r1 = "getActualUri->has no READ_EXTERNAL_STORAGE permissions";
        com.android.util.Log.iRelease(r0, r1);
        r0 = "content://settings/system/ringtone1";
        return r0;
    L_0x002d:
        r7 = 0;
        r0 = r9.getContentResolver();	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r2 = projection;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r3 = 0;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r4 = 0;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1 = r10;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        if (r7 == 0) goto L_0x0055;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
    L_0x003e:
        r0 = r7.moveToFirst();	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        if (r0 == 0) goto L_0x0055;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
    L_0x0044:
        r0 = "_data";	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r0 = r7.getColumnIndex(r0);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r6 = r7.getString(r0);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        if (r7 == 0) goto L_0x0054;
    L_0x0051:
        r7.close();
    L_0x0054:
        return r6;
    L_0x0055:
        r0 = "content://settings/system/ringtone1";	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        if (r7 == 0) goto L_0x005d;
    L_0x005a:
        r7.close();
    L_0x005d:
        return r0;
    L_0x005e:
        r8 = move-exception;
        r0 = "RingtoneHelper";	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1.<init>();	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r2 = "getActualUri : SQLiteException = ";	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1 = r1.append(r2);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r2 = r8.getMessage();	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1 = r1.append(r2);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r1 = r1.toString();	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        com.android.util.Log.e(r0, r1);	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        r0 = "content://settings/system/ringtone1";	 Catch:{ SQLiteException -> 0x005e, all -> 0x0086 }
        if (r7 == 0) goto L_0x0085;
    L_0x0082:
        r7.close();
    L_0x0085:
        return r0;
    L_0x0086:
        r0 = move-exception;
        if (r7 == 0) goto L_0x008c;
    L_0x0089:
        r7.close();
    L_0x008c:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.RingtoneHelper.getActualUri(android.content.Context, android.net.Uri):java.lang.String");
    }

    public static java.lang.String getActualUriTitle(android.content.Context r11, android.net.Uri r12) {
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
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = 0;
        if (r12 != 0) goto L_0x0004;
    L_0x0003:
        return r10;
    L_0x0004:
        r0 = "content://settings/system/ringtone1";
        r1 = r12.toString();
        r0 = r0.equals(r1);
        if (r0 == 0) goto L_0x001c;
    L_0x0011:
        r0 = "ro.config.deskclock_timer_alert";
        r1 = "Timer_Beep.ogg";
        r9 = android.os.SystemProperties.get(r0, r1);
        return r9;
    L_0x001c:
        r0 = "android.permission.READ_EXTERNAL_STORAGE";
        r0 = com.android.util.CompatUtils.hasPermission(r11, r0);
        if (r0 != 0) goto L_0x002f;
    L_0x0025:
        r0 = "RingtoneHelper";
        r1 = "getActualUri->has no READ_EXTERNAL_STORAGE permissions";
        com.android.util.Log.iRelease(r0, r1);
        return r10;
    L_0x002f:
        r7 = 0;
        r0 = r11.getContentResolver();	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r2 = projection;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r3 = 0;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r4 = 0;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1 = r12;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        if (r7 == 0) goto L_0x0057;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
    L_0x0040:
        r0 = r7.moveToFirst();	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        if (r0 == 0) goto L_0x0057;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
    L_0x0046:
        r0 = "title";	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r0 = r7.getColumnIndex(r0);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r6 = r7.getString(r0);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        if (r7 == 0) goto L_0x0056;
    L_0x0053:
        r7.close();
    L_0x0056:
        return r6;
    L_0x0057:
        if (r7 == 0) goto L_0x005c;
    L_0x0059:
        r7.close();
    L_0x005c:
        return r10;
    L_0x005d:
        r8 = move-exception;
        r0 = "RingtoneHelper";	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1.<init>();	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r2 = "getActualUri : SQLiteException = ";	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1 = r1.append(r2);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r2 = r8.getMessage();	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1 = r1.append(r2);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        r1 = r1.toString();	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        com.android.util.Log.e(r0, r1);	 Catch:{ SQLiteException -> 0x005d, all -> 0x0082 }
        if (r7 == 0) goto L_0x0081;
    L_0x007e:
        r7.close();
    L_0x0081:
        return r10;
    L_0x0082:
        r0 = move-exception;
        if (r7 == 0) goto L_0x0088;
    L_0x0085:
        r7.close();
    L_0x0088:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.deskclock.RingtoneHelper.getActualUriTitle(android.content.Context, android.net.Uri):java.lang.String");
    }

    public static Uri getDefaultUri(Uri path) {
        if (path == null || "".equals(path.toString()) || "content://settings/system/alarm_alert".equals(path.toString())) {
            return Uri.parse("content://settings/system/alarm_alert");
        }
        if ("silent".equals(path.toString())) {
            return Uri.parse("silent");
        }
        if ("default".equals(path.toString()) || "content://settings/system/ringtone1".equals(path.toString())) {
            return Uri.parse("content://settings/system/ringtone1");
        }
        return path;
    }

    public static Uri getDefaultAlarmRington(Context context, Uri uri) {
        Log.iRelease("RingtoneHelper", "getDefaultAlarmRington");
        if (uri == null) {
            Log.iRelease("RingtoneHelper", "uri == null");
            return null;
        }
        Uri uriRet = uri;
        if ("silent".equals(uri.toString())) {
            Log.iRelease("RingtoneHelper", "SILENT_RINGTONE");
            return null;
        }
        if ("".equals(uri.toString()) || "content://settings/system/alarm_alert".equals(uri.toString())) {
            uriRet = RingtoneManager.getActualDefaultRingtoneUri(context, 4);
            Log.iRelease("RingtoneHelper", "uriRet:" + uriRet);
        } else if ("content://settings/system/ringtone1".equals(uri.toString())) {
            Log.iRelease("RingtoneHelper", "uriRet:content://settings/system/ringtone1");
        }
        return uriRet;
    }

    public static Uri getAvailableRingtone(Context context, Uri uri) {
        if (uri == null) {
            Log.iRelease("RingtoneHelper", "uri == null");
            return null;
        }
        Uri uriRet = uri;
        if ("silent".equals(uri.toString())) {
            Log.iRelease("RingtoneHelper", "SILENT_RINGTONE");
            return null;
        }
        if ("".equals(uri.toString()) || "content://settings/system/alarm_alert".equals(uri.toString())) {
            uriRet = RingtoneManager.getActualDefaultRingtoneUri(context, 4);
            Log.iRelease("RingtoneHelper", "uriRet:" + uriRet);
            if (!(uriRet == null || isRingtoneAvailable(context, uriRet))) {
                return getThemeDefaultRingtoneUri(context, uriRet);
            }
        } else if ("content://settings/system/ringtone1".equals(uri.toString()) || !isRingtoneAvailable(context, uri)) {
            Log.iRelease("RingtoneHelper", "uriRet:DEFAULT_RINGTONE_ITEM");
            return getThemeDefaultRingtoneUri(context, uri);
        }
        return uriRet;
    }

    private static Uri getThemeDefaultRingtoneUri(Context context, Uri defaultUri) {
        String defaultRingtone = getThemeDefaultRingtone(context);
        Log.iRelease("RingtoneHelper", "getThemeDefaultRingtoneUri:" + defaultRingtone);
        if (TextUtils.isEmpty(defaultRingtone)) {
            return defaultUri;
        }
        return Uri.parse(defaultRingtone);
    }

    private static String getThemeDefaultRingtone(Context context) {
        return System.getString(context.getContentResolver(), "theme_alarm_alert_path");
    }

    public static boolean isRingtoneAvailable(Context context, Uri uri) {
        boolean available = false;
        if (CompatUtils.hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE")) {
            Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
            if (cursor != null) {
                if (cursor.getCount() == 1) {
                    available = true;
                }
                cursor.close();
            }
            return available;
        }
        Log.iRelease("RingtoneHelper", "parseUri->has no READ_EXTERNAL_STORAGE permissions");
        return false;
    }

    public static String getAailableRingtoneTitle(Context context, Uri uri) {
        Uri alert = uri;
        if (uri == null || "silent".equals(uri.toString())) {
            return "silent";
        }
        if ("content://settings/system/ringtone1".equals(uri.toString())) {
            return null;
        }
        if ("".equals(uri.toString()) || "content://settings/system/alarm_alert".equals(uri.toString())) {
            alert = getAvailableRingtone(context, uri);
            if (alert == null) {
                return "silent";
            }
        } else if (!isRingtoneAvailable(context, uri)) {
            alert = getUriByPath(context, uri);
        }
        String str = null;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(alert, projection, null, null, null);
            if (cursor != null && cursor.moveToFirst()) {
                str = cursor.getString(cursor.getColumnIndex("title"));
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            Log.e("RingtoneHelper", "getAailableRingtoneTitle : SQLiteException = " + e.getMessage());
            if (cursor != null) {
                cursor.close();
            }
            return null;
        } catch (IllegalArgumentException e2) {
            Log.e("RingtoneHelper", "getAailableRingtoneTitle : IllegalArgumentException = " + e2.getMessage());
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

    public static Uri getUriByPath(Context context, Uri path) {
        if (path == null || "".equals(path.toString()) || "content://settings/system/alarm_alert".equals(path.toString())) {
            return Uri.parse("content://settings/system/alarm_alert");
        }
        if ("silent".equals(path.toString())) {
            return Uri.parse("silent");
        }
        if ("default".equals(path.toString()) || "content://settings/system/ringtone1".equals(path.toString())) {
            return Uri.parse("content://settings/system/ringtone1");
        }
        if (isRingtoneAvailable(context, path)) {
            Log.iRelease("RingtoneHelper", "Ringtone is Available");
            return path;
        } else if (CompatUtils.hasPermission(context, "android.permission.READ_EXTERNAL_STORAGE")) {
            Uri ROOT_INTERNAL = Media.INTERNAL_CONTENT_URI;
            Uri ROOT_EXTERNAL = Media.EXTERNAL_CONTENT_URI;
            String CATALOG_SELECTION = "_data = ? ";
            Cursor cursor = null;
            try {
                cursor = getCursor(context, path, ROOT_EXTERNAL, "_data = ? ");
                Uri parse;
                if (isItemsExsit(cursor)) {
                    Log.iRelease("RingtoneHelper", "Media_EXTERNAL");
                    parse = Uri.parse(ROOT_EXTERNAL + "/" + cursor.getInt(0));
                    if (cursor != null) {
                        cursor.close();
                    }
                    return parse;
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (path.toString().contains("/system/media/Pre-loaded/Music")) {
                    Log.iRelease("RingtoneHelper", "Pre-loaded_Music");
                    return Uri.parse("content://settings/system/ringtone1");
                }
                try {
                    cursor = getCursor(context, path, ROOT_INTERNAL, "_data = ? ");
                    if (isItemsExsit(cursor)) {
                        Log.iRelease("RingtoneHelper", "Media_INTERNAL");
                        parse = Uri.parse(ROOT_INTERNAL + "/" + cursor.getInt(0));
                        if (cursor != null) {
                            cursor.close();
                        }
                        return parse;
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    try {
                        path = Uri.parse(new File(path.toString()).getCanonicalPath());
                    } catch (Exception e) {
                        Log.e("RingtoneHelper", "getUriByPath : IOException = " + e.getMessage());
                    }
                    try {
                        cursor = getCursor(context, path, ROOT_EXTERNAL, "_data = ? ");
                        if (isItemsExsit(cursor)) {
                            Log.iRelease("RingtoneHelper", "NEW_Media_EXTERNAL");
                            parse = Uri.parse(ROOT_EXTERNAL + "/" + cursor.getInt(0));
                            if (cursor != null) {
                                cursor.close();
                            }
                            return parse;
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                        Log.iRelease("RingtoneHelper", "return DEFAULT_RINGTONE");
                        return Uri.parse("content://settings/system/ringtone1");
                    } catch (SQLiteException e2) {
                        Log.e("RingtoneHelper", "getUriByPath : SQLiteException3 = " + e2.getMessage());
                        if (cursor != null) {
                            cursor.close();
                        }
                    } catch (Throwable th) {
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                } catch (SQLiteException e22) {
                    Log.e("RingtoneHelper", "getUriByPath : SQLiteException2 = " + e22.getMessage());
                    if (cursor != null) {
                        cursor.close();
                    }
                } catch (Throwable th2) {
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            } catch (SQLiteException e222) {
                Log.e("RingtoneHelper", "getUriByPath : SQLiteException1 = " + e222.getMessage());
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th3) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        } else {
            Log.iRelease("RingtoneHelper", "getUriByPath->has no READ_EXTERNAL_STORAGE permissions");
            return Uri.parse("content://settings/system/ringtone1");
        }
    }

    private static boolean isItemsExsit(Cursor cursor) {
        if (cursor == null || !cursor.moveToFirst() || cursor.getInt(0) <= 0) {
            return false;
        }
        return true;
    }

    private static Cursor getCursor(Context context, Uri path, Uri storage, String selection) {
        return context.getContentResolver().query(storage, new String[]{"_id"}, selection, new String[]{path.toString()}, null);
    }
}
