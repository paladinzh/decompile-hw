package com.android.gallery3d.data;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory.Options;
import android.graphics.BitmapRegionDecoder;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.app.PanoramaMetadataSupport;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DownloadCache.Entry;
import com.android.gallery3d.util.BaseJob;
import com.android.gallery3d.util.BlackList;
import com.android.gallery3d.util.DrmUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ThreadPool.CancelListener;
import com.android.gallery3d.util.ThreadPool.Job;
import com.android.gallery3d.util.ThreadPool.JobContext;
import java.io.Closeable;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

public class UriImage extends MediaItem {
    private GalleryApp mApplication;
    private Entry mCacheEntry;
    private final String mContentType;
    private ParcelFileDescriptor mFileDescriptor;
    private String mFileName;
    private final String mFilePath;
    private int mHeight;
    private final boolean mIsDrm;
    private Uri mOutputUri;
    private PanoramaMetadataSupport mPanoramaMetadata = new PanoramaMetadataSupport(this);
    private ParcelFileDescriptor mParcelFileDescriptor = null;
    private int mRotation;
    private int mState = 0;
    private final Uri mUri;
    private long mVoiceOffset;
    private int mWidth;

    private class BitmapJob extends BaseJob<Bitmap> {
        private int mType;

        protected BitmapJob(int type) {
            this.mType = type;
        }

        public Bitmap run(JobContext jc) {
            if (UriImage.this.prepareInputFile(jc)) {
                int targetSize = MediaItem.getTargetSize(this.mType);
                Options options = new Options();
                options.inPreferredConfig = Config.ARGB_8888;
                if (UriImage.this.mIsDrm) {
                    DrmUtils.inDrmMode(options);
                }
                if (this.mType == 2 && UriImage.this.mIsDrm) {
                    DrmUtils.inPreviewMode(options);
                }
                ParcelFileDescriptor fileDescriptor = UriImage.this.mFileDescriptor;
                if (fileDescriptor == null) {
                    GalleryLog.d("UriImage", "[decode thumb] FD is null !! " + UriImage.this.mUri);
                    return null;
                }
                Bitmap bitmap;
                synchronized (fileDescriptor) {
                    bitmap = DecodeUtils.decodeThumbnail(jc, fileDescriptor.getFileDescriptor(), options, targetSize, this.mType);
                }
                if (jc.isCancelled() || bitmap == null) {
                    GalleryLog.w("UriImage", "decodeThumbnail failed !! cancelled ? " + jc.isCancelled() + ", ret(Bitmap) = " + bitmap + ", uri:" + UriImage.this.mUri);
                    return null;
                }
                if (this.mType == 2) {
                    bitmap = BitmapUtils.resizeAndCropCenter(bitmap, targetSize, true);
                } else {
                    bitmap = BitmapUtils.resizeDownBySideLength(bitmap, targetSize, true);
                }
                return bitmap;
            }
            GalleryLog.w("UriImage", "[decode thumb]prepareInputFile failed !! " + UriImage.this.mUri);
            return null;
        }

        public String workContent() {
            return String.format("reload uri image. type: %s, uri: %s", new Object[]{Integer.valueOf(this.mType), UriImage.this.mUri});
        }
    }

    private class RegionDecoderJob extends BaseJob<BitmapRegionDecoder> {
        private RegionDecoderJob() {
        }

        public BitmapRegionDecoder run(JobContext jc) {
            if (UriImage.this.prepareInputFile(jc)) {
                ParcelFileDescriptor fileDescriptor = UriImage.this.mFileDescriptor;
                if (fileDescriptor == null) {
                    GalleryLog.d("UriImage", "[create region decoder] FD is null !! " + UriImage.this.mUri);
                    return null;
                }
                BitmapRegionDecoder decoder = DecodeUtils.createBitmapRegionDecoder(jc, fileDescriptor.getFileDescriptor(), false);
                if (decoder == null) {
                    GalleryLog.w("UriImage", "createBitmapRegionDecoder failed !! decoder is null.  cancelled ? " + jc.isCancelled() + ", uri:" + UriImage.this.mUri);
                    return null;
                }
                UriImage.this.mWidth = decoder.getWidth();
                UriImage.this.mHeight = decoder.getHeight();
                return decoder;
            }
            GalleryLog.w("UriImage", "[create region decoder]prepareInputFile failed !! " + UriImage.this.mUri);
            return null;
        }

        public String workContent() {
            return "create region decoder for uriImage: " + UriImage.this.mUri;
        }
    }

    public UriImage(GalleryApp application, Path path, Uri uri, String contentType) {
        super(path, MediaObject.nextVersionNumber());
        this.mUri = uri;
        this.mApplication = (GalleryApp) Utils.checkNotNull(application);
        this.mContentType = contentType;
        this.mFilePath = GalleryUtils.convertUriToPath(this.mApplication.getAndroidContext(), this.mUri);
        resolveFileName(uri);
        this.mIsDrm = DrmUtils.isDrmFile(this.mFilePath);
        CalcVoiceOffset();
    }

    public Job<Bitmap> requestImage(int type) {
        return new BitmapJob(type);
    }

    public Job<BitmapRegionDecoder> requestLargeImage() {
        return new RegionDecoderJob();
    }

    private void openFileOrDownloadTempFile(JobContext jc) {
        int state = openOrDownloadInner(jc);
        synchronized (this) {
            this.mState = state;
            if (!(this.mState == 2 || this.mFileDescriptor == null)) {
                Utils.closeSilently(this.mFileDescriptor);
                this.mFileDescriptor = null;
            }
            notifyAll();
        }
    }

    private int openOrDownloadInner(JobContext jc) {
        String scheme = this.mUri.getScheme();
        if ("content".equals(scheme) || "android.resource".equals(scheme) || "file".equals(scheme)) {
            try {
                if ("image/jpeg".equalsIgnoreCase(this.mContentType)) {
                    Closeable is = this.mApplication.getContentResolver().openInputStream(this.mUri);
                    this.mRotation = Exif.getOrientation(is);
                    Utils.closeSilently(is);
                }
                this.mFileDescriptor = this.mApplication.getContentResolver().openFileDescriptor(this.mUri, "r");
                return jc.isCancelled() ? 0 : 2;
            } catch (Throwable t) {
                GalleryLog.w("UriImage", "fail to open: " + this.mUri + "." + t.getMessage());
                return -1;
            }
        }
        try {
            URL url = new URI(this.mUri.toString()).toURL();
            this.mCacheEntry = this.mApplication.getDownloadCache().download(jc, url);
            if (jc.isCancelled()) {
                return 0;
            }
            if (this.mCacheEntry == null) {
                GalleryLog.w("UriImage", "download failed " + url);
                return -1;
            }
            if ("image/jpeg".equalsIgnoreCase(this.mContentType)) {
                is = new FileInputStream(this.mCacheEntry.cacheFile);
                this.mRotation = Exif.getOrientation(is);
                Utils.closeSilently(is);
            }
            this.mFileDescriptor = ParcelFileDescriptor.open(this.mCacheEntry.cacheFile, 268435456);
            return 2;
        } catch (Throwable t2) {
            GalleryLog.w("UriImage", "download error", t2);
            return -1;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean prepareInputFile(JobContext jc) {
        jc.setCancelListener(new CancelListener() {
            public void onCancel() {
                synchronized (UriImage.this) {
                    UriImage.this.mState = 0;
                    UriImage.this.notifyAll();
                }
            }
        });
        while (true) {
            synchronized (this) {
                if (jc.isCancelled()) {
                    return false;
                } else if (this.mState == 0) {
                    this.mState = 1;
                } else if (this.mState == -1) {
                    return false;
                } else if (this.mState == 2) {
                    return true;
                } else {
                    try {
                        wait();
                    } catch (InterruptedException e) {
                    }
                }
            }
        }
    }

    public int getSupportedOperations() {
        int supported = 131072;
        if (DrmUtils.canSetAsWallPaper(this)) {
            supported = 131104;
        }
        if (!(isBlackList() || this.mIsDrm || !BitmapUtils.isFilterShowSupported(this.mContentType))) {
            supported |= 512;
        }
        if (isSharable() && (!this.mIsDrm || canForward())) {
            supported |= 4;
        }
        if (BitmapUtils.isSupportedByRegionDecoder(this.mContentType) && !this.mIsDrm) {
            supported |= 64;
        }
        if (this.mVoiceOffset > 0) {
            supported |= 134217728;
        }
        if (this.mOutputUri != null) {
            supported |= 524288;
        }
        return supported & -3;
    }

    private boolean isSharable() {
        return "file".equals(this.mUri.getScheme());
    }

    private boolean isBlackList() {
        if (this.mFilePath == null) {
            return false;
        }
        BlackList blackList = BlackList.getInstance();
        int start = 0;
        int i = 0;
        while (i < this.mFilePath.length() - 1) {
            start = i;
            if (this.mFilePath.charAt(i) == '/' && this.mFilePath.charAt(i + 1) != '/') {
                break;
            }
            i++;
        }
        return blackList.match(this.mFilePath.substring(start).toLowerCase());
    }

    public int getMediaType() {
        return 2;
    }

    public Uri getContentUri() {
        return this.mUri;
    }

    public MediaDetails getDetails() {
        MediaDetails details = super.getDetails();
        if (!(this.mWidth == 0 || this.mHeight == 0)) {
            details.addDetail(6, Integer.valueOf(this.mWidth));
            details.addDetail(7, Integer.valueOf(this.mHeight));
        }
        if (this.mContentType != null) {
            details.addDetail(9, this.mContentType);
        }
        if ("file".equals(this.mUri.getScheme())) {
            String filePath = this.mUri.getPath();
            details.addDetail(SmsCheckResult.ESCT_200, filePath);
            MediaDetails.extractExifInfo(details, filePath);
        }
        return details;
    }

    public String getMimeType() {
        return this.mContentType;
    }

    protected void finalize() throws Throwable {
        try {
            if (this.mFileDescriptor != null) {
                Utils.closeSilently(this.mFileDescriptor);
            }
            if (this.mParcelFileDescriptor != null) {
                Utils.closeSilently(this.mParcelFileDescriptor);
            }
            super.finalize();
        } catch (Throwable th) {
            super.finalize();
        }
    }

    public int getWidth() {
        return 0;
    }

    public int getHeight() {
        return 0;
    }

    public int getRotation() {
        return this.mRotation;
    }

    public boolean isDrm() {
        return this.mIsDrm;
    }

    public int getDrmType() {
        return DrmUtils.getObjectType(this.mFilePath);
    }

    public int getRightCount() {
        return DrmUtils.getRightCount(this.mFilePath, 7);
    }

    public boolean hasRight() {
        return DrmUtils.haveRightsForAction(this.mFilePath, 7);
    }

    public boolean getRight() {
        return DrmUtils.haveRightsForAction(this.mFilePath, ApiHelper.DRMSTORE_ACTION_SHOW_DIALOG | 7);
    }

    public boolean canForward() {
        return DrmUtils.canForward(this.mFilePath);
    }

    public boolean hasCountConstraint() {
        return DrmUtils.haveCountConstraints(this.mFilePath, 7);
    }

    public long getVoiceOffset() {
        return this.mVoiceOffset;
    }

    public boolean isVoiceImage() {
        return this.mVoiceOffset > 0;
    }

    private void CalcVoiceOffset() {
        FileNotFoundException e;
        Throwable th;
        String scheme = this.mUri.getScheme();
        long offSet;
        if ("file".equals(scheme)) {
            String path = this.mUri.getPath();
            if (path != null) {
                offSet = Utils.getVoiceOffset(path);
                if (offSet > 0) {
                    this.mVoiceOffset = offSet;
                }
            }
        } else if ("content".equals(scheme)) {
            Closeable closeable = null;
            try {
                this.mParcelFileDescriptor = this.mApplication.getContentResolver().openFileDescriptor(this.mUri, "r");
                if (this.mParcelFileDescriptor != null) {
                    FileInputStream in = new FileInputStream(this.mParcelFileDescriptor.getFileDescriptor());
                    Object in2;
                    try {
                        offSet = Utils.getVoiceOffset(in);
                        if (offSet > 0) {
                            this.mVoiceOffset = offSet;
                            closeable = in;
                        } else {
                            in2 = in;
                        }
                    } catch (FileNotFoundException e2) {
                        e = e2;
                        in2 = in;
                        try {
                            GalleryLog.w("UriImage", "CalcVoiceOffset fail to open: " + this.mUri + "." + e.getMessage());
                            Utils.closeSilently(closeable);
                            closeParcelFileDescriptor();
                        } catch (Throwable th2) {
                            th = th2;
                            Utils.closeSilently(closeable);
                            closeParcelFileDescriptor();
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        in2 = in;
                        Utils.closeSilently(closeable);
                        closeParcelFileDescriptor();
                        throw th;
                    }
                }
                Utils.closeSilently(closeable);
                closeParcelFileDescriptor();
            } catch (FileNotFoundException e3) {
                e = e3;
                GalleryLog.w("UriImage", "CalcVoiceOffset fail to open: " + this.mUri + "." + e.getMessage());
                Utils.closeSilently(closeable);
                closeParcelFileDescriptor();
            }
        }
    }

    public String getFilePath() {
        if (this.mFilePath == null) {
            return super.getFilePath();
        }
        return this.mFilePath;
    }

    public FileDescriptor getFileDescriptor() {
        closeParcelFileDescriptor();
        FileDescriptor fd = null;
        try {
            this.mParcelFileDescriptor = this.mApplication.getContentResolver().openFileDescriptor(this.mUri, "r");
            if (this.mParcelFileDescriptor != null) {
                fd = this.mParcelFileDescriptor.getFileDescriptor();
            }
            return fd;
        } catch (FileNotFoundException e) {
            GalleryLog.w("UriImage", "getFileDescriptor fail to open: " + this.mUri + "." + e.getMessage());
            return null;
        } catch (Throwable th) {
            return null;
        }
    }

    public void closeParcelFileDescriptor() {
        if (this.mParcelFileDescriptor != null) {
            Utils.closeSilently(this.mParcelFileDescriptor);
            this.mParcelFileDescriptor = null;
        }
    }

    public String getName() {
        return this.mFileName;
    }

    private void resolveFileName(Uri uri) {
        String scheme = uri.getScheme();
        if ("file".equals(scheme) || "content".equals(scheme)) {
            try {
                String name = new File(this.mFilePath).getName();
                int end = name.lastIndexOf(".");
                if (end < 0) {
                    this.mFileName = name;
                } else {
                    this.mFileName = name.substring(0, end);
                }
            } catch (Exception e) {
            }
        }
    }

    public boolean saveToOutput() {
        if (this.mUri == null || this.mOutputUri == null) {
            return false;
        }
        String outputFileName = getOutputFileName();
        if (outputFileName == null) {
            return false;
        }
        File outputFile = new File(outputFileName);
        File parentFile = outputFile.getParentFile();
        if (parentFile == null || parentFile.exists() || parentFile.mkdirs()) {
            try {
                if (outputFile.exists() || outputFile.createNewFile()) {
                    Closeable closeable = null;
                    Closeable closeable2 = null;
                    try {
                        closeable = this.mApplication.getContentResolver().openInputStream(this.mUri);
                        closeable2 = this.mApplication.getContentResolver().openOutputStream(this.mOutputUri);
                        if (closeable2 == null) {
                            return false;
                        }
                        byte[] buffer = new byte[524288];
                        while (true) {
                            int readCount = closeable.read(buffer);
                            if (readCount != -1) {
                                closeable2.write(buffer, 0, readCount);
                            } else {
                                Utils.closeSilently(closeable);
                                Utils.closeSilently(closeable2);
                                return true;
                            }
                        }
                    } catch (FileNotFoundException e) {
                        GalleryLog.w("UriImage", "A FileNotFoundException has occurred in saveToOutput() method.");
                        return false;
                    } catch (IOException e2) {
                        GalleryLog.w("UriImage", "A IOException has occurred in saveToOutput() method.");
                        return false;
                    } finally {
                        Utils.closeSilently(closeable);
                        Utils.closeSilently(closeable2);
                    }
                } else {
                    GalleryLog.w("UriImage", "createNewFile fail:" + outputFileName);
                    return false;
                }
            } catch (IOException e3) {
                GalleryLog.w("UriImage", "File.creatNewFile() failed in saveToOutput() method, reason: IOException.");
                return false;
            }
        }
        GalleryLog.w("UriImage", "can not create dirs:" + outputFileName);
        return false;
    }

    public void setOutputUri(Uri uri) {
        this.mOutputUri = uri;
    }

    public String getOutputFileName() {
        if (this.mOutputUri == null) {
            return null;
        }
        return GalleryUtils.convertUriToPath(this.mApplication.getAndroidContext(), this.mOutputUri);
    }
}
