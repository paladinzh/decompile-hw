package com.android.contacts.editor;

import java.util.Locale;

public class HwCustStructuredNameEditorViewImpl extends HwCustStructuredNameEditorView {
    public boolean disableSyncPhoneticName() {
        return "JP".equalsIgnoreCase(Locale.getDefault().getCountry());
    }
}
