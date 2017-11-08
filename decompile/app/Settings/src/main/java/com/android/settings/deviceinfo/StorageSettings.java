package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.text.format.Formatter.BytesResult;
import android.util.Log;
import android.widget.Toast;
import com.android.settings.HwCustSplitUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.ProgressBarPreference;
import com.android.settings.Utils;
import com.android.settings.sdencryption.SdEncryptionUtils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.huawei.cust.HwCustUtils;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class StorageSettings extends StorageSettingsHwBase implements Indexable {
    static final int[] COLOR_PRIVATE = new int[]{Color.parseColor("#ff88ca54"), Color.parseColor("#ff5bb7f5"), Color.parseColor("#ff2cc4ca"), Color.parseColor("#ffffc734"), Color.parseColor("#ffff694d")};
    static final int COLOR_PUBLIC = Color.parseColor("#ff9e9e9e");
    static final int COLOR_WARNING = Color.parseColor("#fff4511e");
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625227);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625650);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
            for (VolumeInfo vol : storage.getVolumes()) {
                if (StorageSettings.isInteresting(vol)) {
                    data.title = storage.getBestVolumeDescription(vol);
                    data.screenTitle = context.getString(2131625227);
                    result.add(data);
                }
            }
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625257);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625255);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625262);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625263);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625261);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625265);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131625264);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131626963);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            if (Utils.isSwitchPrimaryVolumeSupported() && !Utils.hasEncryptedSdcardAsPrimary(context)) {
                data = new SearchIndexableRaw(context);
                data.title = context.getString(2131627697);
                data.screenTitle = context.getString(2131625227);
                result.add(data);
            }
            if (Utils.hasIntentActivity(context.getPackageManager(), "huawei.intent.action.HSM_STORAGE_CLEANER") && Utils.isOwnerUser()) {
                data = new SearchIndexableRaw(context);
                data.title = context.getString(2131627485);
                data.screenTitle = context.getString(2131625227);
                result.add(data);
            }
            data = new SearchIndexableRaw(context);
            data.title = context.getString(2131628125);
            data.screenTitle = context.getString(2131625227);
            result.add(data);
            return result;
        }
    };
    private PreferenceCategory mExternalCategory;
    private PreferenceCategory mInternalCategory;
    private ProgressBarPreference mInternalSummary;
    private boolean mIsTransferToPrivate = false;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (StorageSettings.isInteresting(vol)) {
                StorageSettings.this.refresh();
            }
        }

        public void onDiskDestroyed(DiskInfo disk) {
            StorageSettings.this.refresh();
        }
    };
    private StorageManager mStorageManager;

    public static class DiskInitFragment extends DialogFragment {
        public static void show(Fragment parent, int resId, String diskId) {
            if (parent.isAdded()) {
                Bundle args = new Bundle();
                args.putInt("android.intent.extra.TEXT", resId);
                args.putString("android.os.storage.extra.DISK_ID", diskId);
                DiskInitFragment dialog = new DiskInitFragment();
                dialog.setArguments(args);
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), "disk_init");
                return;
            }
            Log.w("StorageSettings", "DiskInitFragment-->show()-->parent not added!");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            StorageManager sm = (StorageManager) context.getSystemService(StorageManager.class);
            int resId = getArguments().getInt("android.intent.extra.TEXT");
            final String diskId = getArguments().getString("android.os.storage.extra.DISK_ID");
            DiskInfo disk = sm.findDiskById(diskId);
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(resId), new CharSequence[]{disk.getDescription()}));
            builder.setPositiveButton(2131625293, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    Intent intent = new Intent(context, StorageWizardFormatConfirm.class);
                    intent.putExtra("android.os.storage.extra.DISK_ID", diskId);
                    intent.putExtra("format_private", false);
                    DiskInitFragment.this.startActivity(intent);
                }
            });
            builder.setNegativeButton(2131624572, null);
            return builder.create();
        }
    }

    public static class MountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final String mDescription;
        private final StorageManager mStorageManager = ((StorageManager) this.mContext.getSystemService(StorageManager.class));
        private final String mVolumeId;

        public MountTask(Context context, VolumeInfo volume) {
            this.mContext = context.getApplicationContext();
            this.mVolumeId = volume.getId();
            this.mDescription = this.mStorageManager.getBestVolumeDescription(volume);
        }

        protected Exception doInBackground(Void... params) {
            try {
                this.mStorageManager.mount(this.mVolumeId);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        protected void onPostExecute(Exception e) {
            if (e == null) {
                Toast.makeText(this.mContext, this.mContext.getString(2131625310, new Object[]{this.mDescription}), 0).show();
                return;
            }
            Log.e("StorageSettings", "Failed to mount " + this.mVolumeId, e);
            Toast.makeText(this.mContext, this.mContext.getString(2131625311, new Object[]{this.mDescription}), 0).show();
        }
    }

    public static class UnmountTask extends AsyncTask<Void, Void, Exception> {
        private final Context mContext;
        private final String mDescription;
        private final StorageManager mStorageManager = ((StorageManager) this.mContext.getSystemService(StorageManager.class));
        private final String mVolumeId;

        public UnmountTask(Context context, VolumeInfo volume) {
            this.mContext = context.getApplicationContext();
            this.mVolumeId = volume.getId();
            this.mDescription = this.mStorageManager.getBestVolumeDescription(volume);
        }

        protected Exception doInBackground(Void... params) {
            try {
                this.mStorageManager.unmount(this.mVolumeId);
                return null;
            } catch (Exception e) {
                return e;
            }
        }

        protected void onPostExecute(Exception e) {
            if (e == null) {
                Toast.makeText(this.mContext, this.mContext.getString(2131625312, new Object[]{this.mDescription}), 0).show();
                return;
            }
            Log.e("StorageSettings", "Failed to unmount " + this.mVolumeId, e);
            Toast.makeText(this.mContext, this.mContext.getString(2131625313, new Object[]{this.mDescription}), 0).show();
        }
    }

    public static class VolumeUnmountedFragment extends DialogFragment {
        public static void show(Fragment parent, String volumeId) {
            if (parent.isAdded()) {
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", volumeId);
                VolumeUnmountedFragment dialog = new VolumeUnmountedFragment();
                dialog.setArguments(args);
                dialog.setTargetFragment(parent, 0);
                dialog.show(parent.getFragmentManager(), "volume_unmounted");
                return;
            }
            Log.w("StorageSettings", "VolumeUnmountedFragment-->show()-->parent not added!");
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            final VolumeInfo vol = ((StorageManager) context.getSystemService(StorageManager.class)).findVolumeById(getArguments().getString("android.os.storage.extra.VOLUME_ID"));
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(2131625317), new CharSequence[]{vol.getDisk().getDescription()}));
            builder.setPositiveButton(2131625286, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(VolumeUnmountedFragment.this.getActivity(), "no_physical_media", UserHandle.myUserId());
                    boolean hasBaseUserRestriction = RestrictedLockUtils.hasBaseUserRestriction(VolumeUnmountedFragment.this.getActivity(), "no_physical_media", UserHandle.myUserId());
                    if (admin == null || hasBaseUserRestriction) {
                        new MountTask(context, vol).execute(new Void[0]);
                    } else {
                        RestrictedLockUtils.sendShowAdminSupportDetailsIntent(VolumeUnmountedFragment.this.getActivity(), admin);
                    }
                }
            });
            builder.setNegativeButton(2131624572, null);
            return builder.create();
        }
    }

    protected int getMetricsCategory() {
        return 42;
    }

    protected int getHelpResource() {
        return 2131626531;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        this.mStorageManager.registerListener(this.mStorageListener);
        addPreferencesFromResource(2131230775);
        this.mInternalCategory = (PreferenceCategory) findPreference("storage_internal");
        this.mExternalCategory = (PreferenceCategory) findPreference("storage_external");
        this.mInternalSummary = new ProgressBarPreference(context);
        setHasOptionsMenu(true);
        initHwStorageExtension();
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private static boolean isInteresting(VolumeInfo vol) {
        switch (vol.getType()) {
            case 0:
            case 1:
                return true;
            default:
                return false;
        }
    }

    private void refresh() {
        if (isAdded()) {
            Preference storageVolumePreference;
            Context context = getPrefContext();
            getPreferenceScreen().removePreference(this.mInternalCategory);
            getPreferenceScreen().removePreference(this.mExternalCategory);
            this.mInternalCategory.removeAll();
            this.mExternalCategory.removeAll();
            this.mInternalCategory.addPreference(this.mInternalSummary);
            int privateCount = 0;
            long privateUsedBytes = 0;
            long privateTotalBytes = 0;
            long privateFullBytes = 0;
            String privateVolumeUsageString = "";
            List<VolumeInfo> volumes = this.mStorageManager.getVolumes();
            Collections.sort(volumes, VolumeInfo.getDescriptionComparator());
            for (VolumeInfo vol : volumes) {
                if (vol.getType() == 1) {
                    int privateCount2 = privateCount + 1;
                    storageVolumePreference = new StorageVolumePreference(context, vol, COLOR_PRIVATE[privateCount % COLOR_PRIVATE.length]);
                    this.mInternalCategory.addPreference(storageVolumePreference);
                    if (vol.isMountedReadable()) {
                        File path = vol.getPath();
                        privateUsedBytes += path.getTotalSpace() - Utils.getStorageAvailableSize(this.mStorageManager);
                        privateTotalBytes += path.getTotalSpace();
                        privateFullBytes = Utils.ceilToSdcardSize(privateTotalBytes);
                        if (privateFullBytes < privateTotalBytes) {
                            privateFullBytes = privateTotalBytes;
                        } else {
                            privateUsedBytes += privateFullBytes - privateTotalBytes;
                        }
                        privateVolumeUsageString = getString(2131627576, new Object[]{Formatter.formatFileSize(context, privateFullBytes), Formatter.formatFileSize(context, privateFullBytes - privateUsedBytes)});
                        storageVolumePreference.setSummary((CharSequence) privateVolumeUsageString);
                        privateCount = privateCount2;
                    } else {
                        privateCount = privateCount2;
                    }
                } else if (!(vol.getType() != 0 || vol.state == 7 || vol.state == 8)) {
                    storageVolumePreference = new StorageVolumePreference(context, vol, COLOR_PUBLIC);
                    if (SdEncryptionUtils.isSdCardEncryptingOrDecrypting(vol)) {
                        storageVolumePreference.setEnabled(false);
                    }
                    this.mExternalCategory.addPreference(storageVolumePreference);
                }
            }
            for (VolumeRecord rec : this.mStorageManager.getVolumeRecords()) {
                if (rec.getType() == 1 && this.mStorageManager.findVolumeByUuid(rec.getFsUuid()) == null) {
                    Drawable icon = context.getDrawable(2130838439);
                    icon.mutate();
                    icon.setTint(COLOR_PUBLIC);
                    storageVolumePreference = new Preference(context);
                    storageVolumePreference.setLayoutResource(2130969154);
                    storageVolumePreference.setWidgetLayoutResource(2130968998);
                    storageVolumePreference.setKey(rec.getFsUuid());
                    storageVolumePreference.setTitle(rec.getNickname());
                    storageVolumePreference.setSummary(17040443);
                    storageVolumePreference.setIcon(icon);
                    this.mInternalCategory.addPreference(storageVolumePreference);
                }
            }
            for (DiskInfo disk : this.mStorageManager.getDisks()) {
                if (disk.volumeCount == 0 && disk.size > 0) {
                    storageVolumePreference = new Preference(context);
                    storageVolumePreference.setLayoutResource(2130969154);
                    storageVolumePreference.setWidgetLayoutResource(2130968998);
                    storageVolumePreference.setKey(disk.getId());
                    storageVolumePreference.setTitle(disk.getDescription());
                    storageVolumePreference.setSummary(17040440);
                    storageVolumePreference.setIcon(2130838439);
                    this.mExternalCategory.addPreference(storageVolumePreference);
                }
            }
            BytesResult result = Formatter.formatBytes(getResources(), privateUsedBytes, 0);
            this.mInternalSummary.setTitle((CharSequence) privateVolumeUsageString);
            this.mInternalSummary.setPercent((int) ((((double) privateUsedBytes) / ((double) privateFullBytes)) * 100.0d));
            if (this.mInternalCategory.getPreferenceCount() > 0) {
                getPreferenceScreen().addPreference(this.mInternalCategory);
            }
            if (this.mExternalCategory.getPreferenceCount() > 0) {
                getPreferenceScreen().addPreference(this.mExternalCategory);
            }
            if (this.mInternalCategory.getPreferenceCount() == 2 && this.mExternalCategory.getPreferenceCount() == 0 && !this.mIsTransferToPrivate) {
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", "private");
                args.putBoolean("show_memory", true);
                Intent intent = Utils.onBuildStartFragmentIntent(getActivity(), PrivateVolumeSettings.class.getName(), args, null, 2131626958, null, false);
                intent.putExtra("show_drawer_menu", true);
                handleActivityForSplitMode(intent);
                getActivity().startActivity(intent);
                this.mIsTransferToPrivate = true;
                finish();
            }
            updateDefaultStorageStatus();
        }
    }

    private void handleActivityForSplitMode(Intent intent) {
        HwCustSplitUtils splitter = (HwCustSplitUtils) HwCustUtils.createObj(HwCustSplitUtils.class, new Object[]{getActivity()});
        if (splitter != null && splitter.reachSplitSize()) {
            splitter.setAsJumpedActivity(intent);
            splitter.setExitWhenContentGone(false);
        }
    }

    public void onResume() {
        super.onResume();
        this.mStorageManager.registerListener(this.mStorageListener);
        refresh();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public boolean onPreferenceTreeClick(Preference pref) {
        String key = pref.getKey();
        if (key == null) {
            return false;
        }
        Bundle args;
        if (pref instanceof StorageVolumePreference) {
            VolumeInfo vol = this.mStorageManager.findVolumeById(key);
            if (vol == null) {
                return false;
            }
            if (vol.getState() == 0) {
                VolumeUnmountedFragment.show(this, vol.getId());
                return true;
            } else if (vol.getState() == 6) {
                DiskInitFragment.show(this, 2131625318, vol.getDiskId());
                return true;
            } else if (vol.getType() == 1) {
                args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", vol.getId());
                startFragment(this, PrivateVolumeSettings.class.getCanonicalName(), -1, 0, args);
                return true;
            } else if (vol.getType() == 0) {
                args = new Bundle();
                args.putString("android.os.storage.extra.VOLUME_ID", vol.getId());
                startFragment(this, PublicVolumeSettings.class.getCanonicalName(), -1, 0, args);
                return true;
            }
        } else if (!TextUtils.isEmpty(key) && key.startsWith("disk:")) {
            DiskInitFragment.show(this, 2131625319, key);
            return true;
        } else if ("manage_memory".equals(key) || "default_storage_location".equals(key) || "storage_cleaner".equals(key)) {
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), pref);
            super.onPreferenceTreeClick(pref);
        } else {
            args = new Bundle();
            args.putString("android.os.storage.extra.FS_UUID", key);
            startFragment(this, PrivateVolumeForget.class.getCanonicalName(), 2131625292, 0, args);
            return true;
        }
        return false;
    }
}
