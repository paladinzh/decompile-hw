package com.huawei.gallery.refocus.allfocus.app;

import android.graphics.Point;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.exif.ExifTag;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.ui.BitmapScreenNail;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.refocus.app.AbsRefocusPhoto;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeMap;

public class AllFocusPhotoImpl extends AbsRefocusPhoto {
    private int mCRC;
    private boolean mCancelPrepareThread;
    private ArrayList<byte[]> mClarityTable;
    private volatile int mCurrentFoused;
    private int mExifOrientation;
    private int[] mFocalLengthList;
    private int mFromFocalLength;
    private Byte mGridHeight;
    private Byte mGridWidth;
    private AllFocusPhotoListener mListener;
    private Object mLock;
    private byte[] mOrientationArray;
    private int[] mPhotoLen;
    private ArrayList<byte[]> mPhotoList;
    private Byte mPhotoNum;
    private int mRefocusState;
    private String mSaveAsPath;
    private Thread mSaveFileThread;
    private TreeMap<Integer, BitmapScreenNail> mScreenNailAndFocalLength;
    private boolean mSupportFocalLength;

    public interface AllFocusPhotoListener {
        void onGotFocusPoint();

        void onPrepareComplete();

        void onSaveAsComplete(int i, String str);

        void onSaveFileComplete(int i);
    }

    AllFocusPhotoImpl(String filePath, int photoWidth, int photoHeight) {
        super(filePath, photoWidth, photoHeight);
        this.mLock = new Object();
        this.mIsRefocusPhoto = false;
        this.mCancelPrepareThread = false;
        setRefocusState(1);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean prepare() {
        long currentTimeInMills = System.currentTimeMillis();
        if (this.mListener == null) {
            GalleryLog.e("AllFocusPhotoImpl", "please register listener first!");
            return false;
        }
        synchronized (this.mLock) {
            if (getRefocusState() != 1) {
                this.mListener.onPrepareComplete();
                return false;
            }
            setRefocusState(2);
        }
    }

    public void cancelPrepare() {
        this.mCancelPrepareThread = true;
    }

    public final byte[] getPhotoData(int index) {
        if (index < 0 || index > this.mPhotoNum.byteValue() - 1) {
            return new byte[0];
        }
        if (this.mPhotoList == null || this.mPhotoList.isEmpty()) {
            return new byte[0];
        }
        return (byte[]) this.mPhotoList.get(0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final int getPhotoLength(int index) {
        if (index < 0 || index > this.mPhotoNum.byteValue() - 1 || this.mPhotoLen == null) {
            return 0;
        }
        return this.mPhotoLen[0];
    }

    public boolean isRefocusPhoto() {
        return this.mIsRefocusPhoto;
    }

    public Point getFocusPoint() {
        Point focusPoint = new Point(this.mFocusPoint);
        if (focusPoint.x == -1 || focusPoint.y == -1) {
            return focusPoint;
        }
        focusPoint = transformToPhotoCoordinate(focusPoint, this.mOrientation + this.mExifOrientation);
        GalleryLog.i("AllFocusPhotoImpl", "getFocusPoint: " + focusPoint.x + ", " + focusPoint.y);
        return focusPoint;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int doRefocus(Point refocusPoint) {
        synchronized (this.mLock) {
            if (getRefocusState() != 1) {
                return 0;
            }
            setRefocusState(4);
        }
    }

    private int save() {
        Object obj;
        Throwable th;
        Closeable closeable = null;
        File dstFile = new File(this.mFileName);
        String tmpFileName = dstFile.getParent() + File.separator + "AllFocusPhoto.data";
        File tmpFile = new File(tmpFileName);
        long currentTimeInMills = System.currentTimeMillis();
        try {
            RandomAccessFile fileWriter = new RandomAccessFile(tmpFileName, "rws");
            byte i = (byte) 0;
            while (i < this.mPhotoNum.byteValue() && !Thread.currentThread().isInterrupted()) {
                try {
                    fileWriter.write((byte[]) this.mPhotoList.get(i));
                    i++;
                } catch (IOException e) {
                    obj = fileWriter;
                } catch (Throwable th2) {
                    th = th2;
                    obj = fileWriter;
                }
            }
            if (!Thread.currentThread().isInterrupted()) {
                fileWriter.writeInt(this.mCRC);
                fileWriter.write(this.mOrientationArray);
            }
            for (i = (byte) 0; i < this.mPhotoNum.byteValue() && !Thread.currentThread().isInterrupted(); i++) {
                fileWriter.write((byte[]) this.mClarityTable.get(i));
            }
            if (this.mSupportFocalLength && !Thread.currentThread().isInterrupted()) {
                byte[] focalLength = new byte[4];
                fileWriter.write("V001".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
                for (i = (byte) 0; i < this.mPhotoNum.byteValue() && !Thread.currentThread().isInterrupted(); i++) {
                    AbsRefocusPhoto.intToByteArray(this.mFocalLengthList[i], focalLength, 0);
                    fileWriter.write(focalLength);
                }
            }
            for (i = (byte) 0; i < this.mPhotoNum.byteValue() && !Thread.currentThread().isInterrupted(); i++) {
                fileWriter.writeInt(this.mPhotoLen[i]);
            }
            if (!Thread.currentThread().isInterrupted()) {
                fileWriter.writeByte(this.mGridWidth.byteValue());
                fileWriter.writeByte(this.mGridHeight.byteValue());
                fileWriter.writeByte(this.mPhotoNum.byteValue());
                fileWriter.writeInt(this.mFocusPoint.x);
                fileWriter.writeInt(this.mFocusPoint.y);
                fileWriter.write("Refocus".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
            }
            fileWriter.close();
            closeable = null;
            if (Thread.currentThread().isInterrupted() || !tmpFile.renameTo(dstFile)) {
                AbsRefocusPhoto.closeSilently(null);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                return -2;
            }
            AbsRefocusPhoto.closeSilently(null);
            if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                tmpFile.deleteOnExit();
            }
            GalleryLog.i("AllFocusPhotoImpl", "Save() cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
            return 0;
        } catch (IOException e2) {
            try {
                GalleryLog.i("AllFocusPhotoImpl", "save() failed because of no space.");
                AbsRefocusPhoto.closeSilently(closeable);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                return -1;
            } catch (Throwable th3) {
                th = th3;
                AbsRefocusPhoto.closeSilently(closeable);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                throw th;
            }
        }
    }

    public int saveAs(String filePath) {
        synchronized (this.mLock) {
            if (getRefocusState() != 1) {
                return 1;
            }
            setRefocusState(8);
            this.mSaveAsPath = filePath;
            this.mSaveFileThread = new Thread(new Runnable() {
                public void run() {
                    IOException e;
                    Object obj;
                    Exception e2;
                    Throwable th;
                    int retVal = -2;
                    Closeable closeable = null;
                    long currentTimeInMills = System.currentTimeMillis();
                    GalleryLog.i("AllFocusPhotoImpl", "do saveas begin");
                    try {
                        RandomAccessFile newFile = new RandomAccessFile(AllFocusPhotoImpl.this.mSaveAsPath, "rws");
                        try {
                            newFile.write((byte[]) AllFocusPhotoImpl.this.mPhotoList.get(0));
                            AllFocusPhotoImpl.this.mListener.onSaveAsComplete(0, AllFocusPhotoImpl.this.mSaveAsPath);
                            AbsRefocusPhoto.closeSilently(newFile);
                            RandomAccessFile randomAccessFile = newFile;
                        } catch (IOException e3) {
                            e = e3;
                            obj = newFile;
                            GalleryLog.i("AllFocusPhotoImpl", "saveAs() failed, reason: IOException." + e.getMessage());
                            AllFocusPhotoImpl.this.mListener.onSaveAsComplete(-1, AllFocusPhotoImpl.this.mSaveAsPath);
                            AbsRefocusPhoto.closeSilently(closeable);
                            GalleryLog.i("AllFocusPhotoImpl", "do saveas end");
                            GalleryLog.i("AllFocusPhotoImpl", "saveAs cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
                            if (!Thread.currentThread().isInterrupted()) {
                                retVal = AllFocusPhotoImpl.this.save();
                            }
                            AllFocusPhotoImpl.this.mListener.onSaveFileComplete(retVal);
                            synchronized (AllFocusPhotoImpl.this.mLock) {
                                AllFocusPhotoImpl.this.setRefocusState(1);
                            }
                        } catch (Exception e4) {
                            e2 = e4;
                            obj = newFile;
                            try {
                                GalleryLog.i("AllFocusPhotoImpl", "saveAs() failed." + e2.getMessage());
                                AllFocusPhotoImpl.this.mListener.onSaveAsComplete(-2, AllFocusPhotoImpl.this.mSaveAsPath);
                                AbsRefocusPhoto.closeSilently(closeable);
                                GalleryLog.i("AllFocusPhotoImpl", "do saveas end");
                                GalleryLog.i("AllFocusPhotoImpl", "saveAs cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
                                if (Thread.currentThread().isInterrupted()) {
                                    retVal = AllFocusPhotoImpl.this.save();
                                }
                                AllFocusPhotoImpl.this.mListener.onSaveFileComplete(retVal);
                                synchronized (AllFocusPhotoImpl.this.mLock) {
                                    AllFocusPhotoImpl.this.setRefocusState(1);
                                }
                            } catch (Throwable th2) {
                                th = th2;
                                AbsRefocusPhoto.closeSilently(closeable);
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            obj = newFile;
                            AbsRefocusPhoto.closeSilently(closeable);
                            throw th;
                        }
                    } catch (IOException e5) {
                        e = e5;
                        GalleryLog.i("AllFocusPhotoImpl", "saveAs() failed, reason: IOException." + e.getMessage());
                        AllFocusPhotoImpl.this.mListener.onSaveAsComplete(-1, AllFocusPhotoImpl.this.mSaveAsPath);
                        AbsRefocusPhoto.closeSilently(closeable);
                        GalleryLog.i("AllFocusPhotoImpl", "do saveas end");
                        GalleryLog.i("AllFocusPhotoImpl", "saveAs cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
                        if (Thread.currentThread().isInterrupted()) {
                            retVal = AllFocusPhotoImpl.this.save();
                        }
                        AllFocusPhotoImpl.this.mListener.onSaveFileComplete(retVal);
                        synchronized (AllFocusPhotoImpl.this.mLock) {
                            AllFocusPhotoImpl.this.setRefocusState(1);
                        }
                    } catch (Exception e6) {
                        e2 = e6;
                        GalleryLog.i("AllFocusPhotoImpl", "saveAs() failed." + e2.getMessage());
                        AllFocusPhotoImpl.this.mListener.onSaveAsComplete(-2, AllFocusPhotoImpl.this.mSaveAsPath);
                        AbsRefocusPhoto.closeSilently(closeable);
                        GalleryLog.i("AllFocusPhotoImpl", "do saveas end");
                        GalleryLog.i("AllFocusPhotoImpl", "saveAs cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
                        if (Thread.currentThread().isInterrupted()) {
                            retVal = AllFocusPhotoImpl.this.save();
                        }
                        AllFocusPhotoImpl.this.mListener.onSaveFileComplete(retVal);
                        synchronized (AllFocusPhotoImpl.this.mLock) {
                            AllFocusPhotoImpl.this.setRefocusState(1);
                        }
                    }
                    GalleryLog.i("AllFocusPhotoImpl", "do saveas end");
                    GalleryLog.i("AllFocusPhotoImpl", "saveAs cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
                    if (Thread.currentThread().isInterrupted()) {
                        retVal = AllFocusPhotoImpl.this.save();
                    }
                    AllFocusPhotoImpl.this.mListener.onSaveFileComplete(retVal);
                    synchronized (AllFocusPhotoImpl.this.mLock) {
                        AllFocusPhotoImpl.this.setRefocusState(1);
                    }
                }
            });
            this.mSaveFileThread.start();
            return 0;
        }
    }

    public void saveFile() {
        synchronized (this.mLock) {
            if (getRefocusState() != 1) {
                return;
            }
            setRefocusState(8);
            this.mSaveFileThread = new Thread(new Runnable() {
                public void run() {
                    AllFocusPhotoImpl.this.mListener.onSaveFileComplete(AllFocusPhotoImpl.this.save());
                    synchronized (AllFocusPhotoImpl.this.mLock) {
                        AllFocusPhotoImpl.this.setRefocusState(1);
                    }
                }
            });
            this.mSaveFileThread.start();
        }
    }

    public void cleanupResource() {
        try {
            if (this.mSaveFileThread != null) {
                this.mSaveFileThread.join(5000);
                if (this.mSaveFileThread.isAlive()) {
                    this.mSaveFileThread.interrupt();
                    this.mSaveFileThread.join();
                }
            }
        } catch (InterruptedException e) {
            GalleryLog.i("AllFocusPhotoImpl", "Thread.join() failed in cleanupResource() method, reason: InterruptedException.");
        }
        this.mPhotoLen = null;
        if (this.mPhotoList != null) {
            this.mPhotoList.clear();
        }
        if (this.mClarityTable != null) {
            this.mClarityTable.clear();
        }
        this.mPhotoList = null;
        this.mClarityTable = null;
        if (this.mScreenNailAndFocalLength != null) {
            for (Object obj : this.mScreenNailAndFocalLength.keySet()) {
                BitmapScreenNail screenNail = (BitmapScreenNail) this.mScreenNailAndFocalLength.get(obj);
                if (screenNail != null) {
                    screenNail.getBitmap().recycle();
                }
            }
            this.mScreenNailAndFocalLength.clear();
            this.mScreenNailAndFocalLength = null;
        }
        this.mSupportFocalLength = false;
        this.mFocusPoint = null;
        closeFile();
    }

    public void setAllFocusPhotoListener(AllFocusPhotoListener listener) {
        this.mListener = listener;
    }

    public int getExifOrientation() {
        return this.mExifOrientation;
    }

    public boolean isSupportAdjustFocus() {
        return this.mSupportFocalLength;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int pickPhotoByFocalLength(int direction) {
        synchronized (this.mLock) {
            if (getRefocusState() != 1) {
                return 0;
            }
            setRefocusState(4);
        }
    }

    private int compareClarity(Point touchPoint) {
        long value = 0;
        int index = 0;
        long currentTimeInMills = System.currentTimeMillis();
        for (byte i = (byte) 0; i < this.mPhotoNum.byteValue(); i++) {
            long tmpValue = intToUnsingedInt(GalleryUtils.littleEdianByteArrayToInt((byte[]) this.mClarityTable.get(i), getBlockIndex(touchPoint) * 4, 4));
            if (tmpValue > value) {
                value = tmpValue;
                index = i;
            }
        }
        GalleryLog.i("AllFocusPhotoImpl", "touch point in photo coordinate is x:" + touchPoint.x + ", y:" + touchPoint.y);
        GalleryLog.i("AllFocusPhotoImpl", "touch area index is " + index);
        GalleryLog.i("AllFocusPhotoImpl", "compareClarity() cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
        return index;
    }

    private void pickPhoto(int refocusedPhoto) {
        long currentTimeInMills = System.currentTimeMillis();
        byte[] byteArray = (byte[]) this.mPhotoList.get(refocusedPhoto);
        int tmpValue = this.mPhotoLen[refocusedPhoto];
        this.mPhotoList.set(refocusedPhoto, (byte[]) this.mPhotoList.get(0));
        this.mPhotoLen[refocusedPhoto] = this.mPhotoLen[0];
        this.mPhotoList.set(0, byteArray);
        this.mPhotoLen[0] = tmpValue;
        byteArray = (byte[]) this.mClarityTable.get(refocusedPhoto);
        this.mClarityTable.set(refocusedPhoto, (byte[]) this.mClarityTable.get(0));
        this.mClarityTable.set(0, byteArray);
        if (this.mSupportFocalLength) {
            this.mFromFocalLength = this.mFocalLengthList[0];
            this.mFocalLengthList[0] = this.mFocalLengthList[refocusedPhoto];
            this.mFocalLengthList[refocusedPhoto] = this.mFromFocalLength;
        }
        GalleryLog.i("AllFocusPhotoImpl", "refocusedPhoto = " + refocusedPhoto);
        GalleryLog.i("AllFocusPhotoImpl", "pickPhoto() cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
    }

    public Collection<BitmapScreenNail> getTransitionBitmap() {
        if (!this.mSupportFocalLength) {
            return null;
        }
        int toFocalLength = this.mFocalLengthList[0];
        if (this.mFromFocalLength == toFocalLength) {
            return null;
        }
        Collection<BitmapScreenNail> bitmapCollection;
        if (this.mFromFocalLength > toFocalLength) {
            bitmapCollection = this.mScreenNailAndFocalLength.subMap(Integer.valueOf(toFocalLength), true, Integer.valueOf(this.mFromFocalLength), false).descendingMap().values();
        } else {
            bitmapCollection = this.mScreenNailAndFocalLength.subMap(Integer.valueOf(this.mFromFocalLength), false, Integer.valueOf(toFocalLength), true).values();
        }
        return bitmapCollection;
    }

    private void setRefocusState(int state) {
        this.mRefocusState = state;
    }

    private int getRefocusState() {
        return this.mRefocusState;
    }

    private boolean openFile() {
        try {
            if (this.mFile != null) {
                closeFile();
            }
            this.mFile = new RandomAccessFile(this.mFileName, "rws");
            this.mFileLen = (int) this.mFile.length();
        } catch (IOException e) {
            GalleryLog.i("AllFocusPhotoImpl", "openFile() failed, reason: IOException." + e.getMessage());
        } catch (Exception e2) {
            GalleryLog.i("AllFocusPhotoImpl", "openFile() failed." + e2.getMessage());
        }
        return this.mFile != null;
    }

    private void closeFile() {
        if (this.mFile != null) {
            try {
                this.mFile.close();
                this.mFile = null;
            } catch (IOException e) {
                GalleryLog.i("AllFocusPhotoImpl", "closeFile() failed, reason: IOException." + e.getMessage());
            } catch (Exception e2) {
                GalleryLog.i("AllFocusPhotoImpl", "closeFile() failed." + e2.getMessage());
            }
        }
    }

    private int getBlockIndex(Point touchPoint) {
        int index;
        long currentTimeInMills = System.currentTimeMillis();
        int blockIndexH = touchPoint.x / (this.mPhotoWidth / this.mGridWidth.byteValue());
        int blockIndexV = touchPoint.y / (this.mPhotoHeight / this.mGridHeight.byteValue());
        if (blockIndexH >= this.mGridWidth.byteValue()) {
            blockIndexH = this.mGridWidth.byteValue() - 1;
        }
        if (blockIndexV >= this.mGridHeight.byteValue()) {
            blockIndexV = this.mGridHeight.byteValue() - 1;
        }
        switch ((this.mOrientation + this.mExifOrientation) % 360) {
            case WMComponent.ORI_90 /*90*/:
                index = (((this.mGridWidth.byteValue() - 1) - blockIndexH) * this.mGridHeight.byteValue()) + blockIndexV;
                break;
            case 180:
                index = (((this.mGridHeight.byteValue() - 1) - blockIndexV) * this.mGridWidth.byteValue()) + ((this.mGridWidth.byteValue() - 1) - blockIndexH);
                break;
            case 270:
                index = (this.mGridHeight.byteValue() * blockIndexH) + ((this.mGridHeight.byteValue() - 1) - blockIndexV);
                break;
            default:
                index = (this.mGridWidth.byteValue() * blockIndexV) + blockIndexH;
                break;
        }
        GalleryLog.i("AllFocusPhotoImpl", "Orientation = " + ((this.mOrientation + this.mExifOrientation) % 360));
        GalleryLog.i("AllFocusPhotoImpl", "block Index = " + index);
        GalleryLog.i("AllFocusPhotoImpl", "getBlockIndex() cost " + (System.currentTimeMillis() - currentTimeInMills) + "ms");
        return index;
    }

    private long intToUnsingedInt(int in) {
        return -1 & ((long) in);
    }

    private boolean checkCRC() {
        return this.mCRC == 623191333;
    }

    private void checkAndRotate() {
        try {
            if (this.mPhotoNum.byteValue() >= (byte) 2) {
                ExifInterface exifInterface = new ExifInterface();
                exifInterface.readExif((byte[]) this.mPhotoList.get(0));
                Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                if (orientationValue != null) {
                    int orientation = ExifInterface.getRotationForOrientationValue(orientationValue.shortValue());
                    this.mExifOrientation = orientation;
                    exifInterface.readExif((byte[]) this.mPhotoList.get(1));
                    orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
                    if (orientationValue != null && orientation != ExifInterface.getRotationForOrientationValue(orientationValue.shortValue())) {
                        ExifTag tag = exifInterface.buildTag(ExifInterface.TAG_ORIENTATION, Short.valueOf(ExifInterface.getOrientationValueForRotation(orientation)));
                        for (byte i = (byte) 1; i < this.mPhotoNum.byteValue() && tag != null; i++) {
                            ByteBuffer byteBuffer = ByteBuffer.wrap((byte[]) this.mPhotoList.get(i));
                            exifInterface.readExif((byte[]) this.mPhotoList.get(i));
                            exifInterface.setTag(tag);
                            exifInterface.rewriteExif(byteBuffer, exifInterface.getAllTags());
                        }
                    }
                }
            }
        } catch (IOException e) {
            GalleryLog.i("AllFocusPhotoImpl", "checkAndRotate() failed, reason: IOException.");
        } catch (Throwable t) {
            GalleryLog.w("AllFocusPhotoImpl", "fail to operate exif." + t.getMessage());
        }
    }
}
