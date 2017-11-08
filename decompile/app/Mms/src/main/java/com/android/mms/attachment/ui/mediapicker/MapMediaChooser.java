package com.android.mms.attachment.ui.mediapicker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.SystemClock;
import android.provider.Settings.Secure;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.messaging.util.OsUtil;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.FragmentTag;
import com.android.rcs.ui.RcsGroupChatComposeMessageFragment;
import com.google.android.gms.R;
import com.google.android.gms.maps.model.LatLng;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.util.MmsCommon;
import com.huawei.rcs.ui.RcsGroupChatComposeMessageActivity;
import com.huawei.rcs.utils.map.abs.RcsMapFragment;
import com.huawei.rcs.utils.map.abs.RcsMapFragment.AddressData;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import com.huawei.rcs.utils.map.impl.RcsGaodeLocationFragment;
import com.huawei.rcs.utils.map.impl.RcsGoogleMapFragment;

public class MapMediaChooser extends MediaChooser {
    private AddressData mAmapAddressData = null;
    private RcsMapFragment mFragment = null;
    private LatLng mLatLng = null;
    private View mMapView;
    private View mMissingPermissionView = null;
    private long mRequestTimeMillis = 0;
    private Button perimissionButton;

    public MapMediaChooser(MediaPicker mediaPicker) {
        super(mediaPicker);
    }

    protected void onFullScreenChanged(boolean fullScreen) {
        super.onFullScreenChanged(fullScreen);
        if (this.mMediaPicker.isFullScreen()) {
            if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                ((RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF")).removeSupportScale();
            } else {
                ((ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF")).removeSupportScale();
            }
            if (FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "MMS_UI_MAP") != null) {
                ((RcsMapFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "MMS_UI_MAP")).showFullView();
            }
        } else if (!this.mMediaPicker.isInLandscape()) {
            if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                ((RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF")).setSupportScale();
            } else {
                ((ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF")).setSupportScale();
            }
            if (FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "MMS_UI_MAP") != null) {
                ((RcsMapFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "MMS_UI_MAP")).hintFullView();
            }
        }
    }

    public static void gotoPositionDialog(final Context context) {
        View contents = View.inflate(context, R.layout.duoqu_position_dialog, null);
        ((TextView) contents.findViewById(R.id.tv_dialog_content)).setText(R.string.duoqu_open_position_content);
        AlertDialog dialog = new Builder(context).setView(contents).setPositiveButton(R.string.yes, new OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                arg0.dismiss();
                Intent it = new Intent();
                it.setPackage("com.android.settings");
                it.setAction("android.settings.LOCATION_SOURCE_SETTINGS");
                context.startActivity(it);
            }
        }).setNegativeButton(R.string.duoqu_setting_cancel, null).setCancelable(false).create();
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    public View destroyView() {
        if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
            ((RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF")).setSupportScale();
        } else {
            ((ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF")).setSupportScale();
        }
        return super.destroyView();
    }

    public boolean isHandlingTouch() {
        return this.mMediaPicker.isFullScreen();
    }

    protected View createView(ViewGroup container) {
        View view = getLayoutInflater().inflate(R.layout.mediapicker_map_chooser, container, false);
        if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
            HwBaseFragment fragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
        } else {
            ComposeMessageFragment fragment2 = (ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
        }
        if (fragment != null) {
            createMapFragment();
        }
        this.mMissingPermissionView = view.findViewById(R.id.missing_permission_view);
        this.perimissionButton = (Button) view.findViewById(R.id.request_perimission_btn);
        this.perimissionButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                MapMediaChooser.this.requestLocationPermission();
            }
        });
        try {
            this.mMapView = view.findViewById(R.id.mediapicker_map_pager);
        } catch (IllegalArgumentException e) {
            MLog.e("MapMediaChooser", "mMapView occur an IllegalArgumentException: " + e);
        }
        updateForPermissionState(hasPermission("android.permission.ACCESS_FINE_LOCATION"));
        return view;
    }

    private void updateForPermissionState(boolean granted) {
        int i = 8;
        if (this.mMapView != null) {
            int i2;
            if (this.mFragment == null && granted) {
                createMapFragment();
            }
            View view = this.mMapView;
            if (granted) {
                i2 = 0;
            } else {
                i2 = 8;
            }
            view.setVisibility(i2);
            if (this.mMissingPermissionView != null) {
                View view2 = this.mMissingPermissionView;
                if (!granted) {
                    i = 0;
                }
                view2.setVisibility(i);
            }
        }
    }

    public LatLng getLatLng() {
        return this.mLatLng;
    }

    public void setLatLng(LatLng latLng) {
        this.mLatLng = latLng;
    }

    public void setAmapAddressData(AddressData addressData) {
        this.mAmapAddressData = addressData;
    }

    public AddressData getAmapAddressData() {
        return this.mAmapAddressData;
    }

    public void createMapFragment() {
        if (hasPermission("android.permission.ACCESS_FINE_LOCATION")) {
            HwBaseFragment fragment;
            if (Boolean.valueOf(RcsMapLoaderFactory.isInChina(getContext())).booleanValue()) {
                this.mFragment = new RcsGaodeLocationFragment();
            } else {
                this.mFragment = new RcsGoogleMapFragment();
            }
            this.mFragment.setMediaPicker(this.mMediaPicker);
            if (this.mMediaPicker.getActivity() instanceof RcsGroupChatComposeMessageActivity) {
                fragment = (RcsGroupChatComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_GCCMF");
            } else {
                ComposeMessageFragment fragment2 = (ComposeMessageFragment) FragmentTag.getFragmentByTag(this.mMediaPicker.getActivity(), "Mms_UI_CMF");
            }
            replaceFragmentIfNotNull(fragment);
        }
    }

    private void replaceFragmentIfNotNull(HwBaseFragment fragment) {
        if (fragment != null) {
            try {
                FragmentTransaction transaction = fragment.getFragmentManager().beginTransaction();
                transaction.replace(R.id.mediapicker_map_pager, this.mFragment, "MMS_UI_MAP");
                transaction.commitAllowingStateLoss();
            } catch (IllegalArgumentException e) {
                MLog.e("MapMediaChooser", "createMapFragment occur an IllegalArgumentException: " + e);
            }
        }
    }

    private boolean hasPermission(String perm) {
        return this.mMediaPicker.getContext().checkSelfPermission(perm) == 0;
    }

    private static boolean hasPermission(Context context, String perm) {
        return context.checkSelfPermission(perm) == 0;
    }

    public static void gotoPackageSettings(Activity act, int requestCode) {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.parse("package:" + act.getPackageName()));
        intent.setFlags(268435456);
        act.startActivityForResult(intent, requestCode);
    }

    protected void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        long currentTimeMillis = SystemClock.elapsedRealtime();
        int permissionGranted = grantResults[0];
        if (requestCode != 2) {
            return;
        }
        if (permissionGranted == 0) {
            updateForPermissionState(true);
        } else if (permissionGranted == -1 && currentTimeMillis - this.mRequestTimeMillis < 500) {
            gotoPackageSettings(this.mMediaPicker.getActivity(), 2);
        }
    }

    private void requestLocationPermission() {
        MmsCommon.setRequestTimeMillis(System.currentTimeMillis());
        OsUtil.requestPermission(this.mMediaPicker.getActivity(), new String[]{"android.permission.ACCESS_FINE_LOCATION"}, 2);
    }

    public void onResume() {
        updateForPermissionState(hasPermission("android.permission.ACCESS_FINE_LOCATION"));
    }

    protected int getActionBarTitleResId() {
        return 1;
    }

    protected void updateActionBar(AbstractEmuiActionBar actionBar) {
        super.updateActionBar(actionBar);
        if (actionBar != null && this.mMediaPicker.isFullScreen()) {
            actionBar.setTitle(getContext().getResources().getString(R.string.rcs_location_selection));
            if (this.mFragment != null) {
                this.mFragment.updateActionBar(actionBar);
            }
        }
    }

    public int getSupportedMediaTypes() {
        return 16;
    }

    protected int getIconTextResource() {
        return R.string.attach_map_location;
    }

    protected void onRestoreChooserState() {
        setSelected(false);
    }

    public int getIconResource() {
        return this.mSelected ? R.drawable.ic_sms_location_checked : R.drawable.ic_sms_location;
    }

    public static void remindUserIfNecessary(Context context) {
        if (hasPermission(context, "android.permission.ACCESS_FINE_LOCATION") && Secure.getInt(context.getContentResolver(), "location_mode", 0) == 0) {
            gotoPositionDialog(context);
        }
    }
}
