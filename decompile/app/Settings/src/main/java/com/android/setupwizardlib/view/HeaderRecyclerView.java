package com.android.setupwizardlib.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.AdapterDataObserver;
import android.support.v7.widget.RecyclerView.LayoutManager;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import com.android.settings.deviceinfo.HwCustMSimSubscriptionStatusTabFragmentImpl;
import com.android.setupwizardlib.DividerItemDecoration.DividedViewHolder;
import com.android.setupwizardlib.R$styleable;

public class HeaderRecyclerView extends RecyclerView {
    private View mHeader;
    private int mHeaderRes;

    public static class HeaderAdapter extends Adapter<ViewHolder> {
        private Adapter mAdapter;
        private View mHeader;
        private final AdapterDataObserver mObserver = new AdapterDataObserver() {
            public void onChanged() {
                HeaderAdapter.this.notifyDataSetChanged();
            }

            public void onItemRangeChanged(int positionStart, int itemCount) {
                HeaderAdapter.this.notifyItemRangeChanged(positionStart, itemCount);
            }

            public void onItemRangeInserted(int positionStart, int itemCount) {
                HeaderAdapter.this.notifyItemRangeInserted(positionStart, itemCount);
            }

            public void onItemRangeMoved(int fromPosition, int toPosition, int itemCount) {
                HeaderAdapter.this.notifyDataSetChanged();
            }

            public void onItemRangeRemoved(int positionStart, int itemCount) {
                HeaderAdapter.this.notifyItemRangeRemoved(positionStart, itemCount);
            }
        };

        public HeaderAdapter(Adapter adapter) {
            this.mAdapter = adapter;
            this.mAdapter.registerAdapterDataObserver(this.mObserver);
            setHasStableIds(this.mAdapter.hasStableIds());
        }

        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID) {
                return new HeaderViewHolder(this.mHeader);
            }
            return this.mAdapter.onCreateViewHolder(parent, viewType);
        }

        public void onBindViewHolder(ViewHolder holder, int position) {
            if (this.mHeader != null) {
                position--;
            }
            if (position >= 0) {
                this.mAdapter.onBindViewHolder(holder, position);
            }
        }

        public int getItemViewType(int position) {
            if (this.mHeader != null) {
                position--;
            }
            if (position < 0) {
                return HwCustMSimSubscriptionStatusTabFragmentImpl.INVALID;
            }
            return this.mAdapter.getItemViewType(position);
        }

        public int getItemCount() {
            int count = this.mAdapter.getItemCount();
            if (this.mHeader != null) {
                return count + 1;
            }
            return count;
        }

        public long getItemId(int position) {
            if (this.mHeader != null) {
                position--;
            }
            if (position < 0) {
                return Long.MAX_VALUE;
            }
            return this.mAdapter.getItemId(position);
        }

        public void setHeader(View header) {
            this.mHeader = header;
        }

        public Adapter getWrappedAdapter() {
            return this.mAdapter;
        }
    }

    private static class HeaderViewHolder extends ViewHolder implements DividedViewHolder {
        public HeaderViewHolder(View itemView) {
            super(itemView);
        }

        public boolean isDividerAllowedAbove() {
            return false;
        }

        public boolean isDividerAllowedBelow() {
            return false;
        }
    }

    public HeaderRecyclerView(Context context) {
        super(context);
        init(null, 0);
    }

    public HeaderRecyclerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs, 0);
    }

    public HeaderRecyclerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(attrs, defStyleAttr);
    }

    private void init(AttributeSet attrs, int defStyleAttr) {
        TypedArray a = getContext().obtainStyledAttributes(attrs, R$styleable.SuwHeaderRecyclerView, defStyleAttr, 0);
        this.mHeaderRes = a.getResourceId(R$styleable.SuwHeaderRecyclerView_suwHeader, 0);
        a.recycle();
    }

    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
        super.onInitializeAccessibilityEvent(event);
        int numberOfHeaders = this.mHeader != null ? 1 : 0;
        event.setItemCount(event.getItemCount() - numberOfHeaders);
        event.setFromIndex(Math.max(event.getFromIndex() - numberOfHeaders, 0));
        if (VERSION.SDK_INT >= 14) {
            event.setToIndex(Math.max(event.getToIndex() - numberOfHeaders, 0));
        }
    }

    public View getHeader() {
        return this.mHeader;
    }

    public void setLayoutManager(LayoutManager layout) {
        super.setLayoutManager(layout);
        if (layout != null && this.mHeader == null && this.mHeaderRes != 0) {
            this.mHeader = LayoutInflater.from(getContext()).inflate(this.mHeaderRes, this, false);
        }
    }

    public void setAdapter(Adapter adapter) {
        if (!(this.mHeader == null || adapter == null)) {
            Adapter headerAdapter = new HeaderAdapter(adapter);
            headerAdapter.setHeader(this.mHeader);
            adapter = headerAdapter;
        }
        super.setAdapter(adapter);
    }
}
