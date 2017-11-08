package com.android.contacts.util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.hap.utils.ImmersionUtils;
import com.google.android.gms.R;
import java.text.NumberFormat;

public class ActionBarCustomTitle {
    private RelativeLayout mTitleLayout;
    private TextView mTitleNumView;
    private TextView mTitleTextView;

    public ActionBarCustomTitle(Context context, LayoutInflater inflater) {
        initCustomTitle(context, inflater);
    }

    public ActionBarCustomTitle(Context context) {
        initCustomTitle(context, (LayoutInflater) context.getSystemService("layout_inflater"));
    }

    private void initCustomTitle(Context context, LayoutInflater inflater) {
        this.mTitleLayout = (RelativeLayout) inflater.inflate(R.layout.contact_actionbar_customtitle, null, false);
        View titleContainer = this.mTitleLayout.findViewById(R.id.titleContainer);
        this.mTitleTextView = (TextView) titleContainer.findViewById(R.id.text);
        this.mTitleNumView = (TextView) titleContainer.findViewById(R.id.num);
        if (context != null && (context.getResources().getConfiguration().locale.getLanguage().equals("zh") || context.getResources().getConfiguration().locale.getLanguage().equals("en"))) {
            this.mTitleTextView.setTextSize((float) context.getResources().getInteger(R.integer.contact_eidtor_title_size));
            this.mTitleNumView.setTextSize((float) context.getResources().getInteger(R.integer.contact_eidtor_num_size));
        }
        ImmersionUtils.setTextViewOrEditViewImmersonColorLight(context, this.mTitleTextView, false);
        if (ImmersionUtils.getImmersionStyle(context) == 1) {
            this.mTitleNumView.setBackgroundResource(R.drawable.csp_actionbar_number_circle_light);
        }
    }

    public void retInitCustomTitle(Context context, LayoutInflater inflater, boolean isDeleteMode) {
        if (isDeleteMode) {
            initCustomTitle(context, inflater);
            return;
        }
        this.mTitleLayout = (RelativeLayout) inflater.inflate(R.layout.contact_actionbar_lefttitle, null, false);
        this.mTitleTextView = (TextView) this.mTitleLayout.findViewById(R.id.left_titleContainer).findViewById(R.id.left_text);
        this.mTitleTextView.setVisibility(0);
        this.mTitleNumView = null;
        ImmersionUtils.setTextViewOrEditViewImmersonColorLight(context, this.mTitleTextView, false);
    }

    public void setCustomTitle(int selectedCount) {
        if (this.mTitleNumView != null) {
            if (selectedCount > 0) {
                this.mTitleNumView.setFocusable(true);
                NumberFormat nFormat = NumberFormat.getInstance();
                if (selectedCount < 10) {
                    this.mTitleNumView.setText(HwCustPreloadContacts.EMPTY_STRING + nFormat.format((long) selectedCount) + HwCustPreloadContacts.EMPTY_STRING);
                    this.mTitleNumView.setContentDescription(HwCustPreloadContacts.EMPTY_STRING + nFormat.format((long) selectedCount) + HwCustPreloadContacts.EMPTY_STRING);
                } else {
                    this.mTitleNumView.setText("" + nFormat.format((long) selectedCount));
                    this.mTitleNumView.setContentDescription("" + nFormat.format((long) selectedCount));
                }
                this.mTitleNumView.setVisibility(0);
            } else {
                this.mTitleNumView.setVisibility(8);
            }
        }
    }

    public void setCustomTitle(String titleString) {
        this.mTitleTextView.setText(titleString);
        this.mTitleTextView.setFocusable(true);
        this.mTitleTextView.setContentDescription(titleString);
    }

    public void setCustomTitle(String titleString, int selectedCount) {
        setCustomTitle(titleString);
        setCustomTitle(selectedCount);
        setCustomTitleDescription(titleString, selectedCount);
    }

    public RelativeLayout getTitleLayout() {
        return this.mTitleLayout;
    }

    private void setCustomTitleDescription(String titleString, int selectedCount) {
        if (this.mTitleNumView == null) {
            HwLog.i("ActionBarCustomTitle", "setCustomTitleDescription mTitleNumView is NULL");
            return;
        }
        if (selectedCount > 0) {
            this.mTitleTextView.setFocusable(false);
            this.mTitleNumView.setFocusable(false);
            String titleDescription = String.format("%1$s %2$d", new Object[]{titleString, Integer.valueOf(selectedCount)});
            View titleContainer = this.mTitleLayout.findViewById(R.id.titleContainer);
            titleContainer.setFocusable(true);
            titleContainer.setContentDescription(titleDescription);
        }
    }
}
