package com.huawei.permission.runtime;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.PermissionInfo;
import android.net.Uri;
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
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.PackageManagerWrapper;
import java.util.HashSet;
import java.util.Set;

public class HwGrantPermissionsActivity extends AlertActivity implements OnClickListener {
    private static final String KEY_HW_PERMISSION_ARRAY = "KEY_HW_PERMISSION_ARRAY";
    private static final String KEY_HW_PERMISSION_PKG = "KEY_HW_PERMISSION_PKG";
    private static final String TAG = "HwGrantPermissionsActivity";
    private PackageInfo mCallingPkgInfo;
    private LinearLayout mLinearLayout = null;
    private Set<String> mPermissionGroup = new HashSet();
    private ViewGroup mViewGroup = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (checkRequest(getIntent())) {
            int i;
            setContentView(R.layout.hw_grantpermissins_activity);
            this.mLinearLayout = (LinearLayout) findViewById(R.id.grant_layout);
            this.mViewGroup = (ViewGroup) this.mLinearLayout.getParent();
            String dialogTitle = getString(R.string.hw_grantpermission_dlg_title);
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
            p.mIconAttrId = 16843605;
            p.mTitle = dialogTitle;
            p.mView = createView();
            p.mPositiveButtonText = getString(R.string.hw_grantpermission_dlg_set);
            p.mPositiveButtonListener = this;
            p.mNegativeButtonText = getString(R.string.hw_grantpermission_dlg_cancel);
            p.mNegativeButtonListener = this;
            setupAlert();
            setFinishOnTouchOutside(false);
            return;
        }
        HwLog.w(TAG, "onCreate: Invalid request");
        finshWithCancel();
    }

    public void onClick(DialogInterface dialog, int which) {
        HwLog.i(TAG, "onClick: which = " + which);
        switch (which) {
            case -2:
                finshWithCancel();
                return;
            case -1:
                if (checkIfPreloadedPkg()) {
                    HwLog.i(TAG, "Preloaded app. pkgname = " + this.mCallingPkgInfo.packageName);
                    redirectToPermissionRequest();
                } else {
                    HwLog.i(TAG, "Not preloaded app. pkgname = " + this.mCallingPkgInfo.packageName);
                    redirectToAppDetail();
                }
                finshWithOK();
                return;
            default:
                return;
        }
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.hw_grantpermissins_activity, this.mViewGroup, false);
        TextView txtContent = (TextView) view.findViewById(R.id.txt_conent);
        int nPermissionCount = this.mPermissionGroup.size();
        txtContent.setText(getResources().getQuantityString(R.plurals.hw_grantpermission_dlg_content, nPermissionCount, new Object[]{Integer.valueOf(nPermissionCount)}));
        LinearLayout layouPermissions = (LinearLayout) view.findViewById(R.id.layout_permissions);
        for (String group : this.mPermissionGroup) {
            layouPermissions.addView(createPermissionItemView(group));
        }
        return view;
    }

    private TextView createPermissionItemView(String permissionDescrib) {
        TextView txView = new TextView(this);
        txView.setLayoutParams(new ViewGroup.LayoutParams(-1, -2));
        txView.setTextColor(getResources().getColor(R.color.emui_list_secondray_text));
        txView.setTextSize(0, getResources().getDimension(R.dimen.hw_grantpermission_dlg_permissionitem_textsize));
        txView.setText("·" + permissionDescrib);
        return txView;
    }

    private boolean checkRequest(Intent intent) {
        if (intent == null) {
            HwLog.w(TAG, "checkRequest: Invalid intent");
            return false;
        }
        String callingPkg = getCallingPackage();
        if (TextUtils.isEmpty(callingPkg)) {
            callingPkg = intent.getStringExtra(KEY_HW_PERMISSION_PKG);
        }
        if (TextUtils.isEmpty(callingPkg)) {
            HwLog.w(TAG, "checkRequest: Fail to get calling package");
            return false;
        }
        String[] requestedPermissions = intent.getStringArrayExtra(KEY_HW_PERMISSION_ARRAY);
        if (requestedPermissions == null || requestedPermissions.length <= 0) {
            HwLog.w(TAG, "checkRequest: Fail to get request permissions, callingPkg = " + callingPkg);
            return false;
        }
        PackageManager pm = getPackageManager();
        this.mCallingPkgInfo = getCallingPackageInfo(pm, callingPkg);
        if (this.mCallingPkgInfo == null) {
            return false;
        }
        if (this.mCallingPkgInfo.requestedPermissions == null || this.mCallingPkgInfo.requestedPermissions.length <= 0) {
            HwLog.w(TAG, "checkRequest: No permission is declared in manifest, callingPkg = " + this.mCallingPkgInfo.packageName);
            return false;
        }
        for (String permission : requestedPermissions) {
            if (!TextUtils.isEmpty(permission)) {
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
                        CharSequence label = null;
                        if (permissionInfo.group != null) {
                            try {
                                label = pm.getPermissionGroupInfo(permissionInfo.group, 0).loadLabel(pm);
                            } catch (NameNotFoundException e) {
                            }
                        }
                        if (label == null) {
                            label = permissionInfo.loadLabel(pm);
                        }
                        if (label == null || label.length() <= 0) {
                            HwLog.w(TAG, "checkRequest: Fail to get permission label : " + permission);
                        } else {
                            this.mPermissionGroup.add(label.toString());
                        }
                    } catch (NameNotFoundException e2) {
                        HwLog.w(TAG, "checkRequest: Fail to get permission info : " + permission);
                    }
                } else {
                    HwLog.w(TAG, "checkRequest: permission is not declared in manifest : " + permission + ", callingPkg = " + this.mCallingPkgInfo.packageName);
                }
            }
        }
        return !this.mPermissionGroup.isEmpty();
    }

    private void finshWithOK() {
        setResult(-1);
        finish();
    }

    private void finshWithCancel() {
        setResult(0);
        finish();
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

    private boolean redirectToAppDetail() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", this.mCallingPkgInfo.packageName, null));
        intent.setComponent(intent.resolveActivity(getPackageManager()));
        try {
            startActivity(intent);
            return true;
        } catch (Exception e) {
            HwLog.e(TAG, "redirectToAppDetail: Exception!", e);
            return false;
        }
    }

    private boolean checkIfPreloadedPkg() {
        return (this.mCallingPkgInfo.applicationInfo.flags & 1) != 0;
    }

    private PackageInfo getCallingPackageInfo(PackageManager pm, String pkgName) {
        try {
            return PackageManagerWrapper.getPackageInfo(pm, pkgName, 4096);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "getCallingPackageInfo: No such package: " + pkgName, e);
            return null;
        }
    }

    public void onBackPressed() {
    }
}
