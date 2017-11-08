package com.android.okhttp.internal.framed;

import java.io.IOException;

public interface IncomingStreamHandler {
    public static final IncomingStreamHandler REFUSE_INCOMING_STREAMS = new IncomingStreamHandler() {
        public void receive(FramedStream stream) throws IOException {
            stream.close(ErrorCode.REFUSED_STREAM);
        }
    };

    void receive(FramedStream framedStream) throws IOException;
}
