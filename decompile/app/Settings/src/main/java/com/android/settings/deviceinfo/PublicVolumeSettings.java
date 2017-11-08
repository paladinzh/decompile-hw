package com.android.settings.deviceinfo;

import android.app.ActivityManager;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.provider.DocumentsContract;
import android.support.v7.preference.Preference;
import android.text.format.Formatter;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.internal.util.Preconditions;
import com.android.settings.ProgressBarPreference;
import com.android.settings.SdCardLockUtils;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageSettings.MountTask;
import com.android.settings.deviceinfo.StorageSettings.UnmountTask;
import java.io.File;
import java.util.Objects;

public class PublicVolumeSettings extends SettingsPreferenceFragment {
    private CheckBox mClearSdCardPinCheckBox;
    private DiskInfo mDisk;
    private Preference mFormatPrivate;
    private Preference mFormatPublic;
    private boolean mIsPermittedToAdopt;
    private Preference mMount;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (Objects.equals(PublicVolumeSettings.this.mVolume.getId(), vol.getId())) {
                PublicVolumeSettings.this.mVolume = vol;
                PublicVolumeSettings.this.update();
            }
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            if (Objects.equals(PublicVolumeSettings.this.mVolume.getFsUuid(), rec.getFsUuid())) {
                PublicVolumeSettings.this.mVolume = PublicVolumeSettings.this.mStorageManager.findVolumeById(PublicVolumeSettings.this.mVolumeId);
                PublicVolumeSettings.this.update();
            }
        }
    };
    private StorageManager mStorageManager;
    private ProgressBarPreference mSummary;
    private Preference mUnmount;
    private VolumeInfo mVolume;
    private String mVolumeId;

    private boolean isVolumeValid() {
        if (this.mVolume == null || this.mVolume.getType() != 0) {
            return false;
        }
        return this.mVolume.isMountedReadable();
    }

    protected int getMetricsCategory() {
        return 42;
    }

    public void onCreate(Bundle icicle) {
        boolean z = false;
        super.onCreate(icicle);
        Context context = getActivity();
        if (UserManager.get(context).isAdminUser() && !ActivityManager.isUserAMonkey()) {
            z = true;
        }
        this.mIsPermittedToAdopt = z;
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        if ("android.provider.action.DOCUMENT_ROOT_SETTINGS".equals(getActivity().getIntent().getAction())) {
            Uri rootUri = getActivity().getIntent().getData();
            if (rootUri != null) {
                this.mVolume = this.mStorageManager.findVolumeByUuid(DocumentsContract.getRootId(rootUri));
            }
        } else {
            String volId = getArguments().getString("android.os.storage.extra.VOLUME_ID");
            if (volId != null) {
                this.mVolume = this.mStorageManager.findVolumeById(volId);
            }
        }
        if (isVolumeValid()) {
            this.mDisk = this.mStorageManager.findDiskById(this.mVolume.getDiskId());
            Preconditions.checkNotNull(this.mDisk);
            this.mVolumeId = this.mVolume.getId();
            addPreferencesFromResource(2131230776);
            getPreferenceScreen().setOrderingAsAdded(true);
            this.mSummary = new ProgressBarPreference(context);
            this.mMount = buildAction(2131625286);
            this.mUnmount = buildAction(2131625287);
            this.mFormatPublic = buildAction(2131625288);
            if (this.mIsPermittedToAdopt) {
                this.mFormatPrivate = buildAction(2131625290);
            }
            Preference storageCleanerPreferece = findPreference("storage_cleaner");
            if (!((storageCleanerPreferece == null || Utils.hasIntentActivity(getPackageManager(), storageCleanerPreferece.getIntent())) && Utils.isOwnerUser())) {
                removePreference("category_storage_settings", "storage_cleaner");
            }
            removeEmptyCategory("category_storage_settings");
            return;
        }
        getActivity().finish();
    }

    public void update() {
        if (isVolumeValid()) {
            getActivity().setTitle(this.mStorageManager.getBestVolumeDescription(this.mVolume));
            Context context = getActivity();
            getPreferenceScreen().removeAll();
            if (this.mVolume.isMountedReadable()) {
                addPreference(this.mSummary);
                File file = this.mVolume.getPath();
                long totalBytes = file.getTotalSpace();
                long usedBytes = totalBytes - file.getFreeSpace();
                this.mSummary.setTitle(getString(2131627576, new Object[]{Formatter.formatFileSize(context, totalBytes), Formatter.formatFileSize(context, freeBytes)}));
                this.mSummary.setPercent((int) ((((double) usedBytes) / ((double) totalBytes)) * 100.0d));
            }
            if (this.mVolume.getState() == 0) {
                addPreference(this.mMount);
            }
            if (this.mVolume.isMountedReadable()) {
                addPreference(this.mUnmount);
            }
            addPreference(this.mFormatPublic);
            if (DefaultStorageLocation.isSdcard() && Utils.isVolumeSDcard(this.mVolume)) {
                this.mUnmount.setEnabled(false);
                this.mFormatPublic.setEnabled(false);
            }
            return;
        }
        getActivity().finish();
    }

    private void addPreference(Preference pref) {
        pref.setOrder(HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID);
        getPreferenceScreen().addPreference(pref);
    }

    private Preference buildAction(int titleRes) {
        Preference pref = new Preference(getPrefContext());
        pref.setLayoutResource(2130968977);
        pref.setWidgetLayoutResource(2130968998);
        pref.setTitle(titleRes);
        return pref;
    }

    public void onResume() {
        super.onResume();
        this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        if (isVolumeValid()) {
            this.mStorageManager.registerListener(this.mStorageListener);
            update();
            return;
        }
        getActivity().finish();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        Context context = getActivity();
        if (pref == this.mMount) {
            new MountTask(context, this.mVolume).execute(new Void[0]);
        } else if (pref == this.mUnmount) {
            if (SdCardLockUtils.isPasswordProtected(context)) {
                showDialog(149);
            } else {
                unmount();
            }
        } else if (pref == this.mFormatPublic) {
            intent = new Intent(context, StorageWizardFormatConfirm.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            intent.putExtra("format_private", false);
            startActivity(intent);
        } else if (pref == this.mFormatPrivate) {
            intent = new Intent(context, StorageWizardFormatConfirm.class);
            intent.putExtra("android.os.storage.extra.DISK_ID", this.mDisk.getId());
            intent.putExtra("format_private", true);
            startActivity(intent);
        }
        return super.onPreferenceTreeClick(pref);
    }

    public Dialog onCreateDialog(int dialogId) {
        if (dialogId != 149) {
            return super.onCreateDialog(dialogId);
        }
        boolean isOwnerUser;
        View view = getActivity().getLayoutInflater().inflate(2130968618, null);
        this.mClearSdCardPinCheckBox = (CheckBox) view.findViewById(2131886216);
        this.mClearSdCardPinCheckBox.setText(2131628097);
        TextView message = (TextView) view.findViewById(16908299);
        if (ActivityManager.getCurrentUser() == 0) {
            isOwnerUser = true;
        } else {
            isOwnerUser = false;
        }
        if (isOwnerUser) {
            this.mClearSdCardPinCheckBox.setVisibility(0);
            message.setText(getResources().getString(2131628151));
        } else {
            this.mClearSdCardPinCheckBox.setVisibility(8);
            message.setText(getResources().getString(2131628157));
        }
        return new Builder(getActivity()).setTitle(2131625276).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (SdCardLockUtils.isSdCardUnlocked(PublicVolumeSettings.this.getActivity()) && PublicVolumeSettings.this.mClearSdCardPinCheckBox.isChecked() && isOwnerUser) {
                    SdCardLockUtils.clearSDLockPassword(PublicVolumeSettings.this.getActivity());
                }
                PublicVolumeSettings.this.unmount();
            }
        }).setNegativeButton(17039360, null).setView(view).create();
    }

    private void unmount() {
        Context context = getActivity();
        if (context != null) {
            new UnmountTask(context, this.mVolume).execute(new Void[0]);
            if (!Utils.isVolumeUsb(this.mVolume) && DefaultStorageLocation.isSdcard()) {
                Intent resetIntent = new Intent(context, DefaultStorageResettingActivity.class);
                resetIntent.setFlags(268435456);
                context.startActivity(resetIntent);
            }
        }
    }
}
