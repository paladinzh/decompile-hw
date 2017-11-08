package com.coremedia.iso.boxes.apple;

import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.Utf8;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ContainerBox;
import com.googlecode.mp4parser.AbstractBox;
import com.googlecode.mp4parser.util.ByteBufferByteChannel;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

public abstract class AbstractAppleMetaDataBox extends AbstractBox implements ContainerBox {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static Logger LOG = Logger.getLogger(AbstractAppleMetaDataBox.class.getName());
    AppleDataBox appleDataBox = new AppleDataBox();

    static {
        boolean z;
        if (AbstractAppleMetaDataBox.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public List<Box> getBoxes() {
        return Collections.singletonList(this.appleDataBox);
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz) {
        return getBoxes(clazz, false);
    }

    public <T extends Box> List<T> getBoxes(Class<T> clazz, boolean recursive) {
        if (clazz.isAssignableFrom(this.appleDataBox.getClass())) {
            return Collections.singletonList(this.appleDataBox);
        }
        return null;
    }

    public AbstractAppleMetaDataBox(String type) {
        super(type);
    }

    public void _parseDetails(ByteBuffer content) {
        long dataBoxSize = IsoTypeReader.readUInt32(content);
        String thisShouldBeData = IsoTypeReader.read4cc(content);
        if (-assertionsDisabled || MapTilsCacheAndResManager.AUTONAVI_DATA_PATH.equals(thisShouldBeData)) {
            this.appleDataBox = new AppleDataBox();
            try {
                this.appleDataBox.parse(new ByteBufferByteChannel(content), null, (long) content.remaining(), null);
                this.appleDataBox.setParent(this);
                return;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        throw new AssertionError();
    }

    protected long getContentSize() {
        return this.appleDataBox.getSize();
    }

    protected void getContent(ByteBuffer byteBuffer) {
        try {
            this.appleDataBox.getBox(new ByteBufferByteChannel(byteBuffer));
        } catch (IOException e) {
            throw new RuntimeException("The Channel is based on a ByteBuffer and therefore it shouldn't throw any exception");
        }
    }

    public String toString() {
        return getClass().getSimpleName() + "{" + "appleDataBox=" + getValue() + '}';
    }

    static long toLong(byte b) {
        if (b < (byte) 0) {
            b += 256;
        }
        return (long) b;
    }

    public String getValue() {
        int i = 0;
        if (this.appleDataBox.getFlags() == 1) {
            return Utf8.convert(this.appleDataBox.getData());
        }
        if (this.appleDataBox.getFlags() == 21) {
            byte[] content = this.appleDataBox.getData();
            long l = 0;
            int length = content.length;
            int current = 1;
            while (i < content.length) {
                l += toLong(content[i]) << ((length - current) * 8);
                i++;
                current++;
            }
            return "" + l;
        } else if (this.appleDataBox.getFlags() != 0) {
            return "unknown";
        } else {
            return String.format("%x", new Object[]{new BigInteger(this.appleDataBox.getData())});
        }
    }
}
