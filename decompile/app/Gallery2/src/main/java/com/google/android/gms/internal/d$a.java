package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public final class d$a extends ka<d$a> {
    private static volatile d$a[] fX;
    public String fY;
    public d$a[] fZ;
    public d$a[] ga;
    public d$a[] gb;
    public String gc;
    public String gd;
    public long ge;
    public boolean gf;
    public d$a[] gg;
    public int[] gh;
    public boolean gi;
    public int type;

    public d$a() {
        s();
    }

    public static d$a[] r() {
        if (fX == null) {
            synchronized (kc.aah) {
                if (fX == null) {
                    fX = new d$a[0];
                }
            }
        }
        return fX;
    }

    public void a(jz jzVar) throws IOException {
        int i = 0;
        jzVar.f(1, this.type);
        if (!this.fY.equals("")) {
            jzVar.b(2, this.fY);
        }
        if (this.fZ != null && this.fZ.length > 0) {
            for (ke keVar : this.fZ) {
                if (keVar != null) {
                    jzVar.a(3, keVar);
                }
            }
        }
        if (this.ga != null && this.ga.length > 0) {
            for (ke keVar2 : this.ga) {
                if (keVar2 != null) {
                    jzVar.a(4, keVar2);
                }
            }
        }
        if (this.gb != null && this.gb.length > 0) {
            for (ke keVar22 : this.gb) {
                if (keVar22 != null) {
                    jzVar.a(5, keVar22);
                }
            }
        }
        if (!this.gc.equals("")) {
            jzVar.b(6, this.gc);
        }
        if (!this.gd.equals("")) {
            jzVar.b(7, this.gd);
        }
        if (this.ge != 0) {
            jzVar.b(8, this.ge);
        }
        if (this.gi) {
            jzVar.a(9, this.gi);
        }
        if (this.gh != null && this.gh.length > 0) {
            for (int f : this.gh) {
                jzVar.f(10, f);
            }
        }
        if (this.gg != null && this.gg.length > 0) {
            while (i < this.gg.length) {
                ke keVar3 = this.gg[i];
                if (keVar3 != null) {
                    jzVar.a(11, keVar3);
                }
                i++;
            }
        }
        if (this.gf) {
            jzVar.a(12, this.gf);
        }
        super.a(jzVar);
    }

    public int c() {
        int i;
        int i2 = 0;
        int c = super.c() + jz.g(1, this.type);
        if (!this.fY.equals("")) {
            c += jz.g(2, this.fY);
        }
        if (this.fZ != null && this.fZ.length > 0) {
            i = c;
            for (ke keVar : this.fZ) {
                if (keVar != null) {
                    i += jz.b(3, keVar);
                }
            }
            c = i;
        }
        if (this.ga != null && this.ga.length > 0) {
            i = c;
            for (ke keVar2 : this.ga) {
                if (keVar2 != null) {
                    i += jz.b(4, keVar2);
                }
            }
            c = i;
        }
        if (this.gb != null && this.gb.length > 0) {
            i = c;
            for (ke keVar22 : this.gb) {
                if (keVar22 != null) {
                    i += jz.b(5, keVar22);
                }
            }
            c = i;
        }
        if (!this.gc.equals("")) {
            c += jz.g(6, this.gc);
        }
        if (!this.gd.equals("")) {
            c += jz.g(7, this.gd);
        }
        if (this.ge != 0) {
            c += jz.d(8, this.ge);
        }
        if (this.gi) {
            c += jz.b(9, this.gi);
        }
        if (this.gh != null && this.gh.length > 0) {
            int i3 = 0;
            for (int cC : this.gh) {
                i3 += jz.cC(cC);
            }
            c = (c + i3) + (this.gh.length * 1);
        }
        if (this.gg != null && this.gg.length > 0) {
            while (i2 < this.gg.length) {
                ke keVar3 = this.gg[i2];
                if (keVar3 != null) {
                    c += jz.b(11, keVar3);
                }
                i2++;
            }
        }
        if (this.gf) {
            c += jz.b(12, this.gf);
        }
        this.DY = c;
        return c;
    }

    public boolean equals(Object o) {
        boolean z = false;
        if (o == this) {
            return true;
        }
        if (!(o instanceof d$a)) {
            return false;
        }
        d$a d_a = (d$a) o;
        if (this.type != d_a.type) {
            return false;
        }
        if (this.fY != null) {
            if (!this.fY.equals(d_a.fY)) {
                return false;
            }
        } else if (d_a.fY != null) {
            return false;
        }
        if (!kc.equals(this.fZ, d_a.fZ) || !kc.equals(this.ga, d_a.ga) || !kc.equals(this.gb, d_a.gb)) {
            return false;
        }
        if (this.gc != null) {
            if (!this.gc.equals(d_a.gc)) {
                return false;
            }
        } else if (d_a.gc != null) {
            return false;
        }
        if (this.gd != null) {
            if (!this.gd.equals(d_a.gd)) {
                return false;
            }
        } else if (d_a.gd != null) {
            return false;
        }
        if (this.ge != d_a.ge || this.gf != d_a.gf || !kc.equals(this.gg, d_a.gg) || !kc.equals(this.gh, d_a.gh) || this.gi != d_a.gi) {
            return false;
        }
        if (this.aae != null && !this.aae.isEmpty()) {
            return this.aae.equals(d_a.aae);
        }
        if (d_a.aae == null || d_a.aae.isEmpty()) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        int i = 1237;
        int i2 = 0;
        int hashCode = ((((((!this.gf ? 1237 : 1231) + (((((this.gd != null ? this.gd.hashCode() : 0) + (((this.gc != null ? this.gc.hashCode() : 0) + (((((((((this.fY != null ? this.fY.hashCode() : 0) + ((this.type + 527) * 31)) * 31) + kc.hashCode(this.fZ)) * 31) + kc.hashCode(this.ga)) * 31) + kc.hashCode(this.gb)) * 31)) * 31)) * 31) + ((int) (this.ge ^ (this.ge >>> 32)))) * 31)) * 31) + kc.hashCode(this.gg)) * 31) + kc.hashCode(this.gh)) * 31;
        if (this.gi) {
            i = 1231;
        }
        hashCode = (hashCode + i) * 31;
        if (!(this.aae == null || this.aae.isEmpty())) {
            i2 = this.aae.hashCode();
        }
        return hashCode + i2;
    }

    public d$a s() {
        this.type = 1;
        this.fY = "";
        this.fZ = r();
        this.ga = r();
        this.gb = r();
        this.gc = "";
        this.gd = "";
        this.ge = 0;
        this.gf = false;
        this.gg = r();
        this.gh = kh.aaj;
        this.gi = false;
        this.aae = null;
        this.DY = -1;
        return this;
    }
}
