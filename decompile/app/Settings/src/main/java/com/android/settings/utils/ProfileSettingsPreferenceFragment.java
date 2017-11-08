package com.android.settings.utils;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settingslib.drawer.UserAdapter;

public abstract class ProfileSettingsPreferenceFragment extends SettingsPreferenceFragment {
    protected abstract String getIntentActionString();

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final UserAdapter profileSpinnerAdapter = UserAdapter.createUserSpinnerAdapter((UserManager) getSystemService("user"), getActivity());
        if (profileSpinnerAdapter != null) {
            final Spinner spinner = (Spinner) setPinnedHeaderView(2130969146);
            spinner.setAdapter(profileSpinnerAdapter);
            spinner.setOnItemSelectedListener(new OnItemSelectedListener() {
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                    UserHandle selectedUser = profileSpinnerAdapter.getUserHandle(position);
                    if (selectedUser.getIdentifier() != UserHandle.myUserId()) {
                        Intent intent = new Intent(ProfileSettingsPreferenceFragment.this.getIntentActionString());
                        intent.addFlags(268435456);
                        intent.addFlags(32768);
                        ProfileSettingsPreferenceFragment.this.getActivity().startActivityAsUser(intent, selectedUser);
                        spinner.setSelection(0);
                    }
                }

                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });
        }
    }
}
