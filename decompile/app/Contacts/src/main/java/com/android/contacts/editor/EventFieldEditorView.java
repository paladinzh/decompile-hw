package com.android.contacts.editor;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import com.amap.api.services.core.AMapException;
import com.android.contacts.datepicker.DatePicker;
import com.android.contacts.datepicker.DatePickerDialog;
import com.android.contacts.datepicker.DatePickerDialog.OnDateSetListener;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType.EditField;
import com.android.contacts.model.account.AccountType.EventEditType;
import com.android.contacts.model.dataitem.DataKind;
import com.android.contacts.util.DateUtils;
import com.android.contacts.util.LunarUtils;
import com.google.android.gms.R;
import java.text.ParsePosition;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class EventFieldEditorView extends LabeledEditorView {
    private Boolean isLunarBirthday;
    private Button mDateView;
    private String mNoDateString;

    public EventFieldEditorView(Context context) {
        super(context);
    }

    public EventFieldEditorView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public EventFieldEditorView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mNoDateString = getContext().getString(R.string.event_edit_field_hint_text);
        this.mDateView = (Button) findViewById(R.id.contact_date_view);
        this.mDateView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                EventFieldEditorView.this.setIfDataPickerShouldBeDisplayed(true);
                EventFieldEditorView.this.showDialog(R.id.dialog_event_date_picker, null);
            }
        });
    }

    public void editNewlyAddedField() {
        showDialog(R.id.dialog_event_date_picker, null);
    }

    protected void requestFocusForFirstEditField() {
        this.mDateView.requestFocus();
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        Button button = this.mDateView;
        if (isReadOnly()) {
            enabled = false;
        }
        button.setEnabled(enabled);
    }

    public void setValues(DataKind kind, ValuesDelta entry, RawContactDelta state, boolean readOnly, ViewIdGenerator vig) {
        boolean z = false;
        if (kind.fieldList.size() != 1) {
            throw new IllegalStateException("kind must have 1 field");
        }
        super.setValues(kind, entry, state, readOnly, vig);
        Button button = this.mDateView;
        if (isEnabled() && !readOnly) {
            z = true;
        }
        button.setEnabled(z);
        rebuildDateView();
    }

    private void rebuildDateView() {
        String column = ((EditField) getKind().fieldList.get(0)).column;
        String data = getEntry().getAsString(column);
        boolean noYearParsed = LunarUtils.hasYear(getContext(), data);
        if (noYearParsed) {
            data = LunarUtils.getCurrentYear() + data.substring(1, data.length());
        }
        String newData = data;
        if (getType() == null || getType().rawValue != 4) {
            this.isLunarBirthday = Boolean.valueOf(false);
            data = DateUtils.formatDate(getContext(), data);
        } else {
            this.isLunarBirthday = Boolean.valueOf(true);
            if (noYearParsed) {
                newData = LunarUtils.solarToLunar(data);
            }
        }
        if (noYearParsed) {
            onFieldChanged(column, newData, null);
        }
        if (TextUtils.isEmpty(data)) {
            this.mDateView.setText(this.mNoDateString);
            setDeleteButtonVisible(false);
            return;
        }
        if (this.isLunarBirthday.booleanValue()) {
            data = LunarUtils.titleSolarToLunar(getContext(), data);
        }
        this.mDateView.setText(data);
        this.mDateView.setTextColor(getResources().getColor(R.color.contact_eidtor_item_content_color));
        setDeleteButtonVisible(true);
    }

    public boolean isEmpty() {
        return TextUtils.isEmpty(getEntry().getAsString(((EditField) getKind().fieldList.get(0)).column));
    }

    public Dialog createDialog(Bundle bundle) {
        if (bundle == null) {
            throw new IllegalArgumentException("bundle must not be null");
        }
        switch (bundle.getInt("dialog_id")) {
            case R.id.dialog_event_date_picker:
                return createDatePickerDialog();
            default:
                return super.createDialog(bundle);
        }
    }

    protected EventEditType getType() {
        return (EventEditType) super.getType();
    }

    protected void onLabelRebuilt() {
        String column = ((EditField) getKind().fieldList.get(0)).column;
        String oldValue = getEntry().getAsString(column);
        boolean noYearParsed = LunarUtils.hasYear(getContext(), getEntry().getAsString(column));
        if (!(getType().rawValue != 4 || noYearParsed || TextUtils.isEmpty(oldValue))) {
            String newValue = LunarUtils.getValidityLunarDate(oldValue);
            if (!newValue.equals(oldValue)) {
                saveValue(column, newValue);
            }
        }
        DataKind kind = getKind();
        Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
        int defaultYear = calendar.get(1);
        getKind().typeToSelect = getType().rawValue;
        boolean isYearOptional = getType().isYearOptional();
        if (!isYearOptional && !TextUtils.isEmpty(oldValue)) {
            Date date2 = kind.dateFormatWithoutYear.parse(oldValue, new ParsePosition(0));
            if (date2 != null) {
                calendar.setTime(date2);
                calendar.set(defaultYear, calendar.get(2), calendar.get(5), 8, 0, 0);
                onFieldChanged(column, kind.dateFormatWithYear.format(calendar.getTime()), null);
                rebuildDateView();
            }
        } else if (isYearOptional && !TextUtils.isEmpty(oldValue)) {
            rebuildDateView();
        }
    }

    private Dialog createDatePickerDialog() {
        final String column = ((EditField) getKind().fieldList.get(0)).column;
        String oldValue = getEntry().getAsString(column);
        this.isLunarBirthday = Boolean.valueOf(getType().rawValue == 4);
        if (!(!this.isLunarBirthday.booleanValue() || TextUtils.isEmpty(oldValue) || oldValue.length() == 0)) {
            if (LunarUtils.hasYear(getContext(), oldValue)) {
                oldValue = LunarUtils.getCurrentYear() + oldValue.substring(1, oldValue.length());
            }
            oldValue = LunarUtils.solarToLunar(oldValue);
        }
        DataKind kind = getKind();
        Calendar calendar = Calendar.getInstance(DateUtils.UTC_TIMEZONE, Locale.US);
        int defaultYear = calendar.get(1);
        int defaultMonth = calendar.get(2);
        int defaultDay = calendar.get(5);
        if (getType() == null) {
            return null;
        }
        int oldYear;
        int oldMonth;
        int oldDay;
        final boolean isYearOptional = getType().isYearOptional();
        int[] time;
        if (TextUtils.isEmpty(oldValue)) {
            if (this.isLunarBirthday.booleanValue()) {
                if (defaultYear > 2035) {
                    defaultYear = 2035;
                    defaultMonth = 0;
                    defaultDay = 1;
                } else if (defaultYear < AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION) {
                    defaultYear = AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION;
                    defaultMonth = 0;
                    defaultDay = 1;
                } else {
                    time = LunarUtils.subFormat(LunarUtils.solarToLunar(LunarUtils.getValidityLunarDate(LunarUtils.addFormat(defaultYear, defaultMonth + 1, defaultDay))));
                    if (time == null) {
                        return null;
                    }
                    defaultYear = time[0];
                    defaultMonth = time[1];
                    defaultDay = time[2];
                }
            } else if (defaultYear > 2037) {
                defaultYear = 2037;
            } else if (defaultYear < AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR) {
                defaultYear = AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR;
            }
            oldYear = defaultYear;
            oldMonth = defaultMonth;
            oldDay = defaultDay;
        } else {
            time = this.isLunarBirthday.booleanValue() ? LunarUtils.subFormat(oldValue) : LunarUtils.subSolarFormat(oldValue);
            if (time == null) {
                return null;
            }
            oldYear = time[0];
            oldMonth = time[1];
            oldDay = time[2];
        }
        final DataKind dataKind = kind;
        DatePickerDialog resultDialog = new DatePickerDialog(getContext(), new OnDateSetListener() {
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                if (year != 0 || isYearOptional) {
                    dataKind.typeToSelect = -1;
                    EventFieldEditorView.this.onFieldChanged(column, EventFieldEditorView.this.lunarDateFormat(year, monthOfYear, dayOfMonth), null);
                    EventFieldEditorView.this.rebuildDateView();
                    return;
                }
                throw new IllegalStateException();
            }
        }, oldYear, oldMonth, oldDay, isYearOptional, this.isLunarBirthday.booleanValue());
        DatePickerDialog.setDatePickerDialog(resultDialog);
        return resultDialog;
    }

    private String lunarDateFormat(int year, int monthOfYear, int dayOfMonth) {
        if (getType().rawValue == 4) {
            if (year == 0) {
                year = LunarUtils.getCurrentYear();
            }
            return LunarUtils.lunarToSolar(LunarUtils.addFormat(year, monthOfYear + 1, dayOfMonth));
        }
        if (year == 0) {
            year = AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST;
        }
        return LunarUtils.addFormat(year, monthOfYear + 1, dayOfMonth);
    }

    public static int getDefaultHourForBirthday() {
        return 8;
    }

    public void clearAllFields() {
        this.mDateView.setText(this.mNoDateString);
        onFieldChanged(((EditField) getKind().fieldList.get(0)).column, "", null);
    }
}
