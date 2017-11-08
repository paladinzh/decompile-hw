package com.loc;

import android.text.TextUtils;
import java.util.Hashtable;
import java.util.Locale;

/* compiled from: GeoHash */
public class cb {
    private static final char[] a = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'j', 'k', 'm', 'n', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z'};
    private static final int[] b = new int[]{16, 8, 4, 2, 1};

    private cb() {
    }

    public static final String a(double d, double d2) {
        return a(d, d2, 6);
    }

    public static final String a(double d, double d2, int i) {
        StringBuilder stringBuilder = new StringBuilder();
        Object obj = 1;
        int i2 = 0;
        int i3 = 0;
        double[] dArr = new double[]{-90.0d, 90.0d};
        double[] dArr2 = new double[]{-180.0d, 180.0d};
        while (stringBuilder.length() < i) {
            double d3;
            if (obj == null) {
                d3 = (dArr[0] + dArr[1]) / 2.0d;
                if (d > d3) {
                    i3 |= b[i2];
                    dArr[0] = d3;
                } else {
                    dArr[1] = d3;
                }
            } else {
                d3 = (dArr2[0] + dArr2[1]) / 2.0d;
                if (d2 > d3) {
                    i3 |= b[i2];
                    dArr2[0] = d3;
                } else {
                    dArr2[1] = d3;
                }
            }
            obj = obj != null ? null : 1;
            if (i2 >= 4) {
                stringBuilder.append(a[i3]);
                i2 = 0;
                i3 = 0;
            } else {
                i2++;
            }
        }
        return stringBuilder.toString();
    }

    private static final String a(String str, String str2) {
        if (TextUtils.isEmpty(str) || TextUtils.isEmpty(str2)) {
            return null;
        }
        Object obj;
        String toLowerCase = str.toLowerCase(Locale.US);
        char charAt = toLowerCase.charAt(toLowerCase.length() - 1);
        if (toLowerCase.length() % 2 != 0) {
            obj = "even";
        } else {
            String str3 = "odd";
        }
        toLowerCase = toLowerCase.substring(0, toLowerCase.length() - 1);
        if (!(((String) ((Hashtable) ca$a.a.get(str2)).get(obj)).indexOf(charAt) == -1 || TextUtils.isEmpty(toLowerCase))) {
            toLowerCase = a(toLowerCase, str2);
        }
        return toLowerCase + a[((String) ((Hashtable) ca$b.a.get(str2)).get(obj)).indexOf(charAt)];
    }

    public static final String[] a(String str) {
        return new String[]{a(str, "right"), a(str, "btm"), a(str, "left"), a(str, "top"), a(r0[0], "top"), a(r0[0], "btm"), a(r0[2], "top"), a(r0[2], "btm"), a(r0[0], "right"), a(r0[8], "top"), a(r0[9], "top"), a(r0[10], "left"), a(r0[11], "left"), a(r0[12], "left"), a(r0[13], "left"), a(r0[14], "btm"), a(r0[15], "btm"), a(r0[16], "btm"), a(r0[17], "btm"), a(r0[18], "right"), a(r0[19], "right"), a(r0[20], "right"), a(r0[21], "right"), a(r0[22], "top")};
    }
}
