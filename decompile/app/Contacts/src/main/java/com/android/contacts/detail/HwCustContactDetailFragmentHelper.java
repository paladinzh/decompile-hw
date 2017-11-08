package com.android.contacts.detail;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.ContextMenu;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.detail.ContactDetailAdapter.ViewEntry;
import com.android.contacts.model.Contact;
import java.util.ArrayList;

public class HwCustContactDetailFragmentHelper {
    public void customizeContextMenu(ContextMenu aMenu) {
    }

    public void setOtherDiaglogVisibleFlag(Bundle bundle) {
    }

    public void setOutStatusForOtherEntry(Bundle bundle, String entry, DetailViewEntry entryView) {
    }

    public void setOtherEntryViewData() {
    }

    public void addOtherEntry(ContactDetailAdapter adapter) {
    }

    public void getOtherDialog(ArrayList<DetailViewEntry> arrayList, DetailViewEntry aEntry, ContactDetailFragment fragment) {
    }

    public void getVideoCallDialog(ArrayList<DetailViewEntry> arrayList, DetailViewEntry aEntry, ContactDetailFragment fragment) {
    }

    public boolean checkOtherEntry(DetailViewEntry selectedEntry) {
        return false;
    }

    public void setFlattenListForOtherEntry(ContactDetailFragment fragment) {
    }

    public void setFlattenListForOtherEntry(ContactDetailFragment fragment, Object object) {
    }

    public void addOtherEntry(ContactDetailAdapter adapter, ArrayList<DetailViewEntry> arrayList, ArrayList<ViewEntry> arrayList2) {
    }

    public void getFTDialog(ArrayList<DetailViewEntry> arrayList, DetailViewEntry aEntry, ContactDetailFragment fragment) {
    }

    public void getIMDialog(ArrayList<DetailViewEntry> arrayList, DetailViewEntry aEntry, ContactDetailFragment fragment) {
    }

    public void handleCustomizationsOnCreate(ContactDetailFragment fragment) {
    }

    public void handleCustomizationsOnDestroy(Context context) {
    }

    public void sendRcsQuestCapability(ArrayList<DetailViewEntry> arrayList) {
    }

    public void setSelectedPhoneNumber(String phoneNumber) {
    }

    public String getSelectedPhoneNumber() {
        return null;
    }

    public void buildCustomEntries(Context context, Contact contactData, String accountType) {
    }

    public void setupCustomFlattenedList(ContactDetailFragment contactDetailFragment) {
    }

    public void setupCustomFlattenedList(ContactDetailFragment contactDetailFragment, Object object) {
    }

    public boolean checkAndInitCall(Context aContext, Intent aIntent) {
        return false;
    }
}
