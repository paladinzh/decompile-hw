package com.android.contacts.hap.optimize;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import com.android.contacts.util.HwLog;

public class DummyFragment extends Fragment {
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
        if (HwLog.HWDBG) {
            HwLog.d("DummyFragment", "DummyFragment(tabName: " + this.tabName + ";tabIndex:" + this.tabIndex + ") Attach to Activity");
        }
        super.onAttach(activity);
    }

    public void onDetach() {
        if (HwLog.HWDBG) {
            HwLog.d("DummyFragment", "DummyFragment(tabName: " + this.tabName + "; tabIndex:" + this.tabIndex + ") Detach from Activity");
        }
        super.onDetach();
    }
}
