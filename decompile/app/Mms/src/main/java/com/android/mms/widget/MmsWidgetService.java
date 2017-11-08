package com.android.mms.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.database.Cursor;
import android.os.Handler;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import android.widget.RemoteViewsService.RemoteViewsFactory;
import com.android.mms.data.Contact;
import com.android.mms.data.Contact.UpdateListener;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;

public class MmsWidgetService extends RemoteViewsService {
    private static int mLastUnreadMsgCount = 0;
    private static final Object sWidgetLock = new Object();

    private static class MmsFactory implements RemoteViewsFactory, UpdateListener {
        private static int SENDERS_TEXT_COLOR_READ;
        private static int SENDERS_TEXT_COLOR_UNREAD;
        private static int SUBJECT_TEXT_COLOR_READ;
        private static int SUBJECT_TEXT_COLOR_UNREAD;
        private final int mAppWidgetId;
        private final AppWidgetManager mAppWidgetManager;
        private final Context mContext;
        private Cursor mConversationCursor;
        Handler mHandler;
        private boolean mShouldShowViewMore;
        private int mUnreadConvCount;
        private Runnable updater = new Runnable() {
            public void run() {
                MmsFactory.this.mAppWidgetManager.notifyAppWidgetViewDataChanged(MmsFactory.this.mAppWidgetId, R.id.conversation_list);
            }
        };

        public MmsFactory(Context context, Intent intent) {
            this.mContext = context;
            this.mAppWidgetId = intent.getIntExtra("appWidgetId", 0);
            this.mAppWidgetManager = AppWidgetManager.getInstance(context);
        }

        public void onCreate() {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "onCreate");
            }
            this.mHandler = new Handler();
            Contact.addListener(this);
        }

        public void onDestroy() {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "onDestroy");
            }
            synchronized (MmsWidgetService.sWidgetLock) {
                if (!(this.mConversationCursor == null || this.mConversationCursor.isClosed())) {
                    this.mConversationCursor.close();
                    this.mConversationCursor = null;
                }
                Contact.removeListener(this);
            }
        }

        public void onDataSetChanged() {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "onDataSetChanged");
            }
            synchronized (MmsWidgetService.sWidgetLock) {
                if (this.mConversationCursor != null) {
                    this.mConversationCursor.close();
                    this.mConversationCursor = null;
                }
                this.mConversationCursor = queryAllConversations();
                this.mUnreadConvCount = queryUnreadCount();
                MmsWidgetService.setMsgUnreadCount(this.mUnreadConvCount);
                onLoadComplete();
            }
        }

        private Cursor queryAllConversations() {
            return SqliteWrapper.query(this.mContext, Conversation.sAllThreadsUri, Conversation.getThreadProjection(), null, null, "date DESC");
        }

        private int queryUnreadCount() {
            int unreadCount = 0;
            if (this.mConversationCursor == null || !this.mConversationCursor.moveToFirst()) {
                return unreadCount;
            }
            do {
                unreadCount += this.mConversationCursor.getInt(11);
            } while (this.mConversationCursor.moveToNext());
            return unreadCount;
        }

        public int getCount() {
            int i = 1;
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "getCount");
            }
            synchronized (MmsWidgetService.sWidgetLock) {
                if (this.mConversationCursor == null) {
                    return 0;
                }
                boolean z;
                int count = getConversationCount();
                if (count < this.mConversationCursor.getCount()) {
                    z = true;
                } else {
                    z = false;
                }
                this.mShouldShowViewMore = z;
                if (!this.mShouldShowViewMore) {
                    i = 0;
                }
                i += count;
                return i;
            }
        }

        private int getConversationCount() {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "getConversationCount");
            }
            return Math.min(this.mConversationCursor.getCount(), 25);
        }

        private SpannableStringBuilder addColor(CharSequence text, int color) {
            SpannableStringBuilder builder = new SpannableStringBuilder(text);
            if (color != 0) {
                builder.setSpan(new ForegroundColorSpan(color), 0, text.length(), 33);
            }
            return builder;
        }

        public RemoteViews getViewAt(int position) {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "getViewAt position: " + position);
            }
            synchronized (MmsWidgetService.sWidgetLock) {
                RemoteViews viewMoreConversationsView;
                if (this.mConversationCursor == null || (this.mShouldShowViewMore && position >= getConversationCount())) {
                    viewMoreConversationsView = getViewMoreConversationsView();
                    return viewMoreConversationsView;
                } else if (this.mConversationCursor.moveToPosition(position)) {
                    int i;
                    int color;
                    Conversation conv = Conversation.from(this.mContext, this.mConversationCursor);
                    RemoteViews remoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.widget_conversation);
                    if (conv.hasUnreadMessages()) {
                        remoteViews.setViewVisibility(R.id.widget_unread_background, 0);
                        remoteViews.setViewVisibility(R.id.widget_read_background, 8);
                    } else {
                        remoteViews.setViewVisibility(R.id.widget_unread_background, 8);
                        remoteViews.setViewVisibility(R.id.widget_read_background, 0);
                    }
                    if (conv.hasAttachment()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    remoteViews.setViewVisibility(R.id.attachment, i);
                    if (conv.hasError()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    remoteViews.setViewVisibility(R.id.error, i);
                    Resources res = this.mContext.getResources();
                    SENDERS_TEXT_COLOR_READ = res.getColor(R.color.text_color_pre);
                    SENDERS_TEXT_COLOR_UNREAD = res.getColor(R.color.unread_message_bg);
                    SUBJECT_TEXT_COLOR_READ = res.getColor(R.color.text_color_pre);
                    SUBJECT_TEXT_COLOR_UNREAD = res.getColor(R.color.unread_message_bg);
                    CharSequence formatTimeStampString = MessageUtils.formatTimeStampString(this.mContext, conv.getDate());
                    if (conv.hasUnreadMessages()) {
                        i = SUBJECT_TEXT_COLOR_UNREAD;
                    } else {
                        i = SUBJECT_TEXT_COLOR_READ;
                    }
                    remoteViews.setTextViewText(R.id.date, addColor(formatTimeStampString, i));
                    if (conv.hasUnreadMessages()) {
                        color = SENDERS_TEXT_COLOR_UNREAD;
                    } else {
                        color = SENDERS_TEXT_COLOR_READ;
                    }
                    remoteViews.setTextColor(R.id.from, color);
                    remoteViews.setTextViewText(R.id.from, conv.getRecipients().formatNames(", "));
                    if (conv.hasDraft()) {
                        i = 0;
                    } else {
                        i = 8;
                    }
                    remoteViews.setViewVisibility(R.id.draft, i);
                    remoteViews.setTextViewText(R.id.subject, addColor(SmileyParser.getInstance().addSmileySpans(Conversation.getSnippetFromCursor(this.mContext, this.mConversationCursor), SMILEY_TYPE.MESSAGE_EDITTEXT), conv.hasUnreadMessages() ? SUBJECT_TEXT_COLOR_UNREAD : SUBJECT_TEXT_COLOR_READ));
                    Intent clickIntent = new Intent("android.intent.action.VIEW");
                    clickIntent.putExtra("thread_id", conv.getThreadId());
                    remoteViews.setOnClickFillInIntent(R.id.widget_conversation, clickIntent);
                    return remoteViews;
                } else {
                    MLog.w("MmsWidgetService", "Failed to move to position: " + position);
                    viewMoreConversationsView = getViewMoreConversationsView();
                    return viewMoreConversationsView;
                }
            }
        }

        private RemoteViews getViewMoreConversationsView() {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "getViewMoreConversationsView");
            }
            RemoteViews view = new RemoteViews(this.mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(R.id.loading_text, this.mContext.getText(R.string.view_more_conversations));
            view.setOnClickFillInIntent(R.id.widget_loading, new Intent("android.intent.action.VIEW"));
            return view;
        }

        public RemoteViews getLoadingView() {
            RemoteViews view = new RemoteViews(this.mContext.getPackageName(), R.layout.widget_loading);
            view.setTextViewText(R.id.loading_text, this.mContext.getText(R.string.loading_conversations));
            return view;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public boolean hasStableIds() {
            return true;
        }

        private void onLoadComplete() {
            int i = 0;
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "onLoadComplete");
            }
            RemoteViews remoteViews = new RemoteViews(this.mContext.getPackageName(), R.layout.widget);
            if (this.mUnreadConvCount <= 0) {
                i = 8;
            }
            remoteViews.setViewVisibility(R.id.widget_unread_count, i);
            if (this.mUnreadConvCount > 0) {
                remoteViews.setTextViewText(R.id.widget_unread_count, Integer.toString(this.mUnreadConvCount));
            }
            this.mAppWidgetManager.partiallyUpdateAppWidget(this.mAppWidgetId, remoteViews);
        }

        public void onUpdate(Contact updated) {
            if (MLog.isLoggable("Mms_widget", 2)) {
                MLog.v("MmsWidgetService", "onUpdate from Contact: " + updated);
            }
            if (this.mHandler != null) {
                this.mHandler.removeCallbacks(this.updater);
                this.mHandler.postDelayed(this.updater, 300);
                return;
            }
            this.updater.run();
        }
    }

    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new MmsFactory(getApplicationContext(), intent);
    }

    public static int getMsgUnreadCount() {
        return mLastUnreadMsgCount;
    }

    private static void setMsgUnreadCount(int msgCount) {
        mLastUnreadMsgCount = msgCount;
    }
}
