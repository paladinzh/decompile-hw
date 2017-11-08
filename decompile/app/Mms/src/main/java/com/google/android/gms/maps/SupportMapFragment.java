package com.google.android.gms.maps;

import android.app.Activity;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.internal.zzx;
import com.google.android.gms.dynamic.zze;
import com.google.android.gms.dynamic.zzf;
import com.google.android.gms.maps.internal.IGoogleMapDelegate;
import com.google.android.gms.maps.internal.IMapFragmentDelegate;
import com.google.android.gms.maps.internal.MapLifecycleDelegate;
import com.google.android.gms.maps.internal.zzac;
import com.google.android.gms.maps.internal.zzad;
import com.google.android.gms.maps.model.RuntimeRemoteException;
import java.util.ArrayList;
import java.util.List;

/* compiled from: Unknown */
public class SupportMapFragment extends Fragment {
    private final zzb zzaSP = new zzb(this);
    private GoogleMap zzaSd;

    /* compiled from: Unknown */
    static class zza implements MapLifecycleDelegate {
        private final IMapFragmentDelegate zzaSe;
        private final Fragment zzalg;

        public zza(Fragment fragment, IMapFragmentDelegate iMapFragmentDelegate) {
            this.zzaSe = (IMapFragmentDelegate) zzx.zzz(iMapFragmentDelegate);
            this.zzalg = (Fragment) zzx.zzz(fragment);
        }

        public void getMapAsync(final OnMapReadyCallback callback) {
            try {
                this.zzaSe.getMapAsync(new com.google.android.gms.maps.internal.zzo.zza(this) {
                    final /* synthetic */ zza zzaSQ;

                    public void zza(IGoogleMapDelegate iGoogleMapDelegate) throws RemoteException {
                        callback.onMapReady(new GoogleMap(iGoogleMapDelegate));
                    }
                });
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onCreate(Bundle savedInstanceState) {
            if (savedInstanceState == null) {
                savedInstanceState = new Bundle();
            }
            try {
                Bundle arguments = this.zzalg.getArguments();
                if (arguments != null && arguments.containsKey("MapOptions")) {
                    zzac.zza(savedInstanceState, "MapOptions", arguments.getParcelable("MapOptions"));
                }
                this.zzaSe.onCreate(savedInstanceState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            try {
                return (View) zze.zzp(this.zzaSe.onCreateView(zze.zzC(inflater), zze.zzC(container), savedInstanceState));
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroy() {
            try {
                this.zzaSe.onDestroy();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onDestroyView() {
            try {
                this.zzaSe.onDestroyView();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onEnterAmbient(Bundle ambientDetails) {
            try {
                this.zzaSe.onEnterAmbient(ambientDetails);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onExitAmbient() {
            try {
                this.zzaSe.onExitAmbient();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onInflate(Activity activity, Bundle attrs, Bundle savedInstanceState) {
            try {
                this.zzaSe.onInflate(zze.zzC(activity), (GoogleMapOptions) attrs.getParcelable("MapOptions"), savedInstanceState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onLowMemory() {
            try {
                this.zzaSe.onLowMemory();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onPause() {
            try {
                this.zzaSe.onPause();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onResume() {
            try {
                this.zzaSe.onResume();
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onSaveInstanceState(Bundle outState) {
            try {
                this.zzaSe.onSaveInstanceState(outState);
            } catch (RemoteException e) {
                throw new RuntimeRemoteException(e);
            }
        }

        public void onStart() {
        }

        public void onStop() {
        }

        public IMapFragmentDelegate zzzV() {
            return this.zzaSe;
        }
    }

    /* compiled from: Unknown */
    static class zzb extends com.google.android.gms.dynamic.zza<zza> {
        private Activity mActivity;
        protected zzf<zza> zzaSh;
        private final List<OnMapReadyCallback> zzaSi = new ArrayList();
        private final Fragment zzalg;

        zzb(Fragment fragment) {
            this.zzalg = fragment;
        }

        private void setActivity(Activity activity) {
            this.mActivity = activity;
            zzzW();
        }

        public void getMapAsync(OnMapReadyCallback callback) {
            if (zztU() == null) {
                this.zzaSi.add(callback);
            } else {
                ((zza) zztU()).getMapAsync(callback);
            }
        }

        public void onEnterAmbient(Bundle ambientDetails) {
            if (zztU() != null) {
                ((zza) zztU()).onEnterAmbient(ambientDetails);
            }
        }

        public void onExitAmbient() {
            if (zztU() != null) {
                ((zza) zztU()).onExitAmbient();
            }
        }

        protected void zza(zzf<zza> zzf) {
            this.zzaSh = zzf;
            zzzW();
        }

        public void zzzW() {
            if (!(this.mActivity == null || this.zzaSh == null || zztU() != null)) {
                try {
                    MapsInitializer.initialize(this.mActivity);
                    IMapFragmentDelegate zzs = zzad.zzaO(this.mActivity).zzs(zze.zzC(this.mActivity));
                    if (zzs != null) {
                        this.zzaSh.zza(new zza(this.zzalg, zzs));
                        for (OnMapReadyCallback mapAsync : this.zzaSi) {
                            ((zza) zztU()).getMapAsync(mapAsync);
                        }
                        this.zzaSi.clear();
                    }
                } catch (RemoteException e) {
                    throw new RuntimeRemoteException(e);
                } catch (GooglePlayServicesNotAvailableException e2) {
                }
            }
        }
    }

    public static SupportMapFragment newInstance() {
        return new SupportMapFragment();
    }

    public static SupportMapFragment newInstance(GoogleMapOptions options) {
        SupportMapFragment supportMapFragment = new SupportMapFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", options);
        supportMapFragment.setArguments(bundle);
        return supportMapFragment;
    }

    @Deprecated
    public final GoogleMap getMap() {
        IMapFragmentDelegate zzzV = zzzV();
        if (zzzV == null) {
            return null;
        }
        try {
            IGoogleMapDelegate map = zzzV.getMap();
            if (map == null) {
                return null;
            }
            if (this.zzaSd == null || this.zzaSd.zzzJ().asBinder() != map.asBinder()) {
                this.zzaSd = new GoogleMap(map);
            }
            return this.zzaSd;
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    public void getMapAsync(OnMapReadyCallback callback) {
        zzx.zzcD("getMapAsync must be called on the main thread.");
        this.zzaSP.getMapAsync(callback);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(SupportMapFragment.class.getClassLoader());
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.zzaSP.setActivity(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.zzaSP.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View onCreateView = this.zzaSP.onCreateView(inflater, container, savedInstanceState);
        onCreateView.setClickable(true);
        return onCreateView;
    }

    public void onDestroy() {
        this.zzaSP.onDestroy();
        super.onDestroy();
    }

    public void onDestroyView() {
        this.zzaSP.onDestroyView();
        super.onDestroyView();
    }

    public final void onEnterAmbient(Bundle ambientDetails) {
        zzx.zzcD("onEnterAmbient must be called on the main thread.");
        this.zzaSP.onEnterAmbient(ambientDetails);
    }

    public final void onExitAmbient() {
        zzx.zzcD("onExitAmbient must be called on the main thread.");
        this.zzaSP.onExitAmbient();
    }

    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        this.zzaSP.setActivity(activity);
        Parcelable createFromAttributes = GoogleMapOptions.createFromAttributes(activity, attrs);
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", createFromAttributes);
        this.zzaSP.onInflate(activity, bundle, savedInstanceState);
    }

    public void onLowMemory() {
        this.zzaSP.onLowMemory();
        super.onLowMemory();
    }

    public void onPause() {
        this.zzaSP.onPause();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        this.zzaSP.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.setClassLoader(SupportMapFragment.class.getClassLoader());
        }
        super.onSaveInstanceState(outState);
        this.zzaSP.onSaveInstanceState(outState);
    }

    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    protected IMapFragmentDelegate zzzV() {
        this.zzaSP.zzzW();
        return this.zzaSP.zztU() != null ? ((zza) this.zzaSP.zztU()).zzzV() : null;
    }
}
