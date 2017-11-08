package com.android.contacts.hap.sim.base;

import android.content.ContentValues;
import android.content.Context;
import android.content.Entity;
import android.content.Entity.NamedContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.contacts.hap.sim.SimContact;
import com.android.contacts.hap.sim.SimPersistanceManager;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.util.HwLog;
import java.util.List;

public class BaseSimPersistenceManager extends SimPersistanceManager {
    private Context mContext;
    private Uri mProviderUri;

    BaseSimPersistenceManager(Uri aProviderUri) {
        this.mProviderUri = aProviderUri;
    }

    protected void init(Context aContext) {
        this.mContext = aContext;
    }

    public Uri insert(SimContact contact, boolean toCheckSpace) {
        return null;
    }

    public int delete(SimContact contact) {
        return 0;
    }

    public int save(RawContactDeltaList state) {
        throw new UnsupportedOperationException();
    }

    public SimContact getContact(Cursor aCursor) {
        SimContact simContact = new SimContact();
        int coulmIndex = aCursor.getColumnIndex("name");
        if (-1 != coulmIndex) {
            simContact.name = aCursor.getString(coulmIndex);
        }
        coulmIndex = aCursor.getColumnIndex("number");
        if (-1 != coulmIndex) {
            simContact.number = aCursor.getString(coulmIndex);
        }
        return simContact;
    }

    public void getContacts(Entity entity, List<SimContact> contactList) {
        if (contactList != null) {
            String lId = entity.getEntityValues().getAsString("_id");
            String lName = null;
            SimContact lContact = new SimContact();
            lContact.id = lId;
            for (NamedContentValues ncValues : entity.getSubValues()) {
                ContentValues values = ncValues.values;
                if ("vnd.android.cursor.item/name".equals(values.getAsString("mimetype")) && lName == null) {
                    lName = values.getAsString("data1");
                }
                if ("vnd.android.cursor.item/phone_v2".equals(values.getAsString("mimetype"))) {
                    if (!TextUtils.isEmpty(lContact.number)) {
                        lContact = new SimContact();
                        lContact.id = lId;
                    }
                    lContact.number = values.getAsString("data1");
                }
                contactList.add(lContact);
            }
            if (lName != null) {
                for (SimContact simContact : contactList) {
                    simContact.name = lName;
                }
            }
        }
    }

    public void getSimContacts(Entity entity, List<SimContact> contactList) {
        getContacts(entity, contactList);
    }

    public Cursor queryAll() {
        if (HwLog.HWDBG) {
            HwLog.v("BaseSimPersistenceManager", "inside queryAll");
            HwLog.v("BaseSimPersistenceManager", "uri : " + this.mProviderUri);
        }
        Cursor simContacts = null;
        try {
            simContacts = this.mContext.getContentResolver().query(this.mProviderUri, null, null, null, null);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        return simContacts;
    }

    public int delete(String efid, String index) {
        return 0;
    }

    public void performHealthCheck(String aAccountType) {
    }
}
