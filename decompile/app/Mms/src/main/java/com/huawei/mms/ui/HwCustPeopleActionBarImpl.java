package com.huawei.mms.ui;

import com.android.mms.HwCustMmsConfigImpl;

public class HwCustPeopleActionBarImpl extends HwCustPeopleActionBar {
    public void setMenuMultiLine(EmuiMenuText menuStart, EmuiMenuText menuMid, EmuiMenuText menuEnd) {
        if (HwCustMmsConfigImpl.getEnablePeopleActionBarMultiLine()) {
            if (menuStart != null) {
                menuStart.setSingleLine(false);
            }
            if (menuMid != null) {
                menuMid.setSingleLine(false);
            }
            if (menuEnd != null) {
                menuEnd.setSingleLine(false);
            }
        }
    }
}
