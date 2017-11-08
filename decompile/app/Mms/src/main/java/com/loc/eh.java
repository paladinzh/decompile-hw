package com.loc;

import java.util.List;

/* compiled from: Unknown */
public final class eh {
    private boolean a = false;
    private String b = "";
    private String c = "";
    private boolean d = false;
    private double e = 0.0d;
    private double f = 0.0d;

    protected eh(List list, String str, String str2, String str3) {
        this.b = str;
        this.c = str3;
        d();
    }

    private void d() {
        int i;
        int i2;
        String[] split;
        int i3 = 0;
        String str = this.c;
        if (str != null && str.length() > 8) {
            i = 0;
            for (i2 = 1; i2 < str.length() - 3; i2++) {
                i ^= str.charAt(i2);
            }
            if (Integer.toHexString(i).equalsIgnoreCase(str.substring(str.length() - 2, str.length()))) {
                boolean z = true;
                if (i2 != 0) {
                    str = this.c.substring(0, this.c.length() - 3);
                    i = 0;
                    for (i2 = 0; i2 < str.length(); i2++) {
                        if (str.charAt(i2) != ',') {
                            i++;
                        }
                    }
                    split = str.split(",", i + 1);
                    if (split.length < 6) {
                        return;
                    }
                    if (!(split[2].equals("") || split[split.length - 3].equals("") || split[split.length - 2].equals("") || split[split.length - 1].equals(""))) {
                        Integer.valueOf(split[2]).intValue();
                        this.e = Double.valueOf(split[split.length - 3]).doubleValue();
                        this.f = Double.valueOf(split[split.length - 2]).doubleValue();
                        this.d = true;
                    }
                }
                if (this.b != null) {
                    if (this.b.length() >= 0 && this.b.contains("GPGGA")) {
                        split = this.b.split(",");
                        if (split.length == 15 && split[2] != null && split[2].length() > 0 && split[4] != null && split[4].length() > 0 && Integer.parseInt(split[7]) >= 5 && Double.parseDouble(split[8]) <= 2.7d) {
                            i3 = 1;
                        }
                    }
                }
                this.a = this.d & i3;
            }
        }
        i2 = 0;
        if (i2 != 0) {
            str = this.c.substring(0, this.c.length() - 3);
            i = 0;
            for (i2 = 0; i2 < str.length(); i2++) {
                if (str.charAt(i2) != ',') {
                    i++;
                }
            }
            split = str.split(",", i + 1);
            if (split.length < 6) {
                Integer.valueOf(split[2]).intValue();
                this.e = Double.valueOf(split[split.length - 3]).doubleValue();
                this.f = Double.valueOf(split[split.length - 2]).doubleValue();
                this.d = true;
            } else {
                return;
            }
        }
        try {
            if (this.b != null) {
                split = this.b.split(",");
                i3 = 1;
            }
        } catch (Exception e) {
        }
        this.a = this.d & i3;
    }

    protected final boolean a() {
        return this.a;
    }

    protected final double b() {
        return this.e;
    }

    protected final double c() {
        return this.f;
    }
}
