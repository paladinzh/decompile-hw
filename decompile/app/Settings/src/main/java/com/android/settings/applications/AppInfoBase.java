package com.android.settings.applications;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.IUsbManager;
import android.hardware.usb.IUsbManager.Stub;
import android.os.Bundle;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.Session;
import java.util.ArrayList;

public abstract class AppInfoBase extends SettingsPreferenceFragment implements Callbacks {
    protected static final String TAG = AppInfoBase.class.getSimpleName();
    protected AppEntry mAppEntry;
    protected EnforcedAdmin mAppsControlDisallowedAdmin;
    protected boolean mAppsControlDisallowedBySystem;
    protected DevicePolicyManager mDpm;
    protected boolean mFinishing;
    protected PackageInfo mPackageInfo;
    protected String mPackageName;
    protected PackageManager mPm;
    protected Session mSession;
    protected ApplicationsState mState;
    protected IUsbManager mUsbManager;
    protected int mUserId;
    protected UserManager mUserManager;

    public static class MyAlertDialogFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int id = getArguments().getInt("id");
            Dialog dialog = ((AppInfoBase) getTargetFragment()).createDialog(id, getArguments().getInt("moveError"));
            if (dialog != null) {
                return dialog;
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        public static MyAlertDialogFragment newInstance(int id, int errorCode) {
            MyAlertDialogFragment dialogFragment = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            args.putInt("moveError", errorCode);
            dialogFragment.setArguments(args);
            return dialogFragment;
        }
    }

    protected abstract AlertDialog createDialog(int i, int i2);

    protected abstract boolean refreshUi();

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFinishing = false;
        this.mState = ApplicationsState.getInstance(getActivity().getApplication());
        this.mSession = this.mState.newSession(this);
        Context context = getActivity();
        this.mDpm = (DevicePolicyManager) context.getSystemService("device_policy");
        this.mUserManager = (UserManager) context.getSystemService("user");
        this.mPm = context.getPackageManager();
        this.mUsbManager = Stub.asInterface(ServiceManager.getService("usb"));
        retrieveAppEntry();
    }

    public void onResume() {
        super.onResume();
        this.mSession.resume();
        this.mAppsControlDisallowedAdmin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_control_apps", this.mUserId);
        this.mAppsControlDisallowedBySystem = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_control_apps", this.mUserId);
        if (!refreshUi()) {
            setIntentAndFinish(true, true);
        }
    }

    public void onPause() {
        this.mSession.pause();
        super.onPause();
    }

    public void onDestroy() {
        this.mSession.release();
        super.onDestroy();
    }

    protected String retrieveAppEntry() {
        Bundle args = getArguments();
        this.mPackageName = args != null ? args.getString("package") : null;
        if (this.mPackageName == null) {
            Intent intent = args == null ? getActivity().getIntent() : (Intent) args.getParcelable("intent");
            if (intent == null || intent.getData() == null) {
                this.mPackageName = "";
            } else {
                this.mPackageName = intent.getData().getSchemeSpecificPart();
            }
        }
        this.mUserId = UserHandle.myUserId();
        this.mAppEntry = this.mState.getEntry(this.mPackageName, this.mUserId);
        if (this.mAppEntry != null) {
            try {
                this.mPackageInfo = this.mPm.getPackageInfo(this.mAppEntry.info.packageName, 12864);
            } catch (NameNotFoundException e) {
                Log.e(TAG, "Exception when retrieving package:" + this.mAppEntry.info.packageName, e);
            }
        } else {
            Log.w(TAG, "Missing AppEntry; maybe reinstalling?");
            this.mPackageInfo = null;
        }
        return this.mPackageName;
    }

    protected void setIntentAndFinish(boolean finish, boolean appChanged) {
        Intent intent = new Intent();
        intent.putExtra("chg", appChanged);
        ((SettingsActivity) getActivity()).finishPreferencePanel(this, -1, intent);
        this.mFinishing = true;
    }

    protected void showDialogInner(int id, int moveErrorCode) {
        if (!this.mFinishing) {
            try {
                DialogFragment newFragment = MyAlertDialogFragment.newInstance(id, moveErrorCode);
                newFragment.setTargetFragment(this, 0);
                newFragment.show(getFragmentManager(), "dialog " + id);
            } catch (Exception e) {
                Log.e(TAG, "showDialogInner()-->e : " + e);
            }
        }
    }

    public void onRunningStateChanged(boolean running) {
    }

    public void onRebuildComplete(ArrayList<AppEntry> arrayList) {
    }

    public void onPackageIconChanged() {
    }

    public void onPackageSizeChanged(String packageName) {
    }

    public void onAllSizesComputed() {
    }

    public void onLauncherInfoChanged() {
    }

    public void onLoadEntriesCompleted() {
    }

    public void onPackageListChanged() {
        refreshUi();
    }

    public static void startAppInfoFragment(Class<?> fragment, int titleRes, String pkg, int uid, Fragment source, int request) {
        startAppInfoFragment((Class) fragment, titleRes, pkg, uid, source.getActivity(), request);
    }

    public static void startAppInfoFragment(Class<?> fragment, int titleRes, String pkg, int uid, Activity source, int request) {
        Bundle args = new Bundle();
        args.putString("package", pkg);
        args.putInt("uid", uid);
        source.startActivityForResultAsUser(Utils.onBuildStartFragmentIntent(source, fragment.getName(), args, null, titleRes, null, false), request, new UserHandle(UserHandle.getUserId(uid)));
    }
}
