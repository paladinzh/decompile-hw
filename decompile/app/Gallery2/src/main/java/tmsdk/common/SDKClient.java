package tmsdk.common;

import android.os.IBinder;
import android.os.RemoteException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdkobf.jl.b;

/* compiled from: Unknown */
public final class SDKClient extends b {
    private static volatile SDKClient Ac = null;
    private static ConcurrentLinkedQueue<MessageHandler> ud = new ConcurrentLinkedQueue();

    private SDKClient() {
    }

    public static boolean addMessageHandler(MessageHandler messageHandler) {
        return ud.add(messageHandler);
    }

    public static SDKClient getInstance() {
        if (Ac == null) {
            synchronized (SDKClient.class) {
                if (Ac == null) {
                    Ac = new SDKClient();
                }
            }
        }
        return Ac;
    }

    public static boolean removeMessageHandler(MessageHandler messageHandler) {
        return ud.remove(messageHandler);
    }

    public IBinder asBinder() {
        return this;
    }

    public DataEntity sendMessage(DataEntity dataEntity) throws RemoteException {
        int what = dataEntity.what();
        Iterator it = ud.iterator();
        while (it.hasNext()) {
            MessageHandler messageHandler = (MessageHandler) it.next();
            if (messageHandler.isMatch(what)) {
                return messageHandler.onProcessing(dataEntity);
            }
        }
        return null;
    }
}
