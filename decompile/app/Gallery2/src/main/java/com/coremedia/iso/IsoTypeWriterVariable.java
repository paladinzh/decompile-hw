package com.coremedia.iso;

import java.nio.ByteBuffer;

public final class IsoTypeWriterVariable {
    public static void write(long v, ByteBuffer bb, int bytes) {
        switch (bytes) {
            case 1:
                IsoTypeWriter.writeUInt8(bb, (int) (255 & v));
                return;
            case 2:
                IsoTypeWriter.writeUInt16(bb, (int) (65535 & v));
                return;
            case 3:
                IsoTypeWriter.writeUInt24(bb, (int) (16777215 & v));
                return;
            case 4:
                IsoTypeWriter.writeUInt32(bb, v);
                return;
            case 8:
                IsoTypeWriter.writeUInt64(bb, v);
                return;
            default:
                throw new RuntimeException("I don't know how to read " + bytes + " bytes");
        }
    }
}
