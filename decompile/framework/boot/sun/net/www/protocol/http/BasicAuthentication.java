package sun.net.www.protocol.http;

import java.io.UnsupportedEncodingException;
import java.net.PasswordAuthentication;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import sun.misc.BASE64Encoder;
import sun.net.www.HeaderParser;

class BasicAuthentication extends AuthenticationInfo {
    static final /* synthetic */ boolean -assertionsDisabled = (!BasicAuthentication.class.desiredAssertionStatus());
    private static final long serialVersionUID = 100;
    String auth;

    private class BasicBASE64Encoder extends BASE64Encoder {
        private BasicBASE64Encoder() {
        }

        protected int bytesPerLine() {
            return 10000;
        }
    }

    public BasicAuthentication(boolean isProxy, String host, int port, String realm, PasswordAuthentication pw) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, host, port, realm);
        byte[] nameBytes = null;
        try {
            nameBytes = (pw.getUserName() + ":").getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i = 0; i < passwd.length; i++) {
            passwdBytes[i] = (byte) passwd[i];
        }
        byte[] concat = new byte[(nameBytes.length + passwdBytes.length)];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length, passwdBytes.length);
        this.auth = "Basic " + new BasicBASE64Encoder().encode(concat);
        this.pw = pw;
    }

    public BasicAuthentication(boolean isProxy, String host, int port, String realm, String auth) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, host, port, realm);
        this.auth = "Basic " + auth;
    }

    public BasicAuthentication(boolean isProxy, URL url, String realm, PasswordAuthentication pw) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, url, realm);
        byte[] nameBytes = null;
        try {
            nameBytes = (pw.getUserName() + ":").getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            if (!-assertionsDisabled) {
                throw new AssertionError();
            }
        }
        char[] passwd = pw.getPassword();
        byte[] passwdBytes = new byte[passwd.length];
        for (int i = 0; i < passwd.length; i++) {
            passwdBytes[i] = (byte) passwd[i];
        }
        byte[] concat = new byte[(nameBytes.length + passwdBytes.length)];
        System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
        System.arraycopy(passwdBytes, 0, concat, nameBytes.length, passwdBytes.length);
        this.auth = "Basic " + new BasicBASE64Encoder().encode(concat);
        this.pw = pw;
    }

    public BasicAuthentication(boolean isProxy, URL url, String realm, String auth) {
        super(isProxy ? AuthenticationInfo.PROXY_AUTHENTICATION : AuthenticationInfo.SERVER_AUTHENTICATION, AuthScheme.BASIC, url, realm);
        this.auth = "Basic " + auth;
    }

    public boolean supportsPreemptiveAuthorization() {
        return true;
    }

    public boolean setHeaders(HttpURLConnection conn, HeaderParser p, String raw) {
        conn.setAuthenticationProperty(getHeaderName(), getHeaderValue(null, null));
        return true;
    }

    public String getHeaderValue(URL url, String method) {
        return this.auth;
    }

    public boolean isAuthorizationStale(String header) {
        return false;
    }

    static String getRootPath(String npath, String opath) {
        int index = 0;
        try {
            npath = new URI(npath).normalize().getPath();
            opath = new URI(opath).normalize().getPath();
        } catch (URISyntaxException e) {
        }
        while (index < opath.length()) {
            int toindex = opath.indexOf(47, index + 1);
            if (toindex == -1 || !opath.regionMatches(0, npath, 0, toindex + 1)) {
                return opath.substring(0, index + 1);
            }
            index = toindex;
        }
        return npath;
    }
}
