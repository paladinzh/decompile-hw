package com.android.systemui.qs;

import android.content.ComponentName;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.LinearLayout;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSDetailClipper.IDetailsCallback;
import com.android.systemui.qs.QSTile.BooleanState;
import com.android.systemui.qs.QSTile.DetailAdapter;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.settings.HwBrightnessController;
import com.android.systemui.settings.ToggleSlider;
import com.android.systemui.statusbar.phone.NotificationPanelView;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.policy.BrightnessMirrorController;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;

public class QSPanel extends LinearLayout implements Tunable, com.android.systemui.qs.QSTile.Host.Callback, IDetailsCallback {
    private HwBrightnessController mBrightnessController;
    private BrightnessMirrorController mBrightnessMirrorController;
    protected final View mBrightnessView;
    private Callback mCallback;
    protected final Context mContext;
    private QSCustomizer mCustomizePanel;
    private Record mDetailRecord;
    protected boolean mExpanded;
    private boolean mGridContentVisible;
    private final H mHandler;
    protected QSTileHost mHost;
    protected boolean mListening;
    private int mMinHeight;
    protected final ArrayList<TileRecord> mRecords;
    private final ArrayList<TileRecord> mSuperPowerSaveRecords;
    protected QSTileLayout mTileLayout;
    protected final View mTileView;

    public interface QSTileLayout {
        void addTile(TileRecord tileRecord);

        int getOffsetTop(TileRecord tileRecord);

        void removeTile(TileRecord tileRecord);

        void setListening(boolean z);

        boolean updateResources();
    }

    public interface Callback {
        void onScanStateChanged(boolean z);

        void onShowingDetail(DetailAdapter detailAdapter, int i, int i2);

        void onToggleStateChanged(boolean z);
    }

    final /* synthetic */ class -void_setupTileLayout__LambdaImpl0 implements OnClickListener {
        private /* synthetic */ QSPanel val$this;

        public /* synthetic */ -void_setupTileLayout__LambdaImpl0(QSPanel qSPanel) {
            this.val$this = qSPanel;
        }

        public void onClick(View arg0) {
            this.val$this.-com_android_systemui_qs_QSPanel_lambda$1(arg0);
        }
    }

    private class H extends Handler {
        private H() {
        }

        public void handleMessage(Message msg) {
            boolean z = true;
            if (msg.what == 1) {
                QSPanel qSPanel = QSPanel.this;
                Record record = (Record) msg.obj;
                if (msg.arg1 == 0) {
                    z = false;
                }
                qSPanel.handleShowDetail(record, z);
            }
        }
    }

    protected static class Record {
        DetailAdapter detailAdapter;
        int x;
        int y;

        protected Record() {
        }
    }

    public static final class TileRecord extends Record {
        public com.android.systemui.qs.QSTile.Callback callback;
        public boolean scanState;
        public QSTile<?> tile;
        public QSTileBaseView tileView;
    }

    public QSPanel(Context context) {
        this(context, null);
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    public QSPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mRecords = new ArrayList();
        this.mSuperPowerSaveRecords = new ArrayList();
        this.mHandler = new H();
        this.mGridContentVisible = true;
        this.mContext = context;
        setOrientation(1);
        setupTileLayout();
        this.mBrightnessView = LayoutInflater.from(context).inflate(R.layout.quick_settings_brightness_dialog, this, false);
        this.mBrightnessView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
        addView(this.mBrightnessView);
        updateResources();
        this.mBrightnessController = new HwBrightnessController(getContext(), (ToggleSlider) findViewById(R.id.brightness_slider));
        this.mTileView = (View) this.mTileLayout;
        this.mMinHeight = getResources().getDimensionPixelSize(R.dimen.qs_panel_min_height);
        if (this.mTileLayout instanceof PagedTileLayout) {
            ((PagedTileLayout) this.mTileLayout).setBrightnessView(this.mBrightnessView);
        }
    }

    public View getBrightnessView() {
        return this.mBrightnessView;
    }

    protected void setupTileLayout() {
        this.mTileLayout = (QSTileLayout) LayoutInflater.from(this.mContext).inflate(R.layout.qs_paged_tile_layout, this, false);
        this.mTileLayout.setListening(this.mListening);
        addView((View) this.mTileLayout);
        findViewById(16908291).setOnClickListener(new -void_setupTileLayout__LambdaImpl0());
    }

    /* synthetic */ void -com_android_systemui_qs_QSPanel_lambda$1(View view) {
        this.mHost.startRunnableDismissingKeyguard(new QSPanel$-void_-com_android_systemui_qs_QSPanel_lambda$1_android_view_View_view_LambdaImpl0(this, view));
    }

    /* synthetic */ void -com_android_systemui_qs_QSPanel_lambda$2(View view) {
        showEdit(view);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        updateResources();
    }

    public boolean isShowingCustomize() {
        return this.mCustomizePanel != null ? this.mCustomizePanel.isCustomizing() : false;
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable((Tunable) this, "qs_show_brightness");
        if (this.mHost != null) {
            setTiles(this.mHost.getTiles());
        }
    }

    protected void onDetachedFromWindow() {
        TunerService.get(this.mContext).removeTunable(this);
        this.mHost.removeCallback(this);
        for (TileRecord record : this.mRecords) {
            record.tile.removeCallbacks();
        }
        super.onDetachedFromWindow();
    }

    public void onTilesChanged() {
        HwLog.i("QSPanel", "onTilesChanged");
        setTiles(this.mHost.getTiles());
    }

    public void onTuningChanged(String key, String newValue) {
        int i = 0;
        if ("qs_show_brightness".equals(key)) {
            View view = this.mBrightnessView;
            if (newValue != null && Integer.parseInt(newValue) == 0) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    public void openDetails(String subPanel) {
        showDetailAdapter(true, getTile(subPanel).getDetailAdapter(), new int[]{getWidth() / 2, 0});
    }

    private QSTile<?> getTile(String subPanel) {
        for (int i = 0; i < this.mRecords.size(); i++) {
            if (subPanel.equals(((TileRecord) this.mRecords.get(i)).tile.getTileSpec())) {
                return ((TileRecord) this.mRecords.get(i)).tile;
            }
        }
        return this.mHost.createTile(subPanel);
    }

    public ArrayList<TileRecord> getSuperPowerModeRecord() {
        this.mSuperPowerSaveRecords.clear();
        QSTile[] superPowerTempTiles = new QSTile[4];
        for (QSTile tile : this.mHost.getAllTiles()) {
            if (tile.getTileSpec().equals("wifi")) {
                superPowerTempTiles[0] = tile;
            } else if (tile.getTileSpec().equals("bt")) {
                superPowerTempTiles[1] = tile;
            } else if (tile.getTileSpec().equals("data")) {
                superPowerTempTiles[2] = tile;
            } else if (tile.getTileSpec().equals("location")) {
                superPowerTempTiles[3] = tile;
            }
        }
        for (int tilePosition = 0; tilePosition < superPowerTempTiles.length; tilePosition++) {
            if (superPowerTempTiles[tilePosition] == null) {
                HwLog.e("QSPanel", "getSuperPowerModeRecord: mSuperPowerSaveTileInfo[" + tilePosition + "]" + " is null");
                return null;
            }
            addTile(superPowerTempTiles[tilePosition], false);
        }
        HwLog.i("QSPanel", "getSuperPowerModeRecord: mSuperPowerSaveRecords.size() =" + this.mSuperPowerSaveRecords.size());
        return this.mSuperPowerSaveRecords;
    }

    public void setBrightnessMirror(BrightnessMirrorController c) {
        this.mBrightnessMirrorController = c;
        ToggleSlider brightnessSlider = (ToggleSlider) findViewById(R.id.brightness_slider);
        brightnessSlider.setMirror((ToggleSlider) c.getMirror().findViewById(R.id.brightness_slider));
        brightnessSlider.setMirrorController(c);
    }

    public void setCallback(Callback callback) {
        this.mCallback = callback;
    }

    public void setHost(QSTileHost host, QSCustomizer customizer) {
        this.mHost = host;
        this.mHost.addCallback(this);
        setTiles(this.mHost.getTiles());
        this.mCustomizePanel = customizer;
        if (this.mCustomizePanel != null) {
            this.mCustomizePanel.setHost(this.mHost);
        }
    }

    public QSTileHost getHost() {
        return this.mHost;
    }

    public void updateResources() {
        this.mMinHeight = getResources().getDimensionPixelSize(R.dimen.qs_panel_min_height);
        for (TileRecord r : this.mRecords) {
            r.tile.clearState();
        }
        if (this.mListening) {
            refreshAllTiles();
        }
        if (this.mTileLayout != null) {
            this.mTileLayout.updateResources();
        }
        LayoutParams lp = (LayoutParams) getLayoutParams();
        if (lp != null) {
            lp.topMargin = getResources().getDimensionPixelOffset(R.dimen.qs_panel_margin_top);
            setLayoutParams(lp);
        }
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mBrightnessMirrorController != null) {
            setBrightnessMirror(this.mBrightnessMirrorController);
        }
    }

    public void setExpanded(boolean expanded) {
        if (this.mExpanded != expanded) {
            this.mExpanded = expanded;
            if (!this.mExpanded && (this.mTileLayout instanceof PagedTileLayout)) {
                ((PagedTileLayout) this.mTileLayout).setCurrentItem(0, false);
            }
            MetricsLogger.visibility(this.mContext, 111, this.mExpanded);
            if (this.mExpanded) {
                logTiles();
            }
        }
    }

    public void registerBrightnessControllerCallback(boolean register) {
        if (this.mBrightnessController != null) {
            HwLog.i("QSPanel", "registerBrightnessControllerCallback::register=" + register);
            if (register) {
                this.mBrightnessController.registerCallbacks();
            } else {
                this.mBrightnessController.unregisterCallbacks();
            }
        }
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
            for (TileRecord r : this.mRecords) {
                r.tile.setListening(this, listening);
            }
            if (this.mListening) {
                refreshAllTiles();
            }
        }
    }

    public void refreshAllTiles() {
        for (TileRecord r : this.mRecords) {
            r.tile.refreshState();
        }
    }

    public void showDetailAdapter(boolean show, DetailAdapter adapter, int[] locationInWindow) {
        int xInWindow = locationInWindow[0];
        int yInWindow = locationInWindow[1];
        ((View) getParent()).getLocationInWindow(locationInWindow);
        Record r = new Record();
        r.detailAdapter = adapter;
        r.x = xInWindow - locationInWindow[0];
        r.y = yInWindow - locationInWindow[1];
        locationInWindow[0] = xInWindow;
        locationInWindow[1] = yInWindow;
        showDetail(show, r);
    }

    protected void showDetail(boolean show, Record r) {
        int i;
        H h = this.mHandler;
        if (show) {
            i = 1;
        } else {
            i = 0;
        }
        h.obtainMessage(1, i, 0, r).sendToTarget();
    }

    public void setTiles(Collection<QSTile<?>> tiles) {
        HwLog.i("QSPanel", "setTiles:" + tiles);
        setTiles(tiles, false);
    }

    public void setTiles(Collection<QSTile<?>> tiles, boolean collapsedView) {
        for (TileRecord record : this.mRecords) {
            this.mTileLayout.removeTile(record);
            record.tile.removeCallback(record.callback);
        }
        this.mRecords.clear();
        for (QSTile<?> tile : tiles) {
            addTile(tile, collapsedView);
        }
    }

    protected void drawTile(TileRecord r, State state) {
        r.tileView.onStateChanged(state);
    }

    protected QSTileBaseView createTileView(QSTile<?> tile, boolean collapsedView) {
        return new QSTileView(this.mContext, tile.createTileView(this.mContext), collapsedView);
    }

    protected boolean shouldShowDetail() {
        return this.mExpanded;
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    protected void addTile(QSTile<?> tile, boolean collapsedView) {
        final TileRecord r = new TileRecord();
        r.tile = tile;
        r.tileView = createTileView(tile, collapsedView);
        com.android.systemui.qs.QSTile.Callback callback = new com.android.systemui.qs.QSTile.Callback() {
            public void onStateChanged(State state) {
                QSPanel.this.drawTile(r, state);
            }

            public void onShowDetail(boolean show) {
                if (QSPanel.this.shouldShowDetail()) {
                    QSPanel.this.showDetail(show, r);
                }
            }

            public void onToggleStateChanged(boolean state) {
                if (QSPanel.this.mDetailRecord == r) {
                    QSPanel.this.fireToggleStateChanged(state);
                }
            }

            public void onScanStateChanged(boolean state) {
                r.scanState = state;
                if (QSPanel.this.mDetailRecord == r) {
                    QSPanel.this.fireScanStateChanged(r.scanState);
                }
            }

            public void onAnnouncementRequested(CharSequence announcement) {
                QSPanel.this.announceForAccessibility(announcement);
            }
        };
        r.tile.addCallback(callback);
        r.callback = callback;
        r.tileView.init(new OnClickListener() {
            public void onClick(View v) {
                HwLog.i("QSPanel", "onTileClick::state=" + r.tile.mState);
                if (r.tile.mState instanceof BooleanState) {
                    BooleanState sState = r.tile.mState;
                    if (!r.tile.getTileSpec().equals("sound")) {
                        BDReporter.e(QSPanel.this.mContext, 356, "CLICK_TYPE:0,BUTTON_NAME:" + r.tile.getTileSpec() + ",state:" + sState.value);
                    }
                } else if (!r.tile.getTileSpec().equals("sound")) {
                    BDReporter.e(QSPanel.this.mContext, 356, "CLICK_TYPE:0,BUTTON_NAME:" + r.tile.getTileSpec());
                }
                QSPanel.this.onTileClick(r.tile);
            }
        }, new OnLongClickListener() {
            public boolean onLongClick(View v) {
                HwLog.i("QSPanel", "onTileLongClick::label=" + r.tile.getTileSpec());
                if (r.tile.mState instanceof BooleanState) {
                    BDReporter.e(QSPanel.this.mContext, 357, "CLICK_TYPE:1,BUTTON_NAME:" + r.tile.getTileSpec() + ",state:" + r.tile.mState.value);
                } else {
                    BDReporter.e(QSPanel.this.mContext, 357, "CLICK_TYPE:1,BUTTON_NAME:" + r.tile.getTileSpec());
                }
                r.tile.longClick();
                return true;
            }
        });
        r.tile.refreshState();
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            this.mSuperPowerSaveRecords.add(r);
            return;
        }
        this.mRecords.add(r);
        if (this.mTileLayout != null) {
            this.mTileLayout.addTile(r);
        }
    }

    public void showEdit(final View v) {
        HwLog.i("QSPanel", "showEdit: " + this.mCustomizePanel.isCustomizing());
        BDReporter.c(getContext(), 25);
        v.post(new Runnable() {
            public void run() {
                boolean enabled;
                NotificationPanelView npv = (NotificationPanelView) QSPanel.this.getRootView().findViewById(R.id.notification_panel);
                NotificationStackScrollLayout stackView = (NotificationStackScrollLayout) npv.findViewById(R.id.notification_stack_scroller);
                if (TextUtils.isEmpty(Secure.getString(QSPanel.this.mContext.getContentResolver(), "enabled_accessibility_services"))) {
                    enabled = false;
                } else {
                    enabled = true;
                }
                if (enabled) {
                    HwLog.i("QSPanel", "set notification_stack_scroller GONE when enter qsEditer in talkback");
                    npv.expandNotificationPanelView();
                    stackView.setVisibility(8);
                }
                if (QSPanel.this.mCustomizePanel != null && !QSPanel.this.mCustomizePanel.isCustomizing()) {
                    int[] loc = new int[2];
                    v.getLocationInWindow(loc);
                    QSPanel.this.mCustomizePanel.show(loc[0], loc[1]);
                }
            }
        });
    }

    protected void onTileClick(QSTile<?> tile) {
        tile.click();
    }

    public void closeDetail() {
        HwLog.i("QSPanel", "closeDetail");
        if (this.mCustomizePanel == null || !this.mCustomizePanel.isCustomizing()) {
            showDetail(false, this.mDetailRecord);
        } else {
            this.mCustomizePanel.hide(this.mCustomizePanel.getWidth() / 2, this.mCustomizePanel.getHeight() / 2);
        }
    }

    public int getGridHeight() {
        return getMeasuredHeight();
    }

    protected void handleShowDetail(Record r, boolean show) {
        if (r instanceof TileRecord) {
            handleShowDetailTile((TileRecord) r, show);
            return;
        }
        int x = 0;
        int y = 0;
        if (r != null) {
            x = r.x;
            y = r.y;
        }
        handleShowDetailImpl(r, show, x, y);
    }

    private void handleShowDetailTile(TileRecord r, boolean show) {
        if ((this.mDetailRecord != null) != show || this.mDetailRecord != r) {
            if (show) {
                r.detailAdapter = r.tile.getDetailAdapter();
                if (r.detailAdapter == null) {
                    return;
                }
            }
            r.tile.setDetailListening(show);
            handleShowDetailImpl(r, show, r.tileView.getLeft() + (r.tileView.getWidth() / 2), ((r.tileView.getTop() + this.mTileLayout.getOffsetTop(r)) + (r.tileView.getHeight() / 2)) + getTop());
        }
    }

    private void handleShowDetailImpl(Record r, boolean show, int x, int y) {
        Record record;
        DetailAdapter detailAdapter = null;
        if (show) {
            record = r;
        } else {
            record = null;
        }
        setDetailRecord(record);
        if (show) {
            detailAdapter = r.detailAdapter;
        }
        fireShowingDetail(detailAdapter, x, y);
    }

    private void setDetailRecord(Record r) {
        if (r != this.mDetailRecord) {
            boolean scanState;
            this.mDetailRecord = r;
            if (this.mDetailRecord instanceof TileRecord) {
                scanState = ((TileRecord) this.mDetailRecord).scanState;
            } else {
                scanState = false;
            }
            fireScanStateChanged(scanState);
        }
    }

    void setGridContentVisibility(boolean visible) {
        int newVis = visible ? 0 : 4;
        setVisibility(newVis);
        if (this.mGridContentVisible != visible) {
            MetricsLogger.visibility(this.mContext, 111, newVis);
        }
        this.mGridContentVisible = visible;
    }

    private void logTiles() {
        StringBuilder tileBuilder = new StringBuilder();
        for (int i = 0; i < this.mRecords.size(); i++) {
            TileRecord tileRecord = (TileRecord) this.mRecords.get(i);
            MetricsLogger.visible(this.mContext, tileRecord.tile.getMetricsCategory());
            tileBuilder.append(tileRecord.tile.getTileSpec()).append(",");
        }
        HwLog.i("QSPanel", "logTiles::mRecords size=" + this.mRecords.size() + ", content=" + tileBuilder);
    }

    private void fireShowingDetail(DetailAdapter detail, int x, int y) {
        if (this.mCallback != null) {
            this.mCallback.onShowingDetail(detail, x, y);
        }
    }

    private void fireToggleStateChanged(boolean state) {
        if (this.mCallback != null) {
            this.mCallback.onToggleStateChanged(state);
        }
    }

    private void fireScanStateChanged(boolean state) {
        if (this.mCallback != null) {
            this.mCallback.onScanStateChanged(state);
        }
    }

    public void clickTile(ComponentName tile) {
        String spec = CustomTile.toSpec(tile);
        int N = this.mRecords.size();
        for (int i = 0; i < N; i++) {
            if (((TileRecord) this.mRecords.get(i)).tile.getTileSpec().equals(spec)) {
                ((TileRecord) this.mRecords.get(i)).tile.click();
                return;
            }
        }
    }

    public void setExpandHeight(int height) {
        this.mTileView.setClipBounds(new Rect(0, 0, this.mTileView.getWidth(), this.mTileView.getHeight() - (getMaxExpandHeight() - height)));
        this.mBrightnessView.setTranslationY((float) (height - getMaxExpandHeight()));
        invalidate();
    }

    public int getMinHeight() {
        return this.mMinHeight;
    }

    public int getMaxHeight() {
        return getGridHeight();
    }

    public int getMaxExpandHeight() {
        return getMaxHeight() - getMinHeight();
    }

    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);
        return true;
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.println("QSPanel: ");
        pw.println("visible=" + getVisibility());
        pw.println("translationX=" + getTranslationX());
        pw.println("translationY=" + getTranslationY());
        pw.println("left=" + getLeft() + ", right=" + getRight() + ", top=" + getTop() + ", bottom=" + getBottom());
    }

    public void onDetailsAnimateStarted() {
        setVisibility(4);
    }

    public HwBrightnessController getBrightnessController() {
        return this.mBrightnessController;
    }

    public void addCallback(HwSuperpowerModeManager modeManager) {
        if (this.mTileLayout != null && (this.mTileLayout instanceof PagedTileLayout)) {
            ((PagedTileLayout) this.mTileLayout).addCallback(modeManager);
        }
    }
}
