package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;

public class DeviceRegisterRspMessage extends PushMessage {
    private byte mResult = (byte) 1;

    public DeviceRegisterRspMessage() {
        super(ay());
    }

    private static byte ay() {
        return (byte) -45;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mResult = bArr[0];
        return this;
    }

    public byte[] encode() {
        return new byte[]{k(), this.mResult};
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aC()).append(" result:").append(this.mResult).toString();
    }
}
