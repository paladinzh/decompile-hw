package com.android.settings;

import android.os.Bundle;

public abstract class OptionsMenuFragment extends InstrumentedFragment {
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);
    }
}
