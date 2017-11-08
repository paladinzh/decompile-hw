package com.android.settings;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.os.Process;
import android.os.RemoteException;
import android.os.UserHandle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.Settings.DeviceAdminSettingsActivity;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class ShowAdminSupportDetailsDialog extends Activity implements OnDismissListener {
    private View mDialogView;
    private DevicePolicyManager mDpm;
    private EnforcedAdmin mEnforcedAdmin;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mDpm = (DevicePolicyManager) getSystemService(DevicePolicyManager.class);
        this.mEnforcedAdmin = getAdminDetailsFromIntent(getIntent());
        Builder builder = new Builder(this);
        this.mDialogView = LayoutInflater.from(builder.getContext()).inflate(2130968616, null);
        initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId);
        builder.setOnDismissListener(this).setPositiveButton(2131624573, null).setView(this.mDialogView).setTitle(2131627106).show();
    }

    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        EnforcedAdmin admin = getAdminDetailsFromIntent(intent);
        if (!this.mEnforcedAdmin.equals(admin)) {
            this.mEnforcedAdmin = admin;
            initializeDialogViews(this.mDialogView, this.mEnforcedAdmin.component, this.mEnforcedAdmin.userId);
        }
    }

    private EnforcedAdmin getAdminDetailsFromIntent(Intent intent) {
        EnforcedAdmin admin = new EnforcedAdmin(null, UserHandle.myUserId());
        if (intent != null && checkIfCallerHasPermission("android.permission.MANAGE_DEVICE_ADMINS")) {
            admin.component = (ComponentName) intent.getParcelableExtra("android.app.extra.DEVICE_ADMIN");
            admin.userId = intent.getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
        }
        return admin;
    }

    private boolean checkIfCallerHasPermission(String permission) {
        boolean z = false;
        try {
            if (AppGlobals.getPackageManager().checkUidPermission(permission, ActivityManagerNative.getDefault().getLaunchedFromUid(getActivityToken())) == 0) {
                z = true;
            }
            return z;
        } catch (RemoteException e) {
            Log.e("AdminSupportDialog", "Could not talk to activity manager." + e.toString());
            return false;
        }
    }

    private void initializeDialogViews(View root, ComponentName admin, int userId) {
        if (admin != null) {
            if (RestrictedLockUtils.isAdminInCurrentUserOrProfile(this, admin) && RestrictedLockUtils.isCurrentUserOrProfile(this, userId)) {
                ActivityInfo ai = null;
                try {
                    ai = AppGlobals.getPackageManager().getReceiverInfo(admin, 0, userId);
                } catch (RemoteException e) {
                    Log.w("AdminSupportDialog", "Missing reciever info" + e.toString());
                }
                if (ai != null) {
                    ((ImageView) root.findViewById(2131886214)).setImageDrawable(getPackageManager().getUserBadgedIcon(ai.loadIcon(getPackageManager()), new UserHandle(userId)));
                }
            } else {
                admin = null;
            }
        }
        setAdminSupportDetails(this, root, new EnforcedAdmin(admin, userId), true);
    }

    public static void setAdminSupportDetails(final Activity activity, View root, final EnforcedAdmin enforcedAdmin, final boolean finishActivity) {
        if (enforcedAdmin != null) {
            if (enforcedAdmin.component != null) {
                DevicePolicyManager dpm = (DevicePolicyManager) activity.getSystemService("device_policy");
                if (RestrictedLockUtils.isAdminInCurrentUserOrProfile(activity, enforcedAdmin.component) && RestrictedLockUtils.isCurrentUserOrProfile(activity, enforcedAdmin.userId)) {
                    if (enforcedAdmin.userId == -10000) {
                        enforcedAdmin.userId = UserHandle.myUserId();
                    }
                    CharSequence supportMessage = null;
                    if (UserHandle.isSameApp(Process.myUid(), 1000)) {
                        supportMessage = dpm.getShortSupportMessageForUser(enforcedAdmin.component, enforcedAdmin.userId);
                    }
                    if (supportMessage != null) {
                        ((TextView) root.findViewById(2131886211)).setText(supportMessage);
                    }
                } else {
                    enforcedAdmin.component = null;
                }
            }
            root.findViewById(2131886212).setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    Intent intent = new Intent();
                    if (enforcedAdmin.component != null) {
                        intent.setClass(activity, DeviceAdminAdd.class);
                        intent.putExtra("android.app.extra.DEVICE_ADMIN", enforcedAdmin.component);
                        intent.putExtra("android.app.extra.CALLED_FROM_SUPPORT_DIALOG", true);
                        activity.startActivityAsUser(intent, new UserHandle(enforcedAdmin.userId));
                    } else {
                        intent.setClass(activity, DeviceAdminSettingsActivity.class);
                        intent.addFlags(268435456);
                        activity.startActivity(intent);
                    }
                    if (finishActivity) {
                        activity.finish();
                    }
                }
            });
        }
    }

    public void onDismiss(DialogInterface dialog) {
        finish();
    }
}
