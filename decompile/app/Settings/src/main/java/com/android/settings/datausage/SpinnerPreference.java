package com.android.settings.datausage;

import android.content.Context;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceViewHolder;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import com.android.settings.datausage.CycleAdapter.SpinnerInterface;

public class SpinnerPreference extends Preference implements SpinnerInterface {
    private CycleAdapter mAdapter;
    private Object mCurrentObject;
    private OnItemSelectedListener mListener;
    private final OnItemSelectedListener mOnSelectedListener = new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (SpinnerPreference.this.mPosition != position) {
                SpinnerPreference.this.mPosition = position;
                SpinnerPreference.this.mCurrentObject = SpinnerPreference.this.mAdapter.getItem(position);
                SpinnerPreference.this.mListener.onItemSelected(parent, view, position, id);
            }
        }

        public void onNothingSelected(AdapterView<?> parent) {
            SpinnerPreference.this.mListener.onNothingSelected(parent);
        }
    };
    private int mPosition;

    public SpinnerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(2130968724);
    }

    public void setAdapter(CycleAdapter cycleAdapter) {
        this.mAdapter = cycleAdapter;
        notifyChanged();
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mListener = listener;
    }

    public Object getSelectedItem() {
        return this.mCurrentObject;
    }

    public void setSelection(int position) {
        this.mPosition = position;
        this.mCurrentObject = this.mAdapter.getItem(this.mPosition);
        notifyChanged();
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        Spinner spinner = (Spinner) holder.findViewById(2131886458);
        spinner.setAdapter(this.mAdapter);
        spinner.setSelection(this.mPosition);
        spinner.setOnItemSelectedListener(this.mOnSelectedListener);
    }

    protected void performClick(View view) {
        view.findViewById(2131886458).performClick();
    }
}
