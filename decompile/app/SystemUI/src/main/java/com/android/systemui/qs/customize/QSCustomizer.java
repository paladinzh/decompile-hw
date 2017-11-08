package com.android.systemui.qs.customize;

import android.content.Context;
import android.content.res.Configuration;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;
import android.widget.Toolbar.OnMenuItemClickListener;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.qs.QSContainer;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.statusbar.phone.NotificationsQuickSettingsContainer;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.KeyguardMonitor.Callback;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;
import java.util.List;

public class QSCustomizer extends LinearLayout implements OnMenuItemClickListener {
    private boolean isShown;
    private TextView mBackView;
    private int mColumns = 4;
    private ViewGroup mContent;
    private boolean mCustomizing;
    private QSTileHost mHost;
    private final Callback mKeyguardCallback = new Callback() {
        public void onKeyguardChanged() {
            if (QSCustomizer.this.mHost.getKeyguardMonitor().isShowing()) {
                QSCustomizer.this.hide(0, 0);
            }
        }
    };
    private GridLayoutManager mLayout;
    private NotificationsQuickSettingsContainer mNotifQsContainer;
    private PhoneStatusBar mPhoneStatusBar;
    private QSContainer mQsContainer;
    private RecyclerView mRecyclerView;
    private TextView mResetView;
    private TileAdapter mTileAdapter;
    private Toolbar mToolbar;

    public QSCustomizer(Context context, AttributeSet attrs) {
        super(context, attrs);
        int themeID = this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
        if (themeID != 0) {
            this.mContext.setTheme(themeID);
        }
        LayoutInflater.from(getContext()).inflate(R.layout.qs_customize_panel_content, this);
        this.mToolbar = (Toolbar) findViewById(16909291);
        TypedValue value = new TypedValue();
        this.mContext.getTheme().resolveAttribute(16843531, value, true);
        this.mToolbar.setNavigationIcon(getResources().getDrawable(value.resourceId, this.mContext.getTheme()));
        this.mToolbar.setNavigationOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                QSCustomizer.this.hide(((int) v.getX()) + (v.getWidth() / 2), ((int) v.getY()) + (v.getHeight() / 2));
            }
        });
        this.mToolbar.setOnMenuItemClickListener(this);
        this.mToolbar.getMenu().add(0, 1, 0, this.mContext.getString(17040491));
        this.mToolbar.setTitle(R.string.qs_edit);
        this.mRecyclerView = (RecyclerView) findViewById(16908298);
        this.mTileAdapter = new TileAdapter(getContext());
        this.mColumns = getResources().getInteger(PerfAdjust.getQuickSettingsNumColumns());
        this.mTileAdapter.setColumns(this.mColumns);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
        this.mTileAdapter.getItemTouchHelper().attachToRecyclerView(this.mRecyclerView);
        this.mLayout = new GridLayoutManager(getContext(), this.mColumns);
        this.mLayout.setSpanSizeLookup(this.mTileAdapter.getSizeLookup());
        this.mRecyclerView.setLayoutManager(this.mLayout);
        this.mRecyclerView.addItemDecoration(this.mTileAdapter.getItemDecoration());
        DefaultItemAnimator animator = new DefaultItemAnimator();
        animator.setMoveDuration(150);
        this.mRecyclerView.setItemAnimator(animator);
        this.mBackView = (TextView) findViewById(R.id.qs_button_back);
        this.mBackView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                QSCustomizer.this.hide(((int) v.getX()) + (v.getWidth() / 2), ((int) v.getY()) + (v.getHeight() / 2));
            }
        });
        this.mResetView = (TextView) findViewById(R.id.qs_button_reset);
        this.mResetView.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                BDReporter.c(QSCustomizer.this.mContext, 354);
                MetricsLogger.action(QSCustomizer.this.getContext(), 359);
                QSCustomizer.this.reset();
            }
        });
        this.mContent = (ViewGroup) findViewById(R.id.qs_customize_content);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        boolean shouldShow = true;
        int i = 0;
        super.onConfigurationChanged(newConfig);
        View navBackdrop = findViewById(R.id.nav_bar_background);
        if (navBackdrop != null) {
            if (newConfig.smallestScreenWidthDp < 600 && newConfig.orientation == 2) {
                shouldShow = false;
            }
            if (!shouldShow) {
                i = 8;
            }
            navBackdrop.setVisibility(i);
        }
        this.mContent.setPadding(getResources().getDimensionPixelSize(R.dimen.qs_customizer_padding_left), getPaddingTop(), getResources().getDimensionPixelSize(R.dimen.qs_customizer_padding_right), getPaddingBottom());
        this.mColumns = getResources().getInteger(PerfAdjust.getQuickSettingsNumColumns());
        this.mLayout.setSpanCount(this.mColumns);
        this.mTileAdapter.setColumns(this.mColumns);
        LayoutParams lp = this.mRecyclerView.getLayoutParams();
        lp.width = -1;
        this.mRecyclerView.setLayoutParams(lp);
        this.mTileAdapter.notifyDataSetChanged();
    }

    public void setHost(QSTileHost host) {
        this.mHost = host;
        this.mPhoneStatusBar = host.getPhoneStatusBar();
        this.mTileAdapter.setHost(host);
    }

    public void updateCustomPanel(State state, String spec) {
        if (this.mTileAdapter != null) {
            this.mTileAdapter.updateTileState(state, spec);
        }
    }

    public void setContainer(NotificationsQuickSettingsContainer notificationsQsContainer) {
        this.mNotifQsContainer = notificationsQsContainer;
    }

    public void setQsContainer(QSContainer qsContainer) {
        this.mQsContainer = qsContainer;
    }

    public void show(int x, int y) {
        HwLog.i("QSCustomizer", "show:x=" + x + ", y=" + y + ", isShown=" + this.isShown);
        if (!this.isShown) {
            MetricsLogger.visible(getContext(), 358);
            this.isShown = true;
            setTileSpecs();
            setVisibility(0);
            this.mContent.setVisibility(0);
            setCustomizing(true);
            animateShow(this.mRecyclerView, true);
            new TileQueryHelper(this.mContext, this.mHost).setListener(this.mTileAdapter);
            this.mNotifQsContainer.setCustomizerAnimating(false);
            this.mNotifQsContainer.setCustomizerShowing(true);
            this.mQsContainer.setCustomizerShowing(true);
            announceForAccessibility(this.mContext.getString(R.string.accessibility_desc_quick_settings_edit));
            this.mHost.getKeyguardMonitor().addCallback(this.mKeyguardCallback);
            refreshLayoutIfNeed();
        }
    }

    public void hide(int x, int y) {
        boolean enabled = true;
        HwLog.i("QSCustomizer", "hide:x=" + x + ", y=" + y + ", isShown=" + this.isShown);
        if (this.isShown) {
            MetricsLogger.hidden(getContext(), 358);
            this.isShown = false;
            this.mToolbar.dismissPopupMenus();
            setCustomizing(false);
            save();
            animateHide(this.mRecyclerView, true);
            this.mContent.setVisibility(8);
            setVisibility(8);
            this.mNotifQsContainer.setCustomizerAnimating(false);
            this.mNotifQsContainer.setCustomizerShowing(false);
            this.mQsContainer.setCustomizerShowing(false);
            if (TextUtils.isEmpty(Secure.getString(this.mContext.getContentResolver(), "enabled_accessibility_services"))) {
                enabled = false;
            }
            if (enabled) {
                HwLog.i("QSCustomizer", "set notification_stack_scroller VISIBLE when quit qsEditer in talkback");
                ((NotificationStackScrollLayout) getRootView().findViewById(R.id.notification_stack_scroller)).setVisibility(0);
            }
            announceForAccessibility(this.mContext.getString(R.string.accessibility_desc_quick_settings));
            this.mHost.getKeyguardMonitor().removeCallback(this.mKeyguardCallback);
        }
    }

    private void setCustomizing(boolean customizing) {
        HwLog.i("QSCustomizer", "setCustomizing:" + customizing);
        this.mCustomizing = customizing;
        this.mQsContainer.notifyCustomizeChanged();
    }

    public boolean isCustomizing() {
        return this.mCustomizing;
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                MetricsLogger.action(getContext(), 359);
                reset();
                break;
        }
        return false;
    }

    private void reset() {
        ArrayList<String> tiles = new ArrayList();
        String defCustTile = System.getString(this.mContext.getContentResolver(), "def_cust_tile");
        String defTiles;
        if (TextUtils.isEmpty(defCustTile)) {
            defTiles = this.mContext.getString(PerfAdjust.getQuickSettingsTilesDefault());
        } else {
            defTiles = defCustTile;
        }
        for (String tile : defTiles.split(",")) {
            tiles.add(tile);
        }
        this.mTileAdapter.setTileSpecs(tiles);
    }

    private void setTileSpecs() {
        List<String> specs = new ArrayList();
        for (QSTile tile : this.mHost.getTiles()) {
            specs.add(tile.getTileSpec());
            HwLog.i("QSCustomizer", "add tile:" + tile.getTileLabel());
        }
        this.mTileAdapter.setTileSpecs(specs);
        this.mRecyclerView.setAdapter(this.mTileAdapter);
    }

    private void save() {
        HwLog.i("QSCustomizer", "save:");
        this.mTileAdapter.saveSpecs(this.mHost);
    }

    public boolean isShown() {
        return this.isShown;
    }

    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return super.onInterceptTouchEvent(ev);
    }

    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private void animateHide(final View v, boolean animate) {
        v.animate().cancel();
        if (animate) {
            v.animate().alpha(0.0f).setDuration(160).setStartDelay(0).setInterpolator(Interpolators.ALPHA_OUT).withEndAction(new Runnable() {
                public void run() {
                    v.setVisibility(8);
                }
            });
            return;
        }
        v.setAlpha(0.0f);
        v.setVisibility(8);
    }

    private void animateShow(View v, boolean animate) {
        v.animate().cancel();
        v.setAlpha(0.0f);
        v.setVisibility(0);
        if (animate) {
            v.animate().alpha(1.0f).setDuration(200).setInterpolator(Interpolators.ALPHA_IN).setStartDelay(100).withEndAction(null);
        } else {
            v.setAlpha(1.0f);
        }
    }

    private void refreshLayoutIfNeed() {
        int column = getResources().getInteger(PerfAdjust.getQuickSettingsNumColumns());
        if (this.mColumns != column) {
            this.mContent.setPadding(getResources().getDimensionPixelSize(R.dimen.qs_customizer_padding_left), getPaddingTop(), getResources().getDimensionPixelSize(R.dimen.qs_customizer_padding_right), getPaddingBottom());
            this.mColumns = column;
            this.mLayout.setSpanCount(this.mColumns);
            this.mTileAdapter.setColumns(this.mColumns);
            LayoutParams lp = this.mRecyclerView.getLayoutParams();
            lp.width = -1;
            this.mRecyclerView.setLayoutParams(lp);
            this.mTileAdapter.notifyDataSetChanged();
        }
    }
}
