package com.google.android.gms.maps;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.dynamic.zze;
import com.google.android.gms.dynamic.zzf;
import com.google.android.gms.maps.internal.IStreetViewPanoramaDelegate;
import com.google.android.gms.maps.internal.IStreetViewPanoramaViewDelegate;
import com.google.android.gms.maps.internal.StreetViewLifecycleDelegate;
import com.google.android.gms.maps.internal.zzad;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class StreetViewPanoramaView extends FrameLayout {
    private final zzb zzaSK;
    private StreetViewPanorama zzaSy;

    /* compiled from: Unknown */
    static class zza implements StreetViewLifecycleDelegate {
        private final IStreetViewPanoramaViewDelegate zzaSL;
        private View zzaSM;
        private final ViewGroup zzaSk;

        public zza(ViewGroup viewGroup, IStreetViewPanoramaViewDelegate iStreetViewPanoramaViewDelegate) {
            this.zzaSL = (IStreetViewPanoramaViewDelegate) zzx.zzz(iStreetViewPanoramaViewDelegate);
            this.zzaSk = (ViewGroup) zzx.zzz(viewGroup);
        }

        public void getStreetViewPanoramaAsync(final OnStreetViewPanoramaReadyCallback callback) {
            try {
                this.zzaSL.getStreetViewPanoramaAsync(new com.google.android.gms.maps.internal.zzaa.zza(this) {
                    final /* synthetic */ zza zzaSN;

                    public void zza(IStreetViewPanoramaDelegate iStreetViewPanoramaDelegate) throws RemoteException {
                        callback.onStreetViewPanoramaReady(new StreetViewPanorama(iStreetViewPanoramaDelegate));
                    }
                });
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            try {
                this.zzaSL.onCreate(savedInstanceState);
                this.zzaSM = (View) zze.zzp(this.zzaSL.getView());
                this.zzaSk.removeAllViews();
                this.zzaSk.addView(this.zzaSM);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            throw new UnsupportedOperationException("onCreateView not allowed on StreetViewPanoramaViewDelegate");
        }

        public void onDestroy() {
            try {
                this.zzaSL.onDestroy();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroyView() {
            throw new UnsupportedOperationException("onDestroyView not allowed on StreetViewPanoramaViewDelegate");
        }

        public void onInflate(Activity activity, Bundle attrs, Bundle savedInstanceState) {
            throw new UnsupportedOperationException("onInflate not allowed on StreetViewPanoramaViewDelegate");
        }

        public void onLowMemory() {
            try {
                this.zzaSL.onLowMemory();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onPause() {
            try {
                this.zzaSL.onPause();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onResume() {
            try {
                this.zzaSL.onResume();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            try {
                this.zzaSL.onSaveInstanceState(outState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onStart() {
        }

        public void onStop() {
        }

        public IStreetViewPanoramaViewDelegate zzAd() {
            return this.zzaSL;
        }
    }

    /* compiled from: Unknown */
    static class zzb extends com.google.android.gms.dynamic.zza<zza> {
        private final Context mContext;
        private final List<OnStreetViewPanoramaReadyCallback> zzaSC = new ArrayList();
        private final StreetViewPanoramaOptions zzaSO;
        protected zzf<zza> zzaSh;
        private final ViewGroup zzaSo;

        zzb(ViewGroup viewGroup, Context context, StreetViewPanoramaOptions streetViewPanoramaOptions) {
            this.zzaSo = viewGroup;
            this.mContext = context;
            this.zzaSO = streetViewPanoramaOptions;
        }

        public void getStreetViewPanoramaAsync(OnStreetViewPanoramaReadyCallback callback) {
            if (zztU() == null) {
                this.zzaSC.add(callback);
            } else {
                ((zza) zztU()).getStreetViewPanoramaAsync(callback);
            }
        }

        protected void zza(zzf<zza> zzf) {
            this.zzaSh = zzf;
            zzzW();
        }

        public void zzzW() {
            if (this.zzaSh != null && zztU() == null) {
                try {
                    this.zzaSh.zza(new zza(this.zzaSo, zzad.zzaO(this.mContext).zza(zze.zzC(this.mContext), this.zzaSO)));
                    for (OnStreetViewPanoramaReadyCallback streetViewPanoramaAsync : this.zzaSC) {
                        ((zza) zztU()).getStreetViewPanoramaAsync(streetViewPanoramaAsync);
                    }
                    this.zzaSC.clear();
                } catch (RemoteException e) {
                    throw new RuntimeRemoteException(e);
                } catch (GooglePlayServicesNotAvailableException e2) {
                }
            }
        }
    }

    public StreetViewPanoramaView(Context context) {
        super(context);
        this.zzaSK = new zzb(this, context, null);
    }

    public StreetViewPanoramaView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.zzaSK = new zzb(this, context, null);
    }

    public StreetViewPanoramaView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.zzaSK = new zzb(this, context, null);
    }

    public StreetViewPanoramaView(Context context, StreetViewPanoramaOptions options) {
        super(context);
        this.zzaSK = new zzb(this, context, options);
    }

    @Deprecated
    public final StreetViewPanorama getStreetViewPanorama() {
        if (this.zzaSy != null) {
            return this.zzaSy;
        }
        this.zzaSK.zzzW();
        if (this.zzaSK.zztU() == null) {
            return null;
        }
        try {
            this.zzaSy = new StreetViewPanorama(((zza) this.zzaSK.zztU()).zzAd().getStreetViewPanorama());
            return this.zzaSy;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void getStreetViewPanoramaAsync(OnStreetViewPanoramaReadyCallback callback) {
        zzx.zzcD("getStreetViewPanoramaAsync() must be called on the main thread");
        this.zzaSK.getStreetViewPanoramaAsync(callback);
    }

    public final void onCreate(Bundle savedInstanceState) {
        this.zzaSK.onCreate(savedInstanceState);
        if (this.zzaSK.zztU() == null) {
            com.google.android.gms.dynamic.zza.zzb((FrameLayout) this);
        }
    }

    public final void onDestroy() {
        this.zzaSK.onDestroy();
    }

    public final void onLowMemory() {
        this.zzaSK.onLowMemory();
    }

    public final void onPause() {
        this.zzaSK.onPause();
    }

    public final void onResume() {
        this.zzaSK.onResume();
    }

    public final void onSaveInstanceState(Bundle outState) {
        this.zzaSK.onSaveInstanceState(outState);
    }
}
