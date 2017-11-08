package cn.com.xy.sms.sdk.net.util;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class e {
    private static final String a = "-noWait";
    private static final String b = "-wait=";
    private static final String c = "-ids=";
    private static final String d = "-domain=";
    private static final String e = "-sql=";
    private int f;
    private boolean g = false;
    private int h = -1;
    private String i;
    private String j;
    private String[] k;
    private Map<String, String> l;
    private String m;

    public e(int i, String str) {
        this.f = i;
        this.m = str;
        d();
    }

    private void a(int i) {
        this.f = i;
    }

    private void a(String str) {
        this.i = str;
    }

    private void a(Map<String, String> map) {
        this.l = map;
    }

    private void a(boolean z) {
        this.g = z;
    }

    private void a(String[] strArr) {
        this.k = strArr;
    }

    private void b(int i) {
        this.h = i;
    }

    private void b(String str) {
        this.j = str;
    }

    private void c(String str) {
        this.m = str;
    }

    private void d() {
        for (String str : this.m.split("\\s(?=-[a-zA-Z]+)")) {
            if (!str.equals(a)) {
                if (str.startsWith(b)) {
                    this.h = Integer.valueOf(str.substring(6)).intValue();
                } else if (str.startsWith(c)) {
                    this.k = str.substring(5).split(",");
                } else if (str.startsWith(d)) {
                    this.i = str.substring(8);
                } else if (str.startsWith(e)) {
                    this.j = str.substring(5);
                } else {
                    if (this.l == null) {
                        this.l = new HashMap();
                    }
                    String[] split = str.split("=");
                    if (split.length >= 2) {
                        this.l.put(split[0], split[1]);
                    } else {
                        this.l.put(split[0], "true");
                    }
                }
            }
            this.g = true;
        }
    }

    private boolean e() {
        return this.g;
    }

    private int f() {
        return this.h;
    }

    private String g() {
        return this.i;
    }

    private Map<String, String> h() {
        return this.l;
    }

    private String i() {
        return this.m;
    }

    public final int a() {
        return this.f;
    }

    public final String b() {
        return this.j;
    }

    public final String[] c() {
        return this.k;
    }

    public final String toString() {
        return new StringBuffer("cmd : ").append(this.m).append("\n targetTo interface:").append(this.f != 0 ? Integer.valueOf(this.f) : "all").append("\n execute right now? ").append(this.g).append("\n just for this ids:").append(this.k != null ? Arrays.toString(this.k) : "all").append("\n reset Wait Date Period to ").append(this.h != -1 ? Integer.valueOf(this.h) : "no change").append("\n reset Domain Url to ").append(this.i != null ? this.i : "no change").append("\n sql:").append(this.j != null ? this.j : "nosql to execute").append("\n other cmd:" + this.l).toString();
    }
}
