package com.android.settings.fingerprint;

import android.content.DialogInterface;
import android.support.v7.preference.DialogPreference;

public interface FingerprintDialogListener {
    void onDialogClicked(DialogInterface dialogInterface, int i, DialogPreference dialogPreference);

    void onDialogCreated(DialogPreference dialogPreference);

    void onItemHighLightOff(DialogPreference dialogPreference);
}
