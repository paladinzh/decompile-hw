package com.coremedia.iso;

import java.nio.ByteBuffer;

public final class IsoTypeWriter {
    static final /* synthetic */ boolean -assertionsDisabled = (!IsoTypeWriter.class.desiredAssertionStatus());

    public static void writeUInt64(ByteBuffer bb, long u) {
        bb.putLong(u);
    }

    public static void writeUInt32(ByteBuffer bb, long u) {
        bb.putInt((int) u);
    }

    public static void writeUInt32BE(ByteBuffer bb, long u) {
        Object obj = null;
        if (!-assertionsDisabled) {
            if (u >= 0 && u <= 4294967296L) {
                obj = 1;
            }
            if (obj == null) {
                throw new AssertionError("The given long is not in the range of uint32 (" + u + ")");
            }
        }
        writeUInt16BE(bb, ((int) u) & 65535);
        writeUInt16BE(bb, (int) ((u >> 16) & 65535));
    }

    public static void writeUInt24(ByteBuffer bb, int i) {
        i &= 16777215;
        writeUInt16(bb, i >> 8);
        writeUInt8(bb, i);
    }

    public static void writeUInt16(ByteBuffer bb, int i) {
        i &= 65535;
        writeUInt8(bb, i >> 8);
        writeUInt8(bb, i & 255);
    }

    public static void writeUInt16BE(ByteBuffer bb, int i) {
        i &= 65535;
        writeUInt8(bb, i & 255);
        writeUInt8(bb, i >> 8);
    }

    public static void writeUInt8(ByteBuffer bb, int i) {
        bb.put((byte) (i & 255));
    }

    public static void writeFixedPont1616(ByteBuffer bb, double v) {
        int result = (int) (65536.0d * v);
        bb.put((byte) ((-16777216 & result) >> 24));
        bb.put((byte) ((16711680 & result) >> 16));
        bb.put((byte) ((65280 & result) >> 8));
        bb.put((byte) (result & 255));
    }

    public static void writeFixedPont88(ByteBuffer bb, double v) {
        short result = (short) ((int) (256.0d * v));
        bb.put((byte) ((65280 & result) >> 8));
        bb.put((byte) (result & 255));
    }

    public static void writeIso639(ByteBuffer bb, String language) {
        if (language.getBytes().length != 3) {
            throw new IllegalArgumentException("\"" + language + "\" language string isn't exactly 3 characters long!");
        }
        int bits = 0;
        for (int i = 0; i < 3; i++) {
            bits += (language.getBytes()[i] - 96) << ((2 - i) * 5);
        }
        writeUInt16(bb, bits);
    }

    public static void writeUtf8String(ByteBuffer bb, String string) {
        bb.put(Utf8.convert(string));
        writeUInt8(bb, 0);
    }
}
