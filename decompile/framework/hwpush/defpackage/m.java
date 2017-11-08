package defpackage;

/* renamed from: m */
public class m {
    private int I;
    private String J;
    private byte[] mMsgData;
    private String mPackageName;
    private byte[] mToken;

    public m(String str, byte[] bArr, byte[] bArr2, int i, String str2) {
        if (bArr != null && bArr2 != null) {
            this.mPackageName = str;
            this.mToken = new byte[bArr.length];
            System.arraycopy(bArr, 0, this.mToken, 0, bArr.length);
            this.mMsgData = new byte[bArr2.length];
            System.arraycopy(bArr2, 0, this.mMsgData, 0, bArr2.length);
            this.I = i;
            this.J = str2;
        }
    }

    public byte[] aD() {
        return this.mToken;
    }

    public byte[] aE() {
        return this.mMsgData;
    }

    public int aF() {
        return this.I;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String l() {
        return this.J;
    }
}
