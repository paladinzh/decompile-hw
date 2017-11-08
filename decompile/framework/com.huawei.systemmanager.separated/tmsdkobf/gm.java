package tmsdkobf;

import android.text.TextUtils;

/* compiled from: Unknown */
public class gm {
    private static int oY = 0;
    public String mDescription;
    public String mFileName;
    public String oZ;
    public String pa;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public String pf;
    public String pg;
    public boolean ph;

    public gm() {
        StringBuilder append = new StringBuilder().append("");
        int i = oY + 1;
        oY = i;
        this.oZ = append.append(i).toString();
    }

    public static void a(StringBuilder stringBuilder, gm gmVar) {
        stringBuilder.append('0');
        stringBuilder.append(gmVar.oZ);
        if (!TextUtils.isEmpty(gmVar.pa)) {
            stringBuilder.append(':');
            stringBuilder.append('1');
            stringBuilder.append(gmVar.pa);
        }
        if (!TextUtils.isEmpty(gmVar.mFileName)) {
            stringBuilder.append(':');
            stringBuilder.append('2');
            stringBuilder.append(gmVar.mFileName);
        }
        if (!TextUtils.isEmpty(gmVar.pb)) {
            stringBuilder.append(':');
            stringBuilder.append('3');
            stringBuilder.append(gmVar.pb);
        }
        if (!TextUtils.isEmpty(gmVar.pc)) {
            stringBuilder.append(':');
            stringBuilder.append('4');
            stringBuilder.append(gmVar.pc);
        }
        if (!TextUtils.isEmpty(gmVar.pd)) {
            stringBuilder.append(':');
            stringBuilder.append('5');
            stringBuilder.append(gmVar.pd);
        }
        if (!TextUtils.isEmpty(gmVar.pe)) {
            stringBuilder.append(':');
            stringBuilder.append('6');
            stringBuilder.append(gmVar.pe);
        }
        stringBuilder.append(':');
        stringBuilder.append('8');
        stringBuilder.append(gmVar.pf);
    }
}
