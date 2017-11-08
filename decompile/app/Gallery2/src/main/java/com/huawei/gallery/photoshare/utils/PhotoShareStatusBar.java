package com.huawei.gallery.photoshare.utils;

import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import com.android.gallery3d.R;
import com.huawei.gallery.app.PhotoShareDownloadActivity;
import com.huawei.gallery.app.PhotoShareUploadActivity;
import com.huawei.gallery.photoshare.receiver.PhotoShareNotificationDeleteIntentReceiver;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;

public class PhotoShareStatusBar {
    private final Context mContext;
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            PhotoShareStatusBar photoShareStatusBar;
            switch (msg.what) {
                case 0:
                    photoShareStatusBar = PhotoShareStatusBar.this;
                    synchronized (photoShareStatusBar) {
                        if (PhotoShareStatusBar.this.mPendingUpdateDownload <= 0 || PhotoShareStatusBar.this.mUpdateNotificationThread != null) {
                            if (PhotoShareStatusBar.this.mPendingUpdateDownload > 0) {
                                PhotoShareStatusBar.this.mHandler.sendMessageDelayed(PhotoShareStatusBar.this.mHandler.obtainMessage(0), 1000);
                                break;
                            }
                        }
                        PhotoShareStatusBar.this.mUpdateNotificationThread = new NotificationUpdateThread(true);
                        PhotoShareStatusBar.this.mUpdateNotificationThread.start();
                        PhotoShareStatusBar.this.mHandler.sendMessageDelayed(PhotoShareStatusBar.this.mHandler.obtainMessage(0), 1000);
                        break;
                    }
                    break;
                case 1:
                    photoShareStatusBar = PhotoShareStatusBar.this;
                    synchronized (photoShareStatusBar) {
                        if (PhotoShareStatusBar.this.mPendingUpdateUpload <= 0 || PhotoShareStatusBar.this.mUpdateNotificationThread != null) {
                            if (PhotoShareStatusBar.this.mPendingUpdateUpload > 0) {
                                PhotoShareStatusBar.this.mHandler.sendMessageDelayed(PhotoShareStatusBar.this.mHandler.obtainMessage(1), 1000);
                                break;
                            }
                        }
                        PhotoShareStatusBar.this.mUpdateNotificationThread = new NotificationUpdateThread(false);
                        PhotoShareStatusBar.this.mUpdateNotificationThread.start();
                        PhotoShareStatusBar.this.mHandler.sendMessageDelayed(PhotoShareStatusBar.this.mHandler.obtainMessage(1), 1000);
                        break;
                    }
                    break;
                case 2:
                    PhotoShareStatusBar.this.mNotificationManager.cancel("photoshare_down_up", 0);
                    PhotoShareUtils.enableDownloadStatusBarNotification(false);
                    return;
                case 3:
                    PhotoShareStatusBar.this.mNotificationManager.cancel("photoshare_down_up", 1);
                    PhotoShareUtils.enableUploadStatusBarNotification(false);
                    return;
                default:
                    return;
            }
        }
    };
    private final NotificationManager mNotificationManager;
    private int mPendingUpdateDownload = 0;
    private int mPendingUpdateUpload = 0;
    private NotificationUpdateThread mUpdateNotificationThread;

    private class NotificationUpdateThread extends Thread {
        private boolean mIsDownload;

        public NotificationUpdateThread(boolean isDownload) {
            super("Notification Update Thread");
            this.mIsDownload = isDownload;
        }

        public void run() {
            Process.setThreadPriority(10);
            synchronized (PhotoShareStatusBar.this) {
                if (PhotoShareStatusBar.this.mUpdateNotificationThread != this) {
                    throw new IllegalStateException("multiple UpdateThreads in PhotoShareDownloadNotificationManager");
                }
                if (this.mIsDownload) {
                    PhotoShareStatusBar.this.mPendingUpdateDownload = 0;
                } else {
                    PhotoShareStatusBar.this.mPendingUpdateUpload = 0;
                }
            }
            PhotoShareStatusBar.this.updateStatusBar(this.mIsDownload);
            synchronized (PhotoShareStatusBar.this) {
                PhotoShareStatusBar.this.mUpdateNotificationThread = null;
            }
        }
    }

    public PhotoShareStatusBar(Context context) {
        this.mContext = context;
        this.mNotificationManager = (NotificationManager) this.mContext.getSystemService("notification");
    }

    private String getTitle(int type) {
        int title;
        switch (type) {
            case 0:
                title = R.string.photoshare_statusbar_uploading;
                break;
            case 1:
                title = R.string.photoshare_statusbar_downloading;
                break;
            case 2:
                title = R.string.photoshare_statusbar_upload_finish;
                break;
            case 3:
                title = R.string.photoshare_statusbar_download_finish;
                break;
            default:
                title = R.string.photoshare_statusbar_download_finish;
                break;
        }
        return this.mContext.getString(title);
    }

    private String getContent(int type, boolean paused, int first, int second) {
        String content = "";
        switch (type) {
            case 0:
            case 1:
                if (paused) {
                    return this.mContext.getResources().getString(R.string.photoshare_statusbar_divide_paused, new Object[]{Integer.valueOf(first), Integer.valueOf(second)});
                }
                return this.mContext.getResources().getString(R.string.photoshare_statusbar_divide, new Object[]{Integer.valueOf(first), Integer.valueOf(second)});
            case 2:
            case 3:
                int successId = type == 2 ? R.plurals.photoshare_statusbar_upload_success : R.plurals.photoshare_statusbar_download_success;
                int failedId = type == 2 ? R.plurals.photoshare_statusbar_upload_failed : R.plurals.photoshare_statusbar_download_failed;
                String success = this.mContext.getResources().getQuantityString(successId, first, new Object[]{Integer.valueOf(first)});
                String failed = this.mContext.getResources().getQuantityString(failedId, second, new Object[]{Integer.valueOf(second)});
                return this.mContext.getResources().getString(R.string.photoshare_updown_finish_tips, new Object[]{success, failed});
            default:
                return content;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void updateNotification(boolean isDownload) {
        synchronized (this) {
            if (isDownload) {
                this.mPendingUpdateDownload++;
                if (this.mPendingUpdateDownload > 1) {
                } else if (!this.mHandler.hasMessages(0)) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(0));
                }
            } else {
                this.mPendingUpdateUpload++;
                if (this.mPendingUpdateUpload > 1) {
                } else if (!this.mHandler.hasMessages(1)) {
                    this.mHandler.sendMessage(this.mHandler.obtainMessage(1));
                }
            }
        }
    }

    public void updateStatusBar(boolean isDownload) {
        int runningCount;
        int waitingCount;
        int successCount;
        int pauseCount;
        int failedCount = 0;
        int type = 0;
        int first = 0;
        int second = 0;
        boolean paused = false;
        if (isDownload) {
            try {
                runningCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(1);
                waitingCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(2);
                successCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(16);
                failedCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(32);
                pauseCount = PhotoShareUtils.getServer().getDownloadFileInfoListCount(12);
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        } else {
            runningCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(1);
            waitingCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(2);
            successCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(16);
            failedCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(32);
            pauseCount = PhotoShareUtils.getServer().getUploadFileInfoListCount(12);
        }
        ComponentName componentName;
        Intent deleteIntent;
        boolean z;
        if (waitingCount == 0 && runningCount == 0) {
            if (pauseCount == 0) {
                if (isDownload) {
                    type = 3;
                } else {
                    type = 2;
                }
                first = successCount;
                second = failedCount;
            } else {
                paused = true;
                if (isDownload) {
                    type = 1;
                } else {
                    type = 0;
                }
                first = successCount + failedCount;
                second = (((successCount + failedCount) + waitingCount) + runningCount) + pauseCount;
            }
            if (first == 0) {
            }
            if (isDownload) {
                componentName = new ComponentName(this.mContext, PhotoShareDownloadActivity.class);
            } else {
                componentName = new ComponentName(this.mContext, PhotoShareUploadActivity.class);
            }
            Intent statusIntent = new Intent();
            statusIntent.setComponent(componentName);
            statusIntent.setAction("com.huawei.gallery.app.photoshare.statusbar.main");
            statusIntent.setFlags(268435456);
            Builder b = new Builder(this.mContext);
            b.setContentTitle(getTitle(type));
            b.setContentText(getContent(type, paused, first, second));
            b.setAutoCancel(true);
            b.setSmallIcon(getSmallIcon(type));
            b.setProgress(100, (first * 100) / second, false);
            b.setSubText(this.mContext.getString(R.string.transmission_notification_new, new Object[]{getCurrentProgressPercentString(((double) first) / ((double) second))}));
            b.setContentIntent(PendingIntent.getActivity(this.mContext, 0, statusIntent, 0));
            deleteIntent = new Intent();
            deleteIntent.setComponent(new ComponentName(this.mContext, PhotoShareNotificationDeleteIntentReceiver.class));
            if (isDownload) {
                deleteIntent.setAction("com.huawei.gallery.action.DOWNLOADNOTIFICATION_DELETE");
            } else {
                deleteIntent.setAction("com.huawei.gallery.action.UPLOADNOTIFICATION_DELETE");
            }
            b.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, deleteIntent, 0));
            if (isDownload) {
            }
            this.mNotificationManager.notify("photoshare_down_up", isDownload ? 0 : 1, b.build());
            if (failedCount != 0) {
                z = false;
            } else {
                z = true;
            }
            resolveMessage(isDownload, type, z);
            return;
        }
        if (isDownload) {
            type = 1;
        } else {
            type = 0;
        }
        first = successCount + failedCount;
        second = (((successCount + failedCount) + waitingCount) + runningCount) + pauseCount;
        if (first == 0 || second != 0) {
            if (isDownload) {
                componentName = new ComponentName(this.mContext, PhotoShareDownloadActivity.class);
            } else {
                componentName = new ComponentName(this.mContext, PhotoShareUploadActivity.class);
            }
            Intent statusIntent2 = new Intent();
            statusIntent2.setComponent(componentName);
            statusIntent2.setAction("com.huawei.gallery.app.photoshare.statusbar.main");
            statusIntent2.setFlags(268435456);
            Builder b2 = new Builder(this.mContext);
            b2.setContentTitle(getTitle(type));
            b2.setContentText(getContent(type, paused, first, second));
            b2.setAutoCancel(true);
            b2.setSmallIcon(getSmallIcon(type));
            if ((type == 1 || type == 0) && second != 0) {
                b2.setProgress(100, (first * 100) / second, false);
                b2.setSubText(this.mContext.getString(R.string.transmission_notification_new, new Object[]{getCurrentProgressPercentString(((double) first) / ((double) second))}));
            }
            b2.setContentIntent(PendingIntent.getActivity(this.mContext, 0, statusIntent2, 0));
            deleteIntent = new Intent();
            deleteIntent.setComponent(new ComponentName(this.mContext, PhotoShareNotificationDeleteIntentReceiver.class));
            if (isDownload) {
                deleteIntent.setAction("com.huawei.gallery.action.DOWNLOADNOTIFICATION_DELETE");
            } else {
                deleteIntent.setAction("com.huawei.gallery.action.UPLOADNOTIFICATION_DELETE");
            }
            b2.setDeleteIntent(PendingIntent.getBroadcast(this.mContext, 0, deleteIntent, 0));
            this.mNotificationManager.notify("photoshare_down_up", isDownload ? 0 : 1, b2.build());
            if (failedCount != 0) {
                z = true;
            } else {
                z = false;
            }
            resolveMessage(isDownload, type, z);
            return;
        }
        this.mNotificationManager.cancel("photoshare_down_up", isDownload ? 0 : 1);
    }

    private void resolveMessage(boolean isDownload, int type, boolean isSuccessful) {
        if (type == 3 && isSuccessful) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2), 2000);
        } else if (type == 2 && isSuccessful) {
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(3), 2000);
        } else if (isDownload) {
            this.mHandler.removeMessages(2);
        } else {
            this.mHandler.removeMessages(3);
        }
    }

    private int getSmallIcon(int type) {
        switch (type) {
            case 0:
                return 17301640;
            case 1:
                return 17301633;
            case 2:
                return 17301641;
            case 3:
                return 17301634;
            default:
                return -1;
        }
    }

    private String getCurrentProgressPercentString(double percent) {
        percent = new BigDecimal(percent).setScale(2, RoundingMode.DOWN).doubleValue();
        NumberFormat pnf = NumberFormat.getPercentInstance();
        pnf.setMinimumFractionDigits(0);
        return pnf.format(percent);
    }
}
