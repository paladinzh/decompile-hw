package com.huawei.permissionmanager.ui;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.support.v4.internal.view.SupportMenu;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.model.HwAppPermissions;
import com.huawei.permissionmanager.utils.ShareLib;
import com.huawei.permissionmanager.utils.SingleAppPermissionHelper.PermissionItemBase;
import com.huawei.permissionmanager.utils.SuperAppPermisionChecker;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.daulapp.DualAppUtil;
import com.huawei.systemmanager.comm.misc.StringUtils;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;

/* compiled from: TagViewHolder */
class OnDialogClickListener implements OnClickListener {
    private static final String TAG = "OnDialogClickListener";
    Builder mBuilder;
    private Context mContext = null;
    private int mCurrentPosition;
    private PermissionItemBase mPermItem;
    private int mPermissionType;
    private String mPkgName;
    private int mUid;

    public OnDialogClickListener(Context context, String pkgName, int uid, int permissionType, PermissionItemBase permItem, int currentPosition) {
        this.mContext = context;
        this.mPkgName = pkgName;
        this.mUid = uid;
        this.mPermissionType = permissionType;
        this.mCurrentPosition = currentPosition;
        this.mPermItem = permItem;
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCurrentPosition == which) {
            HwLog.d(TAG, "mCurrentPosition == which");
            dialog.dismiss();
            return;
        }
        processSelectSetEvent(which);
        dialog.dismiss();
    }

    private String getPermissionContent(int permissionType, int usedFor) {
        SparseIntArray stringIdMap;
        String permissionContent = "";
        if (usedFor == 0) {
            stringIdMap = ShareLib.getBlockedSingalStringIdMap();
        } else {
            stringIdMap = ShareLib.getBlockedNotificationStringIdMap();
        }
        if (stringIdMap.size() == 0) {
            return permissionContent;
        }
        int permissionStringId = stringIdMap.get(permissionType);
        if (permissionStringId != 0) {
            permissionContent = this.mContext.getString(permissionStringId);
        }
        return permissionContent;
    }

    private void showCommitDialog(String pkgName, int permissionType) {
        String permissionContentForNotification = getPermissionContent(permissionType, 1);
        String label = HsmPackageManager.getInstance().getLabel(pkgName);
        String mDialogContent = "";
        if (DualAppUtil.isPackageCloned(this.mContext, pkgName)) {
            mDialogContent = this.mContext.getString(R.string.permission_block_notification_content_for_dual_app, new Object[]{label});
        } else {
            mDialogContent = this.mContext.getString(R.string.permission_block_notification_content, new Object[]{label});
        }
        String mDialogTitle = this.mContext.getString(R.string.permission_block_notification_title, new Object[]{label, permissionContentForNotification});
        this.mBuilder = new Builder(this.mContext);
        this.mBuilder.setTitle(mDialogTitle).setIconAttribute(16843605).setNegativeButton(R.string.cancel, null);
        this.mBuilder.setPositiveButton(R.string.forbidden, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                if (TextUtils.isEmpty(OnDialogClickListener.this.mPkgName)) {
                    HwLog.e(OnDialogClickListener.TAG, "onClick, package is null!");
                    return;
                }
                boolean isLegacy = HsmPackageManager.getInstance().checkIsPackageLegacy(OnDialogClickListener.this.mPkgName);
                HwLog.i(OnDialogClickListener.TAG, "supper app permission config, isLegacy:" + isLegacy + ", pkgName:" + OnDialogClickListener.this.mPkgName);
                if (isLegacy) {
                    DBAdapter.setSinglePermission(OnDialogClickListener.this.mContext, OnDialogClickListener.this.mUid, OnDialogClickListener.this.mPkgName, OnDialogClickListener.this.mPermissionType, 2);
                } else {
                    DBAdapter.setSinglePermissionAndSyncToSys(HwAppPermissions.create(OnDialogClickListener.this.mContext, OnDialogClickListener.this.mPkgName), OnDialogClickListener.this.mContext, OnDialogClickListener.this.mUid, OnDialogClickListener.this.mPkgName, OnDialogClickListener.this.mPermissionType, 2, "hsm settings");
                }
                if (OnDialogClickListener.this.mContext.getClass() == PermissionSettingActivity.class) {
                    PermissionSettingActivity parentActivity = (PermissionSettingActivity) OnDialogClickListener.this.mContext;
                    parentActivity.updatePermissionAppListForSpinner(OnDialogClickListener.this.mPermissionType, OnDialogClickListener.this.mUid, OnDialogClickListener.this.mPkgName, 2);
                    parentActivity.onHeaderChanged();
                    parentActivity.updateUI();
                }
                if (OnDialogClickListener.this.mContext.getClass() == SingleAppActivity.class) {
                    ((SingleAppActivity) OnDialogClickListener.this.mContext).updatePermissionListForSpinner(OnDialogClickListener.this.mPermissionType, 2, OnDialogClickListener.this.mPermItem);
                }
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, OnDialogClickListener.this.mPkgName, HsmStatConst.PARAM_KEY, String.valueOf(OnDialogClickListener.this.mPermissionType), HsmStatConst.PARAM_VAL, String.valueOf(2));
                HsmStat.statE(36, statParam);
            }
        });
        this.mBuilder.setMessage(StringUtils.highlight(mDialogContent, 0, mDialogContent.length(), SupportMenu.CATEGORY_MASK));
        this.mBuilder.setCancelable(true);
        this.mBuilder.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                OnDialogClickListener.this.mBuilder = null;
            }
        });
        this.mBuilder.show();
    }

    public void processSelectSetEvent(int position) {
        int permissionOperation = 0;
        HwLog.d(TAG, "processSelectSetEvent, position:" + position);
        switch (position) {
            case 0:
                permissionOperation = 1;
                break;
            case 2:
                permissionOperation = 2;
                if (SuperAppPermisionChecker.getInstance(this.mContext).checkIfIsInAppPermissionList(this.mPkgName, this.mPermissionType)) {
                    showCommitDialog(this.mPkgName, this.mPermissionType);
                    return;
                }
                break;
        }
        DBAdapter.setSinglePermissionAndSyncToSys(HwAppPermissions.create(this.mContext, this.mPkgName), this.mContext, this.mUid, this.mPkgName, this.mPermissionType, permissionOperation, "hsm settings");
        if (this.mContext.getClass() == PermissionSettingActivity.class) {
            PermissionSettingActivity parentActivity = this.mContext;
            parentActivity.updatePermissionAppListForSpinner(this.mPermissionType, this.mUid, this.mPkgName, permissionOperation);
            parentActivity.onHeaderChanged();
            parentActivity.updateUI();
        }
        if (this.mContext.getClass() == SingleAppActivity.class) {
            this.mContext.updatePermissionListForSpinner(this.mPermissionType, permissionOperation, this.mPermItem);
        }
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, this.mPkgName, HsmStatConst.PARAM_KEY, String.valueOf(this.mPermissionType), HsmStatConst.PARAM_VAL, String.valueOf(permissionOperation));
        HsmStat.statE(36, statParam);
    }
}
