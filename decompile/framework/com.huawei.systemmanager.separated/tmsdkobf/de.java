package tmsdkobf;

/* compiled from: Unknown */
public final class de {
    static final /* synthetic */ boolean fJ = (!de.class.desiredAssertionStatus());
    private static de[] ig = new de[6];
    public static final de ih = new de(0, 0, "ETT_MIN");
    public static final de ii = new de(1, 1, "ETT_RING_ONE_SOUND");
    public static final de ij = new de(2, 2, "ETT_USER_CANCEL");
    public static final de ik = new de(3, 3, "ETT_MISS_CALL");
    public static final de il = new de(4, 4, "ETT_USER_HANG_UP");
    public static final de im = new de(5, 5, "ETT_MAX");
    private String gA = new String();
    private int gz;

    private de(int i, int i2, String str) {
        this.gA = str;
        this.gz = i2;
        ig[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
