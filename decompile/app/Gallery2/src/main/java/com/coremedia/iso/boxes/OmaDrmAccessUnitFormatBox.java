package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public final class OmaDrmAccessUnitFormatBox extends AbstractFullBox {
    private byte allBits;
    private int initVectorLength;
    private int keyIndicatorLength;
    private boolean selectiveEncryption;

    protected long getContentSize() {
        return 7;
    }

    public OmaDrmAccessUnitFormatBox() {
        super("odaf");
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.allBits = (byte) IsoTypeReader.readUInt8(content);
        this.selectiveEncryption = (this.allBits & 128) == 128;
        this.keyIndicatorLength = IsoTypeReader.readUInt8(content);
        this.initVectorLength = IsoTypeReader.readUInt8(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt8(byteBuffer, this.allBits);
        IsoTypeWriter.writeUInt8(byteBuffer, this.keyIndicatorLength);
        IsoTypeWriter.writeUInt8(byteBuffer, this.initVectorLength);
    }
}
