package com.android.contacts.list;

import android.content.Intent;
import android.net.Uri;

public interface OnPhoneNumberPickerActionListener {
    void onHomeInActionBarSelected();

    void onPickPhoneNumberAction(Uri uri);

    void onShortcutIntentCreated(Intent intent);
}
