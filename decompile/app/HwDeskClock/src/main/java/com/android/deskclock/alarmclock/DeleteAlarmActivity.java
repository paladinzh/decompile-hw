package com.android.deskclock.alarmclock;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.R;
import com.android.deskclock.ViewHolder$AlarmViewHolder;
import com.android.util.ClockReporter;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.ReflexUtil;
import com.huawei.android.app.ActionBarEx;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class DeleteAlarmActivity extends Activity {
    private List<Alarm> mAlarmList;
    private ListView mAlarmListView;
    private List<Alarm> mDeleteList;
    private OnClickListener onClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (v.getId() == 16908295) {
                ClockReporter.reportEventMessage(DeleteAlarmActivity.this, 31, "");
                DeleteAlarmActivity.this.finish();
            }
            if (v.getId() == 16908296) {
                if (!(DeleteAlarmActivity.this.mDeleteList == null || DeleteAlarmActivity.this.mDeleteList.isEmpty())) {
                    for (Alarm alarm : DeleteAlarmActivity.this.mDeleteList) {
                        Alarms.deleteAlarm(DeskClockApplication.getDeskClockApplication(), alarm.queryAlarmId());
                    }
                }
                DeleteAlarmActivity.this.finish();
            }
        }
    };

    private class DeleteAlarmAdater extends ArrayAdapter<Alarm> {
        public DeleteAlarmAdater(Context context, int resource, List<Alarm> objects) {
            super(context, resource, objects);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final Context context = getContext();
            View row = convertView;
            if (convertView == null) {
                row = LayoutInflater.from(context).inflate(R.layout.delete_alarm_list, parent, false);
                ViewHolder$AlarmViewHolder holder = new ViewHolder$AlarmViewHolder();
                holder.mDaysOfWeek = (DayOfWeekLayout) row.findViewById(R.id.daysOfWeek);
                holder.mDigitalClock = (DigitalClock) row.findViewById(R.id.digitalClock);
                holder.mImageView = (ImageView) row.findViewById(R.id.image_remove_alarm);
                row.setTag(holder);
            }
            ViewHolder$AlarmViewHolder viewHolder = (ViewHolder$AlarmViewHolder) row.getTag();
            Calendar c = Calendar.getInstance();
            final Alarm alarm = (Alarm) getItem(position);
            c.set(11, alarm.hour);
            c.set(12, alarm.minutes);
            viewHolder.mDigitalClock.setTime(c);
            viewHolder.mDaysOfWeek.updateText(alarm);
            viewHolder.mImageView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    ClockReporter.reportEventMessage(context, 30, "");
                    if (DeleteAlarmActivity.this.mDeleteList.contains(alarm)) {
                        HwLog.i("DeleteAlarmAdater", "delete list has a same alarm");
                    } else {
                        DeleteAlarmActivity.this.mDeleteList.add(alarm);
                    }
                    if (DeleteAlarmActivity.this.mAlarmList.contains(alarm)) {
                        DeleteAlarmActivity.this.mAlarmList.remove(alarm);
                        DeleteAlarmAdater.this.remove(alarm);
                    }
                }
            });
            return row;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("DeleteAlarmActivity", "onCreate");
        DeskClockApplication.getDeskClockApplication().openAccelerated(true, this);
        setContentView(R.layout.delete_alarm);
        initView();
        initData(savedInstanceState);
        this.mAlarmListView.setAdapter(new DeleteAlarmAdater(this, R.layout.delete_alarm_list, this.mAlarmList));
    }

    private void initView() {
        this.mAlarmListView = (ListView) findViewById(R.id.delete_list);
        ActionBar actionBar = getActionBar();
        ActionBarEx.setStartIcon(actionBar, true, null, this.onClickListener);
        ActionBarEx.setEndIcon(actionBar, true, null, this.onClickListener);
        DeskClockApplication.getDeskClockApplication().setTranslucentStatus(true, this);
    }

    private void initData(Bundle state) {
        this.mAlarmList = new ArrayList();
        this.mDeleteList = new ArrayList();
        Cursor cursor = Alarms.getAlarmsCursor(DeskClockApplication.getDeskClockApplication().getContentResolver());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                this.mAlarmList.add(new Alarm(cursor));
            }
            cursor.close();
        }
        if (state != null) {
            ArrayList<Integer> deleteList = state.getIntegerArrayList("deleteList");
            if (deleteList != null) {
                int size = this.mAlarmList.size();
                for (int i = 0; i < size; i++) {
                    if (deleteList.contains(Integer.valueOf(((Alarm) this.mAlarmList.get(i)).id))) {
                        this.mDeleteList.add((Alarm) this.mAlarmList.get(i));
                    }
                }
                this.mAlarmList.removeAll(this.mDeleteList);
            }
        }
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        ArrayList<Integer> deleteList = new ArrayList();
        int size = this.mDeleteList.size();
        for (int i = 0; i < size; i++) {
            deleteList.add(Integer.valueOf(((Alarm) this.mDeleteList.get(i)).id));
        }
        outState.putIntegerArrayList("deleteList", deleteList);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("DeleteAlarmActivity", "onConfigurationChanged");
    }

    protected void onStart() {
        super.onStart();
        Log.d("DeleteAlarmActivity", "onStart");
    }

    protected void onRestart() {
        super.onRestart();
        Log.d("DeleteAlarmActivity", "onRestart");
    }

    protected void onResume() {
        super.onResume();
        Log.d("DeleteAlarmActivity", "onResume");
    }

    protected void onPause() {
        super.onPause();
        Log.d("DeleteAlarmActivity", "onPause");
    }

    protected void onStop() {
        super.onStop();
        Log.d("DeleteAlarmActivity", "onStop");
    }

    protected void onDestroy() {
        Log.d("DeleteAlarmActivity", "onDestroy");
        super.onDestroy();
        ReflexUtil.fixInputMethodManagerLeak(this);
        this.mDeleteList.clear();
        this.mAlarmList.clear();
    }
}
