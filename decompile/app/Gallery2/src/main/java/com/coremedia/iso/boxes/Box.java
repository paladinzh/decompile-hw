package com.coremedia.iso.boxes;

import com.coremedia.iso.BoxParser;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;

public interface Box {
    void getBox(WritableByteChannel writableByteChannel) throws IOException;

    ContainerBox getParent();

    long getSize();

    String getType();

    void parse(ReadableByteChannel readableByteChannel, ByteBuffer byteBuffer, long j, BoxParser boxParser) throws IOException;

    void setParent(ContainerBox containerBox);
}
