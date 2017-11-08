package com.android.gallery3d.app;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.MyPrinter;
import com.huawei.gallery.video.ExtractAndMuxVideo;
import com.huawei.gallery.video.ExtractAndMuxVideo.VideoMuxListener;
import com.huawei.gallery.video.VideoUtils;
import com.huawei.gallery.video.VideoUtils.VideoSplitListener;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class MMShareInterceptor implements Callback {
    private static final MyPrinter LOG = new MyPrinter("MMShareInterceptor");
    private static final String[] PROJECTION = new String[]{"mime_type", "_data", "duration", "_size"};
    private int BLOCK_TIME_LINE = 180;
    private int BLOCK_TIME_WEIGHT = 2;
    private String MM_PACKAGE = "com.tencent.mm";
    private int SIZE_THRESHOLD = 20971520;
    private Activity mActivity;
    private int mBlockCount = 1;
    private List<String> mCachedFileList = new ArrayList(20);
    private String mCancelStr = null;
    private boolean mCanceled = false;
    private String mCompressStr = null;
    private String mCompressedVideo;
    private int mCurrentShared = 1;
    private int mDurationInSec;
    Intent mFileIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
    private String mFilePath;
    private long mFileSize;
    Intent mFolderIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FOLDER");
    private Handler mHandler;
    private String mReportTypeForBigdata;
    private String mSendStr = null;
    private VideoSendInfoDialog mShareDialog;
    private Intent mShareIntent;
    private String mSharedFile = null;
    private Thread mSplitThread;
    private int mTotalCount = 0;
    private CancelAndConfirmProgress mVideoProgress;
    private transient boolean mWaiting = false;
    private Object mWaitingLock = new Object();
    private int mWeightBase = 100;

    private class CancelAndConfirmProgress extends ProgressDialog implements OnClickListener, OnCancelListener {
        boolean mAttachedToWindow = false;
        AlertDialog mDialog = null;
        CancelAndConfirmProgress thiz;

        CancelAndConfirmProgress(Activity activity) {
            super(activity);
            setTitle(R.string.video_title_compressing);
            setMax(100);
            setProgressNumberFormat(null);
            setProgressStyle(1);
            setButton(-2, MMShareInterceptor.this.mCancelStr, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    CancelAndConfirmProgress.this.thiz.dismiss();
                    CancelAndConfirmProgress.this.confirmCancel();
                }
            });
            setCancelable(false);
            this.thiz = this;
        }

        public void onAttachedToWindow() {
            this.mAttachedToWindow = true;
            MMShareInterceptor.LOG.d("[onAttachedToWindow] mAttachedToWindow " + this.mAttachedToWindow + ", self " + this);
            super.onAttachedToWindow();
        }

        public void onDetachedFromWindow() {
            this.mAttachedToWindow = false;
            MMShareInterceptor.LOG.d("[onDetachedFromWindow] mAttachedToWindow " + this.mAttachedToWindow + ", self " + this);
            super.onDetachedFromWindow();
        }

        public void dismiss() {
            MMShareInterceptor.LOG.d("[dismiss] mAttachedToWindow " + this.mAttachedToWindow + ", self " + this);
            if (this.mAttachedToWindow) {
                super.dismiss();
            }
        }

        private void confirmCancel() {
            setWaiting(true);
            String percent = GalleryUtils.getPercentString((float) getProgress(), 0);
            String message = MMShareInterceptor.this.mActivity.getString(R.string.video_content_confirm_cancle, new Object[]{percent});
            if (this.mDialog == null) {
                this.mDialog = new Builder(MMShareInterceptor.this.mActivity).setTitle(R.string.video_title_stop).setMessage(message).setNegativeButton(R.string.video_btn_continue, this).setPositiveButton(R.string.video_btn_stop, this).setOnCancelListener(this).setCancelable(true).create();
            }
            this.mDialog.setMessage(message);
            this.mDialog.show();
        }

        public void onCancel(DialogInterface dialog) {
            setWaiting(false);
            this.thiz.show();
        }

        public void cancelCompress() {
            boolean isShowing = isShowing();
            if (isShowing) {
                this.thiz.dismiss();
            } else if (this.mDialog != null && this.mDialog.isShowing()) {
                isShowing = true;
                this.mDialog.dismiss();
            }
            if (isShowing) {
                setWaiting(false);
                MMShareInterceptor.this.cancelShare();
            }
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    this.thiz.show();
                    break;
                case -1:
                    MMShareInterceptor.this.mCanceled = true;
                    MMShareInterceptor.this.cancelShare();
                    break;
            }
            setWaiting(false);
        }

        private void setWaiting(boolean waiting) {
            synchronized (MMShareInterceptor.this.mWaitingLock) {
                MMShareInterceptor.LOG.d("[setWaiting] " + waiting);
                MMShareInterceptor.this.mWaiting = waiting;
                MMShareInterceptor.this.mWaitingLock.notifyAll();
            }
        }
    }

    class MuxListener implements VideoMuxListener {
        private boolean mStarted = false;
        private int mWeight = 60;

        MuxListener(int weight) {
            this.mWeight = weight;
        }

        public void onProgress(int progress) {
            MMShareInterceptor.this.updateProgress((this.mWeight * progress) / 100);
        }

        public void onCompressStart(String targetHint) {
            this.mStarted = true;
            MMShareInterceptor.this.addToPreference(targetHint);
        }

        public void onCompressDone(String target) {
            boolean started = this.mStarted;
            this.mStarted = false;
            boolean failed = false;
            if (target == null) {
                failed = true;
                MMShareInterceptor.this.clear();
            }
            if (failed) {
                MMShareInterceptor.this.endProgress();
                MMShareInterceptor.this.mHandler.sendEmptyMessage(9);
            } else if (MMShareInterceptor.this.mCanceled) {
                MMShareInterceptor.this.deleteVideoFile(target);
                MMShareInterceptor.this.endProgress();
            } else {
                MMShareInterceptor.this.mCompressedVideo = target;
                MMShareInterceptor.this.addToPreference(target);
                MMShareInterceptor.LOG.d("complete compress video");
                File dst = new File(target);
                if (dst.exists() && started) {
                    MMShareInterceptor.this.mFilePath = target;
                    MMShareInterceptor.this.mFileSize = dst.length();
                    MMShareInterceptor.LOG.d("compressed video size is  " + ((dst.length() / 1024) / 1024) + "M, compare to " + ((MMShareInterceptor.this.SIZE_THRESHOLD / 1024) / 1024) + "M");
                    if (dst.length() <= ((long) MMShareInterceptor.this.SIZE_THRESHOLD)) {
                        MMShareInterceptor.this.scanFile(target);
                        MMShareInterceptor.this.endProgress();
                        MMShareInterceptor.this.mHandler.obtainMessage(5, target).sendToTarget();
                        MMShareInterceptor.this.mReportTypeForBigdata = "Compress";
                    } else {
                        MMShareInterceptor.this.mHandler.sendEmptyMessage(2);
                    }
                    return;
                }
                MMShareInterceptor.this.endProgress();
            }
        }

        public boolean isCanceled() {
            return MMShareInterceptor.this.isCanceledOrWaiting();
        }
    }

    class SplitListener implements VideoSplitListener {
        int mBase = 60;
        int mWeight = 40;

        SplitListener(int base) {
            this.mBase = base;
            this.mWeight = 100 - base;
        }

        public boolean isCanceled() {
            return MMShareInterceptor.this.isCanceledOrWaiting();
        }

        public void onStart(int countToBeSplit) {
            MMShareInterceptor.LOG.d("onStart");
            MMShareInterceptor.this.updateProgress(this.mBase);
            MMShareInterceptor.this.mTotalCount = countToBeSplit;
        }

        public void onProgress(int index, String filepath) {
            MMShareInterceptor.LOG.d(String.format("onProgress(int index=%s, String filepath=%s)", new Object[]{Integer.valueOf(index), filepath}));
            MMShareInterceptor.this.updateProgress(this.mBase + ((this.mWeight * index) / MMShareInterceptor.this.mTotalCount));
            MMShareInterceptor.this.mCachedFileList.add(filepath);
            MMShareInterceptor.this.addToPreference(filepath);
            MMShareInterceptor.this.scanFile(filepath);
        }

        public void onEnd() {
            MMShareInterceptor.LOG.d("onEnd");
            MMShareInterceptor.this.deleteVideoFile(MMShareInterceptor.this.mCompressedVideo);
            MMShareInterceptor.this.endProgress();
            if (!MMShareInterceptor.this.mCanceled) {
                MMShareInterceptor.this.mHandler.obtainMessage(7, MMShareInterceptor.this.mTotalCount, 1).sendToTarget();
                MMShareInterceptor.this.mReportTypeForBigdata = "CompressAndTrip";
            }
        }
    }

    private class VideoSendInfoDialog implements OnClickListener {
        AlertDialog mDialog;
        TextView mMessageView;

        VideoSendInfoDialog() {
            ContextThemeWrapper context = GalleryUtils.getHwThemeContext(MMShareInterceptor.this.mActivity, "androidhwext:style/Theme.Emui.Dialog");
            View layout = LayoutInflater.from(context).inflate(R.layout.videoshare_send_info, null);
            int padding = context.getResources().getDimensionPixelSize(R.dimen.alter_dialog_padding_left_right);
            this.mDialog = new Builder(context).setTitle(R.string.video_title_send).setNegativeButton(MMShareInterceptor.this.mCancelStr, this).setPositiveButton(MMShareInterceptor.this.mSendStr, this).create();
            this.mDialog.setView(layout, padding, 0, padding, 0);
            this.mMessageView = (TextView) layout.findViewById(R.id.video_info);
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -2:
                    MMShareInterceptor.this.cancelShare();
                    this.mDialog.dismiss();
                    return;
                case -1:
                    MMShareInterceptor.this.mHandler.sendEmptyMessage(3);
                    return;
                default:
                    return;
            }
        }

        private void showMessage(int total, int current) {
            String msg;
            if (current == 1) {
                msg = MMShareInterceptor.this.mActivity.getResources().getQuantityString(R.plurals.video_content_comfirm_first, total, new Object[]{Integer.valueOf(total)});
            } else {
                msg = MMShareInterceptor.this.mActivity.getResources().getQuantityString(R.plurals.video_content_confirm_next, total, new Object[]{Integer.valueOf(total), Integer.valueOf(current)});
            }
            this.mMessageView.setText(msg);
            this.mDialog.show();
        }
    }

    public MMShareInterceptor(Activity activity) {
        this.mActivity = activity;
        this.mHandler = new Handler(this);
        this.mCancelStr = this.mActivity.getString(R.string.video_btn_cancel);
        this.mSendStr = this.mActivity.getString(R.string.video_btn_send);
        this.mCompressStr = this.mActivity.getString(R.string.video_btn_compress);
        this.mVideoProgress = new CancelAndConfirmProgress(this.mActivity);
        this.mShareDialog = new VideoSendInfoDialog();
    }

    private boolean setVideo(Uri uri) {
        if (!updateFileInfo(uri)) {
            return false;
        }
        this.mBlockCount = ((this.mDurationInSec + this.BLOCK_TIME_LINE) - 1) / this.BLOCK_TIME_LINE;
        if (this.mDurationInSec == 0 || this.mBlockCount == 0 || this.mFilePath == null) {
            this.mFileSize = 0;
            return false;
        }
        this.mWeightBase = (this.mDurationInSec * 100) / (this.mDurationInSec + (this.mBlockCount * this.BLOCK_TIME_WEIGHT));
        LOG.d("file size(M) is : " + ((this.mFileSize / 1024) / 1024));
        LOG.d("compress video weight is " + this.mWeightBase);
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean updateFileInfo(Uri uri) {
        this.mFilePath = null;
        this.mFileSize = 0;
        this.mDurationInSec = 0;
        if (uri == null) {
            LOG.d("setVideo uri == null");
            return false;
        }
        String scheme = uri.getScheme();
        if ("content".equals(scheme)) {
            try {
                Closeable cursor = this.mActivity.getContentResolver().query(uri, PROJECTION, null, null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    String mimeType = cursor.getString(0);
                    if (mimeType == null || !mimeType.startsWith("video")) {
                        Utils.closeSilently(cursor);
                        return false;
                    }
                    this.mFilePath = cursor.getString(1);
                    this.mDurationInSec = cursor.getInt(2) / 1000;
                    this.mFileSize = cursor.getLong(3);
                }
                Utils.closeSilently(cursor);
                LOG.d(String.format("parameters:{filePath = %s; fileSize = %s; durationInSec = %s; }", new Object[]{this.mFilePath, Long.valueOf(this.mFileSize), Integer.valueOf(this.mDurationInSec)}));
                return true;
            } catch (SQLiteException e) {
                GalleryLog.w("MMShareInterceptor", "Given Uri is not formatted in a way so that it can be found in media store.");
                return false;
            } catch (IllegalArgumentException e2) {
                GalleryLog.w("MMShareInterceptor", "Illegal Uri " + uri);
                return false;
            } catch (Exception e3) {
                GalleryLog.w("MMShareInterceptor", "error . " + uri + ". " + e3.getMessage());
                return false;
            } catch (Throwable th) {
                Utils.closeSilently(null);
            }
        } else {
            LOG.d("setVideo scheme = " + scheme);
            return false;
        }
    }

    public void clearCachedFile() {
        this.mHandler.sendEmptyMessage(6);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onShare(Intent shareToIntent) {
        if (!this.MM_PACKAGE.equals(shareToIntent.getComponent().getPackageName())) {
            return false;
        }
        String action = shareToIntent.getAction();
        if (TextUtils.isEmpty(action) || !"android.intent.action.SEND".equals(action) || !setVideo((Uri) shareToIntent.getParcelableExtra("android.intent.extra.STREAM")) || ((long) this.SIZE_THRESHOLD) > this.mFileSize) {
            return false;
        }
        String filepath = this.mFilePath;
        if (filepath == null) {
            return false;
        }
        if (filepath.endsWith("mp4") || filepath.endsWith("MP4")) {
            this.mCurrentShared = 1;
            this.mHandler.sendEmptyMessage(1);
            this.mShareIntent = new Intent(shareToIntent);
            return true;
        }
        LOG.d("only mp4 file can be compressed.");
        return false;
    }

    private void dealTooLarge() {
        String msgStr;
        int count = this.mBlockCount;
        if (count > 1) {
            msgStr = this.mActivity.getResources().getQuantityString(R.plurals.video_content_with_split, count, new Object[]{Integer.valueOf(count)});
        } else {
            msgStr = this.mActivity.getString(R.string.video_content_no_split);
        }
        Builder builder = new Builder(this.mActivity).setTitle(R.string.video_title_compress).setMessage(msgStr).setNegativeButton(this.mCancelStr, null);
        builder.setPositiveButton(this.mCompressStr, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                GalleryStorage galleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
                if (galleryStorage == null || !GalleryUtils.hasSpaceForSize((long) (MMShareInterceptor.this.mBlockCount * MMShareInterceptor.this.SIZE_THRESHOLD), galleryStorage.getPath())) {
                    MMShareInterceptor.this.mHandler.sendEmptyMessage(8);
                } else {
                    MMShareInterceptor.this.mHandler.sendEmptyMessage(4);
                }
            }
        });
        builder.create().show();
    }

    private void dealCutVideo() {
        try {
            if (this.mSplitThread != null) {
                this.mSplitThread.interrupt();
                this.mSplitThread.join();
            }
        } catch (InterruptedException e) {
            GalleryLog.i("MMShareInterceptor", "Thread.join() failed, reason: InterruptedException.");
        }
        this.mSplitThread = new Thread() {
            public void run() {
                VideoUtils.splitVideo(MMShareInterceptor.this.mFilePath, MMShareInterceptor.this.mFileSize, MMShareInterceptor.this.mDurationInSec, new SplitListener(MMShareInterceptor.this.mWeightBase));
                MMShareInterceptor.this.mSplitThread = null;
            }
        };
        this.mSplitThread.start();
    }

    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                dealTooLarge();
                return true;
            case 2:
                dealCutVideo();
                return true;
            case 3:
                if (this.mCachedFileList.isEmpty()) {
                    return true;
                }
                shareToMM((String) this.mCachedFileList.remove(0), true);
                return true;
            case 4:
                startProgress();
                ExtractAndMuxVideo.extractDecodeEditEncodeMuxAudioVideo(this.mFilePath, new MuxListener(this.mWeightBase));
                return true;
            case 5:
                LOG.d("call shareToMM");
                shareToMM(msg.obj.toString(), true);
                return true;
            case 6:
                checkCachedFile();
                return true;
            case 7:
                this.mShareDialog.showMessage(msg.arg1, msg.arg2);
                return true;
            case 8:
                new Builder(this.mActivity).setTitle(R.string.insufficient_storage_space).setNegativeButton(R.string.i_know, null).create().show();
                return true;
            case 9:
                new Builder(this.mActivity).setTitle(R.string.photoshare_dialog_title_account_invalid).setMessage(R.string.video_fail_message).setPositiveButton(R.string.ok, null).create().show();
                return true;
            default:
                return false;
        }
    }

    private void startProgress() {
        this.mCanceled = false;
        this.mVideoProgress.show();
        this.mVideoProgress.setProgress(0);
    }

    private void updateProgress(int progress) {
        this.mVideoProgress.setProgress(progress);
    }

    private void cancelShare() {
        LOG.d("share will canceled !!! ");
        this.mCanceled = true;
        clear();
        this.mReportTypeForBigdata = "Interrupt";
        reportBigDataForMM();
    }

    private void endProgress() {
        this.mVideoProgress.dismiss();
    }

    private void shareToMM(String filepath, boolean forResult) {
        if (this.mVideoProgress.isShowing()) {
            LOG.d("wrong state, compress progress is showing");
            return;
        }
        Intent shareToIntent = new Intent(this.mShareIntent);
        int flag = shareToIntent.getFlags();
        shareToIntent.putExtra("android.intent.extra.STREAM", Uri.fromFile(new File(filepath)));
        shareToIntent.setFlags(-50331649 & flag);
        LOG.d("set intent to MM .  file " + filepath);
        this.mSharedFile = filepath;
        if (forResult) {
            GalleryUtils.startActivityForResultCatchSecurityEx(this.mActivity, shareToIntent, 9999);
        } else {
            GalleryUtils.startActivityCatchSecurityEx(this.mActivity, shareToIntent);
        }
        reportBigDataForMM();
    }

    private void reportBigDataForMM() {
        if (this.mReportTypeForBigdata != null) {
            ReportToBigData.report(53, String.format("{WeChatVideo:%s}", new Object[]{this.mReportTypeForBigdata}));
            this.mReportTypeForBigdata = null;
        }
    }

    public void onPause() {
        this.mVideoProgress.cancelCompress();
    }

    public void onResult() {
        if (this.mSharedFile != null) {
            this.mSharedFile = null;
        }
        LOG.d("current progress is " + this.mCurrentShared);
        if (!this.mCachedFileList.isEmpty()) {
            Handler handler = this.mHandler;
            int i = this.mTotalCount;
            int i2 = this.mCurrentShared + 1;
            this.mCurrentShared = i2;
            handler.obtainMessage(7, i, i2).sendToTarget();
        }
    }

    private boolean isCanceledOrWaiting() {
        synchronized (this.mWaitingLock) {
            while (this.mWaiting) {
                try {
                    LOG.d("waiting confirm .... ");
                    this.mWaitingLock.wait();
                } catch (Exception ex) {
                    LOG.d("ingore exception", ex);
                }
            }
        }
        return this.mCanceled;
    }

    private void checkCachedFile() {
        try {
            SharedPreferences preferences = this.mActivity.getSharedPreferences(this.MM_PACKAGE, 0);
            for (String file : new HashSet(preferences.getAll().keySet())) {
                deleteVideoFile(file);
            }
            for (File file2 : VideoUtils.SAVE_TO.listFiles()) {
                LOG.w("delete file " + file2.getAbsolutePath() + ", result is " + file2.delete());
            }
            preferences.edit().clear();
        } catch (Throwable th) {
            LOG.d("clear cached file for MM, failded !");
        }
    }

    private void addToPreference(String filepath) {
        if (filepath != null) {
            Editor editor = this.mActivity.getSharedPreferences(this.MM_PACKAGE, 0).edit();
            editor.putBoolean(filepath, true);
            editor.apply();
            editor.commit();
        }
    }

    private void removeFromPreference(String filepath) {
        Editor editor = this.mActivity.getSharedPreferences(this.MM_PACKAGE, 0).edit();
        editor.remove(filepath);
        editor.apply();
        editor.commit();
    }

    private void clear() {
        if (this.mCompressedVideo != null) {
            deleteVideoFile(this.mCompressedVideo);
            this.mCompressedVideo = null;
        }
        for (String splitedVideo : this.mCachedFileList) {
            deleteVideoFile(splitedVideo);
        }
        this.mCachedFileList.clear();
        scanFolder(VideoUtils.SAVE_TO);
    }

    private void deleteVideoFile(String videopath) {
        if (videopath == null) {
            LOG.d("path is null, nothing to delete");
            return;
        }
        File video = new File(videopath);
        removeFromPreference(videopath);
        if (video.exists()) {
            LOG.d("delete file " + videopath + ", result is " + video.delete());
        }
        scanFile(videopath);
    }

    private void scanFile(String file) {
        this.mActivity.sendBroadcast(new Intent(this.mFileIntent).setData(Uri.parse("file://" + file)));
    }

    private void scanFolder(File folder) {
        this.mActivity.sendBroadcast(new Intent(this.mFolderIntent).setData(Uri.fromFile(folder)));
    }
}
