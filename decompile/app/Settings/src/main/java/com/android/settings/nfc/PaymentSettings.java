package com.android.settings.nfc;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.nfc.PaymentBackend.PaymentAppInfo;
import java.util.List;

public class PaymentSettings extends SettingsPreferenceFragment {
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private PaymentBackend mPaymentBackend;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (listening && NfcAdapter.getDefaultAdapter(this.mContext) != null) {
                PaymentBackend paymentBackend = new PaymentBackend(this.mContext);
                paymentBackend.refresh();
                if (paymentBackend.getDefaultApp() != null) {
                    this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627101, new Object[]{app.label}));
                }
            }
        }
    }

    protected int getMetricsCategory() {
        return 70;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mPaymentBackend = new PaymentBackend(getActivity());
        setHasOptionsMenu(true);
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(getActivity());
        List<PaymentAppInfo> appInfos = this.mPaymentBackend.getPaymentAppInfos();
        if (appInfos != null && appInfos.size() > 0) {
            NfcPaymentPreference preference = new NfcPaymentPreference(getPrefContext(), this.mPaymentBackend);
            preference.setKey("payment");
            screen.addPreference(preference);
            screen.addPreference(new NfcForegroundPreference(getPrefContext(), this.mPaymentBackend));
        }
        setPreferenceScreen(screen);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(2130968879, contentRoot, false);
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    public void onResume() {
        super.onResume();
        this.mPaymentBackend.onResume();
    }

    public void onPause() {
        super.onPause();
        this.mPaymentBackend.onPause();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem menuItem = menu.add(2131626501);
        menuItem.setIntent(new Intent(getActivity(), HowItWorks.class));
        menuItem.setShowAsActionFlags(0);
    }
}
