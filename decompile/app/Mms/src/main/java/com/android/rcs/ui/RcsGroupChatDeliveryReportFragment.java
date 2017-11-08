package com.android.rcs.ui;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.widget.CursorAdapter;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation.ConversationQueryHandler;
import com.android.mms.ui.DeliveryReportListItem;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.AbstractEmuiActionBar;
import com.huawei.mms.ui.HwListFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.NumberUtils;

public class RcsGroupChatDeliveryReportFragment extends HwListFragment {
    public static final Uri DELIVERY_REPORT_URI = Uri.parse("content://rcsim/rcs_group_message_delivery_report");
    private static final String[] REPORT_REQUEST_PROJECTION = new String[]{"_id", "addr"};
    protected AbstractEmuiActionBar mActionBarWhenSplit;
    private DeliveryAdapter mAdapter = null;
    private BackgroundQueryHandler mBackgroundQueryHandler = null;
    private long mDate = 0;
    private String mMessageId = null;

    private final class BackgroundQueryHandler extends ConversationQueryHandler {
        public BackgroundQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case 9527:
                    if (cursor != null) {
                        RcsGroupChatDeliveryReportFragment.this.mAdapter.changeCursor(cursor);
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private class DeliveryAdapter extends CursorAdapter {
        private Context mContext = null;
        private LayoutInflater mInflater = null;

        public DeliveryAdapter(Context context, Cursor c, boolean autoRequery) {
            super(context, c, autoRequery);
            this.mContext = context;
            this.mInflater = LayoutInflater.from(context);
        }

        public void bindView(View view, Context context, Cursor cursor) {
            DeliveryReportListItem listItem = (DeliveryReportListItem) view;
            String name = "";
            String address = cursor.getString(1);
            if (!TextUtils.isEmpty(address)) {
                Contact contact = Contact.get(NumberUtils.normalizeNumber(address), true);
                if (contact == null || !contact.existsInDatabase()) {
                    name = address;
                } else {
                    name = contact.getName();
                }
            }
            name = RcsGroupChatDeliveryReportFragment.this.getString(R.string.recipient_label) + '‪' + name + '‬';
            String status = RcsGroupChatDeliveryReportFragment.this.getString(R.string.status_label) + RcsGroupChatDeliveryReportFragment.this.getString(R.string.status_received);
            String deliveryDate = "";
            if (RcsGroupChatDeliveryReportFragment.this.mDate > 0) {
                deliveryDate = RcsGroupChatDeliveryReportFragment.this.getString(R.string.delivered_label) + MessageUtils.formatTimeStampString(this.mContext, RcsGroupChatDeliveryReportFragment.this.mDate, true, true);
            }
            listItem.bind(name, status, deliveryDate);
            if (TextUtils.isEmpty(deliveryDate)) {
                listItem.setMinimumHeight((int) this.mContext.getResources().getDimension(R.dimen.delivery_report_item_height));
            } else {
                listItem.setMinimumHeight((int) this.mContext.getResources().getDimension(R.dimen.delivery_report_item_with_date_height));
            }
        }

        public View newView(Context arg0, Cursor arg1, ViewGroup arg2) {
            return this.mInflater.inflate(R.layout.delivery_report_list_item, null);
        }

        protected void onContentChanged() {
            RcsGroupChatDeliveryReportFragment.this.startMsgListQuery();
        }
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        Log.v("RcsGroupChatDeliveryReportFragment", "onCreate");
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.delivery_report_activity, container, false);
        this.mActionBarWhenSplit = createEmuiActionBar(root);
        return root;
    }

    protected AbstractEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.rcs_delivery_report_top), null);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.v("RcsGroupChatDeliveryReportFragment", "onActivityCreated");
        Intent intent = getIntent();
        this.mMessageId = intent.getStringExtra("bundle_message_id");
        this.mDate = intent.getLongExtra("bundle_sent_time", 0);
        this.mBackgroundQueryHandler = new BackgroundQueryHandler(getActivity().getContentResolver());
        this.mAdapter = new DeliveryAdapter(getContext(), null, false);
        getListView().setAdapter(this.mAdapter);
        this.mActionBarWhenSplit.setTitle(getResources().getString(R.string.delivery_report_activity));
        this.mActionBarWhenSplit.setStartIcon(true, (int) R.drawable.ic_public_back, new OnClickListener() {
            public void onClick(View v) {
                RcsGroupChatDeliveryReportFragment.this.getActivity().onBackPressed();
            }
        });
    }

    public void onDestroy() {
        super.onDestroy();
        Log.v("RcsGroupChatDeliveryReportFragment", "onDestroy");
    }

    public void onResume() {
        super.onResume();
        startMsgListQuery();
    }

    public void onPause() {
        super.onPause();
        this.mBackgroundQueryHandler.cancelOperation(9527);
        this.mAdapter.changeCursor(null);
    }

    private void startMsgListQuery() {
        this.mBackgroundQueryHandler.cancelOperation(9527);
        try {
            this.mBackgroundQueryHandler.startQuery(9527, null, DELIVERY_REPORT_URI, REPORT_REQUEST_PROJECTION, "global_id = ?", new String[]{this.mMessageId}, null);
        } catch (SQLiteException e) {
            SqliteWrapper.checkSQLiteException(getContext(), e);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBarWhenSplit.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
    }
}
