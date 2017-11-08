package com.android.mms.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Intent;
import com.android.mms.MmsApp;

public class ControllerImpl implements Controller {
    private Activity mActivity;
    private Fragment mFragment;

    public ControllerImpl(Activity activity, Fragment fragment) {
        this.mActivity = activity;
        this.mFragment = fragment;
    }

    public void finishFragment(Fragment fragment) {
        if (fragment == this.mFragment) {
            this.mActivity.finish();
        }
    }

    public void startComposeMessage(Intent intent) {
        intent.setComponent(new ComponentName(this.mActivity, ComposeMessageActivity.class));
        if (this.mFragment.isAdded()) {
            this.mFragment.startActivity(intent);
        } else {
            MmsApp.getApplication().getApplicationContext().startActivity(intent);
        }
    }

    public void startComposeMessage(Intent intent, int enterAnim, int exitAnim) {
        startComposeMessage(intent);
        this.mActivity.overridePendingTransition(enterAnim, exitAnim);
    }

    public void setResult(Fragment fragment, int resultCode, Intent data) {
        fragment.getActivity().setResult(resultCode, data);
    }
}
