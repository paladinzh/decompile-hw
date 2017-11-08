package com.autonavi.amap.mapcore;

public class MapSourceGridData {
    public String gridName;
    public String keyGridName;
    public int mIndoorIndex;
    public int mIndoorVersion;
    public Object obj = null;
    public int sourceType;

    public MapSourceGridData(String str, int i) {
        this.gridName = str;
        this.sourceType = i;
        this.keyGridName = i + "-" + this.gridName;
    }

    public MapSourceGridData(String str, int i, int i2, int i3) {
        this.gridName = str;
        this.mIndoorIndex = i2;
        this.mIndoorVersion = i3;
        this.sourceType = i;
        this.keyGridName = i + "-" + this.gridName + "-" + i2;
    }

    public String getKeyGridName() {
        return this.keyGridName;
    }

    public int getSourceType() {
        return this.sourceType;
    }

    public String getGridName() {
        return this.gridName;
    }
}
