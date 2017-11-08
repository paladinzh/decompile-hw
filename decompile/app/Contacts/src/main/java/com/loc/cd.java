package com.loc;

import java.util.Locale;

/* compiled from: Cgi */
public class cd {
    public String a = "";
    public String b = "";
    public int c = 0;
    public int d = 0;
    public int e = 0;
    public int f = 0;
    public int g = 0;
    public int h = 0;
    public int i = 0;
    public int j = -113;
    public int k = 9;

    protected cd(int i) {
        this.k = i;
    }

    public boolean a(cd cdVar) {
        if (cdVar == null) {
            return false;
        }
        switch (cdVar.k) {
            case 1:
                return this.k == 1 && cdVar.c == this.c && cdVar.d == this.d && cdVar.b != null && cdVar.b.equals(this.b);
            case 2:
                return this.k == 2 && cdVar.i == this.i && cdVar.h == this.h && cdVar.g == this.g;
            default:
                return false;
        }
    }

    public String toString() {
        String str = "unknown";
        switch (this.k) {
            case 1:
                return String.format(Locale.US, "GSM lac=%d, cid=%d, mnc=%s", new Object[]{Integer.valueOf(this.c), Integer.valueOf(this.d), this.b});
            case 2:
                return String.format(Locale.US, "CDMA bid=%d, nid=%d, sid=%d", new Object[]{Integer.valueOf(this.i), Integer.valueOf(this.h), Integer.valueOf(this.g)});
            default:
                return str;
        }
    }
}
