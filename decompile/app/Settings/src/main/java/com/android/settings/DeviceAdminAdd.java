package com.android.settings;

import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.AppOpsManager;
import android.app.Dialog;
import android.app.admin.DeviceAdminInfo;
import android.app.admin.DeviceAdminInfo.PolicyInfo;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.UserInfo;
import android.content.res.Resources.NotFoundException;
import android.hdm.HwDeviceManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Process;
import android.os.RemoteCallback;
import android.os.RemoteCallback.OnResultListener;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.UserManager;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.EventLog;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.view.WindowManager;
import android.widget.AppSecurityPermissions;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.users.UserDialogs;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParserException;

public class DeviceAdminAdd extends SettingsDrawerActivity {
    Button mActionButton;
    TextView mAddMsg;
    boolean mAddMsgEllipsized = true;
    ImageView mAddMsgExpander;
    CharSequence mAddMsgText;
    boolean mAdding;
    boolean mAddingProfileOwner;
    TextView mAdminDescription;
    ImageView mAdminIcon;
    TextView mAdminName;
    ViewGroup mAdminPolicies;
    boolean mAdminPoliciesInitialized;
    TextView mAdminWarning;
    AppOpsManager mAppOps;
    Button mCancelButton;
    int mCurSysAppOpMode;
    int mCurToastAppOpMode;
    DevicePolicyManager mDPM;
    DeviceAdminInfo mDeviceAdmin;
    Handler mHandler;
    boolean mIsCalledFromSupportDialog = false;
    private boolean mIsModeError = false;
    String mProfileOwnerName;
    TextView mProfileOwnerWarning;
    boolean mRefreshing;
    TextView mSupportMessage;
    Button mUninstallButton;
    boolean mUninstalling = false;
    boolean mWaitingForRemoveMsg;

    protected void onCreate(Bundle icicle) {
        ResolveInfo ri;
        super.onCreate(icicle);
        this.mHandler = new Handler(getMainLooper());
        this.mDPM = (DevicePolicyManager) getSystemService("device_policy");
        this.mAppOps = (AppOpsManager) getSystemService("appops");
        PackageManager packageManager = getPackageManager();
        if ((getIntent().getFlags() & 268435456) != 0) {
            Log.w("DeviceAdminAdd", "Cannot start ADD_DEVICE_ADMIN as a new task");
            finish();
            return;
        }
        this.mIsCalledFromSupportDialog = getIntent().getBooleanExtra("android.app.extra.CALLED_FROM_SUPPORT_DIALOG", false);
        String action = getIntent().getAction();
        ComponentName who = (ComponentName) getIntent().getParcelableExtra("android.app.extra.DEVICE_ADMIN");
        if (who == null) {
            String packageName = getIntent().getStringExtra("android.app.extra.DEVICE_ADMIN_PACKAGE_NAME");
            List<ComponentName> activeAdmins = this.mDPM.getActiveAdmins();
            if (activeAdmins != null) {
                for (ComponentName component : activeAdmins) {
                    if (component.getPackageName().equals(packageName)) {
                        who = component;
                        this.mUninstalling = true;
                        break;
                    }
                }
            }
            if (who == null) {
                Log.w("DeviceAdminAdd", "No component specified in " + action);
                finish();
                return;
            }
        }
        if (action != null && action.equals("android.app.action.SET_PROFILE_OWNER")) {
            setResult(0);
            setFinishOnTouchOutside(true);
            this.mAddingProfileOwner = true;
            this.mProfileOwnerName = getIntent().getStringExtra("android.app.extra.PROFILE_OWNER_NAME");
            String callingPackage = getCallingPackage();
            if (callingPackage == null || !callingPackage.equals(who.getPackageName())) {
                Log.e("DeviceAdminAdd", "Unknown or incorrect caller");
                finish();
                return;
            }
            try {
                if ((packageManager.getPackageInfo(callingPackage, 0).applicationInfo.flags & 1) == 0) {
                    Log.e("DeviceAdminAdd", "Cannot set a non-system app as a profile owner");
                    finish();
                    return;
                }
            } catch (NameNotFoundException e) {
                Log.e("DeviceAdminAdd", "Cannot find the package " + callingPackage);
                finish();
                return;
            }
        }
        try {
            int i;
            ActivityInfo ai = packageManager.getReceiverInfo(who, 128);
            if (!this.mDPM.isAdminActive(who)) {
                List<ResolveInfo> avail = packageManager.queryBroadcastReceivers(new Intent("android.app.action.DEVICE_ADMIN_ENABLED"), 32768);
                int count = avail == null ? 0 : avail.size();
                boolean found = false;
                i = 0;
                while (i < count) {
                    ri = (ResolveInfo) avail.get(i);
                    if (ai.packageName.equals(ri.activityInfo.packageName) && ai.name.equals(ri.activityInfo.name)) {
                        try {
                            ri.activityInfo = ai;
                            DeviceAdminInfo dpi = new DeviceAdminInfo(this, ri);
                            found = true;
                            break;
                        } catch (XmlPullParserException e2) {
                            Log.w("DeviceAdminAdd", "Bad " + ri.activityInfo, e2);
                        } catch (IOException e3) {
                            Log.w("DeviceAdminAdd", "Bad " + ri.activityInfo, e3);
                        }
                    } else {
                        i++;
                    }
                }
                if (!found) {
                    Log.w("DeviceAdminAdd", "Request to add invalid device admin: " + who);
                    finish();
                    return;
                }
            }
            ri = new ResolveInfo();
            ri.activityInfo = ai;
            try {
                this.mDeviceAdmin = new DeviceAdminInfo(this, ri);
                if ("android.app.action.ADD_DEVICE_ADMIN".equals(getIntent().getAction())) {
                    this.mRefreshing = false;
                    if (this.mDPM.isAdminActive(who)) {
                        if (this.mDPM.isRemovingAdmin(who, Process.myUserHandle().getIdentifier())) {
                            Log.w("DeviceAdminAdd", "Requested admin is already being removed: " + who);
                            finish();
                            return;
                        }
                        ArrayList<PolicyInfo> newPolicies = this.mDeviceAdmin.getUsedPolicies();
                        for (i = 0; i < newPolicies.size(); i++) {
                            if (!this.mDPM.hasGrantedPolicy(who, ((PolicyInfo) newPolicies.get(i)).ident)) {
                                this.mRefreshing = true;
                                break;
                            }
                        }
                        if (!this.mRefreshing) {
                            setResult(-1);
                            finish();
                            return;
                        }
                    }
                }
                if (!this.mAddingProfileOwner || this.mDPM.hasUserSetupCompleted()) {
                    this.mAddMsgText = getIntent().getCharSequenceExtra("android.app.extra.ADD_EXPLANATION");
                    setContentView(2130968735);
                    this.mAdminIcon = (ImageView) findViewById(2131886489);
                    this.mAdminName = (TextView) findViewById(2131886490);
                    this.mAdminDescription = (TextView) findViewById(2131886492);
                    this.mProfileOwnerWarning = (TextView) findViewById(2131886491);
                    this.mAddMsg = (TextView) findViewById(2131886494);
                    this.mAddMsgExpander = (ImageView) findViewById(2131886493);
                    OnClickListener anonymousClass1 = new OnClickListener() {
                        public void onClick(View v) {
                            DeviceAdminAdd.this.toggleMessageEllipsis(DeviceAdminAdd.this.mAddMsg);
                        }
                    };
                    this.mAddMsgExpander.setOnClickListener(anonymousClass1);
                    this.mAddMsg.setOnClickListener(anonymousClass1);
                    this.mAddMsg.getViewTreeObserver().addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
                        public void onGlobalLayout() {
                            boolean hideMsgExpander = DeviceAdminAdd.this.mAddMsg.getLineCount() <= DeviceAdminAdd.this.getEllipsizedLines();
                            DeviceAdminAdd.this.mAddMsgExpander.setVisibility(hideMsgExpander ? 8 : 0);
                            if (hideMsgExpander) {
                                DeviceAdminAdd.this.mAddMsg.setOnClickListener(null);
                                ((View) DeviceAdminAdd.this.mAddMsgExpander.getParent()).invalidate();
                            }
                            DeviceAdminAdd.this.mAddMsg.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    });
                    toggleMessageEllipsis(this.mAddMsg);
                    this.mAdminWarning = (TextView) findViewById(2131886495);
                    this.mAdminPolicies = (ViewGroup) findViewById(2131886496);
                    this.mSupportMessage = (TextView) findViewById(2131886497);
                    this.mCancelButton = (Button) findViewById(2131886370);
                    this.mCancelButton.setFilterTouchesWhenObscured(true);
                    this.mCancelButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            EventLog.writeEvent(90202, DeviceAdminAdd.this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
                            DeviceAdminAdd.this.finish();
                        }
                    });
                    this.mUninstallButton = (Button) findViewById(2131886499);
                    this.mUninstallButton.setFilterTouchesWhenObscured(true);
                    this.mUninstallButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            EventLog.writeEvent(90203, DeviceAdminAdd.this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
                            try {
                                DeviceAdminAdd.this.mDPM.uninstallPackageWithActiveAdmins(DeviceAdminAdd.this.mDeviceAdmin.getPackageName());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            DeviceAdminAdd.this.finish();
                        }
                    });
                    this.mActionButton = (Button) findViewById(2131886498);
                    this.mActionButton.setFilterTouchesWhenObscured(true);
                    this.mActionButton.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (DeviceAdminAdd.this.mAdding) {
                                DeviceAdminAdd.this.addAndFinish();
                            } else if (DeviceAdminAdd.this.isManagedProfile(DeviceAdminAdd.this.mDeviceAdmin) && DeviceAdminAdd.this.mDeviceAdmin.getComponent().equals(DeviceAdminAdd.this.mDPM.getProfileOwner())) {
                                final int userId = UserHandle.myUserId();
                                UserDialogs.createRemoveDialog(DeviceAdminAdd.this, userId, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        UserManager.get(DeviceAdminAdd.this).removeUser(userId);
                                        DeviceAdminAdd.this.finish();
                                    }
                                }).show();
                            } else if (DeviceAdminAdd.this.mUninstalling) {
                                try {
                                    DeviceAdminAdd.this.mDPM.uninstallPackageWithActiveAdmins(DeviceAdminAdd.this.mDeviceAdmin.getPackageName());
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                                DeviceAdminAdd.this.finish();
                            } else if (!DeviceAdminAdd.this.mWaitingForRemoveMsg) {
                                try {
                                    ActivityManagerNative.getDefault().stopAppSwitches();
                                } catch (RemoteException e2) {
                                    e2.printStackTrace();
                                }
                                DeviceAdminAdd.this.mWaitingForRemoveMsg = true;
                                DeviceAdminAdd.this.mDPM.getRemoveWarning(DeviceAdminAdd.this.mDeviceAdmin.getComponent(), new RemoteCallback(new OnResultListener() {
                                    public void onResult(Bundle result) {
                                        CharSequence charSequence;
                                        if (result != null) {
                                            charSequence = result.getCharSequence("android.app.extra.DISABLE_WARNING");
                                        } else {
                                            charSequence = null;
                                        }
                                        DeviceAdminAdd.this.continueRemoveAction(charSequence);
                                    }
                                }, DeviceAdminAdd.this.mHandler));
                                DeviceAdminAdd.this.getWindow().getDecorView().getHandler().postDelayed(new Runnable() {
                                    public void run() {
                                        DeviceAdminAdd.this.continueRemoveAction(null);
                                    }
                                }, 2000);
                            }
                        }
                    });
                    return;
                }
                addAndFinish();
            } catch (XmlPullParserException e22) {
                Log.w("DeviceAdminAdd", "Unable to retrieve device policy " + who, e22);
                finish();
            } catch (IOException e32) {
                Log.w("DeviceAdminAdd", "Unable to retrieve device policy " + who, e32);
                finish();
            }
        } catch (NameNotFoundException e4) {
            Log.w("DeviceAdminAdd", "Unable to retrieve device policy " + who, e4);
            finish();
        }
    }

    void addAndFinish() {
        try {
            this.mDPM.setActiveAdmin(this.mDeviceAdmin.getComponent(), this.mRefreshing);
            EventLog.writeEvent(90201, this.mDeviceAdmin.getActivityInfo().applicationInfo.uid);
            setResult(-1);
        } catch (RuntimeException e) {
            Log.w("DeviceAdminAdd", "Exception trying to activate admin " + this.mDeviceAdmin.getComponent(), e);
            if (this.mDPM.isAdminActive(this.mDeviceAdmin.getComponent())) {
                setResult(-1);
            }
        }
        if (this.mAddingProfileOwner) {
            try {
                this.mDPM.setProfileOwner(this.mDeviceAdmin.getComponent(), this.mProfileOwnerName, UserHandle.myUserId());
            } catch (RuntimeException e2) {
                setResult(0);
            }
        }
        finish();
    }

    void continueRemoveAction(CharSequence msg) {
        if (this.mWaitingForRemoveMsg) {
            this.mWaitingForRemoveMsg = false;
            if (msg == null) {
                try {
                    ActivityManagerNative.getDefault().resumeAppSwitches();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
                this.mDPM.removeActiveAdmin(this.mDeviceAdmin.getComponent());
                finish();
            } else {
                try {
                    ActivityManagerNative.getDefault().stopAppSwitches();
                } catch (RemoteException e2) {
                    e2.printStackTrace();
                }
                Bundle args = new Bundle();
                args.putCharSequence("android.app.extra.DISABLE_WARNING", msg);
                showDialog(1, args);
            }
        }
    }

    protected void onResume() {
        super.onResume();
        updateInterface();
        int uid = this.mDeviceAdmin.getActivityInfo().applicationInfo.uid;
        String pkg = this.mDeviceAdmin.getActivityInfo().applicationInfo.packageName;
        try {
            this.mCurSysAppOpMode = this.mAppOps.checkOp(24, uid, pkg);
            this.mCurToastAppOpMode = this.mAppOps.checkOp(45, uid, pkg);
            this.mAppOps.setMode(24, uid, pkg, 1);
            this.mAppOps.setMode(45, uid, pkg, 1);
        } catch (SecurityException e) {
            this.mIsModeError = true;
            Log.e("DeviceAdminAdd", "SecurityException =" + e.getMessage());
            finish();
        }
    }

    protected void onPause() {
        super.onPause();
        int uid = this.mDeviceAdmin.getActivityInfo().applicationInfo.uid;
        String pkg = this.mDeviceAdmin.getActivityInfo().applicationInfo.packageName;
        if (this.mIsModeError) {
            Log.e("DeviceAdminAdd", "mIsModeError = " + this.mIsModeError);
        } else {
            this.mAppOps.setMode(24, uid, pkg, this.mCurSysAppOpMode);
            this.mAppOps.setMode(45, uid, pkg, this.mCurToastAppOpMode);
        }
        try {
            ActivityManagerNative.getDefault().resumeAppSwitches();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mIsCalledFromSupportDialog) {
            finish();
        }
    }

    protected Dialog onCreateDialog(int id, Bundle args) {
        switch (id) {
            case 1:
                CharSequence msg = args.getCharSequence("android.app.extra.DISABLE_WARNING");
                Builder builder = new Builder(this);
                builder.setTitle(this.mDeviceAdmin.loadLabel(getPackageManager()));
                builder.setMessage(msg);
                builder.setPositiveButton(2131625656, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            ActivityManagerNative.getDefault().resumeAppSwitches();
                        } catch (RemoteException e) {
                            e.printStackTrace();
                        }
                        DeviceAdminAdd.this.mDPM.removeActiveAdmin(DeviceAdminAdd.this.mDeviceAdmin.getComponent());
                        DeviceAdminAdd.this.finish();
                    }
                });
                builder.setNegativeButton(2131625657, null);
                return builder.create();
            default:
                return super.onCreateDialog(id, args);
        }
    }

    void updateInterface() {
        this.mAdminIcon.setImageDrawable(this.mDeviceAdmin.loadIcon(getPackageManager()));
        this.mAdminName.setText(this.mDeviceAdmin.loadLabel(getPackageManager()));
        try {
            this.mAdminDescription.setText(this.mDeviceAdmin.loadDescription(getPackageManager()));
            this.mAdminDescription.setVisibility(0);
        } catch (NotFoundException e) {
            this.mAdminDescription.setVisibility(8);
        }
        if (this.mAddingProfileOwner) {
            this.mProfileOwnerWarning.setVisibility(0);
        }
        if (this.mAddMsgText != null) {
            this.mAddMsg.setText(this.mAddMsgText);
            this.mAddMsg.setVisibility(0);
        } else {
            this.mAddMsg.setVisibility(8);
            this.mAddMsgExpander.setVisibility(8);
        }
        if (this.mRefreshing || this.mAddingProfileOwner || !this.mDPM.isAdminActive(this.mDeviceAdmin.getComponent())) {
            addDeviceAdminPolicies(true);
            this.mAdminWarning.setText(getString(2131626175, new Object[]{this.mDeviceAdmin.getActivityInfo().applicationInfo.loadLabel(getPackageManager())}));
            if (this.mAddingProfileOwner) {
                setTitle(getText(2131626177));
            } else {
                setTitle(getText(2131626172));
            }
            this.mActionButton.setText(getText(2131626173));
            if (isAdminUninstallable()) {
                this.mUninstallButton.setVisibility(0);
            }
            this.mSupportMessage.setVisibility(8);
            this.mAdding = true;
            return;
        }
        this.mAdding = false;
        boolean isProfileOwner = this.mDeviceAdmin.getComponent().equals(this.mDPM.getProfileOwner());
        boolean isManagedProfile = isManagedProfile(this.mDeviceAdmin);
        if (isProfileOwner && isManagedProfile) {
            this.mAdminWarning.setText(2131627109);
            this.mActionButton.setText(2131626227);
        } else if (isProfileOwner || this.mDeviceAdmin.getComponent().equals(this.mDPM.getDeviceOwnerComponentOnCallingUser())) {
            if (isProfileOwner) {
                this.mAdminWarning.setText(2131627110);
            } else {
                this.mAdminWarning.setText(2131627111);
            }
            this.mActionButton.setText(2131626164);
            this.mActionButton.setEnabled(false);
        } else {
            addDeviceAdminPolicies(false);
            this.mAdminWarning.setText(getString(2131626176, new Object[]{this.mDeviceAdmin.getActivityInfo().applicationInfo.loadLabel(getPackageManager())}));
            setTitle(2131626163);
            if (this.mUninstalling) {
                this.mActionButton.setText(2131626166);
            } else {
                this.mActionButton.setText(2131626164);
            }
            if (HwDeviceManager.disallowOp(18, this.mDeviceAdmin.getComponent().getPackageName())) {
                this.mActionButton.setEnabled(false);
            } else {
                this.mActionButton.setEnabled(true);
            }
        }
        CharSequence supportMessage = this.mDPM.getLongSupportMessageForUser(this.mDeviceAdmin.getComponent(), UserHandle.myUserId());
        if (TextUtils.isEmpty(supportMessage)) {
            this.mSupportMessage.setVisibility(8);
            return;
        }
        this.mSupportMessage.setText(supportMessage);
        this.mSupportMessage.setVisibility(0);
    }

    private void addDeviceAdminPolicies(boolean showDescription) {
        if (!this.mAdminPoliciesInitialized) {
            boolean isAdminUser = UserManager.get(this).isAdminUser();
            for (PolicyInfo pi : this.mDeviceAdmin.getUsedPolicies()) {
                this.mAdminPolicies.addView(AppSecurityPermissions.getPermissionItemView(this, getText(isAdminUser ? pi.label : pi.labelForSecondaryUsers), showDescription ? getText(isAdminUser ? pi.description : pi.descriptionForSecondaryUsers) : "", true));
            }
            this.mAdminPoliciesInitialized = true;
        }
    }

    void toggleMessageEllipsis(View v) {
        int i;
        TextView tv = (TextView) v;
        this.mAddMsgEllipsized = !this.mAddMsgEllipsized;
        tv.setEllipsize(this.mAddMsgEllipsized ? TruncateAt.END : null);
        tv.setMaxLines(this.mAddMsgEllipsized ? getEllipsizedLines() : 15);
        ImageView imageView = this.mAddMsgExpander;
        if (this.mAddMsgEllipsized) {
            i = 17302195;
        } else {
            i = 17302194;
        }
        imageView.setImageResource(i);
    }

    int getEllipsizedLines() {
        Display d = ((WindowManager) getSystemService("window")).getDefaultDisplay();
        return d.getHeight() > d.getWidth() ? 5 : 2;
    }

    private boolean isManagedProfile(DeviceAdminInfo adminInfo) {
        UserInfo info = UserManager.get(this).getUserInfo(UserHandle.getUserId(adminInfo.getActivityInfo().applicationInfo.uid));
        return info != null ? info.isManagedProfile() : false;
    }

    private boolean isAdminUninstallable() {
        return !this.mDeviceAdmin.getActivityInfo().applicationInfo.isSystemApp();
    }
}
