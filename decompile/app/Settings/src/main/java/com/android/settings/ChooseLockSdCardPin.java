package com.android.settings;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.os.storage.StorageEventListener;
import android.os.storage.StorageManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.settings.search.Index;

public class ChooseLockSdCardPin extends SettingsActivity {
    private StorageEventListener mStorageListener = new StorageEventListener() {
        public void onStorageStateChanged(String path, String oldState, String newState) {
            Index.getInstance(ChooseLockSdCardPin.this).updateFromClassNameResource(SecuritySettings.class.getName(), true, true);
            if ("bad_removal".equals(newState)) {
                ChooseLockSdCardPin.this.finish();
            }
        }
    };
    private StorageManager mStorageManager;

    public static class ChooseLockSdCardPinFragment extends ChoosePasswordFragmentBase implements OnClickListener {
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (getActivity() instanceof ChooseLockSdCardPin) {
                this.mPasswordMinLetters = 1;
                return;
            }
            throw new SecurityException("Fragment contained in wrong activity");
        }

        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }

        protected String getFooterText() {
            return getActivity().getString(2131628348, new Object[]{Integer.valueOf(this.mPasswordMinLength), Integer.valueOf(this.mPasswordMaxLength), Integer.valueOf(this.mPasswordMinLetters)});
        }

        protected String getErrMsgForIllegalChar(String password) {
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (c < ' ' || c > '') {
                    return getString(2131628196);
                }
            }
            return null;
        }

        protected String getErrMsgForPolicyNotMatch(String password) {
            int letters = 0;
            for (int i = 0; i < password.length(); i++) {
                char c = password.charAt(i);
                if (c >= 'A' && c <= 'Z') {
                    letters++;
                } else if (c >= 'a' && c <= 'z') {
                    letters++;
                }
            }
            if (letters != 0) {
                return null;
            }
            return getResources().getQuantityString(2131689533, 1, new Object[]{Integer.valueOf(1)});
        }

        public void doHandleNext(String password) {
            SdCardLockUtils.setSDLockPassword(getActivity(), password);
            getActivity().setResult(1);
            getActivity().finish();
        }
    }

    public Intent getIntent() {
        Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(":settings:show_fragment", getFragmentClass().getName());
        return modIntent;
    }

    protected boolean isValidFragment(String fragmentName) {
        if (ChooseLockSdCardPinFragment.class.getName().equals(fragmentName)) {
            return true;
        }
        return false;
    }

    Class<? extends Fragment> getFragmentClass() {
        return ChooseLockSdCardPinFragment.class;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (SdCardLockUtils.isPasswordProtected(this)) {
            setTitle(2131628093);
        } else {
            setTitle(2131628091);
        }
        if (isSupportOrientation() || Utils.isTablet()) {
            setRequestedOrientation(-1);
        }
        this.mStorageManager = (StorageManager) getSystemService("storage");
        this.mStorageManager.registerListener(this.mStorageListener);
    }

    protected void onStop() {
        super.onStop();
        SettingsExtUtils.checkHideSoftInput(this);
    }

    public void onDestroy() {
        if (!(this.mStorageManager == null || this.mStorageListener == null)) {
            this.mStorageManager.unregisterListener(this.mStorageListener);
        }
        super.onDestroy();
    }

    private boolean isSupportOrientation() {
        return getResources().getBoolean(2131492920);
    }
}
