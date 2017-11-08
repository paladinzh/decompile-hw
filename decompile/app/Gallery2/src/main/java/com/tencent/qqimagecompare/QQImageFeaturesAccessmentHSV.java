package com.tencent.qqimagecompare;

import java.util.ArrayList;

/* compiled from: Unknown */
public class QQImageFeaturesAccessmentHSV extends QQImageNativeObject {

    /* compiled from: Unknown */
    /* renamed from: com.tencent.qqimagecompare.QQImageFeaturesAccessmentHSV$1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] na = new int[eDimensionType.values().length];

        static {
            try {
                na[eDimensionType.Sharpness.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                na[eDimensionType.Lightness.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    /* compiled from: Unknown */
    public enum eDimensionType {
        Sharpness,
        Lightness
    }

    private static native void AddDimensionC(long j, int i, int i2);

    private static native int GetFeaturesRankC(long j, long[] jArr, int[] iArr);

    public void addDimension(eDimensionType edimensiontype, int i) {
        int i2 = 0;
        switch (AnonymousClass1.na[edimensiontype.ordinal()]) {
            case 1:
                i2 = 1;
                break;
            case 2:
                i2 = 2;
                break;
        }
        AddDimensionC(this.mThisC, i2, i);
    }

    protected native long createNativeObject();

    protected native void destroyNativeObject(long j);

    public int[] getFeaturesRanks(ArrayList<QQImageFeatureHSV> arrayList) {
        int size = arrayList.size();
        int[] iArr = new int[size];
        long[] jArr = new long[size];
        for (int i = 0; i < size; i++) {
            jArr[i] = ((QQImageFeatureHSV) arrayList.get(i)).mThisC;
        }
        GetFeaturesRankC(this.mThisC, jArr, iArr);
        return iArr;
    }
}
