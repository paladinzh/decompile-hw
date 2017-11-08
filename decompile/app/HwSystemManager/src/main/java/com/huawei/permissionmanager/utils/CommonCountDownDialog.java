package com.huawei.permissionmanager.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v4.internal.view.SupportMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.ui.PermissionTableManager;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.util.HwLog;

public class CommonCountDownDialog extends CountDownDialog {
    private static String LOG_TAG = "CommonCountDownDialog";
    private CheckBox mCheckBox;
    private boolean mSupperApp;
    private TextView tvTip;

    class HoldDialogOnClickListener implements OnClickListener {
        int opetationType = 0;

        private class AsyncSetTask extends AsyncTask<Void, Void, Void> {
            private AsyncSetTask() {
            }

            protected Void doInBackground(Void... params) {
                return null;
            }

            protected void onPostExecute(Void result) {
                super.onPostExecute(result);
                HwLog.i(CommonCountDownDialog.LOG_TAG, "write permission data finished.");
                if (CommonCountDownDialog.this.mCallBack != null) {
                    CommonCountDownDialog.this.mCallBack.removePendingCfg(CommonCountDownDialog.this.mAppInfo.mAppUid, CommonCountDownDialog.this.mPermissionType, HoldDialogOnClickListener.this.opetationType, CommonCountDownDialog.this.mAppInfo.mPkgName);
                    CommonCountDownDialog.this.mCallBack.stopService();
                }
            }
        }

        public HoldDialogOnClickListener(int operationType) {
            this.opetationType = operationType;
        }

        public void onClick(View arg0) {
            boolean z = false;
            if (CommonCountDownDialog.this.mClickedButton) {
                HwLog.i(CommonCountDownDialog.LOG_TAG, "User repeatly clicked permission dialog. ignore it. opetationType:" + this.opetationType);
                return;
            }
            CommonCountDownDialog.this.mClickedButton = true;
            boolean stopServiceAysnc = false;
            if (CommonCountDownDialog.this.mCheckBox == null || !CommonCountDownDialog.this.mCheckBox.isChecked()) {
                HwLog.i(CommonCountDownDialog.LOG_TAG, "User clicked without check box checked. opetationType:" + this.opetationType);
            } else {
                stopServiceAysnc = true;
                new AsyncSetTask().execute(new Void[0]);
                HwLog.i(CommonCountDownDialog.LOG_TAG, "User clicked with check box checked. opetationType:" + this.opetationType);
                CommonCountDownDialog.this.mCallBack.addPendingCfg(CommonCountDownDialog.this.mAppInfo.mAppUid, CommonCountDownDialog.this.mPermissionType, this.opetationType, CommonCountDownDialog.this.mAppInfo.mPkgName);
            }
            CommonCountDownDialog.this.mCallBack.callBackAddRecord(this.opetationType, true, CommonCountDownDialog.this);
            CallBackHelper callBackHelper = CommonCountDownDialog.this.mCallBack;
            int i = this.opetationType;
            if (!stopServiceAysnc) {
                z = true;
            }
            callBackHelper.callBackRelease(i, z);
            if (CommonCountDownDialog.this.mThread != null) {
                CommonCountDownDialog.this.mThread.stopThread(true);
            }
            if (this.opetationType == 1) {
                HsmStat.statPerssmisonDialogAction("a", CommonCountDownDialog.this.mPermissionType, CommonCountDownDialog.this.mAppInfo.mPkgName);
            } else if (this.opetationType == 2) {
                HsmStat.statPerssmisonDialogAction("f", CommonCountDownDialog.this.mPermissionType, CommonCountDownDialog.this.mAppInfo.mPkgName);
            }
        }
    }

    public CommonCountDownDialog(Context context, int theme, String holdMessage, String holdTitle, AppInfo appInfo, int permissionType, CallBackHelper callback, boolean isInSuperAppList) {
        super(context, theme);
        this.mContext = context;
        this.mLimitedTime = this.mTimerSecond;
        this.mAppInfo = appInfo;
        this.mPermissionType = permissionType;
        this.mSupperApp = SuperAppPermisionChecker.getInstance(this.mContext).checkIfIsInAppPermissionList(appInfo.mPkgName, permissionType);
        HwLog.i(LOG_TAG, "mSupperApp:" + this.mSupperApp);
        View holdDialogView = LayoutInflater.from(this.mContext).inflate(R.layout.hold_dialog, null);
        if (isInSuperAppList) {
            holdTitle = context.getString(R.string.common_dialog_title_tip);
            if (PermissionTableManager.getInstance(this.mContext).getPermissionObjectByPermissionType(this.mPermissionType) != null) {
                holdMessage = String.format(context.getString(R.string.super_app_permission_forbit_tips), new Object[]{permissionObj.getmPermissionNames()});
            }
        }
        setIcon(0);
        setTitle(holdTitle);
        setView(holdDialogView);
        setCancelable(false);
        getWindow().setType(2003);
        this.tvWarningMessage = (TextView) holdDialogView.findViewById(R.id.tv_hold_dialog_warning_message);
        this.tvWarningMessage.setText(holdMessage);
        TextView tvBillingWarning = (TextView) holdDialogView.findViewById(R.id.tv_billing_warning);
        if (shouldShowBillingWarning()) {
            tvBillingWarning.setVisibility(0);
        }
        this.mAllowButton = (Button) holdDialogView.findViewById(R.id.btn_allow);
        this.mForbidButton = (Button) holdDialogView.findViewById(R.id.btn_forbbid);
        this.mAllowButton.setOnClickListener(new HoldDialogOnClickListener(1));
        this.mForbidButton.setOnClickListener(new HoldDialogOnClickListener(2));
        this.mCheckBox = (CheckBox) holdDialogView.findViewById(R.id.hold_checkbox);
        this.tvTip = (TextView) holdDialogView.findViewById(R.id.tv_tip);
        if (isInSuperAppList) {
            this.tvWarningMessage.setTextColor(SupportMenu.CATEGORY_MASK);
            this.tvTip.setVisibility(8);
            this.mAllowButton.setText(R.string.open_permission);
            this.mCheckBox.setVisibility(8);
        }
        this.mCheckBox.setChecked(donotAskAgainByDefault());
        if (!shouldShowNotAskAgain()) {
            this.mCheckBox.setVisibility(8);
        }
        this.mCallBack = callback;
    }

    private boolean shouldShowBillingWarning() {
        Permission permObj = PermissionTableManager.getInstance(this.mContext).getPermission(this.mPermissionType);
        return permObj == null ? false : permObj.showBillingWarning();
    }

    private boolean shouldShowNotAskAgain() {
        Permission permObj = PermissionTableManager.getInstance(this.mContext).getPermission(this.mPermissionType);
        return permObj == null ? false : permObj.showNotAskAgain();
    }

    private boolean donotAskAgainByDefault() {
        Permission permObj = PermissionTableManager.getInstance(this.mContext).getPermission(this.mPermissionType);
        return permObj == null ? true : permObj.donotAskAgain();
    }
}
