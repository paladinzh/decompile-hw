package defpackage;

import android.os.Bundle;
import com.huawei.android.pushagent.datatype.PushException;
import com.huawei.android.pushagent.datatype.PushException.ErrorType;
import com.huawei.android.pushagent.datatype.pollingmessage.basic.PollingMessage;
import com.huawei.android.pushagent.model.channel.entity.ConnectEntity;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread;
import com.huawei.android.pushagent.model.channel.entity.SocketReadThread.SocketEvent;
import com.huawei.android.pushagent.model.channel.protocol.IPushChannel;
import java.io.InputStream;
import java.io.Serializable;
import java.net.SocketException;

/* renamed from: v */
class v extends SocketReadThread {
    public v(ConnectEntity connectEntity) {
        super(connectEntity);
    }

    protected void bl() {
        Throwable e;
        Object obj;
        IPushChannel iPushChannel = null;
        InputStream inputStream;
        try {
            if (this.ai.S == null || this.ai.S.getSocket() == null) {
                aw.e("PushLog2841", "no socket when in readSSLSocket");
                if (iPushChannel != null) {
                    iPushChannel.close();
                }
                if (this.ai.S != null) {
                    this.ai.S.close();
                    this.ai.S = iPushChannel;
                    return;
                }
                return;
            }
            aw.d("PushLog2841", "socket timeout is " + this.ai.S.getSocket().getSoTimeout());
            inputStream = this.ai.S.getInputStream();
            while (!isInterrupted() && this.ai.S.hasConnection()) {
                try {
                    Serializable b;
                    if (inputStream != null) {
                        b = PollingMessage.b(inputStream);
                    } else {
                        aw.i("PushLog2841", "InputStream is null, get pollingMessage failed");
                        Object obj2 = iPushChannel;
                    }
                    if (b != null) {
                        au.bJ();
                        Bundle bundle = new Bundle();
                        bundle.putSerializable("push_msg", b);
                        this.ai.a(SocketEvent.SocketEvent_MSG_RECEIVED, bundle);
                    }
                } catch (SocketException e2) {
                    aw.d("PushLog2841", "SocketException:" + e2.toString());
                } catch (Throwable e3) {
                    aw.d("PushLog2841", "call getEntityByCmdId cause:" + e3.toString(), e3);
                    throw e3;
                } catch (Exception e4) {
                    e3 = e4;
                }
            }
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ai.S != null) {
                this.ai.S.close();
                this.ai.S = iPushChannel;
            }
        } catch (Exception e5) {
            e3 = e5;
            obj = iPushChannel;
            try {
                throw new PushException(e3, ErrorType.Err_Read);
            } catch (Throwable th) {
                e3 = th;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (this.ai.S != null) {
                    this.ai.S.close();
                    this.ai.S = iPushChannel;
                }
                throw e3;
            }
        } catch (Throwable th2) {
            e3 = th2;
            obj = iPushChannel;
            if (inputStream != null) {
                inputStream.close();
            }
            if (this.ai.S != null) {
                this.ai.S.close();
                this.ai.S = iPushChannel;
            }
            throw e3;
        }
    }
}
