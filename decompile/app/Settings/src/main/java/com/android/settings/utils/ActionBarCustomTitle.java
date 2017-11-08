package com.android.settings.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import java.text.NumberFormat;

public class ActionBarCustomTitle {
    private RelativeLayout mTitleLayout;
    private TextView mTitleNumView;
    private TextView mTitleTextView;

    public ActionBarCustomTitle(Context context) {
        initCustomTitle(context, (LayoutInflater) context.getSystemService("layout_inflater"));
    }

    private void initCustomTitle(Context context, LayoutInflater inflater) {
        this.mTitleLayout = (RelativeLayout) inflater.inflate(2130968690, null, false);
        View titleContainer = this.mTitleLayout.findViewById(2131886408);
        this.mTitleTextView = (TextView) titleContainer.findViewById(2131886308);
        this.mTitleNumView = (TextView) titleContainer.findViewById(2131886409);
    }

    public void setCustomTitle(int selectedCount) {
        if (selectedCount > 0) {
            this.mTitleNumView.setFocusable(true);
            NumberFormat nFormat = NumberFormat.getInstance();
            if (selectedCount < 10) {
                this.mTitleNumView.setText(" " + nFormat.format((long) selectedCount) + " ");
                this.mTitleNumView.setContentDescription(" " + nFormat.format((long) selectedCount) + " ");
            } else {
                this.mTitleNumView.setText("" + nFormat.format((long) selectedCount));
                this.mTitleNumView.setContentDescription("" + nFormat.format((long) selectedCount));
            }
            this.mTitleNumView.setVisibility(0);
            return;
        }
        this.mTitleNumView.setVisibility(8);
    }

    public void setCustomTitle(String titleString) {
        this.mTitleTextView.setText(titleString);
        this.mTitleTextView.setFocusable(true);
        this.mTitleTextView.setContentDescription(titleString);
    }

    public void setCustomTitle(String titleString, int selectedCount) {
        setCustomTitle(titleString);
        setCustomTitle(selectedCount);
    }

    public RelativeLayout getTitleLayout() {
        return this.mTitleLayout;
    }
}
