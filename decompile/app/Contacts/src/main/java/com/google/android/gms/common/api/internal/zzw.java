package com.google.android.gms.common.api.internal;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.common.zzc;
import com.google.android.gms.common.zze;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* compiled from: Unknown */
public class zzw extends Fragment implements OnCancelListener {
    private boolean mStarted;
    private int zzaiA = -1;
    private ConnectionResult zzaiB;
    private final Handler zzaiC = new Handler(Looper.getMainLooper());
    protected zzn zzaiD;
    private final SparseArray<zza> zzaiE = new SparseArray();
    private boolean zzaiz;

    /* compiled from: Unknown */
    private class zza implements OnConnectionFailedListener {
        public final int zzaiF;
        public final GoogleApiClient zzaiG;
        public final OnConnectionFailedListener zzaiH;
        final /* synthetic */ zzw zzaiI;

        public zza(zzw zzw, int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
            this.zzaiI = zzw;
            this.zzaiF = i;
            this.zzaiG = googleApiClient;
            this.zzaiH = onConnectionFailedListener;
            googleApiClient.registerConnectionFailedListener(this);
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.append(prefix).append("GoogleApiClient #").print(this.zzaiF);
            writer.println(":");
            this.zzaiG.dump(prefix + "  ", fd, writer, args);
        }

        public void onConnectionFailed(@NonNull ConnectionResult result) {
            this.zzaiI.zzaiC.post(new zzb(this.zzaiI, this.zzaiF, result));
        }

        public void zzpR() {
            this.zzaiG.unregisterConnectionFailedListener(this);
            this.zzaiG.disconnect();
        }
    }

    /* compiled from: Unknown */
    private class zzb implements Runnable {
        final /* synthetic */ zzw zzaiI;
        private final int zzaiJ;
        private final ConnectionResult zzaiK;

        public zzb(zzw zzw, int i, ConnectionResult connectionResult) {
            this.zzaiI = zzw;
            this.zzaiJ = i;
            this.zzaiK = connectionResult;
        }

        @MainThread
        public void run() {
            if (this.zzaiI.mStarted && !this.zzaiI.zzaiz) {
                this.zzaiI.zzaiz = true;
                this.zzaiI.zzaiA = this.zzaiJ;
                this.zzaiI.zzaiB = this.zzaiK;
                if (this.zzaiK.hasResolution()) {
                    try {
                        this.zzaiK.startResolutionForResult(this.zzaiI.getActivity(), ((this.zzaiI.getActivity().getSupportFragmentManager().getFragments().indexOf(this.zzaiI) + 1) << 16) + 1);
                    } catch (SendIntentException e) {
                        this.zzaiI.zzpP();
                    }
                } else if (this.zzaiI.zzpQ().isUserResolvableError(this.zzaiK.getErrorCode())) {
                    this.zzaiI.zzb(this.zzaiJ, this.zzaiK);
                } else if (this.zzaiK.getErrorCode() != 18) {
                    this.zzaiI.zza(this.zzaiJ, this.zzaiK);
                } else {
                    this.zzaiI.zzc(this.zzaiJ, this.zzaiK);
                }
            }
        }
    }

    @Nullable
    public static zzw zza(FragmentActivity fragmentActivity) {
        zzx.zzcD("Must be called from main thread of process");
        try {
            zzw zzw = (zzw) fragmentActivity.getSupportFragmentManager().findFragmentByTag("GmsSupportLifecycleFrag");
            return (zzw == null || zzw.isRemoving()) ? null : zzw;
        } catch (Throwable e) {
            throw new IllegalStateException("Fragment with tag GmsSupportLifecycleFrag is not a SupportLifecycleFragment", e);
        }
    }

    private void zza(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLifecycleFrag", "Unresolved error while connecting client. Stopping auto-manage.");
        zza zza = (zza) this.zzaiE.get(i);
        if (zza != null) {
            zzbD(i);
            OnConnectionFailedListener onConnectionFailedListener = zza.zzaiH;
            if (onConnectionFailedListener != null) {
                onConnectionFailedListener.onConnectionFailed(connectionResult);
            }
        }
        zzpP();
    }

    public static zzw zzb(FragmentActivity fragmentActivity) {
        zzw zza = zza(fragmentActivity);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        if (zza == null) {
            zza = zzpO();
            if (zza == null) {
                Log.w("GmsSupportLifecycleFrag", "Unable to find connection error message resources (Did you include play-services-base and the proper proguard rules?); error dialogs may be unavailable.");
                zza = new zzw();
            }
            supportFragmentManager.beginTransaction().add(zza, "GmsSupportLifecycleFrag").commitAllowingStateLoss();
            supportFragmentManager.executePendingTransactions();
        }
        return zza;
    }

    private static String zzi(ConnectionResult connectionResult) {
        return connectionResult.getErrorMessage() + " (" + connectionResult.getErrorCode() + ": " + zze.getErrorString(connectionResult.getErrorCode()) + ')';
    }

    @Nullable
    private static zzw zzpO() {
        Class cls;
        try {
            cls = Class.forName("com.google.android.gms.common.api.internal.SupportLifecycleFragmentImpl");
        } catch (Throwable e) {
            if (Log.isLoggable("GmsSupportLifecycleFrag", 3)) {
                Log.d("GmsSupportLifecycleFrag", "Unable to find SupportLifecycleFragmentImpl class", e);
            }
            cls = null;
        }
        if (cls != null) {
            try {
                return (zzw) cls.newInstance();
            } catch (Throwable e2) {
                if (Log.isLoggable("GmsSupportLifecycleFrag", 3)) {
                    Log.d("GmsSupportLifecycleFrag", "Unable to instantiate SupportLifecycleFragmentImpl class", e2);
                }
            }
        }
        return null;
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        for (int i = 0; i < this.zzaiE.size(); i++) {
            ((zza) this.zzaiE.valueAt(i)).dump(prefix, fd, writer, args);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Object obj = null;
        switch (requestCode) {
            case 1:
                if (resultCode != -1) {
                    if (resultCode == 0) {
                        this.zzaiB = new ConnectionResult(13, null);
                        break;
                    }
                }
                break;
            case 2:
                break;
        }
        if (obj == null) {
            zza(this.zzaiA, this.zzaiB);
        } else {
            zzpP();
        }
    }

    public void onCancel(DialogInterface dialogInterface) {
        zza(this.zzaiA, new ConnectionResult(13, null));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.zzaiz = savedInstanceState.getBoolean("resolving_error", false);
            this.zzaiA = savedInstanceState.getInt("failed_client_id", -1);
            if (this.zzaiA >= 0) {
                this.zzaiB = new ConnectionResult(savedInstanceState.getInt("failed_status"), (PendingIntent) savedInstanceState.getParcelable("failed_resolution"));
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("resolving_error", this.zzaiz);
        if (this.zzaiA >= 0) {
            outState.putInt("failed_client_id", this.zzaiA);
            outState.putInt("failed_status", this.zzaiB.getErrorCode());
            outState.putParcelable("failed_resolution", this.zzaiB.getResolution());
        }
    }

    public void onStart() {
        super.onStart();
        this.mStarted = true;
        if (!this.zzaiz) {
            for (int i = 0; i < this.zzaiE.size(); i++) {
                ((zza) this.zzaiE.valueAt(i)).zzaiG.connect();
            }
        }
    }

    public void onStop() {
        super.onStop();
        this.mStarted = false;
        for (int i = 0; i < this.zzaiE.size(); i++) {
            ((zza) this.zzaiE.valueAt(i)).zzaiG.disconnect();
        }
    }

    public void zza(int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
        boolean z = false;
        zzx.zzb((Object) googleApiClient, (Object) "GoogleApiClient instance cannot be null");
        if (this.zzaiE.indexOfKey(i) < 0) {
            z = true;
        }
        zzx.zza(z, "Already managing a GoogleApiClient with id " + i);
        this.zzaiE.put(i, new zza(this, i, googleApiClient, onConnectionFailedListener));
        if (this.mStarted && !this.zzaiz) {
            googleApiClient.connect();
        }
    }

    protected void zzb(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLifecycleFrag", "Failed to connect due to user resolvable error " + zzi(connectionResult));
        zza(i, connectionResult);
    }

    public void zzbD(int i) {
        zza zza = (zza) this.zzaiE.get(i);
        this.zzaiE.remove(i);
        if (zza != null) {
            zza.zzpR();
        }
    }

    protected void zzc(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLifecycleFrag", "Unable to connect, GooglePlayServices is updating.");
        zza(i, connectionResult);
    }

    protected void zzpP() {
        this.zzaiz = false;
        this.zzaiA = -1;
        this.zzaiB = null;
        if (this.zzaiD != null) {
            this.zzaiD.unregister();
            this.zzaiD = null;
        }
        for (int i = 0; i < this.zzaiE.size(); i++) {
            ((zza) this.zzaiE.valueAt(i)).zzaiG.connect();
        }
    }

    protected zzc zzpQ() {
        return zzc.zzoK();
    }
}
