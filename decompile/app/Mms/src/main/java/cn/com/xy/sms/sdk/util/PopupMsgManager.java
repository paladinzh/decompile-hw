package cn.com.xy.sms.sdk.util;

import android.content.Context;
import cn.com.xy.sms.sdk.smsmessage.BusinessSmsMessage;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/* compiled from: Unknown */
public class PopupMsgManager {
    public static LinkedList<BusinessSmsMessage> businessSmsList = new LinkedList();
    public static HashSet<String> hasPhoneThird = new HashSet();
    public static boolean hasRemoveData = false;
    public static List<String> removePhoneNumList = new ArrayList();

    public static synchronized int addAllToFirst(LinkedList<BusinessSmsMessage> linkedList) {
        int i;
        synchronized (PopupMsgManager.class) {
            i = -1;
            int size = linkedList.size() - 1;
            if (size >= 0) {
                int size2 = businessSmsList.size();
                int i2 = size;
                while (i2 >= 0) {
                    int i3;
                    BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) linkedList.get(i2);
                    Object value = businessSmsMessage.getValue("opensms_enable");
                    String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(businessSmsMessage.getOriginatingAddress());
                    if (value == null || !"false".equalsIgnoreCase(value.toString())) {
                        if (!removePhoneNumList.contains(phoneNumberNo86)) {
                            businessSmsList.addFirst(businessSmsMessage);
                            i3 = i;
                            i2--;
                            i = i3;
                        }
                    }
                    if (size2 == 0 && size == 0) {
                        businessSmsList.addFirst(businessSmsMessage);
                    }
                    i3 = 1;
                    i2--;
                    i = i3;
                }
            }
            removePhoneNumList.clear();
            linkedList.clear();
        }
        return i;
    }

    public static boolean addThirdPopupMsgData(Map<String, Object> map) {
        Object obj;
        boolean z;
        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86((String) map.get("phoneNumber"));
        if (businessSmsList == null || businessSmsList.isEmpty()) {
            obj = null;
            z = false;
        } else {
            Iterator it = businessSmsList.iterator();
            obj = null;
            while (it != null && it.hasNext()) {
                BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) it.next();
                if (businessSmsMessage != null && businessSmsMessage.messageBody == null) {
                    String phoneNumberNo862 = StringUtils.getPhoneNumberNo86(businessSmsMessage.getOriginatingAddress());
                    if (!(StringUtils.isNull(phoneNumberNo86) || StringUtils.isNull(phoneNumberNo862) || !phoneNumberNo86.equals(phoneNumberNo862))) {
                        z = true;
                        break;
                    }
                }
            }
            z = false;
        }
        if (z && obj != null) {
            obj.valueMap = map;
            obj.originatingAddress = phoneNumberNo86;
            businessSmsList.remove(obj);
            if (!hasPhoneThird.contains(phoneNumberNo86)) {
                hasPhoneThird.add(phoneNumberNo86);
            }
        } else {
            obj = new BusinessSmsMessage();
            obj.valueMap = map;
            obj.originatingAddress = phoneNumberNo86;
        }
        if (obj != null) {
            businessSmsList.addLast(obj);
        }
        return true;
    }

    public static synchronized void clearBusinessMessage(List<BusinessSmsMessage> list) {
        synchronized (PopupMsgManager.class) {
            if (list != null) {
                businessSmsList.removeAll(list);
                list.size();
            }
        }
    }

    public static void clearPhoneThird() {
        hasPhoneThird.clear();
    }

    public static void clearPopup() {
        businessSmsList.clear();
        DuoquUtils.getSdkDoAction().clearPopup();
    }

    public static synchronized int clearUserClickBusinessMessage() {
        synchronized (PopupMsgManager.class) {
            int size = businessSmsList.size();
            Object arrayList = new ArrayList();
            for (int i = 0; i < size; i++) {
                Object value = ((BusinessSmsMessage) businessSmsList.get(i)).getValue("opensms_enable");
                if (value != null && "false".equalsIgnoreCase(value.toString())) {
                    arrayList.add((BusinessSmsMessage) businessSmsList.get(i));
                }
            }
            if (arrayList.isEmpty()) {
                return -1;
            }
            businessSmsList.removeAll(arrayList);
            arrayList.clear();
            return 1;
        }
    }

    public static boolean containsPhoneThird(String str) {
        return hasPhoneThird.contains(str);
    }

    public static synchronized int getBusinessMessageSize() {
        int size;
        synchronized (PopupMsgManager.class) {
            size = businessSmsList.size();
        }
        return size;
    }

    public static synchronized BusinessSmsMessage getBussinessMessageByIndex(int i) {
        synchronized (PopupMsgManager.class) {
            if (i >= 0) {
                try {
                    if (i < businessSmsList.size()) {
                        BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) businessSmsList.get(i);
                        return businessSmsMessage;
                    }
                } catch (Throwable th) {
                }
            }
            return null;
        }
    }

    public static boolean getHasRemoveData() {
        return hasRemoveData;
    }

    public static synchronized boolean removeBusinessMessage(BusinessSmsMessage businessSmsMessage) {
        boolean remove;
        synchronized (PopupMsgManager.class) {
            try {
                remove = businessSmsList.remove(businessSmsMessage);
            } catch (Throwable th) {
                return false;
            }
        }
        return remove;
    }

    public static synchronized BusinessSmsMessage removeBusinessMessageByIndex(int i) {
        BusinessSmsMessage businessSmsMessage;
        synchronized (PopupMsgManager.class) {
            try {
                businessSmsMessage = (BusinessSmsMessage) businessSmsList.remove(i);
            } catch (Throwable th) {
                return null;
            }
        }
        return businessSmsMessage;
    }

    public static void removeBusinessMessageByNum(Context context, String str, boolean z, Map<String, String> map) {
        try {
            if (!(StringUtils.isNull(str) || businessSmsList == null || businessSmsList.isEmpty())) {
                Iterator it = businessSmsList.iterator();
                while (it != null && it.hasNext()) {
                    BusinessSmsMessage businessSmsMessage = (BusinessSmsMessage) it.next();
                    if (businessSmsMessage != null) {
                        String phoneNumberNo86 = StringUtils.getPhoneNumberNo86(str);
                        String phoneNumberNo862 = StringUtils.getPhoneNumberNo86(businessSmsMessage.getOriginatingAddress());
                        if (!(StringUtils.isNull(phoneNumberNo86) || StringUtils.isNull(phoneNumberNo862) || !phoneNumberNo86.equals(phoneNumberNo862))) {
                            if (!z) {
                                if (!removePhoneNumList.contains(phoneNumberNo86)) {
                                    removePhoneNumList.add(phoneNumberNo86);
                                }
                                it.remove();
                                hasRemoveData = true;
                            } else if (z && businessSmsMessage.messageBody == null) {
                                it.remove();
                            }
                        }
                    }
                }
            }
        } catch (Throwable th) {
        }
    }

    public static void removePhoneThird(String str) {
        hasPhoneThird.remove(str);
    }

    public static void setHasRemoveData(boolean z) {
        hasRemoveData = z;
    }
}
