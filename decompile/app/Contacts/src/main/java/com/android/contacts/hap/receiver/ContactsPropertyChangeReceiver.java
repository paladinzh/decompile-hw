package com.android.contacts.hap.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import java.util.ArrayList;

public class ContactsPropertyChangeReceiver extends BroadcastReceiver {
    private static boolean BIG_SCREEN_ENABLED = false;
    private static boolean IP_CALL_ENABLED = false;
    private static boolean MERGE_ENABLED = false;
    private static boolean STUB_ENABLED = false;
    private static ArrayList<BigSceenLisener> mListener = new ArrayList();

    public interface BigSceenLisener {
        void onEnabledStatusChanged(boolean z);
    }

    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null && "com.huawei.contacts.action.property".equals(intent.getAction())) {
            setStubEnabled();
            String property = intent.getStringExtra("property");
            boolean checked = intent.getBooleanExtra("checked", false);
            if ("merge_contacts".equals(property)) {
                setMergeEnabled(checked);
            } else if ("big_screen".equals(property)) {
                setBigScreenEnabled(checked);
                notifyBigScreenListener(BIG_SCREEN_ENABLED);
            } else if ("ip_call".equals(property)) {
                setIPCallEnabled(checked);
            }
        }
    }

    private static synchronized void setBigScreenEnabled(boolean aBigScreen) {
        synchronized (ContactsPropertyChangeReceiver.class) {
            BIG_SCREEN_ENABLED = aBigScreen;
        }
    }

    private static synchronized void setIPCallEnabled(boolean aIpCall) {
        synchronized (ContactsPropertyChangeReceiver.class) {
            IP_CALL_ENABLED = aIpCall;
        }
    }

    private static synchronized void setMergeEnabled(boolean aMerge) {
        synchronized (ContactsPropertyChangeReceiver.class) {
            MERGE_ENABLED = aMerge;
        }
    }

    private static synchronized void setStubEnabled() {
        synchronized (ContactsPropertyChangeReceiver.class) {
            STUB_ENABLED = true;
        }
    }

    public static boolean getMergeEnabledFlag() {
        return MERGE_ENABLED;
    }

    public static boolean getStubEnabledFlag() {
        return STUB_ENABLED;
    }

    public static boolean getIpCallEnabledFlag() {
        return IP_CALL_ENABLED;
    }

    public static void notifyBigScreenListener(boolean status) {
        for (BigSceenLisener listener : mListener) {
            listener.onEnabledStatusChanged(status);
        }
    }

    public static void registerBigScreenListener(BigSceenLisener listener) {
        mListener.add(listener);
    }

    public static void unRegisterBigScreenListener(BigSceenLisener listener) {
        mListener.remove(listener);
    }
}
