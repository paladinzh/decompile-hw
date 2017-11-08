package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeReader;
import com.googlecode.mp4parser.AbstractBox;
import java.nio.ByteBuffer;

public class OriginalFormatBox extends AbstractBox {
    private String dataFormat = "    ";

    public OriginalFormatBox() {
        super("frma");
    }

    public String getDataFormat() {
        return this.dataFormat;
    }

    protected long getContentSize() {
        return 4;
    }

    public void _parseDetails(ByteBuffer content) {
        this.dataFormat = IsoTypeReader.read4cc(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(IsoFile.fourCCtoBytes(this.dataFormat));
    }

    public String toString() {
        return "OriginalFormatBox[dataFormat=" + getDataFormat() + "]";
    }
}
