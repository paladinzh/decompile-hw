package tmsdkobf;

import java.io.Serializable;

/* compiled from: Unknown */
public final class cz implements Serializable {
    static final /* synthetic */ boolean fJ = (!cz.class.desiredAssertionStatus());
    public static final cz gB = new cz(0, 0, "CT_NONE");
    public static final cz gC = new cz(1, 1, "CT_GPRS");
    public static final cz gD = new cz(2, 2, "CT_WIFI");
    public static final cz gE = new cz(3, 3, "CT_GPRS_WAP");
    public static final cz gF = new cz(4, 4, "CT_GPRS_NET");
    public static final cz gG = new cz(5, 5, "CT_3G_NET");
    private static cz[] gy = new cz[6];
    private String gA = new String();
    private int gz;

    private cz(int i, int i2, String str) {
        this.gA = str;
        this.gz = i2;
        gy[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
