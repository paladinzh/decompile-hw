package tmsdkobf;

/* compiled from: Unknown */
public final class dg extends fs implements Cloneable {
    static final /* synthetic */ boolean fJ;
    public int fileSize = 0;
    public String ip = "";
    public String iq = "";
    public String softName = "";
    public String version = "";
    public int versionCode = 0;

    static {
        boolean z = false;
        if (!dg.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public dg() {
        p(this.ip);
        q(this.softName);
        r(this.version);
        l(this.versionCode);
        s(this.iq);
        m(this.fileSize);
    }

    public dg(String str, String str2, String str3, int i, String str4, int i2) {
        p(str);
        q(str2);
        r(str3);
        l(i);
        s(str4);
        m(i2);
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
        dg dgVar = (dg) obj;
        if (ft.equals(this.ip, dgVar.ip) && ft.equals(this.softName, dgVar.softName) && ft.equals(this.version, dgVar.version) && ft.equals(this.versionCode, dgVar.versionCode) && ft.equals(this.iq, dgVar.iq) && ft.equals(this.fileSize, dgVar.fileSize)) {
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

    public void l(int i) {
        this.versionCode = i;
    }

    public void m(int i) {
        this.fileSize = i;
    }

    public void p(String str) {
        this.ip = str;
    }

    public void q(String str) {
        this.softName = str;
    }

    public void r(String str) {
        this.version = str;
    }

    public void readFrom(fq fqVar) {
        p(fqVar.a(0, true));
        q(fqVar.a(1, true));
        r(fqVar.a(2, true));
        l(fqVar.a(this.versionCode, 3, false));
        s(fqVar.a(4, false));
        m(fqVar.a(this.fileSize, 5, false));
    }

    public void s(String str) {
        this.iq = str;
    }

    public void writeTo(fr frVar) {
        frVar.a(this.ip, 0);
        frVar.a(this.softName, 1);
        frVar.a(this.version, 2);
        frVar.write(this.versionCode, 3);
        if (this.iq != null) {
            frVar.a(this.iq, 4);
        }
        frVar.write(this.fileSize, 5);
    }
}
