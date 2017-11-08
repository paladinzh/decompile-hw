package android.support.v17.leanback.widget.picker;

import android.content.res.Resources;
import android.support.v17.leanback.R$string;
import java.text.DateFormatSymbols;
import java.util.Calendar;
import java.util.Locale;

class PickerConstant {
    public final String[] ampm;
    public final String dateSeparator;
    public final String[] days;
    public final String[] hours12 = createStringIntArrays(1, 12, "%02d");
    public final String[] hours24 = createStringIntArrays(0, 23, "%02d");
    public final Locale locale;
    public final String[] minutes = createStringIntArrays(0, 59, "%02d");
    public final String[] months;
    public final String timeSeparator;

    public PickerConstant(Locale locale, Resources resources) {
        this.locale = locale;
        DateFormatSymbols symbols = DateFormatSymbols.getInstance(locale);
        this.months = symbols.getShortMonths();
        Calendar calendar = Calendar.getInstance(locale);
        this.days = createStringIntArrays(calendar.getMinimum(5), calendar.getMaximum(5), "%02d");
        this.ampm = symbols.getAmPmStrings();
        this.dateSeparator = resources.getString(R$string.lb_date_separator);
        this.timeSeparator = resources.getString(R$string.lb_time_separator);
    }

    public static String[] createStringIntArrays(int firstNumber, int lastNumber, String format) {
        String[] array = new String[((lastNumber - firstNumber) + 1)];
        for (int i = firstNumber; i <= lastNumber; i++) {
            if (format != null) {
                array[i - firstNumber] = String.format(format, new Object[]{Integer.valueOf(i)});
            } else {
                array[i - firstNumber] = String.valueOf(i);
            }
        }
        return array;
    }
}
