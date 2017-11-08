package com.google.android.gms.common.internal;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.util.Log;

/* compiled from: Unknown */
public class zzh implements OnClickListener {
    private final Activity mActivity;
    private final Intent mIntent;
    private final int zzagz;
    private final Fragment zzalg;

    public zzh(Activity activity, Intent intent, int i) {
        this.mActivity = activity;
        this.zzalg = null;
        this.mIntent = intent;
        this.zzagz = i;
    }

    public zzh(Fragment fragment, Intent intent, int i) {
        this.mActivity = null;
        this.zzalg = fragment;
        this.mIntent = intent;
        this.zzagz = i;
    }

    public void onClick(DialogInterface dialog, int which) {
        try {
            if (this.mIntent != null && this.zzalg != null) {
                this.zzalg.startActivityForResult(this.mIntent, this.zzagz);
            } else if (this.mIntent != null) {
                this.mActivity.startActivityForResult(this.mIntent, this.zzagz);
            }
            dialog.dismiss();
        } catch (ActivityNotFoundException e) {
            Log.e("SettingsRedirect", "Can't redirect to app settings for Google Play services");
        }
    }
}
