package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class XmlBox extends AbstractFullBox {
    String xml = "";

    public XmlBox() {
        super("xml ");
    }

    protected long getContentSize() {
        return (long) (Utf8.utf8StringLengthInBytes(this.xml) + 4);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.xml = IsoTypeReader.readString(content, content.remaining());
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.xml));
    }

    public String toString() {
        return "XmlBox{xml='" + this.xml + '\'' + '}';
    }
}
