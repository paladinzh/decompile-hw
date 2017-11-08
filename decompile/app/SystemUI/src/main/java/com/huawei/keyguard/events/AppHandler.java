package com.huawei.keyguard.events;

import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import com.huawei.keyguard.util.HwLog;
import java.util.ArrayList;
import java.util.HashSet;

public class AppHandler {
    private static AppHandler sInst;
    private MessageCallback mCallbacks = new MessageCallback();
    private Handler mUIHandler = new Handler(Looper.getMainLooper(), this.mCallbacks);

    private static class MessageCallback implements Callback {
        private HashSet<Callback> mAllListeners;
        private ArrayList<Callback> mTempListeners;

        private MessageCallback() {
            this.mAllListeners = new HashSet();
            this.mTempListeners = new ArrayList();
        }

        public boolean handleMessage(Message msg) {
            synchronized (this.mAllListeners) {
                this.mTempListeners.addAll(this.mAllListeners);
            }
            for (int i = 0; i < this.mTempListeners.size(); i++) {
                ((Callback) this.mTempListeners.get(i)).handleMessage(msg);
            }
            this.mTempListeners.clear();
            return false;
        }

        private void addCallback(Callback callback) {
            synchronized (this.mAllListeners) {
                this.mAllListeners.add(callback);
            }
        }

        private void removeCallback(Callback callback) {
            synchronized (this.mAllListeners) {
                this.mAllListeners.remove(callback);
            }
        }
    }

    public static AppHandler getInst() {
        AppHandler appHandler;
        synchronized (AppHandler.class) {
            if (sInst == null) {
                sInst = new AppHandler();
            }
            appHandler = sInst;
        }
        return appHandler;
    }

    private AppHandler() {
    }

    public static Handler getHandler() {
        return getInst().mUIHandler;
    }

    public static void sendImmediateMessage(int msgId) {
        HwLog.w("AppHandler", "sendImmediateMessage " + msgId);
        AppHandler handler = getInst();
        handler.mCallbacks.handleMessage(handler.mUIHandler.obtainMessage(msgId));
    }

    public static void sendMessage(int msgId) {
        HwLog.w("AppHandler", "sendMessage " + msgId);
        getHandler().obtainMessage(msgId).sendToTarget();
    }

    public static void sendSingleMessage(int msgId) {
        sendSingleMessage(msgId, 50);
    }

    public static void sendSingleMessage(int msgId, long delay) {
        HwLog.w("AppHandler", "sendSingleMessage " + msgId);
        Handler handler = getHandler();
        handler.removeMessages(msgId);
        handler.sendMessageDelayed(handler.obtainMessage(msgId), delay);
    }

    public static void sendMessage(int msgId, Object obj) {
        HwLog.w("AppHandler", "sendMessage with obj" + msgId);
        getHandler().obtainMessage(msgId, obj).sendToTarget();
    }

    public static void sendMessage(int msgId, int arg1, int arg2, Object obj) {
        HwLog.w("AppHandler", "sendMessage " + msgId + " with args (" + arg1 + ", " + arg2 + ")");
        getHandler().obtainMessage(msgId, arg1, arg2, obj).sendToTarget();
    }

    public static void addListener(Callback callback) {
        HwLog.w("AppHandler", "addListener " + callback);
        getInst().mCallbacks.addCallback(callback);
    }

    public static void removeListener(Callback callback) {
        HwLog.w("AppHandler", "removeListener " + callback);
        getInst().mCallbacks.removeCallback(callback);
    }
}
