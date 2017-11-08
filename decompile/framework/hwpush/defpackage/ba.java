package defpackage;

/* renamed from: ba */
public class ba {
    private static final String[] bO = new String[]{"android.intent.action.TIME_SET", "android.intent.action.TIMEZONE_CHANGED", "com.huawei.android.push.intent.GET_PUSH_STATE", "com.huawei.android.push.intent.DEREGISTER", "com.huawei.intent.action.SELF_SHOW_FLAG", "com.huawei.android.push.intent.MSG_RESPONSE", "android.ctrlsocket.all.allowed", "android.scroff.ctrlsocket.status"};
    private static final String[] bP = new String[]{"com.huawei.android.push.intent.HEARTBEAT_RSP_TIMEOUT", "com.huawei.intent.action.PUSH_OFF", "com.huawei.action.CONNECT_PUSHSRV", "com.huawei.action.CONNECT_PUSHSRV_POLLINGSRV", "com.huawei.intent.action.PUSH", "com.huawei.android.push.intent.MSG_RSP_TIMEOUT", "com.huawei.android.push.intent.RESET_BASTET", "com.huawei.android.push.intent.RESPONSE_FAIL"};
    private static final String[] bQ = new String[]{"com.huawei.android.push.intent.REGISTER"};

    public static String[] bQ() {
        Object obj = new String[bP.length];
        System.arraycopy(bP, 0, obj, 0, bP.length);
        return obj;
    }

    public static String[] bR() {
        Object obj = new String[bO.length];
        System.arraycopy(bO, 0, obj, 0, bO.length);
        return obj;
    }

    public static String[] bS() {
        Object obj = new String[bQ.length];
        System.arraycopy(bQ, 0, obj, 0, bQ.length);
        return obj;
    }
}
