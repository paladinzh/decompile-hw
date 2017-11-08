package com.huawei.systemmanager.netassistant.traffic.setting.mainpage.model;

import com.huawei.systemmanager.R;

public class PackageInfo implements INumUnit<TrafficUnit> {
    private float num = 0.0f;
    private TrafficUnit unit = TrafficUnit.MB;

    public enum TrafficUnit {
        MB(0, 1048576),
        GB(1, 1073741824);
        
        private long numByte;
        private int resId;

        private TrafficUnit(int id, long num) {
            this.resId = id;
            this.numByte = num;
        }

        public int getUnitId() {
            return this.resId;
        }

        public long getNumByte() {
            return this.numByte;
        }

        public static int getTrafficUnitRes() {
            return R.array.size_unit_no_kb;
        }
    }

    public float getNum() {
        return this.num;
    }

    public TrafficUnit getUnit() {
        return this.unit;
    }

    public TrafficUnit[] getUnitContain() {
        return TrafficUnit.values();
    }

    public long getComputableNum() {
        return (long) (this.num * ((float) this.unit.getNumByte()));
    }

    public void setPackage(float num, TrafficUnit unit) {
        this.num = num;
        this.unit = unit;
    }
}
