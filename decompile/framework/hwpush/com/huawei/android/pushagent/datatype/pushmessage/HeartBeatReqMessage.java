package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;

public class HeartBeatReqMessage extends PushMessage {
    public HeartBeatReqMessage() {
        super(ay());
    }

    private static byte ay() {
        return (byte) -48;
    }

    public PushMessage c(InputStream inputStream) {
        PushMessage.a(inputStream, new byte[1]);
        return this;
    }

    public byte[] encode() {
        return new byte[]{k()};
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aC()).toString();
    }
}
