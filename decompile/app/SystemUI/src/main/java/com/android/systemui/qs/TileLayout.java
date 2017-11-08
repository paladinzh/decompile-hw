package com.android.systemui.qs;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewGroup;
import com.android.systemui.R;
import com.android.systemui.qs.HwSuperpowerModeManager.ModeChangedCallback;
import com.android.systemui.qs.QSPanel.QSTileLayout;
import com.android.systemui.qs.QSPanel.TileRecord;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.PerfAdjust;
import java.util.ArrayList;

public class TileLayout extends ViewGroup implements QSTileLayout, ModeChangedCallback {
    protected int mCellHeight;
    protected int mCellMarginBottom;
    private int mCellMarginTop;
    protected int mCellMarginX;
    protected int mCellMarginY;
    protected int mCellWidth;
    protected int mColumns;
    private boolean mIsSuperpowerMode;
    private boolean mListening;
    protected final ArrayList<TileRecord> mRecords;
    private int mSuperPowerModeColumns;
    private ArrayList<TileRecord> mSuperPowerSaveRecords;
    private ArrayList<TileRecord> mTempRecords;

    public TileLayout(Context context) {
        this(context, null);
    }

    public TileLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mColumns = 5;
        this.mRecords = new ArrayList();
        this.mIsSuperpowerMode = false;
        this.mSuperPowerModeColumns = 5;
        this.mSuperPowerSaveRecords = new ArrayList();
        this.mTempRecords = new ArrayList();
        setFocusableInTouchMode(true);
        updateResources();
    }

    public int getOffsetTop(TileRecord tile) {
        return getTop();
    }

    public void setListening(boolean listening) {
        if (this.mListening != listening) {
            this.mListening = listening;
        }
    }

    public void addTile(TileRecord tile) {
        this.mRecords.add(tile);
        addView(tile.tileView);
    }

    public void removeTile(TileRecord tile) {
        this.mRecords.remove(tile);
        removeView(tile.tileView);
    }

    public void removeAllViews() {
        this.mRecords.clear();
        super.removeAllViews();
    }

    public boolean updateResources() {
        Resources res = this.mContext.getResources();
        int columns = Math.max(1, res.getInteger(PerfAdjust.getQuickSettingsNumColumns()));
        this.mCellHeight = this.mContext.getResources().getDimensionPixelSize(R.dimen.qs_tile_height);
        this.mCellMarginX = res.getDimensionPixelSize(R.dimen.qs_tile_margin_x);
        this.mCellMarginY = res.getDimensionPixelSize(R.dimen.qs_tile_margin_y);
        this.mCellMarginTop = res.getDimensionPixelSize(R.dimen.qs_tile_margin_top);
        this.mCellMarginBottom = res.getDimensionPixelSize(R.dimen.qs_tile_margin_bottom);
        if (this.mIsSuperpowerMode) {
            this.mColumns = this.mSuperPowerModeColumns;
            requestLayout();
            return true;
        } else if (this.mColumns == columns) {
            return false;
        } else {
            this.mColumns = columns;
            requestLayout();
            return true;
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int numTiles = this.mRecords.size();
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int rows = ((this.mColumns + numTiles) - 1) / this.mColumns;
        int height = (((this.mCellHeight * rows) + ((rows - 1) * this.mCellMarginY)) + this.mCellMarginTop) + this.mCellMarginBottom;
        this.mCellWidth = (width - (this.mCellMarginX * (this.mColumns - 1))) / this.mColumns;
        View previousView = this;
        for (TileRecord record : this.mRecords) {
            if (record.tileView.getVisibility() != 8) {
                record.tileView.measure(exactly(this.mCellWidth), exactly(this.mCellHeight));
                previousView = record.tileView.updateAccessibilityOrder(previousView);
            }
        }
        setMeasuredDimension(width, height);
    }

    private static int exactly(int size) {
        return MeasureSpec.makeMeasureSpec(size, 1073741824);
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        int w = getWidth();
        boolean isRtl = getLayoutDirection() == 1;
        int row = 0;
        int column = 0;
        int i = 0;
        while (i < this.mRecords.size()) {
            int right;
            if (column == this.mColumns) {
                row++;
                column -= this.mColumns;
            }
            TileRecord record = (TileRecord) this.mRecords.get(i);
            int left = getColumnStart(column);
            int top = getRowTop(row);
            if (isRtl) {
                right = w - left;
                left = right - this.mCellWidth;
            } else {
                right = left + this.mCellWidth;
            }
            record.tileView.layout(left, top, right, record.tileView.getMeasuredHeight() + top);
            i++;
            column++;
        }
    }

    private int getRowTop(int row) {
        return ((this.mCellHeight + this.mCellMarginY) * row) + this.mCellMarginTop;
    }

    private int getColumnStart(int column) {
        return (this.mCellWidth + this.mCellMarginX) * column;
    }

    public void addCallback(HwSuperpowerModeManager modeManager) {
        modeManager.addCallback(this);
    }

    public void onModeChanged(boolean isSuperpowerMode) {
        this.mIsSuperpowerMode = isSuperpowerMode;
        if (isSuperpowerMode) {
            QSPanel mQSPanel = HwPhoneStatusBar.getInstance().getMQSPanel();
            this.mSuperPowerSaveRecords.clear();
            if (mQSPanel == null) {
                HwLog.e("TileLayout", "onModeChanged: mQSPanel is null");
                return;
            }
            if (mQSPanel.getSuperPowerModeRecord() != null) {
                this.mSuperPowerSaveRecords.addAll(mQSPanel.getSuperPowerModeRecord());
            }
            this.mTempRecords.clear();
            this.mTempRecords.addAll(this.mRecords);
            removeAllViews();
            if (this.mSuperPowerSaveRecords.isEmpty()) {
                HwLog.e("TileLayout", "mSuperPowerSaveRecords isEmpty");
                return;
            }
            for (TileRecord tileRecord : this.mSuperPowerSaveRecords) {
                addTile(tileRecord);
            }
            this.mSuperPowerModeColumns = Math.max(1, this.mRecords.size());
            updateResources();
        } else if (this.mTempRecords == null) {
            HwLog.e("TileLayout", "mTempRecords is null");
            return;
        } else {
            removeAllViews();
            for (TileRecord tileRecord2 : this.mTempRecords) {
                addTile(tileRecord2);
            }
            updateResources();
        }
        HwLog.i("TileLayout", "onModeChanged mRecords.size()" + this.mRecords.size());
    }
}
