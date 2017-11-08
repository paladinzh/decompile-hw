package com.huawei.permissionmanager.ui;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.permissionmanager.utils.IRecommendChangeListener;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.IPackageChangeListener.DefListener;
import java.util.ArrayList;
import java.util.List;

public class ApplicationFragment extends PermissionBaseFragment {
    private static final String LOG_TAG = "ApplicationFragment";
    private BaseAdapter mApplicationAdapter = null;
    private ListView mAppsListview = null;
    private DefListener mExternalStorageListener = new DefListener() {
        public void onExternalChanged(String[] packages, boolean available) {
            if (packages != null && packages.length != 0) {
                new AsynctaskUpdateDB().execute(new Void[0]);
            }
        }

        public void onPackagedAdded(String pkgName) {
            HwLog.i(ApplicationFragment.LOG_TAG, "onPackagedAdded : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }

        public void onPackageRemoved(String pkgName) {
            HwLog.i(ApplicationFragment.LOG_TAG, "onPackageRemoved : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }

        public void onPackageChanged(String pkgName) {
            HwLog.i(ApplicationFragment.LOG_TAG, "onPackageChanged : " + pkgName);
            new AsynctaskUpdateDB().execute(new Void[0]);
        }
    };
    private View mFragmentView = null;
    private int mHeightOffSet = 0;
    private LayoutInflater mInflater = null;
    private OnClickListener mItemClicker = new OnClickListener() {
        public void onClick(View v) {
            AppInfoWrapper currentAppInfo = (AppInfoWrapper) v.getTag(R.id.image);
            if (currentAppInfo != null && currentAppInfo.mAppInfo != null) {
                Intent intent;
                if (currentAppInfo.mPermissionCount == 0) {
                    intent = new Intent();
                    intent.putExtra(ShareCfg.SINGLE_APP_UID, currentAppInfo.mAppInfo.mAppUid);
                    intent.putExtra(ShareCfg.SINGLE_APP_LABEL, currentAppInfo.mAppInfo.mAppLabel);
                    intent.putExtra(ShareCfg.SINGLE_APP_PKGNAME, currentAppInfo.mAppInfo.mPkgName);
                    intent.setClass(ApplicationFragment.this.mContext, SingleAppActivity.class);
                    ApplicationFragment.this.startActivity(intent);
                } else {
                    intent = new Intent();
                    intent.setAction("android.intent.action.MANAGE_APP_PERMISSIONS");
                    intent.putExtra("android.intent.extra.PACKAGE_NAME", currentAppInfo.mAppInfo.mPkgName);
                    intent.putExtra(ShareCfg.EXTRA_HIDE_INFO_BUTTON, true);
                    intent.setPackage("com.android.packageinstaller");
                    try {
                        ApplicationFragment.this.startActivity(intent);
                    } catch (Exception e) {
                        HwLog.w(ApplicationFragment.LOG_TAG, "start permission manager fail", e);
                    }
                }
            }
        }
    };
    private int mListViewPos = 0;
    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == 0) {
                ApplicationFragment.this.mListViewPos = ApplicationFragment.this.mAppsListview.getFirstVisiblePosition();
                View topView = ApplicationFragment.this.mAppsListview.getChildAt(0);
                if (topView != null) {
                    ApplicationFragment.this.mHeightOffSet = topView.getTop();
                }
            }
        }

        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        }
    };
    private IRecommendChangeListener mRecommendChangeListener = new IRecommendChangeListener() {
        public void onApplicationFragmentRecommendAppsChange() {
        }

        public void onPermissionFragmentRecommendAppsChange() {
            ApplicationFragment.this.removeRecommendHeader();
            new AsynctaskUpdateDB().execute(new Void[0]);
        }

        public String toString() {
            return ApplicationFragment.LOG_TAG;
        }
    };

    private class AsynctaskUpdateDB extends AsyncTask<Void, Void, List<AppInfoWrapper>> {
        private AsynctaskUpdateDB() {
        }

        protected List<AppInfoWrapper> doInBackground(Void... params) {
            return AppInfoWrapper.updatePureAppInfoWrapperList(ApplicationFragment.this.mContext, "application fragment");
        }

        protected void onPostExecute(List<AppInfoWrapper> result) {
            super.onPostExecute(result);
            ApplicationFragment.this.hideProgressBar();
            ApplicationFragment.this.mPermissonAppsList.clear();
            ApplicationFragment.this.mPermissonAppsList.addAll(result);
            ApplicationFragment.this.updateRecommendUI();
            ApplicationFragment.this.updateUI();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HsmPackageManager.registerListener(this.mExternalStorageListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContext = getActivity();
        this.mInflater = inflater;
        this.mFragmentView = this.mInflater.inflate(R.layout.all_app_activity, container, false);
        init();
        return this.mFragmentView;
    }

    public void onResume() {
        super.onResume();
        new AsynctaskUpdateDB().execute(new Void[0]);
    }

    public void onDestroy() {
        if (this.mRecommendManager != null) {
            this.mRecommendManager.unregisterListener(this.mRecommendChangeListener);
        }
        HsmPackageManager.unregisterListener(this.mExternalStorageListener);
        super.onDestroy();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ImageView imageView = (ImageView) this.mFragmentView.findViewById(R.id.no_app_icon);
        if (this.mContext.getResources().getBoolean(R.bool.IsSupportOptimizedInterface) && imageView != null) {
            relayoutImageView(imageView);
        }
    }

    private void relayoutImageView(ImageView imageView) {
        LayoutParams imageViewParams = (LayoutParams) imageView.getLayoutParams();
        imageViewParams.topMargin = GlobalContext.getDimensionPixelOffset(R.dimen.permission_emptyview_topspace);
        imageView.setLayoutParams(imageViewParams);
    }

    private void hideProgressBar() {
        View container = getView();
        if (container != null) {
            View progress = container.findViewById(R.id.progress_container);
            if (progress != null) {
                progress.setVisibility(8);
            }
        }
    }

    private void init() {
        this.mAppsListview = (ListView) this.mFragmentView.findViewById(16908298);
        this.mAppsListview.setOnScrollListener(this.mOnScrollListener);
        View footerView = this.mInflater.inflate(R.layout.blank_footer_view, this.mAppsListview, false);
        this.mAppsListview.setFooterDividersEnabled(false);
        this.mAppsListview.addFooterView(footerView, null, false);
        this.mPermissonAppsList = new ArrayList();
        this.mRecommendManager = RecommendManager.getInstance();
        this.mRecommendManager.registerListener(this.mRecommendChangeListener);
    }

    private void updateUI() {
        this.mApplicationAdapter = new AllAppAdapter(this.mContext, this.mPermissonAppsList, this.mItemClicker);
        this.mAppsListview.setAdapter(this.mApplicationAdapter);
        this.mAppsListview.setSelectionFromTop(this.mListViewPos, this.mHeightOffSet);
    }

    private void updateRecommendUI() {
        updateRecommendAppsInfo();
    }

    protected void removeRecommendHeader() {
    }

    protected void updateUIAfterRecommendOperation(List<AppInfoWrapper> result) {
        removeRecommendHeader();
        if (this.mRecommendManager != null) {
            this.mRecommendManager.applicationFragmentChange();
        }
        this.mPermissonAppsList.clear();
        this.mPermissonAppsList.addAll(result);
        updateUI();
    }
}
