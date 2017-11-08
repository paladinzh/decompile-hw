package com.android.setupwizardlib.items;

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v7.widget.RecyclerView.Adapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import com.android.setupwizardlib.R$styleable;
import com.android.setupwizardlib.items.ItemHierarchy.Observer;

public class RecyclerItemAdapter extends Adapter<ItemViewHolder> implements Observer {
    private final ItemHierarchy mItemHierarchy;
    private OnItemSelectedListener mListener;

    public interface OnItemSelectedListener {
        void onItemSelected(IItem iItem);
    }

    public RecyclerItemAdapter(ItemHierarchy hierarchy) {
        this.mItemHierarchy = hierarchy;
        this.mItemHierarchy.registerObserver(this);
    }

    public IItem getItem(int position) {
        return this.mItemHierarchy.getItemAt(position);
    }

    public long getItemId(int position) {
        long j = -1;
        IItem mItem = getItem(position);
        if (!(mItem instanceof AbstractItem)) {
            return -1;
        }
        int id = ((AbstractItem) mItem).getId();
        if (id > 0) {
            j = (long) id;
        }
        return j;
    }

    public int getItemCount() {
        return this.mItemHierarchy.getCount();
    }

    public ItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false);
        final ItemViewHolder viewHolder = new ItemViewHolder(view);
        TypedArray typedArray = parent.getContext().obtainStyledAttributes(R$styleable.SuwRecyclerItemAdapter);
        Drawable selectableItemBackground = typedArray.getDrawable(R$styleable.SuwRecyclerItemAdapter_android_selectableItemBackground);
        if (selectableItemBackground == null) {
            selectableItemBackground = typedArray.getDrawable(R$styleable.SuwRecyclerItemAdapter_selectableItemBackground);
        }
        Drawable background = typedArray.getDrawable(R$styleable.SuwRecyclerItemAdapter_android_colorBackground);
        if (selectableItemBackground == null || background == null) {
            Log.e("RecyclerItemAdapter", "Cannot resolve required attributes. selectableItemBackground=" + selectableItemBackground + " background=" + background);
        } else {
            view.setBackgroundDrawable(new LayerDrawable(new Drawable[]{background, selectableItemBackground}));
        }
        typedArray.recycle();
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                IItem item = viewHolder.getItem();
                if (RecyclerItemAdapter.this.mListener != null && item != null && item.isEnabled()) {
                    RecyclerItemAdapter.this.mListener.onItemSelected(item);
                }
            }
        });
        return viewHolder;
    }

    public void onBindViewHolder(ItemViewHolder holder, int position) {
        IItem item = getItem(position);
        item.onBindView(holder.itemView);
        holder.setEnabled(item.isEnabled());
        holder.setItem(item);
    }

    public int getItemViewType(int position) {
        return getItem(position).getLayoutResource();
    }

    public void onChanged(ItemHierarchy hierarchy) {
        notifyDataSetChanged();
    }

    public ItemHierarchy findItemById(int id) {
        return this.mItemHierarchy.findItemById(id);
    }

    public void setOnItemSelectedListener(OnItemSelectedListener listener) {
        this.mListener = listener;
    }
}
