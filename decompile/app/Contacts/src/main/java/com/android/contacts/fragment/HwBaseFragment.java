package com.android.contacts.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.android.contacts.ContactsApplication;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.widget.SplitActionBarView;

public class HwBaseFragment extends Fragment {
    public Intent mIntent;
    protected SplitActionBarView mSplitActionBarView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            Intent intent = (Intent) savedInstanceState.getParcelable("intent");
            if (intent != null) {
                setIntent(intent);
            }
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        if (!(CommonUtilMethods.calcIfNeedSplitScreen() && outState.getBoolean("save_instance_state_manually"))) {
            super.onSaveInstanceState(outState);
        }
        if (this.mIntent != null) {
            outState.putParcelable("intent", this.mIntent);
        }
    }

    public Intent getIntent() {
        if (this.mIntent != null) {
            return this.mIntent;
        }
        Activity act = getActivity();
        if (act == null) {
            return null;
        }
        return act.getIntent();
    }

    public Context getContext() {
        Activity act = getActivity();
        if (act != null) {
            return act;
        }
        return ContactsApplication.getContext();
    }

    public void setIntent(Intent intent) {
        this.mIntent = intent;
    }

    public Context getApplicationContext() {
        Activity act = getActivity();
        if (act != null) {
            return act.getApplicationContext();
        }
        return ContactsApplication.getContext();
    }

    public Context getApplication() {
        return getActivity().getApplication();
    }

    public void onBackPressed() {
        if (getActivity() != null) {
            getActivity().onBackPressed();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mSplitActionBarView != null) {
            this.mSplitActionBarView.close();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mSplitActionBarView != null) {
            this.mSplitActionBarView.refreshDescription();
        }
    }

    public boolean handleKeyEvent(int aKeyCode) {
        if (82 == aKeyCode) {
            if (this.mSplitActionBarView != null) {
                this.mSplitActionBarView.performaClick(4);
            }
        } else if (4 == aKeyCode && this.mSplitActionBarView != null) {
            this.mSplitActionBarView.close();
        }
        return false;
    }
}
