package com.huawei.android.pushagent.datatype.pushmessage;

import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import defpackage.au;
import defpackage.aw;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class PushDataReqMessage extends PushMessage {
    private int mDataLen;
    private byte[] mExtMsg;
    private int mExtMsgLen;
    private byte[] mMsgData;
    private byte[] mMsgId;
    private byte mNextCMDID = (byte) -1;
    private byte[] mPackageName;
    private int mPackageNameLen;
    private byte[] mToken;

    public PushDataReqMessage() {
        super(ay());
    }

    public static final byte ay() {
        return (byte) -96;
    }

    public byte[] aD() {
        return this.mToken;
    }

    public byte[] aE() {
        return this.mMsgData;
    }

    public byte[] aM() {
        return this.mMsgId;
    }

    public byte[] aN() {
        return this.mExtMsg;
    }

    public int aO() {
        return this.mPackageNameLen;
    }

    public byte[] aP() {
        return this.mPackageName;
    }

    public byte aQ() {
        return this.mNextCMDID;
    }

    public PushMessage c(InputStream inputStream) {
        this.mMsgId = new byte[8];
        PushMessage.a(inputStream, this.mMsgId);
        this.mToken = new byte[32];
        PushMessage.a(inputStream, this.mToken);
        byte[] bArr = new byte[2];
        PushMessage.a(inputStream, bArr);
        int g = au.g(bArr);
        aw.d("PushLog2841", "push message len=" + g);
        this.mDataLen = g;
        this.mMsgData = new byte[g];
        PushMessage.a(inputStream, this.mMsgData);
        bArr = new byte[2];
        try {
            bArr[0] = (byte) inputStream.read();
            if (bArr[0] < (byte) 0) {
                aw.i("PushLog2841", "read first Len:" + bArr[0] + ", not valid len, may be next cmdId in Old PushDataReqMessage");
                this.mNextCMDID = bArr[0];
            } else {
                bArr[1] = (byte) inputStream.read();
                g = bArr[1] + bArr[0];
                aw.d("PushLog2841", "mPackageNameLen=" + g);
                if (g <= 0) {
                    aw.i("PushLog2841", "the package length:" + g + " is Unavailable ");
                } else {
                    this.mPackageNameLen = g;
                    this.mPackageName = new byte[g];
                    PushMessage.a(inputStream, this.mPackageName);
                    bArr = new byte[2];
                    try {
                        bArr[0] = (byte) inputStream.read();
                        if (bArr[0] < (byte) 0) {
                            aw.i("PushLog2841", "read extMsgLen:" + bArr[0] + ", not valid len, may be next cmdId in PushDataReqMessage without extend message");
                            this.mNextCMDID = bArr[0];
                        } else {
                            bArr[1] = (byte) inputStream.read();
                            this.mExtMsgLen = au.g(bArr);
                            aw.d("PushLog2841", "mExtMsgLen=" + this.mExtMsgLen);
                            if (this.mExtMsgLen <= 0) {
                                aw.i("PushLog2841", "the extend message length:" + this.mExtMsgLen + " is invalid ");
                            } else {
                                this.mExtMsg = new byte[this.mExtMsgLen];
                                PushMessage.a(inputStream, this.mExtMsg);
                            }
                        }
                    } catch (Exception e) {
                        aw.d("PushLog2841", "read msg cause:" + e.toString() + " may be has no extend message");
                    }
                }
            }
        } catch (Exception e2) {
            aw.i("PushLog2841", "read msg cause:" + e2.toString() + " may be old PushDataReqMessage");
        }
        return this;
    }

    public byte[] encode() {
        try {
            if (this.mMsgId == null) {
                aw.e("PushLog2841", "encode error, mMsgId = null");
                return null;
            } else if (this.mToken == null) {
                aw.e("PushLog2841", "encode error, reason mToken = null");
                return null;
            } else if (this.mMsgData == null) {
                aw.e("PushLog2841", "encode error, reason mMsgData = null");
                return null;
            } else if (this.mPackageName == null) {
                aw.e("PushLog2841", "encode error, reason mPackage = null");
                return null;
            } else {
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                byteArrayOutputStream.write(k());
                byteArrayOutputStream.write(this.mMsgId);
                byteArrayOutputStream.write(this.mToken);
                byteArrayOutputStream.write(au.c(this.mMsgData.length));
                byteArrayOutputStream.write(this.mMsgData);
                byte[] bArr = new byte[2];
                if (this.mPackageNameLen > 127) {
                    bArr[0] = Byte.MAX_VALUE;
                    bArr[1] = (byte) (this.mPackageNameLen - 127);
                } else {
                    bArr[0] = (byte) 0;
                    bArr[1] = (byte) this.mPackageNameLen;
                }
                byteArrayOutputStream.write(bArr);
                byteArrayOutputStream.write(this.mPackageName);
                return byteArrayOutputStream.toByteArray();
            }
        } catch (Exception e) {
            aw.d("PushLog2841", "encode error," + e.toString());
            return null;
        }
    }

    public String toString() {
        String str;
        String str2 = "null";
        if (this.mPackageName != null) {
            try {
                str = new String(this.mPackageName, "UTF-8");
            } catch (Throwable e) {
                aw.a("PushLog2841", e.toString(), e);
            }
            return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aC()).append(",msgId:").append(au.f(this.mMsgId)).append(",deviceToken:").append(au.f(this.mToken)).append(",msgData:").append(au.f(this.mMsgData)).append(", mPackageLen:").append(this.mPackageNameLen).append(", pkgName:").append(str).toString();
        }
        str = str2;
        return new StringBuffer(getClass().getSimpleName()).append(" cmdId:").append(aC()).append(",msgId:").append(au.f(this.mMsgId)).append(",deviceToken:").append(au.f(this.mToken)).append(",msgData:").append(au.f(this.mMsgData)).append(", mPackageLen:").append(this.mPackageNameLen).append(", pkgName:").append(str).toString();
    }
}
