package com.android.contacts;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.util.HwLog;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContactPreRefreshService {
    private static final CopyOnWriteArrayList<Listener> sListeners = new CopyOnWriteArrayList();
    private Handler mMainHandler = new Handler(Looper.getMainLooper());

    public interface Listener {
        void onDelContactsCompleted(Intent intent);
    }

    public static void registerListener(Listener listener) {
        if (listener instanceof Activity) {
            sListeners.add(0, listener);
            return;
        }
        throw new ClassCastException("Only activities can be registered to receive callback from " + ContactPreRefreshService.class.getName());
    }

    public static void unregisterListener(Listener listener) {
        sListeners.remove(listener);
    }

    private void deliverCallback(final Intent callbackIntent) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                ContactPreRefreshService.this.deliverCallbackOnUiThread(callbackIntent);
            }
        });
    }

    private void deliverCallbackOnUiThread(Intent callbackIntent) {
        for (Listener listener : sListeners) {
            if (callbackIntent.getComponent().equals(((Activity) listener).getIntent().getComponent())) {
                listener.onDelContactsCompleted(callbackIntent);
                return;
            }
        }
    }

    public void delContactsCallback(Context context, long[] aContactIds) {
        Intent callbackIntent = new Intent(context, PeopleActivity.class);
        callbackIntent.setAction("action_hide_delete_contacts");
        callbackIntent.putExtra("ContactIds", aContactIds);
        deliverCallback(callbackIntent);
        HwLog.v("ContactDelService", " call delContactsCallback");
    }

    public void delOneContactCallback(Context context, long contactId) {
        delContactsCallback(context, new long[]{contactId});
    }
}
