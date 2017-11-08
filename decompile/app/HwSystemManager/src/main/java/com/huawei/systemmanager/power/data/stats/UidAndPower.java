package com.huawei.systemmanager.power.data.stats;

import com.android.internal.os.BatterySipper;
import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import java.io.Serializable;
import java.util.Comparator;

public class UidAndPower {
    private double power;
    private BatterySipper sipper;
    private int uid;

    public static class Cmp implements Comparator<UidAndPower>, Serializable {
        private static final long serialVersionUID = -1;

        public int compare(UidAndPower arg0, UidAndPower arg1) {
            if (arg0.power > arg1.power) {
                return -1;
            }
            if (arg0.power < arg1.power) {
                return 1;
            }
            return 0;
        }
    }

    public UidAndPower(int id, double pwr, BatterySipper sipper) {
        this.uid = id;
        this.power = pwr;
        this.sipper = sipper;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("UidAndPowe[").append(this.uid).append(SqlMarker.COMMA_SEPARATE).append(this.power).append("]");
        return buf.toString();
    }

    public int getUid() {
        return this.uid;
    }

    public void setUid(int uid) {
        this.uid = uid;
    }

    public double getPower() {
        return this.power;
    }

    public void setPower(double power) {
        this.power = power;
    }

    public BatterySipper getSipper() {
        return this.sipper;
    }

    public void setSipper(BatterySipper sipper) {
        this.sipper = sipper;
    }
}
