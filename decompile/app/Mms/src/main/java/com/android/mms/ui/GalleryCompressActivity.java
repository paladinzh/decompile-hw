package com.android.mms.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import com.google.android.gms.location.places.Place;

public class GalleryCompressActivity extends Activity {
    private GalleryCompressFragment mGalleryCompressFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        getWindow().setFlags(Place.TYPE_SUBLOCALITY_LEVEL_2, Place.TYPE_SUBLOCALITY_LEVEL_2);
        getWindow().addFlags(134217728);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        Fragment fragment = fragmentManager.findFragmentByTag("Mms_UI_GCF");
        if (fragment instanceof GalleryCompressFragment) {
            this.mGalleryCompressFragment = (GalleryCompressFragment) fragment;
        } else {
            this.mGalleryCompressFragment = new GalleryCompressFragment();
            transaction.replace(16908290, this.mGalleryCompressFragment, "Mms_UI_GCF");
        }
        this.mGalleryCompressFragment.setController(new ControllerImpl(this, this.mGalleryCompressFragment));
        transaction.commitAllowingStateLoss();
        fragmentManager.executePendingTransactions();
    }

    public void onBackPressed() {
        super.onBackPressed();
    }
}
