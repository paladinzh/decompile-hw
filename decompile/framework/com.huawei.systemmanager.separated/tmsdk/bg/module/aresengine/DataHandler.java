package tmsdk.bg.module.aresengine;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import tmsdk.common.module.aresengine.FilterResult;
import tmsdk.common.module.aresengine.TelephonyEntity;
import tmsdkobf.jq;

/* compiled from: Unknown */
public class DataHandler extends Handler {
    private static final Looper wQ;
    private ConcurrentLinkedQueue<DataHandlerCallback> wR = new ConcurrentLinkedQueue();

    /* compiled from: Unknown */
    public interface DataHandlerCallback {
        void onCallback(TelephonyEntity telephonyEntity, int i, int i2, Object... objArr);
    }

    static {
        Thread bF = jq.ct().bF(DataHandler.class.getName());
        bF.start();
        wQ = bF.getLooper();
    }

    public DataHandler() {
        super(wQ);
    }

    public final void addCallback(DataHandlerCallback dataHandlerCallback) {
        this.wR.add(dataHandlerCallback);
    }

    public void handleMessage(Message message) {
        if (message.what == 3456) {
            FilterResult filterResult = (FilterResult) message.obj;
            Iterator it = filterResult.mDotos.iterator();
            while (it.hasNext()) {
                Runnable runnable = (Runnable) it.next();
                if (runnable instanceof Thread) {
                    ((Thread) runnable).start();
                } else {
                    runnable.run();
                }
            }
            TelephonyEntity telephonyEntity = filterResult.mData;
            int i = filterResult.mFilterfiled;
            int i2 = filterResult.mState;
            Object[] objArr = filterResult.mParams;
            Iterator it2 = this.wR.iterator();
            while (it2.hasNext()) {
                ((DataHandlerCallback) it2.next()).onCallback(telephonyEntity, i, i2, objArr);
            }
        }
    }

    public final void removeCallback(DataHandlerCallback dataHandlerCallback) {
        this.wR.remove(dataHandlerCallback);
    }

    public synchronized void sendMessage(FilterResult filterResult) {
        if (filterResult != null) {
            Message obtainMessage = obtainMessage(3456);
            obtainMessage.obj = filterResult;
            obtainMessage.sendToTarget();
        }
    }
}
