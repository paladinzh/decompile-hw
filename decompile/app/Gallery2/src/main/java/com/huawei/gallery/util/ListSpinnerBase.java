package com.huawei.gallery.util;

import android.content.Context;
import android.database.DataSetObserver;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import com.android.internal.R;
import java.util.ArrayList;

public abstract class ListSpinnerBase extends Spinner {
    protected ListAdapter mListAdapter;

    protected static class AlertAdapter implements ListAdapter, SpinnerAdapter {
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

    protected abstract int getCustomLayoutId();

    public ListSpinnerBase(Context context, AttributeSet attrs) {
        super(context, attrs);
        setCharSequenceArray(context.obtainStyledAttributes(attrs, R.styleable.AbsSpinner).getTextArray(0));
        setBackground(null);
    }

    public ListSpinnerBase(Context context) {
        this(context, null);
    }

    public void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
    }

    public void setCharSequenceArray(CharSequence[] entries) {
        if (entries != null && entries.length > 0) {
            ArrayAdapter<CharSequence> adapter = new ArrayAdapter(getContext(), getCustomLayoutId(), 16908308, entries);
            super.setAdapter(adapter);
            adapter.setDropDownViewResource(com.android.gallery3d.R.layout.simple_list_item_single_choice);
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
        return super.performClick();
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == 2) {
            return true;
        }
        return super.onTouchEvent(event);
    }
}
