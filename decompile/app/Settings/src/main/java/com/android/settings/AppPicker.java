package com.android.settings;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.android.settings.applications.AppViewHolder;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppPicker extends ListActivity {
    private static final Comparator<MyApplicationInfo> sDisplayNameComparator = new Comparator<MyApplicationInfo>() {
        private final Collator collator = Collator.getInstance();

        public final int compare(MyApplicationInfo a, MyApplicationInfo b) {
            return this.collator.compare(a.label, b.label);
        }
    };
    private AppListAdapter mAdapter;
    private boolean mDebuggableOnly;
    private String mPermissionName;

    public class AppListAdapter extends ArrayAdapter<MyApplicationInfo> {
        private final LayoutInflater mInflater;
        private final List<MyApplicationInfo> mPackageInfoList = new ArrayList();

        public AppListAdapter(Context context) {
            MyApplicationInfo info;
            super(context, 0);
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            List<ApplicationInfo> pkgs = context.getPackageManager().getInstalledApplications(0);
            for (int i = 0; i < pkgs.size(); i++) {
                ApplicationInfo ai = (ApplicationInfo) pkgs.get(i);
                if (!(ai.uid == 1000 || (AppPicker.this.mDebuggableOnly && (ai.flags & 2) == 0 && "user".equals(Build.TYPE)))) {
                    if (AppPicker.this.mPermissionName != null) {
                        boolean requestsPermission = false;
                        try {
                            PackageInfo pi = AppPicker.this.getPackageManager().getPackageInfo(ai.packageName, 4096);
                            if (pi.requestedPermissions != null) {
                                for (String requestedPermission : pi.requestedPermissions) {
                                    if (requestedPermission.equals(AppPicker.this.mPermissionName)) {
                                        requestsPermission = true;
                                        break;
                                    }
                                }
                                if (!requestsPermission) {
                                }
                            }
                        } catch (NameNotFoundException e) {
                            Log.i("AppPicker", "AppListAdapter packageName not found:", e);
                        }
                    }
                    info = new MyApplicationInfo();
                    info.info = ai;
                    info.label = info.info.loadLabel(AppPicker.this.getPackageManager()).toString();
                    this.mPackageInfoList.add(info);
                }
            }
            Collections.sort(this.mPackageInfoList, AppPicker.sDisplayNameComparator);
            info = new MyApplicationInfo();
            info.label = context.getText(2131624123);
            this.mPackageInfoList.add(0, info);
            addAll(this.mPackageInfoList);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            AppViewHolder holder = AppViewHolder.createOrRecycle(this.mInflater, convertView);
            convertView = holder.rootView;
            MyApplicationInfo info = (MyApplicationInfo) getItem(position);
            holder.appName.setText(info.label);
            if (info.info != null) {
                holder.appIcon.setImageDrawable(info.info.loadIcon(AppPicker.this.getPackageManager()));
                holder.summary.setText(info.info.packageName);
            } else {
                holder.appIcon.setImageDrawable(null);
                holder.summary.setText("");
                holder.summary.setVisibility(8);
            }
            holder.disabled.setVisibility(8);
            return convertView;
        }
    }

    class MyApplicationInfo {
        ApplicationInfo info;
        CharSequence label;

        MyApplicationInfo() {
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPermissionName = getIntent().getStringExtra("com.android.settings.extra.REQUESTIING_PERMISSION");
        this.mDebuggableOnly = getIntent().getBooleanExtra("com.android.settings.extra.DEBUGGABLE", false);
        this.mAdapter = new AppListAdapter(this);
        if (this.mAdapter.getCount() <= 0) {
            finish();
        } else {
            setListAdapter(this.mAdapter);
            ListView listView = getListView();
            if (listView != null) {
                listView.setDivider(getDrawable(2130838530));
            }
        }
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onStop() {
        super.onStop();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    protected void onListItemClick(ListView l, View v, int position, long id) {
        MyApplicationInfo app = (MyApplicationInfo) this.mAdapter.getItem(position);
        Intent intent = new Intent();
        if (app.info != null) {
            intent.setAction(app.info.packageName);
        }
        setResult(-1, intent);
        finish();
    }
}
