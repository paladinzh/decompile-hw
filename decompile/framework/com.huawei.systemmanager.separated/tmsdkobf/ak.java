package tmsdkobf;

import java.util.ArrayList;

/* compiled from: Unknown */
public final class ak extends fs {
    static ArrayList<byte[]> bl = new ArrayList();
    public int bf = 0;
    public int bg = 0;
    public int bh = 0;
    public ArrayList<byte[]> bi = null;
    public int bj = 0;
    public boolean bk = false;

    static {
        bl.add(new byte[]{(byte) 0});
    }

    public fs newInit() {
        return new ak();
    }

    public void readFrom(fq fqVar) {
        this.bf = fqVar.a(this.bf, 0, true);
        this.bg = fqVar.a(this.bg, 1, true);
        this.bh = fqVar.a(this.bh, 2, true);
        this.bi = (ArrayList) fqVar.b(bl, 3, true);
        this.bj = fqVar.a(this.bj, 4, false);
        this.bk = fqVar.a(this.bk, 5, false);
    }

    public void writeTo(fr frVar) {
        frVar.write(this.bf, 0);
        frVar.write(this.bg, 1);
        frVar.write(this.bh, 2);
        frVar.a(this.bi, 3);
        if (this.bj != 0) {
            frVar.write(this.bj, 4);
        }
        if (this.bk) {
            frVar.a(this.bk, 5);
        }
    }
}
