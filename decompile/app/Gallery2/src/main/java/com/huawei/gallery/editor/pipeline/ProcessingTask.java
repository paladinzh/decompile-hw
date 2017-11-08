package com.huawei.gallery.editor.pipeline;

import android.os.Handler;
import android.os.Message;

public abstract class ProcessingTask {
    private Handler mProcessingHandler;
    private Handler mResultHandler;
    protected short mType;

    interface Request {
    }

    interface Result {
    }

    interface Update {
    }

    public abstract Result doInBackground(Request request);

    public abstract void onResult(Result result);

    public boolean postRequest(Request message) {
        int type = getType(message);
        Message msg = this.mProcessingHandler.obtainMessage(type);
        msg.obj = message;
        if (isPriorityTask()) {
            if (this.mProcessingHandler.hasMessages(type)) {
                return false;
            }
            this.mProcessingHandler.sendMessageAtFrontOfQueue(msg);
        } else if (isDelayedTask(message)) {
            if (this.mProcessingHandler.hasMessages(type)) {
                this.mProcessingHandler.removeMessages(type);
            }
            this.mProcessingHandler.sendMessageDelayed(msg, 200);
        } else if (message instanceof Render) {
            this.mProcessingHandler.sendMessageDelayed(msg, (long) ((Render) message).delay);
        } else {
            this.mProcessingHandler.sendMessage(msg);
        }
        return true;
    }

    public void processRequest(Request message) {
        Object result = doInBackground(message);
        Message msg = this.mResultHandler.obtainMessage(getType(message));
        msg.obj = result;
        msg.arg1 = 1;
        this.mResultHandler.sendMessage(msg);
    }

    public void added(ProcessingTaskController taskController) {
        this.mResultHandler = taskController.getResultHandler();
        this.mProcessingHandler = taskController.getProcessingHandler();
        this.mType = taskController.getReservedType();
    }

    public int getType() {
        return this.mType;
    }

    public int getType(Request message) {
        return this.mType << 16;
    }

    public void onUpdate(Update message) {
    }

    public boolean isPriorityTask() {
        return false;
    }

    public boolean isDelayedTask(Request message) {
        return false;
    }
}
