package defpackage;

import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;

/* renamed from: t */
/* synthetic */ class t {
    static final /* synthetic */ int[] ap = new int[SocketEvent.values().length];

    static {
        try {
            ap[SocketEvent.SocketEvent_CONNECTED.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            ap[SocketEvent.SocketEvent_MSG_RECEIVED.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
    }
}
