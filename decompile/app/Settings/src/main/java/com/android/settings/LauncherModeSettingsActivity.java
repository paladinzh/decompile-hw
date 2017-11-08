package com.android.settings;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.RadioButton;
import com.android.settings.dashboard.SummaryLoader;
import com.android.settings.dashboard.SummaryLoader.SummaryProviderFactory;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import com.huawei.android.content.res.ConfigurationEx;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.List;

public class LauncherModeSettingsActivity extends SettingsDrawerActivity implements OnCheckedChangeListener, OnClickListener, Indexable, OnTouchListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            Resources res = context.getResources();
            String screenTitle = res.getString(2131627488);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.LauncherModeSettingsActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.LauncherModeSettingsActivity";
            result.add(data);
            screenTitle = res.getString(2131627486);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.LauncherModeSettingsActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.LauncherModeSettingsActivity";
            result.add(data);
            screenTitle = res.getString(2131627448);
            data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.LauncherModeSettingsActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.LauncherModeSettingsActivity";
            result.add(data);
            return result;
        }
    };
    public static final SummaryProviderFactory SUMMARY_PROVIDER_FACTORY = new SummaryProviderFactory() {
        public com.android.settings.dashboard.SummaryLoader.SummaryProvider createSummaryProvider(Activity activity, SummaryLoader summaryLoader) {
            return new SummaryProvider(activity, summaryLoader);
        }
    };
    private static volatile String[] entryvalues_font_size;
    private static Context mContext;
    private static HwCustLauncherModeSettingsActivity mCustLauncherModeSettingsActivity = ((HwCustLauncherModeSettingsActivity) HwCustUtils.createObj(HwCustLauncherModeSettingsActivity.class, new Object[0]));
    private static ComponentName mDrawerhome = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.drawer.DrawerLauncher");
    private static PackageManager mPackageManager;
    private static ComponentName mSimpleui = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.simpleui.SimpleUILauncher");
    private static ComponentName mUnihome = new ComponentName("com.huawei.android.launcher", "com.huawei.android.launcher.unihome.UniHomeLauncher");
    private ImageView mDrawerModeImage;
    private RadioButton mDrawerModeRadioButton;
    private View mDrawerUISector;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1000:
                    LauncherModeSettingsActivity.this.update();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsNormalHomeCheckBoxChecked = false;
    private Bitmap mNormalImage;
    private ImageView mNormalModeImage;
    private RadioButton mNormalModeRadioButton;
    private View mNormalUISector;

    private static class SummaryProvider implements com.android.settings.dashboard.SummaryLoader.SummaryProvider {
        private final Context mContext;
        private final SummaryLoader mSummaryLoader;

        public SummaryProvider(Context context, SummaryLoader summaryLoader) {
            this.mContext = context;
            this.mSummaryLoader = summaryLoader;
        }

        public void setListening(boolean listening) {
            if (!listening) {
                return;
            }
            if (Utils.getLauncherType() == 4) {
                this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627488));
            } else if (Utils.getLauncherType() == 1) {
                this.mSummaryLoader.setSummary(this, this.mContext.getString(2131627486));
            } else {
                this.mSummaryLoader.setSummary(this, "");
            }
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(2130968839);
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        initView();
        initActionBar();
    }

    protected void onResume() {
        super.onResume();
        refreshView();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mNormalImage != null && !this.mNormalImage.isRecycled()) {
            this.mNormalImage.recycle();
            this.mNormalImage = null;
        }
    }

    private void update() {
        int launchertype = Utils.getLauncherType();
        if (launchertype != 4 && this.mDrawerModeRadioButton.isChecked()) {
            changeUIMode(this, 4, true);
            Secure.putInt(getContentResolver(), "launcher_record", 4);
            finish();
        } else if (launchertype != 1 && this.mNormalModeRadioButton.isChecked()) {
            changeUIMode(this, 1, true);
            Secure.putInt(getContentResolver(), "launcher_record", 1);
            finish();
        }
    }

    public void onClick(View view) {
        if (view == this.mNormalUISector) {
            this.mNormalModeRadioButton.setChecked(true);
            this.mDrawerModeRadioButton.setChecked(false);
            this.mIsNormalHomeCheckBoxChecked = true;
            if (!this.mNormalModeRadioButton.isChecked() && this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(1000, 100);
            }
        } else if (view == this.mDrawerUISector) {
            this.mNormalModeRadioButton.setChecked(false);
            this.mDrawerModeRadioButton.setChecked(true);
            this.mIsNormalHomeCheckBoxChecked = false;
            if (!this.mDrawerModeRadioButton.isChecked() && this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(1000, 100);
            }
        }
    }

    public void onCheckedChanged(CompoundButton checkBox, boolean isChecked) {
        if (isChecked) {
            if (checkBox == this.mNormalModeRadioButton) {
                this.mIsNormalHomeCheckBoxChecked = true;
                this.mDrawerModeRadioButton.setClickable(true);
                this.mDrawerModeRadioButton.setChecked(false);
                this.mNormalModeRadioButton.setClickable(false);
            } else {
                this.mIsNormalHomeCheckBoxChecked = false;
                this.mNormalModeRadioButton.setClickable(true);
                this.mNormalModeRadioButton.setChecked(false);
                this.mDrawerModeRadioButton.setClickable(false);
            }
            if (this.mHandler != null) {
                this.mHandler.sendEmptyMessageDelayed(1000, 100);
            }
        }
    }

    public boolean onTouch(View view, MotionEvent event) {
        View clickview;
        Drawable draw = getResources().getDrawable(2130838535);
        if (view == this.mNormalModeRadioButton && !this.mNormalModeRadioButton.isChecked()) {
            clickview = this.mNormalUISector;
        } else if (view == this.mDrawerModeRadioButton && !this.mDrawerModeRadioButton.isChecked()) {
            clickview = this.mDrawerUISector;
        } else if (view != this.mNormalUISector && view != this.mDrawerUISector) {
            return false;
        } else {
            clickview = view;
        }
        switch (event.getAction()) {
            case 1:
                clickview.setBackground(null);
                break;
            case 3:
                clickview.setBackground(null);
                break;
            case 4:
                clickview.setBackground(null);
                break;
            default:
                clickview.setBackground(draw);
                break;
        }
        return false;
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setTitle(2131627448);
    }

    private void initView() {
        this.mNormalUISector = findViewById(2131886730);
        this.mNormalUISector.setOnClickListener(this);
        this.mNormalUISector.setOnTouchListener(this);
        this.mDrawerUISector = findViewById(2131886737);
        this.mDrawerUISector.setOnClickListener(this);
        this.mDrawerUISector.setOnTouchListener(this);
        this.mNormalModeRadioButton = (RadioButton) findViewById(2131886734);
        this.mNormalModeRadioButton.setOnCheckedChangeListener(this);
        this.mNormalModeRadioButton.setOnTouchListener(this);
        this.mDrawerModeRadioButton = (RadioButton) findViewById(2131886741);
        this.mDrawerModeRadioButton.setOnCheckedChangeListener(this);
        this.mDrawerModeRadioButton.setOnTouchListener(this);
        this.mNormalModeImage = (ImageView) findViewById(2131886735);
        this.mDrawerModeImage = (ImageView) findViewById(2131886736);
    }

    private void refreshView() {
        if (Utils.getLauncherType() == 4) {
            this.mNormalModeRadioButton.setChecked(false);
            this.mDrawerModeRadioButton.setChecked(true);
        } else if (Utils.getLauncherType() == 1) {
            this.mNormalModeRadioButton.setChecked(true);
            this.mDrawerModeRadioButton.setChecked(false);
        }
        boolean hasNavigationBar = true;
        if ("1".equals(SystemProperties.get("qemu.hw.mainkeys"))) {
            hasNavigationBar = false;
        }
        if (!hasNavigationBar) {
            this.mDrawerModeImage.setImageResource(2130838651);
        }
    }

    public static void changeUIMode(Context context, int launcherMode, boolean shouldChangeAndRestart) {
        if (context != null) {
            mContext = context.getApplicationContext();
            if (mPackageManager == null) {
                mPackageManager = mContext.getPackageManager();
            }
            if (launcherMode == 4) {
                disableMode(mSimpleui);
                disableMode(mUnihome);
                enableMode(mDrawerhome);
            } else if (launcherMode == 1) {
                disableMode(mSimpleui);
                disableMode(mDrawerhome);
                enableMode(mUnihome);
            } else if (launcherMode == 2) {
                disableMode(mUnihome);
                disableMode(mDrawerhome);
                enableMode(mSimpleui);
            }
            ItemUseStat.getInstance().handleClick(context, 2, "LauncherModeSettingsActivity", launcherMode);
            updateConfiguration(launcherMode);
            if (shouldChangeAndRestart) {
                changeAndRestart(launcherMode);
            } else if (mCustLauncherModeSettingsActivity == null || !mCustLauncherModeSettingsActivity.isLauncher3Mode()) {
                changePreferredLauncher("com.huawei.android.launcher");
            } else {
                changePreferredLauncher(mCustLauncherModeSettingsActivity.getLauncher3PackageName());
            }
        }
    }

    public static void disableMode(ComponentName name) {
        if (mPackageManager == null) {
            Log.e("LauncherModeSettings", "disableMode is null && name=" + name);
        } else {
            mPackageManager.setComponentEnabledSetting(name, 2, 0);
        }
    }

    public static void enableMode(ComponentName name) {
        if (mPackageManager == null) {
            Log.e("LauncherModeSettings", "enableMode mPackageManager is null && name=" + name);
            return;
        }
        Log.i("LauncherModeSettings", "enableMode name=" + name);
        mPackageManager.setComponentEnabledSetting(name, 1, 0);
    }

    private static void changeAndRestart(int launcherType) {
        if (mCustLauncherModeSettingsActivity == null || !mCustLauncherModeSettingsActivity.isLauncher3Mode()) {
            changePreferredLauncher("com.huawei.android.launcher");
        } else {
            changePreferredLauncher(mCustLauncherModeSettingsActivity.getLauncher3PackageName());
        }
        if (launcherType == 4) {
            System.putIntForUser(mContext.getContentResolver(), "multiwindow_mode_settings", 1, -2);
            restartComponent(mDrawerhome);
        } else if (launcherType == 1) {
            System.putIntForUser(mContext.getContentResolver(), "multiwindow_mode_settings", 1, -2);
            restartComponent(mUnihome);
        } else if (launcherType == 2) {
            System.putIntForUser(mContext.getContentResolver(), "multiwindow_mode_settings", 0, -2);
            restartComponent(mSimpleui);
        }
    }

    public static void changePreferredLauncher(String pkgName) {
        if (mPackageManager != null) {
            int i;
            List<ResolveInfo> resolveInfos = mPackageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").addCategory("android.intent.category.HOME").addCategory("android.intent.category.DEFAULT"), 0);
            for (i = 0; i < resolveInfos.size(); i++) {
                ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
                if (resolveInfo != null) {
                    mPackageManager.clearPackagePreferredActivities(resolveInfo.activityInfo.packageName);
                }
            }
            int sz = resolveInfos.size();
            int find = -1;
            ComponentName[] set = new ComponentName[sz];
            for (i = 0; i < sz; i++) {
                ResolveInfo info = (ResolveInfo) resolveInfos.get(i);
                set[i] = new ComponentName(info.activityInfo.packageName, info.activityInfo.name);
                if (info.activityInfo.packageName.equals(pkgName)) {
                    find = i;
                }
            }
            if (find != -1) {
                IntentFilter inf = new IntentFilter("android.intent.action.MAIN");
                inf.addCategory("android.intent.category.HOME");
                inf.addCategory("android.intent.category.DEFAULT");
                mPackageManager.addPreferredActivity(inf, 1048576, set, set[find]);
            }
        }
    }

    public static void restartComponent(ComponentName comp) {
        if (mContext != null) {
            ActivityManager activityManager = (ActivityManager) mContext.getSystemService("activity");
            if (mCustLauncherModeSettingsActivity == null || !mCustLauncherModeSettingsActivity.isLauncher3Mode()) {
                activityManager.forceStopPackage("com.huawei.android.launcher");
            } else {
                activityManager.forceStopPackage(mCustLauncherModeSettingsActivity.getLauncher3PackageName());
            }
            Intent intent = new Intent("android.intent.action.MAIN", null);
            intent.addCategory("android.intent.category.HOME");
            intent.addFlags(270532608);
            try {
                mContext.startActivity(intent);
            } catch (Exception e) {
                Log.e("LauncherModeSettings", "restartComponent e = " + e.toString());
            }
        }
    }

    public static void updateConfiguration(int launcherMode) {
        Configuration curConfig = new Configuration();
        try {
            curConfig.updateFrom(ActivityManagerNative.getDefault().getConfiguration());
            new ConfigurationEx(curConfig).getExtraConfig().simpleuiMode = launcherMode;
            Log.i("LauncherModeSettings", "updateConfiguration launcherMode = " + launcherMode);
            ActivityManagerNative.getDefault().updatePersistentConfiguration(curConfig);
        } catch (Exception e) {
            Log.e("LauncherModeSettings", "updateConfiguration e = " + e.toString());
        } catch (NoSuchFieldError err) {
            Log.e("LauncherModeSettings", "updateConfiguration err = " + err.toString());
        }
    }

    public static void changeFontSize(Context context, boolean isSimpleModeOn) {
        if (context != null) {
            Configuration curConfig = new Configuration();
            if (entryvalues_font_size == null || entryvalues_font_size.length == 0) {
                entryvalues_font_size = context.getResources().getStringArray(2131361839);
            }
            if (!isSimpleModeOn) {
                try {
                    curConfig.fontScale = Float.parseFloat(entryvalues_font_size[1]);
                } catch (RemoteException e) {
                    Log.e("LauncherModeSettings", "Unable to save font size" + e.toString());
                } catch (IndexOutOfBoundsException e2) {
                    Log.e("LauncherModeSettings", "Unable to switch to extra huge font size" + e2.toString());
                }
            } else if (mCustLauncherModeSettingsActivity == null || !mCustLauncherModeSettingsActivity.isNormalFontSize(context)) {
                curConfig.fontScale = Float.parseFloat(entryvalues_font_size[4]);
            } else {
                curConfig.fontScale = Float.parseFloat(entryvalues_font_size[1]);
            }
            ActivityManagerNative.getDefault().updatePersistentConfiguration(curConfig);
        }
    }
}
