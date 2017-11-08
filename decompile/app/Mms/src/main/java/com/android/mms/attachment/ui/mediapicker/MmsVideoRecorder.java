package com.android.mms.attachment.ui.mediapicker;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.attachment.utils.SafeAsyncTask;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.text.SimpleDateFormat;
import java.util.Random;

public class MmsVideoRecorder extends MediaRecorder {
    private static final Random RANDOM_ID = new Random();
    private static final String externalDir = (Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES) + "/MmsCamera/");
    private static HwCustMmsVideoRecorder mHwCustMmsVideoRecorder = ((HwCustMmsVideoRecorder) HwCustUtils.createObj(HwCustMmsVideoRecorder.class, new Object[0]));
    private final CamcorderProfile mCamcorderProfile;
    private Uri mTempVideoUri;

    private static class CleanErrorCreateFileRunnable implements Runnable {
        private Uri tempUri;

        public CleanErrorCreateFileRunnable(Uri tempuri) {
            this.tempUri = tempuri;
        }

        public void run() {
            if (this.tempUri != null) {
                try {
                    File tempFile = new File(this.tempUri.getPath());
                    if (tempFile.exists() && tempFile.length() > 0 && !tempFile.delete()) {
                        MLog.e("MmsVideoRecorder", "remove error tempFile failed");
                    }
                } catch (Exception e) {
                    MLog.e("MmsVideoRecorder", "remove tempFile error ," + e);
                }
            }
        }
    }

    private static class CleanTempFileRunnable implements Runnable {
        private Uri tempUri;

        public CleanTempFileRunnable(Uri tempuri) {
            this.tempUri = tempuri;
        }

        public void run() {
            if (this.tempUri != null) {
                try {
                    File tempFile = new File(this.tempUri.getPath());
                    if (tempFile.exists() && tempFile.length() == 0 && !tempFile.delete()) {
                        MLog.e("MmsVideoRecorder", "remove tempFile failed");
                    }
                } catch (Exception e) {
                    MLog.e("MmsVideoRecorder", "remove tempFile error ," + e);
                }
            }
        }
    }

    public MmsVideoRecorder(Camera camera, int cameraIndex, int orientation, int maxMessageSize) throws FileNotFoundException {
        int mVideoQuality = 4;
        long mDurationSeconds = 25;
        if (!(mHwCustMmsVideoRecorder == null || mHwCustMmsVideoRecorder.getCustVideoParam() == null)) {
            mHwCustMmsVideoRecorder.initVideoParam();
            mVideoQuality = mHwCustMmsVideoRecorder.getVideoQuality();
            mDurationSeconds = mHwCustMmsVideoRecorder.getDurationSeconds();
        }
        this.mCamcorderProfile = CamcorderProfile.get(cameraIndex, mVideoQuality);
        this.mTempVideoUri = createOutputUri();
        long sizeLimit = (long) (((float) maxMessageSize) * ContentUtil.FONT_SIZE_NORMAL);
        int audioBitRate = this.mCamcorderProfile.audioBitRate;
        int videoBitRate = this.mCamcorderProfile.videoBitRate;
        double initialDurationLimit = ((double) (8 * sizeLimit)) / ((double) (audioBitRate + videoBitRate));
        if (initialDurationLimit < ((double) mDurationSeconds)) {
            double bitRateAdjustmentFactor = initialDurationLimit / ((double) mDurationSeconds);
            audioBitRate = (int) (((double) audioBitRate) * bitRateAdjustmentFactor);
            videoBitRate = (int) (((double) videoBitRate) * bitRateAdjustmentFactor);
        }
        setCamera(camera);
        setOrientationHint(orientation);
        setAudioSource(5);
        setVideoSource(1);
        setOutputFormat(this.mCamcorderProfile.fileFormat);
        if (this.mTempVideoUri != null) {
            setOutputFile(this.mTempVideoUri.getPath());
        }
        setAudioEncodingBitRate(audioBitRate);
        setAudioChannels(this.mCamcorderProfile.audioChannels);
        setAudioEncoder(this.mCamcorderProfile.audioCodec);
        setAudioSamplingRate(this.mCamcorderProfile.audioSampleRate);
        setVideoEncodingBitRate(videoBitRate);
        setVideoEncoder(this.mCamcorderProfile.videoCodec);
        setVideoFrameRate(this.mCamcorderProfile.videoFrameRate);
        setVideoSize(this.mCamcorderProfile.videoFrameWidth, this.mCamcorderProfile.videoFrameHeight);
        setMaxFileSize(sizeLimit);
    }

    Uri getVideoUri() {
        return this.mTempVideoUri;
    }

    int getVideoWidth() {
        return this.mCamcorderProfile.videoFrameWidth;
    }

    int getVideoHeight() {
        return this.mCamcorderProfile.videoFrameHeight;
    }

    void cleanupTempFile() {
        SafeAsyncTask.executeOnThreadPool(new CleanTempFileRunnable(this.mTempVideoUri));
    }

    void cleanupErrorFile() {
        if (this.mTempVideoUri != null) {
            SafeAsyncTask.executeOnThreadPool(new CleanErrorCreateFileRunnable(this.mTempVideoUri));
        }
    }

    String getContentType() {
        if (this.mCamcorderProfile.fileFormat == 2) {
            return "mp4";
        }
        return "3gpp";
    }

    private Uri createOutputUri() {
        Uri uri = null;
        String imageDate = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Long.valueOf(System.currentTimeMillis()));
        Uri tempUri = Uri.parse("file://" + externalDir + (String.format("MmsVideo_%s", new Object[]{imageDate}) + "." + getContentType()));
        File file = new File(externalDir);
        if (file.exists() || file.mkdirs()) {
            if (checkFileDir(tempUri)) {
                uri = tempUri;
            }
            return uri;
        }
        MLog.d("MmsVideoRecorder", "create dir failed:" + externalDir);
        return null;
    }

    private boolean checkFileDir(Uri uri) {
        try {
            File dirFile = new File(uri.getPath());
            if (dirFile.exists()) {
                return true;
            }
            return dirFile.createNewFile();
        } catch (Exception e) {
            MLog.e("MmsVideoRecorder", "checkFileDir failed," + e);
            return false;
        }
    }
}
