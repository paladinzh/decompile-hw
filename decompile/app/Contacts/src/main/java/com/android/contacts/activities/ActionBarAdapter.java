package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.ActionBar.Tab;
import android.app.ActionBar.TabListener;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.SearchView.OnCloseListener;
import android.widget.SearchView.OnQueryTextListener;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.list.ContactsRequest;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.google.android.gms.R;
import com.huawei.android.app.ActionBarEx;

public class ActionBarAdapter implements OnQueryTextListener, OnCloseListener {
    protected final ActionBar mActionBar;
    private final Context mContext;
    private int mCurrentTab = TabState.DEFAULT;
    private boolean mIsFirstQuery;
    protected Listener mListener;
    private final SharedPreferences mPrefs;
    private String mQueryString;
    private boolean mSearchMode;
    private boolean mShowHomeIcon;
    private final MyTabListener mTabListener;

    public interface Listener {
        void onAction(int i);

        void onSelectedTabChanged();
    }

    private class MyTabListener implements TabListener {
        public boolean mIgnoreTabSelected;

        private MyTabListener() {
        }

        public void onTabReselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabUnselected(Tab tab, FragmentTransaction ft) {
        }

        public void onTabSelected(Tab tab, FragmentTransaction ft) {
            if (!this.mIgnoreTabSelected) {
                ActionBarAdapter.this.setCurrentTab(tab.getPosition());
            }
        }
    }

    public interface TabState {
        public static final int ALL = CommonUtilMethods.calcAllContactsTabIndex();
        public static final int COUNT = CommonUtilMethods.calcTabCount();
        public static final int DEFAULT = ALL;
        public static final int DIALER = CommonUtilMethods.calcDailerTabIndex();
        public static final int FAVOR_YELLOWPAGE = CommonUtilMethods.calcFavorOrYPTabIndex();
    }

    public ActionBarAdapter(Context context, Listener listener, ActionBar actionBar, boolean isUsingTwoPanes) {
        this.mContext = context;
        this.mListener = listener;
        this.mActionBar = actionBar;
        this.mPrefs = SharePreferenceUtil.getDefaultSp_de(this.mContext);
        this.mShowHomeIcon = this.mContext.getResources().getBoolean(R.bool.show_home_icon);
        this.mTabListener = new MyTabListener();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        setupTabs();
    }

    private void inflateSearchView() {
    }

    protected void setupTabs() {
        int titleId;
        int tabId;
        if (EmuiFeatureManager.isSystemVoiceCapable()) {
            addTab(TabState.DIALER, R.string.dialer, R.string.dialer, R.id.action_bar_tab_dialer);
        }
        addTab(TabState.ALL, R.string.contactsLabel, R.string.contactsAllLabel, R.id.action_bar_tab_contacts);
        if (EmuiFeatureManager.isShowFavoritesTab(this.mContext.getApplicationContext())) {
            titleId = R.string.contactsFavoritesLabel;
            tabId = R.id.action_bar_tab_favorites;
        } else {
            titleId = R.string.contact_yellowpage_title;
            tabId = R.id.action_bar_tab_yellow_page;
        }
        addTab(TabState.FAVOR_YELLOWPAGE, titleId, titleId, tabId);
    }

    protected int getTabPositionFromNavigationItemPosition(int navItemPos) {
        switch (navItemPos) {
            case 0:
                return TabState.DIALER;
            case 1:
                return TabState.ALL;
            case 2:
                return TabState.FAVOR_YELLOWPAGE;
            default:
                throw new IllegalArgumentException("Parameter must be between 0 and " + Integer.toString(TabState.COUNT - 1) + " inclusive.");
        }
    }

    protected int getNavigationItemPositionFromTabPosition(int tabPos) {
        if (tabPos >= 0 && tabPos < TabState.COUNT) {
            return tabPos;
        }
        throw new IllegalArgumentException("Parameter must be between 0 and " + Integer.toString(TabState.COUNT - 1) + " inclusive.");
    }

    public void initFromrequest(Bundle aSavedState, ContactsRequest aRequest) {
        initFromrequest(aSavedState, aRequest, getCurrentTabFromRequest(aSavedState, aRequest));
    }

    public void initFromrequest(Bundle aSavedState, ContactsRequest aRequest, int currentTab) {
        if (aSavedState == null) {
            this.mSearchMode = aRequest.isSearchMode();
            this.mQueryString = aRequest.getQueryString();
        } else {
            this.mSearchMode = aSavedState.getBoolean("navBar.searchMode");
            this.mQueryString = aSavedState.getString("navBar.query");
        }
        this.mCurrentTab = currentTab;
    }

    public static int getCurrentTabFromRequest(Bundle aSavedState, ContactsRequest aRequest) {
        String str = null;
        int currentTab = TabState.DEFAULT;
        if (aSavedState != null) {
            return aSavedState.getInt("actionBarAdapter.lastTab");
        }
        CharSequence lComponentName = aRequest.getActivityTitle();
        if (143 == aRequest.getActionCode()) {
            if (!TextUtils.isEmpty(lComponentName)) {
                str = lComponentName.toString();
            }
            currentTab = getTabStateForLauncher(str);
        }
        if (142 == aRequest.getActionCode() || (141 == aRequest.getActionCode() && !TextUtils.isEmpty(lComponentName) && "com.android.contacts.activities.CallLogActivity".equals(lComponentName.toString()))) {
            currentTab = TabState.DIALER;
        }
        if (145 == aRequest.getActionCode()) {
            return TabState.ALL;
        }
        return currentTab;
    }

    public void correctLauncherMode(String aComponentName, boolean aCheckDefault) {
        correctLauncherMode(aComponentName, aCheckDefault, true, -1);
    }

    public int correctLauncherMode(String aComponentName, boolean aCheckDefault, boolean aChangeState, int aState) {
        int currState;
        int newState = this.mCurrentTab;
        if (aChangeState) {
            currState = this.mCurrentTab;
        } else {
            currState = aState;
            newState = aState;
        }
        if (aCheckDefault && "com.android.contacts.activities.PeopleActivity".equals(aComponentName)) {
            newState = TabState.ALL;
        } else if (TabState.DIALER != currState && isTabStateIsDialer(aComponentName)) {
            newState = TabState.DIALER;
        } else if (TabState.FAVOR_YELLOWPAGE != currState && isTabStateIsFavorites(aComponentName)) {
            newState = TabState.FAVOR_YELLOWPAGE;
        }
        if (aChangeState) {
            setCurrentTab(newState);
        }
        return newState;
    }

    public void initSearchMode() {
        if (this.mSearchMode) {
            inflateSearchView();
        }
        update();
        if (this.mSearchMode && !TextUtils.isEmpty(this.mQueryString)) {
            setQueryString(this.mQueryString);
        }
    }

    public void initialize(Bundle savedState, ContactsRequest request) {
        initFromrequest(savedState, request);
        initSearchMode();
    }

    public void setListener(Listener listener) {
        this.mListener = listener;
    }

    protected void addTab(int expectedTabIndex, int icon, int description, int id) {
        Tab tab = this.mActionBar.newTab();
        tab.setTabListener(this.mTabListener);
        tab.setText(icon);
        tab.setContentDescription(description);
        ActionBarEx.setTabViewId(tab, id);
        this.mActionBar.addTab(tab);
        if (expectedTabIndex != tab.getPosition()) {
            HwLog.e("ActionBarAdapter", "sometimes focus is not in the current tab");
        }
    }

    public void setCurrentTab(int tab) {
        setCurrentTab(tab, true);
    }

    public void setCurrentTab(int tab, boolean notifyListener) {
        if (tab != this.mCurrentTab) {
            this.mCurrentTab = tab;
            int actionBarSelectedNavIndex = this.mActionBar.getSelectedNavigationIndex();
            switch (this.mActionBar.getNavigationMode()) {
                case 1:
                    if (getCurrentTab() != getTabPositionFromNavigationItemPosition(actionBarSelectedNavIndex)) {
                        this.mActionBar.setSelectedNavigationItem(getNavigationItemPositionFromTabPosition(getCurrentTab()));
                        break;
                    }
                    break;
                case 2:
                    if (getCurrentTab() != actionBarSelectedNavIndex) {
                        this.mActionBar.setSelectedNavigationItem(getCurrentTab());
                        break;
                    }
                    break;
                default:
                    HwLog.w("ActionBarAdapter", "Unknown navigation mode: " + this.mActionBar.getNavigationMode());
                    break;
            }
            if (notifyListener && this.mListener != null) {
                this.mListener.onSelectedTabChanged();
            }
            saveLastTabPreference(getCurrentTab());
        }
    }

    public int getCurrentTab() {
        return this.mCurrentTab;
    }

    public boolean isSearchMode() {
        return this.mSearchMode;
    }

    public void setSearchMode(boolean flag) {
        if (this.mSearchMode != flag) {
            this.mSearchMode = flag;
            update();
            if (this.mSearchMode) {
                inflateSearchView();
                setFocusOnSearchView();
            }
        }
    }

    public String getQueryString() {
        return this.mSearchMode ? this.mQueryString : null;
    }

    public void setQueryString(String query) {
        this.mQueryString = query;
        if (query != null) {
            inflateSearchView();
        }
    }

    public boolean isUpShowing() {
        return this.mSearchMode;
    }

    private void updateDisplayOptions() {
        int current = this.mActionBar.getDisplayOptions() & 30;
        int newFlags = 8;
        if (this.mShowHomeIcon) {
            newFlags = 8 | 2;
        }
        if (this.mSearchMode) {
            newFlags = ((newFlags | 2) | 4) | 16;
        }
        this.mActionBar.setHomeButtonEnabled(this.mSearchMode);
        if (current != newFlags) {
            this.mActionBar.setDisplayOptions(newFlags, 30);
        }
    }

    private void update() {
        if (this.mSearchMode) {
            setFocusOnSearchView();
            if (this.mActionBar.getNavigationMode() != 0) {
                this.mActionBar.setNavigationMode(0);
            }
            if (this.mListener != null) {
                this.mListener.onAction(1);
            }
        } else {
            this.mTabListener.mIgnoreTabSelected = true;
            this.mActionBar.setNavigationMode(2);
            this.mActionBar.setSelectedNavigationItem(getCurrentTab());
            this.mTabListener.mIgnoreTabSelected = false;
            this.mActionBar.setTitle(null);
            if (this.mListener != null) {
                this.mListener.onAction(2);
                this.mListener.onSelectedTabChanged();
            }
        }
        updateDisplayOptions();
    }

    public void init() {
        if (!this.mSearchMode) {
            this.mActionBar.setNavigationMode(2);
            this.mActionBar.setSelectedNavigationItem(getCurrentTab());
            this.mActionBar.setTitle(null);
            updateDisplayOptions();
        }
    }

    public boolean onQueryTextChange(String queryString) {
        if (queryString.equals(this.mQueryString) && !this.mIsFirstQuery) {
            return false;
        }
        this.mIsFirstQuery = false;
        this.mQueryString = queryString;
        if (this.mSearchMode) {
            if (this.mListener != null) {
                this.mListener.onAction(0);
            }
        } else if (!TextUtils.isEmpty(queryString)) {
            setSearchMode(true);
        }
        return true;
    }

    public boolean onQueryTextSubmit(String query) {
        return true;
    }

    public boolean onClose() {
        setSearchMode(false);
        return false;
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("navBar.searchMode", this.mSearchMode);
        outState.putString("navBar.query", this.mQueryString);
        outState.putInt("actionBarAdapter.lastTab", getCurrentTab());
    }

    public void setFocusOnSearchView() {
    }

    protected void saveLastTabPreference(int tab) {
        this.mPrefs.edit().putInt("actionBarAdapter.lastTab", tab).apply();
    }

    public static boolean isTabStateIsDialer(String aComponentName) {
        return "com.android.contacts.activities.DialtactsActivity".equals(aComponentName);
    }

    public static boolean isTabStateIsFavorites(String aComponentName) {
        return "com.android.contacts.FavoritesLauncher".equals(aComponentName);
    }

    public static int getTabStateForLauncher(String aComponentName) {
        if ("com.android.contacts.activities.DialtactsActivity".equals(aComponentName)) {
            return TabState.DIALER;
        }
        if ("com.android.contacts.activities.PeopleActivity".equals(aComponentName)) {
            return TabState.ALL;
        }
        if ("com.android.contacts.FavoritesLauncher".equals(aComponentName)) {
            return TabState.FAVOR_YELLOWPAGE;
        }
        return TabState.DEFAULT;
    }
}
