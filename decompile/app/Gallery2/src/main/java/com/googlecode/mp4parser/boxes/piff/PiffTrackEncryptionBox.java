package com.googlecode.mp4parser.boxes.piff;

import com.googlecode.mp4parser.boxes.AbstractTrackEncryptionBox;

public class PiffTrackEncryptionBox extends AbstractTrackEncryptionBox {
    public PiffTrackEncryptionBox() {
        super("uuid");
    }

    public byte[] getUserType() {
        return new byte[]{(byte) -119, (byte) 116, (byte) -37, (byte) -50, (byte) 123, (byte) -25, (byte) 76, (byte) 81, (byte) -124, (byte) -7, (byte) 113, (byte) 72, (byte) -7, (byte) -120, (byte) 37, (byte) 84};
    }

    public int getFlags() {
        return 0;
    }
}
