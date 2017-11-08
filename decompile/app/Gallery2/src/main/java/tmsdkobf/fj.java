package tmsdkobf;

import java.nio.ByteBuffer;
import java.util.HashMap;

public class fj extends fi {
    static HashMap<String, byte[]> me = null;
    static HashMap<String, HashMap<String, byte[]>> mf = null;
    protected fl md;
    private int mg;

    public /* bridge */ /* synthetic */ void Z(String str) {
        super.Z(str);
    }

    public fj() {
        this.md = new fl();
        this.mg = 0;
        this.md.mj = (short) 2;
    }

    public fj(boolean useVersion3) {
        this.md = new fl();
        this.mg = 0;
        if (useVersion3) {
            n();
        } else {
            this.md.mj = (short) 2;
        }
    }

    public <T> void put(String name, T t) {
        if (name.startsWith(".")) {
            throw new IllegalArgumentException("put name can not startwith . , now is " + name);
        }
        super.put(name, t);
    }

    public void n() {
        super.n();
        this.md.mj = (short) 3;
    }

    public byte[] m() {
        if (this.md.mj != (short) 2) {
            if (this.md.mn == null) {
                this.md.mn = "";
            }
            if (this.md.mo == null) {
                this.md.mo = "";
            }
        } else if (this.md.mn == null || this.md.mn.equals("")) {
            throw new IllegalArgumentException("servantName can not is null");
        } else if (this.md.mo == null || this.md.mo.equals("")) {
            throw new IllegalArgumentException("funcName can not is null");
        }
        fr _os = new fr(0);
        _os.ae(this.ma);
        if (this.md.mj != (short) 2) {
            _os.a(this.mc, 0);
        } else {
            _os.a(this.lX, 0);
        }
        this.md.mp = ft.a(_os.t());
        _os = new fr(0);
        _os.ae(this.ma);
        this.md.writeTo(_os);
        byte[] bodys = ft.a(_os.t());
        int size = bodys.length;
        ByteBuffer buf = ByteBuffer.allocate(size + 4);
        buf.putInt(size + 4).put(bodys).flip();
        return buf.array();
    }

    private void o() {
        fq _is = new fq(this.md.mp);
        _is.ae(this.ma);
        if (me == null) {
            me = new HashMap();
            me.put("", new byte[0]);
        }
        this.mc = _is.a(me, 0, false);
    }

    private void p() {
        fq _is = new fq(this.md.mp);
        _is.ae(this.ma);
        if (mf == null) {
            mf = new HashMap();
            HashMap<String, byte[]> h = new HashMap();
            h.put("", new byte[0]);
            mf.put("", h);
        }
        this.lX = _is.a(mf, 0, false);
        this.lY = new HashMap();
    }

    public void b(byte[] buffer) {
        if (buffer.length >= 4) {
            try {
                fq _is = new fq(buffer, 4);
                _is.ae(this.ma);
                this.md.readFrom(_is);
                if (this.md.mj != (short) 3) {
                    this.mc = null;
                    p();
                    return;
                }
                o();
                return;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        throw new IllegalArgumentException("decode package must include size head");
    }

    public void aa(String servantName) {
        this.md.mn = servantName;
    }

    public void ab(String sFuncName) {
        this.md.mo = sFuncName;
    }

    public int q() {
        return this.md.mm;
    }

    public void ae(int iRequestId) {
        this.md.mm = iRequestId;
    }
}
