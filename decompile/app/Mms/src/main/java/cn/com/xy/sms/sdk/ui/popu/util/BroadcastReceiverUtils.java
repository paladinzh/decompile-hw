package cn.com.xy.sms.sdk.ui.popu.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.support.v4.util.LruCache;
import cn.com.xy.sms.sdk.log.LogManager;

public class BroadcastReceiverUtils {
    private final LruCache<Integer, BroadcastReceiver> mLastReceivers = new LruCache(10);
    private final LruCache<Integer, String> mWorkingReceivers = new LruCache(10);

    public boolean inWorkingReceiver(int contextHashCode, String receiverKey) {
        boolean equals;
        synchronized (this.mWorkingReceivers) {
            equals = receiverKey != null ? receiverKey.equals(this.mWorkingReceivers.get(Integer.valueOf(contextHashCode))) : false;
        }
        return equals;
    }

    public void addWorkingReceiver(int contextHashCode, String receiverKey) {
        synchronized (this.mWorkingReceivers) {
            this.mWorkingReceivers.put(Integer.valueOf(contextHashCode), receiverKey);
        }
    }

    public void removeWorkingReceiver(int contextHashCode) {
        synchronized (this.mWorkingReceivers) {
            this.mWorkingReceivers.remove(Integer.valueOf(contextHashCode));
        }
    }

    public int getReceiverPriority() {
        return 100;
    }

    public BroadcastReceiver getLastReceiver(int contextHashCode) {
        BroadcastReceiver broadcastReceiver;
        synchronized (this.mLastReceivers) {
            broadcastReceiver = (BroadcastReceiver) this.mLastReceivers.get(Integer.valueOf(contextHashCode));
        }
        return broadcastReceiver;
    }

    public void setLastReceiver(int contextHashCode, BroadcastReceiver lastReceiver) {
        synchronized (this.mLastReceivers) {
            this.mLastReceivers.put(Integer.valueOf(contextHashCode), lastReceiver);
        }
    }

    public static void unregisterReceiver(Context context, BroadcastReceiver receiver) {
        if (context != null && receiver != null) {
            try {
                context.unregisterReceiver(receiver);
            } catch (Throwable e) {
                LogManager.e("XIAOYUAN", "BroadcastReceiverUtils unregisterReceiver error:" + e.getMessage(), e);
            }
        }
    }

    public static void registerReceiver(Context context, BroadcastReceiver broadcastReceiver, String action, int priority, String permission) {
        if (context != null && broadcastReceiver != null) {
            try {
                IntentFilter filter = new IntentFilter();
                filter.addAction(action);
                filter.setPriority(priority);
                context.registerReceiver(broadcastReceiver, filter, permission, null);
            } catch (Throwable e) {
                LogManager.e("XIAOYUAN", "BroadcastReceiverUtils: registerReceiver : " + e.getMessage(), e);
            }
        }
    }

    public void reRegisterReceiver(Context context, String receiverKey, ReceiverInterface receiver, BroadcastReceiver cacheReceiver, String action, String permission) {
        if (context != null && receiverKey != null && receiver != null) {
            try {
                if (inWorkingReceiver(context.hashCode(), receiverKey)) {
                    BroadcastReceiver lastReceiver = getLastReceiver(context.hashCode());
                    if (lastReceiver == null || lastReceiver != cacheReceiver) {
                        unregisterReceiver(context, lastReceiver);
                        registerReceiver(context, receiver.getReceiver(), action, getReceiverPriority(), permission);
                        setLastReceiver(context.hashCode(), receiver.getReceiver());
                    }
                } else {
                    unregisterReceiver(context, cacheReceiver);
                }
            } catch (Throwable e) {
                LogManager.e("XIAOYUAN", "BroadcastReceiverUtils reRegisterReceiver error:" + e.getMessage(), e);
            }
        }
    }

    public void register(Context context, String receiverKey, ReceiverInterface receiver, String action, String permission) {
        if (context != null && receiverKey != null && receiver != null) {
            addWorkingReceiver(context.hashCode(), receiverKey);
            unregisterReceiver(context, getLastReceiver(context.hashCode()));
            registerReceiver(context, receiver.getReceiver(), action, getReceiverPriority(), permission);
            setLastReceiver(context.hashCode(), receiver.getReceiver());
        }
    }
}
