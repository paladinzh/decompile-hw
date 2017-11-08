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
import com.android.contacts.hap.encryptcall.EncryptCallUtils;
import com.android.contacts.hap.roaming.IsPhoneNetworkRoamingUtils;
import com.android.contacts.hap.roaming.RoamingLearnManage;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.util.PhoneNumberFormatter;
import com.google.android.gms.R;
import com.huawei.android.telephony.MSimSmsManagerEx;
import java.util.ArrayList;

public class HwCustChooseSubActivityImpl extends HwCustChooseSubActivity {
    private static final int DEFAULT_VALUE = -1;
    private ArrayList<SimViewEntry> mEntries = new ArrayList();

    private class ChooseSubDialogAdapter extends BaseAdapter {
        private ChooseSubDialogAdapter() {
        }

        public int getCount() {
            return HwCustChooseSubActivityImpl.this.mEntries.size();
        }

        public Object getItem(int position) {
            if (HwCustChooseSubActivityImpl.this.mEntries.size() - 1 < position || position < 0) {
                return null;
            }
            return HwCustChooseSubActivityImpl.this.mEntries.get(position);
        }

        public long getItemId(int position) {
            if (HwCustChooseSubActivityImpl.this.mEntries.size() - 1 < position || position < 0) {
                return -1;
            }
            SimViewEntry entry = (SimViewEntry) HwCustChooseSubActivityImpl.this.mEntries.get(position);
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
                convertView = ((Activity) HwCustChooseSubActivityImpl.this.mContext).getLayoutInflater().inflate(R.layout.choose_sub_dialog_item, null);
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

    public HwCustChooseSubActivityImpl(Context context) {
        super(context);
    }

    public boolean buildSimViewEntry(String number, Uri uri, ArrayList<SimViewEntry> mEntries) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
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
        if (EncryptCallUtils.isCallCard1Encrypt()) {
            longValue = lPosID.longValue() + 1;
            lPosID = Long.valueOf(longValue);
            SimViewEntry entryOne = new SimViewEntry(2, number, uri, lOperator3, lSubTitle, false, Long.valueOf(longValue), R.drawable.encrypt_dial_btn_dual_sim_icon_1);
            entryOne.isEncrypt = true;
            mEntries.add(entryOne);
        }
        longValue = lPosID.longValue() + 1;
        lPosID = Long.valueOf(longValue);
        ArrayList<SimViewEntry> arrayList = mEntries;
        arrayList.add(new SimViewEntry(1, number, uri, lOperator2, lSubTitle, false, Long.valueOf(longValue), R.drawable.sim_icon_2));
        if (EncryptCallUtils.isCallCard2Encrypt()) {
            longValue = lPosID.longValue() + 1;
            lPosID = Long.valueOf(longValue);
            SimViewEntry entryTwo = new SimViewEntry(3, number, uri, lOperator3, lSubTitle, false, Long.valueOf(longValue), R.drawable.encrypt_dial_btn_dual_sim_icon_2);
            entryTwo.isEncrypt = true;
            mEntries.add(entryTwo);
        }
        mEntries.add(new SimViewEntry(-1, number, null, lEditBeforeCall, null, false, lPosID, R.drawable.contacts_choose_edit));
        return true;
    }

    public boolean isCdmaBySlot(int slot) {
        if (!EncryptCallUtils.isEncryptCallEnable()) {
            return false;
        }
        boolean simStatus;
        if (slot == 0) {
            if (SimFactoryManager.isSIM1CardPresent()) {
                simStatus = SimFactoryManager.isSimEnabled(0);
            } else {
                simStatus = false;
            }
        } else if (SimFactoryManager.isSIM2CardPresent()) {
            simStatus = SimFactoryManager.isSimEnabled(1);
        } else {
            simStatus = false;
        }
        if (simStatus) {
            return SimFactoryManager.isCdma(SimFactoryManager.getSubscriptionIdBasedOnSlot(slot));
        }
        return false;
    }

    private void updateRecommend(String number) {
        int slotId = CommonUtilMethods.queryLastCallNumberFromCust(IsPhoneNetworkRoamingUtils.removeDashesAndBlanksBrackets(number), this.mContext);
        for (SimViewEntry entry : this.mEntries) {
            if (entry.simSlot == slotId) {
                entry.recommend = true;
            }
        }
    }

    public void encryptCallChooseDialog(int slot, Uri uri, final boolean aLearn, final String number) {
        buildEntries(slot, uri);
        updateRecommend(number);
        ChooseSubDialogAdapter mAdapter = new ChooseSubDialogAdapter();
        View view = ((Activity) this.mContext).getLayoutInflater().inflate(R.layout.choose_sub_dialog, null);
        ListView list = (ListView) view.findViewById(R.id.choose_sub_list);
        list.addHeaderView(new View(this.mContext));
        list.setAdapter(mAdapter);
        list.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SimViewEntry object = view.getTag();
                if (object instanceof SimViewEntry) {
                    SimViewEntry entry = object;
                    AlertDialog aDialog = ((ChooseSubActivity) HwCustChooseSubActivityImpl.this.mContext).mGlobalDialogReference;
                    if (aDialog != null) {
                        aDialog.dismiss();
                    }
                    HwCustChooseSubActivityImpl.this.chooseSim(entry.simSlot, entry.uri, SimFactoryManager.isDualSim(), entry.isEncrypt, aLearn, number);
                }
            }
        });
        Builder builder = new Builder(this.mContext);
        builder.setView(view);
        builder.setTitle(this.mContext.getString(R.string.contacts_call_number, new Object[]{PhoneNumberFormatter.formatNumber(this.mContext, number)}));
        AlertDialog alertDialog = builder.create();
        ((ChooseSubActivity) this.mContext).mGlobalDialogReference = alertDialog;
        alertDialog.setOnDismissListener(new OnDismissListener() {
            public void onDismiss(DialogInterface dialog) {
                ((ChooseSubActivity) HwCustChooseSubActivityImpl.this.mContext).mGlobalDialogReference = null;
                ((Activity) HwCustChooseSubActivityImpl.this.mContext).finish();
            }
        });
        alertDialog.setOnCancelListener(new OnCancelListener() {
            public void onCancel(DialogInterface dialog) {
                ((ChooseSubActivity) HwCustChooseSubActivityImpl.this.mContext).mGlobalDialogReference = null;
                ((Activity) HwCustChooseSubActivityImpl.this.mContext).finish();
            }
        });
        alertDialog.show();
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
            EncryptCallUtils.buildEncryptIntent(miniIntent);
        }
        try {
            this.mContext.startActivity(miniIntent);
        } catch (ActivityNotFoundException e2) {
            Toast.makeText(this.mContext, R.string.quickcontact_missing_app_Toast, 0).show();
        }
    }

    private void buildEntries(int slot, Uri uri) {
        CommonUtilMethods.setSimcardName(this.mContext);
        String lOperator1 = CommonUtilMethods.getSim1CardName();
        String lOperator2 = this.mContext.getString(R.string.encrypt_call);
        String lSubTitle = this.mContext.getResources().getString(R.string.choose_sub_recommend);
        int simResID = R.drawable.contacts_call_normal;
        int encryptResID = R.drawable.encrypt_dial_button_single_sim_icon;
        int encryptId = -1;
        if (SimFactoryManager.isDualSim()) {
            if (slot == 0) {
                encryptResID = R.drawable.encrypt_dial_btn_dual_sim_icon_1;
                simResID = R.drawable.sim_icon_1;
                encryptId = 2;
            } else {
                encryptResID = R.drawable.encrypt_dial_btn_dual_sim_icon_2;
                simResID = R.drawable.sim_icon_2;
                encryptId = 3;
            }
        }
        this.mEntries.add(new SimViewEntry(slot, null, uri, lOperator1, lSubTitle, false, Long.valueOf(0), simResID));
        SimViewEntry entry1 = new SimViewEntry(encryptId, null, uri, lOperator2, lSubTitle, false, Long.valueOf(1), encryptResID);
        entry1.isEncrypt = true;
        this.mEntries.add(entry1);
    }
}
