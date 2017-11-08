package com.android.contacts.list;

import android.net.Uri;

public interface OnContactBrowserActionListener {
    void onDeleteContactAction(Uri uri);

    void onEditContactAction(Uri uri);

    void onInvalidSelection();

    void onSelectionChange();

    void onViewContactAction(Uri uri, boolean z);
}
