package com.huawei.mms.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwMessageUtils;

public class HwPreferenceFragment extends PreferenceFragment {
    private final String mComponentName = (getClass().getSimpleName() + " @" + hashCode() + "  ");
    private Intent mIntent = null;

    public void onAttach(Activity activity) {
        MLog.d("Mms_View", this.mComponentName + " lifecycle onAttach");
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        MLog.d("Mms_View", this.mComponentName + " lifecycle onCreate");
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    public void onStart() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onStart has been run");
        super.onStart();
    }

    public void onResume() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onResume has been run");
        super.onResume();
    }

    public void onPause() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onPause has been run");
        super.onPause();
    }

    public void onStop() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onStop has been run");
        super.onStop();
    }

    public void onTrimMemory(int level) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onTrimMemory");
        super.onTrimMemory(level);
    }

    public void onDestroyView() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onDestroyView");
        super.onDestroyView();
    }

    public void onDestroy() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onDestroy");
        super.onDestroy();
    }

    public void onDetach() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onDetach");
        super.onDetach();
    }

    public void setIntent(Intent newIntent) {
        if (HwMessageUtils.isSplitOn()) {
            this.mIntent = newIntent;
        } else {
            Activity act = getActivity();
            if (act != null) {
                act.setIntent(newIntent);
            }
        }
    }

    public Intent getIntent() {
        Activity act = getActivity();
        if (HwMessageUtils.isSplitOn()) {
            if (this.mIntent == null && act == null) {
                return null;
            }
            if (this.mIntent != null) {
                return this.mIntent;
            }
        } else if (act == null) {
            return null;
        }
        if (act != null) {
            return act.getIntent();
        }
        return null;
    }
}
