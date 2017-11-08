package com.autonavi.amap.mapcore;

/* compiled from: VTMCDataCache */
class VTmcData {
    int createTime;
    byte[] data;
    String eTag;
    String girdName;
    int timeStamp;

    public VTmcData(byte[] bArr) {
        try {
            this.createTime = (int) (System.currentTimeMillis() / 1000);
            byte b = bArr[4];
            this.girdName = new String(bArr, 5, b, "utf-8");
            int i = b + 5;
            int i2 = i + 1;
            b = bArr[i];
            this.eTag = new String(bArr, i2, b);
            this.timeStamp = Convert.getInt(bArr, b + i2);
            this.data = bArr;
        } catch (Exception e) {
            this.data = null;
        }
    }

    public void updateTimeStamp(int i) {
        if (this.data != null) {
            this.createTime = (int) (System.currentTimeMillis() / 1000);
            int i2 = this.data[4] + 5;
            Convert.writeInt(this.data, this.data[i2] + (i2 + 1), i);
            this.timeStamp = i;
        }
    }
}
