package com.android.contacts.editor;

import com.android.contacts.model.dataitem.DataKind;

public interface ContactEditorInfo {
    DataKind getKind();

    String getTitle();
}
