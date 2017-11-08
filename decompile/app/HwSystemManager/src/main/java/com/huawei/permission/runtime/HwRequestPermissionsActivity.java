package com.huawei.permission.runtime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionGroupInfo;
import android.content.pm.PermissionInfo;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.huawei.permissionmanager.model.AppPermissionGroup;
import com.huawei.permissionmanager.model.AppPermissions;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class HwRequestPermissionsActivity extends AlertActivity implements OnClickListener {
    private static final String KEY_HW_PERMISSION_ARRAY = "KEY_HW_PERMISSION_ARRAY";
    private static final int RESULT_CODE_GRANTED = 101;
    private static final int RESULT_CODE_SETTINGS = 100;
    private static final String TAG = "HwRequestPermissionsActivity";
    private PackageInfo mCallingPkgInfo;
    private LinearLayout mLinearLayout = null;
    private Set<String> mPermissionGroup = new HashSet();
    private List<AppPermissionGroup> mRequestedPermissionGroup = new ArrayList();
    private ViewGroup mViewGroup = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HwLog.i(TAG, "received multi permission request.");
        if (checkRequest(getIntent())) {
            int i;
            setContentView(R.layout.hw_requestpermissions_activity);
            this.mLinearLayout = (LinearLayout) findViewById(R.id.request_layout);
            this.mViewGroup = (ViewGroup) this.mLinearLayout.getParent();
            String dialogTitle = HsmPackageManager.getInstance().getLabel(this.mCallingPkgInfo.packageName);
            AlertParams p = this.mAlertParams;
            LayoutParams l = getWindow().getAttributes();
            Context context = GlobalContext.getContext();
            l.y = (int) (HSMConst.DEVICE_SIZE_80 * context.getResources().getDisplayMetrics().density);
            if (1 == context.getResources().getConfiguration().orientation) {
                i = 80;
            } else {
                i = 17;
            }
            l.gravity = i;
            isSupportOptimized(context, l);
            p.mIconAttrId = 16843605;
            p.mTitle = dialogTitle;
            p.mView = createView();
            p.mPositiveButtonText = getString(R.string.hw_grantpermission_dlg_grant);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonText = getString(R.string.hw_grantpermission_dlg_opensettings);
            p.mNegativeButtonListener = this;
            setupAlert();
            setFinishOnTouchOutside(false);
            return;
        }
        HwLog.w(TAG, "onCreate: Invalid request");
        finshWithCancel();
    }

    public void isSupportOptimized(Context context, LayoutParams l) {
        if (Utility.isTablet(context)) {
            l.gravity = 17;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        HwLog.i(TAG, "onClick: which = " + which);
        switch (which) {
            case -2:
                redirectToPermissionRequest();
                finshWithOpenSettings();
                return;
            case -1:
                try {
                    grantPermissions();
                } catch (NullPointerException e) {
                    HwLog.e(TAG, "grant permissions fail." + e.getMessage());
                } catch (Exception e2) {
                    HwLog.e(TAG, "grant permissions fail." + e2.getMessage());
                }
                finshWithGranted();
                return;
            default:
                return;
        }
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.hw_requestpermissions_activity, this.mViewGroup, false);
        TextView txtContent = (TextView) view.findViewById(R.id.txt_conent);
        int nPermissionCount = this.mPermissionGroup.size();
        txtContent.setText(getResources().getQuantityString(R.plurals.hw_grantpermission_dlg_content, nPermissionCount, new Object[]{Integer.valueOf(nPermissionCount)}));
        ((TextView) view.findViewById(R.id.txt_tips)).setText(getResources().getQuantityString(R.plurals.hw_grantpermission_dlg_tips, nPermissionCount, new Object[]{Integer.valueOf(nPermissionCount)}));
        LinearLayout layoutPermissions = (LinearLayout) view.findViewById(R.id.layout_permissions);
        for (String group : this.mPermissionGroup) {
            layoutPermissions.addView(createPermissionItemView(group));
        }
        return view;
    }

    private TextView createPermissionItemView(String permissionDescrib) {
        TextView txView = new TextView(this);
        txView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        txView.setText("·" + permissionDescrib);
        txView.setTextColor(getResources().getColor(R.color.emui_list_secondray_text));
        txView.setTextSize(0, getResources().getDimension(R.dimen.hw_grantpermission_dlg_permissionitem_textsize));
        return txView;
    }

    private boolean checkRequest(Intent intent) {
        if (intent == null) {
            HwLog.w(TAG, "checkRequest: Invalid intent");
            return false;
        }
        String callingPkg = getCallingPackage();
        if (TextUtils.isEmpty(callingPkg)) {
            HwLog.w(TAG, "checkRequest: Fail to get calling package");
            return false;
        }
        PackageManager pm = getPackageManager();
        this.mCallingPkgInfo = getCallingPackageInfo(pm, callingPkg);
        if (this.mCallingPkgInfo == null) {
            HwLog.w(TAG, "checkRequest: caller's info fail.");
            return false;
        } else if (checkIfPreloadedPkg() && TrustedApps.isTrustedApp(callingPkg)) {
            String[] requestedPermissions = intent.getStringArrayExtra(KEY_HW_PERMISSION_ARRAY);
            if (requestedPermissions != null && requestedPermissions.length > 0) {
                return parseRequestedPermissions(pm, requestedPermissions);
            }
            HwLog.w(TAG, "checkRequest: Fail to get request permissions, callingPkg = " + callingPkg);
            return false;
        } else {
            HwLog.w(TAG, "Caller is not allowed to access this interface, caller:" + callingPkg);
            return false;
        }
    }

    private boolean parseRequestedPermissions(PackageManager pm, String[] requestedPermissions) {
        AppPermissions appPermissions = new AppPermissions(this, this.mCallingPkgInfo, null, false, null);
        if (Utility.isNullOrEmptyList(appPermissions.getPermissionGroups())) {
            HwLog.w(TAG, "parseRequestedPermissions: No valid runtime permission is declared = " + this.mCallingPkgInfo.packageName);
            return false;
        }
        for (String permission : requestedPermissions) {
            if (TextUtils.isEmpty(permission)) {
                HwLog.w(TAG, "parseRequestedPermissions:requsted empty permission.");
            } else {
                boolean isPermissionDeclared = false;
                for (String declaredPermission : this.mCallingPkgInfo.requestedPermissions) {
                    if (permission.equals(declaredPermission)) {
                        isPermissionDeclared = true;
                        break;
                    }
                }
                if (isPermissionDeclared) {
                    try {
                        PermissionInfo permissionInfo = pm.getPermissionInfo(permission, 0);
                        PermissionGroupInfo permissionGroupInfo = null;
                        AppPermissionGroup permissionGroup = null;
                        if (permissionInfo.group != null) {
                            try {
                                permissionGroupInfo = pm.getPermissionGroupInfo(permissionInfo.group, 0);
                                if (permissionGroupInfo != null) {
                                    permissionGroup = appPermissions.getPermissionGroup(permissionGroupInfo.name);
                                }
                            } catch (NameNotFoundException e) {
                            }
                        }
                        if (permissionGroup == null) {
                            permissionGroup = appPermissions.getPermissionGroup(permissionInfo.name);
                            if (permissionGroup == null) {
                                HwLog.w(TAG, "parseRequestedPermissions: Fail to get permission group info : " + permission);
                            }
                        }
                        if (permissionGroup.areRuntimePermissionsGranted()) {
                            HwLog.i(TAG, "parseRequestedPermissions: Permission is already granted , " + permission);
                        } else {
                            CharSequence label = null;
                            if (permissionGroupInfo != null) {
                                label = permissionGroupInfo.loadLabel(pm);
                            }
                            if (label == null) {
                                label = permissionInfo.loadLabel(pm);
                            }
                            if (label == null || label.length() <= 0) {
                                HwLog.w(TAG, "parseRequestedPermissions: Fail to get permission label : " + permission);
                            } else {
                                this.mPermissionGroup.add(label.toString());
                                this.mRequestedPermissionGroup.add(permissionGroup);
                            }
                        }
                    } catch (NameNotFoundException e2) {
                        HwLog.w(TAG, "parseRequestedPermissions: Fail to get permission info : " + permission);
                    }
                } else {
                    HwLog.w(TAG, "parseRequestedPermissions: permission is not declared in manifest : " + permission + ", callingPkg = " + this.mCallingPkgInfo.packageName);
                }
            }
        }
        boolean isValidRequest = !this.mPermissionGroup.isEmpty();
        HwLog.w(TAG, "parseRequestedPermissions: has valid permission request:" + this.mPermissionGroup.size());
        return isValidRequest;
    }

    private void finshWithGranted() {
        setResult(101);
        finish();
    }

    private void finshWithOpenSettings() {
        setResult(100);
        finish();
    }

    private void finshWithCancel() {
        setResult(0);
        finish();
    }

    private boolean grantPermissions() {
        boolean z = false;
        if (Utility.isNullOrEmptyList(this.mRequestedPermissionGroup)) {
            HwLog.w(TAG, "grantPermissions: No valid request permission group");
            return false;
        }
        ArrayList<String> grantedList = new ArrayList();
        for (AppPermissionGroup permissionGroup : this.mRequestedPermissionGroup) {
            if (permissionGroup.grantRuntimePermissions(false)) {
                grantedList.add(permissionGroup.getName());
            } else {
                HwLog.w(TAG, "grantPermissions: Fail to grant permission " + permissionGroup.getName());
            }
        }
        HwLog.i(TAG, "grantPermissions: Request count = " + this.mRequestedPermissionGroup.size() + ", granted count = " + grantedList.size());
        if (grantedList.size() != 0) {
            z = true;
        }
        return z;
    }

    private boolean redirectToPermissionRequest() {
        Intent intent = new Intent("android.intent.action.MANAGE_APP_PERMISSIONS");
        intent.putExtra("android.intent.extra.PACKAGE_NAME", this.mCallingPkgInfo.packageName);
        intent.putExtra(ShareCfg.EXTRA_HIDE_INFO_BUTTON, true);
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "redirectToPermissionRequest: Exception!", e);
            return false;
        }
    }

    private boolean checkIfPreloadedPkg() {
        if (this.mCallingPkgInfo.applicationInfo == null || (this.mCallingPkgInfo.applicationInfo.flags & 1) == 0) {
            return false;
        }
        return true;
    }

    private PackageInfo getCallingPackageInfo(PackageManager pm, String pkgName) {
        try {
            return PackageManagerWrapper.getPackageInfo(pm, pkgName, 4096);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getCallingPackageInfo: No such package: " + pkgName, e);
            return null;
        }
    }
}
