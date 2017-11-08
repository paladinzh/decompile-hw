package com.android.common.io;

import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import java.io.IOException;

public class MoreCloseables {
    public static void closeQuietly(Cursor cursor) {
        if (cursor != null) {
            cursor.close();
        }
    }

    public static void closeQuietly(AssetFileDescriptor assetFileDescriptor) {
        if (assetFileDescriptor != null) {
            try {
                assetFileDescriptor.close();
            } catch (IOException e) {
            }
        }
    }
}
