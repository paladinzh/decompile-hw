package com.huawei.systemmanager.spacecleanner.ui.spacemanager;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.SpaceManagerActivity;
import com.huawei.systemmanager.spacecleanner.engine.TrashScanHandler;
import com.huawei.systemmanager.util.HwLog;

public class SpaceManagerFragment extends Fragment {
    private static final String TAG = "SpaceManagerFragment";
    private View fragmentView = null;
    SpaceManagerStateMachine mStateMachine;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.spacemanager_main, container, false);
        return this.fragmentView;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        TrashScanHandler handler = ((SpaceManagerActivity) getActivity()).getScanHandler();
        if (handler == null) {
            HwLog.e(TAG, "onViewCreated TrashScanHandler is null! do nothing");
            return;
        }
        this.mStateMachine = new SpaceManagerStateMachine(this, view, handler);
        this.mStateMachine.start();
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mStateMachine != null) {
            this.mStateMachine.quit();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mStateMachine != null) {
            this.mStateMachine.onResume();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mStateMachine != null) {
            this.mStateMachine.onActivityResult(requestCode, resultCode, data);
        }
    }
}
