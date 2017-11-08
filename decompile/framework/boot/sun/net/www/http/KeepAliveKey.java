package sun.net.www.http;

import java.net.URL;

/* compiled from: KeepAliveCache */
class KeepAliveKey {
    private String host = null;
    private Object obj = null;
    private int port = 0;
    private String protocol = null;

    public KeepAliveKey(URL url, Object obj) {
        this.protocol = url.getProtocol();
        this.host = url.getHost();
        this.port = url.getPort();
        this.obj = obj;
    }

    public boolean equals(Object obj) {
        boolean z = false;
        if (!(obj instanceof KeepAliveKey)) {
            return false;
        }
        KeepAliveKey kae = (KeepAliveKey) obj;
        if (this.host.equals(kae.host) && this.port == kae.port && this.protocol.equals(kae.protocol) && this.obj == kae.obj) {
            z = true;
        }
        return z;
    }

    public int hashCode() {
        String str = this.protocol + this.host + this.port;
        if (this.obj == null) {
            return str.hashCode();
        }
        return str.hashCode() + this.obj.hashCode();
    }
}
