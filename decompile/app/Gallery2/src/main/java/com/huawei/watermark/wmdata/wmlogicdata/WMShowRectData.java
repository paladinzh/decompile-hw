package com.huawei.watermark.wmdata.wmlogicdata;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.huawei.watermark.decoratorclass.WMLog;
import com.huawei.watermark.wmutil.WMStringUtil;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class WMShowRectData {
    private static WMShowRectData instance;
    private static SharedPreferences share = null;
    private int mWMViewpagerHeight;
    private int mWMViewpagerWidth;
    private ConcurrentMap<String, String> mWmMovePositionDataMap = new ConcurrentHashMap();
    private ConcurrentMap<String, ViewSizeObject> mWmViewSizeDataMap = new ConcurrentHashMap();

    public static class ViewSizeObject {
        public float h;
        public float scale;
        public float w;

        public ViewSizeObject(float w, float h, float scale) {
            this.w = w;
            this.h = h;
            this.scale = scale;
        }
    }

    private WMShowRectData(Context context) {
        share = context.getSharedPreferences("wmsharepreferenceshowrectdataname", 0);
    }

    public static synchronized WMShowRectData getInstance(Context context) {
        WMShowRectData wMShowRectData;
        synchronized (WMShowRectData.class) {
            if (instance == null) {
                instance = new WMShowRectData(context);
            }
            wMShowRectData = instance;
        }
        return wMShowRectData;
    }

    public synchronized void clearData() {
        this.mWmMovePositionDataMap.clear();
        this.mWmViewSizeDataMap.clear();
        if (share != null) {
            Editor editor = share.edit();
            editor.clear();
            editor.commit();
        }
    }

    public synchronized void setWMViewSizeData(String key, int w, int h, float scale) {
        String keyname = "wmviewsizedata" + key;
        if (this.mWmViewSizeDataMap.containsKey(keyname)) {
            this.mWmViewSizeDataMap.remove(keyname);
        }
        this.mWmViewSizeDataMap.put("wmviewsizedata" + key, new ViewSizeObject((float) w, (float) h, scale));
    }

    public synchronized void removeWMViewSizeData(String key) {
        String keyname = "wmviewsizedata" + key;
        if (this.mWmViewSizeDataMap.containsKey(keyname)) {
            this.mWmViewSizeDataMap.remove(keyname);
        }
    }

    public synchronized ViewSizeObject getWMViewSizeData(String key) {
        return (ViewSizeObject) this.mWmViewSizeDataMap.get("wmviewsizedata" + key);
    }

    public void setWMMovePositionData(String key, String value) {
        this.mWmMovePositionDataMap.put("wmmovepositiondata" + key, value);
        setStringValue("wmmovepositiondata" + key, value);
    }

    public String getWMMovePositionData(String key, String defaultValue) {
        String res = (String) this.mWmMovePositionDataMap.get("wmmovepositiondata" + key);
        if (!WMStringUtil.isEmptyString(res)) {
            return res;
        }
        res = getStringValue(key, defaultValue);
        this.mWmMovePositionDataMap.put("wmmovepositiondata" + key, res);
        return res;
    }

    public void setWMViewpagerWidth(int w) {
        this.mWMViewpagerWidth = w;
    }

    public int getWMViewpagerWidth() {
        return this.mWMViewpagerWidth;
    }

    public void setWMViewpagerHeight(int h) {
        this.mWMViewpagerHeight = h;
    }

    public int getWMViewpagerHeight() {
        return this.mWMViewpagerHeight;
    }

    private float[] getWMFloatDataByKey(ConcurrentMap<String, String> dataMap, String key, String split) {
        String temp = (String) dataMap.get(key);
        if (temp == null) {
            return new float[2];
        }
        String[] resStr = temp.split(split);
        if (resStr.length != 2) {
            return new float[2];
        }
        float[] res = new float[2];
        try {
            res[0] = Float.parseFloat(resStr[0]);
            res[1] = Float.parseFloat(resStr[1]);
        } catch (Exception e) {
            WMLog.d("WMLogicData", " getWMViewSizeData e");
        }
        return res;
    }

    public synchronized void updateViewSizeAndPositionByNewScale(float scale) {
        for (String key : this.mWmMovePositionDataMap.keySet()) {
            float[] wh = getWMFloatDataByKey(this.mWmMovePositionDataMap, key, "\\|");
            wh[0] = wh[0] * scale;
            wh[1] = wh[1] * scale;
            this.mWmMovePositionDataMap.put(key, wh[0] + "|" + wh[1]);
        }
    }

    private synchronized void setStringValue(String value, String key) {
        if (share != null) {
            Editor editor = share.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    private synchronized String getStringValue(String key, String defaultValue) {
        if (share == null) {
            return defaultValue;
        }
        return share.getString(key, defaultValue);
    }
}
