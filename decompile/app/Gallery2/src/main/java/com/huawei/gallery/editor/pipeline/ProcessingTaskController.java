package com.huawei.gallery.editor.pipeline;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.HandlerThread;
import android.os.Message;
import com.android.gallery3d.util.GalleryLog;
import java.util.HashMap;

public final class ProcessingTaskController implements Callback {
    public static final Object EDITOR_LOCK = new Object();
    private short mCurrentType;
    private HandlerThread mHandlerThread;
    private volatile boolean mInEditor;
    private Handler mProcessingHandler;
    private final Handler mResultHandler;
    private HashMap<Integer, ProcessingTask> mTasks;

    public boolean handleMessage(Message msg) {
        synchronized (EDITOR_LOCK) {
            if (this.mInEditor) {
                ProcessingTask task = (ProcessingTask) this.mTasks.get(Integer.valueOf(msg.what >> 16));
                if (task != null) {
                    task.processRequest((Request) msg.obj);
                    return true;
                }
                return false;
            }
            GalleryLog.w("ProcessingTaskController", "have leave editor, thread ignore this message:" + msg.what);
            return true;
        }
    }

    public ProcessingTaskController() {
        this.mHandlerThread = null;
        this.mProcessingHandler = null;
        this.mTasks = new HashMap();
        this.mInEditor = false;
        this.mResultHandler = new Handler() {
            public void handleMessage(Message msg) {
                if (ProcessingTaskController.this.mInEditor) {
                    ProcessingTask task = (ProcessingTask) ProcessingTaskController.this.mTasks.get(Integer.valueOf(msg.what >> 16));
                    if (task != null) {
                        if (msg.arg1 == 1) {
                            task.onResult((Result) msg.obj);
                        } else if (msg.arg1 == 2) {
                            task.onUpdate((Update) msg.obj);
                        } else {
                            GalleryLog.w("ProcessingTaskController", "received unknown message! " + msg.arg1);
                        }
                    }
                    return;
                }
                GalleryLog.w("ProcessingTaskController", "have leave editor, main ignore this message:" + msg.what);
            }
        };
        this.mHandlerThread = new HandlerThread("ProcessingTaskController", -2);
        this.mHandlerThread.start();
        this.mProcessingHandler = new Handler(this.mHandlerThread.getLooper(), this);
    }

    public Handler getProcessingHandler() {
        return this.mProcessingHandler;
    }

    public Handler getResultHandler() {
        return this.mResultHandler;
    }

    public short getReservedType() {
        short s = this.mCurrentType;
        this.mCurrentType = (short) (s + 1);
        return (short) s;
    }

    public void add(ProcessingTask task) {
        task.added(this);
        this.mTasks.put(Integer.valueOf(task.getType()), task);
    }

    public void quit() {
        this.mHandlerThread.quit();
    }

    public void inEditor(boolean inEditor) {
        this.mInEditor = inEditor;
    }
}
