package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import java.nio.ByteBuffer;

public class VideoMediaHeaderBox extends AbstractMediaHeaderBox {
    private int graphicsmode = 0;
    private int[] opcolor = new int[]{0, 0, 0};

    public VideoMediaHeaderBox() {
        super("vmhd");
        setFlags(1);
    }

    public int getGraphicsmode() {
        return this.graphicsmode;
    }

    public int[] getOpcolor() {
        return this.opcolor;
    }

    protected long getContentSize() {
        return 12;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.graphicsmode = IsoTypeReader.readUInt16(content);
        this.opcolor = new int[3];
        for (int i = 0; i < 3; i++) {
            this.opcolor[i] = IsoTypeReader.readUInt16(content);
        }
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeUInt16(byteBuffer, this.graphicsmode);
        for (int anOpcolor : this.opcolor) {
            IsoTypeWriter.writeUInt16(byteBuffer, anOpcolor);
        }
    }

    public String toString() {
        return "VideoMediaHeaderBox[graphicsmode=" + getGraphicsmode() + ";opcolor0=" + getOpcolor()[0] + ";opcolor1=" + getOpcolor()[1] + ";opcolor2=" + getOpcolor()[2] + "]";
    }
}
