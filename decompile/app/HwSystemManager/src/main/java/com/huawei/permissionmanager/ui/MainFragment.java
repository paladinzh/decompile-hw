package com.huawei.permissionmanager.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.collect.Lists;
import com.huawei.permissionmanager.ui.history.PermissionHistoryActivity;
import com.huawei.permissionmanager.ui.permissionlist.GroupPermItem;
import com.huawei.permissionmanager.ui.permissionlist.ISearchKey;
import com.huawei.permissionmanager.ui.permissionlist.LabelItem;
import com.huawei.permissionmanager.ui.permissionlist.PermissionAdapter;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.ListItem;
import com.huawei.systemmanager.comm.module.ModuleMgr;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.settingsearch.BaseSearchIndexProvider;
import com.huawei.systemmanager.settingsearch.SearchIndexableRaw;
import com.huawei.systemmanager.settingsearch.SettingSearchUtil;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.util.List;

public class MainFragment extends Fragment {
    private static final int DELAY_MSG = 100;
    private static final String LOG_TAG = "MainActivity";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            if (context == null) {
                HwLog.e(MainFragment.LOG_TAG, "getRawDataToIndex, context is null");
                return Lists.newArrayList();
            } else if (ModuleMgr.MODULE_PERMISSION.entryEnabled(context)) {
                return getSearchIndexableRows(context);
            } else {
                HwLog.i(MainFragment.LOG_TAG, "permission module is not enabled!");
                return Lists.newArrayList();
            }
        }

        private List<SearchIndexableRaw> getSearchIndexableRows(Context context) {
            List<ListItem> items = PermissionAdapter.initPermList(PermissionTableManager.getInstance(context), 0);
            List<ListItem> permItems = Lists.newArrayList();
            for (ListItem item : items) {
                if (!(item instanceof LabelItem)) {
                    if (item instanceof GroupPermItem) {
                        permItems.addAll(((GroupPermItem) item).getSubItems());
                    } else {
                        permItems.add(item);
                    }
                }
            }
            List<SearchIndexableRaw> allRaws = Lists.newArrayList();
            for (ListItem item2 : permItems) {
                allRaws.add(buildCommonPowerData(context, item2));
            }
            return allRaws;
        }

        private SearchIndexableRaw buildCommonPowerData(Context ctx, ListItem item) {
            SearchIndexableRaw raw = new SearchIndexableRaw(ctx);
            raw.screenTitle = ctx.getString(R.string.app_name);
            raw.title = item.getTitle(ctx);
            raw.iconResId = R.drawable.ic_settings_permission_manager;
            raw.intentAction = "huawei.intent.action.HSM_PERMISSION_MANAGER";
            raw.intentTargetPackage = "com.huawei.systemmanager";
            raw.intentTargetClass = MainActivity.class.getName();
            if (item instanceof ISearchKey) {
                raw.key = ((ISearchKey) item).getKey();
            } else {
                HwLog.e(MainFragment.LOG_TAG, "item is not Isearchable!");
                raw.key = raw.title;
            }
            return raw;
        }
    };
    private final int MSG_UPDATE_RECOMMEND_PERMISSION = 1;
    private View mFragLayout;
    private ViewPager mPager = null;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget subTabWidget;

    static class MainPagerAdapter extends SubTabFragmentPagerAdapter {
        public MainPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(position));
            HsmStat.statE(35, statParam);
            super.onPageSelected(position);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mFragLayout = inflater.inflate(R.layout.permission_main, null);
        initView(this.mFragLayout);
        return this.mFragLayout;
    }

    private void initView(View view) {
        this.mPager = (ViewPager) view.findViewById(R.id.permission_fragment_container);
        this.subTabWidget = (SubTabWidget) view.findViewById(R.id.subTab_layout);
        this.mSubTabFragmentPagerAdapter = new MainPagerAdapter(getActivity(), this.mPager, this.subTabWidget);
        SubTab applicationSubTab = this.subTabWidget.newSubTab(getString(R.string.permission_applications));
        applicationSubTab.setSubTabId(R.id.systemmanager_permission_applications);
        this.mSubTabFragmentPagerAdapter.addSubTab(applicationSubTab, new ApplicationFragment(), null, true);
        SubTab permissionSubTab = this.subTabWidget.newSubTab(getString(R.string.permission_rights));
        permissionSubTab.setSubTabId(R.id.systemmanager_permission_rights);
        this.mSubTabFragmentPagerAdapter.addSubTab(permissionSubTab, new PermissionFragment(), null, false);
        if (!TextUtils.isEmpty(getSelectItemKey())) {
            this.mPager.setCurrentItem(1);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.permission_menu, menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.permission_history:
                jumpToHistoryActivity();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void jumpToHistoryActivity() {
        if (getActivity() != null) {
            try {
                startActivity(new Intent(getActivity(), PermissionHistoryActivity.class));
                HsmStat.statE(40);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getSelectItemKey() {
        Intent intent = getActivity().getIntent();
        if (intent == null) {
            return "";
        }
        String key = intent.getStringExtra(SettingSearchUtil.KEY_EXTRA_SETTING);
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        return key;
    }
}
