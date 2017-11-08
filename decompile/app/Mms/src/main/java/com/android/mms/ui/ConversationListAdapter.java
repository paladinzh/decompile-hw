package com.android.mms.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.Telephony.MmsSms;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.ImageView;
import android.widget.ListView;
import cn.com.xy.sms.sdk.SmartSmsPublicinfoUtil;
import com.android.mms.data.Conversation;
import com.android.mms.data.Conversation.Cache;
import com.android.mms.ui.BaseConversationListFragment.DeleteThreadListener;
import com.android.mms.util.DraftCache;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsConversationListAdapter;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditableList;
import com.huawei.mms.ui.HwFrameLayoutListItem;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.Log;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;
import huawei.android.widget.CursorSwipeAdapter;
import huawei.android.widget.SimpleSwipeListener;
import huawei.android.widget.SwipeLayout;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConversationListAdapter extends CursorSwipeAdapter implements RecyclerListener, OnScrollListener {
    private static final Uri URI_CONVERSATIONS_DELETE_REPEAT_THREAD = Uri.withAppendedPath(MmsSms.CONTENT_URI, "delete_repeat_thread");
    private boolean isScrolling = false;
    private Activity mActivity = null;
    private long mAfterThreadId = -1;
    private boolean mBeforeEqualFlag;
    private long mBeforeThreadId = -1;
    private long mCachedId = -1;
    private boolean mClickDeleteButton = false;
    private boolean mClickUnreadButton = false;
    private int mCurrentSwipePosition = -1;
    private DeleteThreadListener mDeleteThreadListener = null;
    private final LayoutInflater mFactory;
    private RcsConversationListAdapter mHwCust = null;
    private boolean mIsExtraHuge = false;
    private boolean mIsNotification = false;
    private boolean mIsSwipeDelete = false;
    EditableList mListView;
    private boolean mMarkAllAsReadWhenHasOpenSwipe = false;
    private Object mMultiSwipeLock = new Object();
    private boolean mOnConfigurationChanged = false;
    private OnContentChangedListener mOnContentChangedListener;
    private int mRecId = 0;
    private long mSelectedThreadId = -1;
    private SwipeCallback mSwipeCallback;
    private ArrayList<SwipeLayout> mSwipeLayoutList = new ArrayList();
    private ExecutorService sRepeatThreadPool = null;
    private Map<String, Long> threadsMap = null;

    public interface SwipeCallback {
        boolean isUsefulMode();

        void markAllAsRead();

        void swipeDeleteCallback(long j);

        void swipeMarkAsRead(int i);
    }

    public interface OnContentChangedListener {
        void onContentChanged(ConversationListAdapter conversationListAdapter);
    }

    public boolean isScroll() {
        return this.isScrolling;
    }

    public void setScroll(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public ConversationListAdapter(Context context, Cursor cursor, EditableList listView, int resourceId) {
        super(context, cursor, false);
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mHwCust = new RcsConversationListAdapter(context);
        }
        if (context instanceof Activity) {
            this.mActivity = (Activity) context;
        }
        this.threadsMap = new HashMap();
        int cmId = Log.logCallMethod();
        this.mRecId = resourceId;
        this.mFactory = LayoutInflater.from(context);
        this.mListView = listView;
        this.mIsNotification = context instanceof NotificationList;
        ((ListView) listView).setRecyclerListener(this);
        ResEx.self().initResColor();
        Conversation.checkPrefix(context);
        Log.logCallMethod(cmId);
        ((ListView) listView).setOnScrollListener(this);
    }

    public void notifyDatasetChanged() {
        super.notifyDataSetChanged();
    }

    public int getSwipeLayoutResourceId(int position) {
        return R.id.swipe;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.mCursor.moveToPosition(position)) {
            View v;
            if (convertView == null) {
                v = newView(this.mContext, this.mCursor, parent);
            } else {
                v = convertView;
            }
            if (v == null) {
                v = this.mFactory.inflate(this.mRecId, null);
            }
            if (v != null) {
                if (this.mSwipeLayoutList != null && this.mSwipeLayoutList.size() > 0 && this.mSwipeLayoutList.contains(v) && this.mOnConfigurationChanged) {
                    ((HwFrameLayoutListItem) v).close(false);
                    this.mSwipeLayoutList.clear();
                    this.mOnConfigurationChanged = false;
                }
                bindView(v.findViewById(R.id.mms_animation_list_item_view), this.mContext, this.mCursor);
                fillValues(position, v);
            }
            long id = this.mCursor.getLong(this.mCursor.getColumnIndex("_id"));
            storeThreadId(id);
            if (v != null && id == this.mSelectedThreadId && isHightLightItemNeeded()) {
                v.setBackgroundColor(Color.argb(13, 0, 125, 255));
            } else if (v != null) {
                v.setBackgroundColor(0);
            }
            return v;
        } else {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    public void closeOpenSwipe() {
        synchronized (this.mMultiSwipeLock) {
            if (this.mSwipeLayoutList != null && this.mSwipeLayoutList.size() > 0) {
                for (SwipeLayout swipeLayout : this.mSwipeLayoutList) {
                    swipeLayout.close(true);
                }
                this.mSwipeLayoutList.clear();
            }
        }
    }

    public void fillValues(final int position, View convertView) {
        HwFrameLayoutListItem swipeLayout = (HwFrameLayoutListItem) convertView.findViewById(R.id.swipe);
        if (!swipeLayout.hasUserSetSwipeListener()) {
            swipeLayout.addSwipeListener(new SimpleSwipeListener() {
                public void onStartOpen(SwipeLayout layout) {
                    synchronized (ConversationListAdapter.this.mMultiSwipeLock) {
                        ConversationListAdapter.this.closeOtherSwipeLayout(layout, true);
                    }
                }

                public void onOpen(SwipeLayout layout) {
                    synchronized (ConversationListAdapter.this.mMultiSwipeLock) {
                        ConversationListAdapter.this.closeOtherSwipeLayout(layout, true);
                        ConversationListAdapter.this.mSwipeLayoutList.add(layout);
                    }
                }

                public void onStartClose(SwipeLayout layout) {
                }

                public void onClose(SwipeLayout layout) {
                    synchronized (ConversationListAdapter.this.mMultiSwipeLock) {
                        ConversationListAdapter.this.mSwipeLayoutList.remove(layout);
                    }
                    if (ConversationListAdapter.this.mClickDeleteButton) {
                        if (ConversationListAdapter.this.mDeleteThreadListener != null) {
                            ConversationListAdapter.this.mDeleteThreadListener.setSwipeDeleteThreadId(ConversationListAdapter.this.getItemId(ConversationListAdapter.this.mCurrentSwipePosition));
                            ConversationListAdapter.this.mDeleteThreadListener.comfirmOnClick();
                            ConversationListAdapter.this.setIsSwipeDelete(true);
                        }
                        ConversationListAdapter.this.mDeleteThreadListener = null;
                        ConversationListAdapter.this.mClickDeleteButton = false;
                        ConversationListAdapter.this.mCurrentSwipePosition = -1;
                    }
                    if (ConversationListAdapter.this.mClickUnreadButton) {
                        if (-10000000012L == ConversationListAdapter.this.getItemIdBySwipe(ConversationListAdapter.this.mCurrentSwipePosition)) {
                            ConversationListAdapter.this.mSwipeCallback.swipeMarkAsRead(4);
                        } else if (-10000000011L == ConversationListAdapter.this.getItemIdBySwipe(ConversationListAdapter.this.mCurrentSwipePosition)) {
                            ConversationListAdapter.this.mSwipeCallback.swipeMarkAsRead(5);
                        } else {
                            long threadId = ConversationListAdapter.this.getItemIdBySwipe(ConversationListAdapter.this.mCurrentSwipePosition);
                            Conversation conv = Cache.get(threadId);
                            if (conv == null) {
                                conv = Conversation.get(ConversationListAdapter.this.mContext, threadId, false);
                            }
                            int itemType = ConversationListAdapter.this.getRcsItemType(ConversationListAdapter.this.mCurrentSwipePosition, ConversationListAdapter.this.mCursor);
                            if (!(itemType == -1 || conv.getHwCust() == null)) {
                                if (itemType == 4) {
                                    conv.getHwCust().setGroupId(ConversationListAdapter.this.getRcsGroupId(ConversationListAdapter.this.mCurrentSwipePosition, ConversationListAdapter.this.mCursor));
                                }
                                conv.getHwCust().setRcsThreadType(itemType);
                            }
                            conv.setThreadId(threadId);
                            conv.markAsRead();
                        }
                        ConversationListAdapter.this.mClickUnreadButton = false;
                        ConversationListAdapter.this.mCurrentSwipePosition = -1;
                    }
                    if (ConversationListAdapter.this.mMarkAllAsReadWhenHasOpenSwipe) {
                        ConversationListAdapter.this.mSwipeCallback.markAllAsRead();
                        ConversationListAdapter.this.mMarkAllAsReadWhenHasOpenSwipe = false;
                        ConversationListAdapter.this.mCurrentSwipePosition = -1;
                    }
                }
            });
        }
        ImageView deleteBtn = (ImageView) convertView.findViewById(R.id.trash);
        ImageView unreadBtn = (ImageView) convertView.findViewById(R.id.star);
        if (-10000000012L == getItemId(position) || -10000000011L == getItemId(position)) {
            deleteBtn.setVisibility(8);
        } else {
            deleteBtn.setVisibility(0);
        }
        if (deleteBtn.getVisibility() == 0) {
            deleteBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ConversationListAdapter.this.mCurrentSwipePosition = position;
                    if (ConversationListAdapter.this.mSwipeCallback != null) {
                        ConversationListAdapter.this.mSwipeCallback.swipeDeleteCallback(ConversationListAdapter.this.getItemIdBySwipe(ConversationListAdapter.this.mCurrentSwipePosition));
                    }
                    StatisticalHelper.incrementReportCount(ConversationListAdapter.this.mContext, 2246);
                }
            });
        }
        Conversation conv = Cache.get(getItemIdBySwipe(position));
        if (conv == null) {
            conv = Conversation.get(this.mContext, getItemIdBySwipe(position), false);
        }
        if (conv.getUnreadMessageCount() > 0) {
            unreadBtn.setVisibility(0);
        } else {
            unreadBtn.setVisibility(8);
        }
        if (unreadBtn.getVisibility() == 0) {
            unreadBtn.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ConversationListAdapter.this.mClickUnreadButton = true;
                    ConversationListAdapter.this.mCurrentSwipePosition = position;
                    ConversationListAdapter.this.closeOpenSwipe();
                    StatisticalHelper.incrementReportCount(ConversationListAdapter.this.mContext, 2245);
                }
            });
        }
        if (!this.mSwipeCallback.isUsefulMode() || ((-10000000012L == getItemId(position) || -10000000011L == getItemId(position)) && unreadBtn.getVisibility() != 0)) {
            if (MessageUtils.isNeedLayoutRtl()) {
                swipeLayout.setLeftSwipeEnabled(false);
            } else {
                swipeLayout.setRightSwipeEnabled(false);
            }
        } else if (MessageUtils.isNeedLayoutRtl()) {
            swipeLayout.setLeftSwipeEnabled(true);
        } else {
            swipeLayout.setRightSwipeEnabled(true);
        }
    }

    public void setSwipeCallback(SwipeCallback swipeCallback) {
        this.mSwipeCallback = swipeCallback;
    }

    private void closeOtherSwipeLayout(SwipeLayout layout, boolean hasAnim) {
        if (this.mSwipeLayoutList != null && this.mSwipeLayoutList.size() > 0) {
            for (int i = 0; i < this.mSwipeLayoutList.size(); i++) {
                SwipeLayout swipeLayout = (SwipeLayout) this.mSwipeLayoutList.get(i);
                if (!(swipeLayout == null || swipeLayout == layout)) {
                    swipeLayout.close(hasAnim);
                }
            }
        }
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof ConversationListItem) {
            long threadId = cursor.getLong(0);
            String recipients = cursor.getString(3);
            if (!this.threadsMap.containsKey(recipients) && threadId > 0) {
                this.threadsMap.put(recipients, Long.valueOf(threadId));
            } else if (threadId > 0) {
                long anotherThreadId = ((Long) this.threadsMap.get(recipients)).longValue();
                long resultThreadId = anotherThreadId;
                if (anotherThreadId > threadId) {
                    removeThreadFromBase(threadId, anotherThreadId);
                } else if (anotherThreadId < threadId) {
                    removeThreadFromBase(anotherThreadId, threadId);
                    resultThreadId = threadId;
                }
                if (anotherThreadId != threadId) {
                    this.threadsMap.remove(recipients);
                    this.threadsMap.put(recipients, Long.valueOf(resultThreadId));
                }
            }
            ConversationListItem headerView = (ConversationListItem) view;
            Conversation conv = Conversation.from(context, cursor);
            headerView.setSnippet(Conversation.getSnippetFromCursor(context, cursor));
            if (conv.getHwCust() == null || !conv.getHwCust().isRcsSwitchOn()) {
                headerView.bind(this.mActivity, conv, this.mIsExtraHuge, this.mListView.isInEditMode(), this.mListView.isSelected(conv.getThreadId()), this.mIsNotification, isScroll());
            } else {
                conv.getHwCust().setAdapter(this);
                headerView.bind(context, conv, this.mIsExtraHuge, this.mListView.isInEditMode(), this.mListView.isSelected(conv.getHwCust().getRcsThreadId(conv)), this.mIsNotification, false);
            }
            return;
        }
        MLog.e("ConversationListAdapter", "Unexpected bound view: " + view);
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return createView(this.mFactory, parent, this.mRecId);
    }

    public void clearThreadsMap() {
        if (this.threadsMap != null) {
            MLog.i("ConversationListAdapter", "clearThreadsMap when delete conversations");
            this.threadsMap.clear();
        }
    }

    private static View createView(LayoutInflater inflater, ViewGroup parent, int recId) {
        View view = null;
        try {
            view = ConversationListItem.getCachedConversationItem();
            if (view == null) {
                view = inflater.inflate(recId, parent, false);
            }
        } catch (InflateException e) {
            MLog.e("ConversationListAdapter", "ConversationListAdapter :: createView :: Exception : ", (Throwable) e);
        }
        return view;
    }

    public void setOnContentChangedListener(OnContentChangedListener l) {
        this.mOnContentChangedListener = l;
    }

    protected void onContentChanged() {
        if (this.mCursor != null && !this.mCursor.isClosed() && this.mOnContentChangedListener != null) {
            this.mOnContentChangedListener.onContentChanged(this);
        }
    }

    public void onMovedToScrapHeap(View view) {
        View itemView = view.findViewById(R.id.mms_animation_list_item_view);
        if (itemView != null) {
            ((ConversationListItem) itemView).unbind();
        }
    }

    public long getItemId(int position) {
        try {
            if (this.mHwCust == null || !this.mHwCust.isRcsSwitchOn()) {
                return super.getItemId(position);
            }
            return this.mHwCust.getItemId(position, this.mCursor);
        } catch (IllegalStateException e) {
            MLog.e("ConversationListAdapter", "ConversationListAdapter :: getItemId :: Exception : ", (Throwable) e);
            return 0;
        }
    }

    public int getRcsItemType(int position, Cursor cursor) {
        if (this.mHwCust == null) {
            return -1;
        }
        return this.mHwCust.getRcsItemType(position, cursor);
    }

    public String getRcsGroupId(int position, Cursor cursor) {
        if (this.mHwCust == null) {
            return "";
        }
        return this.mHwCust.getRcsGroupId(position, cursor);
    }

    private long getItemIdBySwipe(int position) {
        return super.getItemId(position);
    }

    public void onScroll(AbsListView arg0, int arg1, int arg2, int arg3) {
    }

    public void onScrollStateChanged(AbsListView arg0, int scrollState) {
        SmartSmsPublicinfoUtil.setScrollStatu(scrollState);
    }

    public void removeThreadFromBase(long deleThreadid, long repeatThreadid) {
        if (this.sRepeatThreadPool == null) {
            this.sRepeatThreadPool = Executors.newSingleThreadExecutor();
        }
        if (DraftCache.getInstance().hasDraft(deleThreadid) && !DraftCache.getInstance().hasDraft(repeatThreadid)) {
            DraftCache.getInstance().setDraftState(repeatThreadid, true);
            DraftCache.getInstance().setDraftState(deleThreadid, false);
        }
        MLog.d("ConversationListAdapter", "remove repeat threads deleThreadid:" + deleThreadid + ", repeatThreadid: " + repeatThreadid);
        new AsyncTask<Long, Void, Void>() {
            protected Void doInBackground(Long... params) {
                ContentValues value = new ContentValues();
                value.put("deleThreadid", params[0]);
                value.put("repeatThreadid", params[1]);
                if (ConversationListAdapter.this.mActivity.getContentResolver().update(ConversationListAdapter.URI_CONVERSATIONS_DELETE_REPEAT_THREAD, value, null, null) > 0 && ConversationListAdapter.this.mOnContentChangedListener != null) {
                    ConversationListAdapter.this.mOnContentChangedListener.onContentChanged(ConversationListAdapter.this);
                }
                return null;
            }
        }.executeOnExecutor(this.sRepeatThreadPool, new Long[]{Long.valueOf(deleThreadid), Long.valueOf(repeatThreadid)});
    }

    public void setSelectedPosition(long tid) {
        this.mSelectedThreadId = tid;
        notifyDataSetInvalidated();
    }

    private boolean isHightLightItemNeeded() {
        if (HwMessageUtils.isSplitOn() && (this.mActivity instanceof ConversationList)) {
            return ((ConversationList) this.mActivity).isSplitState();
        }
        return false;
    }

    private void storeThreadId(long id) {
        if (id == this.mSelectedThreadId) {
            this.mBeforeThreadId = this.mCachedId;
            this.mBeforeEqualFlag = true;
        } else if (this.mBeforeEqualFlag) {
            this.mAfterThreadId = id;
            this.mBeforeEqualFlag = false;
        } else {
            this.mCachedId = id;
        }
    }

    public long getThreadIdToShow() {
        return this.mAfterThreadId == -1 ? this.mBeforeThreadId : this.mAfterThreadId;
    }

    public boolean hasOpenSwipe() {
        return this.mSwipeLayoutList != null && this.mSwipeLayoutList.size() > 0;
    }

    public void deleteConversationWhenClickSwipeDeleteButton(boolean deleteConvNow, DeleteThreadListener listener) {
        this.mClickDeleteButton = deleteConvNow;
        this.mDeleteThreadListener = listener;
        if (deleteConvNow) {
            closeOpenSwipe();
        }
    }

    public void onConfigurationChanged() {
        this.mOnConfigurationChanged = true;
    }

    public void markAllAsReadWhenHasOpenSwipe() {
        this.mMarkAllAsReadWhenHasOpenSwipe = true;
        closeOpenSwipe();
    }

    public boolean getIsSwipeDelete() {
        return this.mIsSwipeDelete;
    }

    public void setIsSwipeDelete(boolean mIsSwipeDelete) {
        this.mIsSwipeDelete = mIsSwipeDelete;
    }
}
