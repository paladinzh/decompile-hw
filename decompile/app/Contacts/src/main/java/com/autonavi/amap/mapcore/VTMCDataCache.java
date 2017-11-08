package com.autonavi.amap.mapcore;

import java.util.ArrayList;
import java.util.Hashtable;

public class VTMCDataCache {
    public static final int MAXSIZE = 500;
    public static final int MAX_EXPIREDTIME = 300;
    private static VTMCDataCache instance;
    static Hashtable<String, f> vtmcHs = new Hashtable();
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

    public synchronized f getData(String str, boolean z) {
        f fVar = (f) vtmcHs.get(str);
        if (z) {
            return fVar;
        }
        if (fVar == null) {
            return null;
        }
        if (((int) (System.currentTimeMillis() / 1000)) - fVar.c > MAX_EXPIREDTIME) {
            return null;
        }
        if (this.mNewestTimeStamp <= fVar.e) {
            return fVar;
        }
        return null;
    }

    public synchronized f putData(byte[] bArr) {
        f fVar = new f(bArr);
        if (this.mNewestTimeStamp < fVar.e) {
            this.mNewestTimeStamp = fVar.e;
        }
        f fVar2 = (f) vtmcHs.get(fVar.b);
        if (fVar2 != null) {
            if (fVar2.d.equals(fVar.d)) {
                fVar2.a(this.mNewestTimeStamp);
                return fVar2;
            }
            deleteData(fVar.b);
        }
        if (vtmcList.size() > MAXSIZE) {
            vtmcHs.remove(vtmcList.get(0));
            vtmcList.remove(0);
        }
        vtmcHs.put(fVar.b, fVar);
        vtmcList.add(fVar.b);
        return fVar;
    }
}
