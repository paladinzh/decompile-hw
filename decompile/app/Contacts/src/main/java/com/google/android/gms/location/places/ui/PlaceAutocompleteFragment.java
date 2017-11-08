package com.google.android.gms.location.places.ui;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import com.google.android.gms.R;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete.IntentBuilder;
import com.google.android.gms.maps.model.LatLngBounds;

@TargetApi(12)
/* compiled from: Unknown */
public class PlaceAutocompleteFragment extends Fragment {
    private View zzaRh;
    private View zzaRi;
    private EditText zzaRj;
    @Nullable
    private LatLngBounds zzaRk;
    @Nullable
    private AutocompleteFilter zzaRl;
    @Nullable
    private PlaceSelectionListener zzaRm;

    private void zzzF() {
        int i = 0;
        int i2 = this.zzaRj.getText().toString().isEmpty() ? 0 : 1;
        View view = this.zzaRi;
        if (i2 == 0) {
            i = 8;
        }
        view.setVisibility(i);
    }

    private void zzzG() {
        int i;
        int connectionStatusCode;
        try {
            startActivityForResult(new IntentBuilder(2).setBoundsBias(this.zzaRk).setFilter(this.zzaRl).zzeq(this.zzaRj.getText().toString()).zzig(1).build(getActivity()), 1);
            i = -1;
        } catch (Throwable e) {
            connectionStatusCode = e.getConnectionStatusCode();
            Log.e("Places", "Could not open autocomplete activity", e);
            i = connectionStatusCode;
            if (i == -1) {
                GoogleApiAvailability.getInstance().showErrorDialogFragment(getActivity(), i, 2);
            }
        } catch (Throwable e2) {
            connectionStatusCode = e2.errorCode;
            Log.e("Places", "Could not open autocomplete activity", e2);
            i = connectionStatusCode;
            if (i == -1) {
                GoogleApiAvailability.getInstance().showErrorDialogFragment(getActivity(), i, 2);
            }
        }
        if (i == -1) {
            GoogleApiAvailability.getInstance().showErrorDialogFragment(getActivity(), i, 2);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == -1) {
                Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                if (this.zzaRm != null) {
                    this.zzaRm.onPlaceSelected(place);
                }
                setText(place.getName().toString());
            } else if (resultCode == 2) {
                Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                if (this.zzaRm != null) {
                    this.zzaRm.onError(status);
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View inflate = inflater.inflate(R.layout.place_autocomplete_fragment, container, false);
        this.zzaRh = inflate.findViewById(R.id.place_autocomplete_search_button);
        this.zzaRi = inflate.findViewById(R.id.place_autocomplete_clear_button);
        this.zzaRj = (EditText) inflate.findViewById(R.id.place_autocomplete_search_input);
        OnClickListener anonymousClass1 = new OnClickListener(this) {
            final /* synthetic */ PlaceAutocompleteFragment zzaRn;

            {
                this.zzaRn = r1;
            }

            public void onClick(View view) {
                this.zzaRn.zzzG();
            }
        };
        this.zzaRh.setOnClickListener(anonymousClass1);
        this.zzaRj.setOnClickListener(anonymousClass1);
        this.zzaRi.setOnClickListener(new OnClickListener(this) {
            final /* synthetic */ PlaceAutocompleteFragment zzaRn;

            {
                this.zzaRn = r1;
            }

            public void onClick(View view) {
                this.zzaRn.setText("");
            }
        });
        zzzF();
        return inflate;
    }

    public void onDestroyView() {
        this.zzaRh = null;
        this.zzaRi = null;
        this.zzaRj = null;
        super.onDestroyView();
    }

    public void setBoundsBias(@Nullable LatLngBounds bounds) {
        this.zzaRk = bounds;
    }

    public void setFilter(@Nullable AutocompleteFilter filter) {
        this.zzaRl = filter;
    }

    public void setHint(CharSequence hint) {
        this.zzaRj.setHint(hint);
        this.zzaRh.setContentDescription(hint);
    }

    public void setOnPlaceSelectedListener(PlaceSelectionListener listener) {
        this.zzaRm = listener;
    }

    public void setText(CharSequence text) {
        this.zzaRj.setText(text);
        zzzF();
    }
}
