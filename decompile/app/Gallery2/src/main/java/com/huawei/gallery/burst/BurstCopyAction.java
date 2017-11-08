package com.huawei.gallery.burst;

import android.net.Uri;
import android.os.Bundle;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaItem;
import com.android.gallery3d.util.FileUtils;
import com.huawei.gallery.actionbar.Action;
import com.huawei.gallery.burst.BurstActionExecutor.ExecutorListener;
import java.io.File;
import java.util.ArrayList;

public class BurstCopyAction extends BurstAction {
    private int mHeight;
    private int mOrientation;
    private ArrayList<Uri> mOriginUris = new ArrayList();
    private ArrayList<File> mSuccessFiles = new ArrayList();
    private int mWidth;

    private void addDataBase(android.net.Uri r19, java.io.File r20) {
        /* JADX: method processing error */
/*
Error: java.util.NoSuchElementException
	at java.util.HashMap$HashIterator.nextNode(HashMap.java:1431)
	at java.util.HashMap$KeyIterator.next(HashMap.java:1453)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.applyRemove(BlockFinallyExtract.java:535)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.extractFinally(BlockFinallyExtract.java:175)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.processExceptionHandler(BlockFinallyExtract.java:80)
	at jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract.visit(BlockFinallyExtract.java:51)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r18 = this;
        r11 = new android.content.ContentValues;
        r11.<init>();
        r0 = r18;
        r3 = r0.mContext;
        r3 = r3.getAndroidContext();
        r2 = r3.getContentResolver();
        r16 = java.lang.System.currentTimeMillis();
        r10 = r20.getName();
        r3 = ".jpg";
        r3 = r10.lastIndexOf(r3);
        r5 = 0;
        r10 = r10.substring(r5, r3);
        r3 = "title";
        r11.put(r3, r10);
        r3 = "_display_name";
        r5 = r20.getName();
        r11.put(r3, r5);
        r3 = "mime_type";
        r5 = "image/jpeg";
        r11.put(r3, r5);
        r3 = "datetaken";
        r5 = java.lang.Long.valueOf(r16);
        r11.put(r3, r5);
        r3 = "date_modified";
        r5 = java.lang.Long.valueOf(r16);
        r11.put(r3, r5);
        r3 = "date_added";
        r5 = java.lang.Long.valueOf(r16);
        r11.put(r3, r5);
        r3 = "width";
        r0 = r18;
        r5 = r0.mWidth;
        r5 = java.lang.Integer.valueOf(r5);
        r11.put(r3, r5);
        r3 = "height";
        r0 = r18;
        r5 = r0.mHeight;
        r5 = java.lang.Integer.valueOf(r5);
        r11.put(r3, r5);
        r3 = "orientation";
        r0 = r18;
        r5 = r0.mOrientation;
        r5 = java.lang.Integer.valueOf(r5);
        r11.put(r3, r5);
        r3 = "_data";
        r5 = r20.getAbsolutePath();
        r11.put(r3, r5);
        r3 = "_size";
        r6 = r20.length();
        r5 = java.lang.Long.valueOf(r6);
        r11.put(r3, r5);
        r3 = 3;
        r4 = new java.lang.String[r3];
        r3 = "datetaken";
        r5 = 0;
        r4[r5] = r3;
        r3 = "latitude";
        r5 = 1;
        r4[r5] = r3;
        r3 = "longitude";
        r5 = 2;
        r4[r5] = r3;
        r8 = 0;
        r5 = 0;
        r6 = 0;
        r7 = 0;
        r3 = r19;
        r8 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        if (r8 == 0) goto L_0x00fe;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
    L_0x00bf:
        r3 = r8.moveToNext();	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        if (r3 == 0) goto L_0x00fe;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
    L_0x00c5:
        r3 = "datetaken";	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r5 = 0;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r6 = r8.getLong(r5);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r5 = java.lang.Long.valueOf(r6);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r11.put(r3, r5);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r3 = 1;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r12 = r8.getDouble(r3);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r3 = 2;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r14 = r8.getDouble(r3);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r6 = 0;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r3 = (r12 > r6 ? 1 : (r12 == r6 ? 0 : -1));	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        if (r3 != 0) goto L_0x00ea;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
    L_0x00e4:
        r6 = 0;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r3 = (r14 > r6 ? 1 : (r14 == r6 ? 0 : -1));	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        if (r3 == 0) goto L_0x00fe;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
    L_0x00ea:
        r3 = "latitude";	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r5 = java.lang.Double.valueOf(r12);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r11.put(r3, r5);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r3 = "longitude";	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r5 = java.lang.Double.valueOf(r14);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r11.put(r3, r5);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
    L_0x00fe:
        r3 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        r2.insert(r3, r11);	 Catch:{ Exception -> 0x0109, all -> 0x0110 }
        if (r8 == 0) goto L_0x0108;
    L_0x0105:
        r8.close();
    L_0x0108:
        return;
    L_0x0109:
        r9 = move-exception;
        if (r8 == 0) goto L_0x0108;
    L_0x010c:
        r8.close();
        goto L_0x0108;
    L_0x0110:
        r3 = move-exception;
        if (r8 == 0) goto L_0x0116;
    L_0x0113:
        r8.close();
    L_0x0116:
        throw r3;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.burst.BurstCopyAction.addDataBase(android.net.Uri, java.io.File):void");
    }

    public BurstCopyAction(GalleryContext context) {
        super(context, Action.SAVE_BURST);
    }

    public boolean onProgressStart(ArrayList<MediaItem> itemArray, Bundle data, ExecutorListener listener) {
        if (itemArray.size() == 0) {
            return true;
        }
        long needSpace = 0;
        for (MediaItem item : itemArray) {
            File itemFile = new File(item.getFilePath());
            if (itemFile.exists()) {
                needSpace += itemFile.length();
            } else {
                data.putInt("KEY_ERROR_CODE", 0);
                return false;
            }
        }
        if (needSpace < new File(((MediaItem) itemArray.get(0)).getFilePath()).getParentFile().getUsableSpace()) {
            return true;
        }
        data.putInt("KEY_ERROR_CODE", 1);
        return false;
    }

    public boolean execute(MediaItem item, Bundle data, ExecutorListener listener) {
        File originFile = new File(item.getFilePath());
        File newFile = FileUtils.getNoneDuplicateFile(originFile.getParentFile(), originFile.getName().replace("BURST", ""));
        if (!FileUtils.copyFileToNewFile(originFile, newFile, 1048576)) {
            return false;
        }
        this.mWidth = item.getWidth();
        this.mHeight = item.getHeight();
        this.mOrientation = item.getRotation();
        this.mSuccessFiles.add(newFile);
        this.mOriginUris.add(item.getContentUri());
        return true;
    }

    public void onProgressComplete(int result, ExecutorListener listener, Bundle data) {
        for (File file : this.mSuccessFiles) {
            addDataBase((Uri) this.mOriginUris.get(0), file);
        }
        if (listener != null) {
            data.putInt("KEY_SUCCESS_COUNT", this.mSuccessFiles.size());
            listener.onActionDone(this.mAction, result == 1, data);
        }
    }
}
