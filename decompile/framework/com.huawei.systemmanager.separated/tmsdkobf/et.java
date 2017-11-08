package tmsdkobf;

/* compiled from: Unknown */
public final class et extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String iH = "";
    public int lc = 0;
    public int ld = 0;
    public int mainHarmId = 0;
    public int seq = 0;
    public String url = "";

    static {
        boolean z = false;
        if (!et.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public et() {
        setUrl(this.url);
        S(this.mainHarmId);
        T(this.lc);
        R(this.seq);
        y(this.iH);
        U(this.ld);
    }

    public void R(int i) {
        this.seq = i;
    }

    public void S(int i) {
        this.mainHarmId = i;
    }

    public void T(int i) {
        this.lc = i;
    }

    public void U(int i) {
        this.ld = i;
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

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        et etVar = (et) obj;
        if (ft.equals(this.url, etVar.url) && ft.equals(this.mainHarmId, etVar.mainHarmId) && ft.equals(this.lc, etVar.lc) && ft.equals(this.seq, etVar.seq) && ft.equals(this.iH, etVar.iH) && ft.equals(this.ld, etVar.ld)) {
            z = true;
        }
        return z;
    }

    public String getUrl() {
        return this.url;
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
        setUrl(fqVar.a(0, true));
        S(fqVar.a(this.mainHarmId, 1, true));
        T(fqVar.a(this.lc, 2, false));
        R(fqVar.a(this.seq, 3, false));
        y(fqVar.a(4, false));
        U(fqVar.a(this.ld, 5, false));
    }

    public void setUrl(String str) {
        this.url = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.url, 0);
        frVar.write(this.mainHarmId, 1);
        frVar.write(this.lc, 2);
        frVar.write(this.seq, 3);
        if (this.iH != null) {
            frVar.a(this.iH, 4);
        }
        frVar.write(this.ld, 5);
    }

    public void y(String str) {
        this.iH = str;
    }
}
