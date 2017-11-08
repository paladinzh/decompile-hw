package com.android.settingslib;

import android.app.ActivityManager;
import android.content.Context;
import com.huawei.bd.Reporter;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONObject;

public class ItemUseStat {
    private static ItemUseStat sInstance;

    public static synchronized ItemUseStat getInstance() {
        ItemUseStat itemUseStat;
        synchronized (ItemUseStat.class) {
            if (sInstance == null) {
                sInstance = new ItemUseStat();
            }
            itemUseStat = sInstance;
        }
        return itemUseStat;
    }

    public static String getShortName(String name) {
        if (name == null) {
            return null;
        }
        return name.substring(name.lastIndexOf(".") + 1);
    }

    public void handleClick(Context context, int level, String name) {
        if (!ActivityManager.isUserAMonkey()) {
            String itemName = getShortName(name);
            Map<String, String> mapObj = new LinkedHashMap();
            mapObj.put("name", itemName);
            handleReport(context, level, new JSONObject(mapObj));
        }
    }

    public void handleClick(Context context, int level, String name, String status) {
        if (!ActivityManager.isUserAMonkey()) {
            String itemName = getShortName(name);
            Map<String, String> mapObj = new LinkedHashMap();
            mapObj.put("name", itemName);
            mapObj.put("status", status);
            handleReport(context, level, new JSONObject(mapObj));
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void handleReport(Context context, int level, JSONObject jsonData) {
        if (!(jsonData == null || context == null || jsonData.length() <= 0)) {
            Reporter.e(context, level, jsonData.toString());
        }
    }
}
