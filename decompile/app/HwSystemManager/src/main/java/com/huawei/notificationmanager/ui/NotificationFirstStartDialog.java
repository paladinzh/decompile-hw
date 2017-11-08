package com.huawei.notificationmanager.ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.huawei.notificationmanager.NotificationFirstStartService.Callback;
import com.huawei.notificationmanager.common.CommonObjects.NotificationCfgInfo;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.app.HsmPackageManager;

public class NotificationFirstStartDialog extends AlertDialog implements OnClickListener {
    private AppInfo mAppinfo;
    private Callback mCallback;
    private Context mContext;
    private NotificationCfgInfo mInfo;
    private TextView mMessage;
    private TextView mTitle;

    public NotificationFirstStartDialog(Context context, int themeID, Callback callback) {
        super(context, themeID);
        this.mContext = context;
        this.mCallback = callback;
        View view = LayoutInflater.from(context).inflate(R.layout.notification_firststart_dialog, null);
        setView(view);
        setCancelable(false);
        getWindow().setType(2003);
        this.mTitle = (TextView) view.findViewById(R.id.notification_firststart_dialog_title);
        this.mMessage = (TextView) view.findViewById(R.id.notification_firststart_dialog_message);
        setButton(-1, context.getResources().getString(R.string.permit), this);
        setButton(-2, context.getResources().getString(R.string.forbidden), this);
    }

    public void refresh(NotificationCfgInfo info, AppInfo appinfo) {
        this.mInfo = info;
        this.mAppinfo = appinfo;
        this.mTitle.setText(this.mContext.getResources().getString(R.string.notification_choice_dialog_title, new Object[]{HsmPackageManager.getInstance().getLabel(appinfo.mPkgName)}));
        this.mMessage.setText(R.string.notification_choice_dialog_message);
        if (getButton(-1) != null) {
            getButton(-1).setText(R.string.permit);
        }
        if (getButton(-2) != null) {
            getButton(-2).setText(R.string.forbidden);
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case -2:
                this.mCallback.onResult(false, this.mInfo, this.mAppinfo);
                return;
            case -1:
                this.mCallback.onResult(true, this.mInfo, this.mAppinfo);
                return;
            default:
                return;
        }
    }
}
