package com.android.contacts;

import android.app.Service;
import android.content.Intent;
import android.content.Loader;
import android.content.Loader.OnLoadCompleteListener;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import com.android.contacts.model.Contact;
import com.android.contacts.model.ContactLoader;
import com.android.contacts.util.HwLog;

public class ViewNotificationService extends Service {
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final String TAG = ViewNotificationService.class.getSimpleName();
    ServiceHandler mHandler = new ServiceHandler();

    class ServiceHandler extends Handler {
        ServiceHandler() {
        }

        public void handleMessage(Message msg) {
            Uri lUri = msg.obj;
            final int lStartId = msg.arg1;
            ContactLoader contactLoader = new ContactLoader(ViewNotificationService.this.getApplicationContext(), lUri, true);
            contactLoader.registerListener(0, new OnLoadCompleteListener<Contact>() {
                public void onLoadComplete(Loader<Contact> loader, Contact data) {
                    try {
                        loader.reset();
                    } catch (RuntimeException e) {
                        HwLog.e(ViewNotificationService.TAG, "Error reseting loader", e);
                    }
                    try {
                        ViewNotificationService.this.stopSelfResult(lStartId);
                    } catch (RuntimeException e2) {
                        HwLog.e(ViewNotificationService.TAG, "Error stopping service", e2);
                    }
                }
            });
            contactLoader.startLoading();
        }
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (DEBUG) {
            HwLog.d(TAG, "onHandleIntent(). Intent: " + intent);
        }
        if (intent != null) {
            Message lMsg = this.mHandler.obtainMessage(0, intent.getData());
            lMsg.arg1 = startId;
            this.mHandler.sendMessage(lMsg);
        }
        return 3;
    }

    public IBinder onBind(Intent intent) {
        return null;
    }
}
