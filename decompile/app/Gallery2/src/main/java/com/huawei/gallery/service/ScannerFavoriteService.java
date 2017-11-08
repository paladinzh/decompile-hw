package com.huawei.gallery.service;

import android.content.Intent;
import android.os.Message;

public class ScannerFavoriteService extends AsyncService {
    public boolean handleMessage(android.os.Message r20) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unreachable block: B:51:0x007b
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.modifyBlocksTree(BlockProcessor.java:248)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:52)
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
        r19 = this;
        r13 = r19.getApplicationContext();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r13 = (com.android.gallery3d.app.GalleryApp) r13;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r14 = r13.getGalleryData();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r2 = 1;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r17 = r14.queryFavorite(r2);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r18 = new java.util.ArrayList;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0 = r18;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r1 = r17;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0.<init>(r1);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r16 = r18.iterator();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
    L_0x001c:
        r2 = r16.hasNext();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        if (r2 == 0) goto L_0x0036;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
    L_0x0022:
        r15 = r16.next();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r15 = (java.lang.String) r15;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0 = r19;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r2 = r0.mServiceHandler;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0 = r20;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r0.what;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r2 = r2.hasMessages(r4);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        if (r2 == 0) goto L_0x0041;
    L_0x0036:
        r0 = r20;
        r2 = r0.arg1;
        r0 = r19;
        r0.stopSelf(r2);
    L_0x003f:
        r2 = 1;
        return r2;
    L_0x0041:
        r12 = 0;
        r2 = com.android.gallery3d.data.BucketHelper.PRE_LOADED_PATH_PICTURE;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r2 = r15.startsWith(r2);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        if (r2 == 0) goto L_0x0101;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
    L_0x004a:
        r3 = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r8 = 0;
        r2 = r13.getContentResolver();	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = 1;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = new java.lang.String[r4];	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r5 = "_data";	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r6 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4[r6] = r5;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r5 = "_data=?";	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r6 = 1;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r6 = new java.lang.String[r6];	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r7 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r6[r7] = r15;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r7 = 0;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r8 = r2.query(r3, r4, r5, r6, r7);	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        if (r8 == 0) goto L_0x0070;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
    L_0x006a:
        r2 = r8.getCount();	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        if (r2 > 0) goto L_0x0071;
    L_0x0070:
        r12 = 1;
    L_0x0071:
        com.android.gallery3d.common.Utils.closeSilently(r8);
    L_0x0074:
        if (r12 == 0) goto L_0x001c;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
    L_0x0076:
        r2 = 0;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r14.updateFavorite(r15, r2);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        goto L_0x001c;
    L_0x007b:
        r10 = move-exception;
        r2 = "ScannerFavoriteService";	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r5 = "RuntimeException in handleMessage.";	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r5 = r10.getMessage();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        com.android.gallery3d.util.GalleryLog.e(r2, r4);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0 = r20;
        r2 = r0.arg1;
        r0 = r19;
        r0.stopSelf(r2);
        goto L_0x003f;
    L_0x00a4:
        r9 = move-exception;
        r2 = "ScannerFavoriteService";	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4.<init>();	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r5 = "get image fail.";	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r5 = r9.getMessage();	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = r4.append(r5);	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        r4 = r4.toString();	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        com.android.gallery3d.util.GalleryLog.e(r2, r4);	 Catch:{ Exception -> 0x00a4, all -> 0x00f1 }
        com.android.gallery3d.common.Utils.closeSilently(r8);
        goto L_0x0074;
    L_0x00c7:
        r9 = move-exception;
        r2 = "ScannerFavoriteService";	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = new java.lang.StringBuilder;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4.<init>();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r5 = "Exception in handleMessage.";	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r5 = r9.getMessage();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.append(r5);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r4 = r4.toString();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        com.android.gallery3d.util.GalleryLog.e(r2, r4);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r0 = r20;
        r2 = r0.arg1;
        r0 = r19;
        r0.stopSelf(r2);
        goto L_0x003f;
    L_0x00f1:
        r2 = move-exception;
        com.android.gallery3d.common.Utils.closeSilently(r8);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        throw r2;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
    L_0x00f6:
        r2 = move-exception;
        r0 = r20;
        r4 = r0.arg1;
        r0 = r19;
        r0.stopSelf(r4);
        throw r2;
    L_0x0101:
        r11 = new java.io.File;	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r11.<init>(r15);	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        r2 = r11.exists();	 Catch:{ RuntimeException -> 0x007b, Exception -> 0x00c7, all -> 0x00f6 }
        if (r2 != 0) goto L_0x0074;
    L_0x010c:
        r12 = 1;
        goto L_0x0074;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.service.ScannerFavoriteService.handleMessage(android.os.Message):boolean");
    }

    protected String getServiceTag() {
        return "ScannerFavoriteService";
    }

    protected void decorateMsg(Message message, Intent intent, int startId) {
        message.what = 1;
        message.arg1 = startId;
        this.mServiceHandler.removeMessages(message.what);
    }
}
