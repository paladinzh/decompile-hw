package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class RegisterTokenRspMessage extends PushMessage {
    private String mPackageName = null;
    private byte mResult = (byte) 1;
    private String mToken = null;

    public RegisterTokenRspMessage() {
        super(ay());
    }

    public RegisterTokenRspMessage(byte b, String str, String str2) {
        super(ay());
        this.mResult = b;
        this.mToken = str;
        this.mPackageName = str2;
    }

    private static byte ay() {
        return (byte) -35;
    }

    public byte aK() {
        return this.mResult;
    }

    public String aS() {
        return this.mToken;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mResult = bArr[0];
        if (bArr[0] != (byte) 0) {
            this.mPackageName = null;
            this.mToken = null;
        }
        bArr = new byte[32];
        PushMessage.a(inputStream, bArr);
        this.mToken = new String(bArr, "UTF-8");
        bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        bArr = new byte[au.g(bArr)];
        PushMessage.a(inputStream, bArr);
        this.mPackageName = new String(bArr, "UTF-8");
        return this;
    }

    public byte[] encode() {
        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            byteArrayOutputStream.write(k());
            byteArrayOutputStream.write(0);
            byteArrayOutputStream.write(this.mToken.getBytes("UTF-8"));
            byteArrayOutputStream.write(au.c(this.mPackageName.length()));
            byteArrayOutputStream.write(this.mPackageName.getBytes("UTF-8"));
            return byteArrayOutputStream.toByteArray();
        } catch (Exception e) {
            aw.e("PushLog2841", "encode error,e " + e.toString());
            return null;
        }
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String toString() {
        return new StringBuffer().append("RegisterTokenRspMessage[").append("result:").append(au.e(this.mResult)).append(",token:").append(this.mToken).append(",packageName:").append(this.mPackageName).append("]").toString();
    }
}
