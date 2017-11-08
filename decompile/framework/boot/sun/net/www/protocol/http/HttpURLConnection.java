package sun.net.www.protocol.http;

import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.Authenticator;
import java.net.Authenticator.RequestorType;
import java.net.CacheRequest;
import java.net.CacheResponse;
import java.net.CookieHandler;
import java.net.HttpCookie;
import java.net.HttpRetryException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.PasswordAuthentication;
import java.net.ProtocolException;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.ProxySelector;
import java.net.ResponseCache;
import java.net.SecureCacheResponse;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TimeZone;
import sun.net.ApplicationProxy;
import sun.net.ProgressMonitor;
import sun.net.ProgressSource;
import sun.net.www.HeaderParser;
import sun.net.www.MessageHeader;
import sun.net.www.MeteredStream;
import sun.net.www.ParseUtil;
import sun.net.www.http.ChunkedInputStream;
import sun.net.www.http.ChunkedOutputStream;
import sun.net.www.http.HttpClient;
import sun.net.www.http.PosterOutputStream;
import sun.security.action.GetBooleanAction;
import sun.security.action.GetIntegerAction;
import sun.security.action.GetPropertyAction;
import sun.util.logging.PlatformLogger;

public class HttpURLConnection extends java.net.HttpURLConnection {
    private static final /* synthetic */ int[] -sun-net-www-protocol-http-AuthSchemeSwitchesValues = null;
    private static final String[] EXCLUDE_HEADERS = new String[]{"Proxy-Authorization", "Authorization"};
    private static final String[] EXCLUDE_HEADERS2 = new String[]{"Proxy-Authorization", "Authorization", "Cookie", "Cookie2"};
    static String HTTP_CONNECT = "CONNECT";
    private static final String RETRY_MSG1 = "cannot retry due to proxy authentication, in streaming mode";
    private static final String RETRY_MSG2 = "cannot retry due to server authentication, in streaming mode";
    private static final String RETRY_MSG3 = "cannot retry due to redirection, in streaming mode";
    private static final String SET_COOKIE = "set-cookie";
    private static final String SET_COOKIE2 = "set-cookie2";
    static final String acceptString = "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2";
    private static final boolean allowRestrictedHeaders = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("sun.net.http.allowRestrictedHeaders"))).booleanValue();
    private static int bufSize4ES = 0;
    private static HttpAuthenticator defaultAuth = null;
    static final int defaultmaxRedirects = 20;
    private static boolean enableESBuffer = false;
    static final String httpVersion = "HTTP/1.1";
    private static final PlatformLogger logger = PlatformLogger.getLogger("sun.net.www.protocol.http.HttpURLConnection");
    static final int maxRedirects = ((Integer) AccessController.doPrivileged(new GetIntegerAction("http.maxRedirects", 20))).intValue();
    private static final Set<String> restrictedHeaderSet;
    private static final String[] restrictedHeaders = new String[]{"Access-Control-Request-Headers", "Access-Control-Request-Method", "Connection", "Content-Length", "Content-Transfer-Encoding", "Host", "Keep-Alive", "Origin", "Trailer", "Transfer-Encoding", "Upgrade", "Via"};
    private static int timeout4ESBuffer;
    public static final String userAgent;
    static final boolean validateProxy = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("http.auth.digest.validateProxy"))).booleanValue();
    static final boolean validateServer = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("http.auth.digest.validateServer"))).booleanValue();
    static final String version = ((String) AccessController.doPrivileged(new GetPropertyAction("java.version")));
    private Object authObj;
    private ResponseCache cacheHandler;
    private MessageHeader cachedHeaders;
    private InputStream cachedInputStream;
    protected CacheResponse cachedResponse;
    byte[] cdata;
    private int connectTimeout;
    private CookieHandler cookieHandler;
    AuthenticationInfo currentProxyCredentials;
    AuthenticationInfo currentServerCredentials;
    Parameters digestparams;
    private boolean doingNTLM2ndStage;
    private boolean doingNTLMp2ndStage;
    String domain;
    private InputStream errorStream;
    private boolean failedOnce;
    private Map<String, List<String>> filteredHeaders;
    protected Handler handler;
    protected HttpClient http;
    private InputStream inputStream;
    protected Proxy instProxy;
    boolean isUserProxyAuth;
    boolean isUserServerAuth;
    boolean needToCheck;
    protected ProgressSource pi;
    private PosterOutputStream poster;
    String proxyAuthKey;
    protected PrintStream ps;
    private int readTimeout;
    private Exception rememberedException;
    String requestURI;
    private MessageHeader requests;
    private MessageHeader responses;
    private HttpClient reuseClient;
    String serverAuthKey;
    private boolean setRequests;
    private boolean setUserCookies;
    private StreamingOutputStream strOutputStream;
    private boolean tryTransparentNTLMProxy;
    private boolean tryTransparentNTLMServer;
    private TunnelState tunnelState;
    private String userCookies;
    private String userCookies2;

    static class ErrorStream extends InputStream {
        ByteBuffer buffer;
        InputStream is;

        private ErrorStream(ByteBuffer buf) {
            this.buffer = buf;
            this.is = null;
        }

        private ErrorStream(ByteBuffer buf, InputStream is) {
            this.buffer = buf;
            this.is = is;
        }

        public static InputStream getErrorStream(InputStream is, long cl, HttpClient http) {
            if (cl == 0) {
                return null;
            }
            long expected;
            int oldTimeout = http.getReadTimeout();
            http.setReadTimeout(HttpURLConnection.timeout4ESBuffer / 5);
            boolean isChunked = false;
            if (cl < 0) {
                expected = (long) HttpURLConnection.bufSize4ES;
                isChunked = true;
            } else {
                expected = cl;
            }
            if (expected > ((long) HttpURLConnection.bufSize4ES)) {
                return null;
            }
            int exp = (int) expected;
            byte[] buffer = new byte[exp];
            int count = 0;
            int time = 0;
            int len = 0;
            while (true) {
                try {
                    len = is.read(buffer, count, buffer.length - count);
                    if (len >= 0) {
                        count += len;
                        if (count < exp && time < HttpURLConnection.timeout4ESBuffer) {
                        }
                        break;
                    }
                    if (!isChunked) {
                        throw new IOException("the server closes before sending " + cl + " bytes of data");
                    }
                    break;
                } catch (SocketTimeoutException e) {
                    try {
                        time += HttpURLConnection.timeout4ESBuffer / 5;
                    } catch (IOException e2) {
                        return null;
                    }
                }
            }
            http.setReadTimeout(oldTimeout);
            if (count == 0) {
                return null;
            }
            if ((((long) count) != expected || isChunked) && (!isChunked || len >= 0)) {
                return new ErrorStream(ByteBuffer.wrap(buffer, 0, count), is);
            }
            is.close();
            return new ErrorStream(ByteBuffer.wrap(buffer, 0, count));
        }

        public int available() throws IOException {
            if (this.is == null) {
                return this.buffer.remaining();
            }
            return this.buffer.remaining() + this.is.available();
        }

        public int read() throws IOException {
            byte[] b = new byte[1];
            int ret = read(b);
            return ret == -1 ? ret : b[0] & 255;
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            int rem = this.buffer.remaining();
            if (rem > 0) {
                int ret = rem < len ? rem : len;
                this.buffer.get(b, off, ret);
                return ret;
            } else if (this.is == null) {
                return -1;
            } else {
                return this.is.read(b, off, len);
            }
        }

        public void close() throws IOException {
            this.buffer = null;
            if (this.is != null) {
                this.is.close();
            }
        }
    }

    class HttpInputStream extends FilterInputStream {
        private static final int SKIP_BUFFER_SIZE = 8096;
        private CacheRequest cacheRequest;
        private int inCache;
        private int markCount;
        private boolean marked;
        private OutputStream outputStream;
        private byte[] skipBuffer;

        public HttpInputStream(InputStream is) {
            super(is);
            this.marked = false;
            this.inCache = 0;
            this.markCount = 0;
            this.cacheRequest = null;
            this.outputStream = null;
        }

        public HttpInputStream(InputStream is, CacheRequest cacheRequest) {
            super(is);
            this.marked = false;
            this.inCache = 0;
            this.markCount = 0;
            this.cacheRequest = cacheRequest;
            try {
                this.outputStream = cacheRequest.getBody();
            } catch (IOException e) {
                this.cacheRequest.abort();
                this.cacheRequest = null;
                this.outputStream = null;
            }
        }

        public synchronized void mark(int readlimit) {
            super.mark(readlimit);
            if (this.cacheRequest != null) {
                this.marked = true;
                this.markCount = 0;
            }
        }

        public synchronized void reset() throws IOException {
            super.reset();
            if (this.cacheRequest != null) {
                this.marked = false;
                this.inCache += this.markCount;
            }
        }

        public int read() throws IOException {
            try {
                byte[] b = new byte[1];
                int ret = read(b);
                return ret == -1 ? ret : b[0] & 255;
            } catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            }
        }

        public int read(byte[] b) throws IOException {
            return read(b, 0, b.length);
        }

        public int read(byte[] b, int off, int len) throws IOException {
            try {
                int nWrite;
                int newLen = super.read(b, off, len);
                if (this.inCache <= 0) {
                    nWrite = newLen;
                } else if (this.inCache >= newLen) {
                    this.inCache -= newLen;
                    nWrite = 0;
                } else {
                    nWrite = newLen - this.inCache;
                    this.inCache = 0;
                }
                if (nWrite > 0 && this.outputStream != null) {
                    this.outputStream.write(b, (newLen - nWrite) + off, nWrite);
                }
                if (this.marked) {
                    this.markCount += newLen;
                }
                return newLen;
            } catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            }
        }

        public long skip(long n) throws IOException {
            long remaining = n;
            if (this.skipBuffer == null) {
                this.skipBuffer = new byte[SKIP_BUFFER_SIZE];
            }
            byte[] localSkipBuffer = this.skipBuffer;
            if (n <= 0) {
                return 0;
            }
            while (remaining > 0) {
                int nr = read(localSkipBuffer, 0, (int) Math.min(8096, remaining));
                if (nr < 0) {
                    break;
                }
                remaining -= (long) nr;
            }
            return n - remaining;
        }

        public void close() throws IOException {
            try {
                if (this.outputStream != null) {
                    if (read() != -1) {
                        this.cacheRequest.abort();
                    } else {
                        this.outputStream.close();
                    }
                }
                super.close();
                HttpURLConnection.this.http = null;
                HttpURLConnection.this.checkResponseCredentials(true);
            } catch (IOException ioex) {
                if (this.cacheRequest != null) {
                    this.cacheRequest.abort();
                }
                throw ioex;
            } catch (Throwable th) {
                HttpURLConnection.this.http = null;
                HttpURLConnection.this.checkResponseCredentials(true);
            }
        }
    }

    class StreamingOutputStream extends FilterOutputStream {
        boolean closed = false;
        boolean error = false;
        IOException errorExcp;
        long expected;
        long written = 0;

        StreamingOutputStream(OutputStream os, long expectedLength) {
            super(os);
            this.expected = expectedLength;
        }

        public void write(int b) throws IOException {
            checkError();
            this.written++;
            if (this.expected == -1 || this.written <= this.expected) {
                this.out.write(b);
                return;
            }
            throw new IOException("too many bytes written");
        }

        public void write(byte[] b) throws IOException {
            write(b, 0, b.length);
        }

        public void write(byte[] b, int off, int len) throws IOException {
            checkError();
            this.written += (long) len;
            if (this.expected == -1 || this.written <= this.expected) {
                this.out.write(b, off, len);
            } else {
                this.out.close();
                throw new IOException("too many bytes written");
            }
        }

        void checkError() throws IOException {
            if (this.closed) {
                throw new IOException("Stream is closed");
            } else if (this.error) {
                throw this.errorExcp;
            } else if (((PrintStream) this.out).checkError()) {
                throw new IOException("Error writing request body to server");
            }
        }

        boolean writtenOK() {
            return this.closed && !this.error;
        }

        public void close() throws IOException {
            if (!this.closed) {
                this.closed = true;
                if (this.expected == -1) {
                    super.close();
                    OutputStream o = HttpURLConnection.this.http.getOutputStream();
                    o.write(13);
                    o.write(10);
                    o.flush();
                } else if (this.written != this.expected) {
                    this.error = true;
                    this.errorExcp = new IOException("insufficient data written");
                    this.out.close();
                    throw this.errorExcp;
                } else {
                    super.flush();
                }
            }
        }
    }

    public enum TunnelState {
        NONE,
        SETUP,
        TUNNELING
    }

    private static /* synthetic */ int[] -getsun-net-www-protocol-http-AuthSchemeSwitchesValues() {
        if (-sun-net-www-protocol-http-AuthSchemeSwitchesValues != null) {
            return -sun-net-www-protocol-http-AuthSchemeSwitchesValues;
        }
        int[] iArr = new int[AuthScheme.values().length];
        try {
            iArr[AuthScheme.BASIC.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[AuthScheme.DIGEST.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[AuthScheme.KERBEROS.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[AuthScheme.NEGOTIATE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[AuthScheme.NTLM.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[AuthScheme.UNKNOWN.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -sun-net-www-protocol-http-AuthSchemeSwitchesValues = iArr;
        return iArr;
    }

    static {
        enableESBuffer = false;
        timeout4ESBuffer = 0;
        bufSize4ES = 0;
        String agent = (String) AccessController.doPrivileged(new GetPropertyAction("http.agent"));
        if (agent == null) {
            agent = "Java/" + version;
        } else {
            agent = agent + " Java/" + version;
        }
        userAgent = agent;
        enableESBuffer = ((Boolean) AccessController.doPrivileged(new GetBooleanAction("sun.net.http.errorstream.enableBuffering"))).booleanValue();
        timeout4ESBuffer = ((Integer) AccessController.doPrivileged(new GetIntegerAction("sun.net.http.errorstream.timeout", 300))).intValue();
        if (timeout4ESBuffer <= 0) {
            timeout4ESBuffer = 300;
        }
        bufSize4ES = ((Integer) AccessController.doPrivileged(new GetIntegerAction("sun.net.http.errorstream.bufferSize", 4096))).intValue();
        if (bufSize4ES <= 0) {
            bufSize4ES = 4096;
        }
        if (allowRestrictedHeaders) {
            restrictedHeaderSet = null;
        } else {
            restrictedHeaderSet = new HashSet(restrictedHeaders.length);
            for (String toLowerCase : restrictedHeaders) {
                restrictedHeaderSet.add(toLowerCase.toLowerCase());
            }
        }
    }

    private static PasswordAuthentication privilegedRequestPasswordAuthentication(String host, InetAddress addr, int port, String protocol, String prompt, String scheme, URL url, RequestorType authType) {
        final String str = host;
        final URL url2 = url;
        final InetAddress inetAddress = addr;
        final int i = port;
        final String str2 = protocol;
        final String str3 = prompt;
        final String str4 = scheme;
        final RequestorType requestorType = authType;
        return (PasswordAuthentication) AccessController.doPrivileged(new PrivilegedAction<PasswordAuthentication>() {
            public PasswordAuthentication run() {
                if (HttpURLConnection.logger.isLoggable(300)) {
                    HttpURLConnection.logger.finest("Requesting Authentication: host =" + str + " url = " + url2);
                }
                PasswordAuthentication pass = Authenticator.requestPasswordAuthentication(str, inetAddress, i, str2, str3, str4, url2, requestorType);
                if (HttpURLConnection.logger.isLoggable(300)) {
                    HttpURLConnection.logger.finest("Authentication returned: " + (pass != null ? pass.toString() : "null"));
                }
                return pass;
            }
        });
    }

    private boolean isRestrictedHeader(String key, String value) {
        if (allowRestrictedHeaders) {
            return false;
        }
        key = key.toLowerCase();
        return restrictedHeaderSet.contains(key) ? (key.equals("connection") && value.equalsIgnoreCase("close")) ? false : true : key.startsWith("sec-");
    }

    private boolean isExternalMessageHeaderAllowed(String key, String value) {
        checkMessageHeader(key, value);
        if (isRestrictedHeader(key, value)) {
            return false;
        }
        return true;
    }

    public static PlatformLogger getHttpLogger() {
        return logger;
    }

    public Object authObj() {
        return this.authObj;
    }

    public void authObj(Object authObj) {
        this.authObj = authObj;
    }

    private void checkMessageHeader(String key, String value) {
        if (key.indexOf(10) != -1) {
            throw new IllegalArgumentException("Illegal character(s) in message header field: " + key);
        } else if (value != null) {
            int index = value.indexOf(10);
            while (index != -1) {
                index++;
                if (index < value.length()) {
                    char c = value.charAt(index);
                    if (c == ' ' || c == '\t') {
                        index = value.indexOf(10, index);
                    }
                }
                throw new IllegalArgumentException("Illegal character(s) in message header value: " + value);
            }
        }
    }

    private void writeRequests() throws IOException {
        if (this.http.usingProxy && tunnelState() != TunnelState.TUNNELING) {
            setPreemptiveProxyAuthentication(this.requests);
        }
        if (!this.setRequests) {
            if (!this.failedOnce) {
                this.requests.prepend(this.method + " " + getRequestURI() + " " + httpVersion, null);
            }
            if (!getUseCaches()) {
                this.requests.setIfNotSet("Cache-Control", "no-cache");
                this.requests.setIfNotSet("Pragma", "no-cache");
            }
            this.requests.setIfNotSet("User-Agent", userAgent);
            int port = this.url.getPort();
            String host = this.url.getHost();
            if (!(port == -1 || port == this.url.getDefaultPort())) {
                host = host + ":" + String.valueOf(port);
            }
            this.requests.setIfNotSet("Host", host);
            this.requests.setIfNotSet("Accept", acceptString);
            if (this.failedOnce || !this.http.getHttpKeepAliveSet()) {
                this.requests.setIfNotSet("Connection", "close");
            } else if (!this.http.usingProxy || tunnelState() == TunnelState.TUNNELING) {
                this.requests.setIfNotSet("Connection", "keep-alive");
            } else {
                this.requests.setIfNotSet("Proxy-Connection", "keep-alive");
            }
            long modTime = getIfModifiedSince();
            if (modTime != 0) {
                Date date = new Date(modTime);
                SimpleDateFormat fo = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'", Locale.US);
                fo.setTimeZone(TimeZone.getTimeZone("GMT"));
                this.requests.setIfNotSet("If-Modified-Since", fo.format(date));
            }
            AuthenticationInfo sauth = AuthenticationInfo.getServerAuth(this.url);
            if (sauth != null && sauth.supportsPreemptiveAuthorization()) {
                this.requests.setIfNotSet(sauth.getHeaderName(), sauth.getHeaderValue(this.url, this.method));
                this.currentServerCredentials = sauth;
            }
            if (!this.method.equals("PUT") && (this.poster != null || streaming())) {
                this.requests.setIfNotSet("Content-type", "application/x-www-form-urlencoded");
            }
            boolean chunked = false;
            if (streaming()) {
                if (this.chunkLength != -1) {
                    this.requests.set("Transfer-Encoding", "chunked");
                    chunked = true;
                } else if (this.fixedContentLengthLong != -1) {
                    this.requests.set("Content-Length", String.valueOf(this.fixedContentLengthLong));
                } else if (this.fixedContentLength != -1) {
                    this.requests.set("Content-Length", String.valueOf(this.fixedContentLength));
                }
            } else if (this.poster != null) {
                synchronized (this.poster) {
                    this.poster.close();
                    this.requests.set("Content-Length", String.valueOf(this.poster.size()));
                }
            }
            if (!(chunked || this.requests.findValue("Transfer-Encoding") == null)) {
                this.requests.remove("Transfer-Encoding");
                if (logger.isLoggable((int) PlatformLogger.WARNING)) {
                    logger.warning("use streaming mode for chunked encoding");
                }
            }
            setCookieHeader();
            this.setRequests = true;
        }
        if (logger.isLoggable(500)) {
            logger.fine(this.requests.toString());
        }
        this.http.writeRequests(this.requests, this.poster, streaming());
        if (this.ps.checkError()) {
            String proxyHost = this.http.getProxyHostUsed();
            int proxyPort = this.http.getProxyPortUsed();
            disconnectInternal();
            if (this.failedOnce) {
                throw new IOException("Error writing to server");
            }
            this.failedOnce = true;
            if (proxyHost != null) {
                setProxiedClient(this.url, proxyHost, proxyPort);
            } else {
                setNewClient(this.url);
            }
            this.ps = (PrintStream) this.http.getOutputStream();
            this.connected = true;
            this.responses = new MessageHeader();
            this.setRequests = false;
            writeRequests();
        }
    }

    protected void setNewClient(URL url) throws IOException {
        setNewClient(url, false);
    }

    protected void setNewClient(URL url, boolean useCache) throws IOException {
        this.http = HttpClient.New(url, null, -1, useCache, this.connectTimeout, this);
        this.http.setReadTimeout(this.readTimeout);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort) throws IOException {
        setProxiedClient(url, proxyHost, proxyPort, false);
    }

    protected void setProxiedClient(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        proxiedConnect(url, proxyHost, proxyPort, useCache);
    }

    protected void proxiedConnect(URL url, String proxyHost, int proxyPort, boolean useCache) throws IOException {
        this.http = HttpClient.New(url, proxyHost, proxyPort, useCache, this.connectTimeout, this);
        this.http.setReadTimeout(this.readTimeout);
    }

    protected HttpURLConnection(URL u, Handler handler) throws IOException {
        this(u, null, handler);
    }

    public HttpURLConnection(URL u, String host, int port) {
        this(u, new Proxy(Type.HTTP, InetSocketAddress.createUnresolved(host, port)));
    }

    public HttpURLConnection(URL u, Proxy p) {
        this(u, p, new Handler());
    }

    protected HttpURLConnection(URL u, Proxy p, Handler handler) {
        super(u);
        this.ps = null;
        this.errorStream = null;
        this.setUserCookies = true;
        this.userCookies = null;
        this.userCookies2 = null;
        this.currentProxyCredentials = null;
        this.currentServerCredentials = null;
        this.needToCheck = true;
        this.doingNTLM2ndStage = false;
        this.doingNTLMp2ndStage = false;
        this.tryTransparentNTLMServer = true;
        this.tryTransparentNTLMProxy = true;
        this.inputStream = null;
        this.poster = null;
        this.setRequests = false;
        this.failedOnce = false;
        this.rememberedException = null;
        this.reuseClient = null;
        this.tunnelState = TunnelState.NONE;
        this.connectTimeout = -1;
        this.readTimeout = -1;
        this.requestURI = null;
        this.cdata = new byte[128];
        this.requests = new MessageHeader();
        this.responses = new MessageHeader();
        this.handler = handler;
        this.instProxy = p;
        if (this.instProxy instanceof ApplicationProxy) {
            try {
                this.cookieHandler = CookieHandler.getDefault();
            } catch (SecurityException e) {
            }
        } else {
            this.cookieHandler = (CookieHandler) AccessController.doPrivileged(new PrivilegedAction<CookieHandler>() {
                public CookieHandler run() {
                    return CookieHandler.getDefault();
                }
            });
        }
        this.cacheHandler = (ResponseCache) AccessController.doPrivileged(new PrivilegedAction<ResponseCache>() {
            public ResponseCache run() {
                return ResponseCache.getDefault();
            }
        });
    }

    public static void setDefaultAuthenticator(HttpAuthenticator a) {
        defaultAuth = a;
    }

    public static InputStream openConnectionCheckRedirects(URLConnection c) throws IOException {
        InputStream in;
        int redirects = 0;
        boolean redir;
        do {
            if (c instanceof HttpURLConnection) {
                ((HttpURLConnection) c).setInstanceFollowRedirects(false);
            }
            in = c.getInputStream();
            redir = false;
            if (c instanceof HttpURLConnection) {
                HttpURLConnection http = (HttpURLConnection) c;
                int stat = http.getResponseCode();
                if (stat >= 300 && stat <= 307 && stat != 306 && stat != java.net.HttpURLConnection.HTTP_NOT_MODIFIED) {
                    URL base = http.getURL();
                    String loc = http.getHeaderField("Location");
                    URL url = null;
                    if (loc != null) {
                        url = new URL(base, loc);
                    }
                    http.disconnect();
                    if (url != null && base.getProtocol().equals(url.getProtocol()) && base.getPort() == url.getPort() && hostsEqual(base, url) && redirects < 5) {
                        redir = true;
                        c = url.openConnection();
                        redirects++;
                        continue;
                    } else {
                        throw new SecurityException("illegal URL redirect");
                    }
                }
            }
        } while (redir);
        return in;
    }

    private static boolean hostsEqual(URL u1, URL u2) {
        boolean z = true;
        final String h1 = u1.getHost();
        final String h2 = u2.getHost();
        if (h1 == null) {
            if (h2 != null) {
                z = false;
            }
            return z;
        } else if (h2 == null) {
            return false;
        } else {
            if (h1.equalsIgnoreCase(h2)) {
                return true;
            }
            final boolean[] result = new boolean[]{false};
            AccessController.doPrivileged(new PrivilegedAction<Void>() {
                public Void run() {
                    try {
                        result[0] = InetAddress.getByName(h1).equals(InetAddress.getByName(h2));
                    } catch (UnknownHostException e) {
                    } catch (SecurityException e2) {
                    }
                    return null;
                }
            });
            return result[0];
        }
    }

    public void connect() throws IOException {
        plainConnect();
    }

    private boolean checkReuseConnection() {
        if (this.connected) {
            return true;
        }
        if (this.reuseClient == null) {
            return false;
        }
        this.http = this.reuseClient;
        this.http.setReadTimeout(getReadTimeout());
        this.http.reuse = false;
        this.reuseClient = null;
        this.connected = true;
        return true;
    }

    protected void plainConnect() throws IOException {
        ProxySelector sel;
        Iterator<Proxy> it;
        if (!this.connected) {
            Object uri;
            if (this.cacheHandler != null && getUseCaches()) {
                try {
                    uri = ParseUtil.toURI(this.url);
                    if (uri != null) {
                        this.cachedResponse = this.cacheHandler.get(uri, getRequestMethod(), this.requests.getHeaders(EXCLUDE_HEADERS));
                        if ("https".equalsIgnoreCase(uri.getScheme()) && !(this.cachedResponse instanceof SecureCacheResponse)) {
                            this.cachedResponse = null;
                        }
                        if (logger.isLoggable(300)) {
                            logger.finest("Cache Request for " + uri + " / " + getRequestMethod());
                            logger.finest("From cache: " + (this.cachedResponse != null ? this.cachedResponse.toString() : "null"));
                        }
                        if (this.cachedResponse != null) {
                            this.cachedHeaders = mapToMessageHeader(this.cachedResponse.getHeaders());
                            this.cachedInputStream = this.cachedResponse.getBody();
                        }
                    }
                } catch (IOException e) {
                }
                if (this.cachedHeaders == null || this.cachedInputStream == null) {
                    this.cachedResponse = null;
                } else {
                    this.connected = true;
                    return;
                }
            }
            Proxy p;
            try {
                if (this.instProxy == null) {
                    sel = (ProxySelector) AccessController.doPrivileged(new PrivilegedAction<ProxySelector>() {
                        public ProxySelector run() {
                            return ProxySelector.getDefault();
                        }
                    });
                    if (sel != null) {
                        uri = ParseUtil.toURI(this.url);
                        if (logger.isLoggable(300)) {
                            logger.finest("ProxySelector Request for " + uri);
                        }
                        it = sel.select(uri).iterator();
                        while (it.hasNext()) {
                            p = (Proxy) it.next();
                            if (this.failedOnce) {
                                this.http = getNewHttpClient(this.url, p, this.connectTimeout, false);
                                this.http.setReadTimeout(this.readTimeout);
                            } else {
                                this.http = getNewHttpClient(this.url, p, this.connectTimeout);
                                this.http.setReadTimeout(this.readTimeout);
                            }
                            if (logger.isLoggable(300) && p != null) {
                                logger.finest("Proxy used: " + p.toString());
                            }
                        }
                    } else if (this.failedOnce) {
                        this.http = getNewHttpClient(this.url, null, this.connectTimeout, false);
                        this.http.setReadTimeout(this.readTimeout);
                    } else {
                        this.http = getNewHttpClient(this.url, null, this.connectTimeout);
                        this.http.setReadTimeout(this.readTimeout);
                    }
                } else if (this.failedOnce) {
                    this.http = getNewHttpClient(this.url, this.instProxy, this.connectTimeout, false);
                    this.http.setReadTimeout(this.readTimeout);
                } else {
                    this.http = getNewHttpClient(this.url, this.instProxy, this.connectTimeout);
                    this.http.setReadTimeout(this.readTimeout);
                }
            } catch (IOException ioex) {
                if (p != Proxy.NO_PROXY) {
                    sel.connectFailed(uri, p.address(), ioex);
                    if (!it.hasNext()) {
                        this.http = getNewHttpClient(this.url, null, this.connectTimeout, false);
                        this.http.setReadTimeout(this.readTimeout);
                        break;
                    }
                }
                throw ioex;
            } catch (IOException e2) {
                throw e2;
            }
            this.ps = (PrintStream) this.http.getOutputStream();
            this.connected = true;
        }
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout) throws IOException {
        return HttpClient.New(url, p, connectTimeout, this);
    }

    protected HttpClient getNewHttpClient(URL url, Proxy p, int connectTimeout, boolean useCache) throws IOException {
        return HttpClient.New(url, p, connectTimeout, useCache, this);
    }

    private void expect100Continue() throws IOException {
        int oldTimeout = this.http.getReadTimeout();
        boolean enforceTimeOut = false;
        boolean timedOut = false;
        if (oldTimeout <= 0) {
            this.http.setReadTimeout(5000);
            enforceTimeOut = true;
        }
        try {
            this.http.parseHTTP(this.responses, this.pi, this);
        } catch (SocketTimeoutException se) {
            if (enforceTimeOut) {
                timedOut = true;
                this.http.setIgnoreContinue(true);
            } else {
                throw se;
            }
        }
        if (!timedOut) {
            String resp = this.responses.getValue(0);
            if (resp != null && resp.startsWith("HTTP/")) {
                String[] sa = resp.split("\\s+");
                this.responseCode = -1;
                try {
                    if (sa.length > 1) {
                        this.responseCode = Integer.parseInt(sa[1]);
                    }
                } catch (NumberFormatException e) {
                }
            }
            if (this.responseCode != 100) {
                throw new ProtocolException("Server rejected operation");
            }
        }
        this.http.setReadTimeout(oldTimeout);
        this.responseCode = -1;
        this.responses.reset();
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        try {
            if (this.doOutput) {
                if (this.method.equals("GET")) {
                    this.method = "POST";
                }
                if (!("POST".equals(this.method) || "PUT".equals(this.method))) {
                    if ("http".equals(this.url.getProtocol())) {
                        throw new ProtocolException("HTTP method " + this.method + " doesn't support output");
                    }
                }
                if (this.inputStream != null) {
                    throw new ProtocolException("Cannot write output after reading input.");
                }
                if (!checkReuseConnection()) {
                    connect();
                }
                boolean expectContinue = false;
                if ("100-Continue".equalsIgnoreCase(this.requests.findValue("Expect"))) {
                    this.http.setIgnoreContinue(false);
                    expectContinue = true;
                }
                if (streaming() && this.strOutputStream == null) {
                    writeRequests();
                }
                if (expectContinue) {
                    expect100Continue();
                }
                this.ps = (PrintStream) this.http.getOutputStream();
                if (streaming()) {
                    if (this.strOutputStream == null) {
                        if (this.chunkLength != -1) {
                            this.strOutputStream = new StreamingOutputStream(new ChunkedOutputStream(this.ps, this.chunkLength), -1);
                        } else {
                            long length = 0;
                            if (this.fixedContentLengthLong != -1) {
                                length = this.fixedContentLengthLong;
                            } else if (this.fixedContentLength != -1) {
                                length = (long) this.fixedContentLength;
                            }
                            this.strOutputStream = new StreamingOutputStream(this.ps, length);
                        }
                    }
                    return this.strOutputStream;
                }
                if (this.poster == null) {
                    this.poster = new PosterOutputStream();
                }
                return this.poster;
            }
            throw new ProtocolException("cannot write to a URLConnection if doOutput=false - call setDoOutput(true)");
        } catch (RuntimeException e) {
            disconnectInternal();
            throw e;
        } catch (ProtocolException e2) {
            int i = this.responseCode;
            disconnectInternal();
            this.responseCode = i;
            throw e2;
        } catch (IOException e3) {
            disconnectInternal();
            throw e3;
        }
    }

    public boolean streaming() {
        if (this.fixedContentLength == -1 && this.fixedContentLengthLong == -1 && this.chunkLength == -1) {
            return false;
        }
        return true;
    }

    private void setCookieHeader() throws IOException {
        if (this.cookieHandler != null) {
            int k;
            synchronized (this) {
                if (this.setUserCookies) {
                    k = this.requests.getKey("Cookie");
                    if (k != -1) {
                        this.userCookies = this.requests.getValue(k);
                    }
                    k = this.requests.getKey("Cookie2");
                    if (k != -1) {
                        this.userCookies2 = this.requests.getValue(k);
                    }
                    this.setUserCookies = false;
                }
            }
            this.requests.remove("Cookie");
            this.requests.remove("Cookie2");
            Object uri = ParseUtil.toURI(this.url);
            if (uri != null) {
                if (logger.isLoggable(300)) {
                    logger.finest("CookieHandler request for " + uri);
                }
                Map<String, List<String>> cookies = this.cookieHandler.get(uri, this.requests.getHeaders(EXCLUDE_HEADERS));
                if (!cookies.isEmpty()) {
                    if (logger.isLoggable(300)) {
                        logger.finest("Cookies retrieved: " + cookies.toString());
                    }
                    for (Entry<String, List<String>> entry : cookies.entrySet()) {
                        String key = (String) entry.getKey();
                        if ("Cookie".equalsIgnoreCase(key) || "Cookie2".equalsIgnoreCase(key)) {
                            List<String> l = (List) entry.getValue();
                            if (!(l == null || l.isEmpty())) {
                                StringBuilder cookieValue = new StringBuilder();
                                for (String value : l) {
                                    cookieValue.append(value).append("; ");
                                }
                                try {
                                    this.requests.add(key, cookieValue.substring(0, cookieValue.length() - 2));
                                } catch (StringIndexOutOfBoundsException e) {
                                }
                            }
                        }
                    }
                }
            }
            if (this.userCookies != null) {
                k = this.requests.getKey("Cookie");
                if (k != -1) {
                    this.requests.set("Cookie", this.requests.getValue(k) + ";" + this.userCookies);
                } else {
                    this.requests.set("Cookie", this.userCookies);
                }
            }
            if (this.userCookies2 != null) {
                k = this.requests.getKey("Cookie2");
                if (k != -1) {
                    this.requests.set("Cookie2", this.requests.getValue(k) + ";" + this.userCookies2);
                } else {
                    this.requests.set("Cookie2", this.userCookies2);
                }
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized InputStream getInputStream() throws IOException {
        Exception e;
        Exception e2;
        if (!this.doInput) {
            throw new ProtocolException("Cannot read from URLConnection if doInput=false (call setDoInput(true))");
        } else if (this.rememberedException != null) {
            if (this.rememberedException instanceof RuntimeException) {
                throw new RuntimeException(this.rememberedException);
            }
            throw getChainedException((IOException) this.rememberedException);
        } else if (this.inputStream != null) {
            return this.inputStream;
        } else {
            int respCode;
            InputStream inputStream;
            URI uri;
            URLConnection uconn;
            CacheRequest cacheRequest;
            if (streaming()) {
                if (this.strOutputStream == null) {
                    getOutputStream();
                }
                this.strOutputStream.close();
                if (!this.strOutputStream.writtenOK()) {
                    throw new IOException("Incomplete output stream");
                }
            }
            int redirects = 0;
            long cl = -1;
            AuthenticationInfo serverAuthentication = null;
            AuthenticationInfo proxyAuthentication = null;
            boolean inNegotiate = false;
            boolean inNegotiateProxy = false;
            this.isUserServerAuth = this.requests.getKey("Authorization") != -1;
            this.isUserProxyAuth = this.requests.getKey("Proxy-Authorization") != -1;
            AuthenticationHeader srvHdr = null;
            while (true) {
                AuthenticationHeader srvHdr2;
                try {
                    if (!checkReuseConnection()) {
                        connect();
                    }
                    if (this.cachedInputStream != null) {
                        break;
                    }
                    if (ProgressMonitor.getDefault().shouldMeterInput(this.url, this.method)) {
                        this.pi = new ProgressSource(this.url, this.method);
                        this.pi.beginTracking();
                    }
                    this.ps = (PrintStream) this.http.getOutputStream();
                    if (!streaming()) {
                        writeRequests();
                    }
                    this.http.parseHTTP(this.responses, this.pi, this);
                    if (logger.isLoggable(500)) {
                        logger.fine(this.responses.toString());
                    }
                    boolean b1 = this.responses.filterNTLMResponses("WWW-Authenticate");
                    boolean b2 = this.responses.filterNTLMResponses("Proxy-Authenticate");
                    if ((b1 || b2) && logger.isLoggable(500)) {
                        logger.fine(">>>> Headers are filtered");
                        logger.fine(this.responses.toString());
                    }
                    this.inputStream = this.http.getInputStream();
                    respCode = getResponseCode();
                    if (respCode == -1) {
                        disconnectInternal();
                        throw new IOException("Invalid Http response");
                    }
                    boolean dontUseNegotiate;
                    Iterator iter;
                    String value;
                    AuthenticationHeader authenticationHeader;
                    String raw;
                    String npath;
                    String opath;
                    AuthenticationInfo a;
                    if (respCode != 407) {
                        inNegotiateProxy = false;
                        this.doingNTLMp2ndStage = false;
                        if (!this.isUserProxyAuth) {
                            this.requests.remove("Proxy-Authorization");
                        }
                        if (proxyAuthentication != null) {
                            proxyAuthentication.addToCache();
                        }
                        if (respCode == 401) {
                            srvHdr2 = srvHdr;
                        } else if (streaming()) {
                            disconnectInternal();
                            throw new HttpRetryException(RETRY_MSG2, java.net.HttpURLConnection.HTTP_UNAUTHORIZED);
                        } else {
                            dontUseNegotiate = false;
                            iter = this.responses.multiValueIterator("WWW-Authenticate");
                            while (iter.hasNext()) {
                                value = ((String) iter.next()).trim();
                                if (value.equalsIgnoreCase("Negotiate")) {
                                    if (value.equalsIgnoreCase("Kerberos")) {
                                    }
                                }
                                if (inNegotiate) {
                                    dontUseNegotiate = true;
                                    this.doingNTLM2ndStage = false;
                                    serverAuthentication = null;
                                } else {
                                    inNegotiate = true;
                                }
                                authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                                raw = authenticationHeader.raw();
                                if (this.doingNTLM2ndStage) {
                                    reset();
                                    if (serverAuthentication.setHeaders(this, null, raw)) {
                                        disconnectWeb();
                                        throw new IOException("Authentication failure");
                                    }
                                    this.doingNTLM2ndStage = false;
                                    this.authObj = null;
                                    setCookieHeader();
                                } else if (serverAuthentication.isAuthorizationStale(raw)) {
                                    serverAuthentication.removeFromCache();
                                    serverAuthentication = getServerAuthentication(authenticationHeader);
                                    this.currentServerCredentials = serverAuthentication;
                                    if (serverAuthentication != null) {
                                        disconnectWeb();
                                        redirects++;
                                        setCookieHeader();
                                    }
                                } else {
                                    disconnectWeb();
                                    redirects++;
                                    this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                    this.currentServerCredentials = serverAuthentication;
                                    setCookieHeader();
                                }
                            }
                            authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                            raw = authenticationHeader.raw();
                            if (this.doingNTLM2ndStage) {
                                reset();
                                if (serverAuthentication.setHeaders(this, null, raw)) {
                                    this.doingNTLM2ndStage = false;
                                    this.authObj = null;
                                    setCookieHeader();
                                } else {
                                    disconnectWeb();
                                    throw new IOException("Authentication failure");
                                }
                            } else if (serverAuthentication.isAuthorizationStale(raw)) {
                                serverAuthentication.removeFromCache();
                                serverAuthentication = getServerAuthentication(authenticationHeader);
                                this.currentServerCredentials = serverAuthentication;
                                if (serverAuthentication != null) {
                                    disconnectWeb();
                                    redirects++;
                                    setCookieHeader();
                                }
                            } else {
                                disconnectWeb();
                                redirects++;
                                this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                this.currentServerCredentials = serverAuthentication;
                                setCookieHeader();
                            }
                        }
                        if (serverAuthentication != null) {
                            if (serverAuthentication instanceof DigestAuthentication) {
                            }
                            if (serverAuthentication instanceof BasicAuthentication) {
                                npath = AuthenticationInfo.reducePath(this.url.getPath());
                                opath = serverAuthentication.path;
                                npath = BasicAuthentication.getRootPath(opath, npath);
                                a = (BasicAuthentication) serverAuthentication.clone();
                                serverAuthentication.removeFromCache();
                                a.path = npath;
                                serverAuthentication = a;
                            }
                            serverAuthentication.addToCache();
                        }
                        inNegotiate = false;
                        inNegotiateProxy = false;
                        this.doingNTLMp2ndStage = false;
                        this.doingNTLM2ndStage = false;
                        if (!this.isUserServerAuth) {
                            this.requests.remove("Authorization");
                        }
                        if (!this.isUserProxyAuth) {
                            this.requests.remove("Proxy-Authorization");
                        }
                        if (respCode == 200) {
                            checkResponseCredentials(false);
                        } else {
                            this.needToCheck = false;
                        }
                        this.needToCheck = true;
                        if (!followRedirect()) {
                            break;
                        }
                        redirects++;
                        setCookieHeader();
                    } else if (streaming()) {
                        disconnectInternal();
                        throw new HttpRetryException(RETRY_MSG1, java.net.HttpURLConnection.HTTP_PROXY_AUTH);
                    } else {
                        AuthenticationHeader authhdr;
                        dontUseNegotiate = false;
                        iter = this.responses.multiValueIterator("Proxy-Authenticate");
                        while (iter.hasNext()) {
                            value = ((String) iter.next()).trim();
                            if (!value.equalsIgnoreCase("Negotiate")) {
                                if (value.equalsIgnoreCase("Kerberos")) {
                                }
                            }
                            if (inNegotiateProxy) {
                                dontUseNegotiate = true;
                                this.doingNTLMp2ndStage = false;
                                proxyAuthentication = null;
                            } else {
                                inNegotiateProxy = true;
                            }
                            authhdr = new AuthenticationHeader("Proxy-Authenticate", this.responses, new HttpCallerInfo(this.url, this.http.getProxyHostUsed(), this.http.getProxyPortUsed()), dontUseNegotiate);
                            if (this.doingNTLMp2ndStage) {
                                proxyAuthentication = resetProxyAuthentication(proxyAuthentication, authhdr);
                                if (proxyAuthentication != null) {
                                    redirects++;
                                    disconnectInternal();
                                    srvHdr2 = srvHdr;
                                }
                                if (proxyAuthentication != null) {
                                    proxyAuthentication.addToCache();
                                }
                                if (respCode == 401) {
                                    srvHdr2 = srvHdr;
                                } else if (streaming()) {
                                    disconnectInternal();
                                    throw new HttpRetryException(RETRY_MSG2, java.net.HttpURLConnection.HTTP_UNAUTHORIZED);
                                } else {
                                    dontUseNegotiate = false;
                                    iter = this.responses.multiValueIterator("WWW-Authenticate");
                                    while (iter.hasNext()) {
                                        value = ((String) iter.next()).trim();
                                        if (value.equalsIgnoreCase("Negotiate")) {
                                            if (value.equalsIgnoreCase("Kerberos")) {
                                            }
                                        }
                                        if (inNegotiate) {
                                            inNegotiate = true;
                                        } else {
                                            dontUseNegotiate = true;
                                            this.doingNTLM2ndStage = false;
                                            serverAuthentication = null;
                                        }
                                        authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                                        raw = authenticationHeader.raw();
                                        if (this.doingNTLM2ndStage) {
                                            if (!(serverAuthentication == null || serverAuthentication.getAuthScheme() == AuthScheme.NTLM)) {
                                                if (serverAuthentication.isAuthorizationStale(raw)) {
                                                    disconnectWeb();
                                                    redirects++;
                                                    this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                                    this.currentServerCredentials = serverAuthentication;
                                                    setCookieHeader();
                                                } else {
                                                    serverAuthentication.removeFromCache();
                                                }
                                            }
                                            serverAuthentication = getServerAuthentication(authenticationHeader);
                                            this.currentServerCredentials = serverAuthentication;
                                            if (serverAuthentication != null) {
                                                disconnectWeb();
                                                redirects++;
                                                setCookieHeader();
                                            }
                                        } else {
                                            reset();
                                            if (serverAuthentication.setHeaders(this, null, raw)) {
                                                disconnectWeb();
                                                throw new IOException("Authentication failure");
                                            }
                                            this.doingNTLM2ndStage = false;
                                            this.authObj = null;
                                            setCookieHeader();
                                        }
                                    }
                                    authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                                    raw = authenticationHeader.raw();
                                    if (this.doingNTLM2ndStage) {
                                        reset();
                                        if (serverAuthentication.setHeaders(this, null, raw)) {
                                            this.doingNTLM2ndStage = false;
                                            this.authObj = null;
                                            setCookieHeader();
                                        } else {
                                            disconnectWeb();
                                            throw new IOException("Authentication failure");
                                        }
                                    } else if (serverAuthentication.isAuthorizationStale(raw)) {
                                        serverAuthentication.removeFromCache();
                                        serverAuthentication = getServerAuthentication(authenticationHeader);
                                        this.currentServerCredentials = serverAuthentication;
                                        if (serverAuthentication != null) {
                                            disconnectWeb();
                                            redirects++;
                                            setCookieHeader();
                                        }
                                    } else {
                                        disconnectWeb();
                                        redirects++;
                                        this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                        this.currentServerCredentials = serverAuthentication;
                                        setCookieHeader();
                                    }
                                }
                                if (serverAuthentication != null) {
                                    if ((serverAuthentication instanceof DigestAuthentication) || this.domain == null) {
                                        if (serverAuthentication instanceof BasicAuthentication) {
                                            npath = AuthenticationInfo.reducePath(this.url.getPath());
                                            opath = serverAuthentication.path;
                                            if (!opath.startsWith(npath) || npath.length() >= opath.length()) {
                                                npath = BasicAuthentication.getRootPath(opath, npath);
                                            }
                                            a = (BasicAuthentication) serverAuthentication.clone();
                                            serverAuthentication.removeFromCache();
                                            a.path = npath;
                                            serverAuthentication = a;
                                        }
                                        serverAuthentication.addToCache();
                                    } else {
                                        DigestAuthentication srv = (DigestAuthentication) serverAuthentication;
                                        StringTokenizer stringTokenizer = new StringTokenizer(this.domain, " ");
                                        String realm = srv.realm;
                                        PasswordAuthentication pw = srv.pw;
                                        this.digestparams = srv.params;
                                        while (stringTokenizer.hasMoreTokens()) {
                                            try {
                                                new DigestAuthentication(false, new URL(this.url, stringTokenizer.nextToken()), realm, "Digest", pw, this.digestparams).addToCache();
                                            } catch (Exception e3) {
                                            }
                                        }
                                    }
                                }
                                inNegotiate = false;
                                inNegotiateProxy = false;
                                this.doingNTLMp2ndStage = false;
                                this.doingNTLM2ndStage = false;
                                if (this.isUserServerAuth) {
                                    this.requests.remove("Authorization");
                                }
                                if (this.isUserProxyAuth) {
                                    this.requests.remove("Proxy-Authorization");
                                }
                                if (respCode == 200) {
                                    checkResponseCredentials(false);
                                } else {
                                    this.needToCheck = false;
                                }
                                this.needToCheck = true;
                                if (!followRedirect()) {
                                    redirects++;
                                    setCookieHeader();
                                } else {
                                    try {
                                        break;
                                    } catch (Exception e4) {
                                    }
                                }
                            } else {
                                raw = this.responses.findValue("Proxy-Authenticate");
                                reset();
                                if (proxyAuthentication.setHeaders(this, authhdr.headerParser(), raw)) {
                                    disconnectInternal();
                                    throw new IOException("Authentication failure");
                                } else if (serverAuthentication != null || srvHdr == null || serverAuthentication.setHeaders(this, srvHdr.headerParser(), raw)) {
                                    this.authObj = null;
                                    this.doingNTLMp2ndStage = false;
                                    srvHdr2 = srvHdr;
                                } else {
                                    disconnectInternal();
                                    throw new IOException("Authentication failure");
                                }
                            }
                        }
                        authhdr = new AuthenticationHeader("Proxy-Authenticate", this.responses, new HttpCallerInfo(this.url, this.http.getProxyHostUsed(), this.http.getProxyPortUsed()), dontUseNegotiate);
                        if (this.doingNTLMp2ndStage) {
                            raw = this.responses.findValue("Proxy-Authenticate");
                            reset();
                            if (proxyAuthentication.setHeaders(this, authhdr.headerParser(), raw)) {
                                if (serverAuthentication != null) {
                                }
                                this.authObj = null;
                                this.doingNTLMp2ndStage = false;
                                srvHdr2 = srvHdr;
                            } else {
                                disconnectInternal();
                                throw new IOException("Authentication failure");
                            }
                        }
                        proxyAuthentication = resetProxyAuthentication(proxyAuthentication, authhdr);
                        if (proxyAuthentication != null) {
                            redirects++;
                            disconnectInternal();
                            srvHdr2 = srvHdr;
                        }
                        if (proxyAuthentication != null) {
                            proxyAuthentication.addToCache();
                        }
                        if (respCode == 401) {
                            srvHdr2 = srvHdr;
                        } else if (streaming()) {
                            dontUseNegotiate = false;
                            iter = this.responses.multiValueIterator("WWW-Authenticate");
                            while (iter.hasNext()) {
                                value = ((String) iter.next()).trim();
                                if (value.equalsIgnoreCase("Negotiate")) {
                                    if (value.equalsIgnoreCase("Kerberos")) {
                                    }
                                }
                                if (inNegotiate) {
                                    dontUseNegotiate = true;
                                    this.doingNTLM2ndStage = false;
                                    serverAuthentication = null;
                                } else {
                                    inNegotiate = true;
                                }
                                authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                                raw = authenticationHeader.raw();
                                if (this.doingNTLM2ndStage) {
                                    reset();
                                    if (serverAuthentication.setHeaders(this, null, raw)) {
                                        disconnectWeb();
                                        throw new IOException("Authentication failure");
                                    }
                                    this.doingNTLM2ndStage = false;
                                    this.authObj = null;
                                    setCookieHeader();
                                } else if (serverAuthentication.isAuthorizationStale(raw)) {
                                    disconnectWeb();
                                    redirects++;
                                    this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                    this.currentServerCredentials = serverAuthentication;
                                    setCookieHeader();
                                } else {
                                    serverAuthentication.removeFromCache();
                                    serverAuthentication = getServerAuthentication(authenticationHeader);
                                    this.currentServerCredentials = serverAuthentication;
                                    if (serverAuthentication != null) {
                                        disconnectWeb();
                                        redirects++;
                                        setCookieHeader();
                                    }
                                }
                            }
                            authenticationHeader = new AuthenticationHeader("WWW-Authenticate", this.responses, new HttpCallerInfo(this.url), dontUseNegotiate);
                            raw = authenticationHeader.raw();
                            if (this.doingNTLM2ndStage) {
                                reset();
                                if (serverAuthentication.setHeaders(this, null, raw)) {
                                    this.doingNTLM2ndStage = false;
                                    this.authObj = null;
                                    setCookieHeader();
                                } else {
                                    disconnectWeb();
                                    throw new IOException("Authentication failure");
                                }
                            } else if (serverAuthentication.isAuthorizationStale(raw)) {
                                serverAuthentication.removeFromCache();
                                serverAuthentication = getServerAuthentication(authenticationHeader);
                                this.currentServerCredentials = serverAuthentication;
                                if (serverAuthentication != null) {
                                    disconnectWeb();
                                    redirects++;
                                    setCookieHeader();
                                }
                            } else {
                                disconnectWeb();
                                redirects++;
                                this.requests.set(serverAuthentication.getHeaderName(), serverAuthentication.getHeaderValue(this.url, this.method));
                                this.currentServerCredentials = serverAuthentication;
                                setCookieHeader();
                            }
                        } else {
                            disconnectInternal();
                            throw new HttpRetryException(RETRY_MSG2, java.net.HttpURLConnection.HTTP_UNAUTHORIZED);
                        }
                        if (serverAuthentication != null) {
                            if (serverAuthentication instanceof DigestAuthentication) {
                            }
                            if (serverAuthentication instanceof BasicAuthentication) {
                                npath = AuthenticationInfo.reducePath(this.url.getPath());
                                opath = serverAuthentication.path;
                                npath = BasicAuthentication.getRootPath(opath, npath);
                                a = (BasicAuthentication) serverAuthentication.clone();
                                serverAuthentication.removeFromCache();
                                a.path = npath;
                                serverAuthentication = a;
                            }
                            serverAuthentication.addToCache();
                        }
                        inNegotiate = false;
                        inNegotiateProxy = false;
                        this.doingNTLMp2ndStage = false;
                        this.doingNTLM2ndStage = false;
                        if (this.isUserServerAuth) {
                            this.requests.remove("Authorization");
                        }
                        if (this.isUserProxyAuth) {
                            this.requests.remove("Proxy-Authorization");
                        }
                        if (respCode == 200) {
                            this.needToCheck = false;
                        } else {
                            checkResponseCredentials(false);
                        }
                        this.needToCheck = true;
                        if (!followRedirect()) {
                            break;
                        }
                        redirects++;
                        setCookieHeader();
                    }
                    try {
                        if (redirects < maxRedirects) {
                            srvHdr = srvHdr2;
                        } else {
                            throw new ProtocolException("Server redirected too many  times (" + redirects + ")");
                        }
                    } catch (RuntimeException e5) {
                        e = e5;
                    } catch (IOException e6) {
                        e2 = e6;
                    }
                } catch (RuntimeException e7) {
                    e = e7;
                } catch (IOException e8) {
                    e2 = e8;
                    srvHdr2 = srvHdr;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    srvHdr2 = srvHdr;
                }
            }
            cl = Long.parseLong(this.responses.findValue("content-length"));
            if (!(this.method.equals("HEAD") || cl == 0 || respCode == 304)) {
                if (respCode == 204) {
                }
                if (!(respCode == 200 || respCode == 203 || respCode == 206 || respCode == 300 || respCode == 301)) {
                    if (respCode == 410) {
                    }
                    if (!(this.inputStream instanceof HttpInputStream)) {
                        this.inputStream = new HttpInputStream(this.inputStream);
                    }
                    if (respCode < 400) {
                        this.poster = null;
                        this.strOutputStream = null;
                        inputStream = this.inputStream;
                        if (this.proxyAuthKey != null) {
                            AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
                        }
                        if (this.serverAuthKey != null) {
                            AuthenticationInfo.endAuthRequest(this.serverAuthKey);
                        }
                    } else if (respCode != 404 || respCode == 410) {
                        throw new FileNotFoundException(this.url.toString());
                    } else {
                        throw new IOException("Server returned HTTP response code: " + respCode + " for URL: " + this.url.toString());
                    }
                }
                if (this.cacheHandler != null) {
                    uri = ParseUtil.toURI(this.url);
                    if (uri != null) {
                        uconn = this;
                        if ("https".equalsIgnoreCase(uri.getScheme())) {
                            try {
                                uconn = (URLConnection) getClass().getField("httpsURLConnection").get(this);
                            } catch (IllegalAccessException e9) {
                            } catch (NoSuchFieldException e10) {
                            }
                        }
                        cacheRequest = this.cacheHandler.put(uri, uconn);
                        if (!(cacheRequest == null || this.http == null)) {
                            this.http.setCacheRequest(cacheRequest);
                            this.inputStream = new HttpInputStream(this.inputStream, cacheRequest);
                        }
                    }
                }
                if (this.inputStream instanceof HttpInputStream) {
                    this.inputStream = new HttpInputStream(this.inputStream);
                }
                if (respCode < 400) {
                    this.poster = null;
                    this.strOutputStream = null;
                    inputStream = this.inputStream;
                    if (this.proxyAuthKey != null) {
                        AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
                    }
                    if (this.serverAuthKey != null) {
                        AuthenticationInfo.endAuthRequest(this.serverAuthKey);
                    }
                } else {
                    if (respCode != 404) {
                    }
                    throw new FileNotFoundException(this.url.toString());
                }
            }
            if (this.pi != null) {
                this.pi.finishTracking();
                this.pi = null;
            }
            this.http.finished();
            this.http = null;
            this.inputStream = new EmptyInputStream();
            this.connected = false;
            if (respCode == 410) {
                if (this.cacheHandler != null) {
                    uri = ParseUtil.toURI(this.url);
                    if (uri != null) {
                        uconn = this;
                        if ("https".equalsIgnoreCase(uri.getScheme())) {
                            uconn = (URLConnection) getClass().getField("httpsURLConnection").get(this);
                        }
                        cacheRequest = this.cacheHandler.put(uri, uconn);
                        this.http.setCacheRequest(cacheRequest);
                        this.inputStream = new HttpInputStream(this.inputStream, cacheRequest);
                    }
                }
            }
            if (this.inputStream instanceof HttpInputStream) {
                this.inputStream = new HttpInputStream(this.inputStream);
            }
            if (respCode < 400) {
                if (respCode != 404) {
                }
                throw new FileNotFoundException(this.url.toString());
            }
            this.poster = null;
            this.strOutputStream = null;
            inputStream = this.inputStream;
            if (this.proxyAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
            }
            if (this.serverAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.serverAuthKey);
            }
        }
        this.rememberedException = e2;
        String te = this.responses.findValue("Transfer-Encoding");
        if (this.http != null && this.http.isKeepingAlive() && enableESBuffer) {
            if (cl <= 0) {
                if (te != null) {
                }
            }
            this.errorStream = ErrorStream.getErrorStream(this.inputStream, cl, this.http);
        }
        throw e2;
        try {
            disconnectInternal();
            this.rememberedException = e;
            throw e;
        } catch (Throwable th3) {
            th2 = th3;
            if (this.proxyAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
            }
            if (this.serverAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.serverAuthKey);
            }
            throw th2;
        }
    }

    private IOException getChainedException(final IOException rememberedException) {
        try {
            final Object[] args = new Object[]{rememberedException.getMessage()};
            IOException chainedException = (IOException) AccessController.doPrivileged(new PrivilegedExceptionAction<IOException>() {
                public IOException run() throws Exception {
                    return (IOException) rememberedException.getClass().getConstructor(String.class).newInstance(args);
                }
            });
            chainedException.initCause(rememberedException);
            return chainedException;
        } catch (Exception e) {
            return rememberedException;
        }
    }

    public InputStream getErrorStream() {
        if (this.connected && this.responseCode >= 400) {
            if (this.errorStream != null) {
                return this.errorStream;
            }
            if (this.inputStream != null) {
                return this.inputStream;
            }
        }
        return null;
    }

    private AuthenticationInfo resetProxyAuthentication(AuthenticationInfo proxyAuthentication, AuthenticationHeader auth) throws IOException {
        if (!(proxyAuthentication == null || proxyAuthentication.getAuthScheme() == AuthScheme.NTLM)) {
            if (proxyAuthentication.isAuthorizationStale(auth.raw())) {
                String value;
                if (proxyAuthentication instanceof DigestAuthentication) {
                    DigestAuthentication digestProxy = (DigestAuthentication) proxyAuthentication;
                    if (tunnelState() == TunnelState.SETUP) {
                        value = digestProxy.getHeaderValue(connectRequestURI(this.url), HTTP_CONNECT);
                    } else {
                        value = digestProxy.getHeaderValue(getRequestURI(), this.method);
                    }
                } else {
                    value = proxyAuthentication.getHeaderValue(this.url, this.method);
                }
                this.requests.set(proxyAuthentication.getHeaderName(), value);
                this.currentProxyCredentials = proxyAuthentication;
                return proxyAuthentication;
            }
            proxyAuthentication.removeFromCache();
        }
        proxyAuthentication = getHttpProxyAuthentication(auth);
        this.currentProxyCredentials = proxyAuthentication;
        return proxyAuthentication;
    }

    TunnelState tunnelState() {
        return this.tunnelState;
    }

    public void setTunnelState(TunnelState tunnelState) {
        this.tunnelState = tunnelState;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void doTunneling() throws IOException {
        int retryTunnel = 0;
        String statusLine = "";
        AuthenticationInfo proxyAuthentication = null;
        String proxyHost = null;
        int proxyPort = -1;
        MessageHeader savedRequests = this.requests;
        this.requests = new MessageHeader();
        boolean inNegotiateProxy = false;
        try {
            setTunnelState(TunnelState.SETUP);
            while (true) {
                if (!checkReuseConnection()) {
                    proxiedConnect(this.url, proxyHost, proxyPort, false);
                }
                sendCONNECTRequest();
                this.responses.reset();
                this.http.parseHTTP(this.responses, null, this);
                if (logger.isLoggable(500)) {
                    logger.fine(this.responses.toString());
                }
                if (this.responses.filterNTLMResponses("Proxy-Authenticate") && logger.isLoggable(500)) {
                    logger.fine(">>>> Headers are filtered");
                    logger.fine(this.responses.toString());
                }
                statusLine = this.responses.getValue(0);
                StringTokenizer st = new StringTokenizer(statusLine);
                st.nextToken();
                int respCode = Integer.parseInt(st.nextToken().trim());
                if (respCode != 407) {
                    break;
                }
                AuthenticationHeader authhdr;
                String raw;
                boolean dontUseNegotiate = false;
                Iterator iter = this.responses.multiValueIterator("Proxy-Authenticate");
                while (iter.hasNext()) {
                    String value = ((String) iter.next()).trim();
                    if (!value.equalsIgnoreCase("Negotiate")) {
                        if (value.equalsIgnoreCase("Kerberos")) {
                        }
                    }
                    if (inNegotiateProxy) {
                        dontUseNegotiate = true;
                        this.doingNTLMp2ndStage = false;
                        proxyAuthentication = null;
                    } else {
                        inNegotiateProxy = true;
                    }
                    authhdr = new AuthenticationHeader("Proxy-Authenticate", this.responses, new HttpCallerInfo(this.url, this.http.getProxyHostUsed(), this.http.getProxyPortUsed()), dontUseNegotiate);
                    if (this.doingNTLMp2ndStage) {
                        proxyAuthentication = resetProxyAuthentication(proxyAuthentication, authhdr);
                        if (proxyAuthentication != null) {
                            break;
                        }
                        proxyHost = this.http.getProxyHostUsed();
                        proxyPort = this.http.getProxyPortUsed();
                        disconnectInternal();
                        retryTunnel++;
                    } else {
                        raw = this.responses.findValue("Proxy-Authenticate");
                        reset();
                        if (proxyAuthentication.setHeaders(this, authhdr.headerParser(), raw)) {
                            disconnectInternal();
                            throw new IOException("Authentication failure");
                        } else {
                            this.authObj = null;
                            this.doingNTLMp2ndStage = false;
                        }
                    }
                    if (retryTunnel >= maxRedirects) {
                        break;
                    }
                }
                authhdr = new AuthenticationHeader("Proxy-Authenticate", this.responses, new HttpCallerInfo(this.url, this.http.getProxyHostUsed(), this.http.getProxyPortUsed()), dontUseNegotiate);
                if (this.doingNTLMp2ndStage) {
                    raw = this.responses.findValue("Proxy-Authenticate");
                    reset();
                    if (proxyAuthentication.setHeaders(this, authhdr.headerParser(), raw)) {
                        this.authObj = null;
                        this.doingNTLMp2ndStage = false;
                    } else {
                        disconnectInternal();
                        throw new IOException("Authentication failure");
                    }
                }
                proxyAuthentication = resetProxyAuthentication(proxyAuthentication, authhdr);
                if (proxyAuthentication != null) {
                    break;
                }
                proxyHost = this.http.getProxyHostUsed();
                proxyPort = this.http.getProxyPortUsed();
                disconnectInternal();
                retryTunnel++;
                if (retryTunnel >= maxRedirects) {
                    break;
                }
            }
            if (retryTunnel >= maxRedirects || respCode != 200) {
                throw new IOException("Unable to tunnel through proxy. Proxy returns \"" + statusLine + "\"");
            }
            this.requests = savedRequests;
            this.responses.reset();
        } finally {
            if (this.proxyAuthKey != null) {
                AuthenticationInfo.endAuthRequest(this.proxyAuthKey);
            }
        }
    }

    static String connectRequestURI(URL url) {
        String host = url.getHost();
        int port = url.getPort();
        if (port == -1) {
            port = url.getDefaultPort();
        }
        return host + ":" + port;
    }

    private void sendCONNECTRequest() throws IOException {
        int port = this.url.getPort();
        this.requests.set(0, HTTP_CONNECT + " " + connectRequestURI(this.url) + " " + httpVersion, null);
        this.requests.setIfNotSet("User-Agent", userAgent);
        String host = this.url.getHost();
        if (!(port == -1 || port == this.url.getDefaultPort())) {
            host = host + ":" + String.valueOf(port);
        }
        this.requests.setIfNotSet("Host", host);
        this.requests.setIfNotSet("Accept", acceptString);
        if (this.http.getHttpKeepAliveSet()) {
            this.requests.setIfNotSet("Proxy-Connection", "keep-alive");
        }
        setPreemptiveProxyAuthentication(this.requests);
        if (logger.isLoggable(500)) {
            logger.fine(this.requests.toString());
        }
        this.http.writeRequests(this.requests, null);
    }

    private void setPreemptiveProxyAuthentication(MessageHeader requests) throws IOException {
        AuthenticationInfo pauth = AuthenticationInfo.getProxyAuth(this.http.getProxyHostUsed(), this.http.getProxyPortUsed());
        if (pauth != null && pauth.supportsPreemptiveAuthorization()) {
            String value;
            if (pauth instanceof DigestAuthentication) {
                DigestAuthentication digestProxy = (DigestAuthentication) pauth;
                if (tunnelState() == TunnelState.SETUP) {
                    value = digestProxy.getHeaderValue(connectRequestURI(this.url), HTTP_CONNECT);
                } else {
                    value = digestProxy.getHeaderValue(getRequestURI(), this.method);
                }
            } else {
                value = pauth.getHeaderValue(this.url, this.method);
            }
            requests.set(pauth.getHeaderName(), value);
            this.currentProxyCredentials = pauth;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private AuthenticationInfo getHttpProxyAuthentication(AuthenticationHeader authhdr) {
        AuthenticationInfo authenticationInfo = null;
        String raw = authhdr.raw();
        final String host = this.http.getProxyHostUsed();
        int port = this.http.getProxyPortUsed();
        if (host != null && authhdr.isPresent()) {
            AuthenticationInfo digestAuthentication;
            HeaderParser p = authhdr.headerParser();
            String realm = p.findValue("realm");
            String scheme = authhdr.scheme();
            AuthScheme authScheme = AuthScheme.UNKNOWN;
            if ("basic".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.BASIC;
            } else if ("digest".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.DIGEST;
            } else if ("ntlm".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NTLM;
                this.doingNTLMp2ndStage = true;
            } else if ("Kerberos".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.KERBEROS;
                this.doingNTLMp2ndStage = true;
            } else if ("Negotiate".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NEGOTIATE;
                this.doingNTLMp2ndStage = true;
            }
            if (realm == null) {
                realm = "";
            }
            this.proxyAuthKey = AuthenticationInfo.getProxyAuthKey(host, port, realm, authScheme);
            AuthenticationInfo ret = AuthenticationInfo.getProxyAuth(this.proxyAuthKey);
            if (ret == null) {
                PasswordAuthentication a;
                switch (-getsun-net-www-protocol-http-AuthSchemeSwitchesValues()[authScheme.ordinal()]) {
                    case 1:
                        InetAddress addr = null;
                        String finalHost = host;
                        try {
                            addr = (InetAddress) AccessController.doPrivileged(new PrivilegedExceptionAction<InetAddress>() {
                                public InetAddress run() throws UnknownHostException {
                                    return InetAddress.getByName(host);
                                }
                            });
                        } catch (PrivilegedActionException e) {
                        }
                        a = privilegedRequestPasswordAuthentication(host, addr, port, "http", realm, scheme, this.url, RequestorType.PROXY);
                        if (a != null) {
                            authenticationInfo = new BasicAuthentication(true, host, port, realm, a);
                        } else {
                            authenticationInfo = ret;
                        }
                        ret = authenticationInfo;
                        break;
                    case 2:
                        a = privilegedRequestPasswordAuthentication(host, null, port, this.url.getProtocol(), realm, scheme, this.url, RequestorType.PROXY);
                        if (a != null) {
                            digestAuthentication = new DigestAuthentication(true, host, port, realm, scheme, a, new Parameters());
                        } else {
                            authenticationInfo = ret;
                        }
                        ret = authenticationInfo;
                        break;
                    case 3:
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Kerberos"));
                        break;
                    case 4:
                        ret = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Negotiate"));
                        break;
                    case 5:
                        NTLMAuthenticationProxy nTLMAuthenticationProxy = NTLMAuthenticationProxy.proxy;
                        if (NTLMAuthenticationProxy.supported) {
                            if (this.tryTransparentNTLMProxy) {
                                nTLMAuthenticationProxy = NTLMAuthenticationProxy.proxy;
                                this.tryTransparentNTLMProxy = NTLMAuthenticationProxy.supportsTransparentAuth;
                            }
                            a = null;
                            if (this.tryTransparentNTLMProxy) {
                                logger.finest("Trying Transparent NTLM authentication");
                            } else {
                                a = privilegedRequestPasswordAuthentication(host, null, port, this.url.getProtocol(), "", scheme, this.url, RequestorType.PROXY);
                            }
                            if (this.tryTransparentNTLMProxy || !(this.tryTransparentNTLMProxy || a == null)) {
                                authenticationInfo = NTLMAuthenticationProxy.proxy.create(true, host, port, a);
                            } else {
                                authenticationInfo = ret;
                            }
                            this.tryTransparentNTLMProxy = false;
                        } else {
                            authenticationInfo = ret;
                        }
                        ret = authenticationInfo;
                        break;
                    case 6:
                        logger.finest("Unknown/Unsupported authentication scheme: " + scheme);
                        break;
                }
            }
            if (ret != null || defaultAuth == null) {
                authenticationInfo = ret;
            } else {
                if (defaultAuth.schemeSupported(scheme)) {
                    try {
                        String a2 = defaultAuth.authString(new URL("http", host, port, "/"), scheme, realm);
                        if (a2 != null) {
                            digestAuthentication = new BasicAuthentication(true, host, port, realm, a2);
                        }
                    } catch (MalformedURLException e2) {
                        authenticationInfo = ret;
                    }
                }
                authenticationInfo = ret;
            }
            if (!(authenticationInfo == null || authenticationInfo.setHeaders(this, p, raw))) {
                authenticationInfo = null;
            }
        }
        if (logger.isLoggable(400)) {
            logger.finer("Proxy Authentication for " + authhdr.toString() + " returned " + (authenticationInfo != null ? authenticationInfo.toString() : "null"));
        }
        return authenticationInfo;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private AuthenticationInfo getServerAuthentication(AuthenticationHeader authhdr) {
        AuthenticationInfo authenticationInfo = null;
        String raw = authhdr.raw();
        if (authhdr.isPresent()) {
            HeaderParser p = authhdr.headerParser();
            String realm = p.findValue("realm");
            String scheme = authhdr.scheme();
            AuthScheme authScheme = AuthScheme.UNKNOWN;
            if ("basic".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.BASIC;
            } else if ("digest".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.DIGEST;
            } else if ("ntlm".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NTLM;
                this.doingNTLM2ndStage = true;
            } else if ("Kerberos".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.KERBEROS;
                this.doingNTLM2ndStage = true;
            } else if ("Negotiate".equalsIgnoreCase(scheme)) {
                authScheme = AuthScheme.NEGOTIATE;
                this.doingNTLM2ndStage = true;
            }
            this.domain = p.findValue("domain");
            if (realm == null) {
                realm = "";
            }
            this.serverAuthKey = AuthenticationInfo.getServerAuthKey(this.url, realm, authScheme);
            AuthenticationInfo ret = AuthenticationInfo.getServerAuth(this.serverAuthKey);
            InetAddress addr = null;
            if (ret == null) {
                try {
                    addr = InetAddress.getByName(this.url.getHost());
                } catch (UnknownHostException e) {
                }
            }
            int port = this.url.getPort();
            if (port == -1) {
                port = this.url.getDefaultPort();
            }
            if (ret == null) {
                PasswordAuthentication a;
                switch (-getsun-net-www-protocol-http-AuthSchemeSwitchesValues()[authScheme.ordinal()]) {
                    case 1:
                        a = privilegedRequestPasswordAuthentication(this.url.getHost(), addr, port, this.url.getProtocol(), realm, scheme, this.url, RequestorType.SERVER);
                        if (a != null) {
                            authenticationInfo = new BasicAuthentication(false, this.url, realm, a);
                            break;
                        }
                    case 2:
                        a = privilegedRequestPasswordAuthentication(this.url.getHost(), addr, port, this.url.getProtocol(), realm, scheme, this.url, RequestorType.SERVER);
                        if (a != null) {
                            this.digestparams = new Parameters();
                            authenticationInfo = new DigestAuthentication(false, this.url, realm, scheme, a, this.digestparams);
                            break;
                        }
                    case 3:
                        authenticationInfo = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Kerberos"));
                        break;
                    case 4:
                        authenticationInfo = new NegotiateAuthentication(new HttpCallerInfo(authhdr.getHttpCallerInfo(), "Negotiate"));
                        break;
                    case 5:
                        NTLMAuthenticationProxy nTLMAuthenticationProxy = NTLMAuthenticationProxy.proxy;
                        if (NTLMAuthenticationProxy.supported) {
                            URL url1;
                            try {
                                URL url = new URL(this.url, "/");
                            } catch (Exception e2) {
                                url1 = this.url;
                            }
                            if (this.tryTransparentNTLMServer) {
                                nTLMAuthenticationProxy = NTLMAuthenticationProxy.proxy;
                                this.tryTransparentNTLMServer = NTLMAuthenticationProxy.supportsTransparentAuth;
                                if (this.tryTransparentNTLMServer) {
                                    nTLMAuthenticationProxy = NTLMAuthenticationProxy.proxy;
                                    this.tryTransparentNTLMServer = NTLMAuthenticationProxy.isTrustedSite(this.url);
                                }
                            }
                            a = null;
                            if (this.tryTransparentNTLMServer) {
                                logger.finest("Trying Transparent NTLM authentication");
                            } else {
                                a = privilegedRequestPasswordAuthentication(this.url.getHost(), addr, port, this.url.getProtocol(), "", scheme, this.url, RequestorType.SERVER);
                            }
                            if (this.tryTransparentNTLMServer || !(this.tryTransparentNTLMServer || a == null)) {
                                authenticationInfo = NTLMAuthenticationProxy.proxy.create(false, url1, a);
                            } else {
                                authenticationInfo = ret;
                            }
                            this.tryTransparentNTLMServer = false;
                            break;
                        }
                        break;
                    case 6:
                        logger.finest("Unknown/Unsupported authentication scheme: " + scheme);
                        break;
                }
            }
            authenticationInfo = ret;
            if (authenticationInfo == null && defaultAuth != null && defaultAuth.schemeSupported(scheme)) {
                String a2 = defaultAuth.authString(this.url, scheme, realm);
                if (a2 != null) {
                    authenticationInfo = new BasicAuthentication(false, this.url, realm, a2);
                }
            }
            if (!(authenticationInfo == null || authenticationInfo.setHeaders(this, p, raw))) {
                authenticationInfo = null;
            }
        }
        if (logger.isLoggable(400)) {
            logger.finer("Server Authentication for " + authhdr.toString() + " returned " + (authenticationInfo != null ? authenticationInfo.toString() : "null"));
        }
        return authenticationInfo;
    }

    private void checkResponseCredentials(boolean inClose) throws IOException {
        try {
            if (this.needToCheck) {
                String raw;
                if (validateProxy && this.currentProxyCredentials != null && (this.currentProxyCredentials instanceof DigestAuthentication)) {
                    raw = this.responses.findValue("Proxy-Authentication-Info");
                    if (inClose || raw != null) {
                        this.currentProxyCredentials.checkResponse(raw, this.method, getRequestURI());
                        this.currentProxyCredentials = null;
                    }
                }
                if (validateServer && this.currentServerCredentials != null && (this.currentServerCredentials instanceof DigestAuthentication)) {
                    raw = this.responses.findValue("Authentication-Info");
                    if (inClose || raw != null) {
                        ((DigestAuthentication) this.currentServerCredentials).checkResponse(raw, this.method, this.url);
                        this.currentServerCredentials = null;
                    }
                }
                if (this.currentServerCredentials == null && this.currentProxyCredentials == null) {
                    this.needToCheck = false;
                }
            }
        } catch (IOException e) {
            disconnectInternal();
            this.connected = false;
            throw e;
        }
    }

    String getRequestURI() throws IOException {
        if (this.requestURI == null) {
            this.requestURI = this.http.getURLFile();
        }
        return this.requestURI;
    }

    private boolean followRedirect() throws IOException {
        if (!getInstanceFollowRedirects()) {
            return false;
        }
        int stat = getResponseCode();
        if (stat < 300 || stat > 307 || stat == 306 || stat == java.net.HttpURLConnection.HTTP_NOT_MODIFIED) {
            return false;
        }
        String loc = getHeaderField("Location");
        if (loc == null) {
            return false;
        }
        Object locUrl;
        try {
            locUrl = new URL(loc);
            if (!this.url.getProtocol().equalsIgnoreCase(locUrl.getProtocol())) {
                return false;
            }
        } catch (MalformedURLException e) {
            locUrl = new URL(this.url, loc);
        }
        disconnectInternal();
        if (streaming()) {
            throw new HttpRetryException(RETRY_MSG3, stat, loc);
        }
        if (logger.isLoggable(500)) {
            logger.fine("Redirected from " + this.url + " to " + locUrl);
        }
        this.responses = new MessageHeader();
        if (stat == java.net.HttpURLConnection.HTTP_USE_PROXY) {
            String proxyHost = locUrl.getHost();
            int proxyPort = locUrl.getPort();
            SecurityManager security = System.getSecurityManager();
            if (security != null) {
                security.checkConnect(proxyHost, proxyPort);
            }
            setProxiedClient(this.url, proxyHost, proxyPort);
            this.requests.set(0, this.method + " " + getRequestURI() + " " + httpVersion, null);
            this.connected = true;
        } else {
            this.url = locUrl;
            this.requestURI = null;
            if (!this.method.equals("POST") || Boolean.getBoolean("http.strictPostRedirect") || stat == 307) {
                if (!checkReuseConnection()) {
                    connect();
                }
                if (this.http != null) {
                    this.requests.set(0, this.method + " " + getRequestURI() + " " + httpVersion, null);
                    int port = this.url.getPort();
                    String host = this.url.getHost();
                    if (!(port == -1 || port == this.url.getDefaultPort())) {
                        host = host + ":" + String.valueOf(port);
                    }
                    this.requests.set("Host", host);
                }
            } else {
                this.requests = new MessageHeader();
                this.setRequests = false;
                setRequestMethod("GET");
                this.poster = null;
                if (!checkReuseConnection()) {
                    connect();
                }
            }
        }
        return true;
    }

    private void reset() throws IOException {
        this.http.reuse = true;
        this.reuseClient = this.http;
        InputStream is = this.http.getInputStream();
        if (!this.method.equals("HEAD")) {
            try {
                if ((is instanceof ChunkedInputStream) || (is instanceof MeteredStream)) {
                    do {
                    } while (is.read(this.cdata) > 0);
                } else {
                    long cl = 0;
                    String cls = this.responses.findValue("Content-Length");
                    if (cls != null) {
                        try {
                            cl = Long.parseLong(cls);
                        } catch (NumberFormatException e) {
                            cl = 0;
                        }
                    }
                    long i = 0;
                    while (i < cl) {
                        int n = is.read(this.cdata);
                        if (n != -1) {
                            i += (long) n;
                        }
                    }
                }
                try {
                    if (is instanceof MeteredStream) {
                        is.close();
                    }
                } catch (IOException e2) {
                }
            } catch (IOException e3) {
                this.http.reuse = false;
                this.reuseClient = null;
                disconnectInternal();
                return;
            }
        }
        this.responseCode = -1;
        this.responses = new MessageHeader();
        this.connected = false;
    }

    private void disconnectWeb() throws IOException {
        if (usingProxy() && this.http.isKeepingAlive()) {
            this.responseCode = -1;
            reset();
            return;
        }
        disconnectInternal();
    }

    private void disconnectInternal() {
        this.responseCode = -1;
        this.inputStream = null;
        if (this.pi != null) {
            this.pi.finishTracking();
            this.pi = null;
        }
        if (this.http != null) {
            this.http.closeServer();
            this.http = null;
            this.connected = false;
        }
    }

    public void disconnect() {
        this.responseCode = -1;
        if (this.pi != null) {
            this.pi.finishTracking();
            this.pi = null;
        }
        if (this.http != null) {
            if (this.inputStream != null) {
                HttpClient hc = this.http;
                boolean ka = hc.isKeepingAlive();
                try {
                    this.inputStream.close();
                } catch (IOException e) {
                }
                if (ka) {
                    hc.closeIdleConnection();
                }
            } else {
                this.http.setDoNotRetry(true);
                this.http.closeServer();
            }
            this.http = null;
            this.connected = false;
        }
        this.cachedInputStream = null;
        if (this.cachedHeaders != null) {
            this.cachedHeaders.reset();
        }
    }

    public boolean usingProxy() {
        boolean z = false;
        if (this.http == null) {
            return false;
        }
        if (this.http.getProxyHostUsed() != null) {
            z = true;
        }
        return z;
    }

    private String filterHeaderField(String name, String value) {
        if (value == null) {
            return null;
        }
        if ((!SET_COOKIE.equalsIgnoreCase(name) && !SET_COOKIE2.equalsIgnoreCase(name)) || this.cookieHandler == null) {
            return value;
        }
        StringBuilder retValue = new StringBuilder();
        boolean multipleCookies = false;
        for (HttpCookie cookie : HttpCookie.parse(value, true)) {
            if (!cookie.isHttpOnly()) {
                if (multipleCookies) {
                    retValue.append(',');
                }
                retValue.append(cookie.header);
                multipleCookies = true;
            }
        }
        return retValue.length() == 0 ? "" : retValue.toString();
    }

    private Map<String, List<String>> getFilteredHeaderFields() {
        if (this.filteredHeaders != null) {
            return this.filteredHeaders;
        }
        Map<String, List<String>> tmpMap = new HashMap();
        Map<String, List<String>> headers;
        if (this.cachedHeaders != null) {
            headers = this.cachedHeaders.getHeaders();
        } else {
            headers = this.responses.getHeaders();
        }
        for (Entry<String, List<String>> e : headers.entrySet()) {
            String key = (String) e.getKey();
            List<String> values = (List) e.getValue();
            List<String> filteredVals = new ArrayList();
            for (String value : values) {
                String fVal = filterHeaderField(key, value);
                if (fVal != null) {
                    filteredVals.add(fVal);
                }
            }
            if (!filteredVals.isEmpty()) {
                tmpMap.put(key, Collections.unmodifiableList(filteredVals));
            }
        }
        Map<String, List<String>> unmodifiableMap = Collections.unmodifiableMap(tmpMap);
        this.filteredHeaders = unmodifiableMap;
        return unmodifiableMap;
    }

    public String getHeaderField(String name) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (this.cachedHeaders != null) {
            return filterHeaderField(name, this.cachedHeaders.findValue(name));
        }
        return filterHeaderField(name, this.responses.findValue(name));
    }

    public Map<String, List<String>> getHeaderFields() {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        return getFilteredHeaderFields();
    }

    public String getHeaderField(int n) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (this.cachedHeaders != null) {
            return filterHeaderField(this.cachedHeaders.getKey(n), this.cachedHeaders.getValue(n));
        }
        return filterHeaderField(this.responses.getKey(n), this.responses.getValue(n));
    }

    public String getHeaderFieldKey(int n) {
        try {
            getInputStream();
        } catch (IOException e) {
        }
        if (this.cachedHeaders != null) {
            return this.cachedHeaders.getKey(n);
        }
        return this.responses.getKey(n);
    }

    public void setRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key is null");
        } else if (isExternalMessageHeaderAllowed(key, value)) {
            this.requests.set(key, value);
        }
    }

    public void addRequestProperty(String key, String value) {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (key == null) {
            throw new NullPointerException("key is null");
        } else if (isExternalMessageHeaderAllowed(key, value)) {
            this.requests.add(key, value);
        }
    }

    public void setAuthenticationProperty(String key, String value) {
        checkMessageHeader(key, value);
        this.requests.set(key, value);
    }

    public synchronized String getRequestProperty(String key) {
        if (key == null) {
            return null;
        }
        for (String equalsIgnoreCase : EXCLUDE_HEADERS) {
            if (key.equalsIgnoreCase(equalsIgnoreCase)) {
                return null;
            }
        }
        if (!this.setUserCookies) {
            if (key.equalsIgnoreCase("Cookie")) {
                return this.userCookies;
            } else if (key.equalsIgnoreCase("Cookie2")) {
                return this.userCookies2;
            }
        }
        return this.requests.findValue(key);
    }

    public synchronized Map<String, List<String>> getRequestProperties() {
        if (this.connected) {
            throw new IllegalStateException("Already connected");
        } else if (this.setUserCookies) {
            return this.requests.getHeaders(EXCLUDE_HEADERS);
        } else {
            Map map = null;
            if (!(this.userCookies == null && this.userCookies2 == null)) {
                map = new HashMap();
                if (this.userCookies != null) {
                    map.put("Cookie", this.userCookies);
                }
                if (this.userCookies2 != null) {
                    map.put("Cookie2", this.userCookies2);
                }
            }
            return this.requests.filterAndAddHeaders(EXCLUDE_HEADERS2, map);
        }
    }

    public void setConnectTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.connectTimeout = timeout;
    }

    public int getConnectTimeout() {
        return this.connectTimeout < 0 ? 0 : this.connectTimeout;
    }

    public void setReadTimeout(int timeout) {
        if (timeout < 0) {
            throw new IllegalArgumentException("timeouts can't be negative");
        }
        this.readTimeout = timeout;
    }

    public int getReadTimeout() {
        return this.readTimeout < 0 ? 0 : this.readTimeout;
    }

    public CookieHandler getCookieHandler() {
        return this.cookieHandler;
    }

    String getMethod() {
        return this.method;
    }

    private MessageHeader mapToMessageHeader(Map<String, List<String>> map) {
        MessageHeader headers = new MessageHeader();
        if (map == null || map.isEmpty()) {
            return headers;
        }
        for (Entry<String, List<String>> entry : map.entrySet()) {
            String key = (String) entry.getKey();
            for (String value : (List) entry.getValue()) {
                if (key == null) {
                    headers.prepend(key, value);
                } else {
                    headers.add(key, value);
                }
            }
        }
        return headers;
    }
}
