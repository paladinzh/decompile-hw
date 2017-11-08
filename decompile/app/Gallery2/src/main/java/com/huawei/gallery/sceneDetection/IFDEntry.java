package com.huawei.gallery.sceneDetection;

/* compiled from: SceneInformationParser */
class IFDEntry {
    private int mCount = (((((this.mIFDEntryBuffer[(this.mIFDEntryOffset + 2) + 2] & 255) << 24) | ((this.mIFDEntryBuffer[((this.mIFDEntryOffset + 2) + 2) + 1] & 255) << 16)) | ((this.mIFDEntryBuffer[((this.mIFDEntryOffset + 2) + 2) + 2] & 255) << 8)) | (this.mIFDEntryBuffer[((this.mIFDEntryOffset + 2) + 2) + 3] & 255));
    private byte[] mIFDEntryBuffer;
    private int mIFDEntryOffset;
    private int mTag = (((this.mIFDEntryBuffer[this.mIFDEntryOffset] & 255) << 8) | (this.mIFDEntryBuffer[this.mIFDEntryOffset + 1] & 255));
    private int mType = (((this.mIFDEntryBuffer[this.mIFDEntryOffset + 2] & 255) << 8) | (this.mIFDEntryBuffer[(this.mIFDEntryOffset + 2) + 1] & 255));
    private int mValueOffset = (((((this.mIFDEntryBuffer[((this.mIFDEntryOffset + 2) + 2) + 4] & 255) << 24) | ((this.mIFDEntryBuffer[(((this.mIFDEntryOffset + 2) + 2) + 4) + 1] & 255) << 16)) | ((this.mIFDEntryBuffer[(((this.mIFDEntryOffset + 2) + 2) + 4) + 2] & 255) << 8)) | (this.mIFDEntryBuffer[(((this.mIFDEntryOffset + 2) + 2) + 4) + 3] & 255));

    public int getmValueOffset() {
        return this.mValueOffset;
    }

    public IFDEntry(byte[] ifdEntryBuffer, int offset) {
        this.mIFDEntryBuffer = ifdEntryBuffer;
        this.mIFDEntryOffset = offset;
    }

    public static boolean isIFDEntry(byte[] ifdEntryBuffer, int offset) {
        return ifdEntryBuffer.length - offset >= 12;
    }
}
