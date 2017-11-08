package com.huawei.permissionmanager.ui.history;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.google.android.collect.Maps;
import com.google.common.collect.Lists;
import com.huawei.permissionmanager.db.AppInfo;
import com.huawei.permissionmanager.db.DBAdapter;
import com.huawei.permissionmanager.db.HistoryRecord;
import com.huawei.permissionmanager.db.PermissionDbVisitor;
import com.huawei.permissionmanager.ui.Permission;
import com.huawei.permissionmanager.ui.PermissionTableManager;
import com.huawei.permissionmanager.ui.SingleAppActivity;
import com.huawei.permissionmanager.utils.ShareCfg;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.BaseListFragment;
import com.huawei.systemmanager.comm.component.ContentLoader;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comparator.SizeComparator;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPackageManager;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PermissionHistoryFragment extends BaseListFragment<HistoryItem> {
    private static final int DATA_LOADER_ID = 1;
    public static final SizeComparator<HistoryItem> HISTORYITEM_COMPARATOR = new SizeComparator<HistoryItem>() {
        public long getKey(HistoryItem t) {
            return t.getLatestTime();
        }
    };
    public static final String TAG = "PermissionHistory";
    private PermissionHistoryAdapter adapter;
    private LayoutInflater mLayoutInflater;

    public static class DataLoader extends ContentLoader<List<HistoryItem>> {
        private int action = 11;
        private final ContentObserver mForceLoadObserver = new ContentObserver(new Handler()) {
            public void onChange(boolean selfChange) {
                DataLoader.this.onContentChanged();
            }
        };

        DataLoader(Context ctx) {
            super(ctx);
        }

        DataLoader(Context ctx, int action) {
            super(ctx);
            this.action = action;
        }

        public List<HistoryItem> loadInBackground() {
            HwLog.i(PermissionHistoryFragment.TAG, "loadInBackground begin");
            Context ctx = getContext();
            PermissionTableManager manager = PermissionTableManager.getInstance(ctx.getApplicationContext());
            List<HistoryRecord> records;
            if (this.action == 11) {
                records = PermissionDbVisitor.getAllowedHistoryRecord(ctx);
            } else {
                records = PermissionDbVisitor.getForbiddenHistoryRecord(ctx);
            }
            HashMap<String, HistoryItem> results = Maps.newHashMap();
            for (HistoryRecord record : records) {
                String pkgName = record.getPkgName();
                HsmPkgInfo info = HsmPackageManager.getInstance().getPkgInfo(pkgName);
                if (info != null) {
                    AppInfo appInfo = null;
                    try {
                        appInfo = DBAdapter.getInstance(ctx).getAppByPkgName(pkgName, "permission history fragment");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (appInfo != null) {
                        int permissionType = record.getPermissionType();
                        Permission permission = manager.getPermission(permissionType);
                        if (permission == null) {
                            HwLog.e(PermissionHistoryFragment.TAG, "loadInBackground could not find permission by type:" + permissionType);
                        } else {
                            String key = info.getPackageName() + permissionType;
                            if (results.containsKey(key)) {
                                HistoryItem historyItem = (HistoryItem) results.get(key);
                                historyItem.setHistoryCount(historyItem.getHistoryCount() + record.getCount());
                                if (historyItem.getLatestTime() < record.getTimestamp()) {
                                    historyItem.setLatestTime(record.getTimestamp());
                                }
                            } else {
                                results.put(key, new HistoryItem(info, permission, record.getCount(), record.getTimestamp()));
                            }
                        }
                    }
                }
            }
            ArrayList<HistoryItem> result = Lists.newArrayList(results.values());
            Collections.sort(result, PermissionHistoryFragment.HISTORYITEM_COMPARATOR);
            HwLog.i(PermissionHistoryFragment.TAG, "loadInBackground end, result size is " + result.size());
            return result;
        }

        protected void registerDataObserver() {
            HwLog.d(PermissionHistoryFragment.TAG, "registerDataObserver");
            getContext().getContentResolver().registerContentObserver(PermissionDbVisitor.HISTORY_URI, true, this.mForceLoadObserver);
        }

        protected void unRegisterDataObser() {
            HwLog.d(PermissionHistoryFragment.TAG, "unRegisterDataObserver");
            getContext().getContentResolver().unregisterContentObserver(this.mForceLoadObserver);
        }
    }

    public static class PermissionHistoryAdapter extends CommonAdapter<HistoryItem> {
        long mTodayStartTime;

        private static class ViewHolder {
            TextView description;
            ImageView icon;
            TextView title;

            private ViewHolder() {
            }
        }

        PermissionHistoryAdapter(Context ctx, LayoutInflater inflater) {
            super(ctx, inflater);
            this.mTodayStartTime = 0;
            this.mTodayStartTime = TimeUtil.getTodayStartTime();
        }

        protected View newView(int position, ViewGroup parent, HistoryItem item) {
            View innerView = getInflater().inflate(R.layout.permission_history_list_item, null);
            ViewHolder holder = new ViewHolder();
            holder.icon = (ImageView) innerView.findViewById(R.id.icon);
            holder.title = (TextView) innerView.findViewById(R.id.title);
            holder.description = (TextView) innerView.findViewById(R.id.description);
            innerView.setTag(holder);
            return innerView;
        }

        protected void bindView(int position, View view, HistoryItem item) {
            Context ctx = getContext();
            ViewHolder holder = (ViewHolder) view.getTag();
            holder.icon.setImageDrawable(item.getIcon());
            holder.title.setText(item.getHistoryDescription(ctx));
            long recordTime = item.getLatestTime();
            String todayReject = getString(R.string.permission_history_times_record, Long.valueOf(item.getHistoryCount()));
            String latestRejectTime = getString(R.string.permission_history_latest_record, DateUtils.formatDateTime(ctx, recordTime, 17));
            holder.description.setText(getString(R.string.permission_history_description, todayReject, latestRejectTime));
        }

        public boolean swapData(List<? extends HistoryItem> list) {
            HwLog.i(PermissionHistoryFragment.TAG, "swap data, list size:" + (list == null ? 0 : list.size()));
            this.mTodayStartTime = TimeUtil.getTodayStartTime();
            return super.swapData(list);
        }

        public long getDateStartTime() {
            return this.mTodayStartTime;
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.permission_history, container, false);
        this.mLayoutInflater = inflater;
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.adapter = new PermissionHistoryAdapter(getApplicationContext(), this.mLayoutInflater);
        setListAdapter(this.adapter);
        initLoader(1);
    }

    public Loader<List<HistoryItem>> onCreateLoader(int id, Bundle args) {
        return new DataLoader(getApplicationContext());
    }

    public void onResume() {
        super.onResume();
        checkTodayStartTime();
        if (this.adapter != null) {
            this.adapter.notifyDataSetChanged();
        }
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        Activity ac = getActivity();
        if (ac != null) {
            HistoryItem item = (HistoryItem) getAdapter().getItem(position);
            Intent intent = new Intent(ac, SingleAppActivity.class);
            intent.putExtra(ShareCfg.SINGLE_APP_UID, item.getUid());
            intent.putExtra(ShareCfg.SINGLE_APP_LABEL, item.getLabel());
            intent.putExtra(ShareCfg.SINGLE_APP_PKGNAME, item.getPackageName());
            try {
                startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void resetListPosition() {
        HwLog.i(TAG, "resetListPosition");
        AdapterView list = getListView();
        if (list != null) {
            list.setSelection(0);
        }
    }

    private void checkTodayStartTime() {
        if (isDataLoadFinished()) {
            ListAdapter adapter = getAdapter();
            if (adapter != null && (adapter instanceof PermissionHistoryAdapter)) {
                if (TimeUtil.getTodayStartTime() != ((PermissionHistoryAdapter) adapter).getDateStartTime()) {
                    HwLog.i(TAG, "checkTodayStartTime, today starttime changed!");
                    notifyLoader(1);
                }
            }
        }
    }
}
