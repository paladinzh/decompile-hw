package com.huawei.keyguard.third.exif;

import com.huawei.keyguard.util.HwLog;
import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;

class ExifData {
    private static final byte[] USER_COMMENT_ASCII = new byte[]{(byte) 65, (byte) 83, (byte) 67, (byte) 73, (byte) 73, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_JIS = new byte[]{(byte) 74, (byte) 73, (byte) 83, (byte) 0, (byte) 0, (byte) 0, (byte) 0, (byte) 0};
    private static final byte[] USER_COMMENT_UNICODE = new byte[]{(byte) 85, (byte) 78, (byte) 73, (byte) 67, (byte) 79, (byte) 68, (byte) 69, (byte) 0};
    private static final byte[] USER_COMMENT_UTF8 = new byte[]{(byte) 85, (byte) 84, (byte) 70, (byte) 45, (byte) 56, (byte) 32, (byte) 32, (byte) 32};
    private final ByteOrder mByteOrder;
    private final IfdData[] mIfdDatas = new IfdData[5];
    private ArrayList<byte[]> mStripBytes = new ArrayList();
    private byte[] mThumbnail;

    ExifData(ByteOrder order) {
        this.mByteOrder = order;
    }

    protected void setCompressedThumbnail(byte[] thumbnail) {
        this.mThumbnail = thumbnail;
    }

    protected void setStripBytes(int index, byte[] strip) {
        if (index < 0) {
            HwLog.w("ExifData", "setStripBytes fail index = " + index);
            return;
        }
        if (index < this.mStripBytes.size()) {
            this.mStripBytes.set(index, strip);
        } else {
            for (int i = this.mStripBytes.size(); i < index; i++) {
                this.mStripBytes.add(null);
            }
            this.mStripBytes.add(strip);
        }
    }

    protected IfdData getIfdData(int ifdId) {
        if (ExifTag.isValidIfd(ifdId)) {
            return this.mIfdDatas[ifdId];
        }
        return null;
    }

    protected void addIfdData(IfdData data) {
        int id = data.getId();
        if (ExifTag.isValidIfd(id)) {
            this.mIfdDatas[id] = data;
        }
    }

    protected ExifTag getTag(short tag, int ifd) {
        ExifTag exifTag = null;
        if (!ExifTag.isValidIfd(ifd)) {
            return null;
        }
        IfdData ifdData = this.mIfdDatas[ifd];
        if (ifdData != null) {
            exifTag = ifdData.getTag(tag);
        }
        return exifTag;
    }

    protected String getUserComment() {
        IfdData ifdData = this.mIfdDatas[2];
        if (ifdData == null) {
            HwLog.w("ExifData", "The ifdData is null error!");
            return null;
        }
        ExifTag tag = ifdData.getTag(ExifInterface.getTrueTagKey(ExifInterface.TAG_USER_COMMENT));
        if (tag == null) {
            HwLog.w("ExifData", "The tag is null error!");
            return null;
        } else if (tag.getComponentCount() < 8) {
            HwLog.w("ExifData", "The tag.getComponentCount() is: " + tag.getComponentCount());
            return null;
        } else {
            byte[] buf = new byte[tag.getComponentCount()];
            tag.getBytes(buf);
            byte[] code = new byte[8];
            System.arraycopy(buf, 0, code, 0, 8);
            try {
                if (Arrays.equals(code, USER_COMMENT_ASCII)) {
                    return new String(buf, 8, buf.length - 8, "US-ASCII");
                }
                if (Arrays.equals(code, USER_COMMENT_JIS)) {
                    return new String(buf, 8, buf.length - 8, "EUC-JP");
                }
                if (Arrays.equals(code, USER_COMMENT_UNICODE)) {
                    return new String(buf, 8, buf.length - 8, "UTF-16");
                }
                if (Arrays.equals(code, USER_COMMENT_UTF8)) {
                    return new String(buf, 8, buf.length - 8, "UTF-8");
                }
                HwLog.w("ExifData", "The user comment code is wrong!");
                return null;
            } catch (UnsupportedEncodingException e) {
                HwLog.w("ExifData", "Failed to decode the user comment");
                return null;
            }
        }
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
        return super.hashCode();
    }
}
