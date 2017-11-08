package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.d$a;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
class cr {

    /* compiled from: Unknown */
    public static class a {
        private final Map<String, d$a> Ws;
        private final d$a Wt;

        public void a(String str, d$a d_a) {
            this.Ws.put(str, d_a);
        }

        public Map<String, d$a> jF() {
            return Collections.unmodifiableMap(this.Ws);
        }

        public d$a jG() {
            return this.Wt;
        }

        public String toString() {
            return "Properties: " + jF() + " pushAfterEvaluate: " + this.Wt;
        }
    }

    /* compiled from: Unknown */
    public static class e {
        private final List<a> WA;
        private final List<a> WB;
        private final List<a> WC;
        private final List<String> WF;
        private final List<String> WG;
        private final List<a> Wx;
        private final List<a> Wy;
        private final List<a> Wz;

        public List<a> jN() {
            return this.Wx;
        }

        public List<a> jO() {
            return this.Wy;
        }

        public List<a> jP() {
            return this.Wz;
        }

        public List<a> jQ() {
            return this.WA;
        }

        public List<a> jR() {
            return this.WB;
        }

        public List<String> jU() {
            return this.WF;
        }

        public List<String> jV() {
            return this.WG;
        }

        public List<a> jW() {
            return this.WC;
        }

        public String toString() {
            return "Positive predicates: " + jN() + "  Negative predicates: " + jO() + "  Add tags: " + jP() + "  Remove tags: " + jQ() + "  Add macros: " + jR() + "  Remove macros: " + jW();
        }
    }

    public static d$a g(d$a d_a) {
        d$a d_a2 = new d$a();
        d_a2.type = d_a.type;
        d_a2.gh = (int[]) d_a.gh.clone();
        if (d_a.gi) {
            d_a2.gi = d_a.gi;
        }
        return d_a2;
    }
}
