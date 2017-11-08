package com.android.contacts.util;

import android.content.Context;
import android.os.AsyncTask;
import android.telephony.PhoneNumberUtils;
import android.widget.TextView;
import com.android.contacts.GeoUtil;
import com.android.i18n.phonenumbers.AsYouTypeFormatter;
import com.android.i18n.phonenumbers.PhoneNumberUtil;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class PhoneNumberFormatter {

    private static class TextWatcherLoadAsyncTask extends AsyncTask<Void, Void, PhoneNumberFormattingTextWatcherEx> {
        private Context mContext;
        private final TextView mTextView;

        public TextWatcherLoadAsyncTask(Context context, TextView textView) {
            this.mContext = context;
            this.mTextView = textView;
        }

        protected PhoneNumberFormattingTextWatcherEx doInBackground(Void... params) {
            if (this.mContext != null) {
                return new PhoneNumberFormattingTextWatcherEx(GeoUtil.getCurrentCountryIso(this.mContext));
            }
            HwLog.d("PhoneNumberFormatter", "getCurrentCountryIso  error,  context is null");
            return null;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        protected void onPostExecute(PhoneNumberFormattingTextWatcherEx watcher) {
            if (!(watcher == null || isCancelled() || this.mTextView == null)) {
                this.mTextView.addTextChangedListener(watcher);
            }
        }
    }

    private PhoneNumberFormatter() {
    }

    public static final void setPhoneNumberFormattingTextWatcher(Context context, TextView textView) {
        new TextWatcherLoadAsyncTask(context, textView).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void[]) null);
    }

    public static String parsePhoneNumber(String value) {
        if (value == null) {
            return value;
        }
        Matcher m = Pattern.compile("[\\(\\[\\{].*?[\\)\\]\\}]").matcher(value);
        Pattern chineseChar = Pattern.compile("[一-龥]|[a-z]|[A-M]|[O-Z]");
        while (m.find()) {
            String t = m.group(0);
            if (chineseChar.matcher(t).find()) {
                value = value.replace(t, "");
            }
        }
        Matcher m2 = chineseChar.matcher(value);
        if (m2.find()) {
            return value.replace(m2.group(0), "");
        }
        return value;
    }

    public static String formatNumber(Context context, String number) {
        if (context == null || number == null) {
            return number;
        }
        String formatted = null;
        AsYouTypeFormatter formatter = PhoneNumberUtil.getInstance().getAsYouTypeFormatter(GeoUtil.getCurrentCountryIso(context));
        char lastNonSeparator = '\u0000';
        int len = number.length();
        for (int i = 0; i < len; i++) {
            char c = number.charAt(i);
            if (PhoneNumberUtils.isNonSeparator(c)) {
                if (lastNonSeparator != '\u0000') {
                    formatted = formatter.inputDigit(lastNonSeparator);
                }
                lastNonSeparator = c;
            }
        }
        if (lastNonSeparator != '\u0000') {
            formatted = formatter.inputDigit(lastNonSeparator);
        }
        return formatted;
    }
}
