package com.android.settings.deviceinfo;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.storage.DiskInfo;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.os.storage.VolumeRecord;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.text.format.Formatter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import com.android.settings.ProgressBarPreference;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageSettings.MountTask;
import com.android.settingslib.deviceinfo.StorageMeasurement;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementDetails;
import com.android.settingslib.deviceinfo.StorageMeasurement.MeasurementReceiver;
import com.google.android.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class PrivateVolumeSettings extends SettingsPreferenceFragment {
    private static final int[] ITEMS_NO_SHOW_SHARED = new int[]{2131625325};
    private static final int[] ITEMS_SHOW_SHARED = new int[]{2131625325, 2131625326, 2131625327, 2131625328, 2131625330};
    private UserInfo mCurrentUser;
    private long mFirmWareBytes;
    private int mHeaderPoolIndex;
    private List<PreferenceCategory> mHeaderPreferencePool = Lists.newArrayList();
    private int mItemPoolIndex;
    private List<StorageItemPreference> mItemPreferencePool = Lists.newArrayList();
    private StorageMeasurement mMeasure;
    private boolean mNeedsUpdate;
    private final MeasurementReceiver mReceiver = new MeasurementReceiver() {
        public void onDetailsChanged(MeasurementDetails details) {
            PrivateVolumeSettings.this.updateDetails(details);
        }
    };
    private VolumeInfo mSharedVolume;
    private final StorageEventListener mStorageListener = new StorageEventListener() {
        public void onVolumeStateChanged(VolumeInfo vol, int oldState, int newState) {
            if (Objects.equals(PrivateVolumeSettings.this.mVolume.getId(), vol.getId())) {
                PrivateVolumeSettings.this.mVolume = vol;
                PrivateVolumeSettings.this.update();
            }
        }

        public void onVolumeRecordChanged(VolumeRecord rec) {
            if (Objects.equals(PrivateVolumeSettings.this.mVolume.getFsUuid(), rec.getFsUuid())) {
                PrivateVolumeSettings.this.mVolume = PrivateVolumeSettings.this.mStorageManager.findVolumeById(PrivateVolumeSettings.this.mVolumeId);
                PrivateVolumeSettings.this.update();
            }
        }
    };
    private StorageManager mStorageManager;
    private ProgressBarPreference mSummary;
    private UserManager mUserManager;
    private VolumeInfo mVolume;
    private String mVolumeId;

    private static class ClearCacheObserver extends Stub {
        private int mRemaining;
        private final PrivateVolumeSettings mTarget;

        public ClearCacheObserver(PrivateVolumeSettings target, int remaining) {
            this.mTarget = target;
            this.mRemaining = remaining;
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            synchronized (this) {
                int i = this.mRemaining - 1;
                this.mRemaining = i;
                if (i == 0) {
                    this.mTarget.getActivity().runOnUiThread(new Runnable() {
                        public void run() {
                            ClearCacheObserver.this.mTarget.update();
                        }
                    });
                }
            }
        }
    }

    public static class ConfirmClearCacheFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            final Context context = getActivity();
            Builder builder = new Builder(context);
            builder.setTitle(2131625273);
            builder.setMessage(getString(2131625274));
            builder.setPositiveButton(17039370, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    PrivateVolumeSettings target = (PrivateVolumeSettings) ConfirmClearCacheFragment.this.getTargetFragment();
                    PackageManager pm = context.getPackageManager();
                    for (int userId : ((UserManager) context.getSystemService(UserManager.class)).getProfileIdsWithDisabled(context.getUserId())) {
                        List<PackageInfo> infos = pm.getInstalledPackagesAsUser(0, userId);
                        ClearCacheObserver observer = new ClearCacheObserver(target, infos.size());
                        for (PackageInfo info : infos) {
                            pm.deleteApplicationCacheFilesAsUser(info.packageName, userId, observer);
                        }
                    }
                }
            });
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }
    }

    public static class OtherInfoFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            String title = getArguments().getString("android.intent.extra.TITLE");
            final Intent intent = (Intent) getArguments().getParcelable("android.intent.extra.INTENT");
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(2131625332), new CharSequence[]{title}));
            builder.setPositiveButton(2131625294, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    OtherInfoFragment.this.startActivity(intent);
                }
            });
            builder.setNegativeButton(17039360, null);
            return builder.create();
        }
    }

    public static class RenameFragment extends DialogFragment {
        public static void show(PrivateVolumeSettings parent, VolumeInfo vol) {
            if (parent.isAdded()) {
                RenameFragment dialog = new RenameFragment();
                dialog.setTargetFragment(parent, 0);
                Bundle args = new Bundle();
                args.putString("android.os.storage.extra.FS_UUID", vol.getFsUuid());
                dialog.setArguments(args);
                dialog.show(parent.getFragmentManager(), "rename");
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            final StorageManager storageManager = (StorageManager) context.getSystemService(StorageManager.class);
            final String fsUuid = getArguments().getString("android.os.storage.extra.FS_UUID");
            VolumeInfo vol = storageManager.findVolumeByUuid(fsUuid);
            VolumeRecord rec = storageManager.findRecordByUuid(fsUuid);
            Builder builder = new Builder(context);
            View view = LayoutInflater.from(builder.getContext()).inflate(2130968742, null, false);
            final EditText nickname = (EditText) view.findViewById(2131886503);
            nickname.setText(rec.getNickname());
            builder.setTitle(2131625316);
            builder.setView(view);
            builder.setPositiveButton(2131624575, new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    storageManager.setVolumeNickname(fsUuid, nickname.getText().toString());
                }
            });
            builder.setNegativeButton(2131624572, null);
            return builder.create();
        }
    }

    public static class UserInfoFragment extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Context context = getActivity();
            CharSequence userLabel = getArguments().getCharSequence("android.intent.extra.TITLE");
            CharSequence userSize = getArguments().getCharSequence("android.intent.extra.SUBJECT");
            Builder builder = new Builder(context);
            builder.setMessage(TextUtils.expandTemplate(getText(2131625333), new CharSequence[]{userLabel, userSize}));
            builder.setPositiveButton(17039370, null);
            return builder.create();
        }
    }

    private boolean isVolumeValid() {
        if (this.mVolume == null || this.mVolume.getType() != 1) {
            return false;
        }
        return this.mVolume.isMountedReadable();
    }

    public PrivateVolumeSettings() {
        setRetainInstance(true);
    }

    protected int getMetricsCategory() {
        return 42;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Context context = getActivity();
        this.mUserManager = (UserManager) context.getSystemService(UserManager.class);
        this.mStorageManager = (StorageManager) context.getSystemService(StorageManager.class);
        this.mVolumeId = getArguments().getString("android.os.storage.extra.VOLUME_ID");
        if (this.mVolumeId != null) {
            this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        }
        this.mSharedVolume = this.mStorageManager.findEmulatedForPrivate(this.mVolume);
        this.mMeasure = new StorageMeasurement(context, this.mVolume, this.mSharedVolume);
        this.mMeasure.setReceiver(this.mReceiver);
        if (isVolumeValid()) {
            addPreferencesFromResource(2131230776);
            if (!getArguments().getBoolean("show_memory", false)) {
                removePreference("manage_memory");
                removePreference("category_storage_settings");
            }
            getPreferenceScreen().setOrderingAsAdded(true);
            this.mSummary = new ProgressBarPreference(context);
            this.mCurrentUser = this.mUserManager.getUserInfo(UserHandle.myUserId());
            this.mNeedsUpdate = true;
            setHasOptionsMenu(true);
            Preference storageCleanerPreferece = findPreference("storage_cleaner");
            if (!((storageCleanerPreferece == null || Utils.hasIntentActivity(getPackageManager(), storageCleanerPreferece.getIntent())) && Utils.isOwnerUser())) {
                removePreference("category_storage_settings", "storage_cleaner");
            }
            removeEmptyCategory("category_storage_settings");
            return;
        }
        getActivity().finish();
    }

    private void setTitle() {
        List<DiskInfo> disks = this.mStorageManager.getDisks();
        if (disks == null || disks.size() <= 0) {
            getActivity().setTitle(2131628125);
        } else {
            getActivity().setTitle(this.mStorageManager.getBestVolumeDescription(this.mVolume));
        }
    }

    private int addUserAndProfiles(List<UserInfo> allUsers, boolean showHeaders, boolean showShared) {
        int addedUserCount = 0;
        int userCount = allUsers.size();
        for (int userIndex = 0; userIndex < userCount; userIndex++) {
            UserInfo userInfo = (UserInfo) allUsers.get(userIndex);
            if (isProfileOf(this.mCurrentUser, userInfo)) {
                if (userInfo.isClonedProfile()) {
                    addDetailItems(showHeaders ? addCategory(getPreferenceScreen(), getResources().getString(2131628938)) : getPreferenceScreen(), showShared, userInfo.id);
                } else {
                    addDetailItems(showHeaders ? addCategory(getPreferenceScreen(), userInfo.name) : getPreferenceScreen(), showShared, userInfo.id);
                }
                addedUserCount++;
            }
        }
        return addedUserCount;
    }

    private void update() {
        if (isVolumeValid()) {
            setTitle();
            getFragmentManager().invalidateOptionsMenu();
            Context context = getActivity();
            PreferenceGroup screen = (PreferenceGroup) findPreference("category_internal_storage");
            if (screen != null) {
                screen.removeAll();
                addPreference(screen, this.mSummary);
                List<UserInfo> allUsers = this.mUserManager.getUsers();
                int userCount = allUsers.size();
                boolean showHeaders = userCount > 1;
                boolean isMountedReadable = this.mSharedVolume != null ? this.mSharedVolume.isMountedReadable() : false;
                this.mItemPoolIndex = 0;
                this.mHeaderPoolIndex = 0;
                if (userCount - addUserAndProfiles(allUsers, showHeaders, isMountedReadable) > 0) {
                    PreferenceGroup otherUsers = addCategory(getPreferenceScreen(), getText(2131625303));
                    for (int userIndex = 0; userIndex < userCount; userIndex++) {
                        UserInfo userInfo = (UserInfo) allUsers.get(userIndex);
                        if (!isProfileOf(this.mCurrentUser, userInfo)) {
                            addItem(otherUsers, 0, userInfo.name, userInfo.id);
                        }
                    }
                }
                addItem(screen, 2131628673, null, -10000);
                addItem(screen, 2131625329, null, -10000);
                long totalBytes = this.mVolume.getPath().getTotalSpace();
                long fullBytes = Utils.ceilToSdcardSize(totalBytes);
                long freeBytes = Utils.getStorageAvailableSize(this.mStorageManager);
                this.mFirmWareBytes = 0;
                if (fullBytes > totalBytes) {
                    this.mFirmWareBytes = fullBytes - totalBytes;
                } else {
                    fullBytes = totalBytes;
                }
                long usedBytes = fullBytes - freeBytes;
                this.mSummary.setTitle(getString(2131627576, new Object[]{Formatter.formatFileSize(context, fullBytes), Formatter.formatFileSize(context, freeBytes)}));
                this.mSummary.setPercent((int) ((((double) usedBytes) / ((double) fullBytes)) * 100.0d));
                this.mMeasure.forceMeasure();
                this.mNeedsUpdate = false;
                return;
            }
            return;
        }
        getActivity().finish();
    }

    private void addPreference(PreferenceGroup group, Preference pref) {
        group.addPreference(pref);
    }

    private PreferenceCategory addCategory(PreferenceGroup group, CharSequence title) {
        PreferenceCategory category;
        if (this.mHeaderPoolIndex < this.mHeaderPreferencePool.size()) {
            category = (PreferenceCategory) this.mHeaderPreferencePool.get(this.mHeaderPoolIndex);
        } else {
            category = new PreferenceCategory(getPrefContext(), null, 16842892);
            this.mHeaderPreferencePool.add(category);
        }
        category.setLayoutResource(2130968916);
        category.setTitle(title);
        category.removeAll();
        addPreference(group, category);
        this.mHeaderPoolIndex++;
        return category;
    }

    private void addDetailItems(PreferenceGroup category, boolean showShared, int userId) {
        int[] itemsToAdd = showShared ? ITEMS_SHOW_SHARED : ITEMS_NO_SHOW_SHARED;
        for (int addItem : itemsToAdd) {
            addItem(category, addItem, null, userId);
        }
    }

    private void addItem(PreferenceGroup group, int titleRes, CharSequence title, int userId) {
        if (!isAddItemUnavailable(titleRes, title)) {
            StorageItemPreference item;
            if (this.mItemPoolIndex < this.mItemPreferencePool.size()) {
                item = (StorageItemPreference) this.mItemPreferencePool.get(this.mItemPoolIndex);
            } else {
                item = buildItem();
                this.mItemPreferencePool.add(item);
            }
            if (title != null) {
                item.setTitle(title);
                item.setKey(title.toString());
            } else {
                item.setTitle(titleRes);
                item.setKey(Integer.toString(titleRes));
            }
            item.setSummary(2131625258);
            item.userHandle = userId;
            addPreference(group, item);
            this.mItemPoolIndex++;
        }
    }

    private boolean isAddItemUnavailable(int res, CharSequence title) {
        if (title != null || res > 0) {
            return false;
        }
        Log.w("StorageSettings", "addItem failed , cause title is null and titleRes is not available");
        return true;
    }

    private StorageItemPreference buildItem() {
        return new StorageItemPreference(getPrefContext());
    }

    public void onResume() {
        super.onResume();
        this.mVolume = this.mStorageManager.findVolumeById(this.mVolumeId);
        if (isVolumeValid()) {
            this.mStorageManager.registerListener(this.mStorageListener);
            if (this.mNeedsUpdate) {
                update();
            } else {
                setTitle();
            }
            return;
        }
        getActivity().finish();
    }

    public void onPause() {
        super.onPause();
        this.mStorageManager.unregisterListener(this.mStorageListener);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mMeasure != null) {
            this.mMeasure.onDestroy();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(2132017160, menu);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        boolean z = false;
        if (isVolumeValid()) {
            MenuItem rename = menu.findItem(2131887657);
            MenuItem mount = menu.findItem(2131887658);
            MenuItem unmount = menu.findItem(2131887659);
            MenuItem format = menu.findItem(2131887660);
            MenuItem migrate = menu.findItem(2131887661);
            if ("private".equals(this.mVolume.getId())) {
                rename.setVisible(false);
                mount.setVisible(false);
                unmount.setVisible(false);
                format.setVisible(false);
            } else {
                boolean z2;
                if (this.mVolume.getType() == 1) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                rename.setVisible(z2);
                if (this.mVolume.getState() == 0) {
                    z2 = true;
                } else {
                    z2 = false;
                }
                mount.setVisible(z2);
                unmount.setVisible(this.mVolume.isMountedReadable());
                format.setVisible(true);
            }
            format.setTitle(2131625289);
            VolumeInfo privateVol = getActivity().getPackageManager().getPrimaryStorageCurrentVolume();
            if (!(privateVol == null || privateVol.getType() != 1 || Objects.equals(this.mVolume, privateVol))) {
                z = true;
            }
            migrate.setVisible(z);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Context context = getActivity();
        Bundle args = new Bundle();
        switch (item.getItemId()) {
            case 2131887657:
                RenameFragment.show(this, this.mVolume);
                return true;
            case 2131887658:
                new MountTask(context, this.mVolume).execute(new Void[0]);
                return true;
            case 2131887659:
                args.putString("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                startFragment(this, PrivateVolumeUnmount.class.getCanonicalName(), 2131625287, 0, args);
                return true;
            case 2131887660:
                args.putString("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                startFragment(this, PrivateVolumeFormat.class.getCanonicalName(), 2131625288, 0, args);
                return true;
            case 2131887661:
                Intent intent = new Intent(context, StorageWizardMigrateConfirm.class);
                intent.putExtra("android.os.storage.extra.VOLUME_ID", this.mVolume.getId());
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void updateDetails(MeasurementDetails details) {
        for (int i = 0; i < this.mItemPoolIndex; i++) {
            int itemTitleId;
            StorageItemPreference item = (StorageItemPreference) this.mItemPreferencePool.get(i);
            int userId = item.userHandle;
            try {
                itemTitleId = Integer.parseInt(item.getKey());
            } catch (NumberFormatException e) {
                itemTitleId = 0;
            }
            switch (itemTitleId) {
                case 0:
                    updatePreference(item, details.usersSize.get(userId));
                    break;
                case 2131625325:
                    updatePreference(item, details.appsSize.get(userId));
                    break;
                case 2131625326:
                    updatePreference(item, totalValues(details, userId, Environment.DIRECTORY_DCIM, Environment.DIRECTORY_MOVIES, Environment.DIRECTORY_PICTURES));
                    break;
                case 2131625327:
                    updatePreference(item, totalValues(details, userId, Environment.DIRECTORY_MOVIES));
                    break;
                case 2131625328:
                    updatePreference(item, totalValues(details, userId, Environment.DIRECTORY_MUSIC, Environment.DIRECTORY_ALARMS, Environment.DIRECTORY_NOTIFICATIONS, Environment.DIRECTORY_RINGTONES, Environment.DIRECTORY_PODCASTS));
                    break;
                case 2131625329:
                    updatePreference(item, details.cacheSize);
                    break;
                case 2131625330:
                    updatePreference(item, totalValues(details, userId, Environment.DIRECTORY_DOWNLOADS) + details.miscSize.get(userId));
                    break;
                case 2131628673:
                    updatePreference(item, this.mFirmWareBytes);
                    break;
                default:
                    String key = item.getKey();
                    if (!(key == null || userId == UserHandle.myUserId())) {
                        UserInfo userInfo = this.mUserManager.getUserInfo(userId);
                        if (userInfo != null && key.equals(userInfo.name)) {
                            updatePreference(item, details.usersSize.get(userId));
                            break;
                        }
                    }
            }
        }
    }

    private void updatePreference(StorageItemPreference pref, long size) {
        pref.setSummary(Formatter.formatFileSize(getActivity(), size));
    }

    private boolean isProfileOf(UserInfo user, UserInfo profile) {
        if (user.id == profile.id) {
            return true;
        }
        if (user.profileGroupId != -10000) {
            return user.profileGroupId == profile.profileGroupId;
        } else {
            return false;
        }
    }

    private static long totalValues(MeasurementDetails details, int userId, String... keys) {
        long total = 0;
        HashMap<String, Long> map = (HashMap) details.mediaSize.get(userId);
        if (map != null) {
            for (String key : keys) {
                if (map.containsKey(key)) {
                    total += ((Long) map.get(key)).longValue();
                }
            }
        } else {
            Log.w("StorageSettings", "MeasurementDetails mediaSize array does not have key for user " + userId);
        }
        return total;
    }
}
