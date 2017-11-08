package com.android.contacts.hap.calllog;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import com.android.contacts.PhoneCallDetailsHelper;
import com.android.contacts.calllog.CallLogAdapter;
import com.android.contacts.calllog.CallLogAdapter.CallFetcher;
import com.android.contacts.calllog.CallLogListItemViews;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class CallLogMultiSelectionAdapter extends CallLogAdapter {
    private static final String TAG = CallLogMultiSelectionAdapter.class.getSimpleName();
    private CallLogMultiSelectionActivity mActivityRef;
    private int mDistanceBetweenItems;
    private int mLandMultiSelectCallLogNameWidth;
    private int mPortraitMultiSelectCallLogNameWidth;
    private int mRoundSidesWidth;

    public CallLogMultiSelectionAdapter(Context context, CallFetcher callFetcher, ContactInfoHelper contactInfoHelper) {
        super(context, callFetcher, contactInfoHelper);
        this.mActivityRef = (CallLogMultiSelectionActivity) context;
        this.mDistanceBetweenItems = context.getResources().getDimensionPixelSize(R.dimen.call_log_second_line_item_distance);
        this.mRoundSidesWidth = context.getResources().getDimensionPixelSize(R.dimen.contact_round_sides_width);
    }

    protected void bindChildView(View view, Context context, Cursor cursor) {
        super.bindChildView(view, context, cursor);
        bindNewView(view, cursor, context);
    }

    protected void bindGroupView(View view, Context context, Cursor cursor, int groupSize, boolean expanded) {
        super.bindGroupView(view, context, cursor, groupSize, expanded);
        bindNewView(view, cursor, context);
    }

    protected void bindStandAloneView(View view, Context context, Cursor cursor) {
        super.bindStandAloneView(view, context, cursor);
        bindNewView(view, cursor, context);
    }

    private void bindNewView(View view, Cursor cursor, Context context) {
        CallLogListItemViews views = (CallLogListItemViews) view.getTag();
        views.secondaryActionViewLayout.setVisibility(8);
        if (context.getResources().getConfiguration().orientation == 2) {
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                this.mLandMultiSelectCallLogNameWidth = context.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_name_view_width);
            } else {
                this.mLandMultiSelectCallLogNameWidth = context.getResources().getDimensionPixelSize(R.dimen.multi_select_first_line_name_view_width);
            }
            PhoneCallDetailsHelper.adjustNameViewWidth(views, this.mLandMultiSelectCallLogNameWidth, this.mDistanceBetweenItems, this.mRoundSidesWidth);
        } else {
            this.mPortraitMultiSelectCallLogNameWidth = context.getResources().getDimensionPixelSize(R.dimen.call_log_first_line_name_view_width);
            PhoneCallDetailsHelper.adjustNameViewWidth(views, this.mPortraitMultiSelectCallLogNameWidth, this.mDistanceBetweenItems, this.mRoundSidesWidth);
        }
        views.getCheckBox().setChecked(this.mActivityRef.mSelectedIds.contains(Long.valueOf(cursor.getLong(0))));
    }

    protected View newChildView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    protected View newGroupView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    protected View newStandAloneView(Context context, ViewGroup parent) {
        return initializeNewView(context, parent);
    }

    private View initializeNewView(Context context, ViewGroup parent) {
        View view = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(R.layout.calllog_multi_select_item_time_axis, parent, false);
        findAndCacheTimeAxisWidgetViews(context, view, view.findViewById(R.id.call_log_list_item_time_axis_child_content), false);
        CallLogListItemViews views = (CallLogListItemViews) view.getTag();
        views.setCheckBox((CheckBox) view.findViewById(R.id.checkbox));
        views.primaryActionView.setOnClickListener(null);
        views.primaryActionView.setClickable(false);
        views.primaryActionView.setFocusable(false);
        views.primaryActionView.setFocusableInTouchMode(false);
        ((LinearLayout) view).setDescendantFocusability(393216);
        return view;
    }

    protected boolean isFromCallLogFragment() {
        return false;
    }

    public void addGroup(int cursorPosition, int size, boolean expanded) {
        super.addGroup(cursorPosition, size, expanded);
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "cursorPosition ::" + cursorPosition + " size :: " + size);
        }
    }
}
