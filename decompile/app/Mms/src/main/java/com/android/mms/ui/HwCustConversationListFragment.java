package com.android.mms.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.MenuItem;
import android.view.View;
import com.android.mms.data.Conversation;
import com.android.mms.ui.BaseConversationListFragment.BaseFragmentMenu;
import com.huawei.mms.ui.EmuiMenu;
import java.util.List;

public class HwCustConversationListFragment {
    public HwCustConversationListFragment(Context context) {
    }

    public boolean onCustomMenuItemClick(Fragment fragment, MenuItem item) {
        return false;
    }

    public void setRcsMenu(EmuiMenu menu, int id, int groupId, boolean isInLandscape) {
    }

    public void registerOtherListenerOnCreate(BaseFragmentMenu menu) {
    }

    public void unRegisterOtherListenerOnDestroy() {
    }

    public String getNewSelection(int token, Object cookie, String selection) {
        return selection;
    }

    public boolean openRcsThreadId(Conversation conv) {
        return false;
    }

    public long getNewThreadId(Conversation conv, long threadId) {
        return threadId;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    public List<Long> getNewArrayList(List<Long> list) {
        return list;
    }

    public void showGroupInviteDialogIfNeeded() {
    }

    public void inflateRcsDisconnectNotify(View rootView) {
    }

    public void startUserGuide() {
    }

    public void setActionValid() {
    }

    public void onStop() {
    }

    public void onContentChanged() {
    }

    public void clearRcsGroupSubject() {
    }

    public boolean isRcsTable(int tableToUse) {
        return false;
    }

    public String getAddress(Context context, int tableToUse, long groupThreadId, Cursor cursor) {
        return null;
    }

    public String getMatch(int tableToUse, Cursor cursor) {
        return null;
    }

    public boolean isThread(int tableToUse, boolean defaultValue) {
        return defaultValue;
    }
}
