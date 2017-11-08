package sun.net.www.protocol.http;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

public class AuthCacheImpl implements AuthCache {
    HashMap hashtable = new HashMap();

    public void setMap(HashMap map) {
        this.hashtable = map;
    }

    public synchronized void put(String pkey, AuthCacheValue value) {
        LinkedList list = (LinkedList) this.hashtable.get(pkey);
        String skey = value.getPath();
        if (list == null) {
            list = new LinkedList();
            this.hashtable.put(pkey, list);
        }
        ListIterator iter = list.listIterator();
        while (iter.hasNext()) {
            AuthenticationInfo inf = (AuthenticationInfo) iter.next();
            if (inf.path == null || inf.path.startsWith(skey)) {
                iter.remove();
            }
        }
        iter.add(value);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized AuthCacheValue get(String pkey, String skey) {
        LinkedList list = (LinkedList) this.hashtable.get(pkey);
        if (list != null && list.size() != 0) {
            if (skey == null) {
                return (AuthenticationInfo) list.get(0);
            }
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                AuthenticationInfo inf = (AuthenticationInfo) iter.next();
                if (skey.startsWith(inf.path)) {
                    return inf;
                }
            }
            return null;
        }
    }

    public synchronized void remove(String pkey, AuthCacheValue entry) {
        LinkedList list = (LinkedList) this.hashtable.get(pkey);
        if (list != null) {
            if (entry == null) {
                list.clear();
                return;
            }
            ListIterator iter = list.listIterator();
            while (iter.hasNext()) {
                if (entry.equals((AuthenticationInfo) iter.next())) {
                    iter.remove();
                }
            }
        }
    }
}
