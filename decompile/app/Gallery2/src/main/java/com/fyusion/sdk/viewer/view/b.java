package com.fyusion.sdk.viewer.view;

import com.fyusion.sdk.common.m;
import fyusion.vislib.FrameBlender;
import fyusion.vislib.TransformationParameters;

/* compiled from: Unknown */
public class b extends FrameBlender implements m {
    public boolean a(float f, int[] iArr, float[] fArr, Object obj, int[] iArr2, float[] fArr2, Object obj2) {
        return super.queryBlendingInfoForFrameId(f, iArr, fArr, (TransformationParameters) obj, iArr2, fArr2, (TransformationParameters) obj2);
    }
}
