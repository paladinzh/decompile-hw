package com.autonavi.amap.mapcore;

/* compiled from: VMapDataCache */
class VMapDataRecoder {
    int mDataSource;
    String mGridName;
    int mcreateTime;
    int times = 0;

    public VMapDataRecoder(String str, int i) {
        if (str != null) {
            try {
                if (str.length() != 0) {
                    this.mcreateTime = (int) (System.currentTimeMillis() / 1000);
                    this.mDataSource = i;
                    this.mGridName = str;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                this.mGridName = null;
            }
        }
    }
}
