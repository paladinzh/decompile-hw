package com.google.android.gms.dynamic;

import android.os.Bundle;

/* compiled from: Unknown */
public interface LifecycleDelegate {
    void onCreate(Bundle bundle);

    void onDestroy();

    void onLowMemory();

    void onPause();

    void onResume();

    void onSaveInstanceState(Bundle bundle);
}
