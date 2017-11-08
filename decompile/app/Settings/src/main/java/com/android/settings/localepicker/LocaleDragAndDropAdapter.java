package com.android.settings.localepicker;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.LocaleList;
import android.support.v4.view.MotionEventCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemAnimator.ItemAnimatorFinishedListener;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.SimpleCallback;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import com.android.internal.app.LocalePicker;
import com.android.internal.app.LocaleStore.LocaleInfo;
import com.android.settings.ItemUseStat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class LocaleDragAndDropAdapter extends Adapter<CustomViewHolder> {
    private static OnClickListener mOnClickListener = new OnClickListener() {
        public void onClick(View v) {
        }
    };
    private final Context mContext;
    private boolean mDragEnabled = true;
    private final List<LocaleInfo> mFeedItemList;
    private final ItemTouchHelper mItemTouchHelper;
    private LocaleList mLocalesSetLast = null;
    private LocaleList mLocalesToSetNext = null;
    private RecyclerView mParentView = null;
    private boolean mRemoveMode = false;

    class CustomViewHolder extends ViewHolder implements OnTouchListener {
        private final LocaleDragCell mLocaleDragCell;

        public CustomViewHolder(LocaleDragCell view) {
            super(view);
            this.mLocaleDragCell = view;
            this.mLocaleDragCell.getDragHandle().setOnTouchListener(this);
        }

        public LocaleDragCell getLocaleDragCell() {
            return this.mLocaleDragCell;
        }

        public boolean onTouch(View v, MotionEvent event) {
            if (LocaleDragAndDropAdapter.this.mDragEnabled) {
                switch (MotionEventCompat.getActionMasked(event)) {
                    case 0:
                        LocaleDragAndDropAdapter.this.mItemTouchHelper.startDrag(this);
                        break;
                }
            }
            return false;
        }
    }

    public LocaleDragAndDropAdapter(Context context, List<LocaleInfo> feedItemList) {
        this.mFeedItemList = feedItemList;
        this.mContext = context;
        final float dragElevation = TypedValue.applyDimension(1, 8.0f, context.getResources().getDisplayMetrics());
        this.mItemTouchHelper = new ItemTouchHelper(new SimpleCallback(3, 0) {
            private int mSelectionStatus = -1;

            public boolean onMove(RecyclerView view, ViewHolder source, ViewHolder target) {
                LocaleDragAndDropAdapter.this.onItemMove(source.getAdapterPosition(), target.getAdapterPosition());
                return true;
            }

            public void onSwiped(ViewHolder viewHolder, int i) {
            }

            public void onChildDraw(Canvas c, RecyclerView recyclerView, ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                if (this.mSelectionStatus != -1) {
                    viewHolder.itemView.setElevation(this.mSelectionStatus == 1 ? dragElevation : 0.0f);
                    this.mSelectionStatus = -1;
                }
            }

            public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
                super.onSelectedChanged(viewHolder, actionState);
                if (actionState == 2) {
                    this.mSelectionStatus = 1;
                } else if (actionState == 0) {
                    this.mSelectionStatus = 0;
                }
            }
        });
    }

    public void setRecyclerView(RecyclerView rv) {
        this.mParentView = rv;
        this.mItemTouchHelper.attachToRecyclerView(rv);
    }

    public CustomViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        return new CustomViewHolder((LocaleDragCell) LayoutInflater.from(this.mContext).inflate(2130968851, viewGroup, false));
    }

    public void onBindViewHolder(CustomViewHolder holder, final int i) {
        boolean z;
        boolean z2 = false;
        LocaleInfo feedItem = (LocaleInfo) this.mFeedItemList.get(i);
        LocaleDragCell dragCell = holder.getLocaleDragCell();
        String label = feedItem.getFullNameNative();
        String description = feedItem.getFullNameInUiLanguage();
        if (i == 0) {
            dragCell.setLabelAndDescription(label, description, true);
        } else {
            dragCell.setLabelAndDescription(label, description, false);
        }
        dragCell.setLocalized(feedItem.isTranslated());
        dragCell.setShowCheckbox(this.mRemoveMode);
        if (this.mRemoveMode) {
            z = false;
        } else {
            z = true;
        }
        dragCell.setShowMiniLabel(z);
        if (i == 0) {
            dragCell.setMiniLabel(33751080, true);
        } else {
            dragCell.setMiniLabel(33751080, false);
        }
        if (this.mRemoveMode) {
            z = false;
        } else {
            z = this.mDragEnabled;
        }
        dragCell.setShowHandle(z);
        if (this.mRemoveMode) {
            z2 = feedItem.getChecked();
        }
        dragCell.setChecked(z2);
        dragCell.setTag(feedItem);
        dragCell.setOnClickListener(mOnClickListener);
        if (i != 0) {
            dragCell.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    LocaleDragAndDropAdapter.this.onItemMove(i, 0);
                    LocaleDragAndDropAdapter.this.notifyDataSetChanged();
                    LocaleDragAndDropAdapter.this.doTheUpdate();
                }
            });
        }
    }

    public int getItemCount() {
        int itemCount;
        if (this.mFeedItemList != null) {
            itemCount = this.mFeedItemList.size();
        } else {
            itemCount = 0;
        }
        if (itemCount < 2 || this.mRemoveMode) {
            setDragEnabled(false);
        } else {
            setDragEnabled(true);
        }
        return itemCount;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (fromPosition < 0 || toPosition < 0) {
            Log.e("LocaleDragAndDropAdapter", String.format(Locale.US, "Negative position in onItemMove %d -> %d", new Object[]{Integer.valueOf(fromPosition), Integer.valueOf(toPosition)}));
        } else {
            LocaleInfo saved = (LocaleInfo) this.mFeedItemList.get(fromPosition);
            this.mFeedItemList.remove(fromPosition);
            this.mFeedItemList.add(toPosition, saved);
        }
        notifyItemChanged(fromPosition);
        notifyItemChanged(toPosition);
        notifyItemMoved(fromPosition, toPosition);
    }

    void setRemoveMode(boolean removeMode) {
        this.mRemoveMode = removeMode;
        int itemCount = this.mFeedItemList.size();
        for (int i = 0; i < itemCount; i++) {
            ((LocaleInfo) this.mFeedItemList.get(i)).setChecked(false);
            notifyItemChanged(i);
        }
    }

    boolean isRemoveMode() {
        return this.mRemoveMode;
    }

    void removeItem(int position) {
        int itemCount = this.mFeedItemList.size();
        if (itemCount > 1 && position >= 0 && position < itemCount) {
            this.mFeedItemList.remove(position);
            notifyDataSetChanged();
        }
    }

    void addLocale(LocaleInfo li) {
        this.mFeedItemList.add(li);
        notifyItemInserted(this.mFeedItemList.size() - 1);
        doTheUpdate();
    }

    public void doTheUpdate() {
        int count = this.mFeedItemList.size();
        Locale[] newList = new Locale[count];
        for (int i = 0; i < count; i++) {
            newList[i] = ((LocaleInfo) this.mFeedItemList.get(i)).getLocale();
        }
        updateLocalesWhenAnimationStops(new LocaleList(newList));
    }

    public void updateLocalesWhenAnimationStops(LocaleList localeList) {
        if (!localeList.equals(this.mLocalesToSetNext)) {
            final Locale defaultLocale = Locale.getDefault();
            LocaleList.setDefault(localeList);
            this.mLocalesToSetNext = localeList;
            this.mParentView.getItemAnimator().isRunning(new ItemAnimatorFinishedListener() {
                public void onAnimationsFinished() {
                    if (LocaleDragAndDropAdapter.this.mLocalesToSetNext != null && !LocaleDragAndDropAdapter.this.mLocalesToSetNext.equals(LocaleDragAndDropAdapter.this.mLocalesSetLast)) {
                        if (!defaultLocale.equals(LocaleDragAndDropAdapter.this.mLocalesToSetNext.get(0))) {
                            ItemUseStat.getInstance().handleClick(LocaleDragAndDropAdapter.this.mContext, 2, "switch language", LocaleDragAndDropAdapter.this.mLocalesToSetNext.get(0).toString());
                        }
                        LocalePicker.updateLocales(LocaleDragAndDropAdapter.this.mLocalesToSetNext);
                        LocaleDragAndDropAdapter.this.mLocalesSetLast = LocaleDragAndDropAdapter.this.mLocalesToSetNext;
                        LocaleDragAndDropAdapter.this.mLocalesToSetNext = null;
                    }
                }
            });
        }
    }

    private void setDragEnabled(boolean enabled) {
        this.mDragEnabled = enabled;
    }

    public void saveState(Bundle outInstanceState) {
        if (outInstanceState != null) {
            ArrayList<String> selectedLocales = new ArrayList();
            for (LocaleInfo li : this.mFeedItemList) {
                if (li.getChecked()) {
                    selectedLocales.add(li.getId());
                }
            }
            outInstanceState.putStringArrayList("selectedLocales", selectedLocales);
        }
    }

    public void restoreState(Bundle savedInstanceState) {
        if (savedInstanceState != null && this.mRemoveMode) {
            ArrayList<String> selectedLocales = savedInstanceState.getStringArrayList("selectedLocales");
            if (selectedLocales != null && !selectedLocales.isEmpty()) {
                for (LocaleInfo li : this.mFeedItemList) {
                    li.setChecked(selectedLocales.contains(li.getId()));
                }
                notifyItemRangeChanged(0, this.mFeedItemList.size());
            }
        }
    }
}
