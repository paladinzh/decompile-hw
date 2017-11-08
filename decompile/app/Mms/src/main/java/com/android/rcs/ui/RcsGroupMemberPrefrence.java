package com.android.rcs.ui;

import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import com.google.android.gms.R;

public class RcsGroupMemberPrefrence extends Preference {
    private BaseAdapter mAdapter = null;

    public RcsGroupMemberPrefrence(Context context) {
        super(context);
    }

    public RcsGroupMemberPrefrence(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public RcsGroupMemberPrefrence(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    protected View onCreateView(ViewGroup parent) {
        return LayoutInflater.from(getContext()).inflate(R.layout.rcs_groupchat_detail_preference_group_member, parent, false);
    }

    public void setAdapter(BaseAdapter adapter) {
        this.mAdapter = adapter;
        notifyChanged();
    }

    protected void onBindView(View view) {
        super.onBindView(view);
        GridView gv = (GridView) view.findViewById(R.id.grid_group_member);
        if (gv != null && gv.getAdapter() == null) {
            gv.setAdapter(this.mAdapter);
        }
    }
}
