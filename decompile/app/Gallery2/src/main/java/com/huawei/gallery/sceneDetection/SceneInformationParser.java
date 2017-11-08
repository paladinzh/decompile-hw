package com.huawei.gallery.sceneDetection;

import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

public class SceneInformationParser {
    static final byte[] APP1_MARKER = new byte[]{(byte) -1, (byte) -31};
    static final byte[] MAKER_NOTE_TAG = new byte[]{(byte) -110, (byte) 124};
    static final int[] SHARPNESS_LOOKUP_TABLE = new int[]{5, 2, 0, 0};
    static final byte[] SOI_MARKER = new byte[]{(byte) -1, (byte) -40};
    private byte[] mImageBuffer;
    private String mImageFilePath;
    private boolean mIsCameraImage;
    private boolean mIsImageFileExists;
    private boolean mIsImageFilePathNull;
    private int mOffset;
    private int mSceneInformationOffset;
    private int mTiffHeaderOffset;

    SceneInformationParser(String imageFilePath) {
        boolean z;
        this.mImageFilePath = imageFilePath;
        if (this.mImageFilePath == null) {
            z = true;
        } else {
            z = false;
        }
        this.mIsImageFilePathNull = z;
        if (!this.mIsImageFilePathNull) {
            this.mIsImageFileExists = new File(imageFilePath).exists();
            if (this.mIsImageFileExists) {
                this.mIsCameraImage = isContainSceneInformationMakerNote();
            } else {
                this.mIsCameraImage = false;
            }
        }
    }

    public byte[] getSceneInfoMakerNote(int controllerBits, int sharpnessLevel) {
        if (this.mIsImageFilePathNull) {
            return getSceneInfoMakerNoteFromNullFile(controllerBits);
        }
        if (!this.mIsImageFileExists) {
            return new byte[0];
        }
        if (this.mIsCameraImage) {
            return getSceneInfoMakerNoteFromCameraImage(controllerBits);
        }
        return getSceneInfoMakerNoteFromOutsideSource(controllerBits, sharpnessLevel);
    }

    private byte[] getSceneInfoMakerNoteFromCameraImage(int controllerBits) {
        byte[] sceneInfoMakerNote = new byte[100];
        Arrays.fill(sceneInfoMakerNote, (byte) 0);
        try {
            System.arraycopy(this.mImageBuffer, this.mSceneInformationOffset, sceneInfoMakerNote, 0, 100);
            setDataInSceneInfo(controllerBits, sceneInfoMakerNote, 0);
        } catch (Throwable t) {
            GalleryLog.d("SceneInformationParser", "getSceneInfoMakerNote exception:" + t);
        }
        return sceneInfoMakerNote;
    }

    private byte[] getSceneInfoMakerNoteFromOutsideSource(int controllerBits, int sharpnessLevel) {
        byte[] sceneInfoMakerNote = new byte[100];
        Arrays.fill(sceneInfoMakerNote, (byte) 0);
        try {
            setDataInSceneInfo(controllerBits, sceneInfoMakerNote, 0);
            setDataInSceneInfo(-10, sceneInfoMakerNote, 4);
            int nightResult = convertSharpnessLevelToNightResult(sharpnessLevel);
            setDataInSceneInfo(nightResult, sceneInfoMakerNote, 28);
            setDataInSceneInfo(setNightBitInSdResult(nightResult, 0), sceneInfoMakerNote, 12);
            setDataInSceneInfo(getISO(), sceneInfoMakerNote, 32);
        } catch (Throwable t) {
            GalleryLog.d("SceneInformationParser", "getSceneInfoMakerNoteFromOutsideSource exception:" + t);
        }
        return sceneInfoMakerNote;
    }

    private byte[] getSceneInfoMakerNoteFromNullFile(int controllerBits) {
        byte[] sceneInfoMakerNote = new byte[100];
        Arrays.fill(sceneInfoMakerNote, (byte) 0);
        try {
            setDataInSceneInfo(controllerBits, sceneInfoMakerNote, 0);
            setDataInSceneInfo(-1, sceneInfoMakerNote, 28);
        } catch (Throwable t) {
            GalleryLog.d("SceneInformationParser", "getSceneInfoMakerNoteFromNullFile exception:" + t);
        }
        return sceneInfoMakerNote;
    }

    private int setNightBitInSdResult(int nightResult, int sdResult) {
        if (nightResult <= 0 || nightResult > 5) {
            return sdResult;
        }
        return sdResult | 8;
    }

    private int convertSharpnessLevelToNightResult(int sharpnessLevel) {
        if (sharpnessLevel < 0 || sharpnessLevel > 3) {
            return -1;
        }
        return SHARPNESS_LOOKUP_TABLE[sharpnessLevel];
    }

    private void setDataInSceneInfo(int data, byte[] sceneInfo, int offset) {
        if (sceneInfo.length - offset >= 4) {
            try {
                byte[] controllerBitsArray = convertIntToBytes(data);
                for (int currentIndex = 0; currentIndex < 4; currentIndex++) {
                    sceneInfo[offset + currentIndex] = controllerBitsArray[currentIndex];
                }
            } catch (IndexOutOfBoundsException e) {
                GalleryLog.e("SceneInformationParser", "Array index out of bounds in setDataInSceneInfo!");
            }
        }
    }

    private byte[] convertIntToBytes(int inputNum) {
        byte[] resultBytes = new byte[4];
        for (int currentByteCount = 0; currentByteCount < 4; currentByteCount++) {
            resultBytes[currentByteCount] = (byte) inputNum;
            inputNum >>= 8;
        }
        return resultBytes;
    }

    private boolean isContainSceneInformationMakerNote() {
        File file;
        Throwable th;
        FileInputStream inputStream = null;
        try {
            File imageFile = new File(this.mImageFilePath);
            try {
                FileInputStream fileInputStream = new FileInputStream(imageFile);
                try {
                    byte[] imageHeaderBuffer = new byte[((SOI_MARKER.length + APP1_MARKER.length) + 2)];
                    if (fileInputStream.read(imageHeaderBuffer) != imageHeaderBuffer.length) {
                        GalleryLog.d("SceneInformationParser", "Read in image data error.");
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e) {
                                GalleryLog.w("SceneInformationParser", "io error.");
                            }
                        }
                        return false;
                    }
                    this.mOffset = 0;
                    if (isJpegFile(imageHeaderBuffer, this.mOffset) && isContainApp1(imageHeaderBuffer, this.mOffset + SOI_MARKER.length)) {
                        this.mOffset += SOI_MARKER.length + APP1_MARKER.length;
                        int app1Length = ((imageHeaderBuffer[this.mOffset] & 255) << 8) | (imageHeaderBuffer[this.mOffset + 1] & 255);
                        this.mOffset += 2;
                        this.mImageBuffer = new byte[(SOI_MARKER.length + app1Length)];
                        System.arraycopy(imageHeaderBuffer, 0, this.mImageBuffer, 0, imageHeaderBuffer.length);
                        if (fileInputStream.read(this.mImageBuffer, imageHeaderBuffer.length, this.mImageBuffer.length - imageHeaderBuffer.length) != this.mImageBuffer.length - imageHeaderBuffer.length) {
                            GalleryLog.d("SceneInformationParser", "Read in image data error.");
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2) {
                                    GalleryLog.w("SceneInformationParser", "io error.");
                                }
                            }
                            return false;
                        }
                        this.mOffset += 6;
                        this.mTiffHeaderOffset = this.mOffset;
                        if (this.mImageBuffer[this.mOffset] == (byte) 77 && this.mImageBuffer[this.mOffset + 1] == (byte) 77) {
                            this.mOffset += 8;
                            int ifdEntryNum = ((this.mImageBuffer[this.mOffset] & 255) << 8) | (this.mImageBuffer[this.mOffset + 1] & 255);
                            this.mOffset += 2;
                            int exifIFDOffset = -1;
                            int count = 0;
                            while (count < ifdEntryNum) {
                                if (ExifIFDPointer.isExifIFDPointer(this.mImageBuffer, this.mOffset)) {
                                    exifIFDOffset = new ExifIFDPointer(this.mImageBuffer, this.mOffset).getmValueOffset();
                                    break;
                                }
                                count++;
                                this.mOffset += 12;
                            }
                            if (exifIFDOffset == -1) {
                                GalleryLog.d("SceneInformationParser", "Input image file doesn't contain EXIF IFD.");
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e3) {
                                        GalleryLog.w("SceneInformationParser", "io error.");
                                    }
                                }
                                return false;
                            }
                            this.mOffset = this.mTiffHeaderOffset + exifIFDOffset;
                            int exifIFDEntryNum = ((this.mImageBuffer[this.mOffset] & 255) << 8) | (this.mImageBuffer[this.mOffset + 1] & 255);
                            this.mOffset += 2;
                            count = 0;
                            while (count < exifIFDEntryNum) {
                                if (MakerNoteIFDEntry.isSceneInfoMakerNoteIFDEntry(this.mImageBuffer, this.mOffset, this.mTiffHeaderOffset)) {
                                    this.mSceneInformationOffset = new MakerNoteIFDEntry(this.mImageBuffer, this.mOffset).getmValueOffset() + this.mTiffHeaderOffset;
                                    this.mIsCameraImage = true;
                                    if (fileInputStream != null) {
                                        try {
                                            fileInputStream.close();
                                        } catch (IOException e4) {
                                            GalleryLog.w("SceneInformationParser", "io error.");
                                        }
                                    }
                                    return true;
                                }
                                count++;
                                this.mOffset += 12;
                            }
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e5) {
                                    GalleryLog.w("SceneInformationParser", "io error.");
                                }
                            }
                            inputStream = fileInputStream;
                            return false;
                        }
                        GalleryLog.d("SceneInformationParser", "Bigedian is not supported.");
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e6) {
                                GalleryLog.w("SceneInformationParser", "io error.");
                            }
                        }
                        return false;
                    }
                    GalleryLog.d("SceneInformationParser", "Input file is not jpeg file or does not contain APP1.");
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e7) {
                            GalleryLog.w("SceneInformationParser", "io error.");
                        }
                    }
                    return false;
                } catch (FileNotFoundException e8) {
                    inputStream = fileInputStream;
                    file = imageFile;
                } catch (IOException e9) {
                    inputStream = fileInputStream;
                    file = imageFile;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = fileInputStream;
                }
            } catch (FileNotFoundException e10) {
                try {
                    GalleryLog.w("SceneInformationParser", "file not found.");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e11) {
                            GalleryLog.w("SceneInformationParser", "io error.");
                        }
                    }
                    return false;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e12) {
                            GalleryLog.w("SceneInformationParser", "io error.");
                        }
                    }
                    throw th;
                }
            } catch (IOException e13) {
                GalleryLog.w("SceneInformationParser", "io error.");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e14) {
                        GalleryLog.w("SceneInformationParser", "io error.");
                    }
                }
                return false;
            } catch (Throwable th4) {
                th = th4;
                file = imageFile;
                if (inputStream != null) {
                    inputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e15) {
            GalleryLog.w("SceneInformationParser", "file not found.");
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        } catch (IOException e16) {
            GalleryLog.w("SceneInformationParser", "io error.");
            if (inputStream != null) {
                inputStream.close();
            }
            return false;
        }
    }

    private boolean isJpegFile(byte[] imageHeaderBuffer, int offset) {
        if (imageHeaderBuffer.length - offset < SOI_MARKER.length) {
            return false;
        }
        int curInd = 0;
        while (curInd < SOI_MARKER.length) {
            if (imageHeaderBuffer[offset] != SOI_MARKER[curInd]) {
                return false;
            }
            curInd++;
            offset++;
        }
        return true;
    }

    private boolean isContainApp1(byte[] imageHeaderBuffer, int offset) {
        if (imageHeaderBuffer.length - offset < APP1_MARKER.length) {
            return false;
        }
        int curInd = 0;
        while (curInd < APP1_MARKER.length) {
            if (imageHeaderBuffer[offset] != APP1_MARKER[curInd]) {
                return false;
            }
            curInd++;
            offset++;
        }
        return true;
    }

    private int getISO() {
        int iso = 0;
        try {
            ExifInterface exifInterface = new ExifInterface();
            exifInterface.readExif(this.mImageFilePath);
            iso = exifInterface.getTagIntValue(ExifInterface.TAG_ISO_SPEED_RATINGS).intValue();
        } catch (FileNotFoundException e) {
            GalleryLog.d("SceneInformationParser", "File not found error.");
        } catch (IOException e2) {
            GalleryLog.d("SceneInformationParser", "File format is not JPEG.");
        } catch (Exception e3) {
            GalleryLog.d("SceneInformationParser", "JPEG file does not contain ISO.");
        }
        return iso;
    }
}
