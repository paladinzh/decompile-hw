package com.android.contacts.detail;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewCache;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.ViewEntry;
import com.android.contacts.model.Contact;
import java.util.ArrayList;

public class HwCustContactDetailAdapter {
    public HwCustContactDetailAdapter(Context mContext) {
    }

    public boolean isCustHideGeoInfo() {
        return false;
    }

    public void setVisibility(TextView textView, int targetValue) {
    }

    public boolean isVideoCallEntry(String type1, String type2) {
        return false;
    }

    public void addOtherEntry(Context context, ArrayList<ViewEntry> arrayList, Contact contactData, String entryType, ContactDetailAdapter adapter) {
    }

    public boolean isVTCall(String callType) {
        return false;
    }

    public DetailViewEntry getNewVideoCallEntry() {
        return new DetailViewEntry(false);
    }

    public boolean isSupportedVideoCall() {
        return false;
    }

    public void setVisiblityForOtherEntry(DetailViewCache aViews, DetailViewEntry aEntry, ContactDetailFragment detailFragment) {
    }

    public boolean isHideGeoInfoOfNoNameCall(boolean isNoNameCall) {
        return false;
    }

    public ArrayList<String> getmSupportFileTransferList() {
        return new ArrayList();
    }

    public void addOtherEntry(Context context, ArrayList<ViewEntry> arrayList, Contact contactData, String entryType, ContactDetailFragment fragment, ContactDetailAdapter adapter) {
    }

    public void updateFTCapInAdapter(boolean fTcap, String phoneNumber, ArrayList<ViewEntry> arrayList, ContactDetailAdapter adapter) {
    }

    public void addPhoneNum(String number, Context context) {
    }

    public void initForCustomizations(Context context) {
    }

    public void setRcsViewVisibility(boolean isPhoneItem, View view, DetailViewEntry entry, Context context) {
    }

    public boolean isFileTransferEntry(String type1, String type2) {
        return false;
    }

    public boolean isIMTransferEntry(String type1, String type2) {
        return false;
    }

    public void initRCSCapabityquest() {
    }

    public void updateContactDetailUIOnRcsStatusChanged(ArrayList<ViewEntry> arrayList, ContactDetailAdapter adapter, boolean loginStatus) {
    }

    public void setSelectedPhoneNumber(String selectedPhoneNumber) {
    }

    public int getCustomLayoutIfNeeded(DetailViewEntry entry, int actualLayoutId) {
        return actualLayoutId;
    }
}
