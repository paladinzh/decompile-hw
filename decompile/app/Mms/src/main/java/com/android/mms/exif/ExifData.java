package com.android.mms.exif;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

class ExifData {
    private static final byte[] USER_COMMENT_ASCII = new byte[]{(byte) 65, (byte) 83, (byte) 67, (byte) 73, (byte) 73, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_JIS = new byte[]{(byte) 74, (byte) 73, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_UNICODE = new byte[]{(byte) 85, (byte) 78, (byte) 73, (byte) 67, (byte) 79, (byte) 68, (byte) 69, (byte) 0};
    private final ByteOrder mByteOrder;
    private final IfdData[] mIfdDatas = new IfdData[5];
    private final ArrayList<byte[]> mStripBytes = new ArrayList();
    private byte[] mThumbnail;

    ExifData(ByteOrder order) {
        this.mByteOrder = order;
    }

    protected byte[] getCompressedThumbnail() {
        return this.mThumbnail;
    }

    protected void setCompressedThumbnail(byte[] thumbnail) {
        this.mThumbnail = thumbnail;
    }

    protected boolean hasCompressedThumbnail() {
        return this.mThumbnail != null;
    }

    protected void setStripBytes(int index, byte[] strip) {
        if (index < this.mStripBytes.size()) {
            this.mStripBytes.set(index, strip);
            return;
        }
        for (int i = this.mStripBytes.size(); i < index; i++) {
            this.mStripBytes.add(null);
        }
        this.mStripBytes.add(strip);
    }

    protected int getStripCount() {
        return this.mStripBytes.size();
    }

    protected byte[] getStrip(int index) {
        return (byte[]) this.mStripBytes.get(index);
    }

    protected boolean hasUncompressedStrip() {
        return this.mStripBytes.size() != 0;
    }

    protected ByteOrder getByteOrder() {
        return this.mByteOrder;
    }

    protected IfdData getIfdData(int ifdId) {
        if (ExifTag.isValidIfd(ifdId)) {
            return this.mIfdDatas[ifdId];
        }
        return null;
    }

    protected void addIfdData(IfdData data) {
        this.mIfdDatas[data.getId()] = data;
    }

    protected IfdData getOrCreateIfdData(int ifdId) {
        IfdData ifdData = this.mIfdDatas[ifdId];
        if (ifdData != null) {
            return ifdData;
        }
        ifdData = new IfdData(ifdId);
        this.mIfdDatas[ifdId] = ifdData;
        return ifdData;
    }

    protected ExifTag getTag(short tag, int ifd) {
        IfdData ifdData = this.mIfdDatas[ifd];
        if (ifdData == null) {
            return null;
        }
        return ifdData.getTag(tag);
    }

    protected ExifTag addTag(ExifTag tag) {
        if (tag != null) {
            return addTag(tag, tag.getIfd());
        }
        return null;
    }

    protected ExifTag addTag(ExifTag tag, int ifdId) {
        if (tag == null || !ExifTag.isValidIfd(ifdId)) {
            return null;
        }
        return getOrCreateIfdData(ifdId).setTag(tag);
    }

    protected void clearThumbnailAndStrips() {
        this.mThumbnail = null;
        this.mStripBytes.clear();
    }

    protected void removeTag(short tagId, int ifdId) {
        IfdData ifdData = this.mIfdDatas[ifdId];
        if (ifdData != null) {
            ifdData.removeTag(tagId);
        }
    }

    protected List<ExifTag> getAllTags() {
        ArrayList<ExifTag> ret = new ArrayList();
        for (IfdData d : this.mIfdDatas) {
            if (d != null) {
                ExifTag[] tags = d.getAllTags();
                if (tags != null) {
                    for (ExifTag t : tags) {
                        ret.add(t);
                    }
                }
            }
        }
        if (ret.size() == 0) {
            return null;
        }
        return ret;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || !(obj instanceof ExifData)) {
            return false;
        }
        ExifData data = (ExifData) obj;
        if (data.mByteOrder != this.mByteOrder || data.mStripBytes.size() != this.mStripBytes.size() || !Arrays.equals(data.mThumbnail, this.mThumbnail)) {
            return false;
        }
        int i;
        for (i = 0; i < this.mStripBytes.size(); i++) {
            if (!Arrays.equals((byte[]) data.mStripBytes.get(i), (byte[]) this.mStripBytes.get(i))) {
                return false;
            }
        }
        for (i = 0; i < 5; i++) {
            IfdData ifd1 = data.getIfdData(i);
            IfdData ifd2 = getIfdData(i);
            if (ifd1 != ifd2 && ifd1 != null && !ifd1.equals(ifd2)) {
                return false;
            }
        }
        return true;
    }

    public int hashCode() {
        StringBuilder builder = new StringBuilder();
        builder.append(this.mStripBytes);
        return builder.hashCode();
    }
}
