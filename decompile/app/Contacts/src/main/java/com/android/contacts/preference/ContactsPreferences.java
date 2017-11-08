package com.android.contacts.preference;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.ContactDisplayUtils;
import com.google.android.gms.R;
import com.huawei.cust.HwCustUtils;

public final class ContactsPreferences extends ContentObserver {
    private Context mContext;
    private HwCustContactsPreferences mCust = null;
    private int mDisplayOrder = -1;
    private Handler mHandler;
    private ChangeListener mListener = null;
    private int mSortOrder = -1;

    public interface ChangeListener {
        void onChange();
    }

    public ContactsPreferences(Context context) {
        super(null);
        this.mContext = context;
        this.mHandler = new Handler();
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustContactsPreferences) HwCustUtils.createObj(HwCustContactsPreferences.class, new Object[]{context});
        }
    }

    public boolean isSortOrderUserChangeable() {
        return this.mContext.getResources().getBoolean(R.bool.config_sort_order_user_changeable);
    }

    public int getDefaultSortOrder() {
        int order;
        if (this.mContext.getResources().getBoolean(R.bool.config_default_sort_order_primary)) {
            order = 1;
        } else {
            order = 2;
        }
        if (this.mCust == null || !this.mCust.isChangeSortByLang()) {
            return order;
        }
        return this.mCust.getOrderByLang(order, 0);
    }

    public int getSortOrder() {
        if (!isSortOrderUserChangeable()) {
            return getDefaultSortOrder();
        }
        if (this.mSortOrder == -1) {
            try {
                this.mSortOrder = System.getInt(this.mContext.getContentResolver(), "android.contacts.SORT_ORDER");
            } catch (SettingNotFoundException e) {
                this.mSortOrder = getDefaultSortOrder();
            }
        }
        return this.mSortOrder;
    }

    public void setSortOrder(int sortOrder) {
        this.mSortOrder = sortOrder;
        System.putInt(this.mContext.getContentResolver(), "android.contacts.SORT_ORDER", sortOrder);
    }

    public boolean isDisplayOrderUserChangeable() {
        return this.mContext.getResources().getBoolean(R.bool.config_display_order_user_changeable);
    }

    public int getDefaultDisplayOrder() {
        int order;
        if (this.mContext.getResources().getBoolean(R.bool.config_default_display_order_primary)) {
            order = 1;
        } else {
            order = 2;
        }
        if (this.mCust == null || !this.mCust.isChangeSortByLang()) {
            return order;
        }
        return this.mCust.getOrderByLang(order, 1);
    }

    public int getDisplayOrder() {
        if (!isDisplayOrderUserChangeable()) {
            return getDefaultDisplayOrder();
        }
        if (this.mDisplayOrder == -1) {
            try {
                this.mDisplayOrder = System.getInt(this.mContext.getContentResolver(), "android.contacts.DISPLAY_ORDER");
            } catch (SettingNotFoundException e) {
                this.mDisplayOrder = getDefaultDisplayOrder();
            }
        }
        return this.mDisplayOrder;
    }

    public void setDisplayOrder(int displayOrder) {
        this.mDisplayOrder = displayOrder;
        System.putInt(this.mContext.getContentResolver(), "android.contacts.DISPLAY_ORDER", displayOrder);
        ContactDisplayUtils.setNameDisplayOrder(getDisplayOrder());
    }

    public void registerChangeListener(ChangeListener listener) {
        if (this.mListener != null) {
            unregisterChangeListener();
        }
        this.mListener = listener;
        this.mDisplayOrder = -1;
        this.mSortOrder = -1;
        ContentResolver contentResolver = this.mContext.getContentResolver();
        contentResolver.registerContentObserver(System.getUriFor("android.contacts.SORT_ORDER"), false, this);
        contentResolver.registerContentObserver(System.getUriFor("android.contacts.DISPLAY_ORDER"), false, this);
    }

    public void unregisterChangeListener() {
        if (this.mListener != null) {
            this.mContext.getContentResolver().unregisterContentObserver(this);
            this.mListener = null;
        }
    }

    public void onChange(boolean selfChange) {
        this.mHandler.post(new Runnable() {
            public void run() {
                ContactsPreferences.this.mSortOrder = -1;
                ContactsPreferences.this.mDisplayOrder = -1;
                if (ContactsPreferences.this.mListener != null) {
                    ContactsPreferences.this.mListener.onChange();
                }
            }
        });
    }
}
