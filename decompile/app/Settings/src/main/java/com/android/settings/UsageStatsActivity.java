package com.android.settings;

import android.app.Activity;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class UsageStatsActivity extends Activity implements OnItemSelectedListener {
    private UsageStatsAdapter mAdapter;
    private LayoutInflater mInflater;
    private PackageManager mPm;
    private UsageStatsManager mUsageStatsManager;

    public static class AppNameComparator implements Comparator<UsageStats> {
        private Map<String, String> mAppLabelList;

        AppNameComparator(Map<String, String> appList) {
            this.mAppLabelList = appList;
        }

        public final int compare(UsageStats a, UsageStats b) {
            return ((String) this.mAppLabelList.get(a.getPackageName())).compareTo((String) this.mAppLabelList.get(b.getPackageName()));
        }
    }

    static class AppViewHolder {
        TextView lastTimeUsed;
        TextView pkgName;
        TextView usageTime;

        AppViewHolder() {
        }
    }

    public static class LastTimeUsedComparator implements Comparator<UsageStats> {
        public final int compare(UsageStats a, UsageStats b) {
            if (a.getLastTimeUsed() < b.getLastTimeUsed()) {
                return 1;
            }
            if (a.getLastTimeUsed() > b.getLastTimeUsed()) {
                return -1;
            }
            return 0;
        }
    }

    class UsageStatsAdapter extends BaseAdapter {
        private AppNameComparator mAppLabelComparator;
        private final ArrayMap<String, String> mAppLabelMap = new ArrayMap();
        private int mDisplayOrder = 0;
        private LastTimeUsedComparator mLastTimeUsedComparator = new LastTimeUsedComparator();
        private final ArrayList<UsageStats> mPackageStats = new ArrayList();
        private UsageTimeComparator mUsageTimeComparator = new UsageTimeComparator();

        UsageStatsAdapter() {
            Calendar cal = Calendar.getInstance();
            cal.add(6, -5);
            List<UsageStats> stats = UsageStatsActivity.this.mUsageStatsManager.queryUsageStats(4, cal.getTimeInMillis(), System.currentTimeMillis());
            if (stats != null) {
                ArrayMap<String, UsageStats> map = new ArrayMap();
                int statCount = stats.size();
                for (int i = 0; i < statCount; i++) {
                    UsageStats pkgStats = (UsageStats) stats.get(i);
                    try {
                        this.mAppLabelMap.put(pkgStats.getPackageName(), UsageStatsActivity.this.mPm.getApplicationInfo(pkgStats.getPackageName(), 0).loadLabel(UsageStatsActivity.this.mPm).toString());
                        UsageStats existingStats = (UsageStats) map.get(pkgStats.getPackageName());
                        if (existingStats == null) {
                            map.put(pkgStats.getPackageName(), pkgStats);
                        } else {
                            existingStats.add(pkgStats);
                        }
                    } catch (NameNotFoundException e) {
                    }
                }
                this.mPackageStats.addAll(map.values());
                this.mAppLabelComparator = new AppNameComparator(this.mAppLabelMap);
                sortList();
            }
        }

        public int getCount() {
            return this.mPackageStats.size();
        }

        public Object getItem(int position) {
            return this.mPackageStats.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder;
            if (convertView == null) {
                convertView = UsageStatsActivity.this.mInflater.inflate(2130969225, null);
                holder = new AppViewHolder();
                holder.pkgName = (TextView) convertView.findViewById(2131887292);
                holder.lastTimeUsed = (TextView) convertView.findViewById(2131887293);
                holder.usageTime = (TextView) convertView.findViewById(2131887294);
                convertView.setTag(holder);
            } else {
                holder = (AppViewHolder) convertView.getTag();
            }
            UsageStats pkgStats = (UsageStats) this.mPackageStats.get(position);
            if (pkgStats != null) {
                holder.pkgName.setText((String) this.mAppLabelMap.get(pkgStats.getPackageName()));
                holder.lastTimeUsed.setText(DateUtils.formatSameDayTime(pkgStats.getLastTimeUsed(), System.currentTimeMillis(), 2, 2));
                holder.usageTime.setText(DateUtils.formatElapsedTime(pkgStats.getTotalTimeInForeground() / 1000));
            } else {
                Log.w("UsageStatsActivity", "No usage stats info for package:" + position);
            }
            return convertView;
        }

        void sortList(int sortOrder) {
            if (this.mDisplayOrder != sortOrder) {
                this.mDisplayOrder = sortOrder;
                sortList();
            }
        }

        private void sortList() {
            if (this.mDisplayOrder == 0) {
                Collections.sort(this.mPackageStats, this.mUsageTimeComparator);
            } else if (this.mDisplayOrder == 1) {
                Collections.sort(this.mPackageStats, this.mLastTimeUsedComparator);
            } else if (this.mDisplayOrder == 2) {
                Collections.sort(this.mPackageStats, this.mAppLabelComparator);
            }
            notifyDataSetChanged();
        }
    }

    public static class UsageTimeComparator implements Comparator<UsageStats> {
        public final int compare(UsageStats a, UsageStats b) {
            if (a.getTotalTimeInForeground() < b.getTotalTimeInForeground()) {
                return 1;
            }
            if (a.getTotalTimeInForeground() > b.getTotalTimeInForeground()) {
                return -1;
            }
            return 0;
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(2130969224);
        this.mUsageStatsManager = (UsageStatsManager) getSystemService("usagestats");
        this.mInflater = (LayoutInflater) getSystemService("layout_inflater");
        this.mPm = getPackageManager();
        ((Spinner) findViewById(2131887290)).setOnItemSelectedListener(this);
        ListView listView = (ListView) findViewById(2131887291);
        this.mAdapter = new UsageStatsAdapter();
        listView.setAdapter(this.mAdapter);
    }

    public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
        this.mAdapter.sortList(position);
    }

    public void onNothingSelected(AdapterView<?> adapterView) {
    }
}
