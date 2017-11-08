package com.android.rcs.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsProfile;

public class RcsGroupInviteDialog extends DialogFragment {
    private AlertDialog mDialog;
    private String mGlobalGroupId;
    private int mNotificationId;
    private NotificationManager mNotificationManager;

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle bundle = getArguments();
        this.mNotificationId = bundle.getInt("notificationId");
        this.mGlobalGroupId = bundle.getString("globalgroupId");
        this.mNotificationManager = (NotificationManager) getActivity().getSystemService("notification");
        initDialog(getActivity(), bundle);
        return this.mDialog;
    }

    private void initDialog(Context context, Bundle bundle) {
        int themeID = getResources().getIdentifier("androidhwext:style/Theme.Emui.Dialog.Alert", null, null);
        if (themeID == 0) {
            themeID = 3;
        }
        Builder builder = new Builder(context, themeID) {
        };
        builder.setTitle(R.string.rcs_group_invite);
        builder.setPositiveButton(R.string.group_accept_btn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    RcsProfile.getRcsService().acceptGroupInviteAccept(RcsGroupInviteDialog.this.mGlobalGroupId);
                } catch (Exception e) {
                    MLog.e("RcsGroupInviteDialog", "initDialog Call RcsService Error");
                }
                RcsGroupInviteDialog.this.removeNotification();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.group_reject_btn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                try {
                    RcsProfile.getRcsService().rejectGroupInvite(RcsGroupInviteDialog.this.mGlobalGroupId);
                } catch (Exception e) {
                    MLog.e("RcsGroupInviteDialog", "initDialog Call RcsService Error");
                }
                RcsGroupInviteDialog.this.removeNotification();
                dialog.dismiss();
            }
        });
        builder.setMessage(bundle.getString("body"));
        builder.setCancelable(true);
        this.mDialog = builder.create();
    }

    private void removeNotification() {
        if (this.mNotificationManager != null) {
            this.mNotificationManager.cancel(this.mNotificationId);
        }
        if (MessagingNotification.getRcsMessagingNotification() != null) {
            MessagingNotification.getRcsMessagingNotification().removeGlobalGroupId(this.mGlobalGroupId);
        }
        if (MessagingNotification.getRcsMessagingNotification() != null) {
            MessagingNotification.getRcsMessagingNotification().deleteGroupInviteNotificationID(getActivity(), String.valueOf(this.mNotificationId));
        }
    }
}
