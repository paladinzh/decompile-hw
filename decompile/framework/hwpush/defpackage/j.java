package defpackage;

import com.huawei.android.pushagent.model.channel.protocol.IPushChannel.ChannelType;

/* renamed from: j */
public class j {
    public String E;
    public ChannelType F;
    public boolean G;
    public int port;

    public j(String str, int i, boolean z, ChannelType channelType) {
        this.E = str;
        this.port = i;
        this.G = z;
        this.F = channelType;
    }

    public String toString() {
        return new StringBuffer().append("ip:").append(this.E).append(" port:").append(this.port).append(" useProxy:").append(this.G).append(" conType").append(this.F).toString();
    }
}
