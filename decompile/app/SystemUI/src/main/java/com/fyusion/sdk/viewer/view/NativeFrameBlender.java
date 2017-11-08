package com.fyusion.sdk.viewer.view;

import com.fyusion.sdk.common.c.b;
import com.fyusion.sdk.common.o;
import com.fyusion.sdk.common.p;
import com.fyusion.sdk.viewer.view.e.a;
import fyusion.vislib.CVTransform;
import fyusion.vislib.TransformationParameters;
import fyusion.vislib.VislibJavaHelper;
import java.io.File;

/* compiled from: Unknown */
public class NativeFrameBlender implements b {
    private o a;

    static {
        System.loadLibrary("vislib_jni");
    }

    private static b a(TransformationParameters transformationParameters) {
        b bVar = new b();
        CVTransform transformForParameters = VislibJavaHelper.getTransformForParameters(transformationParameters);
        bVar.a = (double) transformForParameters.getTransform().get(0);
        bVar.b = (double) transformForParameters.getTransform().get(3);
        bVar.e = (double) transformForParameters.getTransform().get(2);
        bVar.c = (double) transformForParameters.getTransform().get(1);
        bVar.d = (double) transformForParameters.getTransform().get(4);
        bVar.f = (double) transformForParameters.getTransform().get(5);
        return bVar;
    }

    public void init(o oVar, p pVar) {
        if (pVar.getCameraWidth() > 0 && pVar.getCameraHeight() > 0 && pVar.getWidth() > 0 && pVar.getHeight() > 0) {
            this.a = oVar;
            this.a.setSizes(pVar.getCameraWidth(), pVar.getCameraHeight(), pVar.getWidth(), pVar.getHeight(), -1.0f);
        }
    }

    public void init(File file, p pVar) {
        if (file.exists()) {
            this.a = new b();
            if (this.a.setTweeningFileAndSizes(file.getAbsolutePath(), pVar.getCameraWidth(), pVar.getCameraHeight(), pVar.getWidth(), pVar.getHeight(), -1.0f)) {
                this.a.setIndexingOffset(pVar.getStabilizationDataFrameOffset());
                this.a.setLoopClosed(pVar.isLoopClosed(), pVar.getStartFrame(), pVar.getEndFrame());
            }
        }
    }

    public a queryBlendingInfoForFrameId(float f) {
        if (this.a != null) {
            int[] iArr = new int[1];
            float[] fArr = new float[1];
            TransformationParameters transformationParameters = new TransformationParameters();
            int[] iArr2 = new int[1];
            float[] fArr2 = new float[1];
            TransformationParameters transformationParameters2 = new TransformationParameters();
            if (this.a.a(f, iArr, fArr, transformationParameters, iArr2, fArr2, transformationParameters2)) {
                a aVar = new a();
                aVar.a = iArr[0];
                aVar.b = fArr[0];
                aVar.c = a(transformationParameters);
                aVar.d = iArr2[0];
                aVar.e = fArr2[0];
                aVar.f = a(transformationParameters2);
                return aVar;
            }
        }
        return null;
    }
}
