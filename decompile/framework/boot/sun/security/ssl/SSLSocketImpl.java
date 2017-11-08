package sun.security.ssl;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.AlgorithmConstraints;
import java.security.GeneralSecurityException;
import java.security.PrivilegedAction;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.jar.Pack200.Packer;
import javax.crypto.BadPaddingException;
import javax.net.ssl.HandshakeCompletedEvent;
import javax.net.ssl.HandshakeCompletedListener;
import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLProtocolException;
import javax.net.ssl.SSLSession;

public final class SSLSocketImpl extends BaseSSLSocketImpl {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final int cs_APP_CLOSED = 7;
    private static final int cs_CLOSED = 6;
    private static final int cs_DATA = 2;
    private static final int cs_ERROR = 4;
    private static final int cs_HANDSHAKE = 1;
    private static final int cs_RENEGOTIATE = 3;
    private static final int cs_SENT_CLOSE = 5;
    private static final int cs_START = 0;
    private static final Debug debug = Debug.getInstance("ssl");
    private AccessControlContext acc;
    private AlgorithmConstraints algorithmConstraints = null;
    private boolean autoClose = true;
    private byte[] clientVerifyData;
    private SSLException closeReason;
    private int connectionState;
    private byte doClientAuth;
    private boolean enableSessionCreation = true;
    private CipherSuiteList enabledCipherSuites;
    private ProtocolList enabledProtocols;
    private boolean expectingFinished;
    private HashMap<HandshakeCompletedListener, AccessControlContext> handshakeListeners;
    private final Object handshakeLock = new Object();
    private volatile SSLSessionImpl handshakeSession;
    private Handshaker handshaker;
    private ByteArrayOutputStream heldRecordBuffer = null;
    private String host;
    private String identificationProtocol = null;
    private AppInputStream input;
    private InputRecord inrec;
    private boolean isFirstAppOutputRecord = true;
    private AppOutputStream output;
    private ProtocolVersion protocolVersion = ProtocolVersion.DEFAULT;
    private String rawHostname;
    private CipherBox readCipher;
    private final Object readLock = new Object();
    private MAC readMAC;
    private boolean roleIsServer;
    private boolean secureRenegotiation;
    private byte[] serverVerifyData;
    private SSLSessionImpl sess;
    private InputStream sockInput;
    private OutputStream sockOutput;
    private SSLContextImpl sslContext;
    private CipherBox writeCipher;
    final ReentrantLock writeLock = new ReentrantLock();
    private MAC writeMAC;

    private static class NotifyHandshakeThread extends Thread {
        private HandshakeCompletedEvent event;
        private Set<Entry<HandshakeCompletedListener, AccessControlContext>> targets;

        NotifyHandshakeThread(Set<Entry<HandshakeCompletedListener, AccessControlContext>> entrySet, HandshakeCompletedEvent e) {
            super("HandshakeCompletedNotify-Thread");
            this.targets = new HashSet((Collection) entrySet);
            this.event = e;
        }

        public void run() {
            for (Entry<HandshakeCompletedListener, AccessControlContext> entry : this.targets) {
                final HandshakeCompletedListener l = (HandshakeCompletedListener) entry.getKey();
                AccessController.doPrivileged(new PrivilegedAction<Void>() {
                    public Void run() {
                        l.handshakeCompleted(NotifyHandshakeThread.this.event);
                        return null;
                    }
                }, (AccessControlContext) entry.getValue());
            }
        }
    }

    public /* bridge */ /* synthetic */ void bind(SocketAddress bindpoint) {
        super.bind(bindpoint);
    }

    public /* bridge */ /* synthetic */ SocketAddress getLocalSocketAddress() {
        return super.getLocalSocketAddress();
    }

    public /* bridge */ /* synthetic */ SocketAddress getRemoteSocketAddress() {
        return super.getRemoteSocketAddress();
    }

    public /* bridge */ /* synthetic */ void setPerformancePreferences(int connectionTime, int latency, int bandwidth) {
        super.setPerformancePreferences(connectionTime, latency, bandwidth);
    }

    static {
        boolean z;
        if (SSLSocketImpl.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    SSLSocketImpl(SSLContextImpl context, String host, int port) throws IOException, UnknownHostException {
        SocketAddress socketAddress;
        this.host = host;
        this.rawHostname = host;
        init(context, false);
        if (host != null) {
            socketAddress = new InetSocketAddress(host, port);
        } else {
            socketAddress = new InetSocketAddress(InetAddress.getByName(null), port);
        }
        connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl context, InetAddress host, int port) throws IOException {
        init(context, false);
        connect(new InetSocketAddress(host, port), 0);
    }

    SSLSocketImpl(SSLContextImpl context, String host, int port, InetAddress localAddr, int localPort) throws IOException, UnknownHostException {
        SocketAddress socketAddress;
        this.host = host;
        this.rawHostname = host;
        init(context, false);
        bind(new InetSocketAddress(localAddr, localPort));
        if (host != null) {
            socketAddress = new InetSocketAddress(host, port);
        } else {
            socketAddress = new InetSocketAddress(InetAddress.getByName(null), port);
        }
        connect(socketAddress, 0);
    }

    SSLSocketImpl(SSLContextImpl context, InetAddress host, int port, InetAddress localAddr, int localPort) throws IOException {
        init(context, false);
        bind(new InetSocketAddress(localAddr, localPort));
        connect(new InetSocketAddress(host, port), 0);
    }

    SSLSocketImpl(SSLContextImpl context, boolean serverMode, CipherSuiteList suites, byte clientAuth, boolean sessionCreation, ProtocolList protocols, String identificationProtocol, AlgorithmConstraints algorithmConstraints) throws IOException {
        this.doClientAuth = clientAuth;
        this.enableSessionCreation = sessionCreation;
        this.identificationProtocol = identificationProtocol;
        this.algorithmConstraints = algorithmConstraints;
        init(context, serverMode);
        this.enabledCipherSuites = suites;
        this.enabledProtocols = protocols;
    }

    SSLSocketImpl(SSLContextImpl context) {
        init(context, false);
    }

    SSLSocketImpl(SSLContextImpl context, Socket sock, String host, int port, boolean autoClose) throws IOException {
        super(sock);
        if (sock.isConnected()) {
            this.host = host;
            this.rawHostname = host;
            init(context, false);
            this.autoClose = autoClose;
            doneConnect();
            return;
        }
        throw new SocketException("Underlying socket is not connected");
    }

    private void init(SSLContextImpl context, boolean isServer) {
        this.sslContext = context;
        this.sess = SSLSessionImpl.nullSession;
        this.handshakeSession = null;
        this.roleIsServer = isServer;
        this.connectionState = 0;
        this.readCipher = CipherBox.NULL;
        this.readMAC = MAC.NULL;
        this.writeCipher = CipherBox.NULL;
        this.writeMAC = MAC.NULL;
        this.secureRenegotiation = false;
        this.clientVerifyData = new byte[0];
        this.serverVerifyData = new byte[0];
        this.enabledCipherSuites = this.sslContext.getDefaultCipherSuiteList(this.roleIsServer);
        this.enabledProtocols = this.sslContext.getDefaultProtocolList(this.roleIsServer);
        this.inrec = null;
        this.acc = AccessController.getContext();
        this.input = new AppInputStream(this);
        this.output = new AppOutputStream(this);
    }

    public void connect(SocketAddress endpoint, int timeout) throws IOException {
        if (this.self != this) {
            throw new SocketException("Already connected");
        } else if (endpoint instanceof InetSocketAddress) {
            super.connect(endpoint, timeout);
            doneConnect();
        } else {
            throw new SocketException("Cannot handle non-Inet socket addresses.");
        }
    }

    void doneConnect() throws IOException {
        if (this.self == this) {
            this.sockInput = super.getInputStream();
            this.sockOutput = super.getOutputStream();
        } else {
            this.sockInput = this.self.getInputStream();
            this.sockOutput = this.self.getOutputStream();
        }
        initHandshaker();
    }

    private synchronized int getConnectionState() {
        return this.connectionState;
    }

    private synchronized void setConnectionState(int state) {
        this.connectionState = state;
    }

    AccessControlContext getAcc() {
        return this.acc;
    }

    void writeRecord(OutputRecord r) throws IOException {
        writeRecord(r, false);
    }

    void writeRecord(OutputRecord r, boolean holdRecord) throws IOException {
        while (r.contentType() == (byte) 23) {
            switch (getConnectionState()) {
                case 1:
                    performInitialHandshake();
                    continue;
                case 2:
                case 3:
                    break;
                case 4:
                    fatal((byte) 0, "error while writing to socket");
                    continue;
                case 5:
                case 6:
                case 7:
                    if (this.closeReason != null) {
                        throw this.closeReason;
                    }
                    throw new SocketException("Socket closed");
                default:
                    throw new SSLProtocolException("State error, send app data");
            }
            if (r.isEmpty()) {
            }
            if (r.isAlert((byte) 0) || getSoLinger() < 0) {
                this.writeLock.lock();
                try {
                    writeRecordInternal(r, holdRecord);
                } finally {
                    this.writeLock.unlock();
                }
            } else {
                boolean interrupted = Thread.interrupted();
                try {
                    if (this.writeLock.tryLock((long) getSoLinger(), TimeUnit.SECONDS)) {
                        writeRecordInternal(r, holdRecord);
                        this.writeLock.unlock();
                    } else {
                        Throwable ssle = new SSLException("SO_LINGER timeout, close_notify message cannot be sent.");
                        if (this.self != this && !this.autoClose) {
                            fatal((byte) -1, ssle);
                        } else if (debug != null && Debug.isOn("ssl")) {
                            System.out.println(threadName() + ", received Exception: " + ssle);
                        }
                        this.sess.invalidate();
                    }
                } catch (InterruptedException e) {
                    interrupted = true;
                } catch (Throwable th) {
                    this.writeLock.unlock();
                }
                if (interrupted) {
                    Thread.currentThread().interrupt();
                    return;
                }
                return;
            }
        }
        if (r.isEmpty()) {
            if (r.isAlert((byte) 0)) {
            }
            this.writeLock.lock();
            writeRecordInternal(r, holdRecord);
        }
    }

    private void writeRecordInternal(OutputRecord r, boolean holdRecord) throws IOException {
        r.addMAC(this.writeMAC);
        r.encrypt(this.writeCipher);
        if (holdRecord) {
            if (getTcpNoDelay()) {
                holdRecord = false;
            } else if (this.heldRecordBuffer == null) {
                this.heldRecordBuffer = new ByteArrayOutputStream(40);
            }
        }
        r.write(this.sockOutput, holdRecord, this.heldRecordBuffer);
        if (this.connectionState < 4) {
            checkSequenceNumber(this.writeMAC, r.contentType());
        }
        if (this.isFirstAppOutputRecord && r.contentType() == (byte) 23) {
            this.isFirstAppOutputRecord = false;
        }
    }

    boolean needToSplitPayload() {
        this.writeLock.lock();
        try {
            boolean z;
            if (this.protocolVersion.v > ProtocolVersion.TLS10.v || !this.writeCipher.isCBCMode() || this.isFirstAppOutputRecord) {
                z = false;
            } else {
                z = Record.enableCBCProtection;
            }
            this.writeLock.unlock();
            return z;
        } catch (Throwable th) {
            this.writeLock.unlock();
        }
    }

    void readDataRecord(InputRecord r) throws IOException {
        if (getConnectionState() == 1) {
            performInitialHandshake();
        }
        readRecord(r, true);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void readRecord(InputRecord r, boolean needAppData) throws IOException {
        boolean handshaking;
        synchronized (this.readLock) {
            while (true) {
                int state = getConnectionState();
                if (state == 6 || state == 4 || state == 7) {
                    r.close();
                    return;
                }
                try {
                    r.setAppDataValid(false);
                    r.read(this.sockInput, this.sockOutput);
                    try {
                        r.decrypt(this.readMAC, this.readCipher);
                    } catch (BadPaddingException e) {
                        byte alertType;
                        if (r.contentType() == (byte) 22) {
                            alertType = (byte) 40;
                        } else {
                            alertType = (byte) 20;
                        }
                        fatal(alertType, e.getMessage(), e);
                    }
                    synchronized (this) {
                        switch (r.contentType()) {
                            case (byte) 20:
                                if (this.connectionState == 1 || this.connectionState == 3) {
                                    if (r.available() == 1) {
                                        if (r.read() != 1) {
                                        }
                                        changeReadCiphers();
                                        this.expectingFinished = true;
                                        break;
                                    }
                                }
                                fatal((byte) 10, "illegal change cipher spec msg, state = " + this.connectionState);
                                changeReadCiphers();
                                this.expectingFinished = true;
                            case (byte) 21:
                                recvAlert(r);
                                break;
                            case (byte) 22:
                                initHandshaker();
                                if (!this.handshaker.activated()) {
                                    if (this.connectionState == 3) {
                                        this.handshaker.activate(this.protocolVersion);
                                    } else {
                                        this.handshaker.activate(null);
                                    }
                                }
                                this.handshaker.process_record(r, this.expectingFinished);
                                this.expectingFinished = false;
                                if (this.handshaker.invalidated) {
                                    this.handshaker = null;
                                    if (this.connectionState == 3) {
                                        this.connectionState = 2;
                                    }
                                } else if (this.handshaker.isDone()) {
                                    this.secureRenegotiation = this.handshaker.isSecureRenegotiation();
                                    this.clientVerifyData = this.handshaker.getClientVerifyData();
                                    this.serverVerifyData = this.handshaker.getServerVerifyData();
                                    this.sess = this.handshaker.getSession();
                                    this.handshakeSession = null;
                                    this.handshaker = null;
                                    this.connectionState = 2;
                                    if (this.handshakeListeners != null) {
                                        new NotifyHandshakeThread(this.handshakeListeners.entrySet(), new HandshakeCompletedEvent(this, this.sess)).start();
                                    }
                                }
                                if (!needAppData && this.connectionState == 2) {
                                    break;
                                }
                                break;
                                break;
                            case SecureRandom.DEFAULT_SDK_TARGET_FOR_CRYPTO_PROVIDER_WORKAROUND /*23*/:
                                if (this.connectionState == 2 || this.connectionState == 3 || this.connectionState == 5) {
                                    if (!this.expectingFinished) {
                                        if (needAppData) {
                                            r.setAppDataValid(true);
                                            break;
                                        }
                                        throw new SSLException("Discarding app data");
                                    }
                                    throw new SSLProtocolException("Expecting finished message, received data");
                                }
                                throw new SSLProtocolException("Data received in non-data state: " + this.connectionState);
                                break;
                            default:
                                if (debug != null && Debug.isOn("ssl")) {
                                    System.out.println(threadName() + ", Received record type: " + r.contentType());
                                }
                                break;
                        }
                        if (this.connectionState < 4) {
                            checkSequenceNumber(this.readMAC, r.contentType());
                        }
                        return;
                    }
                } catch (Throwable e2) {
                    try {
                        fatal((byte) 10, e2);
                    } catch (IOException e3) {
                    }
                    throw e2;
                } catch (EOFException eof) {
                    handshaking = getConnectionState() <= 1;
                    r8 = !requireCloseNotify ? handshaking : true;
                    if (debug != null && Debug.isOn("ssl")) {
                        boolean z;
                        System.out.println(threadName() + ", received EOFException: " + (z ? Packer.ERROR : "ignored"));
                    }
                    if (z) {
                        SSLException e4;
                        if (handshaking) {
                            e4 = new SSLHandshakeException("Remote host closed connection during handshake");
                        } else {
                            e4 = new SSLProtocolException("Remote host closed connection incorrectly");
                        }
                        e4.initCause(eof);
                        throw e4;
                    }
                    closeInternal(false);
                }
            }
        }
    }

    private void checkSequenceNumber(MAC mac, byte type) throws IOException {
        if (this.connectionState < 4 && mac != MAC.NULL) {
            if (mac.seqNumOverflow()) {
                if (debug != null && Debug.isOn("ssl")) {
                    System.out.println(threadName() + ", sequence number extremely close to overflow " + "(2^64-1 packets). Closing connection.");
                }
                fatal((byte) 40, "sequence number overflow");
            }
            if (type != (byte) 22 && mac.seqNumIsHuge()) {
                if (debug != null && Debug.isOn("ssl")) {
                    System.out.println(threadName() + ", request renegotiation " + "to avoid sequence number overflow");
                }
                startHandshake();
            }
        }
    }

    AppInputStream getAppInputStream() {
        return this.input;
    }

    AppOutputStream getAppOutputStream() {
        return this.output;
    }

    private void initHandshaker() {
        boolean z = true;
        switch (this.connectionState) {
            case 0:
            case 2:
                if (this.connectionState == 0) {
                    this.connectionState = 1;
                } else {
                    this.connectionState = 3;
                }
                SSLContextImpl sSLContextImpl;
                ProtocolList protocolList;
                if (this.roleIsServer) {
                    sSLContextImpl = this.sslContext;
                    protocolList = this.enabledProtocols;
                    byte b = this.doClientAuth;
                    ProtocolVersion protocolVersion = this.protocolVersion;
                    if (this.connectionState != 1) {
                        z = false;
                    }
                    this.handshaker = new ServerHandshaker(this, sSLContextImpl, protocolList, b, protocolVersion, z, this.secureRenegotiation, this.clientVerifyData, this.serverVerifyData);
                } else {
                    boolean z2;
                    sSLContextImpl = this.sslContext;
                    protocolList = this.enabledProtocols;
                    ProtocolVersion protocolVersion2 = this.protocolVersion;
                    if (this.connectionState == 1) {
                        z2 = true;
                    } else {
                        z2 = false;
                    }
                    this.handshaker = new ClientHandshaker(this, sSLContextImpl, protocolList, protocolVersion2, z2, this.secureRenegotiation, this.clientVerifyData, this.serverVerifyData);
                }
                this.handshaker.setEnabledCipherSuites(this.enabledCipherSuites);
                this.handshaker.setEnableSessionCreation(this.enableSessionCreation);
                return;
            case 1:
            case 3:
                return;
            default:
                throw new IllegalStateException("Internal error");
        }
    }

    private void performInitialHandshake() throws IOException {
        synchronized (this.handshakeLock) {
            if (getConnectionState() == 1) {
                kickstartHandshake();
                if (this.inrec == null) {
                    this.inrec = new InputRecord();
                    this.inrec.setHandshakeHash(this.input.r.getHandshakeHash());
                    this.inrec.setHelloVersion(this.input.r.getHelloVersion());
                    this.inrec.enableFormatChecks();
                }
                readRecord(this.inrec, false);
                this.inrec = null;
            }
        }
    }

    public void startHandshake() throws IOException {
        startHandshake(true);
    }

    private void startHandshake(boolean resumable) throws IOException {
        checkWrite();
        try {
            if (getConnectionState() == 1) {
                performInitialHandshake();
            } else {
                kickstartHandshake();
            }
        } catch (Exception e) {
            handleException(e, resumable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void kickstartHandshake() throws IOException {
        switch (this.connectionState) {
            case 0:
                throw new SocketException("handshaking attempted on unconnected socket");
            case 1:
                break;
            case 2:
                if (this.secureRenegotiation || Handshaker.allowUnsafeRenegotiation) {
                    if (!(this.secureRenegotiation || debug == null || !Debug.isOn("handshake"))) {
                        System.out.println("Warning: Using insecure renegotiation");
                    }
                    initHandshaker();
                    break;
                }
                throw new SSLHandshakeException("Insecure renegotiation is not allowed");
            case 3:
                return;
            default:
                throw new SocketException("connection is closed");
        }
        if (!this.handshaker.activated()) {
            if (this.connectionState == 3) {
                this.handshaker.activate(this.protocolVersion);
            } else {
                this.handshaker.activate(null);
            }
            if (this.handshaker instanceof ClientHandshaker) {
                this.handshaker.kickstart();
            } else if (this.connectionState != 1) {
                this.handshaker.kickstart();
                this.handshaker.handshakeHash.reset();
            }
        }
    }

    public boolean isClosed() {
        return getConnectionState() == 7;
    }

    boolean checkEOF() throws IOException {
        switch (getConnectionState()) {
            case 0:
                throw new SocketException("Socket is not connected");
            case 1:
            case 2:
            case 3:
            case 5:
                return false;
            case 7:
                throw new SocketException("Socket is closed");
            default:
                if (this.closeReason == null) {
                    return true;
                }
                IOException e = new SSLException("Connection has been shutdown: " + this.closeReason);
                e.initCause(this.closeReason);
                throw e;
        }
    }

    void checkWrite() throws IOException {
        if (checkEOF() || getConnectionState() == 5) {
            throw new SocketException("Connection closed by remote host");
        }
    }

    protected void closeSocket() throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeSocket()");
        }
        if (this.self == this) {
            super.close();
        } else {
            this.self.close();
        }
    }

    private void closeSocket(boolean selfInitiated) throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeSocket(selfInitiated)");
        }
        if (this.self == this) {
            super.close();
        } else if (this.autoClose) {
            this.self.close();
        } else if (selfInitiated) {
            waitForClose(false);
        }
    }

    public void close() throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called close()");
        }
        closeInternal(true);
        setConnectionState(7);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void closeInternal(boolean selfInitiated) throws IOException {
        int i = 7;
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", called closeInternal(" + selfInitiated + ")");
        }
        boolean closeSocketCalled = false;
        Throwable th = null;
        switch (getConnectionState()) {
            case 0:
                closeSocket(selfInitiated);
                break;
            case 4:
                closeSocket();
                break;
            case 6:
            case 7:
                break;
            default:
                try {
                    synchronized (this) {
                        int state = getConnectionState();
                        if (state != 6 && state != 4 && state != 7) {
                            if (state != 5) {
                                try {
                                    warning((byte) 0);
                                    this.connectionState = 5;
                                } catch (Throwable th2) {
                                    this.connectionState = 4;
                                    th = th2;
                                    closeSocketCalled = true;
                                    closeSocket(selfInitiated);
                                }
                            }
                            break;
                        }
                        break;
                    }
                } catch (Throwable th3) {
                    synchronized (this) {
                        if (this.connectionState != 7) {
                            i = 6;
                        }
                        this.connectionState = i;
                        notifyAll();
                        if (closeSocketCalled) {
                            disposeCiphers();
                        }
                        if (th != null) {
                            if (th instanceof Error) {
                                Error error = (Error) th;
                            } else if (th instanceof RuntimeException) {
                                RuntimeException runtimeException = (RuntimeException) th;
                            }
                        }
                    }
                }
                break;
        }
        synchronized (this) {
            if (this.connectionState != 7) {
                i = 6;
            }
            this.connectionState = i;
            notifyAll();
        }
        if (closeSocketCalled) {
            disposeCiphers();
        }
        if (th != null) {
            if (th instanceof Error) {
                throw ((Error) th);
            } else if (th instanceof RuntimeException) {
                throw ((RuntimeException) th);
            }
        }
    }

    void waitForClose(boolean rethrow) throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", waiting for close_notify or alert: state " + getConnectionState());
        }
        while (true) {
            try {
                int state = getConnectionState();
                if (state != 6 && state != 4 && state != 7) {
                    if (this.inrec == null) {
                        this.inrec = new InputRecord();
                    }
                    try {
                        readRecord(this.inrec, true);
                    } catch (SocketTimeoutException e) {
                    }
                }
            } catch (Object e2) {
                if (debug != null && Debug.isOn("ssl")) {
                    System.out.println(threadName() + ", Exception while waiting for close " + e2);
                }
                if (rethrow) {
                    throw e2;
                }
                return;
            }
        }
        this.inrec = null;
    }

    private void disposeCiphers() {
        synchronized (this.readLock) {
            this.readCipher.dispose();
        }
        this.writeLock.lock();
        try {
            this.writeCipher.dispose();
        } finally {
            this.writeLock.unlock();
        }
    }

    void handleException(Exception e) throws IOException {
        handleException(e, true);
    }

    private synchronized void handleException(Exception e, boolean resumable) throws IOException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", handling exception: " + e.toString());
        }
        if ((e instanceof InterruptedIOException) && resumable) {
            throw ((IOException) e);
        } else if (this.closeReason == null) {
            boolean isSSLException = e instanceof SSLException;
            if (isSSLException || !(e instanceof IOException)) {
                byte alertType;
                if (!isSSLException) {
                    alertType = (byte) 80;
                } else if (e instanceof SSLHandshakeException) {
                    alertType = (byte) 40;
                } else {
                    alertType = (byte) 10;
                }
                fatal(alertType, (Throwable) e);
            } else {
                try {
                    fatal((byte) 10, (Throwable) e);
                } catch (IOException e2) {
                }
                throw ((IOException) e);
            }
        } else if (e instanceof IOException) {
            throw ((IOException) e);
        } else {
            throw Alerts.getSSLException((byte) 80, e, "Unexpected exception");
        }
    }

    void warning(byte description) {
        sendAlert((byte) 1, description);
    }

    synchronized void fatal(byte description, String diagnostic) throws IOException {
        fatal(description, diagnostic, null);
    }

    synchronized void fatal(byte description, Throwable cause) throws IOException {
        fatal(description, null, cause);
    }

    synchronized void fatal(byte description, String diagnostic, Throwable cause) throws IOException {
        int i = 7;
        synchronized (this) {
            if (!(this.input == null || this.input.r == null)) {
                this.input.r.close();
            }
            this.sess.invalidate();
            if (this.handshakeSession != null) {
                this.handshakeSession.invalidate();
            }
            int oldState = this.connectionState;
            if (this.connectionState < 4) {
                this.connectionState = 4;
            }
            if (this.closeReason == null) {
                if (oldState == 1) {
                    this.sockInput.skip((long) this.sockInput.available());
                }
                if (description != (byte) -1) {
                    sendAlert((byte) 2, description);
                }
                if (cause instanceof SSLException) {
                    this.closeReason = (SSLException) cause;
                } else {
                    this.closeReason = Alerts.getSSLException(description, cause, diagnostic);
                }
            }
            closeSocket();
            if (this.connectionState < 6) {
                if (oldState != 7) {
                    i = 6;
                }
                this.connectionState = i;
                this.readCipher.dispose();
                this.writeCipher.dispose();
            }
            throw this.closeReason;
        }
    }

    private void recvAlert(InputRecord r) throws IOException {
        byte level = (byte) r.read();
        byte description = (byte) r.read();
        if (description == (byte) -1) {
            fatal((byte) 47, "Short alert message");
        }
        if (debug != null && (Debug.isOn("record") || Debug.isOn("handshake"))) {
            synchronized (System.out) {
                System.out.print(threadName());
                System.out.print(", RECV " + this.protocolVersion + " ALERT:  ");
                if (level == (byte) 2) {
                    System.out.print("fatal, ");
                } else if (level == (byte) 1) {
                    System.out.print("warning, ");
                } else {
                    System.out.print("<level " + (level & 255) + ">, ");
                }
                System.out.println(Alerts.alertDescription(description));
            }
        }
        if (level != (byte) 1) {
            String reason = "Received fatal alert: " + Alerts.alertDescription(description);
            if (this.closeReason == null) {
                this.closeReason = Alerts.getSSLException(description, reason);
            }
            fatal((byte) 10, reason);
        } else if (description == (byte) 0) {
            if (this.connectionState == 1) {
                fatal((byte) 10, "Received close_notify during handshake");
            } else {
                closeInternal(false);
            }
        } else if (this.handshaker != null) {
            this.handshaker.handshakeAlert(description);
        }
    }

    private void sendAlert(byte level, byte description) {
        if (this.connectionState < 5) {
            if (this.connectionState != 1 || (this.handshaker != null && this.handshaker.started())) {
                OutputRecord r = new OutputRecord((byte) 21);
                r.setVersion(this.protocolVersion);
                boolean useDebug = debug != null ? Debug.isOn("ssl") : false;
                if (useDebug) {
                    synchronized (System.out) {
                        System.out.print(threadName());
                        System.out.print(", SEND " + this.protocolVersion + " ALERT:  ");
                        if (level == (byte) 2) {
                            System.out.print("fatal, ");
                        } else if (level == (byte) 1) {
                            System.out.print("warning, ");
                        } else {
                            System.out.print("<level = " + (level & 255) + ">, ");
                        }
                        System.out.println("description = " + Alerts.alertDescription(description));
                    }
                }
                r.write(level);
                r.write(description);
                try {
                    writeRecord(r);
                } catch (Object e) {
                    if (useDebug) {
                        System.out.println(threadName() + ", Exception sending alert: " + e);
                    }
                }
            }
        }
    }

    private void changeReadCiphers() throws SSLException {
        if (this.connectionState == 1 || this.connectionState == 3) {
            CipherBox oldCipher = this.readCipher;
            try {
                this.readCipher = this.handshaker.newReadCipher();
                this.readMAC = this.handshaker.newReadMAC();
                oldCipher.dispose();
                return;
            } catch (GeneralSecurityException e) {
                throw ((SSLException) new SSLException("Algorithm missing:  ").initCause(e));
            }
        }
        throw new SSLProtocolException("State error, change cipher specs");
    }

    void changeWriteCiphers() throws SSLException {
        if (this.connectionState == 1 || this.connectionState == 3) {
            CipherBox oldCipher = this.writeCipher;
            try {
                this.writeCipher = this.handshaker.newWriteCipher();
                this.writeMAC = this.handshaker.newWriteMAC();
                oldCipher.dispose();
                this.isFirstAppOutputRecord = true;
                return;
            } catch (GeneralSecurityException e) {
                throw ((SSLException) new SSLException("Algorithm missing:  ").initCause(e));
            }
        }
        throw new SSLProtocolException("State error, change cipher specs");
    }

    synchronized void setVersion(ProtocolVersion protocolVersion) {
        this.protocolVersion = protocolVersion;
        this.output.r.setVersion(protocolVersion);
    }

    synchronized String getHost() {
        if (this.host == null || this.host.length() == 0) {
            this.host = getInetAddress().getHostName();
        }
        return this.host;
    }

    synchronized String getRawHostname() {
        return this.rawHostname;
    }

    public synchronized void setHost(String host) {
        this.host = host;
        this.rawHostname = host;
    }

    public synchronized InputStream getInputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (this.connectionState == 0) {
            throw new SocketException("Socket is not connected");
        }
        return this.input;
    }

    public synchronized OutputStream getOutputStream() throws IOException {
        if (isClosed()) {
            throw new SocketException("Socket is closed");
        } else if (this.connectionState == 0) {
            throw new SocketException("Socket is not connected");
        }
        return this.output;
    }

    public SSLSession getSession() {
        SSLSession sSLSession;
        if (getConnectionState() == 1) {
            try {
                startHandshake(false);
            } catch (Object e) {
                if (debug != null && Debug.isOn("handshake")) {
                    System.out.println(threadName() + ", IOException in getSession():  " + e);
                }
            }
        }
        synchronized (this) {
            sSLSession = this.sess;
        }
        return sSLSession;
    }

    public synchronized SSLSession getHandshakeSession() {
        return this.handshakeSession;
    }

    synchronized void setHandshakeSession(SSLSessionImpl session) {
        this.handshakeSession = session;
    }

    public synchronized void setEnableSessionCreation(boolean flag) {
        this.enableSessionCreation = flag;
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnableSessionCreation(this.enableSessionCreation);
        }
    }

    public synchronized boolean getEnableSessionCreation() {
        return this.enableSessionCreation;
    }

    public synchronized void setNeedClientAuth(boolean flag) {
        this.doClientAuth = flag ? (byte) 2 : (byte) 0;
        if (!(this.handshaker == null || !(this.handshaker instanceof ServerHandshaker) || this.handshaker.activated())) {
            ((ServerHandshaker) this.handshaker).setClientAuth(this.doClientAuth);
        }
    }

    public synchronized boolean getNeedClientAuth() {
        return this.doClientAuth == (byte) 2;
    }

    public synchronized void setWantClientAuth(boolean flag) {
        this.doClientAuth = flag ? (byte) 1 : (byte) 0;
        if (!(this.handshaker == null || !(this.handshaker instanceof ServerHandshaker) || this.handshaker.activated())) {
            ((ServerHandshaker) this.handshaker).setClientAuth(this.doClientAuth);
        }
    }

    public synchronized boolean getWantClientAuth() {
        boolean z = true;
        synchronized (this) {
            if (this.doClientAuth != (byte) 1) {
                z = false;
            }
        }
        return z;
    }

    public synchronized void setUseClientMode(boolean flag) {
        boolean z = false;
        synchronized (this) {
            boolean z2;
            SSLContextImpl sSLContextImpl;
            switch (this.connectionState) {
                case 0:
                    boolean z3 = this.roleIsServer;
                    if (flag) {
                        z2 = false;
                    } else {
                        z2 = true;
                    }
                    if (z3 != z2 && this.sslContext.isDefaultProtocolList(this.enabledProtocols)) {
                        sSLContextImpl = this.sslContext;
                        if (flag) {
                            z2 = false;
                        } else {
                            z2 = true;
                        }
                        this.enabledProtocols = sSLContextImpl.getDefaultProtocolList(z2);
                    }
                    if (!flag) {
                        z = true;
                    }
                    this.roleIsServer = z;
                    break;
                case 1:
                    if (!-assertionsDisabled) {
                        if (!(this.handshaker != null)) {
                            throw new AssertionError();
                        }
                    }
                    if (!this.handshaker.activated()) {
                        if (this.roleIsServer != (!flag) && this.sslContext.isDefaultProtocolList(this.enabledProtocols)) {
                            sSLContextImpl = this.sslContext;
                            if (flag) {
                                z2 = false;
                            } else {
                                z2 = true;
                            }
                            this.enabledProtocols = sSLContextImpl.getDefaultProtocolList(z2);
                        }
                        if (!flag) {
                            z = true;
                        }
                        this.roleIsServer = z;
                        this.connectionState = 0;
                        initHandshaker();
                        break;
                    }
                    break;
            }
            if (debug != null && Debug.isOn("ssl")) {
                System.out.println(threadName() + ", setUseClientMode() invoked in state = " + this.connectionState);
            }
            throw new IllegalArgumentException("Cannot change mode after SSL traffic has started");
        }
    }

    public synchronized boolean getUseClientMode() {
        return !this.roleIsServer;
    }

    public String[] getSupportedCipherSuites() {
        return this.sslContext.getSupportedCipherSuiteList().toStringArray();
    }

    public synchronized void setEnabledCipherSuites(String[] suites) {
        this.enabledCipherSuites = new CipherSuiteList(suites);
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnabledCipherSuites(this.enabledCipherSuites);
        }
    }

    public synchronized String[] getEnabledCipherSuites() {
        return this.enabledCipherSuites.toStringArray();
    }

    public String[] getSupportedProtocols() {
        return this.sslContext.getSuportedProtocolList().toStringArray();
    }

    public synchronized void setEnabledProtocols(String[] protocols) {
        this.enabledProtocols = new ProtocolList(protocols);
        if (!(this.handshaker == null || this.handshaker.activated())) {
            this.handshaker.setEnabledProtocols(this.enabledProtocols);
        }
    }

    public synchronized String[] getEnabledProtocols() {
        return this.enabledProtocols.toStringArray();
    }

    public void setSoTimeout(int timeout) throws SocketException {
        if (debug != null && Debug.isOn("ssl")) {
            System.out.println(threadName() + ", setSoTimeout(" + timeout + ") called");
        }
        if (this.self == this) {
            super.setSoTimeout(timeout);
        } else {
            this.self.setSoTimeout(timeout);
        }
    }

    public synchronized void addHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("listener is null");
        }
        if (this.handshakeListeners == null) {
            this.handshakeListeners = new HashMap(4);
        }
        this.handshakeListeners.put(listener, AccessController.getContext());
    }

    public synchronized void removeHandshakeCompletedListener(HandshakeCompletedListener listener) {
        if (this.handshakeListeners == null) {
            throw new IllegalArgumentException("no listeners");
        } else if (this.handshakeListeners.remove(listener) == null) {
            throw new IllegalArgumentException("listener not registered");
        } else if (this.handshakeListeners.isEmpty()) {
            this.handshakeListeners = null;
        }
    }

    public synchronized SSLParameters getSSLParameters() {
        SSLParameters params;
        params = super.getSSLParameters();
        params.setEndpointIdentificationAlgorithm(this.identificationProtocol);
        params.setAlgorithmConstraints(this.algorithmConstraints);
        return params;
    }

    public synchronized void setSSLParameters(SSLParameters params) {
        super.setSSLParameters(params);
        this.identificationProtocol = params.getEndpointIdentificationAlgorithm();
        this.algorithmConstraints = params.getAlgorithmConstraints();
        if (!(this.handshaker == null || this.handshaker.started())) {
            this.handshaker.setIdentificationProtocol(this.identificationProtocol);
            this.handshaker.setAlgorithmConstraints(this.algorithmConstraints);
        }
    }

    private static String threadName() {
        return Thread.currentThread().getName();
    }

    public String toString() {
        StringBuffer retval = new StringBuffer(80);
        retval.append(Integer.toHexString(hashCode()));
        retval.append("[");
        retval.append(this.sess.getCipherSuite());
        retval.append(": ");
        if (this.self == this) {
            retval.append(super.toString());
        } else {
            retval.append(this.self.toString());
        }
        retval.append("]");
        return retval.toString();
    }
}
