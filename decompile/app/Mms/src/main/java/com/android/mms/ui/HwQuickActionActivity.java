package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.util.ContactUtil;
import com.android.mms.util.ContactUtil.NumberInfo;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.HwUiStyleUtils;
import com.huawei.mms.util.StatisticalHelper;
import java.util.ArrayList;
import java.util.List;

public class HwQuickActionActivity extends HwBaseActivity {
    private CheckBox mDefaultCheckBox;
    private String mDefaultNumber = "NO_DEFAULT_NUMTER";
    private ContactQueryHandler mDialQueryHandler;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 2015:
                    HwQuickActionActivity.this.finish();
                    return;
                default:
                    return;
            }
        }
    };
    private List<NumberInfo> mNumberInfoList = new ArrayList();

    private class ContactQueryHandler extends AsyncQueryHandler {
        public ContactQueryHandler(ContentResolver resolver) {
            super(resolver);
        }

        protected void onQueryComplete(int token, Object actions, Cursor cursor) {
            switch (token) {
                case 100:
                    HwQuickActionActivity.this.sendStarContact(cursor);
                    return;
                default:
                    return;
            }
        }
    }

    private class NumberSelectionAdapter extends BaseAdapter {

        private class ViewHolder {
            NumberInfo dataInfo;
            TextView label;
            TextView number;

            private ViewHolder() {
            }
        }

        private NumberSelectionAdapter() {
        }

        public int getCount() {
            return HwQuickActionActivity.this.mNumberInfoList.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= HwQuickActionActivity.this.mNumberInfoList.size()) {
                return null;
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(HwQuickActionActivity.this.getBaseContext()).inflate(R.layout.contact_phone_number_selection_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) convertView.findViewById(R.id.contacts_phone_number);
                holder.label = (TextView) convertView.findViewById(R.id.contacts_phone_label);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            NumberInfo numberInfo = (NumberInfo) HwQuickActionActivity.this.mNumberInfoList.get(position);
            holder.number.setText(numberInfo.number);
            holder.label.setText(numberInfo.label);
            holder.dataInfo = numberInfo;
            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    StatisticalHelper.incrementReportCount(HwQuickActionActivity.this.getApplicationContext(), 2199);
                    if (HwQuickActionActivity.this.mDefaultCheckBox.isChecked()) {
                        ContactUtil.setSuperPrimary(HwQuickActionActivity.this.getBaseContext(), holder.dataInfo.dataId);
                    }
                    HwQuickActionActivity.this.toComposeMessageActivity(holder.dataInfo.number);
                }
            });
            return convertView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            if (position >= HwQuickActionActivity.this.mNumberInfoList.size()) {
                return null;
            }
            return HwQuickActionActivity.this.mNumberInfoList.get(position);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MLog.i("HwQuickActionActivity", "onCreate");
        HwUiStyleUtils.removeWhiteTitle(getWindow());
        StatisticalHelper.incrementReportCount(getApplicationContext(), 2198);
        this.mDialQueryHandler = new ContactQueryHandler(getContentResolver());
        resolveIntent();
    }

    private void resolveIntent() {
        Intent intent = getIntent();
        if (intent == null) {
            MLog.i("HwQuickActionActivity", "intent is null");
            finishSelf();
            return;
        }
        long contactId = intent.getLongExtra("QUICKACTION_CONTACT_KEY", -1);
        if (-1 != contactId) {
            MLog.i("HwQuickActionActivity", "contactId: " + contactId);
            String[] projection = new String[]{"data_id", "data1", "is_super_primary", "data2", "data3"};
            String[] selectionArgs = new String[]{"vnd.android.cursor.item/phone_v2"};
            this.mDialQueryHandler.startQuery(100, null, Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId), "entities"), projection, "mimetype=?", selectionArgs, null);
        } else {
            MLog.i("HwQuickActionActivity", "intent didn't contain contactId");
            finishSelf();
        }
    }

    boolean hasDefaultNumber() {
        for (NumberInfo numberInfo : this.mNumberInfoList) {
            if (numberInfo.isDefaultNumber) {
                this.mDefaultNumber = numberInfo.number;
                return true;
            }
        }
        return false;
    }

    private void showMulitNumberSelectDialog() {
        View custView = LayoutInflater.from(this).inflate(R.layout.favorites_select_numbers_dialog_view, null, false);
        this.mDefaultCheckBox = (CheckBox) custView.findViewById(R.id.set_to_default_check);
        ((ListView) custView.findViewById(R.id.contact_number_select_list)).setAdapter(new NumberSelectionAdapter());
        Builder build = new Builder(this);
        build.setTitle(getResources().getString(R.string.clickspan_send_message)).setView(custView, 0, 0, 0, 0).create();
        build.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                HwQuickActionActivity.this.finishSelf();
            }
        }).show();
    }

    private void toComposeMessageActivity(String number) {
        sendSms(number);
    }

    private void sendSms(String number) {
        MLog.i("HwQuickActionActivity", "send Sms");
        Intent smsIntent = new Intent("android.intent.action.SENDTO", Uri.fromParts("smsto", number, null));
        smsIntent.putExtra("QUICKACTION_QUICK_NEW_MESSAGE_KEY", "QUICKACTION_QUICK_NEW_MESSAGE_VALUE");
        smsIntent.addFlags(268435456);
        smsIntent.addFlags(32768);
        try {
            getApplicationContext().startActivity(smsIntent);
        } catch (ActivityNotFoundException e) {
            MLog.i("HwQuickActionActivity", e.getMessage());
        } finally {
            finishSelf();
        }
    }

    public void sendStarContact(Cursor cursor) {
        this.mNumberInfoList = ContactUtil.getContactNumberInfosByCursor(this, cursor);
        if (this.mNumberInfoList.size() == 0) {
            MLog.i("HwQuickActionActivity", "contact has no number");
            Toast.makeText(this, getResources().getString(R.string.mms_quick_action_no_number_toast), 1).show();
            finishSelf();
        } else if (1 == this.mNumberInfoList.size()) {
            MLog.i("HwQuickActionActivity", "contact has one number");
            toComposeMessageActivity(((NumberInfo) this.mNumberInfoList.get(0)).number);
        } else if (hasDefaultNumber()) {
            MLog.i("HwQuickActionActivity", "has default Number");
            toComposeMessageActivity(this.mDefaultNumber);
        } else {
            MLog.i("HwQuickActionActivity", "has no default Number");
            showMulitNumberSelectDialog();
        }
    }

    private void finishSelf() {
        finish();
    }
}
