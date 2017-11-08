package com.fyusion.sdk.viewer.ext.localfyuse;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;
import android.util.Pair;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.m;
import com.fyusion.sdk.processor.FyuseProcessor;
import com.fyusion.sdk.viewer.e;
import com.fyusion.sdk.viewer.internal.b.c.a;
import com.fyusion.sdk.viewer.view.b;
import java.io.File;

/* compiled from: Unknown */
public class c implements e {
    FyuseProcessor a;
    private ProcessItem b;

    public c(FyuseProcessor fyuseProcessor) {
        this.a = fyuseProcessor;
    }

    public void a() {
        DLog.d("LegacyProcessorWrapper", "canceling item");
        if (this.b != null) {
            this.b.cancel();
        }
    }

    public void a(a aVar, File file, e.a aVar2) {
        final l lVar = new l(g.a(), file);
        final e.a aVar3 = aVar2;
        final File file2 = file;
        final a aVar4 = aVar;
        ProcessorListener anonymousClass1 = new k(this) {
            final /* synthetic */ c e;

            public void a(ProcessItem processItem, int i, int i2, Bitmap bitmap, Matrix matrix) {
                aVar3.a(i, i2, new Pair(bitmap, matrix));
            }

            public void onError(ProcessItem processItem, ProcessError processError) {
                aVar3.a(processError.getMessage());
            }

            public void onImageDataReady(ProcessItem processItem) {
            }

            public void onMetadataReady(ProcessItem processItem, int i) {
            }

            public void onProcessComplete(ProcessItem processItem) {
                com.fyusion.sdk.common.ext.e eVar = new com.fyusion.sdk.common.ext.e();
                m bVar = new b();
                if (lVar.a(eVar, bVar)) {
                    h a = d.a(file2.getName(), eVar);
                    aVar4.a(bVar);
                    aVar4.a(a);
                    DLog.d("numProcessedFrames", "" + eVar.getNumberOfProcessedFrames() + "data last frame #: " + aVar4.l());
                }
                aVar3.a();
            }

            public void onProgress(ProcessItem processItem, int i, int i2, Bitmap bitmap) {
            }

            public void onSliceFound(ProcessItem processItem, int i) {
            }

            public void onSliceReady(ProcessItem processItem, int i) {
            }

            public void onTweensReady(ProcessItem processItem) {
            }
        };
        try {
            if (lVar.g().getLevel() < ProcessState.READY_FOR_VIEW.getLevel()) {
                this.b = this.a.prepareForViewing(file, anonymousClass1);
            } else {
                aVar2.a();
            }
        } catch (Throwable e) {
            Log.e("ProcessorWrapper", "Failed loading fyuse file.", e);
            aVar2.a(e.getMessage());
        }
    }
}
