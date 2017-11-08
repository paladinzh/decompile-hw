package com.android.contacts.vcard;

import android.accounts.Account;
import android.net.Uri;

public class ImportRequest {
    public final Account account;
    public final byte[] data;
    public final String displayName;
    public final int entryCount;
    public final String estimatedCharset;
    public final int estimatedVCardType;
    public boolean isMultipleRequest;
    public final Uri uri;
    public final int vcardVersion;

    private ImportRequest(Account account, byte[] data, Uri uri, String displayName, int estimatedType, String estimatedCharset, int vcardVersion, int entryCount) {
        this.account = account;
        this.data = data;
        this.uri = uri;
        this.displayName = displayName;
        this.estimatedVCardType = estimatedType;
        this.estimatedCharset = estimatedCharset;
        this.vcardVersion = vcardVersion;
        this.entryCount = entryCount;
    }

    public static ImportRequest newInstance(Account account, byte[] data, Uri uri, String displayName, int estimatedType, String estimatedCharset, int vcardVersion, int entryCount) {
        return new ImportRequest(account, data, uri, displayName, estimatedType, estimatedCharset, vcardVersion, entryCount);
    }
}
