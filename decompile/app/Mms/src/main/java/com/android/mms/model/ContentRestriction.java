package com.android.mms.model;

import android.content.ContentResolver;
import android.content.Context;
import android.net.Uri;
import com.android.mms.ContentRestrictionException;

public interface ContentRestriction {
    void checkAudioContentType(String str, Context context, Uri uri) throws ContentRestrictionException;

    void checkImageContentType(String str, Context context, Uri uri) throws ContentRestrictionException;

    void checkMessageSize(int i, int i2, ContentResolver contentResolver) throws ContentRestrictionException;

    void checkVideoContentType(String str, Context context, Uri uri) throws ContentRestrictionException;
}
