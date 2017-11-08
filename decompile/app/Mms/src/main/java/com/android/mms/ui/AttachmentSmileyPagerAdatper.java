package com.android.mms.ui;

import android.app.Activity;
import android.content.Context;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import com.android.mms.MmsConfig;
import com.android.mms.ui.IconListAdapter.IconListItem;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsAttachmentSmileyPagerAdatper;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class AttachmentSmileyPagerAdatper {
    private MyPaperAdapter mAdapter;
    private ArrayList<GridView> mAl;
    private Context mContext;
    private RcsAttachmentSmileyPagerAdatper mHwCust = null;
    private SmileyFaceSelectorAdapter mRecentAdapter;

    public AttachmentSmileyPagerAdatper(Context context, boolean IsSmiley, boolean showMmsOptions) {
        this.mContext = context;
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsAttachmentSmileyPagerAdatper(context);
        }
        this.mAl = createGridViewArrayList(context, IsSmiley, showMmsOptions);
        this.mAdapter = new MyPaperAdapter(context, this.mAl);
    }

    public MyPaperAdapter getAdapter() {
        return this.mAdapter;
    }

    private GridView createGridView(Context context, int columns, boolean isSmiley) {
        GridView gridView = new GridView(context);
        gridView.setLayoutParams(new LayoutParams(-1, -2));
        if (!isSmiley) {
            int space = (int) this.mContext.getResources().getDimension(R.dimen.attach_panel_spacing);
            gridView.setHorizontalSpacing(space);
            gridView.setVerticalSpacing(space);
        }
        gridView.setNumColumns(columns);
        gridView.setOverScrollMode(2);
        gridView.setGravity(17);
        return gridView;
    }

    private ArrayList<GridView> createGridViewArrayList(Context context, boolean isSmiley, boolean showMmsOptions) {
        AttachmentTypeSelectorAdapter allItemAdapter;
        int pagerCount;
        ArrayList<GridView> gridViewList = new ArrayList();
        boolean isLand = this.mContext.getResources().getConfiguration().orientation == 2;
        int Rows = getRowCount(isLand, isSmiley, RcsCommonConfig.isRCSSwitchOn());
        int Columns = getColumCount(isLand, isSmiley, RcsCommonConfig.isRCSSwitchOn());
        if (!isSmiley) {
            int slideShowOption;
            if (showMmsOptions) {
                slideShowOption = 0;
            } else {
                slideShowOption = 1;
            }
            if (this.mHwCust == null || !this.mHwCust.getIsGroup()) {
                allItemAdapter = new AttachmentTypeSelectorAdapter(context, slideShowOption, R.layout.icon_list_item);
            } else {
                allItemAdapter = this.mHwCust.getGroupAttachmentTypeSelectorAdapter(R.layout.icon_list_item);
            }
        } else if (RcsCommonConfig.isRCSSwitchOn()) {
            allItemAdapter = new SmileyFaceSelectorAdapter(context, R.layout.smiley_face_item);
        } else {
            allItemAdapter = new SmileyFaceSelectorAdapter(context, R.layout.smiley_face_item_emoji);
        }
        int itemCount = 0;
        if (allItemAdapter != null) {
            itemCount = allItemAdapter.getCount();
        }
        int singerpagerCount = Rows * Columns;
        if (itemCount % singerpagerCount == 0) {
            pagerCount = itemCount / singerpagerCount;
        } else {
            pagerCount = (itemCount / singerpagerCount) + 1;
        }
        for (int i = 0; i < pagerCount; i++) {
            AttachmentTypeSelectorAdapter adapter;
            GridView gridView = createGridView(context, Columns, isSmiley);
            int currentMaxAttachment = Math.min(itemCount, (i + 1) * singerpagerCount);
            List items = new ArrayList(singerpagerCount);
            for (int j = i * singerpagerCount; j < currentMaxAttachment; j++) {
                if (allItemAdapter != null) {
                    items.add((IconListItem) allItemAdapter.getItem(j));
                }
            }
            if (!isSmiley) {
                adapter = new AttachmentTypeSelectorAdapter(context, R.layout.icon_list_item, items);
            } else if (RcsCommonConfig.isRCSSwitchOn()) {
                adapter = new SmileyFaceSelectorAdapter(context, R.layout.smiley_face_item, items);
            } else {
                adapter = new SmileyFaceSelectorAdapter(context, R.layout.smiley_face_item_emoji, items);
                if (i == 0) {
                    this.mRecentAdapter = (SmileyFaceSelectorAdapter) adapter;
                }
            }
            gridView.setAdapter(adapter);
            gridViewList.add(gridView);
        }
        return gridViewList;
    }

    private int getRowCount(boolean isLand, boolean isSmiley, boolean isRcsOn) {
        int ROW_LAND = this.mContext.getResources().getInteger(R.integer.row_land);
        int ROW_SMILE = this.mContext.getResources().getInteger(R.integer.row_smile);
        int ROW_ATTACH = this.mContext.getResources().getInteger(R.integer.row_attach);
        int ROW_LAND_EMOJI = this.mContext.getResources().getInteger(R.integer.row_land_emoji);
        boolean isMultiWindow = false;
        if (this.mContext instanceof Activity) {
            isMultiWindow = ((Activity) this.mContext).isInMultiWindowMode();
        }
        if (isMultiWindow && isSmiley && !isRcsOn) {
            return this.mContext.getResources().getInteger(R.integer.row_multiwindow_emoji);
        }
        if (isMultiWindow && isSmiley && isRcsOn) {
            return ROW_LAND;
        }
        if (isLand) {
            if (!isSmiley || isRcsOn) {
                return ROW_LAND;
            }
            return ROW_LAND_EMOJI;
        } else if (isMultiWindow && !isSmiley) {
            return ROW_LAND;
        } else {
            if (!isSmiley && MmsConfig.isInSimpleUI()) {
                return ROW_LAND;
            }
            if (!isSmiley) {
                ROW_SMILE = ROW_ATTACH;
            }
            return ROW_SMILE;
        }
    }

    private int getColumCount(boolean isLand, boolean isSmiley, boolean isRcsOn) {
        int SMILEY_COLUM = this.mContext.getResources().getInteger(R.integer.smile_colum);
        int ATTACH_COLUM = this.mContext.getResources().getInteger(R.integer.attach_colum);
        int SMILEY_COLUM_LAND = this.mContext.getResources().getInteger(R.integer.smile_colum_land);
        int ATTACH_COLUM_LAND = this.mContext.getResources().getInteger(R.integer.attach_colum_land);
        int SMILEY_COLUM_EMOJI = this.mContext.getResources().getInteger(R.integer.smile_colum_emoji);
        int SMILEY_COLUM_LAND_EMOJI = this.mContext.getResources().getInteger(R.integer.smile_colum_land_emoji);
        boolean isMultiWindow = false;
        if (this.mContext instanceof Activity) {
            isMultiWindow = ((Activity) this.mContext).isInMultiWindowMode();
        }
        if (isMultiWindow && isSmiley && !isRcsOn) {
            return this.mContext.getResources().getInteger(R.integer.smile_colum_multiwindow_emoji);
        }
        if (isLand) {
            if (!isSmiley) {
                SMILEY_COLUM_LAND = ATTACH_COLUM_LAND;
            } else if (!isRcsOn) {
                SMILEY_COLUM_LAND = SMILEY_COLUM_LAND_EMOJI;
            }
            return SMILEY_COLUM_LAND;
        }
        if (!isSmiley) {
            SMILEY_COLUM = ATTACH_COLUM;
        } else if (!isRcsOn) {
            SMILEY_COLUM = SMILEY_COLUM_EMOJI;
        }
        return SMILEY_COLUM;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        if (this.mAl != null) {
            for (int i = 0; i < this.mAl.size(); i++) {
                ((GridView) this.mAl.get(i)).setOnItemClickListener(listener);
            }
        }
    }

    public SmileyFaceSelectorAdapter getRecentAdapter() {
        return this.mRecentAdapter;
    }
}
