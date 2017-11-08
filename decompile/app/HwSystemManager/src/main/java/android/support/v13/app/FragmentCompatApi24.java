package android.support.v13.app;

import android.app.Fragment;

class FragmentCompatApi24 {
    FragmentCompatApi24() {
    }

    public static void setUserVisibleHint(Fragment f, boolean isVisible) {
        f.setUserVisibleHint(isVisible);
    }
}
