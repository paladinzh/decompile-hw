package com.android.mms.attachment.datamodel;

import android.content.Context;
import android.content.CursorLoader;
import android.net.Uri;

public class BoundCursorLoader extends CursorLoader {
    private final String mBindingId;

    public BoundCursorLoader(String bindingId, Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
        this.mBindingId = bindingId;
    }

    public String getBindingId() {
        return this.mBindingId;
    }
}
