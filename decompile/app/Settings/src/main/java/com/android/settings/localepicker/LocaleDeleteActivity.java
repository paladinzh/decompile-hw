package com.android.settings.localepicker;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.utils.ActionBarCustomTitle;
import com.huawei.android.app.ActionBarEx;
import java.util.List;

public class LocaleDeleteActivity extends Activity {
    private static OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
        }
    };
    private static OnDismissListener mOnDismissListener = new OnDismissListener() {
        public void onDismiss(DialogInterface dialog) {
        }
    };
    private LocaleDeleteAdapter mAdapter;
    private View.OnClickListener mCancelListener = new View.OnClickListener() {
        public void onClick(View v) {
            LocaleDeleteActivity.this.finish();
        }
    };
    private MenuItem mDeleteItem;
    private List<LocaleInfo> mFeedsList;
    private LayoutInflater mInflater;
    private ListView mListView;
    private LocaleListHelper mLocaleListHelper;
    private AlertDialog mRemoveLocalesTipDialog;
    private ActionBarCustomTitle mTitle;

    public class LocaleDeleteAdapter extends BaseAdapter {
        public LocaleDeleteAdapter() {
            LocaleDeleteActivity.this.mInflater = (LayoutInflater) LocaleDeleteActivity.this.getSystemService("layout_inflater");
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder mHolder;
            if (convertView == null) {
                convertView = LocaleDeleteActivity.this.mInflater.inflate(2130968849, null);
                mHolder = new ViewHolder();
                mHolder.mTextView = (TextView) convertView.findViewById(2131886728);
                mHolder.mCheckBox = (CheckBox) convertView.findViewById(2131886163);
                convertView.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) convertView.getTag();
            }
            mHolder.mTextView.setText(((LocaleInfo) LocaleDeleteActivity.this.mFeedsList.get(position)).getFullNameNative());
            mHolder.mCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    ((LocaleInfo) LocaleDeleteActivity.this.mFeedsList.get(position)).setChecked(isChecked);
                    LocaleDeleteActivity.this.mTitle.setCustomTitle(LocaleDeleteActivity.this.getMultiSelectionTitle(LocaleDeleteActivity.this.mLocaleListHelper.getCheckedCount()), LocaleDeleteActivity.this.mLocaleListHelper.getCheckedCount());
                    LocaleDeleteActivity.this.invalidateOptionsMenu();
                }
            });
            mHolder.mCheckBox.setChecked(((LocaleInfo) LocaleDeleteActivity.this.mFeedsList.get(position)).getChecked());
            return convertView;
        }

        public int getCount() {
            if (LocaleDeleteActivity.this.mFeedsList == null) {
                return 0;
            }
            return LocaleDeleteActivity.this.mFeedsList.size();
        }

        public Object getItem(int position) {
            if (LocaleDeleteActivity.this.mFeedsList == null) {
                return null;
            }
            return LocaleDeleteActivity.this.mFeedsList.get(position);
        }

        public long getItemId(int position) {
            return (long) position;
        }
    }

    public static class ViewHolder {
        public CheckBox mCheckBox = null;
        public TextView mTextView = null;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(2130968850);
        this.mListView = (ListView) findViewById(2131886758);
        this.mLocaleListHelper = new LocaleListHelper();
        this.mFeedsList = this.mLocaleListHelper.getFeedsList();
        for (LocaleInfo localeInfo : this.mFeedsList) {
            localeInfo.setChecked(false);
        }
        this.mAdapter = new LocaleDeleteAdapter();
        this.mListView.setAdapter(this.mAdapter);
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                boolean z = true;
                CheckBox box = (CheckBox) ((LinearLayout) view).getChildAt(1);
                if (box.isChecked()) {
                    z = false;
                }
                box.setChecked(z);
                LocaleDeleteActivity.this.mTitle.setCustomTitle(LocaleDeleteActivity.this.getMultiSelectionTitle(LocaleDeleteActivity.this.mLocaleListHelper.getCheckedCount()), LocaleDeleteActivity.this.mLocaleListHelper.getCheckedCount());
                LocaleDeleteActivity.this.invalidateOptionsMenu();
            }
        });
        this.mTitle = new ActionBarCustomTitle(this);
        ActionBar mActionBar = getActionBar();
        mActionBar.setTitle(2131628605);
        ActionBarEx.setStartIcon(mActionBar, true, null, this.mCancelListener);
        ActionBarEx.setCustomTitle(mActionBar, this.mTitle.getTitleLayout());
        this.mTitle.setCustomTitle(getMultiSelectionTitle(this.mLocaleListHelper.getCheckedCount()), this.mLocaleListHelper.getCheckedCount());
    }

    public String getMultiSelectionTitle(int count) {
        return count > 0 ? getString(2131628604) : getString(2131628605);
    }

    protected void onResume() {
        super.onResume();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(2132017154, menu);
        this.mDeleteItem = menu.findItem(2131887382);
        MenuItem mSelectAllItem = menu.findItem(2131887643);
        if (this.mLocaleListHelper.isSelectAll()) {
            mSelectAllItem.setIcon(getResources().getDrawable(2130837663));
            mSelectAllItem.setTitle(2131628606);
        } else {
            mSelectAllItem.setIcon(getResources().getDrawable(2130837664));
            mSelectAllItem.setTitle(2131626275);
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case 2131887382:
                showRemoveLocaleWarningDialog();
                return true;
            case 2131887643:
                if (this.mLocaleListHelper.isSelectAll()) {
                    this.mLocaleListHelper.setAllUnCheck();
                } else {
                    this.mLocaleListHelper.setAllCheck();
                }
                this.mTitle.setCustomTitle(getMultiSelectionTitle(this.mLocaleListHelper.getCheckedCount()), this.mLocaleListHelper.getCheckedCount());
                invalidateOptionsMenu();
                this.mAdapter.notifyDataSetChanged();
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        if (this.mLocaleListHelper.isSelect()) {
            this.mDeleteItem.setEnabled(true);
        } else {
            this.mDeleteItem.setEnabled(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void showRemoveLocaleWarningDialog() {
        int checkedCount = this.mLocaleListHelper.getCheckedCount();
        if (checkedCount != 0) {
            if (checkedCount == this.mAdapter.getCount()) {
                this.mRemoveLocalesTipDialog = new Builder(this).setMessage(2131628643).setPositiveButton(17039379, mOnClickListener).setOnDismissListener(mOnDismissListener).create();
                this.mRemoveLocalesTipDialog.show();
            } else {
                this.mRemoveLocalesTipDialog = new Builder(this).setTitle(getResources().getString(2131628641)).setMessage(2131628642).setNegativeButton(17039369, mOnClickListener).setPositiveButton(17039379, new OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        LocaleDeleteActivity.this.mLocaleListHelper.removeChecked(LocaleDeleteActivity.this);
                        LocaleDeleteActivity.this.finish();
                    }
                }).setOnDismissListener(mOnDismissListener).create();
                this.mRemoveLocalesTipDialog.show();
            }
        }
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mRemoveLocalesTipDialog != null && this.mRemoveLocalesTipDialog.isShowing()) {
            this.mRemoveLocalesTipDialog.dismiss();
        }
    }
}
