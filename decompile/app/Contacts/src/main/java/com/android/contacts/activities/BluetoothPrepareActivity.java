package com.android.contacts.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.statistical.StatisticalHelper;
import com.google.android.gms.R;

public class BluetoothPrepareActivity extends Activity implements OnClickListener, OnCheckedChangeListener {
    private int NO_BLUETOOTH_MODE = 2;
    private int SELECT_BLUETOOTH_OPTION_MODE = 0;
    private int SUPPORT_BLUETOOTH_MODE = 1;
    private int SUPPORT_WIFI_MODE = 3;
    private int mCurrentmode = this.SELECT_BLUETOOTH_OPTION_MODE;
    private View mDivider1;
    private ListView mHintTextListView;
    private TextView mHintTextView1;
    private TextView mHintTextView2;
    private TextView mHintTextView3;
    private boolean mIsActivityInBackground = false;
    private Button mNextButton;
    private RadioButton mNoBluetooth;
    private RelativeLayout mNoSupportViewLayout;
    private Button mPreviousButton;
    private RelativeLayout mRadioButtonLayout;
    private RadioButton mSupportBluetooth;
    private RelativeLayout mSupportViewLayout;
    private RadioButton mSupportWiFi;
    private RelativeLayout mSupportWiFiViewLayout;
    private HelpListAdapter mtodolistadapter;

    private static class HelpListAdapter extends BaseAdapter {
        Context mContext = null;
        LayoutInflater mInflater;
        int[] mListItemsId = null;

        public HelpListAdapter(Context context, int textViewResourceId, int[] listItemsId) {
            this.mListItemsId = listItemsId;
            this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
            this.mContext = context;
        }

        public int getCount() {
            return this.mListItemsId.length;
        }

        public Object getItem(int position) {
            return Integer.valueOf(this.mListItemsId[position]);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int aPosition, View aView, ViewGroup parent) {
            if (aView == null) {
                aView = this.mInflater.inflate(R.layout.bluetooth_todo_list_item, null);
            }
            ((TextView) aView.findViewById(R.id.todo_list_textview)).setText(this.mContext.getResources().getString(this.mListItemsId[aPosition], new Object[]{Integer.valueOf(aPosition + 1)}));
            return aView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
        }
        if (CommonUtilMethods.isLargeThemeApplied(getResources())) {
            getWindow().setFlags(16777216, 16777216);
        }
        setContentView(R.layout.import_from_other_device_prepare);
        this.mHintTextView1 = (TextView) findViewById(R.id.hinttextview1);
        this.mHintTextView1.setText(CommonUtilMethods.upPercase(getApplicationContext().getResources().getString(R.string.contact_strDetailsForImport)));
        this.mHintTextView2 = (TextView) findViewById(R.id.hinttextview2);
        this.mHintTextView3 = (TextView) findViewById(R.id.hinttextview3);
        this.mHintTextListView = (ListView) findViewById(R.id.todolist);
        if (this.mHintTextListView != null) {
            this.mHintTextListView.setFastScrollEnabled(true);
        }
        this.mRadioButtonLayout = (RelativeLayout) findViewById(R.id.radio_button_layout);
        this.mSupportViewLayout = (RelativeLayout) findViewById(R.id.support_view);
        this.mNoSupportViewLayout = (RelativeLayout) findViewById(R.id.no_support_view);
        this.mSupportBluetooth = (RadioButton) findViewById(R.id.support_bluetooth);
        this.mNoBluetooth = (RadioButton) findViewById(R.id.no_bluetooth);
        this.mDivider1 = findViewById(R.id.divider1);
        this.mSupportWiFiViewLayout = (RelativeLayout) findViewById(R.id.wifi_support_view);
        this.mSupportWiFi = (RadioButton) findViewById(R.id.wifi_support);
        this.mNextButton = (Button) findViewById(R.id.button_next);
        this.mNextButton.setOnClickListener(this);
        this.mPreviousButton = (Button) findViewById(R.id.button_Previous);
        this.mPreviousButton.setOnClickListener(this);
        this.mSupportBluetooth.setOnCheckedChangeListener(this);
        this.mSupportViewLayout.setOnClickListener(this);
        this.mNoBluetooth.setOnCheckedChangeListener(this);
        this.mNoSupportViewLayout.setOnClickListener(this);
        if (!isWifiDirectEnabled()) {
            this.mSupportWiFiViewLayout.setVisibility(8);
        }
        this.mSupportWiFiViewLayout.setOnClickListener(this);
        this.mSupportWiFi.setOnCheckedChangeListener(this);
        if (savedInstanceState != null) {
            this.mCurrentmode = savedInstanceState.getInt("launchmode");
            updateViewBasedOnMode(this.mCurrentmode);
        }
        ActionBar actionBar = getActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                setResult(0);
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt("launchmode", this.mCurrentmode);
    }

    private void updateViewBasedOnMode(int aMode) {
        this.mCurrentmode = aMode;
        if (aMode == this.SELECT_BLUETOOTH_OPTION_MODE) {
            this.mRadioButtonLayout.setVisibility(0);
            this.mHintTextView1.setText(CommonUtilMethods.upPercase(getApplicationContext().getResources().getString(R.string.contact_strDetailsForImport)));
            this.mHintTextView1.setVisibility(0);
            this.mHintTextView2.setVisibility(8);
            this.mHintTextListView.setVisibility(8);
            this.mHintTextView3.setVisibility(8);
        } else if (aMode == this.SUPPORT_BLUETOOTH_MODE) {
            this.mRadioButtonLayout.setVisibility(8);
            this.mHintTextView2.setText(R.string.strToDoForImport);
            this.mHintTextView2.setVisibility(0);
            this.mHintTextView1.setVisibility(8);
            this.mDivider1.setVisibility(8);
            this.mHintTextListView.setVisibility(0);
            this.mtodolistadapter = new HelpListAdapter(getApplicationContext(), R.id.todo_list_textview, new int[]{R.string.str_todo_bluetooth_import_prepare_opt1, R.string.str_todo_bluetooth_import_prepare_opt2});
            this.mHintTextListView.setAdapter(this.mtodolistadapter);
            this.mHintTextView3.setText(R.string.strDetailsForImport2);
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter == null) {
                return;
            }
            if (adapter.isEnabled()) {
                this.mHintTextView3.setVisibility(8);
            } else {
                this.mHintTextView3.setVisibility(0);
            }
        } else if (aMode == this.SUPPORT_WIFI_MODE) {
            this.mIsActivityInBackground = true;
            startActivity(getWiFiIntent());
            finishActivity();
        } else {
            this.mRadioButtonLayout.setVisibility(8);
            this.mHintTextView2.setText(R.string.contact_str_no_bluetooth_support);
            this.mHintTextView2.setVisibility(0);
            this.mHintTextView3.setVisibility(8);
            this.mHintTextView1.setVisibility(8);
            this.mDivider1.setVisibility(8);
            this.mHintTextListView.setVisibility(0);
            this.mtodolistadapter = new HelpListAdapter(getApplicationContext(), R.id.todo_list_textview, new int[]{R.string.str_no_bluetooth_import_support_opt1, R.string.str_no_bluetooth_import_support_opt2, R.string.str_no_bluetooth_import_support_opt3, R.string.str_no_bluetooth_import_support_opt4});
            this.mHintTextListView.setAdapter(this.mtodolistadapter);
            this.mNextButton.setText(getApplicationContext().getResources().getString(R.string.strFinish));
        }
    }

    protected void onStop() {
        super.onStop();
        if (this.mIsActivityInBackground) {
            finish();
        }
    }

    public void onBackPressed() {
        super.onBackPressed();
    }

    public Intent getWiFiIntent() {
        Intent intent = CommonUtilMethods.getImportContactsViaWifiIntent();
        String WIFIIMPORTREQUEST = "wifiImportRequest";
        Bundle bundle = new Bundle();
        bundle.putString("wifiImportRequest", "wifiImportRequest");
        intent.putExtras(bundle);
        return intent;
    }

    private boolean isWifiDirectEnabled() {
        PackageManager pm = getApplicationContext().getPackageManager();
        if (pm != null) {
            try {
                int state = pm.getApplicationEnabledSetting("com.huawei.android.wfdft");
                if (state == 1 || state == 0) {
                    return true;
                }
            } catch (IllegalArgumentException ex) {
                ex.printStackTrace();
            }
        }
        return false;
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.support_view:
                this.mSupportBluetooth.setChecked(true);
                this.mNoBluetooth.setChecked(false);
                this.mSupportWiFi.setChecked(false);
                return;
            case R.id.wifi_support_view:
                this.mSupportBluetooth.setChecked(false);
                this.mNoBluetooth.setChecked(false);
                this.mSupportWiFi.setChecked(true);
                return;
            case R.id.no_support_view:
                this.mSupportBluetooth.setChecked(false);
                this.mNoBluetooth.setChecked(true);
                this.mSupportWiFi.setChecked(false);
                return;
            case R.id.button_Previous:
                if (this.mCurrentmode == this.SELECT_BLUETOOTH_OPTION_MODE) {
                    finish();
                    return;
                } else if (this.mCurrentmode == this.SUPPORT_BLUETOOTH_MODE) {
                    updateViewBasedOnMode(this.SELECT_BLUETOOTH_OPTION_MODE);
                    return;
                } else if (this.mCurrentmode == this.NO_BLUETOOTH_MODE) {
                    updateViewBasedOnMode(this.SELECT_BLUETOOTH_OPTION_MODE);
                    this.mNextButton.setText(getApplicationContext().getResources().getString(R.string.CS_next));
                    return;
                } else {
                    return;
                }
            case R.id.button_next:
                if (this.mCurrentmode == this.SELECT_BLUETOOTH_OPTION_MODE) {
                    boolean isSupportChecked = this.mSupportBluetooth.isChecked();
                    boolean isSupportWifiChecked = this.mSupportWiFi.isChecked();
                    if (isSupportChecked) {
                        StatisticalHelper.report(4012);
                        updateViewBasedOnMode(this.SUPPORT_BLUETOOTH_MODE);
                        return;
                    } else if (isSupportWifiChecked) {
                        StatisticalHelper.report(4013);
                        updateViewBasedOnMode(this.SUPPORT_WIFI_MODE);
                        return;
                    } else {
                        StatisticalHelper.report(4014);
                        updateViewBasedOnMode(this.NO_BLUETOOTH_MODE);
                        this.mNextButton.setText(getApplicationContext().getResources().getString(R.string.strFinish));
                        return;
                    }
                } else if (this.mCurrentmode == this.SUPPORT_BLUETOOTH_MODE) {
                    this.mIsActivityInBackground = true;
                    startActivity(CommonUtilMethods.getImportContactsViaBtIntent());
                    finishActivity();
                    return;
                } else {
                    finishActivity();
                    return;
                }
            default:
                return;
        }
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.isPressed() && isChecked) {
            switch (buttonView.getId()) {
                case R.id.support_bluetooth:
                    this.mSupportBluetooth.setChecked(true);
                    this.mNoBluetooth.setChecked(false);
                    this.mSupportWiFi.setChecked(false);
                    break;
                case R.id.wifi_support:
                    this.mSupportBluetooth.setChecked(false);
                    this.mNoBluetooth.setChecked(false);
                    this.mSupportWiFi.setChecked(true);
                    break;
                case R.id.no_bluetooth:
                    this.mSupportBluetooth.setChecked(false);
                    this.mNoBluetooth.setChecked(true);
                    this.mSupportWiFi.setChecked(false);
                    break;
            }
        }
    }

    private void finishActivity() {
        finish();
        overridePendingTransition(0, 0);
    }
}
