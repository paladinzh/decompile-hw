package com.android.contacts.list;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ContactsUnavailableFragment extends Fragment {
    private TextView mMessageView;
    private ProgressBar mProgress;
    private LinearLayout mProgressLayout;
    private Integer mProviderStatus;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mView = inflater.inflate(R.layout.contacts_unavailable_fragment, null);
        this.mMessageView = (TextView) this.mView.findViewById(R.id.message);
        this.mProgress = (ProgressBar) this.mView.findViewById(R.id.progress);
        this.mProgressLayout = (LinearLayout) this.mView.findViewById(R.id.processlayout);
        if (this.mProviderStatus != null) {
            updateStatus(this.mProviderStatus.intValue());
        }
        return this.mView;
    }

    public void updateStatus(int providerStatus) {
        this.mProviderStatus = Integer.valueOf(providerStatus);
        if (this.mView != null) {
            switch (providerStatus) {
                case 1:
                    this.mMessageView.setText(R.string.upgrade_status_in_progress);
                    this.mMessageView.setGravity(1);
                    this.mMessageView.setVisibility(0);
                    this.mProgress.setVisibility(0);
                    if (this.mProgressLayout == null) {
                        HwLog.w("ContactsUnavailableFragment", "mProgressLayout is NULL");
                        break;
                    } else {
                        this.mProgressLayout.setPadding(0, 0, 0, calcBottomLocation());
                        break;
                    }
            }
        }
    }

    private int calcBottomLocation() {
        if (getActivity() == null || getResources() == null) {
            HwLog.w("ContactsUnavailableFragment", "lactivity is NULL or getResources is NULL");
            return 0;
        }
        return CommonUtilMethods.getActionBarAndStatusHeight(getActivity(), getResources().getConfiguration().orientation == 1);
    }
}
