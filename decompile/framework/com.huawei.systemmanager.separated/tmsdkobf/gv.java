package tmsdkobf;

import android.text.TextUtils;
import java.util.List;

/* compiled from: Unknown */
public class gv {
    private static int pv = 0;
    public String mDescription;
    public String mFileName;
    public int mID;
    public String om;
    public List<String> op;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public int pw;

    public gv() {
        int i = pv + 1;
        pv = i;
        this.mID = i;
    }

    public static void a(StringBuilder stringBuilder, gv gvVar, boolean z, boolean z2) {
        for (String str : gvVar.op) {
            stringBuilder.append('0');
            stringBuilder.append(gvVar.mID);
            stringBuilder.append(':');
            stringBuilder.append('7');
            stringBuilder.append(!z2 ? '0' : '1');
            if (!TextUtils.isEmpty(str)) {
                stringBuilder.append(':');
                stringBuilder.append('1');
                stringBuilder.append(str);
            }
            if (!TextUtils.isEmpty(gvVar.mFileName)) {
                stringBuilder.append(':');
                stringBuilder.append('2');
                stringBuilder.append(gvVar.mFileName);
            }
            if (!TextUtils.isEmpty(gvVar.pb)) {
                stringBuilder.append(':');
                stringBuilder.append('3');
                stringBuilder.append(gvVar.pb);
            }
            if (!TextUtils.isEmpty(gvVar.pc)) {
                stringBuilder.append(':');
                stringBuilder.append('4');
                stringBuilder.append(gvVar.pc);
            }
            if (!TextUtils.isEmpty(gvVar.pd)) {
                stringBuilder.append(':');
                stringBuilder.append('5');
                stringBuilder.append(gvVar.pd);
            }
            if (!TextUtils.isEmpty(gvVar.pe)) {
                stringBuilder.append(':');
                stringBuilder.append('6');
                stringBuilder.append(gvVar.pe);
            }
            stringBuilder.append(';');
        }
    }

    public static void bb() {
        pv = 0;
    }
}
