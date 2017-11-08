package com.android.contacts.list;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.SystemProperties;
import android.provider.ContactsContract.RawContacts;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import com.android.contacts.activities.HwCustCommonUtilMethods;
import com.android.contacts.activities.IceActivity;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.util.Constants;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.ArrayList;

public class HwCustDefaultContactBrowseListFragmentImpl extends HwCustDefaultContactBrowseListFragment {
    private static final String TAG = "HwCustDefaultContactBrowseListFragmentImpl";

    public boolean onOptionsItemSelectedForCust(MenuItem item, Context context) {
        if (isAddShareMenu() && item.getItemId() == R.id.menu_share) {
            HwCustCommonUtilMethods.startShareContacts(context);
            return true;
        } else if (!HwCustCommonConstants.IS_AAB_ATT || !item.getTitle().equals(context.getResources().getString(R.string.menu_att_accounts))) {
            return false;
        } else {
            HwCustCommonUtilMethods.startAABClient(context, true);
            return true;
        }
    }

    public boolean supportReadOnly() {
        return HwCustContactFeatureUtils.isSupportPredefinedReadOnlyFeature();
    }

    public boolean isReadOnlyContact(long contactID, Context context) {
        boolean readonly = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, "contact_id = ? AND raw_contact_is_read_only = 1", new String[]{String.valueOf(contactID)}, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return false;
            }
            readonly = cursor.getCount() >= 1;
            if (cursor != null) {
                cursor.close();
            }
            return readonly;
        } catch (SQLiteException e) {
            Log.i(TAG, "getDataFromDB() cannot query the entries ..");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void customizeListHeaderViews(DefaultContactBrowseListFragment fragment, LayoutInflater inflater, ArrayList<View> mHeaderViews) {
        if (isSupportICERequired()) {
            addHeaderView(fragment, inflater, R.string.ice_emergency_contact_title, new Intent(fragment.getActivity(), IceActivity.class), mHeaderViews);
        }
    }

    private void addHeaderView(DefaultContactBrowseListFragment fragment, LayoutInflater inflater, int resId, Intent intent, ArrayList<View> mHeaderViews) {
        ListView listView = fragment.getListView();
        if (listView != null) {
            View view = inflater.inflate(R.layout.contact_list_group_item_view, null, false);
            ((ImageView) view.findViewById(R.id.photo)).setImageResource(R.drawable.contact_list_groups);
            TextView nameView = (TextView) view.findViewById(R.id.name);
            nameView.setText(resId);
            if (Constants.isEXTRA_HUGE()) {
                nameView.setTextSize(1, 28.0f);
                nameView.setSingleLine(Boolean.TRUE.booleanValue());
            }
            ViewUtil.setStateListIcon(fragment.getActivity(), (ImageView) view.findViewById(R.id.action));
            view.setTag(intent);
            listView.addHeaderView(view);
            mHeaderViews.add(view);
        }
    }

    public boolean isSupportICERequired() {
        return HwCustContactFeatureUtils.isSupportIceEmergencyContacts();
    }

    private boolean isAddShareMenu() {
        return HwCustContactFeatureUtils.isMoveShareContactsToMainMenu();
    }

    public boolean isRemoveShareContacts() {
        if (!"237".equals(SystemProperties.get("ro.config.hw_opta", "")) || HwCustContactFeatureUtils.isMoveShareContactsToMainMenu()) {
            return false;
        }
        return true;
    }

    public void makeMenuItemInVisible(Menu menu) {
        if (menu != null) {
            menu.findItem(R.id.menu_share).setVisible(false);
        }
    }
}
