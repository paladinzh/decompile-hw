package com.android.mms.ui;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.http.AndroidHttpClient;
import android.os.Handler;
import android.util.LruCache;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AbsListView.RecyclerListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.mms.ui.menu.ISmartSmsUIHolder;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.MmsApp;
import com.android.mms.transaction.MmsConnectionManager;
import com.android.mms.ui.CryptoMessageListAdapter.CryptoMessageViewListener;
import com.android.mms.ui.TextSpanLinkingCache.DataLoadedListener;
import com.android.mms.util.ItemLayoutCallback;
import com.android.rcs.ui.RcsMessageListAdapter;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EditableList;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsScaleSupport.SacleListener;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class MessageListAdapter extends CursorAdapter implements OnScrollListener, DataLoadedListener, SacleListener {
    private static final int[] ALL_SUPOORT_RES = new int[]{R.layout.message_list_item_send, R.layout.message_list_item_recv};
    static final String[] PROJECTION = new String[]{"transport_type", "_id", "thread_id", "address", "body", "sub_id", "date", "date_sent", "read", NumberInfo.TYPE_KEY, "status", "locked", "error_code", "sub", "sub_cs", "date", "date_sent", "read", "m_type", "msg_box", "d_rpt", "rr", "err_type", "locked", "st", "network_type", "text_only", "date_favadd", "origin_id", "subject", "service_center", "addr_body", "time_body", "is_secret", "risk_url_body", "service_kind"};
    static final String[] SINGLE_VIEW_PROJECTION = new String[]{"transport_type", "_id", "thread_id", "address", "body", "sub_id", "date", "date_sent", "read", NumberInfo.TYPE_KEY, "status", "locked", "error_code", "sub", "sub_cs", "date", "date_sent", "read", "m_type", "msg_box", "d_rpt", "rr", "err_type", "locked", "st", "network_type", "text_only", "date_favadd", "origin_id", "subject", "service_center", "addr_body", "time_body", "is_secret", "risk_url_body", "group_id", "group_all", "group_sent", "group_fail"};
    static final int[] layoutResIds = new int[]{R.layout.message_list_item_recv, R.layout.message_list_item_send};
    protected static final ColumnsMap mColumnsMapDefault = new ColumnsMap();
    static final Bitmap[] msgItemBmRes = new Bitmap[msgItemDwResIds.length];
    static final int[] msgItemDwResIds = new int[]{R.drawable.ic_lock_message_sms, R.drawable.ic_list_alert_sms_failed, R.drawable.csp_menu_collapse_dark, R.drawable.csp_menu_expand, R.drawable.bg_message_sent_project};
    private static final Map<String, MmsConnectionManager> sMmsConnectionManagerMap = new HashMap();
    private int boxType;
    private boolean isFromSimCardSms;
    public ListScrollAnimation listScrollAnimation;
    private String mAddress;
    private List<View> mCachedReceiveItems;
    private List<View> mCachedSendItems;
    public ColumnsMap mColumnsMap;
    public Context mContext;
    private CryptoMessageListAdapter mCryptoMsgLiatAdapter;
    public Pattern mHighlight;
    private HwCustMessageListAdapter mHwCustMessageListAdapter;
    private boolean mInFavorites;
    private boolean mInIcc;
    protected LayoutInflater mInflater;
    private boolean mIsDarkThemeOn;
    private boolean mIsFling;
    private boolean mIsGroupConversation;
    private boolean mIsMultiSimActive;
    private ItemLayoutCallback<MessageItem> mLastItemLayoutCallback;
    public EditableList mListView;
    protected final MessageItemCache mMessageItemCache;
    private Map<Integer, Long> mMessageTimeStampMap;
    protected Handler mMsgListItemHandler;
    private OnDataSetChangedListener mOnDataSetChangedListener;
    private RcsMessageListAdapter mRcsMessageListAdapter;
    float mScale;
    private ISmartSmsUIHolder mSmartSmsUIHolder;
    TextSpanLinkingCache mTextSpanLinkingCache;

    public interface OnDataSetChangedListener {
        void onContentChanged(MessageListAdapter messageListAdapter);

        void onDataSetChanged(MessageListAdapter messageListAdapter);
    }

    public static class ColumnsMap {
        public int mColumnAddrInBody;
        public int mColumnGroupAll;
        public int mColumnGroupFail;
        public int mColumnGroupSent;
        public int mColumnIsSecret;
        public int mColumnMmsDate;
        public int mColumnMmsDateSent;
        public int mColumnMmsDeliveryReport;
        public int mColumnMmsErrorType;
        public int mColumnMmsLocked;
        public int mColumnMmsMessageBox;
        public int mColumnMmsMessageType;
        public int mColumnMmsReadReport;
        public int mColumnMmsStatus;
        public int mColumnMmsSubject;
        public int mColumnMmsSubjectCharset;
        public int mColumnMmsTextOnly;
        public int mColumnMsgId;
        public int mColumnMsgType;
        public int mColumnNetworkType;
        public int mColumnRiskUrlInBody;
        public int mColumnSmsAddress;
        public int mColumnSmsBody;
        public int mColumnSmsDate;
        public int mColumnSmsDateSent;
        public int mColumnSmsErrorCode;
        public int mColumnSmsLocked;
        public int mColumnSmsServiceCenter;
        public int mColumnSmsStatus;
        public int mColumnSmsSubject;
        public int mColumnSmsType;
        public int mColumnSubId;
        public int mColumnTimeInBody;
        public int mColumnUID;

        public ColumnsMap() {
            this.mColumnMsgType = 0;
            this.mColumnMsgId = 1;
            this.mColumnSmsAddress = 3;
            this.mColumnSmsBody = 4;
            this.mColumnSubId = 5;
            this.mColumnSmsDate = 6;
            this.mColumnSmsDateSent = 7;
            this.mColumnSmsType = 9;
            this.mColumnSmsStatus = 10;
            this.mColumnSmsLocked = 11;
            this.mColumnSmsErrorCode = 12;
            this.mColumnMmsSubject = 13;
            this.mColumnMmsSubjectCharset = 14;
            this.mColumnMmsMessageType = 18;
            this.mColumnMmsMessageBox = 19;
            this.mColumnMmsDeliveryReport = 20;
            this.mColumnMmsReadReport = 21;
            this.mColumnMmsErrorType = 22;
            this.mColumnMmsLocked = 23;
            this.mColumnSubId = 5;
            this.mColumnNetworkType = 25;
            this.mColumnMmsTextOnly = 26;
            this.mColumnUID = 35;
            this.mColumnGroupAll = 36;
            this.mColumnGroupSent = 37;
            this.mColumnGroupFail = 38;
            this.mColumnMmsDate = 15;
            this.mColumnSmsSubject = 29;
            this.mColumnSmsServiceCenter = 30;
            this.mColumnMmsDateSent = 16;
            this.mColumnAddrInBody = 31;
            this.mColumnTimeInBody = 32;
            this.mColumnIsSecret = 33;
            this.mColumnRiskUrlInBody = 34;
        }

        public ColumnsMap(Cursor cursor) {
            try {
                this.mColumnMsgType = cursor.getColumnIndexOrThrow("transport_type");
            } catch (IllegalArgumentException e) {
                MLog.w("colsMap", e.getMessage());
            }
            try {
                this.mColumnMsgId = cursor.getColumnIndexOrThrow("_id");
            } catch (IllegalArgumentException e2) {
                MLog.w("colsMap", e2.getMessage());
            }
            try {
                this.mColumnSmsAddress = cursor.getColumnIndexOrThrow("address");
            } catch (IllegalArgumentException e22) {
                MLog.w("colsMap", e22.getMessage());
            }
            try {
                this.mColumnSmsBody = cursor.getColumnIndexOrThrow("body");
            } catch (IllegalArgumentException e222) {
                MLog.w("colsMap", e222.getMessage());
            }
            if (MessageUtils.isMultiSimEnabled()) {
                try {
                    this.mColumnSubId = cursor.getColumnIndexOrThrow("sub_id");
                } catch (IllegalArgumentException e2222) {
                    this.mColumnSubId = -1;
                    MLog.w("colsMap", e2222.getMessage());
                }
                try {
                    this.mColumnNetworkType = cursor.getColumnIndexOrThrow("network_type");
                } catch (IllegalArgumentException e22222) {
                    MLog.w("colsMap", e22222.getMessage());
                }
            }
            try {
                this.mColumnSmsDate = cursor.getColumnIndexOrThrow("date");
            } catch (IllegalArgumentException e222222) {
                MLog.w("colsMap", e222222.getMessage());
            }
            try {
                this.mColumnSmsDateSent = cursor.getColumnIndexOrThrow("date_sent");
            } catch (IllegalArgumentException e2222222) {
                MLog.w("colsMap", e2222222.getMessage());
                if (MessageUtils.IS_CHINA_TELECOM_OPTA_OPTB) {
                    this.mColumnSmsDateSent = this.mColumnSmsDate;
                }
            }
            try {
                this.mColumnMmsDate = cursor.getColumnIndexOrThrow("date");
                this.mColumnMmsDateSent = cursor.getColumnIndexOrThrow("date_sent");
            } catch (IllegalArgumentException e22222222) {
                MLog.w("colsMap", e22222222.getMessage());
                if (MessageUtils.IS_CHINA_TELECOM_OPTA_OPTB) {
                    this.mColumnMmsDateSent = this.mColumnMmsDate;
                }
            }
            try {
                this.mColumnSmsType = cursor.getColumnIndexOrThrow(NumberInfo.TYPE_KEY);
            } catch (IllegalArgumentException e222222222) {
                MLog.w("colsMap", e222222222.getMessage());
            }
            try {
                this.mColumnSmsStatus = cursor.getColumnIndexOrThrow("status");
            } catch (IllegalArgumentException e2222222222) {
                MLog.w("colsMap", e2222222222.getMessage());
            }
            try {
                this.mColumnSmsLocked = cursor.getColumnIndexOrThrow("locked");
            } catch (IllegalArgumentException e22222222222) {
                MLog.w("colsMap", e22222222222.getMessage());
            }
            try {
                this.mColumnSmsErrorCode = cursor.getColumnIndexOrThrow("error_code");
            } catch (IllegalArgumentException e222222222222) {
                MLog.w("colsMap", e222222222222.getMessage());
            }
            try {
                this.mColumnMmsSubject = cursor.getColumnIndexOrThrow("sub");
            } catch (IllegalArgumentException e2222222222222) {
                MLog.w("colsMap", e2222222222222.getMessage());
            }
            try {
                this.mColumnMmsSubjectCharset = cursor.getColumnIndexOrThrow("sub_cs");
            } catch (IllegalArgumentException e22222222222222) {
                MLog.w("colsMap", e22222222222222.getMessage());
            }
            try {
                this.mColumnMmsMessageType = cursor.getColumnIndexOrThrow("m_type");
            } catch (IllegalArgumentException e222222222222222) {
                MLog.w("colsMap", e222222222222222.getMessage());
            }
            try {
                this.mColumnMmsMessageBox = cursor.getColumnIndexOrThrow("msg_box");
            } catch (IllegalArgumentException e2222222222222222) {
                MLog.w("colsMap", e2222222222222222.getMessage());
            }
            try {
                this.mColumnMmsDeliveryReport = cursor.getColumnIndexOrThrow("d_rpt");
            } catch (IllegalArgumentException e22222222222222222) {
                MLog.w("colsMap", e22222222222222222.getMessage());
            }
            try {
                this.mColumnMmsReadReport = cursor.getColumnIndexOrThrow("rr");
            } catch (IllegalArgumentException e222222222222222222) {
                MLog.w("colsMap", e222222222222222222.getMessage());
            }
            try {
                this.mColumnMmsErrorType = cursor.getColumnIndexOrThrow("err_type");
            } catch (IllegalArgumentException e2222222222222222222) {
                MLog.w("colsMap", e2222222222222222222.getMessage());
            }
            try {
                this.mColumnMmsLocked = cursor.getColumnIndexOrThrow("locked");
            } catch (IllegalArgumentException e22222222222222222222) {
                MLog.w("colsMap", e22222222222222222222.getMessage());
            }
            try {
                this.mColumnMmsStatus = cursor.getColumnIndexOrThrow("st");
                this.mColumnSmsServiceCenter = cursor.getColumnIndexOrThrow("service_center");
            } catch (IllegalArgumentException e222222222222222222222) {
                MLog.w("colsMap", e222222222222222222222.getMessage());
            }
            try {
                this.mColumnMmsTextOnly = cursor.getColumnIndexOrThrow("text_only");
            } catch (IllegalArgumentException e2222222222222222222222) {
                MLog.w("colsMap", e2222222222222222222222.getMessage());
            }
            try {
                this.mColumnUID = cursor.getColumnIndexOrThrow("group_id");
            } catch (IllegalArgumentException e22222222222222222222222) {
                MLog.e("colsMap", e22222222222222222222222.getMessage());
            }
            try {
                this.mColumnGroupAll = cursor.getColumnIndexOrThrow("group_all");
            } catch (IllegalArgumentException e222222222222222222222222) {
                MLog.e("colsMap", e222222222222222222222222.getMessage());
            }
            try {
                this.mColumnGroupSent = cursor.getColumnIndexOrThrow("group_sent");
            } catch (IllegalArgumentException e2222222222222222222222222) {
                MLog.e("colsMap", e2222222222222222222222222.getMessage());
            }
            try {
                this.mColumnGroupFail = cursor.getColumnIndexOrThrow("group_fail");
            } catch (IllegalArgumentException e22222222222222222222222222) {
                MLog.e("colsMap", e22222222222222222222222222.getMessage());
            }
        }
    }

    protected static class MessageItemCache extends LruCache<Long, MessageItem> {
        public MessageItemCache(int maxSize) {
            super(maxSize);
        }

        protected void entryRemoved(boolean evicted, Long key, MessageItem oldValue, MessageItem newValue) {
            oldValue.cancelPduLoading();
        }
    }

    public RcsMessageListAdapter getRcsMessageListAdapter() {
        return this.mRcsMessageListAdapter;
    }

    public void setLastItemLayoutCallback(ItemLayoutCallback<MessageItem> callBack) {
        this.mLastItemLayoutCallback = callBack;
    }

    public void setMCryptoMsgLiatAdapterLister(CryptoMessageViewListener lister) {
        this.mCryptoMsgLiatAdapter.setmCryptoMessageViewListener(lister);
    }

    public MessageListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight, int subId, int loadType) {
        this(context, c, listView, useDefaultColumnsMap, highlight, loadType);
        this.mColumnsMap.mColumnSubId = subId;
        this.isFromSimCardSms = true;
        if (this.mRcsMessageListAdapter != null) {
            this.mRcsMessageListAdapter.setFromSimCardSms(this.isFromSimCardSms);
        }
    }

    public MessageListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight, String address) {
        this(context, c, listView, useDefaultColumnsMap, highlight, 0, address);
    }

    public MessageListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight, int loadType) {
        this(context, c, listView, useDefaultColumnsMap, highlight, loadType, null);
    }

    public MessageListAdapter(Context context, Cursor c, ListView listView, boolean useDefaultColumnsMap, Pattern highlight, int loadType, String address) {
        super(context, c, 2);
        this.mScale = ContentUtil.FONT_SIZE_NORMAL;
        this.boxType = 0;
        this.mIsFling = false;
        this.mIsDarkThemeOn = false;
        this.isFromSimCardSms = false;
        this.mSmartSmsUIHolder = null;
        this.mCachedSendItems = new Vector();
        this.mCachedReceiveItems = new Vector();
        this.mMessageTimeStampMap = new HashMap();
        this.mCryptoMsgLiatAdapter = new CryptoMessageListAdapter();
        this.mInFavorites = false;
        this.mInIcc = false;
        this.mContext = context;
        this.mAddress = address;
        this.mScale = PreferenceUtils.getPreferenceFloat(this.mContext, "pref_key_sms_font_scale", ContentUtil.FONT_SIZE_NORMAL);
        for (int i = 0; i < msgItemDwResIds.length; i++) {
            msgItemBmRes[i] = BitmapFactory.decodeResource(MmsApp.getApplication().getResources(), msgItemDwResIds[i]);
        }
        this.mHighlight = highlight;
        this.mInflater = (LayoutInflater) this.mContext.getSystemService("layout_inflater");
        if (loadType == 0) {
            HwBackgroundLoader.getBackgroundHandler().post(new Runnable() {
                public void run() {
                    MessageListAdapter.this.cacheMessageListItems();
                }
            });
        }
        this.mMessageItemCache = new MessageItemCache(VTMCDataCache.MAXSIZE);
        if (useDefaultColumnsMap) {
            this.mColumnsMap = new ColumnsMap();
        } else if (c != null) {
            this.mColumnsMap = new ColumnsMap(c);
        }
        if (listView instanceof EditableList) {
            this.mListView = (EditableList) listView;
        }
        this.listScrollAnimation = new ListScrollAnimation(context, listView);
        listView.setOnScrollListener(this);
        this.mIsMultiSimActive = MessageUtils.isMultiSimActive();
        this.mTextSpanLinkingCache = TextSpanLinkingCache.getInstance(this.mContext);
        this.mTextSpanLinkingCache.addDataLoadedListener(this);
        listView.setRecyclerListener(new RecyclerListener() {
            public void onMovedToScrapHeap(View view) {
                if (view instanceof MessageListItem) {
                    ((MessageListItem) view).unbind();
                }
            }
        });
        this.mHwCustMessageListAdapter = (HwCustMessageListAdapter) HwCustUtils.createObj(HwCustMessageListAdapter.class, new Object[]{context});
        this.mRcsMessageListAdapter = new RcsMessageListAdapter(context, this);
        this.mRcsMessageListAdapter.setRcsMessageListAdapter(this.mContext, this);
        if (context instanceof ISmartSmsUIHolder) {
            this.mSmartSmsUIHolder = (ISmartSmsUIHolder) context;
        }
        if (MessageItem.mMmsRiskUrlCache != null) {
            MessageItem.mMmsRiskUrlCache.clear();
        }
        clearRichUrlCache();
        HwMessageUtils.setNetWorkState(true);
    }

    private void clearRichUrlCache() {
        if (MessageItem.msmsRichUrlCache != null) {
            MessageItem.msmsRichUrlCache.clear();
        }
    }

    public void setMultiSimActive(boolean active) {
        this.mIsMultiSimActive = active;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        if (!this.mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        } else if (this.mCursor.moveToPosition(position)) {
            View v;
            this.boxType = getItemViewType(this.mCursor);
            if (convertView == null) {
                v = newView(this.mContext, this.mCursor, parent);
                if (v instanceof MessageListItem) {
                    MessageListItem mli = (MessageListItem) v;
                    mli.setListView((MessageListView) this.mListView);
                    mli.setTextSpanLinkingCache(this.mTextSpanLinkingCache);
                }
            } else {
                v = convertView;
            }
            bindView(v, this.mContext, this.mCursor);
            if (v != null) {
                v.clearAnimation();
            }
            this.listScrollAnimation.startAnim(v, position);
            return v;
        } else {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }
    }

    public void bindView(View view, Context context, Cursor cursor) {
        if (view instanceof MessageListItem) {
            MessageItem msgItem = getCachedMessageItem(cursor.getString(this.mColumnsMap.mColumnMsgType), cursor.getLong(this.mColumnsMap.mColumnMsgId), cursor);
            if (msgItem != null) {
                boolean isSelected;
                boolean z;
                MessageListItem mli = (MessageListItem) view;
                mli.setSearchString(this.mHwCustMessageListAdapter != null ? this.mHwCustMessageListAdapter.getSearchString() : "");
                if (this.mRcsMessageListAdapter != null) {
                    mli = this.mRcsMessageListAdapter.bindView(view, cursor, mli);
                    mli.setTextSpanLinkingCache(this.mTextSpanLinkingCache);
                }
                mli.setItemLayoutCallback(this.mLastItemLayoutCallback);
                mli.setDarkThemeStatus(this.mIsDarkThemeOn);
                mli.setTextScale(this.mScale);
                int position = cursor.getPosition();
                if (this.mHwCustMessageListAdapter != null) {
                    this.mHwCustMessageListAdapter.highlightMessageListItem(mli, position, msgItem.mType, this.boxType);
                }
                boolean isInEditMode = this.mListView != null ? this.mListView.isInEditMode() : false;
                if (this.mListView != null) {
                    isSelected = ((MessageListView) this.mListView).isSelected(msgItem.mType, msgItem.mMsgId, msgItem.mUid, msgItem.mLocked);
                } else {
                    isSelected = false;
                }
                mli.setEditAble(isInEditMode);
                mli.setMultiChoice(isInEditMode);
                if (this.mRcsMessageListAdapter != null) {
                    this.mRcsMessageListAdapter.setThreadId(mli);
                }
                mli.setFlingState(this.mIsFling);
                this.mMessageTimeStampMap.put(Integer.valueOf(position), Long.valueOf(msgItem.mDate));
                mli.setNeedShowTimePhase(needShowTimePhase(position, cursor, msgItem.mSubId));
                mli.bind(msgItem, this.mIsGroupConversation, position, this.mIsMultiSimActive, this.mAddress);
                mli.setMsgListItemHandler(this.mMsgListItemHandler);
                this.mCryptoMsgLiatAdapter.setAdapterForMsgListItem(mli, this);
                if (mli.getMessageItem().isMms() && this.mListView != null && this.mListView.getViewMode() == 3) {
                    mli.setCheckBoxEnable(!isInEditMode);
                } else {
                    mli.setCheckBoxEnable(true);
                    mli.setChecked(isInEditMode, isSelected);
                }
                CryptoMessageListAdapter cryptoMessageListAdapter = this.mCryptoMsgLiatAdapter;
                Context context2 = this.mContext;
                long j = msgItem.mDate;
                if (position == getCount() - 1) {
                    z = true;
                } else {
                    z = false;
                }
                cryptoMessageListAdapter.dealwithEncryptoSms(context2, mli, j, z);
            }
        }
    }

    private boolean needShowTimePhase(int position, Cursor cursor, int subId) {
        Long timeStampPre = (Long) this.mMessageTimeStampMap.get(Integer.valueOf(position - 1));
        Long timeStampCurrent = (Long) this.mMessageTimeStampMap.get(Integer.valueOf(position));
        if (timeStampPre == null) {
            if (!cursor.moveToPosition(position - 1)) {
                return true;
            }
            timeStampPre = getSMSDate(cursor, 6);
            Long timeStampSentPre = getSMSDate(cursor, 7);
            cursor.moveToPosition(position);
            if (timeStampSentPre.longValue() != -1) {
                timeStampPre = getDisplayTime(timeStampPre.longValue(), timeStampSentPre.longValue(), subId);
            }
            this.mMessageTimeStampMap.put(Integer.valueOf(position - 1), timeStampPre);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStampPre.longValue());
        int preMsgYear = calendar.get(1);
        int preMsgDay = calendar.get(6);
        calendar.setTimeInMillis(timeStampCurrent.longValue());
        int curMsgYear = calendar.get(1);
        int curMsgDay = calendar.get(6);
        if (preMsgYear == curMsgYear && preMsgDay == curMsgDay) {
            return false;
        }
        return true;
    }

    private Long getSMSDate(Cursor cursor, int cloumnIndex) {
        Long timeStampt = Long.valueOf(cursor.getLong(cloumnIndex));
        if (timeStampt.longValue() == 0 || timeStampt.longValue() == 1) {
            return Long.valueOf(-1);
        }
        if (timeStampt.longValue() < 10000000000L) {
            timeStampt = Long.valueOf(timeStampt.longValue() * 1000);
        }
        return timeStampt;
    }

    private Long getDisplayTime(long localTime, long networkTime, int subId) {
        if (HwMessageUtils.displayMmsSentTime(localTime, networkTime, subId)) {
            return Long.valueOf(networkTime);
        }
        return Long.valueOf(localTime);
    }

    public void setOnDataSetChangedListener(OnDataSetChangedListener l) {
        this.mOnDataSetChangedListener = l;
    }

    public void setMsgListItemHandler(Handler handler) {
        this.mMsgListItemHandler = handler;
    }

    public void setIsGroupConversation(boolean isGroup) {
        this.mIsGroupConversation = isGroup;
    }

    public void cancelBackgroundLoading() {
        this.mMessageItemCache.evictAll();
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        MLog.v("MessageListAdapter", "MessageListAdapter.notifyDataSetChanged().");
        if (MessageUtils.getZoomFlag()) {
            MessageUtils.setZoomFlag(false);
        }
        this.mMessageItemCache.evictAll();
        if (this.mOnDataSetChangedListener != null) {
            this.mOnDataSetChangedListener.onDataSetChanged(this);
        }
    }

    protected void onContentChanged() {
        if (getCursor() != null && !getCursor().isClosed() && this.mOnDataSetChangedListener != null) {
            this.mOnDataSetChangedListener.onContentChanged(this);
        }
    }

    protected int getItemViewResId(Context context, int boxType) {
        boolean isIncoming = true;
        if (!(boxType == 0 || boxType == 2)) {
            isIncoming = false;
        }
        int type = isIncoming ? 1 : 0;
        if (type < ALL_SUPOORT_RES.length) {
            return ALL_SUPOORT_RES[type];
        }
        return 0;
    }

    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view;
        boolean z = true;
        if (this.boxType == 0 || 2 == this.boxType) {
            view = getCachedReceiveListItem();
        } else {
            view = getCachedSendListItem();
        }
        if (view == null) {
            view = this.mInflater.inflate(getItemViewResId(context, this.boxType), parent, false);
            if (view instanceof MessageListItem) {
                MessageListItem mli = (MessageListItem) view;
                if (!(this.boxType == 0 || 2 == this.boxType)) {
                    z = false;
                }
                mli.initTextColor(z);
            }
        }
        if (this.mRcsMessageListAdapter != null) {
            view = this.mRcsMessageListAdapter.newView(this.mInflater, parent, this.boxType, view);
        }
        return (this.boxType == 2 || this.boxType == 3) ? view : view;
    }

    private final void checkCollumnMap() {
        if (this.mInIcc) {
            if (this.mColumnsMap == mColumnsMapDefault) {
                setColumnMap(false);
            }
        } else if (this.mColumnsMap != mColumnsMapDefault) {
            setColumnMap(true);
        }
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c) {
        int i = this.mInFavorites ? 1 : this.mInIcc ? 3 : 0;
        return getCachedMessageItem(type, msgId, c, i);
    }

    public MessageItem getCachedMessageItem(String type, long msgId, Cursor c, int loadType) {
        if (loadType == 1) {
            return getCachedFavoritesMessageItem(type, msgId, c);
        }
        checkCollumnMap();
        MessageItem item = (this.mRcsMessageListAdapter == null || !this.mRcsMessageListAdapter.isChatType(type)) ? (MessageItem) this.mMessageItemCache.get(Long.valueOf(getKey(type, msgId))) : null;
        if (item == null && c != null && isCursorValid(c)) {
            try {
                if (this.mRcsMessageListAdapter == null || !this.mRcsMessageListAdapter.isChatType(type)) {
                    item = new MessageItem(this.mContext, type, c, this.mColumnsMap, this.mHighlight, loadType, false, this.mIsGroupConversation);
                } else {
                    item = this.mRcsMessageListAdapter.getCachedMessageItem(type, msgId, c, loadType, false, this.mIsGroupConversation);
                }
                if ("sms".equals(type) && this.isFromSimCardSms && item != null) {
                    item.mSubId = this.mColumnsMap.mColumnSubId;
                }
                if ((this.mRcsMessageListAdapter == null || !this.mRcsMessageListAdapter.isChatType(type)) && item != null) {
                    this.mMessageItemCache.put(Long.valueOf(getKey(item.mType, item.mMsgId)), item);
                }
            } catch (MmsException e) {
                MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) e);
            } catch (NullPointerException ee) {
                MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) ee);
            }
        }
        return item;
    }

    public void setInFavorites(boolean favorites) {
        this.mInFavorites = favorites;
    }

    private MessageItem getCachedFavoritesMessageItem(String type, long msgId, Cursor c) {
        MessageItem messageItem;
        MmsException e;
        NullPointerException ee;
        MessageItem item = (MessageItem) this.mMessageItemCache.get(Long.valueOf(getKey(type, msgId)));
        if (item != null || c == null) {
            return item;
        }
        if (!isCursorValid(c)) {
            return item;
        }
        try {
            messageItem = new MessageItem(this.mContext, type, c, this.mColumnsMap, this.mHighlight, 1);
            try {
                if ("sms".equals(type)) {
                    messageItem.mSubId = this.mColumnsMap.mColumnSubId;
                    messageItem.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_SMS, messageItem.mMsgId);
                } else {
                    messageItem.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_MMS, messageItem.mMsgId);
                }
                this.mMessageItemCache.put(Long.valueOf(getKey(messageItem.mType, messageItem.mMsgId)), messageItem);
                return messageItem;
            } catch (MmsException e2) {
                e = e2;
                MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) e);
                return messageItem;
            } catch (NullPointerException e3) {
                ee = e3;
                MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) ee);
                return messageItem;
            }
        } catch (MmsException e4) {
            e = e4;
            messageItem = item;
            MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) e);
            return messageItem;
        } catch (NullPointerException e5) {
            ee = e5;
            messageItem = item;
            MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) ee);
            return messageItem;
        }
    }

    public MessageItem getCachedMessageItemWithIdAssigned(String msgType, long msgId, Cursor c) {
        return getCachedMessageItemWithIdAssigned(msgType, msgId, c, 0);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public MessageItem getCachedMessageItemWithIdAssigned(String msgType, long msgId, Cursor c, int loadType) {
        MmsException e;
        MessageItem item;
        NullPointerException ee;
        MessageItem item2 = (MessageItem) this.mMessageItemCache.get(Long.valueOf(getKey(msgType, msgId)));
        boolean isFound = false;
        if (item2 != null || c == null) {
            return item2;
        }
        if (!isCursorValid(c)) {
            return item2;
        }
        int posBefore = c.getPosition();
        c.moveToFirst();
        while (true) {
            try {
                if (!msgType.equals(c.getString(0)) || msgId != c.getLong(1)) {
                    if (!c.moveToNext()) {
                        break;
                    }
                } else {
                    break;
                }
            } catch (MmsException e2) {
                e = e2;
                item = item2;
            } catch (NullPointerException e3) {
                ee = e3;
                item = item2;
            }
        }
        if (isFound) {
            if (1 == loadType) {
                item = new MessageItem(this.mContext, msgType, c, this.mColumnsMap, this.mHighlight, 1);
                try {
                    if ("sms".equals(msgType)) {
                        item.mSubId = this.mColumnsMap.mColumnSubId;
                        item.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_SMS, item.mMsgId);
                    } else {
                        item.mMessageUri = ContentUris.withAppendedId(FavoritesUtils.URI_FAV_MMS, item.mMsgId);
                    }
                } catch (MmsException e4) {
                    e = e4;
                    MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) e);
                    c.moveToPosition(posBefore);
                    return item;
                } catch (NullPointerException e5) {
                    ee = e5;
                    MLog.e("MessageListAdapter", "getCachedMessageItem: ", (Throwable) ee);
                    c.moveToPosition(posBefore);
                    return item;
                }
            }
            item = new MessageItem(this.mContext, msgType, c, this.mColumnsMap, this.mHighlight, 0, false, this.mIsGroupConversation);
            if ("sms".equals(msgType) && this.isFromSimCardSms) {
                item.mSubId = this.mColumnsMap.mColumnSubId;
            }
            this.mMessageItemCache.put(Long.valueOf(getKey(item.mType, item.mMsgId)), item);
        } else {
            item = item2;
        }
        c.moveToPosition(posBefore);
        return item;
    }

    public boolean isCursorValid(Cursor cursor) {
        if (cursor == null || cursor.isClosed() || cursor.isBeforeFirst() || cursor.isAfterLast()) {
            return false;
        }
        return true;
    }

    public static long getKey(String type, long id) {
        return getKey("mms".equals(type), id);
    }

    public static long getKey(boolean isMms, long id) {
        return isMms ? -id : id;
    }

    public boolean areAllItemsEnabled() {
        return true;
    }

    public int getViewTypeCount() {
        if (this.mRcsMessageListAdapter == null || !this.mRcsMessageListAdapter.isRcsSwitchOn()) {
            return 4;
        }
        return this.mRcsMessageListAdapter.getViewTypeCount();
    }

    public int getItemViewType(int position) {
        Cursor cursor = (Cursor) getItem(position);
        if (cursor == null) {
            return -1;
        }
        return getItemViewType(cursor);
    }

    protected int getItemViewType(Cursor cursor) {
        int i = 1;
        String type = cursor.getString(this.mColumnsMap.mColumnMsgType);
        int boxId;
        if ("sms".equals(type)) {
            boxId = cursor.getInt(this.mColumnsMap.mColumnSmsType);
            if (boxId == 1 || boxId == 0) {
                i = 0;
            }
            return i;
        } else if (this.mRcsMessageListAdapter == null || !this.mRcsMessageListAdapter.isChatType(type)) {
            boxId = cursor.getInt(this.mColumnsMap.mColumnMmsMessageBox);
            i = (boxId == 1 || boxId == 0) ? 2 : 3;
            return i;
        } else {
            return this.mRcsMessageListAdapter.getItemViewType(cursor, cursor.getInt(this.mColumnsMap.mColumnSmsType));
        }
    }

    public Cursor getCursorForItem(MessageItem item) {
        Cursor cursor = getCursor();
        if (!isCursorValid(cursor) || !cursor.moveToFirst()) {
            return null;
        }
        do {
            if (cursor.getLong(this.mRowIDColumn) == item.mMsgId && item.mType != null && item.mType.equals(cursor.getString(0))) {
                return cursor;
            }
        } while (cursor.moveToNext());
        return null;
    }

    public ColumnsMap getColumnsMap() {
        return this.mColumnsMap;
    }

    public void setColumnMap(boolean isdefault) {
        if (isdefault) {
            this.mColumnsMap = mColumnsMapDefault;
        } else if (getCursor() != null) {
            this.mColumnsMap = new ColumnsMap(getCursor());
        }
    }

    public void setTextScale(float scale) {
        if (scale >= 0.7f && scale <= 3.0f) {
            this.mScale = scale;
        }
    }

    public void onScaleChanged(float ScaleSize) {
        setTextScale(ScaleSize);
        notifyDataSetChanged();
    }

    public long getItemId(int pos) {
        if (this.mInIcc) {
            if (this.mCursor != null && this.mCursor.moveToPosition(pos)) {
                return ManageSimMessages.getSimItemId(this.mCursor);
            }
        } else if (this.mCursor != null && this.mCursor.moveToPosition(pos)) {
            long msgId = this.mCursor.getLong(1);
            if (MessageItem.isMms(this.mCursor.getString(0))) {
                msgId = -msgId;
            }
            return msgId;
        }
        return 0;
    }

    public String getType() {
        return this.mCursor.getString(0);
    }

    private static String getMessageIdFromUri(String uri) {
        return uri.substring(uri.lastIndexOf(47) + 1);
    }

    public static void saveConnectionManagerToMap(String uri, boolean isUserStop, boolean downloadingStatus, boolean manualDownloadMode, AndroidHttpClient mmsTransactionCleint) {
        synchronized (sMmsConnectionManagerMap) {
            String msgId = getMessageIdFromUri(uri);
            MmsConnectionManager mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(msgId);
            if (mmsConnectionManager == null) {
                mmsConnectionManager = new MmsConnectionManager();
            }
            mmsConnectionManager.setUserStopTransaction(isUserStop);
            mmsConnectionManager.setDownloadingStatus(downloadingStatus);
            mmsConnectionManager.setManualDownloadMode(manualDownloadMode);
            mmsConnectionManager.setMmsTransactionCleint(mmsTransactionCleint);
            if (MLog.isLoggable("Mms_app", 2)) {
                MLog.d("MessageListAdapter", "saveConnectionManagerToMap uri: " + uri + ", isUserStop:" + isUserStop + ", downloadingStatus:" + downloadingStatus + ", manualDownloadMode" + manualDownloadMode);
            }
            sMmsConnectionManagerMap.put(msgId, mmsConnectionManager);
        }
    }

    public static void removeConnectionManagerFromMap(String uri) {
        synchronized (sMmsConnectionManagerMap) {
            sMmsConnectionManagerMap.remove(getMessageIdFromUri(uri));
        }
    }

    public static MmsConnectionManager getConnectionManagerFromMap(String uri) {
        MmsConnectionManager mmsConnectionManager;
        synchronized (sMmsConnectionManagerMap) {
            mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(getMessageIdFromUri(uri));
        }
        return mmsConnectionManager;
    }

    public static boolean getUserStopTransaction(String uri) {
        synchronized (sMmsConnectionManagerMap) {
            MmsConnectionManager mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(getMessageIdFromUri(uri));
            if (mmsConnectionManager == null) {
                return false;
            }
            boolean userStopTransaction = mmsConnectionManager.getUserStopTransaction();
            return userStopTransaction;
        }
    }

    public static boolean getDownloadingStatusFromMap(String uri) {
        synchronized (sMmsConnectionManagerMap) {
            MmsConnectionManager mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(getMessageIdFromUri(uri));
            if (mmsConnectionManager == null) {
                return false;
            }
            boolean downloadingStatus = mmsConnectionManager.getDownloadingStatus();
            return downloadingStatus;
        }
    }

    public static boolean getManualDownloadFromMap(String uri) {
        synchronized (sMmsConnectionManagerMap) {
            MmsConnectionManager mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(getMessageIdFromUri(uri));
            if (mmsConnectionManager == null) {
                return false;
            }
            boolean manualDownloadMode = mmsConnectionManager.getManualDownloadMode();
            return manualDownloadMode;
        }
    }

    public static AndroidHttpClient getMmsTransactionCleintFromMap(String uri) {
        synchronized (sMmsConnectionManagerMap) {
            MmsConnectionManager mmsConnectionManager = (MmsConnectionManager) sMmsConnectionManagerMap.get(getMessageIdFromUri(uri));
            if (mmsConnectionManager == null) {
                return null;
            }
            AndroidHttpClient mmsTransactionCleint = mmsConnectionManager.getMmsTransactionCleint();
            return mmsTransactionCleint;
        }
    }

    public void onScroll(AbsListView listview, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        if (this.mCryptoMsgLiatAdapter != null) {
            this.mCryptoMsgLiatAdapter.onScroll(visibleItemCount);
        }
    }

    public void onScrollStateChanged(AbsListView view, int scrollState) {
        boolean z = false;
        if (scrollState != 0) {
            z = true;
        }
        this.mIsFling = z;
        this.mTextSpanLinkingCache.setFlingState(this.mIsFling);
        if (this.mSmartSmsUIHolder != null) {
            this.mSmartSmsUIHolder.setFlingState(this.mIsFling);
        }
        if (this.mCryptoMsgLiatAdapter != null) {
            this.mCryptoMsgLiatAdapter.onScrollStateChanged(view, scrollState);
        }
        if (scrollState == 0) {
            resetListScrollAnimation();
        }
    }

    public void resetListScrollAnimation() {
        this.listScrollAnimation.setDownTranslation(0.0f);
        this.listScrollAnimation.setUpTranslation(0.0f);
        this.listScrollAnimation.setVelocity(0.0f);
    }

    public void onDataLoaded() {
        if (!this.mIsFling) {
            notifyDataSetChanged();
        }
    }

    public void clearTextSpanCache(boolean destory) {
        this.mTextSpanLinkingCache.stopRequestProcessing();
        this.mTextSpanLinkingCache.clearLinkingCache(this.mContext, destory);
    }

    public CryptoMessageListAdapter getCryptoMessageListAdapter() {
        return this.mCryptoMsgLiatAdapter;
    }

    private void cacheMessageListItems() {
        for (int i = 0; i < 3; i++) {
            View viewReceived = this.mInflater.inflate(ALL_SUPOORT_RES[1], null);
            if (viewReceived instanceof MessageListItem) {
                ((MessageListItem) viewReceived).initTextColor(true);
            }
            synchronized (this.mCachedReceiveItems) {
                this.mCachedReceiveItems.add(viewReceived);
            }
            View viewSend = this.mInflater.inflate(ALL_SUPOORT_RES[0], null);
            if (viewSend instanceof MessageListItem) {
                ((MessageListItem) viewSend).initTextColor(false);
            }
            synchronized (this.mCachedSendItems) {
                this.mCachedSendItems.add(viewSend);
            }
        }
    }

    private View getCachedSendListItem() {
        synchronized (this.mCachedSendItems) {
            if (this.mCachedSendItems.size() > 0) {
                View view = (View) this.mCachedSendItems.remove(0);
                return view;
            }
            return null;
        }
    }

    private synchronized View getCachedReceiveListItem() {
        synchronized (this.mCachedReceiveItems) {
            if (this.mCachedReceiveItems.size() > 0) {
                View view = (View) this.mCachedReceiveItems.remove(0);
                return view;
            }
            return null;
        }
    }

    public void clearCachedListItems() {
        synchronized (this.mCachedReceiveItems) {
            this.mCachedReceiveItems.clear();
        }
        synchronized (this.mCachedSendItems) {
            this.mCachedSendItems.clear();
        }
    }

    public void clearCachedListeItemTimes() {
        synchronized (this.mMessageTimeStampMap) {
            this.mMessageTimeStampMap.clear();
        }
    }

    public void setSearchString(String aSearchString) {
        if (this.mHwCustMessageListAdapter != null) {
            this.mHwCustMessageListAdapter.setSearchString(aSearchString);
        }
    }

    public void setPositionList(Integer[] aPositions) {
        if (this.mHwCustMessageListAdapter != null) {
            this.mHwCustMessageListAdapter.setPositionList(aPositions);
        }
    }

    public void setDarkThemeStaus(int darkThemeStatus) {
        boolean z = false;
        if (darkThemeStatus != 0) {
            z = true;
        }
        this.mIsDarkThemeOn = z;
    }
}
