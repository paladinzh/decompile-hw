package com.android.rcs.ui;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.mms.ui.FavoritesListItem;
import com.android.mms.ui.MessageItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransGroupMessageListItem;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.util.HashMap;

public class RcsFavoritesListItem {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;
    private RcsFileTransGroupMessageListItem mFtListItem;
    private LinearLayout mLocLayout;
    private ImageView mLocationImg;
    private TextView mLocationSubTv;
    private TextView mLocationTv;
    private LinearLayout mRcsFavFileListItem;

    public RcsFavoritesListItem(Context context) {
        this.mContext = context;
    }

    public void onFinishInflate(FavoritesListItem listItem) {
        if (this.isRcsOn) {
            ViewStub favFileListItem = (ViewStub) listItem.findViewById(R.id.rcs_fav_file_list_item);
            favFileListItem.setLayoutResource(R.layout.rcs_fav_file_list_item_view);
            favFileListItem.inflate();
            this.mRcsFavFileListItem = (LinearLayout) listItem.findViewById(R.id.rcsFavFileListItem);
            this.mRcsFavFileListItem.setVisibility(8);
            this.mFtListItem = (RcsFileTransGroupMessageListItem) listItem.findViewById(R.id.rcsFtGroupMsgListItem);
            if (this.mFtListItem != null) {
                setOnLongClickListener(this.mFtListItem);
            }
        }
    }

    public void bind(MessageItem msgItem, View bodyView, View messageBlock) {
        if (this.isRcsOn) {
            if (this.mFtListItem != null) {
                this.mFtListItem.setVisibility(8);
            }
            switch (msgItem.mMessageType) {
                case 3:
                case 5:
                case 6:
                    bindFileMessage(msgItem, bodyView, messageBlock);
                    break;
            }
        }
    }

    protected void bindFileMessage(MessageItem msgItem, View bodyView, View messageBlock) {
        bodyView.setVisibility(8);
        messageBlock.setVisibility(8);
        setFavFileBackground(msgItem);
        if (this.mFtListItem != null) {
            this.mFtListItem.setVisibility(0);
        }
        this.mRcsFavFileListItem.setVisibility(0);
        RcsMessageItem rcsMessageItem = msgItem.getRcsMessageItem();
        if (this.mFtListItem != null) {
            this.mFtListItem.bind(rcsMessageItem.getFileItem());
        }
    }

    private void setFavFileBackground(MessageItem msgItem) {
        RcsFileTransGroupMessageItem fileItem = msgItem.getRcsMessageItem().getFileItem();
        if (fileItem == null) {
            return;
        }
        if (fileItem.isImage() || fileItem.isVideo()) {
            this.mRcsFavFileListItem.setBackground(null);
            this.mRcsFavFileListItem.setPaddingRelative(0, 0, 0, 0);
            return;
        }
        int padding = this.mContext.getResources().getDimensionPixelSize(R.dimen.rcs_file_trans_padding);
        this.mRcsFavFileListItem.setPaddingRelative(padding, padding, padding, padding);
        if (msgItem.isInComingMessage()) {
            this.mRcsFavFileListItem.setBackgroundResource(R.drawable.message_pop_incoming_bg);
        } else if (msgItem.mSmsServiceCenterForFavorites == null || !msgItem.mSmsServiceCenterForFavorites.startsWith("rcs")) {
            this.mRcsFavFileListItem.setBackgroundResource(R.drawable.message_pop_send_bg);
        } else {
            this.mRcsFavFileListItem.setBackgroundResource(R.drawable.message_pop_rcs_send_bg);
        }
    }

    private void setOnLongClickListener(View view) {
        view.setOnLongClickListener(new OnLongClickListener() {
            public boolean onLongClick(View v) {
                RcsFavoritesListItem.this.dissmiss();
                return v.showContextMenu();
            }
        });
    }

    private void dissmiss() {
        MLog.d("RcsFavoritesListItem", "dismiss for findbugs");
    }

    public void bindCommonMessage(SpandTextView mBodyTextView, FavoritesListItem listItem) {
        if (this.isRcsOn) {
            this.mRcsFavFileListItem.setVisibility(8);
            long boxId = (long) listItem.getMessageItem().getBoxId();
            if (RcsMapLoader.isLocItem(mBodyTextView.getText().toString())) {
                ViewStub favLocReListItem = (ViewStub) listItem.findViewById(R.id.rcs_fav_loc_list_item);
                if (favLocReListItem != null) {
                    favLocReListItem.setLayoutResource(R.layout.rcs_item_chat_received_location);
                    favLocReListItem.inflate();
                }
                this.mLocationTv = (TextView) listItem.findViewById(R.id.location_attach_title);
                if (this.mLocationTv != null) {
                    this.mLocationTv.setVisibility(8);
                }
                this.mLocLayout = (LinearLayout) listItem.findViewById(R.id.loc_layout_view);
                this.mLocationSubTv = (TextView) listItem.findViewById(R.id.location_attach_subtitle);
                this.mLocationImg = (ImageView) listItem.findViewById(R.id.location_img);
                if (this.mLocLayout != null) {
                    final HashMap<String, String> locInfo = RcsMapLoader.getLocInfo(mBodyTextView.getText().toString());
                    mBodyTextView.setVisibility(8);
                    this.mLocLayout.setVisibility(0);
                    this.mLocationSubTv.setVisibility(0);
                    this.mLocationImg.setVisibility(0);
                    this.mLocationSubTv.setText((CharSequence) locInfo.get("subtitle"));
                    this.mLocLayout.setOnClickListener(new OnClickListener() {
                        public void onClick(View arg0) {
                            RcsMapLoaderFactory.getMapLoader(RcsFavoritesListItem.this.mContext).loadMap(RcsFavoritesListItem.this.mContext, locInfo);
                        }
                    });
                    this.mLocLayout.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            RcsFavoritesListItem.this.dissmiss();
                            return false;
                        }
                    });
                    if (!(boxId == 1 || boxId == 0)) {
                        this.mLocationSubTv.setTextColor(this.mContext.getResources().getColor(R.color.text_color_white_important));
                        this.mLocationImg.setImageResource(R.drawable.rcs_map_item_big_send);
                    }
                }
            } else {
                mBodyTextView.setVisibility(0);
                if (this.mLocLayout != null) {
                    this.mLocLayout.setVisibility(8);
                }
                if (this.mLocationSubTv != null) {
                    this.mLocationSubTv.setVisibility(8);
                }
                if (this.mLocationImg != null) {
                    this.mLocationImg.setVisibility(8);
                }
            }
        }
    }
}
