package com.android.contacts.datepicker;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.text.format.DateFormat;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View.BaseSavedState;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.NumberPicker.OnValueChangeListener;
import com.amap.api.services.core.AMapException;
import com.android.contacts.util.LunarDate;
import com.android.contacts.util.LunarUtils;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Locale;
import libcore.icu.ICU;
import libcore.icu.LocaleData;

public class DatePicker extends FrameLayout {
    private boolean isLunarBirthday;
    private Calendar mCurrentDate;
    private Locale mCurrentLocale;
    private int mDay;
    private String mDayFormat;
    private final AdvancedNumberPicker mDayPicker;
    private boolean mHasYear;
    private Calendar mMaxDate;
    private Calendar mMinDate;
    private int mMonth;
    private final AdvancedNumberPicker mMonthPicker;
    private OnDateChangedListener mOnDateChangedListener;
    private final LinearLayout mPickerContainer;
    private String[] mShortMonths;
    public Calendar mTempDate;
    private String[] mTextDays;
    private String[] mTextMonths;
    private String[] mTextYears;
    private int mYear;
    private String mYearFormat;
    private boolean mYearOptional;
    private final AdvancedNumberPicker mYearPicker;

    public interface OnDateChangedListener {
        void onDateChanged(DatePicker datePicker, int i, int i2, int i3, boolean z);
    }

    private static class SavedState extends BaseSavedState {
        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
        private final int mDay;
        private final boolean mHasYear;
        private final int mMonth;
        private final int mYear;
        private final boolean mYearOptional;

        private SavedState(Parcelable superState, int year, int month, int day, boolean hasYear, boolean yearOptional) {
            super(superState);
            this.mYear = year;
            this.mMonth = month;
            this.mDay = day;
            this.mHasYear = hasYear;
            this.mYearOptional = yearOptional;
        }

        private SavedState(Parcel in) {
            boolean z;
            boolean z2 = true;
            super(in);
            this.mYear = in.readInt();
            this.mMonth = in.readInt();
            this.mDay = in.readInt();
            if (in.readInt() != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mHasYear = z;
            if (in.readInt() == 0) {
                z2 = false;
            }
            this.mYearOptional = z2;
        }

        public int getYear() {
            return this.mYear;
        }

        public int getMonth() {
            return this.mMonth;
        }

        public int getDay() {
            return this.mDay;
        }

        public boolean hasYear() {
            return this.mHasYear;
        }

        public boolean isYearOptional() {
            return this.mYearOptional;
        }

        public void writeToParcel(Parcel dest, int flags) {
            int i;
            int i2 = 1;
            super.writeToParcel(dest, flags);
            dest.writeInt(this.mYear);
            dest.writeInt(this.mMonth);
            dest.writeInt(this.mDay);
            if (this.mHasYear) {
                i = 1;
            } else {
                i = 0;
            }
            dest.writeInt(i);
            if (!this.mYearOptional) {
                i2 = 0;
            }
            dest.writeInt(i2);
        }
    }

    public DatePicker(Context context) {
        this(context, null);
    }

    public DatePicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DatePicker(Context context, AttributeSet attrs, int defStyle) {
        int currentYear;
        super(context, attrs, defStyle);
        this.mYearFormat = "yyyy";
        this.mDayFormat = "d";
        setCurrentLocale(Locale.getDefault());
        int themeID = context.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        Context contextThemeWrapper;
        if (themeID <= 0) {
            contextThemeWrapper = new ContextThemeWrapper(getContext(), 16974123);
        } else {
            contextThemeWrapper = new ContextThemeWrapper(getContext(), themeID);
        }
        ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.date_picker, this, true);
        if (this.mYear == 0) {
            currentYear = LunarUtils.getCurrentYear();
        } else {
            currentYear = this.mYear;
        }
        LunarUtils.init(context, currentYear);
        this.mPickerContainer = (LinearLayout) findViewById(R.id.parent);
        this.mDayPicker = (AdvancedNumberPicker) findViewById(R.id.day);
        this.mDayPicker.setFormatter(AdvancedNumberPicker.TWO_DIGIT_FORMATTER);
        this.mDayPicker.setOnLongPressUpdateInterval(100);
        this.mDayPicker.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                DatePicker.this.mDay = newVal;
                DatePicker.this.notifyDateChanged();
            }
        });
        this.mMonthPicker = (AdvancedNumberPicker) findViewById(R.id.month);
        this.mMonthPicker.setFormatter(AdvancedNumberPicker.TWO_DIGIT_FORMATTER);
        String[] months = LocaleData.get(Locale.getDefault()).shortStandAloneMonthNames;
        if (this.isLunarBirthday) {
            if (this.mYear == 0) {
                currentYear = LunarUtils.getCurrentYear();
            } else {
                currentYear = this.mYear;
            }
            int lunarMonthIndex = LunarDate.getLeapMonthOfLunar(currentYear);
            int max = months.length;
            if (lunarMonthIndex == 0) {
                max--;
            }
            this.mMonthPicker.setMinValue(1);
            this.mMonthPicker.setMaxValue(max);
        } else if (months[0].startsWith(CallInterceptDetails.BRANDED_STATE)) {
            for (int i = 0; i < months.length; i++) {
                months[i] = String.valueOf(i + 1);
            }
            this.mMonthPicker.setMinValue(1);
            this.mMonthPicker.setMaxValue(12);
        } else {
            this.mMonthPicker.setMinValue(1);
            this.mMonthPicker.setMaxValue(12);
            this.mMonthPicker.setDisplayedValues(months);
        }
        this.mMonthPicker.setOnLongPressUpdateInterval(200);
        this.mMonthPicker.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                DatePicker.this.mMonth = newVal - 1;
                DatePicker.this.adjustMaxDay();
                DatePicker.this.notifyDateChanged();
                DatePicker.this.updateDaySpinner();
            }
        });
        this.mYearPicker = (AdvancedNumberPicker) findViewById(R.id.year);
        this.mYearPicker.setOnLongPressUpdateInterval(100);
        this.mYearPicker.setOnValueChangedListener(new OnValueChangeListener() {
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if (DatePicker.this.isLunarBirthday) {
                    DatePicker.this.adjustMonthIndex(DatePicker.this.mYear, newVal);
                }
                DatePicker.this.mYear = newVal;
                DatePicker.this.updateMonthSpinner();
                DatePicker.this.adjustMaxDay();
                DatePicker.this.notifyDateChanged();
                DatePicker.this.updateDaySpinner();
            }
        });
        this.mDayPicker.addFireList(this.mMonthPicker);
        this.mDayPicker.addFireList(this.mYearPicker);
        this.mMonthPicker.addFireList(this.mDayPicker);
        this.mMonthPicker.addFireList(this.mYearPicker);
        this.mYearPicker.addFireList(this.mDayPicker);
        this.mYearPicker.addFireList(this.mMonthPicker);
        this.mDayPicker.setDescendantFocusability(393216);
        this.mMonthPicker.setDescendantFocusability(393216);
        this.mYearPicker.setDescendantFocusability(393216);
        TypedArray a = context.obtainStyledAttributes(attrs, com.android.internal.R.styleable.DatePicker);
        int mStartYear = a.getInt(1, AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR);
        int mEndYear = a.getInt(2, 2037);
        if (this.isLunarBirthday) {
            mStartYear = AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION;
            mEndYear = 2035;
        }
        this.mYearPicker.setMinValue(mStartYear);
        this.mYearPicker.setMaxValue(mEndYear);
        a.recycle();
        Calendar cal = Calendar.getInstance();
        init(cal.get(1), cal.get(2), cal.get(5), null);
        reorderPickers();
        this.mPickerContainer.setLayoutTransition(new LayoutTransition());
        if (!isEnabled()) {
            setEnabled(false);
        }
    }

    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        this.mDayPicker.setEnabled(enabled);
        this.mMonthPicker.setEnabled(enabled);
        this.mYearPicker.setEnabled(enabled);
    }

    private void reorderPickers() {
        this.mPickerContainer.removeAllViews();
        String pattern = ICU.getBestDateTimePattern("yyyyMMMdd", Locale.getDefault());
        String language = Locale.getDefault().getLanguage();
        if ("ar".equals(language) || "fa".equals(language) || "iw".equals(language) || "ur".equals(language)) {
            pattern = "yyyyMMMdd";
        }
        char[] order = ICU.getDateFormatOrder(pattern);
        for (char c : order) {
            switch (c) {
                case Place.TYPE_POST_OFFICE /*77*/:
                    this.mPickerContainer.addView(this.mMonthPicker);
                    break;
                case LocationRequest.PRIORITY_HIGH_ACCURACY /*100*/:
                    this.mPickerContainer.addView(this.mDayPicker);
                    break;
                case 'y':
                    this.mPickerContainer.addView(this.mYearPicker);
                    break;
                default:
                    throw new IllegalArgumentException(Arrays.toString(order));
            }
        }
    }

    private int getCurrentYear() {
        return Calendar.getInstance().get(1);
    }

    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    protected Parcelable onSaveInstanceState() {
        return new SavedState(super.onSaveInstanceState(), this.mYear, this.mMonth, this.mDay, this.mHasYear, this.mYearOptional);
    }

    protected void onRestoreInstanceState(Parcelable state) {
        SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(ss.getSuperState());
        this.mYear = ss.getYear();
        this.mMonth = ss.getMonth();
        this.mDay = ss.getDay();
        this.mHasYear = ss.hasYear();
        this.mYearOptional = ss.isYearOptional();
        updateSpinners();
    }

    public void init(int year, int monthOfYear, int dayOfMonth, OnDateChangedListener onDateChangedListener) {
        init(year, monthOfYear, dayOfMonth, false, false, onDateChangedListener);
    }

    public void init(int year, int monthOfYear, int dayOfMonth, boolean yearOptional, boolean lunarBirthday, OnDateChangedListener onDateChangedListener) {
        int currentYear;
        boolean z;
        if (yearOptional && year == 0) {
            currentYear = getCurrentYear();
        } else {
            currentYear = year;
        }
        this.mYear = currentYear;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        this.mYearOptional = yearOptional;
        this.isLunarBirthday = lunarBirthday;
        if (yearOptional && year == 0) {
            z = false;
        } else {
            z = true;
        }
        this.mHasYear = z;
        this.mOnDateChangedListener = onDateChangedListener;
        updateSpinners();
    }

    private void updateSpinners() {
        updateYearSpinner();
        updateMonthSpinner();
        updateDaySpinner();
        this.mYearPicker.setVisibility(0);
    }

    private void updateDaySpinner() {
        String[] displayedDays;
        Calendar cal = Calendar.getInstance();
        cal.set(this.mHasYear ? this.mYear : AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, this.mMonth, 1);
        int max = cal.getActualMaximum(5);
        this.mDayPicker.setDisplayedValues(null);
        if (this.isLunarBirthday) {
            int lunarYear = this.mHasYear ? this.mYear : LunarUtils.getCurrentYear();
            int lunarIndex = LunarDate.getLeapMonthOfLunar(lunarYear);
            if (lunarIndex == 0) {
                max = LunarDate.getDayCountsOfLunarMonth(this.mHasYear ? this.mYear : LunarUtils.getCurrentYear(), this.mMonth + 1, false);
            } else if (lunarIndex == this.mMonth) {
                max = LunarDate.getDayCountsOfLunarMonth(lunarYear, this.mMonth, true);
            } else if (lunarIndex > this.mMonth) {
                max = LunarDate.getDayCountsOfLunarMonth(lunarYear, this.mMonth + 1, false);
            } else {
                max = LunarDate.getDayCountsOfLunarMonth(lunarYear, this.mMonth, false);
            }
            displayedDays = LunarUtils.getLunarDayArray();
        } else {
            displayedDays = this.mTextDays;
        }
        this.mDayPicker.setMinValue(1);
        this.mDayPicker.setMaxValue(max);
        this.mDayPicker.setDisplayedValues(displayedDays);
        this.mDayPicker.setValue(this.mDay);
        this.mDayPicker.setWrapSelectorWheel(true);
    }

    private void updateMonthSpinner() {
        String[] displayedMonths;
        int max;
        this.mMonthPicker.setDisplayedValues(null);
        if (this.isLunarBirthday) {
            displayedMonths = LunarUtils.getLunarMonthScope(this.mYear == 0 ? LunarUtils.getCurrentYear() : this.mYear);
            max = displayedMonths.length;
            if (LunarDate.getLeapMonthOfLunar(this.mYear == 0 ? LunarUtils.getCurrentYear() : this.mYear) == 0) {
                max--;
            }
            if (max == 12 && this.mMonth == 12) {
                this.mMonth = 0;
            }
        } else {
            displayedMonths = this.mTextMonths;
            max = displayedMonths.length;
        }
        this.mMonthPicker.setMinValue(1);
        this.mMonthPicker.setMaxValue(max);
        this.mMonthPicker.setDisplayedValues(displayedMonths);
        this.mMonthPicker.setValue(this.mMonth + 1);
        this.mMonthPicker.setWrapSelectorWheel(true);
    }

    private void updateYearSpinner() {
        String[] displayedYears;
        int min;
        int max;
        this.mYearPicker.setDisplayedValues(null);
        if (this.isLunarBirthday) {
            displayedYears = LunarUtils.getLunarYearArray();
            min = AMapException.CODE_AMAP_CLIENT_IO_EXCEPTION;
            max = 2035;
        } else {
            displayedYears = this.mTextYears;
            min = AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR;
            max = 2037;
        }
        this.mYearPicker.setMinValue(min);
        this.mYearPicker.setMaxValue(max);
        this.mYearPicker.setDisplayedValues(displayedYears);
        this.mYearPicker.setValue(this.mYear);
        this.mYearPicker.setWrapSelectorWheel(true);
    }

    public int getYear() {
        return (!this.mYearOptional || this.mHasYear) ? this.mYear : 0;
    }

    public boolean isYearOptional() {
        return this.mYearOptional;
    }

    public int getMonth() {
        return this.mMonth;
    }

    public int getDayOfMonth() {
        return this.mDay;
    }

    public boolean hasYear() {
        return this.mHasYear;
    }

    private void adjustMaxDay() {
        Calendar cal = Calendar.getInstance();
        cal.set(1, this.mHasYear ? this.mYear : AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST);
        cal.set(2, this.mMonth);
        int max = cal.getActualMaximum(5);
        if (this.isLunarBirthday) {
            int aYear = this.mHasYear ? this.mYear : LunarUtils.getCurrentYear();
            int lunarIndex = LunarDate.getLeapMonthOfLunar(aYear);
            if (lunarIndex == 0) {
                max = LunarDate.getDayCountsOfLunarMonth(aYear, this.mMonth + 1, false);
            } else if (lunarIndex == this.mMonth) {
                max = LunarDate.getDayCountsOfLunarMonth(aYear, this.mMonth, true);
            } else if (lunarIndex > this.mMonth) {
                max = LunarDate.getDayCountsOfLunarMonth(aYear, this.mMonth + 1, false);
            } else {
                max = LunarDate.getDayCountsOfLunarMonth(aYear, this.mMonth, false);
            }
        }
        if (this.mDay > max) {
            this.mDay = max;
        }
    }

    private void notifyDateChanged() {
        if (this.mOnDateChangedListener != null) {
            int year = (!this.mYearOptional || this.mHasYear) ? this.mYear : 0;
            this.mOnDateChangedListener.onDateChanged(this, year, this.mMonth, this.mDay, this.mHasYear);
        }
    }

    private void setCurrentLocale(Locale locale) {
        if (!locale.equals(this.mCurrentLocale)) {
            int i;
            this.mCurrentLocale = locale;
            this.mTempDate = getCalendarForLocale(this.mTempDate, locale);
            this.mMinDate = getCalendarForLocale(this.mMinDate, locale);
            this.mMaxDate = getCalendarForLocale(this.mMaxDate, locale);
            this.mCurrentDate = getCalendarForLocale(this.mCurrentDate, locale);
            Calendar tempDate = (Calendar) this.mTempDate.clone();
            LocaleData localeData = LocaleData.get(locale);
            if ("zh".equals(Locale.getDefault().getLanguage())) {
                this.mShortMonths = localeData.shortMonthNames;
            } else {
                this.mShortMonths = localeData.shortStandAloneMonthNames;
            }
            this.mDayFormat = DateFormat.getBestDateTimePattern(locale, "d");
            if (usingNumericMonths()) {
                this.mYearFormat = DateFormat.getBestDateTimePattern(locale, "yyyy");
            }
            this.mTextDays = new String[31];
            tempDate.set(2, 0);
            for (i = 0; i < 31; i++) {
                tempDate.set(5, i + 1);
                this.mTextDays[i] = DateFormat.format(this.mDayFormat, tempDate).toString();
            }
            this.mTextMonths = (String[]) Arrays.copyOfRange(this.mShortMonths, 0, 12);
            this.mTextYears = new String[138];
            for (i = 0; i < 138; i++) {
                tempDate.set(1, i + AMapException.CODE_AMAP_CLIENT_UNKNOWN_ERROR);
                this.mTextYears[i] = DateFormat.format(this.mYearFormat, tempDate).toString();
            }
        }
    }

    private boolean usingNumericMonths() {
        return Character.isDigit(this.mShortMonths[0].charAt(0));
    }

    private Calendar getCalendarForLocale(Calendar oldCalendar, Locale locale) {
        if (oldCalendar == null) {
            return Calendar.getInstance(locale);
        }
        long currentTimeMillis = oldCalendar.getTimeInMillis();
        Calendar newCalendar = Calendar.getInstance(locale);
        newCalendar.setTimeInMillis(currentTimeMillis);
        return newCalendar;
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        setCurrentLocale(newConfig.locale);
    }

    private void adjustMonthIndex(int oldYear, int newYear) {
        int cMonth = this.mMonth + 1;
        int oldIndex = LunarDate.getLeapMonthOfLunar(oldYear);
        int newIndex = LunarDate.getLeapMonthOfLunar(newYear);
        if (oldIndex != 0 || newIndex != 0) {
            if (oldIndex == 0 || newIndex == 0) {
                if (oldIndex == 0 || newIndex != 0) {
                    if (newIndex < cMonth) {
                        this.mMonth++;
                    }
                } else if (oldIndex < cMonth) {
                    this.mMonth--;
                }
            } else if (oldIndex < cMonth && newIndex > cMonth) {
                this.mMonth--;
            } else if (oldIndex > cMonth && newIndex < cMonth) {
                this.mMonth++;
            }
        }
    }
}
