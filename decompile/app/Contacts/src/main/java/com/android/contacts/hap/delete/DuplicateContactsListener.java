package com.android.contacts.hap.delete;

public interface DuplicateContactsListener {
    void onDeleteDuplicateContactsCanceled(String str, int i);

    void onDeleteDuplicateContactsFailed(String str, int i);

    void onDeleteDuplicateContactsFinished(String str, int i, int i2);

    void onNoDuplicateContactsFound(String str, int i);
}
