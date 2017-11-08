package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.ext.util.FyuseUtils;
import com.fyusion.sdk.common.h;
import java.io.File;

/* compiled from: Unknown */
public class ProcessItem {
    private ProcessorListener a;
    private File b;
    private ProcessState c;
    private ProcessState d;
    private boolean e;
    private ProcessType f;
    private double[] h = null;
    private b j = new b();
    private FyuseProcessorParameters k;
    private f l;
    private boolean m = false;

    /* compiled from: Unknown */
    public enum ProcessState {
        INITIAL(0),
        CANCELLED(3),
        READY_FOR_VIEW(1),
        READY_FOR_UPLOAD(2);
        
        private int a;

        private ProcessState(int i) {
            this.a = i;
        }

        public int getLevel() {
            return this.a;
        }
    }

    /* compiled from: Unknown */
    public enum ProcessType {
        PROCESS(0),
        GET_INFO(1);
        
        private int a;

        private ProcessType(int i) {
            this.a = i;
        }
    }

    public ProcessItem(File file, ProcessorListener processorListener, ProcessState processState) {
        this.b = file;
        this.a = processorListener;
        this.c = processState;
        this.f = ProcessType.PROCESS;
    }

    private void a() {
        if (this.m && this.d == ProcessState.CANCELLED) {
            h.a("ProcessItem", "State changed, deleting item.");
            FyuseUtils.delete(this.b);
            this.m = false;
        }
    }

    public boolean addRunner() {
        return this.j.a();
    }

    public void cancel() {
        this.e = true;
    }

    public File getFile() {
        return this.b;
    }

    public FyuseProcessorParameters getFyuseProcessorParameters() {
        return this.k;
    }

    public ProcessorListener getListener() {
        return this.a;
    }

    public String getPath() {
        return this.b.getPath();
    }

    public ProcessState getState() {
        return this.d;
    }

    public ProcessState getTarget() {
        return this.c;
    }

    public boolean isCancelled() {
        return this.e;
    }

    public boolean removeRunner() {
        return this.j.b();
    }

    public void setFyuseClass(f fVar) {
        this.l = fVar;
    }

    public void setFyuseProcessorParameters(FyuseProcessorParameters fyuseProcessorParameters) {
        this.k = fyuseProcessorParameters;
    }

    public void setListener(ProcessorListener processorListener) {
        this.a = processorListener;
    }

    public void setState(ProcessState processState) {
        this.d = processState;
        a();
    }
}
