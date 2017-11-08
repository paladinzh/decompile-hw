package com.android.mms.ui;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.util.LruCache;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.CommonGatherLinks;
import com.huawei.mms.util.SafetySmsParser;
import com.huawei.mms.util.TextSpan;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

public class TextSpanLinkingCache {
    private static Map<Integer, TextSpanLinkingCache> mSpanCaches = new HashMap();
    private Context mContext;
    private ExecutorService mExecutorService;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    if (TextSpanLinkingCache.this.mLinkingParsedListener.size() > 0) {
                        for (DataLoadedListener listener : TextSpanLinkingCache.this.mLinkingParsedListener) {
                            listener.onDataLoaded();
                        }
                        return;
                    }
                    return;
                case 2:
                    TextSpanLinkingCache.this.startRequestProcessing(false);
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsFling = false;
    private LinkParsedRunnable mLinkParsingRunnable;
    private Vector<DataLoadedListener> mLinkingParsedListener;
    private LinkedList<LinkingParseRequest> mLinkingRequests;
    private boolean mNeedRefresh = false;
    protected LruCache<Long, SpannableStringBuilder> mTextLinkingCache;

    public interface DataLoadedListener {
        void onDataLoaded();
    }

    public class LinkParsedRunnable implements Runnable {
        private volatile boolean mDone = false;

        public void stopProcessing() {
            this.mDone = true;
        }

        public void run() {
            while (!this.mDone) {
                LinkingParseRequest linkingParseRequest = null;
                synchronized (TextSpanLinkingCache.this.mLinkingRequests) {
                    if (!TextSpanLinkingCache.this.mLinkingRequests.isEmpty()) {
                        linkingParseRequest = (LinkingParseRequest) TextSpanLinkingCache.this.mLinkingRequests.removeFirst();
                    }
                }
                boolean needFresh = TextSpanLinkingCache.this.isNeedRefresh();
                if (linkingParseRequest != null) {
                    boolean doFresh = TextSpanLinkingCache.this.parseLinkingFromBodyText(linkingParseRequest);
                    TextSpanLinkingCache textSpanLinkingCache = TextSpanLinkingCache.this;
                    if (doFresh) {
                        needFresh = true;
                    }
                    textSpanLinkingCache.setNeedFresh(needFresh);
                } else {
                    if (needFresh) {
                        TextSpanLinkingCache.this.setNeedFresh(false);
                        synchronized (TextSpanLinkingCache.this.mHandler) {
                            TextSpanLinkingCache.this.mHandler.removeMessages(1);
                            TextSpanLinkingCache.this.mHandler.sendEmptyMessageDelayed(1, 100);
                        }
                    }
                    boolean isFling = TextSpanLinkingCache.this.isFling();
                    LinkedList -get2 = TextSpanLinkingCache.this.mLinkingRequests;
                    synchronized (-get2) {
                        if (isFling) {
                            try {
                                TextSpanLinkingCache.this.mLinkingRequests.wait();
                            } catch (InterruptedException e) {
                                MLog.e("Mms:TextSpanLinkingCache", "is fling and waitForWork exception::" + e);
                            }
                        }
                    }
                    -get2 = TextSpanLinkingCache.this.mLinkingRequests;
                    synchronized (-get2) {
                        if (!TextSpanLinkingCache.this.isNeedRefresh()) {
                            try {
                                TextSpanLinkingCache.this.mLinkingRequests.wait();
                            } catch (InterruptedException e2) {
                                MLog.e("Mms:TextSpanLinkingCache", "nothing to do and waitForWork exception::" + e2);
                            }
                        }
                    }
                }
            }
        }
    }

    private static final class LinkingParseRequest {
        public final MessageListItem msgListItem;

        public LinkingParseRequest(MessageListItem listItem) {
            this.msgListItem = listItem;
        }
    }

    public static synchronized TextSpanLinkingCache getInstance(Context context) {
        synchronized (TextSpanLinkingCache.class) {
            if (context != null) {
                TextSpanLinkingCache textSpanLinkingCache = (TextSpanLinkingCache) mSpanCaches.get(Integer.valueOf(context.hashCode()));
                if (textSpanLinkingCache != null) {
                    return textSpanLinkingCache;
                }
                textSpanLinkingCache = new TextSpanLinkingCache(context, 20);
                mSpanCaches.put(Integer.valueOf(context.hashCode()), textSpanLinkingCache);
                return textSpanLinkingCache;
            }
            return null;
        }
    }

    private TextSpanLinkingCache(Context context, int cacheSize) {
        this.mContext = context;
        this.mTextLinkingCache = new LruCache(cacheSize);
        this.mLinkingRequests = new LinkedList();
        this.mLinkingParsedListener = new Vector();
        this.mExecutorService = Executors.newFixedThreadPool(20);
    }

    public void addDataLoadedListener(DataLoadedListener listener) {
        this.mLinkingParsedListener.add(listener);
    }

    public void setFlingState(boolean isFling) {
        synchronized (this.mLinkingRequests) {
            this.mIsFling = isFling;
            if (!isFling) {
                setNeedFresh(true);
                notifyWork();
            }
        }
    }

    private boolean isFling() {
        boolean z;
        synchronized (this.mLinkingRequests) {
            z = this.mIsFling;
        }
        return z;
    }

    private void startRequestProcessing(boolean mustUpdateCache) {
        if (this.mLinkParsingRunnable != null) {
            if (mustUpdateCache) {
                setNeedFresh(true);
            }
            notifyWork();
            return;
        }
        try {
            this.mLinkParsingRunnable = new LinkParsedRunnable();
            this.mExecutorService.execute(this.mLinkParsingRunnable);
        } catch (RejectedExecutionException rejectedExecutionException) {
            MLog.e("Mms:TextSpanLinkingCache", rejectedExecutionException.getMessage());
        }
    }

    public void stopRequestProcessing() {
        this.mHandler.removeMessages(2);
        if (this.mLinkParsingRunnable != null) {
            this.mLinkParsingRunnable.stopProcessing();
            this.mLinkParsingRunnable = null;
        }
    }

    private boolean isNeedRefresh() {
        boolean z;
        synchronized (this.mLinkingRequests) {
            z = this.mNeedRefresh;
        }
        return z;
    }

    private void setNeedFresh(boolean fresh) {
        synchronized (this.mLinkingRequests) {
            this.mNeedRefresh = fresh;
        }
    }

    private void notifyWork() {
        synchronized (this.mLinkingRequests) {
            boolean notify = isNeedRefresh() || this.mLinkingRequests.size() > 0;
            if (notify) {
                this.mLinkingRequests.notifyAll();
            }
        }
    }

    public void clearLinkingCache(Context context, boolean destory) {
        synchronized (this.mLinkingRequests) {
            this.mLinkingRequests.clear();
        }
        synchronized (this.mTextLinkingCache) {
            this.mTextLinkingCache.evictAll();
        }
        if (destory) {
            MLog.i("Mms:TextSpanLinkingCache", "Mms:TextSpanLinkingCache clearLinkingCache");
            this.mLinkingParsedListener.clear();
            this.mContext = null;
            this.mExecutorService.shutdownNow();
            mSpanCaches.remove(Integer.valueOf(context.hashCode()));
        }
    }

    private void addRequest(MessageListItem msgListItem, boolean immediate, boolean mustUpdateCache) {
        synchronized (this.mLinkingRequests) {
            if (10 == this.mLinkingRequests.size()) {
                this.mLinkingRequests.removeLast();
            }
            if (immediate) {
                this.mLinkingRequests.addFirst(new LinkingParseRequest(msgListItem));
            } else {
                this.mLinkingRequests.addLast(new LinkingParseRequest(msgListItem));
            }
        }
        if (immediate) {
            startRequestProcessing(mustUpdateCache);
        }
    }

    private boolean parseLinkingFromBodyText(LinkingParseRequest request) {
        MessageItem item = request.msgListItem.getMessageItem();
        if (item.getCryptoMessageItem().isEncryptSms(item)) {
            MLog.d("TextSpanLinkingCache", "isEncryptSms, not cache.");
            return false;
        }
        SpannableStringBuilder strBuilder = MessageUtils.formatMessage(item.mBody, item.mSubId, item.mHighlight, item.mTextContentType, request.msgListItem.getTextScale());
        List<TextSpan> listSpan = CommonGatherLinks.getTextSpans(item.mAddrPosInBody, item.mDatePosInBody, item.mRiskUrlPosInBody, item.mBody, this.mContext, 0 == item.mDate ? System.currentTimeMillis() : item.mDate);
        if (item.mIsSecret == 1) {
            CommonGatherLinks.gatherSafetySms(listSpan);
        }
        long key = MessageListAdapter.getKey(item.mType, item.mMsgId);
        if (listSpan.size() < 1) {
            synchronized (this.mTextLinkingCache) {
                this.mTextLinkingCache.put(Long.valueOf(key), strBuilder);
            }
            return true;
        }
        for (TextSpan span : listSpan) {
            ClickableSpan spanClick = SpandTextView.createSpandClickable(this.mContext, span, strBuilder);
            if (spanClick != null) {
                strBuilder.setSpan(spanClick, span.getStart(), span.getEnd(), 33);
            }
        }
        SafetySmsParser.getInstance().appendSafetySmsSpan(strBuilder, listSpan, this.mContext);
        SafetySmsParser.getInstance().appendRiskSpan(strBuilder, listSpan);
        synchronized (this.mTextLinkingCache) {
            this.mTextLinkingCache.put(Long.valueOf(key), strBuilder);
        }
        return true;
    }

    public SpannableStringBuilder getCacheByKey(long key) {
        SpannableStringBuilder spannableStringBuilder;
        synchronized (this.mTextLinkingCache) {
            spannableStringBuilder = (SpannableStringBuilder) this.mTextLinkingCache.get(Long.valueOf(key));
        }
        return spannableStringBuilder;
    }

    public void updateTextSpanable(MessageListItem msgListItem) {
        updateTextSpanable(msgListItem, false);
    }

    public void updateTextSpanable(final MessageListItem msgListItem, final boolean mustUpdateCache) {
        MessageItem item = msgListItem.getMessageItem();
        if (!"mms".equals(item.mType)) {
            SpannableStringBuilder cachedMessage;
            long key = MessageListAdapter.getKey(item.mType, item.mMsgId);
            synchronized (this.mTextLinkingCache) {
                cachedMessage = (SpannableStringBuilder) this.mTextLinkingCache.get(Long.valueOf(key));
            }
            if (cachedMessage == null || mustUpdateCache) {
                long j;
                Handler handler = this.mHandler;
                Runnable anonymousClass2 = new Runnable() {
                    public void run() {
                        TextSpanLinkingCache.this.addRequest(msgListItem, true, mustUpdateCache);
                    }
                };
                if (mustUpdateCache) {
                    j = 0;
                } else {
                    j = 200;
                }
                handler.postDelayed(anonymousClass2, j);
            }
        }
    }
}
