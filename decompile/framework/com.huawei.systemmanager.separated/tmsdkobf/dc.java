package tmsdkobf;

import java.io.Serializable;

/* compiled from: Unknown */
public final class dc implements Serializable {
    static final /* synthetic */ boolean fJ = (!dc.class.desiredAssertionStatus());
    private static dc[] gZ = new dc[36];
    public static final dc hA = new dc(26, 26, "EP_King_SuperUser");
    public static final dc hB = new dc(27, 27, "EP_Secure_SDK_Ign");
    public static final dc hC = new dc(28, 28, "EP_Tracker");
    public static final dc hD = new dc(29, 29, "EP_TencentUser");
    public static final dc hE = new dc(30, 30, "EP_Album");
    public static final dc hF = new dc(31, 31, "EP_WeShare");
    public static final dc hG = new dc(32, 32, "EP_Tencent_Cleaner");
    public static final dc hH = new dc(33, 35, "EP_Secure_Mini");
    public static final dc hI = new dc(34, 46, "EP_TMSVirusSDK_Eng");
    public static final dc hJ = new dc(35, 37, "EP_END");
    public static final dc ha = new dc(0, 0, "EP_None");
    public static final dc hb = new dc(1, 1, "EP_Secure");
    public static final dc hc = new dc(2, 2, "EP_Phonebook");
    public static final dc hd = new dc(3, 3, "EP_Pim");
    public static final dc he = new dc(4, 4, "EP_QQPhonebook");
    public static final dc hf = new dc(5, 5, "EP_QZone");
    public static final dc hg = new dc(6, 6, "EP_MobileQQ_Secure");
    public static final dc hh = new dc(7, 7, "EP_QQBrowse_Secure");
    public static final dc hi = new dc(8, 8, "EP_XiaoYou");
    public static final dc hj = new dc(9, 9, "EP_Secure_Eng");
    public static final dc hk = new dc(10, 10, "EP_WBlog");
    public static final dc hl = new dc(11, 11, "EP_Phonebook_Eng");
    public static final dc hm = new dc(12, 12, "EP_AppAssistant");
    public static final dc hn = new dc(13, 13, "EP_Secure_SDK");
    public static final dc ho = new dc(14, 14, "EP_KingRoot");
    public static final dc hp = new dc(15, 15, "EP_Secure_SDK_Pay");
    public static final dc hq = new dc(16, 16, "EP_Secure_Jailbreak");
    public static final dc hr = new dc(17, 17, "EP_KingUser");
    public static final dc hs = new dc(18, 18, "EP_Pim_Pro");
    public static final dc ht = new dc(19, 19, "EP_Pim_Jailbreak");
    public static final dc hu = new dc(20, 20, "EP_PhonebookPro");
    public static final dc hv = new dc(21, 21, "EP_PowerManager");
    public static final dc hw = new dc(22, 22, "EP_BenchMark");
    public static final dc hx = new dc(23, 23, "EP_SecurePro_Enhance");
    public static final dc hy = new dc(24, 24, "EP_Pim_Eng");
    public static final dc hz = new dc(25, 25, "EP_SMS_Fraud_Killer");
    private String gA = new String();
    private int gz;

    private dc(int i, int i2, String str) {
        this.gA = str;
        this.gz = i2;
        gZ[i] = this;
    }

    public static dc k(int i) {
        for (int i2 = 0; i2 < gZ.length; i2++) {
            if (gZ[i2].value() == i) {
                return gZ[i2];
            }
        }
        if (fJ) {
            return null;
        }
        throw new AssertionError();
    }

    public static dc n(String str) {
        for (int i = 0; i < gZ.length; i++) {
            if (gZ[i].toString().equals(str)) {
                return gZ[i];
            }
        }
        if (fJ) {
            return null;
        }
        throw new AssertionError();
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
