package com.googlecode.mp4parser.boxes.mp4.objectdescriptors;

import java.nio.ByteBuffer;
import tmsdk.fg.module.spacemanager.SpaceManager;

public class BitWriterBuffer {
    static final /* synthetic */ boolean -assertionsDisabled = (!BitWriterBuffer.class.desiredAssertionStatus());
    private ByteBuffer buffer;
    int initialPos;
    int position = 0;

    public BitWriterBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
        this.initialPos = buffer.position();
    }

    public void writeBits(int i, int numBits) {
        ByteBuffer byteBuffer;
        int i2;
        int i3 = 1;
        if (!-assertionsDisabled) {
            if ((i <= (1 << numBits) + -1 ? 1 : 0) == 0) {
                throw new AssertionError(String.format("Trying to write a value bigger (%s) than the number bits (%s) allows. Please mask the value before writing it and make your code is really working as intended.", new Object[]{Integer.valueOf(i), Integer.valueOf((1 << numBits) - 1)}));
            }
        }
        int left = 8 - (this.position % 8);
        if (numBits <= left) {
            int current = this.buffer.get(this.initialPos + (this.position / 8));
            if (current < 0) {
                current += 256;
            }
            current += i << (left - numBits);
            byteBuffer = this.buffer;
            i2 = this.initialPos + (this.position / 8);
            if (current > 127) {
                current += SpaceManager.ERROR_CODE_UNKNOW;
            }
            byteBuffer.put(i2, (byte) current);
            this.position += numBits;
        } else {
            int bitsSecondWrite = numBits - left;
            writeBits(i >> bitsSecondWrite, left);
            writeBits(((1 << bitsSecondWrite) - 1) & i, bitsSecondWrite);
        }
        byteBuffer = this.buffer;
        i2 = this.initialPos + (this.position / 8);
        if (this.position % 8 <= 0) {
            i3 = 0;
        }
        byteBuffer.position(i3 + i2);
    }
}
