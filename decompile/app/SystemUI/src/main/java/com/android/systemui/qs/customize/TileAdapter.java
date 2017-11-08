package com.android.systemui.qs.customize;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager.SpanSizeLookup;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerView.Adapter;
import android.support.v7.widget.RecyclerView.ItemDecoration;
import android.support.v7.widget.RecyclerView.LayoutParams;
import android.support.v7.widget.RecyclerView.State;
import android.support.v7.widget.RecyclerView.ViewHolder;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.support.v7.widget.helper.ItemTouchHelper.Callback;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.logging.MetricsLogger;
import com.android.systemui.R;
import com.android.systemui.qs.QSIconView;
import com.android.systemui.qs.QSTile;
import com.android.systemui.qs.customize.TileQueryHelper.TileInfo;
import com.android.systemui.qs.customize.TileQueryHelper.TileStateListener;
import com.android.systemui.qs.external.CustomTile;
import com.android.systemui.statusbar.phone.QSTileHost;
import com.android.systemui.statusbar.phone.SystemUIDialog;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;
import java.util.ArrayList;
import java.util.List;

public class TileAdapter extends Adapter<Holder> implements TileStateListener {
    private int mAccessibilityFromIndex;
    private final AccessibilityManager mAccessibilityManager;
    private boolean mAccessibilityMoving;
    private List<TileInfo> mAllTiles;
    private final Callback mCallbacks = new Callback() {
        public boolean isLongPressDragEnabled() {
            return true;
        }

        public boolean isItemViewSwipeEnabled() {
            return false;
        }

        public void onSelectedChanged(ViewHolder viewHolder, int actionState) {
            boolean z = false;
            HwLog.i("TileAdapter", "onSelectedChanged:" + actionState);
            super.onSelectedChanged(viewHolder, actionState);
            if (actionState != 2) {
                viewHolder = null;
            }
            if (viewHolder != TileAdapter.this.mCurrentDrag) {
                if (TileAdapter.this.mCurrentDrag != null) {
                    int position = TileAdapter.this.mCurrentDrag.getAdapterPosition();
                    if (position > 0 && position < TileAdapter.this.mTiles.size()) {
                        TileInfo info = (TileInfo) TileAdapter.this.mTiles.get(position);
                        CustomizeTileView -get0 = TileAdapter.this.mCurrentDrag.mTileView;
                        if (position > TileAdapter.this.mEditIndex && !info.isSystem) {
                            z = true;
                        }
                        -get0.setShowAppLabel(z);
                    }
                    TileAdapter.this.mCurrentDrag.stopDrag();
                    reportTitle();
                    TileAdapter.this.mCurrentDrag = null;
                }
                if (viewHolder != null) {
                    TileAdapter.this.mCurrentDrag = (Holder) viewHolder;
                    TileAdapter.this.mCurrentDrag.startDrag();
                }
                TileAdapter.this.mHandler.post(new Runnable() {
                    public void run() {
                        TileAdapter.this.notifyItemChanged(TileAdapter.this.mEditIndex);
                    }
                });
            }
        }

        private void reportTitle() {
            int size = TileAdapter.this.mTiles.size();
            StringBuffer bf = new StringBuffer();
            for (int i = 0; i < size; i++) {
                TileInfo info = (TileInfo) TileAdapter.this.mTiles.get(i);
                if (info != null) {
                    if (info.isSystem) {
                        bf.append(info.spec + ",");
                    } else {
                        String tmp = info.spec;
                        if (tmp.length() > 0 && tmp.lastIndexOf("$") > -1 && tmp.lastIndexOf(")") > -1) {
                            tmp = tmp.substring(tmp.lastIndexOf("$") + 1, tmp.lastIndexOf(")"));
                        }
                        bf.append(tmp + ",");
                    }
                }
            }
            BDReporter.e(TileAdapter.this.mContext, 26, "size:" + TileAdapter.this.mCurrentSpecs.size() + ",name:" + bf.toString());
        }

        public boolean canDropOver(RecyclerView recyclerView, ViewHolder current, ViewHolder target) {
            return target.getAdapterPosition() <= TileAdapter.this.mEditIndex + 1;
        }

        public int getMovementFlags(RecyclerView recyclerView, ViewHolder viewHolder) {
            if (viewHolder.getItemViewType() == 1) {
                return Callback.makeMovementFlags(0, 0);
            }
            return Callback.makeMovementFlags(15, 0);
        }

        public boolean onMove(RecyclerView recyclerView, ViewHolder viewHolder, ViewHolder target) {
            return TileAdapter.this.move(viewHolder.getAdapterPosition(), target.getAdapterPosition(), target.itemView);
        }

        public void onSwiped(ViewHolder viewHolder, int direction) {
        }
    };
    private int mColumns = 4;
    private final Context mContext;
    private Holder mCurrentDrag;
    private List<String> mCurrentSpecs;
    private final ItemDecoration mDecoration = new ItemDecoration() {
        private final ColorDrawable mDrawable = new ColorDrawable(-13090232);

        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
            if (parent.getChildItemId(view) == 10000 || parent.getChildItemId(view) == 20000) {
                outRect.set(0, 0, 0, 0);
            } else {
                outRect.set(TileAdapter.this.mMarginTile, 0, TileAdapter.this.mMarginTile, 0);
            }
        }

        public void onDraw(Canvas c, RecyclerView parent, State state) {
            super.onDraw(c, parent, state);
            int childCount = parent.getChildCount();
            int width = parent.getWidth();
            int bottom = parent.getBottom();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);
                if (parent.getChildViewHolder(child).getAdapterPosition() >= TileAdapter.this.mEditIndex || (child instanceof TextView)) {
                    this.mDrawable.setBounds(0, (child.getTop() + ((LayoutParams) child.getLayoutParams()).topMargin) + Math.round(ViewCompat.getTranslationY(child)), width, bottom);
                    return;
                }
            }
        }
    };
    private int mEditIndex;
    private final Handler mHandler = new Handler();
    private QSTileHost mHost;
    private final ItemTouchHelper mItemTouchHelper;
    private int mMarginTile;
    private boolean mNeedsFocus;
    private List<TileInfo> mOtherTiles;
    private final SpanSizeLookup mSizeLookup = new SpanSizeLookup() {
        public int getSpanSize(int position) {
            int type = TileAdapter.this.getItemViewType(position);
            if (type == 1 || type == 4) {
                return TileAdapter.this.mColumns;
            }
            return 1;
        }
    };
    private int mTileDividerIndex;
    private H mTileStateUpdateHandler = new H();
    private final List<TileInfo> mTiles = new ArrayList();

    private class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                TileAdapter.this.onTileStateChanged();
            }
        }
    }

    public class Holder extends ViewHolder {
        private CustomizeTileView mTileView;

        public Holder(View itemView) {
            super(itemView);
            if (itemView instanceof FrameLayout) {
                this.mTileView = (CustomizeTileView) ((FrameLayout) itemView).getChildAt(0);
                this.mTileView.setBackground(null);
                this.mTileView.getIcon().disableAnimation();
            }
        }

        public void clearDrag() {
            if (this.mTileView != null) {
                this.itemView.clearAnimation();
                this.mTileView.findViewById(R.id.tile_label).clearAnimation();
                this.mTileView.findViewById(R.id.tile_label).setAlpha(1.0f);
                this.mTileView.getAppLabel().clearAnimation();
                this.mTileView.getAppLabel().setAlpha(0.6f);
            }
        }

        public void startDrag() {
            if (this.mTileView == null) {
                HwLog.e("TileAdapter", "startDrag:" + this + ", " + this.itemView);
                return;
            }
            HwLog.i("TileAdapter", "startDrag:" + ((TextView) this.mTileView.findViewById(R.id.tile_label)).getText().toString() + " " + this + ", " + this.mTileView + ", " + this.itemView);
            this.itemView.animate().setDuration(100).scaleX(1.2f).scaleY(1.2f);
            this.mTileView.findViewById(R.id.tile_label).animate().setDuration(100).alpha(0.0f);
            this.mTileView.getAppLabel().animate().setDuration(100).alpha(0.0f);
        }

        public void stopDrag() {
            if (this.mTileView != null) {
                HwLog.i("TileAdapter", "stopDrag:" + ((TextView) this.mTileView.findViewById(R.id.tile_label)).getText().toString() + " " + this + ", " + this.mTileView + ", " + this.itemView);
                this.itemView.setScaleX(1.0f);
                this.itemView.setScaleY(1.0f);
                this.mTileView.findViewById(R.id.tile_label).animate().setDuration(33).alpha(1.0f);
                this.mTileView.getAppLabel().animate().setDuration(33).alpha(0.6f);
            }
        }
    }

    public TileAdapter(Context context) {
        this.mContext = context;
        this.mAccessibilityManager = (AccessibilityManager) context.getSystemService(AccessibilityManager.class);
        this.mItemTouchHelper = new ItemTouchHelper(this.mCallbacks);
        this.mMarginTile = context.getResources().getDimensionPixelSize(R.dimen.qs_customizer_tile_margin);
        setHasStableIds(true);
    }

    public long getItemId(int position) {
        if (this.mTiles.get(position) != null) {
            return (long) this.mAllTiles.indexOf(this.mTiles.get(position));
        }
        return position == this.mEditIndex ? 10000 : 20000;
    }

    public void setColumns(int columns) {
        this.mColumns = columns;
    }

    public void setHost(QSTileHost host) {
        this.mHost = host;
    }

    public ItemTouchHelper getItemTouchHelper() {
        return this.mItemTouchHelper;
    }

    public ItemDecoration getItemDecoration() {
        return this.mDecoration;
    }

    public void saveSpecs(QSTileHost host) {
        HwLog.i("TileAdapter", "saveSpecs");
        List<String> newSpecs = new ArrayList();
        int i = 0;
        while (i < this.mTiles.size() && this.mTiles.get(i) != null) {
            newSpecs.add(((TileInfo) this.mTiles.get(i)).spec);
            HwLog.i("TileAdapter", "saveSpecs:" + ((TileInfo) this.mTiles.get(i)).spec);
            i++;
        }
        host.changeTiles(this.mCurrentSpecs, newSpecs);
        this.mCurrentSpecs = newSpecs;
    }

    public void setTileSpecs(List<String> currentSpecs) {
        HwLog.i("TileAdapter", "setTileSpecs");
        for (String tile : currentSpecs) {
            HwLog.i("TileAdapter", "setTileSpecs:" + tile);
        }
        this.mCurrentSpecs = currentSpecs;
        recalcSpecs();
    }

    public void onTilesChanged(List<TileInfo> tiles) {
        HwLog.i("TileAdapter", "onTilesChanged");
        for (TileInfo tile : tiles) {
            HwLog.i("TileAdapter", "onTilesChanged:" + tile.spec);
        }
        if (this.mHost != null) {
            this.mHost.onTilesChanged(tiles);
        }
        this.mAllTiles = tiles;
        List<TileInfo> toRemoved = new ArrayList();
        for (TileInfo tileInfo : this.mAllTiles) {
            if (!(this.mHost == null || this.mHost.isAvailableTile(tileInfo.spec))) {
                HwLog.i("TileAdapter", "remove not available tile: " + tileInfo.spec);
                toRemoved.add(tileInfo);
            }
        }
        this.mAllTiles.removeAll(toRemoved);
        recalcSpecs();
    }

    private void recalcSpecs() {
        if (this.mCurrentSpecs != null && this.mAllTiles != null) {
            int i;
            TileInfo tile;
            this.mOtherTiles = new ArrayList(this.mAllTiles);
            this.mTiles.clear();
            for (i = 0; i < this.mCurrentSpecs.size(); i++) {
                tile = getAndRemoveOther((String) this.mCurrentSpecs.get(i));
                if (tile != null) {
                    this.mTiles.add(tile);
                }
            }
            this.mTiles.add(null);
            i = 0;
            while (i < this.mOtherTiles.size()) {
                tile = (TileInfo) this.mOtherTiles.get(i);
                if (tile.isSystem) {
                    int i2 = i - 1;
                    this.mOtherTiles.remove(i);
                    this.mTiles.add(tile);
                    i = i2;
                }
                i++;
            }
            this.mTileDividerIndex = this.mTiles.size();
            this.mTiles.add(null);
            this.mTiles.addAll(this.mOtherTiles);
            updateDividerLocations();
            notifyDataSetChanged();
        }
    }

    private TileInfo getAndRemoveOther(String s) {
        for (int i = 0; i < this.mOtherTiles.size(); i++) {
            if (((TileInfo) this.mOtherTiles.get(i)).spec.equals(s)) {
                return (TileInfo) this.mOtherTiles.remove(i);
            }
        }
        return null;
    }

    public int getItemViewType(int position) {
        if (this.mAccessibilityMoving && position == this.mEditIndex - 1) {
            return 2;
        }
        if (position == this.mTileDividerIndex && this.mTileDividerIndex > 0) {
            return 4;
        }
        if (this.mTiles == null || position < 0 || position >= this.mTiles.size() || this.mTiles.get(position) != null) {
            return 0;
        }
        return 1;
    }

    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == 4) {
            return new Holder(inflater.inflate(R.layout.qs_customize_tile_divider, parent, false));
        }
        if (viewType == 1) {
            return new Holder(inflater.inflate(R.layout.qs_customize_divider, parent, false));
        }
        FrameLayout frame = (FrameLayout) inflater.inflate(R.layout.qs_customize_tile_frame, parent, false);
        frame.addView(new CustomizeTileView(context, new QSIconView(context)));
        return new Holder(frame);
    }

    public int getItemCount() {
        return this.mTiles.size();
    }

    public boolean onFailedToRecycleView(Holder holder) {
        holder.clearDrag();
        return true;
    }

    public void onBindViewHolder(final Holder holder, int position) {
        boolean z = false;
        int i = 1;
        if (position < 0 || position >= this.mTiles.size()) {
            HwLog.e("TileAdapter", "onBindViewHolder()  position" + position + ",mTiles.size()" + this.mTiles.size());
        } else if (holder.getItemViewType() == 4) {
            int i2;
            View view = holder.itemView;
            if (this.mTileDividerIndex >= this.mTiles.size() - 1) {
                i2 = 4;
            }
            view.setVisibility(i2);
        } else if (holder.getItemViewType() == 1) {
            int i3;
            TextView textView = (TextView) holder.itemView.findViewById(16908310);
            if (this.mCurrentDrag != null) {
                i3 = R.string.drag_to_remove_tiles;
            } else {
                i3 = R.string.drag_to_add_tiles_new;
            }
            textView.setText(i3);
        } else if (holder.getItemViewType() == 2) {
            holder.mTileView.setClickable(true);
            holder.mTileView.setFocusable(true);
            holder.mTileView.setFocusableInTouchMode(true);
            holder.mTileView.setVisibility(0);
            holder.mTileView.setImportantForAccessibility(1);
            holder.mTileView.setContentDescription(this.mContext.getString(R.string.accessibility_qs_edit_position_label, new Object[]{Integer.valueOf(position + 1)}));
            holder.mTileView.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    TileAdapter.this.selectPosition(holder.getAdapterPosition(), v);
                }
            });
            if (this.mNeedsFocus) {
                holder.mTileView.requestLayout();
                holder.mTileView.addOnLayoutChangeListener(new OnLayoutChangeListener() {
                    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                        holder.mTileView.removeOnLayoutChangeListener(this);
                        holder.mTileView.requestFocus();
                    }
                });
                this.mNeedsFocus = false;
            }
        } else {
            final TileInfo info = (TileInfo) this.mTiles.get(position);
            if (info == null) {
                HwLog.e("TileAdapter", "onBindViewHolder()  null == titleInfo");
                return;
            }
            if (position > this.mEditIndex) {
                info.state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_add_tile_label, new Object[]{info.state.label});
            } else if (this.mAccessibilityMoving) {
                info.state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_position_label, new Object[]{Integer.valueOf(position + 1)});
            } else {
                info.state.contentDescription = this.mContext.getString(R.string.accessibility_qs_edit_tile_label, new Object[]{Integer.valueOf(position + 1), info.state.label});
            }
            holder.mTileView.onStateChanged(info.state);
            holder.mTileView.setAppLabel(info.appLabel);
            holder.mTileView.setClickable(true);
            holder.mTileView.setOnClickListener(new OnClickListener() {
                public void onClick(View view) {
                    if (TileAdapter.this.mHost != null) {
                        for (QSTile tile : TileAdapter.this.mHost.getAllTiles()) {
                            if (tile != null && tile.getTileSpec() != null && tile.getTileSpec().equals(info.spec)) {
                                HwLog.i("TileAdapter", "onTileClick::state=" + tile.getState());
                                tile.click();
                                info.state = tile.getState();
                                info.appLabel = tile.getTileLabel();
                                TileAdapter.this.notifyTileStateChanged();
                                break;
                            }
                        }
                    }
                }
            });
            CustomizeTileView -get0 = holder.mTileView;
            if (position > this.mEditIndex && !info.isSystem) {
                z = true;
            }
            -get0.setShowAppLabel(z);
            if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                boolean selectable = !this.mAccessibilityMoving || position < this.mEditIndex;
                holder.mTileView.setClickable(selectable);
                holder.mTileView.setFocusable(selectable);
                CustomizeTileView -get02 = holder.mTileView;
                if (!selectable) {
                    i = 4;
                }
                -get02.setImportantForAccessibility(i);
                if (selectable) {
                    holder.mTileView.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            int position = holder.getAdapterPosition();
                            if (position < 0 || position >= TileAdapter.this.getItemCount()) {
                                HwLog.e("TileAdapter", "onBindViewHolder::holder.mTileView::position=" + position);
                                return;
                            }
                            if (TileAdapter.this.mAccessibilityMoving) {
                                TileAdapter.this.selectPosition(position, v);
                            } else if (position < TileAdapter.this.mEditIndex) {
                                TileAdapter.this.showAccessibilityDialog(position, v);
                            } else {
                                TileAdapter.this.startAccessibleDrag(position);
                            }
                        }
                    });
                }
            }
        }
    }

    private void selectPosition(int position, View v) {
        HwLog.i("TileAdapter", "selectPosition:" + position);
        this.mAccessibilityMoving = false;
        this.mTileDividerIndex--;
        List list = this.mTiles;
        int i = this.mEditIndex;
        this.mEditIndex = i - 1;
        list.remove(i);
        notifyItemRemoved(this.mEditIndex - 1);
        move(this.mAccessibilityFromIndex, position, v);
        notifyDataSetChanged();
    }

    private void showAccessibilityDialog(final int position, final View v) {
        final TileInfo info = (TileInfo) this.mTiles.get(position);
        if (info == null) {
            HwLog.e("TileAdapter", "showAccessibilityDialog()  null == info");
            return;
        }
        CharSequence[] options = new CharSequence[2];
        options[0] = this.mContext.getString(R.string.accessibility_qs_edit_move_tile, new Object[]{info.state.label});
        options[1] = this.mContext.getString(R.string.accessibility_qs_edit_remove_tile, new Object[]{info.state.label});
        AlertDialog dialog = new Builder(this.mContext).setItems(options, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    TileAdapter.this.startAccessibleDrag(position);
                    return;
                }
                TileAdapter.this.move(position, info.isSystem ? TileAdapter.this.mEditIndex : TileAdapter.this.mTileDividerIndex, v);
                TileAdapter.this.notifyItemChanged(TileAdapter.this.mTileDividerIndex);
                TileAdapter.this.notifyDataSetChanged();
            }
        }).setNegativeButton(17039360, null).create();
        SystemUIDialog.setShowForAllUsers(dialog, true);
        SystemUIDialog.applyFlags(dialog);
        dialog.show();
    }

    private void startAccessibleDrag(int position) {
        this.mAccessibilityMoving = true;
        this.mNeedsFocus = true;
        this.mAccessibilityFromIndex = position;
        int index = this.mEditIndex;
        this.mEditIndex = index + 1;
        this.mTileDividerIndex++;
        if (index >= 0) {
            this.mTiles.add(index, null);
        } else {
            HwLog.e("TileAdapter", "invalid mEditIndex is " + index);
        }
        notifyDataSetChanged();
    }

    public SpanSizeLookup getSizeLookup() {
        return this.mSizeLookup;
    }

    private boolean move(int from, int to, View v) {
        if (from < 0 || to < 0 || from >= this.mTiles.size() || to >= this.mTiles.size()) {
            HwLog.e("TileAdapter", "mTiles move::from or to is invalid,from=" + from + ",to=" + to + ", list size=" + this.mTiles.size());
            return false;
        } else if (to == from) {
            return true;
        } else {
            TileInfo titleInfo = (TileInfo) this.mTiles.get(from);
            if (titleInfo == null) {
                HwLog.e("TileAdapter", "move()  null == titleInfo");
                return false;
            }
            CharSequence announcement;
            CharSequence fromLabel = titleInfo.state.label;
            move(from, to, this.mTiles);
            updateDividerLocations();
            if (to >= this.mEditIndex) {
                MetricsLogger.action(this.mContext, 360, strip((TileInfo) this.mTiles.get(to)));
                MetricsLogger.action(this.mContext, 361, from);
                announcement = this.mContext.getString(R.string.accessibility_qs_edit_tile_removed, new Object[]{fromLabel});
            } else if (from >= this.mEditIndex) {
                MetricsLogger.action(this.mContext, 362, strip((TileInfo) this.mTiles.get(to)));
                MetricsLogger.action(this.mContext, 363, to);
                announcement = this.mContext.getString(R.string.accessibility_qs_edit_tile_added, new Object[]{fromLabel, Integer.valueOf(to + 1)});
            } else {
                MetricsLogger.action(this.mContext, 364, strip((TileInfo) this.mTiles.get(to)));
                MetricsLogger.action(this.mContext, 365, to);
                announcement = this.mContext.getString(R.string.accessibility_qs_edit_tile_moved, new Object[]{fromLabel, Integer.valueOf(to + 1)});
            }
            v.announceForAccessibility(announcement);
            saveSpecs(this.mHost);
            return true;
        }
    }

    private void updateDividerLocations() {
        this.mEditIndex = -1;
        this.mTileDividerIndex = this.mTiles.size();
        for (int i = 0; i < this.mTiles.size(); i++) {
            if (this.mTiles.get(i) == null) {
                if (this.mEditIndex == -1) {
                    this.mEditIndex = i;
                } else {
                    this.mTileDividerIndex = i;
                }
            }
        }
        if (this.mTiles.size() - 1 == this.mTileDividerIndex) {
            notifyItemChanged(this.mTileDividerIndex);
        }
        HwLog.i("TileAdapter", "mTileDividerIndex=" + this.mTileDividerIndex);
    }

    private static String strip(TileInfo tileInfo) {
        String spec = tileInfo.spec;
        if (spec.startsWith("custom(")) {
            return CustomTile.getComponentFromSpec(spec).getPackageName();
        }
        return spec;
    }

    private <T> void move(int from, int to, List<T> list) {
        if (from < 0 || to < 0 || from >= list.size() || to >= list.size()) {
            HwLog.e("TileAdapter", "list move::from or to is invalid,from=" + from + ",to=" + to + ", list size=" + list);
            return;
        }
        list.add(to, list.remove(from));
        notifyItemMoved(from, to);
    }

    public void updateTileState(QSTile.State state, String spec) {
        if (this.mTiles != null && this.mAllTiles != null && spec != null) {
            for (TileInfo info : this.mTiles) {
                if (info != null && spec.equals(info.spec)) {
                    info.appLabel = state.label;
                    info.state = state;
                    notifyTileStateChanged();
                    return;
                }
            }
            for (TileInfo info2 : this.mAllTiles) {
                if (info2 != null && spec.equals(info2.spec)) {
                    info2.appLabel = state.label;
                    info2.state = state;
                    notifyTileStateChanged();
                    return;
                }
            }
        }
    }

    private void notifyTileStateChanged() {
        this.mTileStateUpdateHandler.removeMessages(1);
        Message msg = new Message();
        msg.what = 1;
        this.mTileStateUpdateHandler.sendMessageDelayed(msg, 100);
    }

    private void onTileStateChanged() {
        notifyDataSetChanged();
    }
}
