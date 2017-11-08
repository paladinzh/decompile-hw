package com.android.settings.bluetooth;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.bluetooth.BluetoothClass;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.SystemProperties;
import android.os.UserManager;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.text.Html;
import android.text.TextUtils;
import android.text.TextUtils.TruncateAt;
import android.util.Log;
import android.util.Pair;
import android.util.TypedValue;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.ImageView;
import android.widget.TextView;
import com.android.settings.Utils;
import com.android.settings.search.Index;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$string;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.CachedBluetoothDevice.Callback;
import com.android.settingslib.bluetooth.HidProfile;
import com.android.settingslib.bluetooth.LocalBluetoothProfile;
import com.huawei.iconnect.wearable.CompanionAppHelper;
import com.huawei.iconnect.wearable.ManufacturerDataHelper;
import java.util.HashMap;
import java.util.Map.Entry;

public final class BluetoothDevicePreference extends Preference implements Callback, OnClickListener {
    private static int sDimAlpha = Integer.MIN_VALUE;
    public final String BLUETOOTH = this.r.getString(2131627234);
    public final String COMPUTER = this.r.getString(2131627228);
    public final String HEADPHONE = this.r.getString(2131627232);
    public final String HEADSET = this.r.getString(2131627229);
    public final String IMAGING = this.r.getString(2131627231);
    public final String INPUT_PERIPHERAL = this.r.getString(2131627233);
    public final String PHONE = this.r.getString(2131627230);
    private String contentDescription = null;
    private BluetoothDevicePreferenceListener mBluetoothDevicePreferenceListener;
    private final CachedBluetoothDevice mCachedDevice;
    private Context mContext;
    private String mDeviceAppPackageName = "";
    private AlertDialog mDisconnectDialog;
    private final boolean mFactoryMode;
    private HashMap<Integer, byte[]> mManufacturerSpecificData;
    private OnClickListener mOnSettingsClickListener;
    private boolean mRefreshSummary = true;
    private AlertDialog mWearDialog;
    Resources r = getContext().getResources();

    public interface BluetoothDevicePreferenceListener {
        void onBluetoothWearDialogDismiss();

        void onBluetoothWearRequireDeleteDevice(CachedBluetoothDevice cachedBluetoothDevice);
    }

    public BluetoothDevicePreference(Context context, CachedBluetoothDevice cachedDevice) {
        boolean z = true;
        super(context);
        if (sDimAlpha == Integer.MIN_VALUE) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16842803, outValue, true);
            sDimAlpha = (int) (outValue.getFloat() * 255.0f);
        }
        if (!"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            z = SystemProperties.getBoolean("sys.bt.testmode", false);
        }
        this.mFactoryMode = z;
        this.mCachedDevice = cachedDevice;
        this.mContext = context;
        if (cachedDevice.getBondState() != 12) {
            setWidgetLayoutResource(2130968998);
        } else if (!((UserManager) context.getSystemService("user")).hasUserRestriction("no_config_bluetooth")) {
            setWidgetLayoutResource(2130969000);
        }
        this.mCachedDevice.registerCallback(this);
        onDeviceAttributesChanged();
    }

    void rebind() {
        notifyChanged();
    }

    CachedBluetoothDevice getCachedDevice() {
        return this.mCachedDevice;
    }

    public void setOnSettingsClickListener(OnClickListener listener) {
        this.mOnSettingsClickListener = listener;
    }

    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
        this.mCachedDevice.unregisterCallback(this);
        if (this.mDisconnectDialog != null) {
            this.mDisconnectDialog.dismiss();
            this.mDisconnectDialog = null;
        }
    }

    public void onDeviceAttributesChanged() {
        if (this.mFactoryMode) {
            setTitle(this.mCachedDevice.getName() + " (RSSI: " + this.mCachedDevice.getRssi() + ")");
        } else {
            setTitle(this.mCachedDevice.getName());
        }
        if (this.mRefreshSummary) {
            int summaryResId = this.mCachedDevice.getConnectionSummary();
            if (summaryResId != 0) {
                setSummary(summaryResId);
            } else {
                setSummary(null);
            }
        }
        Pair<Integer, String> pair = getBtClassDrawableWithDescription();
        if (((Integer) pair.first).intValue() != 0) {
            setIcon(((Integer) pair.first).intValue());
            this.contentDescription = (String) pair.second;
        }
        setEnabled(!this.mCachedDevice.isBusy());
        HashMap<Integer, byte[]> manufacturerSpecificData = this.mCachedDevice.getManufacturerSpecificData();
        if (!(manufacturerSpecificData == null || manufacturerSpecificData.isEmpty())) {
            this.mManufacturerSpecificData = manufacturerSpecificData;
        }
        notifyHierarchyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder view) {
        if (findPreferenceInHierarchy("bt_checkbox") != null) {
            setDependency("bt_checkbox");
        }
        if (this.mCachedDevice.getBondState() == 12) {
            View deviceDetails = view.findViewById(16908312);
            if (deviceDetails != null) {
                deviceDetails.setContentDescription(getContext().getString(2131627585));
                deviceDetails.setOnClickListener(this);
                deviceDetails.setTag(this.mCachedDevice);
            }
        }
        ImageView imageView = (ImageView) view.findViewById(16908294);
        if (imageView != null) {
            LayoutParams layoutParams = imageView.getLayoutParams();
            int iconSize = this.r.getDimensionPixelSize(2131558990);
            layoutParams.width = iconSize;
            layoutParams.height = iconSize;
            imageView.setLayoutParams(layoutParams);
            imageView.setContentDescription(this.contentDescription);
        }
        ((TextView) view.findViewById(16908310)).setEllipsize(TruncateAt.END);
        super.onBindViewHolder(view);
    }

    public void onClick(View v) {
        if (this.mOnSettingsClickListener != null) {
            this.mOnSettingsClickListener.onClick(v);
        }
    }

    public boolean equals(Object o) {
        if (o == null || !(o instanceof BluetoothDevicePreference)) {
            return false;
        }
        return this.mCachedDevice.equals(((BluetoothDevicePreference) o).mCachedDevice);
    }

    public int hashCode() {
        return this.mCachedDevice.hashCode();
    }

    public int compareTo(Preference another) {
        if (another instanceof BluetoothDevicePreference) {
            return this.mCachedDevice.compareTo(((BluetoothDevicePreference) another).mCachedDevice);
        }
        return super.compareTo(another);
    }

    boolean onClicked() {
        int bondState = this.mCachedDevice.getBondState();
        if (handleWearable()) {
            return true;
        }
        if (this.mCachedDevice.isConnected()) {
            askDisconnect();
        } else if (bondState == 12) {
            this.mCachedDevice.connect(true);
            this.mCachedDevice.setHumanConnect(true);
        } else if (bondState == 10) {
            return pair();
        }
        return true;
    }

    private void askDisconnect() {
        Context context = getContext();
        String name = this.mCachedDevice.getName();
        if (TextUtils.isEmpty(name)) {
            name = context.getString(2131624444);
        }
        String message = context.getString(2131624436, new Object[]{name});
        String title = context.getString(2131624435);
        this.mDisconnectDialog = Utils.showDisconnectDialog(context, this.mDisconnectDialog, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BluetoothDevicePreference.this.mCachedDevice.disconnect();
                Utils.removeSprefData(BluetoothDevicePreference.this.mContext, BluetoothDevicePreference.this.mCachedDevice.getDevice().getAddress());
            }
        }, title, Html.fromHtml(message));
    }

    private boolean pair() {
        if (this.mCachedDevice.startPairing()) {
            Context context = getContext();
            if (!Secure.putInt(context.getContentResolver(), "db_bluetooth_launch_pairing", 1)) {
                HwLog.e("BluetoothDevicePreference", "failed to save launch pairing status, key = db_bluetooth_launch_pairing, value = 1");
            }
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.className = BluetoothSettings.class.getName();
            data.title = this.mCachedDevice.getName();
            data.screenTitle = context.getResources().getString(2131624807);
            data.iconResId = 2130838350;
            data.enabled = true;
            Index.getInstance(context).updateFromSearchIndexableData(data);
            return true;
        }
        if (!Secure.putInt(getContext().getContentResolver(), "db_bluetooth_launch_pairing", 0)) {
            HwLog.e("BluetoothDevicePreference", "failed to save launch pairing status, key = db_bluetooth_launch_pairing, value = 0");
        }
        Utils.showError(getContext(), this.mCachedDevice.getName(), R$string.bluetooth_pairing_error_message_Toast);
        return false;
    }

    private Pair<Integer, String> getBtClassDrawableWithDescription() {
        BluetoothClass btClass = this.mCachedDevice.getBtClass();
        if (btClass != null) {
            switch (btClass.getMajorDeviceClass()) {
                case 256:
                    return new Pair(Integer.valueOf(2130838217), this.COMPUTER);
                case 512:
                    return new Pair(Integer.valueOf(R$drawable.ic_bt_cellphone), this.PHONE);
                case 1280:
                    return new Pair(Integer.valueOf(HidProfile.getHidClassDrawable(btClass)), this.INPUT_PERIPHERAL);
                case 1536:
                    return new Pair(Integer.valueOf(2130838215), this.IMAGING);
                case 1792:
                    return new Pair(Integer.valueOf(R$drawable.ic_bt_watch), this.BLUETOOTH);
            }
        }
        HwLog.w("BluetoothDevicePreference", "mBtClass is null");
        for (LocalBluetoothProfile profile : this.mCachedDevice.getProfiles()) {
            int resId = profile.getDrawableResource(btClass);
            if (resId != 0) {
                return new Pair(Integer.valueOf(resId), null);
            }
        }
        if (btClass != null) {
            if (btClass.doesClassMatch(0)) {
                return new Pair(Integer.valueOf(R$drawable.ic_bt_headset_hfp), this.HEADSET);
            }
            if (btClass.doesClassMatch(1)) {
                return new Pair(Integer.valueOf(R$drawable.ic_bt_headphones_a2dp), this.HEADPHONE);
            }
        }
        return new Pair(Integer.valueOf(2130838221), this.BLUETOOTH);
    }

    public void setRefreshSummary(boolean mRefreshSummary) {
        this.mRefreshSummary = mRefreshSummary;
    }

    private boolean handleWearable() {
        if (this.mCachedDevice.getBondState() != 10) {
            return false;
        }
        if (this.mManufacturerSpecificData == null || this.mManufacturerSpecificData.isEmpty()) {
            this.mManufacturerSpecificData = this.mCachedDevice.getManufacturerSpecificData();
        }
        Log.e("BluetoothDevicePreference", "handleWearable.deviceName:" + this.mCachedDevice.getName() + "|mManufacturerSpecificData:" + this.mManufacturerSpecificData);
        if (this.mManufacturerSpecificData != null) {
            for (Entry entry : this.mManufacturerSpecificData.entrySet()) {
                Object companyID = entry.getKey();
                Log.d("BluetoothDevicePreference", "deviceName:" + this.mCachedDevice.getName() + "|companyID:" + companyID + "|manufacturerData:" + entry.getValue());
            }
        }
        if (!Utils.isIConnectApiExist()) {
            return false;
        }
        int bleDeviceType = ManufacturerDataHelper.getRemoteBleDeviceType(getContext(), this.mManufacturerSpecificData, this.mCachedDevice.getDevice());
        this.mDeviceAppPackageName = CompanionAppHelper.getPackageNameOfCompanion(getContext(), this.mManufacturerSpecificData, this.mCachedDevice.getDevice());
        Log.d("BluetoothDevicePreference", "deviceName:" + this.mCachedDevice.getName() + "|bleDeviceType:" + bleDeviceType + "|devicePackageName:" + this.mDeviceAppPackageName);
        if ((bleDeviceType != 1 && bleDeviceType != 2) || this.mDeviceAppPackageName == null) {
            return false;
        }
        if (this.mWearDialog != null && this.mWearDialog.isShowing()) {
            this.mWearDialog.dismiss();
        }
        return showWearDialog(bleDeviceType);
    }

    private boolean showWearDialog(int bleDeviceType) {
        boolean handled = false;
        Builder builder;
        if (bleDeviceType == 2) {
            boolean isHuaweiWatchPaired = ManufacturerDataHelper.isHuaweiWatchPaired(getContext(), this.mManufacturerSpecificData, this.mCachedDevice.getDevice());
            boolean isAndroidWearInstalled = Utils.hasPackageInfo(getContext().getPackageManager(), this.mDeviceAppPackageName);
            if (isHuaweiWatchPaired) {
                builder = new Builder(getContext());
                builder.setTitle(2131628505);
                builder.setMessage(2131628506);
                builder.setPositiveButton(2131627945, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (BluetoothDevicePreference.this.mBluetoothDevicePreferenceListener != null) {
                            BluetoothDevicePreference.this.mBluetoothDevicePreferenceListener.onBluetoothWearRequireDeleteDevice(BluetoothDevicePreference.this.mCachedDevice);
                        }
                    }
                });
                this.mWearDialog = builder.show();
                setOnDismissListener();
                handled = true;
            } else if (isAndroidWearInstalled) {
                builder = new Builder(getContext());
                builder.setMessage(2131628500);
                builder.setTitle(2131628504);
                builder.setPositiveButton(2131628504, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            Intent launchIntent = BluetoothDevicePreference.this.getContext().getPackageManager().getLaunchIntentForPackage(BluetoothDevicePreference.this.mDeviceAppPackageName);
                            if (launchIntent != null) {
                                BluetoothDevicePreference.this.getContext().startActivity(launchIntent);
                            } else {
                                Log.e("BluetoothDevicePreference", "app not started as launchIntent is null, mDeviceAppPackageName:" + BluetoothDevicePreference.this.mDeviceAppPackageName);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
                builder.setNegativeButton(17039360, null);
                this.mWearDialog = builder.show();
                setOnDismissListener();
                handled = true;
            } else {
                boolean isPhoneAndDeviceTypeUnmatched = ManufacturerDataHelper.isPhoneAndDeviceTypeUnmatched(getContext(), this.mManufacturerSpecificData, this.mCachedDevice.getDevice());
                builder = new Builder(getContext()).setMessage(2131628499);
                if (TextUtils.isEmpty(this.mDeviceAppPackageName)) {
                    builder.setMessage(2131628635);
                    builder.setPositiveButton(2131627945, null);
                } else if (isPhoneAndDeviceTypeUnmatched || !Utils.isAppMarketInstalled(getContext())) {
                    builder.setPositiveButton(2131627945, null);
                } else {
                    builder.setTitle(2131628503);
                    builder.setPositiveButton(2131628503, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent marketIntent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + BluetoothDevicePreference.this.mDeviceAppPackageName));
                                Utils.cancelSplit(BluetoothDevicePreference.this.mContext, marketIntent);
                                BluetoothDevicePreference.this.getContext().startActivity(marketIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton(17039360, null);
                }
                this.mWearDialog = builder.show();
                setOnDismissListener();
                handled = true;
            }
        } else if (bleDeviceType == 1) {
            if (TextUtils.isEmpty(this.mDeviceAppPackageName)) {
                return false;
            }
            if (Utils.hasPackageInfo(getContext().getPackageManager(), this.mDeviceAppPackageName)) {
                builder = new Builder(getContext());
                builder.setMessage(2131628502);
                builder.setTitle(2131628504);
                builder.setPositiveButton(2131628504, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        Intent huaweiWearIntent = new Intent("com.huawei.bone.ACTION_ADD_DEVICE");
                        huaweiWearIntent.setPackage(BluetoothDevicePreference.this.mDeviceAppPackageName);
                        if (!Utils.hasIntentActivity(BluetoothDevicePreference.this.getContext().getPackageManager(), huaweiWearIntent)) {
                            huaweiWearIntent = BluetoothDevicePreference.this.getContext().getPackageManager().getLaunchIntentForPackage(BluetoothDevicePreference.this.mDeviceAppPackageName);
                        }
                        if (huaweiWearIntent != null) {
                            huaweiWearIntent.setPackage(BluetoothDevicePreference.this.mDeviceAppPackageName);
                            huaweiWearIntent.putExtra("com.huawei.bone.extra.CALLER", "EMUI");
                            huaweiWearIntent.putExtra("com.huawei.bone.extra.MANUFACTURER_INFO", BluetoothDevicePreference.this.mManufacturerSpecificData);
                            huaweiWearIntent.putExtra("com.huawei.bone.extra.DEVICE_MAC_ADDRESS", BluetoothDevicePreference.this.mCachedDevice.getDevice().getAddress());
                            try {
                                BluetoothDevicePreference.this.getContext().startActivity(huaweiWearIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                });
                builder.setNegativeButton(17039360, null);
                this.mWearDialog = builder.show();
                setOnDismissListener();
                handled = true;
            } else {
                builder = new Builder(getContext()).setMessage(2131628501);
                if (Utils.isAppMarketInstalled(getContext())) {
                    builder.setTitle(2131628503);
                    builder.setPositiveButton(2131628503, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            try {
                                Intent marketIntent = new Intent("android.intent.action.VIEW", Uri.parse("market://details?id=" + BluetoothDevicePreference.this.mDeviceAppPackageName));
                                Utils.cancelSplit(BluetoothDevicePreference.this.mContext, marketIntent);
                                BluetoothDevicePreference.this.getContext().startActivity(marketIntent);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    builder.setNegativeButton(17039360, null);
                } else {
                    builder.setPositiveButton(2131627945, null);
                }
                this.mWearDialog = builder.show();
                setOnDismissListener();
                handled = true;
            }
        }
        return handled;
    }

    private void setOnDismissListener() {
        if (this.mWearDialog != null) {
            this.mWearDialog.setOnDismissListener(new OnDismissListener() {
                public void onDismiss(DialogInterface dialog) {
                    if (BluetoothDevicePreference.this.mBluetoothDevicePreferenceListener != null) {
                        BluetoothDevicePreference.this.mBluetoothDevicePreferenceListener.onBluetoothWearDialogDismiss();
                    }
                }
            });
        }
    }

    public void setBluetoothDevicePreferenceListener(BluetoothDevicePreferenceListener listener) {
        this.mBluetoothDevicePreferenceListener = listener;
    }
}
