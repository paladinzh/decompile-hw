package com.android.contacts.list;

import android.content.Context;
import android.content.SharedPreferences;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.list.ContactListFilterController.ContactListFilterListener;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.SharePreferenceUtil;
import java.util.ArrayList;
import java.util.List;

/* compiled from: ContactListFilterController */
class ContactListFilterControllerImpl extends ContactListFilterController {
    private final Context mContext;
    private ContactListFilter mFilter;
    private final List<ContactListFilterListener> mListeners = new ArrayList();

    public ContactListFilterControllerImpl(Context context) {
        this.mContext = context;
        this.mFilter = ContactListFilter.restoreDefaultPreferences(getSharedPreferences());
        checkFilterValidity(true);
    }

    public void addListener(ContactListFilterListener listener) {
        this.mListeners.add(listener);
    }

    public void removeListener(ContactListFilterListener listener) {
        this.mListeners.remove(listener);
    }

    public ContactListFilter getFilter() {
        return this.mFilter;
    }

    private SharedPreferences getSharedPreferences() {
        return SharePreferenceUtil.getDefaultSp_de(this.mContext);
    }

    public void setContactListFilter(ContactListFilter filter, boolean persistent) {
        setContactListFilter(filter, persistent, true);
    }

    private void setContactListFilter(ContactListFilter filter, boolean persistent, boolean notifyListeners) {
        if (filter != null) {
            if (!SharePreferenceUtil.getDefaultSp_de(this.mContext).getBoolean("preference_show_sim_contacts", true) && CommonUtilMethods.isSimAccount(filter.accountType)) {
                filter = ContactListFilter.createFilterWithType(-2);
            }
            if (!filter.equals(this.mFilter)) {
                this.mFilter = filter;
                if (persistent) {
                    ContactListFilter.storeToPreferences(getSharedPreferences(), this.mFilter);
                }
                if (notifyListeners && !this.mListeners.isEmpty()) {
                    notifyContactListFilterChanged();
                }
            }
        }
    }

    public void selectCustomFilter() {
        setContactListFilter(ContactListFilter.createFilterWithType(-3), true);
    }

    private void notifyContactListFilterChanged() {
        for (ContactListFilterListener listener : this.mListeners) {
            listener.onContactListFilterChanged();
        }
    }

    public void checkFilterValidity(boolean notifyListeners) {
        if (this.mFilter != null) {
            switch (this.mFilter.filterType) {
                case -6:
                    setContactListFilter(ContactListFilter.restoreDefaultPreferences(getSharedPreferences()), false, notifyListeners);
                    break;
                case 0:
                    if (!filterAccountExists()) {
                        setContactListFilter(ContactListFilter.createFilterWithType(-2), true, notifyListeners);
                        this.mFilter.mIsFiterChanged = true;
                        break;
                    }
                    break;
            }
        }
    }

    private boolean filterAccountExists() {
        boolean z = true;
        AccountTypeManager accountTypeManager = AccountTypeManager.getInstance(this.mContext);
        if (!CommonUtilMethods.isSimAccount(this.mFilter.accountType)) {
            return accountTypeManager.contains(new AccountWithDataSet(this.mFilter.accountName, this.mFilter.accountType, this.mFilter.dataSet), false);
        }
        if (1 == SimFactoryManager.getSimState(SimFactoryManager.getSlotIdBasedOnAccountType(this.mFilter.accountType))) {
            z = false;
        }
        return z;
    }

    public void resetDefaultFilterToAllTypeIfNecessary() {
        if (this.mFilter != null && this.mFilter.filterType == -3) {
            setContactListFilter(ContactListFilter.createFilterWithType(-2), true);
        }
    }
}
