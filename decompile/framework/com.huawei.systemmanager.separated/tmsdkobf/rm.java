package tmsdkobf;

import tmsdk.common.utils.l;

/* compiled from: Unknown */
public class rm {
    private static int oY = 0;
    public String mFileName;
    public String oZ;
    public String pa;
    public String pb;
    public String pc;
    public String pd;
    public String pe;
    public String pf;

    public rm() {
        StringBuilder append = new StringBuilder().append("");
        int i = oY + 1;
        oY = i;
        this.oZ = append.append(i).toString();
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append('0');
        stringBuilder.append(this.oZ);
        if (l.dl(this.pa)) {
            stringBuilder.append(':');
            stringBuilder.append('1');
            stringBuilder.append(this.pa);
        }
        if (l.dl(this.mFileName)) {
            stringBuilder.append(':');
            stringBuilder.append('2');
            stringBuilder.append(this.mFileName);
        }
        if (l.dl(this.pb)) {
            stringBuilder.append(':');
            stringBuilder.append('3');
            stringBuilder.append(this.pb);
        }
        if (l.dl(this.pc)) {
            stringBuilder.append(':');
            stringBuilder.append('4');
            stringBuilder.append(this.pc);
        }
        if (l.dl(this.pd)) {
            stringBuilder.append(':');
            stringBuilder.append('5');
            stringBuilder.append(this.pd);
        }
        if (l.dl(this.pe)) {
            stringBuilder.append(':');
            stringBuilder.append('6');
            stringBuilder.append(this.pe);
        }
        stringBuilder.append(':');
        stringBuilder.append('8');
        stringBuilder.append(this.pf);
        return stringBuilder.toString();
    }
}
