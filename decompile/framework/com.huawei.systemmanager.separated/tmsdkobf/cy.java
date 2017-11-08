package tmsdkobf;

/* compiled from: Unknown */
public final class cy extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public String ew = "";
    public int score = 0;
    public String title = "";
    public String user = "";

    static {
        boolean z = false;
        if (!cy.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public cy() {
        setTitle(this.title);
        setComment(this.ew);
        d(this.user);
        i(this.score);
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

    public void d(String str) {
        this.user = str;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (obj == null) {
            return false;
        }
        cy cyVar = (cy) obj;
        if (ft.equals(this.title, cyVar.title) && ft.equals(this.ew, cyVar.ew) && ft.equals(this.user, cyVar.user) && ft.equals(this.score, cyVar.score)) {
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

    public void i(int i) {
        this.score = i;
    }

    public void readFrom(fq fqVar) {
        setTitle(fqVar.a(0, true));
        setComment(fqVar.a(1, true));
        d(fqVar.a(2, true));
        i(fqVar.a(this.score, 3, true));
    }

    public void setComment(String str) {
        this.ew = str;
    }

    public void setTitle(String str) {
        this.title = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.title, 0);
        frVar.a(this.ew, 1);
        frVar.a(this.user, 2);
        frVar.write(this.score, 3);
    }
}
