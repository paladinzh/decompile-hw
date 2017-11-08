package com.huawei.harassmentinterception.ui;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;

public interface IDataLoadingWidget {

    public static abstract class DataLoadingBaseActivity extends HsmActivity implements IDataLoadingWidget {
        private Handler mDataLoadingHandler = new DataLoadingMsgHandler(this);
        private DataLoadingThread mDataLoadingThread = null;
        private DataChangeObserver mDataObserver = null;
        private Uri mDataUri = null;
        private boolean mIsAbortLoading = false;
        private boolean mIsLoading = false;

        public Context getContext() {
            return this;
        }

        protected Handler getMessageHandler() {
            return this.mDataLoadingHandler;
        }

        protected void loadData() {
            if (this.mDataLoadingThread == null) {
                this.mDataLoadingThread = new DataLoadingThread("HarassIntercept_DataLoadingAc", this);
                this.mDataLoadingThread.start();
            }
        }

        protected void loadDataWithDelay(long lDelay) {
            postLoadDelayMsg(lDelay);
        }

        protected void setDataSourceUri(Uri uri) {
            this.mDataUri = uri;
        }

        protected void registerDataObserver() {
            if (this.mDataUri != null) {
                if (this.mDataObserver == null) {
                    this.mDataObserver = new DataChangeObserver();
                }
                getContentResolver().registerContentObserver(this.mDataUri, true, this.mDataObserver);
            }
        }

        protected void unregisterDataObserver() {
            if (this.mDataObserver != null) {
                getContentResolver().unregisterContentObserver(this.mDataObserver);
            }
        }

        public void onLoadData() {
            loadData();
        }

        public void onLoadingStart() {
        }

        public void onLoadingComplete() {
            this.mDataLoadingThread = null;
        }

        public void onLoadingAbort() {
            this.mDataLoadingThread = null;
        }

        public void abortLoading() {
            this.mIsAbortLoading = true;
        }

        public boolean isAbortLoading() {
            return this.mIsAbortLoading;
        }

        public boolean isLoadingData() {
            return this.mIsLoading;
        }

        public boolean isDataChanged() {
            if (this.mDataObserver != null) {
                return this.mDataObserver.isDataChanged();
            }
            return true;
        }

        public void postLoadingStartMsg() {
            this.mIsLoading = true;
            MsgPostHelper.postLoadingStartMsg(this.mDataLoadingHandler);
        }

        public void postAppendMsg(Object obj) {
            MsgPostHelper.postAppendMsg(this.mDataLoadingHandler, obj);
        }

        public void postLoadingCompleteMsg() {
            this.mIsLoading = false;
            MsgPostHelper.postLoadingCompleteMsg(this.mDataLoadingHandler);
        }

        public void postLoadingAbortMsg() {
            this.mIsLoading = false;
            MsgPostHelper.postLoadingAbortMsg(this.mDataLoadingHandler);
        }

        public void postLoadDelayMsg(long lDelay) {
            MsgPostHelper.postDelayLoadMsg(this.mDataLoadingHandler, lDelay);
        }

        public boolean onHandleMessage(Message msg) {
            switch (msg.what) {
                case 106:
                    registerDataObserver();
                    return true;
                default:
                    return false;
            }
        }

        protected void onResume() {
            if (this.mDataObserver != null) {
                this.mDataObserver.resetDataChangeFlag();
                unregisterDataObserver();
            }
            super.onResume();
        }

        public void onStop() {
            MsgPostHelper.postRegDataObserverMsg(this.mDataLoadingHandler, 50);
            super.onStop();
        }

        public void onDestroy() {
            abortLoading();
            this.mDataLoadingHandler.removeMessages(106);
            unregisterDataObserver();
            super.onDestroy();
        }
    }

    public static class DataChangeObserver extends ContentObserver {
        private boolean mIsDataChanged = true;

        public DataChangeObserver() {
            super(null);
        }

        public boolean isDataChanged() {
            return this.mIsDataChanged;
        }

        public void resetDataChangeFlag() {
            this.mIsDataChanged = false;
        }

        public void onChange(boolean selfChange) {
            this.mIsDataChanged = true;
        }
    }

    public static class DataLoadingMsgHandler extends Handler {
        private static final String TAG = "DataLoadingMsgHandler";
        WeakReference<IDataLoadingWidget> mWidget;

        DataLoadingMsgHandler(IDataLoadingWidget widget) {
            this.mWidget = new WeakReference(widget);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            IDataLoadingWidget widget = (IDataLoadingWidget) this.mWidget.get();
            if (widget == null) {
                HwLog.w(TAG, "handleMessage: Widget is destroyed");
            } else if (!widget.onHandleMessage(msg)) {
                switch (msg.what) {
                    case 101:
                        widget.onLoadingStart();
                        break;
                    case 102:
                        widget.onAppendData(msg.obj);
                        break;
                    case 103:
                        widget.onLoadingComplete();
                        break;
                    case 104:
                        widget.onLoadingAbort();
                        break;
                    case 105:
                        widget.onLoadData();
                        break;
                }
            }
        }
    }

    public static class DataLoadingThread extends Thread {
        private static final String TAG = "DataLoadingThread";
        WeakReference<IDataLoadingWidget> mWidget;

        public DataLoadingThread(String name, IDataLoadingWidget widget) {
            super(name);
            this.mWidget = new WeakReference(widget);
        }

        public void run() {
            IDataLoadingWidget widget = (IDataLoadingWidget) this.mWidget.get();
            if (widget == null) {
                HwLog.w(TAG, "DataLoadingThread: Widget is destroyed");
                return;
            }
            widget.postLoadingStartMsg();
            widget.onLoadDataInBackground();
            widget = (IDataLoadingWidget) this.mWidget.get();
            if (widget == null) {
                HwLog.w(TAG, "DataLoadingThread: Widget is destroyed after loading");
                return;
            }
            if (widget.isAbortLoading()) {
                widget.postLoadingAbortMsg();
            } else {
                widget.postLoadingCompleteMsg();
            }
        }
    }

    public static class MsgPostHelper {
        public static void postLoadingStartMsg(Handler msgHanlder) {
            Message message = msgHanlder.obtainMessage();
            message.what = 101;
            message.sendToTarget();
        }

        public static void postAppendMsg(Handler msgHanlder, Object obj) {
            Message message = msgHanlder.obtainMessage();
            message.what = 102;
            message.obj = obj;
            message.sendToTarget();
        }

        public static void postLoadingCompleteMsg(Handler msgHanlder) {
            Message message = msgHanlder.obtainMessage();
            message.what = 103;
            message.sendToTarget();
        }

        public static void postLoadingAbortMsg(Handler msgHanlder) {
            Message message = msgHanlder.obtainMessage();
            message.what = 104;
            message.sendToTarget();
        }

        public static void postDelayLoadMsg(Handler msgHanlder, long nDelay) {
            postMsgWithDelay(msgHanlder, 105, nDelay);
        }

        public static void postRegDataObserverMsg(Handler msgHanlder, long nDelay) {
            postMsgWithDelay(msgHanlder, 106, nDelay);
        }

        private static void postMsgWithDelay(Handler msgHanlder, int nMsg, long nDelay) {
            Message message = msgHanlder.obtainMessage();
            message.what = nMsg;
            msgHanlder.sendMessageDelayed(message, nDelay);
        }
    }

    Context getContext();

    boolean isAbortLoading();

    void onAppendData(Object obj);

    boolean onHandleMessage(Message message);

    void onLoadData();

    void onLoadDataInBackground();

    void onLoadingAbort();

    void onLoadingComplete();

    void onLoadingStart();

    void postAppendMsg(Object obj);

    void postLoadingAbortMsg();

    void postLoadingCompleteMsg();

    void postLoadingStartMsg();
}
