package com.fyusion.sdk.processor;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.SystemClock;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.FyuseProcessorParameters;
import com.fyusion.sdk.common.ext.ProcessError;
import com.fyusion.sdk.common.ext.ProcessItem;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessDataType;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessState;
import com.fyusion.sdk.common.ext.ProcessItem.ProcessType;
import com.fyusion.sdk.common.ext.ProcessedImageListener;
import com.fyusion.sdk.common.ext.ProcessedImageListener.ProcessedImageError;
import com.fyusion.sdk.common.ext.ProcessorListener;
import com.fyusion.sdk.common.ext.f;
import com.fyusion.sdk.common.ext.g;
import com.fyusion.sdk.common.ext.j;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.ext.l;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/* compiled from: Unknown */
public class FyuseProcessor implements f {
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
    k b = new k(this) {
        final /* synthetic */ FyuseProcessor a;

        {
            this.a = r1;
        }

        public void a(ProcessItem processItem, int i, int i2, Bitmap bitmap, Matrix matrix) {
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).a(processItem, i, i2, bitmap, matrix);
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
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).onMetadataReady(processItem, i);
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
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).a(processItem, i, i2, bitmap, null);
            }
        }

        public void onSliceFound(ProcessItem processItem, int i) {
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).onSliceFound(processItem, i);
            }
        }

        public void onSliceReady(ProcessItem processItem, int i) {
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).onSliceReady(processItem, i);
            }
        }

        public void onTweensReady(ProcessItem processItem) {
            if (processItem.getListener() instanceof k) {
                ((k) processItem.getListener()).onTweensReady(processItem);
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
                    DLog.i("FyuseProcessor", "File is already in process queue: " + processItem.getFile().getName());
                    return processItem2;
                }
            }
        }
        if ((SystemClock.elapsedRealtime() - r2 <= 20000 ? 1 : null) == null) {
            processorListener.onError(processItem, ProcessError.RECORDING_IN_PROGRESS);
            return processItem;
        }
    }

    private boolean a(File file, ProcessedImageListener processedImageListener) {
        if (processedImageListener == null || file == null) {
            return false;
        }
        try {
            if (new l(g.a(), file).g().getLevel() >= ProcessState.READY_FOR_VIEW.getLevel()) {
                return true;
            }
            processedImageListener.onError(ProcessedImageError.FYUSE_NOT_PREPARED, "Item has not been prepared / rendered. Please call prepareForView first.");
            return false;
        } catch (IOException e) {
            processedImageListener.onError(ProcessedImageError.FYUSE_DOES_NOT_EXIST, "Item does not exist.");
            return false;
        }
    }

    private boolean b(ProcessItem processItem) {
        return !processItem.getFile().isDirectory() ? processItem.getFile().exists() : !f.c(processItem.getFile().getPath()) ? new File(processItem.getFile(), j.ak).exists() || new File(processItem.getFile(), j.ag).exists() : false;
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

    public FyuseProcessorParameters getFyuseProcessorParameters() {
        return this.c;
    }

    public synchronized ProcessItem prepareForUpload(File file, ProcessorListener processorListener) {
        if (file == null) {
            return null;
        }
        ProcessItem processItem = new ProcessItem(file, processorListener, ProcessState.READY_FOR_UPLOAD);
        processItem.setFyuseProcessorParameters(this.c);
        a(processItem, this.b);
        return processItem;
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

    public ProcessItem requestImagesAtDegreeList(File file, ProcessedImageListener processedImageListener, double[] dArr) {
        if (!a(file, processedImageListener) || dArr == null || dArr.length == 0) {
            return null;
        }
        ProcessItem processItem = new ProcessItem(file, ProcessType.GET_INFO, ProcessDataType.DEGREE_LIST, dArr);
        processItem.setFyuseProcessorParameters(this.c);
        this.a.submit(new c(processItem, processedImageListener));
        return processItem;
    }

    public ProcessItem requestImagesAtInterval(File file, ProcessedImageListener processedImageListener, int i) {
        if (!a(file, processedImageListener)) {
            return null;
        }
        if (i >= 0 && i <= 360) {
            ProcessItem processItem = new ProcessItem(file, ProcessType.GET_INFO, ProcessDataType.INTERVAL, i);
            processItem.setFyuseProcessorParameters(this.c);
            this.a.submit(new c(processItem, processedImageListener));
            return processItem;
        }
        processedImageListener.onError(ProcessedImageError.INVALID_INPUT_RANGE, "Please specify an interval in the valid range: [0,360] degrees.");
        return null;
    }

    public void setFyuseProcessorParameters(FyuseProcessorParameters fyuseProcessorParameters) {
        this.c = fyuseProcessorParameters;
    }
}
