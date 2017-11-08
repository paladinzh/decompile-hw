package com.huawei.systemmanager.adblock.ui.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.adblock.comm.AdConst;
import com.huawei.systemmanager.adblock.ui.presenter.AdPresenterImpl;
import com.huawei.systemmanager.adblock.ui.presenter.IAdPresenter;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;

public class AdBlockAppListActivity extends HsmActivity implements IAdView {
    private static final String TAG = "AdBlockAppListActivity";
    private IAdPresenter mAdPresenter;
    private View mAllOpLayout = null;
    private Switch mAllOpSwitch = null;
    private Context mAppContext = null;
    private TextView mCountView = null;
    private ListView mListView = null;
    private View mProgressBarLayout = null;

    public boolean isSupprotMultiUser() {
        return false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mAppContext = getApplicationContext();
        setContentView(R.layout.adblock_app_list);
        findViewAndInitSetting();
        this.mAdPresenter = new AdPresenterImpl(getApplicationContext(), this);
        this.mAdPresenter.onCreate(savedInstanceState);
    }

    protected void onResume() {
        super.onResume();
        this.mAdPresenter.onResume();
    }

    protected void onPause() {
        this.mAdPresenter.onPause();
        super.onPause();
    }

    protected void onDestroy() {
        this.mAdPresenter.onDestroy();
        super.onDestroy();
    }

    private void findViewAndInitSetting() {
        this.mProgressBarLayout = findViewById(R.id.adblock_app_loading);
        this.mCountView = (TextView) findViewById(R.id.adblock_count_tip);
        this.mAllOpLayout = findViewById(R.id.adblock_all_op_switch_layout);
        ((TextView) this.mAllOpLayout.findViewById(ViewUtil.HWID_TEXT_1)).setText(R.string.startupmgr_all_op);
        this.mAllOpSwitch = (Switch) this.mAllOpLayout.findViewById(R.id.switcher);
        this.mListView = (ListView) findViewById(R.id.adblock_list_view);
    }

    public void initListView(ListAdapter listAdapter, OnItemClickListener listener) {
        this.mListView.setItemsCanFocus(false);
        this.mListView.setAdapter(listAdapter);
        this.mListView.setOnItemClickListener(listener);
    }

    public void showProgressBar(boolean show) {
        if (this.mProgressBarLayout != null) {
            this.mProgressBarLayout.setVisibility(show ? 0 : 8);
        }
    }

    public void updateTipView(int totalCount, int checkedCount) {
        if (checkedCount == 0) {
            this.mCountView.setText(this.mAppContext.getResources().getQuantityString(R.plurals.scan_result_advertises, totalCount, new Object[]{Integer.valueOf(totalCount)}));
        } else {
            this.mCountView.setText(this.mAppContext.getResources().getQuantityString(R.plurals.ad_blocked_num, checkedCount, new Object[]{Integer.valueOf(checkedCount)}));
        }
        Intent data = new Intent();
        data.putExtra("totalCount", totalCount);
        data.putExtra(AdConst.BUNDLE_CHECKED_COUNT, checkedCount);
        setResult(-1, data);
    }

    public void updateAllOpSwitch(boolean hasMember, boolean on, OnCheckedChangeListener listener) {
        this.mAllOpLayout.setVisibility(hasMember ? 0 : 8);
        this.mAllOpSwitch.setOnCheckedChangeListener(null);
        this.mAllOpSwitch.setChecked(on);
        this.mAllOpSwitch.setOnCheckedChangeListener(listener);
    }
}
