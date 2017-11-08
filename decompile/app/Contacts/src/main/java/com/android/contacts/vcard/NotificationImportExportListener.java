package com.android.contacts.vcard;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.vcard.VCardEntry;
import com.google.android.gms.R;
import java.text.NumberFormat;

public class NotificationImportExportListener implements VCardImportExportListener, Callback {
    private final Activity mContext;
    private final Handler mHandler = new Handler(this);
    private final NotificationManager mNotificationManager;

    public NotificationImportExportListener(Activity activity) {
        this.mContext = activity;
        this.mNotificationManager = (NotificationManager) activity.getSystemService("notification");
    }

    public boolean handleMessage(Message msg) {
        Toast.makeText(this.mContext, msg.obj, 1).show();
        return true;
    }

    public void onImportProcessed(ImportRequest request, int jobId, int sequence) {
        String displayName;
        String message;
        if (request.displayName != null) {
            displayName = request.displayName;
            message = this.mContext.getString(R.string.vcard_import_will_start_message, new Object[]{displayName});
        } else {
            displayName = this.mContext.getString(R.string.vcard_unknown_filename);
            message = this.mContext.getString(R.string.vcard_import_will_start_message_with_default_name);
        }
        if (sequence == 0) {
            String text = "";
            if (request.isMultipleRequest) {
                String vCard = this.mContext.getString(R.string.str_vcard_plural);
                text = this.mContext.getString(R.string.vcard_import_will_start_message, new Object[]{vCard});
            } else {
                text = message;
            }
            if (request.entryCount > 0 && request.entryCount < 50) {
                this.mHandler.obtainMessage(0, text).sendToTarget();
            }
        }
        this.mNotificationManager.notify("VCardServiceProgress", jobId, constructProgressNotification(this.mContext, 1, message, message, jobId, displayName, -1, 0));
    }

    public void onImportParsed(ImportRequest request, int jobId, VCardEntry entry, int currentCount, int totalCount, VCardService service) {
        if (!entry.isIgnorable()) {
            String tickerText = "";
            this.mNotificationManager.notify("VCardServiceProgress", jobId, constructProgressNotification(this.mContext.getApplicationContext(), 1, this.mContext.getString(R.string.importing_vcard_description, new Object[]{entry.getDisplayName()}), "", jobId, request.displayName, totalCount, currentCount));
        }
    }

    public void onImportFinished(ImportRequest request, int jobId, Uri createdUri) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
    }

    public void onImportFailed(ImportRequest request) {
        this.mHandler.obtainMessage(0, this.mContext.getString(R.string.vcard_import_request_rejected_message)).sendToTarget();
    }

    public void onImportCanceled(ImportRequest request, int jobId) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
    }

    public void onExportProcessed(ExportRequest request, int jobId) {
        String displayName = request.destUri.getLastPathSegment();
        String message = this.mContext.getString(R.string.vcard_export_will_start_message, new Object[]{displayName});
        if (request.selectedContactIds != null && request.selectedContactIds.length > 0 && request.selectedContactIds.length < 100) {
            this.mHandler.obtainMessage(0, message).sendToTarget();
        }
        this.mNotificationManager.notify("VCardServiceProgress", jobId, constructProgressNotification(this.mContext, 2, message, message, jobId, displayName, -1, 0));
    }

    public void onExportFailed(ExportRequest request) {
        this.mHandler.obtainMessage(0, this.mContext.getString(R.string.vcard_export_request_rejected_message)).sendToTarget();
    }

    public void onCancelRequest(CancelRequest request, int type) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
    }

    static Notification constructProgressNotification(Context context, int type, String description, String tickerText, int jobId, String displayName, int totalCount, int currentCount) {
        CommonUtilMethods.constructAndSendSummaryNotification(context, description);
        Intent intent = new Intent(context, CancelActivity.class);
        intent.setData(new Builder().scheme("invalidscheme").authority("invalidauthority").appendQueryParameter("job_id", String.valueOf(jobId)).appendQueryParameter("display_name", displayName).appendQueryParameter("type", String.valueOf(type)).build());
        Notification.Builder builder = new Notification.Builder(context);
        builder.setOngoing(true).setProgress(totalCount, currentCount, totalCount == -1).setContentTitle(description).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true).setSmallIcon(CommonUtilMethods.getBitampIcon(context, R.drawable.ic_notification_contacts)).setContentIntent(PendingIntent.getActivity(context, 0, intent, 0));
        if (totalCount > 0) {
            double percent = ((double) currentCount) / ((double) totalCount);
            NumberFormat progressPercentFormat = NumberFormat.getPercentInstance();
            progressPercentFormat.setMaximumFractionDigits(0);
            builder.setContentText(progressPercentFormat.format(percent));
        }
        return builder.getNotification();
    }

    static Notification constructImportFailureNotification(Context context, String reason) {
        CommonUtilMethods.constructAndSendSummaryNotification(context, context.getString(R.string.vcard_import_failed_Toast));
        return new Notification.Builder(context).setAutoCancel(true).setSmallIcon(CommonUtilMethods.getBitampIcon(context, R.drawable.ic_notification_contacts)).setContentTitle(context.getString(R.string.vcard_import_failed_Toast)).setContentText(reason).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true).setContentIntent(PendingIntent.getActivity(context, 0, new Intent(), 0)).getNotification();
    }

    public void onMemoryFull(ImportRequest request, int jobId) {
        this.mHandler.obtainMessage(0, this.mContext.getString(R.string.str_databasefull)).sendToTarget();
        onImportCanceled(request, jobId);
    }
}
