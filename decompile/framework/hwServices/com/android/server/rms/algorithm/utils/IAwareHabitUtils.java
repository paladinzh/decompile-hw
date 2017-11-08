package com.android.server.rms.algorithm.utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserManager;
import android.rms.iaware.AwareConfig;
import android.rms.iaware.AwareConfig.Item;
import android.rms.iaware.AwareConfig.SubItem;
import android.rms.iaware.AwareConstant.Database;
import android.rms.iaware.AwareLog;
import android.rms.iaware.ICMSManager;
import android.rms.iaware.ICMSManager.Stub;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.server.rms.iaware.hiber.constant.AppHibernateCst;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class IAwareHabitUtils {
    public static final String APPMNG = "AppManagement";
    public static final String AUTO_APP_SWITCH_DECREMENT = "appSwitch_decrement";
    public static final String AUTO_DECREMENT = "decrement";
    public static final String AUTO_DECREMENT_TYPE = "decrement_type";
    public static final float AVGUSEDFREQUENCY = 0.5f;
    public static final int DECREASE_ROUNDS = 29;
    public static final int DELETED_FLAG = 1;
    private static final String DELETED_TIME_STR = "deletedTime";
    public static final int EMAIL_DB_TYPE = 1;
    public static final int EMAIL_TOP_N = 1;
    public static final String HABIT_CONFIG = "HabitConfig";
    public static final String HABIT_EMAIL_COUNT = "emailCount";
    public static final String HABIT_FILTER_LIST = "HabitFilterList";
    public static final String HABIT_HIGH_END = "highEnd";
    public static final String HABIT_IM_COUNT = "imCount";
    public static final String HABIT_LOW_END = "lowEnd";
    public static final String HABIT_LRU_COUNT = "lruCount";
    public static final String HABIT_MOST_USED_COUNT = "mostUsedCount";
    public static final int HABIT_PROTECT_MAX_TRAIN_COUNTS = 14;
    public static final int IM_DB_TYPE = 0;
    public static final int IM_TOP_N = 3;
    private static final String SELECT_DELETED_SQL = " (select appPkgName from PkgName WHERE deleted=1 AND userID = ?)";
    private static final String SELECT_PKGNAME_SQL = " (select appPkgName from PkgName WHERE userID = ?)";
    private static final String TAG = "IAwareHabitUtils";
    public static final int UNDELETED_FLAG = 0;
    private static final String WHERECLAUSE = "appPkgName =?  and userId = ?";

    public static void loadUsageData(ContentResolver contentResolver, Map<String, Integer> map, int i) {
        if (contentResolver != null && map != null) {
            Cursor query;
            String str = "deleted =0 AND userID=?";
            try {
                query = contentResolver.query(Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes"}, str, new String[]{String.valueOf(i)}, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUsageData ");
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        map.put(query.getString(0), Integer.valueOf(query.getInt(1)));
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void loadAppAssociateInfo(ContentResolver contentResolver, Map<String, Integer> map, ArrayList<ArrayList<Integer>> arrayList, int i) {
        if (contentResolver != null && map != null && arrayList != null) {
            int i2;
            Cursor query;
            if (!arrayList.isEmpty()) {
                arrayList.clear();
            }
            int size = map.size();
            for (i2 = 0; i2 < size; i2++) {
                ArrayList arrayList2 = new ArrayList();
                for (int i3 = 0; i3 < size; i3++) {
                    arrayList2.add(Integer.valueOf(0));
                }
                arrayList.add(arrayList2);
            }
            String str = "srcPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND dstPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND userID=?";
            try {
                query = contentResolver.query(Database.ASSOCIATE_URI, new String[]{"srcPkgName", "dstPkgName", "transitionTimes"}, str, new String[]{String.valueOf(i), String.valueOf(i), String.valueOf(i)}, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadAppAssociateInfo ");
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(0);
                        String string2 = query.getString(1);
                        int i4 = query.getInt(2);
                        if (map.containsKey(string) && map.containsKey(string2)) {
                            int intValue = ((Integer) map.get(string)).intValue();
                            i2 = ((Integer) map.get(string2)).intValue();
                            if (intValue < size && i2 < size) {
                                ((ArrayList) arrayList.get(intValue)).set(i2, Integer.valueOf(i4));
                            }
                        }
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void loadPkgInfo(ContentResolver contentResolver, Map<String, Integer> map, Map<Integer, String> map2, Map<String, Integer> map3, int i) {
        if (contentResolver != null && map != null && map2 != null && map3 != null) {
            Cursor query;
            String str = "deleted =0  AND userID =?";
            try {
                query = contentResolver.query(Database.PKGNAME_URI, new String[]{"appPkgName", "totalUseTimes"}, str, new String[]{String.valueOf(i)}, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadPkgInfo ");
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(0);
                        int i2 = query.getInt(1);
                        if (!map.containsKey(string)) {
                            int size = map.size();
                            map.put(string, Integer.valueOf(size));
                            map2.put(Integer.valueOf(size), string);
                            map3.put(string, Integer.valueOf(i2));
                        }
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static void updatePkgNameTable(Context context) {
        Cursor query;
        if (context != null) {
            ContentResolver contentResolver = context.getContentResolver();
            if (contentResolver != null) {
                try {
                    query = contentResolver.query(Database.PKGRECORD_URI, new String[]{"appPkgName", "flag", "userID"}, null, null, null);
                } catch (SQLiteException e) {
                    AwareLog.e(TAG, "Error: updatePkgNameTable ");
                    query = null;
                }
                if (query != null) {
                    while (query.moveToNext()) {
                        String string = query.getString(0);
                        int i = query.getInt(1);
                        int i2 = query.getInt(2);
                        ContentValues contentValues = new ContentValues();
                        if (i != 0) {
                            contentValues.put("deleted", Integer.valueOf(1));
                            contentValues.put(DELETED_TIME_STR, Integer.valueOf(29));
                            try {
                                contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", new String[]{string, String.valueOf(i2)});
                            } catch (SQLiteException e2) {
                                AwareLog.e(TAG, "Error: updatePkgNameTable ");
                            } catch (Throwable th) {
                                query.close();
                            }
                        } else {
                            contentValues.put("deleted", Integer.valueOf(0));
                            contentValues.put(DELETED_TIME_STR, Integer.valueOf(0));
                            try {
                                contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", new String[]{string, String.valueOf(i2)});
                            } catch (SQLiteException e3) {
                                AwareLog.e(TAG, "Error: updatePkgNameTable ");
                            }
                        }
                    }
                    deleteOverDueInfo(contentResolver, context);
                    ContentValues contentValues2 = new ContentValues();
                    contentValues2.put(DELETED_TIME_STR, Integer.valueOf(1));
                    try {
                        contentResolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), contentValues2, null, null);
                    } catch (SQLiteException e4) {
                        AwareLog.e(TAG, "Error: updatePkgNameTable ");
                    }
                    query.close();
                }
            }
        }
    }

    public static void updateReInstallPkgNameInfo(ContentResolver contentResolver, List<Integer> list) {
        if (contentResolver != null && list != null) {
            Object arraySet = new ArraySet();
            for (int i = 0; i < list.size(); i++) {
                arraySet.clear();
                loadReInstallPkgFromUserData(contentResolver, arraySet, ((Integer) list.get(i)).intValue());
                AwareLog.i(TAG, " update Name userId=" + list.get(i) + " set=" + arraySet + " i=" + i);
                Iterator it = arraySet.iterator();
                while (it.hasNext()) {
                    String str = (String) it.next();
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("deleted", Integer.valueOf(0));
                    contentValues.put(DELETED_TIME_STR, Integer.valueOf(0));
                    try {
                        contentResolver.update(Database.PKGNAME_URI, contentValues, "appPkgName=? AND userID=?", new String[]{str, String.valueOf(list.get(i))});
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error: updateReInstallPkgNameInfo ");
                    }
                }
            }
        }
    }

    private static void loadReInstallPkgFromUserData(ContentResolver contentResolver, Set<String> set, int i) {
        Cursor query;
        String str = "UserData.appPkgName NOT IN (SELECT appPkgName from PkgRecord where userID=?)  AND UserData.appPkgName IN (SELECT appPkgName from PkgName where deleted =1 and userID=?) AND UserData.userID=?";
        try {
            query = contentResolver.query(Database.USERDATA_URI, new String[]{"UserData.appPkgName"}, str, new String[]{String.valueOf(i), String.valueOf(i), String.valueOf(i)}, "UserData._id");
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadReInstallPkgFromUserData ");
            query = null;
        }
        if (query != null) {
            while (query.moveToNext()) {
                try {
                    String string = query.getString(0);
                    if (string != null) {
                        set.add(string);
                    }
                } finally {
                    query.close();
                }
            }
        }
    }

    private static void deleteOverDueInfo(ContentResolver contentResolver, Context context) {
        if (contentResolver != null && context != null) {
            UserManager userManager = UserManager.get(context);
            if (userManager != null) {
                List users = userManager.getUsers();
                if (users != null) {
                    try {
                        contentResolver.delete(Database.PKGNAME_URI, "deleted=1 AND deletedTime=0", null);
                    } catch (SQLiteException e) {
                        AwareLog.e(TAG, "Error: deleteOverDueInfo ");
                    }
                    String str = "userID = ? AND (srcPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?) OR dstPkgName NOT IN  (select appPkgName from PkgName WHERE userID = ?))";
                    for (int i = 0; i < users.size(); i++) {
                        UserInfo userInfo = (UserInfo) users.get(i);
                        if (userInfo != null) {
                            int i2 = userInfo.id;
                            try {
                                contentResolver.delete(Database.ASSOCIATE_URI, str, new String[]{String.valueOf(i2), String.valueOf(i2), String.valueOf(i2)});
                            } catch (SQLiteException e2) {
                                AwareLog.e(TAG, "Error: deleteOverDueInfo ");
                            }
                        }
                    }
                }
            }
        }
    }

    public static void loadUserdataInfo(ContentResolver contentResolver, Map<String, Integer> map, Map<Integer, String> map2, List<Entry<Integer, Long>> list, int i) {
        if (contentResolver != null && map != null && map2 != null && list != null) {
            Cursor query;
            String str = "UserData.appPkgName NOT IN  (select appPkgName from PkgName WHERE deleted=1 AND userID = ?) AND UserData.appPkgName NOT IN (select appPkgName from PkgRecord where flag=1 AND userID = ?)  AND UserData.userID=?";
            try {
                query = contentResolver.query(Database.USERDATA_URI, new String[]{"UserData.appPkgName", "UserData.time"}, str, new String[]{String.valueOf(i), String.valueOf(i), String.valueOf(i)}, "UserData._id");
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUserdataInfo ");
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    try {
                        String string = query.getString(0);
                        long j = query.getLong(1);
                        if (!map.containsKey(string)) {
                            int size = map.size();
                            map.put(string, Integer.valueOf(size));
                            map2.put(Integer.valueOf(size), string);
                        }
                        list.add(new SimpleEntry(map.get(string), Long.valueOf(j)));
                    } finally {
                        query.close();
                    }
                }
            }
        }
    }

    public static List<Integer> getUserIDList(ContentResolver contentResolver) {
        Cursor query;
        String str = "userID >=0 )GROUP BY (userID";
        try {
            query = contentResolver.query(Database.USERDATA_URI, new String[]{"userID"}, str, null, null);
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: getUserIDList ");
            query = null;
        }
        if (query == null) {
            return null;
        }
        List<Integer> arrayList = new ArrayList();
        while (query.moveToNext()) {
            try {
                arrayList.add(Integer.valueOf(query.getInt(0)));
            } finally {
                query.close();
            }
        }
        return arrayList;
    }

    public static void deleteTable(ContentResolver contentResolver) {
        if (contentResolver != null) {
            try {
                contentResolver.delete(Database.ASSOCIATE_URI, null, null);
                contentResolver.delete(Database.PKGNAME_URI, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTable ");
            }
        }
    }

    public static void deleteUserCount(ContentResolver contentResolver, int i) {
        if (contentResolver != null) {
            String str = "userID = ?";
            try {
                contentResolver.delete(Database.ASSOCIATE_URI, str, new String[]{String.valueOf(i)});
                contentResolver.delete(Database.PKGNAME_URI, str, new String[]{String.valueOf(i)});
                contentResolver.delete(Database.PKGRECORD_URI, str, new String[]{String.valueOf(i)});
                contentResolver.delete(Database.USERDATA_URI, str, new String[]{String.valueOf(i)});
                contentResolver.delete(Database.HABITPROTECTLIST_URI, str, new String[]{String.valueOf(i)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteUserCount ");
            }
        }
    }

    public static Set<String> loadRemovedPkg(ContentResolver contentResolver, int i) {
        if (contentResolver == null) {
            return null;
        }
        Cursor query;
        Set<String> arraySet = new ArraySet();
        String str = "flag =1  AND userID =?";
        try {
            query = contentResolver.query(Database.PKGRECORD_URI, new String[]{"appPkgName"}, str, new String[]{String.valueOf(i)}, null);
        } catch (SQLiteException e) {
            AwareLog.e(TAG, "Error: loadRemovedPkg ");
            query = null;
        }
        if (query == null) {
            return arraySet;
        }
        while (query.moveToNext()) {
            try {
                arraySet.add(query.getString(0));
            } finally {
                query.close();
            }
        }
        return arraySet;
    }

    public static void decreaseAppCount(ContentResolver contentResolver) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(1));
            try {
                contentResolver.update(Database.PKGNAME_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_APP_SWITCH_DECREMENT).build(), contentValues, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: decreaseAppCount ");
            }
            return;
        }
        AwareLog.e(TAG, "decreaseAppCount resolver is null");
    }

    public static void insertDataToUserdataTable(ContentResolver contentResolver, String str, long j, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("appPkgName", str);
            contentValues.put("time", Long.valueOf(j));
            contentValues.put("userID", Integer.valueOf(i));
            try {
                contentResolver.insert(Database.USERDATA_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToUserdataTable ");
            }
        }
    }

    public static void deleteTheRemovedPkgFromDB(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            String str2 = "appPkgName =? AND userID =?";
            try {
                contentResolver.delete(Database.PKGRECORD_URI, str2, new String[]{str, String.valueOf(i)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteTheRemovedPkgFromDB ");
            }
        }
    }

    public static void insertDataToPkgRecordTable(ContentResolver contentResolver, ContentValues contentValues) {
        if (contentResolver != null && contentValues != null) {
            try {
                contentResolver.insert(Database.PKGRECORD_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertDataToPkgNameTable ");
            }
        }
    }

    public static void deleteHabitProtectList(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("deleted", Integer.valueOf(1));
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(14));
            try {
                contentResolver.update(Database.HABITPROTECTLIST_URI, contentValues, WHERECLAUSE, new String[]{str, String.valueOf(i)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: deleteHabitProtectList ");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadHabitProtectList(ContentResolver contentResolver, List<ProtectApp> list, int i) {
        Cursor cursor = null;
        if (contentResolver != null && list != null) {
            String str = "userId = ?";
            try {
                cursor = contentResolver.query(Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.deleted", "HabitProtectList.avgUsedFrequency", "HabitProtectList.UserId"}, str, new String[]{String.valueOf(i)}, "CAST (HabitProtectList.avgUsedFrequency AS REAL) desc");
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadHabitProtectList ");
            }
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String string = cursor.getString(0);
                    int i2 = cursor.getInt(1);
                    int i3 = cursor.getInt(2);
                    String string2 = cursor.getString(3);
                    int i4 = cursor.getInt(4);
                    float f = 0.0f;
                    f = Float.parseFloat(string2);
                    list.add(new ProtectApp(string, i2, i3, f, i4));
                }
                cursor.close();
            }
        }
    }

    public static void insertHabitProtectList(ContentResolver contentResolver, String str, int i, int i2) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("appPkgName", str);
            contentValues.put("appType", Integer.valueOf(i));
            contentValues.put("recentUsed", AppHibernateCst.INVALID_PKG);
            contentValues.put("userID", Integer.valueOf(i2));
            AwareLog.d(TAG, "habit protect list insert:" + i + " pkgName:" + str);
            try {
                contentResolver.insert(Database.HABITPROTECTLIST_URI, contentValues);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: insertHabitProtectList ");
            }
        }
    }

    public static void updateDeletedHabitProtectList(ContentResolver contentResolver, String str, int i) {
        if (contentResolver != null && str != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("deleted", Integer.valueOf(0));
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(0));
            AwareLog.d(TAG, "habit protect list update type: pkgName:" + str);
            try {
                contentResolver.update(Database.HABITPROTECTLIST_URI, contentValues, WHERECLAUSE, new String[]{str, String.valueOf(i)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectList ");
            }
        }
    }

    public static void updateDeletedHabitProtectApp(ContentResolver contentResolver) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DELETED_TIME_STR, Integer.valueOf(1));
            try {
                contentResolver.update(Database.HABITPROTECTLIST_URI.buildUpon().appendQueryParameter(AUTO_DECREMENT_TYPE, AUTO_DECREMENT).build(), contentValues, null, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            }
            try {
                contentResolver.delete(Database.HABITPROTECTLIST_URI, "deleted = 1  and deletedTime < 1", null);
            } catch (SQLiteException e2) {
                AwareLog.e(TAG, "Error: updateDeletedHabitProtectApp ");
            }
        }
    }

    public static void updateHabitProtectList(ContentResolver contentResolver, String str, String str2, String str3, int i) {
        if (contentResolver != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("recentUsed", str2);
            contentValues.put("avgUsedFrequency", str3);
            try {
                contentResolver.update(Database.HABITPROTECTLIST_URI, contentValues, WHERECLAUSE, new String[]{str, String.valueOf(i)});
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: updateHabitProtectList ");
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void loadUnDeletedHabitProtectList(ContentResolver contentResolver, List<ProtectApp> list, int i) {
        if (contentResolver != null && list != null) {
            Cursor query;
            try {
                query = contentResolver.query(Database.HABITPROTECTLIST_URI, new String[]{"HabitProtectList.appPkgName", "HabitProtectList.appType", "HabitProtectList.recentUsed", "HabitProtectList.avgUsedFrequency"}, " HabitProtectList.deleted = 0 and userId = ?", new String[]{String.valueOf(i)}, null);
            } catch (SQLiteException e) {
                AwareLog.e(TAG, "Error: loadUnDeletedHabitProtectList ");
                query = null;
            }
            if (query != null) {
                while (query.moveToNext()) {
                    String string = query.getString(0);
                    int i2 = query.getInt(1);
                    String string2 = query.getString(2);
                    float f = 0.0f;
                    f = Float.parseFloat(query.getString(3));
                    list.add(new ProtectApp(string, i2, string2, f));
                }
                query.close();
            }
        }
    }

    private static AwareConfig getConfig(String str, String str2) {
        AwareConfig awareConfig = null;
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            AwareLog.e(TAG, "featureName or configName is null");
            return null;
        }
        try {
            ICMSManager asInterface = Stub.asInterface(ServiceManager.getService("IAwareCMSService"));
            if (asInterface == null) {
                AwareLog.e(TAG, "can not find service IAwareCMSService.");
            } else {
                awareConfig = asInterface.getConfig(str, str2);
            }
        } catch (RemoteException e) {
            AwareLog.e(TAG, "AppMngFeature getConfig RemoteException");
        }
        return awareConfig;
    }

    public static ArraySet<String> getHabitFilterListFromCMS() {
        AwareConfig config = getConfig(APPMNG, HABIT_FILTER_LIST);
        if (config != null) {
            ArraySet<String> arraySet = new ArraySet();
            for (Item item : config.getConfigList()) {
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.e(TAG, "getHabitFilterListFromCMS continue cause null item");
                } else {
                    for (SubItem subItem : item.getSubItemList()) {
                        if (!(subItem == null || TextUtils.isEmpty(subItem.getValue()))) {
                            arraySet.add(subItem.getValue());
                        }
                    }
                }
            }
            AwareLog.d(TAG, "getHabitFilterListFromCMS pkgSet=" + arraySet);
            return arraySet;
        }
        AwareLog.e(TAG, "getHabitFilterListFromCMS failure cause null configList");
        return null;
    }

    public static Map<String, Integer> getConfigFromCMS(String str, String str2) {
        AwareConfig config = getConfig(str, str2);
        if (config != null) {
            Map arrayMap = new ArrayMap();
            for (Item item : config.getConfigList()) {
                if (item == null || item.getSubItemList() == null) {
                    AwareLog.e(TAG, "getConfigFromCMS continue cause null item");
                } else {
                    for (SubItem subItem : item.getSubItemList()) {
                        if (subItem != null) {
                            String name = subItem.getName();
                            if (name != null) {
                                int parseInt;
                                try {
                                    parseInt = Integer.parseInt(subItem.getValue());
                                } catch (NumberFormatException e) {
                                    AwareLog.e(TAG, "getConfigFromCMS NumberFormatException Ex");
                                    parseInt = 0;
                                }
                                arrayMap.put(name, Integer.valueOf(parseInt));
                            }
                        }
                    }
                }
            }
            AwareLog.d(TAG, "getConfigFromCMS config=" + arrayMap);
            return arrayMap;
        }
        AwareLog.e(TAG, "getConfigFromCMS failure cause null configList");
        return null;
    }

    public static boolean isGCMApp(Context context, String str) {
        if (str == null || context == null) {
            return false;
        }
        long currentTimeMillis = System.currentTimeMillis();
        PackageManager packageManager = context.getPackageManager();
        if (packageManager == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.SEND");
        intent.setPackage(str);
        intent.setAction("com.google.android.c2dm.intent.RECEIVE");
        List queryBroadcastReceivers = packageManager.queryBroadcastReceivers(intent, 0);
        AwareLog.d(TAG, "isGCMApp spend time:" + (System.currentTimeMillis() - currentTimeMillis) + " pkg: " + str);
        if (queryBroadcastReceivers == null || queryBroadcastReceivers.size() <= 0) {
            return false;
        }
        AwareLog.i(TAG, "isGCMApp pkg: " + str);
        return true;
    }

    public static boolean isGuestUser(Context context, int i) {
        boolean z = false;
        if (context == null) {
            return false;
        }
        UserManager userManager = UserManager.get(context);
        if (userManager == null) {
            return false;
        }
        UserInfo userInfo = userManager.getUserInfo(i);
        if (userInfo != null) {
            z = userInfo.isGuest();
        }
        return z;
    }
}
