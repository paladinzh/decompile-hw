package tmsdk.common.utils;

import java.io.UnsupportedEncodingException;

/* compiled from: Unknown */
public class b {
    static final /* synthetic */ boolean fJ;

    /* compiled from: Unknown */
    static abstract class a {
        public byte[] KJ;
        public int KK;

        a() {
        }
    }

    /* compiled from: Unknown */
    static class b extends a {
        private static final int[] KL = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, -1, 63, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, -1, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private static final int[] KM = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, 62, -1, -1, 52, 53, 54, 55, 56, 57, 58, 59, 60, 61, -1, -1, -1, -2, -1, -1, -1, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, -1, -1, -1, -1, 63, -1, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
        private final int[] KN;
        private int state;
        private int value;

        public b(int i, byte[] bArr) {
            this.KJ = bArr;
            this.KN = (i & 8) != 0 ? KM : KL;
            this.state = 0;
            this.value = 0;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(byte[] bArr, int i, int i2, boolean z) {
            if (this.state == 6) {
                return false;
            }
            int i3 = i2 + i;
            int i4 = this.state;
            int i5 = this.value;
            int i6 = 0;
            byte[] bArr2 = this.KJ;
            int[] iArr = this.KN;
            int i7 = i;
            while (i7 < i3) {
                if (i4 == 0) {
                    while (i7 + 4 <= i3) {
                        i5 = (((iArr[bArr[i7] & 255] << 18) | (iArr[bArr[i7 + 1] & 255] << 12)) | (iArr[bArr[i7 + 2] & 255] << 6)) | iArr[bArr[i7 + 3] & 255];
                        if (i5 >= 0) {
                            bArr2[i6 + 2] = (byte) ((byte) i5);
                            bArr2[i6 + 1] = (byte) ((byte) (i5 >> 8));
                            bArr2[i6] = (byte) ((byte) (i5 >> 16));
                            i6 += 3;
                            i7 += 4;
                        } else if (i7 < i3) {
                            i7 = i5;
                            if (z) {
                                switch (i4) {
                                    case 1:
                                        this.state = 6;
                                        return false;
                                    case 2:
                                        i5 = i6 + 1;
                                        bArr2[i6] = (byte) ((byte) (i7 >> 4));
                                        i6 = i5;
                                        break;
                                    case 3:
                                        i5 = i6 + 1;
                                        bArr2[i6] = (byte) ((byte) (i7 >> 10));
                                        i6 = i5 + 1;
                                        bArr2[i5] = (byte) ((byte) (i7 >> 2));
                                        break;
                                    case 4:
                                        this.state = 6;
                                        return false;
                                }
                                this.state = i4;
                                this.KK = i6;
                                return true;
                            }
                            this.state = i4;
                            this.value = i7;
                            this.KK = i6;
                            return true;
                        }
                    }
                    if (i7 < i3) {
                        i7 = i5;
                        if (z) {
                            this.state = i4;
                            this.value = i7;
                            this.KK = i6;
                            return true;
                        }
                        switch (i4) {
                            case 1:
                                this.state = 6;
                                return false;
                            case 2:
                                i5 = i6 + 1;
                                bArr2[i6] = (byte) ((byte) (i7 >> 4));
                                i6 = i5;
                                break;
                            case 3:
                                i5 = i6 + 1;
                                bArr2[i6] = (byte) ((byte) (i7 >> 10));
                                i6 = i5 + 1;
                                bArr2[i5] = (byte) ((byte) (i7 >> 2));
                                break;
                            case 4:
                                this.state = 6;
                                return false;
                        }
                        this.state = i4;
                        this.KK = i6;
                        return true;
                    }
                }
                i = i7 + 1;
                i7 = iArr[bArr[i7] & 255];
                int i8;
                switch (i4) {
                    case 0:
                        if (i7 >= 0) {
                            i8 = i7;
                            i7 = i4 + 1;
                            i5 = i8;
                            break;
                        } else if (i7 != -1) {
                            this.state = 6;
                            return false;
                        }
                    case 1:
                        if (i7 < 0) {
                            if (i7 != -1) {
                                this.state = 6;
                                return false;
                            }
                            i7 = i4;
                            break;
                        }
                        break;
                    case 2:
                        if (i7 < 0) {
                            if (i7 == -2) {
                                i7 = i6 + 1;
                                bArr2[i6] = (byte) ((byte) (i5 >> 4));
                                i8 = i7;
                                i7 = 4;
                                i6 = i8;
                                break;
                            }
                            if (i7 != -1) {
                                this.state = 6;
                                return false;
                            }
                            i7 = i4;
                            break;
                        }
                        break;
                    case 3:
                        if (i7 < 0) {
                            if (i7 == -2) {
                                bArr2[i6 + 1] = (byte) ((byte) (i5 >> 2));
                                bArr2[i6] = (byte) ((byte) (i5 >> 10));
                                i6 += 2;
                                i7 = 5;
                                break;
                            } else if (i7 != -1) {
                                this.state = 6;
                                return false;
                            }
                        }
                        i5 = (i5 << 6) | i7;
                        bArr2[i6 + 2] = (byte) ((byte) i5);
                        bArr2[i6 + 1] = (byte) ((byte) (i5 >> 8));
                        bArr2[i6] = (byte) ((byte) (i5 >> 16));
                        i6 += 3;
                        i7 = 0;
                        break;
                    case 4:
                        if (i7 == -2) {
                            i7 = i4 + 1;
                            break;
                        } else if (i7 != -1) {
                            this.state = 6;
                            return false;
                        }
                    case 5:
                        if (i7 != -1) {
                            this.state = 6;
                            return false;
                        }
                        i7 = i4;
                        break;
                }
            }
            i7 = i5;
            if (z) {
                switch (i4) {
                    case 1:
                        this.state = 6;
                        return false;
                    case 2:
                        i5 = i6 + 1;
                        bArr2[i6] = (byte) ((byte) (i7 >> 4));
                        i6 = i5;
                        break;
                    case 3:
                        i5 = i6 + 1;
                        bArr2[i6] = (byte) ((byte) (i7 >> 10));
                        i6 = i5 + 1;
                        bArr2[i5] = (byte) ((byte) (i7 >> 2));
                        break;
                    case 4:
                        this.state = 6;
                        return false;
                }
                this.state = i4;
                this.KK = i6;
                return true;
            }
            this.state = i4;
            this.value = i7;
            this.KK = i6;
            return true;
        }
    }

    /* compiled from: Unknown */
    static class c extends a {
        private static final byte[] KO = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 43, (byte) 47};
        private static final byte[] KP = new byte[]{(byte) 65, (byte) 66, (byte) 67, (byte) 68, (byte) 69, (byte) 70, (byte) 71, (byte) 72, (byte) 73, (byte) 74, (byte) 75, (byte) 76, (byte) 77, (byte) 78, (byte) 79, (byte) 80, (byte) 81, (byte) 82, (byte) 83, (byte) 84, (byte) 85, (byte) 86, (byte) 87, (byte) 88, (byte) 89, (byte) 90, (byte) 97, (byte) 98, (byte) 99, (byte) 100, (byte) 101, (byte) 102, (byte) 103, (byte) 104, (byte) 105, (byte) 106, (byte) 107, (byte) 108, (byte) 109, (byte) 110, (byte) 111, (byte) 112, (byte) 113, (byte) 114, (byte) 115, (byte) 116, (byte) 117, (byte) 118, (byte) 119, (byte) 120, (byte) 121, (byte) 122, (byte) 48, (byte) 49, (byte) 50, (byte) 51, (byte) 52, (byte) 53, (byte) 54, (byte) 55, (byte) 56, (byte) 57, (byte) 45, (byte) 95};
        static final /* synthetic */ boolean fJ = (!b.class.desiredAssertionStatus());
        private final byte[] KQ;
        int KR;
        public final boolean KS;
        public final boolean KT;
        public final boolean KU;
        private final byte[] KV;
        private int count;

        public c(int i, byte[] bArr) {
            boolean z = true;
            this.KJ = bArr;
            this.KS = (i & 1) == 0;
            this.KT = (i & 2) == 0;
            if ((i & 4) == 0) {
                z = false;
            }
            this.KU = z;
            this.KV = (i & 8) != 0 ? KP : KO;
            this.KQ = new byte[2];
            this.KR = 0;
            this.count = !this.KT ? -1 : 19;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean a(byte[] r12, int r13, int r14, boolean r15) {
            /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxOverflowException: Regions stack size limit reached
	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:37)
	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:61)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:14)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
            /*
            r11 = this;
            r6 = r11.KV;
            r7 = r11.KJ;
            r0 = 0;
            r1 = r11.count;
            r8 = r14 + r13;
            r2 = -1;
            r3 = r11.KR;
            switch(r3) {
                case 0: goto L_0x000f;
                case 1: goto L_0x002a;
                case 2: goto L_0x004b;
                default: goto L_0x000f;
            };
        L_0x000f:
            r3 = r2;
            r2 = r13;
        L_0x0011:
            r4 = -1;
            if (r3 != r4) goto L_0x0070;
        L_0x0014:
            r5 = r1;
            r4 = r0;
        L_0x0016:
            r0 = r2 + 3;
            if (r0 <= r8) goto L_0x00b1;
        L_0x001a:
            if (r15 != 0) goto L_0x010a;
        L_0x001c:
            r0 = r8 + -1;
            if (r2 == r0) goto L_0x022b;
        L_0x0020:
            r0 = r8 + -2;
            if (r2 == r0) goto L_0x023a;
        L_0x0024:
            r11.KK = r4;
            r11.count = r5;
            r0 = 1;
            return r0;
        L_0x002a:
            r3 = r13 + 2;
            if (r3 > r8) goto L_0x000f;
        L_0x002e:
            r2 = r11.KQ;
            r3 = 0;
            r2 = r2[r3];
            r2 = r2 & 255;
            r2 = r2 << 16;
            r3 = r13 + 1;
            r4 = r12[r13];
            r4 = r4 & 255;
            r4 = r4 << 8;
            r2 = r2 | r4;
            r13 = r3 + 1;
            r3 = r12[r3];
            r3 = r3 & 255;
            r2 = r2 | r3;
            r3 = 0;
            r11.KR = r3;
            goto L_0x000f;
        L_0x004b:
            r3 = r13 + 1;
            if (r3 > r8) goto L_0x000f;
        L_0x004f:
            r2 = r11.KQ;
            r3 = 0;
            r2 = r2[r3];
            r2 = r2 & 255;
            r2 = r2 << 16;
            r3 = r11.KQ;
            r4 = 1;
            r3 = r3[r4];
            r3 = r3 & 255;
            r3 = r3 << 8;
            r2 = r2 | r3;
            r3 = r13 + 1;
            r4 = r12[r13];
            r4 = r4 & 255;
            r2 = r2 | r4;
            r4 = 0;
            r11.KR = r4;
            r10 = r2;
            r2 = r3;
            r3 = r10;
            goto L_0x0011;
        L_0x0070:
            r0 = 0;
            r4 = 1;
            r5 = r3 >> 18;
            r5 = r5 & 63;
            r5 = r6[r5];
            r5 = (byte) r5;
            r7[r0] = r5;
            r0 = 2;
            r5 = r3 >> 12;
            r5 = r5 & 63;
            r5 = r6[r5];
            r5 = (byte) r5;
            r7[r4] = r5;
            r4 = 3;
            r5 = r3 >> 6;
            r5 = r5 & 63;
            r5 = r6[r5];
            r5 = (byte) r5;
            r7[r0] = r5;
            r0 = 4;
            r3 = r3 & 63;
            r3 = r6[r3];
            r3 = (byte) r3;
            r7[r4] = r3;
            r1 = r1 + -1;
            if (r1 != 0) goto L_0x0014;
        L_0x009b:
            r1 = r11.KU;
            if (r1 != 0) goto L_0x00aa;
        L_0x009f:
            r4 = r0 + 1;
            r1 = 10;
            r7[r0] = r1;
        L_0x00a5:
            r0 = 19;
            r5 = r0;
            goto L_0x0016;
        L_0x00aa:
            r1 = 5;
            r3 = 13;
            r7[r0] = r3;
            r0 = r1;
            goto L_0x009f;
        L_0x00b1:
            r0 = r12[r2];
            r0 = r0 & 255;
            r0 = r0 << 16;
            r1 = r2 + 1;
            r1 = r12[r1];
            r1 = r1 & 255;
            r1 = r1 << 8;
            r0 = r0 | r1;
            r1 = r2 + 2;
            r1 = r12[r1];
            r1 = r1 & 255;
            r0 = r0 | r1;
            r1 = r0 >> 18;
            r1 = r1 & 63;
            r1 = r6[r1];
            r1 = (byte) r1;
            r7[r4] = r1;
            r1 = r4 + 1;
            r3 = r0 >> 12;
            r3 = r3 & 63;
            r3 = r6[r3];
            r3 = (byte) r3;
            r7[r1] = r3;
            r1 = r4 + 2;
            r3 = r0 >> 6;
            r3 = r3 & 63;
            r3 = r6[r3];
            r3 = (byte) r3;
            r7[r1] = r3;
            r1 = r4 + 3;
            r0 = r0 & 63;
            r0 = r6[r0];
            r0 = (byte) r0;
            r7[r1] = r0;
            r2 = r2 + 3;
            r0 = r4 + 4;
            r1 = r5 + -1;
            if (r1 != 0) goto L_0x0014;
        L_0x00f7:
            r1 = r11.KU;
            if (r1 != 0) goto L_0x0102;
        L_0x00fb:
            r4 = r0 + 1;
            r1 = 10;
            r7[r0] = r1;
            goto L_0x00a5;
        L_0x0102:
            r1 = r0 + 1;
            r3 = 13;
            r7[r0] = r3;
            r0 = r1;
            goto L_0x00fb;
        L_0x010a:
            r0 = r11.KR;
            r0 = r2 - r0;
            r1 = r8 + -1;
            if (r0 == r1) goto L_0x012e;
        L_0x0112:
            r0 = r11.KR;
            r0 = r2 - r0;
            r1 = r8 + -2;
            if (r0 == r1) goto L_0x018b;
        L_0x011a:
            r0 = r11.KT;
            if (r0 != 0) goto L_0x0207;
        L_0x011e:
            r0 = fJ;
            if (r0 == 0) goto L_0x0221;
        L_0x0122:
            r0 = fJ;
            if (r0 != 0) goto L_0x0024;
        L_0x0126:
            if (r2 == r8) goto L_0x0024;
        L_0x0128:
            r0 = new java.lang.AssertionError;
            r0.<init>();
            throw r0;
        L_0x012e:
            r0 = 0;
            r1 = r11.KR;
            if (r1 > 0) goto L_0x0160;
        L_0x0133:
            r1 = r2 + 1;
            r2 = r12[r2];
        L_0x0137:
            r2 = r2 & 255;
            r2 = r2 << 4;
            r3 = r11.KR;
            r0 = r3 - r0;
            r11.KR = r0;
            r3 = r4 + 1;
            r0 = r2 >> 6;
            r0 = r0 & 63;
            r0 = r6[r0];
            r0 = (byte) r0;
            r7[r4] = r0;
            r0 = r3 + 1;
            r2 = r2 & 63;
            r2 = r6[r2];
            r2 = (byte) r2;
            r7[r3] = r2;
            r2 = r11.KS;
            if (r2 != 0) goto L_0x016a;
        L_0x0159:
            r2 = r11.KT;
            if (r2 != 0) goto L_0x0177;
        L_0x015d:
            r2 = r1;
            r4 = r0;
            goto L_0x011e;
        L_0x0160:
            r1 = r11.KQ;
            r3 = 0;
            r0 = 1;
            r1 = r1[r3];
            r10 = r2;
            r2 = r1;
            r1 = r10;
            goto L_0x0137;
        L_0x016a:
            r2 = r0 + 1;
            r3 = 61;
            r7[r0] = r3;
            r0 = r2 + 1;
            r3 = 61;
            r7[r2] = r3;
            goto L_0x0159;
        L_0x0177:
            r2 = r11.KU;
            if (r2 != 0) goto L_0x0183;
        L_0x017b:
            r2 = r0 + 1;
            r3 = 10;
            r7[r0] = r3;
            r0 = r2;
            goto L_0x015d;
        L_0x0183:
            r2 = r0 + 1;
            r3 = 13;
            r7[r0] = r3;
            r0 = r2;
            goto L_0x017b;
        L_0x018b:
            r0 = 0;
            r1 = r11.KR;
            r3 = 1;
            if (r1 > r3) goto L_0x01da;
        L_0x0191:
            r1 = r2 + 1;
            r2 = r12[r2];
            r10 = r1;
            r1 = r2;
            r2 = r10;
        L_0x0198:
            r1 = r1 & 255;
            r9 = r1 << 10;
            r1 = r11.KR;
            if (r1 > 0) goto L_0x01e1;
        L_0x01a0:
            r3 = r2 + 1;
            r1 = r12[r2];
            r2 = r3;
        L_0x01a5:
            r1 = r1 & 255;
            r1 = r1 << 2;
            r1 = r1 | r9;
            r3 = r11.KR;
            r0 = r3 - r0;
            r11.KR = r0;
            r0 = r4 + 1;
            r3 = r1 >> 12;
            r3 = r3 & 63;
            r3 = r6[r3];
            r3 = (byte) r3;
            r7[r4] = r3;
            r3 = r0 + 1;
            r4 = r1 >> 6;
            r4 = r4 & 63;
            r4 = r6[r4];
            r4 = (byte) r4;
            r7[r0] = r4;
            r0 = r3 + 1;
            r1 = r1 & 63;
            r1 = r6[r1];
            r1 = (byte) r1;
            r7[r3] = r1;
            r1 = r11.KS;
            if (r1 != 0) goto L_0x01eb;
        L_0x01d3:
            r1 = r11.KT;
            if (r1 != 0) goto L_0x01f3;
        L_0x01d7:
            r4 = r0;
            goto L_0x011e;
        L_0x01da:
            r1 = r11.KQ;
            r3 = 0;
            r0 = 1;
            r1 = r1[r3];
            goto L_0x0198;
        L_0x01e1:
            r3 = r11.KQ;
            r1 = r0 + 1;
            r0 = r3[r0];
            r10 = r1;
            r1 = r0;
            r0 = r10;
            goto L_0x01a5;
        L_0x01eb:
            r1 = r0 + 1;
            r3 = 61;
            r7[r0] = r3;
            r0 = r1;
            goto L_0x01d3;
        L_0x01f3:
            r1 = r11.KU;
            if (r1 != 0) goto L_0x01ff;
        L_0x01f7:
            r1 = r0 + 1;
            r3 = 10;
            r7[r0] = r3;
            r0 = r1;
            goto L_0x01d7;
        L_0x01ff:
            r1 = r0 + 1;
            r3 = 13;
            r7[r0] = r3;
            r0 = r1;
            goto L_0x01f7;
        L_0x0207:
            if (r4 <= 0) goto L_0x011e;
        L_0x0209:
            r0 = 19;
            if (r5 == r0) goto L_0x011e;
        L_0x020d:
            r0 = r11.KU;
            if (r0 != 0) goto L_0x021a;
        L_0x0211:
            r0 = r4;
        L_0x0212:
            r4 = r0 + 1;
            r1 = 10;
            r7[r0] = r1;
            goto L_0x011e;
        L_0x021a:
            r0 = r4 + 1;
            r1 = 13;
            r7[r4] = r1;
            goto L_0x0212;
        L_0x0221:
            r0 = r11.KR;
            if (r0 == 0) goto L_0x0122;
        L_0x0225:
            r0 = new java.lang.AssertionError;
            r0.<init>();
            throw r0;
        L_0x022b:
            r0 = r11.KQ;
            r1 = r11.KR;
            r3 = r1 + 1;
            r11.KR = r3;
            r2 = r12[r2];
        L_0x0235:
            r2 = (byte) r2;
            r0[r1] = r2;
            goto L_0x0024;
        L_0x023a:
            r0 = r11.KQ;
            r1 = r11.KR;
            r3 = r1 + 1;
            r11.KR = r3;
            r3 = r12[r2];
            r3 = (byte) r3;
            r0[r1] = r3;
            r0 = r11.KQ;
            r1 = r11.KR;
            r3 = r1 + 1;
            r11.KR = r3;
            r2 = r2 + 1;
            r2 = r12[r2];
            goto L_0x0235;
            */
            throw new UnsupportedOperationException("Method not decompiled: tmsdk.common.utils.b.c.a(byte[], int, int, boolean):boolean");
        }
    }

    static {
        boolean z = false;
        if (!b.class.desiredAssertionStatus()) {
            z = true;
        }
        fJ = z;
    }

    private b() {
    }

    public static byte[] decode(String str, int i) {
        return decode(str.getBytes(), i);
    }

    public static byte[] decode(byte[] bArr, int i) {
        return decode(bArr, 0, bArr.length, i);
    }

    public static byte[] decode(byte[] bArr, int i, int i2, int i3) {
        b bVar = new b(i3, new byte[((i2 * 3) / 4)]);
        if (!bVar.a(bArr, i, i2, true)) {
            throw new IllegalArgumentException("bad base-64");
        } else if (bVar.KK == bVar.KJ.length) {
            return bVar.KJ;
        } else {
            Object obj = new byte[bVar.KK];
            System.arraycopy(bVar.KJ, 0, obj, 0, bVar.KK);
            return obj;
        }
    }

    public static byte[] encode(byte[] bArr, int i) {
        return encode(bArr, 0, bArr.length, i);
    }

    public static byte[] encode(byte[] bArr, int i, int i2, int i3) {
        c cVar = new c(i3, null);
        int i4 = (i2 / 3) * 4;
        if (!cVar.KS) {
            switch (i2 % 3) {
                case 1:
                    i4 += 2;
                    break;
                case 2:
                    i4 += 3;
                    break;
            }
        } else if (i2 % 3 > 0) {
            i4 += 4;
        }
        if (cVar.KT && i2 > 0) {
            i4 += (!cVar.KU ? 1 : 2) * (((i2 - 1) / 57) + 1);
        }
        cVar.KJ = new byte[i4];
        cVar.a(bArr, i, i2, true);
        if (fJ || cVar.KK == i4) {
            return cVar.KJ;
        }
        throw new AssertionError();
    }

    public static String encodeToString(byte[] bArr, int i) {
        try {
            return new String(encode(bArr, i), "US-ASCII");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError(e);
        }
    }
}
