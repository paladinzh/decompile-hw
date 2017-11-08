package com.coremedia.iso.boxes.apple;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;

public class AppleDataReferenceBox extends AbstractFullBox {
    private String dataReference;
    private int dataReferenceSize;
    private String dataReferenceType;

    public AppleDataReferenceBox() {
        super("rdrf");
    }

    protected long getContentSize() {
        return (long) (this.dataReferenceSize + 12);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.dataReferenceType = IsoTypeReader.read4cc(content);
        this.dataReferenceSize = CastUtils.l2i(IsoTypeReader.readUInt32(content));
        this.dataReference = IsoTypeReader.readString(content, this.dataReferenceSize);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(IsoFile.fourCCtoBytes(this.dataReferenceType));
        IsoTypeWriter.writeUInt32(byteBuffer, (long) this.dataReferenceSize);
        byteBuffer.put(Utf8.convert(this.dataReference));
    }
}
