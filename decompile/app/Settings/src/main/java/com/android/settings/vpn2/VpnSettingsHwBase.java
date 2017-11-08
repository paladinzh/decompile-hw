package com.android.settings.vpn2;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.ArrayMap;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnProfile;
import com.android.settings.ItemUseStat;
import com.android.settings.MLog;
import com.android.settings.RestrictedSettingsFragment;
import com.android.settings.SettingsActivity;
import java.util.Map;

public class VpnSettingsHwBase extends RestrictedSettingsFragment {
    protected Map<String, LegacyVpnPreference> mLegacyVpnPreferences = new ArrayMap();
    protected String mSelectedKey;
    protected final IConnectivityManager mService = Stub.asInterface(ServiceManager.getService("connectivity"));

    public VpnSettingsHwBase(String restrictionKey) {
        super(restrictionKey);
    }

    protected void createNewVPN() {
        long millis = generateNewKey();
        MLog.v("VpnSettingsHwBase", "key:" + millis);
        editVpn(new VpnProfile(Long.toHexString(millis)), true);
    }

    protected void editVpn(VpnProfile vpnProfile, boolean edit) {
        Bundle bundle = new Bundle();
        bundle.putParcelable("profile", vpnProfile);
        bundle.putBoolean("VpnEditing", edit);
        ((SettingsActivity) getActivity()).startPreferencePanel(VpnAddFragment.class.getName(), bundle, 2131626383, null, this, 1);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == -1 && data.getExtras() != null) {
            Bundle bundle = data.getExtras();
            if (bundle != null) {
                updatePreference((VpnProfile) bundle.getParcelable("profile"), bundle.getBoolean("VpnEditing"));
            }
        }
    }

    public long generateNewKey() {
        long millis = System.currentTimeMillis();
        MLog.v("VpnSettingsHwBase", "key:" + millis);
        while (this.mLegacyVpnPreferences.containsKey(Long.toHexString(millis))) {
            millis++;
        }
        return millis;
    }

    public void updatePreference(VpnProfile profile, boolean isEditing) {
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateEmptyMessage();
    }

    private void updateEmptyMessage() {
        LinearLayout emptyView = (LinearLayout) getView().findViewById(2131886922);
        ImageView emptyIcon = (ImageView) getView().findViewById(2131886560);
        emptyIcon.setBackgroundResource(2130838392);
        Configuration configuration = getResources().getConfiguration();
        LinearLayout parent = (LinearLayout) emptyIcon.getParent();
        float density = getResources().getDisplayMetrics().density;
        if (2 == configuration.orientation) {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (70.0f * density), parent.getPaddingEnd(), 0);
        } else {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (160.0f * density), parent.getPaddingEnd(), 0);
        }
        Button emptyBtn = (Button) getView().findViewById(2131886923);
        TextView emptyTv = (TextView) getView().findViewById(2131886561);
        emptyBtn.setText(2131626395);
        emptyTv.setText(2131627312);
        emptyBtn.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                ItemUseStat.getInstance().handleClick(VpnSettingsHwBase.this.getActivity(), 2, "add_vpn_button");
                VpnSettingsHwBase.this.createNewVPN();
            }
        });
        setEmptyView(emptyView);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateEmptyMessage();
        getActivity().invalidateOptionsMenu();
    }

    protected TextView initEmptyTextView() {
        return null;
    }

    public void onPause() {
        ItemUseStat.getInstance().cacheData(getActivity());
        super.onPause();
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    protected void disconnect(VpnProfile profile) {
        try {
            LegacyVpnInfo connected = this.mService.getLegacyVpnInfo(UserHandle.myUserId());
            if (connected != null && profile.key.equals(connected.key)) {
                this.mService.prepareVpn("[Legacy VPN]", "[Legacy VPN]", UserHandle.myUserId());
            }
        } catch (RemoteException e) {
            Log.e("VpnSettingsHwBase", "Failed to disconnect", e);
        }
    }
}
