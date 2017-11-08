package sun.net.www.protocol.http;

import java.io.IOException;
import java.net.Authenticator.RequestorType;
import java.net.URL;
import java.util.HashMap;
import sun.misc.BASE64Decoder;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class NegotiateAuthentication extends AuthenticationInfo {
    static HashMap<String, Negotiator> cache = null;
    private static final long serialVersionUID = 100;
    static HashMap<String, Boolean> supported = null;
    private final HttpCallerInfo hci;
    private Negotiator negotiator;

    class B64Encoder extends BASE64Encoder {
        B64Encoder() {
        }

        protected int bytesPerLine() {
            return 100000;
        }
    }

    public NegotiateAuthentication(HttpCallerInfo hci) {
        super(RequestorType.PROXY == hci.authType ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, hci.scheme.equalsIgnoreCase("Negotiate") ? AuthScheme.NEGOTIATE : AuthScheme.KERBEROS, hci.url, "");
        this.negotiator = null;
        this.hci = hci;
    }

    public boolean supportsPreemptiveAuthorization() {
        return false;
    }

    public static synchronized boolean isSupported(HttpCallerInfo hci) {
        synchronized (NegotiateAuthentication.class) {
            if (supported == null) {
                supported = new HashMap();
                cache = new HashMap();
            }
            String hostname = hci.host.toLowerCase();
            if (supported.containsKey(hostname)) {
                boolean booleanValue = ((Boolean) supported.get(hostname)).booleanValue();
                return booleanValue;
            }
            Negotiator neg = Negotiator.getNegotiator(hci);
            if (neg != null) {
                supported.put(hostname, Boolean.valueOf(true));
                cache.put(hostname, neg);
                return true;
            }
            supported.put(hostname, Boolean.valueOf(false));
            return false;
        }
    }

    public String getHeaderValue(URL url, String method) {
        throw new RuntimeException("getHeaderValue not supported");
    }

    public boolean isAuthorizationStale(String header) {
        return false;
    }

    public synchronized boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        byte[] incoming = null;
        try {
            String[] parts = raw.split("\\s+");
            if (parts.length > 1) {
                incoming = new BASE64Decoder().decodeBuffer(parts[1]);
            }
            conn.setAuthenticationProperty(getHeaderName(), this.hci.scheme + " " + new B64Encoder().encode(incoming == null ? firstToken() : nextToken(incoming)));
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    private byte[] firstToken() throws IOException {
        this.negotiator = null;
        if (cache != null) {
            synchronized (cache) {
                this.negotiator = (Negotiator) cache.get(getHost());
                if (this.negotiator != null) {
                    cache.remove(getHost());
                }
            }
        }
        if (this.negotiator == null) {
            this.negotiator = Negotiator.getNegotiator(this.hci);
            if (this.negotiator == null) {
                throw new IOException("Cannot initialize Negotiator");
            }
        }
        return this.negotiator.firstToken();
    }

    private byte[] nextToken(byte[] token) throws IOException {
        return this.negotiator.nextToken(token);
    }
}
