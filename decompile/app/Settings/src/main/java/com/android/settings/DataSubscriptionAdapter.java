package com.android.settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckedTextView;
import com.android.settings.DataSubscriptionActivity.SimState;
import java.util.List;

public class DataSubscriptionAdapter extends BaseAdapter {
    private Context context;
    private List<SimState> simStates;

    public DataSubscriptionAdapter(List<SimState> simStates, Context context) {
        this.simStates = simStates;
        this.context = context;
    }

    public int getCount() {
        return this.simStates.size();
    }

    public Object getItem(int arg0) {
        return this.simStates.get(arg0);
    }

    public long getItemId(int arg0) {
        return (long) arg0;
    }

    public View getView(int arg0, View arg1, ViewGroup arg2) {
        View view = LayoutInflater.from(this.context).inflate(2130968844, null);
        CheckedTextView checkedTextView = (CheckedTextView) view.findViewById(2131886750);
        SimState mSimState = (SimState) this.simStates.get(arg0);
        if (!(checkedTextView == null || mSimState == null)) {
            checkedTextView.setChecked(mSimState.isPrefDataSubscription());
            checkedTextView.setEnabled(mSimState.isActive());
            checkedTextView.setText(mSimState.getTextRes());
        }
        return view;
    }
}
