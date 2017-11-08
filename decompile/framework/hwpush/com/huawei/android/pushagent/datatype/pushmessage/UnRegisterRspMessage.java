package com.huawei.android.pushagent.datatype.pushmessage;

import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class UnRegisterRspMessage extends PushMessage {
    private byte mResult = (byte) 1;
    private String mToken = null;

    public UnRegisterRspMessage() {
        super(ay());
    }

    private static byte ay() {
        return (byte) -41;
    }

    public String aS() {
        return this.mToken;
    }

    public PushMessage c(InputStream inputStream) {
        byte[] bArr = new byte[32];
        PushMessage.a(inputStream, bArr);
        this.mToken = new String(bArr, "UTF-8");
        bArr = new byte[1];
        PushMessage.a(inputStream, bArr);
        this.mResult = bArr[0];
        return this;
    }

    public byte[] encode() {
        byte[] bArr = null;
        try {
            if (TextUtils.isEmpty(this.mToken)) {
                aw.e("PushLog2841", "encode error, mToken = " + this.mToken);
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(k());
                byteArrayOutputStream.write(this.mToken.getBytes("UTF-8"));
                bArr = byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            aw.e("PushLog2841", "encode error " + e.toString());
        }
        return bArr;
    }

    public String toString() {
        return "UnRegisterRspMessage[token:" + this.mToken + " result:" + this.mResult + "]";
    }
}
