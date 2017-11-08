package com.huawei.android.sdk.imagefilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class HwImageFilters {
    private static final int DESTROY_FILTER_MOULD = 3;
    public static final int FILTER_BLUE = 13;
    public static final int FILTER_CHILDHOOD = 7;
    public static final int FILTER_DAWN = 6;
    public static final int FILTER_DUSK = 9;
    public static final int FILTER_HALO = 8;
    public static final int FILTER_HANDSOME = 14;
    public static final int FILTER_ILLUSION = 11;
    public static final int FILTER_INDIVIDUALITY = 16;
    private static final Object FILTER_LOCK = new Object();
    public static final int FILTER_MONO = 12;
    public static final int FILTER_MORAN = 1;
    public static final int FILTER_NOSTALGIA = 3;
    public static final int FILTER_ORIGINAL = 0;
    public static final int FILTER_PURE = 2;
    public static final int FILTER_SENTIMENTAL = 15;
    public static final int FILTER_SWEET = 10;
    private static final int FILTER_TYPE_MAX = 16;
    public static final int FILTER_VALENCIA = 4;
    public static final int FILTER_VINTAGE = 5;
    private static final int INIT_FILTER_MOULD = 2;
    private static final String TAG = "HwImageFilters";
    public static final int VERSION_FOR_HONOR_PLUS = 61020;
    private static boolean m_Loaded;
    private Handler mAsynchronousHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2:
                    if (HwImageFilters.this.mPath != null) {
                        HwImageFilters.this.nativeApplyFilterInit(HwImageFilters.this.mPath + "/");
                        return;
                    }
                    return;
                case 3:
                    HwImageFilters.this.nativeApplyFilterDestroy();
                    return;
                default:
                    return;
            }
        }
    };
    private Context mContext;
    private String mPath;
    private int sHuaweiFilterVersion = -1;

    native void nativeApplyFilter(Bitmap bitmap, int i, int i2, int i3, int i4);

    native void nativeApplyFilterDestroy();

    native void nativeApplyFilterInit(String str);

    native int nativeCameraEffectsApply(Bitmap bitmap, int i, int i2);

    native int nativeGetHuaweiFilterVersion(int i);

    native int setBitmapPixels(byte[] bArr, int i, int i2, Bitmap bitmap, int i3, int i4, int i5);

    static {
        m_Loaded = false;
        m_Loaded = false;
        try {
            System.loadLibrary("jni_mrc_cg_sdk_filters");
            System.loadLibrary("mrc_cg_filters");
            m_Loaded = true;
        } catch (UnsatisfiedLinkError e) {
            Log.e(TAG, "filterMist so load faile.");
        }
    }

    public boolean isFeatureSupported() {
        return m_Loaded;
    }

    public void initFilter(Context context) {
        if (m_Loaded) {
            if (context == null) {
                Log.e(TAG, "init filter failed.context is null");
                return;
            }
            this.mContext = context;
            this.mPath = this.mContext.getFilesDir().getAbsolutePath();
            copyFilesFassets("mixIm.dat", this.mPath + "/mixIm.dat");
            copyFilesFassets("filter.xml", this.mPath + "/filter.xml");
            nativeApplyFilterInit(this.mPath + "/");
        }
    }

    public void destroyFilter() {
        if (m_Loaded) {
            this.mAsynchronousHandler.sendEmptyMessage(3);
        }
    }

    public int getVersion() {
        int i;
        synchronized (FILTER_LOCK) {
            if (this.sHuaweiFilterVersion == -1) {
                this.sHuaweiFilterVersion = nativeGetHuaweiFilterVersion(0);
            }
            i = this.sHuaweiFilterVersion;
        }
        return i;
    }

    public Bitmap applyImageFilter(Bitmap inputBitmap, int imageFilterType, int imageFilterGradualRatio) {
        if (!isFeatureSupported()) {
            return inputBitmap;
        }
        if (imageFilterType < 0 || imageFilterType > 16) {
            Log.e(TAG, "filterImage faile cause imagefilter type does not exist.");
            return inputBitmap;
        } else if (imageFilterType == 0) {
            return inputBitmap;
        } else {
            nativeApplyFilter(inputBitmap, inputBitmap.getWidth(), inputBitmap.getHeight(), imageFilterType, imageFilterGradualRatio);
            return inputBitmap;
        }
    }

    public Bitmap applyImageFilter(Bitmap inputBitmap, int imageFilterType) {
        return applyImageFilter(inputBitmap, imageFilterType, 100);
    }

    private void copyFilesFassets(String oldPath, String newPath) {
        Throwable th;
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            for (String file : this.mContext.getAssets().list("")) {
                if (file.contains(oldPath)) {
                    inputStream = this.mContext.getAssets().open(oldPath);
                    break;
                }
            }
            if (inputStream == null) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "hw image filter stream close failed: is close.");
                    }
                }
            } else if (new File(newPath).exists()) {
                Log.i(TAG, "dat and xml has existed");
                inputStream.close();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "hw image filter stream close failed: is close.");
                    }
                }
            } else {
                FileOutputStream fos = new FileOutputStream(new File(newPath));
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int byteCount = inputStream.read(buffer);
                        if (byteCount == -1) {
                            break;
                        }
                        fos.write(buffer, 0, byteCount);
                    }
                    fos.flush();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e3) {
                            Log.e(TAG, "hw image filter stream close failed: is close.");
                        }
                    }
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException e4) {
                            Log.e(TAG, "hw image filter stream close failed: fos close. ");
                        }
                    }
                } catch (IOException e5) {
                    fileOutputStream = fos;
                    try {
                        Log.e(TAG, "hw image filter file read or write error");
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e6) {
                                Log.e(TAG, "hw image filter stream close failed: is close.");
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e7) {
                                Log.e(TAG, "hw image filter stream close failed: fos close. ");
                            }
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e8) {
                                Log.e(TAG, "hw image filter stream close failed: is close.");
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e9) {
                                Log.e(TAG, "hw image filter stream close failed: fos close. ");
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fileOutputStream = fos;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    throw th;
                }
            }
        } catch (IOException e10) {
            Log.e(TAG, "hw image filter file read or write error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
    }

    public int previewCameraFilterEffect(Bitmap outputBitmap, int imageFilterType, int imageFilterGradualRatio) {
        return nativeCameraEffectsApply(outputBitmap, imageFilterType, imageFilterGradualRatio);
    }

    public int transferYUVToBitmap(Bitmap outputBitmap, byte[] inputYUV, int yuvWidth, int yuvHeight, int rotation, boolean mirror, boolean blur) {
        return setBitmapPixels(inputYUV, yuvWidth, yuvHeight, outputBitmap, (rotation / 90) * 90, mirror ? 1 : 0, blur ? 1 : 0);
    }
}
