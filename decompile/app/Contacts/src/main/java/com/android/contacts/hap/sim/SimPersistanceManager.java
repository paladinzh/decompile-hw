package com.android.contacts.hap.sim;

import android.content.Entity;
import android.database.Cursor;
import android.net.Uri;
import com.android.contacts.model.RawContactDeltaList;
import java.util.List;

public abstract class SimPersistanceManager {
    public abstract int delete(SimContact simContact);

    public abstract int delete(String str, String str2);

    public abstract SimContact getContact(Cursor cursor);

    public abstract void getContacts(Entity entity, List<SimContact> list);

    public abstract void getSimContacts(Entity entity, List<SimContact> list);

    public abstract Uri insert(SimContact simContact, boolean z);

    public abstract void performHealthCheck(String str);

    public abstract Cursor queryAll();

    public abstract int save(RawContactDeltaList rawContactDeltaList);
}
