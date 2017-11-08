package com.android.systemui.screenshot;

import android.app.Notification.Action;
import android.app.Notification.BigPictureStyle;
import android.app.Notification.Builder;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Canvas;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Process;
import android.provider.MediaStore.Images.Media;
import android.util.Log;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.screenshot.GlobalScreenshot.DeleteScreenshotReceiver;
import com.android.systemui.screenshot.GlobalScreenshot.TargetChosenReceiver;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/* compiled from: GlobalScreenshot */
abstract class SaveImageInBackgroundTask extends AsyncTask<Void, Void, Void> implements HwSaveImageTaskItf {
    private static boolean mTickerAddSpace;
    private CountDownLatch mAnimationLock = new CountDownLatch(1);
    private final String mImageFileName;
    private final String mImageFilePath;
    private final int mImageHeight;
    private final long mImageTime;
    private final int mImageWidth;
    private final Builder mNotificationBuilder;
    private final NotificationManager mNotificationManager;
    private final BigPictureStyle mNotificationStyle;
    private final SaveImageInBackgroundData mParams;
    private final Builder mPublicNotificationBuilder;
    private final File mScreenshotDir;
    private boolean mScrollClicked = false;

    SaveImageInBackgroundTask(Context context, SaveImageInBackgroundData data, NotificationManager nManager) {
        Resources r = context.getResources();
        this.mParams = data;
        this.mImageTime = System.currentTimeMillis();
        String imageDate = new SimpleDateFormat("yyyyMMdd-HHmmss", Locale.ENGLISH).format(new Date(this.mImageTime));
        this.mImageFileName = String.format("Screenshot_%s.png", new Object[]{imageDate});
        this.mScreenshotDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Screenshots");
        this.mImageFilePath = new File(this.mScreenshotDir, this.mImageFileName).getAbsolutePath();
        this.mImageWidth = data.image.getWidth();
        this.mImageHeight = data.image.getHeight();
        int iconSize = data.iconSize;
        int previewWidth = data.previewWidth;
        int previewHeight = data.previewheight;
        Canvas c = new Canvas();
        Paint paint = new Paint();
        ColorMatrix desat = new ColorMatrix();
        desat.setSaturation(0.25f);
        paint.setColorFilter(new ColorMatrixColorFilter(desat));
        Matrix matrix = new Matrix();
        Bitmap picture = Bitmap.createBitmap(previewWidth, previewHeight, data.image.getConfig());
        matrix.setTranslate((float) ((previewWidth - this.mImageWidth) / 2), (float) ((previewHeight - this.mImageHeight) / 2));
        c.setBitmap(picture);
        c.drawBitmap(data.image, matrix, paint);
        c.drawColor(1090519039);
        c.setBitmap(null);
        float scale = ((float) iconSize) / ((float) Math.min(this.mImageWidth, this.mImageHeight));
        Bitmap icon = Bitmap.createBitmap(iconSize, iconSize, data.image.getConfig());
        matrix.setScale(scale, scale);
        matrix.postTranslate((((float) iconSize) - (((float) this.mImageWidth) * scale)) / 2.0f, (((float) iconSize) - (((float) this.mImageHeight) * scale)) / 2.0f);
        c.setBitmap(icon);
        c.drawBitmap(data.image, matrix, paint);
        c.drawColor(1090519039);
        c.setBitmap(null);
        mTickerAddSpace = !mTickerAddSpace;
        this.mNotificationManager = nManager;
        long now = System.currentTimeMillis();
        this.mNotificationStyle = new BigPictureStyle().bigPicture(picture.createAshmemBitmap());
        this.mPublicNotificationBuilder = new Builder(context).setContentTitle(r.getString(R.string.screenshot_saving_title)).setContentText(r.getString(R.string.screenshot_saving_text)).setSmallIcon(R.drawable.stat_notify_image).setCategory("progress").setWhen(now).setShowWhen(true).setColor(r.getColor(17170519));
        SystemUI.overrideNotificationAppName(context, this.mPublicNotificationBuilder);
        this.mNotificationBuilder = new Builder(context).setTicker(r.getString(R.string.screenshot_saving_ticker) + (mTickerAddSpace ? " " : BuildConfig.FLAVOR)).setContentTitle(r.getString(R.string.screenshot_saving_title)).setContentText(r.getString(R.string.screenshot_saving_text)).setSmallIcon(R.drawable.stat_notify_image).setWhen(now).setShowWhen(true).setColor(r.getColor(17170519)).setStyle(this.mNotificationStyle).setPublicVersion(this.mPublicNotificationBuilder.build());
        this.mNotificationBuilder.setFlag(32, true);
        SystemUI.overrideNotificationAppName(context, this.mNotificationBuilder);
        this.mNotificationBuilder.setLargeIcon(icon.createAshmemBitmap());
        this.mNotificationStyle.bigLargeIcon((Bitmap) null);
    }

    protected Void doInBackground(Void... params) {
        if (isCancelled()) {
            return null;
        }
        Process.setThreadPriority(-2);
        Context context = this.mParams.context;
        Bitmap image = this.mParams.image;
        Resources r = context.getResources();
        try {
            this.mScreenshotDir.mkdirs();
            long dateSeconds = this.mImageTime / 1000;
            OutputStream out = new FileOutputStream(this.mImageFilePath);
            image.compress(CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            ContentValues values = new ContentValues();
            ContentResolver resolver = context.getContentResolver();
            values.put("_data", this.mImageFilePath);
            values.put("title", this.mImageFileName);
            values.put("_display_name", this.mImageFileName);
            values.put("datetaken", Long.valueOf(this.mImageTime));
            values.put("date_added", Long.valueOf(dateSeconds));
            values.put("date_modified", Long.valueOf(dateSeconds));
            values.put("mime_type", "image/png");
            values.put("width", Integer.valueOf(this.mImageWidth));
            values.put("height", Integer.valueOf(this.mImageHeight));
            values.put("_size", Long.valueOf(new File(this.mImageFilePath).length()));
            Uri uri = resolver.insert(Media.EXTERNAL_CONTENT_URI, values);
            String subjectDate = DateFormat.getDateTimeInstance().format(new Date(this.mImageTime));
            String subject = String.format("Screenshot (%s)", new Object[]{subjectDate});
            Intent intent = new Intent("android.intent.action.SEND");
            intent.setType("image/png");
            intent.putExtra("android.intent.extra.STREAM", uri);
            intent.putExtra("android.intent.extra.SUBJECT", subject);
            intent = new Intent(context, TargetChosenReceiver.class);
            this.mNotificationBuilder.addAction(new Action.Builder(R.drawable.ic_screenshot_share, r.getString(17040504), PendingIntent.getActivity(context, 0, Intent.createChooser(intent, null, PendingIntent.getBroadcast(context, 0, intent, 1342177280).getIntentSender()).addFlags(268468224), 268435456)).build());
            PendingIntent deleteAction = PendingIntent.getBroadcast(context, 0, new Intent(context, DeleteScreenshotReceiver.class).putExtra("android:screenshot_uri_id", uri.toString()), 1342177280);
            this.mNotificationBuilder.addAction(new Action.Builder(R.drawable.ic_screenshot_delete, r.getString(17040226), deleteAction).build());
            this.mParams.imageUri = uri;
            this.mParams.image = null;
            this.mParams.errorMsgResId = 0;
        } catch (Exception e) {
            Log.e("SaveImageInBackgroundTask", "doInBackground save file failed. Exception: " + e);
            this.mParams.clearImage();
            this.mParams.errorMsgResId = R.string.screenshot_failed_to_save_text;
        }
        if (image != null) {
            image.recycle();
        }
        waitAnimationEnd();
        return null;
    }

    protected void onPostExecute(Void params) {
        if (this.mParams.errorMsgResId != 0) {
            GlobalScreenshot.notifyScreenshotError(this.mParams.context, this.mNotificationManager, this.mParams.errorMsgResId);
        } else {
            onFileSaved(this.mParams.imageUri);
            Context context = this.mParams.context;
            Resources r = context.getResources();
            Intent launchIntent = new Intent("android.intent.action.VIEW");
            launchIntent.setDataAndType(this.mParams.imageUri, "image/png");
            launchIntent.setFlags(268435456);
            long now = System.currentTimeMillis();
            this.mPublicNotificationBuilder.setContentTitle(r.getString(R.string.screenshot_saved_title)).setContentText(r.getString(R.string.screenshot_saved_text)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, launchIntent, 0)).setWhen(now).setAutoCancel(true).setColor(context.getColor(17170519));
            this.mNotificationBuilder.setContentTitle(r.getString(R.string.screenshot_saved_title)).setContentText(r.getString(R.string.screenshot_saved_text)).setContentIntent(PendingIntent.getActivity(this.mParams.context, 0, launchIntent, 0)).setWhen(now).setAutoCancel(true).setColor(context.getColor(17170519)).setPublicVersion(this.mPublicNotificationBuilder.build()).setFlag(32, false);
            if (this.mScrollClicked) {
                HwScreenshotNotifications.clearNotifyMsg(this.mParams.context, false);
            } else {
                HwGlobalScreenshot.sendBroadcastForNotification(this.mParams.context, this.mParams.imageUri, this.mImageTime, 0, -1);
            }
        }
        this.mParams.finisher.run();
        this.mParams.clearContext();
    }

    protected void onCancelled(Void params) {
        this.mParams.finisher.run();
        this.mParams.clearImage();
        this.mParams.clearContext();
        this.mNotificationManager.cancel(R.id.notification_screenshot);
    }

    public void onScreenshotAnimationEnd() {
        Log.i("GlobalScreenshot", "SaveImageInBackgroundTask.onScreenshotAnimationEnd called");
        this.mAnimationLock.countDown();
    }

    private void waitAnimationEnd() {
        try {
            if (!this.mAnimationLock.await(4500, TimeUnit.MILLISECONDS)) {
                Log.w("GlobalScreenshot", "SaveImageInBackgroundTask.waitAnimationEnd wait timeout");
            }
        } catch (InterruptedException e) {
            Log.e("GlobalScreenshot", "waitAnimationEnd InterruptedException");
        } catch (Exception e2) {
            Log.e("GlobalScreenshot", "waitAnimationEnd Exception");
        }
    }

    public void onScrollButtonClicked() {
        this.mScrollClicked = true;
    }
}
