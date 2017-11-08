package com.huawei.mms.ui;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.huawei.mms.ui.PeopleActionBar.PeopleActionBarAdapter;

public class HwCustPeopleActionBar {

    public interface IHwCustPeopleActionBarCallback {
        int getMenuAddBlacklistId();

        int getMenuRemBlacklistId();

        boolean isActionBarExpand();

        void setMenuBlacklistVisibility(int i);

        void setMenuWeichatVisibility(int i);

        void updateMenu();
    }

    public interface PeopleActionBarAdapterExt {
        void createGroupChat();

        boolean isRcsGroupChat();

        void showRcsGroupChatDetail();
    }

    public void setMenuMultiLine(EmuiMenuText menuStart, EmuiMenuText menuMid, EmuiMenuText menuEnd) {
    }

    public void initRcsProfile(PeopleActionBarAdapter peopelAdapter) {
    }

    public void initRcsExtProfile(PeopleActionBarAdapterExt extAdapter) {
    }

    public void initRcsView(View view, Context context) {
    }

    public void updateRcsMenu(boolean isGroup) {
    }

    public void updateRcsMenu(boolean isGroup, LinearLayout mMenuHolder, boolean isBlacklistFeatureEnable, boolean hasWeichat) {
    }

    public boolean isRcsGroupChat() {
        return false;
    }

    public boolean changeToGroupChatMenu() {
        return false;
    }

    public boolean changeToGroupChatIcon(EmuiAvatarImage view) {
        return false;
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public TextView updateTitle(Context context, View view, TextView title) {
        return title;
    }

    public void showComposingStatus(Context context, boolean isTyping) {
    }

    public void setGroupMemberCount(boolean isRcsGroupChat, String number, TextView title, TextView subTitle) {
    }

    public void updateGroupMenuHolderLayout(boolean isLandscape, boolean mIsExpanded) {
    }

    public void resetParaLayoutGroup(boolean mIsExpanded) {
    }

    public boolean isShowMenuHolder(boolean show) {
        return show;
    }

    public void setHwCustCallback(IHwCustPeopleActionBarCallback callback) {
    }

    public void updateSearchMode(boolean aSearchmode, ViewGroup aTitleHolder, View aProfileView) {
    }

    public void updateMenuItems(EmuiMenuText aMenuThird, EmuiMenuText aMenuFourth, String[] aMoreMenuOperationStrings, int[] aMoreMenuItemIds) {
    }

    public String[] getMoreMenuString(String[] aMoreMenuOperationStrings) {
        return aMoreMenuOperationStrings;
    }

    public int[] getmMoreMenuItemIds(int[] aMoreMenuItemIds) {
        return aMoreMenuItemIds;
    }
}
