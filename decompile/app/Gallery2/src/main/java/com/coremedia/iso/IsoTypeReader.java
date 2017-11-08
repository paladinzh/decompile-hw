package com.coremedia.iso;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

public final class IsoTypeReader {
    public static long readUInt32BE(ByteBuffer bb) {
        return (((((long) readUInt8(bb)) << 24) + (((long) readUInt8(bb)) << 16)) + (((long) readUInt8(bb)) << 8)) + (((long) readUInt8(bb)) << null);
    }

    public static long readUInt32(ByteBuffer bb) {
        long i = (long) bb.getInt();
        if (i < 0) {
            return i + 4294967296L;
        }
        return i;
    }

    public static int readUInt24(ByteBuffer bb) {
        return ((readUInt16(bb) << 8) + 0) + byte2int(bb.get());
    }

    public static int readUInt16(ByteBuffer bb) {
        return ((byte2int(bb.get()) << 8) + 0) + byte2int(bb.get());
    }

    public static int readUInt16BE(ByteBuffer bb) {
        return (byte2int(bb.get()) + 0) + (byte2int(bb.get()) << 8);
    }

    public static int readUInt8(ByteBuffer bb) {
        return byte2int(bb.get());
    }

    public static int byte2int(byte b) {
        return b < (byte) 0 ? b + 256 : b;
    }

    public static String readString(ByteBuffer byteBuffer) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        while (true) {
            int read = byteBuffer.get();
            if (read == 0) {
                return Utf8.convert(out.toByteArray());
            }
            out.write(read);
        }
    }

    public static String readString(ByteBuffer byteBuffer, int length) {
        byte[] buffer = new byte[length];
        byteBuffer.get(buffer);
        return Utf8.convert(buffer);
    }

    public static long readUInt64(ByteBuffer byteBuffer) {
        long result = 0 + (readUInt32(byteBuffer) << 32);
        if (result >= 0) {
            return result + readUInt32(byteBuffer);
        }
        throw new RuntimeException("I don't know how to deal with UInt64! long is not sufficient and I don't want to use BigInt");
    }

    public static double readFixedPoint1616(ByteBuffer bb) {
        byte[] bytes = new byte[4];
        bb.get(bytes);
        return ((double) ((((((bytes[0] << 24) & -16777216) | 0) | ((bytes[1] << 16) & 16711680)) | ((bytes[2] << 8) & 65280)) | (bytes[3] & 255))) / 65536.0d;
    }

    public static float readFixedPoint88(ByteBuffer bb) {
        byte[] bytes = new byte[2];
        bb.get(bytes);
        return ((float) ((short) ((bytes[1] & 255) | ((short) (((bytes[0] << 8) & 65280) | 0))))) / 256.0f;
    }

    public static String readIso639(ByteBuffer bb) {
        int bits = readUInt16(bb);
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 3; i++) {
            result.append((char) (((bits >> ((2 - i) * 5)) & 31) + 96));
        }
        return result.toString();
    }

    public static String read4cc(ByteBuffer bb) {
        byte[] b = new byte[4];
        bb.get(b);
        return IsoFile.bytesToFourCC(b);
    }
}
