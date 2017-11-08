package com.android.settings;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkPolicyManager;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.UserHandle;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Toast;
import com.android.ims.ImsManager;
import com.android.settings.bluetooth.Utils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

public class ResetNetworkConfirm extends OptionsMenuFragment {
    private View mContentView;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            if (!Utils.isMonkeyRunning()) {
                Context context = ResetNetworkConfirm.this.getActivity();
                ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
                if (connectivityManager != null) {
                    connectivityManager.factoryReset();
                }
                WifiManager wifiManager = (WifiManager) context.getSystemService("wifi");
                if (wifiManager != null) {
                    wifiManager.factoryReset();
                }
                TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService("phone");
                if (telephonyManager != null) {
                    telephonyManager.factoryReset(ResetNetworkConfirm.this.mSubId);
                }
                NetworkPolicyManager policyManager = (NetworkPolicyManager) context.getSystemService("netpolicy");
                if (policyManager != null) {
                    try {
                        policyManager.factoryReset(telephonyManager.getSubscriberId(ResetNetworkConfirm.this.mSubId));
                    } catch (Exception e) {
                        Log.e("ResetNetworkConfirm", "Exception e :" + e.toString());
                    }
                }
                ResetNetworkConfirm.this.factoryResetSettings();
                BluetoothManager btManager = (BluetoothManager) context.getSystemService("bluetooth");
                if (btManager != null) {
                    BluetoothAdapter btAdapter = btManager.getAdapter();
                    if (btAdapter != null) {
                        btAdapter.factoryReset();
                    }
                }
                ImsManager.factoryReset(context);
                Toast.makeText(context, 2131625414, 0).show();
            }
        }
    };
    private int mSubId = -1;

    private void establishFinalConfirmationState() {
        this.mContentView.findViewById(2131887084).setOnClickListener(this.mFinalClickListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_network_reset", UserHandle.myUserId());
        if (RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_network_reset", UserHandle.myUserId())) {
            return inflater.inflate(2130968876, null);
        }
        if (admin != null) {
            View view = inflater.inflate(2130968617, null);
            ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
            view.setVisibility(0);
            return view;
        }
        this.mContentView = inflater.inflate(2130969055, null);
        establishFinalConfirmationState();
        return this.mContentView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Bundle args = getArguments();
        if (args != null) {
            this.mSubId = args.getInt("subscription", -1);
        }
    }

    protected int getMetricsCategory() {
        return 84;
    }

    private void factoryResetSettings() {
        new AsyncTask<Void, Void, Void>() {
            protected Void doInBackground(Void... params) {
                Utils.factoryReset(ResetNetworkConfirm.this.getActivity(), "3");
                return null;
            }

            protected void onPostExecute(Void result) {
            }
        }.execute(new Void[0]);
        Context ctx = getActivity();
        if (ctx != null) {
            Utils.resetBtAndWifiP2pDeviceName(ctx);
            Editor editor = PreferenceManager.getDefaultSharedPreferences(ctx).edit();
            editor.putString("bt_discoverable_timeout", "twomin");
            editor.putInt("bt_discoverable_timeout_number", 120);
            editor.apply();
            turnOffBluetooth(ctx);
        }
    }

    private void turnOffBluetooth(Context ctx) {
        if (ctx != null) {
            LocalBluetoothManager manager = Utils.getLocalBtManager(ctx);
            if (manager != null) {
                manager.getBluetoothAdapter().setBluetoothEnabled(false);
            }
        }
    }
}
