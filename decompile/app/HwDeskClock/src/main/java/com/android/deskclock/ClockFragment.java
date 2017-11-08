package com.android.deskclock;

import android.app.Activity;
import android.app.Fragment;
import android.view.Menu;

public class ClockFragment extends Fragment {
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.setGroupVisible(R.id.alarm_clock_button_menu, false);
    }

    public boolean[] notifyFragmentChange(boolean[] oldVisible, boolean curVisible, int i) {
        if (oldVisible[i] != curVisible) {
            if (curVisible) {
                onFragmentResume();
            } else {
                onFragmentPause();
            }
        }
        oldVisible[i] = curVisible;
        return oldVisible;
    }

    public void onFragmentResume() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.invalidateOptionsMenu();
            activity.closeContextMenu();
            activity.closeOptionsMenu();
        }
    }

    public void onFragmentPause() {
    }
}
