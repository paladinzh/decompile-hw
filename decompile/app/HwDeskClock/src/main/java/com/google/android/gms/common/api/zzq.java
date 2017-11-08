package com.google.android.gms.common.api;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
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
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.Log;
import android.util.SparseArray;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.internal.zzx;
import java.io.FileDescriptor;
import java.io.PrintWriter;

/* compiled from: Unknown */
public class zzq extends Fragment implements OnCancelListener, LoaderCallbacks<ConnectionResult> {
    private boolean zzaaK;
    private int zzaaL = -1;
    private ConnectionResult zzaaM;
    private final Handler zzaaN = new Handler(Looper.getMainLooper());
    private final SparseArray<zzb> zzaaO = new SparseArray();

    /* compiled from: Unknown */
    static class zza extends Loader<ConnectionResult> implements ConnectionCallbacks, OnConnectionFailedListener {
        public final GoogleApiClient zzaaQ;
        private boolean zzaaV;
        private ConnectionResult zzaaW;

        public zza(Context context, GoogleApiClient googleApiClient) {
            super(context);
            this.zzaaQ = googleApiClient;
        }

        private void zzh(ConnectionResult connectionResult) {
            this.zzaaW = connectionResult;
            if (isStarted() && !isAbandoned()) {
                deliverResult(connectionResult);
            }
        }

        public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
            super.dump(prefix, fd, writer, args);
            this.zzaaQ.dump(prefix, fd, writer, args);
        }

        public void onConnected(Bundle connectionHint) {
            this.zzaaV = false;
            zzh(ConnectionResult.zzYi);
        }

        public void onConnectionFailed(ConnectionResult result) {
            this.zzaaV = true;
            zzh(result);
        }

        public void onConnectionSuspended(int cause) {
        }

        protected void onReset() {
            this.zzaaW = null;
            this.zzaaV = false;
            this.zzaaQ.unregisterConnectionCallbacks(this);
            this.zzaaQ.unregisterConnectionFailedListener(this);
            this.zzaaQ.disconnect();
        }

        protected void onStartLoading() {
            super.onStartLoading();
            this.zzaaQ.registerConnectionCallbacks(this);
            this.zzaaQ.registerConnectionFailedListener(this);
            if (this.zzaaW != null) {
                deliverResult(this.zzaaW);
            }
            if (!this.zzaaQ.isConnected() && !this.zzaaQ.isConnecting() && !this.zzaaV) {
                this.zzaaQ.connect();
            }
        }

        protected void onStopLoading() {
            this.zzaaQ.disconnect();
        }

        public boolean zznM() {
            return this.zzaaV;
        }
    }

    /* compiled from: Unknown */
    private static class zzb {
        public final GoogleApiClient zzaaQ;
        public final OnConnectionFailedListener zzaaR;

        private zzb(GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
            this.zzaaQ = googleApiClient;
            this.zzaaR = onConnectionFailedListener;
        }
    }

    /* compiled from: Unknown */
    private class zzc implements Runnable {
        private final int zzaaT;
        private final ConnectionResult zzaaU;
        final /* synthetic */ zzq zzaaX;

        public zzc(zzq zzq, int i, ConnectionResult connectionResult) {
            this.zzaaX = zzq;
            this.zzaaT = i;
            this.zzaaU = connectionResult;
        }

        public void run() {
            if (this.zzaaU.hasResolution()) {
                try {
                    this.zzaaU.startResolutionForResult(this.zzaaX.getActivity(), ((this.zzaaX.getActivity().getSupportFragmentManager().getFragments().indexOf(this.zzaaX) + 1) << 16) + 1);
                } catch (SendIntentException e) {
                    this.zzaaX.zznK();
                }
            } else if (GooglePlayServicesUtil.isUserRecoverableError(this.zzaaU.getErrorCode())) {
                GooglePlayServicesUtil.showErrorDialogFragment(this.zzaaU.getErrorCode(), this.zzaaX.getActivity(), this.zzaaX, 2, this.zzaaX);
            } else {
                this.zzaaX.zza(this.zzaaT, this.zzaaU);
            }
        }
    }

    private void zza(int i, ConnectionResult connectionResult) {
        Log.w("GmsSupportLoaderLifecycleFragment", "Unresolved error while connecting client. Stopping auto-manage.");
        zzb zzb = (zzb) this.zzaaO.get(i);
        if (zzb != null) {
            zzbi(i);
            OnConnectionFailedListener onConnectionFailedListener = zzb.zzaaR;
            if (onConnectionFailedListener != null) {
                onConnectionFailedListener.onConnectionFailed(connectionResult);
            }
        }
        zznK();
    }

    private void zzb(int i, ConnectionResult connectionResult) {
        if (!this.zzaaK) {
            this.zzaaK = true;
            this.zzaaL = i;
            this.zzaaM = connectionResult;
            this.zzaaN.post(new zzc(this, i, connectionResult));
        }
    }

    public static zzq zzc(FragmentActivity fragmentActivity) {
        zzx.zzch("Must be called from main thread of process");
        FragmentManager supportFragmentManager = fragmentActivity.getSupportFragmentManager();
        try {
            zzq zzq = (zzq) supportFragmentManager.findFragmentByTag("GmsSupportLoaderLifecycleFragment");
            if (zzq != null && !zzq.isRemoving()) {
                return zzq;
            }
            Fragment zzq2 = new zzq();
            supportFragmentManager.beginTransaction().add(zzq2, "GmsSupportLoaderLifecycleFragment").commit();
            supportFragmentManager.executePendingTransactions();
            return zzq2;
        } catch (Throwable e) {
            throw new IllegalStateException("Fragment with tag GmsSupportLoaderLifecycleFragment is not a SupportLoaderLifecycleFragment", e);
        }
    }

    private void zznK() {
        int i = 0;
        this.zzaaK = false;
        this.zzaaL = -1;
        this.zzaaM = null;
        LoaderManager loaderManager = getLoaderManager();
        while (i < this.zzaaO.size()) {
            int keyAt = this.zzaaO.keyAt(i);
            zza zzbk = zzbk(keyAt);
            if (zzbk != null && zzbk.zznM()) {
                loaderManager.destroyLoader(keyAt);
                loaderManager.initLoader(keyAt, null, this);
            }
            i++;
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

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        int i = 0;
        while (i < this.zzaaO.size()) {
            int keyAt = this.zzaaO.keyAt(i);
            zza zzbk = zzbk(keyAt);
            if (zzbk == null || ((zzb) this.zzaaO.valueAt(i)).zzaaQ == zzbk.zzaaQ) {
                getLoaderManager().initLoader(keyAt, null, this);
            } else {
                getLoaderManager().restartLoader(keyAt, null, this);
            }
            i++;
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

    public Loader<ConnectionResult> onCreateLoader(int id, Bundle args) {
        return new zza(getActivity(), ((zzb) this.zzaaO.get(id)).zzaaQ);
    }

    public /* synthetic */ void onLoadFinished(Loader loader, Object obj) {
        zza(loader, (ConnectionResult) obj);
    }

    public void onLoaderReset(Loader<ConnectionResult> loader) {
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
        if (!this.zzaaK) {
            for (int i = 0; i < this.zzaaO.size(); i++) {
                getLoaderManager().initLoader(this.zzaaO.keyAt(i), null, this);
            }
        }
    }

    public void zza(int i, GoogleApiClient googleApiClient, OnConnectionFailedListener onConnectionFailedListener) {
        zzx.zzb((Object) googleApiClient, (Object) "GoogleApiClient instance cannot be null");
        zzx.zza(this.zzaaO.indexOfKey(i) < 0, "Already managing a GoogleApiClient with id " + i);
        this.zzaaO.put(i, new zzb(googleApiClient, onConnectionFailedListener));
        if (getActivity() != null) {
            LoaderManager.enableDebugLogging(false);
            getLoaderManager().initLoader(i, null, this);
        }
    }

    public void zza(Loader<ConnectionResult> loader, ConnectionResult connectionResult) {
        if (!connectionResult.isSuccess()) {
            zzb(loader.getId(), connectionResult);
        }
    }

    public void zzbi(int i) {
        this.zzaaO.remove(i);
        getLoaderManager().destroyLoader(i);
    }

    public GoogleApiClient zzbj(int i) {
        if (getActivity() != null) {
            zza zzbk = zzbk(i);
            if (zzbk != null) {
                return zzbk.zzaaQ;
            }
        }
        return null;
    }

    zza zzbk(int i) {
        try {
            return (zza) getLoaderManager().getLoader(i);
        } catch (Throwable e) {
            throw new IllegalStateException("Unknown loader in SupportLoaderLifecycleFragment", e);
        }
    }
}
