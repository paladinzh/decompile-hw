package com.googlecode.mp4parser.boxes.mp4.samplegrouping;

import com.coremedia.iso.Hex;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class CencSampleEncryptionInformationGroupEntry extends GroupEntry {
    private int isEncrypted;
    private byte ivSize;
    private byte[] kid = new byte[16];

    public void parse(ByteBuffer byteBuffer) {
        this.isEncrypted = IsoTypeReader.readUInt24(byteBuffer);
        this.ivSize = (byte) IsoTypeReader.readUInt8(byteBuffer);
        this.kid = new byte[16];
        byteBuffer.get(this.kid);
    }

    public ByteBuffer get() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(20);
        IsoTypeWriter.writeUInt24(byteBuffer, this.isEncrypted);
        IsoTypeWriter.writeUInt8(byteBuffer, this.ivSize);
        byteBuffer.put(this.kid);
        byteBuffer.rewind();
        return byteBuffer;
    }

    public String toString() {
        return "CencSampleEncryptionInformationGroupEntry{isEncrypted=" + this.isEncrypted + ", ivSize=" + this.ivSize + ", kid=" + Hex.encodeHex(this.kid) + '}';
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CencSampleEncryptionInformationGroupEntry that = (CencSampleEncryptionInformationGroupEntry) o;
        return this.isEncrypted == that.isEncrypted && this.ivSize == that.ivSize && Arrays.equals(this.kid, that.kid);
    }

    public int hashCode() {
        return (((this.isEncrypted * 31) + this.ivSize) * 31) + (this.kid != null ? Arrays.hashCode(this.kid) : 0);
    }
}
