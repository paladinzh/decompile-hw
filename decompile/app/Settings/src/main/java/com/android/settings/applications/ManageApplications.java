package com.android.settings.applications;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageItemInfo;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.icu.text.AlphabeticIndex;
import android.icu.text.AlphabeticIndex.ImmutableIndex;
import android.os.Bundle;
import android.os.Environment;
import android.os.LocaleList;
import android.os.UserHandle;
import android.os.UserManager;
import android.preference.PreferenceFrameLayout;
import android.preference.PreferenceFrameLayout.LayoutParams;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filter.FilterResults;
import android.widget.Filterable;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.Spinner;
import android.widget.TextView;
import com.android.settings.AppHeader;
import com.android.settings.InstrumentedFragment;
import com.android.settings.ItemUseStat;
import com.android.settings.Settings.AllApplicationsActivity;
import com.android.settings.Settings.DomainsURLsAppListActivity;
import com.android.settings.Settings.HighPowerApplicationsActivity;
import com.android.settings.Settings.NotificationAppListActivity;
import com.android.settings.Settings.OverlaySettingsActivity;
import com.android.settings.Settings.StorageUseActivity;
import com.android.settings.Settings.UsageAccessSettingsActivity;
import com.android.settings.Settings.WriteSettingsActivity;
import com.android.settings.SettingsActivity;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settings.applications.AppStateAppOpsBridge.PermissionState;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.applications.AppStateUsageBridge.UsageState;
import com.android.settings.applications.ResetAppsHelper.OnResetCompletedListener;
import com.android.settings.fuelgauge.HighPowerDetail;
import com.android.settings.fuelgauge.PowerWhitelistBackend;
import com.android.settings.notification.AppNotificationSettings;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.Callbacks;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;
import com.android.settingslib.applications.ApplicationsState.Session;
import com.android.settingslib.applications.ApplicationsState.VolumeFilter;
import com.huawei.cust.HwCustUtils;
import com.huawei.hsm.permission.StubController;
import com.huawei.permission.IHoldService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ManageApplications extends InstrumentedFragment implements OnItemClickListener, OnItemSelectedListener, OnResetCompletedListener, Indexable {
    static final boolean DEBUG = Log.isLoggable("ManageApplications", 3);
    public static final AppFilter[] FILTERS = new AppFilter[]{new CompoundFilter(AppStatePowerBridge.FILTER_POWER_WHITELISTED, ApplicationsState.FILTER_ALL_ENABLED), new CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED, ApplicationsState.FILTER_ALL_ENABLED), ApplicationsState.FILTER_EVERYTHING, ApplicationsState.FILTER_ALL_ENABLED, ApplicationsState.FILTER_DISABLED, AppStateNotificationBridge.FILTER_APP_NOTIFICATION_BLOCKED, AppStateNotificationBridge.FILTER_APP_NOTIFICATION_SILENCED, AppStateNotificationBridge.FILTER_APP_NOTIFICATION_HIDE_SENSITIVE, AppStateNotificationBridge.FILTER_APP_NOTIFICATION_HIDE_ALL, AppStateNotificationBridge.FILTER_APP_NOTIFICATION_PRIORITY, ApplicationsState.FILTER_PERSONAL, ApplicationsState.FILTER_WORK, ApplicationsState.FILTER_WITH_DOMAIN_URLS, AppStateUsageBridge.FILTER_APP_USAGE, AppStateOverlayBridge.FILTER_SYSTEM_ALERT_WINDOW, AppStateWriteSettingsBridge.FILTER_WRITE_SETTINGS, ApplicationsState.FILTER_CLONE};
    public static final int[] FILTER_LABELS = new int[]{2131626975, 2131626915, 2131626915, 2131626916, 2131625643, 2131626919, 2131626924, 2131626922, 2131626923, 2131626921, 2131626917, 2131626918, 2131626920, 2131626915, 2131627054, 2131627060, 2131628938};
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String appName = context.getString(2131627409);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = appName;
            data.screenTitle = appName;
            result.add(data);
            return result;
        }
    };
    public ApplicationsAdapter mApplications;
    private ApplicationsState mApplicationsState;
    private String mCurrentPkgName;
    private int mCurrentUid;
    public int mFilter;
    private FilterSpinnerAdapter mFilterAdapter;
    private Spinner mFilterSpinner;
    private boolean mFinishAfterDialog;
    private LayoutInflater mInflater;
    CharSequence mInvalidSizeStr;
    private View mListContainer;
    public int mListType;
    private ListView mListView;
    private View mLoadingContainer;
    private NotificationBackend mNotifBackend;
    private Menu mOptionsMenu;
    private ResetAppsHelper mResetAppsHelper;
    private View mRootView;
    private boolean mShowSystem;
    private int mSortOrder = 2131887647;
    private View mSpinnerHeader;
    private String mVolumeName;
    private String mVolumeUuid;

    static class ApplicationsAdapter extends BaseAdapter implements Filterable, Callbacks, Callback, RecyclerListener, SectionIndexer {
        private static final SectionInfo[] EMPTY_SECTIONS = new SectionInfo[0];
        private final ArrayList<View> mActive = new ArrayList();
        private ArrayList<AppEntry> mBaseEntries;
        private final Context mContext;
        CharSequence mCurFilterPrefix;
        private HwCustManageApplications mCustManageApplications;
        private ArrayList<AppEntry> mEntries;
        private final AppStateBaseBridge mExtraInfoBridge;
        private Filter mFilter = new Filter() {
            protected FilterResults performFiltering(CharSequence constraint) {
                ArrayList<AppEntry> entries = ApplicationsAdapter.this.applyPrefixFilter(constraint, ApplicationsAdapter.this.mBaseEntries);
                FilterResults fr = new FilterResults();
                fr.values = entries;
                fr.count = entries.size();
                return fr;
            }

            protected void publishResults(CharSequence constraint, FilterResults results) {
                ApplicationsAdapter.this.mCurFilterPrefix = constraint;
                ApplicationsAdapter.this.mEntries = (ArrayList) results.values;
                ApplicationsAdapter.this.rebuildSections();
                ApplicationsAdapter.this.notifyDataSetChanged();
            }
        };
        private int mFilterMode;
        private boolean mHasReceivedBridgeCallback;
        private boolean mHasReceivedLoadEntries;
        private ImmutableIndex mIndex;
        private int mLastSortMode = -1;
        private final ManageApplications mManageApplications;
        private AppFilter mOverrideFilter;
        private PackageManager mPm;
        private int[] mPositionToSectionIndex;
        private boolean mResumed;
        private SectionInfo[] mSections = EMPTY_SECTIONS;
        private final Session mSession;
        private final ApplicationsState mState;
        private int mWhichSize = 0;

        public ApplicationsAdapter(ApplicationsState state, ManageApplications manageApplications, int filterMode) {
            this.mState = state;
            this.mSession = state.newSession(this);
            this.mManageApplications = manageApplications;
            this.mContext = manageApplications.getActivity();
            this.mPm = this.mContext.getPackageManager();
            this.mFilterMode = filterMode;
            this.mCustManageApplications = (HwCustManageApplications) HwCustUtils.createObj(HwCustManageApplications.class, new Object[0]);
            if (this.mManageApplications.mListType == 1) {
                this.mExtraInfoBridge = new AppStateNotificationBridge(this.mContext, this.mState, this, manageApplications.mNotifBackend);
            } else if (this.mManageApplications.mListType == 4) {
                this.mExtraInfoBridge = new AppStateUsageBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 5) {
                this.mExtraInfoBridge = new AppStatePowerBridge(this.mState, this);
            } else if (this.mManageApplications.mListType == 6) {
                this.mExtraInfoBridge = new AppStateOverlayBridge(this.mContext, this.mState, this);
            } else if (this.mManageApplications.mListType == 7) {
                this.mExtraInfoBridge = new AppStateWriteSettingsBridge(this.mContext, this.mState, this);
            } else {
                this.mExtraInfoBridge = null;
            }
        }

        public void setOverrideFilter(AppFilter overrideFilter) {
            this.mOverrideFilter = overrideFilter;
            rebuild(true);
        }

        public void setFilter(int filter) {
            this.mFilterMode = filter;
            rebuild(true);
        }

        public void resume(int sort) {
            if (ManageApplications.DEBUG) {
                Log.i("ManageApplications", "Resume!  mResumed=" + this.mResumed);
            }
            if (this.mResumed) {
                rebuild(sort);
                return;
            }
            this.mResumed = true;
            this.mSession.resume();
            this.mLastSortMode = sort;
            if (this.mExtraInfoBridge != null) {
                this.mExtraInfoBridge.resume();
            }
            rebuild(false);
        }

        public void pause() {
            if (this.mResumed) {
                this.mResumed = false;
                this.mSession.pause();
                if (this.mExtraInfoBridge != null) {
                    this.mExtraInfoBridge.pause();
                }
            }
        }

        public void reload() {
            if (this.mExtraInfoBridge != null) {
                this.mExtraInfoBridge.onPackageListChanged();
            }
            rebuild(true);
        }

        public void release() {
            this.mSession.release();
            if (this.mExtraInfoBridge != null) {
                this.mExtraInfoBridge.release();
            }
        }

        public void rebuild(int sort) {
            if (sort != this.mLastSortMode) {
                this.mLastSortMode = sort;
                rebuild(true);
            }
        }

        private void requestJlogEnable(boolean enable) {
            if (this.mContext instanceof SettingsActivity) {
                ((SettingsActivity) this.mContext).requestJlogEnable(enable);
            }
        }

        public void rebuild(boolean eraseold) {
            if (this.mHasReceivedLoadEntries && (this.mExtraInfoBridge == null || this.mHasReceivedBridgeCallback)) {
                AppFilter filterObj;
                Comparator<AppEntry> comparatorObj;
                requestJlogEnable(false);
                if (ManageApplications.DEBUG) {
                    Log.i("ManageApplications", "Rebuilding app list...");
                }
                if (Environment.isExternalStorageEmulated()) {
                    this.mWhichSize = 0;
                } else {
                    this.mWhichSize = 1;
                }
                if (this.mFilterMode == 101) {
                    filterObj = BlackListUtils.FILTER_FORBIDDEN;
                } else {
                    filterObj = ManageApplications.FILTERS[this.mFilterMode];
                }
                if (this.mOverrideFilter != null) {
                    filterObj = this.mOverrideFilter;
                }
                if (!this.mManageApplications.mShowSystem) {
                    filterObj = new CompoundFilter(filterObj, ApplicationsState.FILTER_DOWNLOADED_AND_LAUNCHER);
                }
                switch (this.mLastSortMode) {
                    case 2131887648:
                        switch (this.mWhichSize) {
                            case 1:
                                comparatorObj = ApplicationsState.INTERNAL_SIZE_COMPARATOR;
                                break;
                            case 2:
                                comparatorObj = ApplicationsState.EXTERNAL_SIZE_COMPARATOR;
                                break;
                            default:
                                comparatorObj = ApplicationsState.SIZE_COMPARATOR;
                                break;
                        }
                    default:
                        comparatorObj = ApplicationsState.ALPHA_COMPARATOR;
                        break;
                }
                ArrayList<AppEntry> entries = this.mSession.rebuild(filterObj, comparatorObj);
                if (entries != null || eraseold) {
                    if (entries != null && (this.mFilterMode == 0 || this.mFilterMode == 1)) {
                        entries = removeDuplicateIgnoringUser(entries);
                    }
                    onRebuildComplete(BlackListUtils.filterBlackListApp(entries, this.mFilterMode));
                    requestJlogEnable(true);
                    return;
                }
                requestJlogEnable(true);
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private static boolean packageNameEquals(PackageItemInfo info1, PackageItemInfo info2) {
            if (info1 == null || info2 == null || info1.packageName == null || info2.packageName == null) {
                return false;
            }
            return info1.packageName.equals(info2.packageName);
        }

        private ArrayList<AppEntry> removeDuplicateIgnoringUser(ArrayList<AppEntry> entries) {
            int size = entries.size();
            ArrayList<AppEntry> returnEntries = new ArrayList(size);
            PackageItemInfo lastInfo = null;
            for (int i = 0; i < size; i++) {
                AppEntry appEntry = (AppEntry) entries.get(i);
                PackageItemInfo info = appEntry.info;
                if (!packageNameEquals(lastInfo, appEntry.info)) {
                    returnEntries.add(appEntry);
                }
                lastInfo = info;
            }
            returnEntries.trimToSize();
            return returnEntries;
        }

        public void onRebuildComplete(ArrayList<AppEntry> entries) {
            this.mBaseEntries = entries;
            if (this.mBaseEntries != null) {
                this.mEntries = applyPrefixFilter(this.mCurFilterPrefix, this.mBaseEntries);
                if (this.mCustManageApplications != null) {
                    this.mEntries = this.mCustManageApplications.removeThemeApp(this.mEntries, this.mContext);
                    this.mEntries = this.mCustManageApplications.removeConfigApp(this.mEntries, this.mContext);
                }
                rebuildSections();
            } else {
                this.mEntries = null;
                this.mSections = EMPTY_SECTIONS;
                this.mPositionToSectionIndex = null;
            }
            notifyDataSetChanged();
            if (!(this.mSession.getAllApps().size() == 0 || this.mManageApplications.mListContainer.getVisibility() == 0)) {
                Utils.handleLoadingContainer(this.mManageApplications.mLoadingContainer, this.mManageApplications.mListContainer, true, true);
            }
            if (this.mManageApplications.mListType != 4) {
                this.mManageApplications.setHasDisabled(this.mState.haveDisabledApps());
                ApplicationExtUtils.setForbiddenFilterEnabledIfNeeded(this.mManageApplications, this.mManageApplications.mFilterAdapter);
            }
        }

        private void rebuildSections() {
            if (this.mEntries == null || !this.mManageApplications.mListView.isFastScrollEnabled()) {
                this.mSections = EMPTY_SECTIONS;
                this.mPositionToSectionIndex = null;
                return;
            }
            if (this.mIndex == null) {
                LocaleList locales = this.mContext.getResources().getConfiguration().getLocales();
                if (locales.size() == 0) {
                    locales = new LocaleList(new Locale[]{Locale.ENGLISH});
                }
                AlphabeticIndex index = new AlphabeticIndex(locales.get(0));
                int localeCount = locales.size();
                for (int i = 1; i < localeCount; i++) {
                    index.addLabels(new Locale[]{locales.get(i)});
                }
                index.addLabels(new Locale[]{Locale.ENGLISH});
                this.mIndex = index.buildImmutableIndex();
            }
            ArrayList<SectionInfo> sections = new ArrayList();
            int lastSecId = -1;
            int totalEntries = this.mEntries.size();
            this.mPositionToSectionIndex = new int[totalEntries];
            for (int pos = 0; pos < totalEntries; pos++) {
                String label = ((AppEntry) this.mEntries.get(pos)).label;
                ImmutableIndex immutableIndex = this.mIndex;
                if (TextUtils.isEmpty(label)) {
                    label = "";
                }
                int secId = immutableIndex.getBucketIndex(label);
                if (secId != lastSecId) {
                    lastSecId = secId;
                    sections.add(new SectionInfo(this.mIndex.getBucket(secId).getLabel(), pos));
                }
                this.mPositionToSectionIndex[pos] = sections.size() - 1;
            }
            this.mSections = (SectionInfo[]) sections.toArray(EMPTY_SECTIONS);
        }

        private void updateLoading() {
            boolean z;
            View -get6 = this.mManageApplications.mLoadingContainer;
            View -get4 = this.mManageApplications.mListContainer;
            if (!this.mHasReceivedLoadEntries || this.mSession.getAllApps().size() == 0) {
                z = false;
            } else {
                z = true;
            }
            Utils.handleLoadingContainer(-get6, -get4, z, false);
        }

        ArrayList<AppEntry> applyPrefixFilter(CharSequence prefix, ArrayList<AppEntry> origEntries) {
            Log.d("ManageApplications", "mListType" + this.mManageApplications.mListType);
            if (this.mManageApplications.mListType == 5) {
                ArrayList<AppEntry> allEntries = getAllAppList(origEntries);
                origEntries.clear();
                origEntries = allEntries;
            }
            if (prefix == null || prefix.length() == 0) {
                return origEntries;
            }
            String prefixStr = ApplicationsState.normalize(prefix.toString());
            String spacePrefixStr = " " + prefixStr;
            ArrayList<AppEntry> newEntries = new ArrayList();
            for (int i = 0; i < origEntries.size(); i++) {
                AppEntry entry = (AppEntry) origEntries.get(i);
                String nlabel = entry.getNormalizedLabel();
                if (nlabel.startsWith(prefixStr) || nlabel.indexOf(spacePrefixStr) != -1) {
                    newEntries.add(entry);
                }
            }
            return newEntries;
        }

        private ArrayList<AppEntry> getAllAppList(ArrayList<AppEntry> origEntries) {
            ArrayList<AppEntry> allAppEntries = new ArrayList();
            ArrayList<String> allAppsFromPhoneManager = new ArrayList();
            try {
                IHoldService service = StubController.getHoldService();
                if (service == null) {
                    Log.e("ManageApplications", "hsm_get_freeze_list service is null!");
                    return origEntries;
                }
                Bundle bundle = new Bundle();
                bundle.putString("freeze_list_type", "protect");
                Bundle bundleProtect = service.callHsmService("hsm_get_freeze_list", bundle);
                bundle.putString("freeze_list_type", "unprotect");
                Bundle bundleUnprotect = service.callHsmService("hsm_get_freeze_list", bundle);
                if (bundleProtect != null) {
                    ArrayList<String> protectApps = bundleProtect.getStringArrayList("frz_protect");
                    if (protectApps != null) {
                        allAppsFromPhoneManager.addAll(protectApps);
                    }
                }
                if (bundleUnprotect != null) {
                    ArrayList<String> unprotectApps = bundleUnprotect.getStringArrayList("frz_unprotect");
                    if (unprotectApps != null) {
                        allAppsFromPhoneManager.addAll(unprotectApps);
                    }
                }
                for (AppEntry appEntry : origEntries) {
                    if (allAppsFromPhoneManager.contains(appEntry.info.packageName)) {
                        allAppEntries.add(appEntry);
                    }
                }
                return allAppEntries;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void onExtraInfoUpdated() {
            this.mHasReceivedBridgeCallback = true;
            rebuild(false);
        }

        public void onRunningStateChanged(boolean running) {
            this.mManageApplications.getActivity().setProgressBarIndeterminateVisibility(running);
        }

        public void onPackageListChanged() {
            rebuild(false);
        }

        public void onPackageIconChanged() {
        }

        public void onLoadEntriesCompleted() {
            this.mHasReceivedLoadEntries = true;
            rebuild(false);
        }

        public void onPackageSizeChanged(String packageName) {
            for (int i = 0; i < this.mActive.size(); i++) {
                AppViewHolder holder = (AppViewHolder) ((View) this.mActive.get(i)).getTag();
                if (holder.entry.info.packageName.equals(packageName)) {
                    synchronized (holder.entry) {
                        updateSummary(holder);
                    }
                    if (holder.entry.info.packageName.equals(this.mManageApplications.mCurrentPkgName) && this.mLastSortMode == 2131887648) {
                        rebuild(false);
                    }
                    return;
                }
            }
        }

        public void onLauncherInfoChanged() {
            if (!this.mManageApplications.mShowSystem) {
                rebuild(false);
            }
        }

        public void onAllSizesComputed() {
            if (this.mLastSortMode == 2131887648) {
                rebuild(false);
            }
        }

        public int getCount() {
            return this.mEntries != null ? this.mEntries.size() : 0;
        }

        public Object getItem(int position) {
            return this.mEntries.get(position);
        }

        public AppEntry getAppEntry(int position) {
            return (AppEntry) this.mEntries.get(position);
        }

        public long getItemId(int position) {
            return ((AppEntry) this.mEntries.get(position)).id;
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public boolean isEnabled(int position) {
            return true;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder = AppViewHolder.createOrRecycle(this.mManageApplications.mInflater, convertView);
            convertView = holder.rootView;
            AppEntry entry = (AppEntry) this.mEntries.get(position);
            synchronized (entry) {
                holder.entry = entry;
                if (entry.label != null) {
                    holder.appName.setText(entry.label);
                }
                this.mState.ensureIcon(entry);
                if (entry.icon != null) {
                    holder.appIcon.setImageDrawable(entry.icon);
                }
                updateSummary(holder);
                if ((entry.info.flags & 8388608) == 0) {
                    holder.disabled.setVisibility(0);
                    holder.disabled.setText(2131625648);
                } else if (entry.info.enabled) {
                    holder.disabled.setVisibility(8);
                } else {
                    holder.disabled.setVisibility(0);
                    holder.disabled.setText(2131625647);
                }
                ApplicationExtUtils.changeDisableTextViewIfNeeded(entry.info.packageName, holder.disabled);
            }
            this.mActive.remove(convertView);
            this.mActive.add(convertView);
            convertView.setEnabled(isEnabled(position));
            return convertView;
        }

        private void updateSummary(AppViewHolder holder) {
            switch (this.mManageApplications.mListType) {
                case 1:
                    if (holder.entry.extraInfo != null) {
                        holder.summary.setText(InstalledAppDetails.getNotificationSummary((AppRow) holder.entry.extraInfo, this.mContext));
                        return;
                    } else {
                        holder.summary.setText(null);
                        return;
                    }
                case 2:
                    holder.summary.setSingleLine(false);
                    holder.summary.setText(getDomainsSummary(holder.entry.info.packageName));
                    return;
                case 4:
                    if (holder.entry.extraInfo != null) {
                        int i;
                        TextView textView = holder.summary;
                        if (new UsageState((PermissionState) holder.entry.extraInfo).isPermissible()) {
                            i = 2131626851;
                        } else {
                            i = 2131626852;
                        }
                        textView.setText(i);
                        return;
                    }
                    holder.summary.setText(null);
                    return;
                case 5:
                    holder.summary.setText(HighPowerDetail.getSummary(this.mContext, holder.entry));
                    return;
                case 6:
                    holder.summary.setText(DrawOverlayDetails.getSummary(this.mContext, holder.entry));
                    return;
                case 7:
                    holder.summary.setText(WriteSettingsDetails.getSummary(this.mContext, holder.entry));
                    return;
                default:
                    holder.updateSizeText(this.mManageApplications.mInvalidSizeStr, this.mWhichSize);
                    return;
            }
        }

        public Filter getFilter() {
            return this.mFilter;
        }

        public void onMovedToScrapHeap(View view) {
            this.mActive.remove(view);
        }

        private CharSequence getDomainsSummary(String packageName) {
            if (this.mPm.getIntentVerificationStatusAsUser(packageName, UserHandle.myUserId()) == 3) {
                return this.mContext.getString(2131626934);
            }
            ArraySet<String> result = Utils.getHandledDomains(this.mPm, packageName);
            if (result.size() == 0) {
                return this.mContext.getString(2131626934);
            }
            if (result.size() == 1) {
                return this.mContext.getString(2131626935, new Object[]{result.valueAt(0)});
            }
            return this.mContext.getString(2131626936, new Object[]{result.valueAt(0)});
        }

        public Object[] getSections() {
            return this.mSections;
        }

        public int getPositionForSection(int sectionIndex) {
            return this.mSections[sectionIndex].position;
        }

        public int getSectionForPosition(int position) {
            return this.mPositionToSectionIndex[position];
        }
    }

    static class FilterSpinnerAdapter extends ArrayAdapter<CharSequence> {
        private final ArrayList<Integer> mFilterOptions = new ArrayList();
        private final ManageApplications mManageApplications;

        public FilterSpinnerAdapter(ManageApplications manageApplications) {
            super(manageApplications.getActivity(), 2130968779);
            setDropDownViewResource(17367049);
            this.mManageApplications = manageApplications;
        }

        public int getFilter(int position) {
            return ((Integer) this.mFilterOptions.get(position)).intValue();
        }

        public void setFilterEnabled(int filter, boolean enabled) {
            if (enabled) {
                enableFilter(filter);
            } else {
                disableFilter(filter);
            }
        }

        public void enableFilter(int filter) {
            if (!this.mFilterOptions.contains(Integer.valueOf(filter))) {
                if (ManageApplications.DEBUG) {
                    Log.d("ManageApplications", "Enabling filter " + filter);
                }
                this.mFilterOptions.add(Integer.valueOf(filter));
                Collections.sort(this.mFilterOptions);
                this.mManageApplications.mSpinnerHeader.setVisibility(this.mFilterOptions.size() > 1 ? 0 : 8);
                ApplicationExtUtils.selectForbiddenFilterIfNeeded(this.mManageApplications, this.mManageApplications.mFilterSpinner, this.mFilterOptions);
                notifyDataSetChanged();
                if (this.mFilterOptions.size() == 1) {
                    if (ManageApplications.DEBUG) {
                        Log.d("ManageApplications", "Auto selecting filter " + filter);
                    }
                    this.mManageApplications.mFilterSpinner.setSelection(0);
                    this.mManageApplications.onItemSelected(null, null, 0, 0);
                }
            }
        }

        public void disableFilter(int filter) {
            if (this.mFilterOptions.remove(Integer.valueOf(filter))) {
                if (ManageApplications.DEBUG) {
                    Log.d("ManageApplications", "Disabling filter " + filter);
                }
                Collections.sort(this.mFilterOptions);
                this.mManageApplications.mSpinnerHeader.setVisibility(this.mFilterOptions.size() > 1 ? 0 : 8);
                notifyDataSetChanged();
                if (this.mManageApplications.mFilter == filter && this.mFilterOptions.size() > 0) {
                    if (ManageApplications.DEBUG) {
                        Log.d("ManageApplications", "Auto selecting filter " + this.mFilterOptions.get(0));
                    }
                    this.mManageApplications.mFilterSpinner.setSelection(0);
                    this.mManageApplications.onItemSelected(null, null, 0, 0);
                }
            }
        }

        public int getCount() {
            return this.mFilterOptions.size();
        }

        public CharSequence getItem(int position) {
            if (this.mManageApplications.isAdded()) {
                return getFilterString(((Integer) this.mFilterOptions.get(position)).intValue());
            }
            return null;
        }

        private CharSequence getFilterString(int filter) {
            if (filter == 101) {
                return this.mManageApplications.getString(2131628368);
            }
            return this.mManageApplications.getString(ManageApplications.FILTER_LABELS[filter]);
        }
    }

    private static class SectionInfo {
        final String label;
        final int position;

        public SectionInfo(String label, int position) {
            this.label = label;
            this.position = position;
        }

        public String toString() {
            return this.label;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        String className = null;
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        this.mApplicationsState = ApplicationsState.getInstance(getActivity().getApplication());
        Intent intent = getActivity().getIntent();
        Bundle args = getArguments();
        if (args != null) {
            className = args.getString("classname");
        }
        if (className == null) {
            className = intent.getComponent().getClassName();
        }
        Log.d("ManageApplications", "ClassName from intent is: " + className);
        if (className.equals(AllApplicationsActivity.class.getName())) {
            this.mShowSystem = true;
        } else if (className.equals(NotificationAppListActivity.class.getName())) {
            this.mListType = 1;
            this.mNotifBackend = new NotificationBackend();
        } else if (className.equals(DomainsURLsAppListActivity.class.getName())) {
            this.mListType = 2;
        } else if (className.equals(StorageUseActivity.class.getName())) {
            if (args == null || !args.containsKey("volumeUuid")) {
                this.mListType = 0;
            } else {
                this.mVolumeUuid = args.getString("volumeUuid");
                this.mVolumeName = args.getString("volumeName");
                this.mListType = 3;
            }
            this.mSortOrder = 2131887648;
        } else if (className.equals(UsageAccessSettingsActivity.class.getName())) {
            this.mListType = 4;
        } else if (className.equals(HighPowerApplicationsActivity.class.getName())) {
            this.mListType = 5;
            this.mShowSystem = true;
            PowerWhitelistBackend.getInstance().refreshList();
        } else if (className.equals(OverlaySettingsActivity.class.getName())) {
            this.mListType = 6;
        } else if (className.equals(WriteSettingsActivity.class.getName())) {
            this.mListType = 7;
        } else {
            this.mListType = 0;
        }
        this.mFilter = getDefaultFilter();
        if (savedInstanceState != null) {
            this.mSortOrder = savedInstanceState.getInt("sortOrder", this.mSortOrder);
            this.mShowSystem = savedInstanceState.getBoolean("showSystem", this.mShowSystem);
            this.mCurrentPkgName = savedInstanceState.getString("currentPkgName", this.mCurrentPkgName);
        }
        this.mInvalidSizeStr = getActivity().getText(2131625673);
        this.mResetAppsHelper = new ResetAppsHelper(getActivity());
        this.mResetAppsHelper.setOnResetCompletedListener(this);
        ApplicationExtUtils.setShowingBlackListAppFlagIfNeeded(getActivity(), intent);
        if (ApplicationExtUtils.getShowingBlackListAppFlag()) {
            this.mFilter = 101;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mInflater = inflater;
        this.mRootView = inflater.inflate(2130968860, null);
        this.mLoadingContainer = this.mRootView.findViewById(2131886754);
        this.mLoadingContainer.setVisibility(0);
        this.mListContainer = this.mRootView.findViewById(2131886773);
        if (this.mListContainer != null) {
            View emptyView = this.mListContainer.findViewById(16908292);
            ListView lv = (ListView) this.mListContainer.findViewById(16908298);
            if (emptyView != null) {
                lv.setEmptyView(emptyView);
            }
            lv.setOnItemClickListener(this);
            lv.setSaveEnabled(true);
            lv.setItemsCanFocus(true);
            lv.setTextFilterEnabled(true);
            this.mListView = lv;
            this.mListView.setDivider(getResources().getDrawable(2130838530));
            this.mApplications = new ApplicationsAdapter(this.mApplicationsState, this, this.mFilter);
            if (savedInstanceState != null) {
                this.mApplications.mHasReceivedLoadEntries = savedInstanceState.getBoolean("hasEntries", false);
                this.mApplications.mHasReceivedBridgeCallback = savedInstanceState.getBoolean("hasBridge", false);
            }
            this.mListView.setAdapter(this.mApplications);
            View footerView = inflater.inflate(2130968937, this.mListView, false);
            if (footerView != null) {
                this.mListView.addFooterView(footerView, null, false);
                this.mListView.setFooterDividersEnabled(true);
            }
            this.mListView.setRecyclerListener(this.mApplications);
            this.mListView.setFastScrollEnabled(isFastScrollEnabled());
            Utils.prepareCustomPreferencesList(container, this.mRootView, this.mListView, false);
        }
        if (container instanceof PreferenceFrameLayout) {
            ((LayoutParams) this.mRootView.getLayoutParams()).removeBorders = true;
        }
        createHeader();
        this.mResetAppsHelper.onRestoreInstanceState(savedInstanceState);
        return this.mRootView;
    }

    private void createHeader() {
        Activity activity = getActivity();
        FrameLayout pinnedHeader = (FrameLayout) this.mRootView.findViewById(2131886470);
        this.mSpinnerHeader = (ViewGroup) activity.getLayoutInflater().inflate(2130968637, pinnedHeader, false);
        this.mFilterSpinner = (Spinner) this.mSpinnerHeader.findViewById(2131886266);
        this.mFilterAdapter = new FilterSpinnerAdapter(this);
        this.mFilterSpinner.setAdapter(this.mFilterAdapter);
        this.mFilterSpinner.setOnItemSelectedListener(this);
        pinnedHeader.addView(this.mSpinnerHeader, 0);
        this.mFilterAdapter.enableFilter(getDefaultFilter());
        if (this.mListType == 0) {
            this.mFilterAdapter.disableFilter(10);
            this.mFilterAdapter.disableFilter(16);
            this.mFilterAdapter.disableFilter(11);
            if (UserManager.get(getActivity()).getUserProfiles().size() > 1) {
                this.mFilterAdapter.enableFilter(10);
                for (UserInfo userInfo : UserManager.get(activity).getUsers(true)) {
                    if (userInfo.isClonedProfile()) {
                        this.mFilterAdapter.enableFilter(16);
                    } else if (userInfo.isManagedProfile()) {
                        this.mFilterAdapter.enableFilter(11);
                    }
                }
            }
        }
        if (this.mListType == 1) {
            this.mFilterAdapter.enableFilter(5);
            this.mFilterAdapter.enableFilter(6);
            this.mFilterAdapter.enableFilter(7);
            this.mFilterAdapter.enableFilter(8);
            this.mFilterAdapter.enableFilter(9);
        }
        if (this.mListType == 5) {
            this.mFilterAdapter.enableFilter(1);
        }
        if (this.mListType == 3) {
            this.mApplications.setOverrideFilter(new VolumeFilter(this.mVolumeUuid));
        }
        ApplicationExtUtils.registerBlackListSharePreferenceListener(this, this.mFilterAdapter);
        ApplicationExtUtils.setForbiddenFilterEnabledIfNeeded(this, this.mFilterAdapter);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if (this.mListType == 3) {
            AppHeader.createAppHeader(getActivity(), null, this.mVolumeName, null, -1, (FrameLayout) this.mRootView.findViewById(2131886470));
        }
    }

    private int getDefaultFilter() {
        switch (this.mListType) {
            case 2:
                return 12;
            case 4:
                return 13;
            case 5:
                return 0;
            case 6:
                return 14;
            case 7:
                return 15;
            default:
                return 2;
        }
    }

    private boolean isFastScrollEnabled() {
        boolean z = false;
        switch (this.mListType) {
            case 0:
            case 1:
            case 3:
                if (this.mSortOrder == 2131887647) {
                    z = true;
                }
                return z;
            default:
                return false;
        }
    }

    protected int getMetricsCategory() {
        switch (this.mListType) {
            case 0:
                return 65;
            case 1:
                return 133;
            case 2:
                return 143;
            case 3:
                return 182;
            case 4:
                return 95;
            case 5:
                return 184;
            case 6:
                return 221;
            case 7:
                return 221;
            default:
                return 0;
        }
    }

    public void onResume() {
        super.onResume();
        updateView();
        updateOptionsMenu();
        if (this.mApplications != null) {
            this.mApplications.resume(this.mSortOrder);
            this.mApplications.updateLoading();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        this.mResetAppsHelper.onSaveInstanceState(outState);
        outState.putInt("sortOrder", this.mSortOrder);
        outState.putBoolean("showSystem", this.mShowSystem);
        outState.putBoolean("hasEntries", this.mApplications.mHasReceivedLoadEntries);
        outState.putBoolean("hasBridge", this.mApplications.mHasReceivedBridgeCallback);
        outState.putString("currentPkgName", this.mCurrentPkgName);
    }

    public void onPause() {
        super.onPause();
        if (this.mApplications != null) {
            this.mApplications.pause();
        }
    }

    public void onStop() {
        super.onStop();
        this.mResetAppsHelper.stop();
        ApplicationExtUtils.setShowingBlackListAppFlag(false);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mApplications != null) {
            this.mApplications.release();
        }
        this.mRootView = null;
        ApplicationExtUtils.dismissWarningDialog();
        ApplicationExtUtils.unregisterBlackListSharePreferenceListener();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && this.mCurrentPkgName != null) {
            if (this.mListType == 1) {
                this.mApplications.mExtraInfoBridge.forceUpdate(this.mCurrentPkgName, this.mCurrentUid);
            } else if (this.mListType != 5 && this.mListType != 6 && this.mListType != 7) {
                this.mApplicationsState.requestSize(this.mCurrentPkgName, UserHandle.getUserId(this.mCurrentUid));
            } else if (this.mFinishAfterDialog) {
                getActivity().onBackPressed();
            } else {
                this.mApplications.mExtraInfoBridge.forceUpdate(this.mCurrentPkgName, this.mCurrentUid);
            }
        }
    }

    private void startApplicationDetailsActivity() {
        switch (this.mListType) {
            case 1:
                startAppInfoFragment(AppNotificationSettings.class, 2131626738);
                return;
            case 2:
                startAppInfoFragment(AppLaunchSettings.class, 2131625601);
                return;
            case 3:
                startAppInfoFragment(AppStorageSettings.class, 2131625227);
                return;
            case 4:
                startAppInfoFragment(UsageAccessDetails.class, 2131626959);
                return;
            case 5:
                HighPowerDetail.show(this, this.mCurrentPkgName, 1, this.mFinishAfterDialog);
                return;
            case 6:
                startAppInfoFragment(DrawOverlayDetails.class, 2131627052);
                return;
            case 7:
                startAppInfoFragment(WriteSettingsDetails.class, 2131627062);
                return;
            default:
                startAppInfoFragment(InstalledAppDetails.class, 2131625599);
                return;
        }
    }

    private void startAppInfoFragment(Class<?> fragment, int titleRes) {
        AppInfoBase.startAppInfoFragment((Class) fragment, titleRes, this.mCurrentPkgName, this.mCurrentUid, (Fragment) this, 1);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        if (this.mListType != 2 && this.mListType != 5) {
            HelpUtils.prepareHelpMenuItem(getActivity(), menu, this.mListType == 0 ? 2131626530 : 2131626529, getClass().getName());
            this.mOptionsMenu = menu;
            inflater.inflate(2132017155, menu);
            updateOptionsMenu();
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    public void onDestroyOptionsMenu() {
        this.mOptionsMenu = null;
    }

    void updateOptionsMenu() {
        boolean z = false;
        if (this.mOptionsMenu != null) {
            boolean z2;
            MenuItem findItem = this.mOptionsMenu.findItem(2131887644);
            if (this.mListType == 0 || this.mListType == 1) {
                z2 = true;
            } else {
                z2 = false;
            }
            findItem.setVisible(z2);
            this.mOptionsMenu.findItem(2131887644).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_SETTING)));
            findItem = this.mOptionsMenu.findItem(2131887647);
            z2 = this.mListType == 3 ? this.mSortOrder != 2131887647 : false;
            findItem.setVisible(z2);
            findItem = this.mOptionsMenu.findItem(2131887648);
            z2 = this.mListType == 3 ? this.mSortOrder != 2131887648 : false;
            findItem.setVisible(z2);
            findItem = this.mOptionsMenu.findItem(2131887645);
            z2 = !this.mShowSystem ? this.mListType != 5 : false;
            findItem.setVisible(z2);
            MenuItem findItem2 = this.mOptionsMenu.findItem(2131887646);
            if (this.mShowSystem && this.mListType != 5) {
                z = true;
            }
            findItem2.setVisible(z);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean onOptionsItemSelected(MenuItem item) {
        int menuId = item.getItemId();
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case 2131887644:
                ((SettingsActivity) getActivity()).startPreferencePanel(AdvancedAppSettings.class.getName(), null, 2131626926, null, this, 2);
                return true;
            case 2131887645:
            case 2131887646:
                boolean z;
                if (this.mShowSystem) {
                    z = false;
                } else {
                    z = true;
                }
                this.mShowSystem = z;
                ItemUseStat.getInstance().handleClick(getActivity(), 2, this.mShowSystem ? "manage_apps_show_system" : "manage_apps_show_normal");
                this.mApplications.rebuild(false);
                break;
            case 2131887647:
            case 2131887648:
                this.mSortOrder = menuId;
                this.mListView.setFastScrollEnabled(isFastScrollEnabled());
                if (this.mApplications != null) {
                    this.mApplications.rebuild(this.mSortOrder);
                    break;
                }
                break;
            case 2131887649:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "manage_apps_reset_apps");
                this.mResetAppsHelper.buildResetDialog();
                return true;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        if (this.mApplications != null && this.mApplications.getCount() > position) {
            AppEntry entry = this.mApplications.getAppEntry(position);
            this.mCurrentPkgName = entry.info.packageName;
            this.mCurrentUid = entry.info.uid;
            if (BlackListUtils.isBlackListApp(entry.info.packageName)) {
                ApplicationExtUtils.showWarningDialog(getActivity(), entry.info);
            } else {
                startApplicationDetailsActivity();
            }
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "app_item_clicked", this.mCurrentPkgName);
        }
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mFilter = this.mFilterAdapter.getFilter(position);
        this.mApplications.setFilter(this.mFilter);
        if (DEBUG) {
            Log.d("ManageApplications", "Selecting filter " + this.mFilter);
        }
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }

    public void updateView() {
        updateOptionsMenu();
        Activity host = getActivity();
        if (host != null) {
            host.invalidateOptionsMenu();
        }
    }

    public void setHasDisabled(boolean hasDisabledApps) {
        if (this.mListType == 0) {
            this.mFilterAdapter.setFilterEnabled(3, hasDisabledApps);
            this.mFilterAdapter.setFilterEnabled(4, hasDisabledApps);
        }
    }

    public void onResetCompleted() {
        if (isAdded() && this.mApplications != null) {
            this.mApplications.reload();
        }
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        String className = null;
        super.onActivityCreated(savedInstanceState);
        Intent intent = getActivity().getIntent();
        Bundle args = getArguments();
        if (args != null) {
            className = args.getString("classname");
        }
        if (className == null) {
            className = intent.getComponent().getClassName();
        }
        if (UsageAccessSettingsActivity.class.getName().equals(className)) {
            getActivity().getActionBar().setTitle(2131626143);
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        } else if (OverlaySettingsActivity.class.getName().equals(className)) {
            getActivity().getActionBar().setTitle(2131627046);
        } else if (WriteSettingsActivity.class.getName().equals(className)) {
            getActivity().getActionBar().setTitle(2131627061);
        } else if (DomainsURLsAppListActivity.class.getName().equals(className)) {
            getActivity().getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    private void finish() {
        if (getActivity() != null) {
            if (getFragmentManager().getBackStackEntryCount() > 0) {
                getFragmentManager().popBackStack();
            } else {
                getActivity().finish();
            }
        }
    }
}
