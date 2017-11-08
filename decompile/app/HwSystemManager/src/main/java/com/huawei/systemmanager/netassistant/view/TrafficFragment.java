package com.huawei.systemmanager.netassistant.view;

import android.app.Fragment;
import android.view.ViewStub;

public abstract class TrafficFragment extends Fragment {
    protected abstract ViewStub getEmptyView();

    protected void showEmptyView() {
        ViewStub stub = getEmptyView();
        if (stub != null) {
            stub.setVisibility(0);
        }
    }
}
