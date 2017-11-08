package com.fyusion.sdk.common.ext;

import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.util.FyuseUtils;
import java.io.File;

/* compiled from: Unknown */
public class ProcessItem {
    private ProcessorListener a;
    private File b;
    private ProcessState c;
    private ProcessState d;
    private boolean e;
    private ProcessType f;
    private int g;
    private double[] h = null;
    private ProcessDataType i;
    private a j = new a();
    private FyuseProcessorParameters k;
    private e l;
    private boolean m = false;

    /* compiled from: Unknown */
    public enum ProcessDataType {
        INTERVAL(0),
        DEGREE_LIST(1);
        
        private int a;

        private ProcessDataType(int i) {
            this.a = i;
        }

        public int getLevel() {
            return this.a;
        }
    }

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

        public int getLevel() {
            return this.a;
        }
    }

    public ProcessItem(File file, ProcessType processType, ProcessDataType processDataType, int i) {
        this.b = file;
        this.a = null;
        this.i = processDataType;
        this.g = i;
        this.h = null;
        this.f = processType;
    }

    public ProcessItem(File file, ProcessType processType, ProcessDataType processDataType, double[] dArr) {
        this.b = file;
        this.a = null;
        this.i = processDataType;
        this.g = 0;
        this.h = dArr;
        this.f = processType;
    }

    public ProcessItem(File file, ProcessorListener processorListener, ProcessState processState) {
        this.b = file;
        this.a = processorListener;
        this.c = processState;
        this.f = ProcessType.PROCESS;
    }

    private void a() {
        if (this.m && this.d == ProcessState.CANCELLED) {
            DLog.d("ProcessItem", "State changed, deleting item.");
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

    public void delete() {
        this.e = true;
        this.m = true;
    }

    public double[] getAngleList() {
        return this.h;
    }

    public ProcessDataType getDataType() {
        return this.i;
    }

    public File getFile() {
        return this.b;
    }

    public e getFyuseClass() {
        return this.l;
    }

    public FyuseProcessorParameters getFyuseProcessorParameters() {
        return this.k;
    }

    public int getInterval() {
        return this.g;
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

    public ProcessType getType() {
        return this.f;
    }

    public boolean isCancelled() {
        return this.e;
    }

    public boolean removeRunner() {
        return this.j.b();
    }

    public void setFyuseClass(e eVar) {
        this.l = eVar;
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

    public void setTarget(ProcessState processState) {
        this.c = processState;
    }

    public void waitForRunners() {
        this.j.a(true);
    }
}
