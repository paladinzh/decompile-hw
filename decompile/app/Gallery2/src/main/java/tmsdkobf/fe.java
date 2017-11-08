package tmsdkobf;

import java.util.HashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import tmsdk.common.CallerIdent;
import tmsdk.common.utils.d;

/* compiled from: Unknown */
public class fe {
    private static ReentrantReadWriteLock lV = new ReentrantReadWriteLock();
    private static HashMap<String, Object> lW = new HashMap();

    private static Object a(int i, long j) {
        String str = "" + i + "-" + j;
        lV.readLock().lock();
        Object obj = lW.get(str);
        lV.readLock().unlock();
        return obj != null ? obj : b(i, j);
    }

    public static Object ad(int i) {
        return a(i, CallerIdent.getIdent(1, 4294967296L));
    }

    private static Object b(int i, long j) {
        Object obj;
        Object obj2 = null;
        switch (i) {
            case 4:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new lq(j, "com.tencent.meri");
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 5:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new hz(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 9:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new ig(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 12:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new gd(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            case 17:
                obj = "" + i + "-" + j;
                lV.readLock().lock();
                obj2 = lW.get(obj);
                lV.readLock().unlock();
                if (obj2 == null) {
                    obj2 = new fd(j);
                    d.d("ServiceCenter", "create service: " + obj);
                    break;
                }
                break;
            default:
                obj = null;
                break;
        }
        if (!(obj == null || obj2 == null)) {
            lV.writeLock().lock();
            if (lW.get(obj) == null) {
                lW.put(obj, obj2);
            }
            lV.writeLock().unlock();
        }
        return obj2;
    }
}
