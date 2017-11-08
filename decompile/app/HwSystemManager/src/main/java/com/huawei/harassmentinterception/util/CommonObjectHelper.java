package com.huawei.harassmentinterception.util;

import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import java.util.ArrayList;
import java.util.List;

public class CommonObjectHelper {
    public static void setItemCheckState(List<? extends ContactInfo> list, int pos, boolean enable) {
        ((ContactInfo) list.get(pos)).setSelected(enable);
    }

    public static boolean isOverLastData(List<? extends ContactInfo> list, int pos) {
        return pos >= list.size();
    }

    public static int getCheckedItemNum(List<? extends ContactInfo> list) {
        int num = 0;
        for (ContactInfo contact : list) {
            if (contact.isSelected()) {
                num++;
            }
        }
        return num;
    }

    public static ArrayList<ContactInfo> getCheckedItems(List<? extends ContactInfo> list) {
        ArrayList<ContactInfo> phoneList = new ArrayList();
        for (ContactInfo contact : list) {
            if (contact.isSelected()) {
                phoneList.add(contact);
            }
        }
        return phoneList;
    }

    public static boolean getItemCheckState(List<? extends ContactInfo> list, int pos) {
        if (pos < list.size()) {
            return ((ContactInfo) list.get(pos)).isSelected();
        }
        return false;
    }

    public static void doSelect(List<? extends ContactInfo> list, boolean enable) {
        for (ContactInfo contact : list) {
            contact.setSelected(enable);
        }
    }

    public static boolean isAllItemChecked(List<? extends ContactInfo> list, int num) {
        return num == list.size();
    }

    public static void deleteItems(List<? extends ContactInfo> list, Object obj) {
        if (obj != null && obj.getClass() == ArrayList.class) {
            list.removeAll((ArrayList) obj);
        }
    }
}
