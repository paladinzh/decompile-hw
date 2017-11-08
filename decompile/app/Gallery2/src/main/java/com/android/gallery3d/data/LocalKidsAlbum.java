package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.MediaStore.Files;
import android.provider.MediaStore.Images.Media;
import android.provider.MediaStore.Video;
import com.android.gallery3d.app.GalleryApp;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.MediaSet.ItemConsumer;
import com.android.gallery3d.util.Constant;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import com.huawei.gallery.kidsmode.KidsMode;
import java.io.Closeable;
import java.util.ArrayList;

public class LocalKidsAlbum extends LocalMergeQuerySet {
    private static final String[] COUNT_PROJECTION = new String[]{"count(*)", "SUM((CASE WHEN media_type=3 THEN 1 ELSE 0 END))"};
    private static final Uri EXTERNAL_FILE_URI = Files.getContentUri("external");
    private static final Uri[] mWatchUris = new Uri[]{Media.EXTERNAL_CONTENT_URI, Video.Media.EXTERNAL_CONTENT_URI, Constant.RELOAD_URI_KIDS_ALBUM};
    private String mAlbumType;
    private final GalleryApp mApplication;
    private String mBucketId;
    private String mClauseWithId;
    private final ChangeNotifier mNotifier;
    private final String mOrderClause;
    private String mQueryClause;
    private final ContentResolver mResolver;
    private String[] mSelectArgs;

    public java.util.ArrayList<java.lang.String> getVideoFileList() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0071 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r10 = this;
        r9 = 0;
        r0 = new java.lang.StringBuilder;
        r0.<init>();
        r1 = " media_type = 3 AND ";
        r0 = r0.append(r1);
        r1 = r10.mClauseWithId;
        r0 = r0.append(r1);
        r3 = r0.toString();
        r8 = new java.util.ArrayList;
        r8.<init>();
        r6 = 0;
        r0 = r10.mResolver;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = EXTERNAL_FILE_URI;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r2 = PROJECTION;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r4 = r10.mSelectArgs;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r5 = r10.mOrderClause;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r6 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        if (r6 != 0) goto L_0x003c;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
    L_0x002d:
        r0 = "LocalKidsAlbum";	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = "query fail";	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        com.android.gallery3d.util.GalleryLog.w(r0, r1);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        if (r6 == 0) goto L_0x003b;
    L_0x0038:
        com.android.gallery3d.common.Utils.closeSilently(r6);
    L_0x003b:
        return r9;
    L_0x003c:
        r0 = r6.moveToNext();	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        if (r0 == 0) goto L_0x0072;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
    L_0x0042:
        r0 = "_data";	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r0 = r6.getColumnIndexOrThrow(r0);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r0 = r6.getString(r0);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r8.add(r0);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        goto L_0x003c;
    L_0x0051:
        r7 = move-exception;
        r0 = "LocalKidsAlbum";	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1.<init>();	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r2 = "query fail:";	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = r1.append(r2);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = r1.append(r7);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        r1 = r1.toString();	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        com.android.gallery3d.util.GalleryLog.w(r0, r1);	 Catch:{ Exception -> 0x0051, all -> 0x0078 }
        if (r6 == 0) goto L_0x0071;
    L_0x006e:
        com.android.gallery3d.common.Utils.closeSilently(r6);
    L_0x0071:
        return r9;
    L_0x0072:
        if (r6 == 0) goto L_0x0077;
    L_0x0074:
        com.android.gallery3d.common.Utils.closeSilently(r6);
    L_0x0077:
        return r8;
    L_0x0078:
        r0 = move-exception;
        if (r6 == 0) goto L_0x007e;
    L_0x007b:
        com.android.gallery3d.common.Utils.closeSilently(r6);
    L_0x007e:
        throw r0;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.data.LocalKidsAlbum.getVideoFileList():java.util.ArrayList<java.lang.String>");
    }

    public LocalKidsAlbum(Path path, GalleryApp application) {
        super(path, MediaObject.nextVersionNumber());
        this.mApplication = application;
        this.mResolver = application.getContentResolver();
        this.mNotifier = new ChangeNotifier((MediaSet) this, mWatchUris, application);
        this.mAlbumType = getTypeFromPath(path);
        this.mBucketId = "parent".equalsIgnoreCase(this.mAlbumType) ? path.getSuffix() : "0";
        this.mOrderClause = "date_modified DESC ";
        initQueryClause(this.mAlbumType);
    }

    private static String getTypeFromPath(Path path) {
        if (path == null) {
            return "";
        }
        String[] name = path.split();
        if (name.length >= 3) {
            return name[2];
        }
        throw new IllegalArgumentException(path.toString());
    }

    public ArrayList<MediaItem> getMediaItem(int start, int count) {
        long startTime = System.currentTimeMillis();
        GalleryUtils.assertNotInRenderThread();
        Uri queryUri = EXTERNAL_FILE_URI.buildUpon().appendQueryParameter("limit", start + "," + count).build();
        ArrayList<MediaItem> mediaItemList = new ArrayList();
        DataManager dataManager = this.mApplication.getDataManager();
        Closeable closeable = null;
        try {
            closeable = this.mResolver.query(queryUri, PROJECTION, this.mQueryClause, this.mSelectArgs, this.mOrderClause);
            if (closeable == null) {
                GalleryLog.w("LocalKidsAlbum", "query kids item fail: " + queryUri);
                printExcuteInfo(startTime, "getMediaItem");
                return mediaItemList;
            }
            while (closeable.moveToNext()) {
                mediaItemList.add(LocalMergeQuerySet.loadOrUpdateItem(closeable, dataManager, this.mApplication));
            }
            Utils.closeSilently(closeable);
            printExcuteInfo(startTime, "getMediaItem");
            return mediaItemList;
        } catch (Exception e) {
            GalleryLog.w("LocalKidsAlbum", "query kids item fail: " + e);
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public int getMediaItemCount() {
        long startTime = System.currentTimeMillis();
        if (this.mCachedCount == -1 || this.mCachedVideoCount == -1) {
            Closeable closeable = null;
            try {
                closeable = this.mResolver.query(EXTERNAL_FILE_URI, COUNT_PROJECTION, this.mQueryClause, this.mSelectArgs, null);
                if (closeable == null) {
                    GalleryLog.w("LocalKidsAlbum", "query kids item fail");
                    this.mCachedCount = 0;
                    this.mCachedVideoCount = 0;
                    printExcuteInfo(startTime, "getMediaItemCount");
                    return 0;
                }
                Utils.assertTrue(closeable.moveToNext());
                this.mCachedCount = closeable.getInt(0);
                this.mCachedVideoCount = closeable.getInt(1);
                Utils.closeSilently(closeable);
            } catch (Exception e) {
                GalleryLog.w("LocalKidsAlbum", "query kids item fail:" + e);
                this.mCachedCount = 0;
                this.mCachedVideoCount = 0;
            } finally {
                Utils.closeSilently(closeable);
            }
        }
        printExcuteInfo(startTime, "getMediaItemCount");
        return this.mCachedCount;
    }

    public String getName() {
        return "";
    }

    public long reload() {
        if (this.mNotifier.isDirty()) {
            this.mDataVersion = MediaObject.nextVersionNumber();
            invalidCachedCount();
        }
        initQueryClause(this.mAlbumType);
        return this.mDataVersion;
    }

    public boolean isLeafAlbum() {
        return true;
    }

    protected int enumerateTotalMediaItems(ItemConsumer consumer, int startIndex) {
        return getMediaItemCount();
    }

    private void initQueryClause(String type) {
        if (type != null) {
            ArrayList<String> result = getKidsAlbumOrDataPath(type);
            this.mClauseWithId = getWhereClauseByList(type, result);
            this.mQueryClause = " media_type in (1, 3) AND " + this.mClauseWithId;
            this.mSelectArgs = getSelectArgsByList(type, result);
            return;
        }
        GalleryLog.w("LocalKidsAlbum", "initQueryClause type is null");
    }

    private String getWhereClauseByList(String type, ArrayList<String> list) {
        if (list == null || list.size() <= 0) {
            return null;
        }
        String clauseBegin;
        if ("media".equalsIgnoreCase(type)) {
            clauseBegin = " _data in (";
        } else if ("parent".equalsIgnoreCase(type)) {
            clauseBegin = "bucket_id=" + this.mBucketId + " AND " + " _data in (";
        } else {
            clauseBegin = " bucket_id in (";
        }
        StringBuffer where = new StringBuffer();
        where.append(clauseBegin);
        for (int i = 0; i < list.size(); i++) {
            where.append("?");
            if (i != list.size() - 1) {
                where.append(",");
            }
        }
        where.append(")");
        return where.toString();
    }

    private String[] getSelectArgsByList(String type, ArrayList<String> list) {
        if (list == null || list.size() <= 0) {
            return new String[0];
        }
        String[] selectArgs = new String[list.size()];
        if ("media".equalsIgnoreCase(type) || "parent".equalsIgnoreCase(type)) {
            return (String[]) list.toArray(selectArgs);
        }
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i) != null) {
                selectArgs[i] = String.valueOf(GalleryUtils.getBucketId((String) list.get(i)));
            } else {
                selectArgs[i] = "0";
            }
        }
        return selectArgs;
    }

    private ArrayList<String> getKidsAlbumOrDataPath(String type) {
        Throwable th;
        Closeable cursor = null;
        String[] projection = null;
        String queryClause = null;
        Uri uri = null;
        ArrayList<String> pathResult = new ArrayList(2);
        try {
            if ("camera".equalsIgnoreCase(type)) {
                uri = KidsMode.CAMERA_URI;
                projection = new String[]{"camera_path"};
            } else if ("paint".equalsIgnoreCase(type)) {
                uri = KidsMode.PAINT_URI;
                projection = new String[]{"paint_path"};
            } else if ("media".equalsIgnoreCase(type) || "parent".equalsIgnoreCase(type)) {
                uri = KidsMode.MEDIA_URI;
                String[] projection2 = new String[]{"bucket_name"};
                try {
                    queryClause = "1) GROUP BY (1";
                    projection = projection2;
                } catch (RuntimeException e) {
                    projection = projection2;
                    try {
                        GalleryLog.w("LocalKidsAlbum", "getKidsAlbumOrDataPath error");
                        if (cursor != null) {
                            Utils.closeSilently(cursor);
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (cursor != null) {
                            Utils.closeSilently(cursor);
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    projection = projection2;
                    if (cursor != null) {
                        Utils.closeSilently(cursor);
                    }
                    throw th;
                }
            }
            cursor = this.mResolver.query(uri, projection, queryClause, null, null);
            if (cursor == null) {
                if (cursor != null) {
                    Utils.closeSilently(cursor);
                }
                return null;
            }
            while (cursor.moveToNext()) {
                pathResult.add(cursor.getString(0));
            }
            if (cursor != null) {
                Utils.closeSilently(cursor);
            }
            return pathResult;
        } catch (RuntimeException e2) {
        }
    }
}
