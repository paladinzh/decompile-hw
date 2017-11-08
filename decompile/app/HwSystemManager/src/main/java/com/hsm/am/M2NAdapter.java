package com.hsm.am;

import android.app.IActivityManager;
import android.os.RemoteException;

public class M2NAdapter {
    public static int stopUser(IActivityManager iAm, int userid, boolean force) throws RemoteException {
        return iAm.stopUser(userid, force, null);
    }
}
