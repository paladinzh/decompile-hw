package com.huawei.permissionmanager.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import com.huawei.permissionmanager.utils.CommonFunctionUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.util.HwLog;

class PermissionProhibitionDialogFragment extends DialogFragment {
    private static final String ARG_APP_NAME = "app_name";
    private static final String ARG_CURRENT_STATUS = "current_status";
    private static final String ARG_PACKAGE_NAME = "package_name";
    private static final String ARG_PERMISSION_ITEM = "permission_item";
    private static final String ARG_PERMISSION_NAME = "permission_name";
    private static final String ARG_PERMISSION_TYPE = "permission_type";
    private static final String ARG_UID = "uid";
    private static final String LOG_TAG = "PermissionProhibitionDialogFragment";
    private String mAppName = "";
    private Context mContext = null;
    private int mCurrentStatus = 0;
    private String mPermissionName = "";
    private int mPermissionType = -1;
    private String mPkgName = "";
    private int mUid = 0;

    private static class OnDialogClickListener implements OnClickListener {
        private Context mContext = null;

        public OnDialogClickListener(Context context, String pkgName, int uid, int permissionType, int currentStatus) {
            this.mContext = context;
        }

        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    CommonFunctionUtil.changeSmsPermission(this.mContext);
                    return;
                default:
                    return;
            }
        }
    }

    PermissionProhibitionDialogFragment() {
    }

    static PermissionProhibitionDialogFragment newInstance(String permissionName, String appName, String pkgName, int uid, int permissionType, int currentStatus) {
        PermissionProhibitionDialogFragment df = new PermissionProhibitionDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PERMISSION_NAME, permissionName);
        args.putString("app_name", appName);
        args.putString("package_name", pkgName);
        args.putInt("uid", uid);
        args.putInt("permission_type", permissionType);
        args.putInt(ARG_CURRENT_STATUS, currentStatus);
        df.setArguments(args);
        return df;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        Bundle args = getArguments();
        if (args != null) {
            this.mPermissionName = args.getString(ARG_PERMISSION_NAME);
            this.mAppName = args.getString("app_name");
            this.mPkgName = args.getString("package_name");
            this.mUid = args.getInt("uid");
            this.mPermissionType = args.getInt("permission_type");
            this.mCurrentStatus = args.getInt(ARG_CURRENT_STATUS);
            return;
        }
        HwLog.e(LOG_TAG, "arguments is null.");
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new Builder(getActivity()).setTitle(this.mPermissionName).setMessage(String.format(getString(R.string.tips_default_sms), new Object[]{this.mAppName})).setPositiveButton(R.string.I_know, new OnDialogClickListener(this.mContext, this.mPkgName, this.mUid, this.mPermissionType, this.mCurrentStatus)).create();
    }
}
