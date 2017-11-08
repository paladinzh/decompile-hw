package com.android.contacts.hap.copy;

import com.android.contacts.vcard.CancelRequest;

public interface ContactsCopyListener {
    void onCancelRequest(CancelRequest cancelRequest);

    void onCopyContactsCanceled(String str, int i);

    void onCopyContactsFailed(String str);

    void onCopyContactsFinished(String str, int i);

    void onCopyContactsParsed(String str, String str2, int i, String str3, int i2, int i3, CopyContactService copyContactService);

    void onCopyContactsQueued(String str, int i, int i2);
}
