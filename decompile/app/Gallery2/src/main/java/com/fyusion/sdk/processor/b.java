package com.fyusion.sdk.processor;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.processor.a.a;
import com.fyusion.sdk.processor.h.c;
import java.io.IOException;

/* compiled from: Unknown */
public class b extends a {
    private ProcessItem b;
    private ProcessorListener c;
    private l d;

    public b(ProcessItem processItem, ProcessorListener processorListener) {
        this.b = processItem;
        this.c = processorListener;
        this.d = new l(g.a(), processItem.getFile());
    }

    private void a(final ProcessItem processItem) {
        new e(this.d, processItem, new c(this) {
            final /* synthetic */ b b;

            public void a(int i) {
                if (this.b.c instanceof k) {
                    ((k) this.b.c).onSliceFound(processItem, i);
                }
            }

            public void a(int i, int i2) {
                if (this.b.c instanceof k) {
                    ((k) this.b.c).a(processItem, i, i2, null, null);
                }
            }

            public void a(ProcessError processError) {
                this.b.c.onError(processItem, processError);
            }

            public void a(boolean z, int i) {
                if (z) {
                    processItem.setState(ProcessState.READY_FOR_UPLOAD);
                    this.b.c.onProcessComplete(processItem);
                    return;
                }
                this.b.c.onError(processItem, ProcessError.CORRUPT_DATA);
            }

            public void b(int i) {
                if (this.b.c instanceof k) {
                    ((k) this.b.c).onSliceReady(processItem, i);
                }
            }

            public void b(int i, int i2) {
                if (this.b.c instanceof k) {
                    ((k) this.b.c).onMetadataReady(processItem, i);
                }
            }
        }).a();
    }

    private void b(ProcessItem processItem) {
        new d(this.d).a(new k(this) {
            final /* synthetic */ b a;

            {
                this.a = r1;
            }

            public void a(ProcessItem processItem, int i, int i2, Bitmap bitmap, Matrix matrix) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).a(processItem, i, i2, bitmap, matrix);
                }
            }

            public void onError(ProcessItem processItem, ProcessError processError) {
                this.a.c.onError(processItem, processError);
            }

            public void onImageDataReady(ProcessItem processItem) {
                this.a.c.onImageDataReady(processItem);
            }

            public void onMetadataReady(ProcessItem processItem, int i) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).onMetadataReady(processItem, i);
                }
            }

            public void onProcessComplete(ProcessItem processItem) {
            }

            public void onProgress(ProcessItem processItem, int i, int i2, Bitmap bitmap) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).a(processItem, i, i2, bitmap, null);
                }
            }

            public void onSliceFound(ProcessItem processItem, int i) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).onSliceFound(processItem, i);
                }
            }

            public void onSliceReady(ProcessItem processItem, int i) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).onSliceReady(processItem, i);
                }
            }

            public void onTweensReady(ProcessItem processItem) {
                if (this.a.c instanceof k) {
                    ((k) this.a.c).onTweensReady(processItem);
                }
            }
        }, processItem);
        DLog.d("FyuseProcessorJob", "Target: VIEW | Start processing to VIEW");
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void a() {
        if (this.b.isCancelled()) {
            this.c.onError(this.b, ProcessError.USER_CANCEL_REQUEST);
            return;
        }
        a.a().b();
        try {
            ProcessState g = this.d.g();
            this.b.setState(g);
            DLog.d("FyuseProcessorJob", "Item state: " + this.b.getState().getLevel() + " | Target: " + this.b.getTarget().getLevel());
            if (g.getLevel() >= this.b.getTarget().getLevel()) {
                this.c.onProcessComplete(this.b);
            } else if (g.getLevel() < ProcessState.READY_FOR_VIEW.getLevel()) {
                b(this.b);
            } else if (this.b.getTarget() == ProcessState.READY_FOR_UPLOAD && g == ProcessState.READY_FOR_VIEW) {
                a(this.b);
            }
            a.a().c();
        } catch (IOException e) {
            e.printStackTrace();
            this.c.onError(this.b, ProcessError.FILE_NOT_FOUND);
        } catch (Throwable th) {
            a.a().c();
        }
    }

    public void a(RuntimeException runtimeException) {
    }
}
