package defpackage;

/* renamed from: g */
public class g {
    public String q;
    public Object r;
    public Class s;

    public g(String str, Class cls, Object obj) {
        this.q = str;
        this.s = cls;
        this.r = obj;
    }

    public g(String str, Class cls, String str2) {
        this.q = str;
        this.s = cls;
        a(str2);
    }

    public void a(String str) {
        if (String.class == this.s) {
            this.r = str;
        } else if (Integer.class == this.s) {
            this.r = Integer.valueOf(Integer.parseInt(str));
        } else if (Long.class == this.s) {
            this.r = Long.valueOf(Long.parseLong(str));
        } else if (Boolean.class == this.s) {
            this.r = Boolean.valueOf(Boolean.parseBoolean(str));
        } else {
            this.r = null;
        }
    }

    public String toString() {
        return new StringBuffer().append(this.q).append(":").append(this.r).append(":").append(this.s.getSimpleName()).toString();
    }
}
