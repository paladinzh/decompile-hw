package com.huawei.systemmanager.power.ui;

import android.app.ActionBar;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.optimize.base.Utility;
import com.huawei.systemmanager.power.util.SavingSettingUtil;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

public class HistoryOfHighPowerAppActivity extends HsmActivity {
    private static final String TAG = "HistoryOfHighPowerAppActivity";
    private HistoryOfHighPowerAdapter mAdapter;
    private Context mContext;
    private ListView mHistoryHpAppListView;
    private LinearLayout mNoHistoryLinearLayout;
    private PackageManager mPm;
    private List<HistoryAppInfo> mShowList = new ArrayList();

    private static class HistoryHighPowerAppComparator implements Comparator<HistoryAppInfo>, Serializable {
        private static final long serialVersionUID = 1;

        private HistoryHighPowerAppComparator() {
        }

        public int compare(HistoryAppInfo lObject, HistoryAppInfo rObject) {
            Long lTime = lObject.getmTime();
            Long rTime = rObject.getmTime();
            if (lTime.longValue() > rTime.longValue()) {
                return -1;
            }
            if (lTime.longValue() < rTime.longValue()) {
                return 1;
            }
            return 0;
        }
    }

    class MyDataLoaderTask extends AsyncTask<Void, Void, List<HistoryAppInfo>> {
        MyDataLoaderTask() {
        }

        protected List<HistoryAppInfo> doInBackground(Void... params) {
            return HistoryOfHighPowerAppActivity.this.initHistoryList();
        }

        protected void onPostExecute(List<HistoryAppInfo> result) {
            HistoryOfHighPowerAppActivity.this.mShowList.clear();
            HistoryOfHighPowerAppActivity.this.mShowList.addAll(result);
            HistoryOfHighPowerAppActivity.this.notifyAdapter();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.history_high_power_activity);
        this.mContext = getApplicationContext();
        this.mPm = this.mContext.getPackageManager();
        this.mHistoryHpAppListView = (ListView) findViewById(R.id.history_high_power_app_list);
        this.mNoHistoryLinearLayout = (LinearLayout) findViewById(R.id.no_history_item_layout);
        this.mAdapter = new HistoryOfHighPowerAdapter(this);
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.textview_history_title);
        actionBar.setDisplayHomeAsUpEnabled(true);
        new MyDataLoaderTask().execute(new Void[0]);
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    protected void onResume() {
        super.onResume();
        notifyAdapter();
        this.mHistoryHpAppListView.setAdapter(this.mAdapter);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void notifyAdapter() {
        if (this.mShowList.size() == 0) {
            this.mNoHistoryLinearLayout.setVisibility(0);
            this.mHistoryHpAppListView.setVisibility(8);
        } else {
            this.mNoHistoryLinearLayout.setVisibility(8);
            this.mHistoryHpAppListView.setVisibility(0);
        }
        if (this.mAdapter != null) {
            this.mAdapter.swapData(this.mShowList);
        }
    }

    private ArrayList<HistoryAppInfo> initHistoryList() {
        ArrayList<HistoryAppInfo> result = new ArrayList();
        for (Entry<String, Long> entry : SavingSettingUtil.getRougeAppWithTimeInfo(this).entrySet()) {
            String pkName = (String) entry.getKey();
            boolean exist = HsmPackageManager.getInstance().packageExists(pkName, 8192);
            boolean full_exist = HsmPackageManager.getInstance().packageExists(pkName, 0);
            if (exist && full_exist) {
                result.add(createHistoryAppInfo(pkName, (Long) entry.getValue()));
            }
        }
        HwLog.i("chad", "--> result.size = " + result.size());
        Collections.sort(result, new HistoryHighPowerAppComparator());
        return result;
    }

    private HistoryAppInfo createHistoryAppInfo(String packageName, Long timeWhenHighPower) {
        HistoryAppInfo info = new HistoryAppInfo();
        String label = Utility.getAppName(packageName, this);
        if (label == null) {
            label = "";
        }
        String replaceBlankAppName = Utility.replaceBlank(label);
        info.setmIcon(Utility.getAppLogo(packageName, this));
        info.setmLabel(replaceBlankAppName);
        info.setmTime(timeWhenHighPower);
        try {
            String[] pkgArrays = this.mPm.getPackagesForUid(this.mPm.getApplicationInfo(packageName, 0).uid);
            if (pkgArrays == null || pkgArrays.length <= 1) {
                info.setShareUidApps(false);
                return info;
            }
            info.setShareUidApps(true);
            return info;
        } catch (NameNotFoundException e) {
            HwLog.e(TAG, "createHistoryAppInfo pkgName:" + packageName + " not found exception");
            info.setShareUidApps(false);
        }
    }
}
