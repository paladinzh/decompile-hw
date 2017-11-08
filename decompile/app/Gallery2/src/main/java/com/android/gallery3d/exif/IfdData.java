package com.android.gallery3d.exif;

import java.util.HashMap;
import java.util.Map;

class IfdData {
    private static final int[] sIfds = new int[]{0, 1, 2, 3, 4};
    private final short TAG_ID_MAKER_NOTE = (short) -28036;
    private final Map<Short, ExifTag> mExifTags = new HashMap();
    private final int mIfdId;
    private int mOffsetToNextIfd = 0;

    IfdData(int ifdId) {
        this.mIfdId = ifdId;
    }

    protected static int[] getIfds() {
        return sIfds;
    }

    protected ExifTag[] getAllTags() {
        return (ExifTag[]) this.mExifTags.values().toArray(new ExifTag[this.mExifTags.size()]);
    }

    protected int getId() {
        return this.mIfdId;
    }

    protected ExifTag getTag(short tagId) {
        return (ExifTag) this.mExifTags.get(Short.valueOf(tagId));
    }

    protected ExifTag setTag(ExifTag tag) {
        if (tag.getTagId() == (short) -28036) {
            String value = tag.getValueAsString();
            if (value != null && value.matches("^(####)(.|\\s)*")) {
                return null;
            }
        }
        tag.setIfd(this.mIfdId);
        return (ExifTag) this.mExifTags.put(Short.valueOf(tag.getTagId()), tag);
    }

    protected void removeTag(short tagId) {
        this.mExifTags.remove(Short.valueOf(tagId));
    }

    protected int getTagCount() {
        return this.mExifTags.size();
    }

    protected void setOffsetToNextIfd(int offset) {
        this.mOffsetToNextIfd = offset;
    }

    protected int getOffsetToNextIfd() {
        return this.mOffsetToNextIfd;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj != null && (obj instanceof IfdData)) {
            IfdData data = (IfdData) obj;
            if (data.getId() == this.mIfdId && data.getTagCount() == getTagCount()) {
                for (ExifTag tag : data.getAllTags()) {
                    if (!ExifInterface.isOffsetTag(tag.getTagId()) && !tag.equals((ExifTag) this.mExifTags.get(Short.valueOf(tag.getTagId())))) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }
}
