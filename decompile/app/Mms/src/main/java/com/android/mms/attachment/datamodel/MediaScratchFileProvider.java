package com.android.mms.attachment.datamodel;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import com.android.mms.attachment.Factory;
import java.io.File;
import java.util.List;

public class MediaScratchFileProvider extends FileProvider {
    private static final SimpleArrayMap<Uri, String> sUriToDisplayNameMap = new SimpleArrayMap();

    public static boolean isMediaScratchSpaceUri(Uri uri) {
        boolean z = false;
        if (uri == null) {
            return false;
        }
        List<String> segments = uri.getPathSegments();
        if (TextUtils.equals(uri.getScheme(), "content") && TextUtils.equals(uri.getAuthority(), "com.android.mms.attachment.camera.data.MediaScratchFileProvider") && segments.size() == 1) {
            z = FileProvider.isValidFileId((String) segments.get(0));
        }
        return z;
    }

    File getFile(String path, String extension) {
        return getFileWithExtension(path, extension);
    }

    private static File getFileWithExtension(String path, String extension) {
        File directory = getDirectory(Factory.get().getApplicationContext());
        if (!TextUtils.isEmpty(extension)) {
            path = path + "." + extension;
        }
        return new File(directory, path);
    }

    private static File getDirectory(Context context) {
        return new File(context.getCacheDir(), "mediascratchspace");
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (projection != null && projection.length > 0 && TextUtils.equals(projection[0], "_display_name") && isMediaScratchSpaceUri(uri)) {
            String displayName;
            synchronized (sUriToDisplayNameMap) {
                displayName = (String) sUriToDisplayNameMap.get(uri);
            }
            if (!TextUtils.isEmpty(displayName)) {
                MatrixCursor cursor = new MatrixCursor(new String[]{"_display_name"});
                cursor.newRow().add(displayName);
                return cursor;
            }
        }
        return null;
    }
}
