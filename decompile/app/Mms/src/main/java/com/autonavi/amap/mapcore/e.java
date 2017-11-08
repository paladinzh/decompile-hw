package com.autonavi.amap.mapcore;

/* compiled from: VMapDataCache */
class e {
    String a;
    int b;
    int c;
    int d = 0;

    public e(String str, int i) {
        if (str != null) {
            try {
                if (str.length() != 0) {
                    this.b = (int) (System.currentTimeMillis() / 1000);
                    this.c = i;
                    this.a = str;
                }
            } catch (ArrayIndexOutOfBoundsException e) {
                this.a = null;
            }
        }
    }
}
