package com.googlecode.mp4parser.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;
import java.util.Arrays;

public abstract class AbstractTrackEncryptionBox extends AbstractFullBox {
    int defaultAlgorithmId;
    int defaultIvSize;
    byte[] default_KID;

    protected AbstractTrackEncryptionBox(String type) {
        super(type);
    }

    public int getDefaultIvSize() {
        return this.defaultIvSize;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.defaultAlgorithmId = IsoTypeReader.readUInt24(content);
        this.defaultIvSize = IsoTypeReader.readUInt8(content);
        this.default_KID = new byte[16];
        content.get(this.default_KID);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt24(byteBuffer, this.defaultAlgorithmId);
        IsoTypeWriter.writeUInt8(byteBuffer, this.defaultIvSize);
        byteBuffer.put(this.default_KID);
    }

    protected long getContentSize() {
        return 24;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AbstractTrackEncryptionBox that = (AbstractTrackEncryptionBox) o;
        return this.defaultAlgorithmId == that.defaultAlgorithmId && this.defaultIvSize == that.defaultIvSize && Arrays.equals(this.default_KID, that.default_KID);
    }

    public int hashCode() {
        return (((this.defaultAlgorithmId * 31) + this.defaultIvSize) * 31) + (this.default_KID != null ? Arrays.hashCode(this.default_KID) : 0);
    }
}
