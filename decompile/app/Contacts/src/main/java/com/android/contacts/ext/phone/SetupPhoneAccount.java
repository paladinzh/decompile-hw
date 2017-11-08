package com.android.contacts.ext.phone;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import android.text.TextUtils;
import android.util.Xml;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.Objects;
import com.google.android.gms.R;
import com.google.android.gms.common.Scopes;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.cust.HwCustUtils;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class SetupPhoneAccount {
    private static final int[] PREDEFINED_GROUPS_LABELS = new int[]{R.string.str_group_family, R.string.str_group_friends, R.string.str_group_work};
    private static final String[] PREDEFINED_GROUPS_UIDS = new String[]{"PREDEFINED_HUAWEI_GROUP_FAMILY", "PREDEFINED_HUAWEI_GROUP_FRIENDS", "PREDEFINED_HUAWEI_GROUP_WORK"};
    private static ArrayList<Contact> mContactList = new ArrayList();
    private static HashMap<String, Integer> mEmailAddrType = new HashMap();
    private static HashMap<String, Integer> mPhoneType = new HashMap();
    private static HashSet<Integer> mPredefinedGroupLabelResId = new HashSet();

    public static class Contact {
        ArrayList<AddrDetail> address = new ArrayList();
        String avatarPath;
        String company;
        ArrayList<EmailDetail> emails = new ArrayList();
        String name;
        ArrayList<PhoneDetail> numbers = new ArrayList();

        static class AddrDetail {
            String address;
            int type = 0;

            AddrDetail() {
            }

            public int hashCode() {
                if (this.address != null) {
                    return this.address.hashCode() + 0;
                }
                return 0;
            }

            public boolean equals(Object o) {
                if (!(o instanceof AddrDetail)) {
                    return false;
                }
                return Objects.equal(this.address, ((AddrDetail) o).address);
            }
        }

        static class EmailDetail {
            String email;
            int type;

            EmailDetail() {
            }

            public int hashCode() {
                if (this.email != null) {
                    return this.email.hashCode() + 0;
                }
                return 0;
            }

            public boolean equals(Object o) {
                if (!(o instanceof EmailDetail)) {
                    return false;
                }
                return Objects.equal(this.email, ((EmailDetail) o).email);
            }
        }

        static class PhoneDetail {
            String number;
            int type;

            PhoneDetail() {
            }

            public int hashCode() {
                if (this.number != null) {
                    return this.number.hashCode() + 0;
                }
                return 0;
            }

            public boolean equals(Object o) {
                if (!(o instanceof PhoneDetail)) {
                    return false;
                }
                return Objects.equal(this.number, ((PhoneDetail) o).number);
            }
        }

        public void addPhone(String aType, String aNumber) {
            if (!TextUtils.isEmpty(aNumber)) {
                PhoneDetail phoneDetail = new PhoneDetail();
                phoneDetail.number = aNumber;
                if (aType == null || SetupPhoneAccount.mPhoneType.get(aType) == null) {
                    phoneDetail.type = ((Integer) SetupPhoneAccount.mPhoneType.get("Mobile")).intValue();
                } else {
                    phoneDetail.type = ((Integer) SetupPhoneAccount.mPhoneType.get(aType)).intValue();
                }
                this.numbers.add(phoneDetail);
            }
        }

        public void addEmail(String aType, String aEmail) {
            if (!TextUtils.isEmpty(aEmail)) {
                EmailDetail emailDetail = new EmailDetail();
                emailDetail.email = aEmail;
                if (aType == null || SetupPhoneAccount.mEmailAddrType.get(aType) == null) {
                    emailDetail.type = ((Integer) SetupPhoneAccount.mEmailAddrType.get("Home")).intValue();
                } else {
                    emailDetail.type = ((Integer) SetupPhoneAccount.mEmailAddrType.get(aType)).intValue();
                }
                this.emails.add(emailDetail);
            }
        }

        public void addAddress(String aType, String aAddr) {
            if (!TextUtils.isEmpty(aAddr)) {
                AddrDetail addrDetail = new AddrDetail();
                addrDetail.address = aAddr;
                if (aType == null || SetupPhoneAccount.mEmailAddrType.get(aType) == null) {
                    addrDetail.type = ((Integer) SetupPhoneAccount.mEmailAddrType.get("Home")).intValue();
                } else {
                    addrDetail.type = ((Integer) SetupPhoneAccount.mEmailAddrType.get(aType)).intValue();
                }
                this.address.add(addrDetail);
            }
        }

        public int hashCode() {
            int hashCode = 0;
            if (this.name != null) {
                hashCode = this.name.hashCode() + 0;
            }
            if (this.numbers != null) {
                hashCode += this.numbers.hashCode();
            }
            if (this.emails != null) {
                return hashCode + this.emails.hashCode();
            }
            return hashCode;
        }

        public boolean equals(Object o) {
            if (!(o instanceof Contact)) {
                return false;
            }
            Contact target = (Contact) o;
            if (Objects.equal(this.name, target.name) && Objects.equal(this.numbers, target.numbers) && Objects.equal(this.emails, target.emails) && Objects.equal(this.address, target.address)) {
                return true;
            }
            return false;
        }
    }

    public static HashSet<Integer> getPredefinedGroupabelResId() {
        if (mPredefinedGroupLabelResId.size() == 0) {
            for (int valueOf : PREDEFINED_GROUPS_LABELS) {
                mPredefinedGroupLabelResId.add(Integer.valueOf(valueOf));
            }
        }
        return mPredefinedGroupLabelResId;
    }

    public static boolean updatePredefinedGroupsLabelRes(Context c) {
        if (PREDEFINED_GROUPS_LABELS.length <= 0) {
            return true;
        }
        ArrayList<ContentProviderOperation> operationList = new ArrayList();
        int index = 0;
        for (String uid : PREDEFINED_GROUPS_UIDS) {
            Builder update = ContentProviderOperation.newUpdate(Groups.CONTENT_URI);
            update.withSelection("sync1= ?", new String[]{uid});
            update.withValue("title", c.getString(PREDEFINED_GROUPS_LABELS[index]));
            if (QueryUtil.isHAPProviderInstalled()) {
                update.withValue("res_package", c.getPackageName());
            }
            update.withValue("title_res", Integer.valueOf(PREDEFINED_GROUPS_LABELS[index]));
            operationList.add(update.build());
            index++;
        }
        try {
            c.getContentResolver().applyBatch("com.android.contacts", operationList);
            return true;
        } catch (OperationApplicationException e) {
            e.printStackTrace();
            return false;
        } catch (RemoteException e2) {
            e2.printStackTrace();
            return false;
        } catch (Exception e3) {
            e3.printStackTrace();
            return false;
        }
    }

    private static void updateContactUnitsInMeWidget(Context aContext) {
        aContext.sendBroadcast(new Intent("com.android.contacts.hotline.updated"));
    }

    private static void addPhoneType() {
        mPhoneType.put("Home", Integer.valueOf(1));
        mPhoneType.put("Mobile", Integer.valueOf(2));
        mPhoneType.put("Work", Integer.valueOf(3));
        mPhoneType.put("Fax", Integer.valueOf(5));
        mPhoneType.put("Other", Integer.valueOf(7));
    }

    private static void addEmailAndAddrType() {
        mEmailAddrType.put("Home", Integer.valueOf(1));
        mEmailAddrType.put("Work", Integer.valueOf(2));
        mEmailAddrType.put("Other", Integer.valueOf(3));
    }

    public static boolean addTypeAndParseLoadPreDefinedContacts(Context context) {
        addPhoneType();
        addEmailAndAddrType();
        parseAndLoadPreDefinedContactsFiles(context, "/xml/predefined_data.xml");
        boolean parseSuccess = saveToDatabase(mContactList, context);
        updateContactUnitsInMeWidget(context);
        return parseSuccess;
    }

    private static void parseAndLoadPreDefinedContactsFiles(Context context, String fileName) {
        ArrayList<File> files = new ArrayList();
        try {
            files = HwCfgFilePolicy.getCfgFileList(fileName, 0);
        } catch (NoClassDefFoundError e) {
            HwLog.e("SetupPhoneAccount", "caught exception:", e);
        }
        if (files.size() == 0) {
            HwLog.w("SetupPhoneAccount", "No config file found for:" + fileName);
            return;
        }
        Iterator<File> it = files.iterator();
        while (it.hasNext()) {
            parseAndLoadPreDefinedContacts(context, (File) it.next());
        }
    }

    private static void parseAndLoadPreDefinedContacts(Context context, File file) {
        FileNotFoundException e;
        XmlPullParserException xmp;
        IOException ioe;
        Throwable th;
        InputStream inputStream = null;
        String mcc_mnc = CommonUtilMethods.getTelephonyManager(context).getSimOperator();
        if (TextUtils.isEmpty(mcc_mnc) || mcc_mnc.length() < 5) {
            mcc_mnc = "not_match";
        }
        String currentMCC = mcc_mnc.substring(0, 3);
        String currentMNC = mcc_mnc.substring(3);
        HwCustSetupPhoneAccount hwCustSetupPhoneAccount = null;
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            hwCustSetupPhoneAccount = (HwCustSetupPhoneAccount) HwCustUtils.createObj(HwCustSetupPhoneAccount.class, new Object[]{context});
            if (hwCustSetupPhoneAccount != null) {
                hwCustSetupPhoneAccount.getServiceProviderName();
            }
        }
        try {
            InputStream lInput = new FileInputStream(file);
            try {
                XmlPullParser parser = Xml.newPullParser();
                if (parser == null) {
                    if (lInput != null) {
                        try {
                            lInput.close();
                        } catch (IOException e2) {
                            HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e2.getMessage());
                            e2.printStackTrace();
                        }
                    }
                    return;
                }
                parser.setInput(lInput, null);
                ArrayList<String> groups = new ArrayList();
                int eventType = parser.getEventType();
                while (1 != eventType) {
                    if (2 == eventType && !"datainitializor".equals(parser.getName())) {
                        if ("contact".equals(parser.getName())) {
                            Contact contact = new Contact();
                            while (true) {
                                if (2 != eventType) {
                                    if (3 == eventType && "contact".equals(parser.getName())) {
                                        break;
                                    }
                                }
                                String tag = parser.getName();
                                if ("name".equals(tag)) {
                                    contact.name = parser.nextText();
                                } else if ("phone".equals(tag)) {
                                    contact.addPhone(parser.getAttributeValue(null, "type"), parser.nextText());
                                } else if (Scopes.EMAIL.equals(tag)) {
                                    contact.addEmail(parser.getAttributeValue(null, "type"), parser.nextText());
                                } else if ("address".equals(tag)) {
                                    contact.addAddress(parser.getAttributeValue(null, "type"), parser.nextText());
                                } else if ("avatar".equals(tag)) {
                                    contact.avatarPath = parser.nextText();
                                } else if ("company".equals(tag)) {
                                    contact.company = parser.nextText();
                                } else {
                                    HwLog.i("SetupPhoneAccount", " No match ");
                                }
                                eventType = parser.next();
                            }
                            if (!mContactList.contains(contact)) {
                                mContactList.add(contact);
                            }
                        } else if ("group".equals(parser.getName())) {
                            while (true) {
                                if (2 != eventType) {
                                    if (3 == eventType && "group".equals(parser.getName())) {
                                        break;
                                    }
                                } else if ("name".equals(parser.getName())) {
                                    groups.add(parser.nextText());
                                }
                                eventType = parser.next();
                            }
                        } else if ("cust".equals(parser.getName())) {
                            String custMCC = parser.getAttributeValue(null, "mcc");
                            String custMNC = parser.getAttributeValue(null, "mnc");
                            if (hwCustSetupPhoneAccount != null) {
                                hwCustSetupPhoneAccount.getSpnInfoFromXML(parser);
                            }
                            if (HwLog.HWDBG) {
                                HwLog.d("SetupPhoneAccount", "parseAndLoadPreDefinedContacts: current MNCC = " + currentMCC + custMNC);
                                HwLog.d("SetupPhoneAccount", "parseAndLoadPreDefinedContacts: cust MNCC = " + custMCC + custMNC);
                            }
                            if ((!currentMCC.equals(custMCC) && !"*".equals(custMCC)) || (!currentMNC.equals(custMNC) && !"*".equals(custMNC))) {
                                while (1 != eventType) {
                                    if ("cust".equals(parser.getName()) && 3 == eventType) {
                                        break;
                                    }
                                    eventType = parser.next();
                                }
                            } else if (hwCustSetupPhoneAccount != null) {
                                hwCustSetupPhoneAccount.parserContactsAndGroup(parser, mContactList, groups, eventType);
                            } else {
                                parserContactsAndGroup(parser, groups);
                            }
                        }
                    }
                    eventType = parser.next();
                }
                if (lInput != null) {
                    try {
                        lInput.close();
                    } catch (IOException e22) {
                        HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e22.getMessage());
                        e22.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e3) {
                e = e3;
                inputStream = lInput;
            } catch (XmlPullParserException e4) {
                xmp = e4;
                inputStream = lInput;
            } catch (IOException e5) {
                ioe = e5;
                inputStream = lInput;
            } catch (Throwable th2) {
                th = th2;
                inputStream = lInput;
            }
        } catch (FileNotFoundException e6) {
            e = e6;
            try {
                e.printStackTrace();
                HwLog.w("SetupPhoneAccount", "File not found for loading pre-loaded contacts");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222) {
                        HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e222.getMessage());
                        e222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2222) {
                        HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e2222.getMessage());
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e7) {
            xmp = e7;
            xmp.printStackTrace();
            HwLog.e("SetupPhoneAccount", "Exception occured while parsing xml " + xmp.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e22222) {
                    HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e22222.getMessage());
                    e22222.printStackTrace();
                }
            }
        } catch (IOException e8) {
            ioe = e8;
            ioe.printStackTrace();
            HwLog.e("SetupPhoneAccount", "Exception occured while parsing xml " + ioe.getMessage());
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e222222) {
                    HwLog.e("SetupPhoneAccount", "Cannot close pre-defined file " + e222222.getMessage());
                    e222222.printStackTrace();
                }
            }
        }
    }

    public static void parserContactsAndGroup(XmlPullParser parser, ArrayList<String> groups) {
        if (parser != null && mContactList != null && groups != null) {
            try {
                int eventType = parser.getEventType();
                while (1 != eventType && (!"cust".equals(parser.getName()) || 3 != eventType)) {
                    if ("contact".equals(parser.getName())) {
                        Contact contact = new Contact();
                        while (1 != eventType) {
                            if (2 != eventType) {
                                if (3 == eventType && "contact".equals(parser.getName())) {
                                    break;
                                }
                            }
                            String tag = parser.getName();
                            if ("name".equals(tag)) {
                                contact.name = parser.nextText();
                            } else if ("phone".equals(tag)) {
                                contact.addPhone(parser.getAttributeValue(null, "type"), parser.nextText());
                            } else if (Scopes.EMAIL.equals(tag)) {
                                contact.addEmail(parser.getAttributeValue(null, "type"), parser.nextText());
                            } else if ("address".equals(tag)) {
                                contact.addAddress(parser.getAttributeValue(null, "type"), parser.nextText());
                            } else if ("avatar".equals(tag)) {
                                contact.avatarPath = parser.nextText();
                            } else if ("company".equals(tag)) {
                                contact.company = parser.nextText();
                            } else {
                                HwLog.i("SetupPhoneAccount", " No match ");
                            }
                            eventType = parser.next();
                        }
                        if (!mContactList.contains(contact)) {
                            mContactList.add(contact);
                        }
                    } else if ("group".equals(parser.getName())) {
                        while (true) {
                            if (2 != eventType) {
                                if (3 == eventType && "group".equals(parser.getName())) {
                                    break;
                                }
                            } else if (parser.getName().equals("name")) {
                                groups.add(parser.nextText());
                            }
                            eventType = parser.next();
                        }
                    }
                    eventType = parser.next();
                }
            } catch (XmlPullParserException xmp) {
                xmp.printStackTrace();
                HwLog.e("SetupPhoneAccount", "Exception occured while parsing xml " + xmp.getMessage());
            } catch (IOException ioe) {
                ioe.printStackTrace();
                HwLog.e("SetupPhoneAccount", "Exception occured while parsing xml " + ioe.getMessage());
            }
        }
    }

    private static boolean saveToDatabase(ArrayList<Contact> contactList, Context aContext) {
        if (contactList.size() == 0) {
            HwLog.d("SetupPhoneAccount", "Empty list encountered");
            return false;
        }
        try {
            ArrayList<ContentProviderOperation> lOperation = new ArrayList();
            for (Contact contact : contactList) {
                int backReference = lOperation.size();
                Builder lBuilder = ContentProviderOperation.newInsert(RawContacts.CONTENT_URI);
                lBuilder.withValue("account_name", "Phone");
                lBuilder.withValue("account_type", "com.android.huawei.phone");
                lBuilder.withValue("sync4", "PREDEFINED_HUAWEI_CONTACT");
                if (HLUtils.isShowHotNumberOnTop && contact.company != null) {
                    lBuilder.withValue("is_care", Integer.valueOf(1));
                }
                lOperation.add(lBuilder.build());
                lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                lBuilder.withValue("mimetype", "vnd.android.cursor.item/name");
                lBuilder.withValue("data1", contact.name);
                lBuilder.withValue("data2", contact.name);
                lBuilder.withValueBackReference("raw_contact_id", backReference);
                lOperation.add(lBuilder.build());
                if (contact.company != null) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValue("data1", contact.company);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/organization");
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    lOperation.add(lBuilder.build());
                }
                if (contact.avatarPath != null) {
                    Bitmap photo = BitmapFactory.decodeFile(contact.avatarPath);
                    HwLog.i("SetupPhoneAccount", "Pre-loaded contact has a image path - " + contact.avatarPath + " , Bitmap after parsing is - " + photo);
                    if (photo != null) {
                        OutputStream stream = new ByteArrayOutputStream();
                        lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                        lBuilder.withValue("mimetype", "vnd.android.cursor.item/photo");
                        photo.compress(CompressFormat.JPEG, 75, stream);
                        lBuilder.withValue("data15", stream.toByteArray());
                        photo.recycle();
                        lBuilder.withValueBackReference("raw_contact_id", backReference);
                        lOperation.add(lBuilder.build());
                    }
                }
                for (PhoneDetail phoneDetail : contact.numbers) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
                    lBuilder.withValue("data1", phoneDetail.number);
                    lBuilder.withValue("data2", Integer.valueOf(phoneDetail.type));
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    lOperation.add(lBuilder.build());
                }
                for (EmailDetail emailDetail : contact.emails) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/email_v2");
                    lBuilder.withValue("data1", emailDetail.email);
                    lBuilder.withValue("data2", Integer.valueOf(emailDetail.type));
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    lOperation.add(lBuilder.build());
                }
                for (AddrDetail addrDetail : contact.address) {
                    lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                    lBuilder.withValue("mimetype", "vnd.android.cursor.item/postal-address_v2");
                    lBuilder.withValue("data2", Integer.valueOf(addrDetail.type));
                    lBuilder.withValue("data1", addrDetail.address);
                    lBuilder.withValueBackReference("raw_contact_id", backReference);
                    lOperation.add(lBuilder.build());
                }
            }
            if (lOperation.size() > 0) {
                int retryTime = 0;
                boolean applyBatchSuccess = false;
                while (!applyBatchSuccess && retryTime < 3) {
                    try {
                        aContext.getContentResolver().applyBatch("com.android.contacts", lOperation);
                        applyBatchSuccess = true;
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                        try {
                            Thread.sleep(3000);
                        } catch (InterruptedException e2) {
                        }
                        retryTime++;
                    }
                }
            }
            if (EmuiFeatureManager.isProductCustFeatureEnable()) {
                HwCustSetupPhoneAccountHelper mCust = (HwCustSetupPhoneAccountHelper) HwCustUtils.createObj(HwCustSetupPhoneAccountHelper.class, new Object[0]);
                if (mCust != null) {
                    mCust.customizePredefinedContactsAndGroups(aContext);
                }
            }
        } catch (RemoteException e3) {
            e3.printStackTrace();
        } catch (OperationApplicationException e4) {
            e4.printStackTrace();
        }
        return true;
    }
}
