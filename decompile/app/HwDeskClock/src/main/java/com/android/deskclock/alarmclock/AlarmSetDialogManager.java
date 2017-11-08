package com.android.deskclock.alarmclock;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.content.SharedPreferences.Editor;
import android.util.SparseBooleanArray;
import com.android.deskclock.DeskClockApplication;
import com.android.deskclock.R;
import com.android.deskclock.ToastMaster;
import com.android.deskclock.alarmclock.Alarm.DaysOfWeek;
import com.android.util.ClockReporter;
import com.android.util.DayOfWeekRepeatUtil;
import com.android.util.Log;
import com.android.util.Utils;
import com.huawei.cust.HwCustUtils;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.LocaleData;

public class AlarmSetDialogManager {
    private static AlarmSetDialogManager sDialogManager;
    private boolean[] defaultSelected;
    private SparseBooleanArray isSelected = null;
    private AlertDialog mAlertDialog;

    public interface SelectDialogCallBack {
        void cancel();

        void confirm(int i);

        void confirm(SparseBooleanArray sparseBooleanArray);

        void confirm(String str);

        void confirm(String str, ArrayList<Integer> arrayList, int i);
    }

    public static class HwConfirmListener implements OnClickListener {
        private SelectDialogCallBack mCallback;
        private Context mContext;

        public HwConfirmListener(Context context, SelectDialogCallBack callback) {
            this.mContext = context;
            this.mCallback = callback;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            ClockReporter.reportEventContainMessage(this.mContext, 42, "VOLUME", whichButton);
            this.mCallback.confirm(whichButton);
            dialog.dismiss();
        }
    }

    static class LocalOnClickListener implements OnClickListener {
        private SelectDialogCallBack mCallBack;
        private int mFirstDayOfWeek;
        private int mIsOK;
        private SparseBooleanArray mIsSelected;
        private int mLength;
        private int mWhichBtn;
        private WeakReference<Context> wRfCtx;

        public LocalOnClickListener(Context context, SelectDialogCallBack callBack, int okorCancel, int length, int firstDayOfWeek, SparseBooleanArray isSelected) {
            this.wRfCtx = new WeakReference(context);
            this.mCallBack = callBack;
            this.mIsOK = okorCancel;
            this.mLength = length;
            this.mFirstDayOfWeek = firstDayOfWeek;
            this.mIsSelected = isSelected;
        }

        public LocalOnClickListener(Context context, SelectDialogCallBack callBack, int okorCancel, int whichBtn) {
            this.wRfCtx = new WeakReference(context);
            this.mCallBack = callBack;
            this.mIsOK = okorCancel;
            this.mWhichBtn = whichBtn;
        }

        public void onClick(DialogInterface dialog, int which) {
            Context setAlarm = (Context) this.wRfCtx.get();
            if (setAlarm == null) {
                return;
            }
            if (this.mIsOK == 1) {
                dialog.dismiss();
                SparseBooleanArray isSelectedNew = new SparseBooleanArray();
                for (int j = 0; j < this.mLength; j++) {
                    isSelectedNew.put((this.mFirstDayOfWeek + j) % 7, this.mIsSelected.get(j));
                }
                this.mCallBack.confirm(isSelectedNew);
            } else if (this.mIsOK == 0) {
                this.mCallBack.cancel();
                dialog.dismiss();
            } else if (this.mIsOK == 3) {
                ClockReporter.reportEventContainMessage(setAlarm, 36, "DURATION", which);
                this.mWhichBtn = which;
                this.mCallBack.confirm(this.mWhichBtn);
                dialog.dismiss();
            } else if (this.mIsOK == 2) {
                dialog.dismiss();
            }
        }
    }

    static class OnClickCancelListener implements OnClickListener {
        private Context mContext;
        private int mReportId;

        public OnClickCancelListener(Context context, int reportId) {
            this.mContext = context;
            this.mReportId = reportId;
        }

        public void onClick(DialogInterface dialog, int which) {
            ClockReporter.reportEventMessage(this.mContext, this.mReportId, "");
        }
    }

    private AlarmSetDialogManager() {
    }

    public static synchronized AlarmSetDialogManager getInstance() {
        AlarmSetDialogManager alarmSetDialogManager;
        synchronized (AlarmSetDialogManager.class) {
            if (sDialogManager == null) {
                sDialogManager = new AlarmSetDialogManager();
            }
            alarmSetDialogManager = sDialogManager;
        }
        return alarmSetDialogManager;
    }

    public static final String getRepeatType(Context context, int dayofWeektype) {
        String[] mRepeatArrays = context.getResources().getStringArray(R.array.List_alarmsetting_setrepeat);
        if (Utils.isChinaRegionalVersion() && DayOfWeekRepeatUtil.isHasWorkDayfn()) {
            if (dayofWeektype > 1) {
                dayofWeektype = ((dayofWeektype - 1) % 3) + 2;
            }
            if (dayofWeektype > 4 || dayofWeektype < 0) {
                dayofWeektype = 0;
            }
            return mRepeatArrays[dayofWeektype];
        }
        String[] newArrays = new String[4];
        int length = newArrays.length;
        for (int i = 0; i < length; i++) {
            if (i > 1) {
                newArrays[i] = mRepeatArrays[i + 1];
            } else {
                newArrays[i] = mRepeatArrays[i];
            }
        }
        if (dayofWeektype > 3 || dayofWeektype < 0) {
            if (dayofWeektype == 4) {
                dayofWeektype = 1;
            } else {
                dayofWeektype = 0;
            }
        }
        return newArrays[dayofWeektype];
    }

    public static final String getRepeatTypeAll(Context context, int dayofWeektype) {
        String[] mRepeatArrays = context.getResources().getStringArray(R.array.List_alarmsetting_setrepeat);
        if (dayofWeektype > 1) {
            dayofWeektype = ((dayofWeektype - 1) % 3) + 2;
        }
        if (dayofWeektype > 4 || dayofWeektype < 0) {
            dayofWeektype = 0;
        }
        return mRepeatArrays[dayofWeektype];
    }

    public static final String getRepeatTypeOfChina(Context context, int dayofWeektype) {
        if (Utils.isChinaRegionalVersion()) {
            return getRepeatTypeAll(context, dayofWeektype);
        }
        return getRepeatType(context, dayofWeektype);
    }

    private static final ArrayList<Integer> doRepeat(int whichButton) {
        ArrayList<Integer> weekdays;
        switch (whichButton) {
            case 0:
                return new ArrayList();
            case 1:
            case MetaballPath.POINT_NUM /*4*/:
                weekdays = new ArrayList();
                weekdays.add(Integer.valueOf(0));
                weekdays.add(Integer.valueOf(1));
                weekdays.add(Integer.valueOf(2));
                weekdays.add(Integer.valueOf(3));
                weekdays.add(Integer.valueOf(4));
                return weekdays;
            case 2:
            case 3:
                weekdays = new ArrayList();
                weekdays.add(Integer.valueOf(0));
                weekdays.add(Integer.valueOf(1));
                weekdays.add(Integer.valueOf(2));
                weekdays.add(Integer.valueOf(3));
                weekdays.add(Integer.valueOf(4));
                weekdays.add(Integer.valueOf(5));
                weekdays.add(Integer.valueOf(6));
                return weekdays;
            default:
                return null;
        }
    }

    public Dialog createSingleDialog(Context context, SelectDialogCallBack callBack, int whichBtn, boolean[] mDayOfWeeks) {
        final Context localCtx = (Context) new WeakReference(context).get();
        if (localCtx == null) {
            return null;
        }
        CharSequence[] newArrays;
        CharSequence[] selectItems = localCtx.getResources().getTextArray(R.array.List_alarmsetting_setrepeat);
        if (Utils.isChinaRegionalVersion()) {
            newArrays = selectItems;
        } else {
            newArrays = new CharSequence[4];
            int length = newArrays.length;
            for (int i = 0; i < length; i++) {
                if (i > 1) {
                    newArrays[i] = selectItems[i + 1];
                } else {
                    newArrays[i] = selectItems[i];
                }
            }
        }
        final Context context2 = context;
        final SelectDialogCallBack selectDialogCallBack = callBack;
        final boolean[] zArr = mDayOfWeeks;
        AlertDialog alertDialog = new Builder(localCtx).setIconAttribute(16843605).setTitle(R.string.alarm_repeat).setNegativeButton(17039360, new OnClickCancelListener(context, 23)).setSingleChoiceItems(newArrays, whichBtn, new OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                dialog.dismiss();
                ClockReporter.reportEventContainMessage(context2, 22, "REPEAT", whichButton);
                if (Utils.isChinaRegionalVersion() && whichButton > 1) {
                    whichButton = (whichButton % 3) + 2;
                }
                final int typeWich = whichButton;
                final Context context;
                if (whichButton == 4) {
                    if (DayOfWeekRepeatUtil.isHasWorkDayfn()) {
                        selectDialogCallBack.confirm(AlarmSetDialogManager.getRepeatTypeAll(localCtx, typeWich), AlarmSetDialogManager.doRepeat(typeWich), typeWich);
                    } else if (Utils.getDefaultSharedPreferences(localCtx).getBoolean("mark_has_jump_calendar", false)) {
                        if (Utils.checkNetworkStatus(localCtx) < 0) {
                            ToastMaster.showToast(localCtx, (int) R.string.toast_network_disable_hint_Toast, 1);
                        } else {
                            DayOfWeekRepeatUtil.getCalendarWorldData(localCtx);
                            selectDialogCallBack.confirm(AlarmSetDialogManager.getRepeatTypeAll(localCtx, typeWich), AlarmSetDialogManager.doRepeat(typeWich), typeWich);
                        }
                    } else {
                        AlarmSetDialogManager.this.dismissDialog();
                        AlarmSetDialogManager alarmSetDialogManager = AlarmSetDialogManager.this;
                        Builder negativeButton = new Builder(localCtx).setIconAttribute(16843605).setNegativeButton(R.string.alarm_wokeday_no, null);
                        final Context context2 = context2;
                        context = localCtx;
                        final SelectDialogCallBack selectDialogCallBack = selectDialogCallBack;
                        alarmSetDialogManager.mAlertDialog = negativeButton.setPositiveButton(R.string.alarm_wokeday_ok, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Editor editor = Utils.getDefaultSharedPreferences(context2).edit();
                                editor.putBoolean("mark_has_jump_calendar", true);
                                editor.commit();
                                if (!DayOfWeekRepeatUtil.isHasWorkDayfn()) {
                                    if (Utils.checkNetworkStatus(context) < 0) {
                                        ToastMaster.showToast(context, (int) R.string.toast_network_disable_hint_Toast, 1);
                                        return;
                                    }
                                    selectDialogCallBack.confirm(AlarmSetDialogManager.getRepeatTypeAll(context2, typeWich), AlarmSetDialogManager.doRepeat(typeWich), typeWich);
                                    DayOfWeekRepeatUtil.getCalendarWorldData(context);
                                }
                            }
                        }).create();
                        if (AlarmSetDialogManager.this.mAlertDialog != null) {
                            AlarmSetDialogManager.this.mAlertDialog.setView(AlarmSetDialogManager.this.mAlertDialog.getLayoutInflater().inflate(R.layout.alarm_dialog_network_tips, null));
                            AlarmSetDialogManager.this.mAlertDialog.show();
                        }
                    }
                } else if (whichButton == 3) {
                    int i;
                    AlarmSetDialogManager.this.isSelected = new SparseBooleanArray();
                    for (i = 0; i < 7; i++) {
                        AlarmSetDialogManager.this.isSelected.put(i, false);
                    }
                    AlarmSetDialogManager.this.defaultSelected = new boolean[7];
                    for (i = 0; i < 7; i++) {
                        AlarmSetDialogManager.this.defaultSelected[i] = AlarmSetDialogManager.this.isSelected.get(i);
                    }
                    AlarmSetDialogManager.this.dismissDialog();
                    AlarmSetDialogManager alarmSetDialogManager2 = AlarmSetDialogManager.this;
                    AlarmSetDialogManager alarmSetDialogManager3 = AlarmSetDialogManager.this;
                    boolean[] zArr = zArr;
                    context = localCtx;
                    final SelectDialogCallBack selectDialogCallBack2 = selectDialogCallBack;
                    alarmSetDialogManager2.mAlertDialog = (AlertDialog) alarmSetDialogManager3.createMultBtnDialog(zArr, context, new SelectDialogCallBack() {
                        public void cancel() {
                        }

                        public void confirm(String selectStr) {
                        }

                        public void confirm(SparseBooleanArray selectArray) {
                            ArrayList<Integer> weekdays = new ArrayList();
                            for (int i = 0; i < selectArray.size(); i++) {
                                if (selectArray.get(i)) {
                                    weekdays.add(Integer.valueOf(i));
                                }
                            }
                            selectDialogCallBack2.confirm(AlarmSetDialogManager.getRepeatTypeAll(DeskClockApplication.getDeskClockApplication(), typeWich), weekdays, typeWich);
                        }

                        public void confirm(String selectStr, ArrayList<Integer> arrayList, int selectId) {
                        }

                        public void confirm(int choice) {
                        }
                    });
                } else {
                    selectDialogCallBack.confirm(AlarmSetDialogManager.getRepeatType(context2, whichButton), AlarmSetDialogManager.doRepeat(whichButton), whichButton);
                }
            }
        }).create();
        alertDialog.show();
        return alertDialog;
    }

    public static final String toGogaleString(Context context, DaysOfWeek daysOfWeek, boolean showNever) {
        int mDays = daysOfWeek.getCoded();
        if (mDays == 0) {
            return showNever ? context.getText(R.string.never).toString() : "";
        }
        int i;
        int dayCount = 0;
        for (int days = mDays; days > 0; days >>= 1) {
            if ((days & 1) == 1) {
                dayCount++;
            }
        }
        StringBuilder ret = new StringBuilder();
        boolean[] mDayOfWeeks = daysOfWeek.getBooleanArray();
        int firstDayOfWeek = (Calendar.getInstance().getFirstDayOfWeek() + 5) % 7;
        HwCustWeekStartDayController controller = (HwCustWeekStartDayController) HwCustUtils.createObj(HwCustWeekStartDayController.class, new Object[0]);
        if (controller != null) {
            firstDayOfWeek = controller.getFirstDayOfWeekFromDB(context, firstDayOfWeek);
        }
        Log.i("AlarmSetDialogManager", "init : Colck firstDayOfWeek = " + firstDayOfWeek + "  Calendar.getInstance().getFirstDayOfWeek() = " + Calendar.getInstance().getFirstDayOfWeek());
        String[] shortWeekdaytemp = LocaleData.get(Locale.getDefault()).shortStandAloneWeekdayNames;
        String[] shortWeekdays = new String[7];
        for (i = 1; i < shortWeekdaytemp.length; i++) {
            shortWeekdays[(i + 5) % 7] = shortWeekdaytemp[i];
        }
        String[] mRepeatArraysNew = new String[7];
        boolean[] mDayOfWeeksNew = new boolean[7];
        for (int j = 0; j < mDayOfWeeks.length; j++) {
            int week = (firstDayOfWeek + j) % 7;
            mRepeatArraysNew[j] = shortWeekdays[week];
            mDayOfWeeksNew[j] = mDayOfWeeks[week];
        }
        int count = mDayOfWeeksNew.length;
        for (i = 0; i < count; i++) {
            if (mDayOfWeeksNew[i]) {
                ret.append(mRepeatArraysNew[i]);
                dayCount--;
                if (dayCount > 0) {
                    ret.append(" ");
                }
            }
        }
        return ret.toString();
    }

    public void dismissDialog() {
        if (this.mAlertDialog != null && this.mAlertDialog.isShowing()) {
            this.mAlertDialog.dismiss();
        }
        this.mAlertDialog = null;
    }

    public Dialog createMultBtnDialog(boolean[] mDayOfWeeks, Context context, SelectDialogCallBack callBack) {
        Context localCtx = (Context) new WeakReference(context).get();
        if (localCtx == null) {
            return null;
        }
        int firstDayOfWeek = (Calendar.getInstance().getFirstDayOfWeek() + 5) % 7;
        Log.i("AlarmSetDialogManager", "init : Colck firstDayOfWeek = " + firstDayOfWeek + "  Calendar.getInstance().getFirstDayOfWeek() = " + Calendar.getInstance().getFirstDayOfWeek());
        String[] mRepeatArrays = context.getResources().getStringArray(R.array.alarm_set_select_mult_dialog_items);
        HwCustWeekStartDayController controller = (HwCustWeekStartDayController) HwCustUtils.createObj(HwCustWeekStartDayController.class, new Object[0]);
        boolean baseFlow = true;
        if (controller != null) {
            if (controller.handleCustomWeekStartDay(this.isSelected, mDayOfWeeks)) {
                baseFlow = false;
            } else {
                firstDayOfWeek = controller.getFirstDayOfWeekFromDB(context, firstDayOfWeek);
            }
        }
        String[] mRepeatArraysNew = new String[7];
        boolean[] mDayOfWeeksNew = new boolean[7];
        if (baseFlow) {
            for (int j = 0; j < mDayOfWeeks.length; j++) {
                int week = (firstDayOfWeek + j) % 7;
                mRepeatArraysNew[j] = mRepeatArrays[week];
                mDayOfWeeksNew[j] = mDayOfWeeks[week];
            }
            int count = mDayOfWeeksNew.length;
            for (int i = 0; i < count; i++) {
                this.isSelected.put(i, mDayOfWeeksNew[i]);
            }
        } else {
            mRepeatArraysNew = mRepeatArrays;
            mDayOfWeeksNew = mDayOfWeeks;
        }
        Builder multiChoiceItems = new Builder(localCtx).setTitle(R.string.Title_alarm_definerepeat).setMultiChoiceItems(mRepeatArraysNew, mDayOfWeeksNew, new OnMultiChoiceClickListener() {
            public void onClick(DialogInterface dialog, int whichButton, boolean isChecked) {
                AlarmSetDialogManager.this.isSelected.put(whichButton, isChecked);
            }
        });
        return multiChoiceItems.setPositiveButton(17039370, new LocalOnClickListener(localCtx, callBack, 1, mDayOfWeeks.length, firstDayOfWeek, this.isSelected)).setNegativeButton(17039360, null).show();
    }

    public Dialog createSingleDialogSetting(Context context, int whichButton, SelectDialogCallBack callBack) {
        Context localCtx = (Context) new WeakReference(context).get();
        if (localCtx == null) {
            return null;
        }
        return new Builder(localCtx).setTitle(R.string.volume_btn_fuc_tittle).setSingleChoiceItems(R.array.volume_button_setting_entries, whichButton, new HwConfirmListener(context, callBack)).setNegativeButton(17039360, new OnClickCancelListener(context, 43)).create();
    }

    public String[] getBellTimeLengthStrings(Context context) {
        int[] alertTime = new int[]{1, 5, 10, 15, 20, 30};
        String[] alertTimeStr = new String[alertTime.length];
        for (int i = 0; i < alertTimeStr.length; i++) {
            alertTimeStr[i] = context.getResources().getQuantityString(R.plurals.minutes, alertTime[i], new Object[]{Integer.valueOf(alertTime[i])});
        }
        return alertTimeStr;
    }

    public Dialog createBellLengthSDg(Context context, int whichButton, SelectDialogCallBack callBack) {
        Context localCtx = (Context) new WeakReference(context).get();
        if (localCtx == null) {
            return null;
        }
        return new Builder(localCtx).setTitle(R.string.bell_length_setting_title).setSingleChoiceItems(getBellTimeLengthStrings(localCtx), whichButton, new LocalOnClickListener(localCtx, callBack, 3, whichButton)).setNegativeButton(17039360, new OnClickCancelListener(context, 37)).create();
    }
}
