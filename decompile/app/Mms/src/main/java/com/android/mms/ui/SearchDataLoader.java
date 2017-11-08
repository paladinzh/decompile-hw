package com.android.mms.ui;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.provider.Telephony.MmsSms;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsSearchDataLoader;
import com.android.rcs.ui.RcsSearchRowInfo;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ArrayCursor;
import com.huawei.cspcommon.ex.ArrayCursor.CursorData;
import com.huawei.cspcommon.ex.AutoExtendArray;
import com.huawei.cspcommon.ex.HandlerEx;
import com.huawei.cspcommon.ex.HandlerEx.IExtendHandler;
import com.huawei.cspcommon.ex.MultiLoadHandler;
import com.huawei.cspcommon.ex.MultiLoadHandler.CursorMerger;
import com.huawei.cspcommon.ex.MultiLoadHandler.DataJob;
import com.huawei.cspcommon.ex.MultiLoadHandler.ILoadCallBack;
import com.huawei.cspcommon.ex.MultiLoadHandler.ReusedCursor;
import com.huawei.cspcommon.ex.ThreadEx.SerialExecutor;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;

public class SearchDataLoader extends MultiLoadHandler {
    static int POS_ADDRESS = 2;
    static int POS_DATE_TIME = 4;
    static int POS_INDEX_TEXT = 6;
    private static int POS_MATCHWORD = 4;
    static int POS_MSG_BODY = 3;
    static int POS_MSG_ID = 0;
    static int POS_TABLE_TYPE = 8;
    static int POS_THREADID = 1;
    private static int POS_TRANSPORT = 5;
    private static final String[] columnNames = new String[]{"_id", "thread_id", "address", "body", "match_word", "transport_type", "index_text", "table_to_use", "date"};
    private static RcsSearchDataLoader mStaticHwCust = new RcsSearchDataLoader();
    private Handler mBkHandler;
    private SerialExecutor mContactsExecutor;
    private CursorMerger mCursorMerger;
    private IExtendHandler mFreshHandler;
    private HwCustSearchDataLoader mHwCustSearchDataLoader;
    private boolean mIsPaused;
    private String mQueryText;
    private SerialExecutor mThreadsExecutor;

    public static class ConversationMatcher {
        private static ConversationMatcher sMatcher = new ConversationMatcher();
        private static Comparator<Row> sRowComparator = new Comparator<Row>() {
            public int compare(Row lhs, Row rhs) {
                return lhs.mDate - rhs.mDate > 0 ? -1 : 1;
            }
        };
        private boolean mIsBusy = false;
        ArrayList<Conversation> mMatchedConversations;
        ArrayList<String> mMatchedString;
        private String mQueryText;
        private int mWaitCounter = 0;

        public static ConversationMatcher getDefault() {
            return sMatcher;
        }

        private ConversationMatcher() {
        }

        public void reset() {
            synchronized (this) {
                this.mMatchedString = null;
                this.mMatchedConversations = null;
            }
        }

        private boolean waitForIdle() {
            Throwable th;
            boolean ret = true;
            synchronized (this) {
                try {
                    this.mWaitCounter++;
                    int loopCounter = 5;
                    while (this.mIsBusy) {
                        int loopCounter2;
                        try {
                            if (this.mWaitCounter <= 2) {
                                loopCounter2 = loopCounter - 1;
                                if (loopCounter != 0) {
                                    try {
                                        MLog.d("Mms_Search_Loader", "ConversationMatcher wait as busy");
                                        wait(5000);
                                    } catch (InterruptedException e) {
                                    }
                                    loopCounter = loopCounter2;
                                }
                            }
                            ret = false;
                        } catch (Throwable th2) {
                            th = th2;
                            loopCounter2 = loopCounter;
                        }
                    }
                    MLog.d("Mms_Search_Loader", "ConversationMatcher mark as busy");
                    this.mIsBusy = true;
                    this.mWaitCounter--;
                    return ret;
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        private void release() {
            synchronized (this) {
                this.mIsBusy = false;
                notifyAll();
            }
            MLog.d("Mms_Search_Loader", "ConversationMatcher mark as release");
        }

        private static char[] getPhoneMatchNumber(String query) {
            int len = query.length();
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < len; i++) {
                char c = query.charAt(i);
                if (PhoneNumberUtils.isReallyDialable(c)) {
                    sb.append(c);
                } else if (!MessageUtils.isSugarChar(c)) {
                    return new char[0];
                }
            }
            if (sb.length() <= 0) {
                return new char[0];
            }
            char[] ret = new char[sb.length()];
            sb.getChars(0, sb.length(), ret, 0);
            return ret;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public AutoExtendArray<Row> query(String query) {
            query = query.toLowerCase(Locale.getDefault());
            AutoExtendArray<Row> ret = null;
            if (!waitForIdle()) {
                return null;
            }
            try {
                ret = syncQuery(query);
                if (ret != null) {
                    ret.sort(sRowComparator);
                }
                release();
            } catch (Exception e) {
                MLog.i("Mms_Search_Loader", "syncQuery exception");
            } catch (Throwable th) {
                release();
            }
            return ret;
        }

        private boolean isSubQuery(String query) {
            int len = this.mQueryText == null ? 0 : this.mQueryText.length();
            if (len <= 0 || query.length() <= len) {
                return false;
            }
            return query.contains(this.mQueryText);
        }

        private AutoExtendArray<Row> syncQuery(String query) {
            MLog.d("Mms_Search_Loader", "ConversationMatcher syncQuery start.");
            if (query == null || query.length() == 0) {
                return null;
            }
            Collection<Conversation> collection = null;
            if (isSubQuery(query)) {
                MLog.d("Mms_Search_Loader", "Match conversations <sub query>");
                synchronized (this) {
                    collection = this.mMatchedConversations;
                }
            }
            if (collection == null) {
                collection = Conversation.getConversations();
                MLog.d("Mms_Search_Loader", "Match conversations with full data");
            }
            this.mQueryText = query;
            queryFromConversations(collection, query);
            AutoExtendArray<Row> retRows = new AutoExtendArray(Row.class);
            synchronized (this) {
                int total = this.mMatchedConversations.size();
                Context context = MmsApp.getApplication().getApplicationContext();
                for (int i = 0; i < total; i++) {
                    Conversation conv = (Conversation) this.mMatchedConversations.get(i);
                    String number = conv.getRecipients().formatNames(", ");
                    if (!(number.isEmpty() || number.contains(this.mQueryText))) {
                        number = conv.getRecipients().formatNamesAndNumbers(", ");
                    }
                    int count = conv.getMessageCount();
                    if (conv.hasDraft()) {
                        count++;
                    }
                    String msgCountStr = context.getResources().getQuantityString(R.plurals.search_message_count_result, count, new Object[]{Integer.valueOf(count)});
                    String searchResult = (String) this.mMatchedString.get(i);
                    if (SearchDataLoader.mStaticHwCust != null) {
                        if (SearchDataLoader.mStaticHwCust.isAddOtherRowContacts(conv.getHwCust() == null ? 0 : conv.getHwCust().getRcsThreadType())) {
                            SearchDataLoader.mStaticHwCust.addOtherRowContacts(retRows, conv, number, searchResult, msgCountStr);
                        }
                    }
                    retRows.add(new Row(conv.getThreadId(), number, msgCountStr, conv.getDate(), searchResult, searchResult));
                }
            }
            MLog.d("Mms_Search_Loader", "ConversationMatcher syncQuery end.");
            return retRows;
        }

        private void queryFromConversations(Collection<Conversation> all, String query) {
            char[] numberText = getPhoneMatchNumber(query);
            ArrayList<Conversation> matchedConversations = new ArrayList();
            ArrayList<String> matchedString = new ArrayList();
            for (Conversation c : all) {
                String matchText = isConversationMatch(c, query, numberText);
                if (matchText != null) {
                    matchedConversations.add(c);
                    matchedString.add(matchText);
                }
            }
            synchronized (this) {
                this.mMatchedConversations = matchedConversations;
                this.mMatchedString = matchedString;
            }
        }

        private String isConversationMatch(Conversation thread, String query, char[] numberText) {
            for (Contact c : thread.getRecipients()) {
                if (!c.isYpContact()) {
                    String name = c.getName();
                    if (name != null && name.length() >= query.length() && name.toLowerCase(Locale.getDefault()).contains(query)) {
                        return query;
                    }
                }
                String number = c.getNumber();
                if (SearchDataLoader.mStaticHwCust != null && thread.getHwCust() != null && thread.getHwCust().isGroupChat()) {
                    return SearchDataLoader.mStaticHwCust.getMatchStringForGroupChat(c.getName(), query);
                }
                if (c.isEmail()) {
                    if (number != null && number.length() >= query.length() && number.toLowerCase(Locale.getDefault()).contains(query)) {
                        return query;
                    }
                } else if (numberText.length > 0 && number != null) {
                    String ret = NumberUtils.searchInNumber(number.toCharArray(), numberText);
                    if (ret != null) {
                        return ret;
                    }
                }
            }
            return null;
        }
    }

    static class HintData extends CursorData {
        private int mCount;
        private int mPriority;

        public HintData(int count, int priority) {
            this.mCount = count;
            this.mPriority = priority;
        }

        public int getInt(int column) {
            if (column == 1) {
                return this.mCount;
            }
            if (column == 7) {
                return this.mPriority;
            }
            return 0;
        }
    }

    private static class MatchThreadJob implements DataJob {
        private String mQueryHint = null;

        public MatchThreadJob(String queryHint) {
            this.mQueryHint = queryHint;
        }

        public int getToken() {
            return 10001;
        }

        public String getJobName() {
            return "match:thread";
        }

        public Object loadData() {
            return computeRowsContact(this.mQueryHint);
        }

        private Cursor computeRowsContact(String queryText) {
            ConversationMatcher matcher = ConversationMatcher.getDefault();
            long startTime = SystemClock.uptimeMillis();
            AutoExtendArray<Row> rows = matcher.query(queryText);
            HwMessageUtils.showJlogByID(139, (int) (SystemClock.uptimeMillis() - startTime), "Mms::search matched messages!");
            if (rows == null) {
                return null;
            }
            return new ArrayCursor(rows, SearchDataLoader.columnNames);
        }
    }

    public static class Row extends CursorData {
        String mAddress;
        String mBody;
        long mDate;
        private RcsSearchRowInfo mHwCust;
        String mIndexText;
        String mMatchWord;
        long mThreadId;

        public Row(long threadId, String address, String body, long date, String matchWord, String indexText) {
            this.mThreadId = threadId;
            this.mAddress = address;
            this.mBody = body;
            this.mMatchWord = matchWord;
            this.mIndexText = indexText;
            this.mDate = date;
            if (RcsCommonConfig.isRCSSwitchOn()) {
                this.mHwCust = new RcsSearchRowInfo();
            }
        }

        public Row(long threadId, String address, String body, long date, String matchWord, String indexText, int witchTable) {
            this.mThreadId = threadId;
            this.mAddress = address;
            this.mBody = body;
            this.mMatchWord = matchWord;
            this.mIndexText = indexText;
            this.mDate = date;
            if (RcsCommonConfig.isRCSSwitchOn()) {
                this.mHwCust = new RcsSearchRowInfo();
            }
            if (this.mHwCust != null) {
                this.mHwCust.setWitchTable(witchTable);
            }
        }

        public int getInt(int column) {
            if (column != SearchDataLoader.POS_TABLE_TYPE) {
                return 0;
            }
            if (SearchDataLoader.mStaticHwCust == null || !SearchDataLoader.mStaticHwCust.isRcsSwitchOn() || this.mHwCust == null) {
                return 10;
            }
            return this.mHwCust.getWitchTable();
        }

        public long getLong(int column) {
            if (column == SearchDataLoader.POS_THREADID) {
                return this.mThreadId;
            }
            if (column == SearchDataLoader.POS_MSG_ID) {
                return this.mThreadId;
            }
            if (column == SearchDataLoader.POS_DATE_TIME) {
                return this.mDate;
            }
            return 0;
        }

        public short getShort(int column) {
            return (short) 0;
        }

        public String getString(int column) {
            if (column == SearchDataLoader.POS_ADDRESS) {
                return this.mAddress;
            }
            if (column == SearchDataLoader.POS_MSG_BODY) {
                return this.mBody;
            }
            if (column == SearchDataLoader.POS_MATCHWORD) {
                return this.mMatchWord;
            }
            if (column == SearchDataLoader.POS_INDEX_TEXT) {
                return this.mIndexText;
            }
            return null;
        }
    }

    private static class SearchReusedCursor extends ReusedCursor {
        public SearchReusedCursor(Cursor cursor, int priority, String tag) {
            super(cursor, priority, tag);
        }

        protected boolean isPriorityCollomn(int columnIndex) {
            return columnIndex == 7;
        }
    }

    public static void resetMsgIndex(Cursor c) {
        POS_MSG_ID = c.getColumnIndex(columnNames[0]);
        int i = 1 + 1;
        POS_THREADID = c.getColumnIndex(columnNames[1]);
        int i2 = i + 1;
        POS_ADDRESS = c.getColumnIndex(columnNames[i]);
        i = i2 + 1;
        POS_MSG_BODY = c.getColumnIndex(columnNames[i2]);
        i2 = i + 1;
        POS_MATCHWORD = c.getColumnIndex(columnNames[i]);
        i = i2 + 1;
        POS_TRANSPORT = c.getColumnIndex(columnNames[i2]);
        i2 = i + 1;
        POS_INDEX_TEXT = c.getColumnIndex(columnNames[i]);
        i = i2 + 1;
        POS_TABLE_TYPE = c.getColumnIndex(columnNames[i2]);
        i2 = i + 1;
        POS_DATE_TIME = c.getColumnIndex(columnNames[i]);
        MLog.d("Mms_Search_Loader", "ResetMsgIndex: " + POS_MSG_ID + " " + POS_THREADID + " " + POS_ADDRESS + " " + POS_MSG_BODY + " POS_MATCHWORD:" + POS_MATCHWORD + " POS_TRANSPORT:" + POS_TRANSPORT + " " + POS_TABLE_TYPE + " " + POS_INDEX_TEXT + " " + POS_DATE_TIME);
    }

    public boolean isUiPaused() {
        return this.mIsPaused;
    }

    public static int getColPosThreadId() {
        return POS_THREADID;
    }

    public static int getColPosAddress() {
        return POS_ADDRESS;
    }

    public static int parseCursorMsgId(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        return (int) cursor.getLong(POS_MSG_ID);
    }

    public static long parseCursorThreadId(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        return cursor.getLong(POS_THREADID);
    }

    public static int parseCursorTableType(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        return cursor.getInt(POS_TABLE_TYPE);
    }

    public static long parseCursorDataTime(Cursor cursor) {
        if (cursor == null) {
            return -1;
        }
        return cursor.getLong(POS_DATE_TIME);
    }

    public static String parseCursorMsgBody(Cursor cursor) {
        if (cursor == null) {
            return null;
        }
        return cursor.getString(POS_MSG_BODY);
    }

    public SearchDataLoader(Context context, ILoadCallBack callBack) {
        super(context, callBack);
        this.mHwCustSearchDataLoader = (HwCustSearchDataLoader) HwCustUtils.createObj(HwCustSearchDataLoader.class, new Object[0]);
        this.mIsPaused = false;
        this.mBkHandler = null;
        this.mCursorMerger = new CursorMerger() {
            public Cursor getCursor() {
                final ReusedCursor[] cursorOut = getCursors(2);
                if (cursorOut == null) {
                    return null;
                }
                int total = cursorOut.length >> 1;
                for (int i = 0; i < total; i++) {
                    ReusedCursor rc = cursorOut[i];
                    rc.addReference();
                    if (rc.getCount() > 0) {
                        cursorOut[total + i] = SearchDataLoader.this.getHintCursor(rc.getCount(), rc.getPriority() - 1);
                    }
                }
                Arrays.sort(cursorOut, new Comparator<ReusedCursor>() {
                    public int compare(ReusedCursor lhs, ReusedCursor rhs) {
                        if (lhs == null) {
                            return 1;
                        }
                        if (rhs == null) {
                            return -1;
                        }
                        return lhs.getPriority() - rhs.getPriority();
                    }
                });
                return new MergeCursor(cursorOut) {
                    final ReusedCursor[] mChilds = cursorOut;

                    public void close() {
                        CursorMerger.closeCursors(this.mChilds);
                    }
                };
            }
        };
        this.mFreshHandler = new IExtendHandler() {
            private Runnable mContactsFresher = new Runnable() {
                public void run() {
                    Contact.freshCache();
                    AnonymousClass2.this.updateUI();
                }
            };
            private Runnable mThreadsFresher = new Runnable() {
                public void run() {
                    Conversation.cacheAllThreads(SearchDataLoader.this.mContext.getApplicationContext());
                    AnonymousClass2.this.updateUI();
                }
            };

            private void updateUI() {
                if (SearchDataLoader.this.isUiPaused()) {
                    SearchDataLoader.this.reset();
                } else {
                    SearchDataLoader.this.asyncSearchThreads(SearchDataLoader.this.mQueryText);
                }
            }

            public boolean handleMessage(Message msg) {
                Runnable fresher;
                String queryText = msg.getData().getString("search_string");
                MLog.d("Mms_Search_Loader", "FreshHandler run task " + getMessageName(msg.what));
                switch (msg.what) {
                    case 0:
                        if (!SearchDataLoader.this.isUiPaused()) {
                            SearchDataLoader.this.asyncSearchMessage(queryText);
                        }
                        return true;
                    case 1:
                        fresher = this.mThreadsFresher;
                        break;
                    case 2:
                        fresher = this.mContactsFresher;
                        break;
                    default:
                        return false;
                }
                if (fresher != null) {
                    SearchDataLoader.this.checkThreadsExecutor();
                    if (SearchDataLoader.this.mContactsExecutor == null) {
                        SearchDataLoader.this.mContactsExecutor = SearchDataLoader.this.createSerialExecutor();
                    }
                    SearchDataLoader.this.mContactsExecutor.execute(msg.what, fresher);
                }
                return true;
            }

            private String getMessageName(int what) {
                switch (what) {
                    case 0:
                        return HwCustSearchDataLoaderImpl.LOAD_TAG_MSG;
                    case 1:
                        return "match:thread";
                    case 2:
                        return "match:contact";
                    default:
                        return "SEARCHDATALOADER UNKONW TASK";
                }
            }
        };
        this.mBkHandler = HwBackgroundLoader.getBackgroundHandler();
        ((HandlerEx) this.mBkHandler).setExtendHandler(this.mFreshHandler);
    }

    public int getPriority(String tag) {
        if ("match:thread".equals(tag)) {
            return 1;
        }
        return HwCustSearchDataLoaderImpl.LOAD_TAG_MSG.equals(tag) ? 3 : 3;
    }

    private ReusedCursor getHintCursor(int total, int priority) {
        AutoExtendArray<HintData> data = new AutoExtendArray(HintData.class);
        data.add(new HintData(total, priority));
        return new SearchReusedCursor(new ArrayCursor(data, columnNames), priority, null);
    }

    protected Cursor mergeData(int token, String tag, Object result) {
        if (result != null && HwCustSearchDataLoaderImpl.LOAD_TAG_MSG.equals(tag)) {
            resetMsgIndex((Cursor) result);
        }
        if (result != null) {
            this.mCursorMerger.replace(tag, new SearchReusedCursor((Cursor) result, getPriority(tag), tag));
        }
        return this.mCursorMerger.getCursor();
    }

    public void asyncSearch(String searchString) {
        this.mQueryText = searchString;
        asyncSearchMessage(searchString);
        asyncSearchThreads(searchString);
    }

    public void asyncSearch(String searchString, String threadId) {
        if (this.mHwCustSearchDataLoader != null) {
            this.mHwCustSearchDataLoader.asyncSearchMessageInnerThread(searchString, threadId, this);
        }
    }

    public void reset() {
        this.mQueryText = null;
        ConversationMatcher.getDefault().reset();
    }

    public void onDataChanged(int type) {
        this.mQueryText = ConversationMatcher.getDefault().mQueryText;
        postMessageDealyed(type, this.mQueryText, 200);
    }

    private void checkThreadsExecutor() {
        if (this.mThreadsExecutor == null) {
            this.mThreadsExecutor = createSerialExecutor();
        }
    }

    private void postMessageDealyed(int what, String param, long delay) {
        this.mBkHandler.removeMessages(what);
        Message msg = this.mBkHandler.obtainMessage(what);
        msg.getData().putString("search_string", param);
        this.mBkHandler.sendMessageDelayed(msg, delay);
    }

    private void asyncSearchMessage(String searchString) {
        if (!TextUtils.isEmpty(searchString)) {
            Uri uri = MmsSms.SEARCH_URI.buildUpon().appendQueryParameter("pattern", HwMessageUtils.formatSqlString(searchString)).build();
            if (mStaticHwCust != null) {
                uri = mStaticHwCust.getNewUri(uri, searchString);
            }
            if (hasMessages(10001)) {
                removeMessages(10001);
            }
            startQuery(10001, HwCustSearchDataLoaderImpl.LOAD_TAG_MSG, uri, null, null, null, null);
        }
    }

    private void asyncSearchThreads(String searchString) {
        if (!TextUtils.isEmpty(searchString)) {
            checkThreadsExecutor();
            if (!Conversation.hasAllThreadsCached()) {
                this.mThreadsExecutor.execute(new Runnable() {
                    public void run() {
                        MLog.d("Mms_Search_Loader", "cacheAllThreads before query for threads");
                        Conversation.cacheAllThreads(SearchDataLoader.this.mContext);
                    }
                });
            }
            this.mThreadsExecutor.execute(0, obtainJobRunner().setJob(new MatchThreadJob(searchString)));
        }
    }
}
