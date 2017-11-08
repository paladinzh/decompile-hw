package com.android.server.connectivity;

import android.app.PendingIntent;
import android.content.Context;
import android.net.ConnectivityMetricsEvent;
import android.net.ConnectivityMetricsEvent.Reference;
import android.net.IConnectivityMetricsLogger.Stub;
import android.os.Binder;
import android.os.Parcel;
import android.text.format.DateUtils;
import android.util.Log;
import com.android.server.SystemService;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;

public class MetricsLoggerService extends SystemService {
    private static final boolean DBG = true;
    private static String TAG = "ConnectivityMetricsLoggerService";
    private static final boolean VDBG = false;
    private final int EVENTS_NOTIFICATION_THRESHOLD = 300;
    private final int MAX_NUMBER_OF_EVENTS = 1000;
    private final int THROTTLING_MAX_NUMBER_OF_MESSAGES_PER_COMPONENT = 1000;
    private final int THROTTLING_TIME_INTERVAL_MILLIS = 3600000;
    private final Stub mBinder = new Stub() {
        private final ArrayList<PendingIntent> mPendingIntents = new ArrayList();

        protected void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            if (MetricsLoggerService.this.getContext().checkCallingOrSelfPermission("android.permission.DUMP") != 0) {
                pw.println("Permission Denial: can't dump ConnectivityMetricsLoggerService from from pid=" + Binder.getCallingPid() + ", uid=" + Binder.getCallingUid());
                return;
            }
            boolean dumpSerializedSize = false;
            boolean dumpEvents = false;
            boolean dumpDebugInfo = false;
            for (String arg : args) {
                if (arg.equals("--debug")) {
                    dumpDebugInfo = MetricsLoggerService.DBG;
                } else if (arg.equals("--events")) {
                    dumpEvents = MetricsLoggerService.DBG;
                } else if (arg.equals("--size")) {
                    dumpSerializedSize = MetricsLoggerService.DBG;
                } else if (arg.equals("--all")) {
                    dumpDebugInfo = MetricsLoggerService.DBG;
                    dumpEvents = MetricsLoggerService.DBG;
                    dumpSerializedSize = MetricsLoggerService.DBG;
                }
            }
            synchronized (MetricsLoggerService.this.mEvents) {
                pw.println("Number of events: " + MetricsLoggerService.this.mEvents.size());
                pw.println("Counter: " + MetricsLoggerService.this.mEventCounter);
                if (MetricsLoggerService.this.mEvents.size() > 0) {
                    pw.println("Time span: " + DateUtils.formatElapsedTime((System.currentTimeMillis() - ((ConnectivityMetricsEvent) MetricsLoggerService.this.mEvents.peekFirst()).timestamp) / 1000));
                }
                if (dumpSerializedSize) {
                    Parcel p = Parcel.obtain();
                    for (ConnectivityMetricsEvent e : MetricsLoggerService.this.mEvents) {
                        p.writeParcelable(e, 0);
                    }
                    pw.println("Serialized data size: " + p.dataSize());
                    p.recycle();
                }
                if (dumpEvents) {
                    pw.println();
                    pw.println("Events:");
                    for (ConnectivityMetricsEvent e2 : MetricsLoggerService.this.mEvents) {
                        pw.println(e2.toString());
                    }
                }
            }
            if (dumpDebugInfo) {
                synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                    pw.println();
                    for (int i = 0; i < 5; i++) {
                        if (MetricsLoggerService.this.mThrottlingCounters[i] > 0) {
                            pw.println("Throttling Counter #" + i + ": " + MetricsLoggerService.this.mThrottlingCounters[i]);
                        }
                    }
                    pw.println("Throttling Time Remaining: " + DateUtils.formatElapsedTime((MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis - System.currentTimeMillis()) / 1000));
                }
            }
            synchronized (this.mPendingIntents) {
                if (!this.mPendingIntents.isEmpty()) {
                    pw.println();
                    pw.println("Pending intents:");
                    for (PendingIntent pi : this.mPendingIntents) {
                        pw.println(pi.toString());
                    }
                }
            }
            pw.println();
            MetricsLoggerService.this.mDnsListener.dump(pw);
        }

        public long logEvent(ConnectivityMetricsEvent event) {
            return logEvents(new ConnectivityMetricsEvent[]{event});
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public long logEvents(ConnectivityMetricsEvent[] events) {
            MetricsLoggerService.this.enforceConnectivityInternalPermission();
            if (events == null || events.length == 0) {
                Log.wtf(MetricsLoggerService.TAG, "No events passed to logEvents()");
                return -1;
            }
            int componentTag = events[0].componentTag;
            if (componentTag < 0 || componentTag >= 5) {
                Log.wtf(MetricsLoggerService.TAG, "Unexpected tag: " + componentTag);
                return -1;
            }
            synchronized (MetricsLoggerService.this.mThrottlingCounters) {
                long currentTimeMillis = System.currentTimeMillis();
                if (currentTimeMillis > MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis) {
                    MetricsLoggerService.this.resetThrottlingCounters(currentTimeMillis);
                }
                int[] -get5 = MetricsLoggerService.this.mThrottlingCounters;
                -get5[componentTag] = -get5[componentTag] + events.length;
                if (MetricsLoggerService.this.mThrottlingCounters[componentTag] > 1000) {
                    Log.w(MetricsLoggerService.TAG, "Too many events from #" + componentTag + ". Block until " + MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis);
                    long -get6 = MetricsLoggerService.this.mThrottlingIntervalBoundaryMillis;
                    return -get6;
                }
            }
        }

        public ConnectivityMetricsEvent[] getEvents(Reference reference) {
            MetricsLoggerService.this.enforceDumpPermission();
            long ref = reference.getValue();
            synchronized (MetricsLoggerService.this.mEvents) {
                if (ref > MetricsLoggerService.this.mLastEventReference) {
                    Log.e(MetricsLoggerService.TAG, "Invalid reference");
                    reference.setValue(MetricsLoggerService.this.mLastEventReference);
                    return null;
                }
                if (ref < MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size())) {
                    ref = MetricsLoggerService.this.mLastEventReference - ((long) MetricsLoggerService.this.mEvents.size());
                }
                int numEventsToSkip = MetricsLoggerService.this.mEvents.size() - ((int) (MetricsLoggerService.this.mLastEventReference - ref));
                ConnectivityMetricsEvent[] result = new ConnectivityMetricsEvent[(MetricsLoggerService.this.mEvents.size() - numEventsToSkip)];
                int i = 0;
                for (ConnectivityMetricsEvent e : MetricsLoggerService.this.mEvents) {
                    int i2;
                    if (numEventsToSkip > 0) {
                        numEventsToSkip--;
                        i2 = i;
                    } else {
                        i2 = i + 1;
                        result[i] = e;
                    }
                    i = i2;
                }
                reference.setValue(MetricsLoggerService.this.mLastEventReference);
                return result;
            }
        }

        public boolean register(PendingIntent newEventsIntent) {
            MetricsLoggerService.this.enforceDumpPermission();
            synchronized (this.mPendingIntents) {
                if (this.mPendingIntents.remove(newEventsIntent)) {
                    Log.w(MetricsLoggerService.TAG, "Replacing registered pending intent");
                }
                this.mPendingIntents.add(newEventsIntent);
            }
            return MetricsLoggerService.DBG;
        }

        public void unregister(PendingIntent newEventsIntent) {
            MetricsLoggerService.this.enforceDumpPermission();
            synchronized (this.mPendingIntents) {
                if (!this.mPendingIntents.remove(newEventsIntent)) {
                    Log.e(MetricsLoggerService.TAG, "Pending intent is not registered");
                }
            }
        }
    };
    private DnsEventListenerService mDnsListener;
    private int mEventCounter = 0;
    private final ArrayDeque<ConnectivityMetricsEvent> mEvents = new ArrayDeque();
    private long mLastEventReference = 0;
    private final int[] mThrottlingCounters = new int[5];
    private long mThrottlingIntervalBoundaryMillis;

    public MetricsLoggerService(Context context) {
        super(context);
    }

    public void onStart() {
        resetThrottlingCounters(System.currentTimeMillis());
    }

    public void onBootPhase(int phase) {
        if (phase == SystemService.PHASE_SYSTEM_SERVICES_READY) {
            Log.d(TAG, "onBootPhase: PHASE_SYSTEM_SERVICES_READY");
            publishBinderService("connectivity_metrics_logger", this.mBinder);
            this.mDnsListener = new DnsEventListenerService(getContext());
            publishBinderService(DnsEventListenerService.SERVICE_NAME, this.mDnsListener);
        }
    }

    private void enforceConnectivityInternalPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.CONNECTIVITY_INTERNAL", "MetricsLoggerService");
    }

    private void enforceDumpPermission() {
        getContext().enforceCallingOrSelfPermission("android.permission.DUMP", "MetricsLoggerService");
    }

    private void resetThrottlingCounters(long currentTimeMillis) {
        synchronized (this.mThrottlingCounters) {
            for (int i = 0; i < this.mThrottlingCounters.length; i++) {
                this.mThrottlingCounters[i] = 0;
            }
            this.mThrottlingIntervalBoundaryMillis = 3600000 + currentTimeMillis;
        }
    }

    private void addEvent(ConnectivityMetricsEvent e) {
        while (this.mEvents.size() >= 1000) {
            this.mEvents.removeFirst();
        }
        this.mEvents.addLast(e);
    }
}
