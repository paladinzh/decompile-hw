package com.android.systemui.statusbar.policy;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.util.Log;
import com.android.systemui.statusbar.policy.HotspotController.Callback;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;

public class HotspotControllerImpl implements HotspotController {
    private static final boolean DEBUG = Log.isLoggable("HotspotController", 3);
    private final ArrayList<Callback> mCallbacks = new ArrayList();
    private final ConnectivityManager mConnectivityManager;
    private final Context mContext;
    private int mHotspotState;
    private final Receiver mReceiver = new Receiver();

    static final class OnStartTetheringCallback extends android.net.ConnectivityManager.OnStartTetheringCallback {
        OnStartTetheringCallback() {
        }

        public void onTetheringStarted() {
        }

        public void onTetheringFailed() {
        }
    }

    private final class Receiver extends BroadcastReceiver {
        private boolean mRegistered;

        private Receiver() {
        }

        public void setListening(boolean listening) {
            if (listening && !this.mRegistered) {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Registering receiver");
                }
                IntentFilter filter = new IntentFilter();
                filter.addAction("android.net.wifi.WIFI_AP_STATE_CHANGED");
                HotspotControllerImpl.this.mContext.registerReceiver(this, filter);
                this.mRegistered = true;
            } else if (!listening && this.mRegistered) {
                if (HotspotControllerImpl.DEBUG) {
                    Log.d("HotspotController", "Unregistering receiver");
                }
                HotspotControllerImpl.this.mContext.unregisterReceiver(this);
                this.mRegistered = false;
            }
        }

        public void onReceive(Context context, Intent intent) {
            boolean z;
            if (HotspotControllerImpl.DEBUG) {
                Log.d("HotspotController", "onReceive " + intent.getAction());
            }
            int state = intent.getIntExtra("wifi_state", 14);
            HotspotControllerImpl.this.mHotspotState = state;
            if (!(11 == state || 13 == state)) {
                if (14 != state) {
                    return;
                }
            }
            HotspotControllerImpl hotspotControllerImpl = HotspotControllerImpl.this;
            if (HotspotControllerImpl.this.mHotspotState == 13) {
                z = true;
            } else {
                z = false;
            }
            hotspotControllerImpl.fireCallback(z);
        }
    }

    public HotspotControllerImpl(Context context) {
        this.mContext = context;
        this.mConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("HotspotController state:");
        pw.print("  mHotspotEnabled=");
        pw.println(stateToString(this.mHotspotState));
    }

    private static String stateToString(int hotspotState) {
        switch (hotspotState) {
            case 10:
                return "DISABLING";
            case 11:
                return "DISABLED";
            case 12:
                return "ENABLING";
            case 13:
                return "ENABLED";
            case 14:
                return "FAILED";
            default:
                return null;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void addCallback(Callback callback) {
        synchronized (this.mCallbacks) {
            if (callback != null) {
                if (!this.mCallbacks.contains(callback)) {
                    boolean z;
                    if (DEBUG) {
                        Log.d("HotspotController", "addCallback " + callback);
                    }
                    this.mCallbacks.add(callback);
                    Receiver receiver = this.mReceiver;
                    if (this.mCallbacks.isEmpty()) {
                        z = false;
                    } else {
                        z = true;
                    }
                    receiver.setListening(z);
                }
            }
        }
    }

    public void removeCallback(Callback callback) {
        if (callback != null) {
            if (DEBUG) {
                Log.d("HotspotController", "removeCallback " + callback);
            }
            synchronized (this.mCallbacks) {
                boolean z;
                this.mCallbacks.remove(callback);
                Receiver receiver = this.mReceiver;
                if (this.mCallbacks.isEmpty()) {
                    z = false;
                } else {
                    z = true;
                }
                receiver.setListening(z);
            }
        }
    }

    public boolean isHotspotEnabled() {
        return this.mHotspotState == 13;
    }

    public void setHotspotEnabled(boolean enabled) {
        if (enabled) {
            this.mConnectivityManager.startTethering(0, false, new OnStartTetheringCallback());
            return;
        }
        this.mConnectivityManager.stopTethering(0);
    }

    private void fireCallback(boolean isEnabled) {
        synchronized (this.mCallbacks) {
            for (Callback callback : this.mCallbacks) {
                callback.onHotspotChanged(isEnabled);
            }
        }
    }
}
