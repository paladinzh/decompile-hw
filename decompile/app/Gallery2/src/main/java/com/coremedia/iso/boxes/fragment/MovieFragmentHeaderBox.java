package com.coremedia.iso.boxes.fragment;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class MovieFragmentHeaderBox extends AbstractFullBox {
    private long sequenceNumber;

    public MovieFragmentHeaderBox() {
        super("mfhd");
    }

    protected long getContentSize() {
        return 8;
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt32(byteBuffer, this.sequenceNumber);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.sequenceNumber = IsoTypeReader.readUInt32(content);
    }

    public String toString() {
        return "MovieFragmentHeaderBox{sequenceNumber=" + this.sequenceNumber + '}';
    }
}
