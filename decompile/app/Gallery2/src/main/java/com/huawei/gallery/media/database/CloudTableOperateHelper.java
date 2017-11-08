package com.huawei.gallery.media.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.provider.MediaStore.Files;
import android.text.TextUtils;
import android.util.SparseIntArray;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.BucketHelper;
import com.android.gallery3d.data.CloudLocalAlbum;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.android.gallery3d.util.MediaSetUtils;
import com.huawei.gallery.media.GalleryMedia;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.CloudRecycleUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import java.io.Closeable;
import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class CloudTableOperateHelper {
    private static final Pattern BURST_PATTERN = Pattern.compile("([^/]*)_BURST(\\d{3})_COVER.JPG$");
    private static SparseIntArray sAlbumNameResIds = new SparseIntArray();
    private static HashMap<String, String> sCloudNameMaps = new HashMap();
    private ContentResolver mContentResolver;
    private final ReentrantLock mUpdateFileNameLock = new ReentrantLock();

    private static class AlbumEntry implements Serializable {
        private static final long serialVersionUID = 1;
        public String mAlbumId;
        public String mAlbumName;
        public int mAlbumType;
        public String mRelativePath;
        public int mStatus;
        public String mTempId;

        public AlbumEntry(String albumName, String relativePath, String albumId, String tempId, int albumType, int status) {
            this.mAlbumName = albumName;
            this.mRelativePath = relativePath;
            this.mAlbumId = albumId;
            this.mTempId = tempId;
            this.mAlbumType = albumType;
            this.mStatus = status;
        }

        public int hashCode() {
            return this.mRelativePath.hashCode();
        }

        public boolean equals(Object object) {
            if (!(object instanceof AlbumEntry)) {
                return false;
            }
            return this.mRelativePath.equalsIgnoreCase(((AlbumEntry) object).mRelativePath);
        }
    }

    private static class DeleteFileInCloudFileTableThread extends Thread {
        private DeleteFileInCloudFileTableThread() {
        }

        public void run() {
            try {
                PhotoShareUtils.getServer().deleteGeneralFile();
            } catch (RemoteException e) {
                PhotoShareUtils.dealRemoteException(e);
            }
        }
    }

    public void insertCloudFileTable(android.database.sqlite.SQLiteDatabase r47, android.content.ContentValues r48, android.content.Context r49, boolean r50) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find immediate dominator for block B:58:0x015c in {2, 9, 11, 12, 22, 24, 25, 30, 32, 36, 40, 45, 47, 48, 53, 57, 60, 61, 63, 65, 66, 72, 74, 75, 83, 91, 94, 97, 99, 101, 103, 106, 111, 118, 119, 122, 124, 129, 132, 133, 135, 137, 138, 139, 140, 145, 151, 153, 154, 156, 157, 158, 159, 160} preds:[]
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.computeDominators(BlockProcessor.java:129)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.rerun(BlockProcessor.java:44)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:57)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r46 = this;
        r34 = 0;
        if (r50 == 0) goto L_0x0007;
    L_0x0004:
        r47.beginTransaction();
    L_0x0007:
        com.huawei.gallery.media.CloudLocalSyncService.startCloudSync(r49);
        r14 = r46.doInsertCloudFileTableOperation(r47, r48);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = (r14 > r4 ? 1 : (r14 == r4 ? 0 : -1));	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 != 0) goto L_0x002f;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0014:
        r4 = "CloudTableOperateHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "doInsertCloudFileTableOperation fail";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r50 == 0) goto L_0x0022;
    L_0x001f:
        r47.endTransaction();
    L_0x0022:
        if (r34 == 0) goto L_0x002e;
    L_0x0024:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
    L_0x002e:
        return;
    L_0x002f:
        r4 = "hash";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r30 = r0.getAsString(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "albumId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r17 = r0.getAsString(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "fileName";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r25 = r0.getAsString(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "photoshareLogTag";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "insertCloudFileTable hash ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r30;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = " albumId ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r17;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.printPhotoShareLog(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r36 = 0;
        r18 = 0;
        r21 = 0;
        r5 = "cloud_album";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = 2;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r6 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = "lpath";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r9 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r6[r9] = r4;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = "albumName";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r9 = 1;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r6[r9] = r4;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r7 = "albumId=?";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = 1;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r8 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r8[r4] = r17;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r9 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r10 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r11 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = r47;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r21 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        if (r21 != 0) goto L_0x00b4;
    L_0x009f:
        com.android.gallery3d.common.Utils.closeSilently(r21);
        if (r50 == 0) goto L_0x00a7;
    L_0x00a4:
        r47.endTransaction();
    L_0x00a7:
        if (r34 == 0) goto L_0x00b3;
    L_0x00a9:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
    L_0x00b3:
        return;
    L_0x00b4:
        r4 = r21.moveToNext();	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        if (r4 == 0) goto L_0x00c9;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
    L_0x00ba:
        r4 = 0;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r0 = r21;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r36 = r0.getString(r4);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r4 = 1;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r0 = r21;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r18 = r0.getString(r4);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        goto L_0x00b4;
    L_0x00c9:
        com.android.gallery3d.common.Utils.closeSilently(r21);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00cc:
        r32 = "";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r36 != 0) goto L_0x00d7;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00d1:
        if (r18 != 0) goto L_0x00d7;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00d3:
        r32 = com.huawei.gallery.recycle.utils.CloudRecycleUtils.queryBucketIdByCloudAlbumId(r17);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00d7:
        r4 = com.huawei.gallery.recycle.utils.RecycleUtils.supportRecycle();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x0173;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00dd:
        r4 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.isGUIDSupport();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x0173;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x00e3:
        r26 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r20 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r35 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r27 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r38 = com.huawei.gallery.storage.GalleryStorageManager.getInstance();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r31 = r38.getInnerGalleryStorage();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r31 != 0) goto L_0x01f3;
    L_0x00f5:
        if (r50 == 0) goto L_0x00fa;
    L_0x00f7:
        r47.endTransaction();
    L_0x00fa:
        if (r34 == 0) goto L_0x0106;
    L_0x00fc:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
    L_0x0106:
        return;
    L_0x0107:
        r24 = move-exception;
        r4 = "photoshareLogTag";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r5.<init>();	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r6 = "insertCloudFileTable query cloud album err ";	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r6 = r24.toString();	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        r5 = r5.toString();	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        com.android.gallery3d.util.GalleryLog.e(r4, r5);	 Catch:{ Exception -> 0x0107, all -> 0x015b }
        com.android.gallery3d.common.Utils.closeSilently(r21);
        goto L_0x00cc;
    L_0x012a:
        r23 = move-exception;
        r4 = "photoshareLogTag";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "insertCloudFileTable insert CloudFile Table err ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = r23.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.e(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r50 == 0) goto L_0x014e;
    L_0x014b:
        r47.endTransaction();
    L_0x014e:
        if (r34 == 0) goto L_0x015a;
    L_0x0150:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
    L_0x015a:
        return;
    L_0x015b:
        r4 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r21);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        throw r4;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0160:
        r4 = move-exception;
        if (r50 == 0) goto L_0x0166;
    L_0x0163:
        r47.endTransaction();
    L_0x0166:
        if (r34 == 0) goto L_0x0172;
    L_0x0168:
        r0 = r46;
        r5 = r0.mContentResolver;
        r6 = com.huawei.gallery.media.GalleryMedia.URI;
        r9 = 0;
        r5.notifyChange(r6, r9);
    L_0x0172:
        throw r4;
    L_0x0173:
        r0 = r30;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.getDeletedFileIdentify(r0, r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.findDeletedPhotos(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x00e3;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0181:
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "findDeletedPhotos ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r30;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "  ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r22 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r22.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "deleteFlag";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r22;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "cloud_file";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "id=?";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r9 = java.lang.String.valueOf(r14);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r10 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6[r10] = r9;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r47;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r22;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.update(r4, r1, r5, r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r47.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r39 = new com.huawei.gallery.media.database.CloudTableOperateHelper$DeleteFileInCloudFileTableThread;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r39;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r39.start();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r50 == 0) goto L_0x01e6;
    L_0x01e3:
        r47.endTransaction();
    L_0x01e6:
        if (r34 == 0) goto L_0x01f2;
    L_0x01e8:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
    L_0x01f2:
        return;
    L_0x01f3:
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "insertCloudFileTable pathName = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "; albumName = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r18;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = android.text.TextUtils.isEmpty(r36);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 == 0) goto L_0x0313;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0222:
        r4 = android.text.TextUtils.isEmpty(r32);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 == 0) goto L_0x0313;
    L_0x0228:
        com.android.gallery3d.common.Utils.closeSilently(r21);
    L_0x022b:
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "insertCloudFileTable galleryId = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r26;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "; cloudId = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r20;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "; path = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r35;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r26;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r0 != r4) goto L_0x0502;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0266:
        r4 = "localRealPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.remove(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = android.text.TextUtils.isEmpty(r18);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 != 0) goto L_0x04fc;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0274:
        r4 = sCloudNameMaps;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r18;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.containsKey(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x04f8;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x027e:
        r4 = sCloudNameMaps;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r18;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r12 = r4.get(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r12 = (java.lang.String) r12;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0288:
        r4 = android.text.TextUtils.isEmpty(r36);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 != 0) goto L_0x04ff;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x028e:
        r0 = r31;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r13 = r0.getBucketID(r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0296:
        r10 = r46;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r11 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r33 = r10.getGalleryDataContentValues(r11, r12, r13, r14);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "gallery_media";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r47;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r33;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r28 = r0.insert(r4, r5, r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "insert galleryMedia ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r28;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "  ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = com.huawei.gallery.recycle.utils.RecycleUtils.getPrintableContentValues(r48);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "new Value   ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = com.huawei.gallery.recycle.utils.RecycleUtils.getPrintableContentValues(r33);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = (r28 > r4 ? 1 : (r28 == r4 ? 0 : -1));	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x02fb;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x02f9:
        r34 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x02fb:
        if (r50 == 0) goto L_0x0300;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x02fd:
        r47.setTransactionSuccessful();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0300:
        if (r50 == 0) goto L_0x0305;
    L_0x0302:
        r47.endTransaction();
    L_0x0305:
        if (r34 == 0) goto L_0x015a;
    L_0x0307:
        r0 = r46;
        r4 = r0.mContentResolver;
        r5 = com.huawei.gallery.media.GalleryMedia.URI;
        r6 = 0;
        r4.notifyChange(r5, r6);
        goto L_0x015a;
    L_0x0313:
        if (r36 == 0) goto L_0x0441;
    L_0x0315:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "bucket_id in (";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r31;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r1 = r36;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r0.getBucketID(r1);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = ",";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r38;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r1 = r36;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r0.getOuterGalleryStorageBucketIDs(r1);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = ")";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r19 = r4.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x034b:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "hash=? and ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r19;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = " and relative_cloud_media_id = -1";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r7 = r4.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r30;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = com.huawei.gallery.photoshare.utils.PhotoShareUtils.isGUIDSupport();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 == 0) goto L_0x03c0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0374:
        r4 = "uniqueId";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r48;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r40 = r0.getAsString(r4);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = android.text.TextUtils.isEmpty(r40);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 == 0) goto L_0x048c;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0383:
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "GUID: empty uniqueId in insertCloudFileTable";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        com.android.gallery3d.util.GalleryLog.w(r4, r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "hash=? and ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r19;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = " and ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "_display_name";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "=? and relative_cloud_media_id = -1";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r7 = r4.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 2;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r30;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r25;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x03c0:
        r5 = "gallery_media";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 3;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = "_id";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r9 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6[r9] = r4;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = "cloud_media_id";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r9 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6[r9] = r4;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = "_data";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r9 = 2;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6[r9] = r4;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r9 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r10 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r11 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r47;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r21 = r4.query(r5, r6, r7, r8, r9, r10, r11);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r21 == 0) goto L_0x0228;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x03e3:
        r4 = r21.moveToNext();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 == 0) goto L_0x0228;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x03e9:
        r37 = r21.getCount();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = "photoshareLogTag";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = "hash ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r30;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = " albumId ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r17;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = " same hash count ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r37;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        com.android.gallery3d.util.GalleryLog.printPhotoShareLog(r4, r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r37;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r0 <= r4) goto L_0x04ef;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0428:
        r27 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x042a:
        r4 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r21;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r26 = r0.getInt(r4);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r21;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r20 = r0.getInt(r4);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 2;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r21;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r35 = r0.getString(r4);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        goto L_0x0228;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0441:
        r4 = android.text.TextUtils.isEmpty(r32);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        if (r4 != 0) goto L_0x045f;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0447:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "bucket_id = ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r32;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r19 = r4.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        goto L_0x034b;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x045f:
        r4 = new java.lang.Exception;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "fail to find pathName & localBucket";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        throw r4;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
    L_0x0468:
        r24 = move-exception;
        r4 = "photoshareLogTag";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = "insertCloudFileTable query gallery_media err ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r6 = r24.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.append(r6);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = r5.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        com.android.gallery3d.util.GalleryLog.e(r4, r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        com.android.gallery3d.common.Utils.closeSilently(r21);
        goto L_0x022b;
    L_0x048c:
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4.<init>();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "hash=? and ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r0 = r19;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r0);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = " and ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "_display_name";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "=? and relative_cloud_media_id = -1 and (";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "uniqueId";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = " is NULL or ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "uniqueId";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = " = '' or ";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "uniqueId";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r5 = "=?)";	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r7 = r4.toString();	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 3;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8 = new java.lang.String[r4];	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 0;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r30;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 1;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r25;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r4 = 2;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        r8[r4] = r40;	 Catch:{ Exception -> 0x0468, all -> 0x04f3 }
        goto L_0x03c0;
    L_0x04ef:
        r27 = 0;
        goto L_0x042a;
    L_0x04f3:
        r4 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r21);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        throw r4;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x04f8:
        r12 = r18;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        goto L_0x0288;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x04fc:
        r12 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        goto L_0x0288;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x04ff:
        r13 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        goto L_0x0296;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0502:
        r4 = -1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r20;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r0 != r4) goto L_0x02fb;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0507:
        r43 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r43.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "cloud_media_id";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = java.lang.Long.valueOf(r14);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "cloud_bucket_id";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "albumId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "fileType";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "fileType";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsInteger(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "fileId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "fileId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "videoThumbId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "videoThumbId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "localThumbPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "localThumbPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "localBigThumbPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "localBigThumbPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "thumbType";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 3;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "expand";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "expand";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "source";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "source";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "uniqueId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "uniqueId";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getAsString(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "dirty";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = java.lang.Integer.valueOf(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = new java.lang.String[r4];	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r44 = r0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = java.lang.String.valueOf(r26);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r44[r5] = r4;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "gallery_media";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "_id = ?";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r47;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r43;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r2 = r44;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r16 = r0.update(r4, r1, r5, r2);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "Recycle_CloudTableOpHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = "update galleryMedia affectedCount = ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r16;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.append(r0);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r5.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r16 <= 0) goto L_0x0607;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0605:
        r34 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0607:
        r4 = "localRealPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r48;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r0.getAsString(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = android.text.TextUtils.isEmpty(r4);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x061c;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0616:
        r4 = android.text.TextUtils.isEmpty(r35);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        if (r4 == 0) goto L_0x0697;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x061c:
        if (r27 == 0) goto L_0x02fb;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x061e:
        r42 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r42.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "relative_cloud_media_id";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = java.lang.Long.valueOf(r14);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r42;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = new java.lang.StringBuilder;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "hash =? AND bucket_id in(";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r31;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getBucketID(r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = ",";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r38;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r36;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = r0.getOuterGalleryStorageBucketIDs(r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = ") AND ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "_id";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = " !=? ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = r4.append(r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r45 = r4.toString();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "gallery_media";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = 2;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = new java.lang.String[r5];	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5[r6] = r30;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = java.lang.String.valueOf(r26);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r9 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5[r9] = r6;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r47;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r42;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r2 = r45;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.update(r4, r1, r2, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "CloudTableOperateHelper";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "update galleryMedia ";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        com.android.gallery3d.util.GalleryLog.d(r4, r5);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        goto L_0x02fb;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
    L_0x0697:
        r41 = new android.content.ContentValues;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r41.<init>();	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "localRealPath";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r41;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r35;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.put(r4, r1);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r4 = "cloud_file";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r5 = "id = ?";	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = 1;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6 = new java.lang.String[r6];	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r9 = java.lang.String.valueOf(r14);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r10 = 0;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r6[r10] = r9;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0 = r47;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r1 = r41;	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        r0.update(r4, r1, r5, r6);	 Catch:{ SQLiteException -> 0x012a, all -> 0x0160 }
        goto L_0x061c;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.media.database.CloudTableOperateHelper.insertCloudFileTable(android.database.sqlite.SQLiteDatabase, android.content.ContentValues, android.content.Context, boolean):void");
    }

    public CloudTableOperateHelper(ContentResolver contentResolver) {
        this.mContentResolver = contentResolver;
    }

    public static void initCloudNameMaps() {
        sCloudNameMaps.put("default-album-1", "Camera");
        sCloudNameMaps.put("default-album-2", "Screenshots");
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/DCIM/Camera"), R.string.folder_camera);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Pictures/Screenshots"), GalleryUtils.isScreenRecorderExist() ? R.string.screenshots : R.string.folder_screenshot);
        sAlbumNameResIds.put(GalleryUtils.getBucketId(BucketHelper.PRE_LOADED_PATH_PICTURE), R.string.preset_pictures);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/download"), R.string.folder_download);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Imported"), R.string.folder_imported);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/MagazineUnlock"), R.string.folder_magazine_unlock);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/EditedOnlinePhotos"), R.string.folder_edited_online_photos);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/CloudPicture"), R.string.photoshare_download);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/tencent/QQ_Images"), R.string.folder_qq_images);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/tencent/QQ_Favorite"), R.string.folder_qq_favorite);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/tencent/QzonePic"), R.string.folder_qzone);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/tencent/MicroMsg/WeiXin"), R.string.folder_qq_weixin);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/sina/weibo/save"), R.string.folder_sina_weibo_save);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/sina/weibo/weibo"), R.string.folder_sina_weibo_save);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/taobao"), R.string.folder_taobao);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/UCDownloads"), R.string.folder_ucdownloads);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/QIYIVideo"), R.string.folder_qiyi_video);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/dianping"), R.string.folder_dianping);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/MTXX"), R.string.folder_mtxx);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Photowonder"), R.string.folder_photowonder);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/MYXJ"), R.string.folder_myxj);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Pictures/InstaMag"), R.string.folder_instamag);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/MTTT"), R.string.folder_mttt);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/MomanCamera"), R.string.folder_momancamera);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Bluetooth"), R.string.folder_bluetooth);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/ShareViaWLAN"), R.string.folder_wlan);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Pictures"), R.string.pictures);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Video"), R.string.folder_video);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/百度魔拍"), R.string.folder_wondercam);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/DCIM/GroupRecorder"), R.string.folder_group_recorder);
        sAlbumNameResIds.put(GalleryUtils.getBucketId("/Pictures/Recover"), R.string.toolbarbutton_recover);
    }

    public int updateCloudFileTable(SQLiteDatabase db, ContentValues initialValues, String userWhere, String[] whereArgs, boolean isGuidUri) {
        int thumbType;
        String str;
        String hash;
        SQLiteException e;
        boolean hasRelativeWithOtherFile;
        CharSequence charSequence;
        ContentValues values;
        String titleName;
        Object data;
        String newAlbumId;
        int affectedCount;
        String updateWhere;
        boolean needUpdateDataColumn;
        ContentValues updateThumbTypeContentValues;
        int localMediaId;
        int localThumbType;
        Throwable th;
        String thumbPath = initialValues.getAsString("localThumbPath");
        String bigLcdPath = initialValues.getAsString("localBigThumbPath");
        String realPath = initialValues.getAsString("localRealPath");
        String fileName = initialValues.getAsString("fileName");
        ContentValues contentValues = new ContentValues();
        boolean filePathChanged = false;
        if (PhotoShareUtils.isFileExists(realPath)) {
            thumbType = 3;
            str = realPath;
        } else if (PhotoShareUtils.isFileExists(bigLcdPath)) {
            thumbType = 2;
            str = bigLcdPath;
            contentValues.put("localBigThumbPath", bigLcdPath);
        } else if (PhotoShareUtils.isFileExists(thumbPath)) {
            thumbType = 1;
            str = thumbPath;
            contentValues.put("localThumbPath", thumbPath);
        } else {
            thumbType = 0;
            str = null;
        }
        GalleryLog.printPhotoShareLog("photoshareLogTag", "updateCloudFileTable path " + str + " thumbType " + thumbType);
        int i = 0;
        int mediaId = 0;
        Closeable cursor = null;
        String str2 = null;
        String originalBigThumbPath = null;
        String originalRealPath = null;
        String table = "cloud_file";
        String queryWhere = TextUtils.isEmpty(userWhere) ? "1 = 1" : userWhere;
        if (PhotoShareUtils.isGUIDSupport() && isGuidUri) {
            table = "general_cloud_file";
            queryWhere = queryWhere + " AND recycleFlag = 2";
        }
        if (thumbType == 0 && TextUtils.isEmpty(fileName)) {
            hash = null;
        } else {
            try {
                cursor = db.query(table, new String[]{"id", "localThumbPath", "localBigThumbPath", "localRealPath", "hash", "albumId"}, queryWhere, whereArgs, null, null, null);
                if (cursor == null) {
                    hash = null;
                } else if (cursor.moveToNext()) {
                    mediaId = cursor.getInt(0);
                    String originalThumbPath = cursor.getString(1);
                    originalBigThumbPath = cursor.getString(2);
                    originalRealPath = cursor.getString(3);
                    if (thumbType != 0) {
                        if (thumbType == 1 && str != null && !str.equalsIgnoreCase(originalThumbPath)) {
                            filePathChanged = true;
                        } else if (thumbType == 2 && str != null && !str.equalsIgnoreCase(originalBigThumbPath)) {
                            filePathChanged = true;
                        } else if (thumbType != 3 || str == null || str.equalsIgnoreCase(originalRealPath)) {
                            GalleryLog.printPhotoShareLog("photoshareLogTag", "filePathChanged false");
                        } else {
                            filePathChanged = true;
                        }
                    }
                    hash = cursor.getString(4);
                    try {
                        str2 = cursor.getString(5);
                    } catch (SQLiteException e2) {
                        e = e2;
                        try {
                            GalleryLog.d("photoshareLogTag", "updateCloudFileTable query cloud_file exception " + e.toString());
                            Utils.closeSilently(cursor);
                            if (!TextUtils.isEmpty(fileName)) {
                                hasRelativeWithOtherFile = false;
                                this.mUpdateFileNameLock.lock();
                                try {
                                    hasRelativeWithOtherFile = relativeWithOtherFile(db, fileName, hash, str2, mediaId);
                                } catch (Exception e3) {
                                    GalleryLog.d("Recycle_CloudTableOpHelper", "relativeWithOtherFile  e=" + e3.getMessage());
                                } finally {
                                }
                                if (hasRelativeWithOtherFile) {
                                    charSequence = null;
                                    try {
                                        values = new ContentValues();
                                        values.put("dirty", Integer.valueOf(0));
                                        if (!TextUtils.isEmpty(fileName)) {
                                            if (fileName.lastIndexOf(".") != -1) {
                                                titleName = fileName.substring(0, fileName.lastIndexOf("."));
                                                values.put("_display_name", fileName);
                                                values.put("title", titleName);
                                                values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                                                if (RecycleUtils.supportRecycle()) {
                                                    if (initialValues.containsKey("localThumbPath")) {
                                                        values.put("localThumbPath", thumbPath);
                                                    }
                                                    if (initialValues.containsKey("localBigThumbPath")) {
                                                        values.put("localBigThumbPath", bigLcdPath);
                                                    }
                                                    if (!!TextUtils.isEmpty(realPath)) {
                                                    }
                                                    if (!!TextUtils.isEmpty(bigLcdPath)) {
                                                    }
                                                    data = thumbPath;
                                                    if (!TextUtils.isEmpty(charSequence)) {
                                                        values.put("_data", charSequence);
                                                    }
                                                    newAlbumId = (String) initialValues.get("albumId");
                                                    if (!TextUtils.isEmpty(newAlbumId)) {
                                                        values.put("cloud_bucket_id", newAlbumId);
                                                    }
                                                    if (initialValues.containsKey("uniqueId")) {
                                                        values.put("uniqueId", (String) initialValues.get("uniqueId"));
                                                    }
                                                }
                                                affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                                                GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                                                if (affectedCount > 0) {
                                                    this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                                                }
                                            }
                                        }
                                        titleName = fileName;
                                        values.put("_display_name", fileName);
                                        values.put("title", titleName);
                                        values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                                        if (RecycleUtils.supportRecycle()) {
                                            if (initialValues.containsKey("localThumbPath")) {
                                                values.put("localThumbPath", thumbPath);
                                            }
                                            if (initialValues.containsKey("localBigThumbPath")) {
                                                values.put("localBigThumbPath", bigLcdPath);
                                            }
                                            if (!TextUtils.isEmpty(realPath)) {
                                            }
                                            if (!TextUtils.isEmpty(bigLcdPath)) {
                                            }
                                            data = thumbPath;
                                            if (TextUtils.isEmpty(charSequence)) {
                                                values.put("_data", charSequence);
                                            }
                                            newAlbumId = (String) initialValues.get("albumId");
                                            if (TextUtils.isEmpty(newAlbumId)) {
                                                values.put("cloud_bucket_id", newAlbumId);
                                            }
                                            if (initialValues.containsKey("uniqueId")) {
                                                values.put("uniqueId", (String) initialValues.get("uniqueId"));
                                            }
                                        }
                                        affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                                        GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                                        if (affectedCount > 0) {
                                            this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                                        }
                                    } catch (SQLiteException e4) {
                                        GalleryLog.d("photoshareLogTag", "updateCloudFileTable update fileName error " + e4.toString() + " data = " + null);
                                    }
                                } else {
                                    GalleryLog.d("photoshareLogTag", "fileName " + fileName + " hash " + hash + " albumId " + str2);
                                }
                            }
                            cursor = null;
                            db.beginTransaction();
                            try {
                                updateWhere = TextUtils.isEmpty(userWhere) ? userWhere : "1 = 1";
                                updateWhere = updateWhere + " AND cloud_file.uniqueId = (SELECT uniqueId FROM cloud_recycled_file WHERE cloud_recycled_file.uniqueId = cloud_file.uniqueId" + " AND recycleFlag = 2" + ")";
                                i = db.update("cloud_file", initialValues, updateWhere, whereArgs);
                                db.delete("cloud_recycled_file", userWhere, whereArgs);
                                db.setTransactionSuccessful();
                            } catch (SQLiteException e42) {
                                GalleryLog.w("Recycle_CloudTableOpHelper", "update cloud file error: " + e42.getMessage());
                                needUpdateDataColumn = true;
                                updateThumbTypeContentValues = new ContentValues();
                                cursor = db.query("gallery_media", new String[]{"local_media_id", "thumbType"}, "cloud_media_id=?", new String[]{String.valueOf(mediaId)}, null, null, null);
                                localMediaId = cursor.getInt(0);
                                localThumbType = cursor.getInt(1);
                                if (localMediaId == -1) {
                                    needUpdateDataColumn = false;
                                } else if (localThumbType >= thumbType) {
                                    needUpdateDataColumn = false;
                                }
                                if (localThumbType < thumbType) {
                                    updateThumbTypeContentValues.put("thumbType", Integer.valueOf(thumbType));
                                }
                                if (contentValues.size() > 0) {
                                    affectedCount = db.update("gallery_media", contentValues, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                                    if (affectedCount > 0) {
                                        this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                                    }
                                }
                                if (needUpdateDataColumn) {
                                    updateThumbTypeContentValues.put("_data", str);
                                    updateThumbTypeContentValues.put("date_modified", Long.valueOf(getLastModify(str)));
                                    GalleryUtils.resolveWidthAndHeight(updateThumbTypeContentValues, str);
                                    db.update("gallery_media", updateThumbTypeContentValues, "cloud_media_id=? AND thumbType<?", new String[]{String.valueOf(mediaId), String.valueOf(thumbType)});
                                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; thumbType = " + thumbType);
                                }
                                Utils.closeSilently(cursor);
                                return i;
                            } finally {
                                db.endTransaction();
                            }
                            needUpdateDataColumn = true;
                            updateThumbTypeContentValues = new ContentValues();
                            cursor = db.query("gallery_media", new String[]{"local_media_id", "thumbType"}, "cloud_media_id=?", new String[]{String.valueOf(mediaId)}, null, null, null);
                            localMediaId = cursor.getInt(0);
                            localThumbType = cursor.getInt(1);
                            if (localMediaId == -1) {
                                needUpdateDataColumn = false;
                            } else if (localThumbType >= thumbType) {
                                needUpdateDataColumn = false;
                            }
                            if (localThumbType < thumbType) {
                                updateThumbTypeContentValues.put("thumbType", Integer.valueOf(thumbType));
                            }
                            if (contentValues.size() > 0) {
                                affectedCount = db.update("gallery_media", contentValues, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                                GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                                if (affectedCount > 0) {
                                    this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                                }
                            }
                            if (needUpdateDataColumn) {
                                updateThumbTypeContentValues.put("_data", str);
                                updateThumbTypeContentValues.put("date_modified", Long.valueOf(getLastModify(str)));
                                GalleryUtils.resolveWidthAndHeight(updateThumbTypeContentValues, str);
                                db.update("gallery_media", updateThumbTypeContentValues, "cloud_media_id=? AND thumbType<?", new String[]{String.valueOf(mediaId), String.valueOf(thumbType)});
                                GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; thumbType = " + thumbType);
                            }
                            Utils.closeSilently(cursor);
                            return i;
                        } catch (Throwable th2) {
                            th = th2;
                            Utils.closeSilently(cursor);
                            throw th;
                        }
                    }
                } else {
                    hash = null;
                }
                Utils.closeSilently(cursor);
            } catch (SQLiteException e5) {
                e42 = e5;
                hash = null;
                GalleryLog.d("photoshareLogTag", "updateCloudFileTable query cloud_file exception " + e42.toString());
                Utils.closeSilently(cursor);
                if (TextUtils.isEmpty(fileName)) {
                    hasRelativeWithOtherFile = false;
                    this.mUpdateFileNameLock.lock();
                    hasRelativeWithOtherFile = relativeWithOtherFile(db, fileName, hash, str2, mediaId);
                    if (hasRelativeWithOtherFile) {
                        GalleryLog.d("photoshareLogTag", "fileName " + fileName + " hash " + hash + " albumId " + str2);
                    } else {
                        charSequence = null;
                        values = new ContentValues();
                        values.put("dirty", Integer.valueOf(0));
                        if (TextUtils.isEmpty(fileName)) {
                            if (fileName.lastIndexOf(".") != -1) {
                                titleName = fileName.substring(0, fileName.lastIndexOf("."));
                                values.put("_display_name", fileName);
                                values.put("title", titleName);
                                values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                                if (RecycleUtils.supportRecycle()) {
                                    if (initialValues.containsKey("localThumbPath")) {
                                        values.put("localThumbPath", thumbPath);
                                    }
                                    if (initialValues.containsKey("localBigThumbPath")) {
                                        values.put("localBigThumbPath", bigLcdPath);
                                    }
                                    if (!TextUtils.isEmpty(realPath)) {
                                    }
                                    if (!TextUtils.isEmpty(bigLcdPath)) {
                                    }
                                    data = thumbPath;
                                    if (TextUtils.isEmpty(charSequence)) {
                                        values.put("_data", charSequence);
                                    }
                                    newAlbumId = (String) initialValues.get("albumId");
                                    if (TextUtils.isEmpty(newAlbumId)) {
                                        values.put("cloud_bucket_id", newAlbumId);
                                    }
                                    if (initialValues.containsKey("uniqueId")) {
                                        values.put("uniqueId", (String) initialValues.get("uniqueId"));
                                    }
                                }
                                affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                                GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                                if (affectedCount > 0) {
                                    this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                                }
                            }
                        }
                        titleName = fileName;
                        values.put("_display_name", fileName);
                        values.put("title", titleName);
                        values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                        if (RecycleUtils.supportRecycle()) {
                            if (initialValues.containsKey("localThumbPath")) {
                                values.put("localThumbPath", thumbPath);
                            }
                            if (initialValues.containsKey("localBigThumbPath")) {
                                values.put("localBigThumbPath", bigLcdPath);
                            }
                            if (!TextUtils.isEmpty(realPath)) {
                            }
                            if (!TextUtils.isEmpty(bigLcdPath)) {
                            }
                            data = thumbPath;
                            if (TextUtils.isEmpty(charSequence)) {
                                values.put("_data", charSequence);
                            }
                            newAlbumId = (String) initialValues.get("albumId");
                            if (TextUtils.isEmpty(newAlbumId)) {
                                values.put("cloud_bucket_id", newAlbumId);
                            }
                            if (initialValues.containsKey("uniqueId")) {
                                values.put("uniqueId", (String) initialValues.get("uniqueId"));
                            }
                        }
                        affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                        GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                        if (affectedCount > 0) {
                            this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                        }
                    }
                }
                cursor = null;
                db.beginTransaction();
                if (TextUtils.isEmpty(userWhere)) {
                }
                updateWhere = updateWhere + " AND cloud_file.uniqueId = (SELECT uniqueId FROM cloud_recycled_file WHERE cloud_recycled_file.uniqueId = cloud_file.uniqueId" + " AND recycleFlag = 2" + ")";
                i = db.update("cloud_file", initialValues, updateWhere, whereArgs);
                db.delete("cloud_recycled_file", userWhere, whereArgs);
                db.setTransactionSuccessful();
                needUpdateDataColumn = true;
                updateThumbTypeContentValues = new ContentValues();
                cursor = db.query("gallery_media", new String[]{"local_media_id", "thumbType"}, "cloud_media_id=?", new String[]{String.valueOf(mediaId)}, null, null, null);
                localMediaId = cursor.getInt(0);
                localThumbType = cursor.getInt(1);
                if (localMediaId == -1) {
                    needUpdateDataColumn = false;
                } else if (localThumbType >= thumbType) {
                    needUpdateDataColumn = false;
                }
                if (localThumbType < thumbType) {
                    updateThumbTypeContentValues.put("thumbType", Integer.valueOf(thumbType));
                }
                if (contentValues.size() > 0) {
                    affectedCount = db.update("gallery_media", contentValues, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                    if (affectedCount > 0) {
                        this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                    }
                }
                if (needUpdateDataColumn) {
                    updateThumbTypeContentValues.put("_data", str);
                    updateThumbTypeContentValues.put("date_modified", Long.valueOf(getLastModify(str)));
                    GalleryUtils.resolveWidthAndHeight(updateThumbTypeContentValues, str);
                    db.update("gallery_media", updateThumbTypeContentValues, "cloud_media_id=? AND thumbType<?", new String[]{String.valueOf(mediaId), String.valueOf(thumbType)});
                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; thumbType = " + thumbType);
                }
                Utils.closeSilently(cursor);
                return i;
            } catch (Throwable th3) {
                th = th3;
                hash = null;
                Utils.closeSilently(cursor);
                throw th;
            }
        }
        if (TextUtils.isEmpty(fileName)) {
            hasRelativeWithOtherFile = false;
            this.mUpdateFileNameLock.lock();
            hasRelativeWithOtherFile = relativeWithOtherFile(db, fileName, hash, str2, mediaId);
            if (hasRelativeWithOtherFile) {
                GalleryLog.d("photoshareLogTag", "fileName " + fileName + " hash " + hash + " albumId " + str2);
            } else {
                charSequence = null;
                values = new ContentValues();
                values.put("dirty", Integer.valueOf(0));
                if (TextUtils.isEmpty(fileName)) {
                    if (fileName.lastIndexOf(".") != -1) {
                        titleName = fileName.substring(0, fileName.lastIndexOf("."));
                        values.put("_display_name", fileName);
                        values.put("title", titleName);
                        values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                        if (RecycleUtils.supportRecycle()) {
                            if (initialValues.containsKey("localThumbPath")) {
                                values.put("localThumbPath", thumbPath);
                            }
                            if (initialValues.containsKey("localBigThumbPath")) {
                                values.put("localBigThumbPath", bigLcdPath);
                            }
                            if (!TextUtils.isEmpty(realPath) && PhotoShareUtils.isFileExists(realPath)) {
                                charSequence = realPath;
                            } else if (!TextUtils.isEmpty(bigLcdPath) && TextUtils.isEmpty(originalRealPath)) {
                                data = bigLcdPath;
                            } else if (!TextUtils.isEmpty(thumbPath) && TextUtils.isEmpty(originalRealPath) && TextUtils.isEmpty(originalBigThumbPath)) {
                                data = thumbPath;
                            }
                            if (TextUtils.isEmpty(charSequence)) {
                                values.put("_data", charSequence);
                            }
                            newAlbumId = (String) initialValues.get("albumId");
                            if (TextUtils.isEmpty(newAlbumId)) {
                                values.put("cloud_bucket_id", newAlbumId);
                            }
                            if (initialValues.containsKey("uniqueId")) {
                                values.put("uniqueId", (String) initialValues.get("uniqueId"));
                            }
                        }
                        affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                        GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                        if (affectedCount > 0) {
                            this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                        }
                    }
                }
                titleName = fileName;
                values.put("_display_name", fileName);
                values.put("title", titleName);
                values.put("last_update_time", Long.valueOf(System.currentTimeMillis()));
                if (RecycleUtils.supportRecycle()) {
                    if (initialValues.containsKey("localThumbPath")) {
                        values.put("localThumbPath", thumbPath);
                    }
                    if (initialValues.containsKey("localBigThumbPath")) {
                        values.put("localBigThumbPath", bigLcdPath);
                    }
                    if (!TextUtils.isEmpty(realPath)) {
                    }
                    if (!TextUtils.isEmpty(bigLcdPath)) {
                    }
                    data = thumbPath;
                    if (TextUtils.isEmpty(charSequence)) {
                        values.put("_data", charSequence);
                    }
                    newAlbumId = (String) initialValues.get("albumId");
                    if (TextUtils.isEmpty(newAlbumId)) {
                        values.put("cloud_bucket_id", newAlbumId);
                    }
                    if (initialValues.containsKey("uniqueId")) {
                        values.put("uniqueId", (String) initialValues.get("uniqueId"));
                    }
                }
                affectedCount = db.update("gallery_media", values, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                if (affectedCount > 0) {
                    this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                }
            }
        }
        cursor = null;
        try {
            db.beginTransaction();
            if (TextUtils.isEmpty(userWhere)) {
            }
            if (PhotoShareUtils.isGUIDSupport() && isGuidUri) {
                updateWhere = updateWhere + " AND cloud_file.uniqueId = (SELECT uniqueId FROM cloud_recycled_file WHERE cloud_recycled_file.uniqueId = cloud_file.uniqueId" + " AND recycleFlag = 2" + ")";
            }
            i = db.update("cloud_file", initialValues, updateWhere, whereArgs);
            if (PhotoShareUtils.isGUIDSupport() && isGuidUri && i > 0) {
                db.delete("cloud_recycled_file", userWhere, whereArgs);
            }
            db.setTransactionSuccessful();
            if (i > 0 && mediaId != 0 && !TextUtils.isEmpty(str) && filePathChanged) {
                needUpdateDataColumn = true;
                updateThumbTypeContentValues = new ContentValues();
                cursor = db.query("gallery_media", new String[]{"local_media_id", "thumbType"}, "cloud_media_id=?", new String[]{String.valueOf(mediaId)}, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    localMediaId = cursor.getInt(0);
                    localThumbType = cursor.getInt(1);
                    if (localMediaId == -1) {
                        needUpdateDataColumn = false;
                    } else if (localThumbType >= thumbType) {
                        needUpdateDataColumn = false;
                    }
                    if (localThumbType < thumbType) {
                        updateThumbTypeContentValues.put("thumbType", Integer.valueOf(thumbType));
                    }
                }
                if (contentValues.size() > 0) {
                    affectedCount = db.update("gallery_media", contentValues, "cloud_media_id=?", new String[]{String.valueOf(mediaId)});
                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; affectedCount = " + affectedCount);
                    if (affectedCount > 0) {
                        this.mContentResolver.notifyChange(GalleryMedia.URI, null);
                    }
                }
                if (needUpdateDataColumn) {
                    updateThumbTypeContentValues.put("_data", str);
                    updateThumbTypeContentValues.put("date_modified", Long.valueOf(getLastModify(str)));
                    GalleryUtils.resolveWidthAndHeight(updateThumbTypeContentValues, str);
                    db.update("gallery_media", updateThumbTypeContentValues, "cloud_media_id=? AND thumbType<?", new String[]{String.valueOf(mediaId), String.valueOf(thumbType)});
                    GalleryLog.d("Recycle_CloudTableOpHelper", "updateCloudFile mediaId = " + mediaId + "; thumbType = " + thumbType);
                }
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e422) {
            GalleryLog.d("photoshareLogTag", "updateCloudFileTable exception " + e422.toString());
        } catch (Throwable th4) {
        }
        return i;
        this.mUpdateFileNameLock.unlock();
        Utils.closeSilently(null);
    }

    private boolean relativeWithOtherFile(SQLiteDatabase db, String fileName, String hash, String albumId, int cloudMediaId) {
        Closeable closeable = null;
        String str = null;
        boolean z;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("cloud_album", new String[]{"lpath", "albumName"}, "albumId=?", new String[]{albumId}, null, null, null);
            if (closeable == null) {
                z = false;
                return z;
            }
            if (closeable.moveToNext()) {
                str = closeable.getString(0);
            }
            Utils.closeSilently(closeable);
            GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
            int newRelativeId = -1;
            int cloudId = -1;
            try {
                sQLiteDatabase = db;
                closeable = sQLiteDatabase.query("gallery_media", new String[]{"_id", "cloud_media_id"}, "hash=? and _display_name =? and bucket_id in(" + storageManager.getInnerGalleryStorage().getBucketID(str) + "," + storageManager.getOuterGalleryStorageBucketIDs(str) + ")", new String[]{hash, fileName}, null, null, null);
                if (closeable == null) {
                    z = false;
                    return z;
                }
                while (closeable.moveToNext()) {
                    newRelativeId = closeable.getInt(0);
                    cloudId = closeable.getInt(1);
                    if (cloudId == -1) {
                        break;
                    }
                }
                Utils.closeSilently(closeable);
                if (newRelativeId == -1 || cloudId != -1) {
                    return false;
                }
                GalleryLog.d("CloudTableOperateHelper", "insertNewValues");
                insertNewValues(db, cloudMediaId, newRelativeId);
                return true;
            } catch (SQLiteException e) {
                z = "photoshareLogTag";
                GalleryLog.e(z, "relativeWithOtherFile query gallery_media err " + e.toString());
            } finally {
                Utils.closeSilently(closeable);
            }
        } catch (SQLiteException e2) {
            z = "photoshareLogTag";
            GalleryLog.e(z, "relativeWithOtherFile query cloud album err " + e2.toString());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    private void insertNewValues(SQLiteDatabase db, int cloudMediaId, int newRelativeId) {
        Closeable closeable = null;
        db.beginTransaction();
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("gallery_media", new String[]{"cloud_media_id", "cloud_bucket_id", "fileType", "fileId", "videoThumbId", "thumbType", "localThumbPath", "localBigThumbPath", "expand", "source", "_id"}, "cloud_media_id=?", new String[]{String.valueOf(cloudMediaId)}, null, null, null);
            if (closeable != null) {
                String galleryIdsClause;
                int cloudId = -1;
                String cloudBucketId = null;
                int fileType = 0;
                String fileId = null;
                String videoThumbId = null;
                int thumbType = 0;
                String localThumbPath = null;
                String localBigPath = null;
                String expand = null;
                String source = null;
                StringBuffer sb = new StringBuffer();
                while (closeable.moveToNext()) {
                    if (closeable.isFirst()) {
                        cloudId = closeable.getInt(0);
                        cloudBucketId = closeable.getString(1);
                        fileType = closeable.getInt(2);
                        fileId = closeable.getString(3);
                        videoThumbId = closeable.getString(4);
                        thumbType = closeable.getInt(5);
                        localThumbPath = closeable.getString(6);
                        localBigPath = closeable.getString(7);
                        expand = closeable.getString(8);
                        source = closeable.getString(9);
                    }
                    sb.append(",").append(closeable.getInt(10));
                }
                if (sb.length() > 1) {
                    galleryIdsClause = sb.substring(1);
                } else {
                    galleryIdsClause = "";
                    GalleryLog.printPhotoShareLog("photoshareLogTag", "galleryIdsClause is empty");
                }
                ContentValues values = new ContentValues();
                values.put("cloud_media_id", Integer.valueOf(cloudId));
                values.put("cloud_bucket_id", cloudBucketId);
                values.put("fileType", Integer.valueOf(fileType));
                values.put("fileId", fileId);
                values.put("videoThumbId", videoThumbId);
                values.put("thumbType", Integer.valueOf(thumbType));
                values.put("localThumbPath", localThumbPath);
                values.put("localBigThumbPath", localBigPath);
                values.put("expand", expand);
                values.put("source", source);
                values.put("relative_cloud_media_id", Integer.valueOf(-1));
                SQLiteDatabase sQLiteDatabase2 = db;
                ContentValues contentValues = values;
                GalleryLog.printPhotoShareLog("photoshareLogTag", "new relativeId " + newRelativeId + " update new Values affect count " + sQLiteDatabase2.update("gallery_media", contentValues, "_id= ? ", new String[]{String.valueOf(newRelativeId)}));
                updateOldValues(db, cloudMediaId, galleryIdsClause);
                db.setTransactionSuccessful();
                db.endTransaction();
                Utils.closeSilently(closeable);
            }
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "insertNewValues err " + e.toString());
        } finally {
            db.endTransaction();
            Utils.closeSilently(closeable);
        }
    }

    private void updateOldValues(SQLiteDatabase db, int cloudId, String idsClause) {
        ContentValues values = createDefaultCloudValues();
        values.put("relative_cloud_media_id", Integer.valueOf(cloudId));
        GalleryLog.printPhotoShareLog("photoshareLogTag", "galleryMediaId " + idsClause + " update old " + "Values affect count " + db.update("gallery_media", values, "_id IN (" + idsClause + ")", null));
    }

    public int clearData(SQLiteDatabase db) {
        db.delete("cloud_album", null, null);
        int count = db.delete("cloud_file", null, null);
        db.delete("cloud_recycled_file", null, null);
        db.delete("auto_upload_album", null, null);
        db.update("gallery_media", createDefaultCloudValues(), "cloud_media_id !=-1 and local_media_id !=-1", null);
        db.delete("gallery_media", "cloud_media_id !=-1", null);
        this.mContentResolver.notifyChange(GalleryMedia.URI, null);
        return count;
    }

    private ContentValues createDefaultCloudValues() {
        ContentValues values = new ContentValues();
        values.put("cloud_media_id", Integer.valueOf(-1));
        values.put("cloud_bucket_id", "");
        values.put("dirty", Integer.valueOf(0));
        values.put("fileType", Integer.valueOf(0));
        values.put("fileId", "");
        values.put("videoThumbId", "");
        values.put("thumbType", Integer.valueOf(0));
        values.put("localThumbPath", "");
        values.put("localBigThumbPath", "");
        values.put("expand", "");
        values.put("source", "");
        values.put("uniqueId", "");
        return values;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int deleteCloudAlbum(SQLiteDatabase db, String whereClause, String[] whereArgs) {
        Closeable closeable = null;
        String str = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("cloud_album", new String[]{"albumId", "lpath"}, whereClause, whereArgs, null, null, null);
            StringBuffer stringBuffer = new StringBuffer();
            HashMap<String, String> albumPathId = new HashMap();
            while (closeable != null && closeable.moveToNext()) {
                String albumID = closeable.getString(0);
                String relativePath = closeable.getString(1);
                stringBuffer.append(",").append("\"").append(albumID).append("\"");
                albumPathId.put(relativePath, albumID);
            }
            if (stringBuffer.length() > 0) {
                str = stringBuffer.substring(1);
            }
            if (RecycleUtils.supportRecycle() && albumPathId.size() > 0) {
                RecycleUtils.setPreferenceValue(RecycleUtils.CLOUD_BUCKET_ALBUM_ID, PhotoShareUtils.getCloudALbumBucketId(albumPathId));
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "deleteCloudAlbum exception" + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
        if (str != null) {
            try {
                String[] strArr = new String[]{"id", "tempId"};
                String str2 = "albumId in (" + str + ") and albumType !=1 and albumType !=2";
                closeable = db.query("auto_upload_album", strArr, str2, null, null, null, null);
                while (closeable != null && closeable.moveToNext()) {
                    int id = closeable.getInt(0);
                    String updatedAlbumId = "default-album-200-" + closeable.getString(1) + "-" + String.valueOf(System.currentTimeMillis());
                    ContentValues contentValues = new ContentValues();
                    contentValues.put("albumId", updatedAlbumId);
                    SQLiteDatabase sQLiteDatabase2 = db;
                    ContentValues contentValues2 = contentValues;
                    sQLiteDatabase2.update("auto_upload_album", contentValues2, "id = ?", new String[]{String.valueOf(id)});
                }
                Utils.closeSilently(closeable);
            } catch (SQLiteException e2) {
                GalleryLog.e("photoshareLogTag", "deleteCloudAlbum update autoUploadAlbum exception " + e2.toString());
            } catch (Throwable th2) {
                Utils.closeSilently(closeable);
            }
        }
        return db.delete("cloud_album", whereClause, whereArgs);
    }

    public int deleteCloudFile(Context context, SQLiteDatabase db, String whereClause, String[] whereArgs, boolean byRestoreFail) {
        StringBuffer sb;
        Closeable closeable = null;
        CharSequence cloudIdClause = null;
        int count = 0;
        String table = "cloud_file";
        String id = "id";
        String queryWhere = TextUtils.isEmpty(whereClause) ? "1 = 1" : whereClause;
        if (PhotoShareUtils.isGUIDSupport()) {
            table = "general_cloud_file";
            if (byRestoreFail) {
                queryWhere = queryWhere + " AND recycleFlag = 2";
            } else {
                queryWhere = queryWhere + " AND (recycleFlag != 2 OR recycleFlag IS NULL)";
            }
        }
        try {
            closeable = db.query(table, new String[]{id}, queryWhere, whereArgs, null, null, null);
            sb = new StringBuffer();
            while (closeable != null && closeable.moveToNext()) {
                sb.append(",").append(closeable.getString(0));
            }
            if (sb.length() > 1) {
                cloudIdClause = sb.substring(1);
            } else {
                cloudIdClause = "";
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "deleteCloudFile query cloud file exception " + e.toString());
        } catch (Throwable th) {
        }
        if (TextUtils.isEmpty(cloudIdClause)) {
            return count;
        }
        GalleryLog.d("Recycle_CloudTableOpHelper", "deleteCloudFile cloudIdClause = " + cloudIdClause);
        CharSequence localIdClause = null;
        try {
            closeable = db.query("gallery_media", new String[]{"local_media_id"}, "cloud_media_id in (" + cloudIdClause + ") and local_media_id !=-1", null, null, null, null);
            sb = new StringBuffer();
            while (closeable != null && closeable.moveToNext()) {
                sb.append(",").append(closeable.getString(0));
            }
            if (sb.length() > 1) {
                localIdClause = sb.substring(1);
            } else {
                localIdClause = "";
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e2) {
            GalleryLog.e("photoshareLogTag", "deleteCloudFile query gallery media exception" + e2.toString());
        } catch (Throwable th2) {
        }
        if (!TextUtils.isEmpty(localIdClause)) {
            GalleryLog.d("Recycle_CloudTableOpHelper", "deleteCloudFile localIdClause = " + cloudIdClause);
            context.getContentResolver().delete(Files.getContentUri("external"), "_id in (" + localIdClause + ")", null);
        }
        int affectedCount = db.delete("gallery_media", "cloud_media_id in (" + cloudIdClause + ")", null);
        if (affectedCount > 0) {
            GalleryLog.d("Recycle_CloudTableOpHelper", "deleteCloudFile affectedCount = " + affectedCount);
            this.mContentResolver.notifyChange(GalleryMedia.URI, null);
        }
        db.beginTransaction();
        try {
            if (PhotoShareUtils.isGUIDSupport()) {
                count = db.delete("cloud_file", "id IN (" + cloudIdClause + ")", null);
                if (byRestoreFail && count > 0) {
                    db.delete("cloud_recycled_file", whereClause, whereArgs);
                }
            } else {
                count = db.delete("cloud_file", whereClause, whereArgs);
            }
            db.setTransactionSuccessful();
        } catch (SQLiteException e22) {
            GalleryLog.d("Recycle_CloudTableOpHelper", "delete cloud and recycle error: " + e22.getMessage());
        } finally {
            db.endTransaction();
        }
        return count;
        Utils.closeSilently(closeable);
        Utils.closeSilently(closeable);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public long insertAutoUploadAlbumTable(SQLiteDatabase db, ContentValues initialValues) {
        String relativePath = initialValues.getAsString("relativePath");
        String albumId = initialValues.getAsString("albumId");
        if (!(TextUtils.isEmpty(albumId) || TextUtils.isEmpty(relativePath))) {
            try {
                SQLiteDatabase sQLiteDatabase = db;
                Closeable cursor = sQLiteDatabase.query("cloud_album", new String[]{"albumId"}, "lpath=?", new String[]{relativePath}, null, null, null);
                if (cursor != null && cursor.moveToNext()) {
                    String realAlbumId = cursor.getString(0);
                    if (!albumId.equalsIgnoreCase(realAlbumId)) {
                        initialValues.remove("albumId");
                        initialValues.put("albumId", realAlbumId);
                    }
                }
                Utils.closeSilently(cursor);
            } catch (SQLiteException e) {
                GalleryLog.d("photoshareLogTag", "insertAutoUploadAlbumTable query cloud_album error" + e.toString());
            } catch (Throwable th) {
                Utils.closeSilently(null);
            }
        }
        return db.insert("auto_upload_album", null, initialValues);
    }

    public long insertCloudAlbumTable(SQLiteDatabase db, ContentValues initialValues) {
        long rowId = db.insert("cloud_album", null, initialValues);
        String lPath = initialValues.getAsString("lpath");
        String albumId = initialValues.getAsString("albumId");
        ContentValues updateAlbumIdValues = new ContentValues();
        updateAlbumIdValues.put("albumId", albumId);
        db.update("auto_upload_album", updateAlbumIdValues, "relativePath=?", new String[]{lPath});
        updateAlbumIdValues.put("albumId", albumId);
        db.update("history_album_id", updateAlbumIdValues, "relativePath=?", new String[]{lPath});
        PhotoShareUtils.initialCloudAlbumBucketId();
        return rowId;
    }

    private long doInsertCloudFileTableOperation(SQLiteDatabase db, ContentValues initialValues) {
        if (initialValues != null && initialValues.size() > 0) {
            String localRealPath = initialValues.getAsString("localRealPath");
            if (!(TextUtils.isEmpty(localRealPath) || PhotoShareUtils.isFileExists(localRealPath))) {
                initialValues.remove("localRealPath");
            }
        }
        if (CloudRecycleUtils.queryInsertPermission(db, initialValues.getAsString("uniqueId"))) {
            long id = -1;
            try {
                id = db.insertWithOnConflict("cloud_file", null, initialValues, 0);
            } catch (SQLException e) {
                GalleryLog.printPhotoShareLog("photoshareLogTag", "CloudFile Error inserting " + e.toString());
            }
            return id;
        }
        GalleryLog.d("Recycle_CloudTableOpHelper", "forbidden insert cloud file");
        return -1;
    }

    public static String genDefaultFilePath(String albumId, String hash) {
        return "cloud-" + albumId + "-" + hash;
    }

    private ContentValues getGalleryDataContentValues(ContentValues initialValues, String albumName, int bucketId, long id) {
        String titleName;
        String localFilePath;
        int thumbType;
        ContentValues galleryMediaValues = new ContentValues();
        String localThumbPath = initialValues.getAsString("localThumbPath");
        String localBigThumbPath = initialValues.getAsString("localBigThumbPath");
        String localRealPath = initialValues.getAsString("localRealPath");
        Long size = initialValues.getAsLong("size");
        Long createTime = initialValues.getAsLong("createTime");
        String fileName = initialValues.getAsString("fileName");
        int fileType = initialValues.getAsInteger("fileType").intValue();
        String uniqueId = initialValues.getAsString("uniqueId");
        String mimeType = getMimeType(fileType);
        double latitude = 0.0d;
        if (initialValues.getAsDouble("latitude") != null) {
            latitude = initialValues.getAsDouble("latitude").doubleValue();
        }
        double longitude = 0.0d;
        if (initialValues.getAsDouble("longitude") != null) {
            longitude = initialValues.getAsDouble("longitude").doubleValue();
        }
        if (TextUtils.isEmpty(fileName) || fileName.lastIndexOf(".") == -1) {
            titleName = fileName;
        } else {
            titleName = fileName.substring(0, fileName.lastIndexOf("."));
        }
        int orientation = 0;
        if (initialValues.getAsInteger("orientation") != null) {
            orientation = initialValues.getAsInteger("orientation").intValue();
        }
        int rotate = PhotoShareUtils.getOrientation(orientation);
        String albumId = initialValues.getAsString("albumId");
        String hash = initialValues.getAsString("hash");
        if (!TextUtils.isEmpty(localRealPath)) {
            localFilePath = localRealPath;
            thumbType = 3;
        } else if (!TextUtils.isEmpty(localBigThumbPath)) {
            localFilePath = localBigThumbPath;
            thumbType = 2;
        } else if (TextUtils.isEmpty(localThumbPath)) {
            String str;
            StringBuilder append = new StringBuilder().append("cloud-").append(albumId).append("-");
            if (PhotoShareUtils.isGUIDSupport()) {
                str = uniqueId;
            } else {
                str = hash;
            }
            localFilePath = append.append(str).toString();
            thumbType = 0;
        } else {
            localFilePath = localThumbPath;
            thumbType = 1;
        }
        if (!localFilePath.startsWith("cloud-")) {
            GalleryUtils.resolveWidthAndHeight(initialValues, localFilePath);
        }
        int duration = 0;
        if (initialValues.getAsInteger("duration") != null) {
            duration = initialValues.getAsInteger("duration").intValue();
        }
        int mediaType = fileType == 4 ? 3 : 1;
        int voiceOffset = fileType == 2 ? -1 : 0;
        int rectifyOffset = fileType == 8 ? -1 : 0;
        int refocus = getRefocus(fileType);
        int specialFileType = 0;
        int specialFileOffset = 0;
        if (fileType == 9) {
            specialFileType = 50;
            specialFileOffset = -1;
        }
        galleryMediaValues.put("_data", localFilePath);
        galleryMediaValues.put("_size", size);
        galleryMediaValues.put("date_added", createTime);
        galleryMediaValues.put("date_modified", Long.valueOf(getLastModify(localFilePath)));
        galleryMediaValues.put("mime_type", mimeType);
        galleryMediaValues.put("_display_name", RecycleUtils.getOriginDisplayName(fileName));
        galleryMediaValues.put("title", titleName);
        galleryMediaValues.put("orientation", Integer.valueOf(rotate));
        galleryMediaValues.put("latitude", Double.valueOf(latitude));
        galleryMediaValues.put("longitude", Double.valueOf(longitude));
        galleryMediaValues.put("datetaken", createTime);
        if (albumName != null) {
            galleryMediaValues.put("bucket_id", Integer.valueOf(bucketId));
            galleryMediaValues.put("bucket_display_name", albumName);
        }
        galleryMediaValues.put("duration", Integer.valueOf(duration));
        galleryMediaValues.put("media_type", Integer.valueOf(mediaType));
        galleryMediaValues.put("hw_voice_offset", Integer.valueOf(voiceOffset));
        galleryMediaValues.put("hw_image_refocus", Integer.valueOf(refocus));
        galleryMediaValues.put("hw_rectify_offset", Integer.valueOf(rectifyOffset));
        galleryMediaValues.put("special_file_type", Integer.valueOf(specialFileType));
        galleryMediaValues.put("special_file_offset", Integer.valueOf(specialFileOffset));
        galleryMediaValues.put("hash", hash);
        galleryMediaValues.put("local_media_id", Integer.valueOf(-1));
        galleryMediaValues.put("cloud_media_id", Long.valueOf(id));
        galleryMediaValues.put("dirty", Integer.valueOf(0));
        galleryMediaValues.put("cloud_bucket_id", albumId);
        galleryMediaValues.put("is_hw_burst", Integer.valueOf(matchBurstCover(RecycleUtils.getOriginDisplayName(fileName)) ? 1 : 0));
        galleryMediaValues.put("fileType", Integer.valueOf(fileType));
        galleryMediaValues.put("fileId", initialValues.getAsString("fileId"));
        galleryMediaValues.put("videoThumbId", initialValues.getAsString("videoThumbId"));
        galleryMediaValues.put("localThumbPath", initialValues.getAsString("localThumbPath"));
        galleryMediaValues.put("localBigThumbPath", initialValues.getAsString("localBigThumbPath"));
        galleryMediaValues.put("thumbType", Integer.valueOf(thumbType));
        galleryMediaValues.put("expand", initialValues.getAsString("expand"));
        galleryMediaValues.put("source", initialValues.getAsString("source"));
        galleryMediaValues.put("uniqueId", uniqueId);
        return galleryMediaValues;
    }

    public static int getRefocus(int fileType) {
        if (fileType == 6) {
            return 2;
        }
        if (fileType == 3) {
            return 1;
        }
        return 0;
    }

    public static String getMimeType(int fileType) {
        if (fileType == 4) {
            return "video/mp4";
        }
        if (fileType == 5) {
            return "image/png";
        }
        return "image/jpeg";
    }

    public static boolean matchBurstCover(String path) {
        if (path == null) {
            return false;
        }
        path = path.toUpperCase(Locale.US);
        if (path.endsWith("_COVER.JPG")) {
            return BURST_PATTERN.matcher(path).find();
        }
        return false;
    }

    public Cursor queryLocalAlbum(SQLiteDatabase db, Context context) {
        HashMap<String, String> autoUploadAlbumPath = new HashMap();
        HashMap<String, String> cloudAlbumPath = new HashMap();
        HashMap<String, String> historyAlbumIDPath = new HashMap();
        queryAutoUploadAlbumPath(db, autoUploadAlbumPath);
        queryCloudAlbumPath(db, cloudAlbumPath);
        queryHistoryAlbumPath(db, historyAlbumIDPath);
        ArrayList<AlbumEntry> albumEntries = getAlbumEntries(db, autoUploadAlbumPath, cloudAlbumPath, historyAlbumIDPath);
        moveScreenShotFirst(albumEntries, autoUploadAlbumPath);
        removeCamera(albumEntries);
        MatrixCursor result = new MatrixCursor(new String[]{"albumName", "bucket_relative_path", "albumId", "tempId", "albumType", "status", "bucket_display_name"});
        for (int i = 0; i < albumEntries.size(); i++) {
            AlbumEntry entry = (AlbumEntry) albumEntries.get(i);
            int resId = getResId(entry.mRelativePath);
            Object[] objArr = new Object[7];
            objArr[0] = entry.mAlbumName;
            objArr[1] = entry.mRelativePath;
            objArr[2] = entry.mAlbumId;
            objArr[3] = entry.mTempId;
            objArr[4] = Integer.valueOf(entry.mAlbumType);
            objArr[5] = Integer.valueOf(entry.mStatus);
            objArr[6] = resId != -1 ? context.getString(resId) : entry.mAlbumName;
            result.addRow(objArr);
        }
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ArrayList<AlbumEntry> getAlbumEntries(SQLiteDatabase db, HashMap<String, String> autoUploadAlbumPath, HashMap<String, String> cloudAlbumPath, HashMap<String, String> historyAlbumIDPath) {
        ArrayList<AlbumEntry> albumEntries = new ArrayList();
        Closeable closeable = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("gallery_media", new String[]{"bucket_display_name", "bucket_relative_path"}, "special_file_list = 0 and local_media_id!=-1 and bucket_relative_path is not null and bucket_relative_path!=\"\"", null, "bucket_relative_path", null, null);
            while (closeable != null && closeable.moveToNext()) {
                String name = closeable.getString(0);
                String relativePath = closeable.getString(1);
                String str = (String) cloudAlbumPath.get(relativePath);
                int status = 0;
                String tempID = String.valueOf(GalleryUtils.getBucketId(relativePath));
                int albumType = 0;
                if (autoUploadAlbumPath.containsKey(relativePath)) {
                    status = 1;
                    if (str == null) {
                        str = (String) autoUploadAlbumPath.get(relativePath);
                    }
                }
                String uid = PhotoShareUtils.getUserId();
                if (str == null) {
                    str = (String) historyAlbumIDPath.get(relativePath);
                }
                String thirdAutoUploadAlbumId = CloudLocalAlbum.getThirdAutoUploadAlbumId(GalleryUtils.getBucketId(relativePath));
                if (!TextUtils.isEmpty(relativePath)) {
                    if (relativePath.equals("/DCIM/Camera")) {
                        str = "default-album-1";
                        albumType = 1;
                    } else if (relativePath.equals("/Pictures/Screenshots")) {
                        str = "default-album-2";
                        albumType = 2;
                    } else if (TextUtils.isEmpty(thirdAutoUploadAlbumId)) {
                        if (str == null) {
                            str = "default-album-200-" + tempID + "-" + String.valueOf(System.currentTimeMillis());
                        }
                        albumType = 3;
                    } else {
                        str = thirdAutoUploadAlbumId + "-" + uid;
                        albumType = 4;
                    }
                }
                albumEntries.add(new AlbumEntry(name, relativePath, str, tempID, albumType, status));
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", " query GalleryMedia table exception in queryLocalAlbum" + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(closeable);
        }
        return albumEntries;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void queryHistoryAlbumPath(SQLiteDatabase db, HashMap<String, String> historyAlbumIDPath) {
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable historyAlbumIdCursor = sQLiteDatabase.query("history_album_id", new String[]{"relativePath", "albumId"}, null, null, null, null, null);
            while (historyAlbumIdCursor != null && historyAlbumIdCursor.moveToNext()) {
                historyAlbumIDPath.put(historyAlbumIdCursor.getString(0), historyAlbumIdCursor.getString(1));
            }
            Utils.closeSilently(historyAlbumIdCursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryLocalAlbum query history_album_id table exception in queryLocalAlbum" + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void queryCloudAlbumPath(SQLiteDatabase db, HashMap<String, String> cloudAlbumPath) {
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable cloudAlbumCursor = sQLiteDatabase.query("cloud_album", new String[]{"lpath", "albumId"}, null, null, null, null, null);
            while (cloudAlbumCursor != null && cloudAlbumCursor.moveToNext()) {
                cloudAlbumPath.put(cloudAlbumCursor.getString(0), cloudAlbumCursor.getString(1));
            }
            Utils.closeSilently(cloudAlbumCursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryLocalAlbum query cloud_album table exception in queryLocalAlbum" + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void queryAutoUploadAlbumPath(SQLiteDatabase db, HashMap<String, String> autoUploadAlbumPath) {
        try {
            SQLiteDatabase sQLiteDatabase = db;
            Closeable autoUploadAlbumCursor = sQLiteDatabase.query("auto_upload_album", new String[]{"relativePath", "albumId"}, null, null, null, null, null);
            while (autoUploadAlbumCursor != null && autoUploadAlbumCursor.moveToNext()) {
                autoUploadAlbumPath.put(autoUploadAlbumCursor.getString(0), autoUploadAlbumCursor.getString(1));
            }
            Utils.closeSilently(autoUploadAlbumCursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryLocalAlbum query auto_upload_album table exception in queryLocalAlbum" + e.toString());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
    }

    public static int getResId(String relativePath) {
        return sAlbumNameResIds.get(GalleryUtils.getBucketId(relativePath), -1);
    }

    private void moveScreenShotFirst(ArrayList<AlbumEntry> albumEntries, HashMap<String, String> autoUploadAlbumPath) {
        AlbumEntry albumEntry = null;
        for (int i = 0; i < albumEntries.size(); i++) {
            AlbumEntry tempEntry = (AlbumEntry) albumEntries.get(i);
            if ("/Pictures/Screenshots".equalsIgnoreCase(tempEntry.mRelativePath)) {
                albumEntry = tempEntry;
                albumEntries.remove(i);
                break;
            }
        }
        if (albumEntry == null) {
            int status = 0;
            if (autoUploadAlbumPath.containsKey("/Pictures/Screenshots")) {
                status = 1;
            }
            albumEntry = new AlbumEntry("Screenshots", "/Pictures/Screenshots", "default-album-2", "default-album-2", 2, status);
        }
        albumEntries.add(0, albumEntry);
    }

    private void removeCamera(ArrayList<AlbumEntry> albumEntries) {
        for (int i = 0; i < albumEntries.size(); i++) {
            if ("/DCIM/Camera".equalsIgnoreCase(((AlbumEntry) albumEntries.get(i)).mRelativePath)) {
                albumEntries.remove(i);
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Cursor queryDefaultAlbumIsEmpty(SQLiteDatabase db) {
        MatrixCursor result = new MatrixCursor(new String[]{"isEmpty"});
        int count = 0;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        try {
            String[] strArr = new String[]{"count(*)"};
            String str = "local_media_id !=-1 and bucket_id in (" + (MediaSetUtils.getCameraBucketId() + " , " + storageManager.getOuterGalleryStorageCameraBucketIDs() + " , " + MediaSetUtils.getScreenshotsBucketID() + " , " + storageManager.getOuterGalleryStorageScreenshotsBucketIDs()) + ")";
            Closeable cursor = db.query("gallery_media", strArr, str, null, null, null, null);
            if (cursor != null && cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
            Utils.closeSilently(cursor);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryDefaultAlbumIsEmpty query GalleryMedia err" + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        int isEmpty = 0;
        if (count > 0) {
            isEmpty = 1;
        }
        result.addRow(new Object[]{Integer.valueOf(isEmpty)});
        return result;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public Cursor queryWaitToUploadCount(SQLiteDatabase db) {
        MatrixCursor result = new MatrixCursor(new String[]{"isUploading", "imageCount", "videoCount"});
        if (PhotoShareUtils.isFversionEmpty()) {
            result.addRow(new Object[]{Integer.valueOf(3), Integer.valueOf(0), Integer.valueOf(0)});
            return result;
        }
        int status;
        Closeable closeable = null;
        int imageCount = 0;
        int videoCount = 0;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            closeable = sQLiteDatabase.query("gallery_media", new String[]{"media_type", "count(*)"}, "cloud_media_id = -1 and relative_cloud_media_id = -1 and bucket_id in (" + PhotoShareUtils.getAutoUploadBucketIds() + ")", null, "media_type", null, null);
            if (closeable != null) {
                while (closeable.moveToNext()) {
                    if (closeable.getInt(0) == 1) {
                        imageCount = closeable.getInt(1);
                    } else if (closeable.getInt(0) == 3) {
                        videoCount = closeable.getInt(1);
                    }
                }
            }
            Utils.closeSilently(closeable);
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryWaitToUploadCount query GalleryMedia waiting upload count err" + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        if (imageCount == 0 && videoCount == 0) {
            status = 1;
            imageCount = 0;
            videoCount = 0;
            try {
                sQLiteDatabase = db;
                closeable = sQLiteDatabase.query("gallery_media", new String[]{"media_type", "count(*)"}, "cloud_media_id !=-1", null, "media_type", null, null);
                if (closeable != null) {
                    while (closeable.moveToNext()) {
                        if (closeable.getInt(0) == 1) {
                            imageCount = closeable.getInt(1);
                        } else if (closeable.getInt(0) == 3) {
                            videoCount = closeable.getInt(1);
                        }
                    }
                }
                Utils.closeSilently(closeable);
            } catch (SQLiteException e2) {
                GalleryLog.e("photoshareLogTag", "queryWaitToUploadCount query local count err" + e2.getMessage());
            } catch (Throwable th2) {
                Utils.closeSilently(closeable);
            }
        } else {
            status = 2;
        }
        result.addRow(new Object[]{Integer.valueOf(status), Integer.valueOf(imageCount), Integer.valueOf(videoCount)});
        return result;
    }

    public Cursor queryGalleryMedia(SQLiteDatabase db, String[] projectionIn, String albumID) {
        Cursor cursor = null;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        GalleryStorage innerStorage = storageManager.getInnerGalleryStorage();
        if (innerStorage == null) {
            return null;
        }
        Object obj = null;
        Cursor cursor2;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("auto_upload_album", new String[]{"relativePath"}, "albumID=?", new String[]{albumID}, null, null, null);
            if (cursor == null) {
                GalleryLog.v("photoshareLogTag", "queryGalleryMedia cursor is null");
                cursor2 = null;
                return cursor2;
            }
            StringBuffer sb = new StringBuffer();
            while (cursor.moveToNext()) {
                String relativePath = cursor.getString(0);
                sb.append(", ").append(String.valueOf(innerStorage.getBucketID(relativePath)));
                ArrayList<Integer> outers = storageManager.getOuterGalleryStorageBucketIDsByArrayList(relativePath);
                for (int i = 0; i < outers.size(); i++) {
                    sb.append(", ").append(outers.get(i));
                }
            }
            if (sb.length() > 0) {
                obj = sb.substring(1);
            }
            Utils.closeSilently((Closeable) cursor);
            if (!TextUtils.isEmpty(obj)) {
                try {
                    String str = "bucket_id in (" + obj + ") and dirty = 1";
                    cursor = db.query("gallery_media", projectionIn, str, null, null, null, null);
                } catch (SQLiteException e) {
                    GalleryLog.e("photoshareLogTag", "queryGalleryMedia query GalleryMedia err" + e.getMessage());
                }
            }
            return cursor;
        } catch (SQLiteException e2) {
            cursor2 = "photoshareLogTag";
            GalleryLog.e(cursor2, "queryGalleryMedia query AUTO_UPLOAD_ALBUM_TABLE error" + e2.getMessage());
        } finally {
            Utils.closeSilently(r10);
        }
    }

    public Cursor queryRenamedFiles(SQLiteDatabase db, String[] projectionIn, String albumID) {
        Cursor cursor = null;
        GalleryStorageManager storageManager = GalleryStorageManager.getInstance();
        GalleryStorage innerStorage = storageManager.getInnerGalleryStorage();
        if (innerStorage == null) {
            return null;
        }
        Object obj = null;
        Cursor cursor2;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("cloud_album", new String[]{"lpath"}, "albumId = ?", new String[]{albumID}, null, null, null);
            if (cursor == null) {
                GalleryLog.v("photoshareLogTag", "queryRenamedFiles cursor is null");
                cursor2 = null;
                return cursor2;
            }
            StringBuffer sb = new StringBuffer();
            while (cursor.moveToNext()) {
                String relativePath = cursor.getString(0);
                sb.append(", ").append(String.valueOf(innerStorage.getBucketID(relativePath)));
                ArrayList<Integer> outers = storageManager.getOuterGalleryStorageBucketIDsByArrayList(relativePath);
                for (int i = 0; i < outers.size(); i++) {
                    sb.append(", ").append(outers.get(i));
                }
            }
            GalleryLog.printDFXLog("build sql done");
            if (sb.length() > 0) {
                obj = sb.substring(1);
            }
            Utils.closeSilently((Closeable) cursor);
            if (!TextUtils.isEmpty(obj)) {
                try {
                    String str = "bucket_id in (" + obj + ") and dirty = 2";
                    cursor = db.query("gallery_media", projectionIn, str, null, null, null, null);
                } catch (SQLiteException e) {
                    GalleryLog.e("photoshareLogTag", "queryRenamedFiles query GalleryMedia err" + e.getMessage());
                }
            }
            return cursor;
        } catch (SQLiteException e2) {
            cursor2 = "photoshareLogTag";
            GalleryLog.e(cursor2, "queryRenamedFiles query cloudAlbumTable err" + e2.getMessage());
        } finally {
            Utils.closeSilently(r10);
        }
    }

    public Cursor queryDeletedAlbum(SQLiteDatabase db) {
        Cursor cursor = null;
        try {
            SQLiteDatabase sQLiteDatabase = db;
            cursor = sQLiteDatabase.query("cloud_album", new String[]{"albumId"}, "deleteFlag = 1", null, null, null, null);
            if (cursor == null) {
                return null;
            }
            return cursor;
        } catch (SQLiteException e) {
            GalleryLog.e("photoshareLogTag", "queryDeletedAlbum SQLiteException" + e.getMessage());
        }
    }

    public static long getLastModify(String filePath) {
        File file = new File(filePath);
        if (file.exists()) {
            return file.lastModified();
        }
        return 0;
    }
}
