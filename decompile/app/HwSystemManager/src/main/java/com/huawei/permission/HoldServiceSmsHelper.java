package com.huawei.permission;

import android.content.Context;
import android.os.Binder;
import android.provider.Telephony.Sms;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.permissionmanager.db.RecommendDBHelper;
import com.huawei.permissionmanager.utils.SmsParseUtil;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class HoldServiceSmsHelper {
    private static final int ALLOW_MAX_SPEED = 2000;
    private static final int COUNT_TEN_SMS = 10;
    private static final String HUAWEI_SMS_APPLICATION = "com.android.contacts";
    private static final String LOG_TAG = "HoldServiceSmsHelper";
    private static final long MIN_INTERVAL_TIME = 120000;
    private static final long PERIOD_TIME_FOR_TEN_SMS = 20000;
    private static final int PERMISSION_TYPE_ALLOWED = 1;
    private static final int REMIND_INTERVAL = 30000;
    private static final String SEND_MUTIL_MMS_STATUS = "true";
    private static Map<String, Long> mRemindRecord = new HashMap();
    private static Map<String, Map<Long, String>> mSmsRecordTable = new HashMap();
    private static Object syncSmsTableLock = new Object();

    public static int getPrePermissionTypeAfterGroupSendFilter(int operationType, int permissionType, String pkgName, Context context, String desAddr) {
        int filterResult = operationType;
        if (1 == operationType) {
            if (32 == permissionType && !isDefaultSmsApplication(pkgName, context) && isGroupSendMonitorEnable(context)) {
                try {
                    filterResult = getSendSmsOperationType(pkgName, desAddr);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
            if (8192 == permissionType && !isDefaultSmsApplication(pkgName, context) && isGroupSendMonitorEnable(context)) {
                try {
                    filterResult = getSendMmsOperationType(desAddr);
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                }
            }
        }
        if (Log.HWLog) {
            HwLog.v(LOG_TAG, "Return value is: " + filterResult);
        }
        return filterResult;
    }

    private static boolean isGroupSendMonitorEnable(Context context) {
        boolean sendGroupMonitorEnable = false;
        long identity = Binder.clearCallingIdentity();
        try {
            sendGroupMonitorEnable = RecommendDBHelper.getInstance(context).getSendGroupSmsStatusForServiceProcess();
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "isGroupSendMonitorEnable get Exception!");
        } finally {
            Binder.restoreCallingIdentity(identity);
        }
        return sendGroupMonitorEnable;
    }

    private static boolean isDefaultSmsApplication(String pkgName, Context context) {
        if (Log.HWLog) {
            HwLog.v(LOG_TAG, "isDefaultSmsApplication current pkgName is: " + pkgName);
        }
        if (TextUtils.isEmpty(pkgName) || pkgName.equals("com.android.contacts")) {
            return true;
        }
        String defaultPkgName = null;
        try {
            defaultPkgName = Sms.getDefaultSmsPackage(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return pkgName.equals(defaultPkgName);
    }

    private static int getSendSmsOperationType(String pkgName, String desAddr) {
        long currentTime = System.currentTimeMillis();
        String phoneNumber = SmsParseUtil.getSmsPhoneNumber(desAddr);
        int operationType = 1;
        Map<Long, String> rowMap = new HashMap();
        synchronized (syncSmsTableLock) {
            if (mSmsRecordTable.get(pkgName) != null) {
                rowMap.putAll((Map) mSmsRecordTable.get(pkgName));
                for (Entry<Long, String> entry : rowMap.entrySet()) {
                    if (MIN_INTERVAL_TIME < currentTime - ((Long) entry.getKey()).longValue()) {
                        remove(pkgName, (Long) entry.getKey());
                    }
                }
            }
            Map<Long, String> map = getOrCreate(pkgName);
            map.put(Long.valueOf(currentTime), phoneNumber);
            mSmsRecordTable.put(pkgName, map);
            rowMap.clear();
            rowMap.putAll((Map) mSmsRecordTable.get(pkgName));
        }
        if (!isExceedTheMaxSpeed(getTheEarliestTime(rowMap.keySet()), currentTime, rowMap.size())) {
            return 1;
        }
        HwLog.v(LOG_TAG, "Already exceed the max speed ");
        Set<String> numberSet = new HashSet();
        for (Entry<Long, String> entry2 : rowMap.entrySet()) {
            if (20000 >= currentTime - ((Long) entry2.getKey()).longValue()) {
                numberSet.add((String) entry2.getValue());
            }
        }
        HwLog.v(LOG_TAG, "The numberSet size is " + numberSet.size());
        if (10 <= numberSet.size()) {
            Long lastRemindTime = (Long) mRemindRecord.get(pkgName);
            if (lastRemindTime == null) {
                operationType = 1000;
                HwLog.i(LOG_TAG, "Recognize a new SEND_GROUP_SMS operation ,do remind, pkg = " + pkgName);
            } else if (30000 <= currentTime - lastRemindTime.longValue()) {
                operationType = 1000;
                HwLog.i(LOG_TAG, "Recognize a SEND_GROUP_SMS operation again,do remind, pkg = " + pkgName);
            } else {
                HwLog.i(LOG_TAG, "Recognize SEND_GROUP_SMS operation , skip remind this time, pkg = " + pkgName);
            }
            if (1000 == operationType) {
                mRemindRecord.put(pkgName, Long.valueOf(currentTime));
                synchronized (syncSmsTableLock) {
                    for (Entry<Long, String> entry22 : rowMap.entrySet()) {
                        remove(pkgName, (Long) entry22.getKey());
                    }
                }
            }
        }
        return operationType;
    }

    private static long getTheEarliestTime(Collection<Long> timeSet) {
        TreeSet<Long> treeTimeSet = new TreeSet(timeSet);
        treeTimeSet.comparator();
        return ((Long) treeTimeSet.first()).longValue();
    }

    private static boolean isExceedTheMaxSpeed(long timeBegin, long timeEnd, int count) {
        boolean exceedStatus = false;
        if (count <= 0 || timeEnd <= timeBegin) {
            return false;
        }
        int speed = (int) ((timeEnd - timeBegin) / ((long) count));
        HwLog.v(LOG_TAG, "The current send speed is = " + speed);
        if (2000 > speed) {
            exceedStatus = true;
        }
        return exceedStatus;
    }

    private static int getSendMmsOperationType(String desAddr) {
        if ("true".equalsIgnoreCase(desAddr)) {
            return 1001;
        }
        return 1;
    }

    public static void removeTheTableRecorderForGivenPackage(String pkgName, int permissionType) {
        if (1000 == permissionType) {
            synchronized (syncSmsTableLock) {
                if (mSmsRecordTable.containsKey(pkgName)) {
                    Map<Long, String> rowMap = new HashMap();
                    rowMap.putAll((Map) mSmsRecordTable.get(pkgName));
                    for (Long key : rowMap.keySet()) {
                        remove(pkgName, key);
                    }
                }
            }
        }
    }

    private static String remove(String rowKey, Long columnKey) {
        if (rowKey == null || columnKey == null) {
            return null;
        }
        Map<Long, String> map = (Map) mSmsRecordTable.get(rowKey);
        if (map == null) {
            return null;
        }
        String value = (String) map.remove(columnKey);
        if (map.isEmpty()) {
            mSmsRecordTable.remove(rowKey);
        }
        return value;
    }

    private static Map<Long, String> getOrCreate(String rowKey) {
        Map<Long, String> map = (Map) mSmsRecordTable.get(rowKey);
        if (map != null) {
            return map;
        }
        map = new HashMap();
        mSmsRecordTable.put(rowKey, map);
        return map;
    }
}
