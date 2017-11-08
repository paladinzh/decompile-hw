package tmsdkobf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class fi extends fh {
    private HashMap<String, Object> lZ = new HashMap();
    fq mb = new fq();
    protected HashMap<String, byte[]> mc = null;

    public /* bridge */ /* synthetic */ void Z(String str) {
        super.Z(str);
    }

    public void n() {
        this.mc = new HashMap();
    }

    public void l() {
        this.lZ.clear();
    }

    public <T> void put(String name, T t) {
        if (this.mc == null) {
            super.put(name, t);
        } else if (name == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            fr _out = new fr();
            _out.ae(this.ma);
            _out.a((Object) t, 0);
            this.mc.put(name, ft.a(_out.t()));
        }
    }

    public <T> T a(String name, T proxy) throws fg {
        Object o;
        if (this.mc == null) {
            if (!this.lX.containsKey(name)) {
                return null;
            }
            if (this.lZ.containsKey(name)) {
                return this.lZ.get(name);
            }
            byte[] data = new byte[0];
            Iterator it = ((HashMap) this.lX.get(name)).entrySet().iterator();
            if (it.hasNext()) {
                Entry<String, byte[]> e = (Entry) it.next();
                String className = (String) e.getKey();
                data = (byte[]) e.getValue();
            }
            try {
                this.mb.d(data);
                this.mb.ae(this.ma);
                o = this.mb.b(proxy, 0, true);
                b(name, o);
                return o;
            } catch (Exception ex) {
                throw new fg(ex);
            }
        } else if (!this.mc.containsKey(name)) {
            return null;
        } else {
            if (this.lZ.containsKey(name)) {
                return this.lZ.get(name);
            }
            try {
                o = a((byte[]) this.mc.get(name), (Object) proxy);
                if (o != null) {
                    b(name, o);
                }
                return o;
            } catch (Exception ex2) {
                throw new fg(ex2);
            }
        }
    }

    private Object a(byte[] data, Object proxy) {
        this.mb.d(data);
        this.mb.ae(this.ma);
        return this.mb.b(proxy, 0, true);
    }

    private void b(String name, Object o) {
        this.lZ.put(name, o);
    }

    public byte[] m() {
        if (this.mc == null) {
            return super.m();
        }
        fr _os = new fr(0);
        _os.ae(this.ma);
        _os.a(this.mc, 0);
        return ft.a(_os.t());
    }

    public void b(byte[] buffer) {
        try {
            super.b(buffer);
        } catch (Exception e) {
            this.mb.d(buffer);
            this.mb.ae(this.ma);
            Map _tempdata = new HashMap(1);
            _tempdata.put("", new byte[0]);
            this.mc = this.mb.a(_tempdata, 0, false);
        }
    }
}
