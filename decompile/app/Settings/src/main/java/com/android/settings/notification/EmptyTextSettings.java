package com.android.settings.notification;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.android.settings.SettingsPreferenceFragment;

public abstract class EmptyTextSettings extends SettingsPreferenceFragment {
    private ImageView mEmptyIcon;
    private TextView mEmptyTv;

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Configuration configuration = getResources().getConfiguration();
        ViewGroup contentRoot = (ViewGroup) getListView().getParent();
        View emptyView = getActivity().getLayoutInflater().inflate(2130968772, contentRoot, false);
        this.mEmptyTv = (TextView) emptyView.findViewById(2131886561);
        this.mEmptyIcon = (ImageView) emptyView.findViewById(2131886560);
        LinearLayout parent = (LinearLayout) this.mEmptyIcon.getParent();
        float density = getResources().getDisplayMetrics().density;
        this.mEmptyIcon.setBackgroundResource(2130838387);
        if (2 == configuration.orientation) {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (68.0f * density), parent.getPaddingEnd(), 0);
        } else {
            parent.setPaddingRelative(parent.getPaddingStart(), (int) (120.0f * density), parent.getPaddingEnd(), 0);
        }
        contentRoot.addView(emptyView);
        setEmptyView(emptyView);
    }

    protected void setEmptyText(int text) {
        this.mEmptyTv.setText(text);
    }
}
