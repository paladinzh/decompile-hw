package com.fyusion.sdk.core.util;

import com.huawei.watermark.manager.parse.WMElement;

/* compiled from: Unknown */
public class a {
    static float a(float[] fArr) {
        b.a(fArr.length, 9);
        float f = fArr[0];
        float f2 = fArr[3];
        float f3 = fArr[6];
        float f4 = fArr[1];
        float f5 = fArr[4];
        float f6 = fArr[7];
        float f7 = fArr[2];
        float f8 = fArr[5];
        float f9 = fArr[8];
        return ((((((f * f5) * f9) + ((f2 * f6) * f7)) + ((f3 * f4) * f8)) - ((f * f6) * f8)) - ((f2 * f4) * f9)) - ((f3 * f5) * f7);
    }

    public static void a(float[] fArr, float[] fArr2) {
        b.a(fArr.length, 9);
        b.a(fArr2.length, 9);
        float f = fArr2[0];
        float f2 = fArr2[3];
        float f3 = fArr2[6];
        float f4 = fArr2[1];
        float f5 = fArr2[4];
        float f6 = fArr2[7];
        float f7 = fArr2[2];
        float f8 = fArr2[5];
        float f9 = fArr2[8];
        float a = WMElement.CAMERASIZEVALUE1B1 / a(fArr2);
        float f10 = ((f3 * f8) - (f2 * f9)) * a;
        float f11 = ((f2 * f6) - (f3 * f5)) * a;
        float f12 = ((f6 * f7) - (f4 * f9)) * a;
        f9 = ((f9 * f) - (f3 * f7)) * a;
        f3 = ((f3 * f4) - (f6 * f)) * a;
        f6 = ((f4 * f8) - (f5 * f7)) * a;
        f7 = ((f7 * f2) - (f8 * f)) * a;
        f = ((f * f5) - (f2 * f4)) * a;
        fArr[0] = ((f5 * f9) - (f6 * f8)) * a;
        fArr[3] = f10;
        fArr[6] = f11;
        fArr[1] = f12;
        fArr[4] = f9;
        fArr[7] = f3;
        fArr[2] = f6;
        fArr[5] = f7;
        fArr[8] = f;
    }
}
