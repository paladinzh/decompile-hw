package com.google.android.gms.common.api.internal;

import android.app.Dialog;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.annotation.KeepName;
import com.google.android.gms.common.zzc;

@KeepName
/* compiled from: Unknown */
public class SupportLifecycleFragmentImpl extends zzw {
    protected void zzb(int i, ConnectionResult connectionResult) {
        GooglePlayServicesUtil.showErrorDialogFragment(connectionResult.getErrorCode(), getActivity(), this, 2, this);
    }

    protected void zzc(int i, ConnectionResult connectionResult) {
        final Dialog zza = zzpS().zza(getActivity(), this);
        this.zzaiD = zzn.zza(getActivity().getApplicationContext(), new zzn(this) {
            final /* synthetic */ SupportLifecycleFragmentImpl zzaiM;

            protected void zzpJ() {
                this.zzaiM.zzpP();
                zza.dismiss();
            }
        });
    }

    protected /* synthetic */ zzc zzpQ() {
        return zzpS();
    }

    protected GoogleApiAvailability zzpS() {
        return GoogleApiAvailability.getInstance();
    }
}
