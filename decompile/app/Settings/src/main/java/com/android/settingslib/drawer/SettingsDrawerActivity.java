package com.android.settingslib.drawer;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.UserHandle;
import android.os.UserManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.DrawerLayout.DrawerListener;
import android.util.ArraySet;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import com.android.settingslib.HwAnimationReflection;
import com.android.settingslib.ItemUseStat;
import com.android.settingslib.R$drawable;
import com.android.settingslib.R$id;
import com.android.settingslib.R$layout;
import com.android.settingslib.applications.InterestingConfigChanges;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SettingsDrawerActivity extends Activity {
    private static final List<CategoryListener> mCategoryListeners = new ArrayList();
    private static final List<TileCacheListener> mTileCacheListeners = new ArrayList();
    private static InterestingConfigChanges sConfigTracker;
    private static List<DashboardCategory> sDashboardCategories;
    private static ArraySet<ComponentName> sTileBlacklist = new ArraySet();
    private static HashMap<Pair<String, String>, Tile> sTileCache;
    private boolean mDelayUpdateCategories = true;
    private SettingsDrawerAdapter mDrawerAdapter;
    private DrawerLayout mDrawerLayout;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            boolean z = false;
            if (msg.what == 100) {
                new CategoriesUpdater().execute(new Void[0]);
            } else if (msg.what == 101 && SplitUtils.handleMultiWindowMode(SettingsDrawerActivity.this)) {
                SettingsDrawerActivity settingsDrawerActivity = SettingsDrawerActivity.this;
                if (!SplitUtils.reachSplitSize(SettingsDrawerActivity.this)) {
                    z = true;
                }
                settingsDrawerActivity.showMenuIcon(z);
                SettingsDrawerActivity.this.updateDrawer();
            }
        }
    };
    private boolean mHideMenuItems;
    private boolean mNoDrawer = false;
    private final PackageReceiver mPackageReceiver = new PackageReceiver();
    protected boolean mShowingBackMenu;
    private boolean mShowingMenu;
    private boolean mUseDrawer = false;

    public interface TileCacheListener {
        void onTileCacheChanged();
    }

    public interface CategoryListener {
        void onCategoriesChanged();
    }

    private class CategoriesUpdater extends AsyncTask<Void, Void, List<DashboardCategory>> {
        private CategoriesUpdater() {
        }

        protected List<DashboardCategory> doInBackground(Void... params) {
            if (SettingsDrawerActivity.sConfigTracker.applyNewConfig(SettingsDrawerActivity.this.getResources())) {
                SettingsDrawerActivity.sTileCache.clear();
                SettingsDrawerActivity.this.onTileCacheChanged();
            }
            return TileUtils.getCategories(SettingsDrawerActivity.this, SettingsDrawerActivity.sTileCache);
        }

        protected void onPreExecute() {
            if (SettingsDrawerActivity.sConfigTracker == null || SettingsDrawerActivity.sTileCache == null) {
                SettingsDrawerActivity.this.getDashboardCategories();
            }
        }

        protected void onPostExecute(List<DashboardCategory> dashboardCategories) {
            for (int i = 0; i < dashboardCategories.size(); i++) {
                DashboardCategory category = (DashboardCategory) dashboardCategories.get(i);
                int j = 0;
                while (j < category.tiles.size()) {
                    if (SettingsDrawerActivity.sTileBlacklist.contains(((Tile) category.tiles.get(j)).intent.getComponent())) {
                        int j2 = j - 1;
                        category.tiles.remove(j);
                        j = j2;
                    }
                    j++;
                }
            }
            SettingsDrawerActivity.sDashboardCategories = dashboardCategories;
            SettingsDrawerActivity.this.onCategoriesChanged();
        }
    }

    private class PackageReceiver extends BroadcastReceiver {
        private PackageReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            new CategoriesUpdater().execute(new Void[0]);
        }
    }

    private class SettingsDrawerListener implements DrawerListener {
        private SettingsDrawerListener() {
        }

        public void onDrawerOpened(View drawerView) {
            ItemUseStat.getInstance().handleClick(SettingsDrawerActivity.this, 2, "drawer opened");
            if (!SettingsDrawerActivity.this.mHideMenuItems) {
                SettingsDrawerActivity.this.mHideMenuItems = true;
                SettingsDrawerActivity.this.invalidateOptionsMenu();
            }
            SettingsDrawerActivity.this.setDrawerFeatrue(true);
        }

        public void onDrawerClosed(View drawerView) {
            ItemUseStat.getInstance().handleClick(SettingsDrawerActivity.this, 2, "drawer closed");
            SettingsDrawerActivity.this.mHideMenuItems = false;
            SettingsDrawerActivity.this.invalidateOptionsMenu();
            SettingsDrawerActivity.this.setDrawerFeatrue(false);
        }

        public void onDrawerSlide(View drawerView, float slideOffset) {
            if (SettingsDrawerActivity.this.mHideMenuItems && (Float.compare(slideOffset, 0.0f) == 0 || ((double) Math.abs(slideOffset - 0.0f)) < 1.0E-8d)) {
                SettingsDrawerActivity.this.mHideMenuItems = false;
                SettingsDrawerActivity.this.invalidateOptionsMenu();
                SettingsDrawerActivity.this.setDrawerFeatrue(false);
            } else if (!SettingsDrawerActivity.this.mHideMenuItems && slideOffset > 0.0f) {
                SettingsDrawerActivity.this.mHideMenuItems = true;
                SettingsDrawerActivity.this.invalidateOptionsMenu();
                SettingsDrawerActivity.this.setDrawerFeatrue(true);
            }
        }

        public void onDrawerStateChanged(int newState) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        long startTime = System.currentTimeMillis();
        if (this.mNoDrawer) {
            super.setContentView(R$layout.settings_without_drawer);
        } else {
            super.setContentView(R$layout.settings_with_drawer);
        }
        this.mDrawerLayout = (DrawerLayout) findViewById(R$id.drawer_layout);
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.addDrawerListener(new SettingsDrawerListener());
            getDashboardCategories();
            this.mDrawerAdapter = new SettingsDrawerAdapter(this);
            ListView listView = (ListView) findViewById(R$id.left_drawer);
            if (!(listView == null || this.mNoDrawer)) {
                listView.setAdapter(this.mDrawerAdapter);
                listView.setOnItemClickListener(new OnItemClickListener() {
                    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                        SettingsDrawerActivity.this.onTileClicked(SettingsDrawerActivity.this.mDrawerAdapter.getTile(position));
                        SettingsDrawerActivity.this.reportTileClicked(SettingsDrawerActivity.this.mDrawerAdapter.getTile(position));
                    }
                });
            }
            if (savedInstanceState != null) {
                this.mHideMenuItems = savedInstanceState.getBoolean("save_instance_hide_menu_items", false);
                boolean isReachSplitSize = SplitUtils.reachSplitSize(this);
                boolean isLandscape = getResources().getConfiguration().orientation == 2;
                if (isReachSplitSize && isLandscape) {
                    this.mHideMenuItems = false;
                    Log.i("SettingsDrawerActivity", " onCreate need set mHideMenuItems to false.");
                }
            }
            if (getIntent() == null || !getIntent().getBooleanExtra("show_drawer_menu", false)) {
                showBackIcon();
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (this.mShowingMenu && this.mDrawerLayout != null && item.getItemId() == 16908332 && this.mDrawerAdapter.getCount() != 0) {
            ItemUseStat.getInstance().handleClick(this, 2, "drawer menu");
            if (this.mDrawerLayout.isDrawerOpen(8388611)) {
                closeDrawer();
            } else {
                openDrawer();
            }
            return true;
        } else if (!this.mShowingBackMenu || item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        } else {
            finish();
            return true;
        }
    }

    protected void onResume() {
        boolean z = false;
        super.onResume();
        if (this.mDrawerLayout != null) {
            IntentFilter filter = new IntentFilter("android.intent.action.PACKAGE_ADDED");
            filter.addAction("android.intent.action.PACKAGE_REMOVED");
            filter.addAction("android.intent.action.PACKAGE_CHANGED");
            filter.addAction("android.intent.action.PACKAGE_REPLACED");
            filter.addDataScheme("package");
            registerReceiver(this.mPackageReceiver, filter);
            if (this.mDelayUpdateCategories) {
                this.mHandler.removeMessages(100);
                this.mHandler.sendEmptyMessageDelayed(100, 500);
            } else {
                new CategoriesUpdater().execute(new Void[0]);
            }
        }
        if (getIntent() != null && getIntent().getBooleanExtra("show_drawer_menu", false)) {
            if (!(SplitUtils.reachSplitSize(this) || SplitUtils.isSplitMode(this))) {
                z = true;
            }
            showMenuIcon(z);
        }
    }

    protected void onPause() {
        if (this.mDrawerLayout != null) {
            unregisterReceiver(this.mPackageReceiver);
            if (this.mDelayUpdateCategories) {
                this.mHandler.removeMessages(100);
            }
        }
        super.onPause();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("save_instance_hide_menu_items", this.mHideMenuItems);
    }

    public void addCategoryListener(CategoryListener listener) {
        mCategoryListeners.add(listener);
    }

    public void remCategoryListener(CategoryListener listener) {
        mCategoryListeners.remove(listener);
    }

    public void remTileCacheListener(TileCacheListener listener) {
        mTileCacheListeners.remove(listener);
    }

    public void addTileCacheListener(TileCacheListener listener) {
        mTileCacheListeners.add(listener);
    }

    public void setIsDrawerPresent(boolean isPresent) {
        if (isPresent) {
            this.mDrawerLayout = (DrawerLayout) findViewById(R$id.drawer_layout);
            updateDrawer();
        } else if (this.mDrawerLayout != null) {
            this.mDrawerLayout.setDrawerLockMode(1);
            this.mDrawerLayout = null;
        }
    }

    public void openDrawer() {
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.openDrawer(8388611);
        }
    }

    public void closeDrawer() {
        if (this.mDrawerLayout != null) {
            this.mDrawerLayout.closeDrawers();
        }
    }

    public void setContentView(int layoutResID) {
        ViewGroup parent = (ViewGroup) findViewById(R$id.content_frame);
        if (parent != null) {
            parent.removeAllViews();
        }
        LayoutInflater.from(this).inflate(layoutResID, parent);
    }

    public void setContentView(View view) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view);
    }

    public void setContentView(View view, LayoutParams params) {
        ((ViewGroup) findViewById(R$id.content_frame)).addView(view, params);
    }

    public void updateDrawer() {
        if (this.mDrawerLayout != null) {
            this.mDrawerAdapter.updateCategories();
            if (!this.mNoDrawer) {
                if (this.mDrawerAdapter.getCount() == 0 || SplitUtils.reachSplitSize(this)) {
                    this.mDrawerLayout.setDrawerLockMode(1);
                } else {
                    this.mDrawerLayout.setDrawerLockMode(0);
                }
            }
        }
    }

    public void showMenuIcon(boolean show) {
        if (show) {
            this.mShowingMenu = true;
            if (getActionBar() != null) {
                getActionBar().setHomeAsUpIndicator(R$drawable.ic_public_drawer);
                getActionBar().setDisplayHomeAsUpEnabled(true);
                return;
            }
            return;
        }
        this.mShowingMenu = false;
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(false);
        }
    }

    public void showBackIcon() {
        this.mShowingBackMenu = true;
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(this.mShowingBackMenu);
        }
    }

    public static void initCategories(Context context) {
        Log.i("SettingsDrawerActivity", "initCategories start!");
        if (context != null) {
            getDashboardCategoriesInternal(context);
            Log.i("SettingsDrawerActivity", "initCategories successfully!");
        }
    }

    private static List<DashboardCategory> getDashboardCategoriesInternal(Context context) {
        if (sDashboardCategories == null) {
            sTileCache = new HashMap();
            sConfigTracker = new InterestingConfigChanges();
            sConfigTracker.applyNewConfig(context.getResources());
            sDashboardCategories = TileUtils.getCategories(context, sTileCache);
        }
        return sDashboardCategories;
    }

    public List<DashboardCategory> getDashboardCategories() {
        return getDashboardCategoriesInternal(this);
    }

    protected void onCategoriesChanged() {
        updateDrawer();
        int N = mCategoryListeners.size();
        for (int i = 0; i < N; i++) {
            ((CategoryListener) mCategoryListeners.get(i)).onCategoriesChanged();
        }
    }

    private void onTileCacheChanged() {
        if (mTileCacheListeners != null) {
            int N = mTileCacheListeners.size();
            for (int i = 0; i < N; i++) {
                ((TileCacheListener) mTileCacheListeners.get(i)).onTileCacheChanged();
            }
        }
    }

    protected void setDelayUpdateCategories(boolean delay) {
        this.mDelayUpdateCategories = delay;
    }

    public void setNoDrawer(boolean noDrawer) {
        this.mNoDrawer = noDrawer;
    }

    private int getUserHandleNumFromTile(Tile tile) {
        UserManager userManager = (UserManager) getSystemService("user");
        ArrayList<UserHandle> userHandles = tile.userHandle;
        ArrayList<UserHandle> mUserHandles = new ArrayList();
        for (UserHandle userHandle : userHandles) {
            UserInfo userInfo = userManager.getUserInfo(userHandle.getIdentifier());
            if (!(userInfo == null || userInfo.isClonedProfile())) {
                mUserHandles.add(userHandle);
            }
        }
        return mUserHandles.size();
    }

    public boolean openTile(Tile tile) {
        closeDrawer();
        if (tile == null) {
            startActivity(new Intent("android.settings.SETTINGS").addFlags(32768));
            return true;
        }
        putExtraDataForIntent(tile);
        try {
            int numUserHandles = getUserHandleNumFromTile(tile);
            if (numUserHandles > 1) {
                ProfileSelectDialog.show(getFragmentManager(), tile);
                return false;
            }
            if (numUserHandles == 1) {
                tile.intent.putExtra("show_drawer_menu", true);
                tile.intent.addFlags(32768);
                setTargarIntentOrCancelSplit(tile.intent);
                startActivityAsUser(tile.intent, (UserHandle) tile.userHandle.get(0));
            } else {
                tile.intent.putExtra("show_drawer_menu", true);
                tile.intent.addFlags(32768);
                setTargarIntentOrCancelSplit(tile.intent);
                startActivity(tile.intent);
            }
            overrideTransition(tile);
            return true;
        } catch (ActivityNotFoundException e) {
            Log.w("SettingsDrawerActivity", "Couldn't find tile " + tile.intent, e);
        }
    }

    private void overrideTransition(Tile tile) {
        if (tile != null && tile.intent != null) {
            ComponentName cn = tile.intent.getComponent();
            if (cn != null) {
                String className = cn.getClassName();
                if ("com.huawei.android.hwouc.ui.activities.MainEntranceActivity".equals(className) || "com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity".equals(className)) {
                    new HwAnimationReflection(this).overrideTransition(1);
                }
            }
        }
    }

    protected void onTileClicked(Tile tile) {
        if (openTile(tile) && !startCardManagerAgain(tile)) {
            finish();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean startCardManagerAgain(Tile tile) {
        if (tile == null || tile.intent == null || !"com.huawei.android.dsdscardmanager".equals(getPackagesNameString(getIntent())) || !"com.huawei.android.dsdscardmanager".equals(getPackagesNameString(tile.intent))) {
            return false;
        }
        return true;
    }

    private String getPackagesNameString(Intent intent) {
        if (intent == null || intent.getComponent() == null) {
            return null;
        }
        return intent.getComponent().getPackageName();
    }

    private void putExtraDataForIntent(Tile tile) {
        if (tile != null && tile.intent != null) {
            ComponentName cn = tile.intent.getComponent();
            if (cn != null) {
                String className = cn.getClassName();
                if (className.equals("com.huawei.hwid.ui.extend.setting.StartUpGuideLoginActivity")) {
                    tile.intent.putExtra("START_FOR_GOTO_ACCOUNTCENTER", true);
                } else {
                    if (!className.equals("com.huawei.android.hwouc.ui.activities.MainEntranceActivity")) {
                        if (className.equals("com.huawei.android.hicloud.ui.activity.NewHiSyncSettingActivity")) {
                        }
                    }
                    tile.intent.putExtra("intent_from_settings", true);
                }
            }
        }
    }

    public void onProfileTileOpen() {
        finish();
    }

    public void setTileEnabled(ComponentName component, boolean enabled) {
        boolean isEnabled;
        PackageManager pm = getPackageManager();
        int state = pm.getComponentEnabledSetting(component);
        if (state == 1) {
            isEnabled = true;
        } else {
            isEnabled = false;
        }
        if (isEnabled != enabled || state == 0) {
            int i;
            if (enabled) {
                sTileBlacklist.remove(component);
            } else {
                sTileBlacklist.add(component);
            }
            if (enabled) {
                i = 1;
            } else {
                i = 2;
            }
            pm.setComponentEnabledSetting(component, i, 1);
            new CategoriesUpdater().execute(new Void[0]);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (!this.mHideMenuItems) {
            return super.onPrepareOptionsMenu(menu);
        }
        for (int i = 0; i < menu.size(); i++) {
            menu.getItem(i).setVisible(false);
        }
        setDrawerFeatrue(true);
        return false;
    }

    private void setDrawerFeatrue(boolean useDrawer) {
        if (this.mUseDrawer != useDrawer) {
            getWindow().setHwDrawerFeature(useDrawer, 1);
            getWindow().setDrawerOpend(useDrawer);
            this.mUseDrawer = useDrawer;
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        boolean z = false;
        this.mHandler.removeMessages(101);
        this.mHandler.sendEmptyMessageDelayed(101, 500);
        updateDrawer();
        if (getIntent() != null && getIntent().getBooleanExtra("show_drawer_menu", false)) {
            if (!SplitUtils.reachSplitSize(this)) {
                z = true;
            }
            showMenuIcon(z);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateDrawer();
        boolean reachSplicSize = SplitUtils.reachSplitSize(this);
        if (getIntent() != null && getIntent().getBooleanExtra("show_drawer_menu", false)) {
            showMenuIcon(!reachSplicSize);
        }
        Log.d("SettingsDrawerActivity", "onConfigurationChanged " + reachSplicSize + ",mHideMenuItems=" + this.mHideMenuItems);
        if (reachSplicSize && newConfig.orientation == 2) {
            this.mHideMenuItems = false;
            setDrawerFeatrue(false);
        }
    }

    private void setTargarIntentOrCancelSplit(Intent intent) {
        if (intent != null) {
            if (SplitUtils.notSupportSplit(intent)) {
                SplitUtils.cancelSplit(intent, this);
            } else {
                SplitUtils.setTargetIntent(intent, this);
            }
        }
    }

    protected void reportTileClicked(Tile tile) {
        if (tile != null && tile.intent != null) {
            ComponentName cn = tile.intent.getComponent();
            if (cn != null) {
                ItemUseStat.getInstance().handleClick(this, 2, "drawer", ItemUseStat.getShortName(cn.getClassName()));
            }
        }
    }
}
