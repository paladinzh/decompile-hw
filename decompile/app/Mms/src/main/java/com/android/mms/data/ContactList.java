package com.android.mms.data;

import android.net.Uri;
import android.os.Parcelable;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.util.StringUtils;
import com.android.mms.ui.MessageUtils;
import com.huawei.mms.util.HwMessageUtils;
import java.util.ArrayList;
import java.util.List;

public class ContactList extends ArrayList<Contact> {
    private static final String[] IPHEAD = new String[]{"17900", "17901", StringUtils.phoneFiled17908, StringUtils.phoneFiled17909, "11808", "17950", "17951", "12593", "17931", "17910", StringUtils.phoneFiled17911, "17960", "17968", "17969", StringUtils.phoneFiled10193, "96435"};
    private static final long serialVersionUID = 1;

    public static ContactList getByNumbers(Iterable<String> numbers, boolean canBlock) {
        return getByNumbers((Iterable) numbers, canBlock, false);
    }

    public static ContactList getByNumbers(Iterable<String> numbers, boolean canBlock, boolean replaceNumber) {
        ContactList list = new ContactList();
        for (String number : numbers) {
            if (!TextUtils.isEmpty(number)) {
                Contact contact = Contact.get(number, canBlock);
                if (replaceNumber && !TextUtils.equals(contact.getOriginNumber(), number)) {
                    contact.setOriginNumber(number);
                    contact.setNumber(number);
                }
                list.add(contact);
            }
        }
        return list;
    }

    public static ContactList getByNumbers(String semiSepNumbers, boolean canBlock, boolean replaceNumber) {
        ContactList list = new ContactList();
        for (String number : semiSepNumbers.split(";")) {
            if (!TextUtils.isEmpty(number)) {
                Contact contact = Contact.get(number, canBlock);
                contact.setOriginNumber(number);
                if (replaceNumber && !TextUtils.equals(number, contact.getNumber())) {
                    contact.setNumber(number);
                }
                list.add(contact);
            }
        }
        return list;
    }

    public static ContactList blockingGetByUris(Parcelable[] uris) {
        ContactList list = new ContactList();
        if (uris != null && uris.length > 0) {
            for (Parcelable p : uris) {
                Uri uri = (Uri) p;
                if (!(uri == null || uri.getScheme() == null || !"tel".equals(uri.getScheme()))) {
                    String number = MessageUtils.parseMmsAddress(uri.getSchemeSpecificPart(), true);
                    if (TextUtils.isEmpty(number)) {
                        number = uri.getSchemeSpecificPart();
                    }
                    list.add(Contact.get(number, true));
                }
            }
            List<Contact> contacts = Contact.getByPhoneUris(uris);
            if (contacts != null) {
                list.addAll(contacts);
            }
        }
        return list;
    }

    public static ContactList getByIds(String spaceSepIds, boolean canBlock) {
        if (spaceSepIds.indexOf(32) > 0) {
            return RecipientIdCache.getContacts(spaceSepIds, canBlock);
        }
        return RecipientIdCache.getSingleContact(spaceSepIds, canBlock);
    }

    public String formatNames(String separator) {
        if (size() == 1) {
            Contact contact = (Contact) get(0);
            if (contact == null) {
                return "";
            }
            contact.checkAndUpdateContact();
            return contact.getName();
        }
        String[] names = new String[size()];
        int i = 0;
        for (Contact c : this) {
            if (c != null) {
                c.checkAndUpdateContact();
                int i2 = i + 1;
                names[i] = c.getName();
                i = i2;
            }
        }
        return TextUtils.join(separator, names);
    }

    public String getPurpose() {
        if (size() == 1) {
            return ((Contact) get(0)).getPurpose();
        }
        return null;
    }

    public String formatNamesAndNumbers(String separator) {
        String[] nans = new String[size()];
        int i = 0;
        for (Contact c : this) {
            int i2 = i + 1;
            nans[i] = c.getNameAndNumber();
            i = i2;
        }
        return TextUtils.join(separator, nans);
    }

    public void removeIPAndZeroPrefixForChina() {
        for (Contact current : this) {
            String num = current.getNumber();
            if (num.length() >= 16) {
                String[] strArr = IPHEAD;
                int length = strArr.length;
                int i = 0;
                while (i < length) {
                    if (num.startsWith(strArr[i])) {
                        num = num.substring(5);
                        if (num.length() == 12 && num.charAt(0) == '0') {
                            num = num.substring(1);
                        }
                        current.setNumber(num);
                    } else {
                        i++;
                    }
                }
            }
        }
    }

    public String serialize() {
        return TextUtils.join(";", getNumbers());
    }

    public boolean containsEmail() {
        for (Contact c : this) {
            if (c.isEmail()) {
                return true;
            }
        }
        return false;
    }

    public String[] getNumbers() {
        return getNumbers(false);
    }

    public String[] getNumbers(boolean scrubForMmsAddress) {
        List<String> numbers = new ArrayList();
        for (Contact c : this) {
            String number = c.getNumber();
            if (scrubForMmsAddress) {
                String formatedNumber = MessageUtils.parseMmsAddress(number, true);
                if (!TextUtils.isEmpty(formatedNumber)) {
                    number = formatedNumber;
                }
            }
            if (!(TextUtils.isEmpty(number) || numbers.contains(number))) {
                numbers.add(number);
            }
        }
        return (String[]) numbers.toArray(new String[numbers.size()]);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        try {
            ContactList other = (ContactList) obj;
            if (size() != other.size()) {
                return false;
            }
            for (Contact c : this) {
                if (!other.contains(c)) {
                    return false;
                }
            }
            return true;
        } catch (ClassCastException e) {
            return false;
        }
    }

    public int hashCode() {
        return iterator().hashCode();
    }

    public String formatNoNameContactNumber(String separator) {
        String[] names = new String[size()];
        int i = 0;
        for (Contact c : this) {
            int i2 = i + 1;
            names[i] = HwMessageUtils.formatNumberString(c.getName());
            i = i2;
        }
        return TextUtils.join(separator, names);
    }
}
