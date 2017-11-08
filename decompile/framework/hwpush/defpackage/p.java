package defpackage;

import com.huawei.android.pushagent.model.channel.entity.ConnectEntity.CONNECT_METHOD;

/* renamed from: p */
public /* synthetic */ class p {
    public static final /* synthetic */ int[] Y = new int[CONNECT_METHOD.values().length];

    static {
        try {
            Y[CONNECT_METHOD.CONNECT_METHOD_DIRECT_TrsPort.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            Y[CONNECT_METHOD.CONNECT_METHOD_DIRECT_DefaultPort.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            Y[CONNECT_METHOD.CONNECT_METHOD_Proxy_TrsPort.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            Y[CONNECT_METHOD.CONNECT_METHOD_Proxy_DefaultPort.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
    }
}
