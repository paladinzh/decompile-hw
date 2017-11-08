package tmsdkobf;

import java.io.Serializable;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

/* compiled from: Unknown */
public final class dd implements Serializable {
    static final /* synthetic */ boolean fJ = (!dd.class.desiredAssertionStatus());
    private static dd[] hK = new dd[20];
    public static final dd hL = new dd(0, 0, "ESP_NONE");
    public static final dd hM = new dd(1, SmsCheckResult.ESCT_NORMAL, "ESP_Symbian_V3");
    public static final dd hN = new dd(2, SmsCheckResult.ESCT_PAY, "ESP_Symbian_V5");
    public static final dd hO = new dd(3, 103, "ESP_Symbian_V2");
    public static final dd hP = new dd(4, 104, "ESP_Symbian_3");
    public static final dd hQ = new dd(5, SmsCheckResult.ESCT_201, "ESP_Android_General");
    public static final dd hR = new dd(6, SmsCheckResult.ESCT_202, "ESP_Android_Pad");
    public static final dd hS = new dd(7, SmsCheckResult.ESCT_203, "ESP_Android_HD");
    public static final dd hT = new dd(8, SmsCheckResult.ESCT_301, "ESP_Iphone_General");
    public static final dd hU = new dd(9, SmsCheckResult.ESCT_302, "ESP_Ipad");
    public static final dd hV = new dd(10, SmsCheckResult.ESCT_303, "ESP_Ipod");
    public static final dd hW = new dd(11, 401, "ESP_Kjava_General");
    public static final dd hX = new dd(12, 402, "ESP_NK_Kjava_General");
    public static final dd hY = new dd(13, 501, "ESP_Server_General");
    public static final dd hZ = new dd(14, 601, "ESP_WinPhone_General");
    public static final dd ia = new dd(15, 602, "ESP_WinPhone_Tablet");
    public static final dd ib = new dd(16, 701, "ESP_MTK_General");
    public static final dd ic = new dd(17, 801, "ESP_BB_General");
    public static final dd ie = new dd(18, 901, "ESP_PC_WindowsGeneral");
    public static final dd if = new dd(19, 902, "ESP_END");
    private String gA = new String();
    private int gz;

    private dd(int i, int i2, String str) {
        this.gA = str;
        this.gz = i2;
        hK[i] = this;
    }

    public String toString() {
        return this.gA;
    }

    public int value() {
        return this.gz;
    }
}
