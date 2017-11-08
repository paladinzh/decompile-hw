package com.android.settings.nfc;

import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.os.UserHandle;
import android.os.UserManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.InstrumentedFragment;
import com.android.settings.ShowAdminSupportDetailsDialog;
import com.android.settingslib.HelpUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;

public class AndroidBeam extends InstrumentedFragment {
    private boolean mBeamDisallowedByBase;
    private boolean mBeamDisallowedByOnlyAdmin;
    private NfcAdapter mNfcAdapter;
    private CharSequence mOldActivityTitle;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        HelpUtils.prepareHelpMenuItem(getActivity(), menu, 2131626524, getClass().getName());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        EnforcedAdmin admin = RestrictedLockUtils.checkIfRestrictionEnforced(getActivity(), "no_outgoing_beam", UserHandle.myUserId());
        UserManager um = UserManager.get(getActivity());
        this.mBeamDisallowedByBase = RestrictedLockUtils.hasBaseUserRestriction(getActivity(), "no_outgoing_beam", UserHandle.myUserId());
        if (this.mBeamDisallowedByBase || admin == null) {
            this.mView = inflater.inflate(2130968620, container, false);
            return this.mView;
        }
        View view = inflater.inflate(2130968617, null);
        ShowAdminSupportDetailsDialog.setAdminSupportDetails(getActivity(), view, admin, false);
        view.setVisibility(0);
        this.mBeamDisallowedByOnlyAdmin = true;
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mOldActivityTitle != null) {
            getActivity().getActionBar().setTitle(this.mOldActivityTitle);
        }
    }

    protected int getMetricsCategory() {
        return 69;
    }
}
