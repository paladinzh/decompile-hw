package tmsdkobf;

/* compiled from: Unknown */
public final class fb extends fs {
    static final /* synthetic */ boolean fJ;
    public int lJ = 0;

    static {
        boolean z = false;
        if (!fb.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    public fb() {
        ab(this.lJ);
    }

    public void ab(int i) {
        this.lJ = i;
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
        return ft.equals(this.lJ, ((fb) obj).lJ);
    }

    public int j() {
        return this.lJ;
    }

    public void readFrom(fq fqVar) {
        ab(fqVar.a(this.lJ, 0, true));
    }

    public void writeTo(fr frVar) {
        frVar.write(this.lJ, 0);
    }
}
