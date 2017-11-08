package com.android.mms.ui;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwSIMCardChangedHelper;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;

public class HwSmsCenterNumberEditerActivity extends HwBaseActivity {
    private static String sOldSmsCenter = "";
    EmuiActionBar mActionBar;
    private final OnClickListener mButtonClick = new OnClickListener() {
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.sms_center_number_set_cancel:
                    HwSmsCenterNumberEditerActivity.this.finish();
                    return;
                case R.id.sms_center_number_set_save:
                    HwSmsCenterNumberEditerActivity.this.setSmsCenterNumber(HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.getText().toString(), HwSmsCenterNumberEditerActivity.this.mSubID);
                    HwSmsCenterNumberEditerActivity.this.finish();
                    return;
                case R.id.sms_number_center_restoring:
                    HwSmsCenterNumberEditerActivity.this.restoreSmsCenterNumber();
                    return;
                default:
                    return;
            }
        }
    };
    private final TextWatcher mEditTextChangeWatcher = new TextWatcher() {
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String addStr = s.toString();
            if (count <= 0 || addStr.matches("[\\+\\d]\\d*") || addStr.matches("\\d+") || HwSmsCenterNumberEditerActivity.sOldSmsCenter.equals(addStr)) {
                HwSmsCenterNumberEditerActivity.sOldSmsCenter = addStr;
                if (TextUtils.isEmpty(HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.getText()) || "+".equals(HwSmsCenterNumberEditerActivity.sOldSmsCenter)) {
                    HwSmsCenterNumberEditerActivity.this.setEndIconEnable(false);
                } else {
                    HwSmsCenterNumberEditerActivity.this.setEndIconEnable(true);
                }
                return;
            }
            HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.setText(HwSmsCenterNumberEditerActivity.sOldSmsCenter);
            HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.setSelection(start);
            if (TextUtils.isEmpty(HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.getText()) || "+".equals(HwSmsCenterNumberEditerActivity.sOldSmsCenter)) {
                HwSmsCenterNumberEditerActivity.this.setEndIconEnable(false);
            } else {
                HwSmsCenterNumberEditerActivity.this.setEndIconEnable(true);
            }
        }

        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void afterTextChanged(Editable s) {
        }
    };
    private HwCustHwSmsCenterNumberEditerActivity mHwSmsCenterNumberEditerActivity = ((HwCustHwSmsCenterNumberEditerActivity) HwCustUtils.createObj(HwCustHwSmsCenterNumberEditerActivity.class, new Object[0]));
    int mMutilSimMode = 0;
    private String mOriginalCenterNumInSim = "";
    Button mRestoreCenterNum;
    EditText mSmsCenterEdit;
    int mSubID = -1;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            this.mSubID = getIntent().getIntExtra("intent_key_crad_sub_id", -1);
            this.mMutilSimMode = getIntent().getIntExtra("intent_key_mutil_mode", 0);
        }
        requestWindowFeature(5);
        setContentView(R.layout.sms_center_number_edit);
        this.mSmsCenterEdit = (EditText) findViewById(R.id.sms_center_number_editer);
        this.mRestoreCenterNum = (Button) findViewById(R.id.sms_number_center_restoring);
        this.mRestoreCenterNum.setOnClickListener(this.mButtonClick);
        if (MmsConfig.isModifySMSCenterAddressOnCard()) {
            this.mRestoreCenterNum.setVisibility(8);
        }
        if (this.mSmsCenterEdit != null) {
            this.mSmsCenterEdit.addTextChangedListener(this.mEditTextChangeWatcher);
            new AsyncTask<Void, Void, String>() {
                ProgressDialog mWaitDialog;

                protected String doInBackground(Void... params) {
                    if (!MmsConfig.isModifySMSCenterAddressOnCard()) {
                        HwSIMCardChangedHelper.checkSimWasReplaced(HwSmsCenterNumberEditerActivity.this, HwSmsCenterNumberEditerActivity.this.mSubID);
                    }
                    return HwSmsCenterNumberEditerActivity.this.getSmsCenterNumber(HwSmsCenterNumberEditerActivity.this, HwSmsCenterNumberEditerActivity.this.mSubID);
                }

                protected void onPostExecute(String result) {
                    this.mWaitDialog.dismiss();
                    if (result != null) {
                        HwSmsCenterNumberEditerActivity.this.mOriginalCenterNumInSim = result;
                        HwSmsCenterNumberEditerActivity.sOldSmsCenter = result;
                        HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.setText(result);
                        HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.setSelection(result.length());
                    } else {
                        Toast.makeText(HwSmsCenterNumberEditerActivity.this, HwSmsCenterNumberEditerActivity.this.getResources().getString(R.string.failto_read_sms_center_number_toast_Toast), 0).show();
                    }
                    if (HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit != null && TextUtils.isEmpty(HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.getText())) {
                        HwSmsCenterNumberEditerActivity.this.setEndIconEnable(false);
                    }
                }

                protected void onPreExecute() {
                    super.onPreExecute();
                    ProgressDialog progressDialog = new ProgressDialog(HwSmsCenterNumberEditerActivity.this);
                    this.mWaitDialog = ProgressDialog.show(HwSmsCenterNumberEditerActivity.this, null, HwSmsCenterNumberEditerActivity.this.getResources().getString(R.string.wait));
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        }
        this.mActionBar = new EmuiActionBar(this);
        this.mActionBar.setStartIcon(true, ResEx.self().getStateListDrawable(this, R.drawable.mms_ic_cancel_dark), new OnClickListener() {
            public void onClick(View v) {
                HwSmsCenterNumberEditerActivity.this.finish();
            }
        });
        this.mActionBar.setEndIcon(true, ResEx.self().getStateListDrawable(this, R.drawable.mms_ic_ok_dark), new OnClickListener() {
            public void onClick(View v) {
                if (!(HwSmsCenterNumberEditerActivity.sOldSmsCenter == null || HwSmsCenterNumberEditerActivity.sOldSmsCenter.equals(HwSmsCenterNumberEditerActivity.this.mOriginalCenterNumInSim))) {
                    StatisticalHelper.incrementReportCount(HwSmsCenterNumberEditerActivity.this, AMapException.CODE_AMAP_CLIENT_NEARBY_NULL_RESULT);
                }
                HwSmsCenterNumberEditerActivity.this.setSmsCenterNumber(HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.getText().toString(), HwSmsCenterNumberEditerActivity.this.mSubID);
                HwSmsCenterNumberEditerActivity.this.finish();
            }
        });
        updateTheTitle();
        if (this.mSmsCenterEdit != null) {
            this.mSmsCenterEdit.clearFocus();
        }
    }

    private void setSmsCenterNumberOnBoard(String number) {
        Editor editor = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit();
        switch (this.mSubID) {
            case -1:
                editor.putString("sms_center_number", number);
                break;
            case 0:
                editor.putString("pref_key_simuim1_message_center", number);
                break;
            case 1:
                editor.putString("pref_key_simuim2_message_center", number);
                break;
        }
        if (this.mSubID != -1) {
            editor.putString("sim_center_address_" + this.mSubID, number);
        } else {
            editor.putString("sim_center_address_0", number);
        }
        editor.commit();
    }

    private void setSmsCenterNumber(final String number, final int subID) {
        MLog.v("HwSmsCenterNumberEditerActivity", "setSmsCenterNumber entering");
        HwBackgroundLoader.getInst().postTask(new Runnable() {
            public void run() {
                if (!MmsConfig.isModifySMSCenterAddressOnCard()) {
                    HwSmsCenterNumberEditerActivity.this.setSmsCenterNumberOnBoard(number);
                } else if (MessageUtils.setSmsAddressBySubID("\"" + number + "\"", subID)) {
                    HwSmsCenterNumberEditerActivity.this.setSmsCenterNumberOnBoard(number);
                } else {
                    ResEx.makeToast(R.string.sms_center_set_fail_Toast);
                }
            }
        });
    }

    public String getSmsCenterNumber(Context context, int subID) {
        String smsCenterNum = null;
        String[] strArray;
        if (MmsConfig.isModifySMSCenterAddressOnCard()) {
            smsCenterNum = MessageUtils.getSmsAddressBySubID(subID);
            if (!TextUtils.isEmpty(smsCenterNum)) {
                strArray = smsCenterNum.split("\"");
                if (strArray.length > 1) {
                    smsCenterNum = strArray[1];
                }
            }
            if (TextUtils.isEmpty(smsCenterNum) && MmsConfig.getSMSCAddress() != null) {
                smsCenterNum = MmsConfig.getSMSCAddress();
            }
        } else {
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
            switch (subID) {
                case -1:
                    smsCenterNum = sp.getString("sms_center_number", null);
                    break;
                case 0:
                    smsCenterNum = sp.getString("pref_key_simuim1_message_center", null);
                    break;
                case 1:
                    smsCenterNum = sp.getString("pref_key_simuim2_message_center", null);
                    break;
            }
            if (TextUtils.isEmpty(smsCenterNum)) {
                smsCenterNum = MessageUtils.getSmsAddressBySubID(subID);
                if (!TextUtils.isEmpty(smsCenterNum)) {
                    strArray = smsCenterNum.split("\"");
                    if (strArray.length > 1) {
                        smsCenterNum = strArray[1];
                    }
                }
                if (TextUtils.isEmpty(smsCenterNum) && MmsConfig.getSMSCAddress() != null) {
                    smsCenterNum = MmsConfig.getSMSCAddress();
                }
                if (!TextUtils.isEmpty(smsCenterNum)) {
                    setSmsCenterNumberOnBoard(smsCenterNum);
                }
            }
        }
        if (!TextUtils.isEmpty(smsCenterNum) || this.mHwSmsCenterNumberEditerActivity == null) {
            return smsCenterNum;
        }
        String custSmsCenterNum = this.mHwSmsCenterNumberEditerActivity.getCustReplaceSmsCenterNumber(subID);
        if (TextUtils.isEmpty(custSmsCenterNum)) {
            return smsCenterNum;
        }
        return custSmsCenterNum;
    }

    private void restoreSmsCenterNumber() {
        new AsyncTask<Void, Void, String>() {
            ProgressDialog mWaitDialog;

            protected String doInBackground(Void... params) {
                String strCenterNum = MessageUtils.getSmsAddressBySubID(HwSmsCenterNumberEditerActivity.this.mSubID);
                if (!TextUtils.isEmpty(strCenterNum) || MmsConfig.getSMSCAddress() == null) {
                    return strCenterNum;
                }
                return MmsConfig.getSMSCAddress();
            }

            protected void onPostExecute(String result) {
                this.mWaitDialog.dismiss();
                if (result == null) {
                    Toast.makeText(HwSmsCenterNumberEditerActivity.this.getApplicationContext(), HwSmsCenterNumberEditerActivity.this.getResources().getString(R.string.failto_read_sms_center_number_toast_Toast), 0).show();
                    return;
                }
                if (HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit != null) {
                    HwSmsCenterNumberEditerActivity.this.mSmsCenterEdit.setText(result);
                }
                HwSmsCenterNumberEditerActivity.this.setSmsCenterNumberOnBoard(result);
                Toast.makeText(HwSmsCenterNumberEditerActivity.this, HwSmsCenterNumberEditerActivity.this.getResources().getString(R.string.sms_center_number_restored_Toast), 0).show();
                HwSmsCenterNumberEditerActivity.this.finish();
            }

            protected void onPreExecute() {
                super.onPreExecute();
                ProgressDialog progressDialog = new ProgressDialog(HwSmsCenterNumberEditerActivity.this.getApplicationContext());
                this.mWaitDialog = ProgressDialog.show(HwSmsCenterNumberEditerActivity.this, null, HwSmsCenterNumberEditerActivity.this.getResources().getString(R.string.wait));
            }
        }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
    }

    private void setEndIconEnable(boolean enabled) {
        ((ImageView) getWindow().getDecorView().findViewById(16908296)).setEnabled(enabled);
    }

    private void updateTheTitle() {
        if (this.mActionBar != null) {
            String strTitle = getResources().getString(R.string.sms_center_number);
            if (MessageUtils.getMultiSimState() && this.mSubID >= 0) {
                if (this.mSubID == 0) {
                    if (this.mMutilSimMode == 0) {
                        strTitle = getResources().getString(R.string.pref_title_simuim1_center_number_ug);
                    }
                } else if (MessageUtils.isCTCdmaCardInGsmMode()) {
                    strTitle = getResources().getString(R.string.pref_title_simuim1_center_number_ug);
                } else if (this.mMutilSimMode == 0) {
                    strTitle = getResources().getString(R.string.pref_title_simuim2_center_number_ug);
                }
            }
            this.mActionBar.setTitle(strTitle);
        }
    }

    public void finish() {
        super.finish();
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mSmsCenterEdit.getWindowToken(), 0);
    }

    protected void onPause() {
        super.onPause();
        ((InputMethodManager) getSystemService("input_method")).hideSoftInputFromWindow(this.mSmsCenterEdit.getWindowToken(), 0);
    }

    protected void onStart() {
        super.onStart();
    }
}
