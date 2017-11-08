package com.android.contacts;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.net.Uri;
import android.telecom.PhoneAccountHandle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingLearnManage;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.util.HapEncryptCallUtils;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.huawei.android.telephony.MSimSmsManagerEx;
import java.util.ArrayList;

public class EncryptChooseSubActivity {
    public Context mContext;
    private ArrayList<SimViewEntry> mEntries = new ArrayList();

    final /* synthetic */ class -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl0 implements OnItemClickListener {
        private /* synthetic */ boolean val$aLearn;
        private /* synthetic */ String val$number;
        private /* synthetic */ EncryptChooseSubActivity val$this;

        public /* synthetic */ -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl0(EncryptChooseSubActivity encryptChooseSubActivity, boolean z, String str) {
            this.val$this = encryptChooseSubActivity;
            this.val$aLearn = z;
            this.val$number = str;
        }

        public void onItemClick(AdapterView arg0, View arg1, int arg2, long arg3) {
            this.val$this.-com_android_contacts_EncryptChooseSubActivity_lambda$1(this.val$aLearn, this.val$number, arg0, arg1, arg2, arg3);
        }
    }

    final /* synthetic */ class -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl1 implements OnDismissListener {
        private /* synthetic */ EncryptChooseSubActivity val$this;

        public /* synthetic */ -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl1(EncryptChooseSubActivity encryptChooseSubActivity) {
            this.val$this = encryptChooseSubActivity;
        }

        public void onDismiss(DialogInterface arg0) {
            this.val$this.-com_android_contacts_EncryptChooseSubActivity_lambda$2(arg0);
        }
    }

    final /* synthetic */ class -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl2 implements OnCancelListener {
        private /* synthetic */ EncryptChooseSubActivity val$this;

        public /* synthetic */ -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl2(EncryptChooseSubActivity encryptChooseSubActivity) {
            this.val$this = encryptChooseSubActivity;
        }

        public void onCancel(DialogInterface arg0) {
            this.val$this.-com_android_contacts_EncryptChooseSubActivity_lambda$3(arg0);
        }
    }

    private class ChooseSubDialogAdapter extends BaseAdapter {
        private ChooseSubDialogAdapter() {
        }

        public int getCount() {
            return EncryptChooseSubActivity.this.mEntries.size();
        }

        public Object getItem(int position) {
            if (EncryptChooseSubActivity.this.mEntries.size() - 1 < position || position < 0) {
                return null;
            }
            return EncryptChooseSubActivity.this.mEntries.get(position);
        }

        public long getItemId(int position) {
            if (EncryptChooseSubActivity.this.mEntries.size() - 1 < position || position < 0) {
                return -1;
            }
            SimViewEntry entry = (SimViewEntry) EncryptChooseSubActivity.this.mEntries.get(position);
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
                convertView = ((Activity) EncryptChooseSubActivity.this.mContext).getLayoutInflater().inflate(R.layout.choose_sub_dialog_item, null);
            }
            TextView title = (TextView) convertView.findViewById(R.id.title);
            TextView subTitle = (TextView) convertView.findViewById(R.id.sub_title);
            ((ImageView) convertView.findViewById(R.id.icon_sim)).setImageResource(entry.resId);
            title.setText(entry.title);
            subTitle.setText(entry.subTitle);
            subTitle.setVisibility(entry.recommend ? 0 : 8);
            convertView.setTag(entry);
            return convertView;
        }
    }

    public EncryptChooseSubActivity(Context context) {
        this.mContext = context;
    }

    public boolean buildSimViewEntry(String number, Uri uri, ArrayList<SimViewEntry> mEntries) {
        if (!HapEncryptCallUtils.isEncryptCallEnabled()) {
            return false;
        }
        long longValue;
        String lOperator1 = CommonUtilMethods.getSim1CardName();
        String lOperator2 = CommonUtilMethods.getSim2CardName();
        String lEditBeforeCall = this.mContext.getString(R.string.recentCalls_editBeforeCall);
        String lSubTitle = this.mContext.getString(R.string.choose_sub_recommend);
        String lOperator3 = this.mContext.getString(R.string.encrypt_call);
        Long lPosID = Long.valueOf(0);
        mEntries.add(new SimViewEntry(0, number, uri, lOperator1, lSubTitle, false, lPosID, R.drawable.sim_icon_1));
        if (HapEncryptCallUtils.isCallCard1Encrypt()) {
            longValue = lPosID.longValue() + 1;
            lPosID = Long.valueOf(longValue);
            SimViewEntry entryOne = new SimViewEntry(2, number, uri, lOperator3, lSubTitle, false, Long.valueOf(longValue), R.drawable.ic_contacts_phone1_encryption);
            entryOne.isEncrypt = true;
            mEntries.add(entryOne);
        }
        longValue = lPosID.longValue() + 1;
        lPosID = Long.valueOf(longValue);
        ArrayList<SimViewEntry> arrayList = mEntries;
        arrayList.add(new SimViewEntry(1, number, uri, lOperator2, lSubTitle, false, Long.valueOf(longValue), R.drawable.sim_icon_2));
        if (HapEncryptCallUtils.isCallCard2Encrypt()) {
            longValue = lPosID.longValue() + 1;
            lPosID = Long.valueOf(longValue);
            SimViewEntry entryTwo = new SimViewEntry(3, number, uri, lOperator3, lSubTitle, false, Long.valueOf(longValue), R.drawable.ic_contacts_phone2_encryption);
            entryTwo.isEncrypt = true;
            mEntries.add(entryTwo);
        }
        mEntries.add(new SimViewEntry(-1, number, null, lEditBeforeCall, null, false, lPosID, R.drawable.contacts_choose_edit));
        return true;
    }

    private void updateRecommend(String number) {
        int slotId = CommonUtilMethods.queryLastCallNumberFromEncryptCall(IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number), this.mContext);
        for (SimViewEntry entry : this.mEntries) {
            if (entry.simSlot == slotId) {
                entry.recommend = true;
            }
        }
    }

    public void encryptCallChooseDialog(int slot, Uri uri, boolean aLearn, String number) {
        if (HapEncryptCallUtils.isEncryptCallEnabled()) {
            buildEntries(slot, uri, number);
            updateRecommend(number);
            ChooseSubDialogAdapter mAdapter = new ChooseSubDialogAdapter();
            View view = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.choose_sub_dialog, null);
            ListView list = (ListView) view.findViewById(R.id.choose_sub_list);
            list.addHeaderView(new View(this.mContext));
            list.setAdapter(mAdapter);
            list.setOnItemClickListener(new -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl0(this, aLearn, number));
            Builder builder = new Builder(this.mContext);
            builder.setView(view);
            builder.setTitle(this.mContext.getString(R.string.contacts_call_number, new Object[]{PhoneNumberFormatter.formatNumber(this.mContext, number)}));
            AlertDialog alertDialog = builder.create();
            ((ChooseSubActivity) this.mContext).mGlobalDialogReference = alertDialog;
            alertDialog.setOnDismissListener(new -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl1());
            alertDialog.setOnCancelListener(new -void_encryptCallChooseDialog_int_slot_android_net_Uri_uri_boolean_aLearn_java_lang_String_number_LambdaImpl2());
            alertDialog.show();
        }
    }

    /* synthetic */ void -com_android_contacts_EncryptChooseSubActivity_lambda$1(boolean aLearn, String number, AdapterView parent, View view1, int position, long id) {
        SimViewEntry object = view1.getTag();
        if (object instanceof SimViewEntry) {
            SimViewEntry entry = object;
            AlertDialog aDialog = ((ChooseSubActivity) this.mContext).mGlobalDialogReference;
            if (aDialog != null) {
                aDialog.dismiss();
            }
            if (entry.simSlot == -1) {
                startEditBeforeCall(entry.finalNumber);
            } else {
                chooseSim(entry.simSlot, entry.uri, SimFactoryManager.isDualSim(), entry.isEncrypt, aLearn, number);
            }
        }
    }

    /* synthetic */ void -com_android_contacts_EncryptChooseSubActivity_lambda$2(DialogInterface dialog) {
        ((ChooseSubActivity) this.mContext).mGlobalDialogReference = null;
        ((Activity) this.mContext).finish();
    }

    /* synthetic */ void -com_android_contacts_EncryptChooseSubActivity_lambda$3(DialogInterface dialog) {
        ((ChooseSubActivity) this.mContext).mGlobalDialogReference = null;
        ((Activity) this.mContext).finish();
    }

    private boolean startEditBeforeCall(String number) {
        if (number == null) {
            return false;
        }
        Intent intent = new Intent("android.intent.action.DIAL", CallUtil.getCallUri(number));
        intent.setPackage("com.android.contacts");
        this.mContext.getApplicationContext().startActivity(intent);
        StatisticalHelper.report(2025);
        return true;
    }

    private void chooseSim(int simSlot, Uri uri, boolean isDualSim, boolean isEncrypt, boolean aLearn, String number) {
        Intent miniIntent = new Intent(QueryUtil.isSystemAppForContacts() ? "android.intent.action.CALL_PRIVILEGED" : "android.intent.action.CALL", uri);
        PhoneAccountHandle accountHandle = CallUtil.makePstnPhoneAccountHandleWithPrefix(false, simSlot);
        if (accountHandle != null) {
            miniIntent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE", accountHandle);
        }
        if (aLearn) {
            RoamingLearnManage.saveRoamingLearnCarrier(this.mContext, number, uri.getSchemeSpecificPart());
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
        try {
            this.mContext.startActivity(miniIntent);
        } catch (ActivityNotFoundException e2) {
            Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    private void buildEntries(int slot, Uri uri, String number) {
        CommonUtilMethods.setSimcardName(this.mContext);
        String lOperator1 = "";
        String lOperator2 = this.mContext.getString(R.string.encrypt_call);
        String lSubTitle = this.mContext.getResources().getString(R.string.choose_sub_recommend);
        String lEditBeforeCall = this.mContext.getString(R.string.recentCalls_editBeforeCall);
        int simResID = R.drawable.contact_ic_phone;
        int encryptResID = R.drawable.ic_contacts_encryption;
        int encryptId = -1;
        if (slot == 0) {
            encryptId = 2;
            lOperator1 = CommonUtilMethods.getSim1CardName();
        } else if (slot == 1) {
            encryptId = 3;
            lOperator1 = CommonUtilMethods.getSim2CardName();
        }
        if (SimFactoryManager.isDualSim() && SimFactoryManager.isBothSimEnabled()) {
            if (slot == 0) {
                encryptResID = R.drawable.ic_contacts_phone1_encryption;
                simResID = R.drawable.sim_icon_1;
                encryptId = 2;
            } else {
                encryptResID = R.drawable.ic_contacts_phone2_encryption;
                simResID = R.drawable.sim_icon_2;
                encryptId = 3;
            }
        }
        this.mEntries.add(new SimViewEntry(slot, null, uri, lOperator1, lSubTitle, false, Long.valueOf(0), simResID));
        if (encryptId != -1) {
            SimViewEntry entry1 = new SimViewEntry(encryptId, null, uri, lOperator2, lSubTitle, false, Long.valueOf(1), encryptResID);
            entry1.isEncrypt = true;
            this.mEntries.add(entry1);
        }
        this.mEntries.add(new SimViewEntry(-1, number, null, lEditBeforeCall, null, false, Long.valueOf(2), R.drawable.contacts_choose_edit));
    }
}
