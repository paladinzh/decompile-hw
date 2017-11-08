package com.android.settings.deviceinfo;

import android.content.Context;
import android.support.v7.preference.ListPreference;
import android.util.AttributeSet;
import android.widget.Toast;
import com.android.settings.Utils;

public class StorageListPreference extends ListPreference {
    public StorageListPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public StorageListPreference(Context context) {
        super(context);
    }

    protected void onClick() {
        if (Utils.isMultiUserExist(getContext())) {
            showToast(2131628283, 0);
        } else {
            super.onClick();
        }
    }

    private void showToast(int resId, int duration) {
        Toast.makeText(getContext(), resId, duration).show();
    }
}
