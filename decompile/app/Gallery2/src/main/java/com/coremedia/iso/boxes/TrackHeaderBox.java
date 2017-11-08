package com.coremedia.iso.boxes;

import com.coremedia.iso.IsoTypeReader;
import com.coremedia.iso.IsoTypeWriter;
import com.googlecode.mp4parser.AbstractFullBox;
import java.nio.ByteBuffer;
import tmsdk.common.module.update.UpdateConfig;

public class TrackHeaderBox extends AbstractFullBox {
    private int alternateGroup;
    private long creationTime;
    private long duration;
    private double height;
    private int layer;
    private long[] matrix = new long[]{UpdateConfig.UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL, 0, 0, 0, UpdateConfig.UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL, 0, 0, 0, UpdateConfig.UPDATE_FLAG_VIRUS_BASE};
    private long modificationTime;
    private long trackId;
    private float volume;
    private double width;

    public TrackHeaderBox() {
        super("tkhd");
    }

    public long getCreationTime() {
        return this.creationTime;
    }

    public long getModificationTime() {
        return this.modificationTime;
    }

    public long getTrackId() {
        return this.trackId;
    }

    public long getDuration() {
        return this.duration;
    }

    public int getLayer() {
        return this.layer;
    }

    public int getAlternateGroup() {
        return this.alternateGroup;
    }

    public float getVolume() {
        return this.volume;
    }

    public long[] getMatrix() {
        return this.matrix;
    }

    public double getWidth() {
        return this.width;
    }

    public double getHeight() {
        return this.height;
    }

    protected long getContentSize() {
        long contentSize;
        if (getVersion() == 1) {
            contentSize = 36;
        } else {
            contentSize = 24;
        }
        return contentSize + 60;
    }

    public void _parseDetails(ByteBuffer content) {
        parseVersionAndFlags(content);
        if (getVersion() == 1) {
            this.creationTime = IsoTypeReader.readUInt64(content);
            this.modificationTime = IsoTypeReader.readUInt64(content);
            this.trackId = IsoTypeReader.readUInt32(content);
            IsoTypeReader.readUInt32(content);
            this.duration = IsoTypeReader.readUInt64(content);
        } else {
            this.creationTime = IsoTypeReader.readUInt32(content);
            this.modificationTime = IsoTypeReader.readUInt32(content);
            this.trackId = IsoTypeReader.readUInt32(content);
            IsoTypeReader.readUInt32(content);
            this.duration = IsoTypeReader.readUInt32(content);
        }
        IsoTypeReader.readUInt32(content);
        IsoTypeReader.readUInt32(content);
        this.layer = IsoTypeReader.readUInt16(content);
        this.alternateGroup = IsoTypeReader.readUInt16(content);
        this.volume = IsoTypeReader.readFixedPoint88(content);
        IsoTypeReader.readUInt16(content);
        this.matrix = new long[9];
        for (int i = 0; i < 9; i++) {
            this.matrix[i] = IsoTypeReader.readUInt32(content);
        }
        this.width = IsoTypeReader.readFixedPoint1616(content);
        this.height = IsoTypeReader.readFixedPoint1616(content);
    }

    public void getContent(ByteBuffer byteBuffer) {
        writeVersionAndFlags(byteBuffer);
        if (getVersion() == 1) {
            IsoTypeWriter.writeUInt64(byteBuffer, this.creationTime);
            IsoTypeWriter.writeUInt64(byteBuffer, this.modificationTime);
            IsoTypeWriter.writeUInt32(byteBuffer, this.trackId);
            IsoTypeWriter.writeUInt32(byteBuffer, 0);
            IsoTypeWriter.writeUInt64(byteBuffer, this.duration);
        } else {
            IsoTypeWriter.writeUInt32(byteBuffer, this.creationTime);
            IsoTypeWriter.writeUInt32(byteBuffer, this.modificationTime);
            IsoTypeWriter.writeUInt32(byteBuffer, this.trackId);
            IsoTypeWriter.writeUInt32(byteBuffer, 0);
            IsoTypeWriter.writeUInt32(byteBuffer, this.duration);
        }
        IsoTypeWriter.writeUInt32(byteBuffer, 0);
        IsoTypeWriter.writeUInt32(byteBuffer, 0);
        IsoTypeWriter.writeUInt16(byteBuffer, this.layer);
        IsoTypeWriter.writeUInt16(byteBuffer, this.alternateGroup);
        IsoTypeWriter.writeFixedPont88(byteBuffer, (double) this.volume);
        IsoTypeWriter.writeUInt16(byteBuffer, 0);
        for (int i = 0; i < 9; i++) {
            IsoTypeWriter.writeUInt32(byteBuffer, this.matrix[i]);
        }
        IsoTypeWriter.writeFixedPont1616(byteBuffer, this.width);
        IsoTypeWriter.writeFixedPont1616(byteBuffer, this.height);
    }

    public String toString() {
        StringBuilder result = new StringBuilder();
        result.append("TrackHeaderBox[");
        result.append("creationTime=").append(getCreationTime());
        result.append(";");
        result.append("modificationTime=").append(getModificationTime());
        result.append(";");
        result.append("trackId=").append(getTrackId());
        result.append(";");
        result.append("duration=").append(getDuration());
        result.append(";");
        result.append("layer=").append(getLayer());
        result.append(";");
        result.append("alternateGroup=").append(getAlternateGroup());
        result.append(";");
        result.append("volume=").append(getVolume());
        for (int i = 0; i < this.matrix.length; i++) {
            result.append(";");
            result.append("matrix").append(i).append("=").append(this.matrix[i]);
        }
        result.append(";");
        result.append("width=").append(getWidth());
        result.append(";");
        result.append("height=").append(getHeight());
        result.append("]");
        return result.toString();
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setModificationTime(long modificationTime) {
        this.modificationTime = modificationTime;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public void setAlternateGroup(int alternateGroup) {
        this.alternateGroup = alternateGroup;
    }

    public void setVolume(float volume) {
        this.volume = volume;
    }

    public void setMatrix(long[] matrix) {
        this.matrix = matrix;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public boolean isEnabled() {
        return (getFlags() & 1) > 0;
    }

    public boolean isInMovie() {
        return (getFlags() & 2) > 0;
    }

    public boolean isInPreview() {
        return (getFlags() & 4) > 0;
    }

    public boolean isInPoster() {
        return (getFlags() & 8) > 0;
    }
}
