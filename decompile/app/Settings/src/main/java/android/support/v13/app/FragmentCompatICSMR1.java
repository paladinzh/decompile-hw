package android.support.v13.app;

import android.app.Fragment;

class FragmentCompatICSMR1 {
    FragmentCompatICSMR1() {
    }

    public static void setUserVisibleHint(Fragment f, boolean isVisible) {
        if (f.getFragmentManager() != null) {
            f.setUserVisibleHint(isVisible);
        }
    }
}
