package defpackage;

import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;

/* renamed from: x */
/* synthetic */ class x {
    static final /* synthetic */ int[] ap = new int[SocketEvent.values().length];

    static {
        try {
            ap[SocketEvent.SocketEvent_CONNECTING.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            ap[SocketEvent.SocketEvent_CONNECTED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            ap[SocketEvent.SocketEvent_CLOSE.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            ap[SocketEvent.SocketEvent_MSG_RECEIVED.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
