package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class InputMethodPredicate implements Predicate<ProcessAppItem> {
    private static final String LATIN_INPUT_METHOD = "com.android.inputmethod.latin";
    private static final String TAG = "InputMethodPredicate";
    private InputMethodStringPredicate mPredicate;
    private final boolean mShouldFilter;

    public static class InputMethodStringPredicate implements Predicate<String> {
        private static final String TAG = "InputMethodStringPredicate";
        private final List<String> inputMethods;

        public InputMethodStringPredicate(Context ctx) {
            this.inputMethods = InputMethodPredicate.getInputMethod(ctx);
            HwLog.i(TAG, "InputMethodStringPredicate :: inputMethods = " + this.inputMethods.toString());
        }

        public boolean apply(String input) {
            if (TextUtils.isEmpty(input)) {
                return false;
            }
            String pkg = input;
            if (this.inputMethods == null || !this.inputMethods.contains(input)) {
                return false;
            }
            HwLog.i(TAG, "InputMethodStringPredicate :: getPackageName = " + input);
            return true;
        }
    }

    public InputMethodPredicate(Context ctx, boolean filter) {
        this.mPredicate = new InputMethodStringPredicate(ctx);
        this.mShouldFilter = filter;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        String pkg = input.getPackageName();
        if (this.mPredicate.apply(pkg)) {
            input.setKeyTask(true);
            if (this.mShouldFilter) {
                HwLog.i(TAG, "InputMethodPredicate :: name = " + input.getName() + "; getPackageName = " + pkg);
                return false;
            }
        }
        return true;
    }

    public static ArrayList<String> getInputMethod(Context context) {
        String inputMethod = Secure.getString(context.getContentResolver(), "default_input_method");
        if (inputMethod == null) {
            return Lists.newArrayList(LATIN_INPUT_METHOD);
        }
        String[] temp = inputMethod.split("/");
        ArrayList<String> list = Lists.newArrayList();
        if (temp.length > 0) {
            list.add(temp[0]);
        }
        list.add(LATIN_INPUT_METHOD);
        return list;
    }
}
