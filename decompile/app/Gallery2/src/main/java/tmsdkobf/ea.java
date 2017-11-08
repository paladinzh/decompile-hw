package tmsdkobf;

import com.huawei.watermark.manager.parse.util.ParseJson;

/* compiled from: Unknown */
public final class ea extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int advice = 0;
    public String iH = "";
    public int id = 0;
    public String jk = "";
    public int jl = 0;
    public int level = 0;
    public String name = "";
    public int type = 0;

    static {
        boolean z = false;
        if (!ea.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
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
        foVar.a(this.name, "name");
        foVar.a(this.jk, "shortdesc");
        foVar.a(this.level, ParseJson.LEVEL);
        foVar.a(this.advice, "advice");
        foVar.a(this.iH, "desc");
        foVar.a(this.jl, "scan");
        foVar.a(this.type, "type");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        ea eaVar = (ea) obj;
        if (ft.equals(this.id, eaVar.id) && ft.equals(this.name, eaVar.name) && ft.equals(this.jk, eaVar.jk) && ft.equals(this.level, eaVar.level) && ft.equals(this.advice, eaVar.advice) && ft.equals(this.iH, eaVar.iH) && ft.equals(this.jl, eaVar.jl) && ft.equals(this.type, eaVar.type)) {
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
        this.id = fqVar.a(this.id, 0, true);
        this.name = fqVar.a(1, true);
        this.jk = fqVar.a(2, true);
        this.level = fqVar.a(this.level, 3, true);
        this.advice = fqVar.a(this.advice, 4, true);
        this.iH = fqVar.a(5, true);
        this.jl = fqVar.a(this.jl, 6, true);
        this.type = fqVar.a(this.type, 7, true);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.id, 0);
        frVar.a(this.name, 1);
        frVar.a(this.jk, 2);
        frVar.write(this.level, 3);
        frVar.write(this.advice, 4);
        frVar.a(this.iH, 5);
        frVar.write(this.jl, 6);
        frVar.write(this.type, 7);
    }
}
