package com.autonavi.amap.mapcore;

import java.util.ArrayList;
import java.util.Hashtable;

public class VTMCDataCache {
    public static final int MAXSIZE = 500;
    public static final int MAX_EXPIREDTIME = 300;
    private static VTMCDataCache instance;
    static Hashtable<String, VTmcData> vtmcHs = new Hashtable();
    static ArrayList<String> vtmcList = new ArrayList();
    public int mNewestTimeStamp = 0;

    public static VTMCDataCache getInstance() {
        if (instance == null) {
            instance = new VTMCDataCache();
        }
        return instance;
    }

    public void reset() {
        vtmcList.clear();
        vtmcHs.clear();
    }

    public int getSize() {
        return vtmcList.size();
    }

    private void deleteData(String str) {
        vtmcHs.remove(str);
        for (int i = 0; i < vtmcList.size(); i++) {
            if (((String) vtmcList.get(i)).equals(str)) {
                vtmcList.remove(i);
                return;
            }
        }
    }

    public synchronized VTmcData getData(String str, boolean z) {
        VTmcData vTmcData = (VTmcData) vtmcHs.get(str);
        if (z) {
            return vTmcData;
        }
        if (vTmcData == null) {
            return null;
        }
        if (((int) (System.currentTimeMillis() / 1000)) - vTmcData.createTime > 300) {
            return null;
        }
        if (this.mNewestTimeStamp <= vTmcData.timeStamp) {
            return vTmcData;
        }
        return null;
    }

    public synchronized VTmcData putData(byte[] bArr) {
        VTmcData vTmcData = new VTmcData(bArr);
        if (this.mNewestTimeStamp < vTmcData.timeStamp) {
            this.mNewestTimeStamp = vTmcData.timeStamp;
        }
        VTmcData vTmcData2 = (VTmcData) vtmcHs.get(vTmcData.girdName);
        if (vTmcData2 != null) {
            if (vTmcData2.eTag.equals(vTmcData.eTag)) {
                vTmcData2.updateTimeStamp(this.mNewestTimeStamp);
                return vTmcData2;
            }
            deleteData(vTmcData.girdName);
        }
        if (vtmcList.size() > 500) {
            vtmcHs.remove(vtmcList.get(0));
            vtmcList.remove(0);
        }
        vtmcHs.put(vTmcData.girdName, vTmcData);
        vtmcList.add(vTmcData.girdName);
        return vTmcData;
    }
}
