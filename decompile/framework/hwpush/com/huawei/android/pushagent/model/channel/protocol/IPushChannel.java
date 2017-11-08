package com.huawei.android.pushagent.model.channel.protocol;

import java.io.InputStream;
import java.net.Socket;

public interface IPushChannel {

    public enum ChannelType {
        ChannelType_Normal,
        ChannelType_SSL,
        ChannelType_SSL_Resume,
        ChannelType_Secure
    }

    boolean a(Socket socket);

    boolean a(byte[] bArr);

    ChannelType br();

    void close();

    InputStream getInputStream();

    Socket getSocket();

    boolean hasConnection();
}
