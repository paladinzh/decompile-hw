package com.android.settings.fuelgauge;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Checkable;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.Utils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import java.util.ArrayList;

public class HighPowerDetail extends DialogFragment implements OnClickListener, View.OnClickListener {
    private final PowerWhitelistBackend mBackend = PowerWhitelistBackend.getInstance();
    private boolean mDefaultOn;
    private boolean mIsEnabled;
    private CharSequence mLabel;
    private Checkable mOptionOff;
    private Checkable mOptionOn;
    private String mPackageName;

    public void onCreate(Bundle savedInstanceState) {
        boolean z;
        super.onCreate(savedInstanceState);
        this.mPackageName = getArguments().getString("package");
        PackageManager pm = getContext().getPackageManager();
        try {
            this.mLabel = pm.getApplicationInfo(this.mPackageName, 0).loadLabel(pm);
        } catch (NameNotFoundException e) {
            this.mLabel = this.mPackageName;
        }
        this.mDefaultOn = getArguments().getBoolean("default_on");
        ArrayList<String> protectAppList = Utils.getProtectedAppList(getActivity());
        if (this.mDefaultOn) {
            z = true;
        } else {
            z = protectAppList.contains(this.mPackageName);
        }
        this.mIsEnabled = z;
    }

    public Checkable setup(View view, boolean on) {
        ((TextView) view.findViewById(16908310)).setText(on ? 2131627034 : 2131627035);
        ((TextView) view.findViewById(16908304)).setText(on ? 2131627036 : 2131627037);
        view.setClickable(true);
        view.setOnClickListener(this);
        if (!on && this.mBackend.isSysWhitelisted(this.mPackageName)) {
            view.setEnabled(false);
        }
        return (Checkable) view;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Builder b = new Builder(getContext()).setTitle(this.mLabel).setNegativeButton(2131624572, null).setView(2130968832);
        b.setPositiveButton(17039370, this);
        if (savedInstanceState != null) {
            this.mIsEnabled = savedInstanceState.getBoolean("option_value");
        }
        return b.create();
    }

    public void onStart() {
        super.onStart();
        this.mOptionOn = setup(getDialog().findViewById(2131886715), true);
        this.mOptionOff = setup(getDialog().findViewById(2131886714), false);
        updateViews();
    }

    private void updateViews() {
        this.mOptionOn.setChecked(this.mIsEnabled);
        this.mOptionOff.setChecked(!this.mIsEnabled);
    }

    public void onClick(View v) {
        if (v == this.mOptionOn) {
            this.mIsEnabled = true;
            updateViews();
        } else if (v == this.mOptionOff) {
            this.mIsEnabled = false;
            updateViews();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (which == -1) {
            boolean newValue = this.mIsEnabled;
            if (newValue != Utils.getProtectedAppList(getActivity()).contains(this.mPackageName)) {
                setURIValue(newValue);
                if (newValue) {
                    this.mBackend.addApp(this.mPackageName);
                } else {
                    this.mBackend.removeApp(this.mPackageName);
                }
            }
        }
    }

    public void setURIValue(boolean value) {
        Bundle bundle = new Bundle();
        bundle.putString("pkg_name", this.mPackageName);
        if (value) {
            bundle.putInt("is_protected", 1);
        } else {
            bundle.putInt("is_protected", 0);
        }
        try {
            IHoldService service = StubController.getHoldService();
            if (service == null) {
                MLog.e("HighPowerDetail", "hsm_modify_unifiedpowerapps service is null!");
            } else {
                service.callHsmService("hsm_modify_unifiedpowerapps", bundle);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Fragment target = getTargetFragment();
        if (target != null) {
            target.onActivityResult(getTargetRequestCode(), 0, null);
        }
    }

    public static CharSequence getSummary(Context context, AppEntry entry) {
        return getSummary(context, entry.info.packageName);
    }

    public static CharSequence getSummary(Context context, String pkg) {
        int i;
        if (Utils.getProtectedAppList(context).contains(pkg)) {
            i = 2131626975;
        } else {
            i = 2131626977;
        }
        return context.getString(i);
    }

    public static void show(Fragment caller, String packageName, int requestCode, boolean defaultToOn) {
        HighPowerDetail fragment = new HighPowerDetail();
        Bundle args = new Bundle();
        args.putString("package", packageName);
        args.putBoolean("default_on", defaultToOn);
        fragment.setArguments(args);
        fragment.setTargetFragment(caller, requestCode);
        fragment.show(caller.getFragmentManager(), HighPowerDetail.class.getSimpleName());
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (outState != null) {
            outState.putBoolean("option_value", this.mIsEnabled);
        }
    }
}
