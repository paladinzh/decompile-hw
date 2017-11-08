package com.autonavi.amap.mapcore;

import java.util.ArrayList;
import java.util.HashMap;

public class VMapDataCache {
    private static final int MAXSIZE = 400;
    private static VMapDataCache instance;
    HashMap<String, e> vCancelMapDataHs = new HashMap();
    ArrayList<String> vCancelMapDataList = new ArrayList();
    HashMap<String, e> vMapDataHs = new HashMap();
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

    public synchronized e getRecoder(String str, int i) {
        e eVar;
        eVar = (e) this.vMapDataHs.get(getKey(str, i));
        if (eVar != null) {
            eVar.d++;
        }
        return eVar;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized e getCancelRecoder(String str, int i) {
        e eVar = (e) this.vCancelMapDataHs.get(getKey(str, i));
        if (eVar != null) {
            if (((System.currentTimeMillis() / 1000) - ((long) eVar.b) <= 10 ? 1 : null) == null) {
                return null;
            }
        }
    }

    public synchronized e putRecoder(byte[] bArr, String str, int i) {
        e eVar = new e(str, i);
        if (eVar.a == null) {
            return null;
        }
        if (this.vMapDataHs.size() > MAXSIZE) {
            this.vMapDataHs.remove(this.vMapDataList.get(0));
            this.vMapDataList.remove(0);
        }
        this.vMapDataHs.put(getKey(str, i), eVar);
        this.vMapDataList.add(getKey(str, i));
        return eVar;
    }

    public synchronized e putCancelRecoder(byte[] bArr, String str, int i) {
        if (getRecoder(str, i) != null) {
            return null;
        }
        e eVar = new e(str, i);
        if (eVar.a == null) {
            return null;
        }
        if (this.vCancelMapDataHs.size() > MAXSIZE) {
            this.vCancelMapDataHs.remove(this.vMapDataList.get(0));
            this.vCancelMapDataList.remove(0);
        }
        this.vCancelMapDataHs.put(getKey(str, i), eVar);
        this.vCancelMapDataList.add(getKey(str, i));
        return eVar;
    }
}
