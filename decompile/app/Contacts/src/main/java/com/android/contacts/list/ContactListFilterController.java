package com.android.contacts.list;

import android.content.Context;

public abstract class ContactListFilterController {

    public interface ContactListFilterListener {
        void onContactListFilterChanged();
    }

    public abstract void addListener(ContactListFilterListener contactListFilterListener);

    public abstract void checkFilterValidity(boolean z);

    public abstract ContactListFilter getFilter();

    public abstract void removeListener(ContactListFilterListener contactListFilterListener);

    public abstract void resetDefaultFilterToAllTypeIfNecessary();

    public abstract void selectCustomFilter();

    public abstract void setContactListFilter(ContactListFilter contactListFilter, boolean z);

    public static ContactListFilterController getInstance(Context context) {
        return (ContactListFilterController) context.getApplicationContext().getSystemService("contactListFilter");
    }

    public static ContactListFilterController createContactListFilterController(Context context) {
        return new ContactListFilterControllerImpl(context);
    }
}
