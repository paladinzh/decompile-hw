package com.huawei.rcs.ui;

import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListItem;
import com.android.rcs.ui.LinkerTextTransfer;
import com.android.rcs.ui.RcsGroupChatMessageListAdapter;
import com.android.rcs.ui.RcsGroupChatMessageListItem;
import com.android.rcs.ui.RcsMessageListAdapter;

public class RcseScrollListener implements OnScrollListener {
    private int currentFirstVisibleItem = 0;
    private int currentScrollState = 0;
    private int currentVisibleItemCount = 0;
    private RcsScrollListAdapter mMsgListAdapter;

    public RcseScrollListener(RcsScrollListAdapter adapter) {
        this.mMsgListAdapter = adapter;
    }

    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        this.currentFirstVisibleItem = firstVisibleItem;
        this.currentVisibleItemCount = visibleItemCount;
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        LinkerTextTransfer.getInstance().setScrollState(scrollState);
        int oldScrollState = this.currentScrollState;
        this.currentScrollState = scrollState;
        switch (this.currentScrollState) {
            case 0:
            case 1:
                if (this.currentScrollState == 0) {
                    this.mMsgListAdapter.resetListScrollAnimation();
                }
                this.mMsgListAdapter.setScrollRcs(false);
                int i;
                if (this.mMsgListAdapter instanceof RcsMessageListAdapter) {
                    for (i = 0; i < this.currentVisibleItemCount; i++) {
                        MessageListItem msgListItem = (MessageListItem) view.getChildAt(i);
                        if ((msgListItem instanceof RcsFileTransMessageListItem) && msgListItem.getTag() != null) {
                            MessageItem item = null;
                            MessageItem msgItem = msgListItem.getMessageItem();
                            if (msgItem instanceof RcsFileTransMessageItem) {
                                item = (RcsFileTransMessageItem) msgItem;
                            }
                            if (item != null) {
                                item.createFileIcon();
                                msgListItem.setTag(null);
                                ((RcsFileTransMessageListItem) msgListItem).bind(item, false, this.currentFirstVisibleItem + i);
                            }
                        }
                    }
                    if (needNotifyDataSetChanged(oldScrollState, this.currentScrollState)) {
                        MessageListAdapter msgAdapter = ((RcsMessageListAdapter) this.mMsgListAdapter).getMessageListAdapter();
                        if (msgAdapter != null) {
                            msgAdapter.notifyDataSetChanged();
                            break;
                        }
                    }
                } else if (this.mMsgListAdapter instanceof RcsGroupChatMessageListAdapter) {
                    for (i = 0; i < this.currentVisibleItemCount; i++) {
                        if (view.getChildAt(i) instanceof RcsGroupChatMessageListItem) {
                            RcsGroupChatMessageListItem msgListItem2 = (RcsGroupChatMessageListItem) view.getChildAt(i);
                            if (!(msgListItem2.mFtGroupMsgListItem == null || msgListItem2.mFtGroupMsgListItem.getTag() == null)) {
                                RcsFileTransGroupMessageItem item2 = msgListItem2.mFtGroupMsgListItem.getMessageItem();
                                if (item2 != null) {
                                    item2.createFileIcon();
                                    msgListItem2.mFtGroupMsgListItem.setTag(null);
                                    msgListItem2.mFtGroupMsgListItem.bind(item2);
                                }
                            }
                        }
                    }
                    if (needNotifyDataSetChanged(oldScrollState, this.currentScrollState)) {
                        ((RcsGroupChatMessageListAdapter) this.mMsgListAdapter).notifyDataSetChanged();
                        break;
                    }
                } else {
                    return;
                }
                break;
            case 2:
                this.mMsgListAdapter.setScrollRcs(true);
                break;
        }
    }

    private boolean needNotifyDataSetChanged(int oldScrollState, int newScrollState) {
        if (newScrollState != 0 || newScrollState == oldScrollState) {
            return false;
        }
        return true;
    }
}
