package defpackage;

import android.os.Bundle;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.datatype.pushmessage.NewHeartBeatRspMessage;
import com.huawei.android.pushagent.datatype.pushmessage.PushDataReqMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.Socket;
import java.net.SocketException;

/* renamed from: z */
public class z extends SocketReadThread {
    z(ConnectEntity connectEntity) {
        super(connectEntity);
    }

    protected void bl() {
        InputStream inputStream;
        int i;
        byte aQ;
        Throwable e;
        InputStream inputStream2 = null;
        try {
            if (this.ai.S == null || this.ai.S.getSocket() == null) {
                aw.e("PushLog2841", "no socket when in readSSLSocket");
                if (inputStream2 != null) {
                    inputStream2.close();
                }
                if (this.ai.S != null) {
                    this.ai.S.close();
                    return;
                }
                return;
            }
            Socket socket = this.ai.S.getSocket();
            if (socket != null) {
                aw.d("PushLog2841", "socket timeout is " + socket.getSoTimeout());
            }
            inputStream = this.ai.S.getInputStream();
            byte b = (byte) -1;
            int i2 = -1;
            while (!isInterrupted() && this.ai.S.hasConnection()) {
                try {
                    if (b != (byte) -1) {
                        i = b;
                        b = (byte) -1;
                    } else if (inputStream != null) {
                        i2 = inputStream.read();
                        if (NewHeartBeatRspMessage.ay() == ((byte) i2)) {
                            au.a(this.mContext, 200);
                            i = i2;
                        } else {
                            au.a(this.mContext, 5000);
                            i = i2;
                        }
                    } else {
                        aw.d("PushLog2841", "inputstream is null, cannot get cmdId");
                        i = i2;
                    }
                    if (-1 == i) {
                        aw.d("PushLog2841", "read -1 data, socket may be close");
                        break;
                    }
                    String f = au.f(new byte[]{(byte) i});
                    aw.i("PushLog2841", "received a msg cmdId:" + f);
                    try {
                        Serializable b2;
                        if (PushDataReqMessage.ay() == ((byte) i)) {
                            aw.d("PushLog2841", "is PushDataReqMessage set read TimeOut 100");
                            if (socket != null) {
                                socket.setSoTimeout(100);
                            } else {
                                aw.d("PushLog2841", "socket is null");
                            }
                            b2 = n.b(Byte.valueOf((byte) i), inputStream);
                            if (b2 != null) {
                                PushDataReqMessage pushDataReqMessage = (PushDataReqMessage) b2;
                                if (pushDataReqMessage.aQ() != (byte) -1) {
                                    aw.d("PushLog2841", "is get next cmdId, so set it");
                                    aQ = pushDataReqMessage.aQ();
                                }
                            }
                            aQ = b;
                        } else {
                            b2 = n.b(Byte.valueOf((byte) i), inputStream);
                            aQ = b;
                        }
                        if (b2 != null) {
                            try {
                                au.bJ();
                                Bundle bundle = new Bundle();
                                bundle.putSerializable("push_msg", b2);
                                this.ai.a(SocketEvent.SocketEvent_MSG_RECEIVED, bundle);
                            } catch (InstantiationException e2) {
                                aw.e("PushLog2841", "call getEntityByCmdId(cmd:" + i + " cause InstantiationException");
                                if (socket != null) {
                                    continue;
                                } else if (ChannelMgr.aV() != this.ai.bb()) {
                                    this.ai.S.getSocket().setSoTimeout(0);
                                } else {
                                    this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                                }
                                b = aQ;
                                i2 = i;
                            } catch (Exception e3) {
                                aw.e("PushLog2841", "call getEntityByCmdId(cmd:" + i + " Exception");
                                if (socket != null) {
                                    continue;
                                } else if (ChannelMgr.aV() != this.ai.bb()) {
                                    this.ai.S.getSocket().setSoTimeout(0);
                                } else {
                                    this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                                }
                                b = aQ;
                                i2 = i;
                            }
                        } else {
                            aw.e("PushLog2841", "received invalid msg, cmdId" + f);
                        }
                        if (socket == null) {
                            continue;
                        } else if (ChannelMgr.aV() == this.ai.bb()) {
                            this.ai.S.getSocket().setSoTimeout(0);
                        } else {
                            this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                        }
                    } catch (InstantiationException e4) {
                        aQ = b;
                        aw.e("PushLog2841", "call getEntityByCmdId(cmd:" + i + " cause InstantiationException");
                        if (socket != null) {
                            continue;
                        } else if (ChannelMgr.aV() != this.ai.bb()) {
                            this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                        } else {
                            this.ai.S.getSocket().setSoTimeout(0);
                        }
                        b = aQ;
                        i2 = i;
                    } catch (Exception e5) {
                        aQ = b;
                        aw.e("PushLog2841", "call getEntityByCmdId(cmd:" + i + " Exception");
                        if (socket != null) {
                            continue;
                        } else if (ChannelMgr.aV() != this.ai.bb()) {
                            this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                        } else {
                            this.ai.S.getSocket().setSoTimeout(0);
                        }
                        b = aQ;
                        i2 = i;
                    }
                    b = aQ;
                    i2 = i;
                } catch (SocketException e6) {
                    e = e6;
                    inputStream2 = inputStream;
                } catch (IOException e7) {
                    e = e7;
                } catch (Exception e8) {
                    e = e8;
                } catch (Throwable th) {
                    if (socket != null) {
                        if (ChannelMgr.aV() == this.ai.bb()) {
                            this.ai.S.getSocket().setSoTimeout(0);
                        } else {
                            this.ai.S.getSocket().setSoTimeout((int) (this.ai.T.e(false) + ae.l(this.mContext).ae()));
                        }
                    }
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ai.S != null) {
                this.ai.S.close();
            }
            throw new PushException(" read normal Exit", ErrorType.Err_Read);
        } catch (SocketException e9) {
            e = e9;
            try {
                throw new PushException(e, ErrorType.Err_Read);
            } catch (Throwable th2) {
                e = th2;
                inputStream = inputStream2;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.ai.S != null) {
                    this.ai.S.close();
                }
                throw e;
            }
        } catch (IOException e10) {
            e = e10;
            inputStream = inputStream2;
            try {
                throw new PushException(e, ErrorType.Err_Read);
            } catch (Throwable th3) {
                e = th3;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.ai.S != null) {
                    this.ai.S.close();
                }
                throw e;
            }
        } catch (Exception e11) {
            e = e11;
            inputStream = inputStream2;
            throw new PushException(e, ErrorType.Err_Read);
        } catch (Throwable th4) {
            e = th4;
            inputStream = inputStream2;
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ai.S != null) {
                this.ai.S.close();
            }
            throw e;
        }
    }
}
