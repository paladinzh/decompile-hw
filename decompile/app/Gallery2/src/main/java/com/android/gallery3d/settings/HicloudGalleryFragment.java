package com.android.gallery3d.settings;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import com.android.gallery3d.R;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;

public class HicloudGalleryFragment extends PreferenceFragment {
    private Preference mHicloudGalleryPreference;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.prefs_hicloudgallery);
        this.mHicloudGalleryPreference = findPreference(GallerySettings.KEY_HICLOUD_GALLERY);
        if (this.mHicloudGalleryPreference != null) {
            this.mHicloudGalleryPreference.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    PhotoShareUtils.login(HicloudGalleryFragment.this.getActivity());
                    return true;
                }
            });
        }
    }

    public void onResume() {
        super.onResume();
        int choiceIndex = (PhotoShareUtils.isCloudPhotoSwitchOpen() && CloudAccount.hasLoginAccount(getActivity())) ? 0 : 1;
        String[] choiceItems = getActivity().getResources().getStringArray(R.array.action_on_off);
        if (this.mHicloudGalleryPreference != null) {
            this.mHicloudGalleryPreference.setSummary(choiceItems[choiceIndex]);
        }
    }
}
