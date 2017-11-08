package com.huawei.android.pushagent.datatype.pollingmessage;

import com.huawei.android.pushagent.datatype.pollingmessage.basic.PollingMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PollingDataRspMessage extends PollingMessage {
    private byte mHasMsg;
    private short mLength;
    private byte mMode;
    private short mPollingInterval;
    private short mRequestId;

    public PollingDataRspMessage() {
        super(ay());
    }

    private static byte ay() {
        return (byte) 2;
    }

    public PollingMessage a(InputStream inputStream) {
        try {
            byte[] bArr = new byte[2];
            PollingMessage.a(inputStream, bArr);
            this.mRequestId = (short) au.g(bArr);
            bArr = new byte[1];
            PollingMessage.a(inputStream, bArr);
            this.mMode = bArr[0];
            bArr = new byte[1];
            PollingMessage.a(inputStream, bArr);
            this.mHasMsg = bArr[0];
            bArr = new byte[2];
            PollingMessage.a(inputStream, bArr);
            this.mPollingInterval = (short) au.g(bArr);
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
        }
        return this;
    }

    public boolean aA() {
        return this.mHasMsg == (byte) 1;
    }

    public short aB() {
        return this.mPollingInterval;
    }

    public byte az() {
        return this.mMode;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(au.c(this.mLength));
            byteArrayOutputStream.write(k());
            byteArrayOutputStream.write(au.c(this.mRequestId));
            byteArrayOutputStream.write(this.mMode);
            byteArrayOutputStream.write(this.mHasMsg);
            byteArrayOutputStream.write(au.c(this.mPollingInterval));
            aw.d("PushLog2841", "PollingDataRspMessage encode : baos " + au.f(byteArrayOutputStream.toByteArray()));
            return byteArrayOutputStream.toByteArray();
        } catch (Throwable e) {
            aw.d("PushLog2841", e.toString(), e);
            return null;
        }
    }

    public String toString() {
        return new StringBuffer(getClass().getSimpleName()).append(",mLength:").append(this.mLength).append(",cmdId:").append(aC()).append(",mRequestId:").append(this.mRequestId).append(",mMode:").append(this.mMode).append(",mHasMsg:").append(this.mHasMsg).append(",mPollingInterval:").append(this.mPollingInterval).toString();
    }
}
