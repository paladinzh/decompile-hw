package com.android.settings.applications;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class RunningServices extends SettingsPreferenceFragment {
    private View mLoadingContainer;
    private Menu mOptionsMenu;
    private final Runnable mRunningProcessesAvail = new Runnable() {
        public void run() {
            Utils.handleLoadingContainer(RunningServices.this.mLoadingContainer, RunningServices.this.mRunningProcessesView, true, true);
        }
    };
    private RunningProcessesView mRunningProcessesView;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(2130968862, null);
        this.mRunningProcessesView = (RunningProcessesView) rootView.findViewById(2131886776);
        this.mRunningProcessesView.doCreate();
        this.mLoadingContainer = rootView.findViewById(2131886754);
        return rootView;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mOptionsMenu = menu;
        menu.add(0, 1, 1, 2131625632).setIcon(2130838288).setShowAsAction(2);
        menu.add(0, 2, 2, 2131625633).setIcon(2130838288).setShowAsAction(2);
        updateOptionsMenu();
    }

    public void onResume() {
        super.onResume();
        Utils.handleLoadingContainer(this.mLoadingContainer, this.mRunningProcessesView, this.mRunningProcessesView.doResume(this, this.mRunningProcessesAvail), false);
    }

    public void onPause() {
        super.onPause();
        this.mRunningProcessesView.doPause();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                this.mRunningProcessesView.mAdapter.setShowBackground(false);
                break;
            case 2:
                this.mRunningProcessesView.mAdapter.setShowBackground(true);
                break;
            default:
                return false;
        }
        updateOptionsMenu();
        return super.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateOptionsMenu();
    }

    private void updateOptionsMenu() {
        boolean z = true;
        boolean showingBackground = this.mRunningProcessesView.mAdapter.getShowBackground();
        this.mOptionsMenu.findItem(1).setVisible(showingBackground);
        MenuItem findItem = this.mOptionsMenu.findItem(2);
        if (showingBackground) {
            z = false;
        }
        findItem.setVisible(z);
    }

    protected int getMetricsCategory() {
        return 404;
    }
}
