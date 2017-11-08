package com.android.contacts.hap.rcs.detail;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings.System;
import android.provider.Telephony.Threads;
import android.telephony.PhoneNumberUtils;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.Collapser;
import com.android.contacts.activities.ContactInfoFragment;
import com.android.contacts.detail.ContactDetailAdapter;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.ViewEntry;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.model.Contact;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.IfMsgplusCb;
import com.huawei.rcs.commonInterface.IfMsgplusCb.Stub;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import com.huawei.rcs.util.RCSConst;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;

public class RcsContactDetailFragmentHelper {
    private ArrayList mCallEntry;
    private CapabilityService mCapabilityService;
    private AlertDialog mFTDialog = null;
    private ArrayList<DetailViewEntry> mFTEntries = new ArrayList();
    private ContactDetailFragment mFragment;
    private AlertDialog mIMDialog = null;
    private boolean mIsFTDialogVisible = false;
    private String mPhoneNumber = null;
    private IfMsgplusCb mRcsCallback = new Stub() {
        public void handleEvent(int wEvent, Bundle bundle) {
            Message msg = RcsContactDetailFragmentHelper.this.mRcseEventHandler.obtainMessage(wEvent);
            msg.obj = bundle;
            RcsContactDetailFragmentHelper.this.mRcseEventHandler.sendMessage(msg);
        }
    };
    private Handler mRcseEventHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            int status = msg.what;
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailFragmentHelper", " handleMessage " + msg.what);
            }
            switch (status) {
                case 1501:
                    RcsContactDetailFragmentHelper.this.updateCapability(msg);
                    return;
                default:
                    return;
            }
        }
    };
    private LoginStatusReceiver statusReceiver = null;

    public class LoginStatusReceiver extends BroadcastReceiver {
        public void onReceive(Context context, Intent intent) {
            boolean z = true;
            if (intent.getExtras() != null) {
                int newStatus = intent.getExtras().getInt("new_status");
                if (RcsContactDetailFragmentHelper.this.mCallEntry != null && 1 == newStatus) {
                    RcsContactDetailFragmentHelper.this.sendRcsQuestCapability(RcsContactDetailFragmentHelper.this.mCallEntry);
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactDetailFragmentHelper", "login status changed,sendRcsQuestCapability,login status =" + newStatus);
                    }
                }
                if (RcsContactDetailFragmentHelper.this.mFragment == null) {
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactDetailFragmentHelper", "mFragment = null");
                    }
                    return;
                }
                ContactDetailAdapter adapter = RcsContactDetailFragmentHelper.this.mFragment.getDetailAdapter();
                if (adapter != null && RcsContactDetailFragmentHelper.this.mFragment.getContext() != null) {
                    if (1 != newStatus) {
                        z = false;
                    }
                    adapter.updateContactDetailUIOnRcsStatusChanged(z);
                    adapter.notifyDataSetChanged();
                } else if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailFragmentHelper", "login state changed, Adapter = null or mContext = null");
                }
            }
        }
    }

    private class NumberSelectionAdapter extends BaseAdapter {
        private ContactDetailFragment mFragment;
        private ArrayList<DetailViewEntry> mNumberInfoList;

        private class ViewHolder {
            TextView label;
            TextView number;

            private ViewHolder() {
            }
        }

        public NumberSelectionAdapter(ContactDetailFragment fragment, ArrayList<DetailViewEntry> filterEntries) {
            this.mFragment = fragment;
            this.mNumberInfoList = filterEntries;
        }

        public int getCount() {
            return this.mNumberInfoList.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            if (position >= this.mNumberInfoList.size()) {
                return null;
            }
            ViewHolder holder;
            if (convertView == null) {
                convertView = LayoutInflater.from(this.mFragment.getContext()).inflate(R.layout.contact_phone_number_selection_item, null);
                holder = new ViewHolder();
                holder.number = (TextView) convertView.findViewById(R.id.contacts_phone_number);
                holder.label = (TextView) convertView.findViewById(R.id.contacts_phone_label);
                convertView.setTag(holder);
            }
            holder = (ViewHolder) convertView.getTag();
            holder.number.setText(((DetailViewEntry) this.mNumberInfoList.get(position)).data);
            holder.label.setText(((DetailViewEntry) this.mNumberInfoList.get(position)).typeString);
            convertView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    ViewHolder holder = (ViewHolder) view.getTag();
                    NumberSelectionAdapter.this.mFragment.setSmsDialogVisibleStatus(false);
                    RcsContactDetailFragmentHelper.this.mIsFTDialogVisible = false;
                    Fragment fr = NumberSelectionAdapter.this.mFragment.getFragment();
                    if (fr != null && (fr instanceof ContactInfoFragment)) {
                        ContactInfoFragment cif = (ContactInfoFragment) fr;
                        if (cif.getRcsCust() != null) {
                            cif.getRcsCust().setPersonToSendFile(holder.number.getText().toString());
                        }
                    }
                    RcsContactDetailFragmentHelper.sendFileByRcs(NumberSelectionAdapter.this.mFragment.getContext(), holder.number.getText().toString());
                }
            });
            return convertView;
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public Object getItem(int position) {
            if (position >= this.mNumberInfoList.size()) {
                return null;
            }
            return this.mNumberInfoList.get(position);
        }
    }

    public void setOtherDiaglogVisibleFlag(Bundle bundle) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailFragmentHelper", "setOtherDiaglogVisibleFlag");
            }
            this.mIsFTDialogVisible = bundle.getBoolean("FTDialog");
        }
    }

    public void setOutStatusForOtherEntry(Bundle bundle, String entry, DetailViewEntry entryView) {
        bundle.putBoolean("FTDialog", this.mIsFTDialogVisible);
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDetailFragmentHelper", "setOutStatusForOtherEntry mIsFTDialogVisible = " + this.mIsFTDialogVisible);
        }
        if (this.mIsFTDialogVisible) {
            bundle.putParcelable(entry, entryView);
        }
    }

    public void setOtherEntryViewData() {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            Collapser.collapseList(this.mFTEntries);
        }
    }

    public void addOtherEntry(ContactDetailAdapter adapter, ArrayList<DetailViewEntry> callEntries, ArrayList<ViewEntry> allEntries) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && callEntries != null && adapter != null) {
            adapter.initRCSCapabityquest();
            String number = "";
            for (int i = 0; i < callEntries.size(); i++) {
                adapter.addPhoneNum(((DetailViewEntry) callEntries.get(i)).data);
            }
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailFragmentHelper", "FT entry in addOtherEntry");
            }
            adapter.addOtherEntry("file_transfer_entry_type", allEntries);
        }
    }

    public void getOtherDialog(ArrayList<DetailViewEntry> callEntries, DetailViewEntry aEntry, ContactDetailFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && callEntries != null && aEntry != null && fragment != null && this.mIsFTDialogVisible && this.mFTDialog != null && !this.mFTDialog.isShowing()) {
            getFTDialog(callEntries, aEntry, fragment);
        }
    }

    public boolean checkOtherEntry(DetailViewEntry selectedEntry, RcsContactDetailAdapter hwCustDetAdapter) {
        if (selectedEntry == null || hwCustDetAdapter == null || !EmuiFeatureManager.isRcsFeatureEnable() || (!hwCustDetAdapter.isFileTransferEntry(selectedEntry.mimetype, selectedEntry.mCustom_mimetype) && !hwCustDetAdapter.isIMTransferEntry(selectedEntry.mimetype, selectedEntry.mCustom_mimetype))) {
            return false;
        }
        return true;
    }

    public void setFlattenListForOtherEntry(ContactDetailFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && fragment != null) {
            fragment.flattenList(this.mFTEntries);
        }
    }

    private void updateCapability(Message msg) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || msg == null || this.mFragment == null || this.mFragment.getActivity() == null)) {
            Bundle bundle = msg.obj;
            String phoneNumber = "";
            Capabilities capabilities = null;
            try {
                bundle.setClassLoader(Capabilities.class.getClassLoader());
                phoneNumber = bundle.getString("phonenumber");
                capabilities = (Capabilities) bundle.getParcelable("capabilitiesclass");
                if (capabilities != null) {
                    HwLog.i("RcsContactDetailFragmentHelper", "issupportHTTP: " + capabilities.istFtViaHttpSupported() + " isPreCallSupported: " + capabilities.isPreCallSupported() + " isOnLine: " + capabilities.isOnLine());
                }
            } catch (Exception e) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailFragmentHelper", " updateCapability " + e.toString());
                }
            }
            if (!phoneNumber.isEmpty()) {
                boolean isResponse = false;
                String number = "";
                String oriNumber = "";
                for (int i = 0; i < this.mFragment.getIPCallEntries().size(); i++) {
                    number = ((DetailViewEntry) this.mFragment.getIPCallEntries().get(i)).data;
                    oriNumber = number;
                    number = PhoneNumberUtils.normalizeNumber(number);
                    if (this.mCapabilityService != null && this.mCapabilityService.compareUri(phoneNumber, number)) {
                        isResponse = true;
                        break;
                    }
                }
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailFragmentHelper", " start updateCapability ");
                }
                if (isResponse && capabilities != null) {
                    ContactDetailAdapter adapter = this.mFragment.getDetailAdapter();
                    adapter.updateFTCapInAdapter(capabilities.isFileTransferSupported(), number);
                    adapter.updateCapMap(oriNumber, capabilities.isOnLine(), capabilities.isPreCallSupported(), number);
                    adapter.notifyDataSetChanged();
                }
            }
        }
    }

    public void handleCustomizationsOnDestroy(Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.statusReceiver != null && context != null && this.mCapabilityService != null) {
            context.unregisterReceiver(this.statusReceiver);
            this.mCapabilityService.removeRcsCallBack(Integer.valueOf(1501), this.mRcsCallback);
        }
    }

    public void sendRcsQuestCapability(ArrayList<DetailViewEntry> callEntries) {
        Contact contactData = null;
        if (this.mFragment != null) {
            contactData = this.mFragment.getContactData();
        }
        if (EmuiFeatureManager.isRcsFeatureEnable() && callEntries != null && this.mCapabilityService != null && (r0 == null || !r0.isUserProfile())) {
            this.mCallEntry = callEntries;
            String number = "";
            for (int i = 0; i < callEntries.size(); i++) {
                this.mCapabilityService.sendRequestContactCapabilities(PhoneNumberUtils.normalizeNumber(((DetailViewEntry) callEntries.get(i)).data));
            }
        }
    }

    public void setSelectedPhoneNumber(String phoneNumber) {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            this.mPhoneNumber = phoneNumber;
        }
    }

    public void handleCustomizationsOnCreate(ContactDetailFragment fragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && fragment != null) {
            this.mFragment = fragment;
            registerRCSReceiver(fragment.getContext());
            if (this.mCapabilityService == null) {
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            if (this.mCapabilityService != null) {
                this.mCapabilityService.setRcsCallBack(Integer.valueOf(1501), this.mRcsCallback);
                this.mCapabilityService.checkRcsServiceBind();
            }
        }
    }

    public void getFTDialog(ArrayList<DetailViewEntry> callEntries, DetailViewEntry aEntry, ContactDetailFragment fragment) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || callEntries == null || aEntry == null || fragment == null)) {
            fragment.setIPEntry(aEntry);
            HashMap<Integer, String> numbersDetails = new HashMap();
            Builder builder = new Builder(fragment.getActivity());
            int mPrimarypos = -1;
            ArrayList supportFileList = null;
            ContactDetailAdapter contactDetailAdapter = fragment.getDetailAdapter();
            if (!(contactDetailAdapter == null || contactDetailAdapter.getRcsContactDetailAdapter() == null)) {
                supportFileList = contactDetailAdapter.getRcsContactDetailAdapter().getSupportFileTransferList();
            }
            if (supportFileList != null) {
                int i;
                ArrayList<DetailViewEntry> filterEntries = new ArrayList();
                for (i = 0; i < callEntries.size(); i++) {
                    if (supportFileList.contains(PhoneNumberUtils.normalizeNumber(((DetailViewEntry) callEntries.get(i)).data))) {
                        filterEntries.add((DetailViewEntry) callEntries.get(i));
                    }
                }
                int entriesSize = filterEntries.size();
                for (i = 0; i < entriesSize; i++) {
                    if (((DetailViewEntry) filterEntries.get(i)).isPrimary) {
                        mPrimarypos = i;
                    }
                    numbersDetails.put(Integer.valueOf(i), ((DetailViewEntry) filterEntries.get(i)).data);
                }
                if (entriesSize == 1) {
                    sendRcsFile(fragment, (String) numbersDetails.get(Integer.valueOf(0)));
                } else if (mPrimarypos >= 0) {
                    sendRcsFile(fragment, (String) numbersDetails.get(Integer.valueOf(mPrimarypos)));
                } else {
                    builder.setTitle(fragment.getActivity().getString(R.string.rcs_menu_group_send_file));
                    View custView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.favorites_select_number_dialog_view, null, false);
                    View bottomDivider = custView.findViewById(R.id.bottom_divider);
                    if (bottomDivider != null) {
                        bottomDivider.setVisibility(8);
                    }
                    View checkbox = custView.findViewById(R.id.set_to_default_check);
                    if (checkbox != null) {
                        checkbox.setVisibility(8);
                    }
                    ListView list = (ListView) custView.findViewById(R.id.contact_number_select_list);
                    list.setAdapter(new NumberSelectionAdapter(fragment, filterEntries));
                    list.setFastScrollEnabled(true);
                    builder.setView(custView);
                    fragment.setSmsDialogVisibleStatus(true);
                    this.mIsFTDialogVisible = true;
                    this.mFTDialog = builder.create();
                    final ContactDetailFragment contactDetailFragment = fragment;
                    this.mFTDialog.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            contactDetailFragment.setSmsDialogVisibleStatus(false);
                            RcsContactDetailFragmentHelper.this.mIsFTDialogVisible = false;
                        }
                    });
                    this.mFTDialog.show();
                }
            }
        }
    }

    public void getIMDialog(ArrayList<DetailViewEntry> callEntries, DetailViewEntry aEntry, ContactDetailFragment fragment) {
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || callEntries == null || aEntry == null || fragment == null)) {
            fragment.setIPEntry(aEntry);
            HashMap<Integer, String> numbersDetails = new HashMap();
            Builder builder = new Builder(fragment.getActivity());
            int mPrimarypos = -1;
            ArrayList supportFileList = null;
            ContactDetailAdapter contactDetailAdapter = fragment.getDetailAdapter();
            if (!(contactDetailAdapter == null || contactDetailAdapter.getRcsContactDetailAdapter() == null)) {
                supportFileList = contactDetailAdapter.getRcsContactDetailAdapter().getSupportFileTransferList();
            }
            if (supportFileList != null) {
                int i;
                final ArrayList<DetailViewEntry> filterEntries = new ArrayList();
                for (i = 0; i < callEntries.size(); i++) {
                    if (supportFileList.contains(PhoneNumberUtils.normalizeNumber(((DetailViewEntry) callEntries.get(i)).data))) {
                        filterEntries.add((DetailViewEntry) callEntries.get(i));
                    }
                }
                int entriesSize = filterEntries.size();
                CharSequence[] items = new CharSequence[entriesSize];
                for (i = 0; i < entriesSize; i++) {
                    SpannableStringBuilder stringBuilder = new SpannableStringBuilder();
                    if (Locale.getDefault().getLanguage() == null || !CommonUtilMethods.isLayoutRTL()) {
                        stringBuilder.append(((DetailViewEntry) filterEntries.get(i)).data);
                    } else {
                        stringBuilder.append("‎" + ((DetailViewEntry) filterEntries.get(i)).data);
                    }
                    stringBuilder.append('\n');
                    int indexToBeSpanned = stringBuilder.length();
                    stringBuilder.append(((DetailViewEntry) filterEntries.get(i)).typeString);
                    stringBuilder.setSpan(new RelativeSizeSpan(0.7f), indexToBeSpanned, stringBuilder.length(), 33);
                    stringBuilder.setSpan(new ForegroundColorSpan(fragment.getActivity().getResources().getColor(R.color.contact_list_item_sub_text_color)), indexToBeSpanned, stringBuilder.length(), 33);
                    items[i] = stringBuilder;
                    if (((DetailViewEntry) filterEntries.get(i)).isPrimary) {
                        mPrimarypos = i;
                    }
                    numbersDetails.put(Integer.valueOf(i), ((DetailViewEntry) filterEntries.get(i)).data);
                }
                if (entriesSize == 1) {
                    sendIMToMessage(((DetailViewEntry) filterEntries.get(0)).data, fragment);
                } else if (mPrimarypos >= 0) {
                    sendIMToMessage((String) numbersDetails.get(Integer.valueOf(mPrimarypos)), fragment);
                } else {
                    final ContactDetailFragment contactDetailFragment = fragment;
                    DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            contactDetailFragment.setSmsDialogVisibleStatus(false);
                            RcsContactDetailFragmentHelper.this.mIsFTDialogVisible = false;
                            RcsContactDetailFragmentHelper.this.sendIMToMessage(((DetailViewEntry) filterEntries.get(which)).data, contactDetailFragment);
                        }
                    };
                    builder.setTitle(fragment.getActivity().getString(R.string.rcs_contact_menu_send_im));
                    builder.setSingleChoiceItems(items, -1, clickListener);
                    fragment.setSmsDialogVisibleStatus(true);
                    this.mIsFTDialogVisible = true;
                    this.mIMDialog = builder.create();
                    final ContactDetailFragment contactDetailFragment2 = fragment;
                    this.mIMDialog.setOnDismissListener(new OnDismissListener() {
                        public void onDismiss(DialogInterface dialog) {
                            contactDetailFragment2.setSmsDialogVisibleStatus(false);
                            RcsContactDetailFragmentHelper.this.mIsFTDialogVisible = false;
                        }
                    });
                    this.mIMDialog.show();
                }
            }
        }
    }

    private void sendIMToMessage(String number, ContactDetailFragment fragment) {
        Intent smsIntent = createOnlyImIntent(fragment.getActivity(), 0);
        smsIntent.setData(Uri.fromParts("smsto", number, null));
        fragment.getContext().startActivity(smsIntent);
    }

    private void registerRCSReceiver(Context context) {
        if (this.statusReceiver == null) {
            this.statusReceiver = new LoginStatusReceiver();
        }
        IntentFilter statusFilter = new IntentFilter();
        statusFilter.addAction("com.huawei.rcs.loginstatus");
        if (context != null) {
            context.registerReceiver(this.statusReceiver, statusFilter, "com.huawei.rcs.RCS_BROADCASTER", null);
        }
    }

    private Uri getRcsUri(long threadId, int threadType, boolean isMix) {
        if (1 != threadType) {
            return ContentUris.withAppendedId(RCSConst.RCS_URI_CONVERSATIONS, threadId).buildUpon().appendQueryParameter("threadType", String.valueOf(threadType)).build();
        }
        return ContentUris.withAppendedId(Threads.CONTENT_URI, threadId);
    }

    private Intent createOnlyImIntent(Context context, long threadId) {
        Intent intent = new Intent();
        intent.setClassName(context, "com.android.mms.ui.ComposeMessageActivity");
        if (threadId > 0) {
            intent.setData(getRcsUri(threadId, 2, getIMThreadDisplayMergeStatus(context)));
        }
        intent.putExtra("conversation_mode", 2);
        return intent;
    }

    private boolean getIMThreadDisplayMergeStatus(Context context) {
        int mergeIMStatus = 0;
        try {
            mergeIMStatus = System.getInt(context.getContentResolver(), "im_thread_display_switcher", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (mergeIMStatus == 1) {
            return true;
        }
        return false;
    }

    private void sendRcsFile(ContactDetailFragment fragment, String personToSendFile) {
        Fragment fr = fragment.getFragment();
        if (fr != null && (fr instanceof ContactInfoFragment)) {
            ContactInfoFragment cif = (ContactInfoFragment) fr;
            if (cif.getRcsCust() != null) {
                cif.getRcsCust().setPersonToSendFile(personToSendFile);
            }
        }
        sendFileByRcs(fragment.getContext(), personToSendFile);
    }

    private static void sendFileByRcs(Context context, String personToSendFile) {
        if (context instanceof Activity) {
            Intent chatIntent = new Intent("android.intent.action.SENDTO", Uri.parse("smsto:" + personToSendFile));
            chatIntent.putExtra("SEND_FILE", "RCS_SEND_FILE");
            chatIntent.putExtra("Contacts", "RCS_FT");
            chatIntent.putExtra("ADDRESS", personToSendFile);
            chatIntent.putExtra("send_mode", 1);
            chatIntent.putExtra("force_set_send_mode", true);
            context.startActivity(chatIntent);
        }
    }

    public void closeFTDialog() {
        if (this.mFTDialog != null) {
            this.mFTDialog.dismiss();
        }
    }
}
