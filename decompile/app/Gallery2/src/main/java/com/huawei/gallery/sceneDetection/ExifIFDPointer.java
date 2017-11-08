package com.huawei.gallery.sceneDetection;

/* compiled from: SceneInformationParser */
class ExifIFDPointer extends IFDEntry {
    static final byte[] EXIF_IFD_POINTER_TAG = new byte[]{(byte) -121, (byte) 105};

    public ExifIFDPointer(byte[] ifdEntryBuffer, int offset) {
        super(ifdEntryBuffer, offset);
    }

    public static boolean isExifIFDPointer(byte[] ifdEntryBuffer, int offset) {
        if (!IFDEntry.isIFDEntry(ifdEntryBuffer, offset)) {
            return false;
        }
        int curInd = 0;
        while (curInd < EXIF_IFD_POINTER_TAG.length) {
            if (ifdEntryBuffer[offset] != EXIF_IFD_POINTER_TAG[curInd]) {
                return false;
            }
            curInd++;
            offset++;
        }
        return true;
    }
}
