package com.android.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.activities.RequestPermissionsActivity;
import com.android.contacts.calllog.PhoneNumberHelper;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyDataListener;
import com.android.contacts.hap.roaming.RoamingDialPadDirectlyManager;
import com.android.contacts.hap.roaming.RoamingLearnManage;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.android.contacts.hap.util.RefelctionUtils;
import com.android.contacts.hap.util.UnsupportedException;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.EncryptCallUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.huawei.android.telephony.MSimSmsManagerEx;
import com.huawei.cust.HwCustUtils;
import java.util.ArrayList;

public class ChooseSubActivity extends Activity {
    private boolean fromMms = false;
    private ChooseSubDialogAdapter mAdapter;
    private OnItemClickListener mChooseItemListener = new OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
            SimViewEntry object = view.getTag();
            if (object instanceof SimViewEntry) {
                SimViewEntry entry = object;
                if (entry.simSlot == -1) {
                    ChooseSubActivity.this.editNumberBeforeCall(entry.finalNumber);
                } else {
                    ChooseSubActivity.this.directDial(entry);
                }
            }
        }
    };
    private EncryptChooseSubActivity mEncryptChooseSubActivity = null;
    private ArrayList<SimViewEntry> mEntries = new ArrayList();
    public AlertDialog mGlobalDialogReference;
    private HwCustChooseSubActivity mHwCust = null;
    private boolean mLearn = false;
    RoamingDialPadDirectlyDataListener roamingDirectlyDataListener = new RoamingDialPadDirectlyDataListener() {
        public void selectedDirectlyData(String number) {
            if (ChooseSubActivity.this.mLearn) {
                RoamingLearnManage.saveRoamingLearnCarrier(ChooseSubActivity.this.getApplicationContext(), ChooseSubActivity.this.getIntent().getData().getSchemeSpecificPart(), number);
            }
        }
    };

    private class ChooseSubDialogAdapter extends BaseAdapter {
        private ChooseSubDialogAdapter() {
        }

        public int getCount() {
            return ChooseSubActivity.this.mEntries.size();
        }

        public Object getItem(int position) {
            if (ChooseSubActivity.this.mEntries.size() - 1 < position || position < 0) {
                return null;
            }
            return ChooseSubActivity.this.mEntries.get(position);
        }

        public long getItemId(int position) {
            if (ChooseSubActivity.this.mEntries.size() - 1 < position || position < 0) {
                return -1;
            }
            SimViewEntry entry = (SimViewEntry) ChooseSubActivity.this.mEntries.get(position);
            if (entry != null) {
                return entry.getId();
            }
            return -1;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            SimViewEntry object = getItem(position);
            if (!(object instanceof SimViewEntry)) {
                return convertView;
            }
            SimViewEntry entry = object;
            if (convertView == null) {
                convertView = ChooseSubActivity.this.getLayoutInflater().inflate(R.layout.choose_sub_dialog_item, null);
            }
            ImageView simIcon = (ImageView) convertView.findViewById(R.id.icon_sim);
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView subTitle = (TextView) convertView.findViewById(R.id.sub_title);
            simIcon.setImageResource(entry.resId);
            if (entry.simSlot == 0) {
                simIcon.setContentDescription(ChooseSubActivity.this.getString(R.string.str_filter_sim1));
            } else if (entry.simSlot == 1) {
                simIcon.setContentDescription(ChooseSubActivity.this.getString(R.string.str_filter_sim2));
            }
            title.setText(entry.title);
            subTitle.setText(entry.subTitle);
            if (entry.recommend) {
                subTitle.setVisibility(0);
            } else {
                subTitle.setVisibility(8);
            }
            convertView.setTag(entry);
            return convertView;
        }
    }

    private class GetLastSlotAndRecommendTask extends AsyncTask<Void, Void, Integer> {
        private Context context;
        private String number;

        public GetLastSlotAndRecommendTask(String number, Context context) {
            this.number = IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number);
            this.context = context;
        }

        protected Integer doInBackground(Void... params) {
            if (EncryptCallUtils.getCust().isEncryptCallEnable()) {
                return Integer.valueOf(CommonUtilMethods.queryLastCallNumberFromCust(this.number, this.context));
            }
            if (HapEncryptCallUtils.isEncryptCallEnabled()) {
                return Integer.valueOf(CommonUtilMethods.queryLastCallNumberFromEncryptCall(this.number, this.context));
            }
            return Integer.valueOf(CommonUtilMethods.queryCallNumberLastSlot(this.number, this.context));
        }

        protected void onPostExecute(Integer result) {
            super.onPostExecute(result);
            ChooseSubActivity.this.updateRecommend(result.intValue());
        }
    }

    protected static class SimViewEntry {
        public String finalNumber = null;
        public long id = -1;
        public boolean isEncrypt = false;
        public boolean recommend = false;
        public int resId = -1;
        public int simSlot = -1;
        public String subTitle = null;
        public String title = null;
        public Uri uri = null;

        public long getId() {
            return this.id;
        }

        public SimViewEntry(int aSimSlot, String aFinalNumber, Uri aUri, String aTitle, String aSubTitle, boolean aRecommend, Long aPosition, int aResId) {
            this.simSlot = aSimSlot;
            this.finalNumber = aFinalNumber;
            this.uri = aUri;
            this.title = aTitle;
            this.subTitle = aSubTitle;
            this.recommend = aRecommend;
            this.id = aPosition.longValue();
            this.resId = aResId;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (RequestPermissionsActivity.startPermissionActivity(this)) {
            finish();
            return;
        }
        try {
            RefelctionUtils.invokeMethod("setHwFloating", getWindow(), new Object[]{Boolean.valueOf(true)});
        } catch (UnsupportedException e) {
            HwLog.e("ChooseSubActivity", "UnsupportedException");
        }
        Intent intent = getIntent();
        if (intent != null && intent.getAction() != null && intent.getData() != null) {
            Uri uri = intent.getData();
            this.mLearn = intent.getBooleanExtra("needlearn", false);
            this.fromMms = intent.getBooleanExtra("fromMms", false);
            String number = uri.getSchemeSpecificPart();
            if (CommonUtilMethods.isEmergencyNumber(number, SimFactoryManager.isDualSim())) {
                CommonUtilMethods.dialNumber(getApplicationContext(), Uri.fromParts("tel", number, null), CommonUtilMethods.getEmergencyNumberSimSlot(number, SimFactoryManager.isDualSim()), true, false);
                finish();
                return;
            }
            number = PhoneNumberFormatter.parsePhoneNumber(number);
            if (number == null) {
                finish();
                return;
            }
            uri = Uri.fromParts("tel", number, null);
            getIntent().setData(uri);
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                this.mHwCust = (HwCustChooseSubActivity) HwCustUtils.createObj(HwCustChooseSubActivity.class, new Object[]{this});
            }
            initEncryptChooseSubActivity();
            boolean isDualSim = SimFactoryManager.isDualSim();
            boolean isFirstSimEnabled = CommonUtilMethods.getFirstSimEnabled();
            boolean isSecondSimEnabled = CommonUtilMethods.getSecondSimEnabled();
            if (isDualSim && isFirstSimEnabled && isSecondSimEnabled) {
                KeyguardManager keyguardManager = (KeyguardManager) getSystemService("keyguard");
                if (keyguardManager != null && keyguardManager.isKeyguardLocked() && keyguardManager.isKeyguardSecure()) {
                    sendBroadcast(new Intent("com.android.internal.policy.impl.PhoneWindowManager.UNLOCKED_KEYGUARD"));
                }
                int defaultSimcard = SimFactoryManager.getDefaultSimcard();
                if (defaultSimcard == -1) {
                    intelligentClassicCall(number, uri);
                } else {
                    extremeSimplicityCall(defaultSimcard, uri, number);
                }
            } else {
                int aSimType = -1;
                if (isDualSim) {
                    if (isFirstSimEnabled) {
                        aSimType = SimFactoryManager.getSlotidBasedOnSubscription(0);
                    } else if (isSecondSimEnabled) {
                        aSimType = SimFactoryManager.getSlotidBasedOnSubscription(1);
                    }
                }
                Uri roamingUri = getRoamingDialNumberUri(uri, number, getIntent(), aSimType);
                if (roamingUri != null) {
                    if (isPopEncryptChooseDialog(this.mEncryptChooseSubActivity)) {
                        this.mEncryptChooseSubActivity.encryptCallChooseDialog(aSimType, roamingUri, this.mLearn, number);
                    } else {
                        chooseSim(aSimType, roamingUri, isDualSim);
                        close();
                    }
                }
            }
        }
    }

    private void initEncryptChooseSubActivity() {
        if (HapEncryptCallUtils.isEncryptCallEnabled()) {
            boolean showEncryptChooseSubActivity = false;
            if (HapEncryptCallUtils.isCdmaBySlot(0)) {
                showEncryptChooseSubActivity = true;
            } else if (HapEncryptCallUtils.isCdmaBySlot(1)) {
                showEncryptChooseSubActivity = true;
            }
            if (showEncryptChooseSubActivity) {
                this.mEncryptChooseSubActivity = new EncryptChooseSubActivity(this);
            }
        }
    }

    private boolean isPopEncryptChooseDialog(EncryptChooseSubActivity mEncryptChooseSubActivity) {
        return mEncryptChooseSubActivity != null && (HapEncryptCallUtils.isCdmaBySlot(0) || HapEncryptCallUtils.isCdmaBySlot(1));
    }

    private Uri getRoamingDialNumberUri(Uri uri, String number, Intent intent, int aSimType) {
        if (number == null || intent == null) {
            return uri;
        }
        boolean isRoamingStatus = IsPhoneNetworkRoamingUtils.isPhoneNetworkRoamging();
        Uri resultUri = null;
        if (isRoamingStatus) {
            if (intent.getBooleanExtra("is_from_yellow", false) && IsPhoneNetworkRoamingUtils.isChinaSIMCard()) {
                String normalized = IsPhoneNetworkRoamingUtils.produectData4ByCountryISO(number, "CN");
                if (!TextUtils.isEmpty(normalized)) {
                    intent.putExtra("EXTRA_NORMALIZED_NUMBER", normalized);
                }
            }
            String roamingNumber = RoamingDialPadDirectlyManager.getDialpadRoamingNumber(this, number, intent, isRoamingStatus, false, this.roamingDirectlyDataListener, aSimType);
            if (!TextUtils.isEmpty(roamingNumber)) {
                resultUri = Uri.fromParts("tel", roamingNumber, null);
            }
        } else {
            resultUri = uri;
        }
        return resultUri;
    }

    private void extremeSimplicityCall(int defaultSimcard, Uri uri, String number) {
        int simSlot = defaultSimcard == 0 ? 0 : 1;
        Uri roamingUri = getRoamingDialNumberUri(uri, number, getIntent(), simSlot);
        if (roamingUri != null) {
            if (this.mEncryptChooseSubActivity == null || !HapEncryptCallUtils.isCdmaBySlot(simSlot)) {
                chooseSim(simSlot, roamingUri, true);
            } else {
                this.mEncryptChooseSubActivity.encryptCallChooseDialog(simSlot, roamingUri, this.mLearn, number);
            }
        }
    }

    private void updateRecommend(int slotId) {
        if (slotId != -1) {
            for (SimViewEntry entry : this.mEntries) {
                if (entry.simSlot == slotId) {
                    entry.recommend = true;
                }
            }
            this.mAdapter.notifyDataSetChanged();
        }
    }

    private void intelligentClassicCall(String number, Uri uri) {
        buildEntries(number, uri);
        this.mAdapter = new ChooseSubDialogAdapter();
        new GetLastSlotAndRecommendTask(number, getApplicationContext()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
        View view = getLayoutInflater().inflate(R.layout.choose_sub_dialog, null);
        ListView list = (ListView) view.findViewById(R.id.choose_sub_list);
        list.setFastScrollEnabled(true);
        list.addHeaderView(new View(getApplicationContext()));
        list.setAdapter(this.mAdapter);
        list.setOnItemClickListener(this.mChooseItemListener);
        Builder builder = new Builder(this);
        builder.setView(view);
        builder.setTitle(getResources().getString(R.string.contacts_call_number, new Object[]{PhoneNumberFormatter.formatNumber(getApplicationContext(), number)}));
        AlertDialog alertDialog = builder.create();
        this.mGlobalDialogReference = alertDialog;
        alertDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                ChooseSubActivity.this.mGlobalDialogReference = null;
                ChooseSubActivity.this.close();
            }
        });
        alertDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                ChooseSubActivity.this.mGlobalDialogReference = null;
                ChooseSubActivity.this.close();
            }
        });
        alertDialog.show();
    }

    private void buildEntries(String number, Uri uri) {
        if (this.mEncryptChooseSubActivity != null) {
            if (this.mEncryptChooseSubActivity.buildSimViewEntry(number, uri, this.mEntries)) {
                return;
            }
        }
        CommonUtilMethods.setSimcardName(getApplicationContext());
        String lOperator1 = CommonUtilMethods.getSim1CardName();
        String lOperator2 = CommonUtilMethods.getSim2CardName();
        String lEditBeforeCall = getResources().getString(R.string.recentCalls_editBeforeCall);
        String lSubTitle = getResources().getString(R.string.choose_sub_recommend);
        SimViewEntry entry0 = new SimViewEntry(0, number, uri, lOperator1, lSubTitle, false, Long.valueOf(0), R.drawable.sim_icon_1);
        SimViewEntry entry1 = new SimViewEntry(1, number, uri, lOperator2, lSubTitle, false, Long.valueOf(1), R.drawable.sim_icon_2);
        this.mEntries.add(entry0);
        this.mEntries.add(entry1);
        Intent intent = getIntent();
        if (intent != null) {
            if (intent.getBooleanExtra("extra_show_edit_before_call", true) && !new PhoneNumberHelper(getResources()).isSipNumber(number)) {
                this.mEntries.add(new SimViewEntry(-1, number, null, lEditBeforeCall, null, false, Long.valueOf(2), R.drawable.contacts_choose_edit));
            }
        }
    }

    private void directDial(SimViewEntry entry) {
        Uri roamingUri = getRoamingDialNumberUri(entry.uri, entry.finalNumber, getIntent(), entry.simSlot);
        if (roamingUri != null) {
            int slot = entry.simSlot;
            if (entry.simSlot == 2) {
                slot = 0;
            } else if (entry.simSlot == 3) {
                slot = 1;
            }
            chooseSim(slot, roamingUri, true, entry.isEncrypt);
        }
        if (entry.simSlot == 0) {
            StatisticalHelper.report(3009);
        } else if (entry.simSlot == 1) {
            StatisticalHelper.report(3010);
        }
    }

    private void editNumberBeforeCall(String number) {
        Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(number));
        intent.setPackage("com.android.contacts");
        StatisticalHelper.report(3011);
        startActivity(intent);
    }

    private void chooseSim(int simSlot, Uri uri, boolean isDualSim) {
        chooseSim(simSlot, uri, isDualSim, false);
    }

    private void chooseSim(int simSlot, Uri uri, boolean isDualSim, boolean isEncrypt) {
        String action = (!QueryUtil.isSystemAppForContacts() || this.fromMms) ? "android.intent.action.CALL" : "android.intent.action.CALL_PRIVILEGED";
        Intent miniIntent = new Intent(action, uri);
        PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, simSlot);
        if (accountHandle != null) {
            miniIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
        }
        if (this.mLearn) {
            RoamingLearnManage.saveRoamingLearnCarrier(getApplicationContext(), getIntent().getData().getSchemeSpecificPart(), uri.getSchemeSpecificPart());
        }
        if (isDualSim) {
            try {
                MSimSmsManagerEx.setSimIdToIntent(miniIntent, simSlot);
            } catch (Exception e) {
                miniIntent.putExtra("subscription", simSlot);
                e.printStackTrace();
            }
        }
        if (isEncrypt) {
            HapEncryptCallUtils.buildEncryptIntent(miniIntent);
        }
        StatisticalHelper.reportDialPortal(getApplicationContext(), 1);
        try {
            startActivity(miniIntent);
        } catch (ActivityNotFoundException e2) {
            Toast.makeText(this, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    protected void close() {
        finish();
    }

    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
        if (this.mGlobalDialogReference != null) {
            this.mGlobalDialogReference.dismiss();
        }
        close();
    }

    public void finish() {
        super.finish();
        overridePendingTransition(0, R.anim.slide_down);
    }
}
