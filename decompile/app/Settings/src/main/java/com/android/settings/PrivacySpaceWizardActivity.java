package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnApplyWindowInsetsListener;
import android.view.View.OnClickListener;
import android.view.WindowInsets;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.navigation.NaviUtils;
import com.android.settings.views.pagerHelper.PagerHelperAdapter;
import com.android.settings.views.pagerHelper.PagerHelperChangeListener;
import com.android.settings.views.pagerHelper.PagerHelperSpotsView;

public class PrivacySpaceWizardActivity extends Activity implements OnClickListener {
    private long mChallenge;
    private boolean mHasChallenge;
    private PrivacySpaceSettingsHelper mHelper;
    private TextView mNextButton;
    private WizardkWindowInsetsListener mWindowListener = new WizardkWindowInsetsListener();

    private static class WizardkWindowInsetsListener implements OnApplyWindowInsetsListener {
        private WizardkWindowInsetsListener() {
        }

        public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
            if (!(v == null || insets == null)) {
                v.setPadding(0, insets.getSystemWindowInsetTop(), 0, 0);
            }
            return insets;
        }
    }

    private int[] getDrawables() {
        if (NaviUtils.isFrontFingerNaviEnabled()) {
            return new int[]{2130838578, 2130838580};
        }
        return new int[]{2130838579, 2130838580};
    }

    private int[] getSummaries() {
        return new int[]{0, 0};
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHelper = new PrivacySpaceSettingsHelper((Activity) this);
        if (UserHandle.myUserId() == 0 && this.mHelper.canAddPrivacyUser()) {
            initParams();
            prepareContentView();
            updateContentView();
            return;
        }
        finish();
    }

    protected void onResume() {
        super.onResume();
        Utils.hideNavigationBar(getWindow(), 2050);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 16908332) {
            return super.onOptionsItemSelected(item);
        }
        finish();
        return true;
    }

    private void initParams() {
        Intent intent = getIntent();
        this.mHasChallenge = intent.getBooleanExtra("has_challenge", false);
        this.mChallenge = intent.getLongExtra("challenge", 0);
    }

    private void prepareContentView() {
        if (getActionBar() != null) {
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }
        setContentView(2130969031);
        LinearLayout parentView = (LinearLayout) findViewById(2131887012);
        if (parentView != null) {
            parentView.setFitsSystemWindows(true);
            parentView.setOnApplyWindowInsetsListener(this.mWindowListener);
        }
    }

    private void updateContentView() {
        initViewPager();
        TextView prevButton = (TextView) findViewById(2131886328);
        if (prevButton != null) {
            prevButton.setVisibility(8);
        }
        this.mNextButton = (TextView) findViewById(2131886329);
        if (this.mNextButton != null) {
            this.mNextButton.setText(2131626195);
            this.mNextButton.setVisibility(0);
            this.mNextButton.setOnClickListener(this);
        }
    }

    private void initViewPager() {
        PagerHelperAdapter adapter = new PagerHelperAdapter(this, getDrawables(), getSummaries(), null);
        PagerHelperSpotsView spotView = (PagerHelperSpotsView) findViewById(2131886711);
        spotView.setPageCount(adapter.getCount());
        PagerHelperChangeListener pageChangeListener = new PagerHelperChangeListener(spotView);
        ViewPager viewPager = (ViewPager) findViewById(2131886710);
        viewPager.setAdapter(adapter);
        viewPager.setOnPageChangeListener(pageChangeListener);
        viewPager.setCurrentItem(0);
    }

    public void onClick(View v) {
        if (v != this.mNextButton) {
            return;
        }
        if (this.mHelper.isOwnerSecure()) {
            this.mHelper.launchChooseLockSettingsForPrivacyUser(this.mHasChallenge, this.mChallenge);
        } else {
            this.mHelper.launchChooseLockSettingsForMainUser(getIntent());
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 203 && (resultCode == -1 || resultCode == 1)) {
            setResult(-1, data);
            finish();
        }
        if (requestCode == 31 && resultCode == -1) {
            setResult(-1, data);
            finish();
        }
    }
}
