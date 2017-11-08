package com.android.contacts.hap.rcs.map;

import android.os.Bundle;
import android.widget.RelativeLayout;

public interface RcsLocationMgr {
    void displayMap(double d, double d2, boolean z);

    void locationOnDestroy();

    void setLocationListener(RcsLocationListener rcsLocationListener);

    void setMapdisplayView(RelativeLayout relativeLayout, Bundle bundle);

    void setPrecallLocationListener(RcsPrecallLocationListener rcsPrecallLocationListener);

    void startLoadMap(double d, double d2);
}
