package com.avast.android.shepherd.obfuscated;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.OutputStream;

/* compiled from: Unknown */
public class j {
    private static final byte[] a = new byte[]{(byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70};

    public static byte[] a(long j) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream(8);
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);
        dataOutputStream.writeLong(j);
        byte[] toByteArray = byteArrayOutputStream.toByteArray();
        dataOutputStream.close();
        return toByteArray;
    }
}
