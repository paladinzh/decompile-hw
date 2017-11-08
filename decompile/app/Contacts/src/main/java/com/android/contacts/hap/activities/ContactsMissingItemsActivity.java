package com.android.contacts.hap.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.AsyncTaskExecutors;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.List;

public class ContactsMissingItemsActivity extends Activity implements OnItemClickListener {
    private ActionBar mActionBar;
    private ContactsMissingItemsAdapter mAdapter;
    private Context mContext;
    private ListView mListView;

    private class ContactsMissingItemsAdapter extends BaseAdapter {
        private final LayoutInflater mLayoutInflater;
        private int[] mListAdapterCount;

        public ContactsMissingItemsAdapter(Context context, int[] listCount) {
            this.mLayoutInflater = LayoutInflater.from(context);
            this.mListAdapterCount = listCount;
        }

        public int getCount() {
            return 3;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = this.mLayoutInflater.inflate(R.layout.contacts_missing_items, parent, false);
            }
            TextView list_item = (TextView) convertView.findViewById(R.id.list_item);
            ((TextView) convertView.findViewById(R.id.list_item_hint)).setText(String.format(ContactsMissingItemsActivity.this.mContext.getResources().getQuantityText(R.plurals.listTotalContactsCountUsed, this.mListAdapterCount[position]).toString(), new Object[]{Integer.valueOf(this.mListAdapterCount[position])}));
            switch (position) {
                case 0:
                    list_item.setText(R.string.contacts_missing_name);
                    StatisticalHelper.sendReport(1165, this.mListAdapterCount[0]);
                    break;
                case 1:
                    list_item.setText(R.string.contacts_missing_number);
                    StatisticalHelper.sendReport(1166, this.mListAdapterCount[1]);
                    break;
                case 2:
                    list_item.setText(R.string.contacts_missing_number_and_mail);
                    StatisticalHelper.sendReport(1167, this.mListAdapterCount[2]);
                    break;
            }
            return convertView;
        }
    }

    private class LoadContactsCount extends AsyncTask<Void, Void, Void> {
        private int[] mListCount;

        private LoadContactsCount() {
            this.mListCount = new int[3];
        }

        protected Void doInBackground(Void... params) {
            for (int i = 0; i < 3; i++) {
                this.mListCount[i] = ContactsMissingItemsActivity.this.getTotalContactCount(ContactsMissingItemsActivity.this.mContext, i);
            }
            return null;
        }

        protected void onPostExecute(Void unused) {
            ContactsMissingItemsActivity.this.createAdapter(this.mListCount);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        this.mContext = getApplicationContext();
        this.mActionBar = getActionBar();
        this.mActionBar.setDisplayHomeAsUpEnabled(true);
        this.mListView = new ListView(this);
        setContentView(this.mListView);
        this.mListView.setOnItemClickListener(this);
    }

    public void onStart() {
        super.onStart();
        new LoadContactsCount().executeOnExecutor(AsyncTaskExecutors.THREAD_POOL_EXECUTOR, new Void[0]);
    }

    public void onResume() {
        super.onResume();
    }

    public void onStop() {
        super.onStop();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    private void createAdapter(int[] mListCount) {
        this.mAdapter = new ContactsMissingItemsAdapter(this, mListCount);
        this.mListView.setAdapter(this.mAdapter);
    }

    private int getTotalContactCount(Context context, int index) {
        String[] strArr = null;
        int totalCount = 0;
        StringBuffer selection = new StringBuffer();
        List<String> selectionArgs = new ArrayList();
        switch (index) {
            case 0:
                selection.append("has_name").append("=0");
                break;
            case 1:
                selection.append("has_phone_number").append("=0");
                break;
            case 2:
                selection.append("has_phone_number").append("=0");
                selection.append(" AND ");
                selection.append("has_email").append("=0");
                break;
        }
        if (!CommonUtilMethods.isPrivacyModeEnabled(context)) {
            if (!TextUtils.isEmpty(selection.toString())) {
                selection.append(" AND ");
            }
            selection.append("is_private = 0");
        }
        Cursor cursor = null;
        try {
            ContentResolver contentResolver = context.getContentResolver();
            Uri uri = Contacts.CONTENT_URI;
            String[] strArr2 = new String[]{"_id"};
            String stringBuffer = selection.toString();
            if (selectionArgs.size() > 0) {
                strArr = (String[]) selectionArgs.toArray(new String[selectionArgs.size()]);
            }
            cursor = contentResolver.query(uri, strArr2, stringBuffer, strArr, null);
            if (cursor != null) {
                totalCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
            return totalCount;
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
        Intent lIntent = new Intent();
        lIntent.putExtra("missingItemIndex", position);
        lIntent.setClass(this, ContactsMissingItemsDetailActivity.class);
        startActivity(lIntent);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
