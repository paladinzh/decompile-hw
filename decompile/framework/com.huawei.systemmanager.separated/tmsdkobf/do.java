package tmsdkobf;

/* compiled from: Unknown */
public final class do extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int iD = 0;
    public int iE = 0;
    public int iF = 0;

    static {
        boolean z = false;
        if (!do.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public do() {
        p(this.iD);
        q(this.iE);
        r(this.iF);
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
        foVar.a(this.iD, "hostId");
        foVar.a(this.iE, "pluginId");
        foVar.a(this.iF, "pluginVersion");
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        do doVar = (do) obj;
        if (ft.equals(this.iD, doVar.iD) && ft.equals(this.iE, doVar.iE) && ft.equals(this.iF, doVar.iF)) {
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

    public void p(int i) {
        this.iD = i;
    }

    public void q(int i) {
        this.iE = i;
    }

    public void r(int i) {
        this.iF = i;
    }

    public void readFrom(fq fqVar) {
        p(fqVar.a(this.iD, 0, false));
        q(fqVar.a(this.iE, 1, false));
        r(fqVar.a(this.iF, 2, false));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.iD, 0);
        frVar.write(this.iE, 1);
        frVar.write(this.iF, 2);
    }
}
