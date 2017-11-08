package com.huawei.mms.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.mms.MmsApp;
import com.android.mms.attachment.ui.mediapicker.MediaPicker;
import com.android.mms.ui.Controller;
import com.android.mms.ui.ConversationList;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.util.ActivityExWrapper;
import com.huawei.mms.util.HwMessageUtils;

public class HwBaseFragment extends Fragment {
    private static final String TAG = "Mms_View";
    protected boolean isPeeking;
    private final String mComponentName = (getClass().getSimpleName() + " @" + hashCode() + "  ");
    private Intent mIntent = null;
    private int mOritation;
    Controller mUiController;

    protected void viewLog(String msg) {
        MLog.d(TAG, msg);
    }

    public void onAttach(Activity activity) {
        MLog.d(TAG, this.mComponentName + " lifecycle onAttach");
        super.onAttach(activity);
        setRightFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        MLog.d(TAG, this.mComponentName + " lifecycle onCreate");
        super.onCreate(savedInstanceState);
        this.mOritation = getResources().getConfiguration().orientation;
        Object result = new ActivityExWrapper(getActivity()).run("getIsPeeking");
        if (result != null) {
            this.isPeeking = ((Boolean) result).booleanValue();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        MLog.d(TAG, this.mComponentName + " lifecycle onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        MLog.d(TAG, this.mComponentName + "lifecycle onCreateView");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        MLog.d(TAG, this.mComponentName + "lifecycle onViewCreated");
        super.onViewCreated(view, savedInstanceState);
    }

    public void onStart() {
        MLog.d(TAG, this.mComponentName + "lifecycle onStart");
        super.onStart();
    }

    public void onResume() {
        MLog.d(TAG, this.mComponentName + "lifecycle onResume");
        super.onResume();
    }

    public void onPause() {
        MLog.d(TAG, this.mComponentName + "lifecycle onPause");
        super.onPause();
    }

    public void onStop() {
        MLog.d(TAG, this.mComponentName + "lifecycle onStop");
        super.onStop();
    }

    public void onTrimMemory(int level) {
        MLog.d(TAG, this.mComponentName + "lifecycle onTrimMemory");
        super.onTrimMemory(level);
    }

    public void onDestroyView() {
        MLog.d(TAG, this.mComponentName + "lifecycle onDestroyView");
        super.onDestroyView();
    }

    public void onDestroy() {
        MLog.d(TAG, this.mComponentName + "lifecycle onDestroy");
        super.onDestroy();
    }

    public void onDetach() {
        MLog.d(TAG, this.mComponentName + "lifecycle onDetach");
        super.onDetach();
    }

    public final boolean isInLandscape() {
        boolean z = true;
        if (isAdded()) {
            if (getResources().getConfiguration().orientation != 2) {
                z = false;
            }
            return z;
        }
        if (MmsApp.getApplication().getApplicationContext().getResources().getConfiguration().orientation != 2) {
            z = false;
        }
        return z;
    }

    public void onRotationChanged(int oldOritation, int newOritation) {
    }

    public boolean onBackPressed() {
        return false;
    }

    public boolean needHidePanel() {
        return false;
    }

    public Context getContext() {
        Activity act = getActivity();
        if (act != null) {
            return act;
        }
        return MmsApp.getApplication().getApplicationContext();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        MLog.d(TAG, this.mComponentName + "lifecycle onConfigurationChanged");
        super.onConfigurationChanged(newConfig);
        if (this.mOritation != newConfig.orientation) {
            onRotationChanged(this.mOritation, newConfig.orientation);
            this.mOritation = newConfig.orientation;
        }
    }

    public boolean checkIsAdded() {
        if (isAdded()) {
            return true;
        }
        MLog.e(TAG, getClass().getName() + " is not currently added to its activity...");
        return false;
    }

    public boolean finishSelf(boolean noAnim) {
        MLog.d(TAG, "finishSelf " + noAnim);
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
            act.overridePendingTransition(0, 0);
            MLog.e(TAG, "Fragment finish without animation.");
        }
        return true;
    }

    public Intent getIntent() {
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

    protected Controller getController() {
        return this.mUiController;
    }

    public void setController(Controller uiContrller) {
        this.mUiController = uiContrller;
    }

    public boolean isPeeking() {
        return this.isPeeking;
    }

    public void setPeeking(boolean isPeeking) {
        this.isPeeking = isPeeking;
    }

    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) {
            setRightFragment();
        }
    }

    private void setRightFragment() {
        if (!(this instanceof MediaPicker) && (getActivity() instanceof ConversationList)) {
            ((ConversationList) getActivity()).setRightFragment(this);
        }
    }
}
