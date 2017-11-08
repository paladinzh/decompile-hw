package com.android.contacts;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.ActivityNotFoundException;
import android.content.AsyncQueryHandler;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import java.io.Serializable;
import java.util.ArrayList;

public class QuickPressPickNumberActivity extends Activity {
    private static final String[] CALL_CONTACT_COLUMNS = new String[]{"data_id", "display_name", "data1", "data2", "data3", "is_super_primary", "_id"};
    private int mContactQueryToken;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == -100) {
                QuickPressPickNumberActivity.this.finish();
            }
        }
    };
    private CheckBox mSetDefaultCheckBox;

    private class DialQueryHandler extends AsyncQueryHandler {
        public DialQueryHandler(ContentResolver resolver) {
            super(resolver);
        }

        protected void onQueryComplete(int token, Object actions, Cursor cursor) {
            QuickPressPickNumberActivity.this.callStarContact(cursor);
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static class NumberInfo implements Serializable {
        private static final long serialVersionUID = 1;
        public long dataId;
        public String label;
        public String number;
    }

    private class NumberSelectionAdapter extends BaseAdapter {
        private ArrayList<NumberInfo> numberInfoList;

        private class ViewHolder {
            NumberInfo dataInfo;
            TextView label;
            TextView number;

            private ViewHolder() {
            }
        }

        public NumberSelectionAdapter(ArrayList<NumberInfo> numberList) {
            this.numberInfoList = numberList;
        }

        public int getCount() {
            return this.numberInfoList.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= this.numberInfoList.size()) {
                return null;
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(QuickPressPickNumberActivity.this.getBaseContext()).inflate(R.layout.contact_phone_number_selection_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) convertView.findViewById(R.id.contacts_phone_number);
                holder.label = (TextView) convertView.findViewById(R.id.contacts_phone_label);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            NumberInfo numberInfo = (NumberInfo) this.numberInfoList.get(position);
            holder.number.setText(numberInfo.number);
            holder.label.setText(numberInfo.label);
            holder.dataInfo = numberInfo;
            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    QuickPressPickNumberActivity.this.callNumber(holder.dataInfo.number);
                    if (QuickPressPickNumberActivity.this.mSetDefaultCheckBox.isChecked()) {
                        QuickPressPickNumberActivity.this.startService(ContactSaveService.createSetSuperPrimaryIntent(QuickPressPickNumberActivity.this.getApplicationContext(), holder.dataInfo.dataId, false));
                    }
                }
            });
            return convertView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            if (position >= this.numberInfoList.size()) {
                return null;
            }
            return this.numberInfoList.get(position);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            RefelctionUtils.invokeMethod("setHwFloating", getWindow(), new Object[]{Boolean.valueOf(true)});
        } catch (UnsupportedException e) {
            HwLog.e("QuickPressPickNumberActivity", "UnsupportedException");
        }
        Intent intent = getIntent();
        if (intent == null) {
            finish();
            return;
        }
        if (intent.getBooleanExtra("callStarNumber", false)) {
            queryAndCallStarNumers(intent.getData());
        } else if (intent.getBooleanExtra("callcamcard", false)) {
            StatisticalHelper.report(1176);
            startCamCard();
        } else {
            finish();
        }
    }

    private void queryAndCallStarNumers(Uri lookupUri) {
        String[] selectionArgs = new String[]{"vnd.android.cursor.item/phone_v2"};
        DialQueryHandler queryHandler = new DialQueryHandler(getContentResolver());
        int i = this.mContactQueryToken;
        this.mContactQueryToken = i + 1;
        queryHandler.startQuery(i, null, Uri.withAppendedPath(lookupUri, "entities"), CALL_CONTACT_COLUMNS, "mimetype=?", selectionArgs, null);
    }

    private void callStarContact(Cursor cursor) {
        if (cursor == null || cursor.getCount() == 0) {
            Toast.makeText(this, getResources().getString(R.string.contact_has_no_available_numbers), 0).show();
            delayFinish();
            return;
        }
        ArrayList<NumberInfo> numberInfoList = getSelectedContactInfo(cursor);
        if (numberInfoList != null && numberInfoList.size() > 1) {
            String defaultNumber = getDefaultNumber(cursor);
            if (defaultNumber != null) {
                callNumber(defaultNumber);
            } else {
                showMulitNumberSelectDialog(numberInfoList);
            }
        } else if (numberInfoList != null && numberInfoList.size() == 1) {
            callNumber(((NumberInfo) numberInfoList.get(0)).number);
        }
    }

    private String getDefaultNumber(Cursor numberCursor) {
        if (numberCursor == null) {
            return null;
        }
        String defaultNumber = null;
        numberCursor.moveToFirst();
        while (numberCursor.getInt(numberCursor.getColumnIndex("is_super_primary")) == 0) {
            if (!numberCursor.moveToNext()) {
                break;
            }
        }
        defaultNumber = numberCursor.getString(numberCursor.getColumnIndex("data1"));
        return defaultNumber;
    }

    private ArrayList<NumberInfo> getSelectedContactInfo(Cursor cursor) {
        if (cursor == null || cursor.getCount() < 1) {
            return null;
        }
        ArrayList<NumberInfo> numberInfoList = new ArrayList();
        if (cursor.moveToFirst()) {
            ArrayList<String> numberList = new ArrayList();
            do {
                String number = PhoneNumberFormatter.formatNumber(this, cursor.getString(cursor.getColumnIndex("data1")));
                if (!(numberList.contains(number) || TextUtils.isEmpty(number))) {
                    numberList.add(number);
                    String label = (String) Phone.getTypeLabel(getResources(), cursor.getInt(cursor.getColumnIndex("data2")), cursor.getString(cursor.getColumnIndex("data3")));
                    long dataId = cursor.getLong(cursor.getColumnIndex("data_id"));
                    NumberInfo numberInfo = new NumberInfo();
                    numberInfo.number = number;
                    numberInfo.label = label;
                    numberInfo.dataId = dataId;
                    numberInfoList.add(numberInfo);
                }
            } while (cursor.moveToNext());
        }
        return numberInfoList;
    }

    private void showMulitNumberSelectDialog(ArrayList<NumberInfo> numberInfoList) {
        View custView = LayoutInflater.from(this).inflate(R.layout.favorites_select_number_dialog_view, null, false);
        this.mSetDefaultCheckBox = (CheckBox) custView.findViewById(R.id.set_to_default_check);
        ListView list = (ListView) custView.findViewById(R.id.contact_number_select_list);
        list.setFastScrollEnabled(true);
        list.setAdapter(new NumberSelectionAdapter(numberInfoList));
        Builder build = new Builder(this);
        build.setTitle(getResources().getString(R.string.call_other)).setView(custView, 0, 0, 0, 0).create();
        build.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface arg0) {
                QuickPressPickNumberActivity.this.delayFinish();
            }
        }).show();
    }

    private void callNumber(String number) {
        if (!TextUtils.isEmpty(number)) {
            try {
                startActivity(new Intent("com.android.contacts.action.CHOOSE_SUB", Uri.fromParts("tel", number, null)));
                overridePendingTransition(0, 0);
            } catch (ActivityNotFoundException e) {
                HwLog.w("QuickPressPickNumberActivity", " ActivityNotFoundException ");
            }
            delayFinish();
        }
    }

    private void startCamCard() {
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        if (((QuickPressCamcardFragment) fragmentManager.findFragmentByTag("quick_fragment")) == null) {
            transaction.add(new QuickPressCamcardFragment(), "quick_fragment");
            transaction.commit();
        }
    }

    private void delayFinish() {
        if (this.mHandler != null) {
            this.mHandler.sendEmptyMessageDelayed(-100, 100);
        }
    }
}
