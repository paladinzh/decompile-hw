package com.android.gallery3d.app;

import android.content.Context;
import android.net.Uri;

public class WallpaperConfig {
    private static final Uri URI = Uri.parse("content://com.huawei.android.thememanager.ContentProvider/config");

    private static boolean updateItem(android.content.Context r10, java.lang.String r11, java.lang.String r12, int r13) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00a8 in list []
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
        r9 = 0;
        if (r10 == 0) goto L_0x0005;
    L_0x0003:
        if (r11 != 0) goto L_0x0006;
    L_0x0005:
        return r9;
    L_0x0006:
        if (r12 == 0) goto L_0x0005;
    L_0x0008:
        r0 = r10.getContentResolver();
        if (r0 != 0) goto L_0x000f;
    L_0x000e:
        return r9;
    L_0x000f:
        r6 = new android.content.ContentValues;
        r6.<init>();
        r1 = "name";
        r6.put(r1, r11);
        r1 = "value";
        r6.put(r1, r12);
        r1 = "type";
        r2 = java.lang.Integer.valueOf(r13);
        r6.put(r1, r2);
        r1 = new java.lang.StringBuilder;
        r1.<init>();
        r2 = "name = '";
        r1 = r1.append(r2);
        r1 = r1.append(r11);
        r2 = "'";
        r1 = r1.append(r2);
        r3 = r1.toString();
        r7 = 0;
        r1 = URI;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = 0;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r4 = 0;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r5 = 0;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r7 = r0.query(r1, r2, r3, r4, r5);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        if (r7 == 0) goto L_0x0057;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
    L_0x0051:
        r1 = r7.getCount();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        if (r1 != 0) goto L_0x0063;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
    L_0x0057:
        r1 = URI;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r0.insert(r1, r6);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
    L_0x005c:
        if (r7 == 0) goto L_0x0061;
    L_0x005e:
        r7.close();
    L_0x0061:
        r1 = 1;
        return r1;
    L_0x0063:
        r1 = r7.moveToNext();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        if (r1 == 0) goto L_0x00a9;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
    L_0x0069:
        r1 = URI;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2.<init>();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r4 = "_ID = ";	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.append(r4);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r4 = r7.getPosition();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.append(r4);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r4 = 0;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r0.delete(r1, r2, r4);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        goto L_0x0063;
    L_0x0088:
        r8 = move-exception;
        r1 = "WallpaperConfig";	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = new java.lang.StringBuilder;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2.<init>();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r4 = "updateItem Exception name:";	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.append(r4);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.append(r11);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = r2.toString();	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        com.android.gallery3d.util.GalleryLog.e(r1, r2);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        if (r7 == 0) goto L_0x00a8;
    L_0x00a5:
        r7.close();
    L_0x00a8:
        return r9;
    L_0x00a9:
        r1 = URI;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r2 = 0;	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        r0.update(r1, r6, r3, r2);	 Catch:{ Exception -> 0x0088, all -> 0x00b0 }
        goto L_0x005c;
    L_0x00b0:
        r1 = move-exception;
        if (r7 == 0) goto L_0x00b6;
    L_0x00b3:
        r7.close();
    L_0x00b6:
        throw r1;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.gallery3d.app.WallpaperConfig.updateItem(android.content.Context, java.lang.String, java.lang.String, int):boolean");
    }

    public static boolean setWallpaperConfig(Context context, boolean fixed) {
        boolean result;
        if (updateItem(context, "is_wallpaper_fixed", fixed ? "true" : "false", 3)) {
            result = updateItem(context, "wallpaper_left_position", "0", 2);
        } else {
            result = false;
        }
        if (result) {
            return updateItem(context, "wallpaper_right_position", "0.5", 2);
        }
        return false;
    }
}
