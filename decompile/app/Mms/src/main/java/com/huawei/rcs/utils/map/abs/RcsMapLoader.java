package com.huawei.rcs.utils.map.abs;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import com.android.rcs.RcsCommonConfig;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsTransaction;
import java.util.HashMap;
import org.json.JSONObject;

public abstract class RcsMapLoader {
    public abstract void loadMap(Context context, HashMap<String, String> hashMap);

    public abstract void requestMap(Context context, int i);

    public static HashMap<String, String> getLocInfo(String mBodyTextView) {
        HashMap<String, String> locInfo = new HashMap();
        try {
            JSONObject json = new JSONObject(mBodyTextView);
            if (mBodyTextView == null || !mBodyTextView.contains("body")) {
                locInfo.put("title", "");
                locInfo.put("subtitle", "");
            } else {
                String address = json.get("body").toString().replaceAll("(\r\n)|\n", "");
                if (address.indexOf(";") <= 0 || address.length() - 1 <= address.indexOf(";")) {
                    locInfo.put("title", address);
                    locInfo.put("subtitle", address);
                } else {
                    String addressTitle = address.substring(0, address.indexOf(";"));
                    String addressSubtitle = address.substring(address.indexOf(";") + 1);
                    locInfo.put("title", addressTitle);
                    locInfo.put("subtitle", addressSubtitle);
                }
            }
            locInfo.put("longitude", json.get("longitude").toString());
            locInfo.put("latitude", json.get("latitude").toString());
        } catch (Exception e) {
            MLog.d("RcsMapLoader", "getLocInfo error");
        }
        return locInfo;
    }

    public static boolean isLocItem(String body) {
        if (!RcsCommonConfig.isRCSSwitchOn() || body == null || body.equals("")) {
            return false;
        }
        String latitude_tmp = "";
        String longitude_tmp = "";
        try {
            JSONObject object = new JSONObject(body);
            latitude_tmp = object.get("latitude").toString();
            longitude_tmp = object.get("longitude").toString();
        } catch (Exception e) {
            MLog.d("RcsMapLoader", "isLocItem: this is loc item.");
        }
        if (latitude_tmp.equals("") || longitude_tmp.equals("")) {
            return false;
        }
        return true;
    }

    public static boolean isLocItem(Cursor cursor, int position) {
        if (!RcsCommonConfig.isRCSSwitchOn() || cursor == null) {
            return false;
        }
        boolean result;
        int prePosition = cursor.getPosition();
        cursor.moveToPosition(position);
        String longitude_tmp = "";
        String latitude_tmp = "";
        try {
            JSONObject object = new JSONObject(cursor.getString(4));
            latitude_tmp = object.get("latitude").toString();
            longitude_tmp = object.get("longitude").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (latitude_tmp.equals("") || longitude_tmp.equals("")) {
            MLog.d("RcsMapLoader", "isLocItem: this is not a loc item.");
            result = false;
        } else {
            MLog.d("RcsMapLoader", "isLocItem: this is loc item.");
            result = true;
        }
        cursor.moveToPosition(prePosition);
        return result;
    }

    public void sendGroupLocation(Intent data, String mGroupID) {
        if (data != null) {
            String str = mGroupID;
            RcsTransaction.groupSendLocation(str, data.getDoubleExtra("x", 0.0d), data.getDoubleExtra("y", 0.0d), data.getStringExtra("city"), data.getStringExtra("address"));
        }
    }
}
