package com.android.contacts.ext.phone;

import android.content.Context;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import com.android.contacts.ext.phone.SetupPhoneAccount.Contact;
import java.io.IOException;
import java.util.ArrayList;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwCustSetupPhoneAccountImpl extends HwCustSetupPhoneAccount {
    private static final String CUST_TAG = "cust";
    private String custSpn = null;
    private String spn = null;

    public HwCustSetupPhoneAccountImpl(Context context) {
        super(context);
    }

    public void getServiceProviderName() {
        this.spn = TelephonyManager.getDefault().getSimOperatorName();
    }

    public void getSpnInfoFromXML(XmlPullParser parser) {
        this.custSpn = parser.getAttributeValue(null, "spn");
    }

    private boolean isSpnEmpty() {
        if (TextUtils.isEmpty(this.spn)) {
            return true;
        }
        return false;
    }

    private boolean equalsSpnFromXml() {
        if (TextUtils.isEmpty(this.spn) || TextUtils.isEmpty(this.custSpn) || !this.custSpn.equals(this.spn)) {
            return false;
        }
        return true;
    }

    public void parserContactsAndGroup(XmlPullParser parser, ArrayList<Contact> contactList, ArrayList<String> groups, int eventType) {
        if (!isSpnEmpty() && equalsSpnFromXml()) {
            contactList.clear();
            SetupPhoneAccount.parserContactsAndGroup(parser, groups);
        } else if (TextUtils.isEmpty(this.custSpn)) {
            SetupPhoneAccount.parserContactsAndGroup(parser, groups);
        } else {
            moveToEndCustTag(parser, eventType);
        }
    }

    private void moveToEndCustTag(XmlPullParser parser, int eventType) {
        while (1 != eventType) {
            try {
                if (!CUST_TAG.equals(parser.getName()) || 3 != eventType) {
                    eventType = parser.next();
                } else {
                    return;
                }
            } catch (XmlPullParserException xmp) {
                xmp.printStackTrace();
                return;
            } catch (IOException ioe) {
                ioe.printStackTrace();
                return;
            }
        }
    }
}
