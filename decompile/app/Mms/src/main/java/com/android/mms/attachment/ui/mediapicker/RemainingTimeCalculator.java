package com.android.mms.attachment.ui.mediapicker;

import android.content.Context;
import android.os.StatFs;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import com.android.mms.MmsApp;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.util.Locale;

public class RemainingTimeCalculator {
    private static String mExternalStorage;
    private static String mInternalStorage;
    private static RemainingTimeCalculator mRemainingTimeCalculator = null;
    private long mBlocksChangedTime;
    private int mBytesPerSecond;
    private long mCurrentBlocksLimit = 5120;
    private int mCurrentLowerLimit = 0;
    private long mFileSizeChangedTime;
    private long mLastBlocks;
    private long mLastFileSize;
    private long mLimitedTime = -1;
    private long mMaxBytes;
    private File mRecordingFile;
    private File mSDCardDirectory;
    private long mStartTime = 0;

    private RemainingTimeCalculator() {
        initStorage(MmsApp.getApplication().getApplicationContext());
        resetSDCardDirectory();
        if (diskSpaceAvailable()) {
            getCurrentLimitAvailableSize(this.mSDCardDirectory.getAbsolutePath());
        } else {
            switchStoragePath();
        }
    }

    private void resetSDCardDirectory() {
        if (SystemProperties.get("persist.sys.primarysd", "0").equals("1")) {
            this.mSDCardDirectory = getLocalPath(mExternalStorage);
        } else {
            this.mSDCardDirectory = getLocalPath(mInternalStorage);
        }
    }

    public static synchronized RemainingTimeCalculator getInstance() {
        RemainingTimeCalculator remainingTimeCalculator;
        synchronized (RemainingTimeCalculator.class) {
            if (mRemainingTimeCalculator == null) {
                mRemainingTimeCalculator = new RemainingTimeCalculator();
            }
            remainingTimeCalculator = mRemainingTimeCalculator;
        }
        return remainingTimeCalculator;
    }

    public void setFileSizeLimit(File file, long maxBytes) {
        this.mRecordingFile = file;
        this.mMaxBytes = maxBytes;
    }

    public void reset() {
        this.mCurrentLowerLimit = 0;
        this.mBlocksChangedTime = -1;
        this.mFileSizeChangedTime = -1;
        this.mRecordingFile = null;
        this.mMaxBytes = 0;
    }

    public long timeRemaining() {
        IllegalArgumentException e;
        try {
            StatFs fs = new StatFs(this.mSDCardDirectory.getAbsolutePath());
            try {
                long blocks = (long) fs.getAvailableBlocks();
                long blockSize = (long) fs.getBlockSize();
                long now = System.currentTimeMillis();
                if (this.mBlocksChangedTime == -1 || blocks != this.mLastBlocks) {
                    this.mBlocksChangedTime = now;
                    this.mLastBlocks = blocks;
                }
                long result = (((this.mLastBlocks - this.mCurrentBlocksLimit) * blockSize) / ((long) this.mBytesPerSecond)) - ((now - this.mBlocksChangedTime) / 1000);
                if (this.mRecordingFile == null) {
                    this.mCurrentLowerLimit = 2;
                    return result;
                }
                this.mRecordingFile = new File(this.mRecordingFile.getAbsolutePath());
                long fileSize = this.mRecordingFile.length();
                if (this.mFileSizeChangedTime == -1 || fileSize != this.mLastFileSize) {
                    this.mFileSizeChangedTime = now;
                    this.mLastFileSize = fileSize;
                }
                long result2 = (((this.mMaxBytes - fileSize) / ((long) this.mBytesPerSecond)) - ((now - this.mFileSizeChangedTime) / 1000)) - 1;
                this.mCurrentLowerLimit = result < result2 ? 2 : 1;
                if (this.mLimitedTime == -1) {
                    return Math.min(result, result2);
                }
                return Math.min(Math.min(result, result2), (this.mLimitedTime - (now - this.mStartTime)) / 1000);
            } catch (IllegalArgumentException e2) {
                e = e2;
                MLog.w("RemainTimeCalculator", "timeRemaining : IllegalArgumentException = " + e.getMessage());
                return -1;
            }
        } catch (IllegalArgumentException e3) {
            e = e3;
            MLog.w("RemainTimeCalculator", "timeRemaining : IllegalArgumentException = " + e.getMessage());
            return -1;
        }
    }

    public int currentLowerLimit() {
        return this.mCurrentLowerLimit;
    }

    public boolean diskSpaceAvailable() {
        return diskSpaceAvailable(this.mSDCardDirectory.getAbsolutePath());
    }

    public boolean diskSpaceAvailable(String path) {
        boolean z = false;
        try {
            StatFs fs = new StatFs(path);
            long nAvailaBlock = (long) fs.getAvailableBlocks();
            long blockSizeInBytes = (long) fs.getBlockSize();
            long nSDFreeSize = nAvailaBlock * blockSizeInBytes;
            MLog.d("RemainTimeCalculator", "diskSpaceAvailable : fs.getAvailableBlocks() = " + nAvailaBlock + "  fs.getBlockSize() = " + blockSizeInBytes + " nSDFreeSize = " + nSDFreeSize);
            if (nSDFreeSize > 20971520) {
                z = true;
            }
            return z;
        } catch (IllegalArgumentException e) {
            MLog.w("RemainTimeCalculator", "diskSpaceAvailable : IllegalArgumentException = " + e.getMessage());
            return false;
        }
    }

    private void getCurrentLimitAvailableSize(String path) {
        try {
            int blockScale = new StatFs(path).getBlockSize() / 4096;
            if (blockScale == 0) {
                blockScale = 1;
            }
            this.mCurrentBlocksLimit = (long) (5120 / blockScale);
            MLog.d("RemainTimeCalculator", "getCurrentLimitAvailableSize : mCurrentBlocksLimit = " + this.mCurrentBlocksLimit + " blockScale = " + blockScale);
        } catch (IllegalArgumentException e) {
            MLog.w("RemainTimeCalculator", "getCurrentLimitAvailableSize : IllegalArgumentException = " + e.getMessage());
        }
    }

    public void setBitRate(int bitRate) {
        this.mBytesPerSecond = bitRate / 8;
    }

    public static File getLocalPath(String path) {
        return path == null ? new File("") : new File(path);
    }

    public void switchStoragePath() {
        resetSDCardDirectory();
        if (!diskSpaceAvailable()) {
            if (getLocalPath(mExternalStorage).exists() && diskSpaceAvailable(mExternalStorage)) {
                this.mSDCardDirectory = getLocalPath(mExternalStorage);
            } else if (this.mSDCardDirectory.getAbsolutePath().equals(mExternalStorage) && getLocalPath(mInternalStorage).exists()) {
                this.mSDCardDirectory = getLocalPath(mInternalStorage);
            }
        }
        getCurrentLimitAvailableSize(this.mSDCardDirectory.getAbsolutePath());
    }

    public static void initStorage(Context context) {
        StorageVolume[] volumes = ((StorageManager) context.getSystemService("storage")).getVolumeList();
        if (volumes != null) {
            for (StorageVolume volume : volumes) {
                String path = volume == null ? null : volume.getPath();
                if (path != null) {
                    if (volume == null || volume.isEmulated()) {
                        setmInternalStorage(path);
                    } else {
                        if (!(!path.toLowerCase(Locale.ENGLISH).startsWith("/mnt/usb") ? path.toLowerCase(Locale.ENGLISH).startsWith("/storage/usb") : true)) {
                            setmExternalStorage(path);
                        }
                    }
                }
            }
            if (!getLocalPath(mExternalStorage).exists()) {
                setmExternalStorage(mInternalStorage);
            }
            MLog.d("RemainTimeCalculator", "got ExternalStorage " + mExternalStorage + "\n mInternalStorage " + mInternalStorage);
        }
    }

    public static void setmExternalStorage(String externalStorage) {
        mExternalStorage = externalStorage;
    }

    public static void setmInternalStorage(String internalStorage) {
        mInternalStorage = internalStorage;
    }
}
