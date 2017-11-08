package com.android.contacts.hap.copy;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri.Builder;
import android.os.Handler;
import android.os.Looper;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.HwLog;
import com.android.contacts.vcard.CancelRequest;
import com.google.android.gms.R;
import java.text.NumberFormat;

public class NotificationCopyContactsListener implements ContactsCopyListener {
    private final Context mContext;
    private boolean mExportToSimFlag;
    private final Handler mHandler;
    private boolean mImportToSimFlag;
    private final NotificationManager mNotificationManager;

    static class NotificationData {
        boolean mIsExportToSim;
        boolean mIsImportFromSim;
        Context mcontext;
        int mcurrentCount;
        String mdescription;
        String mdisplayName;
        int mjobId;
        int mtotalCount;

        NotificationData(Context acontext, String adescription, int ajobId, String adisplayName, int atotalCount, int acurrentCount, boolean aIsExportToSim, boolean aIsImportFromSim) {
            this.mcontext = acontext;
            this.mdescription = adescription;
            this.mjobId = ajobId;
            this.mdisplayName = adisplayName;
            this.mtotalCount = atotalCount;
            this.mcurrentCount = acurrentCount;
            this.mIsExportToSim = aIsExportToSim;
            this.mIsImportFromSim = aIsImportFromSim;
        }
    }

    public NotificationCopyContactsListener(Context aContext) {
        this.mContext = aContext;
        this.mNotificationManager = (NotificationManager) aContext.getSystemService("notification");
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mExportToSimFlag = false;
        this.mImportToSimFlag = false;
    }

    public NotificationCopyContactsListener(Context aContext, boolean aExportTosimFlag) {
        this.mContext = aContext;
        this.mNotificationManager = (NotificationManager) aContext.getSystemService("notification");
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mExportToSimFlag = aExportTosimFlag;
        this.mImportToSimFlag = false;
    }

    public NotificationCopyContactsListener(Context aContext, boolean aExportTosimFlag, boolean aImportTosimFlag) {
        this.mContext = aContext;
        this.mNotificationManager = (NotificationManager) aContext.getSystemService("notification");
        this.mHandler = new Handler(Looper.getMainLooper());
        this.mExportToSimFlag = aExportTosimFlag;
        this.mImportToSimFlag = aImportTosimFlag;
    }

    private void showToast(final String message) {
        this.mHandler.post(new Runnable() {
            public void run() {
                int themeID = NotificationCopyContactsListener.this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                if (themeID == 0) {
                    HwLog.d("Contact", "if case value of themeID is ::::" + themeID);
                    Toast.makeText(NotificationCopyContactsListener.this.mContext, message, 1).show();
                    return;
                }
                HwLog.d("Contact", "else case value of themeID is ::::" + themeID);
                Toast.makeText(new ContextThemeWrapper(NotificationCopyContactsListener.this.mContext, themeID), message, 1).show();
            }
        });
    }

    public void onCopyContactsQueued(String displayName, int jobId, int totalCount) {
    }

    public void onCopyContactsParsed(String aTitle, String aAccountType, int jobId, String aName, int currentCount, int totalCount, CopyContactService service) {
        String description;
        if (aAccountType == null) {
            if (this.mExportToSimFlag) {
                description = this.mContext.getString(R.string.exporting_contact_description, new Object[]{aName});
            } else if (this.mImportToSimFlag) {
                description = this.mContext.getString(R.string.importing_contact_description, new Object[]{aName});
            } else {
                description = this.mContext.getString(R.string.copying_contact_description, new Object[]{aName});
            }
        } else if ("com.android.huawei.phone".equalsIgnoreCase(aAccountType)) {
            if (this.mExportToSimFlag) {
                description = this.mContext.getString(R.string.exporting_to_dest_account, new Object[]{this.mContext.getString(R.string.phoneLabelsGroup)});
            } else if (this.mImportToSimFlag) {
                description = this.mContext.getString(R.string.importing_to_dest_account, new Object[]{this.mContext.getString(R.string.phoneLabelsGroup)});
            } else {
                description = this.mContext.getString(R.string.copying_to_dest_account, new Object[]{this.mContext.getString(R.string.phoneLabelsGroup)});
            }
        } else if (CommonUtilMethods.isSimAccount(aAccountType)) {
            String accountName = SimFactoryManager.getSimCardDisplayLabel(aAccountType);
            if (this.mExportToSimFlag) {
                description = this.mContext.getString(R.string.exporting_to_dest_account, new Object[]{accountName});
            } else if (this.mImportToSimFlag) {
                description = this.mContext.getString(R.string.importing_to_dest_account, new Object[]{accountName});
            } else {
                description = this.mContext.getString(R.string.copying_to_dest_account, new Object[]{accountName});
            }
        } else if (this.mExportToSimFlag) {
            description = this.mContext.getString(R.string.exporting_to_dest_account, new Object[]{aName});
        } else if (this.mImportToSimFlag) {
            description = this.mContext.getString(R.string.importing_to_dest_account, new Object[]{aName});
        } else {
            description = this.mContext.getString(R.string.copying_to_dest_account, new Object[]{aName});
        }
        CopyContactService copyContactService = service;
        copyContactService.startForegroundCopyNotification(constructProgressNotification(new NotificationData(this.mContext.getApplicationContext(), description, jobId, aTitle, totalCount, currentCount, this.mExportToSimFlag, this.mImportToSimFlag)));
        this.mNotificationManager.cancel("CopyContactsServiceProgress", jobId);
    }

    public void onCopyContactsFinished(String aDescription, int jobId) {
        this.mNotificationManager.cancelAll();
    }

    public void onCopyContactsFailed(String aName) {
        if (this.mContext.getString(R.string.email_full).equals(aName)) {
            showToast(this.mContext.getString(R.string.email_full));
        } else if (this.mContext.getString(R.string.sim_available_space_full).equals(aName)) {
            showToast(this.mContext.getString(R.string.sim_available_space_full));
        } else {
            if (this.mExportToSimFlag) {
                showToast(this.mContext.getString(R.string.export_contacts_request_rejected_message));
            } else if (this.mImportToSimFlag) {
                showToast(this.mContext.getString(R.string.import_contacts_request_rejected_message));
            } else {
                showToast(this.mContext.getString(R.string.copy_contacts_request_rejected_message));
            }
        }
    }

    public void onCopyContactsCanceled(String aDescription, int jobId) {
        this.mNotificationManager.cancelAll();
    }

    public void onCancelRequest(CancelRequest request) {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancelAll();
        }
    }

    static Notification constructProgressNotification(NotificationData aNotificationData) {
        boolean z;
        CommonUtilMethods.constructAndSendSummaryNotification(aNotificationData.mcontext, aNotificationData.mdescription);
        Intent intent = new Intent(aNotificationData.mcontext, CancelActivity.class);
        intent.setData(new Builder().scheme("invalidscheme").authority("invalidauthority").appendQueryParameter("job_id", String.valueOf(aNotificationData.mjobId)).appendQueryParameter("display_name", aNotificationData.mdisplayName).build());
        if (aNotificationData.mIsExportToSim) {
            intent.putExtra("export_to_sim", true);
        }
        if (aNotificationData.mIsImportFromSim) {
            intent.putExtra("import_to_sim", true);
        }
        Notification.Builder builder = new Notification.Builder(aNotificationData.mcontext);
        Notification.Builder ongoing = builder.setOngoing(true);
        int i = aNotificationData.mtotalCount;
        int i2 = aNotificationData.mcurrentCount;
        if (aNotificationData.mtotalCount == -1) {
            z = true;
        } else {
            z = false;
        }
        ongoing.setProgress(i, i2, z).setContentTitle(aNotificationData.mdescription).setSmallIcon(CommonUtilMethods.getBitampIcon(aNotificationData.mcontext, R.drawable.ic_notification_contacts)).setGroup("group_key_contacts").setGroupSummary(false).setShowWhen(true).setContentIntent(PendingIntent.getActivity(aNotificationData.mcontext, 0, intent, 0));
        NumberFormat percentInstance = NumberFormat.getPercentInstance();
        percentInstance.setMaximumFractionDigits(0);
        if (aNotificationData.mtotalCount > 0) {
            builder.setContentText(percentInstance.format(((double) aNotificationData.mcurrentCount) / ((double) aNotificationData.mtotalCount)));
        }
        return builder.getNotification();
    }
}
