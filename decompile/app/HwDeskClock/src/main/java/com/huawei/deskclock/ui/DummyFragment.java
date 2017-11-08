package com.huawei.deskclock.ui;

import android.app.Activity;
import android.os.Bundle;
import com.android.deskclock.ClockFragment;
import com.android.util.Log;

public class DummyFragment extends ClockFragment {
    private boolean replaced;
    private int tabIndex;
    private String tabName;

    public DummyFragment(String tabName, int tabIndex) {
        this.tabName = tabName;
        this.tabIndex = tabIndex;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            this.tabName = savedInstanceState.getString("tag");
            this.tabIndex = savedInstanceState.getInt("tagindex");
            this.replaced = savedInstanceState.getBoolean("tagreplaced");
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("tag", this.tabName);
        outState.putInt("tagindex", this.tabIndex);
        outState.putBoolean("tagreplaced", this.replaced);
    }

    public int getTabIndex() {
        return this.tabIndex;
    }

    public boolean isReplaced() {
        return this.replaced;
    }

    public void setReplaced(boolean replaced) {
        this.replaced = replaced;
    }

    public void onAttach(Activity activity) {
        Log.d("DummyFragment", "DummyFragment(tabName: " + this.tabName + ";tabIndex:" + this.tabIndex + ") Attach to Activity:" + activity);
        super.onAttach(activity);
    }

    public void onDetach() {
        Log.d("DummyFragment", "DummyFragment(tabName: " + this.tabName + "; tabIndex:" + this.tabIndex + ") Detach from Activity:" + getActivity());
        super.onDetach();
    }
}
