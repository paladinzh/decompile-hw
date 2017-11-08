package com.android.gallery3d.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.ui.ScreenNail;
import com.android.gallery3d.util.ThreadPool.Job;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.barcode.BarcodeScanResultItem;
import java.io.File;
import java.io.FileDescriptor;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public abstract class MediaItem extends MediaObject {
    private static int sFullScreenThumbnailTargetSize = 1920;
    private static final BytesBufferPool sMicroThumbBufferPool = new BytesBufferPool(4, 204800);
    private static BitmapPool sMicroThumbPool;
    private static BitmapPool sMicroVideoThumbPool;
    private static int sMicroVideoThumbnailTargetSize = SmsCheckResult.ESCT_200;
    private static int sMicrothumbnailTargetSize = SmsCheckResult.ESCT_200;
    private static int sSmallThumbnailTargetSize = 640;
    private static final BitmapPool sThumbPool;
    private static int sThumbnailTargetSize = 640;
    private BarcodeScanResultItem mBarcodeScanResult;
    private boolean mIsBarcodeScanned = false;
    private boolean mIsBarcodeStartScan = false;
    private Bitmap mScreenNailBitmapProxy;

    public abstract int getHeight();

    public abstract String getMimeType();

    public abstract int getWidth();

    public abstract Job<Bitmap> requestImage(int i);

    public abstract Job<BitmapRegionDecoder> requestLargeImage();

    static {
        BitmapPool bitmapPool;
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY) {
            bitmapPool = new BitmapPool(4);
        } else {
            bitmapPool = null;
        }
        sThumbPool = bitmapPool;
    }

    public MediaItem(Path path, long version) {
        super(path, version);
    }

    public Bitmap getLatestCacheImage() {
        return null;
    }

    public Job<Bitmap> requestCacheImage() {
        return null;
    }

    public long getDateInMs() {
        return 0;
    }

    public long getDateModifiedInSec() {
        return 0;
    }

    public int getNormalizedDate() {
        return 0;
    }

    public String getName() {
        return null;
    }

    public void getLatLong(double[] latLong) {
        latLong[0] = 0.0d;
        latLong[1] = 0.0d;
    }

    public String[] getTags() {
        return null;
    }

    public int getFullImageRotation() {
        return getRotation();
    }

    public int getRotation() {
        return 0;
    }

    public long getSize() {
        return 0;
    }

    public String getFilePath() {
        return "";
    }

    public Bitmap getScreenNailBitmap(int type) {
        return getScreenNailBitmapProxy();
    }

    public final Bitmap getScreenNailBitmapProxy() {
        if (this.mScreenNailBitmapProxy == null) {
            return null;
        }
        Bitmap proxyBitmap = this.mScreenNailBitmapProxy;
        this.mScreenNailBitmapProxy = null;
        return proxyBitmap;
    }

    public boolean canShare() {
        return true;
    }

    public ScreenNail getScreenNail() {
        return null;
    }

    public static int getTargetSize(int type) {
        return getTargetSize(type, false);
    }

    public static int getTargetSize(int type, boolean needMicroVideoThumbSize) {
        switch (type) {
            case 1:
            case 20:
                return sThumbnailTargetSize;
            case 2:
            case 8:
                return needMicroVideoThumbSize ? sMicroVideoThumbnailTargetSize : sMicrothumbnailTargetSize;
            case 16:
                return sFullScreenThumbnailTargetSize;
            case 24:
                return sSmallThumbnailTargetSize;
            default:
                throw new RuntimeException("should only request thumb/microthumb from cache");
        }
    }

    public static BitmapPool getMicroThumbPool(int targetSize) {
        return targetSize == sMicroVideoThumbnailTargetSize ? getMicroVideoThumbPool() : getMicroThumbPool();
    }

    public static BitmapPool getMicroThumbPool() {
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY && sMicroThumbPool == null) {
            initializeMicroThumbPool();
        }
        return sMicroThumbPool;
    }

    public static BitmapPool getMicroVideoThumbPool() {
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY && sMicroVideoThumbPool == null) {
            initializeMicroVideoThumbPool();
        }
        return sMicroVideoThumbPool;
    }

    public static BitmapPool getThumbPool() {
        return sThumbPool;
    }

    public static BytesBufferPool getBytesBufferPool() {
        return sMicroThumbBufferPool;
    }

    private static void initializeMicroThumbPool() {
        BitmapPool bitmapPool;
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY) {
            bitmapPool = new BitmapPool(sMicrothumbnailTargetSize, sMicrothumbnailTargetSize, 32);
        } else {
            bitmapPool = null;
        }
        sMicroThumbPool = bitmapPool;
    }

    private static void initializeMicroVideoThumbPool() {
        BitmapPool bitmapPool;
        if (ApiHelper.HAS_REUSING_BITMAP_IN_BITMAP_FACTORY) {
            bitmapPool = new BitmapPool(sMicroVideoThumbnailTargetSize, sMicroVideoThumbnailTargetSize, 16);
        } else {
            bitmapPool = null;
        }
        sMicroVideoThumbPool = bitmapPool;
    }

    public static void setThumbnailSizes(int size, int microSize, int microVideoSize) {
        sThumbnailTargetSize = size;
        sSmallThumbnailTargetSize = size >> 1;
        if (sMicrothumbnailTargetSize != microSize) {
            sMicrothumbnailTargetSize = Utils.nextPowerOf2(microSize);
            initializeMicroThumbPool();
        }
        if (sMicroVideoThumbnailTargetSize != microVideoSize) {
            sMicroVideoThumbnailTargetSize = microVideoSize;
            initializeMicroVideoThumbPool();
        }
    }

    public static void setFullScreenThumbnailSizes(int size) {
        sFullScreenThumbnailTargetSize = size;
    }

    public long getVoiceOffset() {
        return 0;
    }

    public boolean isVoiceImage() {
        return false;
    }

    public boolean isRefocusPhoto() {
        return false;
    }

    public boolean isRectifyImage() {
        return false;
    }

    public int getRectifyOffset() {
        return 0;
    }

    public int getRefocusPhotoType() {
        return 0;
    }

    public boolean is3DPanorama() {
        return false;
    }

    public boolean is3DModelImage() {
        return false;
    }

    public int getSpecialFileType() {
        return 0;
    }

    public FileDescriptor getFileDescriptor() {
        return null;
    }

    public void closeParcelFileDescriptor() {
    }

    public boolean isDrm() {
        return false;
    }

    public int getDrmType() {
        return 4;
    }

    public boolean isMyFavorite() {
        return false;
    }

    public int getRightCount() {
        return 0;
    }

    public boolean hasRight() {
        return true;
    }

    public boolean getRight() {
        return true;
    }

    public boolean canForward() {
        return true;
    }

    public boolean hasCountConstraint() {
        return false;
    }

    public void setAsFavorite(Context context) {
        throw new UnsupportedOperationException();
    }

    public void cancelFavorite(Context context) {
        throw new UnsupportedOperationException();
    }

    public boolean isBurstCover() {
        return false;
    }

    public Path getBurstSetPath() {
        return null;
    }

    public void setOutputUri(Uri uri) {
    }

    public boolean saveToOutput() {
        return false;
    }

    public String getOutputFileName() {
        return null;
    }

    public void setBarcodeResult(BarcodeScanResultItem result) {
        this.mBarcodeScanResult = result;
        this.mIsBarcodeScanned = true;
        this.mIsBarcodeStartScan = false;
    }

    public BarcodeScanResultItem getBarcodeResult() {
        return this.mBarcodeScanResult;
    }

    public boolean isBarcodeNeedScan() {
        return (this.mIsBarcodeScanned || this.mIsBarcodeStartScan) ? false : true;
    }

    public void setBarcodeScanFlag(boolean flag) {
        this.mIsBarcodeStartScan = flag;
    }

    public final void setScreenNailBitmapProxy(Bitmap proxy) {
        this.mScreenNailBitmapProxy = proxy;
    }

    public int getExtraTag() {
        return 0;
    }

    public File getDestinationDirectory() {
        return null;
    }

    public int getId() {
        return 0;
    }

    public boolean isCloudPlaceholder() {
        return false;
    }

    public boolean isWaitToUpload() {
        return false;
    }

    public boolean supportComment() {
        return false;
    }

    public FileInfo getFileInfo() {
        return null;
    }

    public boolean isOnlyCloudItem() {
        return false;
    }

    public boolean isContainCloud() {
        return false;
    }

    public boolean isRecycleItem() {
        return false;
    }
}
