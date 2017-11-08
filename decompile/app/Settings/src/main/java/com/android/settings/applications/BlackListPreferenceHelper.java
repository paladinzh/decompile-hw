package com.android.settings.applications;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Map;

public class BlackListPreferenceHelper {
    public static void clearDisableRecord(Context context) {
        if (context != null) {
            SharedPreferences sharedPreference = getBlackListSharePreference(context);
            if (sharedPreference != null) {
                synchronized (BlackListPreferenceHelper.class) {
                    Editor edit = sharedPreference.edit();
                    edit.clear();
                    edit.commit();
                }
            }
        }
    }

    public static void saveDisableRecord(ArrayList<String> mBlackList, Context context) {
        if (mBlackList != null && context != null) {
            SharedPreferences sharedPreference = getBlackListSharePreference(context);
            if (sharedPreference != null) {
                synchronized (BlackListPreferenceHelper.class) {
                    Editor edit = sharedPreference.edit();
                    for (String s : mBlackList) {
                        edit.putBoolean(s, true);
                    }
                    edit.commit();
                }
            }
        }
    }

    public static void saveDisableRecord(String packageName, Context context) {
        if (!TextUtils.isEmpty(packageName) && context != null) {
            SharedPreferences sharedPreference = getBlackListSharePreference(context);
            if (sharedPreference != null) {
                synchronized (BlackListPreferenceHelper.class) {
                    Editor edit = sharedPreference.edit();
                    edit.putBoolean(packageName, true);
                    edit.commit();
                }
            }
        }
    }

    public static void removeDisableRecord(String packageName, Context context) {
        if (!TextUtils.isEmpty(packageName) && context != null) {
            SharedPreferences sharedPreference = getBlackListSharePreference(context);
            if (sharedPreference != null) {
                synchronized (BlackListPreferenceHelper.class) {
                    Editor edit = sharedPreference.edit();
                    edit.remove(packageName);
                    edit.commit();
                }
            }
        }
    }

    public static ArrayList<String> getDisabledAppList(Context context) {
        if (context == null) {
            return null;
        }
        SharedPreferences sharedPreference = getBlackListSharePreference(context);
        if (sharedPreference == null) {
            return null;
        }
        synchronized (BlackListPreferenceHelper.class) {
            Map<String, ?> disableStatusMap = sharedPreference.getAll();
            if (disableStatusMap != null) {
                ArrayList<String> arrayList = new ArrayList(disableStatusMap.keySet());
                return arrayList;
            }
            return null;
        }
    }

    private static SharedPreferences getBlackListSharePreference(Context context) {
        if (context == null) {
            return null;
        }
        return context.getSharedPreferences("disabled_app", 0);
    }
}
