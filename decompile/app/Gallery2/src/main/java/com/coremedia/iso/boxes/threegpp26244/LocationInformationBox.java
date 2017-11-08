package com.coremedia.iso.boxes.threegpp26244;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.Utf8;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;

public class LocationInformationBox extends AbstractFullBox {
    private String additionalNotes = "";
    private double altitude;
    private String astronomicalBody = "";
    private String language;
    private double latitude;
    private double longitude;
    private String name = "";
    private int role;

    public LocationInformationBox() {
        super("loci");
    }

    protected long getContentSize() {
        return (long) (((Utf8.convert(this.name).length + 22) + Utf8.convert(this.astronomicalBody).length) + Utf8.convert(this.additionalNotes).length);
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        this.language = IsoTypeReader.readIso639(content);
        this.name = IsoTypeReader.readString(content);
        this.role = IsoTypeReader.readUInt8(content);
        this.longitude = IsoTypeReader.readFixedPoint1616(content);
        this.latitude = IsoTypeReader.readFixedPoint1616(content);
        this.altitude = IsoTypeReader.readFixedPoint1616(content);
        this.astronomicalBody = IsoTypeReader.readString(content);
        this.additionalNotes = IsoTypeReader.readString(content);
    }

    protected void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        IsoTypeWriter.writeIso639(byteBuffer, this.language);
        byteBuffer.put(Utf8.convert(this.name));
        byteBuffer.put((byte) 0);
        IsoTypeWriter.writeUInt8(byteBuffer, this.role);
        IsoTypeWriter.writeFixedPont1616(byteBuffer, this.longitude);
        IsoTypeWriter.writeFixedPont1616(byteBuffer, this.latitude);
        IsoTypeWriter.writeFixedPont1616(byteBuffer, this.altitude);
        byteBuffer.put(Utf8.convert(this.astronomicalBody));
        byteBuffer.put((byte) 0);
        byteBuffer.put(Utf8.convert(this.additionalNotes));
        byteBuffer.put((byte) 0);
    }
}
