package com.googlecode.mp4parser.authoring;

import java.util.Date;
import tmsdk.common.module.update.UpdateConfig;

public class TrackMetaData implements Cloneable {
    private Date creationTime = new Date();
    private int group = 0;
    private double height;
    private String language;
    int layer;
    private long[] matrix = new long[]{UpdateConfig.UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL, 0, 0, 0, UpdateConfig.UPDATE_FLAG_WHITELIST_PERMISSION_CONTROL, 0, 0, 0, UpdateConfig.UPDATE_FLAG_VIRUS_BASE};
    private Date modificationTime = new Date();
    private long timescale;
    private long trackId = 1;
    private float volume;
    private double width;

    public String getLanguage() {
        return this.language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public long getTimescale() {
        return this.timescale;
    }

    public void setTimescale(long timescale) {
        this.timescale = timescale;
    }

    public void setModificationTime(Date modificationTime) {
        this.modificationTime = modificationTime;
    }

    public Date getCreationTime() {
        return this.creationTime;
    }

    public void setCreationTime(Date creationTime) {
        this.creationTime = creationTime;
    }

    public double getWidth() {
        return this.width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public long[] getMatrix() {
        return this.matrix;
    }

    public void setMatrix(long[] m) {
        this.matrix = m;
    }

    public double getHeight() {
        return this.height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public long getTrackId() {
        return this.trackId;
    }

    public void setTrackId(long trackId) {
        this.trackId = trackId;
    }

    public int getLayer() {
        return this.layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public float getVolume() {
        return this.volume;
    }

    public int getGroup() {
        return this.group;
    }

    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}
