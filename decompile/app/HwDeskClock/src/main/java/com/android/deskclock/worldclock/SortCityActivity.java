package com.android.deskclock.worldclock;

import android.app.ActionBar;
import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.os.UserHandle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.ViewHolder$WorldClockViewHolder;
import com.android.deskclock.drag.DragSortListView;
import com.android.deskclock.drag.DragSortListView.DropListener;
import com.android.deskclock.worldclock.City.LocationColumns;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Log;
import com.huawei.android.app.ActionBarEx;
import java.util.ArrayList;
import java.util.List;

public class SortCityActivity extends ListActivity {
    private BaseAdapter mAdapter;
    private List<City> mCityList;
    private DragSortListView mCityListView;
    private Cursor mCursor;
    private List<City> mDeleteCityList;
    private final DropListener mDropListener = new DropListener() {
        public void drop(int from, int to) {
            if (from < SortCityActivity.this.mCityList.size()) {
                City tmp = (City) SortCityActivity.this.mCityList.get(from);
                SortCityActivity.this.mCityList.remove(from);
                SortCityActivity.this.mCityList.add(to, tmp);
            }
            ((BaseAdapter) SortCityActivity.this.mCityListView.getAdapter()).notifyDataSetChanged();
            SortCityActivity.this.mCityListView.invalidateViews();
        }
    };
    private OnClickListener onClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Config.getTouch_mode()) {
                switch (v.getId()) {
                    case 16908295:
                        ClockReporter.reportEventMessage(SortCityActivity.this, 96, "");
                        SortCityActivity.this.finish();
                        break;
                    case 16908296:
                        if (true) {
                            int i;
                            int size = SortCityActivity.this.mDeleteCityList.size();
                            for (i = 0; i < size; i++) {
                                SortCityActivity.this.getContentResolver().delete(LocationColumns.CONTENT_URI, "city_index = ? ", new String[]{((City) SortCityActivity.this.mDeleteCityList.get(i)).cityIndex});
                                TimeZoneUtils.updatePreFromWorldPage(SortCityActivity.this, ((City) SortCityActivity.this.mDeleteCityList.get(i)).cityIndex);
                            }
                            ClockReporter.reportEventMessage(SortCityActivity.this, 74, "");
                            for (i = 0; i < SortCityActivity.this.mCityList.size(); i++) {
                                ContentValues values = new ContentValues();
                                values.put("sort_order", Integer.valueOf(i + 1));
                                SortCityActivity.this.getContentResolver().update(LocationColumns.CONTENT_URI, values, "city_index = ? ", new String[]{((City) SortCityActivity.this.mCityList.get(i)).cityIndex});
                            }
                            SortCityActivity.this.setResult(-1);
                            SortCityActivity.this.finish();
                            break;
                        }
                        break;
                }
            }
        }
    };
    private BroadcastReceiver syncDatereceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            SortCityActivity.this.mAdapter.notifyDataSetChanged();
        }
    };

    class EditCityAdapter extends BaseAdapter {
        private LayoutInflater mFactory;

        public EditCityAdapter(Context context) {
            this.mFactory = LayoutInflater.from(context);
        }

        public int getCount() {
            return SortCityActivity.this.mCityList != null ? SortCityActivity.this.mCityList.size() : 0;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder$WorldClockViewHolder viewHolder;
            final City mCity = (City) SortCityActivity.this.mCityList.get(position);
            if (convertView == null) {
                convertView = (RelativeLayout) this.mFactory.inflate(R.layout.sort_list, parent, false);
                viewHolder = new ViewHolder$WorldClockViewHolder();
                viewHolder.mCityName = (TextView) convertView.findViewById(R.id.city_label);
                viewHolder.mDeleteImage = (ImageView) convertView.findViewById(R.id.image_remove_city);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder$WorldClockViewHolder) convertView.getTag();
            }
            viewHolder.mCityName.setText(mCity.getDisplayName(SortCityActivity.this));
            viewHolder.mDeleteImage.setVisibility(0);
            viewHolder.mDeleteImage.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    SortCityActivity.this.mDeleteCityList.add(mCity);
                    ClockReporter.reportEventMessage(SortCityActivity.this, 73, "");
                    if (SortCityActivity.this.mCityList.contains(mCity)) {
                        SortCityActivity.this.mCityList.remove(mCity);
                    }
                    EditCityAdapter.this.notifyDataSetChanged();
                }
            });
            return convertView;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AlarmsMainActivity.ismLockedEnter()) {
            sendBroadcastAsUser(new Intent("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD"), UserHandle.OWNER);
        }
        DeskClockApplication.getDeskClockApplication().openAccelerated(true, this);
        Log.d("SortCityActivity", "onCreate");
        setContentView(R.layout.drag_listview);
        initView();
        initData();
        initAdapter();
    }

    private void initAdapter() {
        this.mAdapter = new EditCityAdapter(this);
        this.mCityListView.setAdapter(this.mAdapter);
        this.mCityListView.setItemsCanFocus(false);
        this.mCityListView.setDropListener(this.mDropListener);
    }

    private void initView() {
        this.mCityListView = (DragSortListView) getListView();
        ActionBar actionBar = getActionBar();
        ActionBarEx.setStartIcon(actionBar, true, null, this.onClickListener);
        ActionBarEx.setEndIcon(actionBar, true, null, this.onClickListener);
        DeskClockApplication.getDeskClockApplication().setTranslucentStatus(true, this);
    }

    private boolean judgeisCity(City city) {
        if ("c500".equals(city.cityIndex) || "c501".equals(city.cityIndex) || "c502".equals(city.cityIndex)) {
            return false;
        }
        return true;
    }

    private void initData() {
        this.mCursor = getContentResolver().query(LocationColumns.CONTENT_URI, null, null, null, null);
        this.mCityList = new ArrayList();
        this.mDeleteCityList = new ArrayList();
        if (this.mCursor != null && this.mCursor.moveToFirst()) {
            int i = 0;
            while (i < this.mCursor.getCount()) {
                City city = new City(getApplicationContext(), this.mCursor);
                if (city.sortId != 0 && judgeisCity(city)) {
                    this.mCityList.add(city);
                }
                i++;
                this.mCursor.moveToNext();
            }
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("SortCityActivity", "onConfigurationChanged");
        initAdapter();
    }

    protected void onStart() {
        super.onStart();
        Log.d("SortCityActivity", "onStart");
    }

    protected void onRestart() {
        super.onRestart();
        Log.d("SortCityActivity", "onRestart");
    }

    protected void onResume() {
        super.onResume();
        Log.d("SortCityActivity", "onResume");
        TimeZoneUtils.registerUpdateUIBroadcast(this, this.syncDatereceiver);
    }

    protected void onPause() {
        super.onPause();
        Log.d("SortCityActivity", "onPause");
    }

    protected void onStop() {
        super.onStop();
        Log.d("SortCityActivity", "onStop");
    }

    protected void onDestroy() {
        Log.d("SortCityActivity", "onDestroy");
        TimeZoneUtils.unRegisterUpdateUIBroadcast(this, this.syncDatereceiver);
        if (!(this.mCursor == null || this.mCursor.isClosed())) {
            this.mCursor.close();
            this.mCursor = null;
        }
        if (this.mDeleteCityList != null) {
            this.mDeleteCityList.clear();
            this.mDeleteCityList = null;
        }
        if (this.mCityList != null) {
            this.mCityList.clear();
        }
        this.mCityList = null;
        super.onDestroy();
        this.mCityListView = null;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                if (!Config.getTouch_mode()) {
                    onBackPressed();
                    break;
                }
                break;
        }
        return true;
    }
}
