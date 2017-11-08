package com.android.settings.inputmethod;

import android.content.Context;
import android.content.Intent;
import android.hardware.input.InputDeviceIdentifier;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.hardware.input.KeyboardLayout;
import android.os.Bundle;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.view.InputDevice;
import com.android.settings.SettingsPreferenceFragment;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map.Entry;

public class KeyboardLayoutPickerFragment extends SettingsPreferenceFragment implements InputDeviceListener {
    private InputManager mIm;
    private int mInputDeviceId = -1;
    private InputDeviceIdentifier mInputDeviceIdentifier;
    private KeyboardLayout[] mKeyboardLayouts;
    private HashMap<CheckBoxPreference, KeyboardLayout> mPreferenceMap = new HashMap();

    protected int getMetricsCategory() {
        return 58;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Intent i = getActivity().getIntent();
        if (i != null) {
            this.mInputDeviceIdentifier = (InputDeviceIdentifier) i.getParcelableExtra("input_device_identifier");
        }
        if (this.mInputDeviceIdentifier == null) {
            getActivity().finish();
        }
        this.mIm = (InputManager) getSystemService("input");
        this.mKeyboardLayouts = this.mIm.getKeyboardLayouts();
        Arrays.sort(this.mKeyboardLayouts);
        setPreferenceScreen(createPreferenceHierarchy());
    }

    public void onResume() {
        super.onResume();
        this.mIm.registerInputDeviceListener(this, null);
        if (this.mInputDeviceIdentifier == null || this.mInputDeviceIdentifier.getDescriptor() == null) {
            getActivity().finish();
            return;
        }
        InputDevice inputDevice = this.mIm.getInputDeviceByDescriptor(this.mInputDeviceIdentifier.getDescriptor());
        if (inputDevice == null) {
            getActivity().finish();
            return;
        }
        this.mInputDeviceId = inputDevice.getId();
        updateCheckedState();
    }

    public void onPause() {
        this.mIm.unregisterInputDeviceListener(this);
        this.mInputDeviceId = -1;
        super.onPause();
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (this.mInputDeviceIdentifier == null || this.mInputDeviceIdentifier.getDescriptor() == null) {
            getActivity().finish();
            return false;
        }
        if (preference instanceof CheckBoxPreference) {
            CheckBoxPreference checkboxPref = (CheckBoxPreference) preference;
            KeyboardLayout layout = (KeyboardLayout) this.mPreferenceMap.get(checkboxPref);
            if (layout != null) {
                if (checkboxPref.isChecked()) {
                    this.mIm.addKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, layout.getDescriptor());
                } else {
                    this.mIm.removeKeyboardLayoutForInputDevice(this.mInputDeviceIdentifier, layout.getDescriptor());
                }
                return true;
            }
        }
        return super.onPreferenceTreeClick(preference);
    }

    public void onInputDeviceAdded(int deviceId) {
    }

    public void onInputDeviceChanged(int deviceId) {
        if (this.mInputDeviceId >= 0 && deviceId == this.mInputDeviceId) {
            updateCheckedState();
        }
    }

    public void onInputDeviceRemoved(int deviceId) {
        if (this.mInputDeviceId >= 0 && deviceId == this.mInputDeviceId) {
            getActivity().finish();
        }
    }

    private PreferenceScreen createPreferenceHierarchy() {
        PreferenceScreen root = getPreferenceManager().createPreferenceScreen(getActivity());
        Context context = getActivity();
        for (KeyboardLayout layout : this.mKeyboardLayouts) {
            CheckBoxPreference pref = new CheckBoxPreference(context);
            pref.setTitle(layout.getLabel());
            pref.setSummary(layout.getCollection());
            root.addPreference(pref);
            this.mPreferenceMap.put(pref, layout);
        }
        return root;
    }

    private void updateCheckedState() {
        String[] enabledKeyboardLayouts = this.mIm.getEnabledKeyboardLayoutsForInputDevice(this.mInputDeviceIdentifier);
        Arrays.sort(enabledKeyboardLayouts);
        for (Entry<CheckBoxPreference, KeyboardLayout> entry : this.mPreferenceMap.entrySet()) {
            ((CheckBoxPreference) entry.getKey()).setChecked(Arrays.binarySearch(enabledKeyboardLayouts, ((KeyboardLayout) entry.getValue()).getDescriptor()) >= 0);
        }
    }
}
