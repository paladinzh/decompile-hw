package com.android.contacts.vcard;

import android.net.Uri;

public class ExportRequest {
    public final Uri destUri;
    public final String exportType;
    public boolean mIsSelectedContacts;
    public long[] selectedContactIds;

    public ExportRequest(Uri destUri) {
        this(destUri, null);
    }

    public ExportRequest(Uri destUri, String exportType) {
        this.destUri = destUri;
        this.exportType = exportType;
    }
}
