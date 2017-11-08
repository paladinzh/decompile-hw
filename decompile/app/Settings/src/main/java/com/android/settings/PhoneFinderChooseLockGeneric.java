package com.android.settings;

import android.app.Fragment;
import com.android.settings.ChooseLockGeneric.ChooseLockGenericFragment;

public class PhoneFinderChooseLockGeneric extends ChooseLockGeneric {

    public static class PhoneFinderChooseLockGenericFragment extends ChooseLockGenericFragment {
        protected void disableUnusablePreferences(int quality, boolean allowBiometric) {
            super.disableUnusablePreferencesImpl(Math.max(quality, 65536), true);
        }
    }

    protected boolean isValidFragment(String fragmentName) {
        return PhoneFinderChooseLockGenericFragment.class.getName().equals(fragmentName);
    }

    Class<? extends Fragment> getFragmentClass() {
        return PhoneFinderChooseLockGenericFragment.class;
    }
}
