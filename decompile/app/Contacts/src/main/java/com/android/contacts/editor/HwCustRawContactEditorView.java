package com.android.contacts.editor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import java.util.ArrayList;

public class HwCustRawContactEditorView {
    public boolean handleAnniversaryCust(KindSectionView sectionView, ArrayList<KindSectionView> arrayList) {
        return false;
    }

    public void hideTextFieldsEditorView(TextFieldsEditorView mOrganisationName) {
    }

    public void removeViews(ViewGroup mFields, LinearLayout mVibration) {
    }

    public void addViews(ViewGroup mFields, View view) {
    }

    public LinearLayout inflateNewViews(Context context, LayoutInflater mInflater, ContactEditorFragment mFragment, ViewGroup mFields, long rawContactId) {
        return null;
    }
}
