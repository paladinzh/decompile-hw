package defpackage;

import com.huawei.android.pushagent.model.flowcontrol.ReconnectMgr.RECONNECTEVENT;

/* renamed from: aj */
public /* synthetic */ class aj {
    public static final /* synthetic */ int[] bj = new int[RECONNECTEVENT.values().length];

    static {
        try {
            bj[RECONNECTEVENT.NETWORK_CHANGE.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            bj[RECONNECTEVENT.TRS_QUERIED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            bj[RECONNECTEVENT.SOCKET_CLOSE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            bj[RECONNECTEVENT.SOCKET_CONNECTED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
