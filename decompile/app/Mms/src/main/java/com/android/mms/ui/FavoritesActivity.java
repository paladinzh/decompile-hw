package com.android.mms.ui;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import com.google.android.gms.R;
import com.huawei.mms.ui.HwBaseActivity;

public class FavoritesActivity extends HwBaseActivity {
    private FavoritesFragment mFragment;
    private boolean mIsFromLauncher = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mIsFromLauncher = getIntent().getBooleanExtra("is_from_launcher", false);
        setProgressBarVisibility(false);
        getWindow().setSoftInputMode(18);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        this.mFragment = new FavoritesFragment();
        this.mFragment.setController(new ControllerImpl(this, this.mFragment));
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(16908290, this.mFragment, "Mms_UI_FAV");
        transaction.commit();
    }

    public void onBackPressed() {
        if (!this.mFragment.onBackPressed()) {
            super.onBackPressed();
        }
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        this.mFragment.onNewIntent(intent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                if (!this.mIsFromLauncher) {
                    onBackPressed();
                    break;
                }
                Intent itt = new Intent(this, ConversationList.class);
                itt.setAction("android.intent.action.MAIN");
                startActivity(itt);
                overridePendingTransition(R.anim.activity_from_launcher_enter, R.anim.activity_from_launcher_exit);
                finish();
                break;
        }
        return this.mFragment.onOptionsItemSelected(item);
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (!(this.mFragment == null || this.mFragment.mActionMode == null)) {
                    this.mFragment.mActionMode.finish();
                    this.mFragment.mActionMode = null;
                    return true;
                }
        }
        return super.dispatchTouchEvent(event);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.conversation_list_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
