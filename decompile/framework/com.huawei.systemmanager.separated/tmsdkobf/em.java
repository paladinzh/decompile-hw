package tmsdkobf;

/* compiled from: Unknown */
public final class em extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int calltime = 0;
    public int clientlogic = 0;
    public String phonenum = "";
    public int tagtype = 0;
    public int talktime = 0;
    public int teltype = de.ih.value();
    public int useraction = 0;

    static {
        boolean z = false;
        if (!em.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public em() {
        Q(this.phonenum);
        I(this.useraction);
        J(this.teltype);
        K(this.talktime);
        L(this.calltime);
        M(this.clientlogic);
        N(this.tagtype);
    }

    public void I(int i) {
        this.useraction = i;
    }

    public void J(int i) {
        this.teltype = i;
    }

    public void K(int i) {
        this.talktime = i;
    }

    public void L(int i) {
        this.calltime = i;
    }

    public void M(int i) {
        this.clientlogic = i;
    }

    public void N(int i) {
        this.tagtype = i;
    }

    public void Q(String str) {
        this.phonenum = str;
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
        em emVar = (em) obj;
        if (ft.equals(this.phonenum, emVar.phonenum) && ft.equals(this.useraction, emVar.useraction) && ft.equals(this.teltype, emVar.teltype) && ft.equals(this.talktime, emVar.talktime) && ft.equals(this.calltime, emVar.calltime) && ft.equals(this.clientlogic, emVar.clientlogic) && ft.equals(this.tagtype, emVar.tagtype)) {
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
        Q(fqVar.a(0, true));
        I(fqVar.a(this.useraction, 1, true));
        J(fqVar.a(this.teltype, 2, false));
        K(fqVar.a(this.talktime, 3, false));
        L(fqVar.a(this.calltime, 4, false));
        M(fqVar.a(this.clientlogic, 5, false));
        N(fqVar.a(this.tagtype, 6, false));
    }

    public void writeTo(fr frVar) {
        frVar.a(this.phonenum, 0);
        frVar.write(this.useraction, 1);
        frVar.write(this.teltype, 2);
        frVar.write(this.talktime, 3);
        frVar.write(this.calltime, 4);
        frVar.write(this.clientlogic, 5);
        frVar.write(this.tagtype, 6);
    }
}
