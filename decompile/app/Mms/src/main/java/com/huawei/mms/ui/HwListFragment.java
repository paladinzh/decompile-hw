package com.huawei.mms.ui;

import android.app.Activity;
import android.app.ListFragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.mms.MmsApp;
import com.android.mms.ui.Controller;
import com.android.mms.ui.ConversationList;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.HwMessageUtils;

public class HwListFragment extends ListFragment {
    private final String mComponentName = (getClass().getSimpleName() + " @" + hashCode() + "  ");
    private Intent mIntent = null;
    private int mOritation;
    Controller mUiController;

    public void onAttach(Activity activity) {
        MLog.d("Mms_View", this.mComponentName + " lifecycle onAttach");
        super.onAttach(activity);
    }

    public void onCreate(Bundle savedInstanceState) {
        MLog.d("Mms_View", this.mComponentName + " lifecycle onCreate");
        super.onCreate(savedInstanceState);
        this.mOritation = getResources().getConfiguration().orientation;
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
        MLog.d("Mms_View", this.mComponentName + "lifecycle onStart run");
        super.onStart();
    }

    public void onResume() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onResume run");
        super.onResume();
    }

    public void onPause() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onPause run");
        super.onPause();
    }

    public void onStop() {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onStop run");
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

    public final boolean isInLandscape() {
        return getResources().getConfiguration().orientation == 2;
    }

    public void onRotationChanged(int oldOritation, int newOritation) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public void onConfigurationChanged(Configuration newConfig) {
        MLog.d("Mms_View", this.mComponentName + "lifecycle onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (this.mOritation != newConfig.orientation) {
            onRotationChanged(this.mOritation, newConfig.orientation);
            this.mOritation = newConfig.orientation;
        }
    }

    public Context getContext() {
        Activity act = getActivity();
        if (act != null) {
            return act;
        }
        return MmsApp.getApplication().getApplicationContext();
    }

    public boolean finishSelf(boolean noAnim) {
        MLog.d("Mms_View", "finishSelf " + noAnim);
        if (this.mUiController != null) {
            this.mUiController.finishFragment(this);
            return true;
        }
        Activity act = getActivity();
        if (act == null) {
            return false;
        }
        act.finish();
        if (noAnim) {
            MLog.e("Mms_View", "Fragment finish without animation.");
            act.overridePendingTransition(0, 0);
        }
        return true;
    }

    protected Intent getIntent() {
        Activity act = getActivity();
        if (HwMessageUtils.isSplitOn()) {
            if (act == null && this.mIntent == null) {
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

    protected Controller getController() {
        return this.mUiController;
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

    public void setController(Controller uiContrller) {
        this.mUiController = uiContrller;
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setRightFragment();
        }
    }

    private void setRightFragment() {
        if (getActivity() instanceof ConversationList) {
            ((ConversationList) getActivity()).setRightFragment(this);
        }
    }
}
