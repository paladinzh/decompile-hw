package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.SystemProperties;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceCategory;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.MLog;
import com.android.settings.SdCardLockUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class StorageSettingsHwBase extends SettingsPreferenceFragment {
    private static int mSwitchVolumeIndex = -1;
    private ListPreference mDefaultStoragePreference;
    protected Dialog mDialog = null;
    private boolean mIsShowDefaultSwitch = true;
    private final BroadcastReceiver mMediaShareChangeReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals("android.intent.action.MEDIA_SHARED")) {
                    StorageSettingsHwBase.this.mDefaultStoragePreference.setEnabled(false);
                    if (StorageSettingsHwBase.this.mDialog != null && StorageSettingsHwBase.this.mDialog.isShowing()) {
                        StorageSettingsHwBase.this.mDialog.dismiss();
                    }
                } else if (action.equals("android.intent.action.MEDIA_UNSHARED")) {
                    StorageSettingsHwBase.this.mDefaultStoragePreference.setEnabled(true);
                } else if ((action.equals("android.intent.action.MEDIA_BAD_REMOVAL") || action.equals("android.intent.action.MEDIA_REMOVED")) && StorageSettingsHwBase.this.mDialog != null && StorageSettingsHwBase.this.mDialog.isShowing()) {
                    StorageSettingsHwBase.this.mDialog.dismiss();
                }
            }
        }
    };
    protected StorageManager mStorageManager;
    PreferenceCategory mStorgeSettingsCategory;
    private List<CharSequence> mVolumes = new ArrayList();
    private List<CharSequence> mVolumesIndex = new ArrayList();

    public void onDestroy() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
        getActivity().unregisterReceiver(this.mMediaShareChangeReceiver);
        super.onDestroy();
    }

    protected void initHwStorageExtension() {
        this.mStorageManager = (StorageManager) getSystemService("storage");
        this.mStorgeSettingsCategory = (PreferenceCategory) findPreference("category_storage_settings");
        initDefaultStorage();
        updateDefaultStorageStatus();
        Preference storageCleanerPreferece = this.mStorgeSettingsCategory.findPreference("storage_cleaner");
        if (!(Utils.hasIntentActivity(getPackageManager(), storageCleanerPreferece.getIntent()) && Utils.isOwnerUser())) {
            this.mStorgeSettingsCategory.removePreference(storageCleanerPreferece);
            MLog.w("StorageSettings", "remove storage cleaner preference");
        }
        if (this.mStorgeSettingsCategory.getPreferenceCount() < 1) {
            getPreferenceScreen().removePreference(this.mStorgeSettingsCategory);
            MLog.w("StorageSettings", "remove storage settings category");
        }
        IntentFilter intentFilter_Shared = new IntentFilter("android.intent.action.MEDIA_SHARED");
        intentFilter_Shared.addAction("android.intent.action.MEDIA_UNSHARED");
        intentFilter_Shared.addAction("android.intent.action.MEDIA_BAD_REMOVAL");
        intentFilter_Shared.addAction("android.intent.action.MEDIA_REMOVED");
        intentFilter_Shared.addDataScheme("file");
        getActivity().registerReceiver(this.mMediaShareChangeReceiver, intentFilter_Shared);
    }

    private void initDefaultStorage() {
        this.mDefaultStoragePreference = (StorageListPreference) this.mStorgeSettingsCategory.findPreference("default_storage_location");
        asyncGetSdcardState();
    }

    private void updateDefaultStoragePreference() {
        this.mDefaultStoragePreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String current = StorageSettingsHwBase.this.mDefaultStoragePreference.getValue();
                if (current == null || current.isEmpty()) {
                    StorageSettingsHwBase.mSwitchVolumeIndex = -1;
                } else {
                    StorageSettingsHwBase.mSwitchVolumeIndex = Integer.valueOf(current).intValue();
                }
                if (Integer.valueOf((String) newValue).intValue() == StorageSettingsHwBase.mSwitchVolumeIndex) {
                    return true;
                }
                if (!DefaultStorageLocation.isInternal()) {
                    StorageSettingsHwBase.this.showDialog(3);
                } else if (LockPatternUtils.isDeviceEncryptionEnabled()) {
                    StorageSettingsHwBase.this.showDialog(4);
                } else if (StorageSettingsHwBase.this.isOutCapacityLowerThanInternal()) {
                    StorageSettingsHwBase.this.showDialog(5);
                } else {
                    StorageSettingsHwBase.this.showDialog(3);
                }
                return true;
            }
        });
    }

    private void asyncGetSdcardState() {
        new AsyncTask<Void, Void, Boolean>() {
            protected Boolean doInBackground(Void... params) {
                boolean z;
                if (!Utils.isSwitchPrimaryVolumeSupported() || Utils.hasEncryptedSdcardAsPrimary(StorageSettingsHwBase.this.getActivity())) {
                    z = true;
                } else {
                    z = SdCardLockUtils.isPasswordProtected(StorageSettingsHwBase.this.getActivity());
                }
                return Boolean.valueOf(z);
            }

            protected void onPostExecute(Boolean result) {
                if (result.booleanValue()) {
                    MLog.w("StorageSettings", "switch default storge loaction not supported!");
                    if (StorageSettingsHwBase.this.mStorgeSettingsCategory != null) {
                        StorageSettingsHwBase.this.mStorgeSettingsCategory.removePreference(StorageSettingsHwBase.this.mDefaultStoragePreference);
                        StorageSettingsHwBase.this.mIsShowDefaultSwitch = false;
                        return;
                    }
                    return;
                }
                StorageSettingsHwBase.this.updateDefaultStoragePreference();
            }
        }.execute(new Void[0]);
    }

    private boolean addVolumes(StorageVolume storageVolume) {
        Object storageState = null;
        try {
            storageState = this.mStorageManager.getVolumeState(storageVolume.getPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        if ("bad_removal".equals(storageState) || "removed".equals(storageState) || "unmounted".equals(storageState)) {
            return false;
        }
        this.mVolumes.add(storageVolume.getUserLabel());
        this.mVolumesIndex.add("" + this.mVolumesIndex.size());
        return true;
    }

    public void updateDefaultStorageStatus() {
        if (this.mIsShowDefaultSwitch) {
            if (getActivity() == null || this.mDefaultStoragePreference == null) {
                MLog.w("StorageSettings", "no need to update default storage status");
            } else {
                this.mVolumes.clear();
                this.mVolumesIndex.clear();
                boolean isSdCardChecked = false;
                for (StorageVolume volume : this.mStorageManager.getVolumeList()) {
                    if (Utils.isVolumeExternalSDcard(getActivity(), volume)) {
                        if (!isSdCardChecked && isSdcardAvailable(volume)) {
                            addVolumes(volume);
                        }
                        isSdCardChecked = true;
                    } else if (!Utils.isVolumeUsb(getActivity(), volume)) {
                        addVolumes(volume);
                    }
                }
                if (this.mVolumes.size() < 1) {
                    this.mStorgeSettingsCategory.removePreference(this.mDefaultStoragePreference);
                    this.mIsShowDefaultSwitch = false;
                    MLog.w("StorageSettings", "No volume can be set default storage!");
                    return;
                }
                this.mDefaultStoragePreference.setEntries((CharSequence[]) this.mVolumes.toArray(new CharSequence[this.mVolumes.size()]));
                this.mDefaultStoragePreference.setEntryValues((CharSequence[]) this.mVolumesIndex.toArray(new CharSequence[this.mVolumesIndex.size()]));
                if (externalStorageShared()) {
                    this.mDefaultStoragePreference.setEnabled(false);
                } else {
                    this.mDefaultStoragePreference.setEnabled(true);
                }
                if (DefaultStorageLocation.isSdcard()) {
                    this.mDefaultStoragePreference.setValue("1");
                    this.mDefaultStoragePreference.setSummary(2131627416);
                } else {
                    this.mDefaultStoragePreference.setValue("0");
                    this.mDefaultStoragePreference.setSummary(2131627575);
                }
            }
        }
    }

    private boolean externalStorageShared() {
        for (StorageVolume volume : this.mStorageManager.getVolumeList()) {
            if ("shared".equals(this.mStorageManager.getVolumeState(volume.getPath()))) {
                return true;
            }
        }
        return false;
    }

    protected Dialog createStorageInUseDialog() {
        int resId;
        if (DefaultStorageLocation.isSdcard()) {
            resId = 2131627508;
        } else {
            resId = 2131627509;
            if ("0".equals(SystemProperties.get("persist.sys.is_switch_sdcard", "0"))) {
                resId = 2131628126;
            }
        }
        setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                StorageSettingsHwBase.this.mDefaultStoragePreference.setValue("" + StorageSettingsHwBase.mSwitchVolumeIndex);
            }
        });
        return new Builder(getActivity()).setTitle(2131627359).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                SystemProperties.set("persist.sys.is_switch_sdcard", "1");
                DefaultStorageLocation.switchVolume(StorageSettingsHwBase.this.getActivity());
            }
        }).setNegativeButton(2131624572, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StorageSettingsHwBase.this.mDefaultStoragePreference.setValue("" + StorageSettingsHwBase.mSwitchVolumeIndex);
            }
        }).setMessage(resId).create();
    }

    protected Dialog createSwitchWarningDialog(final int type) {
        int resId = 0;
        switch (type) {
            case 4:
                resId = 2131628132;
                break;
            case 5:
                resId = 2131628133;
                break;
        }
        setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                StorageSettingsHwBase.this.mDefaultStoragePreference.setValue("" + StorageSettingsHwBase.mSwitchVolumeIndex);
            }
        });
        return new Builder(getActivity()).setTitle(2131627350).setPositiveButton(2131625656, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StorageSettingsHwBase.this.showNextWarningDialog(type);
            }
        }).setNegativeButton(2131624572, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                StorageSettingsHwBase.this.mDefaultStoragePreference.setValue("" + StorageSettingsHwBase.mSwitchVolumeIndex);
            }
        }).setMessage(resId).create();
    }

    private void showNextWarningDialog(int currentType) {
        switch (currentType) {
            case 4:
                if (isOutCapacityLowerThanInternal()) {
                    showDialog(5);
                    return;
                } else {
                    showDialog(3);
                    return;
                }
            case 5:
                showDialog(3);
                return;
            default:
                return;
        }
    }

    private boolean isOutCapacityLowerThanInternal() {
        long internalTotalSize = 0;
        long externalTotalSize = 0;
        for (StorageVolume volume : this.mStorageManager.getVolumeList()) {
            if (volume != null) {
                if (volume.isEmulated()) {
                    internalTotalSize += volume.getPathFile().getTotalSpace();
                } else if (Utils.isVolumeExternalSDcard(getActivity(), volume) && isSdCardAvailableAndWriteable(volume)) {
                    File pathFile = volume.getPathFile();
                    if (externalTotalSize <= 0) {
                        externalTotalSize = pathFile.getTotalSpace();
                    }
                }
            }
        }
        if (externalTotalSize >= 8589934592L || externalTotalSize >= internalTotalSize) {
            return false;
        }
        return true;
    }

    private boolean isSdCardAvailableAndWriteable(StorageVolume volume) {
        boolean unavailable;
        String state = this.mStorageManager.getVolumeState(volume.getPath());
        if ("unmountable".equals(state) || "nofs".equals(state) || HwCustStatusImpl.SUMMARY_UNKNOWN.equals(state) || "mounted_ro".equals(state) || "checking".equals(state) || "bad_removal".equals(state) || "removed".equals(state)) {
            unavailable = true;
        } else {
            unavailable = "unmounted".equals(state);
        }
        if (unavailable) {
            return false;
        }
        return true;
    }

    public Dialog onCreateDialog(int id) {
        this.mDialog = null;
        switch (id) {
            case 3:
                this.mDialog = createStorageInUseDialog();
                break;
            case 4:
                this.mDialog = createSwitchWarningDialog(4);
                break;
            case 5:
                this.mDialog = createSwitchWarningDialog(5);
                break;
        }
        return this.mDialog;
    }

    private boolean isSdcardAvailable(StorageVolume volume) {
        return "mounted".equals(getStorageManager().getVolumeState(volume.getPath()));
    }

    private StorageManager getStorageManager() {
        if (this.mStorageManager == null) {
            this.mStorageManager = (StorageManager) getSystemService("storage");
        }
        return this.mStorageManager;
    }
}
