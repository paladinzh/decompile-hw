package com.android.settings.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.AccessibilityServiceInfo.CapabilityInfo;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.ConfirmDeviceCredentialActivity;
import com.android.settings.ItemUseStat;
import com.android.settingslib.accessibility.AccessibilityUtils;
import java.util.List;

public class ToggleAccessibilityServicePreferenceFragment extends ToggleFeaturePreferenceFragment implements OnClickListener, OnPreferenceChangeListener {
    private ComponentName mComponentName;
    private LockPatternUtils mLockPatternUtils;
    private final SettingsContentObserver mSettingsContentObserver = new SettingsContentObserver(new Handler()) {
        public void onChange(boolean selfChange, Uri uri) {
            String settingValue = Secure.getString(ToggleAccessibilityServicePreferenceFragment.this.getContentResolver(), "enabled_accessibility_services");
            ToggleAccessibilityServicePreferenceFragment.this.mToggleSwitch.setChecked(!TextUtils.isEmpty(settingValue) ? settingValue.contains(ToggleAccessibilityServicePreferenceFragment.this.mComponentName.flattenToString()) : false);
        }
    };
    private int mShownDialogId;

    protected int getMetricsCategory() {
        return 4;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mLockPatternUtils = new LockPatternUtils(getActivity());
    }

    public void onResume() {
        this.mSettingsContentObserver.register(getContentResolver());
        this.mSettingsContentObserver.dispatchChange(false);
        super.onResume();
    }

    public void onPause() {
        this.mSettingsContentObserver.unregister(getContentResolver());
        super.onPause();
        ItemUseStat.getInstance().cacheData(getActivity());
    }

    public void onPreferenceToggled(String preferenceKey, boolean enabled) {
        AccessibilityUtils.setAccessibilityServiceState(getActivity(), ComponentName.unflattenFromString(preferenceKey), enabled);
    }

    private AccessibilityServiceInfo getAccessibilityServiceInfo() {
        List<AccessibilityServiceInfo> serviceInfos = AccessibilityManager.getInstance(getActivity()).getInstalledAccessibilityServiceList();
        int serviceInfoCount = serviceInfos.size();
        for (int i = 0; i < serviceInfoCount; i++) {
            AccessibilityServiceInfo serviceInfo = (AccessibilityServiceInfo) serviceInfos.get(i);
            if (!(serviceInfo == null || serviceInfo.getResolveInfo() == null || serviceInfo.getResolveInfo().serviceInfo == null)) {
                ResolveInfo resolveInfo = serviceInfo.getResolveInfo();
                if (this.mComponentName.getPackageName().equals(resolveInfo.serviceInfo.packageName) && this.mComponentName.getClassName().equals(resolveInfo.serviceInfo.name)) {
                    return serviceInfo;
                }
            }
        }
        return null;
    }

    public Dialog onCreateDialog(int dialogId) {
        AccessibilityServiceInfo info;
        switch (dialogId) {
            case 1:
                this.mShownDialogId = 1;
                info = getAccessibilityServiceInfo();
                if (info == null) {
                    return null;
                }
                AlertDialog ad = new Builder(getActivity()).setTitle(getString(2131625909, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())})).setView(createEnableDialogContentView(info)).setCancelable(true).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
                OnTouchListener filterTouchListener = new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if ((event.getFlags() & 1) == 0) {
                            return false;
                        }
                        if (event.getAction() == 1) {
                            Toast.makeText(v.getContext(), 2131625911, 0).show();
                        }
                        return true;
                    }
                };
                ad.create();
                ad.getButton(-1).setOnTouchListener(filterTouchListener);
                return ad;
            case 2:
                this.mShownDialogId = 2;
                if (getAccessibilityServiceInfo() == null) {
                    return null;
                }
                return new Builder(getActivity()).setTitle(getString(2131625919, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())})).setMessage(getString(2131625920, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())})).setCancelable(true).setPositiveButton(17039370, this).setNegativeButton(17039360, this).create();
            default:
                throw new IllegalArgumentException();
        }
    }

    private boolean isFullDiskEncrypted() {
        return StorageManager.isNonDefaultBlockEncrypted();
    }

    private View createEnableDialogContentView(AccessibilityServiceInfo info) {
        LayoutInflater inflater = (LayoutInflater) getSystemService("layout_inflater");
        View content = inflater.inflate(2130968773, null);
        TextView encryptionWarningView = (TextView) content.findViewById(2131886565);
        if (isFullDiskEncrypted()) {
            encryptionWarningView.setText(getString(2131625912, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())}));
            encryptionWarningView.setVisibility(0);
        } else {
            encryptionWarningView.setVisibility(8);
        }
        ((TextView) content.findViewById(2131886566)).setText(getString(2131625910, new Object[]{info.getResolveInfo().loadLabel(getPackageManager())}));
        LinearLayout capabilitiesView = (LinearLayout) content.findViewById(2131886567);
        View capabilityView = inflater.inflate(17367096, null);
        ((ImageView) capabilityView.findViewById(16909110)).setImageDrawable(getResources().getDrawable(17302568));
        ((TextView) capabilityView.findViewById(16909114)).setText(getString(2131625917));
        ((TextView) capabilityView.findViewById(16909115)).setText(getString(2131625918));
        List<CapabilityInfo> capabilities = info.getCapabilityInfos();
        if ("com.google.android.marvin.talkback".equals(info.getResolveInfo().serviceInfo.packageName)) {
            try {
                capabilities.add(new CapabilityInfo(169, 2131627924, 2131627925));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        capabilitiesView.addView(capabilityView);
        int capabilityCount = capabilities.size();
        for (int i = 0; i < capabilityCount; i++) {
            CapabilityInfo capability = (CapabilityInfo) capabilities.get(i);
            capabilityView = inflater.inflate(17367096, null);
            ((ImageView) capabilityView.findViewById(16909110)).setImageDrawable(getResources().getDrawable(17302568));
            ((TextView) capabilityView.findViewById(16909114)).setText(getString(capability.titleResId));
            ((TextView) capabilityView.findViewById(16909115)).setText(getString(capability.descResId));
            capabilitiesView.addView(capabilityView);
        }
        return content;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode != 1) {
            return;
        }
        if (resultCode == -1) {
            handleConfirmServiceEnabled(true);
            if (isFullDiskEncrypted()) {
                this.mLockPatternUtils.clearEncryptionPassword();
                Global.putInt(getContentResolver(), "require_password_to_decrypt", 0);
                return;
            }
            return;
        }
        handleConfirmServiceEnabled(false);
    }

    public void onClick(DialogInterface dialog, int which) {
        boolean checked = true;
        switch (which) {
            case -2:
                if (this.mShownDialogId != 2) {
                    checked = false;
                }
                handleConfirmServiceEnabled(checked);
                restoreSwitchState();
                return;
            case -1:
                AccessibilityServiceInfo info = getAccessibilityServiceInfo();
                String packageName = "";
                if (!(info == null || info.getResolveInfo() == null)) {
                    packageName = info.getResolveInfo().loadLabel(getPackageManager()).toString();
                }
                if (this.mShownDialogId == 1) {
                    if (HwCustAccessibilitySettings.TalkBack_TITLE.equals(packageName)) {
                        ItemUseStat.getInstance().handleClick(getActivity(), 2, HwCustAccessibilitySettings.TalkBack_TITLE, "on");
                    }
                    if (isFullDiskEncrypted()) {
                        startActivityForResult(ConfirmDeviceCredentialActivity.createIntent(createConfirmCredentialReasonMessage(), null), 1);
                        return;
                    } else {
                        handleConfirmServiceEnabled(true);
                        return;
                    }
                }
                if (HwCustAccessibilitySettings.TalkBack_TITLE.equals(packageName)) {
                    ItemUseStat.getInstance().handleClick(getActivity(), 2, HwCustAccessibilitySettings.TalkBack_TITLE, "off");
                    Log.d("ToggleAccessibilityServicePreferenceFragment", "Disabling Talkback service.");
                }
                handleConfirmServiceEnabled(false);
                return;
            default:
                throw new IllegalArgumentException();
        }
    }

    private void handleConfirmServiceEnabled(boolean confirmed) {
        getArguments().putBoolean("checked", confirmed);
        onPreferenceToggled(this.mPreferenceKey, confirmed);
    }

    private String createConfirmCredentialReasonMessage() {
        int resId = 2131625916;
        switch (this.mLockPatternUtils.getKeyguardStoredPasswordQuality(UserHandle.myUserId())) {
            case 65536:
                resId = 2131625914;
                break;
            case 131072:
            case 196608:
                resId = 2131625915;
                break;
        }
        return getString(resId, new Object[]{getAccessibilityServiceInfo().getResolveInfo().loadLabel(getPackageManager())});
    }

    protected void onProcessArguments(Bundle arguments) {
        super.onProcessArguments(arguments);
        String settingsTitle = arguments.getString("settings_title");
        String settingsComponentName = arguments.getString("settings_component_name");
        if (!(TextUtils.isEmpty(settingsTitle) || TextUtils.isEmpty(settingsComponentName))) {
            Intent settingsIntent = new Intent("android.intent.action.MAIN").setComponent(ComponentName.unflattenFromString(settingsComponentName.toString()));
            if (!getPackageManager().queryIntentActivities(settingsIntent, 0).isEmpty()) {
                this.mSettingsTitle = settingsTitle;
                this.mSettingsIntent = settingsIntent;
                this.mSettingsMenuIcon = arguments.getInt("menu_icon");
                setHasOptionsMenu(true);
            }
        }
        this.mComponentName = (ComponentName) arguments.getParcelable("component_name");
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (this.mToggleSwitch == preference) {
            if (((Boolean) newValue).booleanValue()) {
                getArguments().putBoolean("checked", false);
                removeDialog(2);
                showDialog(1);
                setOnDialogCancelListener();
            } else {
                getArguments().putBoolean("checked", true);
                removeDialog(1);
                showDialog(2);
                setOnDialogCancelListener();
            }
        }
        return true;
    }

    protected void onInstallToggleSwitch() {
        this.mToggleSwitch.setTitle(getArguments().getString("title"));
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
    }

    private void restoreSwitchState() {
        this.mToggleSwitch.setOnPreferenceChangeListener(null);
        this.mSettingsContentObserver.dispatchChange(false);
        this.mToggleSwitch.setOnPreferenceChangeListener(this);
    }

    private void setOnDialogCancelListener() {
        setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                if (dialog != null) {
                    ToggleAccessibilityServicePreferenceFragment.this.onClick(dialog, -2);
                }
            }
        });
    }
}
