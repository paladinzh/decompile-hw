package com.android.gallery3d.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.ParcelFileDescriptor;
import android.os.ParcelFileDescriptor.AutoCloseOutputStream;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.AsyncTaskUtil;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.DataManager;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.data.MediaObject;
import com.android.gallery3d.data.MtpImage;
import com.android.gallery3d.data.Path;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GalleryProvider extends ContentProvider {
    public static final Uri BASE_URI = Uri.parse("content://com.android.gallery3d.provider");
    private static Uri sBaseUri;
    private DataManager mDataManager;

    private interface PipeDataWriter<T> {
        void writeDataToPipe(ParcelFileDescriptor parcelFileDescriptor, T t);
    }

    private final class MtpPipeDataWriter implements PipeDataWriter<Object> {
        private final MtpImage mImage;

        private MtpPipeDataWriter(MtpImage image) {
            this.mImage = image;
        }

        public void writeDataToPipe(ParcelFileDescriptor output, Object args) {
            IOException e;
            NullPointerException e2;
            Throwable th;
            Closeable closeable = null;
            try {
                Closeable os = new AutoCloseOutputStream(output);
                try {
                    os.write(this.mImage.getImageData());
                    Utils.closeSilently(os);
                    closeable = os;
                } catch (IOException e3) {
                    e = e3;
                    closeable = os;
                    GalleryLog.w("GalleryProvider", "fail to download: " + this.mImage.toString() + "." + e.getMessage());
                    Utils.closeSilently(closeable);
                } catch (NullPointerException e4) {
                    e2 = e4;
                    closeable = os;
                    try {
                        GalleryLog.w("GalleryProvider", "fail to download: " + this.mImage.toString() + "." + e2.getMessage());
                        Utils.closeSilently(closeable);
                    } catch (Throwable th2) {
                        th = th2;
                        Utils.closeSilently(closeable);
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    closeable = os;
                    Utils.closeSilently(closeable);
                    throw th;
                }
            } catch (IOException e5) {
                e = e5;
                GalleryLog.w("GalleryProvider", "fail to download: " + this.mImage.toString() + "." + e.getMessage());
                Utils.closeSilently(closeable);
            } catch (NullPointerException e6) {
                e2 = e6;
                GalleryLog.w("GalleryProvider", "fail to download: " + this.mImage.toString() + "." + e2.getMessage());
                Utils.closeSilently(closeable);
            }
        }
    }

    public static Uri getUriFor(Context context, Path path) {
        if (sBaseUri == null) {
            sBaseUri = Uri.parse("content://" + context.getPackageName() + ".provider");
        }
        return sBaseUri.buildUpon().appendEncodedPath(path.toString().substring(1)).build();
    }

    public int delete(Uri uri, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    public String getType(Uri uri) {
        String str = null;
        long token = Binder.clearCallingIdentity();
        try {
            MediaItem item = (MediaItem) getDataManager().getMediaObject(Path.fromString(uri.getPath()));
            if (item != null) {
                str = item.getMimeType();
            }
            Binder.restoreCallingIdentity(token);
            return str;
        } catch (Throwable th) {
            Binder.restoreCallingIdentity(token);
        }
    }

    public Uri insert(Uri uri, ContentValues values) {
        throw new UnsupportedOperationException();
    }

    public boolean onCreate() {
        return true;
    }

    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        long token = Binder.clearCallingIdentity();
        try {
            MediaObject object = getDataManager().getMediaObject(Path.fromString(uri.getPath()));
            if (object == null) {
                GalleryLog.w("GalleryProvider", "cannot find: " + uri);
                return null;
            } else if (object instanceof MtpImage) {
                Cursor queryMtpItem = queryMtpItem((MtpImage) object, projection, selection, selectionArgs, sortOrder);
                Binder.restoreCallingIdentity(token);
                return queryMtpItem;
            } else {
                Binder.restoreCallingIdentity(token);
                return null;
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private Cursor queryMtpItem(MtpImage image, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Object[] columnValues = new Object[projection.length];
        int n = projection.length;
        for (int i = 0; i < n; i++) {
            String column = projection[i];
            if ("_display_name".equals(column)) {
                columnValues[i] = image.getName();
            } else if ("_size".equals(column)) {
                columnValues[i] = Long.valueOf(image.getSize());
            } else if ("mime_type".equals(column)) {
                columnValues[i] = image.getMimeType();
            } else if ("datetaken".equals(column)) {
                columnValues[i] = Long.valueOf(image.getDateInMs());
            } else {
                GalleryLog.w("GalleryProvider", "unsupported column: " + column);
            }
        }
        MatrixCursor cursor = new MatrixCursor(projection);
        cursor.addRow(columnValues);
        return cursor;
    }

    public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
        long token = Binder.clearCallingIdentity();
        try {
            if (mode.contains("w")) {
                throw new FileNotFoundException("cannot open file for write");
            }
            MediaObject object = getDataManager().getMediaObject(Path.fromString(uri.getPath()));
            if (object == null) {
                throw new FileNotFoundException(uri.toString());
            } else if (object instanceof MtpImage) {
                ParcelFileDescriptor openPipeHelper = openPipeHelper(null, new MtpPipeDataWriter((MtpImage) object));
                return openPipeHelper;
            } else {
                throw new FileNotFoundException("unspported type: " + object);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        throw new UnsupportedOperationException();
    }

    private synchronized DataManager getDataManager() {
        if (this.mDataManager == null) {
            this.mDataManager = ((GalleryApp) getContext().getApplicationContext()).getDataManager();
        }
        return this.mDataManager;
    }

    private static <T> ParcelFileDescriptor openPipeHelper(final T args, final PipeDataWriter<T> func) throws FileNotFoundException {
        try {
            final ParcelFileDescriptor[] pipe = ParcelFileDescriptor.createPipe();
            AsyncTaskUtil.executeInParallel(new AsyncTask<Object, Object, Object>() {
                protected Object doInBackground(Object... params) {
                    try {
                        func.writeDataToPipe(pipe[1], args);
                        return null;
                    } finally {
                        Utils.closeSilently(pipe[1]);
                    }
                }
            }, (Object[]) null);
            return pipe[0];
        } catch (IOException e) {
            throw new FileNotFoundException("failure making pipe");
        }
    }
}
