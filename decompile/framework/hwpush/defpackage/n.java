package defpackage;

import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.DeviceRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.DeviceRegisterRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.HeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.HeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewDeviceRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewDeviceRegisterRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;
import java.util.HashMap;

/* renamed from: n */
public class n {
    private static HashMap H = new HashMap();

    static {
        H.put(Byte.valueOf((byte) -47), HeartBeatRspMessage.class);
        H.put(Byte.valueOf((byte) -37), NewHeartBeatRspMessage.class);
        H.put(Byte.valueOf((byte) -45), DeviceRegisterRspMessage.class);
        H.put(Byte.valueOf((byte) -33), NewDeviceRegisterRspMessage.class);
        H.put(Byte.valueOf((byte) -35), RegisterTokenRspMessage.class);
        H.put(Byte.valueOf((byte) -41), UnRegisterRspMessage.class);
        H.put(Byte.valueOf((byte) -96), PushDataReqMessage.class);
        H.put(Byte.valueOf((byte) -48), HeartBeatReqMessage.class);
        H.put(Byte.valueOf((byte) -38), NewHeartBeatReqMessage.class);
        H.put(Byte.valueOf((byte) -46), DeviceRegisterReqMessage.class);
        H.put(Byte.valueOf((byte) -34), NewDeviceRegisterReqMessage.class);
        H.put(Byte.valueOf((byte) -36), RegisterTokenReqMessage.class);
        H.put(Byte.valueOf((byte) -42), UnRegisterReqMessage.class);
        H.put(Byte.valueOf((byte) -95), PushDataRspMessage.class);
        H.put(Byte.valueOf((byte) -92), DecoupledPushMessage.class);
        H.put(Byte.valueOf((byte) -91), DecoupledPushMessage.class);
        H.put(Byte.valueOf((byte) -90), DecoupledPushMessage.class);
        H.put(Byte.valueOf((byte) -89), DecoupledPushMessage.class);
    }

    public static PushMessage b(Byte b, InputStream inputStream) {
        if (H.containsKey(b)) {
            PushMessage pushMessage = (PushMessage) ((Class) H.get(b)).newInstance();
            if (pushMessage.k() == (byte) -1) {
                pushMessage.a(b.byteValue());
            }
            PushMessage c = pushMessage.c(inputStream);
            if (c != null) {
                aw.d("PushLog2841", "after decode msg:" + au.e(c.k()));
            } else {
                aw.e("PushLog2841", "call " + pushMessage.getClass().getSimpleName() + " decode failed!");
            }
            return c;
        }
        aw.e("PushLog2841", "cmdId:" + b + " is not exist, all:" + H.keySet());
        throw new InstantiationException("cmdId:" + b + " is not register");
    }
}
