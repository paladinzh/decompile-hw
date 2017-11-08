package com.android.contacts.editor;

import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.dataitem.DataKind;

public interface Editor {

    public interface EditorListener {
        void onDeleteRequested(Editor editor);

        void onRequest(int i);
    }

    void clearAllFields();

    void deleteEditor();

    void editNewlyAddedField();

    boolean isEmpty();

    void setDeletable(boolean z);

    void setEditorListener(EditorListener editorListener);

    void setValues(DataKind dataKind, ValuesDelta valuesDelta, RawContactDelta rawContactDelta, boolean z, ViewIdGenerator viewIdGenerator);
}
