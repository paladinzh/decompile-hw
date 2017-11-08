package java.net;

import java.io.IOException;

class PlainDatagramSocketImpl extends AbstractPlainDatagramSocketImpl {
    private static native void init();

    protected native synchronized void bind0(int i, InetAddress inetAddress) throws SocketException;

    protected native void connect0(InetAddress inetAddress, int i) throws SocketException;

    protected native void datagramSocketClose();

    protected native void datagramSocketCreate() throws SocketException;

    protected native void disconnect0(int i);

    protected native byte getTTL() throws IOException;

    protected native int getTimeToLive() throws IOException;

    protected native void join(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native void leave(InetAddress inetAddress, NetworkInterface networkInterface) throws IOException;

    protected native synchronized int peek(InetAddress inetAddress) throws IOException;

    protected native synchronized int peekData(DatagramPacket datagramPacket) throws IOException;

    protected native synchronized void receive0(DatagramPacket datagramPacket) throws IOException;

    protected native void send(DatagramPacket datagramPacket) throws IOException;

    protected native void setTTL(byte b) throws IOException;

    protected native void setTimeToLive(int i) throws IOException;

    protected native Object socketGetOption(int i) throws SocketException;

    protected native void socketSetOption(int i, Object obj) throws SocketException;

    PlainDatagramSocketImpl() {
    }

    static {
        init();
    }
}
