package com.android.settings;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import com.android.internal.R;
import java.util.ArrayList;

public class ListSpinner extends Spinner {
    private ListAdapter mListAdapter;
    private AlertDialog mPopup;
    private TextView mTitle;

    private static class AlertAdapter implements ListAdapter, SpinnerAdapter {
        private SpinnerAdapter mAdapter;
        private ListAdapter mListAdapter;

        public AlertAdapter(SpinnerAdapter adapter) {
            this.mAdapter = adapter;
            if (adapter instanceof ListAdapter) {
                this.mListAdapter = (ListAdapter) adapter;
            }
        }

        public int getCount() {
            return this.mAdapter == null ? 0 : this.mAdapter.getCount();
        }

        public Object getItem(int position) {
            return this.mAdapter == null ? null : this.mAdapter.getItem(position);
        }

        public long getItemId(int position) {
            return this.mAdapter == null ? -1 : this.mAdapter.getItemId(position);
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getDropDownView(position, convertView, parent);
        }

        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return this.mAdapter == null ? null : this.mAdapter.getDropDownView(position, convertView, parent);
        }

        public boolean hasStableIds() {
            return this.mAdapter != null ? this.mAdapter.hasStableIds() : false;
        }

        public void registerDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.registerDataSetObserver(observer);
            }
        }

        public void unregisterDataSetObserver(DataSetObserver observer) {
            if (this.mAdapter != null) {
                this.mAdapter.unregisterDataSetObserver(observer);
            }
        }

        public boolean areAllItemsEnabled() {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.areAllItemsEnabled();
            }
            return true;
        }

        public boolean isEnabled(int position) {
            ListAdapter adapter = this.mListAdapter;
            if (adapter != null) {
                return adapter.isEnabled(position);
            }
            return true;
        }

        public int getItemViewType(int position) {
            return 0;
        }

        public int getViewTypeCount() {
            return 1;
        }

        public boolean isEmpty() {
            return getCount() == 0;
        }
    }

    public ListSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCharSequenceArray(context.obtainStyledAttributes(attrs, R.styleable.AbsSpinner).getTextArray(0));
        setBackground(null);
    }

    public ListSpinner(Context context) {
        this(context, null);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.mTitle = (TextView) findViewById(16908310);
        if (this.mTitle != null && this.mTitle.getText() != null && !this.mTitle.getText().equals(getPrompt())) {
            this.mTitle.setText(getPrompt());
        }
    }

    public void setCharSequenceArray(CharSequence[] entries) {
        if (entries != null && entries.length > 0) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(getContext(), 2130968981, 16908308, entries);
            super.setAdapter(adapter);
            adapter.setDropDownViewResource(2130969135);
            this.mListAdapter = new AlertAdapter(adapter);
        }
    }

    public void setAdapter(SpinnerAdapter spinnerAdapter) {
        if (spinnerAdapter instanceof ArrayAdapter) {
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter) spinnerAdapter;
            ArrayList<CharSequence> list = new ArrayList();
            int count = adapter.getCount();
            for (int i = 0; i < count; i++) {
                list.add((CharSequence) adapter.getItem(i));
            }
            CharSequence[] entries = new CharSequence[list.size()];
            list.toArray(entries);
            setCharSequenceArray(entries);
        }
    }

    public boolean performClick() {
        try {
            this.mPopup = new Builder(getContext()).setTitle(getPrompt()).setSingleChoiceItems(this.mListAdapter, getSelectedItemPosition(), new OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    ListSpinner.this.setSelection(which);
                    ListSpinner.this.performItemClick(null, which, ListSpinner.this.mListAdapter.getItemId(which));
                    ListSpinner.this.mPopup.dismiss();
                }
            }).setNegativeButton(17039360, null).create();
            if (!(this.mPopup == null || this.mPopup.isShowing())) {
                this.mPopup.show();
            }
            return true;
        } catch (Exception e) {
            MLog.d("ListSpinner", e.getMessage());
            return true;
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 2) {
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void dismissDialog() {
        if (this.mPopup != null && this.mPopup.isShowing()) {
            this.mPopup.dismiss();
        }
    }
}
