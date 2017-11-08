package tmsdkobf;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.DataEntity;
import tmsdkobf.jl.b;

/* compiled from: Unknown */
public final class jo extends b {
    private static ConcurrentLinkedQueue<jm> ud = new ConcurrentLinkedQueue();
    private static volatile jo ue = null;

    private jo() {
    }

    public static boolean a(jm jmVar) {
        return ud.add(jmVar);
    }

    public static jo cp() {
        if (ue == null) {
            synchronized (jo.class) {
                if (ue == null) {
                    ue = new jo();
                }
            }
        }
        return ue;
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = ud.iterator();
        while (it.hasNext()) {
            jm jmVar = (jm) it.next();
            if (jmVar.isMatch(what)) {
                return jmVar.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
