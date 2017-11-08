package com.android.gallery3d.ui;

import android.graphics.Rect;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

class NinePatchChunk {
    public int[] mColor;
    public int[] mDivX;
    public int[] mDivY;
    public Rect mPaddings = new Rect();

    NinePatchChunk() {
    }

    private static void readIntArray(int[] data, ByteBuffer buffer) {
        int n = data.length;
        for (int i = 0; i < n; i++) {
            data[i] = buffer.getInt();
        }
    }

    private static void checkDivCount(int length) {
        if (length == 0 || (length & 1) != 0) {
            throw new RuntimeException("invalid nine-patch: " + length);
        }
    }

    public static NinePatchChunk deserialize(byte[] data) {
        ByteBuffer byteBuffer = ByteBuffer.wrap(data).order(ByteOrder.nativeOrder());
        if (byteBuffer.get() == (byte) 0) {
            return null;
        }
        NinePatchChunk chunk = new NinePatchChunk();
        chunk.mDivX = new int[byteBuffer.get()];
        chunk.mDivY = new int[byteBuffer.get()];
        chunk.mColor = new int[byteBuffer.get()];
        checkDivCount(chunk.mDivX.length);
        checkDivCount(chunk.mDivY.length);
        byteBuffer.getInt();
        byteBuffer.getInt();
        chunk.mPaddings.left = byteBuffer.getInt();
        chunk.mPaddings.right = byteBuffer.getInt();
        chunk.mPaddings.top = byteBuffer.getInt();
        chunk.mPaddings.bottom = byteBuffer.getInt();
        byteBuffer.getInt();
        readIntArray(chunk.mDivX, byteBuffer);
        readIntArray(chunk.mDivY, byteBuffer);
        readIntArray(chunk.mColor, byteBuffer);
        return chunk;
    }
}
