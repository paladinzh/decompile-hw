package com.android.deskclock.alarmclock;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.SparseBooleanArray;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.TimePicker.OnTimeChangedListener;
import android.widget.Toast;
import com.android.deskclock.AlarmsMainActivity;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.RingCache;
import com.android.deskclock.RingtoneHelper;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.alarmclock.Alarm.DaysOfWeek;
import com.android.deskclock.alarmclock.AlarmSetDialogManager.SelectDialogCallBack;
import com.android.util.ClockReporter;
import com.android.util.CompatUtils;
import com.android.util.Config;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.HwLog;
import com.android.util.Log;
import com.android.util.ReflexUtil;
import com.android.util.UIUtils;
import com.android.util.UIUtils.DialogCallBack;
import com.android.util.Utils;
import com.huawei.android.app.ActionBarEx;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

public class SetAlarm extends Activity {
    private static boolean mIsAddAlarmMode = true;
    private Alarm alarm;
    OnClickListener dialogInterface = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    Intent intent = new Intent("android.intent.action.RINGTONE_PICKER");
                    intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", SetAlarm.this.onRestoreRingtone());
                    intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
                    intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
                    intent.putExtra("android.intent.extra.ringtone.TYPE", 4);
                    intent.putExtra("android.intent.extra.ringtone.TITLE", SetAlarm.this.getTitle());
                    SetAlarm.this.startActivityForResult(intent, 14);
                    return;
                case 1:
                    try {
                        Intent musicIntent = new Intent("android.intent.action.PICK");
                        musicIntent.setData(Uri.parse("content://media/external/audio/media"));
                        musicIntent.putExtra("android.intent.extra.ringtone.EXISTING_URI", SetAlarm.this.onRestoreRingtone());
                        SetAlarm.this.startActivityForResult(musicIntent, 15);
                        return;
                    } catch (RuntimeException e) {
                        Log.e("SetAlarm", "onClick : RuntimeException = " + e.getMessage());
                        return;
                    }
                default:
                    return;
            }
        }
    };
    private EditText editText;
    private boolean isClickSave = false;
    private boolean isSaved = false;
    private boolean isUpdate = false;
    private Dialog mAlertDialog;
    private boolean mBoolVibrate;
    private boolean mChooseRingState = false;
    private DaysOfWeek mDaysOfWeek = new DaysOfWeek(0);
    private int mDaysOfWeekType = 0;
    private String mDefaultTag;
    private int mDefaultVolume;
    private Button mDeletebtn;
    private SetAlarmFragment mFragment = null;
    private boolean mHasTouch;
    private int mHour = -1;
    private int mId;
    private String mLableStr;
    private boolean mMarkUri = false;
    private int mMinute = -1;
    private Uri mOldUri;
    private String mRepeatStr;
    private Uri mTempRingToneUri;
    private TimePicker mTimePicker;
    private Timer mTimer = new Timer();
    private String mUpdateStr;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case 16908295:
                    HwLog.i("SetAlarm", "onClickListener click cancel");
                    ClockReporter.reportEventMessage(SetAlarm.this, 20, "");
                    if (SetAlarm.this.isUpdate) {
                        SetAlarm.this.showSaveDialog();
                        return;
                    }
                    SetAlarm.this.exitForSelf(false);
                    SetAlarm.this.setResult(0);
                    SetAlarm.this.finish();
                    return;
                case 16908296:
                    HwLog.i("SetAlarm", "onClickListener click ok to save alarm");
                    ClockReporter.reportEventMessage(SetAlarm.this, 19, "");
                    if (SetAlarm.this.mTimePicker != null) {
                        SetAlarm.this.mTimePicker.clearFocus();
                    } else {
                        SetAlarm.this.mFragment.clearTimeFocus();
                    }
                    SetAlarm.this.saveAndExit();
                    return;
                case R.id.delete_alarm:
                    HwLog.i("SetAlarm", "onClickListener click  to delete alarm");
                    ClockReporter.reportEventMessage(SetAlarm.this, 67, "");
                    String message = SetAlarm.this.getString(R.string.delete_alarm_confirm);
                    SetAlarm.this.dismissDialog();
                    SetAlarm.this.mAlertDialog = UIUtils.createAlertDialog(SetAlarm.this, SetAlarm.this.getString(R.string.delete), message, R.string.delete, 17039360, new DialogCallBack() {
                        public void confirm() {
                            ClockReporter.reportEventMessage(SetAlarm.this, 68, "");
                            Alarms.deleteAlarm(DeskClockApplication.getDeskClockApplication(), SetAlarm.this.mId);
                            SetAlarm.this.finish();
                        }

                        public void cancel() {
                        }
                    }, 1);
                    return;
                default:
                    return;
            }
        }
    };

    static class LocalTextWatcher implements TextWatcher {
        private int editEnd = 0;
        private int editStart = 0;
        private InputMethodManager imm;
        private int nowcurrentLenght = 0;
        WeakReference<SetAlarm> wRfCtx;

        public LocalTextWatcher(SetAlarm context) {
            this.wRfCtx = new WeakReference(context);
            this.imm = (InputMethodManager) context.getSystemService("input_method");
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
            SetAlarm setAlarm = (SetAlarm) this.wRfCtx.get();
            if (setAlarm != null) {
                if (calculateLenght(setAlarm) > 200) {
                    ToastMaster.showToast(DeskClockApplication.getDeskClockApplication(), (int) R.string.strlenght_full_Toast, 0);
                    if (this.imm != null) {
                        this.imm.restartInput(setAlarm.editText);
                    }
                    this.editStart = setAlarm.editText.getSelectionStart();
                    this.editEnd = setAlarm.editText.getSelectionEnd();
                    setAlarm.editText.removeTextChangedListener(this);
                    while (calculateLenght(setAlarm) > 200) {
                        s.delete(this.editStart - 1, this.editEnd);
                        this.editStart--;
                        this.editEnd--;
                    }
                    Log.d("SetAlarm", "RecorderTextWatcher->afterTextChanged : " + this.editStart + "--setSelection to end-->>" + this.editEnd + "  lenght = " + calculateLenght(setAlarm));
                    if (calculateLenght(setAlarm) >= 200) {
                        setAlarm.editText.setSelection(setAlarm.editText.length());
                    }
                    setAlarm.editText.addTextChangedListener(this);
                }
                if (calculateLenght(setAlarm) == 0) {
                    ((AlertDialog) setAlarm.mAlertDialog).getButton(-1).setEnabled(false);
                } else {
                    ((AlertDialog) setAlarm.mAlertDialog).getButton(-1).setEnabled(true);
                }
            }
        }

        private int calculateLenght(SetAlarm setAlarm) {
            try {
                this.nowcurrentLenght = setAlarm.editText.getText().toString().trim().getBytes("UTF-8").length;
            } catch (UnsupportedEncodingException e) {
                Log.d("SetAlarm", "RecorderTextWatcher->calculateLenght : UnsupportedEncodingException = " + e.getMessage());
            }
            return this.nowcurrentLenght;
        }
    }

    static class LocalTimerTask extends TimerTask {
        WeakReference<SetAlarm> wRfCtx;

        public LocalTimerTask(SetAlarm context) {
            this.wRfCtx = new WeakReference(context);
        }

        public void run() {
            SetAlarm setAlarm = (SetAlarm) this.wRfCtx.get();
            if (setAlarm != null) {
                InputMethodManager imm = (InputMethodManager) setAlarm.getSystemService("input_method");
                if (imm == null) {
                    Log.w("SetAlarm", "get InputMethodManager fail ");
                } else {
                    imm.showSoftInput(setAlarm.editText, 0);
                    Log.d("AlarmSetDialogManager", "openInputMethod->run : open input method. ");
                }
            }
        }
    }

    private void setObject2Null() {
        if (this.mTimePicker != null) {
            this.mTimePicker.setOnTimeChangedListener(null);
        }
        this.mTimePicker = null;
        this.mDeletebtn = null;
        this.alarm = null;
        this.mDaysOfWeek = null;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.dRelease("SetAlarm", "onCreate");
        if (Config.istablet()) {
            setContentView(R.layout.set_alarm_container_pad);
        } else {
            setContentView(R.layout.set_alarm_container);
        }
        if (icicle != null) {
            this.alarm = (Alarm) icicle.getParcelable("currentAlarm");
            this.mHasTouch = icicle.getBoolean("touchCircle", false);
        }
        ActionBar actionBar = getActionBar();
        ActionBarEx.setStartIcon(actionBar, true, null, this.onClickListener);
        ActionBarEx.setEndIcon(actionBar, true, null, this.onClickListener);
        actionBar.setTitle(R.string.menu_edit_alarm);
        setAddAlarmMode(false);
        DeskClockApplication.getDeskClockApplication().setTranslucentStatus(true, this);
        if (this.alarm == null) {
            this.alarm = (Alarm) getIntent().getParcelableExtra("intent.extra.alarm");
            if (this.alarm == null) {
                this.alarm = new Alarm();
            } else {
                this.mOldUri = this.alarm.alert;
            }
        }
        if (this.alarm.isDefaultAlarm()) {
            setTitle(R.string.add_alarm);
            setAddAlarmMode(true);
            actionBar.setTitle(R.string.add_alarm);
        }
        this.mDefaultVolume = this.alarm.queryVolume();
        this.mDefaultTag = this.alarm.queryAlarmlable();
        init();
        updatePrefs(this.alarm);
    }

    private static void setAddAlarmMode(boolean isAddAlarmMode) {
        mIsAddAlarmMode = isAddAlarmMode;
    }

    protected void onResume() {
        super.onResume();
        Log.d("SetAlarm", "onResume");
        if (this.editText != null && this.editText.isFocused()) {
            openInputMethod();
        }
        if (this.mTimePicker != null) {
            int hour = this.mTimePicker.getCurrentHour().intValue();
            int minute = this.mTimePicker.getCurrentMinute().intValue();
            this.mTimePicker.setIs24HourView(Boolean.valueOf(DateFormat.is24HourFormat(DeskClockApplication.getDeskClockApplication())));
            this.mTimePicker.setCurrentHour(Integer.valueOf(hour));
            this.mTimePicker.setCurrentMinute(Integer.valueOf(minute));
            this.mTimePicker.setOnTimeChangedListener(new OnTimeChangedListener() {
                public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                    SetAlarm.this.isUpdate = true;
                    if (!SetAlarm.this.mHasTouch) {
                        ClockReporter.reportEventMessage(SetAlarm.this, 76, "");
                        SetAlarm.this.mHasTouch = true;
                    }
                }
            });
        }
    }

    public void getTimePickTime(int hour, int minite) {
        this.isUpdate = true;
        this.mHour = hour;
        this.mMinute = minite;
    }

    public int getHour() {
        return this.mHour;
    }

    public int getMinute() {
        return this.mMinute;
    }

    private void init() {
        this.mHour = this.alarm.queryAlarmHour();
        this.mMinute = this.alarm.qeuryAlarmMinites();
        this.mTimePicker = (TimePicker) findViewById(R.id.alarm_timePicker);
        if (this.mTimePicker != null) {
            this.mTimePicker.setIs24HourView(Boolean.valueOf(DateFormat.is24HourFormat(DeskClockApplication.getDeskClockApplication())));
            if (!(this.mHour == -1 || this.mMinute == -1)) {
                this.mTimePicker.setCurrentHour(Integer.valueOf(this.mHour));
                this.mTimePicker.setCurrentMinute(Integer.valueOf(this.mMinute));
            }
            this.mHour = this.mTimePicker.getCurrentHour().intValue();
            this.mMinute = this.mTimePicker.getMinute();
        }
        this.mDeletebtn = (Button) findViewById(R.id.delete_alarm);
        if (this.mDeletebtn != null) {
            this.mDeletebtn.setTypeface(Utils.getmRobotoXianBlackTypeface());
        }
        if (mIsAddAlarmMode) {
            if (this.mDeletebtn != null) {
                this.mDeletebtn.setVisibility(8);
            }
            View endLine = findViewById(R.id.lineEnd);
            if (endLine != null) {
                endLine.setVisibility(8);
            }
        }
        if (this.mDeletebtn != null) {
            this.mDeletebtn.setOnClickListener(this.onClickListener);
        }
    }

    protected Uri onRestoreRingtone() {
        Uri uri = this.alarm.alert;
        if (uri == null || "silent".equals(uri.toString())) {
            return null;
        }
        if ("content://settings/system/alarm_alert".equals(uri.toString())) {
            return RingtoneHelper.getAvailableRingtone(DeskClockApplication.getDeskClockApplication(), uri);
        }
        if (RingtoneHelper.isRingtoneAvailable(DeskClockApplication.getDeskClockApplication(), uri)) {
            return uri;
        }
        return RingtoneHelper.getUriByPath(DeskClockApplication.getDeskClockApplication(), uri);
    }

    public void setDaysOfWeek(DaysOfWeek dow) {
        this.mDaysOfWeek.set(dow);
    }

    public DaysOfWeek getDaysOfWeek() {
        return this.mDaysOfWeek;
    }

    private int getResultType(int weekCode, int selectId) {
        if (selectId == 3 && weekCode == 127) {
            return 2;
        }
        if (selectId == 3 && weekCode == 31) {
            return 1;
        }
        if (selectId == 3 && weekCode == 0) {
            return 0;
        }
        return selectId;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d("SetAlarm", "onConfigurationChanged");
        this.alarm = buildAlarm();
        if (this.mTimePicker != null) {
            this.mTimePicker.setOnTimeChangedListener(null);
        }
        this.mTimePicker = null;
        if (Config.istablet()) {
            setContentView(R.layout.set_alarm_container_pad);
        } else {
            setContentView(R.layout.set_alarm_container);
        }
        init();
        updatePrefs(this.alarm);
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("SetAlarm", "onSaveInstanceState");
        outState.putParcelable("currentAlarm", buildAlarmFromUi());
        outState.putBoolean("touchCircle", this.mHasTouch);
    }

    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d("SetAlarm", "onRestoreInstanceState");
        if (savedInstanceState != null) {
            this.alarm = (Alarm) savedInstanceState.getParcelable("currentAlarm");
        }
    }

    private void updatePrefs(Alarm alarm) {
        String repeatShow;
        this.mLableStr = alarm.getLabelOrDefault(DeskClockApplication.getDeskClockApplication());
        this.mId = alarm.queryAlarmId();
        this.mHour = alarm.queryAlarmHour();
        this.mMinute = alarm.qeuryAlarmMinites();
        setDaysOfWeek(alarm.queryDaysOfWeek());
        this.mDaysOfWeekType = alarm.queryDaysOfWeekType();
        String dayofweekShow = AlarmSetDialogManager.getRepeatTypeOfChina(DeskClockApplication.getDeskClockApplication(), this.mDaysOfWeekType);
        if (alarm.isUserDefineRing()) {
            repeatShow = AlarmSetDialogManager.toGogaleString(DeskClockApplication.getDeskClockApplication(), alarm.queryDaysOfWeek(), false);
        } else {
            repeatShow = dayofweekShow;
        }
        this.mBoolVibrate = alarm.queryAlarmVibrate();
        Bundle bundle = new Bundle();
        String ringtStr = getRingStrFromUri(alarm.queryAlarmAlert());
        bundle.putString("label", this.mLableStr);
        bundle.putBoolean("vibrate", this.mBoolVibrate);
        bundle.putString("repeat", repeatShow);
        bundle.putString("rington", ringtStr);
        bundle.putInt("hour", this.mHour);
        bundle.putInt("minute", this.mMinute);
        bundle.putBoolean("add", mIsAddAlarmMode);
        SetAlarmFragment fragment = SetAlarmFragment.newInstance(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.set_alarm_container, fragment);
        this.mFragment = fragment;
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    private String getRingStrFromUri(Uri uri) {
        String title = RingtoneHelper.getAailableRingtoneTitle(DeskClockApplication.getDeskClockApplication(), uri);
        Resources res = getResources();
        if (title == null) {
            return res.getString(R.string.default_ringtone);
        }
        if ("silent".equals(title)) {
            return res.getString(R.string.silent_alarm_summary);
        }
        return title;
    }

    public void onPause() {
        super.onPause();
        Log.d("SetAlarm", "onPause");
    }

    private long saveAlarm() {
        long time;
        this.alarm = buildAlarmFromUi();
        if (this.alarm.isDefaultAlarm() || !Alarms.isExistInDB(this, this.alarm.queryAlarmId())) {
            time = Alarms.addAlarm(DeskClockApplication.getDeskClockApplication(), this.alarm);
            RingCache.getInstance().addRingCache(this, this.alarm.queryAlarmAlert());
            this.mId = this.alarm.queryAlarmId();
        } else {
            time = Alarms.setAlarm(DeskClockApplication.getDeskClockApplication(), this.alarm);
            if (this.mMarkUri) {
                RingCache.getInstance().deleteRingCache(this, this.mOldUri, false);
                RingCache.getInstance().addRingCache(this, this.alarm.queryAlarmAlert());
            }
            RingCache.getInstance().checkRingCache(this, false);
        }
        if (Utils.getDefaultSharedPreferences(DeskClockApplication.getDeskClockApplication()).getInt("is_power_off_alarm_id", -1) != -1 && Alarms.isAirplaneMode(DeskClockApplication.getDeskClockApplication()) == 0) {
            Alarms.closeAirplaneMode(DeskClockApplication.getDeskClockApplication());
        }
        return time;
    }

    private Alarm buildAlarm() {
        Alarm tempAlarm = buildAlarmFromUi();
        tempAlarm.saveAlarmAlert(this.alarm.queryAlarmAlert());
        return tempAlarm;
    }

    private Alarm buildAlarmFromUi() {
        Alarm saveAlarm = new Alarm();
        saveAlarm.saveAlarmId(this.mId);
        saveAlarm.saveAlarmEnable(true);
        if (this.mTimePicker != null) {
            saveAlarm.saveAlarmHour(this.mTimePicker.getCurrentHour().intValue());
            saveAlarm.saveAlarmMinites(this.mTimePicker.getCurrentMinute().intValue());
        } else {
            saveAlarm.saveAlarmHour(this.mHour);
            saveAlarm.saveAlarmMinites(this.mMinute);
        }
        saveAlarm.saveDaysOfWeek(getDaysOfWeek());
        saveAlarm.saveDaysOfWeekType(this.mDaysOfWeekType);
        saveAlarm.savedaysOfWeekShow(this.mRepeatStr);
        saveAlarm.saveAlarmVibrate(this.mBoolVibrate);
        saveAlarm.saveVolume(this.mDefaultVolume);
        if (this.mUpdateStr != null) {
            saveAlarm.saveAlarmLabel(this.mUpdateStr);
        } else {
            saveAlarm.saveAlarmLabel(this.mDefaultTag);
        }
        if (this.mMarkUri) {
            saveAlarm.saveAlarmAlert(Uri.parse(RingtoneHelper.getActualUri(DeskClockApplication.getDeskClockApplication(), this.alarm.alert)));
        } else {
            saveAlarm.saveAlarmAlert(this.alarm.queryAlarmAlert());
        }
        Log.i("SetAlarm", "buildAlarmFromUi : save alert = " + saveAlarm.queryAlarmAlert());
        return saveAlarm;
    }

    private void saveAndExit() {
        long time = saveAlarm();
        if (time > 0) {
            popAlarmSetToast(DeskClockApplication.getDeskClockApplication(), time);
        }
        exitForSelf(true);
        this.isSaved = true;
        Intent alarmIntent = new Intent();
        alarmIntent.putExtra("currentId", this.alarm.queryAlarmId());
        setResult(-1, alarmIntent);
        reportAlarmMessage(this.alarm);
        finish();
    }

    private void reportAlarmMessage(Alarm alarm) {
        if (alarm != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(11, alarm.queryAlarmHour());
            calendar.set(12, alarm.qeuryAlarmMinites());
            ClockReporter.reportEventContainMessage(this, 90, new SimpleDateFormat("HH:mm", Locale.ENGLISH).format(Long.valueOf(calendar.getTimeInMillis())), 0);
        }
    }

    public static void popAlarmSetToast(Context context, int hour, int minute, DaysOfWeek daysOfWeek, int dayOfWeekType) {
        Calendar c;
        if (dayOfWeekType == 4 && Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.isHasWorkDayfn()) {
            c = Alarms.calculateAlarmForWorkDay(hour, minute, daysOfWeek);
        } else {
            c = Alarms.calculateAlarm(hour, minute, daysOfWeek);
        }
        popAlarmSetToast(context, c.getTimeInMillis());
    }

    public static void popAlarmSetToast(Context context, long timeInMillis) {
        Toast toast = Toast.makeText(context, formatToast(context, timeInMillis), 1);
        ToastMaster.setToast(toast);
        toast.show();
    }

    public static String formatToast(Context context, long timeInMillis) {
        long delta = timeInMillis - System.currentTimeMillis();
        long hours = delta / 3600000;
        long minutes = (delta / 60000) % 60;
        long days = hours / 24;
        hours %= 24;
        String daySeq = days == 0 ? "" : context.getResources().getQuantityString(R.plurals.days, (int) days, new Object[]{Integer.valueOf((int) days)});
        String minSeq = minutes == 0 ? "" : context.getResources().getQuantityString(R.plurals.minutes, (int) minutes, new Object[]{Integer.valueOf((int) minutes)});
        String hourSeq = hours == 0 ? "" : context.getResources().getQuantityString(R.plurals.hours, (int) hours, new Object[]{Integer.valueOf((int) hours)});
        int index = (((days > 0 ? 1 : (days == 0 ? 0 : -1)) > 0 ? 1 : 0) | ((hours > 0 ? 1 : (hours == 0 ? 0 : -1)) > 0 ? 2 : 0)) | ((minutes > 0 ? 1 : (minutes == 0 ? 0 : -1)) > 0 ? 4 : 0);
        String[] formats = context.getResources().getStringArray(R.array.alarmclock_set_toast);
        if (index == 0) {
            return String.format(formats[index], new Object[]{Integer.valueOf(1)});
        }
        return String.format(formats[index], new Object[]{daySeq, hourSeq, minSeq});
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        Log.iRelease("SetAlarm", "onRequestPermissionsResult  requestCode =" + requestCode);
        handleResult(requestCode, -1, null);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.iRelease("SetAlarm", "onActivityResult");
        handleResult(requestCode, resultCode, data);
    }

    public void handleResult(int requestCode, int resultCode, Intent data) {
        this.mChooseRingState = false;
        if (requestCode == 0) {
            if (resultCode == -1) {
                if (!CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE")) {
                    if (data != null) {
                        this.mTempRingToneUri = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                    }
                    CompatUtils.grantPermissionsByManager(this, 102);
                } else if (data != null) {
                    this.alarm.alert = (Uri) data.getParcelableExtra("android.intent.extra.ringtone.PICKED_URI");
                    Log.d("SetAlarm", "onActivityResult : uri = " + this.alarm.alert);
                    this.mMarkUri = true;
                    this.mFragment.updateRington(getRingStrFromUri(this.alarm.alert));
                }
            }
        } else if (requestCode == 101) {
            if (CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE")) {
                showRingtoneLyt();
            }
        } else if (requestCode == 102 && CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE") && this.mTempRingToneUri != null) {
            this.alarm.alert = this.mTempRingToneUri;
            Log.d("SetAlarm", "onActivityResult : uri = " + this.alarm.alert);
            this.mMarkUri = true;
            this.mFragment.updateRington(getRingStrFromUri(this.alarm.alert));
        }
    }

    public void showSaveDialog() {
        dismissDialog();
        this.mAlertDialog = UIUtils.createAlertDialog(this, getString(R.string.dialog_alarm_save), getString(R.string.set_alarm_save_context), R.string.dialog_alarm_save, R.string.dialog_alarm_discard, new DialogCallBack() {
            public void confirm() {
                ClockReporter.reportEventMessage(SetAlarm.this, 19, "");
                if (SetAlarm.this.mTimePicker != null) {
                    SetAlarm.this.mTimePicker.clearFocus();
                } else {
                    SetAlarm.this.mFragment.clearTimeFocus();
                }
                SetAlarm.this.saveAndExit();
            }

            public void cancel() {
                SetAlarm.this.exitForSelf(false);
                SetAlarm.this.setResult(0);
                SetAlarm.this.finish();
            }
        }, 0);
    }

    public void updateVibrate(boolean flag) {
        this.isUpdate = true;
        this.mBoolVibrate = flag;
    }

    public void updateRington() {
        ClockReporter.reportEventMessage(this, 24, "");
        if (CompatUtils.hasPermission(this, "android.permission.READ_EXTERNAL_STORAGE")) {
            showRingtoneLyt();
        } else {
            CompatUtils.grantPermissionsByManager(this, 101);
        }
    }

    public void updateRepeat() {
        ClockReporter.reportEventMessage(this, 21, "");
        showRepeatDialog();
    }

    public void updateLabel() {
        ClockReporter.reportEventMessage(this, 26, "");
        showLabelDialog();
    }

    public void updateDelete() {
        ClockReporter.reportEventMessage(this, 67, "");
        String message = getString(R.string.delete_alarm_confirm);
        dismissDialog();
        this.mAlertDialog = UIUtils.createAlertDialog(this, getString(R.string.delete), message, R.string.delete, 17039360, new DialogCallBack() {
            public void confirm() {
                ClockReporter.reportEventMessage(SetAlarm.this, 68, "");
                Alarms.deleteAlarm(DeskClockApplication.getDeskClockApplication(), SetAlarm.this.mId);
                SetAlarm.this.finish();
            }

            public void cancel() {
            }
        }, 1);
    }

    private void showRingtoneLyt() {
        this.isUpdate = true;
        if (!this.mChooseRingState) {
            Intent ringtoneIntent = new Intent("android.intent.action.RINGTONE_PICKER");
            ringtoneIntent.addCategory("android.intent.category.HWRING");
            if (getPackageManager().queryIntentActivities(ringtoneIntent, 0).size() > 0) {
                Intent intent = new Intent();
                intent.setAction("android.intent.action.RINGTONE_PICKER");
                intent.addCategory("android.intent.category.HWRING");
                intent.putExtra("android.intent.extra.ringtone.TYPE", 4);
                intent.putExtra("android.intent.extra.ringtone.EXISTING_URI", onRestoreRingtone());
                intent.putExtra("android.intent.extra.ringtone.SHOW_SILENT", true);
                intent.putExtra("android.intent.extra.ringtone.SHOW_DEFAULT", false);
                intent.putExtra("android.intent.extra.ringtone.INCLUDE_DRM", false);
                try {
                    startActivityForResult(intent, 0);
                    this.mChooseRingState = true;
                    return;
                } catch (Exception e) {
                    Log.e("SetAlarm", "onClick : Exception = " + e.getMessage());
                    return;
                }
            }
            String[] alarmRingtoneType = getResources().getStringArray(R.array.alarm_ringtone_filters);
            Builder builder = new Builder(this);
            builder.setTitle(R.string.alert);
            builder.setCancelable(true);
            builder.setItems(alarmRingtoneType, this.dialogInterface);
            builder.create().show();
        }
    }

    private void showRepeatDialog() {
        int wichSelect;
        this.isUpdate = true;
        if (Utils.isChinaRegionalVersion()) {
            if (this.mDaysOfWeekType > 1) {
                wichSelect = ((this.mDaysOfWeekType - 1) % 3) + 2;
            } else {
                wichSelect = this.mDaysOfWeekType;
            }
        } else if (this.mDaysOfWeekType == 4) {
            wichSelect = 1;
        } else {
            wichSelect = this.mDaysOfWeekType;
        }
        dismissDialog();
        this.mAlertDialog = AlarmSetDialogManager.getInstance().createSingleDialog(this, new SelectDialogCallBack() {
            public void confirm(String selectStr) {
            }

            public void cancel() {
            }

            public void confirm(SparseBooleanArray selectArray) {
            }

            public void confirm(String selectStr, ArrayList<Integer> arrayList, int selectId) {
                if (selectId == 4) {
                    ClockReporter.reportEventMessage(SetAlarm.this, 8, "");
                }
                SetAlarm.this.setDaysOfWeek(SetAlarm.this.getDaysFromIntent(arrayList));
                int resultType = SetAlarm.this.getResultType(SetAlarm.this.getDaysOfWeek().getCoded(), selectId);
                if (!(resultType == 1 || resultType == 2)) {
                    if (resultType == 0) {
                    }
                    SetAlarm.this.alarm.saveDaysOfWeekType(selectId);
                    SetAlarm.this.mDaysOfWeekType = selectId;
                    if (SetAlarm.this.alarm.isUserDefineRing()) {
                        SetAlarm.this.alarm.savedaysOfWeekShow(selectStr);
                        SetAlarm.this.mFragment.updateRepeat(selectStr);
                        return;
                    }
                    String daysOfWeekStr = AlarmSetDialogManager.toGogaleString(DeskClockApplication.getDeskClockApplication(), SetAlarm.this.getDaysOfWeek(), false);
                    SetAlarm.this.alarm.savedaysOfWeekShow(daysOfWeekStr);
                    SetAlarm.this.mFragment.updateRepeat(daysOfWeekStr);
                }
                selectId = resultType;
                selectStr = AlarmSetDialogManager.getRepeatTypeAll(DeskClockApplication.getDeskClockApplication(), resultType);
                SetAlarm.this.alarm.saveDaysOfWeekType(selectId);
                SetAlarm.this.mDaysOfWeekType = selectId;
                if (SetAlarm.this.alarm.isUserDefineRing()) {
                    SetAlarm.this.alarm.savedaysOfWeekShow(selectStr);
                    SetAlarm.this.mFragment.updateRepeat(selectStr);
                    return;
                }
                String daysOfWeekStr2 = AlarmSetDialogManager.toGogaleString(DeskClockApplication.getDeskClockApplication(), SetAlarm.this.getDaysOfWeek(), false);
                SetAlarm.this.alarm.savedaysOfWeekShow(daysOfWeekStr2);
                SetAlarm.this.mFragment.updateRepeat(daysOfWeekStr2);
            }

            public void confirm(int choice) {
            }
        }, wichSelect, this.mDaysOfWeek.getBooleanArray());
    }

    private void showLabelDialog() {
        this.isUpdate = true;
        dismissDialog();
        this.mAlertDialog = createEditextDialog(this, new SelectDialogCallBack() {
            public void confirm(String selectStr) {
                SetAlarm.this.mLableStr = selectStr;
                SetAlarm.this.mUpdateStr = selectStr;
                SetAlarm.this.mFragment.updateLabel(SetAlarm.this.mUpdateStr);
            }

            public void cancel() {
            }

            public void confirm(SparseBooleanArray selectArray) {
            }

            public void confirm(String selectStr, ArrayList<Integer> arrayList, int selectId) {
            }

            public void confirm(int choice) {
            }
        }, this.mLableStr);
        if (this.mAlertDialog != null) {
            this.mAlertDialog.show();
        }
    }

    protected void onStart() {
        super.onStart();
        Log.d("SetAlarm", "onStart");
    }

    protected void onRestart() {
        super.onRestart();
        Log.d("SetAlarm", "onRestart");
    }

    protected void onStop() {
        super.onStop();
        Log.d("SetAlarm", "onStop");
    }

    private void dismissDialog() {
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
        }
        this.mAlertDialog = null;
    }

    protected void onDestroy() {
        Log.d("SetAlarm", "onDestroy");
        if (this.isClickSave) {
            saveAndExit();
        }
        dismissDialog();
        AlarmSetDialogManager.getInstance().dismissDialog();
        super.onDestroy();
        this.mTimer.cancel();
        this.mTimer.purge();
        ReflexUtil.fixInputMethodManagerLeak(this);
        setObject2Null();
    }

    private DaysOfWeek getDaysFromIntent(ArrayList<Integer> days) {
        DaysOfWeek daysOfWeek = new DaysOfWeek(0);
        if (days != null) {
            int size = days.size();
            int[] daysArray = new int[size];
            for (int i = 0; i < size; i++) {
                daysArray[i] = ((Integer) days.get(i)).intValue();
            }
            daysOfWeek.setDaysOfWeek(true, daysArray);
            Log.d("SetAlarm", "getDaysFromIntent : not null week days = " + daysOfWeek.queryDaysOfWeekCode());
        }
        return daysOfWeek;
    }

    public Dialog createEditextDialog(SetAlarm context, final SelectDialogCallBack callBack, String lableStr) {
        SetAlarm localCtx = (SetAlarm) new WeakReference(context).get();
        if (localCtx == null) {
            return null;
        }
        AlertDialog textDialog = new Builder(localCtx).setIconAttribute(16843605).setTitle(R.string.label).setPositiveButton(17039370, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                ClockReporter.reportEventMessage(SetAlarm.this, 27, "");
                if (!(SetAlarm.this.editText == null || SetAlarm.this.editText.getText() == null || SetAlarm.this.editText.getText().toString() == null)) {
                    callBack.confirm(SetAlarm.this.editText.getText().toString().trim());
                }
                dialog.dismiss();
            }
        }).setNegativeButton(17039360, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ClockReporter.reportEventMessage(SetAlarm.this, 28, "");
            }
        }).create();
        View textEntryView = textDialog.getLayoutInflater().inflate(R.layout.alarm_dialog_text_entry, null);
        textDialog.setView(textEntryView);
        this.editText = (EditText) textEntryView.findViewById(R.id.username_edit);
        this.editText.setText(lableStr);
        this.editText.selectAll();
        this.editText.requestFocus();
        this.editText.addTextChangedListener(new LocalTextWatcher(this));
        openInputMethod();
        return textDialog;
    }

    public void openInputMethod() {
        this.mTimer.schedule(new LocalTimerTask(this), 270);
    }

    public void onBackPressed() {
        if (this.isUpdate) {
            showSaveDialog();
            return;
        }
        exitForSelf(false);
        setResult(0);
        finish();
    }

    private void exitForSelf(boolean isUpdate) {
        Intent intent = getIntent();
        boolean is_quickaction_type = false;
        if (intent != null) {
            is_quickaction_type = intent.getBooleanExtra("is_quickaction_type", false);
        }
        if (is_quickaction_type) {
            if (isUpdate) {
                Intent mainIntent = new Intent(this, AlarmsMainActivity.class);
                mainIntent.setFlags(268468224);
                mainIntent.putExtra("deskclock.select.tab", 0);
                startActivity(mainIntent);
            }
            ClockReporter.reportEventContainMessage(this, 75, "STARTWAY", 0);
        }
    }
}
