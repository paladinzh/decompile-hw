package com.google.android.gms.tagmanager;

import com.google.android.gms.internal.d$a;
import com.google.android.gms.tagmanager.cr.e;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/* compiled from: Unknown */
class ct {
    private static final by<d$a> WH = new by(di.ku(), true);
    private final DataLayer TN;
    private final ag WJ;
    private final Map<String, aj> WK;
    private final Map<String, aj> WL;
    private final Map<String, aj> WM;
    private final k<com.google.android.gms.tagmanager.cr.a, by<d$a>> WN;
    private final k<String, b> WO;
    private final Set<e> WP;
    private final Map<String, c> WQ;
    private volatile String WR;
    private int WS;

    /* compiled from: Unknown */
    interface a {
        void a(e eVar, Set<com.google.android.gms.tagmanager.cr.a> set, Set<com.google.android.gms.tagmanager.cr.a> set2, cn cnVar);
    }

    /* compiled from: Unknown */
    private static class b {
        private by<d$a> WY;
        private d$a Wt;

        public b(by<d$a> byVar, d$a d_a) {
            this.WY = byVar;
            this.Wt = d_a;
        }

        public d$a jG() {
            return this.Wt;
        }

        public by<d$a> ka() {
            return this.WY;
        }
    }

    /* compiled from: Unknown */
    private static class c {
        private final Set<e> WP = new HashSet();
        private final Map<e, List<com.google.android.gms.tagmanager.cr.a>> WZ = new HashMap();
        private final Map<e, List<com.google.android.gms.tagmanager.cr.a>> Xa = new HashMap();
        private final Map<e, List<String>> Xb = new HashMap();
        private final Map<e, List<String>> Xc = new HashMap();
        private com.google.android.gms.tagmanager.cr.a Xd;

        public Set<e> kb() {
            return this.WP;
        }

        public Map<e, List<com.google.android.gms.tagmanager.cr.a>> kc() {
            return this.WZ;
        }

        public Map<e, List<String>> kd() {
            return this.Xb;
        }

        public Map<e, List<String>> ke() {
            return this.Xc;
        }

        public Map<e, List<com.google.android.gms.tagmanager.cr.a>> kf() {
            return this.Xa;
        }

        public com.google.android.gms.tagmanager.cr.a kg() {
            return this.Xd;
        }
    }

    private by<d$a> a(d$a d_a, Set<String> set, dk dkVar) {
        if (!d_a.gi) {
            return new by(d_a, true);
        }
        d$a g;
        int i;
        by a;
        switch (d_a.type) {
            case 2:
                g = cr.g(d_a);
                g.fZ = new d$a[d_a.fZ.length];
                for (i = 0; i < d_a.fZ.length; i++) {
                    a = a(d_a.fZ[i], (Set) set, dkVar.bS(i));
                    if (a == WH) {
                        return WH;
                    }
                    g.fZ[i] = (d$a) a.getObject();
                }
                return new by(g, false);
            case 3:
                g = cr.g(d_a);
                if (d_a.ga.length == d_a.gb.length) {
                    g.ga = new d$a[d_a.ga.length];
                    g.gb = new d$a[d_a.ga.length];
                    for (i = 0; i < d_a.ga.length; i++) {
                        a = a(d_a.ga[i], (Set) set, dkVar.bT(i));
                        by a2 = a(d_a.gb[i], (Set) set, dkVar.bU(i));
                        if (a == WH || a2 == WH) {
                            return WH;
                        }
                        g.ga[i] = (d$a) a.getObject();
                        g.gb[i] = (d$a) a2.getObject();
                    }
                    return new by(g, false);
                }
                bh.t("Invalid serving value: " + d_a.toString());
                return WH;
            case 4:
                if (set.contains(d_a.gc)) {
                    bh.t("Macro cycle detected.  Current macro reference: " + d_a.gc + "." + "  Previous macro references: " + set.toString() + ".");
                    return WH;
                }
                set.add(d_a.gc);
                by<d$a> a3 = dl.a(a(d_a.gc, (Set) set, dkVar.jq()), d_a.gh);
                set.remove(d_a.gc);
                return a3;
            case 7:
                g = cr.g(d_a);
                g.gg = new d$a[d_a.gg.length];
                for (i = 0; i < d_a.gg.length; i++) {
                    a = a(d_a.gg[i], (Set) set, dkVar.bV(i));
                    if (a == WH) {
                        return WH;
                    }
                    g.gg[i] = (d$a) a.getObject();
                }
                return new by(g, false);
            default:
                bh.t("Unknown type: " + d_a.type);
                return WH;
        }
    }

    private by<d$a> a(String str, Set<String> set, bj bjVar) {
        this.WS++;
        b bVar = (b) this.WO.get(str);
        if (bVar == null || this.WJ.jb()) {
            c cVar = (c) this.WQ.get(str);
            if (cVar != null) {
                com.google.android.gms.tagmanager.cr.a kg;
                by a = a(str, cVar.kb(), cVar.kc(), cVar.kd(), cVar.kf(), cVar.ke(), set, bjVar.iS());
                if (((Set) a.getObject()).isEmpty()) {
                    kg = cVar.kg();
                } else {
                    if (((Set) a.getObject()).size() > 1) {
                        bh.w(jZ() + "Multiple macros active for macroName " + str);
                    }
                    kg = (com.google.android.gms.tagmanager.cr.a) ((Set) a.getObject()).iterator().next();
                }
                com.google.android.gms.tagmanager.cr.a aVar = kg;
                if (aVar != null) {
                    by a2 = a(this.WM, aVar, (Set) set, bjVar.jh());
                    boolean z = a.jr() && a2.jr();
                    by<d$a> byVar = a2 != WH ? new by(a2.getObject(), z) : WH;
                    d$a jG = aVar.jG();
                    if (byVar.jr()) {
                        this.WO.e(str, new b(byVar, jG));
                    }
                    a(jG, (Set) set);
                    this.WS--;
                    return byVar;
                }
                this.WS--;
                return WH;
            }
            bh.t(jZ() + "Invalid macro: " + str);
            this.WS--;
            return WH;
        }
        a(bVar.jG(), (Set) set);
        this.WS--;
        return bVar.ka();
    }

    private by<d$a> a(Map<String, aj> map, com.google.android.gms.tagmanager.cr.a aVar, Set<String> set, ck ckVar) {
        boolean z = true;
        d$a d_a = (d$a) aVar.jF().get(com.google.android.gms.internal.b.FUNCTION.toString());
        if (d_a != null) {
            String str = d_a.gd;
            aj ajVar = (aj) map.get(str);
            if (ajVar != null) {
                by<d$a> byVar = (by) this.WN.get(aVar);
                if (byVar != null && !this.WJ.jb()) {
                    return byVar;
                }
                Map hashMap = new HashMap();
                boolean z2 = true;
                for (Entry entry : aVar.jF().entrySet()) {
                    by a = a((d$a) entry.getValue(), (Set) set, ckVar.bs((String) entry.getKey()).e((d$a) entry.getValue()));
                    if (a == WH) {
                        return WH;
                    }
                    boolean z3;
                    if (a.jr()) {
                        aVar.a((String) entry.getKey(), (d$a) a.getObject());
                        z3 = z2;
                    } else {
                        z3 = false;
                    }
                    hashMap.put(entry.getKey(), a.getObject());
                    z2 = z3;
                }
                if (ajVar.a(hashMap.keySet())) {
                    if (z2) {
                        if (!ajVar.iy()) {
                        }
                        byVar = new by(ajVar.u(hashMap), z);
                        if (z) {
                            this.WN.e(aVar, byVar);
                        }
                        ckVar.d((d$a) byVar.getObject());
                        return byVar;
                    }
                    z = false;
                    byVar = new by(ajVar.u(hashMap), z);
                    if (z) {
                        this.WN.e(aVar, byVar);
                    }
                    ckVar.d((d$a) byVar.getObject());
                    return byVar;
                }
                bh.t("Incorrect keys for function " + str + " required " + ajVar.jd() + " had " + hashMap.keySet());
                return WH;
            }
            bh.t(str + " has no backing implementation.");
            return WH;
        }
        bh.t("No function id in properties");
        return WH;
    }

    private by<Set<com.google.android.gms.tagmanager.cr.a>> a(Set<e> set, Set<String> set2, a aVar, cs csVar) {
        Set hashSet = new HashSet();
        Collection hashSet2 = new HashSet();
        boolean z = true;
        for (e eVar : set) {
            cn jp = csVar.jp();
            by a = a(eVar, (Set) set2, jp);
            if (((Boolean) a.getObject()).booleanValue()) {
                aVar.a(eVar, hashSet, hashSet2, jp);
            }
            boolean z2 = z && a.jr();
            z = z2;
        }
        hashSet.removeAll(hashSet2);
        csVar.b(hashSet);
        return new by(hashSet, z);
    }

    private void a(d$a d_a, Set<String> set) {
        if (d_a != null) {
            by a = a(d_a, (Set) set, new bw());
            if (a != WH) {
                Object o = di.o((d$a) a.getObject());
                if (o instanceof Map) {
                    this.TN.push((Map) o);
                } else if (o instanceof List) {
                    for (Object o2 : (List) o2) {
                        if (o2 instanceof Map) {
                            this.TN.push((Map) o2);
                        } else {
                            bh.w("pushAfterEvaluate: value not a Map");
                        }
                    }
                } else {
                    bh.w("pushAfterEvaluate: value not a Map or List");
                }
            }
        }
    }

    private String jZ() {
        if (this.WS <= 1) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(Integer.toString(this.WS));
        for (int i = 2; i < this.WS; i++) {
            stringBuilder.append(' ');
        }
        stringBuilder.append(": ");
        return stringBuilder.toString();
    }

    by<Boolean> a(com.google.android.gms.tagmanager.cr.a aVar, Set<String> set, ck ckVar) {
        by a = a(this.WL, aVar, (Set) set, ckVar);
        Boolean n = di.n((d$a) a.getObject());
        ckVar.d(di.r(n));
        return new by(n, a.jr());
    }

    by<Boolean> a(e eVar, Set<String> set, cn cnVar) {
        boolean z = true;
        for (com.google.android.gms.tagmanager.cr.a a : eVar.jO()) {
            by a2 = a(a, (Set) set, cnVar.jj());
            if (((Boolean) a2.getObject()).booleanValue()) {
                cnVar.f(di.r(Boolean.valueOf(false)));
                return new by(Boolean.valueOf(false), a2.jr());
            }
            boolean z2 = z && a2.jr();
            z = z2;
        }
        for (com.google.android.gms.tagmanager.cr.a a3 : eVar.jN()) {
            a2 = a(a3, (Set) set, cnVar.jk());
            if (((Boolean) a2.getObject()).booleanValue()) {
                z = z && a2.jr();
            } else {
                cnVar.f(di.r(Boolean.valueOf(false)));
                return new by(Boolean.valueOf(false), a2.jr());
            }
        }
        cnVar.f(di.r(Boolean.valueOf(true)));
        return new by(Boolean.valueOf(true), z);
    }

    by<Set<com.google.android.gms.tagmanager.cr.a>> a(String str, Set<e> set, Map<e, List<com.google.android.gms.tagmanager.cr.a>> map, Map<e, List<String>> map2, Map<e, List<com.google.android.gms.tagmanager.cr.a>> map3, Map<e, List<String>> map4, Set<String> set2, cs csVar) {
        final Map<e, List<com.google.android.gms.tagmanager.cr.a>> map5 = map;
        final Map<e, List<String>> map6 = map2;
        final Map<e, List<com.google.android.gms.tagmanager.cr.a>> map7 = map3;
        final Map<e, List<String>> map8 = map4;
        return a((Set) set, (Set) set2, new a(this) {
            final /* synthetic */ ct WT;

            public void a(e eVar, Set<com.google.android.gms.tagmanager.cr.a> set, Set<com.google.android.gms.tagmanager.cr.a> set2, cn cnVar) {
                List list = (List) map5.get(eVar);
                List list2 = (List) map6.get(eVar);
                if (list != null) {
                    set.addAll(list);
                    cnVar.jl().b(list, list2);
                }
                list = (List) map7.get(eVar);
                list2 = (List) map8.get(eVar);
                if (list != null) {
                    set2.addAll(list);
                    cnVar.jm().b(list, list2);
                }
            }
        }, csVar);
    }

    by<Set<com.google.android.gms.tagmanager.cr.a>> a(Set<e> set, cs csVar) {
        return a((Set) set, new HashSet(), new a(this) {
            final /* synthetic */ ct WT;

            {
                this.WT = r1;
            }

            public void a(e eVar, Set<com.google.android.gms.tagmanager.cr.a> set, Set<com.google.android.gms.tagmanager.cr.a> set2, cn cnVar) {
                set.addAll(eVar.jP());
                set2.addAll(eVar.jQ());
                cnVar.jn().b(eVar.jP(), eVar.jU());
                cnVar.jo().b(eVar.jQ(), eVar.jV());
            }
        }, csVar);
    }

    synchronized void bD(String str) {
        this.WR = str;
    }

    public synchronized void ba(String str) {
        bD(str);
        af bm = this.WJ.bm(str);
        t iZ = bm.iZ();
        for (com.google.android.gms.tagmanager.cr.a a : (Set) a(this.WP, iZ.iS()).getObject()) {
            a(this.WK, a, new HashSet(), iZ.iR());
        }
        bm.ja();
        bD(null);
    }
}
