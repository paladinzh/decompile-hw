package com.android.contacts.hap.tianyi;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import com.android.contacts.CallUtil;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class TianyiDialer extends Activity {
    private EditText countryCodeInput;
    private LinearLayout layout1;
    private LinearLayout layout2;
    private LinearLayout layout3;
    private LinearLayout layout4;
    private ActionBar mActionBar;
    private ImageButton mCallButton;
    private final OnClickListener mCallButtonClickListener = new OnClickListener() {
        public void onClick(View v) {
            String number = TianyiDialer.this.numberInput.getText().toString().trim();
            String countryCode = TianyiDialer.this.countryCodeInput.getText().toString().trim();
            String tianyiNumber = TianyiDialer.this.getTianyiNumber(countryCode, number);
            if (!TianyiDialer.this.findCountryNameByCode(TianyiDialer.this.addPlusForCountryCode(countryCode)).equals("UNKNOWN") || TianyiDialer.this.mRadioFlag == 4) {
                if (CommonConstants.LOG_DEBUG) {
                    HwLog.d("TianyiDialer", "Tianyi::Initiating call");
                }
                Intent callIntent = CallUtil.getCallIntent(tianyiNumber, TianyiDialer.this.mSubscription);
                callIntent.putExtra("tianyi_dialer", true);
                TianyiDialer.this.startActivity(callIntent);
                TianyiDialer.this.finish();
                return;
            }
            TianyiDialer.this.displayDialog(TianyiDialer.this.getString(R.string.country_code_wrong_header), TianyiDialer.this.getString(R.string.country_code_wrong_tip));
        }
    };
    private ArrayList<CountryCodeAndName> mCountryList = new ArrayList();
    private String mNetworkISO;
    private final OnClickListener mRadioClickListener = new OnClickListener() {
        public void onClick(View v) {
            TianyiDialer.this.radioBut1.setChecked(false);
            TianyiDialer.this.radioBut2.setChecked(false);
            TianyiDialer.this.radioBut3.setChecked(false);
            TianyiDialer.this.radioBut4.setChecked(false);
            switch (v.getId()) {
                case R.id.tianyi_radio1:
                    TianyiDialer.this.radioBut1.setChecked(true);
                    return;
                case R.id.tianyi_radio2:
                    TianyiDialer.this.radioBut2.setChecked(true);
                    return;
                case R.id.tianyi_radio3:
                    TianyiDialer.this.radioBut3.setChecked(true);
                    return;
                case R.id.tianyi_radio4:
                    TianyiDialer.this.radioBut4.setChecked(true);
                    return;
                default:
                    return;
            }
        }
    };
    private int mRadioFlag = 1;
    private Set<String> mSignedCTOperators;
    private int mSubscription;
    private String mTianyiDialerLabel;
    private TelephonyManager manager;
    private EditText numberInput;
    private RadioButton radioBut1;
    private RadioButton radioBut2;
    private RadioButton radioBut3;
    private RadioButton radioBut4;
    private final OnCheckedChangeListener radioListener = new OnCheckedChangeListener() {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            TianyiDialer.this.radioBut1.setChecked(false);
            TianyiDialer.this.radioBut2.setChecked(false);
            TianyiDialer.this.radioBut3.setChecked(false);
            TianyiDialer.this.radioBut4.setChecked(false);
            switch (buttonView.getId()) {
                case R.id.radioButton1:
                    if (isChecked) {
                        TianyiDialer.this.radioBut1.setChecked(true);
                        TianyiDialer.this.mRadioFlag = 1;
                        TianyiDialer.this.updateCountryCodeText("+86");
                        return;
                    }
                    return;
                case R.id.radioButton2:
                    if (isChecked) {
                        TianyiDialer.this.radioBut2.setChecked(true);
                        TianyiDialer.this.mRadioFlag = 2;
                        TianyiDialer.this.updateCountryCodeText("**133");
                        return;
                    }
                    return;
                case R.id.radioButton3:
                    if (isChecked) {
                        TianyiDialer.this.radioBut3.setChecked(true);
                        TianyiDialer.this.mRadioFlag = 3;
                        Intent intent = new Intent("android.intent.action.VIEW");
                        intent.setAction("com.huawei.android.TIANYI_DIALER_LIST");
                        TianyiDialer.this.startActivityForResult(intent, 0);
                        return;
                    }
                    return;
                case R.id.radioButton4:
                    if (isChecked) {
                        TianyiDialer.this.radioBut4.setChecked(true);
                        TianyiDialer.this.mRadioFlag = 4;
                        TianyiDialer.this.updateCountryCodeText("");
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };

    public static class CountryCodeAndName {
        String countryCode;
        String countryName;

        CountryCodeAndName(String countryCode, String aCountryName) {
            this.countryCode = countryCode;
            this.countryName = aCountryName;
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        if (!getResources().getBoolean(R.bool.config_tianyi_dialer)) {
            HwLog.w("TianyiDialer", "Not China Telecom");
            finish();
        }
        tianyiDialerFirstStart();
        this.mActionBar = getActionBar();
        if (this.mActionBar != null) {
            this.mActionBar.setDisplayHomeAsUpEnabled(true);
            this.mActionBar.setDisplayShowTitleEnabled(true);
        }
        String number = "";
        String countryCode = "";
        Intent intent = getIntent();
        Uri data = intent.getData();
        if (data != null && "tel".equals(data.getScheme())) {
            number = data.getSchemeSpecificPart();
        }
        this.mSubscription = intent.getIntExtra("subscription", -1);
        loadCountryCodeAndNames();
        setContentView(R.layout.tianyi_dialer);
        this.mCallButton = (ImageButton) findViewById(R.id.contact_btn_call);
        this.mCallButton.setOnClickListener(this.mCallButtonClickListener);
        this.mTianyiDialerLabel = getString(R.string.tianyi_dialer_label);
        this.numberInput = (EditText) findViewById(R.id.tianyi_number_input);
        this.countryCodeInput = (EditText) findViewById(R.id.tianyi_country_code_input);
        setInputTextChangeListener();
        updateInputText("+86", number);
        setCursorAtLast(this.numberInput);
        this.radioBut1 = (RadioButton) findViewById(R.id.radioButton1);
        this.radioBut2 = (RadioButton) findViewById(R.id.radioButton2);
        this.radioBut3 = (RadioButton) findViewById(R.id.radioButton3);
        this.radioBut4 = (RadioButton) findViewById(R.id.radioButton4);
        this.radioBut1.setOnCheckedChangeListener(this.radioListener);
        this.radioBut2.setOnCheckedChangeListener(this.radioListener);
        this.radioBut3.setOnCheckedChangeListener(this.radioListener);
        this.radioBut4.setOnCheckedChangeListener(this.radioListener);
        this.layout1 = (LinearLayout) findViewById(R.id.tianyi_radio1);
        this.layout2 = (LinearLayout) findViewById(R.id.tianyi_radio2);
        this.layout3 = (LinearLayout) findViewById(R.id.tianyi_radio3);
        this.layout4 = (LinearLayout) findViewById(R.id.tianyi_radio4);
        this.layout1.setClickable(true);
        this.layout2.setClickable(true);
        this.layout3.setClickable(true);
        this.layout4.setClickable(true);
        this.layout1.setOnClickListener(this.mRadioClickListener);
        this.layout2.setOnClickListener(this.mRadioClickListener);
        this.layout3.setOnClickListener(this.mRadioClickListener);
        this.layout4.setOnClickListener(this.mRadioClickListener);
        this.manager = (TelephonyManager) getSystemService("phone");
        loadSignedCTOperators();
    }

    private void tianyiDialerFirstStart() {
        SharedPreferences sp = getSharedPreferences("TIANYI_SP", 0);
        if (sp.getBoolean("isTianyiDialerFirstStart", true)) {
            goToTianyiHelper();
        }
        sp.edit().putBoolean("isTianyiDialerFirstStart", false).commit();
        HwLog.i("TianyiDialer", "isTianyiDialerFirstStart is " + sp.getBoolean("isTianyiDialerFirstStart", true));
    }

    private void goToTianyiHelper() {
        Intent intent = new Intent("android.intent.action.VIEW");
        intent.setAction("com.huawei.android.TIANYI_HELPER");
        startActivity(intent);
    }

    private void setInputTextChangeListener() {
        this.countryCodeInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s != null && !TextUtils.isEmpty(s.toString().trim())) {
                    String countryCode = TianyiDialer.this.addPlusForCountryCode(TianyiDialer.this.countryCodeInput.getText().toString().trim());
                    TianyiDialer.this.setActionBarTitle(TianyiDialer.this.findCountryNameByCode(countryCode), countryCode);
                }
            }
        });
        this.numberInput.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s == null || TextUtils.isEmpty(s.toString().trim())) {
                    TianyiDialer.this.mCallButton.setClickable(false);
                } else {
                    TianyiDialer.this.mCallButton.setClickable(true);
                }
            }
        });
    }

    private String addPlusForCountryCode(String countryCode) {
        if (countryCode == null || countryCode.startsWith("+") || countryCode.equals("**133*86") || countryCode.equals("**133")) {
            return countryCode;
        }
        return "+" + countryCode;
    }

    private void setActionBarTitle(String countryName, String countryCode) {
        if ("CALL_BACK".equals(countryName)) {
            countryName = getString(R.string.country_china);
        }
        if ("UNKNOWN".equals(countryName)) {
            countryName = getString(R.string.tianyi_unknown);
        }
        if (this.mActionBar != null) {
            this.mActionBar.setTitle(this.mTianyiDialerLabel + " (" + countryName + HwCustPreloadContacts.EMPTY_STRING + countryCode + ")");
        }
    }

    protected String findCountryNameByCode(String countryCode) {
        ArrayList<String> countryNameList = new ArrayList();
        if (countryCode == null) {
            return "UNKNOWN";
        }
        if (countryCode.equals("**133") || countryCode.equals("**133*86")) {
            return "CALL_BACK";
        }
        for (CountryCodeAndName country : this.mCountryList) {
            if (country.countryCode.equals(countryCode)) {
                countryNameList.add(country.countryName);
            }
        }
        int countryCount = countryNameList.size();
        if (countryCount == 1) {
            return (String) countryNameList.get(0);
        }
        if (countryCount <= 1) {
            return "UNKNOWN";
        }
        StringBuffer countryNames = new StringBuffer();
        for (int i = 0; i < countryCount; i++) {
            countryNames.append((String) countryNameList.get(i));
            if (i != countryCount - 1) {
                countryNames.append("/");
            }
        }
        return countryNames.toString();
    }

    private String getTianyiNumber(String countryCode, String number) {
        if (number == null) {
            return null;
        }
        String tianyiNumber;
        countryCode = addPlusForCountryCode(countryCode);
        switch (this.mRadioFlag) {
            case 2:
                if (!number.startsWith("0")) {
                    tianyiNumber = "**133*86" + number + "#";
                    break;
                }
                tianyiNumber = "**133*86" + number.substring(1) + "#";
                break;
            case 4:
                tianyiNumber = number;
                break;
            default:
                if (!number.startsWith("0")) {
                    tianyiNumber = countryCode + number;
                    break;
                }
                tianyiNumber = countryCode + number.substring(1);
                break;
        }
        return tianyiNumber;
    }

    private void updateInputText(String countryCode, String number) {
        this.numberInput.setText(number);
        this.countryCodeInput.setText(countryCode);
    }

    private void updateCountryCodeText(String countryCode) {
        if (!(countryCode == null || countryCode.equals(this.countryCodeInput.getText().toString()))) {
            this.countryCodeInput.setText(countryCode);
            setActionBarTitle(findCountryNameByCode(countryCode), countryCode);
        }
    }

    private void displayDialog(String headerString, String tipString) {
        Builder builder = new Builder(this);
        builder.setTitle(headerString);
        builder.setMessage(tipString);
        builder.setNegativeButton(getString(R.string.dialog_ok), null);
        builder.create().show();
    }

    protected void onResume() {
        super.onResume();
        this.mNetworkISO = this.manager.getNetworkCountryIso();
        boolean isSigned = this.mSignedCTOperators.contains(this.mNetworkISO.toUpperCase(Locale.US));
        this.radioBut2.setEnabled(isSigned);
        this.layout2.setEnabled(isSigned);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == -1) {
            switch (requestCode) {
                case 0:
                    if (data != null) {
                        updateCountryCodeText(data.getStringExtra("countryCode"));
                        setCursorAtLast(this.numberInput);
                        break;
                    }
                    break;
            }
        }
    }

    private void setCursorAtLast(EditText editText) {
        if (editText != null) {
            Editable et = editText.getEditableText();
            Selection.setSelection(et, et.length());
        }
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

    @SuppressLint({"HwHardCodeDateFormat"})
    private void loadSignedCTOperators() {
        this.mSignedCTOperators = new HashSet();
        this.mSignedCTOperators.add("US");
        this.mSignedCTOperators.add("CA");
        this.mSignedCTOperators.add("PR");
        this.mSignedCTOperators.add("HN");
        this.mSignedCTOperators.add("CO");
        this.mSignedCTOperators.add("UY");
        this.mSignedCTOperators.add("PY");
        this.mSignedCTOperators.add("BR");
        this.mSignedCTOperators.add("BO");
        this.mSignedCTOperators.add("AI");
        this.mSignedCTOperators.add("AG");
        this.mSignedCTOperators.add("VC");
        this.mSignedCTOperators.add("KN");
        this.mSignedCTOperators.add("DO");
        this.mSignedCTOperators.add("GD");
        this.mSignedCTOperators.add("LC");
        this.mSignedCTOperators.add("PE");
        this.mSignedCTOperators.add("BB");
        this.mSignedCTOperators.add("KY");
        this.mSignedCTOperators.add("JM");
        this.mSignedCTOperators.add("HT");
        this.mSignedCTOperators.add("GY");
        this.mSignedCTOperators.add("DM");
        this.mSignedCTOperators.add("MS");
        this.mSignedCTOperators.add("VE");
        this.mSignedCTOperators.add("GT");
        this.mSignedCTOperators.add("CL");
        this.mSignedCTOperators.add("BZ");
        this.mSignedCTOperators.add("AR");
        this.mSignedCTOperators.add("TT");
        this.mSignedCTOperators.add("TC");
        this.mSignedCTOperators.add("VG");
        this.mSignedCTOperators.add("PA");
        this.mSignedCTOperators.add("MX");
        this.mSignedCTOperators.add("NI");
        this.mSignedCTOperators.add("EC");
        this.mSignedCTOperators.add("CU");
        this.mSignedCTOperators.add("SR");
        this.mSignedCTOperators.add("CR");
        this.mSignedCTOperators.add("SV");
        this.mSignedCTOperators.add("SG");
        this.mSignedCTOperators.add("MY");
        this.mSignedCTOperators.add("VN");
        this.mSignedCTOperators.add("MM");
        this.mSignedCTOperators.add("ID");
        this.mSignedCTOperators.add("TH");
        this.mSignedCTOperators.add("LA");
        this.mSignedCTOperators.add("KH");
        this.mSignedCTOperators.add("PH");
        this.mSignedCTOperators.add("BN");
        this.mSignedCTOperators.add("JP");
        this.mSignedCTOperators.add("KR");
        this.mSignedCTOperators.add("MN");
        this.mSignedCTOperators.add("KZ");
        this.mSignedCTOperators.add("KG");
        this.mSignedCTOperators.add("TM");
        this.mSignedCTOperators.add("UZ");
        this.mSignedCTOperators.add("TJ");
        this.mSignedCTOperators.add("AZ");
        this.mSignedCTOperators.add("GE");
        this.mSignedCTOperators.add("IN");
        this.mSignedCTOperators.add("PK");
        this.mSignedCTOperators.add("NP");
        this.mSignedCTOperators.add("BT");
        this.mSignedCTOperators.add("LK");
        this.mSignedCTOperators.add("MV");
        this.mSignedCTOperators.add("BD");
        this.mSignedCTOperators.add("BH");
        this.mSignedCTOperators.add("SY");
        this.mSignedCTOperators.add("YE");
        this.mSignedCTOperators.add("IQ");
        this.mSignedCTOperators.add("SA");
        this.mSignedCTOperators.add("JO");
        this.mSignedCTOperators.add("QA");
        this.mSignedCTOperators.add("IL");
        this.mSignedCTOperators.add("KW");
        this.mSignedCTOperators.add("IR");
        this.mSignedCTOperators.add("PS");
        this.mSignedCTOperators.add("AE");
        this.mSignedCTOperators.add("LB");
        this.mSignedCTOperators.add("OM");
        this.mSignedCTOperators.add("AF");
        this.mSignedCTOperators.add("AM");
        this.mSignedCTOperators.add("TR");
        this.mSignedCTOperators.add("EG");
        this.mSignedCTOperators.add("DZ");
        this.mSignedCTOperators.add("SD");
        this.mSignedCTOperators.add("MA");
        this.mSignedCTOperators.add("TZ");
        this.mSignedCTOperators.add("MU");
        this.mSignedCTOperators.add("SN");
        this.mSignedCTOperators.add("AO");
        this.mSignedCTOperators.add("CI");
        this.mSignedCTOperators.add("CV");
        this.mSignedCTOperators.add("KE");
        this.mSignedCTOperators.add("LS");
        this.mSignedCTOperators.add("CM");
        this.mSignedCTOperators.add("ZW");
        this.mSignedCTOperators.add("GM");
        this.mSignedCTOperators.add("GN");
        this.mSignedCTOperators.add("BW");
        this.mSignedCTOperators.add("BJ");
        this.mSignedCTOperators.add("ZA");
        this.mSignedCTOperators.add("MW");
        this.mSignedCTOperators.add("SC");
        this.mSignedCTOperators.add("MZ");
        this.mSignedCTOperators.add("UG");
        this.mSignedCTOperators.add("LR");
        this.mSignedCTOperators.add("CN");
        this.mSignedCTOperators.add("NE");
        this.mSignedCTOperators.add("DJ");
        this.mSignedCTOperators.add("CD");
        this.mSignedCTOperators.add("CG");
        this.mSignedCTOperators.add("NG");
        this.mSignedCTOperators.add("GH");
        this.mSignedCTOperators.add("SL");
        this.mSignedCTOperators.add("TD");
        this.mSignedCTOperators.add("MG");
        this.mSignedCTOperators.add("TG");
        this.mSignedCTOperators.add("BI");
        this.mSignedCTOperators.add("ZM");
        this.mSignedCTOperators.add("ET");
        this.mSignedCTOperators.add("ML");
        this.mSignedCTOperators.add("GW");
        this.mSignedCTOperators.add("TN");
        this.mSignedCTOperators.add("IT");
        this.mSignedCTOperators.add("ES");
        this.mSignedCTOperators.add("GR");
        this.mSignedCTOperators.add("SE");
        this.mSignedCTOperators.add("PT");
        this.mSignedCTOperators.add("NO");
        this.mSignedCTOperators.add("AT");
        this.mSignedCTOperators.add("NL");
        this.mSignedCTOperators.add("DE");
        this.mSignedCTOperators.add("FI");
        this.mSignedCTOperators.add("FR");
        this.mSignedCTOperators.add("CH");
        this.mSignedCTOperators.add("GB");
        this.mSignedCTOperators.add("JE");
        this.mSignedCTOperators.add("IE");
        this.mSignedCTOperators.add("CY");
        this.mSignedCTOperators.add("MT");
        this.mSignedCTOperators.add("RO");
        this.mSignedCTOperators.add("HR");
        this.mSignedCTOperators.add("BE");
        this.mSignedCTOperators.add("CZ");
        this.mSignedCTOperators.add("LU");
        this.mSignedCTOperators.add("BA");
        this.mSignedCTOperators.add("IS");
        this.mSignedCTOperators.add("LI");
        this.mSignedCTOperators.add("MC");
        this.mSignedCTOperators.add("EE");
        this.mSignedCTOperators.add("LT");
        this.mSignedCTOperators.add("ME");
        this.mSignedCTOperators.add("RS");
        this.mSignedCTOperators.add("SI");
        this.mSignedCTOperators.add("RU");
        this.mSignedCTOperators.add("RE");
        this.mSignedCTOperators.add("UA");
        this.mSignedCTOperators.add("BY");
        this.mSignedCTOperators.add("DK");
        this.mSignedCTOperators.add("MD");
        this.mSignedCTOperators.add("BG");
        this.mSignedCTOperators.add("PL");
        this.mSignedCTOperators.add("SK");
        this.mSignedCTOperators.add("LV");
        this.mSignedCTOperators.add("HU");
        this.mSignedCTOperators.add("AL");
        this.mSignedCTOperators.add("VA");
        this.mSignedCTOperators.add("MK");
        this.mSignedCTOperators.add("SM");
        this.mSignedCTOperators.add("AD");
        this.mSignedCTOperators.add("PF");
        this.mSignedCTOperators.add("AU");
        this.mSignedCTOperators.add("NZ");
        this.mSignedCTOperators.add("PG");
        this.mSignedCTOperators.add("TO");
        this.mSignedCTOperators.add("VU");
        this.mSignedCTOperators.add("FJ");
        this.mSignedCTOperators.add("WS");
        this.mSignedCTOperators.add("NR");
    }

    private void loadCountryCodeAndNames() {
        this.mCountryList.add(new CountryCodeAndName("+86", getString(R.string.country_china)));
        this.mCountryList.add(new CountryCodeAndName("+93", getString(R.string.country_afghanistan)));
        this.mCountryList.add(new CountryCodeAndName("+374", getString(R.string.country_armenia)));
        this.mCountryList.add(new CountryCodeAndName("+994", getString(R.string.country_azerbaijan)));
        this.mCountryList.add(new CountryCodeAndName("+973", getString(R.string.country_bahrain)));
        this.mCountryList.add(new CountryCodeAndName("+880", getString(R.string.country_bangladesh)));
        this.mCountryList.add(new CountryCodeAndName("+773", getString(R.string.country_bruneiDarussalam)));
        this.mCountryList.add(new CountryCodeAndName("+770", getString(R.string.country_eastTimor)));
        this.mCountryList.add(new CountryCodeAndName("+852", getString(R.string.country_hongKong)));
        this.mCountryList.add(new CountryCodeAndName("+91", getString(R.string.country_india)));
        this.mCountryList.add(new CountryCodeAndName("+72", getString(R.string.country_indonesia)));
        this.mCountryList.add(new CountryCodeAndName("+98", getString(R.string.country_iran)));
        this.mCountryList.add(new CountryCodeAndName("+964", getString(R.string.country_iraq)));
        this.mCountryList.add(new CountryCodeAndName("+972", getString(R.string.country_israel)));
        this.mCountryList.add(new CountryCodeAndName("+81", getString(R.string.country_japan)));
        this.mCountryList.add(new CountryCodeAndName("+962", getString(R.string.country_jordan)));
        this.mCountryList.add(new CountryCodeAndName("+7", getString(R.string.country_kazakhstan)));
        this.mCountryList.add(new CountryCodeAndName("+82", getString(R.string.country_republicofKorea)));
        this.mCountryList.add(new CountryCodeAndName("+965", getString(R.string.country_kuwait)));
        this.mCountryList.add(new CountryCodeAndName("+996", getString(R.string.country_kirghizstan)));
        this.mCountryList.add(new CountryCodeAndName("+856", getString(R.string.country_laos)));
        this.mCountryList.add(new CountryCodeAndName("+961", getString(R.string.country_lebanon)));
        this.mCountryList.add(new CountryCodeAndName("+853", getString(R.string.country_macao)));
        this.mCountryList.add(new CountryCodeAndName("+70", getString(R.string.country_malaysia)));
        this.mCountryList.add(new CountryCodeAndName("+960", getString(R.string.country_maldives)));
        this.mCountryList.add(new CountryCodeAndName("+976", getString(R.string.country_mongolia)));
        this.mCountryList.add(new CountryCodeAndName("+977", getString(R.string.country_nepal)));
        this.mCountryList.add(new CountryCodeAndName("+968", getString(R.string.country_oman)));
        this.mCountryList.add(new CountryCodeAndName("+92", getString(R.string.country_pakistan)));
        this.mCountryList.add(new CountryCodeAndName("+970", getString(R.string.country_palestine)));
        this.mCountryList.add(new CountryCodeAndName("+974", getString(R.string.country_qatar)));
        this.mCountryList.add(new CountryCodeAndName("+75", getString(R.string.country_singapore)));
        this.mCountryList.add(new CountryCodeAndName("+94", getString(R.string.country_srilanka)));
        this.mCountryList.add(new CountryCodeAndName("+963", getString(R.string.country_syria)));
        this.mCountryList.add(new CountryCodeAndName("+886", getString(R.string.country_taiwan)));
        this.mCountryList.add(new CountryCodeAndName("+992", getString(R.string.country_tajikistan)));
        this.mCountryList.add(new CountryCodeAndName("+76", getString(R.string.country_thailand)));
        this.mCountryList.add(new CountryCodeAndName("+90", getString(R.string.country_turkey)));
        this.mCountryList.add(new CountryCodeAndName("+993", getString(R.string.country_turkmenistan)));
        this.mCountryList.add(new CountryCodeAndName("+971", getString(R.string.country_unitedArabEmirates)));
        this.mCountryList.add(new CountryCodeAndName("+998", getString(R.string.country_uzbekistan)));
        this.mCountryList.add(new CountryCodeAndName("+84", getString(R.string.country_vietnam)));
        this.mCountryList.add(new CountryCodeAndName("+967", getString(R.string.country_yemen)));
        this.mCountryList.add(new CountryCodeAndName("+213", getString(R.string.country_algeria)));
        this.mCountryList.add(new CountryCodeAndName("+244", getString(R.string.country_angola)));
        this.mCountryList.add(new CountryCodeAndName("+229", getString(R.string.country_benin)));
        this.mCountryList.add(new CountryCodeAndName("+267", getString(R.string.country_botswana)));
        this.mCountryList.add(new CountryCodeAndName("+226", getString(R.string.country_burkinaFaso)));
        this.mCountryList.add(new CountryCodeAndName("+257", getString(R.string.country_burundi)));
        this.mCountryList.add(new CountryCodeAndName("+237", getString(R.string.country_cameroon)));
        this.mCountryList.add(new CountryCodeAndName("+34", getString(R.string.country_theCanaryIslands)));
        this.mCountryList.add(new CountryCodeAndName("+238", getString(R.string.country_capeVerde)));
        this.mCountryList.add(new CountryCodeAndName("+236", getString(R.string.country_centralAfricanRepublic)));
        this.mCountryList.add(new CountryCodeAndName("+34952", getString(R.string.country_ceuta)));
        this.mCountryList.add(new CountryCodeAndName("+242", getString(R.string.country_republicOfTheCongo)));
        this.mCountryList.add(new CountryCodeAndName("+243", getString(R.string.country_democraticRepublicOfCongo)));
        this.mCountryList.add(new CountryCodeAndName("+20", getString(R.string.country_egypt)));
        this.mCountryList.add(new CountryCodeAndName("+240", getString(R.string.country_equatorialGuinea)));
        this.mCountryList.add(new CountryCodeAndName("+241", getString(R.string.country_gabon)));
        this.mCountryList.add(new CountryCodeAndName("+220", getString(R.string.country_gambia)));
        this.mCountryList.add(new CountryCodeAndName("+233", getString(R.string.country_ghana)));
        this.mCountryList.add(new CountryCodeAndName("+224", getString(R.string.country_guinea)));
        this.mCountryList.add(new CountryCodeAndName("+254", getString(R.string.country_kenya)));
        this.mCountryList.add(new CountryCodeAndName("+231", getString(R.string.country_liberia)));
        this.mCountryList.add(new CountryCodeAndName("+218", getString(R.string.country_libya)));
        this.mCountryList.add(new CountryCodeAndName("+261", getString(R.string.country_madagascar)));
        this.mCountryList.add(new CountryCodeAndName("+265", getString(R.string.country_malawi)));
        this.mCountryList.add(new CountryCodeAndName("+223", getString(R.string.country_mali)));
        this.mCountryList.add(new CountryCodeAndName("+222", getString(R.string.country_mauritania)));
        this.mCountryList.add(new CountryCodeAndName("+230", getString(R.string.country_mauritius)));
        this.mCountryList.add(new CountryCodeAndName("+212", getString(R.string.country_morocco)));
        this.mCountryList.add(new CountryCodeAndName("+258", getString(R.string.country_mozambique)));
        this.mCountryList.add(new CountryCodeAndName("+264", getString(R.string.country_namibia)));
        this.mCountryList.add(new CountryCodeAndName("+227", getString(R.string.country_niger)));
        this.mCountryList.add(new CountryCodeAndName("+234", getString(R.string.country_nigeria)));
        this.mCountryList.add(new CountryCodeAndName("+221", getString(R.string.country_senegal)));
        this.mCountryList.add(new CountryCodeAndName("+248", getString(R.string.country_seychelles)));
        this.mCountryList.add(new CountryCodeAndName("+232", getString(R.string.country_sierraLeone)));
        this.mCountryList.add(new CountryCodeAndName("+27", getString(R.string.country_southAfrica)));
        this.mCountryList.add(new CountryCodeAndName("+249", getString(R.string.country_sudan)));
        this.mCountryList.add(new CountryCodeAndName("+255", getString(R.string.country_tanzania)));
        this.mCountryList.add(new CountryCodeAndName("+216", getString(R.string.country_tunisia)));
        this.mCountryList.add(new CountryCodeAndName("+256", getString(R.string.country_uganda)));
        this.mCountryList.add(new CountryCodeAndName("+260", getString(R.string.country_zambia)));
        this.mCountryList.add(new CountryCodeAndName("+255", getString(R.string.country_zanzibar)));
        this.mCountryList.add(new CountryCodeAndName("+263", getString(R.string.country_zimbabwe)));
        this.mCountryList.add(new CountryCodeAndName("+355", getString(R.string.country_albania)));
        this.mCountryList.add(new CountryCodeAndName("+376", getString(R.string.country_andorra)));
        this.mCountryList.add(new CountryCodeAndName("+43", getString(R.string.country_austria)));
        this.mCountryList.add(new CountryCodeAndName("+351", getString(R.string.country_azores)));
        this.mCountryList.add(new CountryCodeAndName("+375", getString(R.string.country_belarus)));
        this.mCountryList.add(new CountryCodeAndName("+32", getString(R.string.country_belgium)));
        this.mCountryList.add(new CountryCodeAndName("+387", getString(R.string.country_bosniaAndHerzegovina)));
        this.mCountryList.add(new CountryCodeAndName("+359", getString(R.string.country_bulgaria)));
        this.mCountryList.add(new CountryCodeAndName("+495", getString(R.string.country_corsica)));
        this.mCountryList.add(new CountryCodeAndName("+385", getString(R.string.country_croatia)));
        this.mCountryList.add(new CountryCodeAndName("+357", getString(R.string.country_cyprus)));
        this.mCountryList.add(new CountryCodeAndName("+420", getString(R.string.country_czechRepublic)));
        this.mCountryList.add(new CountryCodeAndName("+45", getString(R.string.country_denmark)));
        this.mCountryList.add(new CountryCodeAndName("+372", getString(R.string.country_estonia)));
        this.mCountryList.add(new CountryCodeAndName("+298", getString(R.string.country_faroeIslands)));
        this.mCountryList.add(new CountryCodeAndName("+358", getString(R.string.country_finland)));
        this.mCountryList.add(new CountryCodeAndName("+33", getString(R.string.country_france)));
        this.mCountryList.add(new CountryCodeAndName("+995", getString(R.string.country_georgia)));
        this.mCountryList.add(new CountryCodeAndName("+49", getString(R.string.country_germany)));
        this.mCountryList.add(new CountryCodeAndName("+350", getString(R.string.country_gibraltar)));
        this.mCountryList.add(new CountryCodeAndName("+30", getString(R.string.country_greece)));
        this.mCountryList.add(new CountryCodeAndName("+36", getString(R.string.country_hungary)));
        this.mCountryList.add(new CountryCodeAndName("+34", getString(R.string.country_ibiza)));
        this.mCountryList.add(new CountryCodeAndName("+354", getString(R.string.country_iceland)));
        this.mCountryList.add(new CountryCodeAndName("+353", getString(R.string.country_ireland)));
        this.mCountryList.add(new CountryCodeAndName("+39", getString(R.string.country_italy)));
        this.mCountryList.add(new CountryCodeAndName("+44", getString(R.string.country_jersey)));
        this.mCountryList.add(new CountryCodeAndName("+381", getString(R.string.country_kosovo)));
        this.mCountryList.add(new CountryCodeAndName("+371", getString(R.string.country_latvia)));
        this.mCountryList.add(new CountryCodeAndName("+423", getString(R.string.country_liechtenstein)));
        this.mCountryList.add(new CountryCodeAndName("+370", getString(R.string.country_lithuania)));
        this.mCountryList.add(new CountryCodeAndName("+352", getString(R.string.country_luxembourg)));
        this.mCountryList.add(new CountryCodeAndName("+389", getString(R.string.country_macedonia)));
        this.mCountryList.add(new CountryCodeAndName("+351", getString(R.string.country_madeira)));
        this.mCountryList.add(new CountryCodeAndName("+34971", getString(R.string.country_mallorca)));
        this.mCountryList.add(new CountryCodeAndName("+356", getString(R.string.country_malta)));
        this.mCountryList.add(new CountryCodeAndName("+373", getString(R.string.country_moldova)));
        this.mCountryList.add(new CountryCodeAndName("+377", getString(R.string.country_monaco)));
        this.mCountryList.add(new CountryCodeAndName("+382", getString(R.string.country_montenegro)));
        this.mCountryList.add(new CountryCodeAndName("+31", getString(R.string.country_netherlands)));
        this.mCountryList.add(new CountryCodeAndName("+44", getString(R.string.country_northernIreland)));
        this.mCountryList.add(new CountryCodeAndName("+47", getString(R.string.country_norway)));
        this.mCountryList.add(new CountryCodeAndName("+48", getString(R.string.country_poland)));
        this.mCountryList.add(new CountryCodeAndName("+351", getString(R.string.country_portugal)));
        this.mCountryList.add(new CountryCodeAndName("+40", getString(R.string.country_romania)));
        this.mCountryList.add(new CountryCodeAndName("+7", getString(R.string.country_russianFederation)));
        this.mCountryList.add(new CountryCodeAndName("+39", getString(R.string.country_sardinia)));
        this.mCountryList.add(new CountryCodeAndName("+381", getString(R.string.country_serbia)));
        this.mCountryList.add(new CountryCodeAndName("+39", getString(R.string.country_sicily)));
        this.mCountryList.add(new CountryCodeAndName("+421", getString(R.string.country_slovakia)));
        this.mCountryList.add(new CountryCodeAndName("+386", getString(R.string.country_slovenia)));
        this.mCountryList.add(new CountryCodeAndName("+34", getString(R.string.country_spain)));
        this.mCountryList.add(new CountryCodeAndName("+46", getString(R.string.country_sweden)));
        this.mCountryList.add(new CountryCodeAndName("+41", getString(R.string.country_switzerland)));
        this.mCountryList.add(new CountryCodeAndName("+380", getString(R.string.country_ukraine)));
        this.mCountryList.add(new CountryCodeAndName("+44", getString(R.string.country_unitedKingdom)));
        this.mCountryList.add(new CountryCodeAndName("+358", getString(R.string.country_alandIslands)));
        this.mCountryList.add(new CountryCodeAndName("+1264", getString(R.string.country_anguilla)));
        this.mCountryList.add(new CountryCodeAndName("+1268", getString(R.string.country_antiguaAndBarbuda)));
        this.mCountryList.add(new CountryCodeAndName("+297", getString(R.string.country_aruba)));
        this.mCountryList.add(new CountryCodeAndName("+1242", getString(R.string.country_bahamas)));
        this.mCountryList.add(new CountryCodeAndName("+1246", getString(R.string.country_barbados)));
        this.mCountryList.add(new CountryCodeAndName("+501", getString(R.string.country_belize)));
        this.mCountryList.add(new CountryCodeAndName("+1441", getString(R.string.country_bermuda)));
        this.mCountryList.add(new CountryCodeAndName("+1", getString(R.string.country_canada)));
        this.mCountryList.add(new CountryCodeAndName("+1345", getString(R.string.country_caymanIslands)));
        this.mCountryList.add(new CountryCodeAndName("+53", getString(R.string.country_cuba)));
        this.mCountryList.add(new CountryCodeAndName("+1767", getString(R.string.country_dominica)));
        this.mCountryList.add(new CountryCodeAndName("+1809", getString(R.string.country_dominicanRepublic)));
        this.mCountryList.add(new CountryCodeAndName("+503", getString(R.string.country_elSalvador)));
        this.mCountryList.add(new CountryCodeAndName("+1473", getString(R.string.country_grenada)));
        this.mCountryList.add(new CountryCodeAndName("+590", getString(R.string.country_guadeloupe)));
        this.mCountryList.add(new CountryCodeAndName("+509", getString(R.string.country_haiti)));
        this.mCountryList.add(new CountryCodeAndName("+1876", getString(R.string.country_jamaica)));
        this.mCountryList.add(new CountryCodeAndName("+011", getString(R.string.country_marieGalante)));
        this.mCountryList.add(new CountryCodeAndName("+596", getString(R.string.country_martinique)));
        this.mCountryList.add(new CountryCodeAndName("+52", getString(R.string.country_mexico)));
        this.mCountryList.add(new CountryCodeAndName("+599", getString(R.string.country_netherlandsAntilles)));
        this.mCountryList.add(new CountryCodeAndName("+505", getString(R.string.country_nicaragua)));
        this.mCountryList.add(new CountryCodeAndName("+507", getString(R.string.country_panama)));
        this.mCountryList.add(new CountryCodeAndName("+1787", getString(R.string.country_puertoRico)));
        this.mCountryList.add(new CountryCodeAndName("+869", getString(R.string.country_saintKittsAndNevis)));
        this.mCountryList.add(new CountryCodeAndName("+721", getString(R.string.country_sintMaarten)));
        this.mCountryList.add(new CountryCodeAndName("+1649", getString(R.string.country_turksAndCaicosIslands)));
        this.mCountryList.add(new CountryCodeAndName("+1", getString(R.string.country_USA)));
        this.mCountryList.add(new CountryCodeAndName("+284", getString(R.string.country_virginIslandsBritish)));
        this.mCountryList.add(new CountryCodeAndName("+1340", getString(R.string.country_virginIslandsUS)));
        this.mCountryList.add(new CountryCodeAndName("+71", getString(R.string.country_australia)));
        this.mCountryList.add(new CountryCodeAndName("+779", getString(R.string.country_fiji)));
        this.mCountryList.add(new CountryCodeAndName("+789", getString(R.string.country_frenchPolynesia)));
        this.mCountryList.add(new CountryCodeAndName("+1671", getString(R.string.country_guam)));
        this.mCountryList.add(new CountryCodeAndName("+787", getString(R.string.country_newCaledonia)));
        this.mCountryList.add(new CountryCodeAndName("+74", getString(R.string.country_newzealand)));
        this.mCountryList.add(new CountryCodeAndName("+780", getString(R.string.country_palau)));
        this.mCountryList.add(new CountryCodeAndName("+775", getString(R.string.country_papuaNewGuinea)));
        this.mCountryList.add(new CountryCodeAndName("+73", getString(R.string.country_philippines)));
        this.mCountryList.add(new CountryCodeAndName("+785", getString(R.string.country_samoa)));
        this.mCountryList.add(new CountryCodeAndName("+776", getString(R.string.country_tonga)));
        this.mCountryList.add(new CountryCodeAndName("+54", getString(R.string.country_argentina)));
        this.mCountryList.add(new CountryCodeAndName("+591", getString(R.string.country_bolivia)));
        this.mCountryList.add(new CountryCodeAndName("+55", getString(R.string.country_brazil)));
        this.mCountryList.add(new CountryCodeAndName("+56", getString(R.string.country_chile)));
        this.mCountryList.add(new CountryCodeAndName("+57", getString(R.string.country_columbia)));
        this.mCountryList.add(new CountryCodeAndName("+593", getString(R.string.country_ecuadoe)));
        this.mCountryList.add(new CountryCodeAndName("+594", getString(R.string.country_frenchGuiana)));
        this.mCountryList.add(new CountryCodeAndName("+592", getString(R.string.country_guyana)));
        this.mCountryList.add(new CountryCodeAndName("+595", getString(R.string.country_paraguay)));
        this.mCountryList.add(new CountryCodeAndName("+51", getString(R.string.country_peru)));
        this.mCountryList.add(new CountryCodeAndName("+597", getString(R.string.country_surinam)));
        this.mCountryList.add(new CountryCodeAndName("+598", getString(R.string.country_uruguay)));
        this.mCountryList.add(new CountryCodeAndName("+58", getString(R.string.country_venezuela)));
    }
}
