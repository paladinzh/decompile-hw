package com.huawei.gallery.sceneDetection;

/* compiled from: SceneInformationParser */
class MakerNoteIFDEntry extends IFDEntry {
    static final byte[] MAKER_NOTE_IFD_ENTRY_TAG = new byte[]{(byte) -110, (byte) 124};
    static final byte[] SCENE_INFO_FLAG = new byte[]{(byte) 35, (byte) 35, (byte) 35, (byte) 35};

    public MakerNoteIFDEntry(byte[] ifdEntryBuffer, int offset) {
        super(ifdEntryBuffer, offset);
    }

    public static boolean isSceneInfoMakerNoteIFDEntry(byte[] ifdEntryBuffer, int offset, int tiffHeaderOffset) {
        if (!IFDEntry.isIFDEntry(ifdEntryBuffer, offset)) {
            return false;
        }
        int curInd = 0;
        while (curInd < MAKER_NOTE_IFD_ENTRY_TAG.length) {
            if (ifdEntryBuffer[offset] != MAKER_NOTE_IFD_ENTRY_TAG[curInd]) {
                return false;
            }
            curInd++;
            offset++;
        }
        short type = (short) (((ifdEntryBuffer[offset] & 255) << 8) | (ifdEntryBuffer[offset + 1] & 255));
        offset += 2;
        if (type != (short) 7) {
            return false;
        }
        int count = ((((ifdEntryBuffer[offset] & 255) << 24) | ((ifdEntryBuffer[offset + 1] & 255) << 16)) | ((ifdEntryBuffer[offset + 2] & 255) << 8)) | (ifdEntryBuffer[offset + 3] & 255);
        offset += 4;
        if (count <= 4) {
            return false;
        }
        offset = tiffHeaderOffset + (((((ifdEntryBuffer[offset] & 255) << 24) | ((ifdEntryBuffer[offset + 1] & 255) << 16)) | ((ifdEntryBuffer[offset + 2] & 255) << 8)) | (ifdEntryBuffer[offset + 3] & 255));
        curInd = 0;
        while (curInd < SCENE_INFO_FLAG.length) {
            if (ifdEntryBuffer[offset] != SCENE_INFO_FLAG[curInd]) {
                return false;
            }
            curInd++;
            offset++;
        }
        return true;
    }
}
