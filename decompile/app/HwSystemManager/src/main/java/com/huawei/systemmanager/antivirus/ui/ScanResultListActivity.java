package com.huawei.systemmanager.antivirus.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.optimizer.addetect.ui.AdDetailActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.antivirus.ScanResultEntity;
import com.huawei.systemmanager.antivirus.ui.RiskPermDetailFragment.RiskPermDetailActivity;
import com.huawei.systemmanager.antivirus.utils.AntiVirusTools;
import com.huawei.systemmanager.antivirus.utils.AntivirusTipUtil;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Map;

public class ScanResultListActivity extends HsmActivity {
    private static final String TAG = "ScanResultListActivity";
    private ScanResultEntity mChooseItem = null;
    private Context mContext;
    private boolean mIsFromMainScreenEnter = false;
    private ListView mListView = null;
    private LinearLayout mNoAppLayout = null;
    private TextView mNoAppTextView = null;
    private ArrayList<ScanResultEntity> mResultList = null;
    private int mResultType = -1;
    private String mTypeText = null;

    private class ItemsAdapter extends BaseAdapter {
        private ArrayList<Map<String, Object>> mList = new ArrayList();

        private class ViewHolder {
            ImageView appIcon;
            TextView appName;
            TextView appType;

            private ViewHolder() {
            }
        }

        public void setData() {
            this.mList.clear();
            PackageManager pm = ScanResultListActivity.this.getPackageManager();
            ArrayList<ScanResultEntity> uninstallList = new ArrayList();
            for (ScanResultEntity result : ScanResultListActivity.this.mResultList) {
                Map<String, Object> map = result.getResultInfoMap(pm, ScanResultListActivity.this.mContext, ScanResultListActivity.this.mTypeText);
                if (map == null) {
                    uninstallList.add(result);
                    HwLog.i(ScanResultListActivity.TAG, "setData remove uninstalled pkg:" + result.getPackageName());
                } else {
                    this.mList.add(map);
                }
            }
            ScanResultListActivity.this.mResultList.removeAll(uninstallList);
            notifyDataSetChanged();
        }

        public int getCount() {
            return this.mList.size();
        }

        public Object getItem(int position) {
            return this.mList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            Map<String, Object> map = (Map) this.mList.get(position);
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = LayoutInflater.from(ScanResultListActivity.this.mContext).inflate(R.layout.common_list_item_twolines_image_arrow, null);
                holder.appIcon = (ImageView) convertView.findViewById(R.id.image);
                holder.appName = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                holder.appType = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.appIcon.setImageDrawable((Drawable) map.get(Const.APP_ICON));
            holder.appName.setText((String) map.get(Const.APP_NAME));
            holder.appType.setText((String) map.get("type"));
            return convertView;
        }
    }

    private class ListItemClickListener implements OnItemClickListener {
        private ListItemClickListener() {
        }

        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            ScanResultListActivity.this.mChooseItem = (ScanResultEntity) ScanResultListActivity.this.mResultList.get(position);
            if (ScanResultListActivity.this.mResultType == AntiVirusTools.TYPE_ADVERTISE) {
                if (ScanResultListActivity.this.mIsFromMainScreenEnter) {
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_PKG, ScanResultListActivity.this.mChooseItem.packageName);
                    HsmStat.statE((int) Events.E_FROM_MAINSCREEN_TO_AD_CLICK_VIRUS_ITEM, statParam);
                }
                ScanResultListActivity.this.startDetailActivity(ScanResultListActivity.this.mChooseItem, AdDetailActivity.class);
            } else if (ScanResultListActivity.this.mResultType == AntiVirusTools.TYPE_RISKPERM) {
                ScanResultListActivity.this.startRiskPermActivity(ScanResultListActivity.this.mChooseItem);
            } else {
                ScanResultListActivity.this.startDetailActivity(ScanResultListActivity.this.mChooseItem, VirusDetailsActivity.class);
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.virus_scan_result);
        this.mListView = (ListView) findViewById(R.id.scan_result_list);
        this.mNoAppLayout = (LinearLayout) findViewById(R.id.antivirus_no_app_layout);
        this.mNoAppTextView = (TextView) findViewById(R.id.antivirus_no_app_textview);
        this.mContext = getApplicationContext();
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        boolean z;
        this.mResultType = Integer.valueOf(intent.getIntExtra(AntiVirusTools.RESULT_TYPE, -1)).intValue();
        this.mResultList = (ArrayList) intent.getExtra(AntiVirusTools.RESULT_LIST);
        if (intent.getIntExtra(AntiVirusTools.KEY_FROM_MAIN_SCREEN_TO_AD, -1) > 0) {
            z = true;
        } else {
            z = false;
        }
        this.mIsFromMainScreenEnter = z;
        if (-1 == this.mResultType || this.mResultList == null) {
            finish();
            return;
        }
        initActionBar();
        if (this.mResultType == AntiVirusTools.TYPE_RISKPERM) {
            AntivirusTipUtil.putViewedCompetitor(getApplicationContext(), ScanResultEntity.convertToStringArray(this.mResultList));
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        switch (this.mResultType) {
            case 303:
                setTitle(R.string.software_type_risk);
                actionBar.setTitle(R.string.software_type_risk);
                this.mTypeText = getResources().getString(R.string.app_contains_risk);
                this.mNoAppTextView.setText(R.string.antivirus_no_app_risk);
                break;
            case 304:
                setTitle(R.string.software_type_not_official);
                actionBar.setTitle(R.string.software_type_not_official);
                this.mTypeText = getResources().getString(R.string.app_contains_not_official);
                this.mNoAppTextView.setText(R.string.antivirus_no_app_unofficial);
                break;
            case AntiVirusTools.TYPE_VIRUS /*305*/:
                setTitle(R.string.software_type_virus);
                actionBar.setTitle(R.string.software_type_virus);
                this.mTypeText = getResources().getString(R.string.app_contains_virus);
                this.mNoAppTextView.setText(R.string.antivirus_no_app_virus);
                break;
            case AntiVirusTools.TYPE_ADVERTISE /*307*/:
                setTitle(R.string.addetect_title);
                actionBar.setTitle(R.string.addetect_title);
                this.mTypeText = getResources().getString(R.string.addetect_title);
                this.mNoAppTextView.setText(R.string.no_advertisements);
                setTitle(this.mTypeText);
                break;
            case AntiVirusTools.TYPE_RISKPERM /*308*/:
                setTitle(R.string.antivirus_risk_permission_app_title);
                this.mTypeText = getResources().getString(R.string.antivirus_risk_permission_app_title);
                this.mNoAppTextView.setText(R.string.antivirus_no_risk_permission_app);
                break;
        }
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    protected void onResume() {
        super.onResume();
        ItemsAdapter adapter = new ItemsAdapter();
        this.mListView.setAdapter(adapter);
        adapter.setData();
        this.mListView.setOnItemClickListener(new ListItemClickListener());
        if (this.mResultList.size() == 0) {
            this.mListView.setVisibility(8);
            this.mNoAppLayout.setVisibility(0);
            ViewUtil.initEmptyViewMargin(this.mContext, this.mNoAppLayout);
        }
    }

    private void startDetailActivity(ScanResultEntity chooseItem, Class clazz) {
        Intent intent = new Intent();
        intent.setClass(this, clazz);
        intent.putExtra("result", this.mChooseItem);
        intent.putExtra(AntiVirusTools.RESULT_TYPE, this.mResultType);
        if (this.mResultType == AntiVirusTools.TYPE_ADVERTISE && this.mIsFromMainScreenEnter) {
            intent.putExtra(AntiVirusTools.KEY_FROM_MAIN_SCREEN_TO_AD, 1);
        }
        startActivityForResult(intent, 10001);
    }

    private void startRiskPermActivity(ScanResultEntity chooseItem) {
        if (chooseItem != null) {
            String pkg = chooseItem.getPackageName();
            Intent intent = new Intent(this, RiskPermDetailActivity.class);
            intent.putExtra(RiskPermDetailFragment.KEY_PACKAGE, pkg);
            try {
                startActivityForResult(intent, 10001);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finishActivity();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackPressed() {
        finishActivity();
        super.onBackPressed();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10001 && data != null && data.getBooleanExtra(AntiVirusTools.DELETE_ITEM, false)) {
            this.mResultList.remove(this.mChooseItem);
        }
    }

    private void finishActivity() {
        Intent intent = new Intent();
        intent.putExtra(AntiVirusTools.RESULT_LIST, this.mResultList);
        intent.putExtra(AntiVirusTools.RESULT_TYPE, this.mResultType);
        setResult(AntiVirusTools.RESULT_CODE, intent);
        finish();
    }
}
