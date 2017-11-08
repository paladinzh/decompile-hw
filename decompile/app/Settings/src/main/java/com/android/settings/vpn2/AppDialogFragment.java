package com.android.settings.vpn2;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.PackageInfo;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.os.Bundle;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.util.Log;
import com.android.internal.net.VpnConfig;

public class AppDialogFragment extends DialogFragment implements com.android.settings.vpn2.AppDialog.Listener {
    private Listener mListener;
    private PackageInfo mPackageInfo;
    private final IConnectivityManager mService = Stub.asInterface(ServiceManager.getService("connectivity"));
    private UserManager mUserManager;

    public interface Listener {
        void onCancel();

        void onForget();
    }

    public static void show(Fragment parent, PackageInfo packageInfo, String label, boolean managing, boolean connected) {
        show(parent, null, packageInfo, label, managing, connected);
    }

    public static void show(Fragment parent, Listener listener, PackageInfo packageInfo, String label, boolean managing, boolean connected) {
        if (parent.isAdded()) {
            Bundle args = new Bundle();
            args.putParcelable("package", packageInfo);
            args.putString("label", label);
            args.putBoolean("managing", managing);
            args.putBoolean("connected", connected);
            AppDialogFragment frag = new AppDialogFragment();
            frag.mListener = listener;
            frag.setArguments(args);
            frag.setTargetFragment(parent, 0);
            frag.show(parent.getFragmentManager(), "vpnappdialog");
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mUserManager = UserManager.get(getContext());
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String label = args.getString("label");
        boolean managing = args.getBoolean("managing");
        boolean connected = args.getBoolean("connected");
        this.mPackageInfo = (PackageInfo) args.getParcelable("package");
        if (managing) {
            return new AppDialog(getActivity(), this, this.mPackageInfo, label);
        }
        Builder dlog = new Builder(getActivity()).setTitle(label).setMessage(getActivity().getString(2131626386)).setNegativeButton(getActivity().getString(2131626378), null);
        if (connected && !isUiRestricted()) {
            dlog.setPositiveButton(getActivity().getString(2131626387), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    AppDialogFragment.this.onDisconnect(dialog);
                }
            });
        }
        return dlog.create();
    }

    public void onCancel(DialogInterface dialog) {
        dismiss();
        if (this.mListener != null) {
            this.mListener.onCancel();
        }
        super.onCancel(dialog);
    }

    public void onForget(DialogInterface dialog) {
        if (!isUiRestricted()) {
            int userId = getUserId();
            try {
                this.mService.setVpnPackageAuthorization(this.mPackageInfo.packageName, userId, false);
                onDisconnect(dialog);
            } catch (RemoteException e) {
                Log.e("AppDialogFragment", "Failed to forget authorization of " + this.mPackageInfo.packageName + " for user " + userId, e);
            }
            if (this.mListener != null) {
                this.mListener.onForget();
            }
        }
    }

    private void onDisconnect(DialogInterface dialog) {
        if (!isUiRestricted()) {
            int userId = getUserId();
            try {
                if (this.mPackageInfo.packageName.equals(getConnectedPackage(this.mService, userId))) {
                    this.mService.setAlwaysOnVpnPackage(userId, null, false);
                    this.mService.prepareVpn(this.mPackageInfo.packageName, "[Legacy VPN]", userId);
                }
            } catch (RemoteException e) {
                Log.e("AppDialogFragment", "Failed to disconnect package " + this.mPackageInfo.packageName + " for user " + userId, e);
            }
        }
    }

    private boolean isUiRestricted() {
        return this.mUserManager.hasUserRestriction("no_config_vpn", UserHandle.of(getUserId()));
    }

    private int getUserId() {
        return UserHandle.getUserId(this.mPackageInfo.applicationInfo.uid);
    }

    private static String getConnectedPackage(IConnectivityManager service, int userId) throws RemoteException {
        VpnConfig config = service.getVpnConfig(userId);
        if (config != null) {
            return config.user;
        }
        return null;
    }
}
