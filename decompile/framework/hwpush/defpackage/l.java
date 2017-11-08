package defpackage;

import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataReqMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.PollingDataRspMessage;
import com.huawei.android.pushagent.datatype.pollingmessage.basic.PollingMessage;
import java.io.InputStream;
import java.util.HashMap;

/* renamed from: l */
public class l {
    private static HashMap H = new HashMap();

    static {
        H.put(Byte.valueOf((byte) 1), PollingDataReqMessage.class);
        H.put(Byte.valueOf((byte) 2), PollingDataRspMessage.class);
    }

    public static PollingMessage a(Byte b, InputStream inputStream) {
        if (H.containsKey(b)) {
            PollingMessage pollingMessage = (PollingMessage) ((Class) H.get(b)).newInstance();
            PollingMessage a = pollingMessage.a(inputStream);
            if (a != null) {
                aw.d("PushLog2841", "after decode msg:" + a);
            } else {
                aw.e("PushLog2841", "call " + pollingMessage.getClass().getSimpleName() + " decode failed!");
            }
            return a;
        }
        aw.e("PushLog2841", "cmdId:" + b + " is not exist, all:" + H.keySet());
        throw new InstantiationException("cmdId:" + b + " is not register");
    }
}
