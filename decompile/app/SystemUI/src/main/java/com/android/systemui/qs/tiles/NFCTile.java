package com.android.systemui.qs.tiles;

import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.SystemProperties;
import android.widget.Switch;
import com.android.systemui.R;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.Host;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import java.io.File;

public class NFCTile extends QSTile<BooleanState> {
    private static final String NFC_DEVICE_PATH = SystemProperties.get("ro.cfg.nfc.node", "/dev/pn544");
    private final AnimationIcon mDisable = new AnimationIcon(R.drawable.ic_nfc_on2off, R.drawable.ic_nfc_tile_off);
    private final AnimationIcon mEnable = new AnimationIcon(R.drawable.ic_nfc_off2on, R.drawable.ic_nfc_tile_on);

    public NFCTile(Host host) {
        super(host);
    }

    public BooleanState newTileState() {
        return new BooleanState();
    }

    protected void handleClick() {
        boolean newState = !((BooleanState) this.mState).value;
        NfcAdapter nfcAdapter = null;
        try {
            nfcAdapter = NfcAdapter.getNfcAdapter(this.mContext);
        } catch (UnsupportedOperationException e) {
            HwLog.e("NFCTile", "handleClick::UnsupportedOperationException " + e);
        } catch (Exception e2) {
            HwLog.e("NFCTile", "handleClick::Exception " + e2);
        }
        if (nfcAdapter == null) {
            HwLog.e("NFCTile", "handleClick::system nfc adapter is null");
            return;
        }
        int currentState = nfcAdapter.getAdapterState();
        HwLog.i("NFCTile", "handleClick::nfc currentState=" + currentState + ", newState=" + newState);
        if (!newState && (3 == currentState || 2 == currentState)) {
            nfcAdapter.disable();
        } else if (newState && (1 == currentState || 4 == currentState)) {
            nfcAdapter.enable();
        }
        refreshState(Boolean.valueOf(newState));
    }

    protected void handleUpdateState(BooleanState state, Object arg) {
        boolean isNFCOn = arg != null ? ((Boolean) arg).booleanValue() : SystemUiUtil.isNFCEnable(this.mContext);
        state.label = this.mContext.getString(R.string.nfc_widget_name);
        state.labelTint = isNFCOn ? 1 : 0;
        state.value = isNFCOn;
        state.icon = isNFCOn ? this.mEnable : this.mDisable;
        state.textChangedDelay = (long) (isNFCOn ? 300 : 83);
        state.contentDescription = this.mContext.getString(R.string.nfc_widget_name);
        String name = Switch.class.getName();
        state.expandedAccessibilityClassName = name;
        state.minimalAccessibilityClassName = name;
    }

    public int getMetricsCategory() {
        return 268;
    }

    public Intent getLongClickIntent() {
        return new Intent("android.settings.NFCSHARING_SETTINGS").setPackage("com.android.settings");
    }

    public CharSequence getTileLabel() {
        return this.mContext.getString(R.string.nfc_widget_name);
    }

    public void setListening(boolean listening) {
    }

    public boolean isAvailable() {
        File nfc_node = new File(NFC_DEVICE_PATH);
        boolean featureEnable = this.mContext.getPackageManager().hasSystemFeature("android.hardware.nfc");
        boolean nfcNodeExist = nfc_node.exists();
        HwLog.i("NFCTile", "isAvailable::featureEnable=" + featureEnable + ", nfcNodeExist=" + nfcNodeExist);
        if (featureEnable && nfcNodeExist) {
            return true;
        }
        return false;
    }

    protected String composeChangeAnnouncement() {
        if (this.mState.value) {
            return this.mContext.getString(R.string.accessibility_quick_settings_nfc_changed_on);
        }
        return this.mContext.getString(R.string.accessibility_quick_settings_nfc_changed_off);
    }
}
