package com.huawei.hwid.core.d.b.a;

import android.os.Process;
import android.util.Log;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class a {
    private final SimpleDateFormat a;
    private final long b;
    private final long c;
    private final long d;
    private final int e;
    private final String f;
    private final String g;
    private final int h;
    private final String i;
    private final String j;

    public static class a {
        private final long a;
        private final long b;
        private final long c;
        private final int d;
        private final String e;
        private String f;
        private int g;
        private String h;
        private String i;

        private a(int i, String str) {
            this.a = System.currentTimeMillis();
            this.b = (long) Process.myPid();
            this.c = (long) Process.myTid();
            this.d = i;
            this.e = str;
        }

        public a a() {
            return new a();
        }

        public a a(String str) {
            this.h = str;
            return this;
        }

        public a a(Throwable th) {
            StackTraceElement stackTraceElement = null;
            if (th == null) {
                StackTraceElement[] stackTrace = new IllegalArgumentException().getStackTrace();
                if (stackTrace.length > 2) {
                    stackTraceElement = stackTrace[2];
                }
            } else {
                this.i = Log.getStackTraceString(th);
                stackTraceElement = th.getStackTrace()[0];
            }
            if (stackTraceElement != null) {
                this.f = stackTraceElement.getFileName();
                this.g = stackTraceElement.getLineNumber();
            }
            return this;
        }
    }

    private a(a aVar) {
        this.a = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
        this.b = aVar.a;
        this.c = aVar.b;
        this.d = aVar.c;
        this.e = aVar.d;
        this.f = aVar.e;
        this.g = aVar.f;
        this.h = aVar.g;
        this.i = aVar.h;
        this.j = aVar.i;
    }

    public void a(StringBuilder stringBuilder) {
        stringBuilder.append("[");
        stringBuilder.append(this.a.format(Long.valueOf(this.b)));
        stringBuilder.append(" ");
        stringBuilder.append(a(this.e)).append("/").append(this.f);
        stringBuilder.append(" ");
        stringBuilder.append(this.c).append(":").append(this.d);
        stringBuilder.append(" ");
        stringBuilder.append(this.g).append(":").append(this.h);
        stringBuilder.append("]");
    }

    public void b(StringBuilder stringBuilder) {
        stringBuilder.append(this.i);
    }

    public void c(StringBuilder stringBuilder) {
        if (this.j != null) {
            stringBuilder.append('\n').append(this.j);
        }
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        a(stringBuilder);
        stringBuilder.append(" ");
        b(stringBuilder);
        c(stringBuilder);
        stringBuilder.append("\n");
        return stringBuilder.toString();
    }

    private static char a(int i) {
        switch (i) {
            case 2:
                return 'V';
            case 3:
                return 'D';
            case 4:
                return 'I';
            case 5:
                return 'W';
            case 6:
                return 'E';
            case 7:
                return 'A';
            default:
                return 'V';
        }
    }

    public static a a(int i, String str) {
        return new a(i, str);
    }
}
