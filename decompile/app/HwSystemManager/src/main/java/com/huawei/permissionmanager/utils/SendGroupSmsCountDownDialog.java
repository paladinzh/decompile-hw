package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

public class SendGroupSmsCountDownDialog extends CountDownDialog {
    private static String LOG_TAG = "SendGroupSmsCountDownDialog";
    private TextView mSmsContentTextview;

    class HoldDialogOnClickListener implements OnClickListener {
        int opetationType = 0;

        private class AsyncSetTask extends AsyncTask<Void, Void, Void> {
            private AsyncSetTask() {
            }

            protected Void doInBackground(Void... params) {
                if (1 == HoldDialogOnClickListener.this.opetationType) {
                    return null;
                }
                DBAdapter.setSinglePermissionAndSyncToSys(HwAppPermissions.create(SendGroupSmsCountDownDialog.this.mContext, SendGroupSmsCountDownDialog.this.mAppInfo.mPkgName), SendGroupSmsCountDownDialog.this.mContext, SendGroupSmsCountDownDialog.this.mAppInfo.mAppUid, SendGroupSmsCountDownDialog.this.mAppInfo.mPkgName, SendGroupSmsCountDownDialog.this.mPermissionType, HoldDialogOnClickListener.this.opetationType, "group sms dialog");
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                SendGroupSmsCountDownDialog.this.mCallBack.callBackAddRecord(HoldDialogOnClickListener.this.opetationType, true, SendGroupSmsCountDownDialog.this);
                SendGroupSmsCountDownDialog.this.mCallBack.callBackRelease(HoldDialogOnClickListener.this.opetationType, true);
            }
        }

        public HoldDialogOnClickListener(int operationType) {
            this.opetationType = operationType;
        }

        public void onClick(View arg0) {
            if (!SendGroupSmsCountDownDialog.this.mClickedButton) {
                SendGroupSmsCountDownDialog.this.mClickedButton = true;
                new AsyncSetTask().execute(new Void[0]);
                if (SendGroupSmsCountDownDialog.this.mThread != null) {
                    SendGroupSmsCountDownDialog.this.mThread.stopThread(true);
                }
            }
        }
    }

    public SendGroupSmsCountDownDialog(Context context, int theme, String labelName, String smsContent, AppInfo appInfo, int permissionType, CallBackHelper callback) {
        super(context, theme);
        HwLog.d(LOG_TAG, "theme:" + theme);
        this.mContext = context;
        this.mLimitedTime = this.mTimerSecond;
        this.mAppInfo = appInfo;
        getEfficiencyPermissionType(permissionType);
        View holdDialogView = LayoutInflater.from(this.mContext).inflate(R.layout.send_group_sms_hold_dialog, null);
        setIcon(0);
        setTitle(getGroupSendMonitorTitle(permissionType));
        setView(holdDialogView);
        setCancelable(false);
        getWindow().setType(2003);
        this.tvWarningMessage = (TextView) holdDialogView.findViewById(R.id.tv_hold_dialog_warning_message);
        this.tvWarningMessage.setText(getGroupSendMonitorDescription(permissionType, labelName));
        this.mAllowButton = (Button) holdDialogView.findViewById(R.id.btn_allow);
        this.mForbidButton = (Button) holdDialogView.findViewById(R.id.btn_forbbid);
        this.mAllowButton.setOnClickListener(new HoldDialogOnClickListener(1));
        this.mForbidButton.setOnClickListener(new HoldDialogOnClickListener(2));
        this.mSmsContentTextview = (TextView) holdDialogView.findViewById(R.id.tv_sms_content);
        if (1000 == permissionType) {
            this.mSmsContentTextview.setVisibility(0);
            this.mSmsContentTextview.setMovementMethod(new ScrollingMovementMethod());
            this.mSmsContentTextview.setText(smsContent);
        } else {
            this.mSmsContentTextview.setVisibility(8);
        }
        this.mCallBack = callback;
    }

    private void getEfficiencyPermissionType(int permissionType) {
        if (1000 == permissionType) {
            this.mPermissionType = 32;
        } else {
            this.mPermissionType = 8192;
        }
    }

    private String getGroupSendMonitorTitle(int permissionType) {
        String dialogTitle = "";
        if (1000 == permissionType) {
            return this.mContext.getString(R.string.Send_Group_Sms_Dialog_Title);
        }
        return this.mContext.getString(R.string.Send_Group_Mms_Dialog_Title);
    }

    private String getGroupSendMonitorDescription(int permissionType, String labelName) {
        String dialogTitle = "";
        if (1000 == permissionType) {
            return this.mContext.getString(R.string.Send_Group_Sms_Dialog_Description, new Object[]{labelName});
        }
        return this.mContext.getString(R.string.Send_Group_Mms_Dialog_Description, new Object[]{labelName});
    }
}
