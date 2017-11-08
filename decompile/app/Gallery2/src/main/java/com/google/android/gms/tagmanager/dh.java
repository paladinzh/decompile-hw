package com.google.android.gms.tagmanager;

/* compiled from: Unknown */
class dh extends Number implements Comparable<dh> {
    private double XF;
    private long XG;
    private boolean XH = true;

    private dh(long j) {
        this.XG = j;
    }

    public static dh v(long j) {
        return new dh(j);
    }

    public int a(dh dhVar) {
        return (kk() && dhVar.kk()) ? new Long(this.XG).compareTo(Long.valueOf(dhVar.XG)) : Double.compare(doubleValue(), dhVar.doubleValue());
    }

    public byte byteValue() {
        return (byte) ((int) longValue());
    }

    public /* synthetic */ int compareTo(Object x0) {
        return a((dh) x0);
    }

    public double doubleValue() {
        return !kk() ? this.XF : (double) this.XG;
    }

    public boolean equals(Object other) {
        return (other instanceof dh) && a((dh) other) == 0;
    }

    public float floatValue() {
        return (float) doubleValue();
    }

    public int hashCode() {
        return new Long(longValue()).hashCode();
    }

    public int intValue() {
        return km();
    }

    public boolean kj() {
        return !kk();
    }

    public boolean kk() {
        return this.XH;
    }

    public long kl() {
        return !kk() ? (long) this.XF : this.XG;
    }

    public int km() {
        return (int) longValue();
    }

    public short kn() {
        return (short) ((int) longValue());
    }

    public long longValue() {
        return kl();
    }

    public short shortValue() {
        return kn();
    }

    public String toString() {
        return !kk() ? Double.toString(this.XF) : Long.toString(this.XG);
    }
}
