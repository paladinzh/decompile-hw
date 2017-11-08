package com.autonavi.amap.mapcore;

import java.util.ArrayList;
import java.util.HashMap;

public class VMapDataCache {
    private static final int MAXSIZE = 400;
    private static VMapDataCache instance;
    HashMap<String, VMapDataRecoder> vCancelMapDataHs = new HashMap();
    ArrayList<String> vCancelMapDataList = new ArrayList();
    HashMap<String, VMapDataRecoder> vMapDataHs = new HashMap();
    ArrayList<String> vMapDataList = new ArrayList();

    public static VMapDataCache getInstance() {
        if (instance == null) {
            instance = new VMapDataCache();
        }
        return instance;
    }

    public synchronized void reset() {
        this.vMapDataHs.clear();
        this.vMapDataList.clear();
        this.vCancelMapDataHs.clear();
        this.vCancelMapDataList.clear();
    }

    public int getSize() {
        return this.vMapDataHs.size();
    }

    static String getKey(String str, int i) {
        return str + "-" + i;
    }

    public synchronized VMapDataRecoder getRecoder(String str, int i) {
        VMapDataRecoder vMapDataRecoder;
        vMapDataRecoder = (VMapDataRecoder) this.vMapDataHs.get(getKey(str, i));
        if (vMapDataRecoder != null) {
            vMapDataRecoder.times++;
        }
        return vMapDataRecoder;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized VMapDataRecoder getCancelRecoder(String str, int i) {
        VMapDataRecoder vMapDataRecoder = (VMapDataRecoder) this.vCancelMapDataHs.get(getKey(str, i));
        if (vMapDataRecoder != null) {
            if (((System.currentTimeMillis() / 1000) - ((long) vMapDataRecoder.mcreateTime) <= 10 ? 1 : null) == null) {
                return null;
            }
        }
    }

    public synchronized VMapDataRecoder putRecoder(byte[] bArr, String str, int i) {
        VMapDataRecoder vMapDataRecoder = new VMapDataRecoder(str, i);
        if (vMapDataRecoder.mGridName == null) {
            return null;
        }
        if (this.vMapDataHs.size() > 400) {
            this.vMapDataHs.remove(this.vMapDataList.get(0));
            this.vMapDataList.remove(0);
        }
        this.vMapDataHs.put(getKey(str, i), vMapDataRecoder);
        this.vMapDataList.add(getKey(str, i));
        return vMapDataRecoder;
    }

    public synchronized VMapDataRecoder putCancelRecoder(byte[] bArr, String str, int i) {
        if (getRecoder(str, i) != null) {
            return null;
        }
        VMapDataRecoder vMapDataRecoder = new VMapDataRecoder(str, i);
        if (vMapDataRecoder.mGridName == null) {
            return null;
        }
        if (this.vCancelMapDataHs.size() > 400) {
            this.vCancelMapDataHs.remove(this.vMapDataList.get(0));
            this.vCancelMapDataList.remove(0);
        }
        this.vCancelMapDataHs.put(getKey(str, i), vMapDataRecoder);
        this.vCancelMapDataList.add(getKey(str, i));
        return vMapDataRecoder;
    }
}
