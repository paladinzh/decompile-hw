package com.android.settings.applications;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.UriPermission;
import android.content.pm.IPackageDataObserver.Stub;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.UserHandle;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceCategory;
import android.text.format.Formatter;
import android.util.Log;
import android.util.MutableInt;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.android.settings.ParentControl;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.StorageWizardMoveConfirm;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.TreeMap;

public class AppStorageSettings extends AppInfoWithHeader implements OnClickListener, Callbacks, DialogInterface.OnClickListener {
    private static final String TAG = AppStorageSettings.class.getSimpleName();
    private Preference mAppSize;
    private Preference mCacheSize;
    private boolean mCanClearData = true;
    private VolumeInfo[] mCandidates;
    private Button mChangeStorageButton;
    private Button mClearCacheButton;
    private ClearCacheObserver mClearCacheObserver;
    private Button mClearDataButton;
    private ClearUserDataObserver mClearDataObserver;
    private LayoutPreference mClearUri;
    private Button mClearUriButton;
    private CharSequence mComputingStr;
    private Preference mDataSize;
    private Builder mDialogBuilder;
    private Preference mExternalCodeSize;
    private Preference mExternalDataSize;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (AppStorageSettings.this.getView() != null) {
                switch (msg.what) {
                    case 1:
                        AppStorageSettings.this.processClearMsg(msg);
                        break;
                    case 3:
                        AppStorageSettings.this.mState.requestSize(AppStorageSettings.this.mPackageName, AppStorageSettings.this.mUserId);
                        break;
                }
            }
        }
    };
    private boolean mHaveSizes = false;
    private CharSequence mInvalidSizeStr;
    private long mLastCacheSize = -1;
    private long mLastCodeSize = -1;
    private long mLastDataSize = -1;
    private long mLastExternalCodeSize = -1;
    private long mLastExternalDataSize = -1;
    private long mLastTotalSize = -1;
    private Preference mStorageUsed;
    private Preference mTotalSize;
    private PreferenceCategory mUri;

    class ClearCacheObserver extends Stub {
        ClearCacheObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            Message msg = AppStorageSettings.this.mHandler.obtainMessage(3);
            msg.arg1 = succeeded ? 1 : 2;
            AppStorageSettings.this.mHandler.sendMessage(msg);
        }
    }

    class ClearUserDataObserver extends Stub {
        ClearUserDataObserver() {
        }

        public void onRemoveCompleted(String packageName, boolean succeeded) {
            int i = 1;
            Message msg = AppStorageSettings.this.mHandler.obtainMessage(1);
            if (!succeeded) {
                i = 2;
            }
            msg.arg1 = i;
            AppStorageSettings.this.mHandler.sendMessage(msg);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230741);
        setupViews();
        if (this.mAppEntry != null) {
            initMoveDialog();
        }
    }

    public void onResume() {
        super.onResume();
        this.mState.requestSize(this.mPackageName, this.mUserId);
        disableParentControlItems();
    }

    private void setupViews() {
        this.mComputingStr = getActivity().getText(2131625672);
        this.mInvalidSizeStr = getActivity().getText(2131625673);
        this.mTotalSize = findPreference("total_size");
        this.mAppSize = findPreference("app_size");
        this.mDataSize = findPreference("data_size");
        this.mExternalCodeSize = findPreference("external_code_size");
        this.mExternalDataSize = findPreference("external_data_size");
        if (Environment.isExternalStorageEmulated()) {
            PreferenceCategory category = (PreferenceCategory) findPreference("storage_category");
            category.removePreference(this.mExternalCodeSize);
            category.removePreference(this.mExternalDataSize);
        }
        this.mClearDataButton = (Button) ((LayoutPreference) findPreference("clear_data_button")).findViewById(2131887199);
        this.mStorageUsed = findPreference("storage_used");
        this.mChangeStorageButton = (Button) ((LayoutPreference) findPreference("change_storage_button")).findViewById(2131887198);
        this.mChangeStorageButton.setText(2131626902);
        this.mChangeStorageButton.setOnClickListener(this);
        this.mCacheSize = findPreference("cache_size");
        this.mClearCacheButton = (Button) ((LayoutPreference) findPreference("clear_cache_button")).findViewById(2131887200);
        this.mClearCacheButton.setText(2131625606);
        this.mUri = (PreferenceCategory) findPreference("uri_category");
        this.mClearUri = (LayoutPreference) this.mUri.findPreference("clear_uri_button");
        this.mClearUriButton = (Button) this.mClearUri.findViewById(2131886824);
        this.mClearUriButton.setText(2131625608);
        this.mClearUriButton.setOnClickListener(this);
    }

    public void onClick(View v) {
        if (v == this.mClearCacheButton) {
            if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
                if (this.mClearCacheObserver == null) {
                    this.mClearCacheObserver = new ClearCacheObserver();
                }
                this.mPm.deleteApplicationCacheFiles(this.mPackageName, this.mClearCacheObserver);
            } else {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
            }
        } else if (v == this.mClearDataButton) {
            if (this.mAppsControlDisallowedAdmin != null && !this.mAppsControlDisallowedBySystem) {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
            } else if (this.mAppEntry.info.manageSpaceActivityName == null) {
                showDialogInner(1, 0);
            } else if (!Utils.isMonkeyRunning()) {
                Intent intent = new Intent("android.intent.action.VIEW");
                intent.setClassName(this.mAppEntry.info.packageName, this.mAppEntry.info.manageSpaceActivityName);
                try {
                    startActivityForResult(intent, 2);
                } catch (ActivityNotFoundException e) {
                    Log.i(TAG, "Do not find the Activity");
                }
            }
        } else if (v == this.mChangeStorageButton && this.mDialogBuilder != null && !isMoveInProgress()) {
            this.mDialogBuilder.show();
        } else if (v == this.mClearUriButton) {
            if (this.mAppsControlDisallowedAdmin == null || this.mAppsControlDisallowedBySystem) {
                clearUriPermissions();
            } else {
                RestrictedLockUtils.sendShowAdminSupportDetailsIntent(getActivity(), this.mAppsControlDisallowedAdmin);
            }
        }
    }

    private boolean isMoveInProgress() {
        try {
            AppGlobals.getPackageManager().checkPackageStartable(this.mPackageName, UserHandle.myUserId());
            return false;
        } catch (RemoteException e) {
            return true;
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        Context context = getActivity();
        VolumeInfo targetVol = this.mCandidates[which];
        if (!Objects.equals(targetVol, context.getPackageManager().getPackageCurrentVolume(this.mAppEntry.info))) {
            Intent intent = new Intent(context, StorageWizardMoveConfirm.class);
            intent.putExtra("android.os.storage.extra.VOLUME_ID", targetVol.getId());
            intent.putExtra("android.intent.extra.PACKAGE_NAME", this.mAppEntry.info.packageName);
            startActivity(intent);
        }
        dialog.dismiss();
    }

    private String getSizeStr(long size) {
        if (size == -1) {
            return this.mInvalidSizeStr.toString();
        }
        return Formatter.formatFileSize(getActivity(), size);
    }

    private void refreshSizeInfo() {
        int parentStatus = ParentControl.getParentControlStatus(getActivity());
        if (this.mAppEntry.size == -2 || this.mAppEntry.size == -1) {
            this.mLastTotalSize = -1;
            this.mLastCacheSize = -1;
            this.mLastDataSize = -1;
            this.mLastCodeSize = -1;
            if (!this.mHaveSizes) {
                this.mAppSize.setSummary(this.mComputingStr);
                this.mDataSize.setSummary(this.mComputingStr);
                this.mCacheSize.setSummary(this.mComputingStr);
                this.mTotalSize.setSummary(this.mComputingStr);
            }
            this.mClearDataButton.setEnabled(false);
            this.mClearCacheButton.setEnabled(false);
        } else {
            this.mHaveSizes = true;
            long codeSize = this.mAppEntry.codeSize;
            long dataSize = this.mAppEntry.dataSize;
            if (Environment.isExternalStorageEmulated()) {
                codeSize += this.mAppEntry.externalCodeSize;
                dataSize += this.mAppEntry.externalDataSize;
            } else {
                if (this.mLastExternalCodeSize != this.mAppEntry.externalCodeSize) {
                    this.mLastExternalCodeSize = this.mAppEntry.externalCodeSize;
                    this.mExternalCodeSize.setSummary(getSizeStr(this.mAppEntry.externalCodeSize));
                }
                if (this.mLastExternalDataSize != this.mAppEntry.externalDataSize) {
                    this.mLastExternalDataSize = this.mAppEntry.externalDataSize;
                    this.mExternalDataSize.setSummary(getSizeStr(this.mAppEntry.externalDataSize));
                }
            }
            if (this.mLastCodeSize != codeSize) {
                this.mLastCodeSize = codeSize;
                this.mAppSize.setSummary(getSizeStr(codeSize));
            }
            if (this.mLastDataSize != dataSize) {
                this.mLastDataSize = dataSize;
                this.mDataSize.setSummary(getSizeStr(dataSize));
            }
            long cacheSize = this.mAppEntry.cacheSize + this.mAppEntry.externalCacheSize;
            if (this.mLastCacheSize != cacheSize) {
                this.mLastCacheSize = cacheSize;
                this.mCacheSize.setSummary(getSizeStr(cacheSize));
            }
            if (this.mLastTotalSize != this.mAppEntry.size) {
                this.mLastTotalSize = this.mAppEntry.size;
                this.mTotalSize.setSummary(getSizeStr(this.mAppEntry.size));
            }
            if (this.mAppEntry.dataSize + this.mAppEntry.externalDataSize <= 0 || !this.mCanClearData) {
                this.mClearDataButton.setEnabled(false);
            } else {
                ParentControl.enableButton(this.mClearDataButton, true, parentStatus);
                this.mClearDataButton.setOnClickListener(this);
            }
            if (cacheSize <= 0) {
                this.mClearCacheButton.setEnabled(false);
            } else {
                ParentControl.enableButton(this.mClearCacheButton, true, parentStatus);
                this.mClearCacheButton.setOnClickListener(this);
            }
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mClearCacheButton.setEnabled(false);
            this.mClearDataButton.setEnabled(false);
        }
    }

    protected boolean refreshUi() {
        retrieveAppEntry();
        if (this.mAppEntry == null) {
            return false;
        }
        refreshSizeInfo();
        refreshGrantedUriPermissions();
        StorageManager storage = (StorageManager) getContext().getSystemService(StorageManager.class);
        this.mStorageUsed.setSummary(storage.getBestVolumeDescription(getActivity().getPackageManager().getPackageCurrentVolume(this.mAppEntry.info)));
        refreshButtons();
        return true;
    }

    private void refreshButtons() {
        initMoveDialog();
        initDataButtons();
    }

    private void initDataButtons() {
        if (this.mAppEntry.info.manageSpaceActivityName == null && ((this.mAppEntry.info.flags & 65) == 1 || this.mDpm.packageHasActiveAdmins(this.mPackageName))) {
            this.mClearDataButton.setText(2131625621);
            this.mClearDataButton.setEnabled(false);
            this.mCanClearData = false;
        } else {
            if (this.mAppEntry.info.manageSpaceActivityName != null) {
                this.mClearDataButton.setText(2131625639);
            } else {
                this.mClearDataButton.setText(2131625621);
            }
            this.mClearDataButton.setOnClickListener(this);
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mClearDataButton.setEnabled(false);
        }
    }

    private void initMoveDialog() {
        Context context = getActivity();
        StorageManager storage = (StorageManager) context.getSystemService(StorageManager.class);
        List<VolumeInfo> candidates = context.getPackageManager().getPackageCandidateVolumes(this.mAppEntry.info);
        if (candidates.size() > 1) {
            Collections.sort(candidates, VolumeInfo.getDescriptionComparator());
            CharSequence[] labels = new CharSequence[candidates.size()];
            int current = -1;
            for (int i = 0; i < candidates.size(); i++) {
                String volDescrip = storage.getBestVolumeDescription((VolumeInfo) candidates.get(i));
                if (Objects.equals(volDescrip, this.mStorageUsed.getSummary())) {
                    current = i;
                }
                labels[i] = volDescrip;
            }
            this.mCandidates = (VolumeInfo[]) candidates.toArray(new VolumeInfo[candidates.size()]);
            this.mDialogBuilder = new Builder(getContext()).setTitle(2131626903).setSingleChoiceItems(labels, current, this).setNegativeButton(2131624572, null);
            return;
        }
        removePreference("storage_used");
        removePreference("change_storage_button");
        removePreference("storage_space");
    }

    private void initiateClearUserData() {
        this.mClearDataButton.setEnabled(false);
        String packageName = this.mAppEntry.info.packageName;
        Log.i(TAG, "Clearing user data for package : " + packageName);
        if (this.mClearDataObserver == null) {
            this.mClearDataObserver = new ClearUserDataObserver();
        }
        if (((ActivityManager) getActivity().getSystemService("activity")).clearApplicationUserData(packageName, this.mClearDataObserver)) {
            this.mClearDataButton.setText(2131625653);
            return;
        }
        Log.i(TAG, "Couldnt clear application user data for package:" + packageName);
        showDialogInner(2, 0);
    }

    private void processClearMsg(Message msg) {
        int result = msg.arg1;
        String packageName = this.mAppEntry.info.packageName;
        this.mClearDataButton.setText(2131625621);
        if (result == 1) {
            Log.i(TAG, "Cleared user data for package : " + packageName);
            this.mState.requestSize(this.mPackageName, this.mUserId);
            return;
        }
        ParentControl.enableButton(this.mClearDataButton, true, getActivity());
    }

    private void refreshGrantedUriPermissions() {
        removeUriPermissionsFromUi();
        List<UriPermission> perms = ((ActivityManager) getActivity().getSystemService("activity")).getGrantedUriPermissions(this.mAppEntry.info.packageName).getList();
        if (perms.isEmpty()) {
            this.mClearUriButton.setVisibility(8);
            return;
        }
        PackageManager pm = getActivity().getPackageManager();
        Map<CharSequence, MutableInt> uriCounters = new TreeMap();
        for (UriPermission perm : perms) {
            CharSequence app = pm.resolveContentProvider(perm.getUri().getAuthority(), 0).applicationInfo.loadLabel(pm);
            MutableInt count = (MutableInt) uriCounters.get(app);
            if (count == null) {
                uriCounters.put(app, new MutableInt(1));
            } else {
                count.value++;
            }
        }
        for (Entry<CharSequence, MutableInt> entry : uriCounters.entrySet()) {
            int numberResources = ((MutableInt) entry.getValue()).value;
            Preference pref = new Preference(getPrefContext());
            pref.setTitle((CharSequence) entry.getKey());
            pref.setSummary((CharSequence) getPrefContext().getResources().getQuantityString(2131689483, numberResources, new Object[]{Integer.valueOf(numberResources)}));
            pref.setSelectable(false);
            pref.setLayoutResource(2130968829);
            pref.setOrder(0);
            Log.v(TAG, "Adding preference '" + pref + "' at order " + 0);
            this.mUri.addPreference(pref);
        }
        if (this.mAppsControlDisallowedBySystem) {
            this.mClearUriButton.setEnabled(false);
        }
        this.mClearUri.setOrder(0);
        this.mClearUriButton.setVisibility(0);
    }

    private void clearUriPermissions() {
        ((ActivityManager) getActivity().getSystemService("activity")).clearGrantedUriPermissions(this.mAppEntry.info.packageName);
        refreshGrantedUriPermissions();
    }

    private void removeUriPermissionsFromUi() {
        for (int i = this.mUri.getPreferenceCount() - 1; i >= 0; i--) {
            Preference pref = this.mUri.getPreference(i);
            if (pref != this.mClearUri) {
                this.mUri.removePreference(pref);
            }
        }
    }

    protected AlertDialog createDialog(int id, int errorCode) {
        switch (id) {
            case 1:
                return new Builder(getActivity()).setTitle(getActivity().getText(2131625654)).setMessage(getActivity().getText(2131625655)).setPositiveButton(2131625656, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppStorageSettings.this.initiateClearUserData();
                    }
                }).setNegativeButton(2131625657, null).create();
            case 2:
                return new Builder(getActivity()).setTitle(getActivity().getText(2131625661)).setMessage(getActivity().getText(2131625662)).setNeutralButton(2131625656, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        AppStorageSettings.this.mClearDataButton.setEnabled(false);
                        AppStorageSettings.this.setIntentAndFinish(false, false);
                    }
                }).create();
            default:
                return null;
        }
    }

    public void onPackageSizeChanged(String packageName) {
        if (this.mAppEntry != null && packageName.equals(this.mAppEntry.info.packageName)) {
            refreshSizeInfo();
        }
    }

    public static CharSequence getSummary(AppEntry appEntry, Context context) {
        if (appEntry.size == -2 || appEntry.size == -1) {
            return context.getText(2131625672);
        }
        int i;
        if ((appEntry.info.flags & 262144) != 0) {
            i = 2131626898;
        } else {
            i = 2131626897;
        }
        CharSequence storageType = context.getString(i);
        return context.getString(2131626891, new Object[]{getSize(appEntry, context), storageType});
    }

    private static CharSequence getSize(AppEntry appEntry, Context context) {
        long size = appEntry.size;
        if (size == -1) {
            return context.getText(2131625673);
        }
        return Formatter.formatFileSize(context, size);
    }

    protected int getMetricsCategory() {
        return 19;
    }

    private void disableParentControlItems() {
        if (ParentControl.isChildModeOn(getActivity()) && ParentControl.isRestrictedApp(this.mPackageName)) {
            this.mClearCacheButton.setEnabled(false);
            this.mClearDataButton.setEnabled(false);
        }
    }
}
