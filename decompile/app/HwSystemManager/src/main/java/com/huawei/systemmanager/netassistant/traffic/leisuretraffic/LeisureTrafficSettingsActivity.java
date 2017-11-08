package com.huawei.systemmanager.netassistant.traffic.leisuretraffic;

import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import android.text.TextUtils;
import android.view.ViewGroup;
import android.widget.TimePicker;
import com.huawei.netassistant.ui.view.NetAssistantDialogManager;
import com.huawei.netassistant.ui.view.NetAssistantDialogManager.TrafficSetListener;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.netassistant.util.CommonMethodUtil;
import com.huawei.netassistant.util.DateUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.emui.activities.HsmPreferenceActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;

public class LeisureTrafficSettingsActivity extends HsmPreferenceActivity implements OnPreferenceChangeListener, MessageHandler {
    private static final int END_TIME_MSG = 4;
    private static final int INIT_MSG = 0;
    public static final String LEISURE_TIME_TRAFFIC_END_TIME_KEY = "leisure_time_traffic_end_time";
    public static final String LEISURE_TIME_TRAFFIC_NOTIFY_SWITCH = "leisure_time_traffic_notify_switch";
    public static final String LEISURE_TIME_TRAFFIC_SIZE_KEY = "leisure_time_traffic_size";
    public static final String LEISURE_TIME_TRAFFIC_START_TIME_KEY = "leisure_time_traffic_start_time";
    public static final String LEISURE_TIME_TRAFFIC_SWITCH_KEY = "leisure_time_traffic_switch";
    private static final int SIZE_MSG = 2;
    private static final int START_TIME_MSG = 3;
    private static final int SWITCH_MSG = 1;
    private static final int SWITCH_NOTIFY = 5;
    public static final String TAG = "LeisureTrafficSettingsActivity";
    private static final int UI_END_TIME_MSG = 14;
    private static final int UI_SIZE_MSG = 12;
    private static final int UI_START_TIME_MSG = 13;
    private static final int UI_SWITCH_MSG = 11;
    private GenericHandler mGenericHandler;
    private String mImsi;
    private TextArrowPreference mLeisureTimeTrafficEndTime;
    private SwitchPreference mLeisureTimeTrafficNotify;
    private TextArrowPreference mLeisureTimeTrafficSize;
    private TextArrowPreference mLeisureTimeTrafficStartTime;
    private SwitchPreference mLeisureTimeTrafficSwitch;
    private LeisureTrafficSetting mSetting;
    private GenericHandler mUiGenericHandler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.leisure_time_traffic_settings_preference);
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        this.mImsi = intent.getStringExtra(CommonConstantUtil.KEY_NETASSISTANT_IMSI);
        initPreferenceItem();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mGenericHandler = new GenericHandler(this, handlerThread.getLooper());
        this.mUiGenericHandler = new GenericHandler(this);
    }

    protected void onResume() {
        super.onResume();
        this.mUiGenericHandler.sendEmptyMessageDelayed(0, 100);
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mGenericHandler != null) {
            this.mGenericHandler.quiteLooper();
        }
        if (this.mUiGenericHandler != null) {
            this.mUiGenericHandler.quiteLooper();
        }
    }

    private void initPreferenceItem() {
        this.mLeisureTimeTrafficSwitch = (SwitchPreference) findPreference(LEISURE_TIME_TRAFFIC_SWITCH_KEY);
        this.mLeisureTimeTrafficSize = (TextArrowPreference) findPreference(LEISURE_TIME_TRAFFIC_SIZE_KEY);
        this.mLeisureTimeTrafficStartTime = (TextArrowPreference) findPreference(LEISURE_TIME_TRAFFIC_START_TIME_KEY);
        this.mLeisureTimeTrafficEndTime = (TextArrowPreference) findPreference(LEISURE_TIME_TRAFFIC_END_TIME_KEY);
        this.mLeisureTimeTrafficNotify = (SwitchPreference) findPreference(LEISURE_TIME_TRAFFIC_NOTIFY_SWITCH);
        getPreferenceScreen().removePreference(this.mLeisureTimeTrafficNotify);
        this.mLeisureTimeTrafficSwitch.setOnPreferenceChangeListener(this);
        this.mLeisureTimeTrafficNotify.setOnPreferenceChangeListener(this);
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (this.mSetting == null) {
            HwLog.e(TAG, "mSetting is null error return");
            return false;
        }
        ViewGroup container = (ViewGroup) findViewById(16908290);
        if (preference == this.mLeisureTimeTrafficSize) {
            NetAssistantDialogManager.createTrafficSettingsDialog(this, preference, new TrafficSetListener() {
                public void onSet(float size, String unit) {
                    HwLog.i(LeisureTrafficSettingsActivity.TAG, "**************size  " + size + "       *****unit  " + unit);
                    long totalPkg = CommonMethodUtil.unitConvert(size, unit);
                    String statParam1 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(unit));
                    String statParam2 = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, CommonMethodUtil.formatBytes(LeisureTrafficSettingsActivity.this, totalPkg));
                    HsmStat.statE((int) Events.E_NETASSISTANT_LEISURE_VALUE, statParam1, statParam2);
                    LeisureTrafficSettingsActivity.this.mLeisureTimeTrafficSize.setDetail(CommonMethodUtil.formatBytes(LeisureTrafficSettingsActivity.this, totalPkg));
                    LeisureTrafficSettingsActivity.this.mSetting.setmPackageSize(totalPkg);
                    LeisureTrafficSettingsActivity.this.mSetting.save(null);
                }
            }, getString(R.string.leisure_time_traffic_size), container);
            return true;
        } else if (preference == this.mLeisureTimeTrafficStartTime) {
            NetAssistantDialogManager.createTimePickDialog(this, R.string.leisure_time_traffic_start_time, this.mSetting.getStartHour(), this.mSetting.getStartMinute(), new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    HwLog.d(LeisureTrafficSettingsActivity.TAG, "**************hourOfDay  " + hourOfDay + "       *****minute  " + minute);
                    LeisureTrafficSettingsActivity.this.mLeisureTimeTrafficStartTime.setDetail(DateUtil.formatHourMinute(hourOfDay, minute));
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, DateUtil.formatHourMinute(hourOfDay, minute));
                    HsmStat.statE((int) Events.E_NETASSISTANT_LEISURE_END_TIME, statParam);
                    LeisureTrafficSettingsActivity.this.mSetting.setStartHM(hourOfDay, minute);
                    LeisureTrafficSettingsActivity.this.mSetting.save(null);
                }
            });
            return true;
        } else if (preference != this.mLeisureTimeTrafficEndTime) {
            return false;
        } else {
            NetAssistantDialogManager.createTimePickDialog(this, R.string.leisure_time_traffic_end_time, this.mSetting.getEndHour(), this.mSetting.getEndMinute(), new OnTimeSetListener() {
                public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                    HwLog.d(LeisureTrafficSettingsActivity.TAG, "**************hourOfDay  " + hourOfDay + "       *****minute  " + minute);
                    LeisureTrafficSettingsActivity.this.mLeisureTimeTrafficEndTime.setDetail(DateUtil.formatHourMinute(hourOfDay, minute));
                    String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, DateUtil.formatHourMinute(hourOfDay, minute));
                    HsmStat.statE((int) Events.E_NETASSISTANT_LEISURE_START_TIME, statParam);
                    LeisureTrafficSettingsActivity.this.mSetting.setEndHM(hourOfDay, minute);
                    LeisureTrafficSettingsActivity.this.mSetting.save(null);
                }
            });
            return true;
        }
    }

    public void onHandleMessage(Message msg) {
        boolean z = true;
        LeisureTrafficSetting leisureTrafficSetting;
        switch (msg.what) {
            case 0:
                if (this.mSetting == null) {
                    this.mSetting = new LeisureTrafficSetting(this.mImsi);
                    this.mSetting.get();
                    this.mLeisureTimeTrafficSwitch.setChecked(this.mSetting.ismSwitch());
                    this.mLeisureTimeTrafficSize.setDetail(CommonMethodUtil.formatBytes(this, this.mSetting.getmPackageSize()));
                    this.mLeisureTimeTrafficStartTime.setDetail(this.mSetting.getStartHM());
                    this.mLeisureTimeTrafficEndTime.setDetail(this.mSetting.getEndHM());
                    this.mLeisureTimeTrafficNotify.setChecked(this.mSetting.ismNotify());
                    break;
                }
                HwLog.d(TAG, "INIT_MSG , init already ,return");
                return;
            case 1:
                if (this.mSetting != null) {
                    int value = msg.arg1;
                    leisureTrafficSetting = this.mSetting;
                    if (value != 1) {
                        z = false;
                    }
                    leisureTrafficSetting.setmSwitch(z);
                    this.mSetting.save(null);
                    break;
                }
                HwLog.d(TAG, "SWITCH_MSG , not init ,return");
                return;
            case 5:
                if (this.mSetting != null) {
                    int value1 = msg.arg1;
                    leisureTrafficSetting = this.mSetting;
                    if (value1 != 1) {
                        z = false;
                    }
                    leisureTrafficSetting.setmNotify(z);
                    this.mSetting.save(null);
                    break;
                }
                HwLog.d(TAG, "SWITCH_NOTIFY , not init ,return");
                return;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        HwLog.i(TAG, "onPreferenceChange key = " + preference.getKey() + " value = " + newValue);
        int value;
        if (TextUtils.equals(LEISURE_TIME_TRAFFIC_SWITCH_KEY, preference.getKey())) {
            value = ((Boolean) newValue).booleanValue() ? 1 : 0;
            this.mGenericHandler.sendMessage(Message.obtain(this.mGenericHandler, 1, value, value));
            HwLog.d(TAG, "onPreferenceChange key = " + preference.getKey() + " value = " + value);
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_VAL, String.valueOf(value));
            HsmStat.statE((int) Events.E_NETASSISTANT_LEISURE_SWITCH, statParam);
        } else if (TextUtils.equals(LEISURE_TIME_TRAFFIC_NOTIFY_SWITCH, preference.getKey())) {
            value = ((Boolean) newValue).booleanValue() ? 1 : 0;
            this.mGenericHandler.sendMessage(Message.obtain(this.mGenericHandler, 5, value, value));
            HwLog.d(TAG, "onPreferenceChange key = " + preference.getKey() + " value = " + value);
        }
        return true;
    }
}
