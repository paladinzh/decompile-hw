package tmsdkobf;

import java.io.Serializable;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public final class db implements Serializable {
    static final /* synthetic */ boolean fJ = (!db.class.desiredAssertionStatus());
    private static db[] gM = new db[12];
    public static final db gN = new db(0, 0, "MPT_NONE");
    public static final db gO = new db(1, 1, "MPT_Symbian");
    public static final db gP = new db(2, 2, "MPT_Android");
    public static final db gQ = new db(3, 3, "MPT_Iphone");
    public static final db gR = new db(4, 4, "MPT_Kjava");
    public static final db gS = new db(5, 5, "MPT_Server");
    public static final db gT = new db(6, 6, "MPT_WinPhone");
    public static final db gU = new db(7, 7, "MPT_MTK");
    public static final db gV = new db(8, 8, "MPT_BB");
    public static final db gW = new db(9, 9, "MPT_PC_Windows");
    public static final db gX = new db(10, SmsCheckResult.ESCT_NORMAL, "MPT_SymbianV5");
    public static final db gY = new db(11, SmsCheckResult.ESCT_PAY, "MPT_END");
    private String gA = new String();
    private int gz;

    private db(int i, int i2, String str) {
        this.gA = str;
        this.gz = i2;
        gM[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
