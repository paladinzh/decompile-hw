package com.android.settings.deviceinfo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.os.UserManager;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import com.android.settings.RadioListPreference;
import com.android.settings.RadioListPreferenceManager;
import com.android.settings.RadioListPreferenceManager.OnOptionSelectedListener;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.deviceinfo.ModeSwitchExecutor.Task;
import java.util.LinkedList;
import java.util.List;

public class UsbConnModeChooserFragment extends SettingsPreferenceFragment implements OnOptionSelectedListener {
    private static final int[] DEFAULT_MODES = new int[]{0, 2, 4, 1, 6};
    private static final int[] DEFAULT_MODES_SUMMARY = new int[]{2131627465, 2131627459, 2131625300, 2131628382, 2131628384};
    private static final String[] PREF_KEYS = new String[]{"usb_charging_only", "usb_mtp", "usb_ptp", "usb_power_supply", "usb_midi"};
    private static Intent mChangeStateIntent = new Intent();
    private UsbBackend mBackend;
    private Context mContext;
    private boolean mDeviceConnected = false;
    private ModeSwitchExecutor mExecutor;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            Log.d("UsbConnModeChooserFragment", "handleMessage msg = " + msg.what + ", arg1 = " + msg.arg1);
            if (UsbConnModeChooserFragment.this.getActivity() == null) {
                Log.w("UsbConnModeChooserFragment", "Activity not found.");
                return;
            }
            UsbConnModeChooserFragment.this.mBackend = new UsbBackend(UsbConnModeChooserFragment.this.mContext);
            int mode = UsbConnModeChooserFragment.this.mBackend.getCurrentMode();
            if (msg.what == 1000) {
                UsbConnModeChooserFragment.this.updatePreferences(mode);
            } else if (msg.what == 1001) {
                UsbConnModeChooserFragment.this.handleMsgCheckTimeout(msg, mode);
            } else {
                Log.e("UsbConnModeChooserFragment", "Unexpected msg : " + msg.what);
            }
        }
    };
    private RadioListPreference[] mPrefs = new RadioListPreference[5];
    private LinkedList<Integer> mSupportedIndexes = new LinkedList();
    private RadioListPreferenceManager mSupportedPrefManager;
    private String[] mUsbRegex;
    private BroadcastReceiver mUsbStateReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i("UsbConnModeChooserFragment", "onReceive action = " + intent.getAction());
            if ("android.hardware.usb.action.USB_STATE".equals(action)) {
                UsbConnModeChooserFragment.this.handleBdcastStateChanged(intent);
            } else if ("com.android.settings.usb.CHANGE_SIM_LIMIT".equals(action)) {
                UsbConnModeChooserFragment.this.sendRefresh(true, 0);
            } else if ("usb_tethered".equals(action)) {
                if ("usb_tethered_open".equals(intent.getStringExtra("usb_tethered_type"))) {
                    Log.i("UsbConnModeChooserFragment", "tether open, stop usb mode chooser!");
                    UsbConnModeChooserFragment.this.stopChooser();
                }
            } else if ("android.hardware.usb.action.USB_PORT_CHANGED".equals(action)) {
                UsbConnModeChooserFragment.this.handleBdcastPortChanged();
            } else if ("com.android.settings.usb.UPDATE_CHOOSERS".equals(action)) {
                UsbConnModeChooserFragment.this.sendRefresh(false, -1);
            }
        }
    };

    private class ModeSwitchTask extends Task {
        int modeIndex;
        long timestamp = System.currentTimeMillis();

        public ModeSwitchTask(int modeIndex) {
            this.modeIndex = modeIndex;
        }

        public void execute() {
            Log.d("UsbConnModeChooserFragment", "Task execute start : " + this);
            int index = this.modeIndex;
            UsbConnModeChooserFragment.this.mBackend.setMode(UsbConnModeChooserFragment.DEFAULT_MODES[index]);
            if (index == 1) {
                UsbConnUtils.untetherUsb(UsbConnModeChooserFragment.this.mContext, UsbConnModeChooserFragment.this.mUsbRegex);
            }
            UsbConnModeChooserFragment.this.mHandler.removeMessages(1001);
            UsbConnModeChooserFragment.this.mHandler.sendMessageDelayed(UsbConnModeChooserFragment.this.mHandler.obtainMessage(1001, index, 0), 1500);
            UsbConnModeChooserFragment.this.mContext.sendBroadcast(UsbConnModeChooserFragment.mChangeStateIntent);
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Task [").append(this.timestamp).append("] : ");
            sb.append("mode = ").append(UsbConnModeChooserFragment.PREF_KEYS[this.modeIndex]);
            return sb.toString();
        }
    }

    public UsbConnModeChooserFragment() {
        mChangeStateIntent.setAction("com.android.settings.usb.CHANGE_STATE");
        mChangeStateIntent.setPackage("com.android.settings");
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230919);
        this.mContext = getActivity();
        this.mBackend = new UsbBackend(this.mContext);
        this.mDeviceConnected = this.mBackend.isDeviceConnected();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.hardware.usb.action.USB_PORT_CHANGED");
        filter.addAction("android.hardware.usb.action.USB_STATE");
        filter.addAction("com.android.settings.usb.CHANGE_SIM_LIMIT");
        filter.addAction("com.android.settings.usb.UPDATE_CHOOSERS");
        this.mContext.registerReceiver(this.mUsbStateReceiver, filter);
        this.mUsbRegex = ((ConnectivityManager) getSystemService("connectivity")).getTetherableUsbRegexs();
        initSuppotedModes();
        initPreferences();
        initPrefManager();
        initExecutor();
        if (Utils.isMonkeyRunning()) {
            getActivity().finish();
        }
    }

    public void onResume() {
        super.onResume();
        this.mBackend = new UsbBackend(this.mContext);
        updatePreferences(this.mBackend.getCurrentMode(), true);
    }

    public void onDestroy() {
        if (this.mContext != null) {
            this.mContext.unregisterReceiver(this.mUsbStateReceiver);
        }
        this.mHandler.removeMessages(1000);
        this.mHandler.removeMessages(1001);
        this.mExecutor.stop();
        super.onDestroy();
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    private void updatePreferences(int mode) {
        updatePreferences(mode, false);
    }

    private void updatePreferences(int mode, boolean isNeedUpdateNotify) {
        boolean optionChecked = false;
        for (Integer index : this.mSupportedIndexes) {
            this.mPrefs[index.intValue()].setChecked(mode == DEFAULT_MODES[index.intValue()]);
            if (mode == DEFAULT_MODES[index.intValue()]) {
                Log.d("UsbConnModeChooserFragment", "updated checked : " + PREF_KEYS[index.intValue()] + ", mode = " + mode);
                optionChecked = true;
                if (isNeedUpdateNotify && getActivity() != null) {
                    getActivity().sendBroadcast(mChangeStateIntent);
                }
            }
        }
        if (!optionChecked) {
            Log.w("UsbConnModeChooserFragment", "device in unsupported mode = " + mode);
            printSupportedModes();
            this.mBackend = new UsbBackend(this.mContext);
            if (this.mBackend.isDeviceConnected()) {
                Log.w("UsbConnModeChooserFragment", "Unsupported mode but device connected, show charging_only!");
                this.mPrefs[0].setChecked(true);
            } else if (this.mBackend.isHostConnected() && this.mSupportedIndexes.contains(Integer.valueOf(3))) {
                Log.w("UsbConnModeChooserFragment", "Unsupported mode but host connected, show charging_only!");
                this.mPrefs[3].setChecked(true);
            } else {
                Log.e("UsbConnModeChooserFragment", "no mode should be shown!");
            }
        }
        if (this.mPrefs[3] != null && this.mPrefs[3].isChecked()) {
            this.mPrefs[3].setSummary(2131628382);
        }
    }

    private List<Integer> getRestrictedModes() {
        List<Integer> list = new LinkedList();
        if (this.mContext == null) {
            Log.e("UsbConnModeChooserFragment", "Context is null, stop chooser!");
            stopChooser();
            return list;
        }
        UserManager userManager = (UserManager) this.mContext.getSystemService("user");
        if (userManager == null) {
            Log.w("UsbConnModeChooserFragment", "UserManager is null!");
            return list;
        }
        if (userManager.hasUserRestriction("no_usb_file_transfer")) {
            Log.d("UsbConnModeChooserFragment", "User restriction enabled, MTP and PTP mode will be invisible.");
            list.add(Integer.valueOf(1));
            list.add(Integer.valueOf(2));
        }
        return list;
    }

    private void printSupportedModes() {
        StringBuilder sb = new StringBuilder();
        sb.append("supported modes : ");
        if (this.mSupportedIndexes == null || this.mSupportedIndexes.isEmpty()) {
            Log.e("UsbConnModeChooserFragment", sb.append("no usb mode supported.").toString());
            return;
        }
        for (Integer idx : this.mSupportedIndexes) {
            sb.append(PREF_KEYS[idx.intValue()]).append(" | ");
        }
        Log.d("UsbConnModeChooserFragment", sb.toString());
    }

    private void initSuppotedModes() {
        List<Integer> restricted = getRestrictedModes();
        int idx = 0;
        while (idx < DEFAULT_MODES.length) {
            if (!(isNeedToRemoveMidi(idx) || !this.mBackend.isModeSupported(DEFAULT_MODES[idx]) || restricted.contains(Integer.valueOf(idx)))) {
                this.mSupportedIndexes.add(Integer.valueOf(idx));
            }
            idx++;
        }
        printSupportedModes();
    }

    private void initPreferences() {
        PreferenceScreen screen = getPreferenceScreen();
        if (screen == null) {
            Log.e("UsbConnModeChooserFragment", "PreferenceScreen is null, should never happen.");
            stopChooser();
            return;
        }
        int index = 0;
        while (index < PREF_KEYS.length) {
            this.mPrefs[index] = initPreference(PREF_KEYS[index], index);
            if (this.mPrefs[index] != null) {
                if (!this.mSupportedIndexes.contains(Integer.valueOf(index))) {
                    screen.removePreference(this.mPrefs[index]);
                    this.mPrefs[index] = null;
                }
                index++;
            } else {
                return;
            }
        }
    }

    private void initPrefManager() {
        List<RadioListPreference> prefs = new LinkedList();
        for (Integer index : this.mSupportedIndexes) {
            if (this.mPrefs[index.intValue()] != null) {
                prefs.add(this.mPrefs[index.intValue()]);
            }
        }
        this.mSupportedPrefManager = new RadioListPreferenceManager(prefs);
        this.mSupportedPrefManager.setOnOptionSelectedListener(this);
    }

    private RadioListPreference initPreference(String key, int order) {
        Preference pref = findPreference(key);
        if (pref == null || !(pref instanceof RadioListPreference)) {
            Log.e("UsbConnModeChooserFragment", "Failed to find preference = " + key);
            stopChooser();
            return null;
        }
        RadioListPreference radioPref = (RadioListPreference) pref;
        radioPref.setOrder(order);
        radioPref.setSummary(getActivity().getString(DEFAULT_MODES_SUMMARY[order]));
        return radioPref;
    }

    private void initExecutor() {
        this.mExecutor = new ModeSwitchExecutor();
        this.mExecutor.start();
    }

    private void sendRefresh(boolean needCheck, int index) {
        this.mHandler.removeMessages(1000);
        this.mHandler.sendEmptyMessageDelayed(1000, 500);
        if (needCheck) {
            this.mHandler.removeMessages(1001);
            this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(1001, index, 0), 1500);
        }
    }

    private void handleMsgCheckTimeout(Message msg, int mode) {
        int expectedModeIndex = msg.arg1;
        if (expectedModeIndex != -1) {
            if (mode != DEFAULT_MODES[expectedModeIndex]) {
                if (expectedModeIndex == 3 && this.mPrefs[3] != null) {
                    Spannable summary = new SpannableString(getString(2131628391));
                    summary.setSpan(new ForegroundColorSpan(-65536), 0, summary.length(), 0);
                    this.mPrefs[3].setSummary((CharSequence) summary);
                }
                Log.w("UsbConnModeChooserFragment", "expected mode = " + DEFAULT_MODES[expectedModeIndex] + " real mode = " + mode);
            }
            updatePreferences(mode, true);
        }
    }

    private void handleBdcastStateChanged(Intent intent) {
        boolean usbConnected = intent.getBooleanExtra("connected", false);
        boolean chargingOnly = intent.getBooleanExtra("only_charging", false);
        this.mDeviceConnected = UsbBackend.isDeviceConnected(usbConnected, chargingOnly);
        this.mBackend = new UsbBackend(this.mContext);
        int dataRole = this.mBackend.getCurrentDataRole();
        Log.d("UsbConnModeChooserFragment", "ACTION_USB_STATE usbConnected = " + usbConnected + ", chargingOnly = " + chargingOnly + ", deviceConnected = " + this.mDeviceConnected + ", dataRole = " + dataRole);
        if (!this.mDeviceConnected && !UsbBackend.isPortConnected(dataRole)) {
            Log.i("UsbConnModeChooserFragment", "ACTION_USB_STATE stop chooser!");
            stopChooser();
        }
    }

    private void handleBdcastPortChanged() {
        this.mBackend = new UsbBackend(this.mContext);
        int dataRole = this.mBackend.getCurrentDataRole();
        boolean deviceConnected = this.mBackend.isDeviceConnected();
        Log.d("UsbConnModeChooserFragment", "ACTION_USB_PORT_CHANGED device connected = " + deviceConnected + ", dataRole = " + dataRole);
        if (deviceConnected || UsbBackend.isPortConnected(dataRole)) {
            sendRefresh(false, -1);
            return;
        }
        Log.i("UsbConnModeChooserFragment", "ACTION_USB_PORT_CHANGED stop chooser!");
        stopChooser();
    }

    private void stopChooser() {
        getActivity().finish();
    }

    private int findIndex(RadioListPreference preference) {
        int ret = -1;
        for (Integer index : this.mSupportedIndexes) {
            if (this.mPrefs[index.intValue()] == preference && this.mPrefs[index.intValue()] != null) {
                ret = index.intValue();
            }
        }
        if (ret != -1) {
            return ret;
        }
        Log.e("UsbConnModeChooserFragment", "no valid usb mode index found.");
        if (preference == null) {
            Log.e("UsbConnModeChooserFragment", "input preference is null.");
        } else {
            Log.e("UsbConnModeChooserFragment", "input preference is : " + preference.getKey());
        }
        return 0;
    }

    private boolean isNeedToRemoveMidi(int idx) {
        if (DEFAULT_MODES[idx] != 6) {
            return false;
        }
        if (SystemProperties.getBoolean("ro.config.issupport.midi", true)) {
            return false;
        }
        return true;
    }

    public void onOptionSelected(RadioListPreference preference, int idx) {
        this.mExecutor.submit(new ModeSwitchTask(findIndex(preference)));
        if (this.mPrefs[3] != null) {
            this.mPrefs[3].setSummary(2131628382);
        }
    }

    public boolean isSelectEnabled() {
        return !Utils.isMonkeyRunning();
    }
}
