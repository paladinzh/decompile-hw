package com.android.contacts.editor;

import android.content.Context;
import android.view.View;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;

public class HwCustContactEditorCustomization {
    public void handleSaveCustomization(boolean aIsEditingUserProfile, RawContactDeltaList aState, Context aContext) {
    }

    public void addSectionViewProperty(boolean aIsProfile) {
    }

    public void customizeDefaultAccount(ContactEditorUtils aEditorUtils, Context aContext) {
    }

    public void handleEditorCustomization(DataKind mKind, ValuesDelta entry, RawContactDelta mState, boolean mReadOnly, ViewIdGenerator mViewIdGenerator, View view) {
    }
}
