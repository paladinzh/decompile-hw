package com.fyusion.sdk.viewer.internal.a;

import android.util.Log;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public class a {
    private Map<String, c> a;

    /* compiled from: Unknown */
    private static class a {
        static final a a = new a();
    }

    /* compiled from: Unknown */
    public interface b {
        void a(int i);
    }

    /* compiled from: Unknown */
    public static class c {
        private String a;
        private int b = 0;
        private int c = 0;
        private b d;

        c(String str) {
            this.a = str;
        }

        public void a() {
            this.b++;
            this.c++;
            if (this.d != null) {
                this.d.a(this.c);
            }
            if (this.b >= 10) {
                b();
            }
        }

        void b() {
            if (this.b != 0) {
                b.a(this.a, this.b);
                this.b = 0;
            }
        }

        public String toString() {
            return "[" + this.a + "," + this.b + "," + this.c + "]";
        }
    }

    private a() {
        this.a = new HashMap();
    }

    public static a a() {
        return a.a;
    }

    public synchronized c a(String str) {
        c cVar;
        cVar = (c) this.a.get(str);
        if (cVar == null) {
            cVar = new c(str);
            this.a.put(str, cVar);
        }
        return cVar;
    }

    public synchronized void a(c cVar) {
        if (cVar != null) {
            Log.d("TiltsCollector", "Flush tilts counter: " + cVar);
            cVar.b();
            this.a.remove(cVar.a);
        }
    }
}
