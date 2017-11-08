package tmsdkobf;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class fh {
    protected HashMap<String, HashMap<String, byte[]>> lX = new HashMap();
    protected HashMap<String, Object> lY = new HashMap();
    private HashMap<String, Object> lZ = new HashMap();
    protected String ma = "GBK";
    fq mb = new fq();

    fh() {
    }

    public void Z(String encodeName) {
        this.ma = encodeName;
    }

    public void l() {
        this.lZ.clear();
    }

    public <T> void put(String name, T t) {
        if (name == null) {
            throw new IllegalArgumentException("put key can not is null");
        } else if (t == null) {
            throw new IllegalArgumentException("put value can not is null");
        } else if (t instanceof Set) {
            throw new IllegalArgumentException("can not support Set");
        } else {
            fr _out = new fr();
            _out.ae(this.ma);
            _out.a((Object) t, 0);
            byte[] _sBuffer = ft.a(_out.t());
            HashMap<String, byte[]> pair = new HashMap(1);
            ArrayList<String> listType = new ArrayList(1);
            a(listType, t);
            pair.put(ff.j(listType), _sBuffer);
            this.lZ.remove(name);
            this.lX.put(name, pair);
        }
    }

    private void a(ArrayList<String> listTpye, Object o) {
        if (o.getClass().isArray()) {
            if (!o.getClass().getComponentType().toString().equals("byte")) {
                throw new IllegalArgumentException("only byte[] is supported");
            } else if (Array.getLength(o) <= 0) {
                listTpye.add("Array");
                listTpye.add("?");
            } else {
                listTpye.add("java.util.List");
                a(listTpye, Array.get(o, 0));
            }
        } else if (o instanceof Array) {
            throw new IllegalArgumentException("can not support Array, please use List");
        } else if (o instanceof List) {
            listTpye.add("java.util.List");
            List list = (List) o;
            if (list.size() <= 0) {
                listTpye.add("?");
            } else {
                a(listTpye, list.get(0));
            }
        } else if (o instanceof Map) {
            listTpye.add("java.util.Map");
            Map map = (Map) o;
            if (map.size() <= 0) {
                listTpye.add("?");
                listTpye.add("?");
                return;
            }
            Object key = map.keySet().iterator().next();
            Object value = map.get(key);
            listTpye.add(key.getClass().getName());
            a(listTpye, value);
        } else {
            listTpye.add(o.getClass().getName());
        }
    }

    public byte[] m() {
        fr _os = new fr(0);
        _os.ae(this.ma);
        _os.a(this.lX, 0);
        return ft.a(_os.t());
    }

    public void b(byte[] buffer) {
        this.mb.d(buffer);
        this.mb.ae(this.ma);
        Map _tempdata = new HashMap(1);
        HashMap<String, byte[]> h = new HashMap(1);
        h.put("", new byte[0]);
        _tempdata.put("", h);
        this.lX = this.mb.a(_tempdata, 0, false);
    }
}
