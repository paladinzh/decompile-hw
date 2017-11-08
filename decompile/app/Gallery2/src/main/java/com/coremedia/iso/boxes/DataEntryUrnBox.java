package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class DataEntryUrnBox extends AbstractFullBox {
    private String location;
    private String name;

    public DataEntryUrnBox() {
        super("urn ");
    }

    public String getName() {
        return this.name;
    }

    public String getLocation() {
        return this.location;
    }

    protected long getContentSize() {
        return (long) (((Utf8.utf8StringLengthInBytes(this.name) + 1) + Utf8.utf8StringLengthInBytes(this.location)) + 1);
    }

    public void _parseDetails(ByteBuffer content) {
        this.name = IsoTypeReader.readString(content);
        this.location = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        byteBuffer.put(Utf8.convert(this.name));
        byteBuffer.put((byte) 0);
        byteBuffer.put(Utf8.convert(this.location));
        byteBuffer.put((byte) 0);
    }

    public String toString() {
        return "DataEntryUrlBox[name=" + getName() + ";location=" + getLocation() + "]";
    }
}
