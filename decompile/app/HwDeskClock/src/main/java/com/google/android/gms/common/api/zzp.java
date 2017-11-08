package com.google.android.gms.common.api;

import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* compiled from: Unknown */
public class zzp extends Fragment implements OnCancelListener {
    private boolean mStarted;
    private boolean zzaaK;
    private int zzaaL = -1;
    private ConnectionResult zzaaM;
    private final Handler zzaaN = new Handler(Looper.getMainLooper());
    private final SparseArray<zza> zzaaO = new SparseArray();

    /* compiled from: Unknown */
    private class zza implements OnConnectionFailedListener {
        public final int zzaaP;
        public final GoogleApiClient zzaaQ;
        public final OnConnectionFailedListener zzaaR;
        final /* synthetic */ zzp zzaaS;

        public zza(zzp zzp, int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
            this.zzaaS = zzp;
            this.zzaaP = i;
            this.zzaaQ = googleApiClient;
            this.zzaaR = onConnectionFailedListener;
            googleApiClient.registerConnectionFailedListener(this);
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            writer.append(prefix).append("GoogleApiClient #").print(this.zzaaP);
            writer.println(":");
            this.zzaaQ.dump(prefix + "  ", fd, writer, args);
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzaaS.zzaaN.post(new zzb(this.zzaaS, this.zzaaP, result));
        }

        public void zznL() {
            this.zzaaQ.unregisterConnectionFailedListener(this);
            this.zzaaQ.disconnect();
        }
    }

    /* compiled from: Unknown */
    private class zzb implements Runnable {
        final /* synthetic */ zzp zzaaS;
        private final int zzaaT;
        private final ConnectionResult zzaaU;

        public zzb(zzp zzp, int i, ConnectionResult connectionResult) {
            this.zzaaS = zzp;
            this.zzaaT = i;
            this.zzaaU = connectionResult;
        }

        public void run() {
            if (this.zzaaS.mStarted && !this.zzaaS.zzaaK) {
                this.zzaaS.zzaaK = true;
                this.zzaaS.zzaaL = this.zzaaT;
                this.zzaaS.zzaaM = this.zzaaU;
                if (this.zzaaU.hasResolution()) {
                    try {
                        this.zzaaU.startResolutionForResult(this.zzaaS.getActivity(), ((this.zzaaS.getActivity().getSupportFragmentManager().getFragments().indexOf(this.zzaaS) + 1) << 16) + 1);
                    } catch (SendIntentException e) {
                        this.zzaaS.zznK();
                    }
                } else if (GooglePlayServicesUtil.isUserRecoverableError(this.zzaaU.getErrorCode())) {
                    GooglePlayServicesUtil.showErrorDialogFragment(this.zzaaU.getErrorCode(), this.zzaaS.getActivity(), this.zzaaS, 2, this.zzaaS);
                } else {
                    this.zzaaS.zza(this.zzaaT, this.zzaaU);
                }
            }
        }
    }

    public static zzp zza(FragmentActivity fragmentActivity) {
        zzx.zzch("Must be called from main thread of process");
        try {
            zzp zzp = (zzp) fragmentActivity.getSupportFragmentManager().findFragmentByTag("GmsSupportLifecycleFragment");
            return (zzp == null || zzp.isRemoving()) ? null : zzp;
        } catch (Throwable e) {
            throw new IllegalStateException("Fragment with tag GmsSupportLifecycleFragment is not a SupportLifecycleFragment", e);
        }
    }

    private void zza(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLifecycleFragment", "Unresolved error while connecting client. Stopping auto-manage.");
        zza zza = (zza) this.zzaaO.get(i);
        if (zza != null) {
            zzbi(i);
            OnConnectionFailedListener onConnectionFailedListener = zza.zzaaR;
            if (onConnectionFailedListener != null) {
                onConnectionFailedListener.onConnectionFailed(connectionResult);
            }
        }
        zznK();
    }

    public static zzp zzb(FragmentActivity fragmentActivity) {
        zzp zza = zza(fragmentActivity);
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        if (zza != null) {
            return zza;
        }
        Fragment zzp = new zzp();
        supportFragmentManager.beginTransaction().add(zzp, "GmsSupportLifecycleFragment").commitAllowingStateLoss();
        supportFragmentManager.executePendingTransactions();
        return zzp;
    }

    private void zznK() {
        this.zzaaK = false;
        this.zzaaL = -1;
        this.zzaaM = null;
        for (int i = 0; i < this.zzaaO.size(); i++) {
            ((zza) this.zzaaO.valueAt(i)).zzaaQ.connect();
        }
    }

    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        for (int i = 0; i < this.zzaaO.size(); i++) {
            ((zza) this.zzaaO.valueAt(i)).dump(prefix, fd, writer, args);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Object obj = null;
        switch (requestCode) {
            case 1:
                if (resultCode != -1) {
                    break;
                }
            case 2:
                break;
        }
        if (obj == null) {
            zza(this.zzaaL, this.zzaaM);
        } else {
            zznK();
        }
    }

    public void onCancel(DialogInterface dialogInterface) {
        zza(this.zzaaL, new ConnectionResult(13, null));
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.zzaaK = savedInstanceState.getBoolean("resolving_error", false);
            this.zzaaL = savedInstanceState.getInt("failed_client_id", -1);
            if (this.zzaaL >= 0) {
                this.zzaaM = new ConnectionResult(savedInstanceState.getInt("failed_status"), (PendingIntent) savedInstanceState.getParcelable("failed_resolution"));
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("resolving_error", this.zzaaK);
        if (this.zzaaL >= 0) {
            outState.putInt("failed_client_id", this.zzaaL);
            outState.putInt("failed_status", this.zzaaM.getErrorCode());
            outState.putParcelable("failed_resolution", this.zzaaM.getResolution());
        }
    }

    public void onStart() {
        super.onStart();
        this.mStarted = true;
        if (!this.zzaaK) {
            for (int i = 0; i < this.zzaaO.size(); i++) {
                ((zza) this.zzaaO.valueAt(i)).zzaaQ.connect();
            }
        }
    }

    public void onStop() {
        super.onStop();
        this.mStarted = false;
        for (int i = 0; i < this.zzaaO.size(); i++) {
            ((zza) this.zzaaO.valueAt(i)).zzaaQ.disconnect();
        }
    }

    public void zza(int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
        boolean z = false;
        zzx.zzb((Object) googleApiClient, (Object) "GoogleApiClient instance cannot be null");
        if (this.zzaaO.indexOfKey(i) < 0) {
            z = true;
        }
        zzx.zza(z, "Already managing a GoogleApiClient with id " + i);
        this.zzaaO.put(i, new zza(this, i, googleApiClient, onConnectionFailedListener));
        if (this.mStarted && !this.zzaaK) {
            googleApiClient.connect();
        }
    }

    public void zzbi(int i) {
        zza zza = (zza) this.zzaaO.get(i);
        this.zzaaO.remove(i);
        if (zza != null) {
            zza.zznL();
        }
    }
}
