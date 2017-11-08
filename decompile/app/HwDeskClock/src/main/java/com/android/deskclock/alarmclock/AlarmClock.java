package com.android.deskclock.alarmclock;

import android.app.Activity;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import com.android.deskclock.ClockFragment;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.DigitalClock;
import com.android.deskclock.R;
import com.android.deskclock.RingCache;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.ViewHolder$AlarmViewHolder;
import com.android.deskclock.alarmclock.Alarm.Columns;
import com.android.util.ClockReporter;
import com.android.util.Config;
import com.android.util.Log;
import com.android.util.Utils;
import java.util.Calendar;

public class AlarmClock extends ClockFragment implements OnItemClickListener, OnClickListener {
    private static int backStack = 0;
    private AlarmTimeAdapter mAdapter;
    private AlarmContentObserver mAlarmContentObserver = new AlarmContentObserver(null);
    private LinearLayout mAlarmView;
    private ListView mAlarmsList;
    private BackgroundQueryHandler mBackgroundQueryHandler;
    private Cursor mCursor;
    private View mFootview;
    public LocalBroadcastManager mLocalBroadcastManager;
    private boolean mLongPress = false;
    private int mMaxVisibleRows = 4;
    private Button mNoButton;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && "com.android.deskclock.updatealarmlist".equals(intent.getAction())) {
                AlarmClock.this.mAlarmsList.invalidateViews();
            }
        }
    };
    public boolean mReceiverRegistered = false;
    private boolean mShowMenu = false;
    private View mSpaceLineEnd;
    private boolean mSwitchChanged = false;
    private View noAlarmView;

    public class AlarmContentObserver extends ContentObserver {
        public AlarmContentObserver(Handler handler) {
            super(handler);
        }

        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            AlarmClock.this.startQuery();
        }
    }

    private class AlarmTimeAdapter extends CursorAdapter {
        private Calendar calendar;
        private LayoutInflater mFactory;

        public AlarmTimeAdapter(Context context, Cursor cursor) {
            super(context, cursor);
            this.mFactory = LayoutInflater.from(context);
        }

        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            return this.mFactory.inflate(R.layout.alarm_time, parent, false);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            if (cursor == null || cursor.isClosed()) {
                Log.w("AlarmClock", "the cursor is null or have close.");
                return;
            }
            final Alarm alarm = new Alarm(cursor);
            ViewHolder$AlarmViewHolder viewHolder = (ViewHolder$AlarmViewHolder) view.getTag();
            if (viewHolder == null) {
                viewHolder = new ViewHolder$AlarmViewHolder();
                viewHolder.mSwitchParentView = (RelativeLayout) view.findViewById(R.id.rl_switch);
                viewHolder.mAlarmSwitch = (Switch) view.findViewById(R.id.clock_onoff);
                viewHolder.mDigitalClock = (DigitalClock) view.findViewById(R.id.digitalClock);
                viewHolder.mDaysOfWeek = (DayOfWeekLayout) view.findViewById(R.id.daysOfWeek);
                view.setTag(viewHolder);
            }
            if (Alarms.hasAlarmBeenSnoozed(Utils.getSharedPreferences(context, "AlarmClock", 0), alarm.id)) {
                alarm.enabled = true;
            }
            viewHolder.mAlarmSwitch.setOnCheckedChangeListener(null);
            viewHolder.mAlarmSwitch.setChecked(alarm.enabled);
            final Switch alarmSwitch = viewHolder.mAlarmSwitch;
            viewHolder.mSwitchParentView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    alarmSwitch.setChecked(!alarmSwitch.isChecked());
                }
            });
            viewHolder.mAlarmSwitch.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton compoundbutton, boolean flag) {
                    int i = 1;
                    AlarmClock.this.mSwitchChanged = true;
                    Context deskClockApplication = DeskClockApplication.getDeskClockApplication();
                    StringBuilder append = new StringBuilder().append("switch");
                    if (!flag) {
                        i = 0;
                    }
                    ClockReporter.reportEventContainMessage(deskClockApplication, 13, append.append(i).toString(), 0);
                    AlarmClock.this.updateAlarm(flag, alarm);
                    AlarmClock.this.mSwitchChanged = false;
                    RingCache.getInstance().updateRingCache(DeskClockApplication.getDeskClockApplication(), alarm.alert, flag, false);
                }
            });
            this.calendar = Calendar.getInstance();
            this.calendar.set(11, alarm.hour);
            this.calendar.set(12, alarm.minutes);
            viewHolder.mDigitalClock.setTime(this.calendar);
            viewHolder.mDaysOfWeek.updateText(alarm);
        }
    }

    private final class BackgroundQueryHandler extends AsyncQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            Log.dRelease("Alarmclock", "onQueryComplete......");
            Activity activity = AlarmClock.this.getActivity();
            if (activity != null) {
                switch (token) {
                    case 10001:
                        if (cursor != null && !cursor.isClosed()) {
                            AlarmClock.this.mCursor = cursor;
                            ClockReporter.reportEventContainMessage(AlarmClock.this.getActivity(), 64, "ALARM_COUNT", AlarmClock.this.mCursor.getCount());
                            if (AlarmClock.this.mAdapter == null) {
                                AlarmClock.this.mAdapter = new AlarmTimeAdapter(activity, cursor);
                                AlarmClock.this.mAlarmsList.setAdapter(AlarmClock.this.mAdapter);
                            } else {
                                AlarmClock.this.mAdapter.changeCursor(cursor);
                            }
                            AlarmClock.this.updateUi();
                            break;
                        }
                        Log.w("AlarmClock", "the alarm cursor has been closed.");
                        return;
                        break;
                }
            }
        }
    }

    private void setObject2Null() {
        this.mAlarmView = null;
        this.mAlarmsList = null;
        this.mNoButton = null;
        this.mSpaceLineEnd = null;
        this.mAdapter = null;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.alarm_clock_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (Config.clockTabIndex() == 0) {
            if (this.mShowMenu) {
                android.util.Log.e("menu", "onPrepareOptionsMenu show");
                menu.setGroupVisible(R.id.alarm_clock_button_menu, true);
            } else {
                android.util.Log.e("menu", "onPrepareOptionsMenu hide");
                menu.setGroupVisible(R.id.alarm_clock_button_menu, false);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.alarm_new_btn:
                ClockReporter.reportEventMessage(getActivity(), 9, "");
                addNewAlarm();
                return true;
            case R.id.alarm_settings_btn:
                ClockReporter.reportEventContainMessage(getActivity(), 10, "tab:1", 0);
                Intent intentToSettings = new Intent(getActivity(), SettingsActivity.class);
                intentToSettings.setAction("action_from_alarm");
                startActivity(intentToSettings);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onFragmentPause() {
        super.onFragmentPause();
        setHasOptionsMenu(false);
    }

    public void onFragmentResume() {
        super.onFragmentResume();
        setHasOptionsMenu(true);
        updateMenuState();
    }

    private void updateAlarm(boolean enabled, Alarm alarm) {
        Alarms.enableAlarmInternal(DeskClockApplication.getDeskClockApplication(), alarm.queryAlarmId(), enabled);
        Alarms.setNextAlert(DeskClockApplication.getDeskClockApplication(), this.mSwitchChanged);
        Alarms.clearAutoSilent(DeskClockApplication.getDeskClockApplication(), alarm.queryAlarmId());
        if (enabled) {
            SetAlarm.popAlarmSetToast(DeskClockApplication.getDeskClockApplication(), alarm.queryAlarmHour(), alarm.qeuryAlarmMinites(), alarm.queryDaysOfWeek(), alarm.queryDaysOfWeekType());
        } else {
            if (alarm.queryAlarmId() == Utils.getDefaultSharedPreferences(DeskClockApplication.getDeskClockApplication()).getInt("is_power_off_alarm_id", -1) && Alarms.isAirplaneMode(DeskClockApplication.getDeskClockApplication()) == 0) {
                Alarms.closeAirplaneMode(DeskClockApplication.getDeskClockApplication());
            }
        }
        Alarms.reportOpenAlarmCount(DeskClockApplication.getDeskClockApplication());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Log.iRelease("AlarmClock", "onCreateView");
        this.mAlarmView = (LinearLayout) inflater.inflate(R.layout.alarm_clock, container, false);
        this.mMaxVisibleRows = getResources().getInteger(R.integer.max_visible_rows);
        updateLayout(this.mAlarmView);
        setBackStack(backStack + 1);
        registerLocalBroadcastReceiver();
        return this.mAlarmView;
    }

    private void registerLocalBroadcastReceiver() {
        this.mLocalBroadcastManager = LocalBroadcastManager.getInstance(getActivity());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("com.android.deskclock.updatealarmlist");
        if (!this.mReceiverRegistered) {
            this.mLocalBroadcastManager.registerReceiver(this.mReceiver, intentFilter);
            this.mReceiverRegistered = true;
        }
    }

    private void unregisterLocalBroadcastReceiver() {
        if (this.mReceiverRegistered) {
            this.mLocalBroadcastManager.unregisterReceiver(this.mReceiver);
            this.mReceiverRegistered = false;
        }
    }

    public void onResume() {
        super.onResume();
        Log.dRelease("AlarmClock", "onResume");
        this.mLongPress = false;
    }

    private void updateUi() {
        int i = 0;
        if (this.mCursor != null && !this.mCursor.isClosed()) {
            int count = this.mCursor.getCount();
            boolean exsitAlarm = count >= 1;
            updateMenuState();
            if (!exsitAlarm) {
                if (this.noAlarmView == null) {
                    this.noAlarmView = ((ViewStub) this.mAlarmView.findViewById(R.id.noalarmview)).inflate();
                    this.mNoButton = (Button) this.noAlarmView.findViewById(R.id.new_alarm);
                    if (!Utils.isExistCustomFont() && Utils.isChineseLanguage()) {
                        this.mNoButton.setTypeface(Utils.getmRobotoXianBlackTypeface());
                    }
                    this.mNoButton.setOnClickListener(this);
                    Log.dRelease("AlarmClock", "the no alarm view is new...");
                }
                this.noAlarmView.setVisibility(0);
            } else if (this.noAlarmView != null) {
                this.noAlarmView.setVisibility(8);
            }
            if (this.mSpaceLineEnd != null) {
                int i2;
                if (count < this.mMaxVisibleRows || !exsitAlarm) {
                    Log.dRelease("AlarmClock", "count < 4");
                } else {
                    Log.dRelease("AlarmClock", "count >= 4");
                }
                boolean canScrollVertically = !this.mAlarmsList.canScrollVertically(1) ? this.mAlarmsList.canScrollVertically(-1) : true;
                View view = this.mSpaceLineEnd;
                if (canScrollVertically && exsitAlarm) {
                    i2 = 0;
                } else {
                    i2 = 8;
                }
                view.setVisibility(i2);
            }
            ListView listView = this.mAlarmsList;
            if (!exsitAlarm) {
                i = 8;
            }
            listView.setVisibility(i);
        }
    }

    private void updateMenuState() {
        if (this.mCursor != null && !this.mCursor.isClosed()) {
            boolean exsitAlarm;
            if (this.mCursor.getCount() >= 1) {
                exsitAlarm = true;
            } else {
                exsitAlarm = false;
            }
            if (exsitAlarm) {
                this.mShowMenu = true;
            } else {
                this.mShowMenu = false;
            }
            getActivity().invalidateOptionsMenu();
        }
    }

    private void updateLayout(View v) {
        this.mSpaceLineEnd = v.findViewById(R.id.spaceid_end);
        this.mAlarmsList = (ListView) v.findViewById(R.id.alarms_list);
        if (!Utils.isLandScreen(getActivity())) {
            this.mFootview = LayoutInflater.from(getActivity()).inflate(R.layout.alerm_footview, this.mAlarmsList, false);
            this.mAlarmsList.addFooterView(this.mFootview, null, false);
        }
        this.mAlarmsList.setVerticalScrollBarEnabled(true);
        this.mAlarmsList.setOnItemClickListener(this);
        this.mAlarmsList.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                AlarmClock.this.mLongPress = true;
                Activity activity = AlarmClock.this.getActivity();
                ClockReporter.reportEventMessage(activity, 29, "");
                if (activity != null) {
                    AlarmClock.this.startActivity(new Intent(activity, DeleteAlarmActivity.class));
                }
                return false;
            }
        });
        this.mAlarmsList.setOnScrollListener(new OnScrollListener() {
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                boolean canScrollVertically = !view.canScrollVertically(1) ? view.canScrollVertically(-1) : true;
                if (AlarmClock.this.mSpaceLineEnd != null) {
                    AlarmClock.this.mSpaceLineEnd.setVisibility(canScrollVertically ? 0 : 8);
                }
            }
        });
    }

    private void addNewAlarm() {
        Context activity = getActivity();
        if (activity != null) {
            if (this.mCursor == null || this.mCursor.getCount() <= 1000) {
                startActivity(new Intent(activity, SetAlarm.class));
                return;
            }
            Log.d("AlarmClock", "Maximum limit please remove useless alarm clock.");
            ToastMaster.showToast(activity, (int) R.string.alarm_full_Toast, 1);
        }
    }

    public void onPause() {
        super.onPause();
        Log.dRelease("AlarmClock", "onPause");
    }

    public void onStart() {
        super.onStart();
        Log.dRelease("AlarmClock", "onStart");
    }

    public void onStop() {
        super.onStop();
        Log.dRelease("AlarmClock", "onStop");
    }

    public void onDestroy() {
        if (backStack > 1) {
            setBackStack(2);
        } else {
            setBackStack(backStack - 1);
        }
        if (this.mBackgroundQueryHandler != null) {
            this.mBackgroundQueryHandler.cancelOperation(10001);
            this.mBackgroundQueryHandler.removeCallbacksAndMessages(null);
        }
        Log.dRelease("AlarmClock", "onDestroy");
        Activity activity = getActivity();
        if (activity != null) {
            activity.getContentResolver().unregisterContentObserver(this.mAlarmContentObserver);
        }
        this.mAlarmContentObserver = null;
        try {
            super.onDestroy();
        } catch (Exception e) {
            Log.e("AlarmClock", "onDestroy : Exception = " + e.getMessage());
        }
        ToastMaster.cancelToast();
        setObject2Null();
    }

    public static int queryBackStack() {
        return backStack;
    }

    public static void setBackStack(int value) {
        backStack = value;
    }

    public void onDestroyView() {
        Log.dRelease("AlarmClock", "onDestroyView");
        unregisterLocalBroadcastReceiver();
        if (backStack <= 2) {
            setBackStack(backStack - 1);
        }
        if (this.mAdapter != null) {
            this.mAdapter.changeCursor(null);
        }
        this.mAdapter = null;
        if (this.mCursor != null) {
            this.mCursor.close();
            this.mCursor = null;
        }
        super.onDestroyView();
    }

    public void onItemClick(AdapterView parent, View v, int pos, long id) {
        if (!this.mLongPress) {
            Activity activity = getActivity();
            if (activity != null) {
                ClockReporter.reportEventMessage(activity, 14, "");
                Alarm alarm = new Alarm((Cursor) this.mAlarmsList.getAdapter().getItem(pos));
                Intent intent = new Intent(activity, SetAlarm.class);
                intent.putExtra("intent.extra.alarm", alarm);
                startActivity(intent);
            }
        }
        this.mLongPress = false;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        Log.dRelease("AlarmClock", "onCreate");
        Activity activity = getActivity();
        if (activity != null) {
            this.mBackgroundQueryHandler = new BackgroundQueryHandler(activity.getContentResolver());
            startQuery();
            activity.getContentResolver().registerContentObserver(Columns.CONTENT_URI, true, this.mAlarmContentObserver);
        }
        setBackStack(backStack + 1);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.dRelease("AlarmClock", "onConfigurationChanged");
    }

    private void startQuery() {
        Log.dRelease("Alarmclock", "startQuery");
        if (this.mBackgroundQueryHandler != null) {
            this.mBackgroundQueryHandler.startQuery(10001, Integer.valueOf(0), Columns.CONTENT_URI, null, null, null, "hour, minutes ASC");
        }
    }

    public void onClick(View v) {
        Activity activity = getActivity();
        if (activity != null) {
            switch (v.getId()) {
                case R.id.new_alarm:
                    ClockReporter.reportEventMessage(activity, 9, "");
                    addNewAlarm();
                    break;
            }
        }
    }
}
