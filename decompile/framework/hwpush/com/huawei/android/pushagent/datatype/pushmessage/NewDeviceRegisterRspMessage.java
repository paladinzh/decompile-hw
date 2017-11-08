package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.io.InputStream;

public class NewDeviceRegisterRspMessage extends PushMessage {
    private byte mResult = (byte) 1;

    public NewDeviceRegisterRspMessage() {
        super(ay());
    }

    private static byte ay() {
        return (byte) -33;
    }

    public byte aK() {
        return this.mResult;
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
