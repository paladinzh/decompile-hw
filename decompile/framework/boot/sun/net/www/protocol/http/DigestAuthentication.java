package sun.net.www.protocol.http;

import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Random;
import java.util.StringTokenizer;
import sun.net.www.HeaderParser;
import sun.net.www.protocol.http.HttpURLConnection.TunnelState;

class DigestAuthentication extends AuthenticationInfo {
    static final /* synthetic */ boolean -assertionsDisabled = (!DigestAuthentication.class.desiredAssertionStatus());
    private static final char[] charArray = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final long serialVersionUID = 100;
    private static final String[] zeroPad = new String[]{"00000000", "0000000", "000000", "00000", "0000", "000", "00", "0"};
    private String authMethod;
    Parameters params;

    static class Parameters implements Serializable {
        private static final int cnonceRepeat = 5;
        private static final int cnoncelen = 40;
        private static Random random = new Random();
        private static final long serialVersionUID = -3584543755194526252L;
        private int NCcount = 0;
        private String algorithm = null;
        private String cachedHA1 = null;
        private String cnonce;
        int cnonce_count = 0;
        private String nonce = null;
        private String opaque = null;
        private boolean redoCachedHA1 = true;
        private boolean serverQop = false;

        Parameters() {
            setNewCnonce();
        }

        boolean authQop() {
            return this.serverQop;
        }

        synchronized void incrementNC() {
            this.NCcount++;
        }

        synchronized int getNCCount() {
            return this.NCcount;
        }

        synchronized String getCnonce() {
            if (this.cnonce_count >= 5) {
                setNewCnonce();
            }
            this.cnonce_count++;
            return this.cnonce;
        }

        synchronized void setNewCnonce() {
            byte[] bb = new byte[20];
            char[] cc = new char[cnoncelen];
            random.nextBytes(bb);
            for (int i = 0; i < 20; i++) {
                int x = bb[i] + 128;
                cc[i * 2] = (char) ((x / 16) + 65);
                cc[(i * 2) + 1] = (char) ((x % 16) + 65);
            }
            this.cnonce = new String(cc, 0, (int) cnoncelen);
            this.cnonce_count = 0;
            this.redoCachedHA1 = true;
        }

        synchronized void setQop(String qop) {
            if (qop != null) {
                StringTokenizer st = new StringTokenizer(qop, " ");
                while (st.hasMoreTokens()) {
                    if (st.nextToken().equalsIgnoreCase("auth")) {
                        this.serverQop = true;
                        return;
                    }
                }
            }
            this.serverQop = false;
        }

        synchronized String getOpaque() {
            return this.opaque;
        }

        synchronized void setOpaque(String s) {
            this.opaque = s;
        }

        synchronized String getNonce() {
            return this.nonce;
        }

        synchronized void setNonce(String s) {
            if (!s.equals(this.nonce)) {
                this.nonce = s;
                this.NCcount = 0;
                this.redoCachedHA1 = true;
            }
        }

        synchronized String getCachedHA1() {
            if (this.redoCachedHA1) {
                return null;
            }
            return this.cachedHA1;
        }

        synchronized void setCachedHA1(String s) {
            this.cachedHA1 = s;
            this.redoCachedHA1 = false;
        }

        synchronized String getAlgorithm() {
            return this.algorithm;
        }

        synchronized void setAlgorithm(String s) {
            this.algorithm = s;
        }
    }

    public DigestAuthentication(boolean isProxy, URL url, String realm, String authMethod, PasswordAuthentication pw, Parameters params) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.DIGEST, url, realm);
        this.authMethod = authMethod;
        this.pw = pw;
        this.params = params;
    }

    public DigestAuthentication(boolean isProxy, String host, int port, String realm, String authMethod, PasswordAuthentication pw, Parameters params) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.DIGEST, host, port, realm);
        this.authMethod = authMethod;
        this.pw = pw;
        this.params = params;
    }

    public boolean supportsPreemptiveAuthorization() {
        return true;
    }

    public String getHeaderValue(URL url, String method) {
        return getHeaderValueImpl(url.getFile(), method);
    }

    String getHeaderValue(String requestURI, String method) {
        return getHeaderValueImpl(requestURI, method);
    }

    public boolean isAuthorizationStale(String header) {
        HeaderParser p = new HeaderParser(header);
        String s = p.findValue("stale");
        if (s == null || !s.equals("true")) {
            return false;
        }
        String newNonce = p.findValue("nonce");
        if (newNonce == null || "".equals(newNonce)) {
            return false;
        }
        this.params.setNonce(newNonce);
        return true;
    }

    public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        String method;
        this.params.setNonce(p.findValue("nonce"));
        this.params.setOpaque(p.findValue("opaque"));
        this.params.setQop(p.findValue("qop"));
        String uri = "";
        if (this.type == AuthenticationInfo.PROXY_AUTHENTICATION && conn.tunnelState() == TunnelState.SETUP) {
            uri = HttpURLConnection.connectRequestURI(conn.getURL());
            method = HttpURLConnection.HTTP_CONNECT;
        } else {
            try {
                uri = conn.getRequestURI();
            } catch (IOException e) {
            }
            method = conn.getMethod();
        }
        if (this.params.nonce == null || this.authMethod == null || this.pw == null || this.realm == null) {
            return false;
        }
        if (this.authMethod.length() >= 1) {
            this.authMethod = Character.toUpperCase(this.authMethod.charAt(0)) + this.authMethod.substring(1).toLowerCase();
        }
        String algorithm = p.findValue("algorithm");
        if (algorithm == null || "".equals(algorithm)) {
            algorithm = "MD5";
        }
        this.params.setAlgorithm(algorithm);
        if (this.params.authQop()) {
            this.params.setNewCnonce();
        }
        String value = getHeaderValueImpl(uri, method);
        if (value == null) {
            return false;
        }
        conn.setAuthenticationProperty(getHeaderName(), value);
        return true;
    }

    private String getHeaderValueImpl(String uri, String method) {
        char[] passwd = this.pw.getPassword();
        boolean qop = this.params.authQop();
        String opaque = this.params.getOpaque();
        String cnonce = this.params.getCnonce();
        String nonce = this.params.getNonce();
        String algorithm = this.params.getAlgorithm();
        this.params.incrementNC();
        int nccount = this.params.getNCCount();
        String str = null;
        if (nccount != -1) {
            str = Integer.toHexString(nccount).toLowerCase();
            int len = str.length();
            if (len < 8) {
                str = zeroPad[len] + str;
            }
        }
        try {
            String response = computeDigest(true, this.pw.getUserName(), passwd, this.realm, method, uri, nonce, cnonce, str);
            String ncfield = "\"";
            if (qop) {
                ncfield = "\", nc=" + str;
            }
            String value = this.authMethod + " username=\"" + this.pw.getUserName() + "\", realm=\"" + this.realm + "\", nonce=\"" + nonce + ncfield + ", uri=\"" + uri + "\", response=\"" + response + "\", algorithm=\"" + algorithm;
            if (opaque != null) {
                value = value + "\", opaque=\"" + opaque;
            }
            if (cnonce != null) {
                value = value + "\", cnonce=\"" + cnonce;
            }
            if (qop) {
                value = value + "\", qop=\"auth";
            }
            return value + "\"";
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public void checkResponse(String header, String method, URL url) throws IOException {
        checkResponse(header, method, url.getFile());
    }

    public void checkResponse(String header, String method, String uri) throws IOException {
        char[] passwd = this.pw.getPassword();
        String username = this.pw.getUserName();
        boolean qop = this.params.authQop();
        String opaque = this.params.getOpaque();
        String cnonce = this.params.cnonce;
        String nonce = this.params.getNonce();
        String algorithm = this.params.getAlgorithm();
        int nccount = this.params.getNCCount();
        String str = null;
        if (header == null) {
            throw new ProtocolException("No authentication information in response");
        }
        if (nccount != -1) {
            str = Integer.toHexString(nccount).toUpperCase();
            int len = str.length();
            if (len < 8) {
                str = zeroPad[len] + str;
            }
        }
        try {
            String expected = computeDigest(false, username, passwd, this.realm, method, uri, nonce, cnonce, str);
            HeaderParser headerParser = new HeaderParser(header);
            String rspauth = headerParser.findValue("rspauth");
            if (rspauth == null) {
                throw new ProtocolException("No digest in response");
            } else if (rspauth.equals(expected)) {
                String nextnonce = headerParser.findValue("nextnonce");
                if (nextnonce != null && !"".equals(nextnonce)) {
                    this.params.setNonce(nextnonce);
                }
            } else {
                throw new ProtocolException("Response digest invalid");
            }
        } catch (NoSuchAlgorithmException e) {
            throw new ProtocolException("Unsupported algorithm in response");
        }
    }

    private String computeDigest(boolean isRequest, String userName, char[] password, String realm, String connMethod, String requestURI, String nonceString, String cnonce, String ncValue) throws NoSuchAlgorithmException {
        String HashA1;
        String A2;
        String combo;
        String algorithm = this.params.getAlgorithm();
        boolean md5sess = algorithm.equalsIgnoreCase("MD5-sess");
        if (md5sess) {
            algorithm = "MD5";
        }
        MessageDigest md = MessageDigest.getInstance(algorithm);
        if (md5sess) {
            HashA1 = this.params.getCachedHA1();
            if (HashA1 == null) {
                HashA1 = encode(encode(userName + ":" + realm + ":", password, md) + ":" + nonceString + ":" + cnonce, null, md);
                this.params.setCachedHA1(HashA1);
            }
        } else {
            HashA1 = encode(userName + ":" + realm + ":", password, md);
        }
        if (isRequest) {
            A2 = connMethod + ":" + requestURI;
        } else {
            A2 = ":" + requestURI;
        }
        String HashA2 = encode(A2, null, md);
        if (this.params.authQop()) {
            combo = HashA1 + ":" + nonceString + ":" + ncValue + ":" + cnonce + ":auth:" + HashA2;
        } else {
            combo = HashA1 + ":" + nonceString + ":" + HashA2;
        }
        return encode(combo, null, md);
    }

    private String encode(String src, char[] passwd, MessageDigest md) {
        int i;
        try {
            md.update(src.getBytes("ISO-8859-1"));
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        if (passwd != null) {
            byte[] passwdBytes = new byte[passwd.length];
            for (i = 0; i < passwd.length; i++) {
                passwdBytes[i] = (byte) passwd[i];
            }
            md.update(passwdBytes);
            Arrays.fill(passwdBytes, (byte) 0);
        }
        byte[] digest = md.digest();
        StringBuffer res = new StringBuffer(digest.length * 2);
        for (i = 0; i < digest.length; i++) {
            res.append(charArray[(digest[i] >>> 4) & 15]);
            res.append(charArray[digest[i] & 15]);
        }
        return res.toString();
    }
}
