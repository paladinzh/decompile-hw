package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Configuration;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.Space;
import com.android.systemui.R;
import com.android.systemui.qs.QSPanel.QSTileLayout;
import com.android.systemui.qs.QSPanel.TileRecord;
import com.android.systemui.qs.QSTile.SignalState;
import com.android.systemui.qs.QSTile.State;
import com.android.systemui.qs.customize.QSCustomizer;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import java.util.ArrayList;
import java.util.Collection;

public class QuickQSPanel extends QSPanel {
    private QSPanel mFullPanel;
    private View mHeader;
    private int mMaxTiles;
    private final Tunable mNumTiles = new Tunable() {
        public void onTuningChanged(String key, String newValue) {
            QuickQSPanel.this.setMaxTiles(QuickQSPanel.this.getNumQuickTiles(QuickQSPanel.this.mContext));
        }
    };

    private static class HeaderTileLayout extends LinearLayout implements QSTileLayout {
        private final Space mEndSpacer;
        private boolean mListening;
        protected final ArrayList<TileRecord> mRecords = new ArrayList();

        public HeaderTileLayout(Context context) {
            super(context);
            setClipChildren(false);
            setClipToPadding(false);
            setGravity(16);
            setLayoutParams(new LayoutParams(-1, -1));
            this.mEndSpacer = new Space(context);
            this.mEndSpacer.setLayoutParams(generateLayoutParams());
            updateDownArrowMargin();
            addView(this.mEndSpacer);
            setOrientation(0);
        }

        protected void onConfigurationChanged(Configuration newConfig) {
            super.onConfigurationChanged(newConfig);
            updateDownArrowMargin();
        }

        private void updateDownArrowMargin() {
            LayoutParams params = (LayoutParams) this.mEndSpacer.getLayoutParams();
            params.setMarginStart(this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_expand_margin));
            this.mEndSpacer.setLayoutParams(params);
        }

        public void setListening(boolean listening) {
            if (this.mListening != listening) {
                this.mListening = listening;
                for (TileRecord record : this.mRecords) {
                    record.tile.setListening(this, this.mListening);
                }
            }
        }

        public void addTile(TileRecord tile) {
            addView(tile.tileView, getChildCount() - 1, generateLayoutParams());
            addView(new Space(this.mContext), getChildCount() - 1, generateSpaceParams());
            this.mRecords.add(tile);
            tile.tile.setListening(this, this.mListening);
        }

        private LayoutParams generateSpaceParams() {
            LayoutParams lp = new LayoutParams(0, this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size));
            lp.weight = 1.0f;
            lp.gravity = 17;
            return lp;
        }

        private LayoutParams generateLayoutParams() {
            int size = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_quick_tile_size);
            LayoutParams lp = new LayoutParams(size, size);
            lp.gravity = 17;
            return lp;
        }

        public void removeTile(TileRecord tile) {
            int childIndex = getChildIndex(tile.tileView);
            removeViewAt(childIndex);
            removeViewAt(childIndex);
            this.mRecords.remove(tile);
            tile.tile.setListening(this, false);
        }

        private int getChildIndex(QSTileBaseView tileView) {
            int N = getChildCount();
            for (int i = 0; i < N; i++) {
                if (getChildAt(i) == tileView) {
                    return i;
                }
            }
            return -1;
        }

        public int getOffsetTop(TileRecord tile) {
            return 0;
        }

        public boolean updateResources() {
            return false;
        }

        public boolean hasOverlappingRendering() {
            return false;
        }

        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            if (this.mRecords != null && this.mRecords.size() > 0) {
                View previousView = this;
                for (TileRecord record : this.mRecords) {
                    if (record.tileView.getVisibility() != 8) {
                        previousView = record.tileView.updateAccessibilityOrder(previousView);
                    }
                }
                ((TileRecord) this.mRecords.get(0)).tileView.setAccessibilityTraversalAfter(R.id.alarm_status_collapsed);
                ((TileRecord) this.mRecords.get(this.mRecords.size() - 1)).tileView.setAccessibilityTraversalBefore(R.id.expand_indicator);
            }
        }
    }

    public QuickQSPanel(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (this.mTileLayout != null) {
            for (int i = 0; i < this.mRecords.size(); i++) {
                this.mTileLayout.removeTile((TileRecord) this.mRecords.get(i));
            }
            removeView((View) this.mTileLayout);
        }
        this.mTileLayout = new HeaderTileLayout(context);
        this.mTileLayout.setListening(this.mListening);
        addView((View) this.mTileLayout, 1);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        TunerService.get(this.mContext).addTunable(this.mNumTiles, "sysui_qqs_count");
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        TunerService.get(this.mContext).removeTunable(this.mNumTiles);
    }

    public void setQSPanelAndHeader(QSPanel fullPanel, View header) {
        this.mFullPanel = fullPanel;
        this.mHeader = header;
    }

    protected boolean shouldShowDetail() {
        return !this.mExpanded;
    }

    protected void drawTile(TileRecord r, State state) {
        if (state instanceof SignalState) {
            State copy = r.tile.newTileState();
            state.copyTo(copy);
            ((SignalState) copy).activityIn = false;
            ((SignalState) copy).activityOut = false;
            state = copy;
        }
        super.drawTile(r, state);
    }

    protected QSTileBaseView createTileView(QSTile<?> tile, boolean collapsedView) {
        return new QSTileBaseView(this.mContext, tile.createTileView(this.mContext), collapsedView);
    }

    public void setHost(QSTileHost host, QSCustomizer customizer) {
        super.setHost(host, customizer);
        setTiles(this.mHost.getTiles());
    }

    public void setMaxTiles(int maxTiles) {
        this.mMaxTiles = maxTiles;
        if (this.mHost != null) {
            setTiles(this.mHost.getTiles());
        }
    }

    protected void onTileClick(QSTile<?> tile) {
        tile.secondaryClick();
    }

    public void onTuningChanged(String key, String newValue) {
        if (key.equals("qs_show_brightness")) {
            super.onTuningChanged(key, "0");
        }
    }

    public void setTiles(Collection<QSTile<?>> tiles) {
        ArrayList<QSTile<?>> quickTiles = new ArrayList();
        for (QSTile<?> tile : tiles) {
            quickTiles.add(tile);
            if (quickTiles.size() == this.mMaxTiles) {
                break;
            }
        }
        super.setTiles(quickTiles, true);
    }

    public int getNumQuickTiles(Context context) {
        return TunerService.get(context).getValue("sysui_qqs_count", 5);
    }
}
