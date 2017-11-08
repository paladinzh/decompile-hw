package com.huawei.gallery.editor.pipeline;

import com.huawei.gallery.editor.omron.FaceDetection;

public final class EditorLoadLib {
    public static final boolean ARCSOFT_LOADED;
    public static final boolean FEMININE_EFFECT_LOADED;
    public static final boolean FILTERJNI_LOADED;
    public static final boolean FILTERJNI_MIST_LOADED;
    public static final boolean FILTERJNI_MORPHO_LOADED;
    public static final boolean IS_SUPPORT_IMAGE_EDIT;
    public static final boolean OMRONJNI_LOADED;
    private static final String PRODUCT_MANUFACTURER;
    public static final boolean SFBJNI_LOADED;
    public static final boolean TILTCORRECTIONJNI_LOADED;
    public static final boolean VENUSCROPJNI_LOADED;

    static {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Unexpected register number in merge insn: ?: MERGE  (r5_18 boolean) = (r5_1 boolean), (r5_20 boolean)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMerge(EliminatePhiNodes.java:84)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.replaceMergeInstructions(EliminatePhiNodes.java:68)
	at jadx.core.dex.visitors.ssa.EliminatePhiNodes.visit(EliminatePhiNodes.java:31)
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
        r6 = 1;
        r5 = 0;
        r2 = "ro.product.manufacturer";
        r3 = "";
        r2 = android.os.SystemProperties.get(r2, r3);
        PRODUCT_MANUFACTURER = r2;
        r2 = "jni_filtershow_filters";	 Catch:{ UnsatisfiedLinkError -> 0x00a7 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x00a7 }
        IS_SUPPORT_IMAGE_EDIT = r6;
    L_0x0016:
        r2 = "jni_mrc_cg_filters";	 Catch:{ UnsatisfiedLinkError -> 0x00ce }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x00ce }
        r2 = "imedia_filters";	 Catch:{ UnsatisfiedLinkError -> 0x00ce }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x00ce }
        FILTERJNI_LOADED = r6;
    L_0x0024:
        r2 = "jni_mrc_cg_hazeremoval";	 Catch:{ UnsatisfiedLinkError -> 0x00f1 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x00f1 }
        r2 = "mrc_cg_hazeremoval";	 Catch:{ UnsatisfiedLinkError -> 0x00f1 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x00f1 }
        FILTERJNI_MIST_LOADED = r6;
    L_0x0032:
        r2 = "jni_SFBE";	 Catch:{ UnsatisfiedLinkError -> 0x0114 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x0114 }
        r2 = "SFBE";	 Catch:{ UnsatisfiedLinkError -> 0x0114 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x0114 }
        SFBJNI_LOADED = r6;
    L_0x0040:
        r2 = "mrc_cg_beauty";	 Catch:{ UnsatisfiedLinkError -> 0x0137 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x0137 }
        r2 = "arcsoft_beautyshot";	 Catch:{ UnsatisfiedLinkError -> 0x0137 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x0137 }
        ARCSOFT_LOADED = r6;
    L_0x004e:
        r2 = "jni_omron_facebeautifier";	 Catch:{ UnsatisfiedLinkError -> 0x015a }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x015a }
        r2 = "camera_omron";	 Catch:{ UnsatisfiedLinkError -> 0x015a }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x015a }
        OMRONJNI_LOADED = r6;
    L_0x005c:
        r2 = "jni_tiltcorrection";	 Catch:{ UnsatisfiedLinkError -> 0x017d }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x017d }
        r2 = "tiltcorrection";	 Catch:{ UnsatisfiedLinkError -> 0x017d }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x017d }
        TILTCORRECTIONJNI_LOADED = r6;
    L_0x006a:
        r2 = "jni_feminine_filters";	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        r2 = "morpho_effect_library5";	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        r2 = "Huawei";	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        r3 = PRODUCT_MANUFACTURER;	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        r1 = r2.equalsIgnoreCase(r3);	 Catch:{ UnsatisfiedLinkError -> 0x01a0 }
        FEMININE_EFFECT_LOADED = r1;
    L_0x0081:
        r2 = "jni_morpho_filters";	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        r2 = "morpho_effect_library5";	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        r2 = "Huawei";	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        r3 = PRODUCT_MANUFACTURER;	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        r1 = r2.equalsIgnoreCase(r3);	 Catch:{ UnsatisfiedLinkError -> 0x01b2 }
        FILTERJNI_MORPHO_LOADED = r1;
    L_0x0098:
        r2 = "VenusCropImageFace";	 Catch:{ UnsatisfiedLinkError -> 0x01c4 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01c4 }
        r2 = "jni_VenusCropImageFace";	 Catch:{ UnsatisfiedLinkError -> 0x01c4 }
        java.lang.System.loadLibrary(r2);	 Catch:{ UnsatisfiedLinkError -> 0x01c4 }
        VENUSCROPJNI_LOADED = r6;
    L_0x00a6:
        return;
    L_0x00a7:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x00ca }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ca }
        r3.<init>();	 Catch:{ all -> 0x00ca }
        r4 = "load image edit base so failed.";	 Catch:{ all -> 0x00ca }
        r3 = r3.append(r4);	 Catch:{ all -> 0x00ca }
        r4 = r0.getMessage();	 Catch:{ all -> 0x00ca }
        r3 = r3.append(r4);	 Catch:{ all -> 0x00ca }
        r3 = r3.toString();	 Catch:{ all -> 0x00ca }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x00ca }
        IS_SUPPORT_IMAGE_EDIT = r5;
        goto L_0x0016;
    L_0x00ca:
        r2 = move-exception;
        IS_SUPPORT_IMAGE_EDIT = r5;
        throw r2;
    L_0x00ce:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x00ed }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x00ed }
        r3.<init>();	 Catch:{ all -> 0x00ed }
        r4 = "camerefilter so load faile.";	 Catch:{ all -> 0x00ed }
        r3 = r3.append(r4);	 Catch:{ all -> 0x00ed }
        r3 = r3.append(r0);	 Catch:{ all -> 0x00ed }
        r3 = r3.toString();	 Catch:{ all -> 0x00ed }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x00ed }
        FILTERJNI_LOADED = r5;
        goto L_0x0024;
    L_0x00ed:
        r2 = move-exception;
        FILTERJNI_LOADED = r5;
        throw r2;
    L_0x00f1:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x0110 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0110 }
        r3.<init>();	 Catch:{ all -> 0x0110 }
        r4 = "filterMist so load faile.";	 Catch:{ all -> 0x0110 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0110 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0110 }
        r3 = r3.toString();	 Catch:{ all -> 0x0110 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x0110 }
        FILTERJNI_MIST_LOADED = r5;
        goto L_0x0032;
    L_0x0110:
        r2 = move-exception;
        FILTERJNI_MIST_LOADED = r5;
        throw r2;
    L_0x0114:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x0133 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0133 }
        r3.<init>();	 Catch:{ all -> 0x0133 }
        r4 = "skin so load faile.";	 Catch:{ all -> 0x0133 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0133 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0133 }
        r3 = r3.toString();	 Catch:{ all -> 0x0133 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x0133 }
        SFBJNI_LOADED = r5;
        goto L_0x0040;
    L_0x0133:
        r2 = move-exception;
        SFBJNI_LOADED = r5;
        throw r2;
    L_0x0137:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x0156 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0156 }
        r3.<init>();	 Catch:{ all -> 0x0156 }
        r4 = "skin so load faile.";	 Catch:{ all -> 0x0156 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0156 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0156 }
        r3 = r3.toString();	 Catch:{ all -> 0x0156 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x0156 }
        ARCSOFT_LOADED = r5;
        goto L_0x004e;
    L_0x0156:
        r2 = move-exception;
        ARCSOFT_LOADED = r5;
        throw r2;
    L_0x015a:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x0179 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x0179 }
        r3.<init>();	 Catch:{ all -> 0x0179 }
        r4 = "skin so load faile.";	 Catch:{ all -> 0x0179 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x0179 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x0179 }
        r3 = r3.toString();	 Catch:{ all -> 0x0179 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x0179 }
        OMRONJNI_LOADED = r5;
        goto L_0x005c;
    L_0x0179:
        r2 = move-exception;
        OMRONJNI_LOADED = r5;
        throw r2;
    L_0x017d:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x019c }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x019c }
        r3.<init>();	 Catch:{ all -> 0x019c }
        r4 = "tiltcorrection so load faile.";	 Catch:{ all -> 0x019c }
        r3 = r3.append(r4);	 Catch:{ all -> 0x019c }
        r3 = r3.append(r0);	 Catch:{ all -> 0x019c }
        r3 = r3.toString();	 Catch:{ all -> 0x019c }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x019c }
        TILTCORRECTIONJNI_LOADED = r5;
        goto L_0x006a;
    L_0x019c:
        r2 = move-exception;
        TILTCORRECTIONJNI_LOADED = r5;
        throw r2;
    L_0x01a0:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x01ae }
        r3 = "morpho_effect so load faile.";	 Catch:{ all -> 0x01ae }
        com.android.gallery3d.util.GalleryLog.d(r2, r3, r0);	 Catch:{ all -> 0x01ae }
        FEMININE_EFFECT_LOADED = r5;
        goto L_0x0081;
    L_0x01ae:
        r2 = move-exception;
        FEMININE_EFFECT_LOADED = r5;
        throw r2;
    L_0x01b2:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x01c0 }
        r3 = "morpho_effect so load faile.";	 Catch:{ all -> 0x01c0 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3, r0);	 Catch:{ all -> 0x01c0 }
        FILTERJNI_MORPHO_LOADED = r5;
        goto L_0x0098;
    L_0x01c0:
        r2 = move-exception;
        FILTERJNI_MORPHO_LOADED = r5;
        throw r2;
    L_0x01c4:
        r0 = move-exception;
        r2 = "EditorLoadLib";	 Catch:{ all -> 0x01e3 }
        r3 = new java.lang.StringBuilder;	 Catch:{ all -> 0x01e3 }
        r3.<init>();	 Catch:{ all -> 0x01e3 }
        r4 = "VenusCrop so load faile.";	 Catch:{ all -> 0x01e3 }
        r3 = r3.append(r4);	 Catch:{ all -> 0x01e3 }
        r3 = r3.append(r0);	 Catch:{ all -> 0x01e3 }
        r3 = r3.toString();	 Catch:{ all -> 0x01e3 }
        com.android.gallery3d.util.GalleryLog.d(r2, r3);	 Catch:{ all -> 0x01e3 }
        VENUSCROPJNI_LOADED = r5;
        goto L_0x00a6;
    L_0x01e3:
        r2 = move-exception;
        VENUSCROPJNI_LOADED = r5;
        throw r2;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.gallery.editor.pipeline.EditorLoadLib.<clinit>():void");
    }

    public static boolean isSupportSkin() {
        if (isArcSoftLoaded() || SFBJNI_LOADED) {
            return true;
        }
        return OMRONJNI_LOADED && FaceDetection.getOmronSoVersion() == 0;
    }

    public static boolean isArcSoftLoaded() {
        return OMRONJNI_LOADED ? ARCSOFT_LOADED : false;
    }
}
