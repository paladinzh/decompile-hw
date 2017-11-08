package com.android.server.connectivity;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.net.metrics.DnsEvent;
import android.net.metrics.IDnsEventListener.Stub;
import android.util.Log;
import com.android.internal.annotations.GuardedBy;
import com.android.internal.telephony.HwTelephonyFactory;
import com.android.internal.util.IndentingPrintWriter;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

public class DnsEventListenerService extends Stub {
    private static final boolean DBG = true;
    private static final int MAX_LOOKUPS_PER_DNS_EVENT = 100;
    public static final String SERVICE_NAME = "dns_listener";
    private static final String TAG = DnsEventListenerService.class.getSimpleName();
    private static final boolean VDBG = false;
    private final ConnectivityManager mCm;
    private Context mContext;
    @GuardedBy("this")
    private SortedMap<Integer, DnsEventBatch> mEventBatches = new TreeMap();
    private final NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onLost(Network network) {
            synchronized (DnsEventListenerService.this) {
                DnsEventBatch batch = (DnsEventBatch) DnsEventListenerService.this.mEventBatches.remove(Integer.valueOf(network.netId));
                if (batch != null) {
                    batch.logAndClear();
                }
            }
        }
    };

    private static class DnsEventBatch {
        private int mEventCount;
        private final byte[] mEventTypes = new byte[100];
        private final int[] mLatenciesMs = new int[100];
        private final int mNetId;
        private final byte[] mReturnCodes = new byte[100];

        public DnsEventBatch(int netId) {
            this.mNetId = netId;
        }

        public void addResult(byte eventType, byte returnCode, int latencyMs) {
            this.mEventTypes[this.mEventCount] = eventType;
            this.mReturnCodes[this.mEventCount] = returnCode;
            this.mLatenciesMs[this.mEventCount] = latencyMs;
            this.mEventCount++;
            if (this.mEventCount == 100) {
                logAndClear();
            }
        }

        public void logAndClear() {
            if (this.mEventCount != 0) {
                DnsEvent.logEvent(this.mNetId, Arrays.copyOf(this.mEventTypes, this.mEventCount), Arrays.copyOf(this.mReturnCodes, this.mEventCount), Arrays.copyOf(this.mLatenciesMs, this.mEventCount));
                DnsEventListenerService.maybeLog(String.format("Logging %d results for netId %d", new Object[]{Integer.valueOf(this.mEventCount), Integer.valueOf(this.mNetId)}));
                this.mEventCount = 0;
            }
        }

        public String toString() {
            return String.format("%s %d %d", new Object[]{getClass().getSimpleName(), Integer.valueOf(this.mNetId), Integer.valueOf(this.mEventCount)});
        }
    }

    public DnsEventListenerService(Context context) {
        NetworkRequest request = new Builder().clearCapabilities().build();
        this.mContext = context;
        this.mCm = (ConnectivityManager) context.getSystemService(ConnectivityManager.class);
        this.mCm.registerNetworkCallback(request, this.mNetworkCallback);
    }

    public synchronized void onDnsEvent(int netId, int eventType, int returnCode, int latencyMs) {
        maybeVerboseLog(String.format("onDnsEvent(%d, %d, %d, %d)", new Object[]{Integer.valueOf(netId), Integer.valueOf(eventType), Integer.valueOf(returnCode), Integer.valueOf(latencyMs)}));
        DnsEventBatch batch = (DnsEventBatch) this.mEventBatches.get(Integer.valueOf(netId));
        if (batch == null) {
            batch = new DnsEventBatch(netId);
            this.mEventBatches.put(Integer.valueOf(netId), batch);
        }
        batch.addResult((byte) eventType, (byte) returnCode, latencyMs);
        if (this.mContext != null) {
            HwTelephonyFactory.getHwDataServiceChrManager().onDnsEvent(this.mContext, returnCode, latencyMs);
        }
    }

    public synchronized void dump(PrintWriter writer) {
        IndentingPrintWriter pw = new IndentingPrintWriter(writer, "  ");
        pw.println(TAG + ":");
        pw.increaseIndent();
        for (DnsEventBatch batch : this.mEventBatches.values()) {
            pw.println(batch.toString());
        }
        pw.decreaseIndent();
    }

    private static void maybeLog(String s) {
        Log.d(TAG, s);
    }

    private static void maybeVerboseLog(String s) {
    }
}
