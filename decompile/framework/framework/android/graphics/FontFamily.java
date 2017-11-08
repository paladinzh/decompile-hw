package android.graphics;

import android.content.res.AssetManager;
import android.graphics.FontListParser.Axis;
import android.util.Log;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;

public class FontFamily {
    public static final int GOOGLE_DEFAULT = 0;
    public static final int HW_CHINESE_OVERLARY = 2;
    public static final int HW_ENGLISH_OVERLARY = 1;
    private static String TAG = "FontFamily";
    public long mNativePtr;

    private static native boolean nAddFont(long j, ByteBuffer byteBuffer, int i);

    private static native boolean nAddFontFromAsset(long j, AssetManager assetManager, String str);

    private static native boolean nAddFontWeightStyle(long j, ByteBuffer byteBuffer, int i, List<Axis> list, int i2, boolean z);

    private static native long nCreateFamily(String str, int i);

    private static native void nResetCoverage(long j);

    private static native void nResetFont(long j);

    private static native void nSetHwFontFamilyType(long j, int i);

    private static native void nUnrefFamily(long j);

    private static native boolean nUpdateFont(long j, String str, ByteBuffer byteBuffer, int i);

    public FontFamily() {
        this.mNativePtr = nCreateFamily(null, 0);
        if (this.mNativePtr == 0) {
            throw new IllegalStateException("error creating native FontFamily");
        }
    }

    public FontFamily(String lang, String variant) {
        int varEnum = 0;
        if ("compact".equals(variant)) {
            varEnum = 1;
        } else if ("elegant".equals(variant)) {
            varEnum = 2;
        }
        this.mNativePtr = nCreateFamily(lang, varEnum);
        if (this.mNativePtr == 0) {
            throw new IllegalStateException("error creating native FontFamily");
        }
    }

    protected void finalize() throws Throwable {
        try {
            nUnrefFamily(this.mNativePtr);
        } finally {
            super.finalize();
        }
    }

    public boolean updateFont(String path, ByteBuffer font, int fontIndex) {
        return nUpdateFont(this.mNativePtr, path, font, fontIndex);
    }

    public void resetCoverage() {
        nResetCoverage(this.mNativePtr);
    }

    public void resetFont() {
        nResetFont(this.mNativePtr);
    }

    public void setHwFontFamilyType(int type) {
        nSetHwFontFamilyType(this.mNativePtr, type);
    }

    public boolean addFont(String path, int ttcIndex) {
        Throwable th;
        Throwable th2;
        Throwable th3 = null;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream file = new FileInputStream(path);
            try {
                FileChannel fileChannel = file.getChannel();
                boolean nAddFont = nAddFont(this.mNativePtr, fileChannel.map(MapMode.READ_ONLY, 0, fileChannel.size()), ttcIndex);
                if (file != null) {
                    try {
                        file.close();
                    } catch (Throwable th4) {
                        th3 = th4;
                    }
                }
                if (th3 == null) {
                    return nAddFont;
                }
                try {
                    throw th3;
                } catch (IOException e) {
                    fileInputStream = file;
                }
            } catch (Throwable th5) {
                th = th5;
                th2 = null;
                fileInputStream = file;
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (Throwable th6) {
                        if (th2 == null) {
                            th2 = th6;
                        } else if (th2 != th6) {
                            th2.addSuppressed(th6);
                        }
                    }
                }
                if (th2 == null) {
                    throw th;
                }
                try {
                    throw th2;
                } catch (IOException e2) {
                    Log.e(TAG, "Error mapping font file " + path);
                    return false;
                }
            }
        } catch (Throwable th7) {
            th = th7;
            th2 = null;
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (th2 == null) {
                throw th2;
            }
            throw th;
        }
    }

    public boolean addFontWeightStyle(ByteBuffer font, int ttcIndex, List<Axis> axes, int weight, boolean style) {
        return nAddFontWeightStyle(this.mNativePtr, font, ttcIndex, axes, weight, style);
    }

    public boolean addFontFromAsset(AssetManager mgr, String path) {
        return nAddFontFromAsset(this.mNativePtr, mgr, path);
    }
}
