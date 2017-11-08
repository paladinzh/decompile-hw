package com.fyusion.sdk.processor;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import com.fyusion.sdk.common.ext.FyuseProcessorParameters;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.l;
import com.fyusion.sdk.common.h;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* compiled from: Unknown */
public class FyuseProcessor {
    private static FyuseProcessor d = null;
    ExecutorService a = Executors.newSingleThreadExecutor(new ThreadFactory(this) {
        final /* synthetic */ FyuseProcessor a;

        {
            this.a = r1;
        }

        public Thread newThread(Runnable runnable) {
            return new Thread(runnable, "FyuseProcessor");
        }
    });
    l b = new l(this) {
        final /* synthetic */ FyuseProcessor a;

        {
            this.a = r1;
        }

        public void a(ProcessItem processItem, int i, int i2, Bitmap bitmap, Matrix matrix) {
            if (processItem.getListener() instanceof l) {
                ((l) processItem.getListener()).a(processItem, i, i2, bitmap, matrix);
            }
        }

        public void onError(ProcessItem processItem, ProcessError processError) {
            synchronized (this.a.f) {
                this.a.e.remove(processItem);
            }
            processItem.getListener().onError(processItem, processError);
        }

        public void onImageDataReady(ProcessItem processItem) {
            if (processItem.getTarget() != ProcessState.READY_FOR_UPLOAD) {
                onProcessComplete(processItem);
                return;
            }
            processItem.setState(ProcessState.READY_FOR_VIEW);
            synchronized (this.a.f) {
                this.a.e.remove(processItem);
            }
            this.a.a(processItem);
        }

        public void onMetadataReady(ProcessItem processItem, int i) {
            if (processItem.getListener() instanceof l) {
                ((l) processItem.getListener()).onMetadataReady(processItem, i);
            }
        }

        public void onProcessComplete(ProcessItem processItem) {
            synchronized (this.a.f) {
                this.a.e.remove(processItem);
            }
            processItem.setState(processItem.getTarget());
            processItem.getListener().onProcessComplete(processItem);
        }

        public void onProgress(ProcessItem processItem, int i, int i2, Bitmap bitmap) {
            if (processItem.getListener() instanceof l) {
                ((l) processItem.getListener()).a(processItem, i, i2, bitmap, null);
            }
        }

        public void onSliceFound(ProcessItem processItem, int i) {
            if (processItem.getListener() instanceof l) {
                ((l) processItem.getListener()).onSliceFound(processItem, i);
            }
        }

        public void onSliceReady(ProcessItem processItem, int i) {
            if (processItem.getListener() instanceof l) {
                ((l) processItem.getListener()).onSliceReady(processItem, i);
            }
        }
    };
    private FyuseProcessorParameters c;
    private ArrayList<ProcessItem> e = new ArrayList();
    private final Object f = new Object();

    private FyuseProcessor() {
        if (d != null) {
            throw new IllegalStateException("Already instantiated");
        }
    }

    private synchronized ProcessItem a(ProcessItem processItem) {
        if (processItem == null) {
            return null;
        }
        processItem.setFyuseProcessorParameters(this.c);
        a(processItem, this.b);
        return processItem;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private ProcessItem a(ProcessItem processItem, ProcessorListener processorListener) {
        synchronized (this.f) {
            Iterator it = this.e.iterator();
            while (it.hasNext()) {
                ProcessItem processItem2 = (ProcessItem) it.next();
                if (processItem2.getFile().equals(processItem.getFile())) {
                    processItem2.setListener(processItem.getListener());
                    h.b("FyuseProcessor", "File is already in process queue: " + processItem.getFile().getName());
                    return processItem2;
                }
            }
        }
        if ((SystemClock.elapsedRealtime() - r2 <= 20000 ? 1 : null) == null) {
            processorListener.onError(processItem, ProcessError.RECORDING_IN_PROGRESS);
            return processItem;
        }
    }

    private boolean b(ProcessItem processItem) {
        return !processItem.getFile().isDirectory() ? processItem.getFile().exists() : !g.c(processItem.getFile().getPath()) ? new File(processItem.getFile(), k.ak).exists() || new File(processItem.getFile(), k.ag).exists() : false;
    }

    public static FyuseProcessor getInstance() {
        if (d == null) {
            synchronized (FyuseProcessor.class) {
                if (d == null) {
                    d = new FyuseProcessor();
                }
            }
        }
        return d;
    }

    public synchronized ProcessItem prepareForViewing(File file, ProcessorListener processorListener) {
        if (file != null) {
            ProcessItem processItem = new ProcessItem(file, processorListener, ProcessState.READY_FOR_VIEW);
            processItem.setFyuseProcessorParameters(this.c);
            a(processItem, this.b);
            return processItem;
        }
        processorListener.onError(null, ProcessError.FILE_NOT_FOUND);
        return null;
    }
}
