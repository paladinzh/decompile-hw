package com.android.contacts.vcard;

import android.net.Uri;
import com.android.vcard.VCardEntry;

interface VCardImportExportListener {
    void onCancelRequest(CancelRequest cancelRequest, int i);

    void onExportFailed(ExportRequest exportRequest);

    void onExportProcessed(ExportRequest exportRequest, int i);

    void onImportCanceled(ImportRequest importRequest, int i);

    void onImportFailed(ImportRequest importRequest);

    void onImportFinished(ImportRequest importRequest, int i, Uri uri);

    void onImportParsed(ImportRequest importRequest, int i, VCardEntry vCardEntry, int i2, int i3, VCardService vCardService);

    void onImportProcessed(ImportRequest importRequest, int i, int i2);

    void onMemoryFull(ImportRequest importRequest, int i);
}
