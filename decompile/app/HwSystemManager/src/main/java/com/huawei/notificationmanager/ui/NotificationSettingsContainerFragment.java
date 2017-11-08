package com.huawei.notificationmanager.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.notificationmanager.ui.NotificationSettingsFragment.OnChangeLinstener;
import com.huawei.systemmanager.R;

public class NotificationSettingsContainerFragment extends Fragment {
    private OnChangeLinstener mChangeLinstener;
    private FragmentManager mFM;
    private LinearLayout mLayout;
    private NotificationSettingsFragment mNotiSettingsFragment;
    private RelativeLayout mQuit;
    private OnQuitListener mQuitListener;
    private TextView mTitle;

    interface OnQuitListener {
        void onQuitClicked();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mFM = getActivity().getFragmentManager();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = (LinearLayout) inflater.inflate(R.layout.noti_settings_container, null);
        refresh(getArguments());
        return this.mLayout;
    }

    private void addFragment(Bundle bundle) {
        this.mNotiSettingsFragment = new NotificationSettingsFragment();
        this.mNotiSettingsFragment.setArguments(bundle);
        this.mNotiSettingsFragment.setOnChangeListener(this.mChangeLinstener);
        FragmentTransaction ft = this.mFM.beginTransaction();
        ft.replace(R.id.content, this.mNotiSettingsFragment, NotificationSettingsFragment.class.getName());
        ft.commitAllowingStateLoss();
    }

    protected void refresh(Bundle bundle) {
        addFragment(bundle);
    }

    protected void refreshQuitState(boolean protrait) {
        this.mQuit.setVisibility(8);
    }

    public void onConfigurationChanged(Configuration newConfig) {
        boolean z = true;
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation != 1) {
            z = false;
        }
        refreshQuitState(z);
    }

    protected void setQuitStateListener(OnQuitListener listener) {
        this.mQuitListener = listener;
    }

    protected void setOnChangeListener(OnChangeLinstener linstener) {
        this.mChangeLinstener = linstener;
    }
}
