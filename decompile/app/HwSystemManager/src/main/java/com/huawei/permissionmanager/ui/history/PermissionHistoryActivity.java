package com.huawei.permissionmanager.ui.history;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmFragmentActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.settingsearch.SettingSearchUtil;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class PermissionHistoryActivity extends HsmFragmentActivity {
    private static final int DELAY_MSG = 100;
    private static final String LOG_TAG = "PermissionHistoryActivity";
    private final int MSG_UPDATE_RECOMMEND_PERMISSION = 1;
    private ViewPager mPager = null;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    SubTabWidget subTabWidget;

    static class AccessRecordAdapter extends SubTabFragmentPagerAdapter {
        public AccessRecordAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, String.valueOf(position));
            HsmStat.statE(35, statParam);
            super.onPageSelected(position);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_access_record);
        initView();
    }

    private void initView() {
        getActionBar().setTitle(R.string.permission_manager_operation_history_title);
        this.mPager = (ViewPager) findViewById(R.id.permission_fragment_container);
        this.subTabWidget = (SubTabWidget) findViewById(R.id.subTab_layout);
        this.mSubTabFragmentPagerAdapter = new AccessRecordAdapter(this, this.mPager, this.subTabWidget);
        SubTab applicationSubTab = this.subTabWidget.newSubTab(getString(R.string.permission_forbidden_history));
        applicationSubTab.setSubTabId(R.id.systemmanager_permission_forbidden_history);
        this.mSubTabFragmentPagerAdapter.addSubTab(applicationSubTab, new PermissionForbiddenHistoryFragment(), null, true);
        SubTab permissionSubTab = this.subTabWidget.newSubTab(getString(R.string.permission_allow_history));
        permissionSubTab.setSubTabId(R.id.systemmanager_permission_allow_history);
        this.mSubTabFragmentPagerAdapter.addSubTab(permissionSubTab, new PermissionAllowHistoryFragment(), null, false);
        if (!TextUtils.isEmpty(getSelectItemKey())) {
            this.mPager.setCurrentItem(1);
        }
    }

    public String getSelectItemKey() {
        Intent intent = getIntent();
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
