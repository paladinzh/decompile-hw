package com.android.util;

import android.content.ContentProviderClient;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.Time;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DayOfWeekRepeatUtil {
    private static boolean hasWorkDayfn = false;
    private static boolean isLoadDate = false;
    private static ArrayList<HashSet<String>> mFreedayList;
    private static int[] mRecessInfoJulianday;
    private static int[] mRecessInfoYear;
    private static ArrayList<HashSet<String>> mWorkdayList;

    public static boolean isLoadDate() {
        return isLoadDate;
    }

    public static boolean isHasWorkDayfn() {
        return hasWorkDayfn;
    }

    public static ArrayList<String> getRecessInfoSharePreferences(Context context, String key, String method) {
        isLoadDate = true;
        ArrayList<String> arrayList = null;
        ContentProviderClient contentProviderClient = null;
        try {
            contentProviderClient = context.getContentResolver().acquireUnstableContentProviderClient(Uri.parse("content://com.android.calendar.Recess"));
            if (contentProviderClient == null) {
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                return null;
            }
            Bundle bundle = contentProviderClient.call(method, null, null);
            if (bundle == null) {
                hasWorkDayfn = false;
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                return null;
            }
            arrayList = bundle.getStringArrayList(key);
            if (arrayList == null) {
                hasWorkDayfn = false;
                if (contentProviderClient != null) {
                    contentProviderClient.release();
                }
                return null;
            }
            hasWorkDayfn = true;
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
            return arrayList;
        } catch (Exception e) {
            Log.w("DayOfWeekRepeatUtil", "getRecessInfoSharePreferences : Exception = " + e.getMessage());
            hasWorkDayfn = false;
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
        } catch (Throwable th) {
            if (contentProviderClient != null) {
                contentProviderClient.release();
            }
        }
    }

    public static synchronized int judgetIsFreeOrWorkDay(int dayOfyear, long alarmTime) {
        int isFreeOrWork;
        synchronized (DayOfWeekRepeatUtil.class) {
            isFreeOrWork = 0;
            if (!(mRecessInfoYear == null || mWorkdayList == null || mFreedayList == null)) {
                Time time = new Time();
                time.set(alarmTime);
                int alarmYear = time.year;
                int j = 0;
                boolean inRecessYear = false;
                int length = mRecessInfoYear.length;
                while (j < length) {
                    if (mRecessInfoYear[j] == alarmYear) {
                        inRecessYear = true;
                        break;
                    }
                    j++;
                }
                if (inRecessYear) {
                    if (mFreedayList.size() > j && mFreedayList.get(j) != null && ((HashSet) mFreedayList.get(j)).contains(Integer.toString(dayOfyear))) {
                        isFreeOrWork = 2;
                    }
                    if (mWorkdayList.size() > j && mWorkdayList.get(j) != null && ((HashSet) mWorkdayList.get(j)).contains(Integer.toString(dayOfyear))) {
                        isFreeOrWork = 1;
                    }
                }
            }
        }
        return isFreeOrWork;
    }

    public static synchronized void initGetRestWork(Context context) {
        JSONException e;
        synchronized (DayOfWeekRepeatUtil.class) {
            HwLog.i("DayOfWeekRepeatUtil", "initGetRestWork");
            ArrayList<String> recessDataArray = getRecessInfoSharePreferences(context, "recessinfokey", "recessInfo");
            if (recessDataArray == null) {
                recessDataArray = getChineseHolidayData(context);
            }
            if (recessDataArray == null) {
                HwLog.w("DayOfWeekRepeatUtil", "get Rest Work data wrong.");
                mRecessInfoJulianday = new int[]{0};
                mRecessInfoYear = new int[]{0};
            } else {
                int size = recessDataArray.size();
                mRecessInfoYear = new int[size];
                mRecessInfoJulianday = new int[size];
                mFreedayList = new ArrayList(size);
                mWorkdayList = new ArrayList(size);
                Time time = new Time("UTC");
                int i = 0;
                for (String recessData : recessDataArray) {
                    if (recessData != null) {
                        try {
                            JSONObject JsonData = new JSONObject(recessData);
                            try {
                                int j;
                                int year = JsonData.getInt("year");
                                Log.iRelease("DayOfWeekRepeatUtil", "year = " + year);
                                saveChineseHolidayData(context, String.valueOf(year), recessData);
                                mRecessInfoYear[i] = year;
                                time.set(0, 0, 0, 1, 0, year);
                                time.normalize(true);
                                mRecessInfoJulianday[i] = Time.getJulianDay(time.toMillis(false), time.gmtoff);
                                JSONArray workday = JsonData.getJSONArray("workday");
                                int arrayLength = workday.length();
                                HashSet<String> workdaySet = new HashSet();
                                for (j = 0; j < arrayLength; j++) {
                                    workdaySet.add(workday.getString(j));
                                }
                                mWorkdayList.add(workdaySet);
                                JSONArray freeday = JsonData.getJSONArray("freeday");
                                arrayLength = freeday.length();
                                HashSet<String> freedaySet = new HashSet();
                                for (j = 0; j < arrayLength; j++) {
                                    freedaySet.add(freeday.getString(j));
                                }
                                mFreedayList.add(freedaySet);
                                JSONObject jSONObject = JsonData;
                            } catch (JSONException e2) {
                                e = e2;
                                Log.e("DayOfWeekRepeatUtil", e.getMessage());
                                mRecessInfoJulianday[i] = 0;
                                mRecessInfoYear[i] = 0;
                                i++;
                            }
                        } catch (JSONException e3) {
                            e = e3;
                            Log.e("DayOfWeekRepeatUtil", e.getMessage());
                            mRecessInfoJulianday[i] = 0;
                            mRecessInfoYear[i] = 0;
                            i++;
                        }
                        i++;
                    }
                }
            }
            if (!(mFreedayList == null || mWorkdayList == null)) {
                Log.dRelease("DayOfWeekRepeatUtil", "HwWidgetMonthView-mFreedayList.size() = " + mFreedayList.size());
                Log.dRelease("DayOfWeekRepeatUtil", "HwWidgetMonthView-mWorkdayList.size() = " + mWorkdayList.size());
                if (mFreedayList.size() == 0 || mWorkdayList.size() == 0) {
                    hasWorkDayfn = false;
                }
            }
        }
    }

    public static void getCalendarWorldData(Context context) {
        Log.dRelease("DayOfWeekRepeatUtil", "getCalendarWorldData");
        if (Utils.isChinaRegionalVersion()) {
            Intent intent = new Intent("com.android.calendar.DOWNLOADACCESS");
            intent.setPackage("com.android.calendar");
            context.sendBroadcast(intent);
        }
    }

    public static void isAcrossYearsNow(Context context) {
        Time time = new Time();
        time.set(System.currentTimeMillis());
        int nowYear = time.year;
        boolean inRecessYear = false;
        for (int i : mRecessInfoYear) {
            if (i == nowYear) {
                inRecessYear = true;
                break;
            }
        }
        Time sevenTime = new Time();
        sevenTime.set(System.currentTimeMillis() + 604800000);
        int sevenYear = sevenTime.year;
        int maxSaveYear = getMaxYear();
        Log.dRelease("DayOfWeekRepeatUtil", "currentYear = " + nowYear + " maxYear = " + maxSaveYear + "after seven day year = " + sevenYear);
        if (inRecessYear || nowYear < maxSaveYear) {
            if (sevenYear <= maxSaveYear) {
                return;
            }
        }
        getCalendarWorldData(context);
    }

    public static int getMaxYear() {
        int maxYear = mRecessInfoYear[0];
        int length = mRecessInfoYear.length;
        for (int j = 0; j < length; j++) {
            if (mRecessInfoYear[j] > maxYear) {
                maxYear = mRecessInfoYear[j];
            }
        }
        return maxYear;
    }

    private static void saveChineseHolidayData(Context context, String year, String value) {
        SharedPreferences sp = Utils.getSharedPreferences(context, "chinese_holiday_data", 0);
        if (sp.getString(year, "").equals(value)) {
            HwLog.i("DayOfWeekRepeatUtil", "chinese holiday data " + year + " is exist");
            return;
        }
        Editor editor = sp.edit();
        editor.putString(year, value);
        editor.commit();
    }

    private static ArrayList<String> getChineseHolidayData(Context context) {
        HwLog.i("DayOfWeekRepeatUtil", "getChineseHolidayInfo");
        ArrayList<String> chineseHolidayInfo = new ArrayList();
        Map<String, String> infoMap = Utils.getSharedPreferences(context, "chinese_holiday_data", 0).getAll();
        if (infoMap == null) {
            HwLog.i("DayOfWeekRepeatUtil", "infoMap is null");
            return null;
        }
        Collection<String> infos = infoMap.values();
        if (infos.isEmpty()) {
            HwLog.i("DayOfWeekRepeatUtil", "infos is empty");
            return null;
        }
        hasWorkDayfn = true;
        for (String value : infos) {
            chineseHolidayInfo.add(value);
        }
        return chineseHolidayInfo;
    }
}
