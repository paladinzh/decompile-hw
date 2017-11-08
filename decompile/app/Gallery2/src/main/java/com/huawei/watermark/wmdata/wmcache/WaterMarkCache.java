package com.huawei.watermark.wmdata.wmcache;

import com.huawei.watermark.manager.parse.WaterMark;
import com.huawei.watermark.wmutil.WMCollectionUtil;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.WeakHashMap;

public class WaterMarkCache {
    private static WaterMarkCache instance;
    private Map<String, ArrayList<WaterMark>> mWaterMarks = new WeakHashMap();

    private WaterMarkCache() {
    }

    public static synchronized WaterMarkCache getInstance() {
        WaterMarkCache waterMarkCache;
        synchronized (WaterMarkCache.class) {
            if (instance == null) {
                instance = new WaterMarkCache();
            }
            waterMarkCache = instance;
        }
        return waterMarkCache;
    }

    public synchronized void release() {
        releaseWaterMarks();
    }

    private synchronized void releaseWaterMarks() {
        if (!WMCollectionUtil.isEmptyCollection(this.mWaterMarks)) {
            for (Entry<String, ArrayList<WaterMark>> entry : this.mWaterMarks.entrySet()) {
                ArrayList<WaterMark> wmlist = (ArrayList) entry.getValue();
                if (!(wmlist == null || wmlist.isEmpty())) {
                    for (WaterMark wm : wmlist) {
                        wm.destoryWaterMark();
                    }
                }
            }
            this.mWaterMarks.clear();
        }
    }

    public synchronized ArrayList<WaterMark> getWaterMark(String path) {
        return (ArrayList) this.mWaterMarks.get(path);
    }

    public synchronized void storeWaterMark(String path, ArrayList<WaterMark> waterMark) {
        this.mWaterMarks.put(path, waterMark);
    }

    public synchronized void releaseWaterMark(String path) {
        this.mWaterMarks.remove(path);
    }
}
