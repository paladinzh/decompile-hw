package com.android.rcs.ui;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ui.ListScrollAnimation;
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.PreferenceUtils;
import com.android.mms.util.LruSoftCache;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EditableList;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.ui.RcsFileTransGroupMessageListItem;
import com.huawei.rcs.ui.RcsImageCache;
import com.huawei.rcs.ui.RcsScrollListAdapter;
import com.huawei.rcs.utils.RcsUtility;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class RcsGroupChatMessageListAdapter extends CursorAdapter implements RcsScrollListAdapter {
    private boolean isScrolling = false;
    public ListScrollAnimation listScrollAnimation;
    private GroupMessageColumn mColumnMap = null;
    private OnDataSetChangedListener mDataChangedListener = null;
    private HwBaseFragment mFragment;
    private String mGroupID;
    private Handler mHandler;
    Pattern mHighLight;
    private RcsImageCache mImageCache;
    private int mInOutMsgCount;
    private LayoutInflater mInflater = null;
    protected EditableList mListView;
    private LruSoftCache<String, RcsGroupChatMessageItem> mMessageItemCache = new LruSoftCache(20);
    private Map<Integer, Long> mMessageTimeStampMap = new HashMap();
    float mScale = ContentUtil.FONT_SIZE_NORMAL;
    private long mThreadId = 0;

    public interface OnDataSetChangedListener {
        void onContentChanged();

        void onDataSetChanged();
    }

    public static class GroupMessageColumn {
        private static String ADDRESS = "address";
        private static String BODY = "body";
        private static String DATE = "date";
        private static String GLOBALID = "global_id";
        private static String MESSAGE_ID = "_id";
        private static String READ = "READ";
        private static String STATUS = "status";
        private static String THREAD_ID = "thread_id";
        private static String TYPE = NumberInfo.TYPE_KEY;
        public int columnAddress = 0;
        public int columnBody = 0;
        public int columnDate = 0;
        public int columnGlobalID = 0;
        public int columnMessageID = 0;
        public int columnRead = 0;
        public int columnStatus = 0;
        public int columnThreadID = 0;
        public int columnType = 0;

        public GroupMessageColumn(Cursor cursor) {
            try {
                this.columnType = cursor.getColumnIndexOrThrow(TYPE);
            } catch (IllegalArgumentException e) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnAddress = cursor.getColumnIndexOrThrow(ADDRESS);
            } catch (IllegalArgumentException e2) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnDate = cursor.getColumnIndexOrThrow(DATE);
            } catch (IllegalArgumentException e3) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnBody = cursor.getColumnIndexOrThrow(BODY);
            } catch (IllegalArgumentException e4) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnRead = cursor.getColumnIndexOrThrow(READ);
            } catch (IllegalArgumentException e5) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnStatus = cursor.getColumnIndexOrThrow(STATUS);
            } catch (IllegalArgumentException e6) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnGlobalID = cursor.getColumnIndexOrThrow(GLOBALID);
            } catch (IllegalArgumentException e7) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnMessageID = cursor.getColumnIndexOrThrow(MESSAGE_ID);
            } catch (IllegalArgumentException e8) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
            try {
                this.columnThreadID = cursor.getColumnIndexOrThrow(THREAD_ID);
            } catch (IllegalArgumentException e9) {
                MLog.e("RcsGroupChatMessageListAdapter", "IllegalArgument error");
            }
        }
    }

    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    public RcsImageCache getmImageCache() {
        return this.mImageCache;
    }

    public void removeCache(List<Long> idList) {
        if (idList != null && this.mImageCache != null) {
            for (Long id : idList) {
                this.mMessageItemCache.remove(String.valueOf(id));
                this.mImageCache.removeBitmapCache(RcsUtility.getBitmapFromMemCacheKey(id.longValue(), 2));
            }
        }
    }

    public RcsGroupChatMessageListAdapter(Context context, Cursor c, ListView listView, boolean autoRequery, String groupID, Pattern highlight, HwBaseFragment fragment) {
        super(context, c, autoRequery);
        if (fragment != null) {
            this.mFragment = fragment;
        }
        this.mScale = PreferenceUtils.getPreferenceFloat(context, "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        this.mInflater = LayoutInflater.from(context);
        this.mHighLight = highlight;
        if (c != null) {
            this.mColumnMap = new GroupMessageColumn(c);
        }
        if (listView != null && (listView instanceof EditableList)) {
            this.mListView = (EditableList) listView;
        }
        this.mGroupID = groupID;
        this.mImageCache = RcsImageCache.getInstance(((Activity) context).getFragmentManager(), context);
        RecyclerListener recyclerListener = new RecyclerListener() {
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    ((MessageListItem) view).unbind();
                }
            }
        };
        if (listView != null) {
            listView.setRecyclerListener(recyclerListener);
            this.listScrollAnimation = new ListScrollAnimation(context, listView);
        }
    }

    public void setConversationId(long threadId) {
        this.mThreadId = threadId;
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (this.mColumnMap == null) {
            this.mColumnMap = new GroupMessageColumn(cursor);
        }
        int type = cursor.getInt(this.mColumnMap.columnType);
        String msgId = cursor.getString(this.mColumnMap.columnMessageID);
        RcsFileTransGroupMessageItem.setmCache(this.mImageCache);
        RcsGroupChatMessageItem msgItem = getCachedMessageItem(type, msgId, cursor);
        int viewType = getItemViewType(cursor);
        if (msgItem != null) {
            int position = cursor.getPosition();
            this.mMessageTimeStampMap.put(Integer.valueOf(position), Long.valueOf(msgItem.mDate));
            RcsGroupChatNoticeMessageListItem mli;
            switch (viewType) {
                case 0:
                    mli = (RcsGroupChatNoticeMessageListItem) view;
                    mli.setNeedShowTimePhase(needShowTimePhase(position, cursor));
                    mli.bind(msgItem);
                    return;
                case 1:
                case 2:
                    RcsGroupChatMessageListItem mli2 = (RcsGroupChatMessageListItem) view;
                    boolean isInEditMode = this.mListView != null ? this.mListView.isInEditMode() : false;
                    boolean isSelected = this.mListView != null ? this.mListView.isSelected(Long.parseLong(msgId)) : false;
                    mli2.setConversationId(this.mThreadId);
                    mli2.setTextScale(this.mScale);
                    mli2.setNeedShowTimePhase(needShowTimePhase(position, cursor));
                    mli2.bind(msgItem, position, this.mGroupID, isInEditMode, isSelected, this.isScrolling);
                    mli2.setMsgListItemHandler(this.mHandler);
                    return;
                case 3:
                    mli = (RcsGroupChatNoticeMessageListItem) view;
                    if (TextUtils.isEmpty(cursor.getString(this.mColumnMap.columnBody))) {
                        msgItem.mBody = context.getResources().getString(R.string.rcs_group_invite_hint);
                    } else {
                        msgItem.mBody = context.getResources().getString(R.string.init_group_names, new Object[]{cursor.getString(this.mColumnMap.columnBody)});
                    }
                    mli.setNeedShowTimePhase(needShowTimePhase(position, cursor));
                    mli.bind(msgItem);
                    return;
                default:
                    return;
            }
        }
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        if (view != null) {
            view.clearAnimation();
        }
        this.listScrollAnimation.startAnim(view, position);
        return view;
    }

    public View newView(Context context, Cursor cursor, ViewGroup root) {
        if (this.mColumnMap == null) {
            this.mColumnMap = new GroupMessageColumn(cursor);
        }
        View view;
        RcsGroupChatMessageListItem gcml;
        RcsFileTransGroupMessageListItem ftgmli;
        switch (getItemViewType(cursor)) {
            case 0:
            case 3:
                return this.mInflater.inflate(R.layout.rcs_groupchat_message_list_item_notice, null);
            case 1:
                view = this.mInflater.inflate(R.layout.rcs_groupchat_message_list_item_send, null);
                gcml = (RcsGroupChatMessageListItem) view.findViewById(R.id.msg_list_item_send);
                ftgmli = (RcsFileTransGroupMessageListItem) view.findViewById(R.id.rcsFtGroupMsgListItem);
                if (!(this.mFragment instanceof RcsGroupChatComposeMessageFragment)) {
                    return view;
                }
                gcml.setFragment((RcsGroupChatComposeMessageFragment) this.mFragment);
                ftgmli.setFragment((RcsGroupChatComposeMessageFragment) this.mFragment);
                return view;
            case 2:
                view = this.mInflater.inflate(R.layout.rcs_groupchat_message_list_item_recv, null);
                gcml = (RcsGroupChatMessageListItem) view.findViewById(R.id.msg_list_item_recv);
                ftgmli = (RcsFileTransGroupMessageListItem) view.findViewById(R.id.rcsFtGroupMsgListItem);
                if (!(this.mFragment instanceof RcsGroupChatComposeMessageFragment)) {
                    return view;
                }
                gcml.setFragment((RcsGroupChatComposeMessageFragment) this.mFragment);
                ftgmli.setFragment((RcsGroupChatComposeMessageFragment) this.mFragment);
                return view;
            case 4:
                view = new View(context);
                view.setVisibility(8);
                return view;
            default:
                Log.e("RcsGroupChatMessageListAdapter", "newView message type error");
                return new View(context);
        }
    }

    public int getViewTypeCount() {
        return 5;
    }

    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return -1;
        }
        if (this.mColumnMap == null) {
            this.mColumnMap = new GroupMessageColumn(cursor);
        }
        return getItemViewType(cursor);
    }

    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed() && this.mDataChangedListener != null) {
            this.mDataChangedListener.onContentChanged();
        }
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        this.mMessageItemCache.evictAll();
        if (this.mDataChangedListener != null) {
            this.mDataChangedListener.onDataSetChanged();
        }
    }

    public RcsGroupChatMessageItem getMessageItemWithIdAssigned(int position, Cursor c) {
        RcsGroupChatMessageItem item;
        int posBefore = c.getPosition();
        c.moveToPosition(position);
        try {
            if (RcsCommonConfig.isRCSSwitchOn()) {
                item = new RcsGroupChatMessageItem(this.mContext, c, this.mColumnMap, this.isScrolling, this.mHighLight);
            } else {
                item = null;
            }
        } catch (Throwable e) {
            MLog.e("RcsGroupChatMessageListAdapter", "getMessageItemWithIdAssigned: MmsException = ", e);
            item = null;
        } catch (Throwable ee) {
            MLog.e("RcsGroupChatMessageListAdapter", "getMessageItemWithIdAssigned: NullPointerException = ", ee);
            item = null;
        }
        c.moveToPosition(posBefore);
        return item;
    }

    public int getItemViewType(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        if (this.mColumnMap == null) {
            this.mColumnMap = new GroupMessageColumn(cursor);
        }
        switch (cursor.getInt(this.mColumnMap.columnType)) {
            case 1:
            case 101:
                return 2;
            case 4:
            case 100:
                return 1;
            case 30:
                return 3;
            case Place.TYPE_HOSPITAL /*50*/:
            case Place.TYPE_INSURANCE_AGENCY /*51*/:
            case Place.TYPE_JEWELRY_STORE /*52*/:
                return 0;
            case 112:
                return 4;
            default:
                return 0;
        }
    }

    private RcsGroupChatMessageItem getCachedMessageItem(int type, String msgId, Cursor cursor) {
        MmsException e;
        RcsGroupChatMessageItem item = (RcsGroupChatMessageItem) this.mMessageItemCache.get(this.mContext, getKey(type, msgId));
        if (item != null || cursor == null) {
            return item;
        }
        if (!isCursorValid(cursor)) {
            return item;
        }
        RcsGroupChatMessageItem rcsGroupChatMessageItem;
        try {
            rcsGroupChatMessageItem = new RcsGroupChatMessageItem(this.mContext, cursor, this.mColumnMap, this.isScrolling, this.mHighLight);
            try {
                this.mMessageItemCache.put(rcsGroupChatMessageItem.mMsgId, rcsGroupChatMessageItem);
                return rcsGroupChatMessageItem;
            } catch (MmsException e2) {
                e = e2;
            }
        } catch (MmsException e3) {
            e = e3;
            rcsGroupChatMessageItem = item;
            Log.e("RcsGroupChatMessageListAdapter", "getCachedMessageItem: ", e);
            return rcsGroupChatMessageItem;
        }
    }

    private String getKey(int type, String id) {
        return id;
    }

    private boolean isCursorValid(Cursor cursor) {
        if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    public void updateInOutMsgCount() {
        int count = 0;
        int totalCount = getCount();
        for (int i = 0; i < totalCount; i++) {
            int type = getItemViewType(i);
            if (2 == type || 1 == type) {
                count++;
            }
        }
        this.mInOutMsgCount = count;
    }

    public int getInOutMsgCount() {
        return this.mInOutMsgCount;
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        this.mDataChangedListener = l;
    }

    public void cancelBackgroundLoading() {
        this.mMessageItemCache.evictAll();
        if (this.mImageCache != null) {
            this.mImageCache.clearCache();
        }
    }

    public void setScrollRcs(boolean isScrolling) {
        this.isScrolling = isScrolling;
    }

    public void resetListScrollAnimation() {
        this.listScrollAnimation.setDownTranslation(0.0f);
        this.listScrollAnimation.setUpTranslation(0.0f);
        this.listScrollAnimation.setVelocity(0.0f);
    }

    public GroupMessageColumn getGroupMessageColumn() {
        return this.mColumnMap;
    }

    public RcsGroupChatMessageItem getMessageFromCache(long msgId) {
        if (this.mMessageItemCache == null || this.mMessageItemCache.size() <= 0) {
            return null;
        }
        return (RcsGroupChatMessageItem) this.mMessageItemCache.get(this.mContext, msgId + "");
    }

    public void onScaleChanged(float ScaleSize) {
        setTextScale(ScaleSize);
        notifyDataSetChanged();
    }

    public void setTextScale(float scale) {
        if (scale >= 0.7f && scale <= 3.0f) {
            this.mScale = scale;
        }
    }

    private boolean needShowTimePhase(int position, Cursor cursor) {
        Long timeStampPre = (Long) this.mMessageTimeStampMap.get(Integer.valueOf(position - 1));
        Long timeStampCurrent = (Long) this.mMessageTimeStampMap.get(Integer.valueOf(position));
        if (timeStampPre == null) {
            if (!cursor.moveToPosition(position - 1)) {
                return true;
            }
            timeStampPre = getSMSDate(cursor);
            cursor.moveToPosition(position);
            this.mMessageTimeStampMap.put(Integer.valueOf(position + 1), timeStampPre);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampPre.longValue());
        int preMsgDay = calendar.get(6);
        int preMsgYear = calendar.get(1);
        calendar.setTimeInMillis(timeStampCurrent.longValue());
        int curMsgDay = calendar.get(6);
        if (preMsgYear == calendar.get(1) && preMsgDay == curMsgDay) {
            return false;
        }
        return true;
    }

    private Long getSMSDate(Cursor cursor) {
        Long timeStampt = Long.valueOf(0);
        try {
            if (this.mColumnMap != null) {
                timeStampt = Long.valueOf(cursor.getLong(this.mColumnMap.columnDate));
            }
        } catch (IllegalArgumentException e) {
            MLog.w("RcsGroupChatMessageListAdapter", "getSMSDate(),get time stamp error");
        }
        if (timeStampt.longValue() == 0 || timeStampt.longValue() == 1) {
            return Long.valueOf(-1);
        }
        if (timeStampt.longValue() < 10000000000L) {
            timeStampt = Long.valueOf(timeStampt.longValue() * 1000);
        }
        return timeStampt;
    }

    public void clearCachedListeItemTimes() {
        synchronized (this.mMessageTimeStampMap) {
            this.mMessageTimeStampMap.clear();
        }
    }
}
