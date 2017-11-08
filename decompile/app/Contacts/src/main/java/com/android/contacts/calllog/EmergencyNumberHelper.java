package com.android.contacts.calllog;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import com.android.contacts.PhoneCallDetails;
import com.android.contacts.PhoneCallDetailsViews;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import java.util.Stack;

public class EmergencyNumberHelper {
    private EmergencyNumberHandler mHandler;
    private Stack<QueryObject> mStack;

    private static class EmergencyNumberHandler extends Handler {
        private Handler mMainHandler = null;

        public EmergencyNumberHandler(Looper looper, Handler handler) {
            super(looper);
            this.mMainHandler = handler;
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (msg.obj != null) {
                        QueryObject obj = msg.obj;
                        boolean result = CommonUtilMethods.isEmergencyNumber(obj.mNumber, SimFactoryManager.isDualSim());
                        Message msgBack = this.mMainHandler.obtainMessage();
                        msgBack.what = result ? 2 : 3;
                        msgBack.obj = obj;
                        this.mMainHandler.sendMessage(msgBack);
                        break;
                    }
                    break;
                default:
                    getLooper().quitSafely();
                    break;
            }
            super.handleMessage(msg);
        }
    }

    public static class QueryObject {
        public PhoneCallDetails mDetails = null;
        public String mNumber = null;
        public PhoneCallDetailsViews mViews = null;
    }

    public EmergencyNumberHelper(Handler mainHandler) {
        this.mHandler = null;
        this.mStack = null;
        this.mStack = new Stack();
        HandlerThread ht = new HandlerThread("EmergencyNumberHelper");
        ht.setPriority(10);
        ht.start();
        this.mHandler = new EmergencyNumberHandler(ht.getLooper(), mainHandler);
    }

    public void queryEmergencyNumber(String number, PhoneCallDetailsViews views) {
        QueryObject obj = obtainQueryObject(number, views);
        Message msg = this.mHandler.obtainMessage();
        msg.what = 1;
        msg.obj = obj;
        this.mHandler.sendMessage(msg);
    }

    public void releaseHelper() {
        this.mHandler.sendEmptyMessage(100);
        this.mHandler = null;
    }

    public QueryObject obtainQueryObject(String number, PhoneCallDetailsViews views) {
        QueryObject obj;
        if (this.mStack.isEmpty()) {
            obj = new QueryObject();
        } else {
            obj = (QueryObject) this.mStack.pop();
        }
        obj.mDetails = views.getPhoneCallDetails();
        obj.mNumber = number;
        obj.mViews = views;
        return obj;
    }

    public void recycleQueryObject(QueryObject obj) {
        if (this.mStack.size() < 10) {
            obj.mViews = null;
            obj.mNumber = null;
            this.mStack.push(obj);
        }
    }
}
