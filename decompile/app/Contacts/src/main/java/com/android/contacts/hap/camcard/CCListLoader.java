package com.android.contacts.hap.camcard;

import android.content.Context;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.net.Uri.Builder;
import android.os.Bundle;
import android.text.TextUtils;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.hwsearch.HwSearchCursor.HwSearchContactsCursor;
import com.android.contacts.list.ContactListLoader;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.List;

public class CCListLoader extends ContactListLoader {

    private static class CCardCurosr extends MergeCursor {
        private int mCcardCount;
        private Bundle mExtras;
        private int mNewCount;

        public CCardCurosr(Cursor[] cursors, Bundle extras, int newCount, int ccardCount) {
            super(cursors);
            this.mExtras = extras;
            this.mNewCount = newCount;
            this.mCcardCount = ccardCount;
        }

        public Bundle getExtras() {
            if (this.mExtras == null) {
                this.mExtras = new Bundle();
            }
            this.mExtras.putInt("new_card_count", this.mNewCount);
            this.mExtras.putInt("ccard_count", this.mCcardCount);
            return this.mExtras;
        }
    }

    public CCListLoader(Context context, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        super(context, uri, projection, selection, selectionArgs, sortOrder);
    }

    public Cursor loadInBackground() {
        Cursor cursor = null;
        Cursor ccardCursor;
        if ((!TextUtils.isEmpty(getQueryString())) && QueryUtil.isUseHwSearch()) {
            String groupIds = CamcardGroup.queryPrefinedCCGroups(getContext());
            if (groupIds == null) {
                return null;
            }
            Builder builder = getUri().buildUpon();
            builder.appendQueryParameter("include_groups", groupIds);
            setUri(builder.build());
            ccardCursor = super.loadInBackground();
            if (ccardCursor != null) {
                cursor = new HwSearchContactsCursor(ccardCursor);
            }
            return cursor;
        }
        List<Cursor> cursors = Lists.newArrayList();
        Cursor newCursor = loadNewAdd();
        cursors.add(newCursor);
        ccardCursor = super.loadInBackground();
        cursors.add(ccardCursor);
        return new CCardCurosr((Cursor[]) cursors.toArray(new Cursor[cursors.size()]), ccardCursor == null ? new Bundle() : ccardCursor.getExtras(), newCursor != null ? newCursor.getCount() : 0, ccardCursor != null ? ccardCursor.getCount() : 0);
    }

    private Cursor loadNewAdd() {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        selection.append("is_camcard").append(" = ").append(2).append(" AND ").append("_id").append(" IN (");
        selection.append("SELECT ").append("contact_id").append(" FROM view_data,groups WHERE groups.title = ? AND groups._id = data1 AND ").append("mimetype").append("=?)");
        selectionArgs.add("PREDEFINED_HUAWEI_GROUP_CCARD");
        selectionArgs.add("vnd.android.cursor.item/group_membership");
        return getContext().getContentResolver().query(getUri(), getProjection(), selection.toString(), (String[]) selectionArgs.toArray(new String[0]), getSortOrder());
    }
}
