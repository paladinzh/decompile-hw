package com.android.contacts.ext.phone;

import android.content.Context;
import com.android.contacts.ext.phone.SetupPhoneAccount.Contact;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;

public class HwCustSetupPhoneAccount {
    Context mContext;

    public HwCustSetupPhoneAccount(Context context) {
        this.mContext = context;
    }

    public void getServiceProviderName() {
    }

    public void getSpnInfoFromXML(XmlPullParser parser) {
    }

    public void parserContactsAndGroup(XmlPullParser parser, ArrayList<Contact> arrayList, ArrayList<String> arrayList2, int eventType) {
    }
}
