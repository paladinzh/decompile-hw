package com.android.contacts.list;

import android.content.Intent;
import android.net.Uri;

public interface OnContactPickerActionListener {
    void onCreateNewContactAction();

    void onEditContactAction(Uri uri);

    void onPickContactAction(Uri uri);

    void onShortcutIntentCreated(Intent intent);
}
