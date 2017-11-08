package com.coremedia.iso.boxes.apple;

import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public final class AppleDataBox extends AbstractFullBox {
    private byte[] data;
    private byte[] fourBytes = new byte[4];

    private static AppleDataBox getEmpty() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setVersion(0);
        appleDataBox.setFourBytes(new byte[4]);
        return appleDataBox;
    }

    public static AppleDataBox getStringAppleDataBox() {
        AppleDataBox appleDataBox = getEmpty();
        appleDataBox.setFlags(1);
        appleDataBox.setData(new byte[]{(byte) 0});
        return appleDataBox;
    }

    public static AppleDataBox getUint8AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{(byte) 0});
        return appleDataBox;
    }

    public static AppleDataBox getUint16AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{(byte) 0, (byte) 0});
        return appleDataBox;
    }

    public static AppleDataBox getUint32AppleDataBox() {
        AppleDataBox appleDataBox = new AppleDataBox();
        appleDataBox.setFlags(21);
        appleDataBox.setData(new byte[]{(byte) 0, (byte) 0, (byte) 0, (byte) 0});
        return appleDataBox;
    }

    public AppleDataBox() {
        super(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH);
    }

    protected long getContentSize() {
        return (long) (this.data.length + 8);
    }

    public void setData(byte[] data) {
        this.data = new byte[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public void setFourBytes(byte[] fourBytes) {
        System.arraycopy(fourBytes, 0, this.fourBytes, 0, 4);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.fourBytes = new byte[4];
        content.get(this.fourBytes);
        this.data = new byte[content.remaining()];
        content.get(this.data);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        byteBuffer.put(this.fourBytes, 0, 4);
        byteBuffer.put(this.data);
    }

    public byte[] getData() {
        return this.data;
    }
}
