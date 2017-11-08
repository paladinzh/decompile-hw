package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class dx extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    static ArrayList<Integer> iY;
    static do iZ;
    public String iH = "";
    public ArrayList<Integer> iV = null;
    public String iW = "";
    public do iX = null;
    public int id = 0;
    public int time = 0;

    static {
        boolean z = false;
        if (!dx.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dx() {
        setId(this.id);
        v(this.time);
        y(this.iH);
        e(this.iV);
        B(this.iW);
        a(this.iX);
    }

    public void B(String str) {
        this.iW = str;
    }

    public void a(do doVar) {
        this.iX = doVar;
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException e) {
            if (!fJ) {
                throw new AssertionError();
            }
        }
        return obj;
    }

    public void display(StringBuilder stringBuilder, int i) {
        fo foVar = new fo(stringBuilder, i);
        foVar.a(this.id, "id");
        foVar.a(this.time, "time");
        foVar.a(this.iH, "desc");
        foVar.a(this.iV, "ivalues");
        foVar.a(this.iW, "paramvalues");
        foVar.a(this.iX, "pluginInfo");
    }

    public ArrayList<Integer> e() {
        return this.iV;
    }

    public void e(ArrayList<Integer> arrayList) {
        this.iV = arrayList;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        dx dxVar = (dx) obj;
        if (ft.equals(this.id, dxVar.id) && ft.equals(this.time, dxVar.time) && ft.equals(this.iH, dxVar.iH) && ft.equals(this.iV, dxVar.iV) && ft.equals(this.iW, dxVar.iW) && ft.equals(this.iX, dxVar.iX)) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        try {
            throw new Exception("Need define key first!");
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void readFrom(fq fqVar) {
        setId(fqVar.a(this.id, 0, true));
        v(fqVar.a(this.time, 1, true));
        y(fqVar.a(2, true));
        if (iY == null) {
            iY = new ArrayList();
            iY.add(Integer.valueOf(0));
        }
        e((ArrayList) fqVar.b(iY, 3, false));
        B(fqVar.a(4, false));
        if (iZ == null) {
            iZ = new do();
        }
        a((do) fqVar.a(iZ, 5, false));
    }

    public void setId(int i) {
        this.id = i;
    }

    public void v(int i) {
        this.time = i;
    }

    public void writeTo(fr frVar) {
        frVar.write(this.id, 0);
        frVar.write(this.time, 1);
        frVar.a(this.iH, 2);
        if (this.iV != null) {
            frVar.a(this.iV, 3);
        }
        if (this.iW != null) {
            frVar.a(this.iW, 4);
        }
        if (this.iX != null) {
            frVar.a(this.iX, 5);
        }
    }

    public void y(String str) {
        this.iH = str;
    }
}
