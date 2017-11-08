package com.android.settings.pressure;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.provider.Settings.System;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SearchView;
import android.widget.SearchView.OnQueryTextListener;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.pressure.util.Logger;
import java.util.ArrayList;
import java.util.List;

public class ApplicationListFragment extends SettingsPreferenceFragment implements OnPreferenceClickListener, OnQueryTextListener {
    public static final String TAG = ApplicationListFragment.class.getSimpleName();
    private List<AppItemPreference> mAppSearchList;
    private List<ResolveInfo> mApplicationList;
    private String mClassInfoStored;
    private String mClassName;
    private Context mContext;
    private String mPackageName;
    private PackageManager mPm;
    private PreferenceScreen mPreferenceScreen;
    private ContentResolver mResolver;
    private View mSearchLayout;
    private SearchView mSearchView;
    private int mWhichEdge;

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mWhichEdge = bundle.getInt("pressure_which_edge", 1);
        } else {
            this.mWhichEdge = 1;
        }
        Logger.i(TAG, "mWhichEdge = " + this.mWhichEdge);
        this.mContext = getActivity();
        addPreferencesFromResource(2130969022);
        this.mPm = getPackageManager();
        this.mResolver = getContentResolver();
        this.mAppSearchList = new ArrayList();
        initClassInfoFromStored();
        initApplicationList();
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mSearchLayout = inflater.inflate(2130969024, container, false);
        this.mPreferenceScreen = (PreferenceScreen) findPreference("app_list_screen");
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mSearchView = (SearchView) this.mSearchLayout.findViewById(2131886997);
        initSearchView();
        setHeaderView(this.mSearchLayout);
        initNoneAppPreference();
        initAppPreferenceList();
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mSearchView != null) {
            this.mSearchView.clearFocus();
            this.mSearchView = null;
        }
        if (!(getPreferenceScreen() == null || getHeaderView() == null)) {
            getPreferenceScreen().removePreference(getHeaderView());
        }
        if (getListView() != null) {
            getListView().removeAllViewsInLayout();
        }
        this.mSearchLayout = null;
    }

    public void onDestroy() {
        this.mAppSearchList.clear();
        this.mApplicationList.clear();
        this.mAppSearchList = null;
        this.mApplicationList = null;
        super.onDestroy();
    }

    public boolean onPreferenceClick(Preference preference) {
        if (!(preference instanceof AppItemPreference)) {
            return false;
        }
        AppItemPreference clickedAppPreference = (AppItemPreference) preference;
        if ("none_app".equals(clickedAppPreference.getPackageName())) {
            this.mClassInfoStored = "none_app";
        } else {
            this.mPackageName = clickedAppPreference.getPackageName();
            this.mClassName = clickedAppPreference.getClassName();
            this.mClassInfoStored = this.mPackageName + ";" + this.mClassName;
        }
        Logger.d(TAG, "mClassInfoStored = " + this.mClassInfoStored);
        setClassInfo(this.mClassInfoStored);
        finish();
        return true;
    }

    public boolean onQueryTextSubmit(String s) {
        return false;
    }

    public boolean onQueryTextChange(String newText) {
        this.mPreferenceScreen.removeAll();
        if (this.mAppSearchList == null) {
            Logger.w(TAG, "onQueryTextChange: mAppSearchList is null");
            return false;
        }
        initNoneAppPreference();
        int indexCount = 1;
        for (int index = 0; index < this.mAppSearchList.size(); index++) {
            if (((AppItemPreference) this.mAppSearchList.get(index)).getTitle().toString().contains(newText)) {
                ((AppItemPreference) this.mAppSearchList.get(index)).setOrder(indexCount);
                ((AppItemPreference) this.mAppSearchList.get(index)).setWidgetLayoutResource(2130969023);
                this.mPreferenceScreen.addPreference((Preference) this.mAppSearchList.get(index));
                indexCount++;
            }
        }
        return true;
    }

    private void initApplicationList() {
        if (this.mPm == null) {
            Logger.w(TAG, "initApplicationList: mPm is null");
            return;
        }
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.LAUNCHER");
        if (this.mApplicationList != null) {
            this.mApplicationList.clear();
        }
        this.mApplicationList = this.mPm.queryIntentActivities(intent, 0);
    }

    private void initNoneAppPreference() {
        if (this.mPreferenceScreen == null) {
            Logger.w(TAG, "initNoneAppPreference: mPreferenceScreen is null");
            return;
        }
        AppItemPreference noneAppPreference = new AppItemPreference(this.mContext);
        if ("none_app".equals(this.mClassInfoStored)) {
            noneAppPreference.setChecked(true);
        } else {
            noneAppPreference.setChecked(false);
        }
        noneAppPreference.setPackageName("none_app");
        noneAppPreference.setIcon(2130838183);
        noneAppPreference.setTitle(2131628259);
        noneAppPreference.setOrder(0);
        noneAppPreference.setOnPreferenceClickListener(this);
        noneAppPreference.setWidgetLayoutResource(2130969023);
        this.mPreferenceScreen.addPreference(noneAppPreference);
    }

    private void initAppPreferenceList() {
        if (this.mPreferenceScreen == null) {
            Logger.w(TAG, "initAppPreferenceList: mPreferenceScreen is null");
        } else if (this.mApplicationList == null) {
            Logger.w(TAG, "cannot get app list");
        } else if (this.mPm == null) {
            Logger.w(TAG, "initAppPreferenceList: mPm is null");
        } else {
            int index = 1;
            boolean isChinaArea = Utils.isChinaArea();
            for (ResolveInfo app : this.mApplicationList) {
                if (!(app == null || app.activityInfo == null)) {
                    if (!isChinaArea || !"com.google.android.gms".equals(app.activityInfo.packageName)) {
                        AppItemPreference appItemPreference = new AppItemPreference(this.mContext);
                        if (!this.mClassInfoStored.equals("none_app") && this.mPackageName.equals(app.activityInfo.packageName) && this.mClassName.equals(app.activityInfo.name)) {
                            appItemPreference.setChecked(true);
                        } else {
                            appItemPreference.setChecked(false);
                        }
                        appItemPreference.setTitle(app.loadLabel(this.mPm));
                        Logger.d(TAG, "title = " + app.loadLabel(this.mPm) + ", package = " + app.activityInfo.packageName + ", name = " + app.activityInfo.name + ", " + app.loadIcon(this.mPm).toString());
                        appItemPreference.setIcon(app.loadIcon(this.mPm));
                        appItemPreference.setPackageName(app.activityInfo.packageName);
                        appItemPreference.setClassName(app.activityInfo.name);
                        appItemPreference.setOrder(index);
                        appItemPreference.setWidgetLayoutResource(2130969023);
                        appItemPreference.setResolveInfo(app);
                        appItemPreference.setOnPreferenceClickListener(this);
                        appItemPreference.setPersistent(false);
                        this.mPreferenceScreen.addPreference(appItemPreference);
                        this.mAppSearchList.add(appItemPreference);
                        index++;
                    }
                }
            }
        }
    }

    private void initSearchView() {
        this.mSearchView.setIconifiedByDefault(true);
        this.mSearchView.setIconifiedByDefault(true);
        this.mSearchView.setIconified(false);
        this.mSearchView.onActionViewExpanded();
        this.mSearchView.setFocusable(false);
        this.mSearchView.clearFocus();
        this.mSearchView.setOnQueryTextListener(this);
    }

    private void initClassInfoFromStored() {
        if (this.mWhichEdge == 1) {
            this.mClassInfoStored = System.getString(this.mResolver, "pressure_launch_app_left");
            if (this.mClassInfoStored == null || this.mClassInfoStored.length() == 0) {
                this.mClassInfoStored = "none_app";
                return;
            } else if (this.mClassInfoStored.equals("none_app")) {
                return;
            }
        }
        this.mClassInfoStored = System.getString(this.mResolver, "pressure_launch_app_right");
        if (this.mClassInfoStored == null || this.mClassInfoStored.length() == 0) {
            this.mClassInfoStored = "none_app";
            return;
        } else if (this.mClassInfoStored.equals("none_app")) {
            return;
        }
        String[] classInfo = this.mClassInfoStored.split(";");
        if (classInfo.length == 2) {
            this.mPackageName = classInfo[0];
            this.mClassName = classInfo[1];
        } else {
            this.mPackageName = "";
            this.mClassName = "";
        }
    }

    private void setClassInfo(String classInfo) {
        String whichType = this.mWhichEdge == 1 ? "pressure_launch_app_left" : "pressure_launch_app_right";
        System.putString(this.mResolver, whichType, classInfo);
        ItemUseStat.getInstance().handleClick(this.mContext, 2, whichType, this.mClassName);
        if (!"none_app".equals(classInfo)) {
            getActivity().setResult(-1);
        }
    }
}
