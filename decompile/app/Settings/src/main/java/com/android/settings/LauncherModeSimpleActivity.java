package com.android.settings;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settingslib.drawer.SettingsDrawerActivity;
import java.util.ArrayList;
import java.util.List;

public class LauncherModeSimpleActivity extends SettingsDrawerActivity implements Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String screenTitle = context.getResources().getString(2131627487);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = screenTitle;
            data.screenTitle = screenTitle;
            data.intentAction = "com.android.settings.LauncherModeSimpleActivity";
            data.intentTargetPackage = "com.android.settings";
            data.intentTargetClass = "com.android.settings.LauncherModeSimpleActivity";
            if (SystemProperties.getBoolean("ro.config.simple_mode", true)) {
                result.add(data);
            }
            return result;
        }
    };

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(2130968840);
        Window win = getWindow();
        win.clearFlags(67108864);
        win.addFlags(Integer.MIN_VALUE);
        initActionBar();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        AlphaStateListDrawable drawable = new AlphaStateListDrawable();
        if (Utils.isSimpleModeOn()) {
            drawable.addState(new int[0], getResources().getDrawable(2130838283));
            menu.add(0, 2, 0, getString(2131628597)).setIcon(drawable).setShowAsAction(2);
        } else {
            drawable.addState(new int[0], getResources().getDrawable(2130838278));
            menu.add(0, 1, 0, getString(2131627449)).setIcon(drawable).setShowAsAction(2);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                applySimpleUiMode(this, true);
                finish();
                break;
            case 2:
                exitSimpleUiMode(this, true);
                break;
            case 16908332:
                onBackPressed();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public static void applySimpleUiMode(Context context, boolean shouldChangeAndRestart) {
        if (context != null) {
            LauncherModeSettingsActivity.changeUIMode(context, 2, shouldChangeAndRestart);
            LauncherModeSettingsActivity.changeFontSize(context, true);
            System.putInt(context.getContentResolver(), "Simple mode", 1);
        }
    }

    public static void exitSimpleUiMode(Context context, boolean shouldChangeAndRestart) {
        if (context != null) {
            LauncherModeSettingsActivity.changeUIMode(context, Secure.getInt(context.getContentResolver(), "launcher_record", 0), shouldChangeAndRestart);
            LauncherModeSettingsActivity.changeFontSize(context, false);
            System.putInt(context.getContentResolver(), "Simple mode", 0);
        }
    }

    private void initActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayUseLogoEnabled(false);
        actionBar.setTitle(2131628596);
    }
}
