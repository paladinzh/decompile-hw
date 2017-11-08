package com.android.contacts.datepicker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.text.TextUtils.TruncateAt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.TextView;
import com.android.contacts.datepicker.DatePicker.OnDateChangedListener;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.LunarUtils;
import com.google.android.gms.R;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class DatePickerDialog extends AlertDialog implements OnClickListener, OnDateChangedListener {
    private static DatePickerDialog mDialog;
    private boolean isLunarBirthday;
    private final Calendar mCalendar;
    private final OnDateSetListener mCallBack;
    private final DatePicker mDatePicker;
    private String mDialogTitle;
    private SimpleDateFormat mdateFormatWithoutYear;

    public interface OnDateSetListener {
        void onDateSet(DatePicker datePicker, int i, int i2, int i3);
    }

    public DatePickerDialog(Context context, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean yearOptional, boolean lunarBirthday) {
        int i;
        if (context.getApplicationInfo().targetSdkVersion >= 11) {
            i = 16974982;
        } else {
            i = 16974972;
        }
        this(context, i, callBack, year, monthOfYear, dayOfMonth, yearOptional, lunarBirthday);
    }

    public DatePickerDialog(Context context, int theme, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth, boolean yearOptional, boolean lunarBirthday) {
        super(context);
        this.mCallBack = callBack;
        this.mdateFormatWithoutYear = DateUtils.getNoYearDateFormat();
        this.isLunarBirthday = lunarBirthday;
        this.mCalendar = Calendar.getInstance();
        setButton(-1, context.getText(R.string.menu_done), this);
        setButton(-2, context.getText(17039360), (OnClickListener) null);
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.date_picker_dialog, null);
        setView(view);
        this.mDatePicker = (DatePicker) view.findViewById(R.id.datePicker);
        this.mDatePicker.init(year, monthOfYear, dayOfMonth, yearOptional, this.isLunarBirthday, this);
        updateTitle(year, monthOfYear, dayOfMonth, year != 0);
    }

    public void show() {
        super.show();
        try {
            View view = findViewById(16909084);
            if (view != null && (view instanceof TextView)) {
                TextView title = (TextView) view;
                title.setSingleLine();
                title.setEllipsize(TruncateAt.END);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (this.mCallBack != null) {
            this.mDatePicker.clearFocus();
            this.mCallBack.onDateSet(this.mDatePicker, this.mDatePicker.getYear(), this.mDatePicker.getMonth(), this.mDatePicker.getDayOfMonth());
        }
    }

    public void onDateChanged(DatePicker view, int year, int month, int day, boolean yearToggle) {
        updateTitle(year, month, day, yearToggle);
        if (AccessibilityManager.getInstance(getContext()).isEnabled()) {
            this.mDatePicker.requestAccessibilityFocus();
            this.mDatePicker.setContentDescription(this.mDialogTitle);
            this.mDatePicker.sendAccessibilityEvent(4);
            this.mDatePicker.clearFocus();
            this.mDatePicker.setContentDescription(null);
        }
    }

    private void updateTitle(int year, int month, int day, boolean yearToggle) {
        String titleDisplay;
        this.mCalendar.set(2, month);
        this.mCalendar.set(5, day);
        int flags = DateUtils.getDefaultDateFormat(false);
        if (this.isLunarBirthday) {
            titleDisplay = LunarUtils.updateLunarTitle(getContext(), year, month + 1, day, true);
        } else if (yearToggle) {
            flags = DateUtils.getDefaultDateFormat(true);
            this.mCalendar.set(1, year);
            titleDisplay = android.text.format.DateUtils.formatDateTime(getContext(), this.mCalendar.getTimeInMillis(), flags);
        } else if (!yearToggle && month == 1 && day == 29) {
            titleDisplay = DateUtils.formatDate(getContext(), "--02-29");
        } else if (this.mdateFormatWithoutYear != null) {
            titleDisplay = DateUtils.formatDate(getContext(), this.mdateFormatWithoutYear.format(this.mCalendar.getTime()));
        } else {
            titleDisplay = android.text.format.DateUtils.formatDateTime(getContext(), this.mCalendar.getTimeInMillis(), flags);
        }
        this.mDialogTitle = titleDisplay;
        setTitle(titleDisplay);
    }

    public Bundle onSaveInstanceState() {
        Bundle state = super.onSaveInstanceState();
        state.putInt("year", this.mDatePicker.getYear());
        state.putInt("month", this.mDatePicker.getMonth());
        state.putInt("day", this.mDatePicker.getDayOfMonth());
        state.putBoolean("year_optional", this.mDatePicker.hasYear());
        state.putBoolean("is_lunar_birthday", this.isLunarBirthday);
        return state;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        int year = savedInstanceState.getInt("year");
        int month = savedInstanceState.getInt("month");
        int day = savedInstanceState.getInt("day");
        boolean yearOptional = savedInstanceState.getBoolean("year_optional");
        this.mDatePicker.init(year, month, day, this.mDatePicker.isYearOptional(), this.isLunarBirthday, this);
        updateTitle(year, month, day, yearOptional);
    }

    public static void setDatePickerDialog(DatePickerDialog dialog) {
        mDialog = dialog;
    }

    public static DatePickerDialog getDatePickerDialog() {
        return mDialog;
    }
}
