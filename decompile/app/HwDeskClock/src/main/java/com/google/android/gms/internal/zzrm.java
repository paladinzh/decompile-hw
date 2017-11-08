package com.google.android.gms.internal;

import java.io.IOException;

/* compiled from: Unknown */
public final class zzrm extends zzrr<zzrm> {
    public zza[] zzbbu;

    /* compiled from: Unknown */
    public static final class zza extends zzrr<zza> {
        private static volatile zza[] zzbbv;
        public String name;
        public zza zzbbw;

        /* compiled from: Unknown */
        public static final class zza extends zzrr<zza> {
            private static volatile zza[] zzbbx;
            public int type;
            public zza zzbby;

            /* compiled from: Unknown */
            public static final class zza extends zzrr<zza> {
                public String zzbbA;
                public double zzbbB;
                public float zzbbC;
                public long zzbbD;
                public int zzbbE;
                public int zzbbF;
                public boolean zzbbG;
                public zza[] zzbbH;
                public zza[] zzbbI;
                public String[] zzbbJ;
                public long[] zzbbK;
                public float[] zzbbL;
                public long zzbbM;
                public byte[] zzbbz;

                public zza() {
                    /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r0 = this;
                    <init>();
                    zzCT();
                    return;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.<init>():void");
                }

                public boolean equals(java.lang.Object r6) {
                    /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r5 = this;
                    r4 = 0;
                    if (r6 == r5) goto L_0x0032;
                L_0x0003:
                    r0 = r6 instanceof com.google.android.gms.internal.zzrm.zza.zza.zza;
                    if (r0 == 0) goto L_0x0034;
                L_0x0007:
                    r6 = (com.google.android.gms.internal.zzrm.zza.zza.zza) r6;
                    r0 = r5.zzbbz;
                    r1 = r6.zzbbz;
                    r0 = java.util.Arrays.equals(r0, r1);
                    if (r0 == 0) goto L_0x0035;
                L_0x0013:
                    r0 = r5.zzbbA;
                    if (r0 == 0) goto L_0x0036;
                L_0x0017:
                    r0 = r5.zzbbA;
                    r1 = r6.zzbbA;
                    r0 = r0.equals(r1);
                    if (r0 == 0) goto L_0x003b;
                L_0x0021:
                    r0 = r5.zzbbB;
                    r0 = java.lang.Double.doubleToLongBits(r0);
                    r2 = r6.zzbbB;
                    r2 = java.lang.Double.doubleToLongBits(r2);
                    r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
                    if (r0 == 0) goto L_0x003c;
                L_0x0031:
                    return r4;
                L_0x0032:
                    r0 = 1;
                    return r0;
                L_0x0034:
                    return r4;
                L_0x0035:
                    return r4;
                L_0x0036:
                    r0 = r6.zzbbA;
                    if (r0 == 0) goto L_0x0021;
                L_0x003a:
                    return r4;
                L_0x003b:
                    return r4;
                L_0x003c:
                    r0 = r5.zzbbC;
                    r0 = java.lang.Float.floatToIntBits(r0);
                    r1 = r6.zzbbC;
                    r1 = java.lang.Float.floatToIntBits(r1);
                    if (r0 != r1) goto L_0x0053;
                L_0x004a:
                    r0 = r5.zzbbD;
                    r2 = r6.zzbbD;
                    r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
                    if (r0 == 0) goto L_0x0054;
                L_0x0052:
                    return r4;
                L_0x0053:
                    return r4;
                L_0x0054:
                    r0 = r5.zzbbE;
                    r1 = r6.zzbbE;
                    if (r0 != r1) goto L_0x00a1;
                L_0x005a:
                    r0 = r5.zzbbF;
                    r1 = r6.zzbbF;
                    if (r0 != r1) goto L_0x00a2;
                L_0x0060:
                    r0 = r5.zzbbG;
                    r1 = r6.zzbbG;
                    if (r0 != r1) goto L_0x00a3;
                L_0x0066:
                    r0 = r5.zzbbH;
                    r1 = r6.zzbbH;
                    r0 = com.google.android.gms.internal.zzrv.equals(r0, r1);
                    if (r0 == 0) goto L_0x00a4;
                L_0x0070:
                    r0 = r5.zzbbI;
                    r1 = r6.zzbbI;
                    r0 = com.google.android.gms.internal.zzrv.equals(r0, r1);
                    if (r0 == 0) goto L_0x00a5;
                L_0x007a:
                    r0 = r5.zzbbJ;
                    r1 = r6.zzbbJ;
                    r0 = com.google.android.gms.internal.zzrv.equals(r0, r1);
                    if (r0 == 0) goto L_0x00a6;
                L_0x0084:
                    r0 = r5.zzbbK;
                    r1 = r6.zzbbK;
                    r0 = com.google.android.gms.internal.zzrv.equals(r0, r1);
                    if (r0 == 0) goto L_0x00a7;
                L_0x008e:
                    r0 = r5.zzbbL;
                    r1 = r6.zzbbL;
                    r0 = com.google.android.gms.internal.zzrv.equals(r0, r1);
                    if (r0 == 0) goto L_0x00a8;
                L_0x0098:
                    r0 = r5.zzbbM;
                    r2 = r6.zzbbM;
                    r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1));
                    if (r0 == 0) goto L_0x00a9;
                L_0x00a0:
                    return r4;
                L_0x00a1:
                    return r4;
                L_0x00a2:
                    return r4;
                L_0x00a3:
                    return r4;
                L_0x00a4:
                    return r4;
                L_0x00a5:
                    return r4;
                L_0x00a6:
                    return r4;
                L_0x00a7:
                    return r4;
                L_0x00a8:
                    return r4;
                L_0x00a9:
                    r0 = r5.zza(r6);
                    return r0;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.equals(java.lang.Object):boolean");
                }

                public int hashCode() {
                    /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r7 = this;
                    r0 = 0;
                    r6 = 32;
                    r1 = r7.zzbbz;
                    r1 = java.util.Arrays.hashCode(r1);
                    r1 = r1 + 527;
                    r1 = r1 * 31;
                    r2 = r7.zzbbA;
                    if (r2 == 0) goto L_0x0017;
                L_0x0011:
                    r0 = r7.zzbbA;
                    r0 = r0.hashCode();
                L_0x0017:
                    r0 = r0 + r1;
                    r2 = r7.zzbbB;
                    r2 = java.lang.Double.doubleToLongBits(r2);
                    r0 = r0 * 31;
                    r4 = r2 >>> r6;
                    r2 = r2 ^ r4;
                    r1 = (int) r2;
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbC;
                    r1 = java.lang.Float.floatToIntBits(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r2 = r7.zzbbD;
                    r4 = r7.zzbbD;
                    r4 = r4 >>> r6;
                    r2 = r2 ^ r4;
                    r1 = (int) r2;
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbE;
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbF;
                    r0 = r0 + r1;
                    r1 = r0 * 31;
                    r0 = r7.zzbbG;
                    if (r0 != 0) goto L_0x008a;
                L_0x0048:
                    r0 = 1237; // 0x4d5 float:1.733E-42 double:6.11E-321;
                L_0x004a:
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbH;
                    r1 = com.google.android.gms.internal.zzrv.hashCode(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbI;
                    r1 = com.google.android.gms.internal.zzrv.hashCode(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbJ;
                    r1 = com.google.android.gms.internal.zzrv.hashCode(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbK;
                    r1 = com.google.android.gms.internal.zzrv.hashCode(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzbbL;
                    r1 = com.google.android.gms.internal.zzrv.hashCode(r1);
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r2 = r7.zzbbM;
                    r4 = r7.zzbbM;
                    r4 = r4 >>> r6;
                    r2 = r2 ^ r4;
                    r1 = (int) r2;
                    r0 = r0 + r1;
                    r0 = r0 * 31;
                    r1 = r7.zzDm();
                    r0 = r0 + r1;
                    return r0;
                L_0x008a:
                    r0 = 1231; // 0x4cf float:1.725E-42 double:6.08E-321;
                    goto L_0x004a;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.hashCode():int");
                }

                protected int zzB() {
                    /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r10 = this;
                    r8 = 0;
                    r6 = 4;
                    r4 = 1;
                    r1 = 0;
                    r0 = super.zzB();
                    r2 = r10.zzbbz;
                    r3 = com.google.android.gms.internal.zzsa.zzbcx;
                    r2 = java.util.Arrays.equals(r2, r3);
                    if (r2 == 0) goto L_0x0081;
                L_0x0013:
                    r2 = r10.zzbbA;
                    r3 = "";
                    r2 = r2.equals(r3);
                    if (r2 == 0) goto L_0x0089;
                L_0x001e:
                    r2 = r10.zzbbB;
                    r2 = java.lang.Double.doubleToLongBits(r2);
                    r4 = 0;
                    r4 = java.lang.Double.doubleToLongBits(r4);
                    r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
                    if (r2 == 0) goto L_0x0036;
                L_0x002e:
                    r2 = 3;
                    r4 = r10.zzbbB;
                    r2 = com.google.android.gms.internal.zzrq.zzb(r2, r4);
                    r0 = r0 + r2;
                L_0x0036:
                    r2 = r10.zzbbC;
                    r2 = java.lang.Float.floatToIntBits(r2);
                    r3 = 0;
                    r3 = java.lang.Float.floatToIntBits(r3);
                    if (r2 != r3) goto L_0x0092;
                L_0x0043:
                    r2 = r10.zzbbD;
                    r2 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1));
                    if (r2 == 0) goto L_0x0051;
                L_0x0049:
                    r2 = 5;
                    r4 = r10.zzbbD;
                    r2 = com.google.android.gms.internal.zzrq.zzd(r2, r4);
                    r0 = r0 + r2;
                L_0x0051:
                    r2 = r10.zzbbE;
                    if (r2 != 0) goto L_0x009a;
                L_0x0055:
                    r2 = r10.zzbbF;
                    if (r2 != 0) goto L_0x00a3;
                L_0x0059:
                    r2 = r10.zzbbG;
                    if (r2 != 0) goto L_0x00ac;
                L_0x005d:
                    r2 = r10.zzbbH;
                    if (r2 != 0) goto L_0x00b6;
                L_0x0061:
                    r2 = r10.zzbbI;
                    if (r2 != 0) goto L_0x00d5;
                L_0x0065:
                    r2 = r10.zzbbJ;
                    if (r2 != 0) goto L_0x00f4;
                L_0x0069:
                    r2 = r10.zzbbK;
                    if (r2 != 0) goto L_0x0118;
                L_0x006d:
                    r2 = r10.zzbbM;
                    r1 = (r2 > r8 ? 1 : (r2 == r8 ? 0 : -1));
                    if (r1 == 0) goto L_0x007c;
                L_0x0073:
                    r1 = 13;
                    r2 = r10.zzbbM;
                    r1 = com.google.android.gms.internal.zzrq.zzd(r1, r2);
                    r0 = r0 + r1;
                L_0x007c:
                    r1 = r10.zzbbL;
                    if (r1 != 0) goto L_0x0138;
                L_0x0080:
                    return r0;
                L_0x0081:
                    r2 = r10.zzbbz;
                    r2 = com.google.android.gms.internal.zzrq.zzb(r4, r2);
                    r0 = r0 + r2;
                    goto L_0x0013;
                L_0x0089:
                    r2 = 2;
                    r3 = r10.zzbbA;
                    r2 = com.google.android.gms.internal.zzrq.zzl(r2, r3);
                    r0 = r0 + r2;
                    goto L_0x001e;
                L_0x0092:
                    r2 = r10.zzbbC;
                    r2 = com.google.android.gms.internal.zzrq.zzc(r6, r2);
                    r0 = r0 + r2;
                    goto L_0x0043;
                L_0x009a:
                    r2 = 6;
                    r3 = r10.zzbbE;
                    r2 = com.google.android.gms.internal.zzrq.zzB(r2, r3);
                    r0 = r0 + r2;
                    goto L_0x0055;
                L_0x00a3:
                    r2 = 7;
                    r3 = r10.zzbbF;
                    r2 = com.google.android.gms.internal.zzrq.zzC(r2, r3);
                    r0 = r0 + r2;
                    goto L_0x0059;
                L_0x00ac:
                    r2 = 8;
                    r3 = r10.zzbbG;
                    r2 = com.google.android.gms.internal.zzrq.zzc(r2, r3);
                    r0 = r0 + r2;
                    goto L_0x005d;
                L_0x00b6:
                    r2 = r10.zzbbH;
                    r2 = r2.length;
                    if (r2 <= 0) goto L_0x0061;
                L_0x00bb:
                    r2 = r0;
                    r0 = r1;
                L_0x00bd:
                    r3 = r10.zzbbH;
                    r3 = r3.length;
                    if (r0 < r3) goto L_0x00c4;
                L_0x00c2:
                    r0 = r2;
                    goto L_0x0061;
                L_0x00c4:
                    r3 = r10.zzbbH;
                    r3 = r3[r0];
                    if (r3 != 0) goto L_0x00cd;
                L_0x00ca:
                    r0 = r0 + 1;
                    goto L_0x00bd;
                L_0x00cd:
                    r4 = 9;
                    r3 = com.google.android.gms.internal.zzrq.zzc(r4, r3);
                    r2 = r2 + r3;
                    goto L_0x00ca;
                L_0x00d5:
                    r2 = r10.zzbbI;
                    r2 = r2.length;
                    if (r2 <= 0) goto L_0x0065;
                L_0x00da:
                    r2 = r0;
                    r0 = r1;
                L_0x00dc:
                    r3 = r10.zzbbI;
                    r3 = r3.length;
                    if (r0 < r3) goto L_0x00e3;
                L_0x00e1:
                    r0 = r2;
                    goto L_0x0065;
                L_0x00e3:
                    r3 = r10.zzbbI;
                    r3 = r3[r0];
                    if (r3 != 0) goto L_0x00ec;
                L_0x00e9:
                    r0 = r0 + 1;
                    goto L_0x00dc;
                L_0x00ec:
                    r4 = 10;
                    r3 = com.google.android.gms.internal.zzrq.zzc(r4, r3);
                    r2 = r2 + r3;
                    goto L_0x00e9;
                L_0x00f4:
                    r2 = r10.zzbbJ;
                    r2 = r2.length;
                    if (r2 <= 0) goto L_0x0069;
                L_0x00f9:
                    r2 = r1;
                    r3 = r1;
                    r4 = r1;
                L_0x00fc:
                    r5 = r10.zzbbJ;
                    r5 = r5.length;
                    if (r2 < r5) goto L_0x0107;
                L_0x0101:
                    r0 = r0 + r3;
                    r2 = r4 * 1;
                    r0 = r0 + r2;
                    goto L_0x0069;
                L_0x0107:
                    r5 = r10.zzbbJ;
                    r5 = r5[r2];
                    if (r5 != 0) goto L_0x0110;
                L_0x010d:
                    r2 = r2 + 1;
                    goto L_0x00fc;
                L_0x0110:
                    r4 = r4 + 1;
                    r5 = com.google.android.gms.internal.zzrq.zzfy(r5);
                    r3 = r3 + r5;
                    goto L_0x010d;
                L_0x0118:
                    r2 = r10.zzbbK;
                    r2 = r2.length;
                    if (r2 <= 0) goto L_0x006d;
                L_0x011d:
                    r2 = r1;
                L_0x011e:
                    r3 = r10.zzbbK;
                    r3 = r3.length;
                    if (r1 < r3) goto L_0x012c;
                L_0x0123:
                    r0 = r0 + r2;
                    r1 = r10.zzbbK;
                    r1 = r1.length;
                    r1 = r1 * 1;
                    r0 = r0 + r1;
                    goto L_0x006d;
                L_0x012c:
                    r3 = r10.zzbbK;
                    r4 = r3[r1];
                    r3 = com.google.android.gms.internal.zzrq.zzY(r4);
                    r2 = r2 + r3;
                    r1 = r1 + 1;
                    goto L_0x011e;
                L_0x0138:
                    r1 = r10.zzbbL;
                    r1 = r1.length;
                    if (r1 <= 0) goto L_0x0080;
                L_0x013d:
                    r1 = r10.zzbbL;
                    r1 = r1.length;
                    r1 = r1 * 4;
                    r0 = r0 + r1;
                    r1 = r10.zzbbL;
                    r1 = r1.length;
                    r1 = r1 * 1;
                    r0 = r0 + r1;
                    goto L_0x0080;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.zzB():int");
                }

                public com.google.android.gms.internal.zzrm.zza.zza.zza zzCT() {
                    /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r6 = this;
                    r4 = 0;
                    r2 = 0;
                    r0 = com.google.android.gms.internal.zzsa.zzbcx;
                    r6.zzbbz = r0;
                    r0 = "";
                    r6.zzbbA = r0;
                    r0 = 0;
                    r6.zzbbB = r0;
                    r0 = 0;
                    r6.zzbbC = r0;
                    r6.zzbbD = r4;
                    r6.zzbbE = r2;
                    r6.zzbbF = r2;
                    r6.zzbbG = r2;
                    r0 = com.google.android.gms.internal.zzrm.zza.zzCP();
                    r6.zzbbH = r0;
                    r0 = com.google.android.gms.internal.zzrm.zza.zza.zzCR();
                    r6.zzbbI = r0;
                    r0 = com.google.android.gms.internal.zzsa.zzbcv;
                    r6.zzbbJ = r0;
                    r0 = com.google.android.gms.internal.zzsa.zzbcr;
                    r6.zzbbK = r0;
                    r0 = com.google.android.gms.internal.zzsa.zzbcs;
                    r6.zzbbL = r0;
                    r6.zzbbM = r4;
                    r0 = 0;
                    r6.zzbcd = r0;
                    r0 = -1;
                    r6.zzbco = r0;
                    return r6;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.zzCT():com.google.android.gms.internal.zzrm$zza$zza$zza");
                }

                public void zza(com.google.android.gms.internal.zzrq r9) throws java.io.IOException {
                    /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r8 = this;
                    r6 = 0;
                    r1 = 0;
                    r0 = r8.zzbbz;
                    r2 = com.google.android.gms.internal.zzsa.zzbcx;
                    r0 = java.util.Arrays.equals(r0, r2);
                    if (r0 == 0) goto L_0x0078;
                L_0x000d:
                    r0 = r8.zzbbA;
                    r2 = "";
                    r0 = r0.equals(r2);
                    if (r0 == 0) goto L_0x007f;
                L_0x0018:
                    r2 = r8.zzbbB;
                    r2 = java.lang.Double.doubleToLongBits(r2);
                    r4 = 0;
                    r4 = java.lang.Double.doubleToLongBits(r4);
                    r0 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1));
                    if (r0 == 0) goto L_0x002e;
                L_0x0028:
                    r0 = 3;
                    r2 = r8.zzbbB;
                    r9.zza(r0, r2);
                L_0x002e:
                    r0 = r8.zzbbC;
                    r0 = java.lang.Float.floatToIntBits(r0);
                    r2 = 0;
                    r2 = java.lang.Float.floatToIntBits(r2);
                    if (r0 != r2) goto L_0x0086;
                L_0x003b:
                    r2 = r8.zzbbD;
                    r0 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1));
                    if (r0 == 0) goto L_0x0047;
                L_0x0041:
                    r0 = 5;
                    r2 = r8.zzbbD;
                    r9.zzb(r0, r2);
                L_0x0047:
                    r0 = r8.zzbbE;
                    if (r0 != 0) goto L_0x008d;
                L_0x004b:
                    r0 = r8.zzbbF;
                    if (r0 != 0) goto L_0x0094;
                L_0x004f:
                    r0 = r8.zzbbG;
                    if (r0 != 0) goto L_0x009b;
                L_0x0053:
                    r0 = r8.zzbbH;
                    if (r0 != 0) goto L_0x00a3;
                L_0x0057:
                    r0 = r8.zzbbI;
                    if (r0 != 0) goto L_0x00bd;
                L_0x005b:
                    r0 = r8.zzbbJ;
                    if (r0 != 0) goto L_0x00d7;
                L_0x005f:
                    r0 = r8.zzbbK;
                    if (r0 != 0) goto L_0x00f1;
                L_0x0063:
                    r2 = r8.zzbbM;
                    r0 = (r2 > r6 ? 1 : (r2 == r6 ? 0 : -1));
                    if (r0 == 0) goto L_0x0070;
                L_0x0069:
                    r0 = 13;
                    r2 = r8.zzbbM;
                    r9.zzb(r0, r2);
                L_0x0070:
                    r0 = r8.zzbbL;
                    if (r0 != 0) goto L_0x0108;
                L_0x0074:
                    super.zza(r9);
                    return;
                L_0x0078:
                    r0 = 1;
                    r2 = r8.zzbbz;
                    r9.zza(r0, r2);
                    goto L_0x000d;
                L_0x007f:
                    r0 = 2;
                    r2 = r8.zzbbA;
                    r9.zzb(r0, r2);
                    goto L_0x0018;
                L_0x0086:
                    r0 = 4;
                    r2 = r8.zzbbC;
                    r9.zzb(r0, r2);
                    goto L_0x003b;
                L_0x008d:
                    r0 = 6;
                    r2 = r8.zzbbE;
                    r9.zzz(r0, r2);
                    goto L_0x004b;
                L_0x0094:
                    r0 = 7;
                    r2 = r8.zzbbF;
                    r9.zzA(r0, r2);
                    goto L_0x004f;
                L_0x009b:
                    r0 = 8;
                    r2 = r8.zzbbG;
                    r9.zzb(r0, r2);
                    goto L_0x0053;
                L_0x00a3:
                    r0 = r8.zzbbH;
                    r0 = r0.length;
                    if (r0 <= 0) goto L_0x0057;
                L_0x00a8:
                    r0 = r1;
                L_0x00a9:
                    r2 = r8.zzbbH;
                    r2 = r2.length;
                    if (r0 >= r2) goto L_0x0057;
                L_0x00ae:
                    r2 = r8.zzbbH;
                    r2 = r2[r0];
                    if (r2 != 0) goto L_0x00b7;
                L_0x00b4:
                    r0 = r0 + 1;
                    goto L_0x00a9;
                L_0x00b7:
                    r3 = 9;
                    r9.zza(r3, r2);
                    goto L_0x00b4;
                L_0x00bd:
                    r0 = r8.zzbbI;
                    r0 = r0.length;
                    if (r0 <= 0) goto L_0x005b;
                L_0x00c2:
                    r0 = r1;
                L_0x00c3:
                    r2 = r8.zzbbI;
                    r2 = r2.length;
                    if (r0 >= r2) goto L_0x005b;
                L_0x00c8:
                    r2 = r8.zzbbI;
                    r2 = r2[r0];
                    if (r2 != 0) goto L_0x00d1;
                L_0x00ce:
                    r0 = r0 + 1;
                    goto L_0x00c3;
                L_0x00d1:
                    r3 = 10;
                    r9.zza(r3, r2);
                    goto L_0x00ce;
                L_0x00d7:
                    r0 = r8.zzbbJ;
                    r0 = r0.length;
                    if (r0 <= 0) goto L_0x005f;
                L_0x00dc:
                    r0 = r1;
                L_0x00dd:
                    r2 = r8.zzbbJ;
                    r2 = r2.length;
                    if (r0 >= r2) goto L_0x005f;
                L_0x00e2:
                    r2 = r8.zzbbJ;
                    r2 = r2[r0];
                    if (r2 != 0) goto L_0x00eb;
                L_0x00e8:
                    r0 = r0 + 1;
                    goto L_0x00dd;
                L_0x00eb:
                    r3 = 11;
                    r9.zzb(r3, r2);
                    goto L_0x00e8;
                L_0x00f1:
                    r0 = r8.zzbbK;
                    r0 = r0.length;
                    if (r0 <= 0) goto L_0x0063;
                L_0x00f6:
                    r0 = r1;
                L_0x00f7:
                    r2 = r8.zzbbK;
                    r2 = r2.length;
                    if (r0 >= r2) goto L_0x0063;
                L_0x00fc:
                    r2 = 12;
                    r3 = r8.zzbbK;
                    r4 = r3[r0];
                    r9.zzb(r2, r4);
                    r0 = r0 + 1;
                    goto L_0x00f7;
                L_0x0108:
                    r0 = r8.zzbbL;
                    r0 = r0.length;
                    if (r0 <= 0) goto L_0x0074;
                L_0x010d:
                    r0 = r8.zzbbL;
                    r0 = r0.length;
                    if (r1 >= r0) goto L_0x0074;
                L_0x0112:
                    r0 = 14;
                    r2 = r8.zzbbL;
                    r2 = r2[r1];
                    r9.zzb(r0, r2);
                    r1 = r1 + 1;
                    goto L_0x010d;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.zza(com.google.android.gms.internal.zzrq):void");
                }

                public /* synthetic */ com.google.android.gms.internal.zzrx zzb(com.google.android.gms.internal.zzrp r2) throws java.io.IOException {
                    /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r1 = this;
                    r0 = r1.zzy(r2);
                    return r0;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.zzb(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrx");
                }

                public com.google.android.gms.internal.zzrm.zza.zza.zza zzy(com.google.android.gms.internal.zzrp r7) throws java.io.IOException {
                    /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x0001 in list [B:1:0x0001, B:5:0x000e]
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                    /*
                    r6 = this;
                    r1 = 0;
                L_0x0001:
                    r0 = r7.zzCV();
                    switch(r0) {
                        case 0: goto L_0x000f;
                        case 10: goto L_0x0010;
                        case 18: goto L_0x0017;
                        case 25: goto L_0x001e;
                        case 37: goto L_0x0025;
                        case 40: goto L_0x002c;
                        case 48: goto L_0x0033;
                        case 56: goto L_0x003a;
                        case 64: goto L_0x0041;
                        case 74: goto L_0x0048;
                        case 82: goto L_0x0088;
                        case 90: goto L_0x00c9;
                        case 96: goto L_0x00fe;
                        case 98: goto L_0x0133;
                        case 104: goto L_0x0176;
                        case 114: goto L_0x01b3;
                        case 117: goto L_0x017e;
                        default: goto L_0x0008;
                    };
                L_0x0008:
                    r0 = r6.zza(r7, r0);
                    if (r0 != 0) goto L_0x0001;
                L_0x000e:
                    return r6;
                L_0x000f:
                    return r6;
                L_0x0010:
                    r0 = r7.readBytes();
                    r6.zzbbz = r0;
                    goto L_0x0001;
                L_0x0017:
                    r0 = r7.readString();
                    r6.zzbbA = r0;
                    goto L_0x0001;
                L_0x001e:
                    r2 = r7.readDouble();
                    r6.zzbbB = r2;
                    goto L_0x0001;
                L_0x0025:
                    r0 = r7.readFloat();
                    r6.zzbbC = r0;
                    goto L_0x0001;
                L_0x002c:
                    r2 = r7.zzCX();
                    r6.zzbbD = r2;
                    goto L_0x0001;
                L_0x0033:
                    r0 = r7.zzCY();
                    r6.zzbbE = r0;
                    goto L_0x0001;
                L_0x003a:
                    r0 = r7.zzDa();
                    r6.zzbbF = r0;
                    goto L_0x0001;
                L_0x0041:
                    r0 = r7.zzCZ();
                    r6.zzbbG = r0;
                    goto L_0x0001;
                L_0x0048:
                    r0 = 74;
                    r2 = com.google.android.gms.internal.zzsa.zzb(r7, r0);
                    r0 = r6.zzbbH;
                    if (r0 == 0) goto L_0x006e;
                L_0x0052:
                    r0 = r6.zzbbH;
                    r0 = r0.length;
                L_0x0055:
                    r2 = r2 + r0;
                    r2 = new com.google.android.gms.internal.zzrm.zza[r2];
                    if (r0 != 0) goto L_0x0070;
                L_0x005a:
                    r3 = r2.length;
                    r3 = r3 + -1;
                    if (r0 < r3) goto L_0x0076;
                L_0x005f:
                    r3 = new com.google.android.gms.internal.zzrm$zza;
                    r3.<init>();
                    r2[r0] = r3;
                    r0 = r2[r0];
                    r7.zza(r0);
                    r6.zzbbH = r2;
                    goto L_0x0001;
                L_0x006e:
                    r0 = r1;
                    goto L_0x0055;
                L_0x0070:
                    r3 = r6.zzbbH;
                    java.lang.System.arraycopy(r3, r1, r2, r1, r0);
                    goto L_0x005a;
                L_0x0076:
                    r3 = new com.google.android.gms.internal.zzrm$zza;
                    r3.<init>();
                    r2[r0] = r3;
                    r3 = r2[r0];
                    r7.zza(r3);
                    r7.zzCV();
                    r0 = r0 + 1;
                    goto L_0x005a;
                L_0x0088:
                    r0 = 82;
                    r2 = com.google.android.gms.internal.zzsa.zzb(r7, r0);
                    r0 = r6.zzbbI;
                    if (r0 == 0) goto L_0x00af;
                L_0x0092:
                    r0 = r6.zzbbI;
                    r0 = r0.length;
                L_0x0095:
                    r2 = r2 + r0;
                    r2 = new com.google.android.gms.internal.zzrm.zza.zza[r2];
                    if (r0 != 0) goto L_0x00b1;
                L_0x009a:
                    r3 = r2.length;
                    r3 = r3 + -1;
                    if (r0 < r3) goto L_0x00b7;
                L_0x009f:
                    r3 = new com.google.android.gms.internal.zzrm$zza$zza;
                    r3.<init>();
                    r2[r0] = r3;
                    r0 = r2[r0];
                    r7.zza(r0);
                    r6.zzbbI = r2;
                    goto L_0x0001;
                L_0x00af:
                    r0 = r1;
                    goto L_0x0095;
                L_0x00b1:
                    r3 = r6.zzbbI;
                    java.lang.System.arraycopy(r3, r1, r2, r1, r0);
                    goto L_0x009a;
                L_0x00b7:
                    r3 = new com.google.android.gms.internal.zzrm$zza$zza;
                    r3.<init>();
                    r2[r0] = r3;
                    r3 = r2[r0];
                    r7.zza(r3);
                    r7.zzCV();
                    r0 = r0 + 1;
                    goto L_0x009a;
                L_0x00c9:
                    r0 = 90;
                    r2 = com.google.android.gms.internal.zzsa.zzb(r7, r0);
                    r0 = r6.zzbbJ;
                    if (r0 == 0) goto L_0x00ea;
                L_0x00d3:
                    r0 = r6.zzbbJ;
                    r0 = r0.length;
                L_0x00d6:
                    r2 = r2 + r0;
                    r2 = new java.lang.String[r2];
                    if (r0 != 0) goto L_0x00ec;
                L_0x00db:
                    r3 = r2.length;
                    r3 = r3 + -1;
                    if (r0 < r3) goto L_0x00f2;
                L_0x00e0:
                    r3 = r7.readString();
                    r2[r0] = r3;
                    r6.zzbbJ = r2;
                    goto L_0x0001;
                L_0x00ea:
                    r0 = r1;
                    goto L_0x00d6;
                L_0x00ec:
                    r3 = r6.zzbbJ;
                    java.lang.System.arraycopy(r3, r1, r2, r1, r0);
                    goto L_0x00db;
                L_0x00f2:
                    r3 = r7.readString();
                    r2[r0] = r3;
                    r7.zzCV();
                    r0 = r0 + 1;
                    goto L_0x00db;
                L_0x00fe:
                    r0 = 96;
                    r2 = com.google.android.gms.internal.zzsa.zzb(r7, r0);
                    r0 = r6.zzbbK;
                    if (r0 == 0) goto L_0x011f;
                L_0x0108:
                    r0 = r6.zzbbK;
                    r0 = r0.length;
                L_0x010b:
                    r2 = r2 + r0;
                    r2 = new long[r2];
                    if (r0 != 0) goto L_0x0121;
                L_0x0110:
                    r3 = r2.length;
                    r3 = r3 + -1;
                    if (r0 < r3) goto L_0x0127;
                L_0x0115:
                    r4 = r7.zzCX();
                    r2[r0] = r4;
                    r6.zzbbK = r2;
                    goto L_0x0001;
                L_0x011f:
                    r0 = r1;
                    goto L_0x010b;
                L_0x0121:
                    r3 = r6.zzbbK;
                    java.lang.System.arraycopy(r3, r1, r2, r1, r0);
                    goto L_0x0110;
                L_0x0127:
                    r4 = r7.zzCX();
                    r2[r0] = r4;
                    r7.zzCV();
                    r0 = r0 + 1;
                    goto L_0x0110;
                L_0x0133:
                    r0 = r7.zzDc();
                    r3 = r7.zzll(r0);
                    r2 = r7.getPosition();
                    r0 = r1;
                L_0x0140:
                    r4 = r7.zzDh();
                    if (r4 > 0) goto L_0x015f;
                L_0x0146:
                    r7.zzln(r2);
                    r2 = r6.zzbbK;
                    if (r2 == 0) goto L_0x0165;
                L_0x014d:
                    r2 = r6.zzbbK;
                    r2 = r2.length;
                L_0x0150:
                    r0 = r0 + r2;
                    r0 = new long[r0];
                    if (r2 != 0) goto L_0x0167;
                L_0x0155:
                    r4 = r0.length;
                    if (r2 < r4) goto L_0x016d;
                L_0x0158:
                    r6.zzbbK = r0;
                    r7.zzlm(r3);
                    goto L_0x0001;
                L_0x015f:
                    r7.zzCX();
                    r0 = r0 + 1;
                    goto L_0x0140;
                L_0x0165:
                    r2 = r1;
                    goto L_0x0150;
                L_0x0167:
                    r4 = r6.zzbbK;
                    java.lang.System.arraycopy(r4, r1, r0, r1, r2);
                    goto L_0x0155;
                L_0x016d:
                    r4 = r7.zzCX();
                    r0[r2] = r4;
                    r2 = r2 + 1;
                    goto L_0x0155;
                L_0x0176:
                    r2 = r7.zzCX();
                    r6.zzbbM = r2;
                    goto L_0x0001;
                L_0x017e:
                    r0 = 117; // 0x75 float:1.64E-43 double:5.8E-322;
                    r2 = com.google.android.gms.internal.zzsa.zzb(r7, r0);
                    r0 = r6.zzbbL;
                    if (r0 == 0) goto L_0x019f;
                L_0x0188:
                    r0 = r6.zzbbL;
                    r0 = r0.length;
                L_0x018b:
                    r2 = r2 + r0;
                    r2 = new float[r2];
                    if (r0 != 0) goto L_0x01a1;
                L_0x0190:
                    r3 = r2.length;
                    r3 = r3 + -1;
                    if (r0 < r3) goto L_0x01a7;
                L_0x0195:
                    r3 = r7.readFloat();
                    r2[r0] = r3;
                    r6.zzbbL = r2;
                    goto L_0x0001;
                L_0x019f:
                    r0 = r1;
                    goto L_0x018b;
                L_0x01a1:
                    r3 = r6.zzbbL;
                    java.lang.System.arraycopy(r3, r1, r2, r1, r0);
                    goto L_0x0190;
                L_0x01a7:
                    r3 = r7.readFloat();
                    r2[r0] = r3;
                    r7.zzCV();
                    r0 = r0 + 1;
                    goto L_0x0190;
                L_0x01b3:
                    r0 = r7.zzDc();
                    r2 = r7.zzll(r0);
                    r3 = r0 / 4;
                    r0 = r6.zzbbL;
                    if (r0 == 0) goto L_0x01d3;
                L_0x01c1:
                    r0 = r6.zzbbL;
                    r0 = r0.length;
                L_0x01c4:
                    r3 = r3 + r0;
                    r3 = new float[r3];
                    if (r0 != 0) goto L_0x01d5;
                L_0x01c9:
                    r4 = r3.length;
                    if (r0 < r4) goto L_0x01db;
                L_0x01cc:
                    r6.zzbbL = r3;
                    r7.zzlm(r2);
                    goto L_0x0001;
                L_0x01d3:
                    r0 = r1;
                    goto L_0x01c4;
                L_0x01d5:
                    r4 = r6.zzbbL;
                    java.lang.System.arraycopy(r4, r1, r3, r1, r0);
                    goto L_0x01c9;
                L_0x01db:
                    r4 = r7.readFloat();
                    r3[r0] = r4;
                    r0 = r0 + 1;
                    goto L_0x01c9;
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza.zzy(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrm$zza$zza$zza");
                }
            }

            public zza() {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r0 = this;
                <init>();
                zzCS();
                return;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.<init>():void");
            }

            public static com.google.android.gms.internal.zzrm.zza.zza[] zzCR() {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r0 = zzbbx;
                if (r0 == 0) goto L_0x0007;
            L_0x0004:
                r0 = zzbbx;
                return r0;
            L_0x0007:
                r1 = com.google.android.gms.internal.zzrv.zzbcn;
                monitor-enter(r1);
                r0 = zzbbx;
                if (r0 == 0) goto L_0x0013;
            L_0x000e:
                monitor-exit(r1);
                goto L_0x0004;
            L_0x0010:
                r0 = move-exception;
                monitor-exit(r1);
                throw r0;
            L_0x0013:
                r0 = 0;
                r0 = new com.google.android.gms.internal.zzrm.zza.zza[r0];
                zzbbx = r0;
                goto L_0x000e;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zzCR():com.google.android.gms.internal.zzrm$zza$zza[]");
            }

            public boolean equals(java.lang.Object r4) {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r3 = this;
                r2 = 0;
                if (r4 == r3) goto L_0x0022;
            L_0x0003:
                r0 = r4 instanceof com.google.android.gms.internal.zzrm.zza.zza;
                if (r0 == 0) goto L_0x0024;
            L_0x0007:
                r4 = (com.google.android.gms.internal.zzrm.zza.zza) r4;
                r0 = r3.type;
                r1 = r4.type;
                if (r0 != r1) goto L_0x0025;
            L_0x000f:
                r0 = r3.zzbby;
                if (r0 == 0) goto L_0x0026;
            L_0x0013:
                r0 = r3.zzbby;
                r1 = r4.zzbby;
                r0 = r0.equals(r1);
                if (r0 == 0) goto L_0x002b;
            L_0x001d:
                r0 = r3.zza(r4);
                return r0;
            L_0x0022:
                r0 = 1;
                return r0;
            L_0x0024:
                return r2;
            L_0x0025:
                return r2;
            L_0x0026:
                r0 = r4.zzbby;
                if (r0 == 0) goto L_0x001d;
            L_0x002a:
                return r2;
            L_0x002b:
                return r2;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.equals(java.lang.Object):boolean");
            }

            public int hashCode() {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r2 = this;
                r0 = r2.type;
                r0 = r0 + 527;
                r1 = r0 * 31;
                r0 = r2.zzbby;
                if (r0 == 0) goto L_0x0019;
            L_0x000a:
                r0 = r2.zzbby;
                r0 = r0.hashCode();
            L_0x0010:
                r0 = r0 + r1;
                r0 = r0 * 31;
                r1 = r2.zzDm();
                r0 = r0 + r1;
                return r0;
            L_0x0019:
                r0 = 0;
                goto L_0x0010;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.hashCode():int");
            }

            protected int zzB() {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r3 = this;
                r0 = super.zzB();
                r1 = 1;
                r2 = r3.type;
                r1 = com.google.android.gms.internal.zzrq.zzB(r1, r2);
                r0 = r0 + r1;
                r1 = r3.zzbby;
                if (r1 != 0) goto L_0x0011;
            L_0x0010:
                return r0;
            L_0x0011:
                r1 = 2;
                r2 = r3.zzbby;
                r1 = com.google.android.gms.internal.zzrq.zzc(r1, r2);
                r0 = r0 + r1;
                goto L_0x0010;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zzB():int");
            }

            public com.google.android.gms.internal.zzrm.zza.zza zzCS() {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r2 = this;
                r1 = 0;
                r0 = 1;
                r2.type = r0;
                r2.zzbby = r1;
                r2.zzbcd = r1;
                r0 = -1;
                r2.zzbco = r0;
                return r2;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zzCS():com.google.android.gms.internal.zzrm$zza$zza");
            }

            public void zza(com.google.android.gms.internal.zzrq r3) throws java.io.IOException {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r2 = this;
                r0 = 1;
                r1 = r2.type;
                r3.zzz(r0, r1);
                r0 = r2.zzbby;
                if (r0 != 0) goto L_0x000e;
            L_0x000a:
                super.zza(r3);
                return;
            L_0x000e:
                r0 = 2;
                r1 = r2.zzbby;
                r3.zza(r0, r1);
                goto L_0x000a;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zza(com.google.android.gms.internal.zzrq):void");
            }

            public /* synthetic */ com.google.android.gms.internal.zzrx zzb(com.google.android.gms.internal.zzrp r2) throws java.io.IOException {
                /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r1 = this;
                r0 = r1.zzx(r2);
                return r0;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zzb(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrx");
            }

            public com.google.android.gms.internal.zzrm.zza.zza zzx(com.google.android.gms.internal.zzrp r2) throws java.io.IOException {
                /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
                /*
                r1 = this;
            L_0x0000:
                r0 = r2.zzCV();
                switch(r0) {
                    case 0: goto L_0x000e;
                    case 8: goto L_0x000f;
                    case 18: goto L_0x001a;
                    default: goto L_0x0007;
                };
            L_0x0007:
                r0 = r1.zza(r2, r0);
                if (r0 != 0) goto L_0x0000;
            L_0x000d:
                return r1;
            L_0x000e:
                return r1;
            L_0x000f:
                r0 = r2.zzCY();
                switch(r0) {
                    case 1: goto L_0x0017;
                    case 2: goto L_0x0017;
                    case 3: goto L_0x0017;
                    case 4: goto L_0x0017;
                    case 5: goto L_0x0017;
                    case 6: goto L_0x0017;
                    case 7: goto L_0x0017;
                    case 8: goto L_0x0017;
                    case 9: goto L_0x0017;
                    case 10: goto L_0x0017;
                    case 11: goto L_0x0017;
                    case 12: goto L_0x0017;
                    case 13: goto L_0x0017;
                    case 14: goto L_0x0017;
                    case 15: goto L_0x0017;
                    default: goto L_0x0016;
                };
            L_0x0016:
                goto L_0x0000;
            L_0x0017:
                r1.type = r0;
                goto L_0x0000;
            L_0x001a:
                r0 = r1.zzbby;
                if (r0 == 0) goto L_0x0024;
            L_0x001e:
                r0 = r1.zzbby;
                r2.zza(r0);
                goto L_0x0000;
            L_0x0024:
                r0 = new com.google.android.gms.internal.zzrm$zza$zza$zza;
                r0.<init>();
                r1.zzbby = r0;
                goto L_0x001e;
                */
                throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza.zzx(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrm$zza$zza");
            }
        }

        public zza() {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r0 = this;
            <init>();
            zzCQ();
            return;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.<init>():void");
        }

        public static com.google.android.gms.internal.zzrm.zza[] zzCP() {
            /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
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
            r0 = zzbbv;
            if (r0 == 0) goto L_0x0007;
        L_0x0004:
            r0 = zzbbv;
            return r0;
        L_0x0007:
            r1 = com.google.android.gms.internal.zzrv.zzbcn;
            monitor-enter(r1);
            r0 = zzbbv;
            if (r0 == 0) goto L_0x0013;
        L_0x000e:
            monitor-exit(r1);
            goto L_0x0004;
        L_0x0010:
            r0 = move-exception;
            monitor-exit(r1);
            throw r0;
        L_0x0013:
            r0 = 0;
            r0 = new com.google.android.gms.internal.zzrm.zza[r0];
            zzbbv = r0;
            goto L_0x000e;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zzCP():com.google.android.gms.internal.zzrm$zza[]");
        }

        public boolean equals(java.lang.Object r4) {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r3 = this;
            r2 = 0;
            if (r4 == r3) goto L_0x002a;
        L_0x0003:
            r0 = r4 instanceof com.google.android.gms.internal.zzrm.zza;
            if (r0 == 0) goto L_0x002c;
        L_0x0007:
            r4 = (com.google.android.gms.internal.zzrm.zza) r4;
            r0 = r3.name;
            if (r0 == 0) goto L_0x002d;
        L_0x000d:
            r0 = r3.name;
            r1 = r4.name;
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0032;
        L_0x0017:
            r0 = r3.zzbbw;
            if (r0 == 0) goto L_0x0033;
        L_0x001b:
            r0 = r3.zzbbw;
            r1 = r4.zzbbw;
            r0 = r0.equals(r1);
            if (r0 == 0) goto L_0x0038;
        L_0x0025:
            r0 = r3.zza(r4);
            return r0;
        L_0x002a:
            r0 = 1;
            return r0;
        L_0x002c:
            return r2;
        L_0x002d:
            r0 = r4.name;
            if (r0 == 0) goto L_0x0017;
        L_0x0031:
            return r2;
        L_0x0032:
            return r2;
        L_0x0033:
            r0 = r4.zzbbw;
            if (r0 == 0) goto L_0x0025;
        L_0x0037:
            return r2;
        L_0x0038:
            return r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.equals(java.lang.Object):boolean");
        }

        public int hashCode() {
            /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
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
            r3 = this;
            r1 = 0;
            r0 = r3.name;
            if (r0 == 0) goto L_0x0022;
        L_0x0005:
            r0 = r3.name;
            r0 = r0.hashCode();
        L_0x000b:
            r0 = r0 + 527;
            r0 = r0 * 31;
            r2 = r3.zzbbw;
            if (r2 == 0) goto L_0x0019;
        L_0x0013:
            r1 = r3.zzbbw;
            r1 = r1.hashCode();
        L_0x0019:
            r0 = r0 + r1;
            r0 = r0 * 31;
            r1 = r3.zzDm();
            r0 = r0 + r1;
            return r0;
        L_0x0022:
            r0 = r1;
            goto L_0x000b;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.hashCode():int");
        }

        protected int zzB() {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r3 = this;
            r0 = super.zzB();
            r1 = 1;
            r2 = r3.name;
            r1 = com.google.android.gms.internal.zzrq.zzl(r1, r2);
            r0 = r0 + r1;
            r1 = r3.zzbbw;
            if (r1 != 0) goto L_0x0011;
        L_0x0010:
            return r0;
        L_0x0011:
            r1 = 2;
            r2 = r3.zzbbw;
            r1 = com.google.android.gms.internal.zzrq.zzc(r1, r2);
            r0 = r0 + r1;
            goto L_0x0010;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zzB():int");
        }

        public com.google.android.gms.internal.zzrm.zza zzCQ() {
            /* JADX: method processing error */
/*
Error: java.lang.NullPointerException
	at jadx.core.dex.visitors.ssa.SSATransform.placePhi(SSATransform.java:82)
	at jadx.core.dex.visitors.ssa.SSATransform.process(SSATransform.java:50)
	at jadx.core.dex.visitors.ssa.SSATransform.visit(SSATransform.java:42)
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
            r2 = this;
            r1 = 0;
            r0 = "";
            r2.name = r0;
            r2.zzbbw = r1;
            r2.zzbcd = r1;
            r0 = -1;
            r2.zzbco = r0;
            return r2;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zzCQ():com.google.android.gms.internal.zzrm$zza");
        }

        public void zza(com.google.android.gms.internal.zzrq r3) throws java.io.IOException {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r2 = this;
            r0 = 1;
            r1 = r2.name;
            r3.zzb(r0, r1);
            r0 = r2.zzbbw;
            if (r0 != 0) goto L_0x000e;
        L_0x000a:
            super.zza(r3);
            return;
        L_0x000e:
            r0 = 2;
            r1 = r2.zzbbw;
            r3.zza(r0, r1);
            goto L_0x000a;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zza(com.google.android.gms.internal.zzrq):void");
        }

        public /* synthetic */ com.google.android.gms.internal.zzrx zzb(com.google.android.gms.internal.zzrp r2) throws java.io.IOException {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r1 = this;
            r0 = r1.zzw(r2);
            return r0;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zzb(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrx");
        }

        public com.google.android.gms.internal.zzrm.zza zzw(com.google.android.gms.internal.zzrp r2) throws java.io.IOException {
            /* JADX: method processing error */
/*
Error: java.lang.UnsupportedOperationException
	at java.util.Collections$UnmodifiableCollection.clear(Collections.java:1074)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.markReturnBlocks(BlockProcessor.java:183)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.processBlocksTree(BlockProcessor.java:49)
	at jadx.core.dex.visitors.blocksmaker.BlockProcessor.visit(BlockProcessor.java:38)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r1 = this;
        L_0x0000:
            r0 = r2.zzCV();
            switch(r0) {
                case 0: goto L_0x000e;
                case 10: goto L_0x000f;
                case 18: goto L_0x0016;
                default: goto L_0x0007;
            };
        L_0x0007:
            r0 = r1.zza(r2, r0);
            if (r0 != 0) goto L_0x0000;
        L_0x000d:
            return r1;
        L_0x000e:
            return r1;
        L_0x000f:
            r0 = r2.readString();
            r1.name = r0;
            goto L_0x0000;
        L_0x0016:
            r0 = r1.zzbbw;
            if (r0 == 0) goto L_0x0020;
        L_0x001a:
            r0 = r1.zzbbw;
            r2.zza(r0);
            goto L_0x0000;
        L_0x0020:
            r0 = new com.google.android.gms.internal.zzrm$zza$zza;
            r0.<init>();
            r1.zzbbw = r0;
            goto L_0x001a;
            */
            throw new UnsupportedOperationException("Method not decompiled: com.google.android.gms.internal.zzrm.zza.zzw(com.google.android.gms.internal.zzrp):com.google.android.gms.internal.zzrm$zza");
        }
    }

    public zzrm() {
        zzCO();
    }

    public static zzrm zzw(byte[] bArr) throws zzrw {
        return (zzrm) zzrx.zza(new zzrm(), bArr);
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof zzrm)) {
            return false;
        }
        zzrm zzrm = (zzrm) o;
        return zzrv.equals(this.zzbbu, zzrm.zzbbu) ? zza((zzrr) zzrm) : false;
    }

    public int hashCode() {
        return ((zzrv.hashCode(this.zzbbu) + 527) * 31) + zzDm();
    }

    protected int zzB() {
        int zzB = super.zzB();
        if (this.zzbbu != null && this.zzbbu.length > 0) {
            for (zzrx zzrx : this.zzbbu) {
                if (zzrx != null) {
                    zzB += zzrq.zzc(1, zzrx);
                }
            }
        }
        return zzB;
    }

    public zzrm zzCO() {
        this.zzbbu = zza.zzCP();
        this.zzbcd = null;
        this.zzbco = -1;
        return this;
    }

    public void zza(zzrq zzrq) throws IOException {
        if (this.zzbbu != null && this.zzbbu.length > 0) {
            for (zzrx zzrx : this.zzbbu) {
                if (zzrx != null) {
                    zzrq.zza(1, zzrx);
                }
            }
        }
        super.zza(zzrq);
    }

    public /* synthetic */ zzrx zzb(zzrp zzrp) throws IOException {
        return zzv(zzrp);
    }

    public zzrm zzv(zzrp zzrp) throws IOException {
        while (true) {
            int zzCV = zzrp.zzCV();
            switch (zzCV) {
                case 0:
                    return this;
                case 10:
                    int zzb = zzsa.zzb(zzrp, 10);
                    zzCV = this.zzbbu != null ? this.zzbbu.length : 0;
                    Object obj = new zza[(zzb + zzCV)];
                    if (zzCV != 0) {
                        System.arraycopy(this.zzbbu, 0, obj, 0, zzCV);
                    }
                    while (zzCV < obj.length - 1) {
                        obj[zzCV] = new zza();
                        zzrp.zza(obj[zzCV]);
                        zzrp.zzCV();
                        zzCV++;
                    }
                    obj[zzCV] = new zza();
                    zzrp.zza(obj[zzCV]);
                    this.zzbbu = obj;
                    break;
                default:
                    if (zza(zzrp, zzCV)) {
                        break;
                    }
                    return this;
            }
        }
    }
}
