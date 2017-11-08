package com.huawei.keyguard.events;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.os.Handler;
import android.os.UserHandle;
import android.provider.Settings.System;
import android.util.SparseArray;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.database.ClientHelper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.util.OsUtils;
import java.util.HashSet;

public class EventCenter {
    private static EventCenter sEventCenter = new EventCenter();
    private SlotHashSet<IContentListener> mContentListeners = new SlotHashSet(4);
    private SlotHashSet<IEventListener> mEventLiseners = new SlotHashSet(7);

    public interface IContentListener {
        void onContentChange(boolean z);
    }

    public interface IEventListener extends MessageCenter$IEventReceiver {
    }

    private class ContentChangeObserver extends ContentObserver implements Runnable {
        private boolean mSelfChange = false;
        private int mSlot = 0;

        public ContentChangeObserver(int slot) {
            super(GlobalContext.getBackgroundHandler());
            this.mSlot = slot;
        }

        public void onChange(boolean selfChange) {
            if (EventCenter.this.mContentListeners.hasListener(this.mSlot)) {
                HwLog.i("EventCenter", "ContentChange for slot: " + this.mSlot);
                Handler mHandler = GlobalContext.getUIHandler();
                this.mSelfChange = selfChange;
                mHandler.removeCallbacks(this);
                mHandler.postDelayed(this, 500);
                return;
            }
            HwLog.i("EventCenter", "ContentChange skiped for slot :" + this.mSlot);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            synchronized (EventCenter.this.mContentListeners) {
                HashSet<IContentListener> sets = (HashSet) EventCenter.this.mContentListeners.get(this.mSlot);
                if (sets == null || sets.size() == 0) {
                } else {
                    Object[] listeners = sets.toArray();
                }
            }
        }
    }

    private class EventReceiver extends BroadcastReceiver {
        private int mEventToTrigger;

        private EventReceiver(int event) {
            this.mEventToTrigger = 0;
            this.mEventToTrigger = event;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void onReceive(Context context, Intent intent) {
            if (intent == null) {
                HwLog.e("EventCenter", "Get empty event");
                return;
            }
            HwLog.i("EventCenter", "EventCenter Get :" + intent.getAction());
            synchronized (EventCenter.this.mEventLiseners) {
                HashSet<IEventListener> sets = (HashSet) EventCenter.this.mEventLiseners.get(this.mEventToTrigger);
                if (sets == null || sets.size() == 0) {
                } else {
                    Object[] listeners = sets.toArray();
                }
            }
        }
    }

    private static class SlotHashSet<T> extends SparseArray<HashSet<T>> {
        private final int max_slot;

        private SlotHashSet(int slotsize) {
            this.max_slot = slotsize;
        }

        private void addListener(int event, T listener) {
            synchronized (this) {
                for (int isr = 0; isr < this.max_slot; isr++) {
                    if (((1 << isr) & event) != 0) {
                        addListenerToSlot(1 << isr, listener);
                    }
                }
            }
        }

        private void removeListener(T listener) {
            synchronized (this) {
                int size = size();
                for (int idx = 0; idx < size; idx++) {
                    ((HashSet) valueAt(idx)).remove(listener);
                }
            }
        }

        private void addListenerToSlot(int event, T listener) {
            HashSet hashSet = (HashSet) get(event);
            if (hashSet == null) {
                hashSet = new HashSet();
                put(event, hashSet);
            }
            hashSet.add(listener);
        }

        private boolean hasListener(int slot) {
            boolean z = false;
            synchronized (this) {
                HashSet<T> set = (HashSet) get(slot);
                if (set != null && set.size() > 0) {
                    z = true;
                }
            }
            return z;
        }
    }

    public static final EventCenter getInst() {
        return sEventCenter;
    }

    public void listen(int event, IEventListener listener) {
        this.mEventLiseners.addListener(event, listener);
    }

    public void stopListen(IEventListener listener) {
        this.mEventLiseners.removeListener(listener);
    }

    public void listenContent(int event, IContentListener listener) {
        this.mContentListeners.addListener(event, listener);
    }

    public void stopListenContent(IContentListener listener) {
        this.mContentListeners.removeListener(listener);
    }

    public void init(Context context) {
        registCommonEvent(context);
        registWeatherEvent(context);
        registStepCounterShowEvent(context);
        registPackagesEvent(context);
        registThemeChangeEvent(context);
        registSportInfoEvent(context);
        registSDCardEvent(context);
        registContentObserver(context);
        registSingleHandObserver(context);
        registMagazineObserver(context);
        registUpdateStateObserver(context);
    }

    private void registCommonEvent(Context context) {
        IntentFilter filter = new IntentFilter("android.intent.action.TIME_TICK");
        filter.addAction("android.intent.action.TIME_SET");
        filter.addAction("android.intent.action.DATE_CHANGED");
        filter.addAction("android.intent.action.TIMEZONE_CHANGED");
        context.registerReceiverAsUser(new EventReceiver(1), UserHandle.CURRENT_OR_SELF, filter, null, null);
    }

    private void registWeatherEvent(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.android.action.WEATHER_CHANGE");
        filter.addAction("com.huawei.android.action.TEMPERATURE_FORMAT_CHANGE");
        filter.addAction("com.huawei.android.action.CITYINFO_CHANGE");
        filter.addAction("com.huawei.android.action.CITYINFO_ADD");
        filter.addAction("com.huawei.android.action.CITYINFO_DELETE");
        filter.addAction("com.huawei.android.action.WIDGET_CHANGE");
        context.registerReceiverAsUser(new EventReceiver(16), UserHandle.ALL, filter, null, null);
    }

    private void registSDCardEvent(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.MEDIA_SHARED");
        filter.addAction("android.intent.action.MEDIA_MOUNTED");
        filter.addAction("android.intent.action.MEDIA_EJECT");
        filter.addDataScheme("file");
        context.registerReceiverAsUser(new EventReceiver(64), UserHandle.ALL, filter, null, null);
    }

    private void registStepCounterShowEvent(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.health.ENABLE_STEP_INFO_SHOW");
        filter.addAction("android.com.huawei.bone.NOTIFY_SPORT_DATA");
        context.registerReceiverAsUser(new EventReceiver(2), UserHandle.OWNER, filter, "com.android.keyguard.permission.SEND_STEP_INFO_COUNTER", null);
    }

    private void registSportInfoEvent(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("com.huawei.healthcloud.plugintrack.NOTIFY_SPORT_DATA");
        context.registerReceiverAsUser(new EventReceiver(32), UserHandle.OWNER, filter, "com.android.keyguard.permission.SEND_STEP_INFO_COUNTER", null);
    }

    private void registPackagesEvent(Context context) {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.PACKAGE_ADDED");
        filter.addAction("android.intent.action.PACKAGE_REMOVED");
        filter.addDataScheme("package");
        context.registerReceiverAsUser(new EventReceiver(8), UserHandle.OWNER, filter, null, null);
    }

    private void registThemeChangeEvent(Context context) {
        IntentFilter filter = new IntentFilter("com.huawei.android.thememanager.applytheme");
        filter.addAction("com.huawei.android.thememanager.keyguard_style_changed");
        context.registerReceiverAsUser(new EventReceiver(4), UserHandle.ALL, filter, null, null);
    }

    private void registContentObserver(Context context) {
        HwLog.w("EventCenter", "registContentObserver");
        OsUtils.registerContentObserver(context, System.CONTENT_URI, true, new ContentChangeObserver(1));
    }

    private void registSingleHandObserver(Context context) {
        HwLog.w("EventCenter", "registSingleHandObserver");
        ContentObserver ob = new ContentChangeObserver(2);
        OsUtils.registerSystemObserver(context, "single_hand_switch", false, ob);
        OsUtils.registerSystemObserver(context, "single_hand_mode", false, ob);
    }

    private void registMagazineObserver(Context context) {
        HwLog.w("EventCenter", "registMagazineObserver");
        context.getContentResolver().registerContentObserver(ClientHelper.CONTENT_URI_PICTURES, false, new ContentChangeObserver(4), 0);
    }

    private void registUpdateStateObserver(Context context) {
        HwLog.w("EventCenter", "registUpdateStateObserver");
        context.getContentResolver().registerContentObserver(ClientHelper.CONTENT_URI_COMMON, false, new ContentChangeObserver(8), 0);
    }
}
