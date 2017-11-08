package com.android.settings.applications;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import com.android.settings.Utils;

public class ShortcutPreference extends Preference {
    private final String mPrefKey;
    private final Class mTarget;
    private final int mTitle;

    public ShortcutPreference(Context context, Class target, String prefKey, int prefTitle, int title) {
        super(context);
        this.mTarget = target;
        this.mPrefKey = prefKey;
        this.mTitle = title;
        setTitle(prefTitle);
        setKey(this.mPrefKey);
        setLayoutResource(2130968977);
        setWidgetLayoutResource(2130968998);
    }

    public void performClick() {
        super.performClick();
        Bundle bundle = new Bundle();
        bundle.putString(":settings:fragment_args_key", this.mPrefKey);
        Utils.startWithFragment(getContext(), this.mTarget.getName(), bundle, null, 0, this.mTitle, null);
    }
}
