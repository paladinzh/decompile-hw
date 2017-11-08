package sun.nio.ch;

import java.io.FileDescriptor;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketOption;
import java.nio.ByteBuffer;
import java.nio.channels.Channel;
import java.nio.channels.DatagramChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;

class InheritedChannel {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static final int O_RDONLY = 0;
    private static final int O_RDWR = 2;
    private static final int O_WRONLY = 1;
    private static final int SOCK_DGRAM = 2;
    private static final int SOCK_STREAM = 1;
    private static final int UNKNOWN = -1;
    private static Channel channel = null;
    private static int devnull = -1;
    private static boolean haveChannel = false;

    public static class InheritedDatagramChannelImpl extends DatagramChannelImpl {
        public /* bridge */ /* synthetic */ DatagramChannel connect(SocketAddress sa) {
            return super.connect(sa);
        }

        public /* bridge */ /* synthetic */ DatagramChannel disconnect() {
            return super.disconnect();
        }

        public /* bridge */ /* synthetic */ FileDescriptor getFD() {
            return super.getFD();
        }

        public /* bridge */ /* synthetic */ int getFDVal() {
            return super.getFDVal();
        }

        public /* bridge */ /* synthetic */ SocketAddress getLocalAddress() {
            return super.getLocalAddress();
        }

        public /* bridge */ /* synthetic */ Object getOption(SocketOption name) {
            return super.getOption(name);
        }

        public /* bridge */ /* synthetic */ SocketAddress getRemoteAddress() {
            return super.getRemoteAddress();
        }

        public /* bridge */ /* synthetic */ boolean isConnected() {
            return super.isConnected();
        }

        public /* bridge */ /* synthetic */ void kill() {
            super.kill();
        }

        public /* bridge */ /* synthetic */ SocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ int read(ByteBuffer buf) {
            return super.read(buf);
        }

        public /* bridge */ /* synthetic */ long read(ByteBuffer[] dsts, int offset, int length) {
            return super.read(dsts, offset, length);
        }

        public /* bridge */ /* synthetic */ SocketAddress receive(ByteBuffer dst) {
            return super.receive(dst);
        }

        public /* bridge */ /* synthetic */ SocketAddress remoteAddress() {
            return super.remoteAddress();
        }

        public /* bridge */ /* synthetic */ int send(ByteBuffer src, SocketAddress target) {
            return super.send(src, target);
        }

        public /* bridge */ /* synthetic */ DatagramSocket socket() {
            return super.socket();
        }

        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
            super.translateAndSetInterestOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }

        public /* bridge */ /* synthetic */ int write(ByteBuffer buf) {
            return super.write(buf);
        }

        public /* bridge */ /* synthetic */ long write(ByteBuffer[] srcs, int offset, int length) {
            return super.write(srcs, offset, length);
        }

        InheritedDatagramChannelImpl(SelectorProvider sp, FileDescriptor fd) throws IOException {
            super(sp, fd);
        }

        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            InheritedChannel.detachIOStreams();
        }
    }

    public static class InheritedServerSocketChannelImpl extends ServerSocketChannelImpl {
        public /* bridge */ /* synthetic */ SocketChannel accept() {
            return super.accept();
        }

        public /* bridge */ /* synthetic */ ServerSocketChannel bind(SocketAddress local, int backlog) {
            return super.bind(local, backlog);
        }

        public /* bridge */ /* synthetic */ FileDescriptor getFD() {
            return super.getFD();
        }

        public /* bridge */ /* synthetic */ int getFDVal() {
            return super.getFDVal();
        }

        public /* bridge */ /* synthetic */ SocketAddress getLocalAddress() {
            return super.getLocalAddress();
        }

        public /* bridge */ /* synthetic */ Object getOption(SocketOption name) {
            return super.getOption(name);
        }

        public /* bridge */ /* synthetic */ boolean isBound() {
            return super.isBound();
        }

        public /* bridge */ /* synthetic */ void kill() {
            super.kill();
        }

        public /* bridge */ /* synthetic */ InetSocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ ServerSocket socket() {
            return super.socket();
        }

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
            super.translateAndSetInterestOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }

        InheritedServerSocketChannelImpl(SelectorProvider sp, FileDescriptor fd) throws IOException {
            super(sp, fd, true);
        }

        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            InheritedChannel.detachIOStreams();
        }
    }

    public static class InheritedSocketChannelImpl extends SocketChannelImpl {
        public /* bridge */ /* synthetic */ boolean connect(SocketAddress sa) {
            return super.connect(sa);
        }

        public /* bridge */ /* synthetic */ boolean finishConnect() {
            return super.finishConnect();
        }

        public /* bridge */ /* synthetic */ FileDescriptor getFD() {
            return super.getFD();
        }

        public /* bridge */ /* synthetic */ int getFDVal() {
            return super.getFDVal();
        }

        public /* bridge */ /* synthetic */ SocketAddress getLocalAddress() {
            return super.getLocalAddress();
        }

        public /* bridge */ /* synthetic */ Object getOption(SocketOption name) {
            return super.getOption(name);
        }

        public /* bridge */ /* synthetic */ SocketAddress getRemoteAddress() {
            return super.getRemoteAddress();
        }

        public /* bridge */ /* synthetic */ boolean isConnected() {
            return super.isConnected();
        }

        public /* bridge */ /* synthetic */ boolean isConnectionPending() {
            return super.isConnectionPending();
        }

        public /* bridge */ /* synthetic */ boolean isInputOpen() {
            return super.isInputOpen();
        }

        public /* bridge */ /* synthetic */ boolean isOutputOpen() {
            return super.isOutputOpen();
        }

        public /* bridge */ /* synthetic */ void kill() {
            super.kill();
        }

        public /* bridge */ /* synthetic */ InetSocketAddress localAddress() {
            return super.localAddress();
        }

        public /* bridge */ /* synthetic */ int read(ByteBuffer buf) {
            return super.read(buf);
        }

        public /* bridge */ /* synthetic */ long read(ByteBuffer[] dsts, int offset, int length) {
            return super.read(dsts, offset, length);
        }

        public /* bridge */ /* synthetic */ SocketAddress remoteAddress() {
            return super.remoteAddress();
        }

        public /* bridge */ /* synthetic */ SocketChannel shutdownInput() {
            return super.shutdownInput();
        }

        public /* bridge */ /* synthetic */ SocketChannel shutdownOutput() {
            return super.shutdownOutput();
        }

        public /* bridge */ /* synthetic */ Socket socket() {
            return super.socket();
        }

        public /* bridge */ /* synthetic */ String toString() {
            return super.toString();
        }

        public /* bridge */ /* synthetic */ void translateAndSetInterestOps(int ops, SelectionKeyImpl sk) {
            super.translateAndSetInterestOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndSetReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndSetReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateAndUpdateReadyOps(int ops, SelectionKeyImpl sk) {
            return super.translateAndUpdateReadyOps(ops, sk);
        }

        public /* bridge */ /* synthetic */ boolean translateReadyOps(int ops, int initialOps, SelectionKeyImpl sk) {
            return super.translateReadyOps(ops, initialOps, sk);
        }

        public /* bridge */ /* synthetic */ int write(ByteBuffer buf) {
            return super.write(buf);
        }

        public /* bridge */ /* synthetic */ long write(ByteBuffer[] srcs, int offset, int length) {
            return super.write(srcs, offset, length);
        }

        InheritedSocketChannelImpl(SelectorProvider sp, FileDescriptor fd, InetSocketAddress remote) throws IOException {
            super(sp, fd, remote);
        }

        protected void implCloseSelectableChannel() throws IOException {
            super.implCloseSelectableChannel();
            InheritedChannel.detachIOStreams();
        }
    }

    private static native void close0(int i) throws IOException;

    private static native int dup(int i) throws IOException;

    private static native void dup2(int i, int i2) throws IOException;

    private static native int open0(String str, int i) throws IOException;

    private static native InetAddress peerAddress0(int i);

    private static native int peerPort0(int i);

    private static native int soType0(int i);

    InheritedChannel() {
    }

    static {
        boolean z;
        if (InheritedChannel.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    private static void detachIOStreams() {
        try {
            dup2(devnull, 0);
            dup2(devnull, 1);
            dup2(devnull, 2);
        } catch (IOException e) {
            throw new InternalError();
        }
    }

    private static void checkAccess(Channel c) {
        SecurityManager sm = System.getSecurityManager();
        if (sm != null) {
            sm.checkPermission(new RuntimePermission("inheritedChannel"));
        }
    }

    private static Channel createChannel() throws IOException {
        int i = 1;
        int fdVal = dup(0);
        int st = soType0(fdVal);
        if (st == 1 || st == 2) {
            FileDescriptor fd = (FileDescriptor) Reflect.invoke(Reflect.lookupConstructor("java.io.FileDescriptor", new Class[]{Integer.TYPE}), new Object[]{new Integer(fdVal)});
            SelectorProvider provider = SelectorProvider.provider();
            if (-assertionsDisabled || (provider instanceof SelectorProviderImpl)) {
                Channel c;
                if (st == 1) {
                    InetAddress ia = peerAddress0(fdVal);
                    if (ia == null) {
                        c = new InheritedServerSocketChannelImpl(provider, fd);
                    } else {
                        int port = peerPort0(fdVal);
                        if (!-assertionsDisabled) {
                            if (port <= 0) {
                                i = 0;
                            }
                            if (i == 0) {
                                throw new AssertionError();
                            }
                        }
                        c = new InheritedSocketChannelImpl(provider, fd, new InetSocketAddress(ia, port));
                    }
                } else {
                    c = new InheritedDatagramChannelImpl(provider, fd);
                }
                return c;
            }
            throw new AssertionError();
        }
        close0(fdVal);
        return null;
    }

    public static synchronized Channel getChannel() throws IOException {
        Channel channel;
        synchronized (InheritedChannel.class) {
            if (devnull < 0) {
                devnull = open0("/dev/null", 2);
            }
            if (!haveChannel) {
                channel = createChannel();
                haveChannel = true;
            }
            if (channel != null) {
                checkAccess(channel);
            }
            channel = channel;
        }
        return channel;
    }
}
