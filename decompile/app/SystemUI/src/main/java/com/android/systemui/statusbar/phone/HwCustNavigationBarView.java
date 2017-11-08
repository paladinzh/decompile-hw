package com.android.systemui.statusbar.phone;

import android.content.Context;
import android.os.Looper;
import android.view.View;
import android.view.ViewStub;

public class HwCustNavigationBarView {
    public HwCustNavigationBarView(View[] views, Looper looper, Context context) {
        if (views == null || looper == null) {
            throw new RuntimeException("views or looper require!");
        }
    }

    public void toggle() {
    }

    public void update() {
    }

    public boolean supportDebugInfo() {
        return false;
    }

    public void updateReorient(int currentNavigationType, View currentView) {
    }

    public boolean isCustExpandType(int currentNavigationType) {
        return true;
    }

    public ViewStub[] getCustomizedViewStubArray(ViewStub[] viewStubArray) {
        return viewStubArray;
    }

    public void populateScreenLockViewStubs(ViewStub[] viewStubs) {
    }

    public int getCustLayoutId(int currLayoutId, int rot, int navigationType) {
        return currLayoutId;
    }
}
