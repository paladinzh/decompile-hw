package com.android.mms.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;

public class FragmentTag {
    public static Fragment getFragmentByTag(Activity activity, String tag) {
        return activity.getFragmentManager().findFragmentByTag(tag);
    }

    public static Fragment getFragmentByTag(Context context, String tag) {
        if (context instanceof Activity) {
            return getFragmentByTag((Activity) context, tag);
        }
        return null;
    }
}
