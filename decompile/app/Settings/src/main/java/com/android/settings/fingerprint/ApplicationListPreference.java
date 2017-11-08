package com.android.settings.fingerprint;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.PreferenceViewHolder;
import android.view.View;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import android.widget.TextView;
import com.android.settings.MLog;
import com.android.settings.SettingsPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class ApplicationListPreference extends SettingsPreferenceFragment implements OnPreferenceClickListener {
    private ListView listView;
    private SearchView mAutoCompleteTextview;
    private String[] mBlackApps;
    private Context mContext = null;
    private int mFpId;
    private boolean mIsToFinish = true;
    private PreferenceScreen mPreScreen;
    private Bundle mbundle;
    private List<NewPreference> mlist = new ArrayList();
    private List<ResolveInfo> mlistAppcations = null;
    private View searchview;

    private static class NewPreference extends Preference {
        public ResolveInfo appObjectInfo;

        public ResolveInfo getAppObjectInfo() {
            return this.appObjectInfo;
        }

        public void setAppObjectInfo(ResolveInfo appObjectInfo) {
            this.appObjectInfo = appObjectInfo;
        }

        public NewPreference(Context contect) {
            super(contect);
        }

        public void onBindViewHolder(PreferenceViewHolder view) {
            super.onBindViewHolder(view);
            TextView title = (TextView) view.findViewById(16908310);
            if (title != null) {
                title.setTextColor(-16777216);
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mContext = getActivity();
        addPreferencesFromResource(2131230794);
        this.mbundle = getIntent().getBundleExtra("fp_msg");
        if (this.mbundle == null) {
            finish();
            return;
        }
        this.mFpId = this.mbundle.getInt("fp_id");
        this.mBlackApps = getBlackApps(this.mContext);
        getActivity().setTitle(2131629276);
        initPreference();
        this.searchview = getActivity().getLayoutInflater().inflate(2130969091, null);
        this.listView = (ListView) getActivity().findViewById(2131886938);
        this.mAutoCompleteTextview = (SearchView) this.searchview.findViewById(2131887140);
        this.mAutoCompleteTextview.setIconifiedByDefault(true);
        this.mAutoCompleteTextview.setIconified(false);
        this.mAutoCompleteTextview.onActionViewExpanded();
        this.mAutoCompleteTextview.requestFocus();
        this.mAutoCompleteTextview.setFocusable(false);
        this.mAutoCompleteTextview.setOnQueryTextListener(new OnQueryTextListener() {
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            public boolean onQueryTextChange(String newText) {
                ApplicationListPreference.this.mPreScreen.removeAll();
                int n = ApplicationListPreference.this.mlist.size();
                int indexCount = 0;
                for (int i = 0; i < n; i++) {
                    if (((NewPreference) ApplicationListPreference.this.mlist.get(i)).getTitle().toString().contains(newText)) {
                        ((NewPreference) ApplicationListPreference.this.mlist.get(i)).setOrder(indexCount);
                        ApplicationListPreference.this.mPreScreen.addPreference((Preference) ApplicationListPreference.this.mlist.get(i));
                        indexCount++;
                    }
                }
                return false;
            }
        });
        this.listView.addHeaderView(this.searchview);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ActionBar actionBar = getActivity().getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public void onDestroy() {
        if (this.mAutoCompleteTextview != null) {
            this.mAutoCompleteTextview.clearFocus();
            this.mAutoCompleteTextview = null;
        }
        this.listView.removeHeaderView(this.searchview);
        this.listView.removeAllViewsInLayout();
        this.searchview = null;
        if (this.mPreScreen != null) {
            this.mPreScreen.removeAll();
            this.mPreScreen = null;
        }
        this.mlist.clear();
        this.mlist = null;
        this.mContext = null;
        super.onDestroy();
    }

    public void onPause() {
        if (this.mIsToFinish) {
            setResult(101);
            finish();
        } else {
            this.mIsToFinish = true;
        }
        super.onPause();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (this.mPreScreen == null) {
            return false;
        }
        NewPreference resinfoInfo = (NewPreference) this.mPreScreen.getPreference(preference.getOrder());
        PackageManager pm = getPackageManager();
        if (resinfoInfo.getAppObjectInfo() == null || pm == null) {
            MLog.d("ApplicationListPreference", "Unable to get resinfoInfo");
            setResult(-1);
            finish();
        } else {
            Intent intent = new Intent();
            intent.putExtra("extra.app.pakege", resinfoInfo.getAppObjectInfo().activityInfo.packageName);
            intent.putExtra("extra.app.class", resinfoInfo.getAppObjectInfo().activityInfo.name);
            setResult(-1, intent);
            finish();
        }
        return false;
    }

    private void initPreference() {
        if (this.mPreScreen == null) {
            this.mPreScreen = (PreferenceScreen) findPreference("fp_diy_app_preference");
        }
        PackageManager pm = getPackageManager();
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        if (this.mlistAppcations != null) {
            this.mlistAppcations.clear();
        }
        this.mlistAppcations = pm.queryIntentActivities(intent, 0);
        int indexCount = 0;
        for (ResolveInfo app : this.mlistAppcations) {
            int isBlackApp = 0;
            if (this.mBlackApps != null) {
                for (String appActivityName : this.mBlackApps) {
                    if (appActivityName.equals(app.activityInfo.name)) {
                        isBlackApp = 1;
                        break;
                    }
                }
            }
            if (1 != isBlackApp) {
                NewPreference pref = new NewPreference(this.mContext);
                pref.setTitle(app.loadLabel(pm));
                pref.setIcon(app.loadIcon(pm));
                pref.setOnPreferenceClickListener(this);
                pref.setOrder(indexCount);
                pref.setPersistent(false);
                pref.setAppObjectInfo(app);
                this.mlist.add(pref);
                this.mPreScreen.addPreference(pref);
                indexCount++;
            }
        }
    }

    private String[] getBlackApps(Context context) {
        String black_strings = null;
        try {
            black_strings = Secure.getString(context.getContentResolver(), "fp_black_apps");
        } catch (Throwable e) {
            MLog.e("ApplicationListPreference", "Could not load fp_black_apps", e);
        }
        if (black_strings != null) {
            return black_strings.split(";");
        }
        return null;
    }

    protected int getMetricsCategory() {
        return 49;
    }
}
