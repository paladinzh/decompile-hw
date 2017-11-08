package com.android.mms.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import com.android.mms.ui.FavoritesFragment.MenuEx;

public class HwCustFavoritesFragment {
    public HwCustFavoritesFragment(Fragment Fragment) {
    }

    public void onOptionsItemSelected(FavoritesListView ListView, FavoritesListAdapter mListAdapter, MenuItem item) {
    }

    public void onSelectChange(MenuEx mMenuEx, FavoritesListView ListView, FavoritesListAdapter mListAdapter, Menu optionMenu) {
    }

    public void onItemLongClick(FavoritesListView ListView, int position) {
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, FavoritesListAdapter listAdapter) {
    }

    public boolean detectMessageToForwardForFt(Integer[] selection, Cursor cursor) {
        return false;
    }

    public void toForward(Context context) {
    }

    public void forwardLoc(Context context) {
    }

    public boolean detectMessageToForwardForLoc(FavoritesListView msgListView, Cursor cursor) {
        return false;
    }

    public boolean isInEditMode(FavoritesListView listView) {
        return false;
    }

    public void switchToEdit(MenuEx mMenuEx) {
    }

    public void onSelectChangeBegin(MenuEx mMenuEx) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }

    public void prepareFwdMsg(String msgBody) {
    }

    public String updateForwardSubject(String aFwdSubject, String aMsgSubject) {
        return aFwdSubject;
    }
}
