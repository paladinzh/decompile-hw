package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class eg extends fs {
    static dg fF;
    static ArrayList<df> jF;
    static ArrayList<Integer> jG;
    public boolean bv = false;
    public int category = 0;
    public String dexSha1 = "";
    public int fT = 0;
    public dg fy = null;
    public String jA = "";
    public int jB = 0;
    public int jC = 0;
    public int jD = 0;
    public ArrayList<Integer> jE = null;
    public int jv = 0;
    public ArrayList<df> jw = null;
    public int jx = 0;
    public int jy = 0;
    public int jz = 0;
    public int position = 0;

    public void readFrom(fq fqVar) {
        if (fF == null) {
            fF = new dg();
        }
        this.fy = (dg) fqVar.a(fF, 0, true);
        this.jv = fqVar.a(this.jv, 1, true);
        this.fT = fqVar.a(this.fT, 2, true);
        if (jF == null) {
            jF = new ArrayList();
            jF.add(new df());
        }
        this.jw = (ArrayList) fqVar.b(jF, 3, false);
        this.jx = fqVar.a(this.jx, 4, false);
        this.bv = fqVar.a(this.bv, 5, false);
        this.category = fqVar.a(this.category, 6, false);
        this.position = fqVar.a(this.position, 7, false);
        this.jy = fqVar.a(this.jy, 8, false);
        this.jz = fqVar.a(this.jz, 9, false);
        this.jA = fqVar.a(10, false);
        this.jB = fqVar.a(this.jB, 11, false);
        this.jC = fqVar.a(this.jC, 12, false);
        this.jD = fqVar.a(this.jD, 13, false);
        this.dexSha1 = fqVar.a(14, false);
        if (jG == null) {
            jG = new ArrayList();
            jG.add(Integer.valueOf(0));
        }
        this.jE = (ArrayList) fqVar.b(jG, 15, false);
    }

    public void writeTo(fr frVar) {
        frVar.a(this.fy, 0);
        frVar.write(this.jv, 1);
        frVar.write(this.fT, 2);
        if (this.jw != null) {
            frVar.a(this.jw, 3);
        }
        frVar.write(this.jx, 4);
        frVar.a(this.bv, 5);
        frVar.write(this.category, 6);
        frVar.write(this.position, 7);
        frVar.write(this.jy, 8);
        frVar.write(this.jz, 9);
        if (this.jA != null) {
            frVar.a(this.jA, 10);
        }
        frVar.write(this.jB, 11);
        frVar.write(this.jC, 12);
        frVar.write(this.jD, 13);
        if (this.dexSha1 != null) {
            frVar.a(this.dexSha1, 14);
        }
        if (this.jE != null) {
            frVar.a(this.jE, 15);
        }
    }
}
