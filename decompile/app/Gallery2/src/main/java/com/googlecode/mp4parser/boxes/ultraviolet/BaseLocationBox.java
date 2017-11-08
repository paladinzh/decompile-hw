package com.googlecode.mp4parser.boxes.ultraviolet;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class BaseLocationBox extends AbstractFullBox {
    String baseLocation = "";
    String purchaseLocation = "";

    public BaseLocationBox() {
        super("bloc");
    }

    protected long getContentSize() {
        return 1028;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.baseLocation = IsoTypeReader.readString(content);
        content.get(new byte[((256 - Utf8.utf8StringLengthInBytes(this.baseLocation)) - 1)]);
        this.purchaseLocation = IsoTypeReader.readString(content);
        content.get(new byte[((256 - Utf8.utf8StringLengthInBytes(this.purchaseLocation)) - 1)]);
        content.get(new byte[512]);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(Utf8.convert(this.baseLocation));
        byteBuffer.put(new byte[(256 - Utf8.utf8StringLengthInBytes(this.baseLocation))]);
        byteBuffer.put(Utf8.convert(this.purchaseLocation));
        byteBuffer.put(new byte[(256 - Utf8.utf8StringLengthInBytes(this.purchaseLocation))]);
        byteBuffer.put(new byte[512]);
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BaseLocationBox that = (BaseLocationBox) o;
        if (this.baseLocation == null ? that.baseLocation == null : this.baseLocation.equals(that.baseLocation)) {
            return this.purchaseLocation == null ? that.purchaseLocation == null : this.purchaseLocation.equals(that.purchaseLocation);
        } else {
            return false;
        }
    }

    public int hashCode() {
        int result;
        int i = 0;
        if (this.baseLocation != null) {
            result = this.baseLocation.hashCode();
        } else {
            result = 0;
        }
        int i2 = result * 31;
        if (this.purchaseLocation != null) {
            i = this.purchaseLocation.hashCode();
        }
        return i2 + i;
    }
}
