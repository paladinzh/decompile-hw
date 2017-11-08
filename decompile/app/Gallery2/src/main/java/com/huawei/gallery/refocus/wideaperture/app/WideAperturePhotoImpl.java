package com.huawei.gallery.refocus.wideaperture.app;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Point;
import android.support.v4.app.FragmentTransaction;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.exif.Rational;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.refocus.app.AbsRefocusPhoto;
import com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil;
import com.huawei.watermark.ui.WMComponent;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;
import java.util.Arrays;

public class WideAperturePhotoImpl extends AbsRefocusPhoto {
    private int[] mAngle;
    private int mDepthDataLen;
    private byte[] mDepthDataLenBytes;
    private boolean mDepthDataSupportRangeMeasure;
    private Bitmap mDstBitmap;
    private Bitmap mDstPreviewBitmap;
    private byte[] mEDoFDataLenBuffer;
    private byte[] mEDoFDepth;
    private byte[] mEDoFPhoto;
    private FilterType mFilterType;
    private boolean mHasPositionTag;
    private boolean mHasPriviewPhotoLengthTag;
    private byte[] mHwUnknowExtendBuffer;
    private boolean mIsEDoFFile;
    private WideAperturePhotoListener mListener;
    private long mNativeHandle;
    private byte[] mNormalPhoto;
    private int mNormalPhotoLength;
    private FilterType mOriginalFilterType;
    private int mOriginalRefocusFnum;
    private Point mOriginalRefocusPoint;
    private boolean mPhotoChanged;
    private int mPhotoTakenAngle;
    private Bitmap mPreviewBitmap;
    private boolean mPreviewMode;
    private int mRefocusFnum;
    private String mSaveAsFilePath;
    private Bitmap mSrcBitmap;
    private int mViewMode;
    private int[] mViewProperty;
    private byte[] mWaterMark;
    private int mWaterMarkBottomMargin;
    private int mWaterMarkStartMargin;
    private int mWaterMarkWidth;
    private int mWideAperturePhotoMode;
    private Thread mWideAperturePhotoThread;

    public interface WideAperturePhotoListener {
        void finishRefocus();

        void onGotFocusPoint();

        void onPrepareComplete();

        void onRefocusComplete();

        void onSaveAsComplete(int i, String str);

        void onSaveFileComplete(int i);
    }

    public enum BlurType {
        Normal,
        Bokeh,
        BokehPlus
    }

    public enum FilterType {
        NORMAL,
        PENCIL,
        COMIC,
        MONO,
        PINFOCUS,
        MINIATURE,
        INVALID
    }

    public enum Photo3DViewProperty {
        Display,
        SurfaceCreate
    }

    public enum Property {
        FNum,
        BlurType,
        Position,
        VCMCode,
        OvalAxis,
        WideApertureLevelCount,
        FilterType,
        FilterSeed
    }

    private enum Refocus_Scene {
        Preview,
        SnapShot,
        Gallery
    }

    private boolean parseFile() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:123:? in {7, 13, 23, 27, 33, 37, 42, 48, 54, 61, 63, 68, 72, 77, 83, 89, 97, 103, 107, 112, 117, 118, 120, 122, 124, 125, 126, 127} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r10 = r14.openFile();
        if (r10 == 0) goto L_0x0226;
    L_0x0006:
        r10 = r14.mFileLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 12;
        if (r10 >= r11) goto L_0x0011;
    L_0x000c:
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0011:
        r10 = r14.mFileLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10 + -8;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10 + -4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r4 = (long) r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mDepthDataLenBytes = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mDepthDataLenBytes;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == r11) goto L_0x0035;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x002d:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0035:
        r10 = r14.mDepthDataLenBytes;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = com.android.gallery3d.util.GalleryUtils.littleEdianByteArrayToInt(r10, r11, r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mDepthDataLen = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 8;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r7 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r7);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 8;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 != r11) goto L_0x0073;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x004d:
        r10 = "DepthEn\u0000";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = "UTF-8";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = java.nio.charset.Charset.forName(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.getBytes(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = java.util.Arrays.equals(r7, r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == 0) goto L_0x0073;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0061:
        r10 = r14.mDepthDataLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = (long) r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r4 = r4 - r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 > 0) goto L_0x007b;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x006b:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0073:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x007b:
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mDepthDataLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mEDoFDepth = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mDepthDataLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == r11) goto L_0x009a;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0092:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x009a:
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r9 = com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil.getUncompressedDepthDataLength(r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r1 = com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil.getDepthHeaderLength();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.length;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10 - r1;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 >= r9) goto L_0x00b4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x00aa:
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r9 + r1;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = java.util.Arrays.copyOf(r10, r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mEDoFDepth = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x00b4:
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil.getRefocusPhotoMode(r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mWideAperturePhotoMode = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = "WideAperturePhotoImpl";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11.<init>();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = "Current wide aperture photo mode: ";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.append(r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = r14.mWideAperturePhotoMode;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.append(r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.toString();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        com.android.gallery3d.util.GalleryLog.d(r10, r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mWideAperturePhotoMode;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = -1;
        if (r10 != r11) goto L_0x00e2;
    L_0x00dd:
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x00e2:
        r10 = 8;
        r4 = r4 - r10;
        r10 = 0;
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));
        if (r10 > 0) goto L_0x00f3;
    L_0x00eb:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x00f3:
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 8;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r0 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mIsEDoFFile = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r0);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 8;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 != r11) goto L_0x0120;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0109:
        r10 = "edof\u0000\u0000\u0000\u0000";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = "UTF-8";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = java.nio.charset.Charset.forName(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.getBytes(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = java.util.Arrays.equals(r0, r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == 0) goto L_0x0120;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x011d:
        r10 = 1;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mIsEDoFFile = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0120:
        r10 = r14.mIsEDoFFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == 0) goto L_0x0160;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0124:
        r10 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r4 = r4 - r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mEDoFDataLenBuffer = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mEDoFDataLenBuffer;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == r11) goto L_0x0144;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x013c:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0144:
        r10 = r14.mEDoFDataLenBuffer;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = 4;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = com.android.gallery3d.util.GalleryUtils.littleEdianByteArrayToInt(r10, r11, r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mEDoFPhotoLen = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x014e:
        r10 = r14.mEDoFPhotoLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = (long) r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r4 = r4 - r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = (r4 > r10 ? 1 : (r4 == r10 ? 0 : -1));	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 >= 0) goto L_0x0176;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x0158:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0160:
        r10 = 8;
        r4 = r4 + r10;
        r10 = (int) r4;
        r14.mEDoFPhotoLen = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        goto L_0x014e;
    L_0x0167:
        r2 = move-exception;
        r10 = "WideAperturePhotoImpl";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = "parseFile() failed, reason: IOException.";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        com.android.gallery3d.util.GalleryLog.i(r10, r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0176:
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mEDoFPhotoLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = new byte[r10];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mEDoFPhoto = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mEDoFPhoto;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mEDoFPhotoLen;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == r11) goto L_0x0195;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x018d:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0195:
        r10 = r14.loadExtendInfo(r4);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 != 0) goto L_0x01a3;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x019b:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x01a3:
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r4 = r10.getFilePointer();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mIsEDoFFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == 0) goto L_0x01d6;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x01ad:
        r6 = r14.mNormalPhotoLength;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r6 > 0) goto L_0x01b9;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x01b1:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x01b9:
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = 0;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10.seek(r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = new byte[r6];	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mNormalPhoto = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mFile;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r14.mNormalPhoto;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r10.read(r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        if (r10 == r6) goto L_0x01d6;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
    L_0x01ce:
        r14.cleanupResource();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x01d6:
        r14.loadOrientation();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil.getPhotoTakenAngle(r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mPhotoTakenAngle = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = r14.mEDoFDepth;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = com.huawei.gallery.refocus.wideaperture.utils.WideAperturePhotoUtil.canBeMeasured(r10);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.mDepthDataSupportRangeMeasure = r10;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r14.closeFile();
        r10 = 1;
        return r10;
    L_0x01ee:
        r8 = move-exception;
        r10 = "WideAperturePhotoImpl";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = new java.lang.StringBuilder;	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11.<init>();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = "fail to operate exif.";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.append(r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r12 = r8.getMessage();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.append(r12);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = r11.toString();	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        com.android.gallery3d.util.GalleryLog.w(r10, r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0212:
        r3 = move-exception;
        r10 = "WideAperturePhotoImpl";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r11 = "parseFile() failed, reason: IllegalArgumentException.";	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        com.android.gallery3d.util.GalleryLog.i(r10, r11);	 Catch:{ IOException -> 0x0167, IllegalArgumentException -> 0x0212, Throwable -> 0x01ee, all -> 0x0221 }
        r10 = 0;
        r14.closeFile();
        return r10;
    L_0x0221:
        r10 = move-exception;
        r14.closeFile();
        throw r10;
    L_0x0226:
        r14.cleanupResource();
        r10 = 0;
        return r10;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.refocus.wideaperture.app.WideAperturePhotoImpl.parseFile():boolean");
    }

    public WideAperturePhotoImpl(String filePath, int photoWidth, int photoHeight) {
        super(filePath, photoWidth, photoHeight);
        this.mHasPositionTag = false;
        this.mHasPriviewPhotoLengthTag = false;
        this.mRefocusFnum = -1;
        this.mAngle = new int[]{0, 0};
        this.mViewProperty = new int[]{0, 0, 0, 0};
        this.mDepthDataSupportRangeMeasure = false;
        this.mWaterMarkStartMargin = -1;
        this.mWaterMarkBottomMargin = -1;
        this.mWaterMarkWidth = -1;
        this.mNormalPhotoLength = -1;
        this.mWideAperturePhotoMode = -1;
        this.mOriginalRefocusPoint = new Point();
        this.mOriginalRefocusFnum = -1;
        this.mIsRefocusPhoto = false;
        this.mPreviewMode = false;
        this.mFilterType = FilterType.NORMAL;
        this.mFocusPoint = new Point(-1, -1);
        this.mViewMode = 0;
    }

    public boolean prepare() {
        this.mNativeHandle = WideAperturePhotoUtil.init();
        if (this.mListener == null) {
            GalleryLog.e("WideAperturePhotoImpl", "please register listener first!");
            return false;
        } else if (parseFile()) {
            this.mWideAperturePhotoThread = new Thread(new Runnable() {
                public void run() {
                    WideAperturePhotoImpl.this.mSrcBitmap = BitmapFactory.decodeByteArray(WideAperturePhotoImpl.this.mEDoFPhoto, 0, WideAperturePhotoImpl.this.mEDoFPhotoLen);
                    if (WideAperturePhotoImpl.this.mSrcBitmap == null) {
                        GalleryLog.e("WideAperturePhotoImpl", "fail to decode edof photo");
                        WideAperturePhotoImpl.this.destroy();
                        return;
                    }
                    WideAperturePhotoImpl.this.mPreviewBitmap = WideAperturePhotoImpl.getPreviewBitmap(WideAperturePhotoImpl.this.mSrcBitmap);
                    GalleryLog.d("WideAperturePhotoImpl", String.format("srcBitmap(%sx%s), previewBitmap(%sx%s)", new Object[]{Integer.valueOf(WideAperturePhotoImpl.this.mSrcBitmap.getWidth()), Integer.valueOf(WideAperturePhotoImpl.this.mSrcBitmap.getHeight()), Integer.valueOf(WideAperturePhotoImpl.this.mPreviewBitmap.getWidth()), Integer.valueOf(WideAperturePhotoImpl.this.mPreviewBitmap.getHeight())}));
                    WideAperturePhotoImpl.this.mDstPreviewBitmap = Bitmap.createBitmap(WideAperturePhotoImpl.this.mPreviewBitmap.getWidth(), WideAperturePhotoImpl.this.mPreviewBitmap.getHeight(), Config.ARGB_8888);
                    WideAperturePhotoImpl.this.mIsRefocusPhoto = true;
                    if (WideAperturePhotoImpl.this.getViewMode() == 0) {
                        WideAperturePhotoImpl.this.enterEditMode(true);
                    }
                    WideAperturePhotoImpl.this.mListener.onGotFocusPoint();
                    WideAperturePhotoImpl.this.mListener.onPrepareComplete();
                }
            });
            this.mWideAperturePhotoThread.start();
            return true;
        } else {
            GalleryLog.e("WideAperturePhotoImpl", "fail to parse file");
            return false;
        }
    }

    public boolean isRefocusPhoto() {
        return this.mIsRefocusPhoto;
    }

    public void enterEditMode(boolean previewMode) {
        int scene = Refocus_Scene.Gallery.ordinal();
        Bitmap srcBitmap = this.mPreviewBitmap;
        this.mPreviewMode = previewMode;
        if (!previewMode) {
            srcBitmap = this.mSrcBitmap;
            scene = Refocus_Scene.SnapShot.ordinal();
        }
        if (WideAperturePhotoUtil.prepare(this.mNativeHandle, scene, srcBitmap, this.mEDoFDepth) == 0) {
            initProperty();
        }
    }

    public void leaveEditMode() {
        this.mPreviewMode = false;
        WideAperturePhotoUtil.destroy(this.mNativeHandle);
    }

    public int doRefocus(Point refocusPoint) {
        if (!this.mIsRefocusPhoto) {
            return 0;
        }
        this.mFocusPoint.x = refocusPoint.x;
        this.mFocusPoint.y = refocusPoint.y;
        this.mFocusPoint = transformToPreviewCoordinate(this.mFocusPoint, this.mOrientation);
        return refocusProcess();
    }

    public int applyFilter(FilterType filterType) {
        if (!this.mIsRefocusPhoto) {
            return 0;
        }
        setFilterType(filterType);
        return refocusProcess();
    }

    public void setWideApertureValue(int value) {
        if (this.mRefocusFnum == value) {
            if (this.mListener != null) {
                this.mListener.finishRefocus();
            }
            return;
        }
        if (value < 0 || value > getWideApertureLevel()) {
            this.mRefocusFnum = getWideApertureLevel() / 2;
        } else {
            this.mRefocusFnum = value;
        }
        setProperty(Property.FNum.ordinal(), this.mRefocusFnum, 0);
        refocusProcess();
    }

    public void setWideAperturePhotoListener(WideAperturePhotoListener listener) {
        this.mListener = listener;
    }

    public Point getFocusPoint() {
        Point point = new Point(this.mFocusPoint);
        if (point.x == -1 || point.y == -1) {
            return point;
        }
        return transformToPhotoCoordinate(point, this.mOrientation);
    }

    public Bitmap getProcessedPhoto() {
        return this.mDstPreviewBitmap;
    }

    public int getWideApertureValue() {
        return this.mRefocusFnum;
    }

    public int getWideApertureLevel() {
        int[] value = new int[2];
        WideAperturePhotoUtil.refocusAndFilterGetProperty(this.mNativeHandle, Property.WideApertureLevelCount.ordinal(), value, this.mEDoFDepth);
        return value[0];
    }

    public void saveFile() {
        if (getViewMode() != 0) {
            if (this.mListener != null) {
                this.mListener.onSaveFileComplete(-2);
            }
            GalleryLog.e("WideAperturePhotoImpl", "invalid view mode");
            return;
        }
        try {
            if (this.mWideAperturePhotoThread != null) {
                this.mWideAperturePhotoThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("WideAperturePhotoImpl", "Thread.join() failed in saveFile() method, reason: InterruptedException.");
        } catch (Throwable th) {
            this.mWideAperturePhotoThread = null;
        }
        this.mWideAperturePhotoThread = null;
        this.mWideAperturePhotoThread = new Thread(new Runnable() {
            public void run() {
                WideAperturePhotoImpl.this.mPhotoChanged = false;
                WideAperturePhotoImpl.this.generateNormalPhoto();
                WideAperturePhotoUtil.compressDepthData(WideAperturePhotoImpl.this.mNativeHandle, WideAperturePhotoImpl.this.mEDoFDepth);
                int retVal = WideAperturePhotoImpl.this.save();
                WideAperturePhotoImpl.this.leaveEditMode();
                WideAperturePhotoImpl.this.enterEditMode(true);
                if (WideAperturePhotoImpl.this.mListener != null) {
                    WideAperturePhotoImpl.this.mListener.onSaveFileComplete(retVal);
                }
            }
        });
        this.mWideAperturePhotoThread.start();
    }

    public int saveAs(String filePath) {
        if (getViewMode() != 0) {
            if (this.mListener != null) {
                this.mListener.onSaveAsComplete(-2, filePath);
                this.mListener.onSaveFileComplete(-2);
            }
            GalleryLog.e("WideAperturePhotoImpl", "invalid view mode");
            return -2;
        }
        try {
            if (this.mWideAperturePhotoThread != null) {
                this.mWideAperturePhotoThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("WideAperturePhotoImpl", "Thread.join() failed in saveAs() method, reason: InterruptedException.");
        } catch (Throwable th) {
            this.mWideAperturePhotoThread = null;
        }
        this.mWideAperturePhotoThread = null;
        this.mSaveAsFilePath = filePath;
        this.mWideAperturePhotoThread = new Thread(new Runnable() {
            /* JADX WARNING: inconsistent code. */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                WideAperturePhotoImpl.this.mPhotoChanged = false;
                WideAperturePhotoImpl.this.generateNormalPhoto();
                if (WideAperturePhotoImpl.this.mDstBitmap != null) {
                    try {
                        ExifInterface exif = new ExifInterface();
                        if (WideAperturePhotoImpl.this.mIsEDoFFile) {
                            exif.readExif(WideAperturePhotoImpl.this.mNormalPhoto);
                        } else {
                            exif.readExif(WideAperturePhotoImpl.this.mEDoFPhoto);
                        }
                        exif.removeCompressedThumbnail();
                        WideAperturePhotoImpl.this.addExtraExifTag(exif);
                        OutputStream exifWriter = exif.getExifWriterStream(WideAperturePhotoImpl.this.mSaveAsFilePath);
                        WideAperturePhotoImpl.this.mDstBitmap.compress(CompressFormat.JPEG, 96, exifWriter);
                        exifWriter.flush();
                        if (WideAperturePhotoImpl.this.mListener != null) {
                            WideAperturePhotoImpl.this.mListener.onSaveAsComplete(0, WideAperturePhotoImpl.this.mSaveAsFilePath);
                        }
                        AbsRefocusPhoto.closeSilently(exifWriter);
                        WideAperturePhotoImpl.this.mSaveAsFilePath = null;
                    } catch (IOException e) {
                        if (WideAperturePhotoImpl.this.mListener != null) {
                            WideAperturePhotoImpl.this.mListener.onSaveAsComplete(-1, WideAperturePhotoImpl.this.mSaveAsFilePath);
                        }
                        GalleryLog.i("WideAperturePhotoImpl", "saveAs() fail because of no space, save path:" + WideAperturePhotoImpl.this.mSaveAsFilePath);
                    } catch (RuntimeException e2) {
                        GalleryLog.i("WideAperturePhotoImpl", "Catch a RuntimeException in saveAs() method.");
                    } catch (Exception e3) {
                        if (WideAperturePhotoImpl.this.mListener != null) {
                            WideAperturePhotoImpl.this.mListener.onSaveAsComplete(-2, WideAperturePhotoImpl.this.mSaveAsFilePath);
                        }
                        GalleryLog.i("WideAperturePhotoImpl", "saveAs() fail because of state error, save path:" + WideAperturePhotoImpl.this.mSaveAsFilePath);
                    } catch (Throwable th) {
                        AbsRefocusPhoto.closeSilently(null);
                        WideAperturePhotoImpl.this.mSaveAsFilePath = null;
                    }
                } else if (WideAperturePhotoImpl.this.mListener != null) {
                    WideAperturePhotoImpl.this.mListener.onSaveAsComplete(-2, WideAperturePhotoImpl.this.mSaveAsFilePath);
                }
                WideAperturePhotoUtil.compressDepthData(WideAperturePhotoImpl.this.mNativeHandle, WideAperturePhotoImpl.this.mEDoFDepth);
                int retVal = WideAperturePhotoImpl.this.save();
                WideAperturePhotoImpl.this.leaveEditMode();
                WideAperturePhotoImpl.this.enterEditMode(true);
                if (WideAperturePhotoImpl.this.mListener != null) {
                    WideAperturePhotoImpl.this.mListener.onSaveFileComplete(retVal);
                }
            }
        });
        this.mWideAperturePhotoThread.start();
        return 0;
    }

    public void restoreOriginalRefocusPoint() {
        this.mFocusPoint = this.mOriginalRefocusPoint;
        this.mFilterType = this.mOriginalFilterType;
        this.mRefocusFnum = this.mOriginalRefocusFnum;
        setRefocusPointAndFilterSeed();
        setFilterType(this.mFilterType);
        setProperty(Property.FNum.ordinal(), this.mRefocusFnum, 0);
    }

    public void cleanupResource() {
        try {
            if (this.mWideAperturePhotoThread != null) {
                this.mWideAperturePhotoThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("WideAperturePhotoImpl", "Thread.join() failed in cleanupResource() method, reason: InterruptedException.");
        } catch (Throwable th) {
            this.mWideAperturePhotoThread = null;
        }
        this.mWideAperturePhotoThread = null;
        destroy();
    }

    private void generateNormalPhoto() {
        WideAperturePhotoUtil.compressDepthData(this.mNativeHandle, this.mEDoFDepth);
        leaveEditMode();
        enterEditMode(false);
        setRefocusPointAndFilterSeed();
        try {
            this.mDstBitmap = Bitmap.createBitmap(this.mSrcBitmap.getWidth(), this.mSrcBitmap.getHeight(), Config.ARGB_8888);
            WideAperturePhotoUtil.process(this.mNativeHandle, this.mSrcBitmap, this.mEDoFDepth, this.mDstBitmap);
            addWaterMark();
        } catch (IllegalArgumentException e) {
            GalleryLog.i("WideAperturePhotoImpl", "createBitmap() failed in generateNormalPhoto() method, reason: IllegalArgumentException.");
        }
    }

    private void destroy() {
        if (getViewMode() == 0) {
            leaveEditMode();
        }
        this.mEDoFPhoto = null;
        this.mEDoFDepth = null;
        this.mDepthDataLenBytes = null;
        this.mFocusPoint = null;
        this.mNormalPhoto = null;
        this.mWaterMark = null;
        recycleBitmap(this.mSrcBitmap);
        this.mSrcBitmap = null;
        recycleBitmap(this.mPreviewBitmap);
        this.mPreviewBitmap = null;
        recycleBitmap(this.mDstPreviewBitmap);
        this.mDstPreviewBitmap = null;
        recycleBitmap(this.mDstBitmap);
        this.mDstBitmap = null;
        WideAperturePhotoUtil.uninit(this.mNativeHandle);
        this.mNativeHandle = 0;
    }

    public int getOrientation() {
        return this.mOrientation;
    }

    public boolean photoChanged() {
        return this.mPhotoChanged;
    }

    public int getFilterTypeIndex() {
        return this.mFilterType.ordinal();
    }

    private void recycleBitmap(Bitmap bitmap) {
        if (bitmap != null) {
            bitmap.recycle();
        }
    }

    private int refocusProcess() {
        try {
            if (this.mWideAperturePhotoThread != null) {
                this.mWideAperturePhotoThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("WideAperturePhotoImpl", "Thread.join() failed in refocusProcess(), reason: InterruptedException.");
        } catch (Throwable th) {
            this.mWideAperturePhotoThread = null;
        }
        this.mWideAperturePhotoThread = null;
        this.mWideAperturePhotoThread = new Thread(new Runnable() {
            public void run() {
                WideAperturePhotoImpl.this.setRefocusPointAndFilterSeed();
                if (WideAperturePhotoImpl.this.mPreviewMode) {
                    WideAperturePhotoUtil.process(WideAperturePhotoImpl.this.mNativeHandle, WideAperturePhotoImpl.this.mPreviewBitmap, WideAperturePhotoImpl.this.mEDoFDepth, WideAperturePhotoImpl.this.mDstPreviewBitmap);
                    if (WideAperturePhotoImpl.this.mListener != null) {
                        WideAperturePhotoImpl.this.mListener.onRefocusComplete();
                    }
                    WideAperturePhotoImpl.this.mPhotoChanged = true;
                }
            }
        });
        this.mWideAperturePhotoThread.start();
        return 1;
    }

    private void setRefocusPointAndFilterSeed() {
        Point focusPoint = getTargetPhotoRefocusPoint();
        setProperty(Property.Position.ordinal(), focusPoint.x, focusPoint.y);
        setProperty(Property.FilterSeed.ordinal(), focusPoint.x, focusPoint.y);
        GalleryLog.i("WideAperturePhotoImpl", "Original:");
        GalleryLog.i("WideAperturePhotoImpl", "focus point: " + this.mFocusPoint.x + ", " + this.mFocusPoint.y);
        GalleryLog.i("WideAperturePhotoImpl", "processed:");
        GalleryLog.i("WideAperturePhotoImpl", "focus point: " + focusPoint.x + ", " + focusPoint.y);
    }

    private void setFilterType(FilterType filterType) {
        if (this.mFilterType != null && this.mFilterType != filterType) {
            this.mFilterType = filterType;
            setProperty(Property.FilterType.ordinal(), this.mFilterType.ordinal(), 0);
        }
    }

    private void loadOrientation() throws IOException {
        ExifInterface exifInterface = new ExifInterface();
        if (this.mIsEDoFFile) {
            exifInterface.readExif(this.mNormalPhoto);
        } else {
            exifInterface.readExif(this.mEDoFPhoto);
        }
        Integer orientationValue = exifInterface.getTagIntValue(ExifInterface.TAG_ORIENTATION);
        if (orientationValue == null) {
            this.mOrientation = 0;
        } else {
            this.mOrientation = ExifInterface.getRotationForOrientationValue(orientationValue.shortValue());
        }
    }

    private long loadNormalPhotoInfo(long offset) throws IOException {
        if (this.mFile == null) {
            return offset;
        }
        long currentOffset = offset - 8;
        this.mFile.seek(currentOffset);
        byte[] tag = new byte[8];
        if (this.mFile.read(tag) != 8) {
            throw new RuntimeException("there is no photo length tag and data.");
        } else if (Arrays.equals(tag, "ExPicLen".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)))) {
            this.mHasPriviewPhotoLengthTag = true;
            currentOffset -= 4;
            this.mFile.seek(currentOffset);
            byte[] buffer = new byte[4];
            if (this.mFile.read(buffer) != 4) {
                throw new RuntimeException("read data length error, tag is ExPicLen");
            }
            this.mNormalPhotoLength = GalleryUtils.littleEdianByteArrayToInt(buffer, 0, 4);
            GalleryLog.d("WideAperturePhotoImpl", "Have tag: ExPicLen");
            return currentOffset;
        } else {
            this.mFile.seek(offset);
            this.mNormalPhotoLength = (int) offset;
            GalleryLog.d("WideAperturePhotoImpl", "Not have tag: ExPicLen, priview photo length is all offset");
            return offset;
        }
    }

    private void loadUnknowExtendData(long currentOffset) throws IOException {
        if (currentOffset > 0 && this.mNormalPhotoLength > 0) {
            long hwExtendDataLength = currentOffset - ((long) this.mNormalPhotoLength);
            if (hwExtendDataLength > 0) {
                this.mFile.seek((long) this.mNormalPhotoLength);
                this.mHwUnknowExtendBuffer = new byte[((int) hwExtendDataLength)];
                if (((long) this.mFile.read(this.mHwUnknowExtendBuffer)) != hwExtendDataLength) {
                    this.mHwUnknowExtendBuffer = null;
                    throw new RuntimeException("read extend data error.");
                }
            }
            GalleryLog.d("WideAperturePhotoImpl", "priview photo length is " + this.mNormalPhotoLength + ", hw extend data is " + (this.mHwUnknowExtendBuffer == null ? "null" : Integer.valueOf(this.mHwUnknowExtendBuffer.length)));
        }
    }

    public int getWideAperturePhotoMode() {
        return this.mWideAperturePhotoMode;
    }

    private boolean openFile() {
        try {
            if (this.mFile != null) {
                closeFile();
            }
            this.mFile = new RandomAccessFile(this.mFileName, "rws");
            this.mFileLen = (int) this.mFile.length();
        } catch (FileNotFoundException e) {
            GalleryLog.i("WideAperturePhotoImpl", "openFile() failed, reason: FileNotFoundException.");
        } catch (IllegalArgumentException e2) {
            GalleryLog.i("WideAperturePhotoImpl", "openFile() failed, reason: IllegalArgumentException.");
        } catch (IOException e3) {
            GalleryLog.i("WideAperturePhotoImpl", "openFile() failed, reason: IOException.");
        }
        return this.mFile != null;
    }

    private void closeFile() {
        if (this.mFile != null) {
            try {
                this.mFile.close();
                this.mFile = null;
                this.mFileLen = 0;
            } catch (IOException e) {
                GalleryLog.i("WideAperturePhotoImpl", "closeFile() failed, reason: IOException.");
            }
        }
    }

    private boolean loadExtendInfo(long offset) {
        if (this.mFile == null) {
            return false;
        }
        long j = offset;
        try {
            loadUnknowExtendData(loadNormalPhotoInfo(loadWaterMarkInfo(offset)));
            return true;
        } catch (RuntimeException e) {
            GalleryLog.e("WideAperturePhotoImpl", "load extend info failed, reason: RuntimeException. " + e.getMessage());
            return false;
        } catch (Exception e2) {
            GalleryLog.e("WideAperturePhotoImpl", "load extend info failed, " + e2.getMessage());
            return false;
        }
    }

    private long loadWaterMarkInfo(long offset) throws IOException {
        if (this.mFile == null) {
            return offset;
        }
        long currentOffset = offset - 8;
        this.mFile.seek(currentOffset);
        byte[] tag = new byte[8];
        if (this.mFile.read(tag) != 8) {
            throw new RuntimeException("there is no extend info");
        } else if (Arrays.equals(tag, "ExWMark\u0000".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)))) {
            currentOffset -= 4;
            this.mFile.seek(currentOffset);
            byte[] buffer = new byte[4];
            if (this.mFile.read(buffer) != 4) {
                throw new RuntimeException("read data length error, tag is ExWMark\u0000");
            }
            int length = GalleryUtils.littleEdianByteArrayToInt(buffer, 0, 4);
            currentOffset -= (long) length;
            this.mFile.seek(currentOffset);
            this.mWaterMark = new byte[length];
            if (this.mFile.read(this.mWaterMark) != length) {
                throw new RuntimeException("read water-mark data error");
            }
            currentOffset -= 8;
            this.mFile.seek(currentOffset);
            byte[] positionTag = new byte[8];
            if (this.mFile.read(positionTag) != 8) {
                throw new RuntimeException("there is no water mark position data");
            } else if (Arrays.equals(positionTag, "Position".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)))) {
                this.mHasPositionTag = true;
                int[] waterMarkPositionData = new int[3];
                for (int i = 0; i < waterMarkPositionData.length; i++) {
                    currentOffset -= 4;
                    this.mFile.seek(currentOffset);
                    byte[] positionBuffer = new byte[4];
                    if (this.mFile.read(positionBuffer) != 4) {
                        throw new RuntimeException("read data length error, tag is Position, current data index is " + i);
                    }
                    waterMarkPositionData[i] = GalleryUtils.littleEdianByteArrayToInt(positionBuffer, 0, 4);
                }
                this.mWaterMarkStartMargin = waterMarkPositionData[0];
                this.mWaterMarkBottomMargin = waterMarkPositionData[1];
                this.mWaterMarkWidth = waterMarkPositionData[2];
                GalleryLog.d("WideAperturePhotoImpl", String.format("water mark position data: startMargin:%d, bottomMargin:%d, width:%d", new Object[]{Integer.valueOf(this.mWaterMarkStartMargin), Integer.valueOf(this.mWaterMarkBottomMargin), Integer.valueOf(this.mWaterMarkWidth)}));
                this.mFile.seek(currentOffset);
                return currentOffset;
            } else {
                this.mFile.seek(8 + currentOffset);
                GalleryLog.i("WideAperturePhotoImpl", "there is no water mark position data");
                return 8 + currentOffset;
            }
        } else {
            this.mFile.seek(offset);
            GalleryLog.i("WideAperturePhotoImpl", "there is no tag: ExWMark\u0000");
            return offset;
        }
    }

    private int save() {
        Object obj;
        Throwable th;
        Closeable closeable = null;
        Closeable closeable2 = null;
        File dstFile = new File(this.mFileName);
        String tmpFileName = dstFile.getParent() + File.separator + "WideAperturePhoto.data";
        File tmpFile = new File(tmpFileName);
        if (this.mDstBitmap == null) {
            return -2;
        }
        try {
            ExifInterface exif = new ExifInterface();
            if (this.mIsEDoFFile) {
                exif.readExif(this.mNormalPhoto);
            } else {
                exif.readExif(this.mEDoFPhoto);
            }
            exif.removeCompressedThumbnail();
            addExtraExifTag(exif);
            OutputStream exifWriter = exif.getExifWriterStream(tmpFileName);
            this.mDstBitmap.compress(CompressFormat.JPEG, 96, exifWriter);
            exifWriter.flush();
            exifWriter.close();
            closeable2 = null;
            RandomAccessFile fileWriter = new RandomAccessFile(tmpFileName, "rws");
            try {
                fileWriter.seek(fileWriter.length());
                saveExtendInfo(fileWriter);
                fileWriter.write(this.mEDoFPhoto);
                if (!this.mIsEDoFFile) {
                    this.mEDoFDataLenBuffer = new byte[4];
                    AbsRefocusPhoto.intToByteArray(this.mEDoFPhotoLen, this.mEDoFDataLenBuffer, 0);
                }
                fileWriter.write(this.mEDoFDataLenBuffer);
                fileWriter.write("edof\u0000\u0000\u0000\u0000".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
                fileWriter.write(this.mEDoFDepth, 0, this.mDepthDataLen);
                fileWriter.write(this.mDepthDataLenBytes);
                fileWriter.write("DepthEn\u0000".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
                fileWriter.close();
                closeable = null;
                if (tmpFile.renameTo(dstFile)) {
                    AbsRefocusPhoto.closeSilently(null);
                    AbsRefocusPhoto.closeSilently(null);
                    if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                        tmpFile.deleteOnExit();
                    }
                    return 0;
                }
                AbsRefocusPhoto.closeSilently(null);
                AbsRefocusPhoto.closeSilently(null);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                return -2;
            } catch (IOException e) {
                obj = fileWriter;
                try {
                    GalleryLog.i("WideAperturePhotoImpl", "Save bitmap failed, reason: IOException.");
                    AbsRefocusPhoto.closeSilently(closeable2);
                    AbsRefocusPhoto.closeSilently(closeable);
                    if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                        tmpFile.deleteOnExit();
                    }
                    return -1;
                } catch (Throwable th2) {
                    th = th2;
                    AbsRefocusPhoto.closeSilently(closeable2);
                    AbsRefocusPhoto.closeSilently(closeable);
                    if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                        tmpFile.deleteOnExit();
                    }
                    throw th;
                }
            } catch (RuntimeException e2) {
                obj = fileWriter;
                GalleryLog.i("WideAperturePhotoImpl", "catch a RuntimeException in save() method.");
                AbsRefocusPhoto.closeSilently(closeable2);
                AbsRefocusPhoto.closeSilently(closeable);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                return 0;
            } catch (Exception e3) {
                obj = fileWriter;
                GalleryLog.i("WideAperturePhotoImpl", "Save bitmap failed.");
                AbsRefocusPhoto.closeSilently(closeable2);
                AbsRefocusPhoto.closeSilently(closeable);
                if (tmpFile.exists() && dstFile.exists() && !tmpFile.delete()) {
                    tmpFile.deleteOnExit();
                }
                return -2;
            } catch (Throwable th3) {
                th = th3;
                obj = fileWriter;
                AbsRefocusPhoto.closeSilently(closeable2);
                AbsRefocusPhoto.closeSilently(closeable);
                tmpFile.deleteOnExit();
                throw th;
            }
        } catch (IOException e4) {
            GalleryLog.i("WideAperturePhotoImpl", "Save bitmap failed, reason: IOException.");
            AbsRefocusPhoto.closeSilently(closeable2);
            AbsRefocusPhoto.closeSilently(closeable);
            tmpFile.deleteOnExit();
            return -1;
        } catch (RuntimeException e5) {
            GalleryLog.i("WideAperturePhotoImpl", "catch a RuntimeException in save() method.");
            AbsRefocusPhoto.closeSilently(closeable2);
            AbsRefocusPhoto.closeSilently(closeable);
            tmpFile.deleteOnExit();
            return 0;
        } catch (Exception e6) {
            GalleryLog.i("WideAperturePhotoImpl", "Save bitmap failed.");
            AbsRefocusPhoto.closeSilently(closeable2);
            AbsRefocusPhoto.closeSilently(closeable);
            tmpFile.deleteOnExit();
            return -2;
        }
    }

    private void writeHwExtendData(RandomAccessFile fileWriter) throws IOException {
        if (this.mHwUnknowExtendBuffer != null && this.mHwUnknowExtendBuffer.length > 0) {
            fileWriter.write(this.mHwUnknowExtendBuffer);
        }
    }

    private void writeNormalPhotoInfo(RandomAccessFile fileWriter, int normalPhotolength) throws IOException {
        if (this.mHasPriviewPhotoLengthTag) {
            byte[] buffer = new byte[4];
            AbsRefocusPhoto.intToByteArray(normalPhotolength, buffer, 0);
            fileWriter.write(buffer);
            fileWriter.write("ExPicLen".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
        }
    }

    private void writeWaterMarkInfo(RandomAccessFile writer) throws IOException {
        if (this.mWaterMark != null) {
            if (this.mHasPositionTag) {
                byte[] waterMarkPositionData = new byte[4];
                AbsRefocusPhoto.intToByteArray(this.mWaterMarkWidth, waterMarkPositionData, 0);
                writer.write(waterMarkPositionData);
                AbsRefocusPhoto.intToByteArray(this.mWaterMarkBottomMargin, waterMarkPositionData, 0);
                writer.write(waterMarkPositionData);
                AbsRefocusPhoto.intToByteArray(this.mWaterMarkStartMargin, waterMarkPositionData, 0);
                writer.write(waterMarkPositionData);
                writer.write("Position".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
            }
            byte[] buffer = new byte[4];
            AbsRefocusPhoto.intToByteArray(this.mWaterMark.length, buffer, 0);
            writer.write(this.mWaterMark);
            writer.write(buffer);
            writer.write("ExWMark\u0000".getBytes(Charset.forName(XmlUtils.INPUT_ENCODING)));
        }
    }

    private void saveExtendInfo(RandomAccessFile writer) throws Exception {
        if (writer != null) {
            int normalPhotoLength = (int) writer.length();
            writeHwExtendData(writer);
            writeNormalPhotoInfo(writer, normalPhotoLength);
            writeWaterMarkInfo(writer);
        }
    }

    private void initProperty() {
        int[] value = new int[2];
        if (this.mPreviewMode) {
            this.mRefocusFnum = getWideApertureLevel() / 2;
            if (WideAperturePhotoUtil.refocusAndFilterGetProperty(this.mNativeHandle, Property.FNum.ordinal(), value, this.mEDoFDepth) == 0) {
                this.mRefocusFnum = value[0];
                this.mOriginalRefocusFnum = value[0];
            }
            if (WideAperturePhotoUtil.refocusAndFilterGetProperty(this.mNativeHandle, Property.Position.ordinal(), value, this.mEDoFDepth) == 0) {
                this.mFocusPoint.x = value[0];
                this.mFocusPoint.y = value[1];
                this.mOriginalRefocusPoint.x = value[0];
                this.mOriginalRefocusPoint.y = value[1];
                transformToFullViewImageCoordinate(this.mFocusPoint, this.mPhotoTakenAngle);
                this.mPhotoTakenAngle = 0;
            }
            if (WideAperturePhotoUtil.refocusAndFilterGetProperty(this.mNativeHandle, Property.FilterType.ordinal(), value, this.mEDoFDepth) == 0) {
                this.mFilterType = FilterType.values()[value[0]];
                this.mOriginalFilterType = FilterType.values()[value[0]];
            }
        }
        setProperty(Property.FNum.ordinal(), this.mRefocusFnum, 0);
        setProperty(Property.BlurType.ordinal(), BlurType.Bokeh.ordinal(), 0);
        setProperty(Property.FilterType.ordinal(), this.mFilterType.ordinal(), 0);
        setRefocusPointAndFilterSeed();
    }

    public static Bitmap getPreviewBitmap(Bitmap bitmap) {
        boolean swapWidthAndHeight = false;
        if (bitmap == null) {
            return null;
        }
        int width;
        int height;
        int srcWidth = bitmap.getWidth();
        int srcHeight = bitmap.getHeight();
        if (srcHeight > srcWidth) {
            srcWidth ^= srcHeight;
            srcHeight ^= srcWidth;
            srcWidth ^= srcHeight;
            swapWidthAndHeight = true;
        }
        if (srcWidth * 2336 == srcHeight * 4160 || srcWidth * 1840 == srcHeight * 3264) {
            width = 1632;
            height = 920;
        } else if (srcWidth > FragmentTransaction.TRANSIT_ENTER_MASK || srcHeight > FragmentTransaction.TRANSIT_ENTER_MASK) {
            int scale = Utils.nextPowerOf2(Math.max(srcWidth, srcHeight) / 2048);
            width = srcWidth / scale;
            height = srcHeight / scale;
        } else {
            width = srcWidth / 2;
            height = srcHeight / 2;
        }
        if (swapWidthAndHeight) {
            width ^= height;
            height ^= width;
            width ^= height;
        }
        return Bitmap.createScaledBitmap(bitmap, width, height, true);
    }

    public int create3DView() {
        return WideAperturePhotoUtil.create3DView(this.mNativeHandle, this.mSrcBitmap, this.mEDoFDepth, this.mAngle, this.mViewProperty);
    }

    public void destroy3DView() {
        WideAperturePhotoUtil.destroy3DView(this.mNativeHandle);
    }

    public void init3DViewDisplayParams(float[] angle, int viewOrientation, int viewWidth, int viewHeight) {
        this.mAngle[0] = (int) (angle[0] * 1000.0f);
        this.mAngle[1] = (int) (angle[1] * 1000.0f);
        this.mViewProperty[0] = viewOrientation;
        this.mViewProperty[1] = viewWidth;
        this.mViewProperty[2] = viewHeight;
        this.mViewProperty[3] = convertOrientation(this.mOrientation);
    }

    public int set3DViewProperty(int propertyType, int viewOrientation, int viewWidth, int viewHeight) {
        this.mViewProperty[0] = viewOrientation;
        this.mViewProperty[1] = viewWidth;
        this.mViewProperty[2] = viewHeight;
        this.mViewProperty[3] = convertOrientation(this.mOrientation);
        return WideAperturePhotoUtil.set3DViewProperty(this.mNativeHandle, propertyType, this.mViewProperty);
    }

    public int invalidate3DView(float[] angle) {
        this.mAngle[0] = (int) (angle[0] * 1000.0f);
        this.mAngle[1] = (int) (angle[1] * 1000.0f);
        return WideAperturePhotoUtil.invalidate3DView(this.mNativeHandle, this.mAngle);
    }

    private int convertOrientation(int orientation) {
        switch (orientation % 360) {
            case WMComponent.ORI_90 /*90*/:
                return 1;
            case 180:
                return 2;
            case 270:
                return 3;
            default:
                return 0;
        }
    }

    public void setViewMode(int mode) {
        if (mode > 2 || mode < 0) {
            throw new InvalidParameterException("invalid view mode " + mode);
        }
        this.mViewMode = mode;
    }

    public int getViewMode() {
        return this.mViewMode;
    }

    private Point transformToFullViewImageCoordinate(Point point, int angle) {
        int tmp_x = point.x;
        int tmp_y = point.y;
        switch ((angle + 360) % 360) {
            case WMComponent.ORI_90 /*90*/:
                point.x = this.mSrcBitmap.getWidth() - tmp_y;
                point.y = tmp_x;
                break;
            case 180:
                point.x = this.mSrcBitmap.getWidth() - tmp_x;
                point.y = this.mSrcBitmap.getHeight() - tmp_y;
                break;
            case 270:
                point.x = tmp_y;
                point.y = this.mSrcBitmap.getHeight() - tmp_x;
                break;
        }
        return point;
    }

    private void setProperty(int propType, int propValue1, int propValue2) {
        WideAperturePhotoUtil.refocusAndFilterSetProperty(this.mNativeHandle, propType, new int[]{propValue1, propValue2}, this.mEDoFDepth);
    }

    private Point getTargetPhotoRefocusPoint() {
        if (!this.mPreviewMode) {
            return this.mFocusPoint;
        }
        Point point = new Point(this.mFocusPoint);
        float scale = getPreviewImageScale();
        point.x = (int) (((float) this.mFocusPoint.x) * scale);
        point.y = (int) (((float) this.mFocusPoint.y) * scale);
        if (point.x < 0) {
            point.x = 0;
        }
        if (point.y < 0) {
            point.y = 0;
        }
        if (point.x > this.mPreviewBitmap.getWidth()) {
            point.x = this.mPreviewBitmap.getWidth();
        }
        if (point.y > this.mPreviewBitmap.getHeight()) {
            point.y = this.mPreviewBitmap.getHeight();
        }
        return point;
    }

    private float getPreviewImageScale() {
        return ((float) Math.max(this.mPreviewBitmap.getHeight(), this.mPreviewBitmap.getWidth())) / ((float) Math.max(this.mSrcBitmap.getWidth(), this.mSrcBitmap.getHeight()));
    }

    private void addExtraExifTag(ExifInterface exif) {
        int refocusFnum = this.mRefocusFnum;
        if (refocusFnum >= 0 && refocusFnum < ApertureParameter.DOUBLE_SUPPORTED_VALUES.length) {
            exif.setTag(exif.buildTag(ExifInterface.TAG_F_NUMBER, new Rational((long) (ApertureParameter.DOUBLE_SUPPORTED_VALUES[refocusFnum] * 100.0d), 100)));
        }
    }

    private void addWaterMark() {
        if (this.mWaterMark == null || this.mDstBitmap == null) {
            GalleryLog.e("WideAperturePhotoImpl", "mWaterMark is null: " + (this.mWaterMark == null) + ", mDstBitmap is " + this.mDstBitmap);
            return;
        }
        int leftMargin;
        int bottomMargin;
        int waterMarkWidth;
        int shortEdge = Math.min(this.mDstBitmap.getWidth(), this.mDstBitmap.getHeight());
        float waterMarkWidthF = ((float) (shortEdge * 474)) / 1080.0f;
        if (this.mWaterMarkBottomMargin <= 0 || this.mWaterMarkStartMargin <= 0 || this.mWaterMarkWidth <= 0) {
            leftMargin = (int) ((((float) (shortEdge * 45)) / 1080.0f) + 0.5f);
            bottomMargin = (int) ((((float) (shortEdge * 21)) / 1080.0f) + 0.5f);
            waterMarkWidth = (int) (0.5f + waterMarkWidthF);
        } else {
            leftMargin = this.mWaterMarkStartMargin;
            bottomMargin = this.mWaterMarkBottomMargin;
            waterMarkWidth = this.mWaterMarkWidth;
            waterMarkWidthF = (float) waterMarkWidth;
        }
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(this.mWaterMark, 0, this.mWaterMark.length, options);
        if (options.outWidth == 0 || options.outHeight == 0) {
            GalleryLog.e("WideAperturePhotoImpl", "water-mark width is " + options.outWidth + ", height is " + options.outHeight);
            return;
        }
        Bitmap tmpBitmap = BitmapFactory.decodeByteArray(this.mWaterMark, 0, this.mWaterMark.length);
        if (tmpBitmap == null) {
            GalleryLog.e("WideAperturePhotoImpl", "water-mark bitmap is null");
            return;
        }
        Bitmap waterMarkBitmap = Bitmap.createScaledBitmap(tmpBitmap, waterMarkWidth, (int) (((((float) options.outHeight) * waterMarkWidthF) / ((float) options.outWidth)) + 0.5f), false);
        tmpBitmap.recycle();
        if (waterMarkBitmap == null) {
            GalleryLog.e("WideAperturePhotoImpl", "create waterMarkBitmap fail");
            return;
        }
        Canvas canvas = new Canvas();
        canvas.setBitmap(this.mDstBitmap);
        canvas.drawBitmap(waterMarkBitmap, (float) leftMargin, (float) ((this.mDstBitmap.getHeight() - bottomMargin) - waterMarkBitmap.getHeight()), null);
        waterMarkBitmap.recycle();
    }

    public boolean rangeMeasurePrepare() {
        this.mNativeHandle = WideAperturePhotoUtil.init();
        if (this.mListener == null) {
            GalleryLog.e("WideAperturePhotoImpl", "please register listener first!");
            return false;
        } else if (!parseFile()) {
            return false;
        } else {
            this.mWideAperturePhotoThread = new Thread(new Runnable() {
                public void run() {
                    WideAperturePhotoImpl.this.mSrcBitmap = BitmapFactory.decodeByteArray(WideAperturePhotoImpl.this.mEDoFPhoto, 0, WideAperturePhotoImpl.this.mEDoFPhotoLen);
                    if (WideAperturePhotoImpl.this.mSrcBitmap == null) {
                        WideAperturePhotoImpl.this.destroy();
                        return;
                    }
                    WideAperturePhotoImpl.this.mIsRefocusPhoto = true;
                    WideAperturePhotoImpl.this.mListener.onPrepareComplete();
                }
            });
            this.mWideAperturePhotoThread.start();
            return true;
        }
    }

    public byte[] getEDoFPhoto() {
        if (this.mEDoFPhoto != null) {
            return (byte[]) this.mEDoFPhoto.clone();
        }
        return new byte[0];
    }

    public byte[] getDepthData() {
        return (byte[]) this.mEDoFDepth.clone();
    }

    public int getEDoFPhotoLen() {
        return this.mEDoFPhotoLen;
    }

    public Point rangeMeasureTransformToPhotoCoordinate(Point point) {
        return transformToPhotoCoordinate(point, this.mOrientation);
    }

    public Point rangeMeasureTransformToPreviewCoordinate(Point point) {
        return transformToPreviewCoordinate(point, this.mOrientation);
    }

    public boolean isDepthDataSupportRangeMeasure() {
        return this.mDepthDataSupportRangeMeasure;
    }

    public int rangeMeasure(int x1, int y1, int x2, int y2) {
        return WideAperturePhotoUtil.rangeMeasure(this.mNativeHandle, x1, y1, x2, y2, this.mSrcBitmap.getWidth(), this.mSrcBitmap.getHeight(), getDepthData());
    }
}
