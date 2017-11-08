package com.google.android.gms.maps;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.RemoteException;
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

@TargetApi(11)
/* compiled from: Unknown */
public class MapFragment extends Fragment {
    private final zzb zzaSc = new zzb(this);
    private GoogleMap zzaSd;

    /* compiled from: Unknown */
    static class zza implements MapLifecycleDelegate {
        private final IMapFragmentDelegate zzaSe;
        private final Fragment zzavH;

        public zza(Fragment fragment, IMapFragmentDelegate iMapFragmentDelegate) {
            this.zzaSe = (IMapFragmentDelegate) zzx.zzz(iMapFragmentDelegate);
            this.zzavH = (Fragment) zzx.zzz(fragment);
        }

        public void getMapAsync(final OnMapReadyCallback callback) {
            try {
                this.zzaSe.getMapAsync(new com.google.android.gms.maps.internal.zzo.zza(this) {
                    final /* synthetic */ zza zzaSg;

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
                Bundle arguments = this.zzavH.getArguments();
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
        private final Fragment zzavH;

        zzb(Fragment fragment) {
            this.zzavH = fragment;
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
                        this.zzaSh.zza(new zza(this.zzavH, zzs));
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

    public static MapFragment newInstance() {
        return new MapFragment();
    }

    public static MapFragment newInstance(GoogleMapOptions options) {
        MapFragment mapFragment = new MapFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", options);
        mapFragment.setArguments(bundle);
        return mapFragment;
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
        this.zzaSc.getMapAsync(callback);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.setClassLoader(MapFragment.class.getClassLoader());
        }
        super.onActivityCreated(savedInstanceState);
    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.zzaSc.setActivity(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.zzaSc.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View onCreateView = this.zzaSc.onCreateView(inflater, container, savedInstanceState);
        onCreateView.setClickable(true);
        return onCreateView;
    }

    public void onDestroy() {
        this.zzaSc.onDestroy();
        super.onDestroy();
    }

    public void onDestroyView() {
        this.zzaSc.onDestroyView();
        super.onDestroyView();
    }

    public final void onEnterAmbient(Bundle ambientDetails) {
        zzx.zzcD("onEnterAmbient must be called on the main thread.");
        this.zzaSc.onEnterAmbient(ambientDetails);
    }

    public final void onExitAmbient() {
        zzx.zzcD("onExitAmbient must be called on the main thread.");
        this.zzaSc.onExitAmbient();
    }

    @SuppressLint({"NewApi"})
    public void onInflate(Activity activity, AttributeSet attrs, Bundle savedInstanceState) {
        super.onInflate(activity, attrs, savedInstanceState);
        this.zzaSc.setActivity(activity);
        Parcelable createFromAttributes = GoogleMapOptions.createFromAttributes(activity, attrs);
        Bundle bundle = new Bundle();
        bundle.putParcelable("MapOptions", createFromAttributes);
        this.zzaSc.onInflate(activity, bundle, savedInstanceState);
    }

    public void onLowMemory() {
        this.zzaSc.onLowMemory();
        super.onLowMemory();
    }

    public void onPause() {
        this.zzaSc.onPause();
        super.onPause();
    }

    public void onResume() {
        super.onResume();
        this.zzaSc.onResume();
    }

    public void onSaveInstanceState(Bundle outState) {
        if (outState != null) {
            outState.setClassLoader(MapFragment.class.getClassLoader());
        }
        super.onSaveInstanceState(outState);
        this.zzaSc.onSaveInstanceState(outState);
    }

    public void setArguments(Bundle args) {
        super.setArguments(args);
    }

    protected IMapFragmentDelegate zzzV() {
        this.zzaSc.zzzW();
        return this.zzaSc.zztU() != null ? ((zza) this.zzaSc.zztU()).zzzV() : null;
    }
}
