package com.android.contacts.hap.rcs.detail;

import android.content.Context;
import android.provider.Settings.System;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.widget.ImageView;
import com.android.contacts.detail.ContactDetailAdapter;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewCache;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.ViewEntry;
import com.android.contacts.detail.ContactDetailFragment;
import com.android.contacts.hap.CommonConstants;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.rcs.RcsContactsUtils;
import com.android.contacts.hap.rcs.RcseProfile;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.android.contacts.model.Contact;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.DialerHighlighter;
import com.huawei.rcs.capability.CapabilityService;
import com.huawei.rcs.commonInterface.metadata.Capabilities;
import java.util.ArrayList;
import java.util.HashMap;

public class RcsContactDetailAdapter {
    private CapabilityService mCapabilityService;
    private int mFileIndex = -1;
    private int mIMIndex = -1;
    private HashMap<String, Integer> mNumberCapMap = new HashMap();
    private ArrayList<String> mSupportFileTransferList = new ArrayList();

    private static class FtCallListener implements OnClickListener {
        private DetailViewEntry aEntry;
        private ContactDetailFragment detailFragment;

        public FtCallListener(DetailViewEntry aEntry, ContactDetailFragment detailFragment) {
            this.aEntry = aEntry;
            this.detailFragment = detailFragment;
        }

        public void onClick(View v) {
            RcsContactDetailFragmentHelper deltailFragmentHelper = this.detailFragment.getRcsCust();
            if (deltailFragmentHelper != null) {
                deltailFragmentHelper.getFTDialog(this.detailFragment.getIPCallEntries(), this.aEntry, this.detailFragment);
            }
        }
    }

    private static class ImCallListener implements OnClickListener {
        private DetailViewEntry aEntry;
        private ContactDetailFragment detailFragment;

        public ImCallListener(DetailViewEntry aEntry, ContactDetailFragment detailFragment) {
            this.aEntry = aEntry;
            this.detailFragment = detailFragment;
        }

        public void onClick(View v) {
            RcsContactDetailFragmentHelper deltailFragmentHelper = this.detailFragment.getRcsCust();
            if (deltailFragmentHelper != null) {
                deltailFragmentHelper.getIMDialog(this.detailFragment.getIPCallEntries(), this.aEntry, this.detailFragment);
            }
        }
    }

    private static class RunnableEx implements Runnable {
        private ContactDetailAdapter mAdapter;

        public RunnableEx(ContactDetailAdapter adapter) {
            this.mAdapter = adapter;
        }

        public void run() {
            if (this.mAdapter != null) {
                this.mAdapter.notifyDataSetChanged();
            }
        }
    }

    public ArrayList<String> getSupportFileTransferList() {
        return this.mSupportFileTransferList;
    }

    public void setVisiblityForOtherEntry(DetailViewCache aViews, DetailViewEntry aEntry, View view, ContactDetailFragment detailFragment) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && aViews != null && aEntry != null && detailFragment != null) {
            if (isFileTransferEntry(aEntry.mimetype, aEntry.mCustom_mimetype)) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailAdapter", "setVisiblityForFTEntry in setVisiblityForOtherEntry");
                }
                setVisiblityForFileTransferEntry(aViews, aEntry, view, detailFragment);
            } else if (isIMTransferEntry(aEntry.mimetype, aEntry.mCustom_mimetype)) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailAdapter", "setVisiblityForIMTransferEntry in setVisiblityForOtherEntry");
                }
                setVisiblityForIMTransferEntry(aViews, aEntry, view, detailFragment);
            }
        }
    }

    public void initForCustomizations(Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && this.mCapabilityService == null && context != null) {
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", "new capabilityService in HwCustContactDetailAdapterImpl");
            }
            this.mCapabilityService = CapabilityService.getInstance("contacts");
        }
        if (this.mCapabilityService != null) {
            this.mCapabilityService.checkRcsServiceBind();
        }
    }

    public boolean isFileTransferEntry(String mimeType, String customMimeType) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && "vnd.android.cursor.item/phone_v2".equals(mimeType)) {
            return "RCS_FT".equals(customMimeType);
        }
        return false;
    }

    public boolean isIMTransferEntry(String mimeType, String customMimeType) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && "vnd.android.cursor.item/phone_v2".equals(mimeType)) {
            return "RCS_IM".equals(customMimeType);
        }
        return false;
    }

    public void addOtherEntry(Context context, ArrayList<ViewEntry> allEntries, Contact contactData, String entryType, ContactDetailFragment fragment, ContactDetailAdapter adapter) {
        int lEntryToBeAddedAtIndex = -1;
        if (!(!EmuiFeatureManager.isRcsFeatureEnable() || context == null || allEntries == null || fragment == null || adapter == null)) {
            CharSequence lPrevMimeType = null;
            String lCurMimeType = null;
            boolean isPhoneNumberExisted = false;
            int lSize = allEntries.size();
            int i = 0;
            while (i < lSize) {
                ViewEntry lViewEntry = (ViewEntry) allEntries.get(i);
                if (!lViewEntry.isDetailViewEntry()) {
                    if (lViewEntry.isKindTitleViewEntry() && !TextUtils.isEmpty(r10)) {
                        lEntryToBeAddedAtIndex = i;
                        break;
                    }
                }
                if (lViewEntry instanceof DetailViewEntry) {
                    lCurMimeType = ((DetailViewEntry) lViewEntry).mimetype;
                    lEntryToBeAddedAtIndex = i + 1;
                    if (HwLog.HWDBG) {
                        HwLog.i("RcsContactDetailAdapter", "lEntryToBeAddedAtIndex=" + lEntryToBeAddedAtIndex);
                    }
                }
                if ("vnd.android.cursor.item/phone_v2".equals(lCurMimeType)) {
                    lPrevMimeType = lCurMimeType;
                    isPhoneNumberExisted = true;
                }
                if (TextUtils.isEmpty(lPrevMimeType) || lPrevMimeType.equals(lCurMimeType)) {
                    if (contactData != null && contactData.isUserProfile() && i == lSize - 1 && !TextUtils.isEmpty(lPrevMimeType)) {
                        lEntryToBeAddedAtIndex = lSize;
                        break;
                    }
                }
                lEntryToBeAddedAtIndex = i;
                break;
                i++;
            }
            if ("file_transfer_entry_type".equals(entryType) && isPhoneNumberExisted) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailAdapter", "addFTEntry: lEntryToBeAddedAtIndex=" + lEntryToBeAddedAtIndex);
                }
                if (contactData == null || !contactData.isUserProfile()) {
                    addFTEntry(context, allEntries, adapter, fragment, lEntryToBeAddedAtIndex);
                }
            }
        }
    }

    private void setVisiblityForFileTransferEntry(DetailViewCache aViews, DetailViewEntry aEntry, View view, ContactDetailFragment detailFragment) {
        OnClickListener fTCallListener = new FtCallListener(aEntry, detailFragment);
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDetailAdapter", "In setVisiblityForFileTransferEntry aEntry.isEnabled= " + aEntry.isEnabled);
        }
        int srcColor = -16777216;
        if (aViews.actionsViewContainer.getContext() != null) {
            srcColor = aViews.actionsViewContainer.getContext().getResources().getColor(R.color.contact_detail_data_text_color);
        }
        if (aEntry.isEnabled) {
            aViews.actionsViewContainer.setEnabled(true);
            aViews.data.setEnabled(true);
            aViews.actionsViewContainer.setOnClickListener(fTCallListener);
            aViews.data.setTextColor(srcColor);
            if (aViews.mPrimaryActionButton != null) {
                aViews.mPrimaryActionButton.setEnabled(true);
                aViews.mPrimaryActionButton.setOnClickListener(fTCallListener);
            }
            aViews.mIcon = (ImageView) view.findViewById(R.id.icon);
            if (aViews.mIcon != null) {
                aViews.mIcon.setVisibility(0);
                aViews.mIcon.setImageResource(R.drawable.btn_detail_normal);
                return;
            }
            return;
        }
        aViews.actionsViewContainer.setEnabled(false);
        aViews.data.setEnabled(false);
        aViews.data.setTextColor(ImmersionUtils.getColorWithAlpha(srcColor, 25));
        if (aViews.mPrimaryActionButton != null) {
            aViews.mPrimaryActionButton.setEnabled(false);
        }
    }

    private void setVisiblityForIMTransferEntry(DetailViewCache aViews, DetailViewEntry aEntry, View view, ContactDetailFragment detailFragment) {
        OnClickListener imCallListener = new ImCallListener(aEntry, detailFragment);
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDetailAdapter", "In setVisiblityForIMEntry aEntry.isEnabled= " + aEntry.isEnabled);
        }
        int srcColor = -16777216;
        if (aViews.actionsViewContainer.getContext() != null) {
            srcColor = aViews.actionsViewContainer.getContext().getResources().getColor(R.color.contact_detail_data_text_color);
        }
        if (aEntry.isEnabled) {
            aViews.actionsViewContainer.setEnabled(true);
            aViews.data.setEnabled(true);
            aViews.data.setTextColor(srcColor);
            aViews.actionsViewContainer.setOnClickListener(imCallListener);
            if (aViews.mPrimaryActionButton != null) {
                aViews.mPrimaryActionButton.setEnabled(true);
                aViews.mPrimaryActionButton.setOnClickListener(imCallListener);
            }
            aViews.mIcon = (ImageView) view.findViewById(R.id.icon);
            if (aViews.mIcon != null) {
                aViews.mIcon.setVisibility(0);
                aViews.mIcon.setImageResource(R.drawable.btn_detail_normal);
                return;
            }
            return;
        }
        aViews.actionsViewContainer.setEnabled(false);
        aViews.data.setEnabled(false);
        aViews.data.setTextColor(ImmersionUtils.getColorWithAlpha(srcColor, 25));
        if (aViews.mPrimaryActionButton != null) {
            aViews.mPrimaryActionButton.setEnabled(false);
        }
    }

    public void setRcsViewVisibility(boolean isPhoneItem, View view, DetailViewEntry entry, Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && view != null && entry != null && RcsContactsUtils.isRCSContactIconEnable()) {
            if (this.mCapabilityService == null && context != null) {
                if (HwLog.HWDBG) {
                    HwLog.d("RcsContactDetailAdapter", "newCapabilityService");
                }
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            if (isPhoneItem && entry.mCustom_mimetype == null) {
                ViewStub vs = (ViewStub) view.findViewById(R.id.rcs_status_phone);
                String Num = DialerHighlighter.cleanNumber(entry.data, false);
                if (!(Num == null || vs == null)) {
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactDetailAdapter", "rcs view");
                    }
                    if (this.mCapabilityService.isRCSContact(Num)) {
                        vs.setLayoutResource(R.layout.rcs_viewstub_core_notif_on_icon);
                        vs.inflate();
                        vs.setVisibility(0);
                    } else {
                        vs.setVisibility(8);
                    }
                }
                if (Num != null && vs == null) {
                    ImageView rcsIcon = (ImageView) view.findViewById(R.id.rcs_icon);
                    if (rcsIcon == null) {
                        return;
                    }
                    if (this.mCapabilityService.isRCSContact(Num)) {
                        rcsIcon.setVisibility(0);
                    } else {
                        rcsIcon.setVisibility(8);
                    }
                }
            }
        }
    }

    private void addFTEntry(Context context, ArrayList<ViewEntry> allEntries, ContactDetailAdapter adapter, ContactDetailFragment fragment, int lEntryToBeAddedAtIndex) {
        boolean isNoNamedContact = false;
        if (CommonConstants.LOG_DEBUG && HwLog.HWDBG) {
            HwLog.d("RcsContactDetailAdapter", "addFileTransferEntry lEntryToBeAddedAtIndex ::" + lEntryToBeAddedAtIndex);
        }
        if (!(fragment == null || fragment.getActivity() == null || fragment.getActivity().getIntent() == null)) {
            isNoNamedContact = fragment.getActivity().getIntent().getBooleanExtra("EXTRA_CALL_LOG_NONAME_CALL", false);
        }
        if (isNoNamedContact && -1 == lEntryToBeAddedAtIndex) {
            lEntryToBeAddedAtIndex = 1;
        }
        if (lEntryToBeAddedAtIndex != -1) {
            int index = lEntryToBeAddedAtIndex;
            allEntries.add(index, getNewFTEntry(context));
            this.mFileIndex = index;
            if (!(fragment == null || fragment.getActivity() == null)) {
                fragment.getActivity().runOnUiThread(new RunnableEx(adapter));
            }
            addIMEntry(context, allEntries, adapter, fragment);
        }
    }

    private void addIMEntry(Context context, ArrayList<ViewEntry> allEntries, ContactDetailAdapter adapter, ContactDetailFragment fragment) {
        int mImWithMms = 1;
        try {
            mImWithMms = System.getInt(context.getContentResolver(), "im_thread_display_switcher", 1);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (HwLog.HWDBG) {
            HwLog.d("RcsContactDetailAdapter", "test_mImWithMms = " + mImWithMms + "");
        }
        if (mImWithMms != 1) {
            DetailViewEntry lDetailViewEntryIM = getNewIMEntry(context);
            this.mIMIndex = this.mFileIndex;
            this.mFileIndex++;
            allEntries.add(this.mIMIndex, lDetailViewEntryIM);
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", "mIMIndex = " + this.mIMIndex + "");
            }
            if (fragment != null && fragment.getActivity() != null) {
                fragment.getActivity().runOnUiThread(new RunnableEx(adapter));
            }
        }
    }

    public void initRCSCapabityquest() {
        if (EmuiFeatureManager.isRcsFeatureEnable()) {
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", " initRCSCapabityquest ");
            }
            this.mFileIndex = -1;
            this.mIMIndex = -1;
            this.mSupportFileTransferList.clear();
        }
    }

    public void updateFTCapInAdapter(boolean fTcap, String phoneNumber, ArrayList<ViewEntry> allEntries, ContactDetailAdapter adapter) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && phoneNumber != null && allEntries != null && adapter != null) {
            String number = PhoneNumberUtils.normalizeNumber(phoneNumber);
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", "updateFTCapInAdapter mFileIndex = " + this.mFileIndex + "");
            }
            if (-1 != this.mFileIndex && this.mFileIndex < allEntries.size()) {
                try {
                    DetailViewEntry lDetailViewEntry = (DetailViewEntry) allEntries.get(this.mFileIndex);
                    if (fTcap) {
                        if (!this.mSupportFileTransferList.contains(number)) {
                            this.mSupportFileTransferList.add(number);
                        }
                    } else if (this.mSupportFileTransferList.contains(number)) {
                        this.mSupportFileTransferList.remove(number);
                    }
                    boolean isSupportFT = !this.mSupportFileTransferList.isEmpty();
                    if (lDetailViewEntry.isEnabled != isSupportFT) {
                        lDetailViewEntry.isEnabled = isSupportFT;
                        allEntries.set(this.mFileIndex, lDetailViewEntry);
                        adapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    HwLog.e("RcsContactDetailAdapter", "updateFTCapInAdapter error: " + e.getMessage());
                }
            }
        }
    }

    public void addPhoneNum(String number, Context context) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && number != null && context != null) {
            if (this.mCapabilityService == null) {
                this.mCapabilityService = CapabilityService.getInstance("contacts");
            }
            String num = PhoneNumberUtils.normalizeNumber(number);
            boolean isSupportFT = this.mCapabilityService.isSupportFT(num);
            Capabilities cap = this.mCapabilityService.getCapabilities(num);
            if (cap != null) {
                updateCapMap(number, cap.isOnLine(), cap.isPreCallSupported(), num);
                HwLog.i("RcsContactDetailAdapter", "addPhoneNum for the last status saved isOnline: " + cap.isOnLine() + " isPreCallSupported : " + cap.isPreCallSupported());
            }
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", "addPhoneNum isSupportFT  " + isSupportFT);
            }
            if (isSupportFT) {
                if (!this.mSupportFileTransferList.contains(num)) {
                    this.mSupportFileTransferList.add(num);
                    if (HwLog.HWDBG) {
                        HwLog.d("RcsContactDetailAdapter", "addPhoneNum mSupportFileTransferList isEmpty  " + this.mSupportFileTransferList.isEmpty());
                    }
                }
            } else if (this.mSupportFileTransferList.contains(num)) {
                this.mSupportFileTransferList.remove(num);
            }
        }
    }

    private DetailViewEntry getNewFTEntry(Context context) {
        DetailViewEntry lFTEntry = new DetailViewEntry(!this.mSupportFileTransferList.isEmpty());
        lFTEntry.data = context.getString(R.string.rcs_contacts_file_transfer);
        lFTEntry.mimetype = "vnd.android.cursor.item/phone_v2";
        lFTEntry.mCustom_mimetype = "RCS_FT";
        lFTEntry.kind = context.getString(R.string.phoneLabelsGroup);
        lFTEntry.typeString = "";
        return lFTEntry;
    }

    private DetailViewEntry getNewIMEntry(Context context) {
        DetailViewEntry lIMEntry = new DetailViewEntry(!this.mSupportFileTransferList.isEmpty());
        lIMEntry.data = context.getString(R.string.rcs_contacts_im_transfer);
        lIMEntry.mimetype = "vnd.android.cursor.item/phone_v2";
        lIMEntry.mCustom_mimetype = "RCS_IM";
        lIMEntry.kind = context.getString(R.string.phoneLabelsGroup);
        lIMEntry.typeString = "";
        return lIMEntry;
    }

    public void updateContactDetailUIOnRcsStatusChanged(ArrayList<ViewEntry> allEntries, ContactDetailAdapter adapter, boolean loginStatus) {
        if (EmuiFeatureManager.isRcsFeatureEnable() && allEntries != null && adapter != null) {
            if (HwLog.HWDBG) {
                HwLog.d("RcsContactDetailAdapter", "updateContactDetailUIOnRcsStatusChanged mFileIndex = " + this.mFileIndex + "");
                HwLog.d("RcsContactDetailAdapter", "updateContactDetailUIOnRcsStatusChanged mIMIndex = " + this.mIMIndex + "");
            }
            if (!loginStatus) {
                int size = allEntries.size();
                if (this.mFileIndex > -1 && this.mFileIndex < size) {
                    DetailViewEntry lDetailViewEntry = (DetailViewEntry) allEntries.get(this.mFileIndex);
                    lDetailViewEntry.isEnabled = false;
                    allEntries.set(this.mFileIndex, lDetailViewEntry);
                }
                adapter.notifyDataSetChanged();
            }
        }
    }

    public void updateCapMap(String oriNumber, boolean isOnlineEnable, boolean isPreCallSupportedEnable, String formatNumber) {
        if (this.mCapabilityService != null) {
            if (isOnlineEnable && isPreCallSupportedEnable) {
                this.mNumberCapMap.put(oriNumber, Integer.valueOf(1));
            } else if (isPreCallSupportedEnable && !isOnlineEnable) {
                this.mNumberCapMap.put(oriNumber, Integer.valueOf(0));
            } else if (!isPreCallSupportedEnable) {
                this.mNumberCapMap.put(oriNumber, Integer.valueOf(-1));
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public int getDisplayState(Contact contactData, String number) {
        if (contactData != null) {
            try {
            } catch (Exception e) {
                HwLog.e("RcsContactDetailAdapter", "failed to update rcs view");
                return -1;
            }
        }
        if (!RcsContactsUtils.isBBVersion()) {
            if (RcseProfile.getRcsService() == null || !RcseProfile.getRcsService().getLoginState()) {
                return 0;
            }
            if (this.mNumberCapMap.containsKey(number)) {
                return ((Integer) this.mNumberCapMap.get(number)).intValue();
            }
            return -1;
        }
        return -1;
    }
}
