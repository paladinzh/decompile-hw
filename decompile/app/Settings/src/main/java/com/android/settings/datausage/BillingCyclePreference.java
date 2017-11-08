package com.android.settings.datausage;

import android.content.Context;
import android.content.Intent;
import android.net.NetworkPolicy;
import android.net.NetworkTemplate;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.preference.Preference;
import android.util.AttributeSet;
import com.android.settings.Utils;
import com.android.settings.datausage.CellDataPreference.DataStateListener;
import com.android.settings.datausage.TemplatePreference.NetworkServices;

public class BillingCyclePreference extends Preference implements TemplatePreference {
    private final DataStateListener mListener = new DataStateListener() {
        public void onChange(boolean selfChange) {
            BillingCyclePreference.this.updateEnabled();
        }
    };
    private NetworkPolicy mPolicy;
    private NetworkServices mServices;
    private int mSubId;
    private NetworkTemplate mTemplate;

    public BillingCyclePreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void onAttached() {
        super.onAttached();
        this.mListener.setListener(true, this.mSubId, getContext());
    }

    public void onDetached() {
        this.mListener.setListener(false, this.mSubId, getContext());
        super.onDetached();
    }

    public void setTemplate(NetworkTemplate template, int subId, NetworkServices services) {
        int i = 1;
        this.mTemplate = template;
        this.mSubId = subId;
        this.mServices = services;
        this.mPolicy = services.mPolicyEditor.getPolicy(this.mTemplate);
        Context context = getContext();
        Object[] objArr = new Object[1];
        if (this.mPolicy != null) {
            i = this.mPolicy.cycleDay;
        }
        objArr[0] = Integer.valueOf(i);
        setSummary(context.getString(2131627148, objArr));
        setIntent(getIntent());
    }

    private void updateEnabled() {
        try {
            boolean isAdminUser;
            if (this.mPolicy != null && this.mServices.mNetworkService.isBandwidthControlEnabled() && this.mServices.mTelephonyManager.getDataEnabled(this.mSubId)) {
                isAdminUser = this.mServices.mUserManager.isAdminUser();
            } else {
                isAdminUser = false;
            }
            setEnabled(isAdminUser);
        } catch (RemoteException e) {
            setEnabled(false);
        }
    }

    public Intent getIntent() {
        Bundle args = new Bundle();
        args.putParcelable("network_template", this.mTemplate);
        return Utils.onBuildStartFragmentIntent(getContext(), BillingCycleSettings.class.getName(), args, null, 0, getTitle(), false);
    }
}
