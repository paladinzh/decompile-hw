package com.android.gallery3d.data;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.AlbumMoveReporter;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.android.gallery3d.util.ReportToBigData;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.media.database.CloudTableOperateHelper;
import com.huawei.gallery.photoshare.utils.PhotoShareConstants;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

public abstract class LocalBucketMediaSet extends LocalMergeQuerySet {
    private static final String[] BUCKET_DATA_PROJECTION = new String[]{"bucket_id", "_data"};
    private static final String[] CLOUD_FILE_DATA_PROJECTION = new String[]{"id", "size", "hash", "localThumbPath", "localBigThumbPath", "fileName", "orientation", "albumId", "duration", "latitude", "longitude", "fileType", "fileId", "source", "videoThumbId", "createTime", "expand"};
    public static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    protected final GalleryApp mApplication;
    protected Uri mBaseUri;
    protected final int mBucketId;
    protected String mName;
    protected final int mResId = getResId(this.mBucketId, this.mApplication.getAndroidContext());
    protected final ContentResolver mResolver;

    /* renamed from: com.android.gallery3d.data.LocalBucketMediaSet$1 */
    class AnonymousClass1 extends Thread {
        final /* synthetic */ HashMap val$cloudMediaSet;
        final /* synthetic */ String val$oldBucketDisplayName;

        AnonymousClass1(HashMap val$cloudMediaSet, String val$oldBucketDisplayName) {
            this.val$cloudMediaSet = val$cloudMediaSet;
            this.val$oldBucketDisplayName = val$oldBucketDisplayName;
        }

        public void run() {
            LocalBucketMediaSet.this.insertCloudFile(this.val$cloudMediaSet, this.val$oldBucketDisplayName, LocalBucketMediaSet.this.mBucketId);
        }
    }

    protected abstract String getDeleteClause();

    @edu.umd.cs.findbugs.annotations.SuppressWarnings({"NP_LOAD_OF_KNOWN_NULL_VALUE"})
    public boolean rename(java.lang.String r29) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:66:? in {10, 12, 19, 24, 25, 30, 33, 40, 48, 52, 53, 55, 56, 57, 58, 59, 60, 61, 62, 64, 65, 67, 68, 69} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r28 = this;
        com.android.gallery3d.util.GalleryUtils.assertNotInRenderThread();
        r20 = new java.io.File;
        r0 = r28;
        r2 = r0.mBucketPath;
        r0 = r20;
        r0.<init>(r2);
        r19 = r20.getName();
        r17 = new java.io.File;
        r2 = r20.getParent();
        r0 = r17;
        r1 = r29;
        r0.<init>(r2, r1);
        r2 = r17.exists();
        if (r2 != 0) goto L_0x002b;
    L_0x0025:
        r2 = r17.mkdirs();
        if (r2 == 0) goto L_0x007b;
    L_0x002b:
        r2 = r17.toString();
        r3 = java.util.Locale.US;
        r2 = r2.toLowerCase(r3);
        r16 = r2.hashCode();
        r15 = r17.getName();
        r9 = 0;
        com.huawei.gallery.util.MediaSyncerHelper.terminateMediaSyncerService();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r0.mResolver;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = EXTERNAL_FILE_URI;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = BUCKET_DATA_PROJECTION;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5 = "bucket_id = ? AND ((media_type IN (1,3)) OR (title='.outside' OR title='.empty_out' OR title='.empty_in' OR title='.inside'))";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6 = 1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6 = new java.lang.String[r6];	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r7 = r0.mBucketId;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r7 = java.lang.String.valueOf(r7);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r27 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6[r27] = r7;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r7 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r9 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r9 != 0) goto L_0x0086;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x0062:
        r2 = "LocalBucketAlbum";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "query fail";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        com.android.gallery3d.util.GalleryLog.w(r2, r3);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = 0;
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        return r2;
    L_0x007b:
        r2 = "LocalBucketAlbum";
        r3 = "Fail to create new Dir";
        com.android.gallery3d.util.GalleryLog.w(r2, r3);
        r2 = 0;
        return r2;
    L_0x0086:
        r26 = new java.util.ArrayList;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r26.<init>();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r14 = new java.util.ArrayList;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r14.<init>();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r8 = r28.getCloudMediaSet();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r17.toString();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r22 = r0.insertDirectory(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x009e:
        r2 = r9.moveToNext();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r2 == 0) goto L_0x01f9;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x00a4:
        r2 = 1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r10 = r9.getString(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r21 = new java.io.File;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r21;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.<init>(r10);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r18 = new java.io.File;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r21.getName();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r18;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r17;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.<init>(r1, r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r18.exists();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r2 == 0) goto L_0x00df;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x00c3:
        r23 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x00c5:
        r0 = r21;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r18;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r24 = r0.renameTo(r1);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r24 != 0) goto L_0x00e2;
    L_0x00cf:
        r2 = 0;
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        return r2;
    L_0x00df:
        r23 = 1;
        goto L_0x00c5;
    L_0x00e2:
        if (r23 == 0) goto L_0x01c7;
    L_0x00e4:
        r2 = EXTERNAL_FILE_URI;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = android.content.ContentProviderOperation.newUpdate(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "_data = ? ";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = new java.lang.String[r4];	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5 = java.lang.String.valueOf(r10);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4[r6] = r5;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withSelection(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "_data";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = r18.toString();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "bucket_id";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = java.lang.Integer.valueOf(r16);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "bucket_display_name";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r25 = r2.withValue(r3, r15);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = -1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r22;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r0 == r2) goto L_0x0129;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x011d:
        r2 = "parent";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = java.lang.Integer.valueOf(r22);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r25;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.withValue(r2, r3);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x0129:
        r2 = r8.containsKey(r10);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        if (r2 == 0) goto L_0x01a5;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x012f:
        r2 = com.huawei.gallery.media.GalleryMedia.URI;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = android.content.ContentProviderOperation.newUpdate(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "_data = ? ";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = new java.lang.String[r4];	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5 = java.lang.String.valueOf(r10);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4[r6] = r5;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withSelection(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "cloud_media_id";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = -1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "cloud_bucket_id";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "fileType";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "fileId";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "videoThumbId";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "thumbType";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = java.lang.Integer.valueOf(r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "localThumbPath";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "localBigThumbPath";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "expand";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "source";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r13 = r2.withValue(r3, r4);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r13.build();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r14.add(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
    L_0x01a5:
        r2 = r25.build();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r26;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.add(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        goto L_0x009e;
    L_0x01b0:
        r12 = move-exception;
        r2 = "LocalBucketAlbum";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        com.android.gallery3d.util.GalleryLog.noPermissionForMediaProviderLog(r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = 0;
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        return r2;
    L_0x01c7:
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r0.mResolver;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = EXTERNAL_FILE_URI;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r4 = "_data = ? ";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5 = 1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5 = new java.lang.String[r5];	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r6 = java.lang.String.valueOf(r10);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r7 = 0;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r5[r7] = r6;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2.delete(r3, r4, r5);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        goto L_0x009e;
    L_0x01df:
        r11 = move-exception;
        r2 = "LocalBucketAlbum";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r3 = "rename fail!";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        com.android.gallery3d.util.GalleryLog.w(r2, r3);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = 0;
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        return r2;
    L_0x01f9:
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r20;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.deleteDirIfNull(r1);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r17;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.makeOutsideFileForWhiteListRename(r1);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = "com.huawei.gallery.provider";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0.applyBatchUpdateByLimit(r14, r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = new com.android.gallery3d.data.LocalBucketMediaSet$1;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r19;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2.<init>(r8, r1);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2.start();	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = "media";	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r0 = r28;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r1 = r26;	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        r2 = r0.applyBatchUpdateByLimit(r1, r2);	 Catch:{ SecurityException -> 0x01b0, RuntimeException -> 0x01df, all -> 0x0235 }
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        return r2;
    L_0x0235:
        r2 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r9);
        r0 = r28;
        r3 = r0.mApplication;
        r3 = r3.getAndroidContext();
        com.huawei.gallery.util.MediaSyncerHelper.startMediaSyncerService(r3);
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.data.LocalBucketMediaSet.rename(java.lang.String):boolean");
    }

    public LocalBucketMediaSet(Path path, GalleryApp application, int bucketId, String name) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mBucketId = bucketId;
        this.mName = name;
    }

    public String getName() {
        if (this.mResId != 0) {
            switch (this.mResId) {
                case R.string.screenshots_folder_multi_sdcard:
                case R.string.external_storage_multi_root_directory:
                case R.string.camera_folder_multi_sdcard:
                    if (GalleryStorageManager.getInstance().getGalleryStorageByBucketID(this.mBucketId) != null) {
                        this.mName = this.mApplication.getResources().getString(this.mResId, new Object[]{galleryStorage.getName()});
                        break;
                    }
                    break;
                default:
                    this.mName = this.mApplication.getResources().getString(this.mResId);
                    break;
            }
        } else if (this.mName != null && this.mName.isEmpty()) {
            this.mName = BucketHelper.getBucketName(this.mResolver, this.mBucketId);
        }
        return this.mName;
    }

    public String getDefaultAlbumName() {
        if (this.mBucketId == MediaSetUtils.getCameraBucketId() || GalleryStorageManager.getInstance().isOuterGalleryStorageCameraBucketID(this.mBucketId)) {
            return this.mApplication.getResources().getString(MediaSetUtils.getCameraAlbumStringId());
        }
        if (GalleryUtils.isScreenRecorderExist() && (this.mBucketId == MediaSetUtils.getScreenshotsBucketID() || GalleryStorageManager.getInstance().isOuterGalleryStorageScreenshotsBucketID(this.mBucketId))) {
            return this.mApplication.getResources().getString(MediaSetUtils.getScreenshotsAlbumStringId());
        }
        return super.getDefaultAlbumName();
    }

    public int getSupportedOperations() {
        return 1076888581;
    }

    public void delete() {
        GalleryUtils.assertNotInRenderThread();
        try {
            this.mResolver.delete(this.mBaseUri, getDeleteClause(), new String[]{String.valueOf(this.mBucketId)});
        } catch (SecurityException e) {
            GalleryLog.noPermissionForMediaProviderLog("LocalBucketAlbum");
        }
        GalleryUtils.deleteExtraFile(this.mResolver, this.mBucketPath, ".outside", ".inside", ".empty_out", ".empty_in");
        this.mApplication.getDataManager().notifyChange(this.mBaseUri);
        this.mApplication.getGalleryData().dropAlbumIndex(this.mBucketId, getBucketPath());
        this.mApplication.getDataManager().notifyChange(Constant.MOVE_OUT_IN_URI);
        GalleryUtils.startScanFavoriteService(this.mApplication.getAndroidContext());
    }

    public boolean isLeafAlbum() {
        return true;
    }

    private static int getResId(int bucketId, Context context) {
        boolean hasAnyMountedOuterGalleryStorage = GalleryStorageManager.getInstance().hasAnyMountedOuterGalleryStorage();
        if (bucketId == MediaSetUtils.getCameraBucketId() && !hasAnyMountedOuterGalleryStorage) {
            return MediaSetUtils.getCameraAlbumStringId();
        }
        if (GalleryUtils.isScreenRecorderExist() && bucketId == MediaSetUtils.getScreenshotsBucketID() && !hasAnyMountedOuterGalleryStorage) {
            return MediaSetUtils.getScreenshotsAlbumStringId();
        }
        return MediaSetUtils.bucketId2ResourceId(bucketId, context);
    }

    public void hide() {
        GalleryUtils.assertNotInRenderThread();
        File hiddenFile = new File(this.mBucketPath, ".hidden");
        if (!hiddenFile.exists()) {
            try {
                if (this.mBucketPath.startsWith("/mnt") || this.mBucketPath.startsWith("/storage")) {
                    if (!hiddenFile.createNewFile()) {
                        GalleryLog.i("LocalBucketAlbum", "hidden file create failed ...");
                    }
                    ContentValues values = new ContentValues();
                    values.put("_data", getExtraFileName(".hidden"));
                    values.put("bucket_id", String.valueOf(this.mBucketId));
                    values.put("media_type", String.valueOf(0));
                    this.mResolver.insert(EXTERNAL_FILE_URI, values);
                } else {
                    FilePreference.put(this.mApplication.getAndroidContext(), String.valueOf(this.mBucketId));
                }
                Keyguard.updateHiddenFlag(this.mResolver, this.mBucketId, true);
                ReportToBigData.reportForHiddenAlbumPath(this.mBucketPath);
            } catch (Exception e) {
                GalleryLog.i("LocalBucketAlbum", "An exception has occurred in hide() method." + e.getMessage());
            }
            setHidden(true);
            this.mApplication.getDataManager().notifyChange(this.mBaseUri);
            this.mResolver.notifyChange(Media.EXTERNAL_CONTENT_URI, null);
        }
    }

    public void show() {
        GalleryUtils.assertNotInRenderThread();
        File hiddenFile = new File(this.mBucketPath, ".hidden");
        try {
            if (hiddenFile.exists()) {
                if (!hiddenFile.delete()) {
                    GalleryLog.i("LocalBucketAlbum", "hidden file deleted failed ...");
                }
                this.mResolver.delete(EXTERNAL_FILE_URI, "_data = ? ", new String[]{getExtraFileName(".hidden")});
            } else if (!(this.mBucketPath.startsWith("/mnt") || this.mBucketPath.startsWith("/storage"))) {
                FilePreference.remove(this.mApplication.getAndroidContext(), String.valueOf(this.mBucketId));
                this.mResolver.notifyChange(Media.EXTERNAL_CONTENT_URI, null);
            }
            Keyguard.updateHiddenFlag(this.mResolver, this.mBucketId, false);
        } catch (Exception e) {
            GalleryLog.i("LocalBucketAlbum", "An exception has occurred in show() method." + e.getMessage());
        }
        setHidden(false);
        this.mApplication.getDataManager().notifyChange(this.mBaseUri);
    }

    private String getExtraFileName(String suffixName) {
        return this.mBucketPath + File.separator + suffixName;
    }

    public void moveOUT() {
        GalleryUtils.assertNotInRenderThread();
        if (this.mBucketPath.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX)) {
            resetPreLoadPicStatusInPrefs();
        } else if (getMediaItemCount() == 0) {
            GalleryUtils.renameExtraFileAndUpdateValues(this.mResolver, this.mBucketPath, ".empty_in", ".empty_out");
            GalleryUtils.renameExtraFileAndUpdateValues(this.mResolver, this.mBucketPath, ".inside", ".outside");
        } else if (GalleryUtils.isOuterVolumeBucketId(this.mBucketPath) || this.mApplication.getGalleryData().isWhiteListBucketId(this.mBucketPath)) {
            GalleryUtils.deleteExtraFile(this.mResolver, this.mBucketPath, ".inside", ".empty_out", ".empty_in");
        } else if (GalleryUtils.isInnerVolumeBucketId(this.mBucketPath)) {
            GalleryUtils.createExtraFileAndInsertValues(this.mResolver, this.mBucketPath, ".outside");
            GalleryUtils.deleteExtraFile(this.mResolver, this.mBucketPath, ".inside", ".empty_out", ".empty_in");
        }
        AlbumMoveReporter.reportAlbumMoveToBigData(Action.MOVEOUT, this.mBucketPath);
        this.mApplication.getGalleryData().addMaxAlbumIndex(this.mBucketId, getBucketPath());
        this.mApplication.getDataManager().notifyChange(Constant.MOVE_OUT_IN_URI);
    }

    public void moveIN() {
        GalleryUtils.assertNotInRenderThread();
        if (this.mBucketPath.startsWith(BucketHelper.PRE_LOADED_PATH_PREFIX)) {
            setPreLoadPicStatusInPrefs();
        } else if (getMediaItemCount() == 0) {
            GalleryUtils.renameExtraFileAndUpdateValues(this.mResolver, this.mBucketPath, ".empty_out", ".empty_in");
            GalleryUtils.renameExtraFileAndUpdateValues(this.mResolver, this.mBucketPath, ".outside", ".inside");
        } else if (GalleryUtils.isOuterVolumeBucketId(this.mBucketPath) || this.mApplication.getGalleryData().isWhiteListBucketId(this.mBucketPath)) {
            GalleryUtils.createExtraFileAndInsertValues(this.mResolver, this.mBucketPath, ".inside");
            GalleryUtils.deleteExtraFile(this.mResolver, this.mBucketPath, ".outside", ".empty_out", ".empty_in");
        } else if (GalleryUtils.isInnerVolumeBucketId(this.mBucketPath)) {
            GalleryUtils.deleteExtraFile(this.mResolver, this.mBucketPath, ".outside", ".empty_out", ".empty_in");
        }
        AlbumMoveReporter.reportAlbumMoveToBigData(Action.MOVEIN, this.mBucketPath);
        this.mApplication.getGalleryData().dropAlbumIndex(this.mBucketId, getBucketPath());
        this.mApplication.getDataManager().notifyChange(Constant.MOVE_OUT_IN_URI);
    }

    public boolean isEmptyAlbum() {
        return getMediaItemCount() == 0;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private HashMap<String, Integer> getCloudMediaSet() {
        HashMap<String, Integer> cloudMediaSet = new HashMap();
        try {
            Closeable cursor = this.mResolver.query(GalleryMedia.URI, new String[]{"_data", "cloud_media_id"}, "bucket_id=? and cloud_media_id!=-1 and local_media_id!=-1", new String[]{String.valueOf(this.mBucketId)}, null);
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    cloudMediaSet.put(cursor.getString(0), Integer.valueOf(cursor.getInt(1)));
                }
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            GalleryLog.v("LocalBucketAlbum", "getCloudMediaSet Sql Exception " + e);
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return cloudMediaSet;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void insertCloudFile(HashMap<String, Integer> cloudMediaSet, String albumName, int bucketId) {
        if (cloudMediaSet.entrySet().size() != 0) {
            ArrayList<ContentProviderOperation> galleryMediaOps = new ArrayList();
            ArrayList<Integer> cloudIds = new ArrayList();
            for (Entry<String, Integer> tempEntry : cloudMediaSet.entrySet()) {
                cloudIds.add((Integer) tempEntry.getValue());
            }
            int group = ((cloudIds.size() + 100) - 1) / 100;
            for (int i = 0; i < group; i++) {
                try {
                    Closeable cursor = this.mResolver.query(PhotoShareConstants.CLOUD_FILE_TABLE_URI, CLOUD_FILE_DATA_PROJECTION, "id in (" + getWhereClause(cloudIds.subList(i * 100, Math.min((i + 1) * 100, cloudIds.size()))) + ")", null, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            galleryMediaOps.add(getGalleryDataFromCursor(cursor, albumName, bucketId));
                        }
                    }
                    Utils.closeSilently(cursor);
                } catch (SQLiteException e) {
                    GalleryLog.v("LocalBucketAlbum", "getCloudMediaSet Sql Exception " + e);
                } catch (Throwable th) {
                    Utils.closeSilently(null);
                }
            }
            applyBatchUpdateByLimit(galleryMediaOps, "com.huawei.gallery.provider");
        }
    }

    private String getMineType(int fileType) {
        if (fileType == 4) {
            return "video/mp4";
        }
        if (fileType == 5) {
            return "image/png";
        }
        return "image/jpeg";
    }

    private int getMediaType(int fileType) {
        if (fileType == 4) {
            return 3;
        }
        return 1;
    }

    private int getRefocus(int fileType) {
        if (fileType == 6) {
            return 2;
        }
        if (fileType == 3) {
            return 1;
        }
        return 0;
    }

    private int getVoiceOffset(int fileType) {
        if (fileType == 2) {
            return -1;
        }
        return 0;
    }

    private String getTitleName(String fileName) {
        if (TextUtils.isEmpty(fileName) || fileName.lastIndexOf(".") == -1) {
            return fileName;
        }
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    private ContentProviderOperation getGalleryDataFromCursor(Cursor cursor, String albumName, int bucketId) {
        String data;
        int thumbType;
        int i;
        String localThumbPath = cursor.getString(3);
        String localBigThumbPath = cursor.getString(4);
        Long size = Long.valueOf(cursor.getLong(1));
        Long createTime = Long.valueOf(cursor.getLong(15));
        String fileName = cursor.getString(5);
        int fileType = cursor.getInt(11);
        String titleName = getTitleName(fileName);
        String mimeType = getMineType(fileType);
        double latitude = cursor.getDouble(9);
        double longitude = cursor.getDouble(10);
        int rotate = PhotoShareUtils.getOrientation(cursor.getInt(6));
        String albumId = cursor.getString(7);
        String hash = cursor.getString(2);
        if (!TextUtils.isEmpty(localBigThumbPath)) {
            data = localBigThumbPath;
            thumbType = 2;
        } else if (TextUtils.isEmpty(localThumbPath)) {
            data = "cloud-" + albumId + "-" + hash;
            thumbType = 0;
        } else {
            data = localThumbPath;
            thumbType = 1;
        }
        int width = 0;
        int height = 0;
        if (!data.startsWith("cloud-")) {
            try {
                Options opts = new Options();
                opts.inJustDecodeBounds = true;
                BitmapFactory.decodeFile(data, opts);
                if (opts.outWidth > 0 && opts.outHeight > 0) {
                    width = opts.outWidth;
                    height = opts.outHeight;
                }
            } catch (Exception e) {
                GalleryLog.w("photoshareLogTag", "updateWidthAndHeight." + e.getMessage());
            }
        }
        Builder withValue = ContentProviderOperation.newInsert(GalleryMedia.URI).withValue("_data", data).withValue("_size", size).withValue("date_added", createTime).withValue("date_modified", Long.valueOf(CloudTableOperateHelper.getLastModify(data))).withValue("mime_type", mimeType).withValue("_display_name", fileName).withValue("title", titleName).withValue("orientation", Integer.valueOf(rotate)).withValue("latitude", Double.valueOf(latitude)).withValue("longitude", Double.valueOf(longitude)).withValue("datetaken", createTime).withValue("bucket_id", Integer.valueOf(bucketId)).withValue("bucket_display_name", albumName).withValue("duration", Integer.valueOf(cursor.getInt(8))).withValue("media_type", Integer.valueOf(getMediaType(fileType))).withValue("hw_voice_offset", Integer.valueOf(getVoiceOffset(fileType))).withValue("hw_image_refocus", Integer.valueOf(getRefocus(fileType))).withValue("hw_rectify_offset", Integer.valueOf(fileType == 8 ? -1 : 0)).withValue("hash", hash).withValue("local_media_id", Integer.valueOf(-1)).withValue("cloud_media_id", Integer.valueOf(cursor.getInt(0))).withValue("dirty", Integer.valueOf(0)).withValue("cloud_bucket_id", albumId);
        String str = "is_hw_burst";
        if (CloudTableOperateHelper.matchBurstCover(fileName)) {
            i = 1;
        } else {
            i = 0;
        }
        return withValue.withValue(str, Integer.valueOf(i)).withValue("fileType", Integer.valueOf(fileType)).withValue("fileId", cursor.getString(12)).withValue("videoThumbId", cursor.getString(14)).withValue("localThumbPath", localThumbPath).withValue("localBigThumbPath", localBigThumbPath).withValue("thumbType", Integer.valueOf(thumbType)).withValue("expand", cursor.getString(16)).withValue("width", Integer.valueOf(width)).withValue("height", Integer.valueOf(height)).withValue("source", cursor.getString(13)).build();
    }

    private String getWhereClause(List<Integer> subList) {
        StringBuffer sb = new StringBuffer();
        for (Integer cloudId : subList) {
            sb.append(", ").append(cloudId);
        }
        if (sb.length() > 0) {
            return sb.substring(1);
        }
        return null;
    }

    private boolean applyBatchUpdateByLimit(ArrayList<ContentProviderOperation> updateOps, String authority) {
        int opsSize = updateOps.size();
        int i = 0;
        while (i <= (opsSize - 1) / 100) {
            List<ContentProviderOperation> tempList = updateOps.subList(i * 100, Math.min((i + 1) * 100, opsSize));
            ArrayList<ContentProviderOperation> subOps = new ArrayList(100);
            subOps.addAll(tempList);
            try {
                this.mResolver.applyBatch(authority, subOps);
                i++;
            } catch (RemoteException e) {
                GalleryLog.e("LocalBucketAlbum", "update sort index failure!");
                return false;
            } catch (OperationApplicationException e2) {
                GalleryLog.e("LocalBucketAlbum", "update sort index failure!");
                return false;
            }
        }
        return true;
    }

    private void deleteDirIfNull(File root) {
        if (root != null && root.listFiles() != null && root.listFiles().length == 0) {
            try {
                this.mResolver.delete(EXTERNAL_FILE_URI, "_data = ? ", new String[]{root.toString()});
            } catch (SecurityException e) {
                GalleryLog.noPermissionForMediaProviderLog("LocalBucketAlbum");
            }
            if (!root.delete()) {
                GalleryLog.w("LocalBucketAlbum", "Delete the dir failure!");
            }
        }
    }

    private int insertDirectory(String path) {
        ContentValues values = new ContentValues();
        values.put("format", Integer.valueOf(12289));
        values.put("_data", path);
        File parentFile = new File(path).getParentFile();
        if (parentFile == null) {
            parentFile = new File(File.separator);
        }
        int bucketId = GalleryUtils.getBucketId(parentFile.toString());
        String parentName = parentFile.getName();
        values.put("bucket_id", Integer.valueOf(bucketId));
        values.put("bucket_display_name", parentName);
        try {
            Uri parentUri = this.mResolver.insert(EXTERNAL_FILE_URI, values);
            if (parentUri != null) {
                return Integer.parseInt((String) parentUri.getPathSegments().get(2));
            }
            GalleryLog.w("LocalBucketAlbum", "insertDirectory fail parentUri = null");
            return -1;
        } catch (Exception e) {
            GalleryLog.w("LocalBucketAlbum", "insertDirectory Exception: " + e.toString());
            return -1;
        }
    }

    public int getBucketId() {
        return this.mBucketId;
    }

    public boolean isSdcardIconNeedShow() {
        if (this.mBucketPath == null) {
            return false;
        }
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage != null && this.mBucketPath.startsWith(innerGalleryStorage.getPath())) {
            return false;
        }
        ArrayList<GalleryStorage> outerGalleryStorageList = GalleryStorageManager.getInstance().getOuterGalleryStorageList();
        int size = outerGalleryStorageList.size();
        for (int i = 0; i < size; i++) {
            GalleryStorage galleryStorage = (GalleryStorage) outerGalleryStorageList.get(i);
            if (galleryStorage.isRemovable() && this.mBucketPath.startsWith(galleryStorage.getPath())) {
                return true;
            }
        }
        return false;
    }

    private void setPreLoadPicStatusInPrefs() {
        Editor editer = this.mApplication.getAndroidContext().getSharedPreferences("system_preload_folder", 0).edit();
        editer.putBoolean(this.mBucketPath, true);
        editer.commit();
    }

    private void resetPreLoadPicStatusInPrefs() {
        SharedPreferences systemPreLoadPreferences = this.mApplication.getAndroidContext().getSharedPreferences("system_preload_folder", 0);
        if (systemPreLoadPreferences.contains(this.mBucketPath)) {
            Editor editer = systemPreLoadPreferences.edit();
            editer.remove(this.mBucketPath);
            editer.commit();
        }
    }

    private void makeOutsideFileForWhiteListRename(File newDir) {
        try {
            if (this.mApplication.getGalleryData().isWhiteListBucketId(this.mBucketPath)) {
                File insideFile = new File(newDir, ".inside");
                File outsideFile = new File(newDir, ".outside");
                if (!outsideFile.exists() && !insideFile.exists()) {
                    if (outsideFile.createNewFile()) {
                        ContentValues values = new ContentValues();
                        values.put("_data", outsideFile.toString());
                        values.put("bucket_id", Integer.valueOf(this.mBucketId));
                        values.put("media_type", Integer.valueOf(0));
                        values.put("title", ".outside");
                        this.mApplication.getContentResolver().insert(EXTERNAL_FILE_URI, values);
                    } else {
                        GalleryLog.i("LocalBucketAlbum", "outsideFile file create failed ...");
                    }
                }
            }
        } catch (Exception e) {
            GalleryLog.i("LocalBucketAlbum", "An exception has occurred in makeOutsideFileForWhiteListRename() method." + e.getMessage());
        }
    }
}
