package com.android.settings.applications;

import android.app.Activity;
import android.app.ActivityManagerNative;
import android.app.AlertDialog.Builder;
import android.app.AppGlobals;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.IPackageManager;
import android.content.pm.UserInfo;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.Toast;
import com.android.internal.content.PackageMonitor;
import com.android.settings.InstrumentedFragment;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class AppCloneSettings extends InstrumentedFragment implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            Resources res = context.getResources();
            List<SearchIndexableRaw> result = new ArrayList();
            if (AppCloneUtils.hasAppCloneCust() && Utils.isOwnerUser() && AppCloneUtils.isSupportAppClone()) {
                String screenTitle = res.getString(2131628554);
                SearchIndexableRaw data = new SearchIndexableRaw(context);
                data.title = screenTitle;
                data.screenTitle = screenTitle;
                data.iconResId = 2130838344;
                data.keywords = screenTitle;
                result.add(data);
            }
            return result;
        }
    };
    private static int mCloneUserId = -1000;
    private OnItemClickListener itemClick = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            final AppInfo appInfo = AppCloneSettings.this.getAppInfoByPosition(position);
            if (appInfo != null) {
                AppCloneSettings.mCloneUserId = AppCloneUtils.getClonedProfileUserId(AppCloneSettings.this.mContext);
                if (SystemProperties.getInt("persist.sys.primarysd", 0) == 1) {
                    new Builder(AppCloneSettings.this.mContext).setTitle(33685903).setMessage(33685904).setPositiveButton(2131624577, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            AppCloneSettings.this.mContext.startActivity(new Intent("android.settings.INTERNAL_STORAGE_SETTINGS"));
                        }
                    }).setNegativeButton(2131625657, new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            appInfo.setChecked(false);
                        }
                    }).show();
                } else if (AppCloneSettings.this.mIsUpdating) {
                    Toast.makeText(AppCloneSettings.this.mContext, 33685905, 1).show();
                } else if (AppCloneSettings.this.isLowStoragState()) {
                    Toast.makeText(AppCloneSettings.this.mContext, 17040234, 0).show();
                } else {
                    boolean isChecked = appInfo.getChecked();
                    if (isChecked && AppCloneSettings.this.cantainsCloneApp(appInfo.getmPackageName())) {
                        new Builder(AppCloneSettings.this.mContext).setMessage(2131628691).setPositiveButton(2131624353, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                try {
                                    if (AppCloneSettings.this.mIpm == null) {
                                        AppCloneSettings.this.mIpm = AppGlobals.getPackageManager();
                                    }
                                    if (AppCloneUtils.isCloneProfileExisted(AppCloneSettings.this.mContext)) {
                                        AppCloneSettings.this.mIpm.deletePackageAsUser(appInfo.getmPackageName(), null, AppCloneSettings.mCloneUserId, 4);
                                        appInfo.setChecked(false);
                                        AppCloneSettings.this.updateCloneAppView();
                                        ItemUseStat.getInstance().handleClick(AppCloneSettings.this.mContext, 3, appInfo.getmPackageName(), "off");
                                    }
                                } catch (RemoteException e) {
                                    Log.e("AppCloneSettings", "deletePackageAsUser failed");
                                }
                            }
                        }).setNegativeButton(2131625657, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).show().getButton(-1).setTextColor(-65536);
                    } else if (!(isChecked || AppCloneSettings.this.cantainsCloneApp(appInfo.getmPackageName()))) {
                        AppCloneSettings.this.newTaskToinstallCloneApp(appInfo.getmPackageName());
                        appInfo.setChecked(true);
                        AppCloneSettings.this.updateCloneAppView();
                        ItemUseStat.getInstance().handleClick(AppCloneSettings.this.mContext, 3, appInfo.getmPackageName(), "on");
                    }
                }
            }
        }
    };
    private List<Category> mAppCategoryList;
    private ListView mAppListView;
    private CategoryAdapter mCategoryAppsAdapter;
    private MyCloneUserStateReceiver mCloneUserStateReceiver;
    private Context mContext;
    private Set<String> mDefaultCloneAppsSet;
    private View mEmptyView;
    private List<String> mExistedPackges;
    private Handler mHandle = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.arg1 == 1) {
                AppCloneSettings.this.updateCloneAppView();
            } else if (msg.arg1 == 2) {
                Toast.makeText(AppCloneSettings.this.mContext, 17040234, 0).show();
            }
        }
    };
    private IPackageManager mIpm;
    private boolean mIsUpdating = false;
    private View mListContainer;
    private View mLoadingView;
    private List<AppInfo> mMainUserOtherAppInfoList;
    private List<AppInfo> mMainUserRecommendAppInfoList;
    private MyPackageMonitor mMyPackageMonitor;
    private ContentObserver mObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            Log.e("AppCloneSettings", "Observer onChange");
            String upDateState = Secure.getString(AppCloneSettings.this.mContext.getContentResolver(), "clone_app_list");
            if (upDateState == null || "".equals(upDateState)) {
                AppCloneSettings.this.mIsUpdating = false;
            } else {
                AppCloneSettings.this.mIsUpdating = true;
            }
            AppCloneSettings.this.mExistedPackges = AppCloneSettings.this.getExistedCloneApps(upDateState);
            AppCloneSettings.this.updateCloneAppView();
        }
    };
    private ProgressDialog mProgressDialog;
    private Map<String, String> mSupportCloneApps;
    private UserManager mUm;

    public class CreateCloneProfileTask extends AsyncTask<String, Void, Integer> {
        protected void onPreExecute() {
            super.onPreExecute();
            if (!AppCloneUtils.isCloneProfileExisted(AppCloneSettings.this.mContext)) {
                AppCloneSettings.this.mProgressDialog.show();
            }
        }

        protected Integer doInBackground(String... params) {
            AppCloneSettings.this.mIpm = AppGlobals.getPackageManager();
            AppCloneSettings.this.mUm = UserManager.get(AppCloneSettings.this.mContext);
            int currentCloneUserId = AppCloneUtils.getClonedProfileUserId(AppCloneSettings.this.mContext);
            if (AppCloneUtils.isCloneProfileExisted(AppCloneSettings.this.mContext)) {
                try {
                    AppCloneSettings.this.mIpm.installExistingPackageAsUser(params[0], currentCloneUserId);
                } catch (RemoteException e) {
                    Log.e("AppCloneSettings", "isCloneProfileExisted---installExistingPackageAsUser ");
                }
                return Integer.valueOf(currentCloneUserId);
            }
            UserInfo userInfo = null;
            try {
                userInfo = AppCloneSettings.this.mUm.createProfileForUser(AppCloneSettings.this.mContext.getResources().getString(2131628938), 67108864, AppCloneSettings.this.mContext.getUserId());
            } catch (Exception e2) {
                if (AppCloneSettings.this.isLowStoragState()) {
                    Message message = new Message();
                    message.arg1 = 2;
                    AppCloneSettings.this.mHandle.sendMessage(message);
                } else {
                    Log.e("AppCloneSettings", "createProfileForUser failed " + e2);
                }
            }
            if (userInfo == null) {
                return Integer.valueOf(-1000);
            }
            try {
                ActivityManagerNative.getDefault().startUserInBackground(userInfo.id);
                AppCloneSettings.this.mIpm.installExistingPackageAsUser(params[0], userInfo.id);
            } catch (RemoteException e3) {
                Log.e("AppCloneSettings", "installExistingPackageAsUser failed");
            }
            return Integer.valueOf(userInfo.id);
        }

        protected void onPostExecute(Integer userId) {
            super.onPostExecute(userId);
            if (AppCloneSettings.this.mContext == null) {
                Log.e("AppCloneSettings", "Context is null ");
            }
            AppCloneSettings.mCloneUserId = userId.intValue();
            AppCloneSettings.this.mProgressDialog.dismiss();
        }
    }

    public class LoadDataTask extends AsyncTask<String, String, String> {
        protected void onPreExecute() {
            super.onPreExecute();
        }

        protected String doInBackground(String... params) {
            AppCloneSettings.this.mMainUserRecommendAppInfoList = AppCloneUtils.getDefaultAppList(AppCloneSettings.this.mContext);
            AppCloneSettings.this.mMainUserOtherAppInfoList = AppCloneUtils.getAppListWithoutdefault(AppCloneSettings.this.mContext);
            AppCloneSettings.this.mDefaultCloneAppsSet = AppCloneUtils.getDefaultAppListFormXml(AppCloneSettings.this.mContext);
            return "";
        }

        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Activity activity = AppCloneSettings.this.getActivity();
            if (activity != null && !activity.isFinishing() && !activity.isDestroyed()) {
                AppCloneSettings.this.updateCloneAppView();
            }
        }
    }

    public final class MyCloneUserStateReceiver extends BroadcastReceiver {
        private Handler mHandler;
        private Message mMessage;

        public MyCloneUserStateReceiver(Handler handler) {
            this.mHandler = handler;
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.USER_REMOVED".equals(intent.getAction())) {
                AppCloneSettings.this.resetSwitchState();
                AppCloneSettings.mCloneUserId = -1000;
                sendMessageToUpdateUI();
            }
        }

        private void sendMessageToUpdateUI() {
            this.mMessage = new Message();
            this.mMessage.arg1 = 1;
            this.mHandler.sendMessage(this.mMessage);
        }
    }

    public final class MyPackageMonitor extends PackageMonitor {
        private Handler mHandler;
        private Message mMessage;

        public MyPackageMonitor(Handler handler) {
            this.mHandler = handler;
        }

        public void onPackageAdded(String packageName, int uid) {
            int userId = UserHandle.getUserId(uid);
            if ((userId != 0 && !AppCloneUtils.isClonedProfile(AppCloneSettings.this.mContext, userId)) || !AppCloneSettings.this.isAppSupportClone(packageName)) {
                return;
            }
            AppInfo appInfo;
            if (AppCloneUtils.isClonedProfile(AppCloneSettings.this.mContext, userId) && !AppCloneSettings.this.cantainsCloneApp(packageName) && !AppCloneSettings.this.cantainsAppInMainUser(packageName)) {
                appInfo = AppCloneSettings.this.createAppInfoByPackageName(packageName);
                appInfo.setChecked(true);
                AppCloneSettings.this.addInstallAppToList(appInfo);
                sendMessageToUpdateUI();
            } else if (userId == 0 && !AppCloneSettings.this.cantainsCloneApp(packageName) && !AppCloneSettings.this.cantainsAppInMainUser(packageName)) {
                AppCloneSettings.this.addInstallAppToList(AppCloneSettings.this.createAppInfoByPackageName(packageName));
                sendMessageToUpdateUI();
            } else if (AppCloneUtils.isClonedProfile(AppCloneSettings.this.mContext, userId) && !AppCloneSettings.this.cantainsCloneApp(packageName) && AppCloneSettings.this.cantainsAppInMainUser(packageName)) {
                appInfo = AppCloneSettings.this.findAppInfoByPackageName(packageName);
                if (appInfo != null) {
                    appInfo.setChecked(true);
                    sendMessageToUpdateUI();
                }
            }
        }

        public void onPackageRemoved(String packageName, int uid) {
            int userId = UserHandle.getUserId(uid);
            if (userId != 0 && !AppCloneUtils.isClonedProfile(AppCloneSettings.this.mContext, userId)) {
                return;
            }
            if (userId == 0 && AppCloneSettings.this.cantainsAppInMainUser(packageName)) {
                AppCloneSettings.this.removeInstallAppToList(packageName);
                sendMessageToUpdateUI();
            } else if (AppCloneUtils.isClonedProfile(AppCloneSettings.this.mContext, userId) && AppCloneSettings.this.cantainsCloneApp(packageName)) {
                AppInfo appInfo = AppCloneSettings.this.findAppInfoByPackageName(packageName);
                if (appInfo != null) {
                    appInfo.setChecked(false);
                    sendMessageToUpdateUI();
                }
            }
        }

        private void sendMessageToUpdateUI() {
            this.mMessage = new Message();
            this.mMessage.arg1 = 1;
            this.mHandler.sendMessage(this.mMessage);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        checkedIsUpdating(Secure.getString(this.mContext.getContentResolver(), "clone_app_list"));
        registeReceiver();
        registerObserver();
    }

    private void registeReceiver() {
        this.mMyPackageMonitor = new MyPackageMonitor(this.mHandle);
        this.mMyPackageMonitor.register(this.mContext, null, UserHandle.ALL, false);
        this.mCloneUserStateReceiver = new MyCloneUserStateReceiver(this.mHandle);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_REMOVED");
        this.mContext.registerReceiver(this.mCloneUserStateReceiver, filter);
    }

    private void loadData() {
        new LoadDataTask().execute(new String[0]);
    }

    private void updateSwitchState() {
        resetSwitchState();
        if (this.mIsUpdating) {
            updateExistedCloneAppsSwitchState();
            return;
        }
        int currentCloneUserId = AppCloneUtils.getClonedProfileUserId(this.mContext);
        if (AppCloneUtils.isClonedProfile(this.mContext, currentCloneUserId) && currentCloneUserId != mCloneUserId) {
            mCloneUserId = currentCloneUserId;
        }
        List<AppInfo> existedCloneApps = AppCloneUtils.getAppListFormUser(this.mContext, mCloneUserId);
        if (this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (existedCloneApps.contains(recommendCloneAppInfo)) {
                    recommendCloneAppInfo.setChecked(true);
                }
            }
        }
        if (this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (existedCloneApps.contains(otherCloneAppInfo)) {
                    otherCloneAppInfo.setChecked(true);
                }
            }
        }
    }

    private void updateCategoryList() {
        if (this.mAppCategoryList == null) {
            this.mAppCategoryList = new ArrayList();
        } else {
            this.mAppCategoryList.clear();
        }
        if (!((this.mMainUserRecommendAppInfoList.size() <= 0 && this.mMainUserOtherAppInfoList.size() <= 0) || this.mEmptyView == null || this.mListContainer == null)) {
            this.mListContainer.setVisibility(0);
            this.mEmptyView.setVisibility(4);
        }
        Category recommendCategory;
        Category otherCategory;
        if (this.mMainUserRecommendAppInfoList.size() > 0 && this.mMainUserOtherAppInfoList.size() > 0) {
            recommendCategory = new Category(getResources().getString(2131628940));
            otherCategory = new Category(getResources().getString(2131628941));
            Collections.sort(this.mMainUserRecommendAppInfoList);
            recommendCategory.setmCategoryItem(this.mMainUserRecommendAppInfoList);
            this.mAppCategoryList.add(recommendCategory);
            Collections.sort(this.mMainUserOtherAppInfoList);
            otherCategory.setmCategoryItem(this.mMainUserOtherAppInfoList);
            this.mAppCategoryList.add(otherCategory);
        } else if (this.mMainUserRecommendAppInfoList.size() == 0 && this.mMainUserOtherAppInfoList.size() > 0) {
            otherCategory = new Category(getResources().getString(2131628941));
            Collections.sort(this.mMainUserOtherAppInfoList);
            otherCategory.setmCategoryItem(this.mMainUserOtherAppInfoList);
            this.mAppCategoryList.add(otherCategory);
        } else if (this.mMainUserRecommendAppInfoList.size() <= 0 || this.mMainUserOtherAppInfoList.size() != 0) {
            if (!(this.mEmptyView == null || this.mListContainer == null)) {
                this.mEmptyView.setVisibility(0);
                this.mListContainer.setVisibility(4);
            }
        } else {
            recommendCategory = new Category(getResources().getString(2131628940));
            Collections.sort(this.mMainUserRecommendAppInfoList);
            recommendCategory.setmCategoryItem(this.mMainUserRecommendAppInfoList);
            this.mAppCategoryList.add(recommendCategory);
        }
    }

    private void updateCloneAppView() {
        updateSwitchState();
        updateCategoryList();
        if (this.mCategoryAppsAdapter != null) {
            this.mCategoryAppsAdapter.notifyDataSetChanged();
        } else {
            setAdapter();
        }
        if (this.mLoadingView != null && this.mLoadingView.getVisibility() == 0) {
            this.mLoadingView.setVisibility(8);
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(2130968676, null);
        this.mListContainer = view.findViewById(2131886383);
        this.mEmptyView = view.findViewById(2131886384);
        this.mEmptyView.setVisibility(4);
        this.mListContainer.setVisibility(8);
        this.mLoadingView = view.findViewById(2131886754);
        this.mLoadingView.setVisibility(0);
        this.mAppListView = (ListView) view.findViewById(16908298);
        this.mAppListView.setOnItemClickListener(this.itemClick);
        loadData();
        return view;
    }

    private void setAdapter() {
        this.mCategoryAppsAdapter = new CategoryAdapter(this.mContext, this.mAppCategoryList);
        this.mAppListView.setAdapter(this.mCategoryAppsAdapter);
    }

    private void newTaskToinstallCloneApp(String packageName) {
        if (this.mProgressDialog == null) {
            this.mProgressDialog = new ProgressDialog(this.mContext);
            this.mProgressDialog.setMessage(this.mContext.getResources().getString(2131628936));
            this.mProgressDialog.setProgressStyle(0);
            this.mProgressDialog.setCancelable(false);
            this.mProgressDialog.setCanceledOnTouchOutside(false);
        }
        new CreateCloneProfileTask().execute(new String[]{packageName});
    }

    private AppInfo getAppInfoByPosition(int position) {
        Iterator category$iterator;
        if (this.mAppCategoryList != null && this.mAppCategoryList.size() == 1) {
            category$iterator = this.mAppCategoryList.iterator();
            if (category$iterator.hasNext()) {
                return (AppInfo) ((Category) category$iterator.next()).getOneTypeItem(position);
            }
        }
        if (this.mAppCategoryList == null || position < 0 || position > getMainUserAppInfoCount()) {
            return null;
        }
        int categroyFirstIndex = 0;
        for (Category category : this.mAppCategoryList) {
            int size = category.getItemCount();
            int categoryIndex = position - categroyFirstIndex;
            if (categoryIndex < size) {
                return (AppInfo) category.getItem(categoryIndex);
            }
            categroyFirstIndex += size;
        }
        return null;
    }

    private int getMainUserAppInfoCount() {
        int count = 0;
        if (this.mAppCategoryList != null) {
            for (Category category : this.mAppCategoryList) {
                count += category.getItemCount();
            }
        }
        return count;
    }

    private void addInstallAppToList(AppInfo appInfo) {
        if (this.mDefaultCloneAppsSet != null) {
            if (this.mDefaultCloneAppsSet.contains(appInfo.getmPackageName())) {
                this.mMainUserRecommendAppInfoList.add(appInfo);
            } else {
                this.mMainUserOtherAppInfoList.add(appInfo);
            }
        }
    }

    private void removeInstallAppToList(String packageName) {
        if (this.mMainUserRecommendAppInfoList != null && this.mMainUserRecommendAppInfoList.size() > 0) {
            List<AppInfo> delMainUserRecommendAppInfoList = new ArrayList();
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (recommendCloneAppInfo.getmPackageName().equals(packageName)) {
                    delMainUserRecommendAppInfoList.add(recommendCloneAppInfo);
                }
            }
            this.mMainUserRecommendAppInfoList.removeAll(delMainUserRecommendAppInfoList);
        }
        if (this.mMainUserOtherAppInfoList != null && this.mMainUserOtherAppInfoList.size() > 0) {
            List<AppInfo> delMainUserOtherAppInfoList = new ArrayList();
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (otherCloneAppInfo.getmPackageName().equals(packageName)) {
                    delMainUserOtherAppInfoList.add(otherCloneAppInfo);
                }
            }
            this.mMainUserOtherAppInfoList.removeAll(delMainUserOtherAppInfoList);
        }
    }

    private boolean cantainsCloneApp(String packageName) {
        if (this.mMainUserRecommendAppInfoList != null && this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (recommendCloneAppInfo.getmPackageName().equals(packageName)) {
                    return recommendCloneAppInfo.getChecked();
                }
            }
        }
        if (this.mMainUserOtherAppInfoList != null && this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (otherCloneAppInfo.getmPackageName().equals(packageName)) {
                    return otherCloneAppInfo.getChecked();
                }
            }
        }
        return false;
    }

    private boolean cantainsAppInMainUser(String packageName) {
        if (this.mMainUserRecommendAppInfoList != null && this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (recommendCloneAppInfo.getmPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        if (this.mMainUserOtherAppInfoList != null && this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (otherCloneAppInfo.getmPackageName().equals(packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean isLowStoragState() {
        boolean isStorageLow = false;
        try {
            this.mIpm = AppGlobals.getPackageManager();
            isStorageLow = this.mIpm.isStorageLow();
        } catch (Exception e) {
            Log.e("AppCloneSettings", "check low storage error because e: " + e);
        }
        return isStorageLow;
    }

    public void onResume() {
        super.onResume();
    }

    protected void checkedIsUpdating(String upDateState) {
        if (upDateState == null || "".equals(upDateState)) {
            this.mIsUpdating = false;
            return;
        }
        this.mIsUpdating = true;
        this.mExistedPackges = getExistedCloneApps(upDateState);
    }

    private List<String> getExistedCloneApps(String upDateState) {
        List<String> existedCloneAppsList = new ArrayList();
        String[] packagesString = upDateState.split(";");
        for (int i = 0; i < packagesString.length; i++) {
            if (!"".equals(packagesString[i])) {
                existedCloneAppsList.add(packagesString[i]);
            }
        }
        return existedCloneAppsList;
    }

    private void updateExistedCloneAppsSwitchState() {
        if (this.mMainUserRecommendAppInfoList != null && this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (this.mExistedPackges.contains(recommendCloneAppInfo.getmPackageName())) {
                    recommendCloneAppInfo.setChecked(true);
                }
            }
        }
        if (this.mMainUserOtherAppInfoList != null && this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (this.mExistedPackges.contains(otherCloneAppInfo.getmPackageName())) {
                    otherCloneAppInfo.setChecked(true);
                }
            }
        }
    }

    private void resetSwitchState() {
        if (this.mMainUserRecommendAppInfoList != null && this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                recommendCloneAppInfo.setChecked(false);
            }
        }
        if (this.mMainUserOtherAppInfoList != null && this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                otherCloneAppInfo.setChecked(false);
            }
        }
    }

    public void onPause() {
        super.onPause();
        ItemUseStat.getInstance().cacheData(this.mContext);
    }

    public void onDestroy() {
        this.mMyPackageMonitor.unregister();
        this.mContext.unregisterReceiver(this.mCloneUserStateReceiver);
        unregisterObserver();
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 130;
    }

    private AppInfo createAppInfoByPackageName(String packageName) {
        return new AppInfo(false, AppCloneUtils.getPackageIcon(this.mContext.getPackageManager(), packageName), packageName, AppCloneUtils.getPackageLabel(this.mContext.getPackageManager(), packageName), this.mContext.getResources().getString(2131628558));
    }

    private AppInfo findAppInfoByPackageName(String packageName) {
        if (this.mMainUserRecommendAppInfoList.size() > 0) {
            for (AppInfo recommendCloneAppInfo : this.mMainUserRecommendAppInfoList) {
                if (recommendCloneAppInfo.getmPackageName().equals(packageName)) {
                    return recommendCloneAppInfo;
                }
            }
        }
        if (this.mMainUserOtherAppInfoList.size() > 0) {
            for (AppInfo otherCloneAppInfo : this.mMainUserOtherAppInfoList) {
                if (otherCloneAppInfo.getmPackageName().equals(packageName)) {
                    return otherCloneAppInfo;
                }
            }
        }
        return null;
    }

    private boolean isAppSupportClone(String packageName) {
        this.mSupportCloneApps = this.mContext.getSharedPreferences("com.android.settings_appclone", 0).getAll();
        if (this.mSupportCloneApps != null) {
            return this.mSupportCloneApps.containsKey(packageName);
        }
        return false;
    }

    private void registerObserver() {
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("clone_app_list"), true, this.mObserver);
    }

    private void unregisterObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mObserver);
    }
}
