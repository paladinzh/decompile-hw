package com.android.gallery3d.settings;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;
import com.android.gallery3d.app.GalleryApp;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class HicloudAccountFragment extends PreferenceFragment {
    private OnPreferenceClickListener mListener = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            if (CloudAccount.hasLoginAccount(HicloudAccountFragment.this.getActivity())) {
                PhotoShareUtils.openAccountCenter(HicloudAccountFragment.this.getActivity().getApplicationContext());
            } else {
                PhotoShareUtils.login(HicloudAccountFragment.this.getActivity().getApplicationContext());
            }
            return true;
        }
    };

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_hicloud_account);
        updateHicloudAccoundPreference();
    }

    public void onResume() {
        super.onResume();
        updateHicloudAccoundPreference();
    }

    private void updateHicloudAccoundPreference() {
        CharSequence string;
        HicloudAccountManager manager = (HicloudAccountManager) ((GalleryApp) getActivity().getApplication()).getAppComponent(HicloudAccountManager.class);
        CloudAccount account = manager.getHicloudAccount();
        Bitmap headPortrait = manager.getHeadPortrait();
        AccountPreference accountPreference = (AccountPreference) findPreference(GallerySettings.KEY_HICLOUD_ACCOUNT);
        accountPreference.setTitle(account == null ? getResources().getString(R.string.log_in_with_huawei_id) : account.getLoginUserName());
        if (account == null) {
            string = getResources().getString(R.string.enable_hicloud_gallery);
        } else {
            string = null;
        }
        accountPreference.setDescription(string);
        accountPreference.setIcon(headPortrait == null ? getResources().getDrawable(R.drawable.ic_contact_default) : new BitmapDrawable(headPortrait));
        accountPreference.setOnPreferenceClickListener(this.mListener);
    }
}
